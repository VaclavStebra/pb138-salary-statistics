/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import com.google.gson.Gson;
import dao.Region;
import dao.RegionManager;
import dao.RegionManagerImpl;
import helpers.CurrencyReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import javax.sql.DataSource;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Vladimir Jarabica
 */
@WebServlet(urlPatterns = {"/region/*"})
public class RegionServlet extends HttpServlet {
    
    private static final double EUR_TO_CZK = CurrencyReader.eurCourse();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        DataSource source = (DataSource) getServletContext().getAttribute("dataSource");
        RegionManagerImpl manager = new RegionManagerImpl();
        manager.setDataSource(source);
        String action = request.getPathInfo();
        if (action == null) {
            action = "/";
        }
        switch (action) {
            case "/": {
                try {
                    Document options = getOptions(manager, request, request.getQueryString() == null);
                    Document tableData = getData(manager, request, request.getQueryString() != null);
                    request.setAttribute("heading", "Region");
                    request.setAttribute("options", documentToString(options));
                    request.setAttribute("table", documentToString(tableData));
                    request.setAttribute("graphUrl", "region");
                    request.getRequestDispatcher("/template.jsp").forward(request, response);
                } catch (ParserConfigurationException | TransformerException | TransformerFactoryConfigurationError | IllegalArgumentException ex) {
                    Logger.getLogger(SectorServlet.class.getName()).log(Level.SEVERE, null, ex);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }
            break;
            case "/data": {
                String data = getJsonData(manager, request);
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json");
                response.getWriter().write(data);
            }
            break;
        }
    }
    
    private Document getData(RegionManager manager, HttpServletRequest request, boolean filter) throws IOException, ParserConfigurationException {
        List<Region> regions = manager.findAllRegions();
        if (filter) {
            String[] years = request.getParameterValues("year");
            String[] degrees = request.getParameterValues("degree");
            String[] countries = request.getParameterValues("country");
            regions = filterByYear(regions, years);
            regions = filterByName(regions, degrees);
            regions = filterByCountry(regions, countries);
        }
        return returnTableData(regions);
    }
    
    private Document getOptions(RegionManager manager, HttpServletRequest request, boolean checkAll) throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = docFactory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element rootElement = doc.createElement("form");
        rootElement.setAttribute("method", "GET");
        rootElement.setAttribute("action", request.getContextPath() + "/region");

        Element div = doc.createElement("div");
        div.setAttribute("class", "form-group");

        Set<String> years = new HashSet<>();
        Set<String> names = new HashSet<>();
        Set<String> countries = new HashSet<>();
        List<Region> regions = manager.findAllRegions();
        for (Region region : regions) {
            years.add(region.getYear());
            names.add(region.getName());
            countries.add(region.getCountry());
        }

        Element p = doc.createElement("p");
        p.setTextContent("Roky:");
        div.appendChild(p);
        String[] yearsParametersValues = request.getParameterValues("year");
        for (String year : years) {
            Element label = doc.createElement("label");
            label.setAttribute("class", "checkbox-inline");
            Element input = doc.createElement("input");
            input.setAttribute("type", "checkbox");
            input.setAttribute("name", "year");
            if (yearsParametersValues != null) {
                for (String parameterYear : yearsParametersValues) {
                    if (parameterYear.equals(year)) {
                        input.setAttribute("checked", "");
                    }
                }
            } else if (checkAll) {
                input.setAttribute("checked", "");
            }
            input.setAttribute("value", year);
            input.setTextContent(year);
            label.appendChild(input);
            div.appendChild(label);
        }

        p = doc.createElement("p");
        Element btn = doc.createElement("button");
        btn.setAttribute("data-show-nothing-options", "year");
        btn.setAttribute("class", "btn btn-xs btn-warning");
        btn.setTextContent("Zrusit vse");
        p.appendChild(btn);
        btn = doc.createElement("button");
        btn.setAttribute("data-show-all-options", "year");
        btn.setAttribute("class", "btn btn-xs btn-success");
        btn.setTextContent("Vybrat vse");
        p.appendChild(btn);
        div.appendChild(p);
        div.appendChild(doc.createElement("br"));

