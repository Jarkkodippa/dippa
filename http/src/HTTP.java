
import com.sun.net.httpserver.HttpServer;


import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.codec.DecoderException;

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

    private static String urlStr = "http://example.com:8080/abc/";
    private static String host = "example.com";
    private static String realm = "ExampleRealm";
    private static String userName = "admin";
    private static String password = "jeeee";
    private static int port = 8080;
    private static HttpClient client;
    private static int status;
    private static GetMethod getMethod;
    private static String responseBody;

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
    static Map httpClientReq(String osoite) throws Exception
    {
        HashMap<String, String> map = new HashMap<String, String>();

        try 
        {
   //         Map<String, String> tietoja = tiedot;
     //       String
            client = new HttpClient();
            urlStr = osoite;
            System.out.println("osoite: " + osoite);

            getMethod = new GetMethod(urlStr);

            status = client.executeMethod(getMethod);
            System.out.println("status: " + status);
            responseBody = getMethod.getResponseBodyAsString();
            System.out.println("responseBody: " + responseBody);

            Header wwAuthHeader = getMethod.getResponseHeader("WWW-Authenticate");
            for (HeaderElement element : wwAuthHeader.getElements()) 
            {
                    System.out.println(element.getName() + ": " + element.getValue());
                    map.put( element.getName(), element.getValue() ); 
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
    static String httpClientAut(Map tiedot) throws Exception
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
    
    
    //Pääsilmukka metodien testaamiseen ja ajamiseen jossain järjestyksessä.
    public void suoritaPaasilmukkaa() 
   {

      tulostaOtsikko("HTTP");




        try
        {
            //http client

        //            Map<String, Object> tietoja;
            Map<String, Object> tietoja = new HashMap<String, Object>();
            httpClientReq("http://192.168.0.1");
            System.out.println("jee" + urlStr);
            httpClientAut(tietoja);
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
