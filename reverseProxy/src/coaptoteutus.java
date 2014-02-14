
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.Exchange;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

import ch.ethz.inf.vs.californium.server.Server;
import java.net.SocketException;
import java.util.concurrent.Executors;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jarkko
 */
public class coaptoteutus
{
 
    
    //Käynnistää Coap serverin.
    boolean runCoapserver() throws Exception
    {
        coapHandler coapserver = new coapHandler();
        tulostaOtsikko("Coap reverse Proxy käynnistetty");

        return true;
    }
    /** Tulostetaan otsikko annetulla merkill� kehystettyn�.
     * @param teksti kehystett�v� otsikkotesti.
     */
   private void tulostaOtsikko(String teksti) 
   {
      // Kehysmerkki.
      final char MERKKI_KEHYS = '*';

      // Reunan ja tekstin v�li vakiona.
      final String VALI = " ";

      // Selvitet��n merkkijonon pituus.
      int pituus = teksti.length();

      // Jos pituus oli OK, niin tulostetaan.
      if (pituus > 0) {
         // Tehd��n yl�- ja alarivi.
         String ylaala = "";
         for (int i = 0; i < pituus + 2 * (VALI.length() + 1); i++)
            ylaala += MERKKI_KEHYS;

         // Yl�rivi.
         System.out.println(ylaala);

         // Keskimm�inen rivi.
         System.out.println(MERKKI_KEHYS + VALI + teksti + VALI + MERKKI_KEHYS);

         // Alarivi.
         System.out.println(ylaala);
      }
   }
   



     //Coapin serveri luokka. Ottaa pyynnöt vastaan ja käsittelee ne.
   static class coapHandler// extends ServerEndpoint
   {


        /*
     * Constructor for a new Hello-World server. Here, the resources
     * of the server are initialized.
     */
        public coapHandler() throws SocketException
        {
           
            Server server = new Server();
            server.setExecutor(Executors.newScheduledThreadPool(4));

            server.add(new provResource());
      //      server.add(new vaarinpain());
      //      server.add(new jsonresource());
            server.add(new Yhteys());
   //         server.add(new MirrorResource("mirror"));
    //        server.add(new LargeResource("large"));

            server.start();
        }
        
        /*
	 *  Sends a GET request to itself
	 */
	public static void selfTest() 
        {
		try {
			Request request = Request.newGet();
			request.setURI("localhost:5683/hello");
			request.send();
			Response response = request.waitForResponse(1000);
			System.out.println("received "+response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
        // Application entry point /////////////////////////////////////////////////
    
    
        /*
     * Definition of the Hello-World Resource
     */
        public class provResource extends ResourceBase
        {

            public provResource()
            {

                super("helloWorld");

            }

           @Override
            public void handleGET(Exchange exchange) 
            {
                    Response response = new Response(ResponseCode.CONTENT);
                    response.setPayload("hello world");
                    respond(exchange, response);
            }
        }
        
   
    }
   

}