        String[] degreesParametersValues = request.getParameterValues("degree");
        p = doc.createElement("p");
        p.setTextContent("Regiony:");
        div.appendChild(p);
        for (String name : names) {
            Element label = doc.createElement("label");
            label.setAttribute("class", "checkbox-inline");
            Element input = doc.createElement("input");
            input.setAttribute("type", "checkbox");
            input.setAttribute("name", "degree");
            if (degreesParametersValues != null) {
                for (String parameterDegree : degreesParametersValues) {
                    if (parameterDegree.equals(name)) {
                        input.setAttribute("checked", "");
                    }
                }
            } else if (checkAll) {
                input.setAttribute("checked", "");
            }
            input.setAttribute("value", name);
            input.setTextContent(name);
            label.appendChild(input);
            div.appendChild(label);
        }

        p = doc.createElement("p");
        btn = doc.createElement("button");
        btn.setAttribute("data-show-nothing-options", "degree");
        btn.setAttribute("class", "btn btn-xs btn-warning");
        btn.setTextContent("Zrusit vse");
        p.appendChild(btn);
        btn = doc.createElement("button");
        btn.setAttribute("data-show-all-options", "degree");
        btn.setAttribute("class", "btn btn-xs btn-success");
        btn.setTextContent("Vybrat vse");
        p.appendChild(btn);
        div.appendChild(p);
        div.appendChild(doc.createElement("br"));

        String[] countryParametersValues = request.getParameterValues("country");
        p = doc.createElement("p");
        p.setTextContent("Zeme:");
        for (String country : countries) {
            Element label = doc.createElement("label");
            label.setAttribute("class", "checkbox-inline");
            Element input = doc.createElement("input");
            input.setAttribute("type", "checkbox");
            input.setAttribute("name", "country");
            if (countryParametersValues != null) {
                for (String countryParameter : countryParametersValues) {
                    if (countryParameter.equals(country)) {
                        input.setAttribute("checked", "");
                    }
                }
            } else if (checkAll) {
                input.setAttribute("checked", "");
            }
            input.setAttribute("value", country);
            input.setTextContent(country);
            label.appendChild(input);
            div.appendChild(label);
        }

        p = doc.createElement("p");
        btn = doc.createElement("button");
        btn.setAttribute("data-show-nothing-options", "country");
        btn.setAttribute("class", "btn btn-xs btn-warning");
        btn.setTextContent("Zrusit vse");
        p.appendChild(btn);
        btn = doc.createElement("button");
        btn.setAttribute("data-show-all-options", "country");
        btn.setAttribute("class", "btn btn-xs btn-success");
        btn.setTextContent("Vybrat vse");
        p.appendChild(btn);
        div.appendChild(p);
        rootElement.appendChild(div);

        Element submit = doc.createElement("input");
        submit.setAttribute("type", "submit");
        submit.setAttribute("class", "btn btn-primary");
        submit.setAttribute("value", "Zobrazit");
        rootElement.appendChild(submit);

