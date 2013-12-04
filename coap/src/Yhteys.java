/*******************************************************************************
 * Copyright (c) 2012, Institute for Pervasive Computing, ETH Zurich.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * 
 * This file is part of the Californium (Cf) CoAP framework.
 ******************************************************************************/

import java.util.logging.Logger;

//import ch.ethz.inf.vs.californium.coap.DELETERequest;
//import ch.ethz.inf.vs.californium.coap.GETRequest;
import ch.ethz.inf.vs.californium.coap.LinkFormat;
import ch.ethz.inf.vs.californium.coap.Option;
//import ch.ethz.inf.vs.californium.coap.POSTRequest;
//import ch.ethz.inf.vs.californium.coap.PUTRequest;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.MediaTypeRegistry;
//import ch.ethz.inf.vs.californium.coap.registries.CodeRegistry;
//import ch.ethz.inf.vs.californium.coap.registries.MediaTypeRegistry;
//import ch.ethz.inf.vs.californium.coap.registries.OptionNumberRegistry;
//import ch.ethz.inf.vs.californium.endpoint.resources.LocalResource;
import ch.ethz.inf.vs.californium.network.Exchange;
import ch.ethz.inf.vs.californium.server.resources.ResourceBase;
import ch.ethz.inf.vs.californium.server.resources.Resource;


import java.util.HashMap;
import java.util.Map;
import java.net.URLDecoder;

import java.util.Arrays;
import java.util.LinkedList;

import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;

/*
 * Yhteys luokka. 
Ottaa vastaan coap postin johonkin HTTP osoitteeseen.
Ottaa yhteyden palauttaa yhteyden otosta saatavan datan.
Putilla voidaan vastata annettuun viestiin.
 * 
 */
public class Yhteys extends ResourceBase// extends LocalResource 
{

    private HTTP yhteys;
    private byte[] data; 
    private String content;
    // Constructors ////////////////////////////////////////////////////////////

    /*
     * Default constructor.
     */
    public Yhteys() 
    {
            this("Yhteys");
    }

    /*
     * Constructs a new storage resource with the given resourceIdentifier.
     */
    public Yhteys(String resourceIdentifier) 
    {
        super(resourceIdentifier);
        getAttributes().setTitle("Luodaan HTTP yhteys!");
     //   setTitle("PUT your data here or POST new resources!");
     //   getAttributes().setResourceType("Storage");
        getAttributes().addResourceType("Storage");
       // isObservable(true);
        yhteys = new HTTP();
  //      json json = new json();
    }

    // REST Operations /////////////////////////////////////////////////////////

    /*
     * GETs the content of this storage resource. 
     * If the content-type of the request is set to application/link-format 
     * or if the resource does not store any data, the contained sub-resources
     * are returned in link format.
     */
    @Override
    public void handleGET(Exchange request) 
    {

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
    }

    /*
     * PUTs content to this resource.
     */
    @Override
    public void handlePUT(Exchange exchange) 
    {

            // store payload
  //          storeData(request);
  //      content = request.getRequest().getPayloadString();
        String paluu = storeAnswer(exchange);

        Response response = new Response(ResponseCode.CHANGED);
        response.setPayload(paluu);
        respond(exchange, response);
            // complete the request
    //    request.respond(CodeRegistry.RESP_CHANGED);
  //      request.respond(CodeRegistry.RESP_CONTENT, paluu, 
    //            MediaTypeRegistry.TEXT_PLAIN);
 //       request.respond();
    }

