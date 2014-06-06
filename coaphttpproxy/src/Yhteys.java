
//import java.util.logging.Logger;


//import ch.ethz.inf.vs.californium.coap.LinkFormat;
//import ch.ethz.inf.vs.californium.coap.Option;

//import ch.ethz.inf.vs.californium.coap.CoAP;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
//import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
//import ch.ethz.inf.vs.californium.coap.MediaTypeRegistry;

import ch.ethz.inf.vs.californium.network.Exchange;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;
import ch.ethz.inf.vs.californium.server.resources.Resource;


import java.util.HashMap;
import java.util.Map;
//import java.net.URLDecoder;

//import java.util.Arrays;
//import java.util.LinkedList;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.MediaTypeRegistry;

/*
 * Yhteys luokka. 
Ottaa vastaan coap postin johonkin HTTP osoitteeseen.
Ottaa yhteyden palauttaa yhteyden otosta saatavan datan.
Putilla voidaan vastata annettuun viestiin.
 * 
 */
public class Yhteys extends ResourceBase// extends LocalResource 
{

    private HTTPClass yhteys;
    //private byte[] data; 
    private String content;
    private String osoite;
    // Constructors ////////////////////////////////////////////////////////////

    /*
     * Default constructor.
     */
    public Yhteys() 
    {
            this("Yhteys");
    }

    /*
     * Yhteys constructor
     */
    public Yhteys(String resourceIdentifier) 
    {
        super(resourceIdentifier);
        getAttributes().setTitle("Luodaan HTTP yhteys!");

        getAttributes().addResourceType("reverse proxy");

        yhteys = new HTTPClass();
        osoite = "";

    }

    // REST Operations /////////////////////////////////////////////////////////

    /*
     * GETs the content of this storage resource. 
     * If the content-type of the request is set to application/link-format 
     * or if the resource does not store any data, the contained sub-resources
     * are returned in link format.
     */
    
    @Override
    public void handleGET(Exchange exchange) 
    {
/*
        if (content != null) 
        {
		request.respond(content);
	} 
        else 
        {
                String subtree = LinkFormat.serializeTree(this);
                Response response = new Response(ResponseCode.CONTENT);
                response.setPayload(subtree);
                response.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_LINK_FORMAT);
                request.respond(response);
        }
    */
        String osoite1 = exchange.toString();
        System.out.println("osoite1 :" +osoite1);

        Map<String, String> arvot = new HashMap<String, String>();
        Request request = exchange.getRequest();
        
        // get request payload as a string
  //      String payload = request.getPayloadString();
        
        System.out.println("osoite4 :" + request.getURI());
        String kokouri = request.getURI();
        uriShortener(kokouri);

        System.out.println("osoite :" +osoite);
  //      System.out.println("payload " + ": " + payload);
        
        Response response = new Response(ResponseCode.CREATED);

            
        json json = new json();
        try
        {

            System.out.println("osoite " + ": " + osoite);
            osoite = "http://"+osoite;
            arvot = yhteys.httpClientReq(osoite);

            System.out.println("arvot " + ": " + arvot);
            System.out.println("arvot loppu");

            String jsonstring = "";
            if(!arvot.containsKey("WWW-Authenticate") && 
                        !arvot.containsKey("Set-Cookie") )
            {
                System.out.println("arvot eka ");
                //createJsonString(
                jsonstring = arvot.get("Body");
                response.getOptions().setContentFormat(
                        MediaTypeRegistry.TEXT_HTML);
        //        jsonstring = json.createJsonString(arvot);
            }
            else
            {
                jsonstring = json.writeJSONauthorization(arvot);
                System.out.println("jsonstring :" + jsonstring);
                response.getOptions().setContentFormat(
                        MediaTypeRegistry.APPLICATION_JSON);
            }

            content = jsonstring;

            changed();
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }


  //      Response response = new Response(ResponseCode.CREATED);
        response.setPayload(content);
        exchange.respond(response);


    }

