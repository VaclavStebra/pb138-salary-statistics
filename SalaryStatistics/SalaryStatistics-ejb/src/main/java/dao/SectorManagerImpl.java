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
public class SectorManagerImpl implements SectorManager {
    
    private WeakHashMap<Long, Sector> cacheId = new WeakHashMap<>();
    private WeakHashMap<String, List<Sector>> cacheName = new WeakHashMap<>();
    
    private static final Logger logger = Logger.getLogger(
            SectorManagerImpl.class.getName());

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
    public void createSector(Sector sector) {
        checkDataSource();
        validate(sector);
        
        if (sector.getId() != null) {
            throw new IllegalEntityException("guest id is already set");
        }
        Connection conn = null;
        PreparedStatement st = null;

        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);

            st = conn.prepareStatement(
                    "INSERT INTO Sector (name, country, year, averageSalary) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            st.setString(1, sector.getName());
            st.setString(2, sector.getCountry());
            st.setString(3, sector.getYear());
            st.setInt(4, sector.getAverageSalary());
           
            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, sector, true);

            Long id = DBUtils.getId(st.getGeneratedKeys());
            // room continues as "known to system"
            sector.setId(id);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when inserting grave into db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public void deleteSector(Sector sector) {
        checkDataSource();
        if (sector == null) {
            throw new IllegalArgumentException("sector is null");
        }
        if (sector.getId() == null) {
            throw new IllegalEntityException("sector id is null");
        }
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);
            st = conn.prepareStatement(
                    "DELETE FROM Sector WHERE id=?"
            );

            st.setLong(1, sector.getId());
            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, sector, false);
            conn.commit();
        } catch (SQLException ex) {
            String msg = "Error when deleting sector from the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public List<Sector> findAllSectors() {
        checkDataSource();
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT \"id\", \"name\", \"country\", \"year\", \"averageSalary\" FROM \"Sector\"");
            return executeQueryForMultipleSectors(st);
        } catch (SQLException ex) {
            String msg = "Error when getting all sectors from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }  
    }

    @Override
    public Sector findSectorById(Long id) {
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
            st = conn.prepareStatement("SELECT id, name, country, year, averageSalary FROM Sector WHERE id = ?");
            st.setLong(1, id);
            
            Sector result = executeQueryForSingleSector(st);
            cacheId.put(id, result);
            
            return result;
        } catch (SQLException ex) {
            String msg = "Error when getting sector with id = " + id + " from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    @Override
    public void updateSector(Sector sector) {
        checkDataSource();
        validate(sector);
        
        if (sector.getId() == null) {
            throw new IllegalEntityException("sector id is null");
        }        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            // Temporary turn autocommit mode off. It is turned back on in 
            // method DBUtils.closeQuietly(...) 
            conn.setAutoCommit(false);   
            st = conn.prepareStatement(
                    "UPDATE Sector SET name=?, country=?, year=?, averageSalary=? WHERE id=?"
            );
            st.setString(1, sector.getName());
            st.setString(2, sector.getCountry());
            st.setString(3, sector.getYear());
            st.setInt(4, sector.getAverageSalary());
            st.setLong(5, sector.getId());

            int count = st.executeUpdate();
            DBUtils.checkUpdatesCount(count, sector, false);
            conn.commit();
            
            cacheId.remove(sector.getId());
            cacheName.remove(sector.getName());
        } catch (SQLException ex) {
            String msg = "Error when updating sector in the db";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.doRollbackQuietly(conn);
            DBUtils.closeQuietly(conn, st);
        }      
    }

    @Override
    public List<Sector> findSectorsByName(String name) {
        if(cacheName.containsKey(name))
            return cacheName.get(name);
        
        checkDataSource();
        
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }
        
        Connection conn = null;
        PreparedStatement st = null;
        try {
            conn = dataSource.getConnection();
            st = conn.prepareStatement(
                    "SELECT id, name, country, year, averageSalary FROM Sector WHERE name = ?");
            st.setString(1, name);
            List<Sector> result = executeQueryForMultipleSectors(st);
            
            cacheName.put(name, result);
            return result;
        } catch (SQLException ex) {
            String msg = "Error when getting sector with name = " + name + " from DB";
            logger.log(Level.SEVERE, msg, ex);
            throw new ServiceFailureException(msg, ex);
        } finally {
            DBUtils.closeQuietly(conn, st);
        }
    }

    private void validate(Sector sector) {
        if (sector == null) {
            throw new IllegalArgumentException("guest is null");
        }
        if (sector.getName() == null) {
            throw new ValidationException("name is null");
        }
        if (sector.getCountry() == null) {
            throw new ValidationException("country is null");
        }
        if (sector.getYear() == null) {
            throw new ValidationException("year is null");
        }
        if (sector.getAverageSalary() == null) {
            throw new ValidationException("average salary is null");
        }
    }

    private List<Sector> executeQueryForMultipleSectors(PreparedStatement st) throws SQLException {
        ResultSet rs = st.executeQuery();
        List<Sector> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowToSector(rs));
        }
        return result;    
    }

    private Sector rowToSector(ResultSet rs) throws SQLException {
        Sector result = new Sector();
        result.setId(rs.getLong("id"));
        result.setName(rs.getString("name"));
        result.setCountry(rs.getString("country"));
        result.setYear(rs.getString("year"));
        result.setAverageSalary(rs.getInt("averageSalary"));
        return result;
    }

    private Sector executeQueryForSingleSector(PreparedStatement st) throws SQLException {
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
            Sector result = rowToSector(rs);                
            if (rs.next()) {
                throw new ServiceFailureException(
                        "Internal integrity error: more sectors with the same id found!");
            }
            return result;
        } else {
            return null;
        }
    }
    
}