        doc.appendChild(rootElement);
        return doc;
    }
    
    private Document returnTableData(List<Region> data) throws IOException, ParserConfigurationException {
        SortedSet<String> years = new TreeSet<>();
        SortedSet<String> countries = new TreeSet<>();
        SortedSet<String> sexes = new TreeSet<>();
        for (Region region : data) {
            years.add(region.getYear());
            countries.add(region.getCountry());
            if (region.getSex() != null) {
                sexes.add(region.getSex());
            }
        }

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = docFactory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element rootElement = doc.createElement("table");

        Element thead = doc.createElement("thead");
        Element theadRow = doc.createElement("tr");
        Element th = doc.createElement("th");
        th.setAttribute("rowspan", "3");
        th.setTextContent("Region");
        theadRow.appendChild(th);
        for (String year : years) {
            th = doc.createElement("th");
            th.setAttribute("colspan", String.valueOf(countries.size() * sexes.size()));
            th.setTextContent(year);
            theadRow.appendChild(th);
        }
        thead.appendChild(theadRow);
        theadRow = doc.createElement("tr");
        for (int i = 0; i < years.size() * sexes.size(); i++) {
            for (String country : countries) {
                th = doc.createElement("th");
                th.setTextContent(country);
                theadRow.appendChild(th);
            }
        }
        thead.appendChild(theadRow);
        theadRow = doc.createElement("tr");
        for (int i = 0; i < years.size(); i++) {
            for (String sex : sexes) {
                th = doc.createElement("th");
                th.setTextContent(sex);
                theadRow.appendChild(th);
            }
        }
        thead.appendChild(theadRow);

        Element tbody = doc.createElement("tbody");
        Map<String, List<Region>> regions = new HashMap<>();
        for (Region region : data) {
            if (regions.containsKey(region.getName())) {
                regions.get(region.getName()).add(region);
            } else {
                List<Region> s = new ArrayList<>();
                s.add(region);
                regions.put(region.getName(), s);
            }
        }
        for (Map.Entry<String, List<Region>> region : regions.entrySet()) {
            Element tr = doc.createElement("tr");
            Element td = doc.createElement("td");
            td.setTextContent(region.getKey());
            tr.appendChild(td);
            List<Region> values = region.getValue();
            values.sort(new Comparator<Region>() {

                @Override
                public int compare(Region o1, Region o2) {
                    int result = o1.getYear().compareTo(o2.getYear());
                    if (result == 0) {
                        result = o1.getCountry().compareTo(o2.getCountry());
                    }
                    if (result == 0) {
                        result = o1.getSex().compareTo(o2.getSex());
                    }
                    return result;
                }

            });
            for (Region r : values) {
                td = doc.createElement("td");
                Double salary = r.getAverageSalary();
                if (r.getCountry().equals("sk")) {
                    salary *= EUR_TO_CZK;
                }
                td.setTextContent(String.valueOf(salary.intValue()));
                tr.appendChild(td);
            }
            tbody.appendChild(tr);
        }

        rootElement.appendChild(thead).appendChild(tbody);
        rootElement.setAttribute("class", "table");
        doc.appendChild(rootElement);
        return doc;
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
    
    private String getJsonData(RegionManager manager, HttpServletRequest request) throws IOException {
        List<Region> data = manager.findAllRegions();
        //String filterStr = request.getParameter("filter");
        //boolean filter = !(filterStr != null && !Boolean.getBoolean(filterStr));
        boolean filter = true;
        if (filter) {
            String[] years = request.getParameterValues("year");
            String[] names = request.getParameterValues("degree");
            String[] countries = request.getParameterValues("country");
            data = filterByYear(data, years);
            data = filterByName(data, names);
            data = filterByCountry(data, countries);
        }
        for (Region region : data) {
            if (region.getCountry().equals("sk")) {
                region.setAverageSalary(region.getAverageSalary() * EUR_TO_CZK);
            }
        }
        return new Gson().toJson(data);
    }
    
    private List<Region> filterByYear(List<Region> regions, String[] years) {
        List<Region> filtered = new ArrayList<>();
        if (years != null) {
            for (Region region : regions) {
                for (String year : years) {
                    if (region.getYear().equals(year)) {
                        filtered.add(region);
                    }
                }
            }
            return filtered;
        } else {
            return filtered;
        }
    }
    
    private List<Region> filterByName(List<Region> regions, String[] names) {
        List<Region> filtered = new ArrayList<>();
        if (names != null) {
            for (Region region : regions) {
                for (String name : names) {
                    if (region.getName().equals(name)) {
                        filtered.add(region);
                    }
                }
            }
            return filtered;
        } else {
            return filtered;
        }
    }
    
    private List<Region> filterByCountry(List<Region> regions, String[] countries) {
        List<Region> filtered = new ArrayList<>();
        if (countries != null) {
            for (Region region : regions) {
                for (String country : countries) {
                    if (region.getCountry().equals(country)) {
                        filtered.add(region);
                    }
                }
            }
            return filtered;
        } else {
            return filtered;
        }
    }
}
