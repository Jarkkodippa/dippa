
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Scanner;


/**
 *
 * @author Jarkko Virtanen
 */
public class Starthttpserverr
{
   public static void main(String[] args)  throws Exception 
   {
        HttpServer server;
      
        System.out.println("servu käynnistetty");
        String inputString = "";
        server = HttpServer.create(new InetSocketAddress(8002), 0);
        Scanner input = new Scanner(System.in);
        System.out.print("Oletuksena käynnistyy salaus, jos allekirjoitus, kirjoita \"alle\": ");
        inputString = input.nextLine();
        if(inputString.equals(""))
        {
            server.createContext("/test", new Serverhttpjarkko());
        }
        if(inputString.equals("alle"))
        {
            server.createContext("/test", new Serverhttpjarkkosign());
        }
        
        server.start();
        System.out.println("servu käynnistetty osoitteessa http://localhost:8002/test");
    
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
