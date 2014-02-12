
//import ch.ethz.inf.vs.californium.coap.GETRequest;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.coap.CoAP;
import ch.ethz.inf.vs.californium.coap.MediaTypeRegistry;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
//import ch.ethz.inf.vs.californium.coap.registries.CodeRegistry;
//import ch.ethz.inf.vs.californium.coap.registries.MediaTypeRegistry;
//import ch.ethz.inf.vs.californium.coap.registries.OptionNumberRegistry;
import ch.ethz.inf.vs.californium.coap.Option;
//import ch.ethz.inf.vs.californium.coap.TokenManager;
//import ch.ethz.inf.vs.californium.coap.POSTRequest;
//import ch.ethz.inf.vs.californium.coap.PUTRequest;
//import ch.ethz.inf.vs.californium.coap.DELETERequest;

//import ch.ethz.inf.vs.californium.endpoint.ServerEndpoint;
//import ch.ethz.inf.vs.californium.endpoint.resources.Resource;
//import ch.ethz.inf.vs.californium.endpoint.resources.LocalResource;
//import ch.ethz.inf.vs.californium.endpoint.resources.RemoteResource;

import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;
import ch.ethz.inf.vs.californium.network.Endpoint;
import ch.ethz.inf.vs.californium.network.EndpointManager;
import ch.ethz.inf.vs.californium.network.EndpointManager.ClientMessageDeliverer;
import ch.ethz.inf.vs.californium.network.config.NetworkConfig;
import ch.ethz.inf.vs.scandium.DTLSConnector;

//import ch.ethz.inf.vs.californium.util.Log;

import ch.ethz.inf.vs.californium.network.Exchange;
import ch.ethz.inf.vs.californium.server.resources.Resource;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;

import ch.ethz.inf.vs.californium.server.Server;

import ch.ethz.inf.vs.californium.Utils;

