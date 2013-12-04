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
     //jee
    public static void main(String[] args) 
    {
        /*
     args = new String[3];
     String url = "jee";
     String viesti = "testi";
                */
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
