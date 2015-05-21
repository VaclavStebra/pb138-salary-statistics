package controllers;

import com.google.gson.Gson;
import dao.Education;
import dao.EducationManager;
import dao.EducationManagerImpl;
import helpers.CurrencyReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
@WebServlet(urlPatterns = {"/education/*"})
public class EducationServlet extends HttpServlet {

    private static final double EUR_TO_CZK = CurrencyReader.eurCourse();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        DataSource source = (DataSource) getServletContext().getAttribute("dataSource");
        EducationManagerImpl manager = new EducationManagerImpl();
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
                    request.setAttribute("heading", "Vzdelani");
                    request.setAttribute("options", documentToString(options));
                    request.setAttribute("table", documentToString(tableData));
                    request.setAttribute("graphUrl", "education");
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

    private Document getData(EducationManager manager, HttpServletRequest request, boolean filter) throws IOException, ParserConfigurationException {
        List<Education> educations = manager.findAllEducations();
        if (filter) {
            String[] years = request.getParameterValues("year");
            String[] degrees = request.getParameterValues("degree");
            String[] countries = request.getParameterValues("country");
            educations = filterByYear(educations, years);
            educations = filterByDegree(educations, degrees);
            educations = filterByCountry(educations, countries);
        }
        return returnTableData(educations);
    }

    private Document getOptions(EducationManager manager, HttpServletRequest request, boolean checkAll) throws ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = docFactory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element rootElement = doc.createElement("form");
        rootElement.setAttribute("method", "GET");
        rootElement.setAttribute("action", request.getContextPath() + "/education");

        Element div = doc.createElement("div");
        div.setAttribute("class", "form-group");

        SortedSet<String> years = new TreeSet<>();
        SortedSet<String> degrees = new TreeSet<>();
        SortedSet<String> countries = new TreeSet<>();
        List<Education> educations = manager.findAllEducations();
        for (Education education : educations) {
            years.add(education.getYear());
            degrees.add(education.getDegree());
            countries.add(education.getCountry());
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
        p.setTextContent("Stupne vzdelani:");
        div.appendChild(p);
        for (String degree : degrees) {
            Element label = doc.createElement("label");
            label.setAttribute("class", "checkbox-inline");
            Element input = doc.createElement("input");
            input.setAttribute("type", "checkbox");
            input.setAttribute("name", "degree");
            if (degreesParametersValues != null) {
                for (String parameterDegree : degreesParametersValues) {
                    if (parameterDegree.equals(degree)) {
                        input.setAttribute("checked", "");
                    }
                }
            } else if (checkAll) {
                input.setAttribute("checked", "");
            }
            input.setAttribute("value", degree);
            input.setTextContent(degree);
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

    private Document returnTableData(List<Education> data) throws IOException, ParserConfigurationException {
        SortedSet<String> years = new TreeSet<>();
        SortedSet<String> countries = new TreeSet<>();
        SortedSet<String> sexes = new TreeSet<>();
        for (Education education : data) {
            years.add(education.getYear());
            countries.add(education.getCountry());
            if (education.getSex() != null) {
                sexes.add(education.getSex());
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
        th.setTextContent("Stupen vzdelani");
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
        Map<String, List<Education>> educations = new HashMap<>();
        for (Education education : data) {
            if (educations.containsKey(education.getDegree())) {
                educations.get(education.getDegree()).add(education);
            } else {
                List<Education> s = new ArrayList<>();
                s.add(education);
                educations.put(education.getDegree(), s);
            }
        }
        for (Map.Entry<String, List<Education>> education : educations.entrySet()) {
            Element tr = doc.createElement("tr");
            Element td = doc.createElement("td");
            td.setTextContent(education.getKey());
            tr.appendChild(td);
            List<Education> values = education.getValue();
            values.sort(new Comparator<Education>() {

                @Override
                public int compare(Education o1, Education o2) {
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
                        for (Education e : values) {
                            if (e.getYear().equals(year) && e.getCountry().equals(country) && e.getSex().equals(sex)) {
                                Double salary = e.getAverageSalary();                
                                if (e.getCountry().equals("sk")) {
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
                    for (Education e : values) {
                        if (e.getYear().equals(year) && e.getCountry().equals(country) && e.getSex()==null) {
                            Double salary = e.getAverageSalary();                
                            if (e.getCountry().equals("sk")) {
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

    private String getJsonData(EducationManager manager, HttpServletRequest request) throws IOException {
        List<Education> data = manager.findAllEducations();
        //String filterStr = request.getParameter("filter");
        //boolean filter = !(filterStr != null && !Boolean.getBoolean(filterStr));
        boolean filter = true;
        if (filter) {
            String[] years = request.getParameterValues("year");
            String[] degrees = request.getParameterValues("degree");
            String[] countries = request.getParameterValues("country");
            data = filterByYear(data, years);
            data = filterByDegree(data, degrees);
            data = filterByCountry(data, countries);
        }
        for (Education education : data) {
            if (education.getCountry().equals("sk")) {
                education.setAverageSalary(education.getAverageSalary() * EUR_TO_CZK);
            }
        }
        return new Gson().toJson(data);
    }

    private List<Education> filterByYear(List<Education> educations, String[] years) {
        List<Education> filtered = new ArrayList<>();
        if (years != null) {
            for (Education education : educations) {
                for (String year : years) {
                    if (education.getYear().equals(year)) {
                        filtered.add(education);
                    }
                }
            }
            return filtered;
        } else {
            return filtered;
        }
    }

    private List<Education> filterByDegree(List<Education> educations, String[] degrees) {
        List<Education> filtered = new ArrayList<>();
        if (degrees != null) {
            for (Education education : educations) {
                for (String degree : degrees) {
                    if (education.getDegree().equals(degree)) {
                        filtered.add(education);
                    }
                }
            }
            return filtered;
        } else {
            return filtered;
        }
    }

    private List<Education> filterByCountry(List<Education> educations, String[] countries) {
        List<Education> filtered = new ArrayList<>();
        if (countries != null) {
            for (Education education : educations) {
                for (String country : countries) {
                    if (education.getCountry().equals(country)) {
                        filtered.add(education);
                    }
                }
            }
            return filtered;
        } else {
            return filtered;
        }
    }
}
