package web;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.apache.commons.dbcp2.BasicDataSource;

/**
 *
 * @author Václav Štěbra <422186@mail.muni.cz>
 */
@WebListener
public class StartListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent ev) {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(StartListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        ServletContext context = ev.getServletContext();
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl("jdbc:derby://localhost:1527/salarystatistics");
        dataSource.setUsername("dbuser");
        dataSource.setPassword("pass");
        context.setAttribute("dataSource", dataSource);
    }

    @Override
    public void contextDestroyed(ServletContextEvent ev) {
        
    }
    
}
