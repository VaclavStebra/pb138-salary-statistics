package controllers;

import com.google.gson.Gson;
import dao.Sector;
import dao.SectorManager;
import dao.SectorManagerImpl;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.dbcp2.BasicDataSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Václav Štěbra <422186@mail.muni.cz>
 */
@WebServlet(urlPatterns = {"/sector/*"})
public class SectorServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SectorServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        SectorManagerImpl manager = new SectorManagerImpl();
        BasicDataSource dataSource = new BasicDataSource();
        // TODO move db config to properties file
        dataSource.setUrl("jdbc:derby://localhost:1527/salarystatistics");
        dataSource.setUsername("dbuser");
        dataSource.setPassword("pass");
        manager.setDataSource(dataSource);
        String action = request.getPathInfo();
        switch (action) {
            case "/options": {
                try {
                    getOptions(manager, request, response);
                } catch (ParserConfigurationException ex) {
                    Logger.getLogger(SectorServlet.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
            case "/tabledata": {
                try {
                    getData(manager, request, response);
                } catch (ParserConfigurationException ex) {
                    Logger.getLogger(SectorServlet.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            }
            case "/data": {
                getJsonData(manager, request, response);
            }
            break;
        }
    }

    private void getData(SectorManager manager, HttpServletRequest request, HttpServletResponse response) throws IOException, ParserConfigurationException {
        List<Sector> sectors = manager.findAllSectors();
        String[] years = request.getParameterValues("year");
        String[] names = request.getParameterValues("name");
        sectors = filterByYear(sectors, years);
        sectors = filterByName(sectors, names);
        returnTableData(sectors, response);
    }

    private void getOptions(SectorManager manager, HttpServletRequest request, HttpServletResponse response) throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = docFactory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element rootElement = doc.createElement("form");
        rootElement.setAttribute("method", "GET");
        rootElement.setAttribute("action", request.getContextPath() + "/sector");
        rootElement.setAttribute("data-ajax-form", "true");

        Element div = doc.createElement("div");
        div.setAttribute("class", "form-group");

        Set<String> years = new HashSet<>();
        Set<String> names = new HashSet<>();
        List<Sector> sectors = manager.findAllSectors();
        for (Sector sector : sectors) {
            years.add(sector.getYear());
            names.add(sector.getName());
        }

        for (String year : years) {
            Element label = doc.createElement("label");
            label.setAttribute("class", "checkbox-inline");
            Element input = doc.createElement("input");
            input.setAttribute("type", "checkbox");
            input.setAttribute("name", "year");
            input.setAttribute("checked", "");
            input.setAttribute("value", year);
            input.setTextContent(year);
            label.appendChild(input);
            div.appendChild(label);
        }
        
        div.appendChild(doc.createElement("br"));
        
        for (String name : names) {
            Element label = doc.createElement("label");
            label.setAttribute("class", "checkbox-inline");
            Element input = doc.createElement("input");
            input.setAttribute("type", "checkbox");
            input.setAttribute("name", "name");
            input.setAttribute("checked", "");
            input.setAttribute("value", name);
            input.setTextContent(name);
            label.appendChild(input);
            div.appendChild(label);
        }

        rootElement.appendChild(div);

        Element submit = doc.createElement("input");
        submit.setAttribute("type", "submit");
        submit.setAttribute("class", "btn btn-primary");
        submit.setAttribute("value", "Zobrazit");
        rootElement.appendChild(submit);

        doc.appendChild(rootElement);
        writeDocumentToResponse(doc, response);
    }

    private void returnTableData(List<Sector> data, HttpServletResponse response) throws IOException, ParserConfigurationException {
        SortedSet<String> years = new TreeSet<>();
        for (Sector sector : data) {
            years.add(sector.getYear());
        }        
        
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = docFactory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element rootElement = doc.createElement("table");

        Element thead = doc.createElement("thead");
        Element theadRow = doc.createElement("tr");
        Element th = doc.createElement("th");
        th.setTextContent("Nazev odvetvi");
        theadRow.appendChild(th);
        for (String year : years) {
            th = doc.createElement("th");
            th.setTextContent(year);
            theadRow.appendChild(th);
        }
        thead.appendChild(theadRow);

        Element tbody = doc.createElement("tbody");
        Map<String, List<Sector>> sectors = new HashMap<>();
        for (Sector sector : data) {
            if (sectors.containsKey(sector.getName())) {
                sectors.get(sector.getName()).add(sector);
            } else {
                List<Sector> s = new ArrayList<>();
                s.add(sector);
                sectors.put(sector.getName(), s);
            }
        }
        for (Entry<String, List<Sector>> sector : sectors.entrySet()) {
            Element tr = doc.createElement("tr");
            Element td = doc.createElement("td");
            td.setTextContent(sector.getKey());
            tr.appendChild(td);
            List<Sector> values = sector.getValue();
            values.sort(new Comparator<Sector>() {

                @Override
                public int compare(Sector o1, Sector o2) {
                    return o1.getYear().compareTo(o2.getYear());
                }
                
            });
            for (Sector s : values) {
                td = doc.createElement("td");
                td.setTextContent(s.getAverageSalary().toString());
                tr.appendChild(td);
            }
            tbody.appendChild(tr);
        }

        rootElement.appendChild(thead).appendChild(tbody);
        rootElement.setAttribute("class", "table");
        doc.appendChild(rootElement);

        writeDocumentToResponse(doc, response);
    }

    private void writeDocumentToResponse(Document doc, HttpServletResponse response) throws TransformerFactoryConfigurationError {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            response.setCharacterEncoding("UTF-8");
            String dataToWrite = sw.toString();
            response.getWriter().write(dataToWrite);
        } catch (IllegalArgumentException | TransformerException | IOException ex) {
            Logger.getLogger(SectorServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void getJsonData(SectorManager manager, HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<Sector> data = manager.findAllSectors();
        String[] years = request.getParameterValues("year");
        String[] names = request.getParameterValues("name");
        data = filterByYear(data, years);
        data = filterByName(data, names);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(data));
    }
    
    private List<Sector> filterByYear(List<Sector> sectors, String[] years) {
        List<Sector> filtered = new ArrayList<>();
        if (years != null) {
            for (Sector sector : sectors) {
                for (String year : years) {
                    if (sector.getYear().equals(year)) {
                        filtered.add(sector);
                    }
                }
            }
            return filtered;
        } else {
            return sectors;
        }
    }
    
    private List<Sector> filterByName(List<Sector> sectors, String[] names) {
        List<Sector> filtered = new ArrayList<>();
        if (names != null) {
            for (Sector sector : sectors) {
                for (String name : names) {
                    if (sector.getName().equals(name)) {
                        filtered.add(sector);
                    }
                }
            }
            return filtered;
        } else {
            return sectors;
        }
    }

}
