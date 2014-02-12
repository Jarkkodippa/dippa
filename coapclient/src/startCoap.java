/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.net.URLEncoder;
import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.Random;

//import com.fasterxml.jackson.core.JsonFactory;
//import com.fasterxml.jackson.core.JsonGenerator;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;


/**
 *
 * @author Jarkko
 * testaamiseen kaikkee turhaa säätöö ei tarvi välittää.
 */
public class startCoap 
{

    public static String calculateNonce()
    {
        Date d = new Date();
        SimpleDateFormat f = new SimpleDateFormat("yyyy:MM:dd:hh:mm:ss");
        String fmtDate = f.format(d);
        Random rand = new Random(100000);
        Integer randomInt = rand.nextInt();
        return DigestUtils.md5Hex(fmtDate + randomInt.toString());
    }
    
     private static String setAuthorizationHeader(Map tiedot, String uri, 
             String username, String password)
        {
    //        HashMap<String, String> map = new HashMap<String, String>();
            String header = "";
            
   //         String username1 = "";
            String realm1 = "";

            String nonce = "";
   //         String uri = "";
            String qop = "";
            String nc = "";
 //           String cnonce = "";
  //          String response = "";
            String opaque = "";

   //         Map<String, Object> challenge = new HashMap<String, Object>();
            Map<String, Object> tietoja = new HashMap<String, Object>();
            Map<String, Object> tietoja2 = new HashMap<String, Object>();
            Map<String, Object> tietoja3 = new HashMap<String, Object>();
            tietoja = tiedot;
            System.out.println("tietoja mapin sisälto" + ": " + tietoja);
            tietoja2 = (HashMap<String,Object>)tietoja.get("www-authenticate");
            System.out.println("tietoja2 mapin sisälto" + ": " + tietoja2);
            tietoja3 = (HashMap<String,Object>)tietoja2.get("challenge");
            System.out.println("tietoja3 mapin sisälto" + ": " + tietoja3);
            if(!tietoja3.isEmpty())
            {
    //            username1 = (String) tietoja.get("username");
                realm1 = (String) tietoja3.get("realm");
                nonce = (String) tietoja3.get("nonce");
 //               uri = (String) tietoja3.get("uri");
                qop = (String) tietoja3.get("qop");
     //           nc = (String) tietoja3.get("nc");
       //         cnonce = (String) tietoja.get("cnonce");
    //            response = (String) tietoja.get("response");
                opaque = (String) tietoja3.get("opaque");
                
                
            }
            nc = "00000001";
            String A1 = DigestUtils.md5Hex(username + ":" + realm1 + ":" + password);
            String A2 = DigestUtils.md5Hex("GET" + ":" + uri);
            
            String nonce2 = calculateNonce();
     //       String cnonce = Integer.toString(Math.abs(new Random().nextInt()));
            String cnonce = nonce2;
            String ncvalue = "00000001";
            
            String responseSeed = A1 + ":" + nonce + ":" + ncvalue + ":" + cnonce + ":" + qop + ":" + A2;
            String response = DigestUtils.md5Hex(responseSeed);
            
            header += "Digest username=\"" + username + "\",";
            header += "realm=\"" + realm1 + "\",";
            header += "nonce=\"" + nonce + "\",";
            header += "uri=\"" + uri + "\",";         
            header += "cnonce=\"" + cnonce + "\",";
            header += "nc=\"" + nc + "\",";
            header += "response=\"" + response + "\",";
            header += "qop=\"" + qop + "\",";
            header += "opaque=\"" + opaque + "\"";

            return header;
        }
     //jee
   public static void main(String[] args) 
   {
       
       Map<String, Object> authmap = new HashMap<String,Object>();
        String url = "jee";
        String viesti = "testi";
  //      coap servu = new coap();
        //http://" +myNafFqdn+ ":5683/
  //      String osoite = "coap://localhost";
   //     String osoite = "coap://localhost/vaarinpain";
  //      String osoite = "coap://localhost/Yhteys/foo.bar.com/httpresurssi/";
        String osoiten = "coap://localhost/Yhteys/";
  //      String osoite1 = "localhost/helloWorld";
  //      int osoite1 = osoite.length();
        String kaannettava = "jee";
      //  String tarkenne = "laitettu dataa";
   //     String tarkenne = "foo.bar.com/httpresurssi/";
//        String tarkenne = "192.168.0.112";
        String tarkenne = "192.168.0.112/priv/index.html";
       
        args = new String[2];
 //       System.out.println("url " + ": " + args[1]);
        url = args[1];
  //      System.out.println("viesti " + ": " + args[2]);
  //      viesti = args[2];
        coaptoteutus coap = new coaptoteutus();
        try
        {
            tarkenne = URLEncoder.encode(tarkenne, "UTF-8");
            osoiten = osoiten + tarkenne;
    //    String tarkenne = "/kokeilu/";
        //ExampleClient POST coap://vs0.inf.ethz.ch:5683/storage my data"
            args[0] = "POST";
      //      args[0] = "DISCOVER";
      //      args[0] = "OBSERVE";
      //      args[0] = "GET";
      //      args[0] = "PUT";
            args[1] = osoiten; //"coap://localhost";

            System.out.println("ooite " + ": " + args[1]);
      //      args[2] = "vaarinpain";
     //       args[2] = "provResource";
      //      args[2] = tarkenne;
      //      args[2] = "";

            
  //          servu.runCoapserver();
            String Servuvastaus = coap.runCoap(args);
            System.out.println("vastaus " + ": " + Servuvastaus);
            authmap = json.readJSON(Servuvastaus);
        }
        catch (Exception e)
        {
            System.err.println("virhe1");
        }
        
        String authheader = setAuthorizationHeader(authmap, "http://192.168.0.112/priv/index.html", "testi", "a");
        
        System.out.println("authheader " + ": " + authheader);
        
        args = new String[2];
        
        args[0] = "DISCOVER";
 
        args[1] = "coap://localhost";

       
        
        System.out.println("url " + ": " + args[1]);
        url = args[1];
  //      System.out.println("viesti " + ": " + args[2]);
  //      viesti = args[2];
        
        try
        {
  //          servu.runCoapserver();
            coap.runCoap(args);
        }
        catch (Exception e)
        {
            System.err.println("virhe2");
        }
   /*     
        args = new String[3];
        
        args[0] = "PUT";
        tarkenne = "laitettu dataa";
 
        osoite = "coap://localhost/Yhteys/foo.bar.com/httpresurssi/";
  //      osoite = "coap://localhost/Yhteys/";
        args[1] = osoite;

        args[2] = tarkenne;
       
      
        url = args[1];
  //      System.out.println("viesti " + ": " + args[2]);
  //      viesti = args[2];
        
        try
        {
  //          servu.runCoapserver();
            coap.runCoap(args);
        }
        catch (Exception e)
        {
            System.err.println("virhe");
        }
      
        args = new String[2];
        args[0] = "GET";
 
        osoite = "coap://localhost/Yhteys/foo.bar.com/httpresurssi/";
  //      osoite = "coap://localhost/Yhteys/";
        args[1] = osoite;

       
      
        url = args[1];
  //      System.out.println("viesti " + ": " + args[2]);
  //      viesti = args[2];
        
        try
        {
  //          servu.runCoapserver();
            coap.runCoap(args);
        }
        catch (Exception e)
        {
            System.err.println("virhe");
        }
*/
   }
}
