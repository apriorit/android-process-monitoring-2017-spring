package com.processmonitoring.request_handler;

import java.util.HashMap;
import java.util.Map;

import com.processmonitoring.bean.CcsOutgoingMessage;
import com.processmonitoring.server.MessageHelper;
import com.processmonitoring.server.XMPPServer;
import com.processmonitoring.util.Util;
/**
 * Handles request from android and desktop client
 */
public class RequestHandler {
	private static XMPPServer xmppServer;
	public static void setServer(XMPPServer xmppServer) {
		RequestHandler.xmppServer = xmppServer;
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
