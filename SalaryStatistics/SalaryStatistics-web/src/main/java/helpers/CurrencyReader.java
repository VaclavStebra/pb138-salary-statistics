package helpers;		
		
import java.io.BufferedReader;		
import java.io.IOException;		
import java.io.InputStreamReader;		
import java.net.MalformedURLException;		
import java.net.URL;		
import java.text.NumberFormat;		
import java.text.ParseException;		
import java.util.Locale;		
import java.util.logging.Level;		
import java.util.logging.Logger;		
		
/**		
 *		
 * @author Václav Štěbra <422186@mail.muni.cz>		
 */		
public class CurrencyReader {		
		
    public static double eurCourse() {		
        try {		
            URL url = new URL("http://www.cnb.cz/cs/financni_trhy/devizovy_trh/kurzy_devizoveho_trhu/denni_kurz.txt");		
            try (BufferedReader in = new BufferedReader(		
                    new InputStreamReader(url.openStream()))) {		
                String inputLine;		
                while ((inputLine = in.readLine()) != null) {		
                    if (inputLine.contains("EUR")) {		
                        String[] parts = inputLine.split("\\|");		
                        NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);		
                        Number number = format.parse(parts[4]);		
                                                return number.doubleValue();		
                    }		
                }		
            } catch (IOException | ParseException ex) {		
                Logger.getLogger(CurrencyReader.class.getName()).log(Level.SEVERE, null, ex);		
            }		
		
        } catch (MalformedURLException ex) {		
            Logger.getLogger(CurrencyReader.class.getName()).log(Level.SEVERE, null, ex);		
        }		
        return 27;		
    }		
		
}