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
public class RegionManagerImpl implements RegionManager {
    private WeakHashMap<Long, Region> cacheId = new WeakHashMap<>();
    
    private static final Logger logger = Logger.getLogger(
            RegionManagerImpl.class.getName());

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
     * Stores new region into database. Id for the new region is automatically
     * generated and stored into id attribute of region.
     *
     * @param region age to be created.
     */
    @Override
    public void createRegion(Region region) {
        checkDataSource();
        validate(region);
        
        if (region.getId() != null) {
            throw new IllegalEntityException("region id is already set");
        }
        Connection conn = null;
        PreparedStatement st = null;

        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);

            st = conn.prepareStatement(
                    "INSERT INTO \"Region\" (\"name\", \"country\", \"year\", \"sex\", \"averageSalary\") VALUES (?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            st.setString(1, region.getName());
            st.setString(2, region.getCountry());
            st.setString(3, region.getYear());
            st.setString(4, region.getSex());
            st.setDouble(5, region.getAverageSalary());
           
            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, region, true);

            Long id = DBUtils.getId(st.getGeneratedKeys());
            // room continues as "known to system"
            region.setId(id);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when inserting region into db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

     /**
     * Deletes region from database.
     *
     * @param region age to be deleted from db.
     */
    @Override
    public void deleteRegion(Region region) {
        checkDataSource();
        if (region == null) {
            throw new IllegalArgumentException("region is null");
        }
        if (region.getId() == null) {
            throw new IllegalEntityException("region id is null");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "DELETE FROM \"Region\" WHERE \"id\"=?"
            );

            st.setLong(1, region.getId());
            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, region, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when deleting region from the db";
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
     * @return list of all regions in database.
     */
    @Override
    public List<Region> findAllRegions() {
        checkDataSource();
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT \"id\", \"name\", \"country\", \"year\", \"sex\", \"averageSalary\" FROM \"Region\"");
            return executeQueryForMultipleRegions(st);
        } catch (SQLException ex) {
            String msg = "Error when getting all regions from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }  
    }

    /**
     * Returns region with given id.
     *
     * @param id primary key of requested region.
     * @return region with given id or null if such region does not exist.
     */
    @Override
    public Region findRegionById(Long id) {
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
            st = conn.prepareStatement("SELECT \"id\", \"name\", \"country\", \"year\", \"sex\", \"averageSalary\""
                    + " FROM \"Region\" WHERE \"id\" = ?");
            st.setLong(1, id);
            
            Region result = executeQueryForSingleRegion(st);
            cacheId.put(id, result);
            
            return result;
        } catch (SQLException ex) {
            String msg = "Error when getting region with id = " + id + " from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    /**
     * Updates region in database.
     *
     * @param region updated region to be stored into database.
     */
    @Override
    public void updateRegion(Region region) {
        checkDataSource();
        validate(region);
        
        if (region.getId() == null) {
            throw new IllegalEntityException("region id is null");
        }        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);   
            st = conn.prepareStatement(
                "UPDATE \"Region\" SET \"name\"=?, \"country\"=?, \"year\"=?, \"sex\"=? ,\"averageSalary\"=? WHERE \"id\"=?"
            );
            st.setString(1, region.getName());
            st.setString(2, region.getCountry());
            st.setString(3, region.getYear());
            st.setString(4, region.getSex());
            st.setDouble(5, region.getAverageSalary());
            st.setLong(6, region.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, region, false);
            conn.commit();
            
            cacheId.remove(region.getId());
        } catch (SQLException ex) {
            String msg = "Error when updating region in the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }      
    }
    
    /**
     * Returns region with given parameters.
     *
     * @param name of requested region.
     * @param country country of requested region.
     * @param year year of requested region.
     * @param sex sex of requested region.
     * @return regions with given parameters or null if such region does not exist.
     */
    @Override
    public List<Region> findRegionsByParameters(String name, String country, String year, String sex){
        checkDataSource();
        
        StringBuilder statement = new StringBuilder(
                "SELECT \"id\", \"name\", \"country\", \"year\", \"sex\", \"averageSalary\" FROM \"Region\" WHERE ");
        
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
        if(sex != null){
            if(numOfParameters != 0)
                statement.append(" AND ");
            statement.append("\"sex\" = ");
            statement.append(sex);
            numOfParameters++;
        }
        if(numOfParameters == 0)
            return findAllRegions();
        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(statement.toString());
            List<Region> result = executeQueryForMultipleRegions(st);
            
            return result;
        } catch (SQLException ex) {
            String msg = "Error when getting regions with parameters from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    private void validate(Region region) {
        if (region == null) {
            throw new IllegalArgumentException("region is null");
        }
        if (region.getName() == null) {
            throw new ValidationException("name is null");
        }
        if (region.getCountry() == null) {
            throw new ValidationException("country is null");
        }
        if (region.getYear() == null) {
            throw new ValidationException("year is null");
        }
        if (region.getAverageSalary() == null) {
            throw new ValidationException("average salary is null");
        }
    }

    private List<Region> executeQueryForMultipleRegions(PreparedStatement st) throws SQLException {
        ResultSet rs = st.executeQuery();
        List<Region> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowToRegion(rs));
        }
        return result;    
    }

    private Region rowToRegion(ResultSet rs) throws SQLException {
        Region result = new Region();
        result.setId(rs.getLong("id"));
        result.setName(rs.getString("name"));
        result.setCountry(rs.getString("country"));
        result.setYear(rs.getString("year"));
        result.setSex(rs.getString("sex"));
        result.setAverageSalary(rs.getDouble("averageSalary"));
        return result;
    }

    private Region executeQueryForSingleRegion(PreparedStatement st) throws SQLException {
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            Region result = rowToRegion(rs);                
            if (rs.next()) {
                throw new ServiceFailureException(
                        "Internal integrity error: more regions with the same id found!");
            }
            return result;
        } else {
            return null;
        }
    }
    
}
