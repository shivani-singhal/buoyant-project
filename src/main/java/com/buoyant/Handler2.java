package com.buoyant;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import java.net.*;
import java.io.*;
import org.json.simple.JSONObject;
import java.nio.charset.StandardCharsets;
 
public class Handler2 extends HttpServlet{
	private JSONObject resultJson;
    private String dockerIp = "127.0.0.1";;

    protected void doPut(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
    {
    	
		if(request.getRequestURI().indexOf("/shift/")==0){
			int shift = Integer.parseInt(request.getRequestURI().substring(7));

			String urlParameters  = "/svc => /#/io.l5d.fs; /svc/redis => " 
										+(100 - shift)+"*/svc/redis1 & "+shift+"*/svc/redis2;";
			byte[] postData       = urlParameters.getBytes( StandardCharsets.UTF_8 );
			String requestURL        = "http://"+dockerIp+":4180/api/1/dtabs/default";
			URL    url            = new URL( requestURL );
			HttpURLConnection conn= (HttpURLConnection) url.openConnection();           
			conn.setDoOutput( true );
			conn.setInstanceFollowRedirects( false );
			conn.setRequestMethod( "PUT" );
			conn.setRequestProperty( "Content-Type", "application/dtab"); 
			conn.setUseCaches( false );
			DataOutputStream wr = new DataOutputStream( conn.getOutputStream());
			wr.write( postData );
			
			response.setStatus(conn.getResponseCode());
		}
    }





    protected void doGet( HttpServletRequest request,
                              HttpServletResponse response ) throws ServletException,
                                                            IOException
        {
        	
        	if(request.getRequestURI().equals("/health")){

	          Thread t[] = new Thread[4];
	          resultJson = new JSONObject();
	          dockerIp = "127.0.0.1";
	            
	          //compare response body
	          t[0] = checkURLBody("http://"+dockerIp+":9990/admin/ping",
	              "linkerd","pong");
	          t[1] = checkURLBody("http://"+dockerIp+":9991/admin/ping",
	              "namerd","pong");

	          //compare response code
	          t[2] = checkURLCode("http://"+dockerIp+":9992/metrics",
	              "linkerd-tcp");
	          t[3] = checkURLCode("http://"+dockerIp+":3000",
	              "linkerd-viz");

	          //wait for all threads to complete execution
	          try{
	            for(Thread thread : t )
	              thread.join();
	          }catch(InterruptedException e){
	            e.printStackTrace();
	          }

	          //build response object
	          response.setContentType("json");
	          response.setStatus(HttpServletResponse.SC_OK);
	          //baseRequest.setHandled(true);
	          response.getWriter().println(resultJson);
	        }
	    }
	        
        private Thread checkURLBody(String url, String labelT, 
	      String result) throws IOException{
	            
	        final String urlName = url;
	        final String label = labelT;
	        final String resultExpected = result;

	        Thread t1;
	        t1 = new Thread(){
	          public void run(){
	            try{
	              URL url = new URL(urlName);
	              BufferedReader in = new BufferedReader(
	              new InputStreamReader(url.openStream()));

	              String inputLine = in.readLine();

	              synchronized(resultJson){
	                if(inputLine.equals(resultExpected))
	                  resultJson.put(label,"up");
	                else
	                  resultJson.put(label,"down");
	              }
	              in.close();

	            }catch(Exception e){
	              synchronized(resultJson){
	                e.printStackTrace();
	                resultJson.put(label,"down");
	              }
	            }
	          }
	              
	        };
	        t1.start();
	        return t1;
	    }

	    private Thread checkURLCode(String url, String labelT)
	    throws IOException{

	        final String urlName = url;
	        final String label = labelT;

	        Thread t1;
	        t1 = new Thread(){
	          public void run(){
	            try{
	              URL url = new URL(urlName);
	              HttpURLConnection connection; 
	              connection = (HttpURLConnection)url.openConnection();
	              connection.setRequestMethod("GET");
	              connection.connect();

	              int code = connection.getResponseCode();

	              synchronized(resultJson){
	                if(code == 200)
	                  resultJson.put(label,"up");
	                else
	                  resultJson.put(label,"down");
	              }

	            }catch(Exception e){
	              e.printStackTrace();
	              synchronized(resultJson){
	                resultJson.put(label,"down");
	              }
	            }
	          }
	        };
	        t1.start();
	        return t1;
	   }
}