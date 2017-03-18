package com.processmonitoring.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.processmonitoring.bean.CcsOutgoingMessage;
import com.processmonitoring.util.Util;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Handles HTTP requests from desktop client
 */
public class ProcessMonitoringHttpServer {
	 HttpServer server;
	 private static XMPPServer xmppServer;
	 public ProcessMonitoringHttpServer(XMPPServer x) {
		 xmppServer = x;
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
	        	
	        	sendResponseToDevice(requestFromDesktopClient);

	        	String response = "Response from HttpServer";
	            t.sendResponseHeaders(200, response.length());
	            OutputStream os = t.getResponseBody();
	            os.write(response.getBytes());
	            os.close();
	        }
	    }
	 private static void sendResponseToDevice(String request) {
		  String messageId = Util.getUniqueMessageId();
	 		Map<String, String> dataPayload = new HashMap<String, String>();
	 		dataPayload.put(Util.PAYLOAD_ATTRIBUTE_MESSAGE, request);
	 		CcsOutgoingMessage message = new CcsOutgoingMessage(Util.Device_token, messageId, dataPayload);
	 		String jsonRequest = MessageHelper.createJsonOutMessage(message);
	 		xmppServer.send(jsonRequest);
	}
}
