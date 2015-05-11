/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dao;

/**
 *
 * @author Tomas Milota
 */
public class Age {
    private Long id;
    private String country, year, sex;
    private Double averageSalary;
    private Integer ageFrom, ageTo;

    public Age(Long id, String country, String year, String sex, double averageSalary, int ageFrom, int ageTo) {
        this.id = id;
        this.country = country;
        this.year = year;
        this.sex = sex;
        this.averageSalary = averageSalary;
        this.ageFrom = ageFrom;
        this.ageTo = ageTo;
    }

    public Age(Long id, String country, String year, double averageSalary, int ageFrom, int ageTo) {
        this.id = id;
        this.country = country;
        this.year = year;
        this.averageSalary = averageSalary;
        this.ageFrom = ageFrom;
        this.ageTo = ageTo;
        this.sex = null;
    }

    public Age() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Double getAverageSalary() {
        return averageSalary;
    }

    public void setAverageSalary(double averageSalary) {
        this.averageSalary = averageSalary;
    }

    public Integer getAgeFrom() {
        return ageFrom;
    }

    public void setAgeFrom(int ageFrom) {
        this.ageFrom = ageFrom;
    }

    public Integer getAgeTo() {
        return ageTo;
    }

    public void setAgeTo(int ageTo) {
        this.ageTo = ageTo;
    }
    
    
}
