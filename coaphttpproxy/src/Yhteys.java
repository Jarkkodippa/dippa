
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.Exchange;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;
import ch.ethz.inf.vs.californium.server.resources.Resource;


import java.util.HashMap;
import java.util.Map;
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
     * Take coap Get reguest and make http Get reguest.
     */
    
    @Override
    public void handleGET(Exchange exchange) 
    {
        Map<String, String> arvot = new HashMap<String, String>();
        Request request = exchange.getRequest();

        String kokouri = request.getURI();
        uriShortener(kokouri);

        System.out.println("osoite :" +osoite);
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
                jsonstring = arvot.get("Body");
                response.getOptions().setContentFormat(
                        MediaTypeRegistry.TEXT_HTML);
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

        response.setPayload(content);
        exchange.respond(response);


    }

    /*
     * Take coap PUT reguest and make http PUT reguest.
     */
    @Override
    public void handlePUT(Exchange exchange) 
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
        Response response = new Response(ResponseCode.CHANGED);
        try
        {
 
            osoite = "http://"+osoite;
            //Json content to map
            arvot = json.readJSON(content);
            
            if(content.contains("akadigest"))
            {
                arvot = yhteys.httpbtClientAut(arvot, osoite);
                
                 System.out.println("arvot " + ": " + arvot);
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
                paluu = yhteys.httpClientAut2(arvot, osoite);
                response.getOptions().setContentFormat(
                        MediaTypeRegistry.TEXT_HTML);
            }
            else
            {
                yhteys.httpPostClient(osoite, arvot);
            }
    
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        

        changed();

        response.setPayload(paluu);
        respond(exchange, response);

    }
   
    //Clean Coap server uri information to given uri.
     private String uriShortener(String kokouri) 
    {

       if(kokouri.contains("coap://localhost:TODO/Yhteys/")) 
        {
            osoite = kokouri.replaceAll("coap://localhost:TODO/Yhteys/", "");
        }
        
        return osoite;
    }

    /*
     * Take coap POST reguest and make http POST reguest.
     */
    @Override
    public void handlePOST(Exchange exchange) 
    {
        Map<String, String> arvot = new HashMap<String, String>();
        Request request = exchange.getRequest();
        
        // get request payload as a string
        String payload = request.getPayloadString();
        
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
            osoite = "http://"+osoite;
            if(content.contains("akadigest"))
            {
                arvot = json.readJSON(content);
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
                paluu = yhteys.httpClientAut2(arvot, osoite);
                response.getOptions().setContentFormat(
                        MediaTypeRegistry.TEXT_HTML);
            }
            else if(content.contains("UserName"))
            {
                arvot = json.readJSON(content);
                arvot = yhteys.httpbtClientAut(arvot, osoite);
                if(arvot.containsKey("body"))
                {
                    paluu = arvot.get("body");
                    response.getOptions().setContentFormat(
                        MediaTypeRegistry.TEXT_HTML);
                }
                else
                {

                    paluu = json.writeJSONauthorization(arvot);
                    response.getOptions().setContentFormat(
                        MediaTypeRegistry.APPLICATION_JSON);
                }
            }
            else if(!content.equals(""))
            {
                paluu = yhteys.httpPostClient(osoite, content);
            }
            else
            {
                arvot = yhteys.httpClientReq(osoite);
                if(arvot.containsKey("body"))
                {
                    paluu = arvot.get("body");
                    response.getOptions().setContentFormat(
                        MediaTypeRegistry.TEXT_HTML);
                }
                else
                {

                    paluu = json.writeJSONauthorization(arvot);
                    response.getOptions().setContentFormat(
                        MediaTypeRegistry.APPLICATION_JSON);
                }
            }

            content = paluu;


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
     * DELETEs this storage resource.
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
        Resource resource = super.getChild(name);
        if (resource == null) {
                resource = new Yhteys(name);
                add(resource);
        }
        return resource;
    }
	
        

}
