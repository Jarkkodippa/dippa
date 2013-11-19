
import ch.ethz.inf.vs.californium.coap.GETRequest;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.coap.registries.CodeRegistry;
import ch.ethz.inf.vs.californium.coap.registries.MediaTypeRegistry;
import ch.ethz.inf.vs.californium.coap.registries.OptionNumberRegistry;
import ch.ethz.inf.vs.californium.coap.Option;
import ch.ethz.inf.vs.californium.coap.TokenManager;
import ch.ethz.inf.vs.californium.coap.POSTRequest;
import ch.ethz.inf.vs.californium.coap.PUTRequest;
import ch.ethz.inf.vs.californium.coap.DELETERequest;

import ch.ethz.inf.vs.californium.endpoint.ServerEndpoint;
import ch.ethz.inf.vs.californium.endpoint.resources.Resource;
import ch.ethz.inf.vs.californium.endpoint.resources.LocalResource;
import ch.ethz.inf.vs.californium.endpoint.resources.RemoteResource;

import ch.ethz.inf.vs.californium.util.Log;

import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.net.UnknownHostException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jarkko
 */
public class coap 
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
        coapserver.start();
        return true;
    }
    
     static boolean runCoap(String[] args) throws Exception
   {
       // initialize parameters
	String method = null;
        URI uri = null; // URI parameter of the request
        String payload = null;
	boolean loop = false;
        
        // display help if no parameters specified
        if (args.length == 0) 
        {
                printInfo();
                return false;
        }

        Log.setLevel(Level.ALL);
        Log.init();
        
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
                request.setOption(new Option(0, OptionNumberRegistry.OBSERVE));
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
        
        request.setURI(uri);
        request.setPayload(payload);
        request.setToken( TokenManager.getInstance().acquireToken() );
        request.setContentType(MediaTypeRegistry.TEXT_PLAIN);

        // enable response queue in order to use blocking I/O
        request.enableResponseQueue(true);

        //
        request.prettyPrint();
        
        // execute request
        try 
        {
            request.execute();

            // loop for receiving multiple responses
            do 
            {

                // receive response

                System.out.println("Receiving response...");
                Response response = null;
                try 
                {
                        response = request.receiveResponse();
                } 
                catch (InterruptedException e) 
                {
                        System.err.println("Failed to receive response: " + e.getMessage());
                        System.exit(ERR_RESPONSE_FAILED);
                }

                // output response

                if (response != null) 
                {

                    response.prettyPrint();
                    System.out.println("Time elapsed (ms): " + response.getRTT());

                    // check of response contains resources
                    if (response.getContentType()==MediaTypeRegistry.APPLICATION_LINK_FORMAT) 
                    {

                        String linkFormat = response.getPayloadString();

                        // create resource three from link format
                        Resource root = RemoteResource.newRoot(linkFormat);
                        if (root != null) 
                        {

                                // output discovered resources
                                System.out.println("\nDiscovered resources:");
                                root.prettyPrint();

                        } 
                        else 
                        {
                                System.err.println("Failed to parse link format");
                                System.exit(ERR_BAD_LINK_FORMAT);
                        }
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

            } 
            while (loop);

        } 
        catch (UnknownHostException e) 
        {
                System.err.println("Unknown host: " + e.getMessage());
                System.exit(ERR_REQUEST_FAILED);
        } 
        catch (IOException e) 
        {
                System.err.println("Failed to execute request: " + e.getMessage());
                System.exit(ERR_REQUEST_FAILED);
        }

	
        return true;
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
     
     //Coapin serveri luokka. Ottaa pyynnöt vastaan ja käsittelee ne.
   static class coapHandler extends ServerEndpoint
   {


        /*
     * Constructor for a new Hello-World server. Here, the resources
     * of the server are initialized.
     */
        public coapHandler() throws SocketException
        {
            try
            {
            // provide an instance of a Hello-World resource
                addResource(new provResource());
                addResource(new vaarinpain());
                addResource(new jsonresource());
                //jsonresource()
            }
          //  catch (SocketException e)
            catch (Exception e)
            {

                System.err.println("Failed to initialize server: " + e.getMessage());
            }
   //         return true;
        }
        
        
        // Application entry point /////////////////////////////////////////////////
    
    @Override
    public void handleRequest(Request request) 
    {
        
        // Add additional handling like special logging here.
        request.prettyPrint();
        
        // dispatch to requested resource
        super.handleRequest(request);
    }

        /*
     * Definition of the Hello-World Resource
     */
        public class provResource extends LocalResource
        {

            public provResource()
            {

                // set resource identifier
                super("helloWorld");

                // set display name
                setTitle("Hello-World Resource");
            }

            @Override
            public void performGET(GETRequest request)
            {

                // respond to the request
                request.respond(CodeRegistry.RESP_CONTENT, "Hello World!", MediaTypeRegistry.TEXT_PLAIN);
            }
        }
        
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
        
        


    }
}
