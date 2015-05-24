package controllers;

import com.google.gson.Gson;
import dao.Age;
import dao.AgeManager;
import dao.AgeManagerImpl;
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
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
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
import org.apache.commons.dbcp2.BasicDataSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Vladimír Jarabica
 */
@WebServlet(urlPatterns = {"/age/*"})
public class AgeServlet extends HttpServlet {

    /**
     * Exchange rate from EUR to CZK
     */
    private static final double EUR_TO_CZK = CurrencyReader.eurCourse();

    /**
     * Comparator for age intervals
     */
    Comparator intervalComparator = new Comparator<String>() {
        @Override
        public int compare(String one, String two) {
            if (one.equals(two)) {
                return 0;
            }
            String[] parts1 = one.split("-");
            String[] parts2 = two.split("-");
            if (parts1[0].equals("Do")) {
                return -1;
            }
            if (parts2[0].equals("Do")) {
                return 1;
            }
            return parts1[0].compareTo(parts2[0]);
        }
    };

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
        AgeManagerImpl manager = new AgeManagerImpl();
        manager.setDataSource(source);
        String action = request.getPathInfo();
        if (action == null) {
            action = "/";
        }
        switch (action) {
            case "/": {
                try {
                    Document options = getOptions(manager, request, request.getParameterValues("start") == null);
                    //Document tableData = getData(manager, request, request.getQueryString() != null);
                    Document tableData = getData(manager, request, request.getParameterValues("start") != null);
                    request.setAttribute("heading", "Vek");
                    request.setAttribute("options", documentToString(options));
                    request.setAttribute("table", documentToString(tableData));
                    request.setAttribute("graphUrl", "age");
                    request.getRequestDispatcher("/template.jsp").forward(request, response);
                } catch (ParserConfigurationException | TransformerException | TransformerFactoryConfigurationError | IllegalArgumentException ex) {
                    Logger.getLogger(SectorServlet.class.getName()).log(Level.SEVERE, null, ex);
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
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

    /**
     * Processes data and returns them as HTML portion of document
     * @param manager manager used to retrieve data
     * @param request http request
     * @param filter whether to filter data
     * @return HTML portion of document
     * @throws IOException
     * @throws ParserConfigurationException 
     */
    private Document getData(AgeManager manager, HttpServletRequest request, boolean filter) throws IOException, ParserConfigurationException {
        List<Age> ages = manager.findAllAges();
        if (filter) {
            String[] years = request.getParameterValues("year");
            String[] intervals = request.getParameterValues("interval");
            String[] countries = request.getParameterValues("country");
            if (years == null || intervals == null || countries == null) {
                return returnMessage();
            }
            ages = filterByCountry(ages, countries);
            ages = filterByYear(ages, years);
            ages = filterByInterval(ages, intervals);
        }
        return returnTableData(ages);
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
    private Document getOptions(AgeManager manager, HttpServletRequest request, boolean checkAll) throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = docFactory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element rootElement = doc.createElement("form");
        rootElement.setAttribute("method", "GET");
        rootElement.setAttribute("action", request.getContextPath() + "/age");

        Element div = doc.createElement("div");
        div.setAttribute("class", "form-group");

        Set<String> years = new HashSet<>();
        Set<String> ageIntervals = new TreeSet<String>(intervalComparator);
        Set<String> countries = new HashSet<>();
        List<Age> ages = manager.findAllAges();
        for (Age age : ages) {
            years.add(age.getYear());
            ageIntervals.add(convertToInterval(age));
            countries.add(age.getCountry());
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

        String[] intervalsParametersValues = request.getParameterValues("interval");
        p = doc.createElement("p");
        p.setTextContent("Intervaly:");
        div.appendChild(p);
        for (String ageInterval : ageIntervals) {
            Element label = doc.createElement("label");
            label.setAttribute("class", "checkbox-inline");
            Element input = doc.createElement("input");
            input.setAttribute("type", "checkbox");
            input.setAttribute("name", "interval");
            if (intervalsParametersValues != null) {
                for (String parameterInterval : intervalsParametersValues) {
                    if (parameterInterval.equals(ageInterval)) {
                        input.setAttribute("checked", "");
                    }
                }
            }
            if (checkAll) {
                input.setAttribute("checked", "");
            }
            input.setAttribute("value", ageInterval);
            input.setTextContent(ageInterval);
            label.appendChild(input);
            div.appendChild(label);
        }

        p = doc.createElement("p");
        btn = doc.createElement("button");
        btn.setAttribute("data-show-nothing-options", "interval");
        btn.setAttribute("class", "btn btn-xs btn-warning");
        btn.setTextContent("Zrusit vse");
        p.appendChild(btn);
        btn = doc.createElement("button");
        btn.setAttribute("data-show-all-options", "interval");
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
    private Document returnTableData(List<Age> data) throws IOException, ParserConfigurationException {
        SortedSet<String> years = new TreeSet<>();
        SortedSet<String> countries = new TreeSet<>();
        SortedSet<String> sexes = new TreeSet<>();
        for (Age age : data) {
            years.add(age.getYear());
            countries.add(age.getCountry());
            if (age.getSex() != null) {
                sexes.add(age.getSex());
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
        th.setTextContent("Interval");
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
        Map<String, List<Age>> ages = new TreeMap<String, List<Age>>(intervalComparator);
        for (Age age : data) {
            if (ages.containsKey(convertToInterval(age))) {
                ages.get(convertToInterval(age)).add(age);
            } else {
                List<Age> a = new ArrayList<>();
                a.add(age);
                ages.put(convertToInterval(age), a);
            }
        }
        for (Map.Entry<String, List<Age>> age : ages.entrySet()) {
            Element tr = doc.createElement("tr");
            Element td = doc.createElement("td");
            td.setTextContent(age.getKey());
            tr.appendChild(td);
            List<Age> values = age.getValue();
            values.sort(new Comparator<Age>() {

                @Override
                public int compare(Age o1, Age o2) {
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
                        for (Age a : values) {
                            if (a.getYear().equals(year) && a.getCountry().equals(country) && a.getSex().equals(sex)) {
                                Double salary = a.getAverageSalary();                
                                if (a.getCountry().equals("sk")) {
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
                    for (Age a : values) {
                        if (a.getYear().equals(year) && a.getCountry().equals(country) && a.getSex()==null) {
                            Double salary = a.getAverageSalary();                
                            if (a.getCountry().equals("sk")) {
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
    private String getJsonData(AgeManager manager, HttpServletRequest request) throws IOException {
        List<Age> data = manager.findAllAges();
        boolean filter = true;
        if (filter) {
            String[] years = request.getParameterValues("year");
            String[] intervals = request.getParameterValues("interval");
            String[] countries = request.getParameterValues("country");
            data = filterByYear(data, years);
            data = filterByInterval(data, intervals);
            data = filterByCountry(data, countries);
        }
        for (Age age : data) {
            if (age.getCountry().equals("sk")) {
                age.setAverageSalary(age.getAverageSalary() * EUR_TO_CZK);
            }
        }
        return new Gson().toJson(data);
    }

    /**
     * Filters data by year
     * @param ages list of ages to filter
     * @param years array of years to filter by
     * @return filtered list of classifications
     */
    private List<Age> filterByYear(List<Age> ages, String[] years) {
        List<Age> filtered = new ArrayList<>();
        if (years != null) {
            for (Age age : ages) {
                for (String year : years) {
                    if (age.getYear().equals(year)) {
                        filtered.add(age);
                    }
                }
            }
            return filtered;
        } else {
            return ages;
        }
    }

    /**
     * Filters data by code
     * @param ages list of ages to filter
     * @param intervals array of intervals to filter by
     * @return filtered list of classifications
     */
    private List<Age> filterByInterval(List<Age> ages, String[] intervals) {
        List<Age> filtered = new ArrayList<>();
        Set<String[]> intervalParts = new HashSet<>();
        if (intervals != null) {
            for (String interval : intervals) {
                String[] parts = convertFromInterval(interval);
                intervalParts.add(parts);
            }
            for (Age age : ages) {
                for (String[] interval : intervalParts) {
                    if (age.getAgeFrom().toString().equals(interval[0]) && age.getAgeTo().toString().equals(interval[1])) {
                        filtered.add(age);
                    }
                }
            }
            return filtered;
        }
        return ages;
    }

    /**
     * Finds if entered string is a number
     * @param str string detection 
     * @return true if str is numeric
     * @return false if str is not numeric
     */
    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * Converts age's parameters ageFrom and ageTo into
     * a one interval as string
     * @param age object to specify interval
     * @return interval in string form
     */
    private String convertToInterval(Age age) {
        if (age.getAgeFrom() == 0) {
            return "Do-" + age.getAgeTo().toString();
        }
        if (age.getAgeTo() == 99) {
            return age.getAgeFrom() + "-a více";
        }
        return age.getAgeFrom().toString() + "-" + age.getAgeTo().toString();
    }

    private String[] convertFromInterval(String interval) {
        String parts[] = interval.split("-", 2);
        if (parts[0].equals("Do")) {
            parts[0] = "0";
        } else if (parts[1].equals("a více")) {
            parts[1] = "99";
        }
        return parts;
    }

    /**
     * Filters data by country
     * @param ages list of ages to filter
     * @param countries array of countries to filter by
     * @return filtered list of classifications
     */
    private List<Age> filterByCountry(List<Age> ages, String[] countries) {
        List<Age> filtered = new ArrayList<>();
        if (countries != null) {
            for (Age age : ages) {
                for (String country : countries) {
                    if (age.getCountry().equals(country)) {
                        filtered.add(age);
                    }
                }
            }
            return filtered;
        } else {
            return filtered;
        }
    }
}
