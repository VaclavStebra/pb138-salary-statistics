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
import javax.xml.transform.TransformerConfigurationException;
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
@WebServlet(urlPatterns = {"", "/sector/*"})
public class SectorServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SectorServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        request.setCharacterEncoding("UTF-8");
        SectorManagerImpl manager = new SectorManagerImpl();
        BasicDataSource dataSource = new BasicDataSource();
        // TODO move db config to properties file
        dataSource.setUrl("jdbc:derby://localhost:1527/salarystatistics");
        dataSource.setUsername("dbuser");
        dataSource.setPassword("pass");
        manager.setDataSource(dataSource);
        String action = request.getPathInfo();
        switch (action) {
            case "/": {
                try {
                    Document options = getOptions(manager, request);
                    Document tableData = getData(manager, request, false);
                    request.setAttribute("options", documentToString(options));
                    request.setAttribute("table", documentToString(tableData));
                    request.getRequestDispatcher("/template.jsp").forward(request, response);
                } catch (ParserConfigurationException | TransformerException | TransformerFactoryConfigurationError | IllegalArgumentException ex) {
                    Logger.getLogger(SectorServlet.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            case "/options": {
                try {
                    Document options = getOptions(manager, request);
                    writeDocumentToResponse(options, response);
                } catch (ParserConfigurationException ex) {
                    Logger.getLogger(SectorServlet.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
            case "/tabledata": {
                try {
                    Document tableData = getData(manager, request, true);
                    writeDocumentToResponse(tableData, response);
                } catch (ParserConfigurationException ex) {
                    Logger.getLogger(SectorServlet.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;
            }
            case "/data": {
                String data = getJsonData(manager, request);
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json");
                response.getWriter().write(data);
            }
            break;
        }
    }

    private Document getData(SectorManager manager, HttpServletRequest request, boolean filter) throws IOException, ParserConfigurationException {
        List<Sector> sectors = manager.findAllSectors();
        if (filter) {
            String[] years = request.getParameterValues("year");
            String[] code = request.getParameterValues("code");
            String[] countries = request.getParameterValues("country");
            sectors = filterByYear(sectors, years);
            sectors = filterByCode(sectors, code);
            sectors = filterByCountry(sectors, countries);
        }
        return returnTableData(sectors);
    }

    private Document getOptions(SectorManager manager, HttpServletRequest request) throws ParserConfigurationException {
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
        Set<String> codes = new HashSet<>();
        Set<String> countries = new HashSet<>();
        List<Sector> sectors = manager.findAllSectors();
        for (Sector sector : sectors) {
            years.add(sector.getYear());
            codes.add(sector.getCode());
            countries.add(sector.getCountry());
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

        for (String code : codes) {
            Element label = doc.createElement("label");
            label.setAttribute("class", "checkbox-inline");
            Element input = doc.createElement("input");
            input.setAttribute("type", "checkbox");
            input.setAttribute("name", "code");
            input.setAttribute("checked", "");
            input.setAttribute("value", code);
            for (Sector sector : sectors) {
                if (sector.getCode().equals(code)) {                    
                    input.setTextContent(sector.getName());
                    break;
                }
            }
            label.appendChild(input);
            div.appendChild(label);
        }
        
        div.appendChild(doc.createElement("br"));

        for (String country : countries) {
            Element label = doc.createElement("label");
            label.setAttribute("class", "checkbox-inline");
            Element input = doc.createElement("input");
            input.setAttribute("type", "checkbox");
            input.setAttribute("name", "country");
            input.setAttribute("checked", "");
            input.setAttribute("value", country);
            input.setTextContent(country);
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
        return doc;
    }

    private Document returnTableData(List<Sector> data) throws IOException, ParserConfigurationException {
        SortedSet<String> years = new TreeSet<>();
        TreeSet<String> countries = new TreeSet<>();
        for (Sector sector : data) {
            years.add(sector.getYear());
            countries.add(sector.getCountry());
        }

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = docFactory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element rootElement = doc.createElement("table");

        Element thead = doc.createElement("thead");
        Element theadRow = doc.createElement("tr");
        Element th = doc.createElement("th");
        th.setAttribute("rowspan", "2");
        th.setTextContent("Nazev odvetvi");
        theadRow.appendChild(th);
        for (String year : years) {
            th = doc.createElement("th");
            th.setAttribute("colspan", String.valueOf(countries.size()));
            th.setTextContent(year);
            theadRow.appendChild(th);
        }
        thead.appendChild(theadRow);
        theadRow = doc.createElement("tr");
        for (int i = 0; i < years.size(); i++) {
            for (String country : countries) {
                th = doc.createElement("th");
                th.setTextContent(country);
                theadRow.appendChild(th);
            }
        }
        thead.appendChild(theadRow);

        Element tbody = doc.createElement("tbody");
        Map<String, List<Sector>> sectors = new HashMap<>();
        for (Sector sector : data) {
            if (sectors.containsKey(sector.getCode())) {
                sectors.get(sector.getCode()).add(sector);
            } else {
                List<Sector> s = new ArrayList<>();
                s.add(sector);
                sectors.put(sector.getCode(), s);
            }
        }
        for (Entry<String, List<Sector>> sector : sectors.entrySet()) {
            Element tr = doc.createElement("tr");
            Element td = doc.createElement("td");
            //TODO
            td.setTextContent(sector.getValue().get(0).getName());
            tr.appendChild(td);
            List<Sector> values = sector.getValue();
            values.sort(new Comparator<Sector>() {

                @Override
                public int compare(Sector o1, Sector o2) {
                    int yearResult = o1.getYear().compareTo(o2.getYear());
                    if (yearResult == 0) {
                        return o1.getCountry().compareTo(o2.getCountry());
                    }
                    return yearResult;
                }

            });
            for (Sector s : values) {
                td = doc.createElement("td");
                Double salary = s.getAverageSalary();
                td.setTextContent(String.valueOf(salary));
                tr.appendChild(td);
            }
            tbody.appendChild(tr);
        }

        rootElement.appendChild(thead).appendChild(tbody);
        rootElement.setAttribute("class", "table");
        doc.appendChild(rootElement);
        return doc;
    }

    private void writeDocumentToResponse(Document doc, HttpServletResponse response) throws TransformerFactoryConfigurationError {
        try {
            String dataToWrite = documentToString(doc);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(dataToWrite);
        } catch (IllegalArgumentException | TransformerException | IOException ex) {
            Logger.getLogger(SectorServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String documentToString(Document doc) throws TransformerException, TransformerFactoryConfigurationError, TransformerConfigurationException, IllegalArgumentException {
        StringWriter sw = new StringWriter();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(doc), new StreamResult(sw));
        String dataToWrite = sw.toString();
        return dataToWrite;
    }

    private String getJsonData(SectorManager manager, HttpServletRequest request) throws IOException {
        List<Sector> data = manager.findAllSectors();
        String filterStr = request.getParameter("filter");
        boolean filter = !(filterStr != null && !Boolean.getBoolean(filterStr));
        if (filter) {
            String[] years = request.getParameterValues("year");
            String[] codes = request.getParameterValues("code");
            String[] countries = request.getParameterValues("country");
            data = filterByYear(data, years);
            data = filterByCode(data, codes);
            data = filterByCountry(data, countries);
        }
        return new Gson().toJson(data);
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
            return filtered;
        }
    }

    private List<Sector> filterByCode(List<Sector> sectors, String[] codes) {
        List<Sector> filtered = new ArrayList<>();
        if (codes != null) {
            for (Sector sector : sectors) {
                for (String code : codes) {
                    if (sector.getCode().equals(code)) {
                        filtered.add(sector);
                    }
                }
            }
            return filtered;
        } else {
            return filtered;
        }
    }
    
     private List<Sector> filterByCountry(List<Sector> sectors, String[] countries) {
        List<Sector> filtered = new ArrayList<>();
        if (countries != null) {
            for (Sector sector : sectors) {
                for (String country : countries) {
                    if (sector.getCountry().equals(country)) {
                        filtered.add(sector);
                    }
                }
            }
            return filtered;
        } else {
            return filtered;
        }
    }

}
