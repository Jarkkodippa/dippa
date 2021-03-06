

import ch.ethz.inf.vs.californium.coap.Request;

import ch.ethz.inf.vs.californium.coap.CoAP;
import ch.ethz.inf.vs.californium.coap.MediaTypeRegistry;


import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.CoAPEndpoint;
import ch.ethz.inf.vs.californium.network.Endpoint;
import ch.ethz.inf.vs.californium.network.EndpointManager;
import ch.ethz.inf.vs.californium.network.EndpointManager.ClientMessageDeliverer;
import ch.ethz.inf.vs.californium.network.config.NetworkConfig;
import ch.ethz.inf.vs.scandium.DTLSConnector;


import ch.ethz.inf.vs.californium.Utils;


import java.net.URI;
import java.net.URISyntaxException;

import java.net.InetSocketAddress;


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
    private static final int IDX_CONTENT_TYPE    = 3;
    // exit codes for runtime errors
    private static final int ERR_MISSING_METHOD  = 1;
    private static final int ERR_UNKNOWN_METHOD  = 2;
    private static final int ERR_MISSING_URI     = 3;
    private static final int ERR_BAD_URI         = 4;
    private static final int ERR_REQUEST_FAILED  = 5;
    private static final int ERR_RESPONSE_FAILED = 6;
   

    
     static String runCoap(String[] args) throws Exception
   {
       // initialize parameters
	String method = null;
        URI uri = null;
        String payload = "";
        boolean loop = false;
        int contentType = 0;
        
        // display help if no parameters specified
        if (args.length == 0) 
        {
                printInfo();
                return "";
        }

        
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
                case IDX_CONTENT_TYPE:
                        contentType = Integer.parseInt(arg);
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
        
        request.getOptions().setContentFormat(
                        contentType);

        System.out.println( request.getDestination().toString() );
        System.out.println( request.getDestinationPort() );

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


            // loop for receiving multiple responses
            do 
            {

                // receive response

                System.out.println("Receiving response...");
                Response response = null;
                try 
                {
                    response = request.waitForResponse();
                } 
                catch (InterruptedException e) 
                {
                        System.err.println("Failed to receive response: " + e.getMessage());
                        System.exit(ERR_RESPONSE_FAILED);
                }


                if (response != null) 
                {

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
			return request;
		} 
                else 
                {
			System.err.println("Unknown method: " + method);
			System.exit(ERR_UNKNOWN_METHOD);
			return null;
		}
	}
     
  

    
}
