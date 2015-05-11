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
public class Region {
    private Long id;
    private String country, year, name, sex;
    private Double averageSalary;

    public Region(Long id, String name, String country, String year, double averageSalary) {
        this.id = id;
        this.country = country;
        this.year = year;
        this.name = name;
        this.averageSalary = averageSalary;
    }

    public Region(Long id, String country, String year, String name, String sex, Double averageSalary) {
        this.id = id;
        this.country = country;
        this.year = year;
        this.name = name;
        this.sex = sex;
        this.averageSalary = averageSalary;
    }

    public Region() {}

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setAverageSalary(Double averageSalary) {
        this.averageSalary = averageSalary;
    }
    
}
