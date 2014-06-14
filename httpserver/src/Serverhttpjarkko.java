
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;

import java.io.IOException;
import java.io.InputStream;

import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 *
 * @author Jarkko Virtanen
 */
class Serverhttpjarkko implements HttpHandler
{
  
   public void handle(HttpExchange t) throws IOException 
   {
        String response = "This is the response";
        
        InputStream btid = t.getRequestBody();
        String btidpyynto;
        String viesti = IOUtils.toString(btid, "UTF-8");
        System.out.println("btidpyynto: "+ viesti);

        String key = "";
        if(viesti != null && !viesti.equals(""))
        {
            btidpyynto = json.retrunencryptJWTbtid(viesti);
            Map<String, String> header = new HashMap<String, String>();
            header.put("X-EricssonLabs-APIKEY", "snakeoilapikey");
            header.put("Content-Type", "text/xml");
            String sisalto = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><requestBootstrappingInfoRequest xmlns=\"http://www.3gpp.org/GBAService\"><btid>" +
                    btidpyynto + 
                    "</btid><nafid>cDEzMy5waXVoYS5uZXQAAAAAAA==</nafid><gsid>0</gsid><gbaUAware>True</gbaUAware></requestBootstrappingInfoRequest>";

            HTTPClass yhteys = new HTTPClass();
             try
            {
                String httpPostClient = "";
                httpPostClient = yhteys.httpPostClient("http://p133.piuha.net:8080/bsf/requestBootstrappingInfo", sisalto, header);

                DOMParser parser = new DOMParser();
                
                try 
                {
                    StringReader kokeilu = new StringReader(httpPostClient);

                    parser.parse(new InputSource( kokeilu ));

                    Document doc = parser.getDocument();


                    key = doc.getDocumentElement().getChildNodes().item(1).getTextContent();
                    System.out.println("Saatu avain: "+key);
                    byte[] testah = Base64.decodeBase64(key);
                    key = Hex.encodeHexString(testah);
                    response = json.uncryptJWE1(viesti, key);
                    JWEHeader headerjwt = new JWEHeader(JWEAlgorithm.DIR,EncryptionMethod.A128CBC_HS256);

                    response =  new StringBuilder(response).reverse().toString();
                    response = json.cryptJWE1(response, key, headerjwt);

                } 
                catch (SAXException e) 
                {
                    System.out.println("virtahe1");
                } 
                catch (IOException e) 
                {
                    System.err.println("virtahje2");
                }
            }
            catch (Exception e)
            {
                System.err.println("virhe2");
            }
        }
        
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        
        os.write(response.getBytes());
        os.close();
        


    
    }
   
 
    
    
}

class Serverhttpjarkkosign implements HttpHandler
{
   
  
   public void handle(HttpExchange t) throws IOException 
   {
        String response = "This is the response";
        
        InputStream btid = t.getRequestBody();
        String btidpyynto;
        String viesti = IOUtils.toString(btid, "UTF-8");
        System.out.println("btidpyynto: "+ viesti);

        String key = "";
        if(viesti != null && !viesti.equals(""))
        {
            btidpyynto = json.retrunSignJWTbtid(viesti); 
            Map<String, String> header = new HashMap<String, String>();
            header.put("X-EricssonLabs-APIKEY", "snakeoilapikey");
            header.put("Content-Type", "text/xml");
            String sisalto = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><requestBootstrappingInfoRequest xmlns=\"http://www.3gpp.org/GBAService\"><btid>" +
                    btidpyynto + 
                    "</btid><nafid>cDEzMy5waXVoYS5uZXQAAAAAAA==</nafid><gsid>0</gsid><gbaUAware>True</gbaUAware></requestBootstrappingInfoRequest>";

            HTTPClass yhteys = new HTTPClass();
             try
            {
                String httpPostClient = "";
                httpPostClient = yhteys.httpPostClient("http://p133.piuha.net:8080/bsf/requestBootstrappingInfo", sisalto, header);

                DOMParser parser = new DOMParser();
                
                try 
                {
                    StringReader kokeilu = new StringReader(httpPostClient);

                    parser.parse(new InputSource( kokeilu ));

                    Document doc = parser.getDocument();


                    key = doc.getDocumentElement().getChildNodes().item(1).getTextContent();
                  
                    System.out.println("Saatu avain: "+key);
                    response = json.openSignJWT(viesti, key);

                    response =  new StringBuilder(response).reverse().toString();
                    response = json.signJWT(response, key);

                } 
                catch (SAXException e) 
                {
                    System.out.println("virtahe1");
                } 
                catch (IOException e) 
                {
                    System.err.println("virtahje2");
                }
            }
            catch (Exception e)
            {
                System.err.println("virhe2");
            }
        }
        
        t.sendResponseHeaders(200, response.length());
        OutputStream os = t.getResponseBody();
        
        os.write(response.getBytes());
        os.close();
        


    
    }
}
