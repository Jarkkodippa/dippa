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


/**
 *
 * @author Jarkko
 * Coap pohjainen testi digest asiakas.
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
    

     
     private static Map setAuthorizationHeaderMap(Map tiedot, String uri, 
             String username, String password)
    {


        String realm1 = "";

        String nonce = "";

        String qop = "";
        String nc = "";

        String opaque = "";
        String algorithm = "";

        Map<String, Object> tietoja = new HashMap<String, Object>();
        Map<String, Object> tietoja2 = new HashMap<String, Object>();
        Map<String, Object> tietoja3 = new HashMap<String, Object>();
        Map<String, Object> lopullinen = new HashMap<String, Object>();
        tietoja = tiedot;
        System.out.println("tietoja mapin sisälto" + ": " + tietoja);
        tietoja2 = (HashMap<String,Object>)tietoja.get("www-authenticate");
        System.out.println("tietoja2 mapin sisälto" + ": " + tietoja2);
        tietoja3 = (HashMap<String,Object>)tietoja2.get("challenge");
        System.out.println("tietoja3 mapin sisälto" + ": " + tietoja3);
        if(!tietoja3.isEmpty())
        {

            realm1 = (String) tietoja3.get("realm");
            nonce = (String) tietoja3.get("nonce");

            qop = (String) tietoja3.get("qop");

            opaque = (String) tietoja3.get("opaque");
            algorithm = (String) tietoja3.get("algorithm");

        }

        nc = "00000001";
        String A1 = DigestUtils.md5Hex(username + ":" + realm1 + ":" + password);
        String A2 = DigestUtils.md5Hex("GET" + ":" + uri);

        String nonce2 = calculateNonce();

        String cnonce = nonce2;
        String ncvalue = "00000001";

    //    String responseSeed = A1 + ":" + nonce + ":" + ncvalue + ":" + cnonce + ":" + qop + ":" + A2;
        String responseSeed = A1 + ":" + nonce + ":" + A2;
        String response = DigestUtils.md5Hex(responseSeed);

        lopullinen.put("Digest username", username);
        lopullinen.put("realm", realm1);
        lopullinen.put("nonce", nonce);
        lopullinen.put("uri", uri);       
        lopullinen.put("cnonce", cnonce);
        lopullinen.put("nc", nc);
        lopullinen.put("response", response);
        if(tietoja3.get("qop") != null && tietoja3.get("qop") != "")
        {
            lopullinen.put("qop", qop);
        }
        if(tietoja3.get("opaque") != null && tietoja3.get("opaque") != "")
        {
            lopullinen.put("opaque", opaque);
        }
        if(tietoja3.get("algorithm") != null && tietoja3.get("algorithm") != "")
        {
            lopullinen.put("algorithm", algorithm);
        }


        return lopullinen;
    }
 
   public static void main(String[] args) 
   {
       
       Map<String, Object> authmap = new HashMap<String,Object>();

        String osoiten = "coap://192.168.0.70/Yhteys/";


        String tarkenne = "192.168.0.112/priv/index.html";
       
        args = new String[2];

        coaptoteutus coap = new coaptoteutus();
        osoiten = osoiten + tarkenne;
        
        try
        {
            tarkenne = URLEncoder.encode(tarkenne, "UTF-8");
            

            args[0] = "POST";
      //      args[0] = "DISCOVER";
      //      args[0] = "OBSERVE";
      //      args[0] = "GET";
      //      args[0] = "PUT";
            args[1] = osoiten; //"coap://localhost";

            System.out.println("ooite " + ": " + args[1]);

            String Servuvastaus = coap.runCoap(args);
            System.out.println("vastaus " + ": " + Servuvastaus);
            authmap = json.readJSON(Servuvastaus);
        }
        catch (Exception e)
        {
            System.err.println("virhe1");
        }
        
        Map authheader = setAuthorizationHeaderMap(authmap, "/priv/index.html", "testi", "a");
        
        System.out.println("authheader " + ": " + authheader);
        
        
       
        try
        {
            args = new String[3];
        
            args[0] = "PUT";

            args[1] = osoiten;
            String palautus2 = json.writeJSONauthentication(authheader);

            args[2] = palautus2;

            String Servuvastaus = coap.runCoap(args);
            System.out.println("vastaus " + ": " + Servuvastaus);
        }
        catch (Exception e)
        {
            System.err.println("virhe2");
        }
        
        args = new String[2];
        
        args[0] = "DISCOVER";
 
        args[1] = "coap://192.168.0.70/";

       
        
        System.out.println("url " + ": " + args[1]);

        
        try
        {

            coap.runCoap(args);
        }
        catch (Exception e)
        {
            System.err.println("virhe3");
        }
 
   }
}
