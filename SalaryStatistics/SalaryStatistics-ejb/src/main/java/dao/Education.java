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
public class Education {
    private Long id;
    private String country, year, degree, sex;
    private Double averageSalary;

    public Education(Long id, String degree, String country, String year, Double averageSalary) {
        this.id = id;
        this.country = country;
        this.year = year;
        this.degree = degree;
        this.averageSalary = averageSalary;
    }

    public Education(Long id, String country, String year, String degree, String sex, Double averageSalary) {
        this.id = id;
        this.country = country;
        this.year = year;
        this.degree = degree;
        this.sex = sex;
        this.averageSalary = averageSalary;
    }

    public Education() {}

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
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

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public Double getAverageSalary() {
        return averageSalary;
    }

    public void setAverageSalary(double averageSalary) {
        this.averageSalary = averageSalary;
    }
}
