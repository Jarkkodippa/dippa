
/**
 *
 * @author Jarkko
 */
public class startServer 
{
     //Käynnistetään reverseProxy
    public static void main(String[] args) 
    {
  
     coaptoteutus servu = new coaptoteutus();

     try
     {
         servu.runCoapserver();

     }
     catch (Exception e)
     {
         System.err.println("virhe");
     }
        
   }
}
