/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dao;

import java.util.List;

/**
 *
 * @author Tomas Milota
 */
public interface SectorManager {
    
    void createSector(Sector sector);
    
    void deleteSector(Sector sector);
    
    List<Sector> findAllSectors();
    
    Sector findSectorById(Long id);
    
    void updateSector(Sector sector);
    
    List<Sector> findSectorsByName(String name);
}
