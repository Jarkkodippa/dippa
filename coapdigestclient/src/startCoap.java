/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

//import org.snmp4j.smi.OctetString;
//säätöövarten
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
//import javax.swing.text.Document;
// The GBA client API itself

import org.apache.commons.codec.binary.Base64;

// Application specific API. HTTP Digest in this case
import net.ericsson.labs.gba.client.GbaHttpDigestMD5;
import static net.java.sip.communicator.sip.security.AKADigest.xorArray;
import static net.java.sip.communicator.sip.security.Milenage.computeOpC;
import org.apache.commons.codec.binary.Hex;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

//säätöpäättyy

//import org.snmp4j.smi.OctetString;
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
    
    public static String calculateNonce()
    {
        Date d = new Date();
        SimpleDateFormat f = new SimpleDateFormat("yyyy:MM:dd:hh:mm:ss");
        String fmtDate = f.format(d);
        Random rand = new Random(100000);
        Integer randomInt = rand.nextInt();
        return DigestUtils.md5Hex(fmtDate + randomInt.toString());
    }
    
    public static byte[] kdfEncode(String Ks, String key, String randk, String impi, String nafid)
    {

       final byte[] FC = {(byte) 0x01};
       

     //  String gbadigest = "gba-digest";
       

       String RES = "";

       try
       {
           byte[] bgbadigest = Ks.getBytes("UTF-8");
           
           byte[] P0 = key.getBytes("UTF-8");
           byte[] P1 = randk.getBytes("UTF-8");
           byte[] P2 = impi.getBytes("UTF-8");
           byte[] P3 = nafid.getBytes("UTF-8");

           int L0 = P0.length;
           int L1 = P1.length;
           int L2 = P2.length;
           int L3 = P3.length;
   
           String S = Hex.encodeHexString( FC ) +
                   ""+ Hex.encodeHexString( P0 ) +
                   ""+ Integer.toHexString(L0) +
                   ""+ Hex.encodeHexString(P1) +
                   ""+ Integer.toHexString(L1) +
                   ""+ Hex.encodeHexString(P2) +
                   ""+ Integer.toHexString(L2) +
                   ""+ Hex.encodeHexString(P3) +
                   ""+ Integer.toHexString(L3);
           System.out.println("jatkoa: "+ S );

           byte[] BS = S.getBytes("UTF-8");

           Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
           SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA256");
           sha256_HMAC.init(secret_key);

           byte[] KDF = sha256_HMAC.doFinal(BS);
         //  RES = Base64.encodeBase64String(KDF);
           return KDF;
       }
       catch(Exception e)
       {
           System.err.println("virhe: " );
   //        return "";
       }

       byte[] paluu = {(byte) 0x00};
       return paluu;

   //    return RES;

    }
    
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    
    public static String kdfEncode1(String Ks, String rand, String impi, String nafid, String gbatype)
    {

       final byte[] FC = {(byte) 0x01};
       
       final byte[] nollat = {(byte) 0x00};

     //  String gbadigest = "gba-digest";
       

       String RES = "";

       try
       {
           byte[] bgbadigest = Ks.getBytes("UTF-8");
           
           byte[] P0 = gbatype.getBytes("UTF-8");
       //    byte[] P1 = rand.getBytes("UTF-8");
           byte[] P1 = hexStringToByteArray(rand);
         //  P1 = new String(P1, "UTF-8").getBytes("UTF-8");
           byte[] P2 = impi.getBytes("UTF-8");
           byte[] P3 = nafid.getBytes("UTF-8");
        //   System.out.println("P1: "+ Hex.encodeHexString(P1  ) );
           System.out.println("P0: "+ new String(P0, "UTF-8")  );
           System.out.println("P1: "+ new String(P1, "UTF-8")  );
           System.out.println("P2: "+ new String(P2, "UTF-8")  );
           System.out.println("P3: "+ new String(P3, "UTF-8")  );

           int L0 = P0.length;
           int L1 = P1.length;
           int L2 = P2.length;
           int L3 = P3.length;
           //int L3 = P3.length+5;
           
           
          // byte[] ua = {(byte) 0x01,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00};
           
           byte[] L0x = BigInteger.valueOf(L0).toByteArray();
           System.out.println("l0x: "+ Hex.encodeHexString( L0x ) );
           byte[] L1x = BigInteger.valueOf(L1).toByteArray();
           System.out.println("l1x: "+ Hex.encodeHexString( L1x ) );
           byte[] L2x = BigInteger.valueOf(L2).toByteArray();
           System.out.println("l2x: "+ Hex.encodeHexString( L2x ) );
           byte[] L3x = BigInteger.valueOf(L3).toByteArray();
           System.out.println("l3x: "+ Hex.encodeHexString( L3x ) );
   
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
           System.out.println("jatkoa: "+ S );

           byte[] BS = S.getBytes("UTF-8");
           byte[] BSx = Hex.decodeHex(S.toCharArray());

           System.out.println("BS arvo: "+ new String(BS, "UTF-8") );
           System.out.println("BSx arvo: "+ new String(BSx, "UTF-8") );
           
           System.out.println("secret key: "+ Ks );
           Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
           SecretKeySpec secret_key = new SecretKeySpec(Ks.getBytes("UTF-8"), "HmacSHA256");
           sha256_HMAC.init(secret_key);

           byte[] KDF = sha256_HMAC.doFinal(BS);
           String KDFS = Hex.encodeHexString( KDF );
           System.out.println("kdfs arvo: "+ KDFS );
   //        System.out.println("KDF: "+ Hex.encodeHexString( KDF ) );
    //       System.out.println("KDF arvo: "+ new String(KDF, "UTF-8")  );
           RES = Base64.encodeBase64String(KDFS.getBytes("UTF-8"));
    //       RES = KDFS.
           System.out.println("RES arvo: "+ RES );
           
           
         // String nafidpal = Hex.encodeHexString(P3) + Hex.encodeHexString(ua);
   //        String nafidpal = nafid + new String(ua, "UTF-8");
           
       
       }
       catch(Exception e)
       {
           System.err.println("virhe1");
           return "";
       }


       return RES;

    }
    
    public static byte[] calculateAKARES1(String nonce, String password)
    {
        Milenage muuttaja = new Milenage();

        String OP = "00000000000000000000000000000000";

        byte[] sres = "paljon kaikkee alustettavaa".getBytes();
        

        try
        {
            byte[] opennonce = Base64.decodeBase64(nonce);


            int arraylength = opennonce.length;
            byte[] arraysrand = Arrays.copyOfRange(opennonce, 0, 16);
            byte[] arraysautn = Arrays.copyOfRange(opennonce, 16, 32);

   //         boolean testi = networkAuthenticated(arraysrand, arraysautn);
            //SQN on 48bit
            byte[] sqn = Arrays.copyOfRange(arraysautn, 0, 6);
            //amf on 16bit
            byte[] amf = Arrays.copyOfRange(arraysautn, 6, 8);

            byte[] passwordbyte = Hex.decodeHex(password.toCharArray());
            //password.getBytes("UTF-8");

            byte[] OPbyte = Hex.decodeHex(OP.toCharArray());               
            byte[] OPc = computeOpC(passwordbyte, OPbyte);
            
            byte[] RESadamen =  muuttaja.f2(passwordbyte, arraysrand, OPc);
            
            int reslength = RESadamen.length;
            
            byte[] CKadamen =  muuttaja.f3(passwordbyte, arraysrand, OPc);
            byte[] IKSadamen =  muuttaja.f4(passwordbyte, arraysrand, OPc);
            byte[] AKSadamen =  muuttaja.f5(passwordbyte, arraysrand, OPc);
            
            

   //         sqn = xorWithKey(sqn, AKSadamen);
            
            byte[] f1adamen =  muuttaja.f1(passwordbyte, arraysrand, OPc, sqn, amf);
// TODO: Poista tarpeettomat
        
            String resstring = Hex.encodeHexString(RESadamen); 
            
            sres = Hex.decodeHex(resstring.toCharArray());


            int sreslength = sres.length;
            
            
            byte[] sres1 = Arrays.copyOfRange(RESadamen, 0, 4);
            
            byte[] sres2 = Arrays.copyOfRange(RESadamen, 4, 8);
            
            sres = xorArray(sres1, sres2);
     //       return RESadamen;
            
            
  //          return resstring;
     //       return new String(RESadamen2, "UTF-8");
        }
        catch(Exception e)
        {
            System.out.println("KD virhe2 ");
        }
        return sres;
 //       return [];
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
            
            
            randkdf = Hex.encodeHexString( arraysrand );

            System.out.println("nonce arvo: "+ randkdf );
          //  randkdf = new String(arraysrand, "UTF-8");
          //  System.out.println("nonce arvo: "+ randkdf );
            
            byte[] arraysautn = Arrays.copyOfRange(opennonce, 16, 32);

            int randlength = arraysrand.length;
            byte[] randlengthx = BigInteger.valueOf(randlength).toByteArray();
            System.out.println("randlengthx: "+ randlength);
   //         boolean testi = networkAuthenticated(arraysrand, arraysautn);
            //SQN on 48bit
            byte[] sqn = Arrays.copyOfRange(arraysautn, 0, 6);
            //amf on 16bit
            byte[] amf = Arrays.copyOfRange(arraysautn, 6, 8);

            byte[] passwordbyte = Hex.decodeHex(password.toCharArray());
            //password.getBytes("UTF-8");

            byte[] OPbyte = Hex.decodeHex(OP.toCharArray());               
            byte[] OPc = computeOpC(passwordbyte, OPbyte);
            
            byte[] RESadamen =  muuttaja.f2(passwordbyte, arraysrand, OPc);
            
            int reslength = RESadamen.length;
            
            byte[] CKadamen =  muuttaja.f3(passwordbyte, arraysrand, OPc);
            byte[] IKSadamen =  muuttaja.f4(passwordbyte, arraysrand, OPc);
            //       Ks = new byte[CKadamen.length + IKSadamen.length];
     //       System.arraycopy(CKadamen, 0, Ks, 0, CKadamen.length);
     //       System.arraycopy(IKSadamen, 0, Ks, CKadamen.length, IKSadamen.length);
            KsS = new String(CKadamen, "UTF-8") +new String(IKSadamen, "UTF-8");
            System.out.println("kss arvo: "+ KsS );
            KsS = Hex.encodeHexString( CKadamen )+Hex.encodeHexString( IKSadamen );
            System.out.println("kss arvo: "+ KsS );
            byte[] AKSadamen =  muuttaja.f5(passwordbyte, arraysrand, OPc);

   //         sqn = xorWithKey(sqn, AKSadamen);
            
            byte[] f1adamen =  muuttaja.f1(passwordbyte, arraysrand, OPc, sqn, amf);
// TODO: Poista tarpeettomat
        
            String resstring = Hex.encodeHexString(RESadamen); 
            
            byte[] sres = Hex.decodeHex(resstring.toCharArray());


            int sreslength = sres.length;
            
            
            byte[] sres1 = Arrays.copyOfRange(RESadamen, 0, 4);
            
            byte[] sres2 = Arrays.copyOfRange(RESadamen, 4, 8);
            
            sres = xorArray(sres1, sres2);
            
            
            byte[] f1adamen2 = Hex.encodeHexString( f1adamen ).getBytes("UTF-8");
            byte[] RESadamen2 =  Hex.encodeHexString( RESadamen ).getBytes("UTF-8");
            byte[] sres3 =  Hex.encodeHexString( sres ).getBytes("UTF-8");
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
            System.out.println("sres pituus: "+ sreslength );
            System.out.println("res pituus: "+ reslength );
            System.out.println("rand lengt: "+ arraysrand.length );
            System.out.println("autn lengt: "+ arraysautn.length );
            System.out.println("nonce arvo: "+ nonce );
            
            
            System.out.println("ik arvo: "+ Hex.encodeHexString( IKSadamen ) );
            System.out.println("ck arvo: "+ Hex.encodeHexString( CKadamen ) );
            System.out.println("opennonce arvo: "+ new String(opennonce2, "UTF-8") );
            System.out.println("srespitkä arvo: "+ new String(sres3, "UTF-8") );
            System.out.println("password arvo: "+ new String(passwordbyte, "UTF-8") );
            System.out.println("arraysrand arvo: "+ new String(arraysrand2, "UTF-8") );
            System.out.println("arraysautn arvo: "+ new String(arraysautn2, "UTF-8") );
            System.out.println("OP arvo: "+ new String(OPbyte, "UTF-8") );
            System.out.println("OPc arvo: "+ new String(OPc2, "UTF-8") );
            System.out.println("RESadamen arvo: "+ new String(RESadamen2, "UTF-8") );
            System.out.println("RESadamen arvo: "+ resstring );
            System.out.println("sqn arvo: "+ new String(sqn2, "UTF-8") );
            System.out.println("AKSadamen arvo: "+ new String(AKSadamen2, "UTF-8") );
            System.out.println("f1adamen arvo: "+ new String(f1adamen2, "UTF-8") );
            System.out.println("amf arvo: "+ new String(amf2, "UTF-8") );
            
   //         resstring = Hex.encodeHexString(sres);
            return resstring;
     //       return new String(RESadamen2, "UTF-8");
        }
        catch(Exception e)
        {
            System.out.println("KD virhe 1");
        }
        
        return "";
    }
    
    /*
    private static byte[] xorWithKey(byte[] a, byte[] key) 
    {
        byte[] out = new byte[a.length];
        for (int i = 0; i < a.length; i++) 
        {
            out[i] = (byte) (a[i] ^ key[i%key.length]);
        }
        return out;
    }
    
*/
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
          //      byte[] sres = calculateAKARES1(nonce, password);

                String res = calculateAKARES(nonce, password);
                
                /*
     //           String A1 = DigestUtils.md5Hex(username + ":" + realm1 + ":" + Hex.encodeHexString(res.getBytes()) );
                String A1 = DigestUtils.md5Hex(username + ":" + realm1 + ":" + res);
                String A2 = DigestUtils.md5Hex("GET" + ":" + uri + ":"+ DigestUtils.md5Hex(""));
        //        String A2 = DigestUtils.md5Hex("GET" + ":" + uri);
                String responseSeed = A1 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + A2;
                response = DigestUtils.md5Hex(responseSeed);
    //            response = res;
                
                System.out.println("Response : "+ response);
                
                
                response = MessageDigestAlgorithm.calculateResponse(algorithm,
                                    username,
                                    realm1,
                                    MessageDigestAlgorithm.decode(password),
                                    nonce,
                                    nc,
                                    cnonce,
                                    "GET",
                                    uri,
                                    "",
                                    qop);
                
                System.out.println("Response : "+ response);
                */
                
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
                /*
                response = MessageDigestAlgorithm.calculateResponse(algorithm,
                                    username,
                                    realm1,
                                    sres,
                                    nonce,
                                    nc,
                                    cnonce,
                                    "GET",
                                    uri,
                                    "",
                                    qop);
                
                System.out.println("Response : "+ response);
                
                response = MessageDigestAlgorithm.calculateResponse(algorithm,
                                    username,
                                    realm1,
                                    sres.toString(),
                                    nonce,
                                    nc,
                                    cnonce,
                                    "GET",
                                    uri,
                                    "",
                                    qop);
                
                System.out.println("Response : "+ response);
                
                response = MessageDigestAlgorithm.calculateResponse(algorithm,
                                    username,
                                    realm1,
                                    res.getBytes(),
                                    nonce,
                                    nc,
                                    cnonce,
                                    "GET",
                                    uri,
                                    "",
                                    qop);
                
                System.out.println("Response : "+ response);
                */
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
        System.out.println("aloitetaan yhteys naffiin ");
        URL httpUrl = new URL(nafUrl);
        System.out.println("aloitetaan yhteys naffiin2 ");
        HttpURLConnection http = (HttpURLConnection) httpUrl.openConnection();
        System.out.println("aloitetaan yhteys naffiin 3");
        http.setRequestMethod("GET");
        System.out.println("aloitetaan yhteys naffiin 4");
        http.connect();
        System.out.println("aloitetaan yhteys naffiin 5");
        if (http.getResponseCode() != 401) 
        {
            throw new Exception("Unexpected HTTP response");
        }

        System.out.println("HTTP yhteys luotu ");
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
       Map<String, Object> content = new HashMap<String,Object>();

       /*
       IMPI: tut.test1@p133.piuha.net
IMPU: sip:tut.test1@p133.piuha.net
key: 41434443524f58594f5552534f583031
       BSF palvelu: http://p133.piuha.net:8080/bsf/bootstrap
       */
        String osoiten = "coap://192.168.0.70/Yhteys/";
