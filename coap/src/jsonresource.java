
import ch.ethz.inf.vs.californium.coap.POSTRequest;
import ch.ethz.inf.vs.californium.coap.registries.CodeRegistry;
import ch.ethz.inf.vs.californium.coap.registries.MediaTypeRegistry;
import ch.ethz.inf.vs.californium.endpoint.resources.LocalResource;
import java.util.HashMap;
import java.util.Map;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jarkko
 */
public class jsonresource extends LocalResource
{
   
        public jsonresource() 
        {
                super("jsonresource");
                setTitle("purkaa jsonin mapiksi");
                setResourceType("ohjaaja");
        }

        @Override
        public void performPOST(POSTRequest request) 
        {

            json json = new json();
            Map<String, Object> sisalto = new HashMap<String,Object>();
            if (request.getContentType()!=MediaTypeRegistry.TEXT_PLAIN) 
            {
                    request.respond(CodeRegistry.RESP_UNSUPPORTED_MEDIA_TYPE, "Use text/plain");
                    return;
            }
            String paluu = request.getPayloadString();//new StringBuilder(request.getPayloadString()).reverse().toString();

       //     sisalto = json.readJSON(paluu);
            sisalto = json.readJSON(paluu);
            try
            {
                paluu = json.writeJSONauthentication(sisalto);
            }
            catch(Exception e) 
            {

                paluu = "error";
            }
            // complete the request
            request.respond(CodeRegistry.RESP_CONTENT, paluu, MediaTypeRegistry.TEXT_PLAIN);
        }
}
        
