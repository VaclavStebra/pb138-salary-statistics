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
public interface RegionManager {
    
    void createRegion(Region region);
    
    void deleteRegion(Region region);
    
    List<Region> findAllRegions();
    
    Region findRegionById(Long id);
    
    void updateRegion(Region region);
    
    List<Region> findRegionsByParameters(String name, String country, String year, String sex);
}
