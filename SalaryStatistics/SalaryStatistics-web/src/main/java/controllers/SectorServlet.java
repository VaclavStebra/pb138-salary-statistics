package controllers;

import com.google.gson.Gson;
import dao.Sector;
import dao.SectorManager;
import dao.SectorManagerImpl;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
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
            case "/options":
                getOptions();
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
        // TODO filter by parameters

        returnTableData(sectors, response);
    }

    private void getOptions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void returnTableData(List<Sector> data, HttpServletResponse response) throws IOException, ParserConfigurationException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = docFactory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element rootElement = doc.createElement("table");

        Element thead = doc.createElement("thead");
        Element theadRow = doc.createElement("tr");
        Element th = doc.createElement("th");
        th.setTextContent("Nazev odvetvi");
        theadRow.appendChild(th);
        th = doc.createElement("th");
        th.setTextContent("Rok");
        theadRow.appendChild(th);
        th = doc.createElement("th");
        th.setTextContent("Plat");
        theadRow.appendChild(th);
        thead.appendChild(theadRow);

        Element tbody = doc.createElement("tbody");
        for (Sector sector : data) {
            Element tr = doc.createElement("tr");
            Element td = doc.createElement("td");
            td.setTextContent(sector.getName());
            tr.appendChild(td);
            td = doc.createElement("td");
            td.setTextContent(sector.getYear());
            tr.appendChild(td);
            td = doc.createElement("td");
            td.setTextContent(sector.getAverageSalary().toString());
            tr.appendChild(td);
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
        // TODO filter data
        
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(data));
    }

}
