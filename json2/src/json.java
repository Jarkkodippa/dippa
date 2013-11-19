
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;

import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;

import net.minidev.json.JSONAware;
import javax.mail.internet.ParameterList;
import org.apache.commons.codec.binary.Base64;

import org.json.JSONObject;
import org.json.JSONArray;


import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.UUID;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jarkko
 */
public class json 
{
   static JSONObject lopullinen;

   public json() 
   {

       JSONObject lopullinen = new JSONObject(); 
   }
   
   
    //Cryptaa Stringin JVS 
    //Palauttaa cryptatun JVS:n.
   public static String cryptJVS(String message, String sharedKey) 
   {

        // Create payload
     //   String message = "Hello world!";
        
        Payload payload = new Payload(message);
        
        System.out.println("JWS payload message: " + message);
        
        
        // Create JWS header with HS256 algorithm
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        System.out.println("Jeee");
        header.setContentType("text/plain");
 //       String vali = header.toJSONObject();
        System.out.println("JWS header: ");
        
        
        // Create JWS object
        JWSObject jwsObject = new JWSObject(header, payload);
        
        
        // Create HMAC signer
  //      String sharedKey = "a0a2abd8-6162-41c3-83d6-1cf559b46afc";
        
        System.out.println("HMAC key: " + sharedKey);
        
        JWSSigner signer = new MACSigner(sharedKey.getBytes());
        
        try
        {
                jwsObject.sign(signer);
                
        } catch (JOSEException e)
        {
        
                System.err.println("Couldn't sign JWS object: " + e.getMessage());
                return "";
        }
        
        
        // Serialise JWS object to compact format
        String s = jwsObject.serialize();
        
        System.out.println("Serialised JWS object: " + s);
        
        
        return s;
        
    }
	
   //purkaa JVS.
   //Palauttaa puretun viestin Stringinä.
   public static String uncryptJVS(String message, String sharedKey) 
   {

        
        
        JWSObject jwsObject;
        // Parse back and check signature
        
        try {
                jwsObject = JWSObject.parse(message);
                
        } catch (ParseException e) 
        {
        
                System.err.println("Couldn't parse JWS object: " + e.getMessage());
                return "";
        }
        
        System.out.println("JWS object successfully parsed");
        
        JWSVerifier verifier = new MACVerifier(sharedKey.getBytes());
        
        boolean verifiedSignature = false;
        
        try 
        {
                verifiedSignature = jwsObject.verify(verifier);
                
        } 
        catch (JOSEException e) 
        {
        
                System.err.println("Couldn't verify signature: " + e.getMessage());
        }
        
        if (verifiedSignature) 
        {
        
                System.out.println("Verified JWS signature!");
        }
        else 
        {
                System.out.println("Bad JWS signature!");
                return "";
        }
        
        String vali = ""+ jwsObject.getPayload();
        System.out.println("Recovered payload message: " + jwsObject.getPayload());
        return vali;
    }
   
   
   //Cryptaa Stringin JVS 
    //Palauttaa cryptatun JVS:n.
   public String cryptJVE(String message, 
           RSAPublicKey publicKey, RSAPrivateKey privateKey) 
   {

       // Compose the JWT claims set

        JWTClaimsSet jwtClaims = new JWTClaimsSet();

        String iss = "https://openid.net";

        jwtClaims.setIssuer(iss);

        String sub = "alice";

        jwtClaims.setSubject(sub);

        List<String> aud = new ArrayList<String>();

        aud.add("https://app-one.com");

        aud.add("https://app-two.com");

        jwtClaims.setAudience(aud);

        // Set expiration in 10 minutes

        Date exp = new Date(new Date().getTime() + 1000*60*10);

        jwtClaims.setExpirationTime(exp);


        Date nbf = new Date();

        jwtClaims.setNotBeforeTime(nbf);


        Date iat = new Date();

        jwtClaims.setIssueTime(iat);

        String jti = UUID.randomUUID().toString();

        jwtClaims.setJWTID(jti);


        // Request JWT encrypted with RSA-OAEP and 128-bit AES/GCM

        JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A128GCM);

        // Create the encrypted JWT object

        EncryptedJWT jwt = new EncryptedJWT(header, jwtClaims);

        // Create an encrypter with the specified public RSA key

      	try 
        {
            RSAEncrypter encrypter = new RSAEncrypter(publicKey);

        // Do the actual encryption

            jwt.encrypt(encrypter);
        }
        catch (JOSEException e) 
        {
        
                System.err.println("Couldn't verify signature: " + e.getMessage());
        }
        // Serialise to JWT compact form

