
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Jarkko Virtanen
 */
public class Starthttpserverr
{
   public static void main(String[] args)  throws Exception 
   {
 //     Serverhttpjarkko server = new Serverhttpjarkko();
        HttpServer server;
       /* 
        try 
        {
        */
        System.out.println("servu käynnistetty");
        server = HttpServer.create(new InetSocketAddress(8002), 0);
 //       server.createContext("/test", new MyHandler());
        server.createContext("/test", new Serverhttpjarkko());
//        server.setExecutor(null); // creates a default executor

        //      HTTP.httpPostClient("http://p133.piuha.net:8080/bsf/requestBootstrappingInfo", tiedot);
        server.start();
        System.out.println("servu käynnistetty osoitteessa http://localhost:8002/test");
    //            return true;
            /*
        } 
            
        catch (IOException ex) 
        {
            System.err.println("virhe");
    //          return false;
        }
             */      
   //   server.server();
   }
   
   static class MyHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String response = "This is the response";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}