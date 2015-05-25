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

    /**
     * Stores new classification into database. Id for the new classification is automatically
     * generated and stored into id attribute of classification.
     *
     * @param classification classification to be created.
     */
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
                    "INSERT INTO \"Classification\" (\"code\", \"name\", \"country\", \"year\", \"averageSalary\") VALUES (?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            st.setString(1, classification.getCode());
            st.setString(2, classification.getName());
            st.setString(3, classification.getCountry());
            st.setString(4, classification.getYear());
            st.setDouble(5, classification.getAverageSalary());
           
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

    /**
     * Deletes classification from database.
     *
     * @param classification classification to be deleted from db.
     */
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
                    "DELETE FROM \"Classification\" WHERE \"id\"=?"
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

    /**
     * Returns list of all rows in the database.
     *
     * @return list of all classifications in database.
     */
    @Override
    public List<Classification> findAllClassifications() {
        checkDataSource();
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT \"id\", \"code\", \"name\", \"country\", \"year\", \"averageSalary\" FROM \"Classification\"");
            return executeQueryForMultipleClassifications(st);
        } catch (SQLException ex) {
            String msg = "Error when getting all classifications from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }  
    }

    /**
     * Returns classification with given id.
     *
     * @param id primary key of requested age.
     * @return classification with given id or null if such classification does not exist.
     */
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
            st = conn.prepareStatement("SELECT \"id\", \"code\", \"name\", \"country\", \"year\", \"averageSalary\" "
                    + "FROM \"Classification\" WHERE \"id\" = ?");
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

    /**
     * Updates classification in database.
     *
     * @param classification updated classification to be stored into database.
     */
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
                "UPDATE \"Classification\" SET \"name\"=?, \"country\"=?, \"year\"=?, \"averageSalary\"=?, \"code\"=? WHERE \"id\"=?"
            );
            st.setString(1, classification.getName());
            st.setString(2, classification.getCountry());
            st.setString(3, classification.getYear());
            st.setDouble(4, classification.getAverageSalary());
            st.setString(5, classification.getCode());
            st.setLong(6, classification.getId());

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
    
    /**
     * Returns classifications with given parameters.
     *
     * @param name of requested age.
     * @param country country of requested age.
     * @param year year of requested age.
     * @return classifications with given parameters or null if such classification does not exist.
     */
    @Override
    public List<Classification> findClassificationsByParameters(String name, String country, String year){
        checkDataSource();
        
        StringBuilder statement = new StringBuilder(
                "SELECT \"id\", \"code\", \"name\", \"country\", \"year\", \"averageSalary\" FROM \"Classification\" WHERE ");
        
        int numOfParameters = 0;
        
        if(name != null){
            statement.append("\"name\" = ");
            statement.append(name);
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
        if (classification.getCode() == null) {
            throw new ValidationException("code is null");
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
        result.setCode(rs.getString("code"));
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
