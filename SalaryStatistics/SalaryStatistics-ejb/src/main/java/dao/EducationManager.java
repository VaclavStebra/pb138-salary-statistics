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
public interface EducationManager {
    
    void createEducation(Education education);
    
    void deleteEducation(Education education);
    
    List<Education> findAllEducations();
    
    Education findEducationById(Long id);
    
    void updateEducation(Education education);
    
    List<Education> findEducationsByParameters(String degree, String country, String year, String sex);
    
}
