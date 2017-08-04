package com.buoyant;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

public class HealthCheck{
	public static void main(String[] args) throws Exception
    {
        Server server = new Server(8080);
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        handler.addServletWithMapping(Handler2.class, "/");

        
 
        server.start();
        server.join();
    }
}