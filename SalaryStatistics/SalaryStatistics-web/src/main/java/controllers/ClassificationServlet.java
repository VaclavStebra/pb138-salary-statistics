package controllers;

import com.google.gson.Gson;
import dao.Classification;
import dao.ClassificationManager;
import dao.ClassificationManagerImpl;
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
@WebServlet(urlPatterns = {"/classification/*"})
public class ClassificationServlet extends HttpServlet {
        
    private static final int EUR_TO_CZK = 25;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        DataSource source = (DataSource) getServletContext().getAttribute("dataSource");
        ClassificationManagerImpl manager = new ClassificationManagerImpl();
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
                    request.setAttribute("heading", "Klasifikace");
                    request.setAttribute("options", documentToString(options));
                    request.setAttribute("table", documentToString(tableData));
                    request.setAttribute("graphUrl", "classification");
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

    private Document getData(ClassificationManager manager, HttpServletRequest request, boolean filter) throws IOException, ParserConfigurationException {
        List<Classification> classifications = manager.findAllClassifications();
        if (filter) {
            String[] years = request.getParameterValues("year");
            String[] code = request.getParameterValues("code");
            String[] countries = request.getParameterValues("country");
            classifications = filterByYear(classifications, years);
            classifications = filterByCode(classifications, code);
            classifications = filterByCountry(classifications, countries);
        }
        return returnTableData(classifications);
    }

    private Document getOptions(ClassificationManager manager, HttpServletRequest request, boolean checkAll) throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = docFactory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element rootElement = doc.createElement("form");
        rootElement.setAttribute("method", "GET");
        rootElement.setAttribute("action", request.getContextPath() + "/classification");

        Element div = doc.createElement("div");
        div.setAttribute("class", "form-group");

        SortedSet<String> years = new TreeSet<>();
        SortedSet<String> codes = new TreeSet<>();
        SortedSet<String> countries = new TreeSet<>();
        List<Classification> classifications = manager.findAllClassifications();
        for (Classification classification : classifications) {
            years.add(classification.getYear());
            codes.add(classification.getCode());
            countries.add(classification.getCountry());
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
        p.setTextContent("Nazvy klasifikace:");
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
            for (Classification classification : classifications) {
                if (classification.getCode().equals(code)) {
                    input.setTextContent(classification.getName());
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

        doc.appendChild(rootElement);
        return doc;
    }

    private Document returnTableData(List<Classification> data) throws IOException, ParserConfigurationException {
        SortedSet<String> years = new TreeSet<>();
        TreeSet<String> countries = new TreeSet<>();
        for (Classification classification : data) {
            years.add(classification.getYear());
            countries.add(classification.getCountry());
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
        Map<String, List<Classification>> classifications = new HashMap<>();
        for (Classification classification : data) {
            if (classifications.containsKey(classification.getCode())) {
                classifications.get(classification.getCode()).add(classification);
            } else {
                List<Classification> s = new ArrayList<>();
                s.add(classification);
                classifications.put(classification.getCode(), s);
            }
        }
        for (Entry<String, List<Classification>> classification : classifications.entrySet()) {
            Element tr = doc.createElement("tr");
            Element td = doc.createElement("td");
            //TODO
            td.setTextContent(classification.getValue().get(0).getName());
            tr.appendChild(td);
            List<Classification> values = classification.getValue();
            values.sort(new Comparator<Classification>() {

                @Override
                public int compare(Classification o1, Classification o2) {
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
                    for (Classification c : values) {
                        if (c.getYear().equals(year) && c.getCountry().equals(country)) {
                            Double salary = c.getAverageSalary();                
                            if (c.getCountry().equals("sk")) {
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
    
    private String documentToString(Document doc) throws TransformerException, TransformerFactoryConfigurationError, TransformerConfigurationException, IllegalArgumentException {
        StringWriter sw = new StringWriter();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(doc), new StreamResult(sw));
        String dataToWrite = sw.toString();
        return dataToWrite;
    }

    private String getJsonData(ClassificationManager manager, HttpServletRequest request) throws IOException {
        List<Classification> data = manager.findAllClassifications();
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
        for (Classification classification : data) {
            if (classification.getCountry().equals("sk")) {
                classification.setAverageSalary(classification.getAverageSalary() * EUR_TO_CZK);
            }
        }
        return new Gson().toJson(data);
    }

    private List<Classification> filterByYear(List<Classification> classifications, String[] years) {
        List<Classification> filtered = new ArrayList<>();
        if (years != null) {
            for (Classification classification : classifications) {
                for (String year : years) {
                    if (classification.getYear().equals(year)) {
                        filtered.add(classification);
                    }
                }
            }
            return filtered;
        } else {
            return filtered;
        }
    }

    private List<Classification> filterByCode(List<Classification> classifications, String[] codes) {
        List<Classification> filtered = new ArrayList<>();
        if (codes != null) {
            for (Classification classification : classifications) {
                for (String code : codes) {
                    if (classification.getCode().equals(code)) {
                        filtered.add(classification);
                    }
                }
            }
            return filtered;
        } else {
            return filtered;
        }
    }

    private List<Classification> filterByCountry(List<Classification> classifications, String[] countries) {
        List<Classification> filtered = new ArrayList<>();
        if (countries != null) {
            for (Classification classification : classifications) {
                for (String country : countries) {
                    if (classification.getCountry().equals(country)) {
                        filtered.add(classification);
                    }
                }
            }
            return filtered;
        } else {
            return filtered;
        }
    }

}