    /*
     * POSTs a new sub-resource to this resource.
     * The name of the new sub-resource is retrieved from the request
     * payload.
     */
    @Override
    public void handlePOST(Exchange exchange) 
    {
        /*
        String osoite1 = request.getLocationPath();
        System.out.println("osoiteloc " + ": " + osoite1);
        osoite1 = request.getLocationQuery();
        System.out.println("osoitelque " + ": " + osoite1);
        osoite1 = request.getUriHost();
        System.out.println("osoitehost " + ": " + osoite1);
        osoite1 = request.getUriPath();
        System.out.println("osoitepath " + ": " + osoite1);
        osoite1 = request.getUriQuery();
        System.out.println("osoiteuriquer " + ": " + osoite1);
*/
        Request request = exchange.getRequest();
        Map<String, String> arvot = new HashMap<String, String>();
        // get request payload as a string
        String payload = request.getPayloadString();

        System.out.println("payload " + ": " + payload);
/*        String[] parts = payload.split("\\?");
            System.out.println("payload eka splitti" + ": " + parts);
            String[] path = parts[0].split("/");
            System.out.println("payload toka splitti " + ": " + path);
        */
        // check if valid Uri-Path specified
        if (payload != null && !payload.isEmpty()) 
        {
  //          HTTP http = new HTTP();
            
            json json = new json();
            try
            {
                String osoite = URLDecoder.decode(payload, "UTF-8");
                osoite = "http://"+payload;
                arvot = yhteys.httpClientReq(osoite);

                String jsonstring = json.createJsonString(arvot);
            // set payload and content type
                content = jsonstring;
                data = jsonstring.getBytes("UTF-8");
                // signal that resource state changed
                changed();
            }
            catch (Exception e) 
            {
                e.printStackTrace();
            }
            
   //         Resource resource = create(new LinkedList<String>(Arrays.asList(path)));
    //        Resource resource = create(new LinkedList<String>(Arrays.asList(payload)));
  //          Resource resource = create1(payload);
   //         createSubResource(request, payload);

            Response response = new Response(ResponseCode.CREATED);
  //          response.getOptions().setLocationPath(resource.getURI());
            exchange.respond(response);


        } 
        else 
        {

                // complete the request
            Response response = new Response(ResponseCode.NOT_ACCEPTABLE);
   //         response.getOptions().setLocationPath(resource.getURI());
            
            response.setPayload("Payload must contain Uri-Path for http address.");
            exchange.respond(response);
      //      request.respond(CodeRegistry.RESP_BAD_REQUEST,
        //                "Payload must contain Uri-Path for http address.");
        }
    }

    
    /*
     * Creates a new sub-resource with the given identifier in this resource.
     * Added checks for resource creation.
     */
    /*
    @Override
    public void createSubResource(Exchange request, String newIdentifier) 
    {

            if (request instanceof PUTRequest) 
            {
                    request.respond(CodeRegistry.RESP_FORBIDDEN, 
                            "PUT restricted to exiting resources");
                    return;
            }

            String osoite = request.getLocationPath();
            System.out.println("osoiteloc " + ": " + osoite);
            osoite = request.getLocationQuery();
            System.out.println("osoitelque " + ": " + osoite);
            osoite = request.getUriHost();
            System.out.println("osoitehost " + ": " + osoite);
            osoite = request.getUriPath();
            System.out.println("osoitepath " + ": " + osoite);
            osoite = request.getUriQuery();
            System.out.println("osoiteuriquer " + ": " + osoite);
*/

