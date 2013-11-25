/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jarkko
 * testaamiseen kaikkee turhaa säätöö ei tarvi välittää.
 */
public class startCoap 
{

     //jee
   public static void main(String[] args) 
   {
       
        args = new String[3];
        String url = "jee";
        String viesti = "testi";
  //      coap servu = new coap();
        //http://" +myNafFqdn+ ":5683/
  //      String osoite = "coap://localhost";
   //     String osoite = "coap://localhost/vaarinpain";
  //      String osoite = "coap://localhost/Yhteys/foo.bar.com/httpresurssi/";
        String osoite = "coap://localhost/Yhteys";
  //      String osoite1 = "localhost/helloWorld";
  //      int osoite1 = osoite.length();
        String kaannettava = "jee";
      //  String tarkenne = "laitettu dataa";
   //     String tarkenne = "foo.bar.com/httpresurssi/";
        String tarkenne = "www.iltalehti.fi";
    //    String tarkenne = "/kokeilu/";
        //ExampleClient POST coap://vs0.inf.ethz.ch:5683/storage my data"
        args[0] = "POST";
  //      args[0] = "DISCOVER";
  //      args[0] = "OBSERVE";
  //      args[0] = "GET";
  //      args[0] = "PUT";
        args[1] = osoite; //"coap://localhost";
        
  //      args[2] = "vaarinpain";
   //     args[2] = "provResource";
        args[2] = tarkenne;
  //      args[2] = "";
               
        coap coap = new coap();
       
        
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
            System.err.println("virhe");
        }
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
            System.err.println("virhe");
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
