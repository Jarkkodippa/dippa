/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.Random;


import net.java.sip.communicator.sip.security.Milenage;

//import org.snmp4j.smi.OctetString;
//säätöövarten
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
// The GBA client API itself

import org.apache.commons.codec.binary.Base64;

// Application specific API. HTTP Digest in this case
import net.ericsson.labs.gba.client.GbaHttpDigestMD5;
import static net.java.sip.communicator.sip.security.Milenage.computeOpC;
import org.apache.commons.codec.binary.Hex;
//säätöpäättyy

//import org.snmp4j.smi.OctetString;
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
    
    public static String calculateAKARES(String nonce, String password)
    {
        Milenage muuttaja = new Milenage();

        String OP = "00000000000000000000000000000000";


        try
        {
            byte[] opennonce = Base64.decodeBase64(nonce);


            int arraylength = opennonce.length;
            byte[] arraysrand = Arrays.copyOfRange(opennonce, 0, 16);
            byte[] arraysautn = Arrays.copyOfRange(opennonce, 16, 32);

            //SQN on 48bit
            byte[] sqn = Arrays.copyOfRange(arraysautn, 0, 6);
            //amf on 16bit
            byte[] amf = Arrays.copyOfRange(arraysautn, 6, 8);

            byte[] passwordbyte = Hex.decodeHex(password.toCharArray());
            //password.getBytes("UTF-8");

            byte[] OPbyte = Hex.decodeHex(OP.toCharArray());               
            byte[] OPc = computeOpC(passwordbyte, OPbyte);
            byte[] RESadamen =  muuttaja.f2(passwordbyte, arraysrand, OPc);
            byte[] CKadamen =  muuttaja.f3(passwordbyte, arraysrand, OPc);
            byte[] IKSadamen =  muuttaja.f4(passwordbyte, arraysrand, OPc);
            byte[] AKSadamen =  muuttaja.f5(passwordbyte, arraysrand, OPc);

            sqn = xorWithKey(sqn, AKSadamen);
            byte[] f1adamen =  muuttaja.f1(passwordbyte, arraysrand, OPc, sqn, amf);

            byte[] f1adamen2 = Hex.encodeHexString( f1adamen ).getBytes("UTF-8");
            byte[] RESadamen2 =  Hex.encodeHexString( RESadamen ).getBytes("UTF-8");
            byte[] CKadamen2 = Hex.encodeHexString( CKadamen ).getBytes("UTF-8");    
            byte[] IKSadamen2 = Hex.encodeHexString( IKSadamen ).getBytes("UTF-8");
            byte[] AKSadamen2 = Hex.encodeHexString( AKSadamen ).getBytes("UTF-8");
            byte[] arraysautn2 = Hex.encodeHexString( arraysautn ).getBytes("UTF-8");
            byte[] arraysrand2 = Hex.encodeHexString( arraysrand ).getBytes("UTF-8");
            byte[] OPc2 = Hex.encodeHexString( OPc ).getBytes("UTF-8");
            byte[] sqn2 = Hex.encodeHexString( sqn ).getBytes("UTF-8");
            byte[] opennonce2 = Hex.encodeHexString( opennonce ).getBytes("UTF-8");
            byte[] amf2 = Hex.encodeHexString( amf ).getBytes("UTF-8");


            System.out.println("opennonce arvo: "+ arraylength );
            System.out.println("rand lengt: "+ arraysrand.length );
            System.out.println("autn lengt: "+ arraysautn.length );
            System.out.println("nonce arvo: "+ nonce );
            System.out.println("opennonce arvo: "+ new String(opennonce2, "UTF-8") );
            System.out.println("password arvo: "+ new String(passwordbyte, "UTF-8") );
            System.out.println("arraysrand arvo: "+ new String(arraysrand2, "UTF-8") );
            System.out.println("arraysautn arvo: "+ new String(arraysautn2, "UTF-8") );
            System.out.println("OP arvo: "+ new String(OPbyte, "UTF-8") );
            System.out.println("OPc arvo: "+ new String(OPc2, "UTF-8") );
            System.out.println("RESadamen arvo: "+ new String(RESadamen2, "UTF-8") );
            System.out.println("sqn arvo: "+ new String(sqn2, "UTF-8") );
            System.out.println("AKSadamen arvo: "+ new String(AKSadamen2, "UTF-8") );
            System.out.println("f1adamen arvo: "+ new String(f1adamen2, "UTF-8") );
            System.out.println("amf arvo: "+ new String(amf2, "UTF-8") );
            
            return new String(RESadamen2, "UTF-8");
        }
        catch(Exception e)
        {
            System.out.println("KD virhe ");
        }
        
        return "";
    }
    
    private static byte[] xorWithKey(byte[] a, byte[] key) 
    {
        byte[] out = new byte[a.length];
        for (int i = 0; i < a.length; i++) 
        {
            out[i] = (byte) (a[i] ^ key[i%key.length]);
        }
        return out;
    }
    

     private static Map setAuthorizationHeaderMap(Map tiedot, String uri, 
             String username, String password)
    {
        String realm1 = "";
        String nonce = "";
        String qop = "";
        String nc = "00000001";
        String opaque = "";
        String algorithm = "";
        String response = "";

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

        String cnonce = calculateNonce();

        if(algorithm.equals("AKAv1-MD5"))
        {
            
            
            try
            {

                String res = calculateAKARES(nonce, password);
                String A2 = DigestUtils.md5Hex("GET" + ":" + uri + ":"+ DigestUtils.md5Hex(""));
                String A1 = DigestUtils.md5Hex(username + ":" + realm1 + ":" + res);
                String responseSeed = A1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + A2;
                response = DigestUtils.md5Hex(responseSeed);
                
                
            }
            catch(Exception e)
            {
                System.out.println("KD virhe ");
            }
            
        }
        else
        {
            String A1 = DigestUtils.md5Hex(username + ":" + realm1 + ":" + password);
            String A2 = DigestUtils.md5Hex("GET" + ":" + uri);
            String responseSeed = A1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + A2;
            response = DigestUtils.md5Hex(responseSeed);
        }
        

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
     
     private static Map setbtAuthorizationHeaderMap(String uri, 
             String username, String realm)
    {


        String realm1 = "";

        String nonce = "";

        String qop = "";
        String nc = "";

        String opaque = "";
        String response = "";

      
        Map<String, Object> lopullinen = new HashMap<String, Object>();
  
        lopullinen.put("Digest username", username);
        lopullinen.put("realm", realm);
        lopullinen.put("nonce", nonce);
        lopullinen.put("uri", uri); 
        lopullinen.put("response", response); 
     

        return lopullinen;
    }
     
     
     private static boolean runUaHttpDigest( String nafUrl, 
            String btid, byte[] ksNaf)  throws Exception
    {

        // First GET over Ua
        URL httpUrl = new URL(nafUrl);
        HttpURLConnection http = (HttpURLConnection) httpUrl.openConnection();
        http.setRequestMethod("GET");
        http.connect();
        if (http.getResponseCode() != 401) 
        {
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
        if (http.getResponseCode() == 200) 
        {
            return true;
        } else 
        {
            return false;
        }
    }
 
   public static void main(String[] args) 
   {
      
       Map<String, Object> authmap = new HashMap<String,Object>();
       Map<String, Object> authheader = new HashMap<String,Object>();

       /*
       IMPI: tut.test1@p133.piuha.net
IMPU: sip:tut.test1@p133.piuha.net
key: 41434443524f58594f5552534f583031
       BSF palvelu: http://p133.piuha.net:8080/bsf/bootstrap
       */
 //       String osoiten = "coap://192.168.0.70/Yhteys/";
//        String akaosoiten = "coap://192.168.0.70/Btyhteys/";
//        String akaosoiten = "coap://localhost/Btyhteys/";
       String akaosoiten = "coap://localhost/Yhteys/";
  //      String osoiten = "http://p133.piuha.net:8080/bsf/bootstrap";
        String osoiten = "coap://localhost/Yhteys/";

//        String tarkenne = "192.168.0.112/priv/index.html";
        String tarkenne = "94.237.64.168:804/priv/index.html";
        String tarkenne1 = "p133.piuha.net:8080/bsf/bootstrap";
       
        args = new String[2];

        coaptoteutus coap = new coaptoteutus();
        osoiten = osoiten + tarkenne;
        akaosoiten = akaosoiten + tarkenne1;
        
        try
        {
 //           tarkenne = URLEncoder.encode(tarkenne, "UTF-8");
            

     //       args[0] = "POST";
      //      args[0] = "DISCOVER";
      //      args[0] = "OBSERVE";
            args[0] = "GET";
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
        
        authheader = setAuthorizationHeaderMap(authmap, "/priv/index.html", "testi", "a");
        
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
        
        
        ///////////tästä AKA digest
        
 //       Map authheader = setAuthorizationHeaderMap(authmap, "/bsf/bootstrap", "tut.test1@p133.piuha.net", "41434443524f58594f5552534f583031");
  //      authheader = setbtAuthorizationHeaderMap("/bsf/bootstrap", "tut.test1@p133.piuha.net", "ims.ericsson.com");
        authheader = setbtAuthorizationHeaderMap("/bsf/bootstrap", "tut.test1@p133.piuha.net", "p133.piuha.net");
       //String uri, String username, String realm)
        
        System.out.println("authheader " + ": " + authheader);
        
        
       
        try
        {
            args = new String[3];
        
            args[0] = "PUT";

            args[1] = akaosoiten;
            String palautus2 = json.writeJSONbtauthentication(authheader);

            args[2] = palautus2;

            String Servuvastaus = coap.runCoap(args);
            System.out.println("vastaus " + ": " + Servuvastaus);
            if(Servuvastaus.contains("www-authenticate"))
            {
                authmap = json.readJSON(Servuvastaus);
            }
            
        }
        catch (Exception e)
        {
            System.err.println("virhe2");
        }
        
        
        
          authheader = setAuthorizationHeaderMap(authmap, "/bsf/bootstrap", "tut.test1@p133.piuha.net", "41434443524f58594f5552534f583031");
 //       Map authheader = setAuthorizationHeaderMap(authmap, "/bsf/bootstrap", "tut.test1@p133.piuha.net", "41434443524f58594f5552534f583031");
 //       Map authheader = setbtAuthorizationHeaderMap("/bsf/bootstrap", "tut.test1@p133.piuha.net", "ims.ericsson.com");
       //String uri, String username, String realm)
        
        System.out.println("authheader " + ": " + authheader);
        
        
       
        try
        {
            args = new String[3];
        
            args[0] = "PUT";

            args[1] = akaosoiten;
            String palautus2 = json.writeJSONbtauthentication(authheader);

            args[2] = palautus2;

            String Servuvastaus = coap.runCoap(args);
            System.out.println("vastaus " + ": " + Servuvastaus);
            if(Servuvastaus.contains("www-authenticate"))
            {
                authmap = json.readJSON(Servuvastaus);
            }
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
*/
       /*
       Tässä nämä

IMPI: tut.test1@p133.piuha.net
IMPU: sip:tut.test1@p133.piuha.net
key: 41434443524f58594f5552534f583031

IMPI: tut.test2@p133.piuha.net
IMPU: sip:tut.test2@p133.piuha.net
key: 41434443524f58594f5552534f583032

BSF palvelu: http://p133.piuha.net:8080/bsf/bootstrap
       
       */
       /*
       // Insert your own IMPI and (AKA) key for your applications
        String myImpi = "tut.test1@p133.piuha.net";
        String myKey = "41434443524f58594f5552534f583031";

        // Use your NAF application FQDN and listening port
  //      String myNafFqdn = "http://p133.piuha.net:8080/bsf/bootstrap";
        String myNafFqdn = "naf.labs.ericsson.net";
        String nafUrl = "http://p133.piuha.net:8080/bsf/bootstrap";
        try {
   //         Sim sim = new (myImpi, myKey);
            // Create an GbaClient instance
            GbaClient gbaclient = new GbaClient(myImpi, myKey);
  //          GbaClient gbaclient1 = new GbaClient(sim);
       //     gbaclient.

            System.out.println("asiakas luotu ");
      //      String btidn = gbaclient.getCurrentBTID();
 //           System.out.println("btid "+ btidn);
            //Bootstrap and keep its context ID in btid
            String btid = gbaclient.bootstrap();
   //         gbaclient.getKsNaf(myNafFqdn)
            System.out.println(gbaclient.printBootstrapContext());

            System.out.println("btid luotu ");
            //Get application secret key ksNaf 
            byte[] ksNaf = gbaclient.getKsNaf(myNafFqdn);

            //Run HTTP Digest Authentication with the KsNaf and btid
 //           boolean authResult = runUaHttpDigest(nafUrl, btid, ksNaf);
   //         System.out.println("HTTP Digest Auth result: " + authResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
         */     
        ////////säätöpäättyy
        /*
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
   */
   }
}
