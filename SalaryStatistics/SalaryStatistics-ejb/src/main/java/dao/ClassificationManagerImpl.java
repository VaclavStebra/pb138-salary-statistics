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
public class ClassificationManagerImpl implements ClassificationManager {
    private WeakHashMap<Long, Classification> cacheId = new WeakHashMap<>();
    
    private static final Logger logger = Logger.getLogger(
            ClassificationManagerImpl.class.getName());

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
    public void createClassification(Classification classification) {
        checkDataSource();
        validate(classification);
        
        if (classification.getId() != null) {
            throw new IllegalEntityException("classification id is already set");
        }
        Connection conn = null;
        PreparedStatement st = null;

        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);

            st = conn.prepareStatement(
                    "INSERT INTO Classification (name, country, year, averageSalary) VALUES (?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            st.setString(1, classification.getName());
            st.setString(2, classification.getCountry());
            st.setString(3, classification.getYear());
            st.setDouble(4, classification.getAverageSalary());
           
            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, classification, true);

            Long id = DBUtils.getId(st.getGeneratedKeys());
            // room continues as "known to system"
            classification.setId(id);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when inserting classification into db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public void deleteClassification(Classification classification) {
        checkDataSource();
        if (classification == null) {
            throw new IllegalArgumentException("classification is null");
        }
        if (classification.getId() == null) {
            throw new IllegalEntityException("classification id is null");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "DELETE FROM Classification WHERE id=?"
            );

            st.setLong(1, classification.getId());
            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, classification, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when deleting classification from the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public List<Classification> findAllClassifications() {
        checkDataSource();
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT id, name, country, year, averageSalary FROM Classification");
            return executeQueryForMultipleClassifications(st);
        } catch (SQLException ex) {
            String msg = "Error when getting all classifications from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }  
    }

    @Override
    public Classification findClassificationById(Long id) {
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
            st = conn.prepareStatement("SELECT id, name, country, year, averageSalary FROM Classification WHERE id = ?");
            st.setLong(1, id);
            
            Classification result = executeQueryForSingleClassification(st);
            cacheId.put(id, result);
            
            return result;
        } catch (SQLException ex) {
            String msg = "Error when getting classification with id = " + id + " from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public void updateClassification(Classification classification) {
        checkDataSource();
        validate(classification);
        
        if (classification.getId() == null) {
            throw new IllegalEntityException("classification id is null");
        }        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);   
            st = conn.prepareStatement(
                    "UPDATE Classification SET name=?, country=?, year=?, averageSalary=? WHERE id=?"
            );
            st.setString(1, classification.getName());
            st.setString(2, classification.getCountry());
            st.setString(3, classification.getYear());
            st.setDouble(4, classification.getAverageSalary());
            st.setLong(5, classification.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, classification, false);
            conn.commit();
            
            cacheId.remove(classification.getId());
        } catch (SQLException ex) {
            String msg = "Error when updating classification in the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }      
    }
    
    @Override
    public List<Classification> findClassificationsByParameters(String name, String country, String year){
        checkDataSource();
        
        StringBuilder statement = new StringBuilder(
                "SELECT id, name, country, year, averageSalary FROM Classification WHERE ");
        
        int numOfParameters = 0;
        
        if(name != null){
            statement.append("name = ");
            statement.append(name);
            numOfParameters++;
        }
        if(country != null){
            if(numOfParameters != 0)
                statement.append(" AND ");
            statement.append("country = ");
            statement.append(country);
            numOfParameters++;
        }
        if(year != null){
            if(numOfParameters != 0)
                statement.append(" AND ");
            statement.append("year = ");
            statement.append(year);
            numOfParameters++;
        }
        if(numOfParameters == 0)
            return findAllClassifications();
        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(statement.toString());
            List<Classification> result = executeQueryForMultipleClassifications(st);
            
            return result;
        } catch (SQLException ex) {
            String msg = "Error when getting classifications with parameters from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    private void validate(Classification classification) {
        if (classification == null) {
            throw new IllegalArgumentException("classification is null");
        }
        if (classification.getName() == null) {
            throw new ValidationException("name is null");
        }
        if (classification.getCountry() == null) {
            throw new ValidationException("country is null");
        }
        if (classification.getYear() == null) {
            throw new ValidationException("year is null");
        }
        if (classification.getAverageSalary() == null) {
            throw new ValidationException("average salary is null");
        }
    }

    private List<Classification> executeQueryForMultipleClassifications(PreparedStatement st) throws SQLException {
        ResultSet rs = st.executeQuery();
        List<Classification> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowToClassification(rs));
        }
        return result;    
    }

    private Classification rowToClassification(ResultSet rs) throws SQLException {
        Classification result = new Classification();
        result.setId(rs.getLong("id"));
        result.setName(rs.getString("name"));
        result.setCountry(rs.getString("country"));
        result.setYear(rs.getString("year"));
        result.setAverageSalary(rs.getDouble("averageSalary"));
        return result;
    }

    private Classification executeQueryForSingleClassification(PreparedStatement st) throws SQLException {
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            Classification result = rowToClassification(rs);                
            if (rs.next()) {
                throw new ServiceFailureException(
                        "Internal integrity error: more classifications with the same id found!");
            }
            return result;
        } else {
            return null;
        }
    }
    
}
