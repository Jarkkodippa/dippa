
//import com.sun.net.httpserver.HttpServer;


import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

//import org.apache.commons.logging.LogFactory;
//import org.apache.commons.codec.DecoderException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
//import java.util.concurrent.Executors;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *HTTP luokka. Tekee HTTP pyynnön ja vastaanottaa siihen liittyvät arvot.
 * @author Jarkko
 */
public class HTTP 
{
    private static final Scanner READER = initializeREADER();
    static int counter = 0;
    
    // Vakiot.
    final String VIESTI_KEHOTE = "Kirjoita komento:";
    final String VIESTI_JAAHYVAISET = "Ohjelma lopetettu.";

    private String urlStr = "http://example.com:8080/abc/";
    private String host = "example.com";
    private String realm = "ExampleRealm";
    private String userName = "admin";
    private String password = "jeeee";
    private int port = 8080;
    private HttpClient client;
    private int status;
    private GetMethod getMethod;
    private String responseBody;

   /** Herja
     */
   private final String VIESTI_HERJA = "Virhe!";
   
   
   public HTTP() 
   {

   }
    
   //Apuluokka mahdollista syötteen lukua varten
   private static Scanner initializeREADER() 
   {
      // Luodaan ja liitetään oletussyötevirtaan.
      Scanner sc = new Scanner(System.in);

      // Lokalisoidaan siten, että esimerkiksi desimaalimerkki on piste.
      Locale enLocale = new Locale("en");
      sc.useLocale(enLocale);

      // Palautetaan lukija.
      return sc;
   }
   
     /**Apuluokka 
      * Tulostetaan otsikko annetulla merkill� kehystettyn�.
     * @param teksti kehystett�v� otsikkotesti.
     */
   private void tulostaOtsikko(String teksti)
   {
      // Kehysmerkki.
      final char MERKKI_KEHYS = '*';

      // Reunan ja tekstin v�li vakiona.
      final String VALI = " ";

      // Selvitet��n merkkijonon pituus.
      int pituus = teksti.length();

      // Jos pituus oli OK, niin tulostetaan.
      if (pituus > 0) {
         // Tehd��n yl�- ja alarivi.
         String ylaala = "";
         for (int i = 0; i < pituus + 2 * (VALI.length() + 1); i++)
            ylaala += MERKKI_KEHYS;

         // Yl�rivi.
         System.out.println(ylaala);

         // Keskimm�inen rivi.
         System.out.println(MERKKI_KEHYS + VALI + teksti + VALI + MERKKI_KEHYS);

         // Alarivi.
         System.out.println(ylaala);
      }
   }

  
    
