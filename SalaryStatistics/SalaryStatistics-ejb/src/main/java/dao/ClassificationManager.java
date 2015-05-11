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
public interface ClassificationManager {
    void createClassification(Classification classification);
    
    void deleteClassification(Classification classification);
    
    List<Classification> findAllClassifications();
    
    Classification findClassificationById(Long id);
    
    void updateClassification(Classification classification);
    
    List<Classification> findClassificationsByParameters(String name, String country, String year);
}
