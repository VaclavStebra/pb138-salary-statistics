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
    
    /**
     * Stores new age into database. Id for the new age is automatically
     * generated and stored into id attribute of age.
     *
     * @param age age to be created.
     */
    void createAge(Age age);
    
    /**
     * Deletes age from database.
     *
     * @param age age to be deleted from db.
     */
    void deleteAge(Age age);
    
    /**
     * Returns list of all rows in the database.
     *
     * @return list of all ages in database.
     */
    List<Age> findAllAges();
    
    /**
     * Returns age with given id.
     *
     * @param id primary key of requested age.
     * @return age with given id or null if such age does not exist.
     */
    Age findAgeById(Long id);
    
    /**
     * Updates age in database.
     *
     * @param age updated age to be stored into database.
     */
    void updateAge(Age age);
    
    /**
     * Returns age with given parameters.
     *
     * @param ageFrom of requested age.
     * @param ageTo of requested age.
     * @param country country of requested age.
     * @param year year of requested age.
     * @param sex sex of requested age.
     * @return ages with given parameters or null if such age does not exist.
     */
    List<Age> findAgesByParameters(Integer ageFrom, Integer ageTo, String country, String year, String sex);
}
