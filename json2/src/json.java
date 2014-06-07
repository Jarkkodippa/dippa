
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

//import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;


import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;

import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.SignedJWT;
//import com.nimbusds.jwt;

//import net.minidev.json.JSONAware;
//import javax.mail.internet.ParameterList;
//import org.apache.commons.codec.binary.Base64;

import org.json.JSONObject;
//import org.json.JSONArray;


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
//import java.security.spec.RSAPrivateKeySpec;
//import java.security.spec.RSAPublicKeySpec;

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
   
      //Make signed JSON Web Token.
   // Return serialized token.
   public static String signJWT(String message, String sharedKey) 
   {


       
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        

        String s = cryptJWS(message, sharedKey, header);
        
        System.out.println("crypted: "+ s);
        String serialized = "";


        try
         {
            SignedJWT  t = SignedJWT.parse(s);
      //      t.getSignature();
            //      String signature = s.
            Base64URL firstPart = header.toBase64URL();
            System.out.println("eka osa: "+ firstPart);
            Base64URL secondPart = Base64URL.encode(s);
            System.out.println("toka osa: "+ secondPart);
            System.out.println("toka osa takaisin: "+ secondPart.toJSONString());
            System.out.println("toka osa takaisin2: "+ secondPart.decodeToString());
            Base64URL thirdPart = t.getSignature();
            System.out.println("kolmas osa: "+ thirdPart);

            SignedJWT signedJWT = new SignedJWT(firstPart, secondPart, thirdPart);
            
            serialized = signedJWT.serialize();
            signedJWT = SignedJWT.parse(serialized);
            System.out.println("signed jwt: "+ signedJWT.getParsedString());
            /*
            Base64URL sigInput = Base64URL.encode(signedJWT.getSigningInput());

            JWSSigner signer = new MACSigner(sharedKey);

            signedJWT.sign(signer);
*/
         } 
        catch (ParseException e)
         {

                 System.err.println("Couldn't parse JWS object2: ");
                 return "";
         }
        /*
        catch (JOSEException e)
         {

                 System.err.println("Couldn't sign JWS object: ");
                 return "";
         }
*/

        return serialized;
        
    }
   
      //Make signed JSON Web Token.
   // Return serialized token.
   public static String signJWT(String message, String sharedKey, String btid) 
   {


       
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        header.setCustomParameter("btid", btid);

        String s = cryptJWS(message, sharedKey, header);
 //       header.setCustomParameter("btid", btid);
        System.out.println("crypted: "+ s);
        String serialized = "";


        try
         {
            SignedJWT  t = SignedJWT.parse(s);
      //      t.getSignature();
            //      String signature = s.
            Base64URL firstPart = header.toBase64URL();
            System.out.println("eka osa: "+ firstPart);
            Base64URL secondPart = Base64URL.encode(s);
            System.out.println("toka osa: "+ secondPart);
            System.out.println("toka osa takaisin: "+ secondPart.toJSONString());
            System.out.println("toka osa takaisin2: "+ secondPart.decodeToString());
            Base64URL thirdPart = t.getSignature();
            System.out.println("kolmas osa: "+ thirdPart);

            SignedJWT signedJWT = new SignedJWT(firstPart, secondPart, thirdPart);
            
            serialized = signedJWT.serialize();
            signedJWT = SignedJWT.parse(serialized);
            System.out.println("signed jwt: "+ signedJWT.getParsedString());
            /*
            Base64URL sigInput = Base64URL.encode(signedJWT.getSigningInput());

            JWSSigner signer = new MACSigner(sharedKey);

            signedJWT.sign(signer);
*/
         } 
        catch (ParseException e)
         {

                 System.err.println("Couldn't parse JWS object2: ");
                 return "";
         }
        /*
        catch (JOSEException e)
         {

                 System.err.println("Couldn't sign JWS object: ");
                 return "";
         }
*/

        return serialized;
        
    }
   
   
    //Make encrypted JSON Web Token.
   // Return serialized token.
   public String encryptJWT(String message, String sharedKey) 
   {


       
        JWEHeader header = new JWEHeader(JWEAlgorithm.A256KW,EncryptionMethod.A128CBC_HS256);

        String s = cryptJWE1(message, sharedKey, header);
        
        System.out.println("crypted: "+ s);
        String serialized = "";


        try
         {
             EncryptedJWT  t = EncryptedJWT.parse(s);
  //          SignedJWT  t = SignedJWT.parse(s);

            Base64URL firstPart = header.toBase64URL();
            System.out.println("eka osa: "+ firstPart);
            Base64URL secondPart = Base64URL.encode(sharedKey);
            
            System.out.println("toka osa: "+ secondPart);
            System.out.println("toka osa takaisin: "+ secondPart.toJSONString());
            System.out.println("toka osa takaisin2: "+ secondPart.decodeToString());
//            Base64URL thirdPart = t.getSignature();
  //          System.out.println("kolmas osa: "+ thirdPart);

            Base64URL fourthPart = Base64URL.encode(s);
  //          SignedJWT signedJWT = new SignedJWT(firstPart, secondPart, thirdPart);
            
  //          serialized = signedJWT.serialize();
   //         signedJWT = SignedJWT.parse(serialized);
    //        System.out.println("signed jwt: "+ signedJWT.getParsedString());
          
         } 
        catch (ParseException e)
         {

                 System.err.println("Couldn't parse JWS object2: ");
                 return "";
         }
        /*
        catch (JOSEException e)
         {

                 System.err.println("Couldn't sign JWS object: ");
                 return "";
         }
*/

        return serialized;
        
    }
   
    //Open signed JSON Web Token.
   // Return open string.
   public static String openSignJWT(String message, String sharedKey) 
   {


       
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

   //     String s = cryptJVS(message, sharedKey, header);
   //     String oo = "oo";
 //       Base64URL Part = oo.
        
     //   String serialized = "";
        


        System.out.println("viesti : "+ message);
//        SignedJWT te = new SignedJWT();
        try
         {
             SignedJWT signedJWT = new SignedJWT(header.toBase64URL(), 
                Base64URL.encode("secondPart"), Base64URL.encode("thirdPart"));
      //      String serialized = signedJWT.serialize();
     //       serialized = signedJWT.serialize();
    //        signedJWT = SignedJWT.parse(serialized);
             
             
            signedJWT = SignedJWT.parse(message);
      //      t.getSignature();
            //      String signature = s.
            
            Base64URL[] Parts = signedJWT.getParsedParts();
            Base64URL firstPart = Parts[0];
            Base64URL secondPart = Parts[1];
            Base64URL thirdPart = Parts[2];
            
            message = secondPart.decodeToString();
            
            return uncryptJWS(message, sharedKey);

            /*
            SignedJWT signedJWT = new SignedJWT(firstPart, secondPart, thirdPart);
            
            serialized = signedJWT.serialize();
            */
            /*
            Base64URL sigInput = Base64URL.encode(signedJWT.getSigningInput());

            JWSSigner signer = new MACSigner(sharedKey);

            signedJWT.sign(signer);
*/
         } 
        catch (ParseException e)
         {

                 System.err.println("Couldn't parse JWS object: ");
                 return "";
         }
        /*
        catch (JOSEException e)
         {

                 System.err.println("Couldn't sign JWS object: ");
                 return "";
         }
*/

    //    return "";
        
    }
   
    //Open signed JSON Web Token.
   // Return open string.
   public static String retrunSignJWTbtid(String message) 
   {

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

        System.out.println("viesti : "+ message);

        Map<String, Object> headermap = new HashMap<String,Object>();
        String headerpart = "";
        try
         {
             SignedJWT signedJWT = new SignedJWT(header.toBase64URL(), 
                Base64URL.encode("secondPart"), Base64URL.encode("thirdPart"));
 
            signedJWT = SignedJWT.parse(message);

       //     signedJWT.
            Base64URL[] Parts = signedJWT.getParsedParts();
            Base64URL firstPart = Parts[0];
            Base64URL secondPart = Parts[1];
            Base64URL thirdPart = Parts[2];
            
            headerpart = firstPart.decodeToString();
            headermap = readJSON(headerpart);
            return (String)headermap.get("btid");
    
         } 
        catch (ParseException e)
         {

                 System.err.println("Couldn't parse JWS object: ");
                 return "";
         }
 
        
    }
   
    //Cryptaa Stringin JVS 
    //Palauttaa cryptatun JVS:n.
   public static String cryptJWS(String message, String sharedKey, JWSHeader header) 
   {


        
        Payload payload = new Payload(message);
        
        System.out.println("JWS payload message: " + message);
        
        
        // Create JWS header with HS256 algorithm
  //      JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        System.out.println("Jeee");
        header.setContentType("text/plain");

        System.out.println("JWS header: " + header.toString());
        
        
        // Create JWS object
        JWSObject jwsObject = new JWSObject(header, payload);
        

        
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
   public static String uncryptJWS(String message, String sharedKey) 
   {

        
        
        JWSObject jwsObject;
        // Parse back and check signature
        
        try 
        {
                jwsObject = JWSObject.parse(message);
                
        } 
        catch (ParseException e) 
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
   public String cryptJWE(String message, 
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
   
   //Cryptaa Stringin JVS 
    //Palauttaa cryptatun JWT:n.
   public String cryptJWE1(String message, String sharedKey, JWEHeader header) 
   {

       Payload payload = new Payload(message);
       header.setContentType("text/plain");
       
       JWEObject jweObject = new JWEObject(header, payload);
       

        System.out.println("Shared key: " + sharedKey);
        
//        JWSSigner signer = new MACSigner(sharedKey.getBytes());
//        JWEEncrypter Encrypter = new AESEncrypter(sharedKey.getBytes());
        
        try
        {
      //          jweObject.;
            DirectEncrypter encrypter = new DirectEncrypter(sharedKey.getBytes());
                jweObject.encrypt(encrypter);
                
        } catch (JOSEException e)
        {
        
                System.err.println("Couldn't encrypt JWE object: " + e.getMessage());
                return "";
        }
        
        
        // Serialise JWS object to compact format
        String s = jweObject.serialize();
        
        System.out.println("Serialised JWS object: " + s);
        
        
        return s;
        
    }
   
   //purkaa JWE.
   //Palauttaa puretun viestin Stringinä.
   public String uncryptJWE1(String message, String sharedKey) 
   {

        
        
        JWEObject jweObject;
        // Parse back and check signature
        
        try 
        {
                jweObject = JWEObject.parse(message);
                
        } 
        catch (ParseException e) 
        {
        
                System.err.println("Couldn't parse JWE object: " + e.getMessage());
                return "";
        }
        
        System.out.println("JWE object successfully parsed");
        
        
        
        try 
        {
                DirectDecrypter decrypter = new DirectDecrypter(sharedKey.getBytes());
                jweObject.decrypt(decrypter);
                
        } 
        catch (JOSEException e) 
        {
        
                System.err.println("Couldn't decrypt message: " + e.getMessage());
        }
        
        String vali = ""+ jweObject.getPayload();
        System.out.println("Recovered payload message: " + jweObject.getPayload());
        return vali;
    }
	
   //purkaa JWE.
   //Palauttaa puretun viestin Stringinä.
   public String uncryptJWE(String message, 
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
   public static Map readJSON(String data) 
   {

        Map<String, Object> map = new HashMap<String,Object>();
        ObjectMapper mapper = new ObjectMapper();
 
	try 
        {
 
		//convert JSON string to Map

            map = mapper.readValue(data, new TypeReference<HashMap<String,Object>>(){});

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
            Map<String, Object> digestresponse = new HashMap<String, Object>();

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

            valiaikainen.put("type", "digest");

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
          HashMap<String, Object> content = new HashMap<String, Object>();
          HashMap<String, Object> authorization2 = new HashMap<String, Object>();

            if(data.containsKey("WWW-Authenticate"))
            {

                HashMap<String, Object> wwwauthenticate = new HashMap<String, Object>();

           
                wwwauthenticate.put( "type", "digest" );  

                wwwauthenticate.put( "challenge", 
                        (HashMap<String,Object>)data.get("WWW-Authenticate") );

   //             tietoja2 = (HashMap<String,Object>)data.get("WWW-authenticate");
                
                authorization2.put( "www-authenticate", wwwauthenticate );
//                content.put(null, data)
            }
            if(data.containsKey("Set-Cookie"))
            {
            
                authorization2.put( "Set-Cookie", 
                        (HashMap<String,Object>)data.get("Set-Cookie") );
//                content.put(null, data)
            }
            if(data.containsKey("Body"))
            {

             //   authorization2.put( "Body", (HashMap<String,Object>)data.get("Body") );
                authorization2.put( "Body", data.get("Body") );
//                content.put(null, data)
            }
            
            
            String palautus = createJsonString(authorization2);
  
            System.out.println( palautus );
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

          HashMap<String, Object> valiaikainen = new HashMap<String, Object>();
          HashMap<String, Object> valiaikainen2 = new HashMap<String, Object>();
/*
          Map<String, Object> tietoja = new HashMap<String, Object>();
        Map<String, Object> tietoja2 = new HashMap<String, Object>();
        Map<String, Object> tietoja3 = new HashMap<String, Object>();
        Map<String, Object> lopullinen = new HashMap<String, Object>();
        tietoja = data;
        System.out.println("tietoja mapin sisälto" + ": " + tietoja);
        tietoja2 = (HashMap<String,Object>)tietoja.get("Authorization");
        System.out.println("tietoja2 mapin sisälto" + ": " + tietoja2);
          */
//        tietoja3 = (HashMap<String,Object>)tietoja2.get("challenge");
 //       System.out.println("tietoja3 mapin sisälto" + ": " + tietoja3);
 //         JSONObject valiaikainen = new JSONObject();
  //        JSONObject valiaikainen2 = new JSONObject();
          if(data.containsKey("Authorization"))
          {
            

            valiaikainen.put("digest-response", 
                    (HashMap<String,Object>)data.get("Authorization"));

            valiaikainen.put("type", "digest");

            valiaikainen2.put("authorization", valiaikainen);
          }
          if(data.containsKey("Cookie"))
          {

                valiaikainen2.put( "Cookie", 
                        (HashMap<String,Object>)data.get("Cookie") );
            //                content.put(null, data)
          }
            String palautus = createJsonString(valiaikainen2);

            return palautus;
      }
      catch(Exception e) 
      {

          return "";
      }
   }
   
   public static String writeJSONbtauthentication(Map data) 
                                                throws IOException
   {
      
      try 
      {

          HashMap<String, Object> valiaikainen = new HashMap<String, Object>();
          HashMap<String, Object> valiaikainen2 = new HashMap<String, Object>();

          if(data.containsKey("Authorization"))
          {

              valiaikainen.put("digest-response", 
                    (HashMap<String,Object>)data.get("Authorization"));

      //      valiaikainen.put("digest-response", data);

            valiaikainen.put("type", "akadigest");

            valiaikainen2.put("authorization", valiaikainen);
          }
          if(data.containsKey("Cookie"))
          {

                valiaikainen2.put( "Cookie", 
                        (HashMap<String,Object>)data.get("Cookie") );
            //                content.put(null, data)
          }
            String palautus = createJsonString(valiaikainen2);

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
   //   uncryptJVS(cryptJVS("testataam", "sharedKey2"), "sharedKey2");
       String jwts = signJWT("testiä", "sharedKey");
       System.out.println("eka: "+ jwts);
       System.out.println("toka: "+  openSignJWT(jwts, "sharedKey"));
       
     /* 
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
      */

   }
   
}
