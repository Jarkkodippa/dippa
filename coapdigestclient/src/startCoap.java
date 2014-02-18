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

//säätöövarten
import java.net.HttpURLConnection;
import java.net.URL;
// The GBA client API itself
import net.ericsson.labs.gba.client.GbaClient;

// Application specific API. HTTP Digest in this case
import net.ericsson.labs.gba.client.GbaHttpDigestMD5;
//säätöpäättyy


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

  //      String osoiten = "coap://192.168.0.70/Yhteys/";
        String osoiten = "coap://localhost/Yhteys/";

  //      String tarkenne = "192.168.0.112/priv/index.html";
        String tarkenne = "94.237.64.168:804/priv/index.html";
       
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
        
        ////säätöö
        /*
        // Insert your own IMPI and (AKA) key for your applications
        String myImpi = "test.user@labs.ericsson.net";
        String myKey = "93ab7cdf014401d44f0b673e11790ad5";

        // Use your NAF application FQDN and listening port
        String myNafFqdn = "naf.labs.ericsson.net";
        String nafUrl = "http://" +myNafFqdn+ ":8080/gbanaf/";

        try {
            // Create an GbaClient instance
            GbaClient gbaclient = new GbaClient(myImpi, myKey);

            //Bootstrap and keep its context ID in btid
            String btid = gbaclient.bootstrap();
            System.out.println(gbaclient.printBootstrapContext());

            //Get application secret key ksNaf 
            byte[] ksNaf = gbaclient.getKsNaf(myNafFqdn);

            //Run HTTP Digest Authentication with the KsNaf and btid
            boolean authResult = runUaHttpDigest(nafUrl, btid, ksNaf);
            System.out.println("HTTP Digest Auth result: " + authResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
                */
        ////////säätöpäättyy
        
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
   
    private static boolean runUaHttpDigest(
            String nafUrl,String btid, byte[] ksNaf) throws Exception 
    {

        // First GET over Ua
        URL httpUrl = new URL(nafUrl);
        HttpURLConnection http = (HttpURLConnection) httpUrl.openConnection();
        http.setRequestMethod("GET");
        http.connect();
        if (http.getResponseCode() != 401) {
            throw new Exception("Unexpected HTTP response");
        }

        //Create a helper class for HttpDigest
        GbaHttpDigestMD5 httpDigest = new GbaHttpDigestMD5(nafUrl, btid, ksNaf);
        //Respond with Authorization header
        System.out.println(http.getHeaderField("WWW-Authenticate"));
        httpDigest.computeDigestResponse(
                http.getHeaderField("WWW-Authenticate"));
        String authorizationHeader = httpDigest.generateAuthorizationHeader();
        System.out.println("Authorization: " + authorizationHeader);

        //Second GET
        http = (HttpURLConnection) httpUrl.openConnection();
        http.setRequestMethod("GET");
        http.addRequestProperty("Authorization", authorizationHeader);
        http.connect();
        if (http.getResponseCode() == 200) {
            return true;
        } else {
            return false;
        }
    }
}
