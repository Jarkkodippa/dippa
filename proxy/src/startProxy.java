/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jarkko
 */
public class startProxy 
{

    //jee
   public static void main(String[] args) 
   {
       System.out.println("Start ");
       try
       {
            json json = new json();
            System.out.println("json käynnissä ");
            coap servu = new coap();
            servu.runCoapserver();
            System.out.println("coap servu käynnissä ");
            HTTP http = new HTTP();
            System.out.println("http käynnissä ");
            
            
            coap coap = new coap();
            args = new String[3];
            args[0] = "POST";
   //         args[0] = "DISCOVER";
   //     args[0] = "GET";
            String osoite = "coap://localhost/vaarinpain";
            args[2] = "kaannettava";
            
            args[1] = osoite;
            coap.runCoap(args);
            args[2] = json.cryptJVS(args[2], "sharedKey2");
            coap.runCoap(args);
            
            
            osoite = "coap://localhost/jsonresource";
            args[1] = osoite;
   //         String kokeilu = json.;
            args[2] = json.writeJSONauthentication("type",  "challenges", 
                                                 "realm", "qop", 
                                                 "nonce",  "opaque");
            coap.runCoap(args);
     //       json.uncryptJVS(args[2], "sharedKey2");
            System.out.println("coap asiakas käynnissä ");
            
 //     json.suoritaPaasilmukkaa();
      }
     catch (Exception e)
     {
         System.err.println("virhe");
     }

   }
  
}
