
import com.sun.org.apache.xerces.internal.parsers.DOMParser;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.Random;


import net.java.sip.communicator.sip.security.Milenage;

import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import static net.java.sip.communicator.sip.security.Milenage.computeOpC;
import org.apache.commons.codec.binary.Hex;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Jarkko
 * Coap pohjainen testi digest asiakas.
 */
public class startCoap 
{
    static byte[] Ks = "871fe57f89d78485f941d1e6cdee5d8c5286423ced5900005958cc1bfed20bec".getBytes();
    static String KsS = "";
    static String randkdf = "";
    
    //Calculate random nonce
    public static String calculateNonce()
    {
        Date d = new Date();
        SimpleDateFormat f = new SimpleDateFormat("yyyy:MM:dd:hh:mm:ss");
        String fmtDate = f.format(d);
        Random rand = new Random(100000);
        Integer randomInt = rand.nextInt();
        return DigestUtils.md5Hex(fmtDate + randomInt.toString());
    }
   
  
    //Convert hexString to bytearray
    //Return byte array.
    public static byte[] hexStringToByteArray(String s) 
    {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    
    //Calculate Key Derivation Function. 
    //Return Base64 encode KDF String.
    public static String kdfEncode1(String Ks, String rand, String impi, 
                                    String nafid, String gbatype)
    {

       final byte[] FC = {(byte) 0x01};
       
       final byte[] nollat = {(byte) 0x00};

       String RES = "";

       try
       {
           byte[] bgbadigest = Ks.getBytes("UTF-8");
           
           byte[] P0 = gbatype.getBytes("UTF-8");
           byte[] P1 = hexStringToByteArray(rand);
           byte[] P2 = impi.getBytes("UTF-8");
           byte[] P3 = nafid.getBytes("UTF-8");
           System.out.println("P0: "+ new String(P0, "UTF-8")  );
           System.out.println("P1: "+ new String(P1, "UTF-8")  );
           System.out.println("P2: "+ new String(P2, "UTF-8")  );
           System.out.println("P3: "+ new String(P3, "UTF-8")  );

           int L0 = P0.length;
           int L1 = P1.length;
           int L2 = P2.length;
           int L3 = P3.length;
           
           byte[] L0x = BigInteger.valueOf(L0).toByteArray();
           byte[] L1x = BigInteger.valueOf(L1).toByteArray();
           byte[] L2x = BigInteger.valueOf(L2).toByteArray();
           byte[] L3x = BigInteger.valueOf(L3).toByteArray();
   
           String S = Hex.encodeHexString( FC ) +
                   ""+ Hex.encodeHexString( P0 ) +
                   ""+ Hex.encodeHexString(nollat) +
                   ""+ Hex.encodeHexString( L0x ) +
                   ""+ Hex.encodeHexString(P1) +
                   ""+ Hex.encodeHexString(nollat) +
                   ""+ Hex.encodeHexString( L1x ) +
                   ""+ Hex.encodeHexString(P2) +
                   ""+ Hex.encodeHexString(nollat) +
                   ""+ Hex.encodeHexString( L2x ) +
                   ""+ Hex.encodeHexString(P3) +
                   ""+ Hex.encodeHexString(nollat) +
                   ""+ Hex.encodeHexString( L3x );

           byte[] BSx = Hex.decodeHex(S.toCharArray());
           byte[] KSx = Hex.decodeHex(Ks.toCharArray());

           Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
           SecretKeySpec secret_key = new SecretKeySpec(KSx, "HmacSHA256");
           sha256_HMAC.init(secret_key);

           byte[] KDF = sha256_HMAC.doFinal(BSx);
           String KDFS = Hex.encodeHexString( KDF );
           RES = Base64.encodeBase64String(KDF);

           System.out.println("RES arvo: "+ RES );

       }
       catch(Exception e)
       {
           System.err.println("virhe1");
           return "";
       }


       return RES;

    }
    
   
    //Emulate SIM and Calculate AKA-digest response parameter and 
    //keys for future use.
    //Return responce String.
    public static String calculateAKARES(String nonce, String password)
    {
        Milenage muuttaja = new Milenage();

        String OP = "00000000000000000000000000000000";


        try
        {
            byte[] opennonce = Base64.decodeBase64(nonce);

            byte[] arraysrand = Arrays.copyOfRange(opennonce, 0, 16);
            
            
            randkdf = Hex.encodeHexString( arraysrand );

            byte[] arraysautn = Arrays.copyOfRange(opennonce, 16, 32);

            byte[] sqn = Arrays.copyOfRange(arraysautn, 0, 6);
            //amf on 16bit
            byte[] amf = Arrays.copyOfRange(arraysautn, 6, 8);

            byte[] passwordbyte = Hex.decodeHex(password.toCharArray());


            byte[] OPbyte = Hex.decodeHex(OP.toCharArray());               
            byte[] OPc = computeOpC(passwordbyte, OPbyte);
            
            byte[] RESadamen =  muuttaja.f2(passwordbyte, arraysrand, OPc);
            
            byte[] CKadamen =  muuttaja.f3(passwordbyte, arraysrand, OPc);
            byte[] IKSadamen =  muuttaja.f4(passwordbyte, arraysrand, OPc);

            KsS = Hex.encodeHexString( CKadamen )+Hex.encodeHexString( IKSadamen );

            String resstring = Hex.encodeHexString(RESadamen); 
            
            
        
            return resstring;
        }
        catch(Exception e)
        {
            System.out.println("KD virhe 1");
        }
        
        return "";
    }

    //Generate Authorization header map.
    //Take Authenticate header map and server uri, username and password.
    //return map that holds authorization information.
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
        tietoja2 = (HashMap<String,Object>)tietoja.get("www-authenticate");
        tietoja3 = (HashMap<String,Object>)tietoja2.get("challenge");
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

                response = MessageDigestAlgorithm.calculateResponse(algorithm,
                                    username,
                                    realm1,
                                    MessageDigestAlgorithm.decode(res),
                                    nonce,
                                    nc,
                                    cnonce,
                                    "GET",
                                    uri,
                                    "",
                                    qop);
                
                System.out.println("Response : "+ response);
              
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
    
    //Generate besid uri, username and realm, empty authorization haeder.
    //Return gererated header MAP structure.
    private static Map setbtAuthorizationHeaderMap(String uri, 
             String username, String realm)
    {
        String nonce = "";

        String response = "";

      
        Map<String, Object> lopullinen = new HashMap<String, Object>();
  
        lopullinen.put("Digest username", username);
        lopullinen.put("realm", realm);
        lopullinen.put("nonce", nonce);
        lopullinen.put("uri", uri); 
        lopullinen.put("response", response); 
     

        return lopullinen;
    }
     
   
   
    //Run HTTP Get reguest, using given url and proxy.
    private static boolean runHttpget( String url, String proxy)  throws Exception
    {

        Map<String, Object> authmap = new HashMap<String,Object>();
        Map<String, Object> authheader = new HashMap<String,Object>();
        Map<String, Object> content = new HashMap<String,Object>();

        String[] args = new String[2];

        coaptoteutus coap = new coaptoteutus();
        String osoiten = proxy+url;
        
        
        try
        {

            args[0] = "GET";
            args[1] = osoiten; //"coap://localhost";

            System.out.println("ooite " + ": " + args[1]);

            String Servuvastaus = coap.runCoap(args);
            System.out.println("vastaus " + ": " + Servuvastaus);
            authmap = json.readJSON(Servuvastaus);
            if(Servuvastaus.contains("www-authenticate"))
            {
                authmap = json.readJSON(Servuvastaus);
                if(authmap.containsKey("Set-Cookie"))
                {
                    authmap = json.readJSON(Servuvastaus);
                    content.put("Cookie", 
                            (HashMap<String,Object>)authmap.get("Set-Cookie"));
                }
                String inputString = "";
                Scanner input = new Scanner(System.in);
                String tunnus = "testi";
                String avain = "a";
                System.out.print("jos tunnus eri kun \""+tunnus+"\" syötä: ");
                inputString = input.nextLine();
                if(!inputString.equals(""))
                {
                    tunnus = inputString;
                }
                System.out.print("jos avain eri kun \""+avain+"\" syötä: ");
                inputString = input.nextLine();
                if(!inputString.equals(""))
                {
                    avain = inputString;
                }
                System.out.println("url " + ": " + url);

                String uriloppu = url.substring((url.indexOf("/")));
                System.out.println("uriloppu " + ": " + uriloppu);
                authheader = setAuthorizationHeaderMap(authmap, uriloppu, tunnus, avain);
        
                System.out.println("authheader " + ": " + authheader);

                content.put("Authorization", authheader);

                try
                {
                    args = new String[4];

                    args[0] = "PUT";

                    args[1] = osoiten;
                    System.out.println("content " + ": " + content);
                    String palautus2 = json.writeJSONauthentication(content);
                    System.out.println("palautus2 " + ": " + palautus2);

                    args[2] = palautus2;

                    args[3] = "50";

                    Servuvastaus = coap.runCoap(args);
                    System.out.println("vastaus " + ": " + Servuvastaus);
                }
                catch (Exception e)
                {
                    System.err.println("virhe2");
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("virhe1");
        }
        
        
        
        content.clear();
        
        return true;
    }
     
    //Contact bsf server usin given proxy and url.
    //Return bsf given btid value. 
    private static String runaka( String url, String proxy)  throws Exception
    {

        Map<String, Object> authmap = new HashMap<String,Object>();
        Map<String, Object> authheader = new HashMap<String,Object>();
        Map<String, Object> content = new HashMap<String,Object>();
        coaptoteutus coap = new coaptoteutus();
        String[] args = new String[2];
        String akaosoiten = proxy+url;
        authheader = setbtAuthorizationHeaderMap("/bsf/bootstrap", "tut.test1@p133.piuha.net", "p133.piuha.net");
      
        System.out.println("authheader " + ": " + authheader);
        
        content.put("Authorization", authheader);
       
        try
        {
            args = new String[4];
        
            args[0] = "PUT";

            args[1] = akaosoiten;
            String palautus2 = json.writeJSONbtauthentication(content);

            args[2] = palautus2;
            
            args[3] = "50";

            System.out.println("palautus2 " + ": " + palautus2);
            String Servuvastaus = coap.runCoap(args);
            content.clear();
            System.out.println("vastaus " + ": " + Servuvastaus);
            if(Servuvastaus.contains("www-authenticate"))
            {
                authmap = json.readJSON(Servuvastaus);
                content.put("Cookie", 
                        (HashMap<String,Object>)authmap.get("Set-Cookie"));
            }
            
        }
        catch (Exception e)
        {
            System.err.println("virhe2");
        }
        
        
        
        authheader = setAuthorizationHeaderMap(authmap, "/bsf/bootstrap", "tut.test1@p133.piuha.net", "41434443524f58594f5552534f583031");

        System.out.println("authheader " + ": " + authheader);
        
        content.put("Authorization", authheader);
        System.out.println("content " + ": " + content);
        String Servuvastaus = "";
       
        try
        {
            args = new String[4];
        
            args[0] = "PUT";

            args[1] = akaosoiten;
            String palautus2 = json.writeJSONbtauthentication(content);

            args[2] = palautus2;
            
            args[3] = "50";

            Servuvastaus = coap.runCoap(args);
            
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
        
        String btid = "";
        DOMParser parser = new DOMParser();
        try 
        {
            StringReader kokeilu = new StringReader(Servuvastaus);
            
            parser.parse(new InputSource( kokeilu ));
            
            Document doc = parser.getDocument();
       
            
            btid = doc.getDocumentElement().getFirstChild().getTextContent();
            System.out.println(btid);
            
            
        } 
        catch (SAXException e) 
        {
            System.out.println("virtahe1");
        } 
        catch (IOException e) 
        {
            System.err.println("virtahje2");
        }
        
        content.clear();
        
        return btid;
    }
    
    //Make post reguest to server using aka ks_naf and sign content. 
    //Return server answer.
    private static String runakasign( String url, String proxy, 
                                String Ks_naf, String btid)  throws Exception
    {
        String Servuvastaus = "";
        
        coaptoteutus coap = new coaptoteutus();
        String[] args = new String[3];
        String osoiten = proxy+url;

        try
        {

            args[0] = "POST";

            args[1] = osoiten;
            System.out.println("ooite " + ": " + args[1]);

            String inputString = "";
            Scanner input = new Scanner(System.in);
            String sisalto = "jeeee";
            System.out.print("Syötä sisältöä, oletuksena \""+sisalto+"\": ");
            inputString = input.nextLine();
            if(!inputString.equals(""))
            {
                sisalto = inputString;
            }
            args[2] = json.signJWT(sisalto, Ks_naf, btid);

            
            Servuvastaus = coap.runCoap(args);
            Servuvastaus = json.openSignJWT(Servuvastaus, Ks_naf);
            System.out.println("vastaus " + ": " + Servuvastaus);
        }
        catch (Exception e)
        {
            System.err.println("virhe1");
        }  
        
        return Servuvastaus;
    }
      
    //Make post reguest to server using aka ks_naf and encrypt content. 
    //Return server answer.
    private static String runakaEncrypt( String url, String proxy, 
                                String Ks_naf, String btid)  throws Exception
    {
        String Servuvastaus = "";
        
        coaptoteutus coap = new coaptoteutus();
        String[] args = new String[3];
        String osoiten = proxy+url;

        try
        {
            args[0] = "POST";
     
            args[1] = osoiten;
            System.out.println("ooite " + ": " + args[1]);
            byte[] testah = Base64.decodeBase64(Ks_naf);
            Ks_naf = Hex.encodeHexString(testah);
            
            String inputString = "";
            Scanner input = new Scanner(System.in);
            String sisalto = "jeeee";
            System.out.print("Syötä sisältöä, oletuksena \""+sisalto+"\": ");
            inputString = input.nextLine();
            if(!inputString.equals(""))
            {
                sisalto = inputString;
            }
    
            args[2] = json.encryptJWT1(sisalto, Ks_naf, btid); 

            
            Servuvastaus = coap.runCoap(args);
            Servuvastaus = json.uncryptJWE1(Servuvastaus, Ks_naf);
            System.out.println("vastaus " + ": " + Servuvastaus);

        }
        catch (Exception e)
        {
            System.err.println("virhe1");
        }    
        
        return Servuvastaus;
    }
    
    //Fullfil nafid ua security content and calculate ks_naf.
    //Return ks-naf String:
    private static String calculateKsnaf( String nafid, 
            String impi)  throws Exception
    {
        String Ks_naf = "";
         try
       {
          
            byte[] ua = {(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00};
            nafid = "p133.piuha.net" + new String(ua, "UTF-8");
            
            String nafidpal = nafid;
           byte[] kokeilu8 = Base64.decodeBase64(Base64.encodeBase64String(nafidpal.getBytes("UTF-8")));
           
            Ks_naf = kdfEncode1(KsS, randkdf, impi, nafid, "gba-me");
            System.out.println("ksnaf1 " + ": " + Ks_naf);

       }
       catch(Exception e)
       {
           System.err.println("virhe1");
           
       }    
        
        return Ks_naf;
    }
 
   public static void main(String[] args) 
   {
      
       while(true)
       {
           Map<String, Object> authmap = new HashMap<String,Object>();
            Map<String, Object> authheader = new HashMap<String,Object>();
            Map<String, Object> content = new HashMap<String,Object>();


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
             String osoite = "coap://192.168.0.70/Yhteys/";

             String tarkenne = "192.168.0.112/priv/index.html";
     //        String tarkenne = "94.237.64.168:804/priv/index.html";
             String tarkenne1 = "p133.piuha.net:8080/bsf/bootstrap";

             String inputString = "";

             Scanner input = new Scanner(System.in);
             System.out.print("Kirjoita k, jos yhdyskäytävä on \""+osoite+"\". Muuten syötä osoite: ");
            inputString = input.nextLine();
            if(!inputString.equals("k"))
            {
                osoite = inputString;
            }
            
             System.out.print("Kirjoita Get, Aka, Vapaa tai Poistu: ");
             inputString = input.nextLine();
             System.out.println(inputString);
            
             if(inputString.equals("Get"))
             {
                try 
                {
                    
                    System.out.print("jos osoite eri kun "+tarkenne+" syötä: ");
                    inputString = input.nextLine();
                    if(!inputString.equals(""))
                    {
                        tarkenne = inputString;
                    }
                    runHttpget(tarkenne, osoite);
                } 
                catch (Exception ex) 
                {
                    Logger.getLogger(startCoap.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
             if(inputString.equals("Vapaa"))
             {
                try 
                {
                    coaptoteutus coap = new coaptoteutus();
                    
                    int maara = 0;
                    System.out.print("Anna aensin attribuuttien lukumäärä ja syötä mahdolliset yhteystapa, osoite ja sisalto enter väliin painaen. ");
                    inputString = input.nextLine();
                    if(!inputString.equals(""))
                    {
                        maara = Integer.parseInt(inputString);
                    }
                    args = new String[maara];
                    inputString = input.nextLine();
                    if(!inputString.equals(""))
                    {
                        args[0] = inputString;
                    }
                    inputString = input.nextLine();
                    if(!inputString.equals(""))
                    {
                        args[1] = osoite+inputString;
                    }
                    inputString = input.nextLine();
                    if(!inputString.equals(""))
                    {
                        args[2] = inputString;
                    }
                    
                    String Servuvastaus = coap.runCoap(args);
                    System.out.println("vastaus " + ": " + Servuvastaus);
                } 
                catch (Exception ex) 
                {
                    Logger.getLogger(startCoap.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if(inputString.equals("Aka"))
            {

                try 
                {
                    
                    System.out.print("BSF osoite, jos eri kun "+tarkenne1+" syötä: ");
                    inputString = input.nextLine();
                    if(!inputString.equals(""))
                    {
                        tarkenne1 = inputString;
                    }
                    String btid = runaka(tarkenne1, osoite);
                    String impi = "tut.test1@p133.piuha.net";
                    String nafid = "p133.piuha.net";
                    System.out.print("NAF id, jos eri kun "+nafid+" syötä: ");
                    inputString = input.nextLine();
                    if(!inputString.equals(""))
                    {
                        nafid = inputString;
                    }
                    String Ks_naf = calculateKsnaf( nafid, impi);
                    String url = "192.168.0.102:8002/test";
                    System.out.print("NAF osoite, jos eri kun \""+url+"\" syötä: ");
                    inputString = input.nextLine();
                    if(!inputString.equals(""))
                    {
                        url = inputString;
                    }
                    System.out.print("oletuksena salaus, muuten kirjoita allekirjoitus: ");
                    inputString = input.nextLine();
                    if(inputString.equals(""))
                    {
                        runakaEncrypt( url, osoite, 
                                Ks_naf, btid);
                    }
                    if(inputString.equals("allekirjoitus"))
                    {
                        runakasign( url, osoite, 
                                Ks_naf, btid);
                    }
                } 
                catch (Exception ex) 
                {
                    Logger.getLogger(startCoap.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if(inputString.equals("Poistu"))
            {
                break;
            }
       }
       
      

        
   }
}