    /*
     * PUTs content to http server.
     */
    @Override
    public void handlePUT(Exchange exchange) 
    {

    //    String paluu = storeAnswer(exchange);
        
        //Read coap client message
        Request request = exchange.getRequest();

        Map<String, Object> arvot = new HashMap<String,Object>();

        json json = new json();

        //Save the payload
        content = exchange.getRequest().getPayloadString();
        System.out.println("sisältö yhteys :" +content);

        String paluu = "";
        String kokouri = request.getURI();
        uriShortener(kokouri);
        Response response = new Response(ResponseCode.CHANGED);
        try
        {
 
            osoite = "http://"+osoite;
            //Json content to map
            arvot = json.readJSON(content);
            
            if(content.contains("akadigest"))
            {
      //          arvot = json.readJSON(content);
                arvot = yhteys.httpbtClientAut(arvot, osoite);
                
                 System.out.println("arvot " + ": " + arvot);
            //    if(arvot.containsKey("body"))
                if(!arvot.containsKey("WWW-Authenticate") && 
                        !arvot.containsKey("Set-Cookie") )
                {
                    paluu = (String) arvot.get("Body");
                }
                else
                {
                    String jsonstring = json.writeJSONauthorization(arvot);
                    paluu = jsonstring;
                    response.getOptions().setContentFormat(
                        MediaTypeRegistry.APPLICATION_JSON);
                }
            }
            else if(content.contains("digest"))
            {
        //        arvot = json.readJSON(content);
                paluu = yhteys.httpClientAut2(arvot, osoite);
                response.getOptions().setContentFormat(
                        MediaTypeRegistry.TEXT_HTML);
            }
            else
            {
                yhteys.httpPostClient(osoite, arvot);
            }
            //httpPostClient(String osoite, Map tiedot)
   //         System.out.println("arvot mapin sisältä " + ": " + arvot);
   //         System.out.println("osoite " + ": " + osoite);
            //Content to http client
    //        paluu = yhteys.httpClientAut2(arvot, osoite);
    
            /*
            if(arvot.containsKey("body"))
            {
                content = (String) arvot.get("body");
            }
            else
            {
                String jsonstring = json.writeJSONauthorization(arvot);
                content = jsonstring;
            }
            */
    
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        

        changed();

        
 //       CoAP.MessageFormat.
   //     response.setType(CoAP.Type.CON);
        
        
        response.setPayload(paluu);
        respond(exchange, response);

    }
    /*
    private String storeAnswer(Exchange exchange) 
    {

        //Read coap client message
        Request request = exchange.getRequest();

        Map<String, Object> arvot = new HashMap<String,Object>();

        json json = new json();

        //Save the payload
        content = exchange.getRequest().getPayloadString();
        System.out.println("sisältö yhteys :" +content);

        String paluu = "";
        String kokouri = request.getURI();
        uriShortener(kokouri);
        try
        {
 
            osoite = "http://"+osoite;
            //Json content to map
            arvot = json.readJSON(content);
            
            if(content.contains("akadigest"))
            {
      //          arvot = json.readJSON(content);
                arvot = yhteys.httpbtClientAut(arvot, osoite);
                
                 System.out.println("arvot " + ": " + arvot);
            //    if(arvot.containsKey("body"))
                if(!arvot.containsKey("WWW-Authenticate") && 
                        !arvot.containsKey("Set-Cookie") )
                {
                    paluu = (String) arvot.get("Body");
                }
                else
                {
                    String jsonstring = json.writeJSONauthorization(arvot);
                    paluu = jsonstring;
                }
            }
            else if(content.contains("digest"))
            {
        //        arvot = json.readJSON(content);
                paluu = yhteys.httpClientAut2(arvot, osoite);
            }
            else
            {
                yhteys.httpPostClient(osoite, arvot);
            }
            //httpPostClient(String osoite, Map tiedot)
   //         System.out.println("arvot mapin sisältä " + ": " + arvot);
   //         System.out.println("osoite " + ": " + osoite);
            //Content to http client
    //        paluu = yhteys.httpClientAut2(arvot, osoite);
    */
            /*
            if(arvot.containsKey("body"))
            {
                content = (String) arvot.get("body");
            }
            else
            {
                String jsonstring = json.writeJSONauthorization(arvot);
                content = jsonstring;
            }
            */
    /*
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        

        changed();
        return paluu;
    }
    */
     private String uriShortener(String kokouri) 
    {

       if(kokouri.contains("coap://localhost:TODO/Yhteys/")) 
        {
            osoite = kokouri.replaceAll("coap://localhost:TODO/Yhteys/", "");
        }
        
        return osoite;
    }

    /*
     * POSTs a new sub-resource to this resource.
     * The name of the new sub-resource is retrieved from the request
     * payload.
     */
    @Override
    public void handlePOST(Exchange exchange) 
    {
        
        String osoite1 = exchange.toString();
        System.out.println("osoite1 :" +osoite1);

        Map<String, String> arvot = new HashMap<String, String>();
        Request request = exchange.getRequest();
        
        // get request payload as a string
        String payload = request.getPayloadString();
        
        System.out.println("osoite4 :" + request.getURI());
        String kokouri = request.getURI();
        uriShortener(kokouri);

        System.out.println("osoite :" +osoite);
        System.out.println("payload " + ": " + payload);
        
        String paluu = "";
        content = "";

        content = exchange.getRequest().getPayloadString();
        
        
        Response response = new Response(ResponseCode.CREATED);
            
        json json = new json();
        try
        {

            System.out.println("osoite " + ": " + osoite);
            osoite = "http://"+osoite;
            if(content.contains("akadigest"))
            {
                arvot = json.readJSON(content);
      //          arvot = json.readJSON(content);
                arvot = yhteys.httpbtClientAut(arvot, osoite);
                if(arvot.containsKey("body"))
                {
                    paluu = (String) arvot.get("body");
                    response.getOptions().setContentFormat(
                        MediaTypeRegistry.TEXT_HTML);
                }
                else
                {
                    String jsonstring = json.writeJSONauthorization(arvot);
                    paluu = jsonstring;
                    response.getOptions().setContentFormat(
                        MediaTypeRegistry.APPLICATION_JSON);
                }
            }
            else if(content.contains("digest"))
            {
                arvot = json.readJSON(content);
        //        arvot = json.readJSON(content);
                paluu = yhteys.httpClientAut2(arvot, osoite);
                response.getOptions().setContentFormat(
                        MediaTypeRegistry.TEXT_HTML);
            }
            else if(content.contains("UserName"))
            {
                arvot = json.readJSON(content);
            //    arvot = yhteys.httpClientReq(osoite);
                arvot = yhteys.httpbtClientAut(arvot, osoite);
                if(arvot.containsKey("body"))
                {
                    //createJsonString(
                    paluu = arvot.get("body");
                    response.getOptions().setContentFormat(
                        MediaTypeRegistry.TEXT_HTML);
            //        jsonstring = json.createJsonString(arvot);
                }
                else
                {

                    paluu = json.writeJSONauthorization(arvot);
                    response.getOptions().setContentFormat(
                        MediaTypeRegistry.APPLICATION_JSON);
                }
            }
            //!digest.equals("")
            else if(!content.equals(""))
            {
     //           arvot = json.readJSON(content);
                paluu = yhteys.httpPostClient(osoite, content);
            }
            else
            {
                arvot = yhteys.httpClientReq(osoite);
                if(arvot.containsKey("body"))
                {
                    //createJsonString(
                    paluu = arvot.get("body");
                    response.getOptions().setContentFormat(
                        MediaTypeRegistry.TEXT_HTML);
            //        jsonstring = json.createJsonString(arvot);
                }
                else
                {

                    paluu = json.writeJSONauthorization(arvot);
                    response.getOptions().setContentFormat(
                        MediaTypeRegistry.APPLICATION_JSON);
                }
            }

            content = paluu;

     //       String jsonstring = "";
/*
            if(arvot.containsKey("body"))
            {
                //createJsonString(
                jsonstring = arvot.get("body");
        //        jsonstring = json.createJsonString(arvot);
            }
            else
            {
                
                jsonstring = json.writeJSONauthorization(arvot);
            }
*/
   //         content = jsonstring;

            changed();
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }


        
        response.setPayload(content);
        exchange.respond(response);



    }

   
    /*
     * DELETEs this storage resource, if it is not root.
     */
    @Override
    public void handleDELETE(Exchange request) 
    {

        this.delete();
	request.respond(new Response(ResponseCode.DELETED));
    }

    	/**
	 * Find the requested child. If the child does not exist yet, create it.
	 */
	@Override
	public Resource getChild(String name) 
        {
            System.out.println("lapsen nimi: " + name);

            Resource resource = super.getChild(name);
            if (resource == null) {
                    resource = new Yhteys(name);
                    add(resource);
            }
            return resource;
	}
	
        

}
