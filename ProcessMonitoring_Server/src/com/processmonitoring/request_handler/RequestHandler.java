package com.processmonitoring.request_handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.processmonitoring.bean.CcsOutgoingMessage;
import com.processmonitoring.dataBase.dataBase;
import com.processmonitoring.server.MessageHelper;
import com.processmonitoring.server.XMPPServer;
import com.processmonitoring.util.Util;
/**
 * Handles request from android and desktop client
 */
public class RequestHandler {
	private static dataBase dBase = new dataBase();
	private static XMPPServer xmppServer;
	public static void setServer(XMPPServer xmppServer) {
		RequestHandler.xmppServer = xmppServer;
	}
	
	public static void handleRequest(String data) {
		try {
			Map<String, Object> listData = (Map<String, Object>) JSONValue.parseWithException(data);
			String type = (String) listData.get("requestType");
			switch(type) {
				case "getUsers":
					String accountName = (String) listData.get("accName");
					HashSet<String> users = dBase.getUsers(accountName);
					JSONObject jsonUsers = new JSONObject();
			    	Iterator<String> iterator = users.iterator();
					int k = 1;
			        while (iterator.hasNext()) {
			        	String userName = iterator.next();
			        	jsonUsers.put(k, userName); 
			        	System.out.println(userName);
					    k++;
			        }	
					 //Send user list  to android device
					RequestHandler.sendResponseToDevice("returnUsers", jsonUsers.toString());
				break;
				case "registration":
					String login = (String) listData.get("login");
					String pass = (String) listData.get("password");
					String masterKey = (String) listData.get("masterKey");
					System.out.println("LOGIN: " + listData.get("login"));
					System.out.println("PASSWORD: " + listData.get("password"));
					System.out.println("masterKey: " + listData.get("masterKey"));
					
					dBase.createAccoutn(login, pass, masterKey);
					
					break;
				case "getAppsList":
					JSONObject jsonBlacklist = new JSONObject();
					k = 0;
					 for (Map.Entry<String, Object> entry : listData.entrySet()) {
						    //System.out.println("Package: " + entry.getKey() + " Name: " + entry.getValue());
						    if(k < 50) {
						    	 jsonBlacklist.put(entry.getKey(), entry.getValue()); 
						    }
						    k++;
					 }	
					 //Send back list of apps to android device
					RequestHandler.sendResponseToDevice("updateBlacklist", jsonBlacklist.toString());
					 break;
				case "updateBlackList":
					 for (Map.Entry<String, Object> entry : listData.entrySet()) {
						  System.out.println("BLOCKED APP: Package: " + entry.getKey() + " Name: " + entry.getValue());
					 }	
					break;
				default:
					break;
			}
		} catch(ParseException e) {
			e.printStackTrace();
		}
		
		
	}
	/**
	 * Sends data to device
	 * it can be request or updated list of applications
	 */
	 public static void sendResponseToDevice(String requestType, String data) {
		    String messageId = Util.getUniqueMessageId();
	 		Map<String, String> dataPayload = new HashMap<String, String>();
	 		dataPayload.put("type", requestType);
	 		dataPayload.put(Util.PAYLOAD_ATTRIBUTE_MESSAGE, data);
	 		CcsOutgoingMessage message = new CcsOutgoingMessage(Util.Device_token, messageId, dataPayload);
	 		String jsonRequest = MessageHelper.createJsonOutMessage(message);
	 		xmppServer.send(jsonRequest);
	}
}
