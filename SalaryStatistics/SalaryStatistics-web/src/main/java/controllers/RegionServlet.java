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
    
    /**
     * Exchange rate from EUR to CZK
     */
    private static final double EUR_TO_CZK = CurrencyReader.eurCourse();

    /**
     * Proccesses HTTP GET request
     * @param request http request
     * @param response http response
     * @throws ServletException
     * @throws IOException 
     */
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
                    Document options = getOptions(manager, request, /*request.getQueryString() == null*/ request.getParameterValues("start") == null);
                    Document tableData = getData(manager, request, /*request.getQueryString() != null*/ request.getParameterValues("start") != null);
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
    
    /**
     * Processes data and returns them as HTML portion of document
     * @param manager manager used to retrieve data
     * @param request http request
     * @param filter whether to filter data
     * @return HTML portion of document
     * @throws IOException
     * @throws ParserConfigurationException 
     */
    private Document getData(RegionManager manager, HttpServletRequest request, boolean filter) throws IOException, ParserConfigurationException {
        List<Region> regions = manager.findAllRegions();
        if (filter) {
            String[] years = request.getParameterValues("year");
            String[] names = request.getParameterValues("name");
            String[] countries = request.getParameterValues("country");
            if (years == null || names == null || countries == null) {
                return returnMessage();
            }
            regions = filterByYear(regions, years);
            regions = filterByName(regions, names);
            regions = filterByCountry(regions, countries);
        }
        return returnTableData(regions);
    }

    /**
     * Appends error message when no filter is checked
     * @return HTML portion of document
     * @throws ParserConfigurationException 
     */
    private Document returnMessage() throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = docFactory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element message = doc.createElement("h4");
        message.setAttribute("class", "bg-danger message");
        message.setTextContent("Je potreba zaskrtnout alespon jednu hodnotu pro kazdej vyber.");
        doc.appendChild(message);
        return doc;
    }
    
    /**
     * Returns HTML portion of document with generated options to filter by
     * @param manager manager used to retrieve data
     * @param request HTTP request
     * @param checkAll whether all options should be checked
     * @return HTML portion of document
     * @throws ParserConfigurationException 
     */
    private Document getOptions(RegionManager manager, HttpServletRequest request, boolean checkAll) throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = docFactory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element rootElement = doc.createElement("form");
        rootElement.setAttribute("method", "GET");
        rootElement.setAttribute("action", request.getContextPath() + "/region");

        Element div = doc.createElement("div");
        div.setAttribute("class", "form-group");

        Set<String> years = new TreeSet<>();
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

        String[] nameParametersValues = request.getParameterValues("name");
        p = doc.createElement("p");
        p.setTextContent("Regiony:");
        div.appendChild(p);
        for (String name : names) {
            Element label = doc.createElement("label");
            label.setAttribute("class", "checkbox-inline");
            Element input = doc.createElement("input");
            input.setAttribute("type", "checkbox");
            input.setAttribute("name", "name");
            if (nameParametersValues != null) {
                for (String parameterName : nameParametersValues) {
                    if (parameterName.equals(name)) {
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
        btn.setAttribute("data-show-nothing-options", "name");
        btn.setAttribute("class", "btn btn-xs btn-warning");
        btn.setTextContent("Zrusit vse");
        p.appendChild(btn);
        btn = doc.createElement("button");
        btn.setAttribute("data-show-all-options", "name");
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
        
        Element hiddenStart = doc.createElement("input");
        hiddenStart.setAttribute("type", "hidden");
        hiddenStart.setAttribute("name", "start");
        hiddenStart.setAttribute("value", "false");
        rootElement.appendChild(hiddenStart);

        doc.appendChild(rootElement);
        return doc;
    }
    
    /**
     * Returns HTML table from data
     * @param data data to show in table
     * @return HTML portion of document
     * @throws IOException
     * @throws ParserConfigurationException 
     */
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
            th.setAttribute("colspan", String.valueOf(countries.size() * (sexes.size() + 1)));
            th.setTextContent(year);
            theadRow.appendChild(th);
        }
        thead.appendChild(theadRow);
        theadRow = doc.createElement("tr");
        for (int i = 0; i < years.size() * (sexes.size() + 1); i++) {
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
            th = doc.createElement("th");
            th.setTextContent("spolu");
            theadRow.appendChild(th);
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
                        if (o1.getSex() == null) {
                            return 1;
                        } else if (o2.getSex() == null) {
                            return -1;
                        }
                        result = o1.getSex().compareTo(o2.getSex());
                    }
                    return result;
                }

            });
            for (String year : years) {
                for (String country : countries) {
                    for (String sex : sexes) {
                        td = doc.createElement("td");
                        boolean found = false;
                        for (Region r : values) {
                            if (r.getYear().equals(year) && r.getCountry().equals(country) && r.getSex().equals(sex)) {
                                Double salary = r.getAverageSalary();                
                                if (r.getCountry().equals("sk")) {
                                    salary *= EUR_TO_CZK;
                                }
                                td.setTextContent(String.valueOf(salary.intValue()));
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            td.setTextContent("-");
                        }
                        tr.appendChild(td);
                    }
                    td = doc.createElement("td");
                    boolean found = false;
                    for (Region r : values) {
                        if (r.getYear().equals(year) && r.getCountry().equals(country) && r.getSex()==null) {
                            Double salary = r.getAverageSalary();                
                            if (r.getCountry().equals("sk")) {
                                salary *= EUR_TO_CZK;
                            }
                            td.setTextContent(String.valueOf(salary.intValue()));
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        td.setTextContent("-");
                    }
                    tr.appendChild(td);
                }
            }
            tbody.appendChild(tr);
        }

        rootElement.appendChild(thead).appendChild(tbody);
        rootElement.setAttribute("class", "table table-hover");
        doc.appendChild(rootElement);
        return doc;
    }
    
    /**
     * Transforms XML document to string
     * @param doc document to transform
     * @return string representing xml document
     * @throws TransformerException
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerConfigurationException
     * @throws IllegalArgumentException 
     */
    private String documentToString(Document doc) throws TransformerException, TransformerFactoryConfigurationError, TransformerConfigurationException, IllegalArgumentException {
        StringWriter sw = new StringWriter();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(doc), new StreamResult(sw));
        String dataToWrite = sw.toString();
        return dataToWrite;
    }
    
    /**
     * Returns JSON representation of data
     * @param manager manager used to retrieve data
     * @param request HTTP request
     * @return JSON representation of data
     * @throws IOException 
     */
    private String getJsonData(RegionManager manager, HttpServletRequest request) throws IOException {
        List<Region> data = manager.findAllRegions();
        //String filterStr = request.getParameter("filter");
        //boolean filter = !(filterStr != null && !Boolean.getBoolean(filterStr));
        boolean filter = true;
        if (filter) {
            String[] years = request.getParameterValues("year");
            String[] names = request.getParameterValues("name");
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
    
    /**
     * Filters data by year
     * @param regions list of regions to filter
     * @param years array of years to filter by
     * @return filtered list of classifications
     */
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
    
    /**
     * Filters data by name
     * @param regions list of regions to filter
     * @param names array of names to filter by
     * @return filtered list of classifications
     */
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
    
    /**
     * Filters data by country
     * @param regions list of regions to filter
     * @param countries array of countries to filter by
     * @return filtered list of classifications
     */
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
