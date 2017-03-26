package com.processmonitoring.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.processmonitoring.bean.CcsOutgoingMessage;
import com.processmonitoring.request_handler.RequestHandler;
import com.processmonitoring.util.Util;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Handles HTTP requests from desktop client
 */
public class ProcessMonitoringHttpServer {
	 HttpServer server;
	 public ProcessMonitoringHttpServer() {
		 
	 }
	 public void create() {
		 try {
				server = HttpServer.create(new InetSocketAddress(8000), 0);
				 server.createContext("/", new Handler());
			        server.setExecutor(null); // creates a default executor
			        server.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
	 }
	 static class Handler implements HttpHandler {
	        @Override
	        public void handle(HttpExchange t) throws IOException {
	        	InputStreamReader isr =  new InputStreamReader(t.getRequestBody(),"utf-8");
	        	BufferedReader br = new BufferedReader(isr);
	        	String requestFromDesktopClient = br.readLine();
	        	System.out.println("Message from desktop client: " + requestFromDesktopClient);
	        	
	        	RequestHandler.sendResponseToDevice("requestListApps", requestFromDesktopClient);

	        	String response = "Response from HttpServer";
	            t.sendResponseHeaders(200, response.length());
	            OutputStream os = t.getResponseBody();
	            os.write(response.getBytes());
	            os.close();
	        }
	    }
}
