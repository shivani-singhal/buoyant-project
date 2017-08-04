package com.buoyant;
import org.eclipse.jetty.server.Server;

public class HealthCheck{
	public static void main(String[] args) throws Exception
    {
        Server server = new Server(8080);
        server.setHandler(new HealthCheckHandler());
 
        server.start();
        server.join();
    }
}