            // omit leading and trailing slashes
            /*
            if (newIdentifier.startsWith("/")) 
            {
                    newIdentifier = newIdentifier.substring(1);
            }
            if (newIdentifier.endsWith("/")) 
            {
                    newIdentifier = newIdentifier.substring(0, newIdentifier.length()-1);
            }*/
            /*
            if (newIdentifier.startsWith("\"")) 
            {
                    newIdentifier = newIdentifier.substring(1);
            }
            if (newIdentifier.endsWith("\"")) 
            {
                    newIdentifier = newIdentifier.substring(0, newIdentifier.length()-1);
            }
            */
            // truncate from special chars onwards 
            /*
            if (newIdentifier.indexOf("/")!=-1) 
            {
                    newIdentifier = newIdentifier.substring(0,newIdentifier.indexOf("/"));
            }
            if (newIdentifier.indexOf("?")!=-1) 
            {
                    newIdentifier = newIdentifier.substring(0,newIdentifier.indexOf("?"));
            }
            if (newIdentifier.indexOf("\r")!=-1) 
            {
                    newIdentifier = newIdentifier.substring(0,newIdentifier.indexOf("\r"));
            }
            if (newIdentifier.indexOf("\n")!=-1) 
            {
                    newIdentifier = newIdentifier.substring(0,newIdentifier.indexOf("\n"));
            }
            */
    /*
            // special restriction
            if (newIdentifier.length()>32) 
            {
                    request.respond(CodeRegistry.RESP_FORBIDDEN, 
                            "Resource segments limited to 32 chars");
                    return;
            }

            // rt by query
            String newRtAttribute = null;
            for (Option query : request.getOptions(OptionNumberRegistry.URI_QUERY)) 
            {
                    String keyValue[] = query.getStringValue().split("=");

                    if (keyValue[0].equals("rt") && keyValue.length==2) 
                    {
                            newRtAttribute = keyValue[1];
                            continue;
                    }
            }

            // create new sub-resource
            if (getResource(newIdentifier)==null) 
            {

                    Yhteys resource = new Yhteys(newIdentifier);
                    if (newRtAttribute!=null) 
                    {
                            resource.setResourceType(newRtAttribute);
                    }

                    add(resource);

                    // store payload
        //            resource.storeData(request);

                    // create new response
             //       Response response = new Response(CodeRegistry.RESP_CREATED);

                    // inform client about the location of the new resource
             //       response.setLocationPath(resource.getPath());

                    // complete the request
              //      request.respond(response);
                    
            //        String paluu = storeAnswer(request);

            // complete the request
    //    request.respond(CodeRegistry.RESP_CHANGED);
                    String paluu = "";
                    try
                    {
                        paluu = new String(data, "UTF-8");
                    }
                    catch (Exception e) 
                    {
                        e.printStackTrace();
                    }
                    request.respond(CodeRegistry.RESP_CONTENT, paluu, MediaTypeRegistry.TEXT_PLAIN);

            } 
            else 
            {
                    // defensive programming if someone incorrectly calls createSubResource()
                    request.respond(CodeRegistry.RESP_INTERNAL_SERVER_ERROR, 
                            "Trying to create existing resource");
                    Logger.getAnonymousLogger().severe(String.format("Cannot create sub resource: %s/[%s] already exists", 
                            this.getPath(), newIdentifier));
            }
    }
*/
    /*
     * DELETEs this storage resource, if it is not root.
     */
    @Override
    public void handleDELETE(Exchange request) 
    {

        this.delete();
	request.respond(new Response(ResponseCode.DELETED));
    }

    // Internal ////////////////////////////////////////////////////////////////

    
    /*
     * Convenience function to store data contained in a 
     * PUT/POST-Request. Notifies observing endpoints about
     * the change of its contents.
     */
    /*
    private void storeData(Exchange request) 
    {

            // set payload and content type
            data = request.getPayload();
            //httpClientAut(Map tiedot)
            clearAttribute(LinkFormat.CONTENT_TYPE);
            setContentTypeCode(request.getContentType());

            // signal that resource state changed
            changed();
    }
*/
    private String storeAnswer(Exchange request) 
    {

       // osoite = "http://"+payload;
        Map<String, Object> arvot = new HashMap<String,Object>();
//         HTTP http = new HTTP();
        json json = new json();
        // set payload and content type
        content = request.getRequest().getPayloadString();
     //   data = request.getPayload();
     //   String str = "";
        String paluu = "";
        try
        {
   //         str = new String(data, "UTF-8");
        //    arvot = json.readJSON(str);
            arvot = json.readJSON(content);
            paluu = yhteys.httpClientAut(arvot);
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        
  //      clearAttribute(LinkFormat.CONTENT_TYPE);
  //      setContentTypeCode(request.getContentType());

        // signal that resource state changed
        changed();
        return paluu;
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
	
	/**
	 * Create a resource hierarchy with according to the specified path.
	 * @param path the path
	 * @return the lowest resource from the hierarchy
	 */
	private Resource create(LinkedList<String> path) 
        {
		String segment;
		do {
			if (path.size() == 0)
				return this;
		
			segment = path.removeFirst();
		} while (segment.isEmpty() || segment.equals("/"));
		
		Yhteys resource = new Yhteys(segment);
   //             Yhteys resource = new Yhteys(segment);
		add(resource);
		return resource.create(path);
	}
        
        /**
	 * Create a resource hierarchy with according to the specified path.
	 * @param path the path
	 * @return the lowest resource from the hierarchy
	 */
	private Resource create1(String segment) 
        {
//		String segment;
  //              segment = path.;
	/*	do {
			if (path.size() == 0)
				return this;
		
			segment = path.removeFirst();
		} while (segment.isEmpty() || segment.equals("/"));
		*/
		Yhteys resource = new Yhteys(segment);
   //             Yhteys resource = new Yhteys(segment);
		add(resource);
      //          new LinkedList<String>(Arrays.asList(segment));
        //        resource.
                return resource;
//		return resource.create(new LinkedList<String>(Arrays.asList(segment)));
	}
	
}
