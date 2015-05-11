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
public interface AgeManager {
    
    void createAge(Age age);
    
    void deleteAge(Age age);
    
    List<Age> findAllAges();
    
    Age findAgeById(Long id);
    
    void updateAge(Age age);
    
    List<Age> findAgesByParameters(Integer ageFrom, Integer ageTo, String country, String year, String sex);
}
