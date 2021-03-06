package controllers;

import com.google.gson.Gson;
import dao.Sector;
import dao.SectorManager;
import dao.SectorManagerImpl;
import helpers.CurrencyReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
 * @author Václav Štěbra <422186@mail.muni.cz>
 */
@WebServlet(urlPatterns = {"", "/sector/*"})
public class SectorServlet extends HttpServlet {
        
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
        SectorManagerImpl manager = new SectorManagerImpl();
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
                    request.setAttribute("heading", "Sektor");
                    request.setAttribute("options", documentToString(options));
                    request.setAttribute("table", documentToString(tableData));
                    request.setAttribute("graphUrl", "sector");
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
     * @param manager sector manager used to retrieve data
     * @param request http request
     * @param filter whether to filter data
     * @return HTML portion of document
     * @throws IOException
     * @throws ParserConfigurationException 
     */
    private Document getData(SectorManager manager, HttpServletRequest request, boolean filter) throws IOException, ParserConfigurationException {
        List<Sector> sectors = manager.findAllSectors();
        if (filter) {
            String[] years = request.getParameterValues("year");
            String[] code = request.getParameterValues("code");
            String[] countries = request.getParameterValues("country");
            if (years == null || code == null || countries == null) {
                return returnMessage();
            }
            sectors = filterByYear(sectors, years);
            sectors = filterByCode(sectors, code);
            sectors = filterByCountry(sectors, countries);
        }
        return returnTableData(sectors);
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
     * @param manager sector manager used to retrieve data
     * @param request HTTP request
     * @param checkAll whether all options should be checked
     * @return HTML portion of document
     * @throws ParserConfigurationException 
     */
    private Document getOptions(SectorManager manager, HttpServletRequest request, boolean checkAll) throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = docFactory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element rootElement = doc.createElement("form");
        rootElement.setAttribute("method", "GET");
        rootElement.setAttribute("action", request.getContextPath() + "/sector");

        Element div = doc.createElement("div");
        div.setAttribute("class", "form-group");

        SortedSet<String> years = new TreeSet<>();
        SortedSet<String> codes = new TreeSet<>();
        SortedSet<String> countries = new TreeSet<>();
        List<Sector> sectors = manager.findAllSectors();
        for (Sector sector : sectors) {
            years.add(sector.getYear());
            codes.add(sector.getCode());
            countries.add(sector.getCountry());
        }

        String[] yearsParametersValues = request.getParameterValues("year");
        Element p = doc.createElement("p");
        p.setTextContent("Roky:");
        div.appendChild(p);
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

        String[] codesParametersValues = request.getParameterValues("code");
        p = doc.createElement("p");
        p.setTextContent("Nazvy odvetvi:");
        div.appendChild(p);
        for (String code : codes) {
            Element label = doc.createElement("label");
            label.setAttribute("class", "checkbox-inline");
            Element input = doc.createElement("input");
            input.setAttribute("type", "checkbox");
            input.setAttribute("name", "code");
            if (codesParametersValues != null) {
                for (String parameterCode : codesParametersValues) {
                    if (parameterCode.equals(code)) {
                        input.setAttribute("checked", "");
                    }
                }
            } else if (checkAll) {
                input.setAttribute("checked", "");
            }
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

        p = doc.createElement("p");
        btn = doc.createElement("button");
        btn.setAttribute("data-show-nothing-options", "code");
        btn.setAttribute("class", "btn btn-xs btn-warning");
        btn.setTextContent("Zrusit vse");
        p.appendChild(btn);
        btn = doc.createElement("button");
        btn.setAttribute("data-show-all-options", "code");
        btn.setAttribute("class", "btn btn-xs btn-success");
        btn.setTextContent("Vybrat vse");
        p.appendChild(btn);
        div.appendChild(p);        
        div.appendChild(doc.createElement("br"));

        String[] countryParametersValues = request.getParameterValues("country");
        p = doc.createElement("p");
        p.setTextContent("Zeme:");
        div.appendChild(p);
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
            for (String year : years) {
                for (String country : countries) {
                    td = doc.createElement("td");
                    boolean found = false;
                    for (Sector s : values) {
                        if (s.getYear().equals(year) && s.getCountry().equals(country)) {
                            Double salary = s.getAverageSalary();                
                            if (s.getCountry().equals("sk")) {
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
    private String getJsonData(SectorManager manager, HttpServletRequest request) throws IOException {
        List<Sector> data = manager.findAllSectors();
        //String filterStr = request.getParameter("filter");
        //boolean filter = !(filterStr != null && !Boolean.getBoolean(filterStr));
        boolean filter = true;
        if (filter) {
            String[] years = request.getParameterValues("year");
            String[] codes = request.getParameterValues("code");
            String[] countries = request.getParameterValues("country");
            data = filterByYear(data, years);
            data = filterByCode(data, codes);
            data = filterByCountry(data, countries);
        }
        for (Sector sector : data) {
            if (sector.getCountry().equals("sk")) {
                sector.setAverageSalary(sector.getAverageSalary() * EUR_TO_CZK);
            }
        }
        return new Gson().toJson(data);
    }

    /**
     * Filters data by year
     * @param sectors list of sectors to filter
     * @param years array of years to filter by
     * @return filtered list of sectors
     */
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

    /**
     * Filters data by code
     * @param sectors list of sectors to filter
     * @param codes array of codes to filter by
     * @return filtered list of sectors
     */
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

    /**
     * Filters data by country
     * @param sectors list of sectors to filter
     * @param countries array of countries to filter by
     * @return filtered list of sectors
     */
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
