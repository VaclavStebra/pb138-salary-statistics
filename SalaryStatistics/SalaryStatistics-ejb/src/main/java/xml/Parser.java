/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xml;

import dao.Age;
import dao.AgeManagerImpl;
import dao.Classification;
import dao.ClassificationManagerImpl;
import dao.Education;
import dao.EducationManagerImpl;
import dao.Sector;
import dao.SectorManagerImpl;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author Tomas Milota
 */
public class Parser {

    public void parseSectorSk(SectorManagerImpl manager) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, ParseException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse("src/main/java/xml/SectorSk.xml");

        NodeList sectorList = doc.getElementsByTagName("PRAC_mzdyNace");
        for (int i = 0; i < sectorList.getLength(); i++) {
            Element sectorNode = (Element) sectorList.item(i);

            String name = sectorNode.getElementsByTagName("UKAZ2").item(0).getTextContent();
            String[] splited = name.split(" ", 2);

            NodeList years = sectorNode.getChildNodes();

            for (int j = 0; j < years.getLength(); j++) {
                if (years.item(j).getNodeType() == Node.TEXT_NODE) {
                    continue;
                }
                Element yearNode = (Element) years.item(j);
                if("UKAZ2".equals(yearNode.getNodeName()))
                    continue;

                String year = yearNode.getNodeName().substring(1);
                if (".".equals(yearNode.getTextContent())) {
                    continue;
                }
                String salaryStr = yearNode.getTextContent().replaceAll(" ", "");
                NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
                Number number = format.parse(salaryStr);
                double salaryDouble = number.doubleValue();
                
                Sector sector = new Sector();
                sector.setCode(splited[0]);
                sector.setName(splited[1]);
                sector.setCountry("sk");
                sector.setYear(year);
                sector.setAverageSalary(salaryDouble);

                manager.createSector(sector);
            }
        }
    }
    
    public void parseClassificationSk(ClassificationManagerImpl manager) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, ParseException {
        Map<String, String> nameMap = new HashMap<>();
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        
        // file with statistics 2012 - 2013
        Document doc = builder.parse("src/main/java/xml/Classification1Sk.xml");
        
        NodeList classificationList = doc.getElementsByTagName("PRAC_strMzdyZamIsco");
        for (int i = 0; i < classificationList.getLength(); i++) {
            Element classificationNode = (Element) classificationList.item(i);

            String name = classificationNode.getElementsByTagName("UKAZ2").item(0).getTextContent();
            String[] splited = name.split(" ", 2);
            if(! nameMap.containsKey(splited[0]))
                nameMap.put(splited[0], splited[1]);
            
            NodeList years = classificationNode.getChildNodes();

            for (int j = 0; j < years.getLength(); j++) {
                if (years.item(j).getNodeType() == Node.TEXT_NODE) {
                    continue;
                }
                Element yearNode = (Element) years.item(j);
                if("MJ".equals(yearNode.getNodeName()) || "UKAZ".equals(yearNode.getNodeName().substring(0, 4)))
                    continue;

                String year = yearNode.getNodeName().substring(1);
                if (".".equals(yearNode.getTextContent()) || "".equals(yearNode.getTextContent())) {
                    continue;
                }
                String salaryStr = yearNode.getTextContent().replaceAll(" ", "");
                NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
                Number number = format.parse(salaryStr);
                double salaryDouble = number.doubleValue();
                
                Classification classification = new Classification();
                classification.setCode(splited[0]);
                classification.setName(splited[1]);
                classification.setCountry("sk");
                classification.setYear(year);
                classification.setAverageSalary(salaryDouble);

                manager.createClassification(classification);
            }
        }
        
        //file with statistics 1998, 2009 - 2011
        doc = builder.parse("src/main/java/xml/Classification2Sk.xml");
        classificationList = doc.getElementsByTagName("PRAC_strMzdyZam");
        for (int i = 0; i < classificationList.getLength(); i++) {
            Element classificationNode = (Element) classificationList.item(i);
            
            if(! "EUR".equals(classificationNode.getElementsByTagName("MJ").item(0).getTextContent()))
                continue;

            String name = classificationNode.getElementsByTagName("UKAZ2").item(0).getTextContent();
            String[] splited = name.split(" ", 2);
            
            NodeList years = classificationNode.getChildNodes();

            for (int j = 0; j < years.getLength(); j++) {
                if (years.item(j).getNodeType() == Node.TEXT_NODE) {
                    continue;
                }
                Element yearNode = (Element) years.item(j);
                if("MJ".equals(yearNode.getNodeName()) || "UKAZ".equals(yearNode.getNodeName().substring(0, 4)))
                    continue;

                String year = yearNode.getNodeName().substring(1);
                if (".".equals(yearNode.getTextContent()) || "".equals(yearNode.getTextContent())) {
                    continue;
                }
                String salaryStr = yearNode.getTextContent().replaceAll(" ", "");
                NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
                Number number = format.parse(salaryStr);
                double salaryDouble = number.doubleValue();
                
                Classification classification = new Classification();
                classification.setCode(splited[0]);
                classification.setName(nameMap.get(splited[0]));
                classification.setCountry("sk");
                classification.setYear(year);
                classification.setAverageSalary(salaryDouble);

                manager.createClassification(classification);
            }
        }
    }
    
    public void parseEducationSk(EducationManagerImpl manager) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, ParseException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse("src/main/java/xml/EducationSk.xml");

        NodeList educationList = doc.getElementsByTagName("PRAC_strMzdyVzdelanie");
        for (int i = 0; i < educationList.getLength(); i++) {
            Element educationNode = (Element) educationList.item(i);
            
            if(! "EUR".equals(educationNode.getElementsByTagName("MJ").item(0).getTextContent()))
                continue;

            String name = educationNode.getElementsByTagName("UKAZ2").item(0).getTextContent();
            String sex = educationNode.getElementsByTagName("UKAZ1").item(0).getTextContent();
            String[] splited = sex.split(" ");
            sex = splited[splited.length - 1];
            
            NodeList years = educationNode.getChildNodes();

            for (int j = 0; j < years.getLength(); j++) {
                if (years.item(j).getNodeType() == Node.TEXT_NODE) {
                    continue;
                }
                Element yearNode = (Element) years.item(j);
                if("MJ".equals(yearNode.getNodeName()) || "UKAZ".equals(yearNode.getNodeName().substring(0, 4)))
                    continue;

                String year = yearNode.getNodeName().substring(1);
                if (".".equals(yearNode.getTextContent()) || "".equals(yearNode.getTextContent())) {
                    continue;
                }
                String salaryStr = yearNode.getTextContent().replaceAll(" ", "");
                NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
                Number number = format.parse(salaryStr);
                double salaryDouble = number.doubleValue();
                
                Education education = new Education();
                education.setDegree(name);
                education.setCountry("sk");
                education.setYear(year);
                education.setAverageSalary(salaryDouble);
                if(!"spolu".equals(sex))
                    education.setSex(sex);

                manager.createEducation(education);
            }
        }
    }
    
    public void parseAgeSk(AgeManagerImpl manager) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException, ParseException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse("src/main/java/xml/AgeSk.xml");

        NodeList educationList = doc.getElementsByTagName("PRAC_strMzdyVek");
        for (int i = 0; i < educationList.getLength(); i++) {
            Element educationNode = (Element) educationList.item(i);
            
            if(! "EUR".equals(educationNode.getElementsByTagName("MJ").item(0).getTextContent()))
                continue;

            String ageStr = educationNode.getElementsByTagName("UKAZ2").item(0).getTextContent();
            Scanner in = new Scanner(ageStr).useDelimiter("[^0-9]+");
            Integer age1 = in.nextInt();
            Integer age2 = null;
            if(in.hasNext())
                age2 = in.nextInt();
            
            String sex = educationNode.getElementsByTagName("UKAZ1").item(0).getTextContent();
            String[] splited = sex.split(" ");
            sex = splited[splited.length - 1];
            
            NodeList years = educationNode.getChildNodes();

            for (int j = 0; j < years.getLength(); j++) {
                if (years.item(j).getNodeType() == Node.TEXT_NODE) {
                    continue;
                }
                Element yearNode = (Element) years.item(j);
                if("MJ".equals(yearNode.getNodeName()) || "UKAZ".equals(yearNode.getNodeName().substring(0, 4)))
                    continue;

                String year = yearNode.getNodeName().substring(1);
                if (".".equals(yearNode.getTextContent()) || "".equals(yearNode.getTextContent())) {
                    continue;
                }
                String salaryStr = yearNode.getTextContent().replaceAll(" ", "");
                NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
                Number number = format.parse(salaryStr);
                double salaryDouble = number.doubleValue();
                
                Age age = new Age();
                
                if(age2 == null){
                    if(age1 == 19){
                        age1 = 0;
                        age2 = 19;
                    }
                    if(age1 == 60)
                        age2 = 99;
                }
                age.setAgeFrom(age1);
                age.setAgeTo(age2);
                age.setCountry("sk");
                age.setYear(year);
                age.setAverageSalary(salaryDouble);
                if(!"spolu".equals(sex))
                    age.setSex(sex);

                manager.createAge(age);
            }
        }
    }

    public static void main(String[] args) throws IOException, SQLException, ParserConfigurationException, SAXException, XPathExpressionException, ParseException {
        Properties pro = new Properties();
        pro.load(new FileInputStream("src/main/java/configuration/jdbc.properties"));

        BasicDataSource ds = new BasicDataSource();
        ds.setUrl(pro.getProperty("url"));
        ds.setUsername(pro.getProperty("username"));
        ds.setPassword(pro.getProperty("password"));

        SectorManagerImpl sectorManager = new SectorManagerImpl();
        sectorManager.setDataSource(ds);
        ClassificationManagerImpl classificationManager = new ClassificationManagerImpl();
        classificationManager.setDataSource(ds);
        EducationManagerImpl educationManager = new EducationManagerImpl();
        educationManager.setDataSource(ds);
        AgeManagerImpl ageManager = new AgeManagerImpl();
        ageManager.setDataSource(ds);

        Parser parser = new Parser();
        parser.parseSectorSk(sectorManager);
        parser.parseClassificationSk(classificationManager);
        parser.parseEducationSk(educationManager);
        parser.parseAgeSk(ageManager);

        ds.close();
    }
}
