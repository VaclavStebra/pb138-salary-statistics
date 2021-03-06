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
public class EducationManagerImpl implements EducationManager {
    private WeakHashMap<Long, Education> cacheId = new WeakHashMap<>();
    
    private static final Logger logger = Logger.getLogger(
            EducationManagerImpl.class.getName());

    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private void checkDataSource() {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not set");
        }
    }

    /**
     * Stores new education into database. Id for the new education is automatically
     * generated and stored into id attribute of education.
     *
     * @param education education to be created.
     */
    @Override
    public void createEducation(Education education) {
        checkDataSource();
        validate(education);
        
        if (education.getId() != null) {
            throw new IllegalEntityException("education id is already set");
        }
        Connection conn = null;
        PreparedStatement st = null;

        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);

            st = conn.prepareStatement(
                    "INSERT INTO \"Education\" (\"degree\", \"country\", \"year\", \"sex\", \"averageSalary\") VALUES (?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            st.setString(1, education.getDegree());
            st.setString(2, education.getCountry());
            st.setString(3, education.getYear());
            st.setString(4, education.getSex());
            st.setDouble(5, education.getAverageSalary());
           
            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, education, true);

            Long id = DBUtils.getId(st.getGeneratedKeys());
            // room continues as "known to system"
            education.setId(id);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when inserting education into db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

    /**
     * Deletes education from database.
     *
     * @param education education to be deleted from db.
     */
    @Override
    public void deleteEducation(Education education) {
        checkDataSource();
        if (education == null) {
            throw new IllegalArgumentException("education is null");
        }
        if (education.getId() == null) {
            throw new IllegalEntityException("education id is null");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "DELETE FROM \"Education\" WHERE \"id\"=?"
            );

            st.setLong(1, education.getId());
            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, education, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when deleting education from the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

    /**
     * Returns list of all rows in the database.
     *
     * @return list of all educations in database.
     */
    @Override
    public List<Education> findAllEducations() {
        checkDataSource();
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT \"id\", \"degree\", \"country\", \"year\", \"sex\", \"averageSalary\" FROM \"Education\"");
            return executeQueryForMultipleEducations(st);
        } catch (SQLException ex) {
            String msg = "Error when getting all educations from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }  
    }

    /**
     * Returns education with given id.
     *
     * @param id primary key of requested education.
     * @return education with given id or null if such education does not exist.
     */
    @Override
    public Education findEducationById(Long id) {
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
            st = conn.prepareStatement("SELECT \"id\", \"degree\", \"country\", \"year\", \"sex\", \"averageSalary\" "
                    + "FROM \"Education\" WHERE \"id\" = ?");
            st.setLong(1, id);
            
            Education result = executeQueryForSingleEducation(st);
            cacheId.put(id, result);
            
            return result;
        } catch (SQLException ex) {
            String msg = "Error when getting education with id = " + id + " from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    /**
     * Updates education in database.
     *
     * @param education updated education to be stored into database.
     */
    @Override
    public void updateEducation(Education education) {
        checkDataSource();
        validate(education);
        
        if (education.getId() == null) {
            throw new IllegalEntityException("education id is null");
        }        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);   
            st = conn.prepareStatement(
                    "UPDATE \"Education\" SET \"degree\"=?, \"country\"=?, \"year\"=?, \"sex\"=? \"averageSalary\"=? WHERE \"id\"=?"
            );
            st.setString(1, education.getDegree());
            st.setString(2, education.getCountry());
            st.setString(3, education.getYear());
            st.setString(4, education.getSex());
            st.setDouble(5, education.getAverageSalary());
            st.setLong(6, education.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, education, false);
            conn.commit();
            
            cacheId.remove(education.getId());
        } catch (SQLException ex) {
            String msg = "Error when updating education in the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }      
    }
    
    /**
     * Returns education with given parameters.
     *
     * @param degree of requested education.
     * @param country country of requested education.
     * @param year year of requested age.
     * @param sex sex of requested age.
     * @return educations with given parameters or null if such education does not exist.
     */
    @Override
    public List<Education> findEducationsByParameters(String degree, String country, String year, String sex){
        checkDataSource();
        
        StringBuilder statement = new StringBuilder(
                "SELECT \"id\", \"degree\", \"country\", \"year\", \"sex\", \"averageSalary\" FROM \"Education\" WHERE ");
        
        int numOfParameters = 0;
        
        if(degree != null){
            statement.append("\"degree\"= ");
            statement.append(degree);
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
            return findAllEducations();
        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(statement.toString());
            List<Education> result = executeQueryForMultipleEducations(st);
            
            return result;
        } catch (SQLException ex) {
            String msg = "Error when getting educations with parameters from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    private void validate(Education education) {
        if (education == null) {
            throw new IllegalArgumentException("education is null");
        }
        if (education.getDegree() == null) {
            throw new ValidationException("degree is null");
        }
        if (education.getCountry() == null) {
            throw new ValidationException("country is null");
        }
        if (education.getYear() == null) {
            throw new ValidationException("year is null");
        }
        if (education.getAverageSalary() == null) {
            throw new ValidationException("average salary is null");
        }
    }

    private List<Education> executeQueryForMultipleEducations(PreparedStatement st) throws SQLException {
        ResultSet rs = st.executeQuery();
        List<Education> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowToEducation(rs));
        }
        return result;    
    }

    private Education rowToEducation(ResultSet rs) throws SQLException {
        Education result = new Education();
        result.setId(rs.getLong("id"));
        result.setDegree(rs.getString("degree"));
        result.setCountry(rs.getString("country"));
        result.setYear(rs.getString("year"));
        result.setSex(rs.getString("sex"));
        result.setAverageSalary(rs.getDouble("averageSalary"));
        return result;
    }

    private Education executeQueryForSingleEducation(PreparedStatement st) throws SQLException {
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            Education result = rowToEducation(rs);                
            if (rs.next()) {
                throw new ServiceFailureException(
                        "Internal integrity error: more educations with the same id found!");
            }
            return result;
        } else {
            return null;
        }
    }
    
}
