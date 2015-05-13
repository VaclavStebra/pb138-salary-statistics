/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dao;

import common.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 *
 * @author Tomas Milota
 */
public class AgeManagerImpl implements AgeManager {
    private WeakHashMap<Long, Age> cacheId = new WeakHashMap<>();
    
    private static final Logger logger = Logger.getLogger(
            AgeManagerImpl.class.getName());

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }

    @Override
    public void createAge(Age age) {
        checkDataSource();
        validate(age);
        
        if (age.getId() != null) {
            throw new IllegalEntityException("age id is already set");
        }
        Connection conn = null;
        PreparedStatement st = null;

        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);

            st = conn.prepareStatement(
                    "INSERT INTO \"Age\" (\"ageFrom\", \"ageTo\", \"country\", \"year\", \"sex\", \"averageSalary\") VALUES (?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            st.setInt(1, age.getAgeFrom());
            st.setInt(2, age.getAgeTo());
            st.setString(3, age.getCountry());
            st.setString(4, age.getYear());
            st.setString(5, age.getSex());
            st.setDouble(6, age.getAverageSalary());
           
            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, age, true);

            Long id = DBUtils.getId(st.getGeneratedKeys());
            // room continues as "known to system"
            age.setId(id);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when inserting age into db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public void deleteAge(Age age) {
        checkDataSource();
        if (age == null) {
            throw new IllegalArgumentException("age is null");
        }
        if (age.getId() == null) {
            throw new IllegalEntityException("age id is null");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "DELETE FROM \"Age\" WHERE \"id\"=?"
            );

            st.setLong(1, age.getId());
            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, age, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when deleting age from the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public List<Age> findAllAges() {
        checkDataSource();
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT \"id\", \"ageFrom\", \"ageTo\", \"country\", \"year\", \"sex\", \"averageSalary\" FROM \"Age\"");
            return executeQueryForMultipleAges(st);
        } catch (SQLException ex) {
            String msg = "Error when getting all ages from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }  
    }

    @Override
    public Age findAgeById(Long id) {
        if(cacheId.containsKey(id)){
            return cacheId.get(id);
        }
        
        checkDataSource();
        
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement("SELECT \"id\", \"ageFrom\", \"ageTo\", \"country\", \"year\", \"sex\", \"averageSalary\""
                    + " FROM \"Age\" WHERE \"id\" = ?");
            st.setLong(1, id);
            
            Age result = executeQueryForSingleAge(st);
            cacheId.put(id, result);
            
            return result;
        } catch (SQLException ex) {
            String msg = "Error when getting age with id = " + id + " from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public void updateAge(Age age) {
        checkDataSource();
        validate(age);
        
        if (age.getId() == null) {
            throw new IllegalEntityException("age id is null");
        }        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);   
            st = conn.prepareStatement(
                    "UPDATE \"Age\" SET \"ageFrom\"=?, \"ageTo\"=?, \"country\"=?, \"year\"=?, \"sex\"=?, \"averageSalary\"=? WHERE \"id\"=?"
            );
            st.setInt(1, age.getAgeFrom());
            st.setInt(2, age.getAgeTo());
            st.setString(3, age.getCountry());
            st.setString(4, age.getYear());
            st.setString(5, age.getSex());
            st.setDouble(6, age.getAverageSalary());
            st.setLong(7, age.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, age, false);
            conn.commit();
            
            cacheId.remove(age.getId());
        } catch (SQLException ex) {
            String msg = "Error when updating age in the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }      
    }
    
    @Override
    public List<Age> findAgesByParameters(Integer ageFrom, Integer ageTo, String country, String year, String sex){
        checkDataSource();
        
        StringBuilder statement = new StringBuilder(
                "SELECT \"id\", \"ageFrom\", \"ageTo\", \"country\", \"year\", \"sex\", \"averageSalary\" FROM \"Age\" WHERE ");
        
        int numOfParameters = 0;
        
        if(ageFrom != null){
            statement.append("\"ageFrom\" = ");
            statement.append(ageFrom);
            numOfParameters++;
        }
        if(ageTo != null){
            if(numOfParameters != 0)
                statement.append(" AND ");
            statement.append("\"ageTo\" = ");
            statement.append(ageTo);
            numOfParameters++;
        }
        if(country != null){
            if(numOfParameters != 0)
                statement.append(" AND ");
            statement.append("\"country\" = ");
            statement.append(country);
            numOfParameters++;
        }
        if(year != null){
            if(numOfParameters != 0)
                statement.append(" AND ");
            statement.append("\"year\" = ");
            statement.append(year);
            numOfParameters++;
        }
        if(sex != null){
            if(numOfParameters != 0)
                statement.append(" AND ");
            statement.append("\"sex\" = ");
            statement.append(sex);
            numOfParameters++;
        }
        if(numOfParameters == 0)
            return findAllAges();
        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(statement.toString());
            List<Age> result = executeQueryForMultipleAges(st);
            
            return result;
        } catch (SQLException ex) {
            String msg = "Error when getting age with parameters from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    private void validate(Age age) {
        if (age == null) {
            throw new IllegalArgumentException("age is null");
        }
        if (age.getAgeFrom()== null) {
            throw new ValidationException("ageFrom is null");
        }
        if (age.getAgeTo()== null) {
            throw new ValidationException("ageTo is null");
        }
        if (age.getCountry() == null) {
            throw new ValidationException("country is null");
        }
        if (age.getYear() == null) {
            throw new ValidationException("year is null");
        }
        if (age.getAverageSalary() == null) {
            throw new ValidationException("average salary is null");
        }
    }

    private List<Age> executeQueryForMultipleAges(PreparedStatement st) throws SQLException {
        ResultSet rs = st.executeQuery();
        List<Age> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowToAge(rs));
        }
        return result;    
    }

    private Age rowToAge(ResultSet rs) throws SQLException {
        Age result = new Age();
        result.setId(rs.getLong("id"));
        result.setAgeFrom(rs.getInt("ageFrom"));
        result.setAgeTo(rs.getInt("ageTo"));
        result.setCountry(rs.getString("country"));
        result.setYear(rs.getString("year"));
        result.setSex(rs.getString("sex"));
        result.setAverageSalary(rs.getDouble("averageSalary"));
        return result;
    }

    private Age executeQueryForSingleAge(PreparedStatement st) throws SQLException {
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            Age result = rowToAge(rs);                
            if (rs.next()) {
                throw new ServiceFailureException(
                        "Internal integrity error: more ages with the same id found!");
            }
            return result;
        } else {
            return null;
        }
    }
    
}