//        String akaosoiten = "coap://192.168.0.70/Btyhteys/";
//        String akaosoiten = "coap://localhost/Btyhteys/";
 //      String akaosoiten = "coap://localhost/Yhteys/";
       
  //      String osoiten = "http://p133.piuha.net:8080/bsf/bootstrap";
  //      String osoiten = "coap://localhost/Yhteys/";

        String akaosoiten = osoiten;
//        String tarkenne = "192.168.0.112/priv/index.html";
        String tarkenne = "94.237.64.168:804/priv/index.html";
        String tarkenne1 = "p133.piuha.net:8080/bsf/bootstrap";
        String nafurl = "p133.piuha.net:8080/naf/resource";
        
        String nafresource = osoiten+nafurl;
       
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
        
        content.put("Authorization", authheader);
       
        try
        {
            args = new String[4];
        
            args[0] = "PUT";

            args[1] = osoiten;
            System.out.println("content " + ": " + content);
    //        String palautus2 = json.writeJSONauthentication(authheader);
            String palautus2 = json.writeJSONauthentication(content);
            System.out.println("palautus2 " + ": " + palautus2);

            args[2] = palautus2;
            
            args[3] = "50";

            String Servuvastaus = coap.runCoap(args);
            System.out.println("vastaus " + ": " + Servuvastaus);
        }
        catch (Exception e)
        {
            System.err.println("virhe2");
        }
        
        content.clear();
        ///////////tästä AKA digest
        
 //       Map authheader = setAuthorizationHeaderMap(authmap, "/bsf/bootstrap", "tut.test1@p133.piuha.net", "41434443524f58594f5552534f583031");
  //      authheader = setbtAuthorizationHeaderMap("/bsf/bootstrap", "tut.test1@p133.piuha.net", "ims.ericsson.com");
        authheader = setbtAuthorizationHeaderMap("/bsf/bootstrap", "tut.test1@p133.piuha.net", "p133.piuha.net");
       //String uri, String username, String realm)
        
        System.out.println("authheader " + ": " + authheader);
        
        content.put("Authorization", authheader);
       
        try
        {
            args = new String[4];
        
            args[0] = "PUT";

            args[1] = akaosoiten;
          //  String palautus2 = json.writeJSONbtauthentication(authheader);
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
 //       Map authheader = setAuthorizationHeaderMap(authmap, "/bsf/bootstrap", "tut.test1@p133.piuha.net", "41434443524f58594f5552534f583031");
 //       Map authheader = setbtAuthorizationHeaderMap("/bsf/bootstrap", "tut.test1@p133.piuha.net", "ims.ericsson.com");
       //String uri, String username, String realm)
        
        System.out.println("authheader " + ": " + authheader);
        
        content.put("Authorization", authheader);
        System.out.println("content " + ": " + content);
        String Servuvastaus = "";
       
        try
        {
            args = new String[4];
        
            args[0] = "PUT";

            args[1] = akaosoiten;
    //        String palautus2 = json.writeJSONbtauthentication(authheader);
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
        
    //    Servuvastaus.
   
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
            // handle SAXException 
        } 
        catch (IOException e) 
        {
            System.err.println("virtahje2");
            // handle IOException 
        }
        
        content.clear();
        
        
  //      String nafid = "http://p133.piuha.net:8080/bsf/"+Hex.encodeHexString( ua );
       // String nafid = "http://p133.piuha.net:8080/bsf/";
        String nafid = "";
         try
       {
           System.out.println("Kokeil: "+ Hex.encodeHexString("naf.labs.ericsson.net".getBytes("UTF-8") ));
           byte[] kokeilu = Base64.decodeBase64("QAAAAA=");
          System.out.println("Kokeilu: "+ Hex.encodeHexString( kokeilu ) );
          
          byte[] kokeilu2 = Base64.decodeBase64("bmFmLmxhYnMuZXJpY3Nzb24ubmV0AQAAAAA=");
          System.out.println("Kokeilu2: "+ Hex.encodeHexString( kokeilu2 ) );
          System.out.println("Kokeilu2: "+ new String(kokeilu2, "UTF-8") );
           
            byte[] ua = {(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00};
            nafid = "p133.piuha.net" + new String(ua, "UTF-8");
            
            String nafidpal = nafid;
           System.out.println("nafidpal: "+ nafidpal  );
           
           System.out.println("nafidpalbas: "+ Base64.encodeBase64String(nafidpal.getBytes("UTF-8")));
           byte[] kokeilu8 = Base64.decodeBase64(Base64.encodeBase64String(nafidpal.getBytes("UTF-8")));
           System.out.println("Kokeipalbas: "+ Hex.encodeHexString(kokeilu8  ) );
           System.out.println("nafidpal: "+ new String(kokeilu8, "UTF-8") );
           
             
            String Ks_naf = kdfEncode1(KsS, randkdf, "tut.test1@p133.piuha.net", nafid, "gba-me");
            System.out.println("ksnaf1 " + ": " + Ks_naf);

            Ks_naf = kdfEncode1(KsS, randkdf, "tut.test1@p133.piuha.net", nafid, "gba-u");
            System.out.println("ksnaf2 " + ": " + Ks_naf);

            Ks_naf = kdfEncode1(KsS, randkdf, "tut.test1@p133.piuha.net", nafid, "gba-digest");
            System.out.println("ksnaf3 " + ": " + Ks_naf);
            
            
            
            byte[] ua1 = {(byte) 0x01,(byte) 0x00,(byte) 0x00,(byte) 0x00,(byte) 0x00};
            nafid = "p133.piuha.net" + new String(ua1, "UTF-8");
            
            nafidpal = nafid;
           System.out.println("nafidpal: "+ nafidpal  );
           
           System.out.println("nafidpalbas: "+ Base64.encodeBase64String(nafidpal.getBytes("UTF-8")));
           byte[] kokeilu9 = Base64.decodeBase64(Base64.encodeBase64String(nafidpal.getBytes("UTF-8")));
           System.out.println("Kokeipalbas: "+ Hex.encodeHexString(kokeilu9  ) );
           System.out.println("nafidpal: "+ new String(kokeilu9, "UTF-8") );
           
             
            Ks_naf = kdfEncode1(KsS, randkdf, "tut.test1@p133.piuha.net", nafid, "gba-me");
            System.out.println("ksnaf1 " + ": " + Ks_naf);

            Ks_naf = kdfEncode1(KsS, randkdf, "tut.test1@p133.piuha.net", nafid, "gba-u");
            System.out.println("ksnaf2 " + ": " + Ks_naf);

            Ks_naf = kdfEncode1(KsS, randkdf, "tut.test1@p133.piuha.net", nafid, "gba-digest");
            System.out.println("ksnaf3 " + ": " + Ks_naf);
            
            nafid = "p133.piuha.net";
            
            nafidpal = nafid;
           System.out.println("nafidpal: "+ nafidpal  );
           
           System.out.println("nafidpalbas: "+ Base64.encodeBase64String(nafidpal.getBytes("UTF-8")));
           byte[] kokeilu10 = Base64.decodeBase64(Base64.encodeBase64String(nafidpal.getBytes("UTF-8")));
           System.out.println("Kokeipalbas: "+ Hex.encodeHexString(kokeilu10  ) );
           System.out.println("nafidpal: "+ new String(kokeilu10, "UTF-8") );
           
             
            Ks_naf = kdfEncode1(KsS, randkdf, "tut.test1@p133.piuha.net", nafid, "gba-me");
            System.out.println("ksnaf1 " + ": " + Ks_naf);

            Ks_naf = kdfEncode1(KsS, randkdf, "tut.test1@p133.piuha.net", nafid, "gba-u");
            System.out.println("ksnaf2 " + ": " + Ks_naf);

            Ks_naf = kdfEncode1(KsS, randkdf, "tut.test1@p133.piuha.net", nafid, "gba-digest");
            System.out.println("ksnaf3 " + ": " + Ks_naf);
       }
       catch(Exception e)
       {
           System.err.println("virhe1");
           
       }   
        //kdfEncode1(String Ks, String rand, String impi, String nafid, String gbatype)
        
 //       content.put("UserName", btid);
     /*   
        try
        {
            runUaHttpDigest( "http://" +nafurl, btid, Ks_naf.getBytes("utf-8"));
        }
        catch (Exception e)
        {
            System.err.println("virhe http digest");
        }
        */
           /* 
        Servuvastaus = "";
       
        try
        {
            args = new String[4];
        
         //   args[0] = "POST";
            args[0] = "PUT";

            args[1] = nafresource;
    //        String palautus2 = json.writeJSONbtauthentication(authheader);
  //          String palautus2 = json.writeJSONbtauthentication(content);

            args[2] = json.createJsonString(content);
            
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
        
        content.clear();
        
        content.put("Cookie", 
                        (HashMap<String,Object>)authmap.get("Set-Cookie"));
        
        authheader = setAuthorizationHeaderMap(authmap, "/naf/resource", btid, Ks_naf);
        
          System.out.println("authheader " + ": " + authheader);
        
        content.put("Authorization", authheader);
       
        try
        {
            args = new String[4];
        
            args[0] = "PUT";

            args[1] = nafresource;
            System.out.println("content " + ": " + content);
    //        String palautus2 = json.writeJSONauthentication(authheader);
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
        
        content.clear();
        
        */
        /*
        
        //Create a helper class for HttpDigest
        GbaHttpDigestMD5 httpDigest = new GbaHttpDigestMD5(nafurl, btid, ksNaf);
        
        URL httpUrl = new URL(nafurl);
        HttpURLConnection http = (HttpURLConnection) httpUrl.openConnection();

    //    http.

//Respond with Authorization header
        System.out.println(http.getHeaderField("WWW-Authenticate"));
        httpDigest.computeDigestResponse(
                http.getHeaderField("WWW-Authenticate"));
        String authorizationHeader = httpDigest.generateAuthorizationHeader();
        System.out.println("Authorization: " + authorizationHeader);
        */
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
