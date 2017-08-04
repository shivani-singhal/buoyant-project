package com.buoyant;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import java.net.*;
import java.io.*;
import org.json.simple.JSONObject;
 
public class HealthCheckHandler extends AbstractHandler{

    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) 
        throws IOException, ServletException{

        //respond only if the uri is correct  
        if(baseRequest.getRequestURI().equals("/health")){

            JSONObject result = new JSONObject();
            
            //compare response body
            checkURLBody("http://127.0.0.1:9990/admin/ping",
              "linkerd","pong",result);
            checkURLBody("http://127.0.0.1:9991/admin/ping",
              "namerd","pong",result);

            //compare response code
            checkURLCode("http://127.0.0.1:9992/metrics",
              "linkerd-tcp",result);
            checkURLCode("http://127.0.0.1:3000",
              "linkerd-viz",result);
            
            //build response object
            response.setContentType("json");
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
            response.getWriter().println(result);
        }
    }

    private void checkURLBody(String urlName, String label, 
      String resultExpected, JSONObject obj) throws IOException{
            
            URL url = new URL(urlName);
            BufferedReader in = new BufferedReader(
            new InputStreamReader(url.openStream()));

            String inputLine = in.readLine();
            if(inputLine.equals(resultExpected))
              obj.put(label,"up");
            else
              obj.put(label,"down");
            in.close();
    }

    private void checkURLCode(String urlName, String label, 
      JSONObject obj) throws IOException{
            
            URL url = new URL(urlName);
            HttpURLConnection connection; 
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int code = connection.getResponseCode();
            if(code == 200)
              obj.put(label,"up");
            else
              obj.put(label,"down");
    }
    
}