    //HTTP client luo http pyynnön
    //Ottaa vastaa WWW-autenticate headerin ja palauttaa sen Mappina.
    public Map httpClientReq(String osoite) throws Exception
    {
        HashMap<String, String> map = new HashMap<String, String>();

        try 
        {
   //         Map<String, String> tietoja = tiedot;
     //       String
            client = new HttpClient();
     //       HttpClient client2 = new HttpClient();
            urlStr = osoite;
            System.out.println("osoite: " + osoite);
            System.out.println("osoite2: " + urlStr);

     //       GetMethod getMethod2 = new GetMethod(urlStr);
            getMethod = new GetMethod(urlStr);
            
            System.out.println("getMethod luotu");

            status = client.executeMethod(getMethod);
            System.out.println("status: " + status);
            responseBody = getMethod.getResponseBodyAsString();
            System.out.println("responseBody: " + responseBody);

            Header wwAuthHeader = getMethod.getResponseHeader("WWW-Authenticate");
            if(wwAuthHeader != null)
            {
                for (HeaderElement element : wwAuthHeader.getElements()) 
                {
                    if(element.getName().contains("Digest")) 
                    {
                        String elementname = element.getName();
                        
                        elementname = elementname.replaceAll("Digest ", "");
                        element.setName(elementname);
                    }
                    System.out.println(element.getName() + ": " + element.getValue());
                    map.put( element.getName(), element.getValue() ); 
                }
            }
            if(responseBody != null || responseBody != "")
            {
    //            map.put( "body", responseBody );
            }
            
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        return map;
    }
    
    //Lähettää http Authorizationin.
    //Palauttaa palvelimelta saadun vastauksen.
    public String httpClientAut(Map tiedot) throws Exception
    {
 //       HashMap<String, String> map = new HashMap<String, String>();
        try 
        {
            Map<String, Object> tietoja = tiedot;
            if(!tiedot.isEmpty())
            {
                userName = (String) tietoja.get("username");
                password = (String) tietoja.get("password");
                host = (String) tietoja.get("host");
                port = (int) tietoja.get(port);
                realm = (String) tietoja.get("realm");
            }
            

            UsernamePasswordCredentials upc = new UsernamePasswordCredentials(userName, password);
            AuthScope as = new AuthScope(host, port, realm);
            client.getState().setCredentials(as, upc);
            status = client.executeMethod(getMethod);
            System.out.println("status: " + status);
            responseBody = getMethod.getResponseBodyAsString();
            System.out.println("responseBody: " + responseBody);

            getMethod.releaseConnection();

        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        return responseBody;

    }
    
    //Lähettää http Authorizationin.
    //Palauttaa palvelimelta saadun vastauksen.
    public String httpClientAut2(Map tiedot, String osoite) throws Exception
    {
        client = new HttpClient();
     //       HttpClient client2 = new HttpClient();
        urlStr = osoite;
        
        getMethod = new GetMethod(urlStr);
        String otsikkotieto = setReguestHeaderf(tiedot);
 //       HashMap<String, String> map = new HashMap<String, String>();
        try 
        {
            /*
            Map<String, Object> tietoja = tiedot;
            if(!tiedot.isEmpty())
            {
                userName = (String) tietoja.get("username");
                password = (String) tietoja.get("password");
                host = (String) tietoja.get("host");
                port = (int) tietoja.get(port);
                realm = (String) tietoja.get("realm");
            }
            */
//Set method kokeilua 
            //getMethod.getResponseHeader("WWW-Authenticate");
        //    getMethod.setRequestHeader("WWW-Authenticate");
            /*
            Header wwAuthHeader = getMethod.getResponseHeader("WWW-Authenticate");
            if(wwAuthHeader != null)
            {
                for (HeaderElement element : wwAuthHeader.getElements()) 
                {
                        System.out.println(element.getName() + ": " + element.getValue());
                        map.put( element.getName(), element.getValue() ); 
                }
            }
            */
   //         Header wwAuthHeader;
            getMethod.setRequestHeader("Authorization", otsikkotieto);
            System.out.println("getmethod sisältää" + ": " + getMethod.getQueryString());
            
       //     UsernamePasswordCredentials upc = new UsernamePasswordCredentials(userName, password);
        //    AuthScope as = new AuthScope(host, port, realm);
        //    client.getState().setCredentials(as, upc);
            status = client.executeMethod(getMethod);
            System.out.println("status: " + status);
            responseBody = getMethod.getResponseBodyAsString();
            System.out.println("responseBody: " + responseBody);

            getMethod.releaseConnection();

        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        return responseBody;

    }
    
     //Luodaan header tiedosto digest arvoista.
        private String setReguestHeaderf(Map tiedot)
        {
    //        HashMap<String, String> map = new HashMap<String, String>();
            String header = "";
            
            String username1 = "";
            String realm1 = "";

            String nonce = "";
            String uri = "";
            String qop = "";
            String nc = "";
     //       int nc = 0;
            String cnonce = "";
            String response = "";
            String opaque = "";
            String algorithm = "";

  //          Map<String, Object> tietoja = tiedot;
            Map<String, Object> tietoja = new HashMap<String, Object>();
            Map<String, Object> tietoja2 = new HashMap<String, Object>();
            Map<String, Object> tietoja3 = new HashMap<String, Object>();
            tietoja = tiedot;
            System.out.println("tietoja mapin sisälto" + ": " + tietoja);
            tietoja2 = (HashMap<String,Object>)tietoja.get("authorization");
            System.out.println("tietoja2 mapin sisälto" + ": " + tietoja2);
            tietoja3 = (HashMap<String,Object>)tietoja2.get("digest-response");
            System.out.println("tietoja3 mapin sisälto" + ": " + tietoja3);
            if(!tietoja3.isEmpty())
            {
                username1 = (String) tietoja3.get("Digest username");
                realm1 = (String) tietoja3.get("realm");
                nonce = (String) tietoja3.get("nonce");
                uri = (String) tietoja3.get("uri");
                qop = (String) tietoja3.get("qop");
             //   nc = (int) tietoja3.get("nc");
                nc = (String) tietoja3.get("nc");
                cnonce = (String) tietoja3.get("cnonce");
                response = (String) tietoja3.get("response");
                opaque = (String) tietoja3.get("opaque");
                algorithm = (String) tietoja3.get("algorithm");
                
            }
            header += "Digest username=\"" + username1 + "\", ";
            header += "realm=\"" + realm1 + "\", ";

            header += "nonce=\"" + nonce + "\", ";
            header += "uri=\"" + uri + "\", ";
            
            if(qop != null && qop != "")
            {
                header += "qop=" + qop + ", ";
            }
            header += "nc=" + nc + ", ";
            header += "cnonce=\"" + cnonce + "\", ";
            header += "response=\"" + response + "\", ";
            
            
      
            if(opaque != null && opaque != "")
            {
                header += "opaque=\"" + opaque + "\", ";
            }
            if(algorithm != null && algorithm != "")
            {
                header += "algorithm=" + algorithm + ", ";
            }
            
            header = removeLastChar(header);
            header = removeLastChar(header);
           // header = header - ",";
            
            System.out.println("header tiedosto" + ": " + header);

            return header;
        }
        
        

public String removeLastChar(String s) {
    if (s == null || s.length() == 0) {
        return s;
    }
    return s.substring(0, s.length()-1);
}


    
    //Pääsilmukka metodien testaamiseen ja ajamiseen jossain järjestyksessä.
    public void suoritaPaasilmukkaa() 
   {

      tulostaOtsikko("HTTP");




        try
        {
            //http client

        //            Map<String, Object> tietoja;
            Map<String, Object> tietoja = new HashMap<String, Object>();
      //      httpClientReq("http://192.168.0.1");
            httpClientReq("http://www.iltalehti.fi");
            System.out.println("jee" + urlStr);
    //        httpClientAut(tietoja);
        //           httpClientReq();
        //          httpClientAut();

            //http server
        //            httpServer();

        }
        catch (Exception e)
        {
           System.out.println(VIESTI_HERJA);
        }
        String komentorivi = READER.nextLine();

      // Lopuksi lyhyet j��hyv�iset.
      System.out.println(VIESTI_JAAHYVAISET);
   }
    
 
}