import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.concurrent.Executors;
import java.net.UnknownHostException;
import java.net.InetSocketAddress;

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
    // resource URI path used for discovery
    private static final String DISCOVERY_RESOURCE = "/.well-known/core";
    // indices of command line parameters
    private static final int IDX_METHOD          = 0;
    private static final int IDX_URI             = 1;
    private static final int IDX_PAYLOAD         = 2;
    // exit codes for runtime errors
    private static final int ERR_MISSING_METHOD  = 1;
    private static final int ERR_UNKNOWN_METHOD  = 2;
    private static final int ERR_MISSING_URI     = 3;
    private static final int ERR_BAD_URI         = 4;
    private static final int ERR_REQUEST_FAILED  = 5;
    private static final int ERR_RESPONSE_FAILED = 6;
    private static final int ERR_BAD_LINK_FORMAT = 7;
    
    /*
    // initialize parameters
    String method = null;
    URI uri = null;
    String payload = "";
    boolean loop = false;
        */
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
   
   //Käynnistää Coap serverin.
    boolean runCoapserver() throws Exception
    {
        coapHandler coapserver = new coapHandler();
        tulostaOtsikko("kokeillaan");
   //     coapserver.start();
        return true;
    }
    
     static String runCoap(String[] args) throws Exception
   {
       // initialize parameters
	String method = null;
        URI uri = null;
        String payload = "";
        boolean loop = false;
        
        // display help if no parameters specified
        if (args.length == 0) 
        {
                printInfo();
                return "";
             //   return false;
        }

     //   Log.setLevel(Level.ALL);
     //   Log.init();
        
        // input parameters
        int idx = 0;
        for (String arg : args) 
        {
            if (arg.startsWith("-")) 
            {
                if (arg.equals("-l")) 
                {
                    loop = true;
                } 
                else 
                {
                    System.out.println("Unrecognized option: " + arg);
                }
            } 
            else 
            {
                switch (idx) 
                {
                case IDX_METHOD:
                        method = arg.toUpperCase();
                        break;
                case IDX_URI:
                        try 
                        {
                                uri = new URI(arg);
                        } 
                        catch (URISyntaxException e) 
                        {
                                System.err.println("Failed to parse URI: " + e.getMessage());
                                System.exit(ERR_BAD_URI);
                        }
                        break;
                case IDX_PAYLOAD:
                        payload = arg;
                        break;
                default:
                        System.out.println("Unexpected argument: " + arg);
                }
                ++idx;
            }
        }
        
        // check if mandatory parameters specified
        if (method == null) 
        {
                System.err.println("Method not specified");
                System.exit(ERR_MISSING_METHOD);
        }
        if (uri == null) 
        {
                System.err.println("URI not specified");
                System.exit(ERR_MISSING_URI);
        }
        
        // create request according to specified method
        Request request = newRequest(method);
        if (request == null) 
        {
                System.err.println("Unknown method: " + method);
                System.exit(ERR_UNKNOWN_METHOD);
        }
        
        if (method.equals("OBSERVE")) 
        {
//                request.setOption(new Option(0, OptionNumberRegistry.OBSERVE));
                loop = true;
        }

        // set request URI
        if (method.equals("DISCOVER") && (uri.getPath() == null || uri.getPath().isEmpty() || uri.getPath().equals("/"))) {
                // add discovery resource path to URI
            try 
            {
                uri = new URI(uri.getScheme(), uri.getAuthority(), DISCOVERY_RESOURCE, uri.getQuery());

            } 
            catch (URISyntaxException e) 
            {
                System.err.println("Failed to parse URI: " + e.getMessage());
                System.exit(ERR_BAD_URI);
            }
        }
        System.out.println("jeeeee");
        request.setURI(uri);
        System.out.println("jeergg");
        request.setPayload(payload);
        System.out.println("jeettttt");
        request.getOptions().setContentFormat(MediaTypeRegistry.TEXT_PLAIN);
 //       request.setToken( TokenManager.getInstance().acquireToken() );
  //      request.setContentType(MediaTypeRegistry.TEXT_PLAIN);

        // enable response queue in order to use blocking I/O
   //     request.enableResponseQueue(true);

        System.out.println("jeeeeegfggf");
        //
     //   request.prettyPrint();
        System.out.println( request.getDestination().toString() );
        System.out.println( request.getDestinationPort() );

        System.out.println("jfgsjgkd");
        if (request.getScheme().equals(CoAP.COAP_SECURE_URI_SCHEME)) {
                Endpoint dtlsEndpoint = new CoAPEndpoint(new DTLSConnector(new InetSocketAddress(0)), NetworkConfig.getStandard());
                dtlsEndpoint.setMessageDeliverer(new ClientMessageDeliverer());
                dtlsEndpoint.start();
                EndpointManager.getEndpointManager().setDefaultSecureEndpoint(dtlsEndpoint);
        } 
        // execute request
        try 
        {
            request.send();
            //request.execute();

            // loop for receiving multiple responses
            do 
            {

                // receive response

                System.out.println("Receiving response...");
                Response response = null;
                try 
                {
                    response = request.waitForResponse();
                 //       response = request.receiveResponse();
                } 
                catch (InterruptedException e) 
                {
                        System.err.println("Failed to receive response: " + e.getMessage());
                        System.exit(ERR_RESPONSE_FAILED);
                }

                System.out.println("seuraavaReceiving response...");
                // output response

                if (response != null) 
                {

     //               response.prettyPrint();
                    System.out.println(Utils.prettyPrint(response));
                    System.out.println("Time elapsed (ms): " + response.getRTT());
                    
                    
                    // check of response contains resources
                    if (response.getOptions().hasContentFormat(
                            MediaTypeRegistry.APPLICATION_LINK_FORMAT)) 
                    {

                            String linkFormat = response.getPayloadString();

                            System.out.println("payloadReceiving response...");
                            // output discovered resources
                            System.out.println("\nDiscovered resources:");
                            System.out.println(linkFormat);

                //            return linkFormat;
                    } 
                    else 
                    {

                            // check if link format was expected by client
                            if (method.equals("DISCOVER")) 
                            {
                                    System.out.println("Server error: Link format not specified");
                            }
                    }


                } 
                else 
                {

                    // no response received	
                    System.err.println("Request timed out");
                    break;
                }

       //         Utils.Payload
                return Utils.prettyPrintPayload(response);
            } 
            while (loop);

        } 

        catch (Exception e) 
        {
                System.err.println("Failed to execute request: " + e.getMessage());
                System.exit(ERR_REQUEST_FAILED);
        }

	System.out.println();
        return "";
    }
     
     /*
	 * Outputs user guide of this program.
	 */
	public static void printInfo() 
        {
		System.out.println("Californium (Cf) Example Client");
		System.out.println("(c) 2012, Institute for Pervasive Computing, ETH Zurich");
		System.out.println();
		System.out.println("Usage: [-l] METHOD URI [PAYLOAD]");
		System.out.println("  METHOD  : {GET, POST, PUT, DELETE, DISCOVER, OBSERVE}");
		System.out.println("  URI     : The CoAP URI of the remote endpoint or resource");
		System.out.println("  PAYLOAD : The data to send with the request");
		System.out.println("Options:");
		System.out.println("  -l      : Loop for multiple responses");
		System.out.println("           (automatic for OBSERVE and separate responses)");
		System.out.println();
		System.out.println("Examples:");
		System.out.println("  ExampleClient DISCOVER coap://localhost");
		System.out.println("  ExampleClient POST coap://vs0.inf.ethz.ch:5683/storage my data");
	}

	/*
	 * Instantiates a new request based on a string describing a method.
	 * 
	 * @return A new request object, or null if method not recognized
	 */
        /*
	private static Request newRequest(String method) 
        {
		if (method.equals("GET")) 
                {
			return new GETRequest();
		} 
                else if (method.equals("POST")) 
                {
			return new POSTRequest();
		} 
                else if (method.equals("PUT")) 
                {
			return new PUTRequest();
		} 
                else if (method.equals("DELETE")) 
                {
			return new DELETERequest();
		} 
                else if (method.equals("DISCOVER")) 
                {
			return new GETRequest();
		} 
                else if (method.equals("OBSERVE")) 
                {
			return new GETRequest();
		} 
                else 
                {
			return null;
		}
	}
        */
        /*
	 * Instantiates a new request based on a string describing a method.
	 * 
	 * @return A new request object, or null if method not recognized
	 */
	private static Request newRequest(String method) 
        {
		if (method.equals("GET")) 
                {
			return Request.newGet();
		} 
                else if (method.equals("POST")) 
                {
                    
			return Request.newPost();
		} 
                else if (method.equals("PUT")) 
                {
			return Request.newPut();
		} 
                else if (method.equals("DELETE")) 
                {
			return Request.newDelete();
		} 
                else if (method.equals("DISCOVER")) 
                {
                    System.out.println("aloitetaan");
			return Request.newGet();
		} 
                else if (method.equals("OBSERVE")) 
                {
			Request request = Request.newGet();
			request.setObserve();
//			loop = true;
			return request;
		} 
                else 
                {
			System.err.println("Unknown method: " + method);
			System.exit(ERR_UNKNOWN_METHOD);
			return null;
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
            /*
            try
            {
            // provide an instance of a Hello-World resource
                addResource(new provResource());
                addResource(new vaarinpain());
                addResource(new jsonresource());
                addResource(new Yhteys());
                //jsonresource()
            }
          //  catch (SocketException e)
            catch (Exception e)
            {

                System.err.println("Failed to initialize server: " + e.getMessage());
            }
            */
   //         return true;
            Server server = new Server();
            server.setExecutor(Executors.newScheduledThreadPool(4));

      //      server.add(new provResource());
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
    @Override
    public void handleRequest(Request request) 
    {
        String osoite1 = request.getLocationPath();
        System.out.println("osoiteloc " + ": " + osoite1);
        osoite1 = request.getLocationQuery();
        System.out.println("osoitelque " + ": " + osoite1);
        osoite1 = request.getUriHost();
        System.out.println("osoitehost " + ": " + osoite1);
        osoite1 = request.getUriPath();
        System.out.println("osoitepath " + ": " + osoite1);
//        osoite1 = request.getUriQuery();
//        System.out.println("osoiteuriquer " + ": " + osoite1);
        if(osoite1.contains("/Yhteys/")) 
        {
            osoite1 = osoite1.replaceAll("/Yhteys/", "");
            try
            {
                osoite1 = URLEncoder.encode(osoite1, "UTF-8");
            }
            catch (Exception e)
            {
                System.err.println("virhe");
            }
            request.setPayload(osoite1);
            request.setUriPath("/Yhteys");
        }
        
        
        // Add additional handling like special logging here.
        request.prettyPrint();
        
        // dispatch to requested resource
        super.handleRequest(request);
    }
*/
        /*
     * Definition of the Hello-World Resource
     */
        public class provResource extends ResourceBase
        {

            public provResource()
            {

                // set resource identifier
                super("helloWorld");

                // set display name
     //           setTitle("Hello-World Resource");
            }

           @Override
            public void handleGET(Exchange exchange) 
            {
                    Response response = new Response(ResponseCode.CONTENT);
                    response.setPayload("hello world");
                    respond(exchange, response);
            }
        }
        
        /*
        public class vaarinpain extends LocalResource 
        {

            public vaarinpain() 
            {
                    super("vaarinpain");
                    setTitle("Teksti käännetään väärinpäin");
                    setResourceType("väärinpäinmuokkaaja");
            }

            @Override
            public void performPOST(POSTRequest request) 
            {

                    if (request.getContentType()!=MediaTypeRegistry.TEXT_PLAIN) {
                            request.respond(CodeRegistry.RESP_UNSUPPORTED_MEDIA_TYPE, "Use text/plain");
                            return;
                    }
                    String paluu = new StringBuilder(request.getPayloadString()).reverse().toString();

                    // complete the request
                    request.respond(CodeRegistry.RESP_CONTENT, paluu, MediaTypeRegistry.TEXT_PLAIN);
            }
        }
        */
        
        


    }
}
