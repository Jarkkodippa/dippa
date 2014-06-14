
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import java.util.HashMap;
import java.util.Map;

/**
 *HTTP luokka. Tekee HTTP pyynnön ja vastaanottaa siihen liittyvät arvot.
 * @author Jarkko
 */
public class HTTPClass 
{
    static int counter = 0;
    
    // Vakiot.
    final String VIESTI_KEHOTE = "Kirjoita komento:";
    final String VIESTI_JAAHYVAISET = "Ohjelma lopetettu.";

    
    private String urlStr = "http://example.com:8080/abc/";

    private HttpClient client;
    private int status;
    private GetMethod getMethod;
    private PostMethod postMethod;
    private String responseBody;

   /** Herja
     */
   private final String VIESTI_HERJA = "Virhe!";
   
   
   public HTTPClass() 
   {

   }
   
   //Ottaa vastaan yhteysosoitteen ja map rakenteen.
   //Tekee edellisten pohjalta POST pyynnön palvelimelle.
   //Palauttaa saasun vastauksen String merkkijonona.
    public String httpPostClient(String osoite, Map tiedot) throws Exception
    {
    
	    Map<String, Object> heatribute = new HashMap<String, Object>();
	    return httpPostClient(osoite, tiedot, heatribute);
    }
    

    //Ottaa vastaan yhteysosoitteen ja map rakenteen sisällölle ja 
    //map rakentee Header parametreille.
   //Tekee edellisten pohjalta POST pyynnön palvelimelle.
   //Palauttaa saasun vastauksen String merkkijonona.
    public String httpPostClient(String osoite, Map tiedot, Map heatribute) throws Exception
    {
        Map<String, Object> tietoja = new HashMap<String, Object>();
            
        tietoja = tiedot;
        
        Map<String, Object> heatribute1 = new HashMap<String, Object>();
            
        heatribute1 = heatribute;
        
        responseBody = "";
        
        try 
        {

            client = new HttpClient();

            urlStr = osoite;
            System.out.println("osoite: " + osoite);
            System.out.println("osoite2: " + urlStr);

            postMethod = new PostMethod(urlStr);
            if(heatribute1 != null && !heatribute1.isEmpty())
            {

                for (Map.Entry<String, Object> entry : heatribute1.entrySet())
                {
                    postMethod.setRequestHeader(entry.getKey(), (String)entry.getValue());
           
                }

            }

            if(!tiedot.isEmpty())
            {
                for (Map.Entry<String, Object> entry : tietoja.entrySet())
                {
                     postMethod.addParameter(entry.getKey(), (String)entry.getValue());
                    
                }
            }
            System.out.println("getMethod luotu");

            status = client.executeMethod(postMethod);
            System.out.println("status: " + status);
            responseBody = postMethod.getResponseBodyAsString();
            System.out.println("responseBody: " + responseBody);

           
            
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        return responseBody;
    }
    
    //Ottaa vastaan yhteysosoitteen ja String jonon sisällölle.
   //Tekee edellisten pohjalta POST pyynnön palvelimelle.
   //Palauttaa saasun vastauksen String merkkijonona.
    public String httpPostClient(String osoite, String sisalto) throws Exception
    {
    
	    Map<String, Object> heatribute = new HashMap<String, Object>();
	    return httpPostClient(osoite, sisalto, heatribute);
    }
    
    //Ottaa vastaan yhteysosoitteen ja String jonon sisällölle ja 
    //map rakentee Header parametreille.
   //Tekee edellisten pohjalta POST pyynnön palvelimelle.
   //Palauttaa saasun vastauksen String merkkijonona.
    public String httpPostClient(String osoite, String sisalto, Map heatribute) throws Exception
    {
        Map<String, Object> tietoja = new HashMap<String, Object>();
            
        Map<String, Object> heatribute1 = new HashMap<String, Object>();
            
        heatribute1 = heatribute;
        
        responseBody = "";
        
        try 
        {

            client = new HttpClient();

            urlStr = osoite;
            System.out.println("osoite: " + osoite);
            System.out.println("osoite2: " + urlStr);

            postMethod = new PostMethod(urlStr);
            if(heatribute1 != null && !heatribute1.isEmpty())
            {

                for (Map.Entry<String, Object> entry : heatribute1.entrySet())
                {
                    postMethod.setRequestHeader(entry.getKey(), (String)entry.getValue());
           
                }

            }

            postMethod.setRequestBody(sisalto);

            System.out.println("getMethod luotu");

            status = client.executeMethod(postMethod);
            System.out.println("status: " + status);
            responseBody = postMethod.getResponseBodyAsString();
            System.out.println("responseBody: " + responseBody);

           
            
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        return responseBody;
    }
    
    //Tekee http Get pyynnön palvelimelle.
    //Ottaa vastaa yhteysosoittee 
    //Palauttaa vastauksena saamansa sisällön Map rakenteena.
    public Map httpClientReq(String osoite) throws Exception
    {
        HashMap<String, Object> map = new HashMap<String, Object>();
        HashMap<String, String> mapdi = new HashMap<String, String>();
        HashMap<String, String> mapco = new HashMap<String, String>();

        try 
        {

            client = new HttpClient();

            urlStr = osoite;
            System.out.println("osoite: " + osoite);
            System.out.println("osoite2: " + urlStr);


            getMethod = new GetMethod(urlStr);
            
            System.out.println("getMethod luotu");

            status = client.executeMethod(getMethod);
            System.out.println("status: " + status);
            responseBody = getMethod.getResponseBodyAsString();
            System.out.println("responseBody: " + responseBody);

            Header wwAuthHeader = getMethod.getResponseHeader("WWW-Authenticate");
            Header headersession = getMethod.getResponseHeader("Set-Cookie");
            if(wwAuthHeader != null)
            {
                for (HeaderElement element : wwAuthHeader.getElements()) 
                {
                    if(element.getName().contains("Digest")) 
                    {
                        String elementname = element.getName();
                        
                        elementname = elementname.replaceAll("Digest ", "");
                        element.setName(elementname);
                    }
                    System.out.println(element.getName() + ": " + element.getValue());
                    mapdi.put( element.getName(), element.getValue() ); 
                }
                map.put("WWW-Authenticate", mapdi);
            }
            
            if(headersession != null)
            {
                for (HeaderElement element : headersession.getElements())
                {
                    
                    System.out.println(element.getName() + ": " + element.getValue());
                    mapco.put( element.getName(), element.getValue() );
                }
                map.put("Set-Cookie", mapco);
            }

            if(responseBody != null || responseBody != "")
            {
                map.put("Body", responseBody);
            }

        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        return map;
    }
    
    //Tekee http Get pyynnön palvelimelle.
    //Ottaa vastaa yhteysosoitteen otsikon Header osan UserName attribuuttiin arvon.  
    //Palauttaa vastauksena saamansa sisällön Map rakenteena.
    public Map httpClientReq(String osoite, String otsikko) throws Exception
    {
        HashMap<String, Object> map = new HashMap<String, Object>();
        HashMap<String, String> mapdi = new HashMap<String, String>();
        HashMap<String, String> mapco = new HashMap<String, String>();

        try 
        {

            client = new HttpClient();

            urlStr = osoite;
            System.out.println("osoite: " + osoite);
            System.out.println("osoite2: " + urlStr);


            getMethod = new GetMethod(urlStr);
            
            System.out.println("getMethod luotu");
            
            getMethod.setRequestHeader("UserName", otsikko);

            status = client.executeMethod(getMethod);
            System.out.println("status: " + status);
            responseBody = getMethod.getResponseBodyAsString();
            System.out.println("responseBody: " + responseBody);

            Header wwAuthHeader = getMethod.getResponseHeader("WWW-Authenticate");
            Header headersession = getMethod.getResponseHeader("Set-Cookie");
            if(wwAuthHeader != null)
            {
                for (HeaderElement element : wwAuthHeader.getElements()) 
                {
                    if(element.getName().contains("Digest")) 
                    {
                        String elementname = element.getName();
                        
                        elementname = elementname.replaceAll("Digest ", "");
                        element.setName(elementname);
                    }
                    System.out.println(element.getName() + ": " + element.getValue());
                    mapdi.put( element.getName(), element.getValue() ); 
                }
                map.put("WWW-Authenticate", mapdi);
            }
            
            if(headersession != null)
            {
                for (HeaderElement element : headersession.getElements())
                {
                    
                    System.out.println(element.getName() + ": " + element.getValue());
                    mapco.put( element.getName(), element.getValue() );
                }
                map.put("Set-Cookie", mapco);
            }

            if(responseBody != null || responseBody != "")
            {
                map.put("Body", responseBody);
            }

        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        return map;
    }
    
  
    //Lähettää http Authorizationin Get pyynnön.
    //Ottaaa vastaan Authorization tiedot sisältävän MAP-rakenteen ja
    //yhteys osoitteen.
    //Palauttaa palvelimelta saadun vastauksen String muodossa.
    public String httpClientAut2(Map tiedot, String osoite) throws Exception
    {
        client = new HttpClient();
        urlStr = osoite;
        
        getMethod = new GetMethod(urlStr);
        String otsikkotieto = setReguestHeaderf(tiedot);
        String otsikkotietoco = "";
        otsikkotietoco = setReguestHeadercookie(tiedot);

        try 
        {
     
            getMethod.setRequestHeader("Authorization", otsikkotieto);
            
            if(!otsikkotietoco.equals(""))
            {
                getMethod.setRequestHeader("Cookie", otsikkotietoco);
            }
            System.out.println("getmethod sisältää" + ": " + getMethod.getQueryString());
            

            status = client.executeMethod(getMethod);
            System.out.println("status: " + status);
            responseBody = getMethod.getResponseBodyAsString();
            System.out.println("responseBody: " + responseBody);
            
            

            getMethod.releaseConnection();

        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        return responseBody;

    }
    
    //Lähettää http Authorizationin Get pyynnön.
    //Ottaaa vastaan Authorization, Cookie ja Username tiedot 
    //sisältävän MAP-rakenteen ja yhteys osoitteen.
    //Palauttaa palvelimelta saadun vastauksen MAP-rakenteena.
    public Map httpbtClientAut(Map tiedot, String osoite) throws Exception
    {
        HashMap<String, Object> map = new HashMap<String, Object>();
        HashMap<String, String> mapdi = new HashMap<String, String>();
        HashMap<String, String> mapco = new HashMap<String, String>();
        client = new HttpClient();

        urlStr = osoite;
        
        System.out.println("osoite : "+ urlStr);
        
        getMethod = new GetMethod(urlStr);
        String otsikkotieto = setReguestHeaderf(tiedot);

        String otsikkotietoco = "";
        otsikkotietoco = setReguestHeadercookie(tiedot);
        
        String otsikkotietouser = "";
        otsikkotietouser = setReguestHeaderusername(tiedot);

        try 
        {
            if(otsikkotieto != null && !otsikkotieto.equals(""))
            {
                getMethod.setRequestHeader("Authorization", otsikkotieto);
            }
            if(otsikkotietoco != null && !otsikkotietoco.equals(""))
            {
                getMethod.setRequestHeader("Cookie", otsikkotietoco);
            }
            if(otsikkotietouser != null && !otsikkotietouser.equals(""))
            {
                getMethod.setRequestHeader("UserName", otsikkotietouser);
            }
            
            status = client.executeMethod(getMethod);
            System.out.println("status: " + status);
            responseBody = getMethod.getResponseBodyAsString();
            System.out.println("responseBody: " + responseBody);
            
            Header wwAuthHeader = getMethod.getResponseHeader("WWW-Authenticate");
            Header headersession = getMethod.getResponseHeader("Set-Cookie");
            if(wwAuthHeader != null)
            {
                for (HeaderElement element : wwAuthHeader.getElements()) 
                {
                    if(element.getName().contains("Digest")) 
                    {
                        String elementname = element.getName();
                        
                        elementname = elementname.replaceAll("Digest ", "");
                        element.setName(elementname);
                    }
                    System.out.println(element.getName() + ": " + element.getValue());
                    mapdi.put( element.getName(), element.getValue() ); 
                }
                map.put("WWW-Authenticate", mapdi);
            }
            
            if(headersession != null)
            {
                for (HeaderElement element : headersession.getElements())
                {
                    
                    System.out.println(element.getName() + ": " + element.getValue());
                    mapco.put( element.getName(), element.getValue() );
                }
                map.put("Set-Cookie", mapco);
            }
            

            if(responseBody != null || responseBody != "")
            {
                map.put("Body", responseBody);
            }

            getMethod.releaseConnection();

        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        return map;

    }
    
      //Käynnistää HTTP yhteyden annettuun osoitteeseen.
    //Palauttaa palvelimelta saadun vastauksen String merkkijonona.
    public String httpconnection(String osoite) throws Exception
    {
        client = new HttpClient();

        urlStr = osoite;
        
        getMethod = new GetMethod(urlStr);

        try 
        {

            status = client.executeMethod(getMethod);
            System.out.println("status: " + status);
            responseBody = getMethod.getResponseBodyAsString();
            System.out.println("responseBody: " + responseBody);

            getMethod.releaseConnection();

        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        return responseBody;

    }
    
     //Tekee header tiedosto Map rakenteessa saamista digest arvoista.
    //Palauttaa String tiedostona Header rakenteen.
    private String setReguestHeaderf(Map tiedot)
    {
        String header = "";

        String username1 = "";
        String realm1 = "";

        String nonce = "";
        String uri = "";
        String qop = "";
        String nc = "";
        String cnonce = "";
        String response = "";
        String opaque = "";
        String algorithm = "";
        String digest = "";
        String integrityprotected = "";
        String auts = "";

        Map<String, Object> tietoja = new HashMap<String, Object>();
        Map<String, Object> tietoja2 = new HashMap<String, Object>();
        Map<String, Object> tietoja3 = new HashMap<String, Object>();
        tietoja = tiedot;
        System.out.println("tietoja mapin sisälto" + ": " + tietoja);
        tietoja2 = (HashMap<String,Object>)tietoja.get("authorization");
        System.out.println("tietoja2 mapin sisälto" + ": " + tietoja2);



        if(tietoja2 != null && tietoja2.get("type").equals("akadigest") )
        {
            System.out.println("jee");
            header = setReguestHeaderbt(tietoja);
        }
        else if(tietoja2 != null && tietoja2.get("type").equals("digest"))
        {
            tietoja3 = (HashMap<String,Object>)tietoja2.get("digest-response");
            System.out.println("tietoja3 mapin sisälto" + ": " + tietoja3);


            if(tietoja3 != null  && !tietoja3.isEmpty())
            {


                username1 = (String) tietoja3.get("Digest username");
                realm1 = (String) tietoja3.get("realm");
                nonce = (String) tietoja3.get("nonce");
                uri = (String) tietoja3.get("uri");
                qop = (String) tietoja3.get("qop");
                nc = (String) tietoja3.get("nc");
                cnonce = (String) tietoja3.get("cnonce");
                response = (String) tietoja3.get("response");
                opaque = (String) tietoja3.get("opaque");
                algorithm = (String) tietoja3.get("algorithm");
                digest = (String) tietoja3.get("digest");
                integrityprotected = (String) tietoja3.get("integrity-protected");
                auts = (String) tietoja3.get("auts");
            }


            header += "Digest username=\"" + username1 + "\", ";
            header += "realm=\"" + realm1 + "\", ";

            header += "nonce=\"" + nonce + "\", ";
            header += "uri=\"" + uri + "\", ";

            if(digest != null && !digest.equals(""))
            {
                header += "digest=" + algorithm + ", ";
            }
            if(algorithm != null && !algorithm.equals(""))
            {
                header += "algorithm=" + algorithm + ", ";
            }
            header += "response=\"" + response + "\", ";
            if(integrityprotected != null && !integrityprotected.equals(""))
            {
                header += "integrity-protected=\"" + integrityprotected + "\", ";
            }
            if(qop != null && !qop.equals(""))
            {
                header += "qop=" + qop + ", ";
            }
            if(nc != null && !nc.equals(""))
            {
                header += "nc=" + nc + ", ";
            }
            if(cnonce != null && !cnonce.equals(""))
            {
                header += "cnonce=\"" + cnonce + "\", ";
            }

            if(opaque != null && !opaque.equals(""))
            {
                header += "opaque=\"" + opaque + "\", ";
            }
            if(auts != null && !auts.equals(""))
            {
                header += "auts=\"" + auts + "\", ";
            }


            header = removeLastChar(header);
            header = removeLastChar(header);
        }

        System.out.println("header tiedosto" + ": " + header);

        return header;
    }
        
    //Tekee AKA-Gigest Header osion MAP raketeessa saamista arvoista.
    //Palauttaa rakenteen String merkkijonona
    private String setReguestHeaderbt(Map tiedot)
    {
        String header = "";

        String username1 = "";
        String realm1 = "";

        String nonce = "";
        String uri = "";
        String qop = "";
        String nc = "";
        String cnonce = "";
        String response = "";
        String opaque = "";
        String algorithm = "";
        String digest = "";
        String integrityprotected = "";
        String auts = "";

        Map<String, Object> tietoja = new HashMap<String, Object>();
        Map<String, Object> tietoja2 = new HashMap<String, Object>();
        Map<String, Object> tietoja3 = new HashMap<String, Object>();
        tietoja = tiedot;
        System.out.println("tietoja mapin sisälto" + ": " + tietoja);
        tietoja2 = (HashMap<String,Object>)tietoja.get("authorization");
        System.out.println("tietoja2 mapin sisälto" + ": " + tietoja2);
        tietoja3 = (HashMap<String,Object>)tietoja2.get("digest-response");
        System.out.println("tietoja3 mapin sisälto" + ": " + tietoja3);


        if(tietoja3 != null  && !tietoja3.isEmpty())
        {


            username1 = (String) tietoja3.get("Digest username");
            realm1 = (String) tietoja3.get("realm");
            nonce = (String) tietoja3.get("nonce");
            uri = (String) tietoja3.get("uri");
            qop = (String) tietoja3.get("qop");
            nc = (String) tietoja3.get("nc");
            cnonce = (String) tietoja3.get("cnonce");
            response = (String) tietoja3.get("response");
            opaque = (String) tietoja3.get("opaque");
            algorithm = (String) tietoja3.get("algorithm");
            digest = (String) tietoja3.get("digest");
            integrityprotected = (String) tietoja3.get("integrity-protected");
            auts = (String) tietoja3.get("auts");
        }

        header += "Digest username=\"" + username1 + "\",";
        header += "realm=\"" + realm1 + "\",";

        header += "nonce=\"" + nonce + "\",";
        header += "uri=\"" + uri + "\",";

        if(digest != null && !digest.equals(""))
        {
            header += "digest=" + algorithm + ",";
        }
        if(qop != null && !qop.equals(""))
        {
            header += "qop=\"" + qop + "\",";
        }


        if(integrityprotected != null && !integrityprotected.equals(""))
        {
            header += "integrity-protected=\"" + integrityprotected + "\",";
        }
        if(nc != null && !nc.equals(""))
        {
            header += "nc=" + nc + ",";
        }
        if(cnonce != null && !cnonce.equals(""))
        {
            header += "cnonce=\"" + cnonce + "\",";
        }
        header += "response=\"" + response + "\",";

        if(opaque != null && !opaque.equals(""))
        {
            header += "opaque=\"" + opaque + "\",";
        }
        if(algorithm != null && !algorithm.equals(""))
        {
            header += "algorithm=\"" + algorithm + "\",";
        }
        if(auts != null && !auts.equals(""))
        {
            header += "auts=\"" + auts + "\",";
        }

        header = removeLastChar(header);

        System.out.println("header tiedosto" + ": " + header);

        return header;
    }
    

    //Tekee annetuista map attribuuteista Header String tiedoston.
    //Palauttaa tiedoston String merkkijonona.
    private String setReguestHeadercookie(Map tiedot)
    {

        String header = "";

        Map<String, Object> tietoja = new HashMap<String, Object>();
        Map<String, Object> tietoja2 = new HashMap<String, Object>();
        tietoja = tiedot;
        System.out.println("tietoja mapin sisälto" + ": " + tietoja);
        tietoja2 = (HashMap<String,Object>)tietoja.get("Cookie");
        System.out.println("tietoja2 mapin sisälto" + ": " + tietoja2);

        if(tietoja2 != null && !tietoja2.isEmpty())
        {

            for (Map.Entry<String, Object> entry : tietoja2.entrySet())
            {
                    header += entry.getKey() +"=" + entry.getValue();

            }

        }



        System.out.println("header tiedosto" + ": " + header);

        return header;
    }
        
    //Lukee annetun Map rakenteen ja 
    //palauttaa siitä UserName kantän arvon String tiedostona.
    private String setReguestHeaderusername(Map tiedot)
    {

        String header = "";

        Map<String, Object> tietoja = new HashMap<String, Object>();
        tietoja = tiedot;
        System.out.println("tietoja mapin sisälto" + ": " + tietoja);

        if(tietoja != null && !tietoja.isEmpty())
        {
            header = (String)tietoja.get("UserName");
        }

        System.out.println("header tiedosto" + ": " + header);

        return header;
    }

    //Otaa vastaan String merkkijonon.
    //Poistaa siitä viimeisen merkin.
    //Palauttaa syntyneen merkkijonon.
    public String removeLastChar(String s) 
    {
        if (s == null || s.length() == 0) 
        {
            return s;
        }
        return s.substring(0, s.length()-1);
    }


    
    //Pääsilmukka metodien testaamiseen ja ajamiseen jossain järjestyksessä.
    public void suoritaPaasilmukkaa() 
   {

        try
        {
            //http client

            Map<String, Object> tietoja = new HashMap<String, Object>();
      
            httpconnection("http://www.iltalehti.fi");
            System.out.println("jee" + urlStr);

        }
        catch (Exception e)
        {
           System.out.println(VIESTI_HERJA);
        }


      // Lopuksi lyhyet j��hyv�iset.
      System.out.println(VIESTI_JAAHYVAISET);
   }
    
 
}
