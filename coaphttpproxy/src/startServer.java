/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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
