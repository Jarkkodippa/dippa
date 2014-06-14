
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

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;


import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;

import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.SignedJWT;


import org.json.JSONObject;


import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

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

         } 
        catch (ParseException e)
         {

                 System.err.println("Couldn't parse JWS object2: ");
                 return "";
         }

        return serialized;
        
    }
   
   //Make signed JSON Web Token.
   // Return serialized token.
   public static String signJWT(String message, String sharedKey, 
                                    String btid) 
   {


       
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
        header.setCustomParameter("btid", btid);

        String s = cryptJWS(message, sharedKey, header);
        System.out.println("crypted: "+ s);
        String serialized = "";


        try
         {
            SignedJWT  t = SignedJWT.parse(s);

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

         } 
        catch (ParseException e)
         {

                 System.err.println("Couldn't parse JWS object2: ");
                 return "";
         }

        return serialized;
        
    }
   
   
    //Make encrypted JSON Web Token.
   // Return serialized token.
   public String encryptJWT(String message, String sharedKey) 
   {

        JWEHeader header = new JWEHeader(JWEAlgorithm.DIR,EncryptionMethod.A128CBC_HS256);
        String s = cryptJWE1(message, sharedKey, header);

        return s;
        
    }
   
    //Make encrypted JSON Web Token.
   // Return serialized token.
   public static String encryptJWT1(String message, String sharedKey, String btid) 
   {

        JWEHeader header = new JWEHeader(JWEAlgorithm.DIR,EncryptionMethod.A128CBC_HS256);
        header.setCustomParameter("btid", btid);
        String s = cryptJWE1(message, sharedKey, header);
        
        System.out.println("crypted: "+ s);
        
        return s;
    }
   
    //Open signed JSON Web Token.
   // Return open string.
   public static String openSignJWT(String message, String sharedKey) 
   {    
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);


        System.out.println("viesti : "+ message);
        try
         {
            SignedJWT signedJWT = new SignedJWT(header.toBase64URL(), 
            Base64URL.encode("secondPart"), Base64URL.encode("thirdPart"));

             
            signedJWT = SignedJWT.parse(message);

            Base64URL[] Parts = signedJWT.getParsedParts();
            Base64URL firstPart = Parts[0];
            Base64URL secondPart = Parts[1];
            Base64URL thirdPart = Parts[2];
            
            message = secondPart.decodeToString();
            
            return uncryptJWS(message, sharedKey);


         } 
        catch (ParseException e)
         {

                 System.err.println("Couldn't parse JWS object: ");
                 return "";
         }

        
    }
   
    //Open signed JSON Web Token.
   // Return btid value on string.
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
   
    //Open encrypted JSON Web Token.
   // Return btid value on string.
   public static String retrunencryptJWTbtid(String message) 
   {
        JWEHeader header = new JWEHeader(JWEAlgorithm.DIR,EncryptionMethod.A128CBC_HS256);

        System.out.println("viesti : "+ message);

        Map<String, Object> headermap = new HashMap<String,Object>();
        String headerpart = "";
        try
         {
            EncryptedJWT EncryptedJWT = new EncryptedJWT(header.toBase64URL(), 
            Base64URL.encode("secondPart"), Base64URL.encode("thirdPart"),
            Base64URL.encode("rourthPart"), Base64URL.encode("fifthPart"));
 
            EncryptedJWT = EncryptedJWT.parse(message);

            Base64URL[] Parts = EncryptedJWT.getParsedParts();
            Base64URL firstPart = Parts[0];
            Base64URL secondPart = Parts[1];
            Base64URL thirdPart = Parts[2];
            
            headerpart = firstPart.decodeToString();
            headermap = readJSON(headerpart);
            return (String)headermap.get("btid");
    
         } 
        catch (ParseException e)
         {

                 System.err.println("Couldn't parse JWE object: ");
                 return "";
         }
 
        
    }
   
    //Cryptaa Stringin JVS 
    //Palauttaa cryptatun JVS:n.
   public static String cryptJWS(String message, String sharedKey, JWSHeader header) 
   {
        Payload payload = new Payload(message);
        
        System.out.println("JWS payload message: " + message);

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
   
 
	
   //purkaa JWS.
   //Palauttaa puretun viestin Stringinä.
   public static String uncryptJWS(String message, String sharedKey) 
   {
        JWSObject jwsObject;
        
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
   
   
   
   //Cryptaa JWE:n 
    //Palauttaa cryptatun JWT:n.
   public static String cryptJWE1(String message, String sharedKey, JWEHeader header) 
   {

       Payload payload = new Payload(message);
       header.setContentType("text/plain");
       
       JWEObject jweObject = new JWEObject(header, payload);

       try 
       {
           byte[] sharedKeybyte = Hex.decodeHex(sharedKey.toCharArray());
           System.out.println("Shared key: " + sharedKey);

            try
            {
                DirectEncrypter encrypter = new DirectEncrypter(sharedKeybyte);
                jweObject.encrypt(encrypter);

            } catch (JOSEException e)
            {

                    System.err.println("Couldn't encrypt JWE object: " + e.getMessage());
                    return "";
            }

       } 
       catch (DecoderException ex) 
       {
           Logger.getLogger(json.class.getName()).log(Level.SEVERE, null, ex);
       }

        
        
        // Serialise JWS object to compact format
        String s = jweObject.serialize();
        
        System.out.println("Serialised JWS object: " + s);
        
        
        return s;
        
    }
   
   //purkaa JWE.
   //Palauttaa puretun viestin Stringinä.
   public static String uncryptJWE1(String message, String sharedKey) 
   {
        JWEObject jweObject;

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
            byte[] sharedKeybyte = Hex.decodeHex(sharedKey.toCharArray());
            DirectDecrypter decrypter = new DirectDecrypter(sharedKeybyte);
            jweObject.decrypt(decrypter);
                
        } 
        catch (JOSEException e) 
        {
        
                System.err.println("Couldn't decrypt message: " + e.getMessage());
        }
        catch (DecoderException e) 
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
    
        return vali;
    }

         
   //Ottaa vastaan JSON objectin.
   //Palauttaa Json Stringin.
 public static String createJsonString(JSONObject json) throws IOException 
 {

    String palautus = ""; 
 
     
    palautus = json.toString();

    return palautus;
 }
 
 //Ottaa vastaan MAP rakenteen.
 //Palauttaa Json Stringin.
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

 //Lukee json sisältävän String merkkijonon.
 //Palauttaa merkkijonosta luodun MAP rakenteen.
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
 
            HashMap<String, Object> wwwauthenticate = new HashMap<String, Object>();

            wwwauthenticate.put( "type", "digest" );  

            wwwauthenticate.put( "challenge", challenge );

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

            valiaikainen.put("digest-response", digestresponse);

            valiaikainen.put("type", "digest");

            valiaikainen2.put("authorization", valiaikainen);
            String palautus = createJsonString(valiaikainen2);

            return palautus;
        
      }
      catch(Exception e) 
      {

          return "";
      }
   }
   
   //Ottaa vastaan MAP rakenteen.
   //Map rakenteesta metodi siirtää WWW-Authenticate, Set-Cookie ja Body
   //osiot JSON String tiedostoon.
   //Palauttaa JSON String tiedoston.
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
 
                authorization2.put( "www-authenticate", wwwauthenticate );

            }
            if(data.containsKey("Set-Cookie"))
            {
            
                authorization2.put( "Set-Cookie", 
                        (HashMap<String,Object>)data.get("Set-Cookie") );

            }
            if(data.containsKey("Body"))
            {

                authorization2.put( "Body", data.get("Body") );

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
   

    //Ottaa vastaan MAP rakenteen.
   //Map rakenteesta metodi siirtää Authorization ja Set-Cookie
   //osiot JSON String tiedostoon. 
   //Authorization kentän tyypiksi asettaa digest
   //Palauttaa JSON String tiedoston.
    public static String writeJSONauthentication(Map data) 
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

            valiaikainen.put("type", "digest");

            valiaikainen2.put("authorization", valiaikainen);
          }
          if(data.containsKey("Cookie"))
          {

                valiaikainen2.put( "Cookie", 
                        (HashMap<String,Object>)data.get("Cookie") );

          }
            String palautus = createJsonString(valiaikainen2);

            return palautus;
      }
      catch(Exception e) 
      {

          return "";
      }
   }
   
   //Ottaa vastaan MAP rakenteen.
   //Map rakenteesta metodi siirtää Authorization ja Set-Cookie
   //osiot JSON String tiedostoon. 
   //Authorization kentän tyypiksi asettaa akadigest
   //Palauttaa JSON String tiedoston.
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

            valiaikainen.put("type", "akadigest");

            valiaikainen2.put("authorization", valiaikainen);
          }
          if(data.containsKey("Cookie"))
          {

                valiaikainen2.put( "Cookie", 
                        (HashMap<String,Object>)data.get("Cookie") );

          }
            String palautus = createJsonString(valiaikainen2);

            return palautus;
      }
      catch(Exception e) 
      {

          return "";
      }
   }
    /** Pääsilmukka testausta varten.
     */
   public void suoritaPaasilmukkaa() 
   {
 
       String kdfsarvo = "4300c02e081e5a267b40470f4a759aa9df88a58eb3c8789f1fe10fb5054857cb";
       String RESarvo = "QwDALggeWiZ7QEcPSnWaqd+IpY6zyHifH+EPtQVIV8s=";
       String avain = "27ab05d20a775e7a1309534be410a955";
   
       byte[] kdfsarvobyte;
       try 
       {
           kdfsarvobyte = Hex.decodeHex(kdfsarvo.toCharArray());
           System.out.println("pituus: "+avain.getBytes().length);
            System.out.println("pituuskdf: "+kdfsarvo.getBytes().length);
            System.out.println("pituuskdf: "+kdfsarvobyte.length);
            System.out.println("pituusres: "+RESarvo.getBytes().length);
            String jwts = signJWT("testiä", "sharedKey");
            System.out.println("ekajws: "+ jwts);
            System.out.println("tokajws: "+  openSignJWT(jwts, "sharedKey"));
        //    EncryptionMethod kaytetaan = new EncryptionMethod(EncryptionMethod.A128CBC_HS256.getName(), null, 256);
           JWEHeader header = new JWEHeader(JWEAlgorithm.DIR,EncryptionMethod.A128CBC_HS256);
      //      JWEHeader header = new JWEHeader(JWEAlgorithm.DIR,EncryptionMethod.A128CBC_HS256);
     //      JWEHeader header = new JWEHeader(JWEAlgorithm.DIR,EncryptionMethod.A256CBC_HS512);
      //      JWEHeader header = new JWEHeader(JWEAlgorithm.DIR,kaytetaan);
            //alla oleva toimii
          //  JWEHeader header = new JWEHeader(JWEAlgorithm.DIR,EncryptionMethod.A256GCM);
            String jwet = cryptJWE1("testiä", kdfsarvo, header); 
       //     String jwet = cryptJWE1("testiä", kdfsarvo, header); 
            System.out.println("ekajwe: "+ jwet);
            jwet = uncryptJWE1(jwet, kdfsarvo); 
       //     jwet = uncryptJWE1(jwet, kdfsarvo);
            System.out.println("tokajwe: "+ jwet);
       } 
       catch (DecoderException ex) 
       {
           Logger.getLogger(json.class.getName()).log(Level.SEVERE, null, ex);
       }
       
     // String s = cryptJVS("testataam", "sharedKey2");
   //   uncryptJVS(cryptJVS("testataam", "sharedKey2"), "sharedKey2");
       
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