        String jwtString = jwt.serialize();
        return jwtString;
        
    }
	
   //purkaa JVS.
   //Palauttaa puretun viestin Stringinä.
   public String uncryptJVE(String message, 
           RSAPublicKey publicKey, RSAPrivateKey privateKey) 
   {
       JWTClaimsSet jwtClaims = new JWTClaimsSet();

       JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A128GCM);

        // Create the encrypted JWT object

        EncryptedJWT jwt = new EncryptedJWT(header, jwtClaims);
        String vali = "";
       // Parse back

        try 
        {
            jwt = EncryptedJWT.parse(message);

            // Create an decrypter with the specified private RSA key

            RSADecrypter decrypter = new RSADecrypter(privateKey);

            // Decrypt

            jwt.decrypt(decrypter);
            
            jwt.getJWTClaimsSet().getIssuer();

            jwt.getJWTClaimsSet().getSubject();

            jwt.getJWTClaimsSet().getAudience().size();

            jwt.getJWTClaimsSet().getExpirationTime();

            jwt.getJWTClaimsSet().getNotBeforeTime();

            jwt.getJWTClaimsSet().getIssueTime();

            jwt.getJWTClaimsSet().getJWTID();
            vali = jwt.getJWTClaimsSet().getSubject().toString();
        }
        catch (Exception e) 
        {
        
                System.err.println("purku epäonnistu");
        }
        


        // Retrieve JWT claims

        
        
        return vali;
    }
   
   public void testEncryptAndDecrypt(RSAPublicKey publicKey, RSAPrivateKey privateKey)

	throws Exception 
   {

        // Compose the JWT claims set

        JWTClaimsSet jwtClaims = new JWTClaimsSet();

        String iss = "https://openid.net";

        jwtClaims.setIssuer(iss);

        String sub = "alice";

        jwtClaims.setSubject(sub);

        List<String> aud = new ArrayList<String>();

        aud.add("https://app-one.com");

        aud.add("https://app-two.com");

        jwtClaims.setAudience(aud);

        // Set expiration in 10 minutes

        Date exp = new Date(new Date().getTime() + 1000*60*10);

        jwtClaims.setExpirationTime(exp);


        Date nbf = new Date();

        jwtClaims.setNotBeforeTime(nbf);


        Date iat = new Date();

        jwtClaims.setIssueTime(iat);

        String jti = UUID.randomUUID().toString();

        jwtClaims.setJWTID(jti);


        // Request JWT encrypted with RSA-OAEP and 128-bit AES/GCM

        JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A128GCM);

        // Create the encrypted JWT object

        EncryptedJWT jwt = new EncryptedJWT(header, jwtClaims);

        // Create an encrypter with the specified public RSA key

        RSAEncrypter encrypter = new RSAEncrypter(publicKey);

        // Do the actual encryption

        jwt.encrypt(encrypter);

        // Serialise to JWT compact form

        String jwtString = jwt.serialize();

        // Parse back

        jwt = EncryptedJWT.parse(jwtString);

        // Create an decrypter with the specified private RSA key

        RSADecrypter decrypter = new RSADecrypter(privateKey);

        // Decrypt

        jwt.decrypt(decrypter);


        // Retrieve JWT claims

        jwt.getJWTClaimsSet().getIssuer();

        jwt.getJWTClaimsSet().getSubject();

        jwt.getJWTClaimsSet().getAudience().size();

        jwt.getJWTClaimsSet().getExpirationTime();

        jwt.getJWTClaimsSet().getNotBeforeTime();

        jwt.getJWTClaimsSet().getIssueTime();

        jwt.getJWTClaimsSet().getJWTID();

    }


        
              /*
   }
{
    "www-authenticate" : {
        "type": "digest",
        "challenge": {
            "realm": "ismo@laitela.com",
            "qop": "auth, auth-int",
            "nonce": "dcd98b7102dd2f0e8b11d0f600bfb0c093",
            "opaque": "5ccc069c403ebaf9f0171e9517f40e41"
        }
    }
}

{"authorization": {
    "type": "digest",
    "digest-response" :
    {
        "username": "IsmoTaalasmaa",
        "realm" : "pihlajakatu@talotaikurit.com",
        "nonce": "dcd98b7102dd2f0e8b11d0f600bfb0c093",
        "uri": "/dir/index.html",
        "qop": "auth",
        "nc:00000001,
        "cnonce":"0a4f113b",
        "response":"6629fae49393a05397450978507c4ef1",
        "opaque":"5ccc069c403ebaf9f0171e9517f40e41"
    }
}
}
   */
           /**
  * Method to convert map into json format
  * @param map with data to be converted into json
  * @return json string
  */
 public static String createJsonString(JSONObject json) throws IOException 
 {

    String palautus = ""; 
 
     
    palautus = json.toString();

    return palautus;
 }
 
  public static String createJsonString(Map jsonMap) throws IOException 
 {

    Writer writer = new StringWriter();
    JsonGenerator jsonGenerator = new JsonFactory().createJsonGenerator(writer);
    ObjectMapper mapper = new ObjectMapper();
    mapper.writeValue(jsonGenerator, jsonMap);
    jsonGenerator.close();

     
    String palautus = ""; 
    palautus = writer.toString();

    return palautus;
 }

 //Lukee json tiedoston mappiin.
 //palauttaa mapin.
   public Map readJSON(String data) 
   {
  //      JSONObject valiaikainen = new JSONObject(data);
        Map<String, Object> map = new HashMap<String,Object>();
        ObjectMapper mapper = new ObjectMapper();
 
	try 
        {
 
		//convert JSON string to Map
            JSONObject valiaikainen = new JSONObject(data);
 //           map = valiaikainen.
  //          valiaikainen.
            map = mapper.readValue(data, new TypeReference<HashMap<String,Object>>(){});
    //        System.out.println("jee");
  //          System.out.println(map);
            return map;
 
	} 
        catch (Exception e) 
        {
		e.printStackTrace();
	}
        return map;
   }
   
   //Kirjoittaa server responcen jsoniin.
   //Palauttaa json stringin.
   public static String writeJSONauthentication(String type, String challenges, 
                                                String realm, String qop, 
                                                String nonce, String opaque) 
                                                throws IOException
   {
      
      try 
      {

            Map<String, Object> challenge = new HashMap<String, Object>();

            challenge.put( "realm", realm );  
            challenge.put( "qop", qop );  
            challenge.put( "nonce", nonce );  
            challenge.put( "opaque", opaque );  
    //        String jsoneka = createJsonString(challenge);
            
            HashMap<String, Object> wwwauthenticate = new HashMap<String, Object>();

            wwwauthenticate.put( "type", "digest" );  

            wwwauthenticate.put( "challenge", challenge );
  //          wwwauthenticate.put( "challenge", jsoneka );
//            String jsontoka = createJsonString(wwwauthenticate);
            
            HashMap<String, Object> authorization2 = new HashMap<String, Object>();
            authorization2.put( "www-authenticate", wwwauthenticate );
            
            String palautus = createJsonString(authorization2);

            return palautus;
      }
      catch(Exception e) 
      {

          return "";
      }
   }
   

   
   
   //Kirjoittaa client reguestin jsoniks.
   //Palautta json stringin.
   public static String writeJSONauthorization(String type,  
                                            String username, String realm,
                                            String uri, String nc, String cnonce,
                                            String response, String qop, 
                                                String nonce, String opaque) 
   {
      
      try 
      {

          JSONObject valiaikainen = new JSONObject();
          JSONObject valiaikainen2 = new JSONObject();
            Map<String, String> digestresponse = new HashMap<String, String>();

            digestresponse.put( "username", username );  
            digestresponse.put( "realm", realm );  
            digestresponse.put( "nonce", nonce );  
            digestresponse.put( "uri", uri );
            digestresponse.put( "qop", qop );  
            digestresponse.put( "nc", nc );  
            digestresponse.put( "cnonce", cnonce );  
            digestresponse.put( "response", response );  
            digestresponse.put( "opaque", opaque );
            
         
//            addMapJsonArray("testi",digestresponse);
            valiaikainen.put("digest-response", digestresponse);
  //          String jsoneka = createJsonString(digestresponse);

            HashMap<String, String> authorization = new HashMap<String, String>();
     //       authorization.put( "type", "digest" );  
            valiaikainen.put("type", "digest");

            
            HashMap<String, String> authorization2 = new HashMap<String, String>();

            valiaikainen2.put("authorization", valiaikainen);
            String palautus = createJsonString(valiaikainen2);
   //         System.out.println( palautus );
  
            return palautus;
        
      }
      catch(Exception e) 
      {

          return "";
      }
   }
   
   
   public static String writeJSONauthorization(Map data) 
   {
      
      try 
      {
          /*
          HashMap<String, Object> authorizationn = new HashMap<String, Object>();
          authorizationn.put("authorization", data.get("authorization"));
          HashMap<String, Object> digestresponsen = new HashMap<String, Object>();
          digestresponsen.put("digest-response", data.get("digest-response"));


          */
            String palautus = createJsonString(data);
    //        System.out.println( palautus );
  
            return palautus;
        
      }
      catch(Exception e) 
      {

          return "";
      }
   }
   

   public static String writeJSONauthentication(Map data) 
                                                throws IOException
   {
      
      try 
      {

   
            String palautus = createJsonString(data);

            return palautus;
      }
      catch(Exception e) 
      {

          return "";
      }
   }
   
    /** Suoritetaan p��silmukkaa niin kauan kuin k�ytt�j� haluaa.
     */
   public void suoritaPaasilmukkaa() 
   {
 
   
     // String s = cryptJVS("testataam", "sharedKey2");
      uncryptJVS(cryptJVS("testataam", "sharedKey2"), "sharedKey2");
      
       try
       {
           String json = writeJSONauthorization("type1", "username1", "realm1", "uri1", 
                                "00000011", "cnonce1", "response1", "qop1", 
               "nonce1", "opaque1");
          String json1 = writeJSONauthentication("type",  "challenges", 
                                                 "realm", "qop", 
                                                 "nonce",  "opaque");
          System.out.println( json );
          System.out.println( json1 );
          
         // writeJSONauthorization(readJSON(json));
          System.out.println( readJSON(json1));
          System.out.println( readJSON(json));
      } 
    catch (Exception e) 
    {

            System.err.println("error");
    }
      

   }
   
}
