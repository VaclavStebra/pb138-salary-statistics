/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xml.cz;

import dao.Classification;
import dao.ClassificationManagerImpl;
import dao.Region;
import dao.RegionManagerImpl;
import dao.Sector;
import dao.SectorManagerImpl;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Filip
 */
public class Parser {
    public static void main(String[] args) throws IOException, SQLException, SAXException, ParserConfigurationException {
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
        RegionManagerImpl regionManager = new RegionManagerImpl();
        regionManager.setDataSource(ds);
        
        Parser parser = new Parser();
        parser.parseSector(sectorManager);
        parser.parseClassification(classificationManager);
        parser.parseRegion(regionManager);
        
        ds.close();
    }
    
    public void parseSector(SectorManagerImpl manager) throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse("src/main/java/xml/cz/PRA0010UU.xml");
        
        NodeList values = doc.getElementsByTagName("hodnota");
        Element value = null;
        
        NodeList years = ((Element) doc.getElementsByTagName("dimcas").item(0)).getElementsByTagName("cas");
        Element year = null;
        
        NodeList names = ((Element) doc.getElementsByTagName("dimdruhspec").item(0)).getElementsByTagName("druhspec");
        Element name = null;
        
        NodeList items = null;
        Element item = null;
        
        Sector sector = null;
        
        for (int i = 0; i < values.getLength(); i++) {
            value = (Element) values.item(i);
            
            if ("1540".equals(value.getElementsByTagName("ukazatel").item(0).getTextContent())) {//1540 - Průměrná hrubá měsíční mzda (na fyzické osoby)
                if("101".equals(value.getElementsByTagName("charhod").item(0).getTextContent())) {//101 - Hodnota za běžné období
                    sector = new Sector();
                    sector.setCountry("cz");
                    sector.setAverageSalary(Double.parseDouble(value.getElementsByTagName("nhodnota").item(0).getTextContent()));
                    
                    for (int j = 0; j < years.getLength(); j++) {
                        year = (Element) years.item(j);
                        if(value.getElementsByTagName("cas").item(0).getTextContent().equals(year.getAttribute("ID"))) {
                            sector.setYear(year.getElementsByTagName("rok").item(0).getTextContent());
                        }
                    }

                    for (int j = 0; j < names.getLength(); j++) {
                        name = (Element) names.item(j);
                        if(value.getElementsByTagName("druhspec").item(0).getTextContent().equals(name.getAttribute("ID"))) {
                            items = name.getElementsByTagName("polozka");
                            for (int k = 0; k < items.getLength(); k++) {
                                item =(Element) items.item(k);
                                if("1".equals(item.getAttribute("PORADI"))) {
                                    sector.setCode(item.getElementsByTagName("kodzaz").item(0).getTextContent());
                                    sector.setName(item.getElementsByTagName("textup").item(0).getTextContent());
                                }
                            }
                        }
                    }
                    
                    manager.createSector(sector);
                }
            }
        }               
    }
    
    public void parseClassification(ClassificationManagerImpl manager) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse("src/main/java/xml/cz/PRA0021UU.xml");
        
        NodeList values = doc.getElementsByTagName("hodnota");
        Element value = null;
        
        NodeList years = ((Element) doc.getElementsByTagName("dimcas").item(0)).getElementsByTagName("cas");
        Element year = null;
        
        NodeList names = ((Element) doc.getElementsByTagName("dimdruhspec").item(0)).getElementsByTagName("druhspec");
        Element name = null;
        
        Classification classification = null;
        
        for (int i = 0; i < values.getLength(); i++) {
            value = (Element) values.item(i);
            
            if("101".equals(value.getElementsByTagName("charhod").item(0).getTextContent())) {
                for (int j = 0; j < names.getLength(); j++) {
                    name = (Element) names.item(j);
                    if(value.getElementsByTagName("druhspec").item(0).getTextContent().equals(name.getAttribute("ID"))) {
                        if(name.getElementsByTagName("polozka").getLength() == 1) {
                            if("Klasifikace zaměstnání".equals(name.getElementsByTagName("textup").item(0).getTextContent())) {
                                if(!("9999".equals(name.getElementsByTagName("kodzaz").item(0).getTextContent()))) {
                                    classification = new Classification();
                                    classification.setCountry("cz");
                                    classification.setName(name.getElementsByTagName("text").item(0).getTextContent());
                                    classification.setCode(name.getElementsByTagName("kodzaz").item(0).getTextContent());
                                    classification.setAverageSalary(Double.parseDouble(value.getElementsByTagName("nhodnota").item(0).getTextContent()));

                                    for (int k = 0; k < years.getLength(); k++) {
                                        year = (Element) years.item(k);
                                        if(value.getElementsByTagName("cas").item(0).getTextContent().equals(year.getAttribute("ID"))) {
                                            classification.setYear(year.getElementsByTagName("rok").item(0).getTextContent());
                                        }
                                    }
                                    manager.createClassification(classification);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    public void parseRegion(RegionManagerImpl manager) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse("src/main/java/xml/cz/PRA0031PU_KR.xml");
        
        NodeList values = doc.getElementsByTagName("hodnota");
        Element value = null;
        
        NodeList years = ((Element) doc.getElementsByTagName("dimcas").item(0)).getElementsByTagName("cas");
        Element year = null;
        
        NodeList names = ((Element) doc.getElementsByTagName("dimdruhspec").item(0)).getElementsByTagName("druhspec");
        Element name = null;
        
        NodeList regionNames = ((Element) doc.getElementsByTagName("dimuzemi").item(0)).getElementsByTagName("uzemi");
        Element regionName = null;
        
        Region region = null;
        
        for (int i = 0; i < values.getLength(); i++) {
            value = (Element) values.item(i);

            if("101".equals(value.getElementsByTagName("charhod").item(0).getTextContent())) {
                for (int j = 0; j < names.getLength(); j++) {
                    name = (Element) names.item(j);
                    if(value.getElementsByTagName("druhspec").item(0).getTextContent().equals(name.getAttribute("ID"))) {
                        region = new Region();
                        region.setCountry("cz");
                        region.setSex(name.getElementsByTagName("text").item(0).getTextContent());
                        region.setAverageSalary(Double.parseDouble(value.getElementsByTagName("nhodnota").item(0).getTextContent()));

                        for (int k = 0; k < years.getLength(); k++) {
                            year = (Element) years.item(k);
                            if(value.getElementsByTagName("cas").item(0).getTextContent().equals(year.getAttribute("ID"))) {
                                region.setYear(year.getElementsByTagName("rok").item(0).getTextContent());
                            }
                        }

                        for (int k = 0; k < regionNames.getLength(); k++) {
                            regionName = (Element) regionNames.item(k);
                            if(value.getElementsByTagName("uzemi").item(0).getTextContent().equals(regionName.getAttribute("ID"))) {
                                region.setName(regionName.getElementsByTagName("text").item(0).getTextContent());
                            }
                        }
                        
                        manager.createRegion(region);
                    }
                }
            }
        }
    }
}
