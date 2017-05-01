package com.processmonitoring.request_handler;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.processmonitoring.bean.CcsOutgoingMessage;
import com.processmonitoring.database.DatabaseHandler;
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

	public static void handleRequest(String tokenID, String data) {
		try {
			Map<String, Object> listData = (Map<String, Object>) JSONValue.parseWithException(data);
			String type = (String) listData.get("requestType");
			String login;
			String password;
			int userID;
			String token;
			Map<String, String> dataPayload = new HashMap<String, String>();
			switch (type) {
			case "user-authentication":
				login = (String) listData.get("login");
				password = (String) listData.get("password");
				
				dataPayload.put("requestType", "user-authentication");
				if (DatabaseHandler.checkAuthentication(login, password)) {
					String masterKey = DatabaseHandler.getMasterKey(login);
					dataPayload.put(Util.PAYLOAD_ATTRIBUTE_MESSAGE, masterKey);
				} else {
					dataPayload.put(Util.PAYLOAD_ATTRIBUTE_MESSAGE, "failed");				
				}
				RequestHandler.sendDataToDevice(tokenID, dataPayload);
				break;
			case "account_registration":
				login = (String) listData.get("login");
				dataPayload.put("requestType", "account_registration");
				
				if (DatabaseHandler.checkRegistration(login)) {
					DatabaseHandler.addAccount(login, (String) listData.get("password"), (String) listData.get("key"));
					dataPayload.put(Util.PAYLOAD_ATTRIBUTE_MESSAGE, "success");
					RequestHandler.sendDataToDevice(tokenID, dataPayload);
				} else {
					dataPayload.put(Util.PAYLOAD_ATTRIBUTE_MESSAGE, "failed");
				}
				RequestHandler.sendDataToDevice(tokenID, dataPayload);
				break;
			case "device_registration":
				DatabaseHandler.addDevice((String) listData.get("login"), (String) listData.get("user_name"), "", "", 0,
						0);
				sendListDevices(tokenID, (String) listData.get("login"));
				break;
			case "save-device-info":
				userID = Integer.parseInt((String) listData.get("user-id"));
				DatabaseHandler.updateDeviceInfo(userID, tokenID, data, 0, 0);
				break;
			case "get-list-devices":
				sendListDevices(tokenID, (String) listData.get("login"));
				break;
			case "get-list-apps":
				userID = Integer.parseInt((String) listData.get("user-id"));
				JSONObject jsonListApps = new JSONObject();
				String appList = DatabaseHandler.getListApps(userID);
				Map<String, Object> mapListApps = (Map<String, Object>) JSONValue.parseWithException(appList);

				for (Map.Entry<String, Object> entry : mapListApps.entrySet()) {
					if (!entry.getKey().equals("requestType") && !entry.getKey().equals("user-id")) {
						jsonListApps.put(entry.getKey(), entry.getValue());
					}
				}
				// Send list with devices to android device
				dataPayload.put("requestType", "list-apps");
				dataPayload.put(Util.PAYLOAD_ATTRIBUTE_MESSAGE, jsonListApps.toString());
				RequestHandler.sendDataToDevice(tokenID, dataPayload);
				break;
			case "update-blacklist":
				userID = Integer.parseInt((String) listData.get("user-id"));
				
				// update only apps list in specific row in mysql database
				DatabaseHandler.updateListApps(userID, data);
				// Send new list of apps to specific android device
				token = DatabaseHandler.getToken(userID);
				Map<String, Object> mapListApps2 = (Map<String, Object>) JSONValue.parseWithException(data);
				JSONObject jsonListApps2 = new JSONObject();
				for (Map.Entry<String, Object> entry : mapListApps2.entrySet()) {
					if (!entry.getKey().equals("requestType") && !entry.getKey().equals("user-id")) {
						jsonListApps2.put(entry.getKey(), entry.getValue());
					}

				}
				dataPayload.put("requestType", "update-blacklist");
				dataPayload.put(Util.PAYLOAD_ATTRIBUTE_MESSAGE, jsonListApps2.toString());
				RequestHandler.sendDataToDevice(token, dataPayload);
				break;
			case "update-list":
				userID = Integer.parseInt((String) listData.get("user-id"));
				token = DatabaseHandler.getToken(userID);
			
				dataPayload.put("requestType", "update-list");
				dataPayload.put(Util.PAYLOAD_ATTRIBUTE_MESSAGE, (String) listData.get("user-id"));
				RequestHandler.sendDataToDevice(token, dataPayload);
				break;
			case "get-file":
				userID = Integer.parseInt((String) listData.get("user-id"));
				token = DatabaseHandler.getToken(userID);
				
				dataPayload.put("requestType", "get-file");
				dataPayload.put(Util.PAYLOAD_ATTRIBUTE_MESSAGE, (String) listData.get("user-id"));
				RequestHandler.sendDataToDevice(token, dataPayload);
				break;
			case "delete-device":
				userID = Integer.parseInt((String) listData.get("user-id"));
				DatabaseHandler.deleteDevice(userID);
				break;
			case "get-list-files":
				userID = Integer.parseInt((String) listData.get("user-id"));
				token = DatabaseHandler.getToken(userID);
				String directory = (String) listData.get("directory");
				
				dataPayload.put("requestType", "get-list-files");
				dataPayload.put("directory", directory);
				dataPayload.put("token", tokenID);
				RequestHandler.sendDataToDevice(token, dataPayload);
				break;
			case "list-files":
				String tokenSender = (String) listData.get("token");
				
				Map<String, Object> mapListFiles = (Map<String, Object>) JSONValue.parseWithException(data);
				JSONObject jsonListFiles = new JSONObject();
				for (Map.Entry<String, Object> entry : mapListFiles.entrySet()) {
					if (!entry.getKey().equals("requestType") && !entry.getKey().equals("token")) {
						jsonListFiles.put(entry.getKey(), entry.getValue());
					}
				}
				dataPayload.put("requestType", "list-files");
				dataPayload.put(Util.PAYLOAD_ATTRIBUTE_MESSAGE, jsonListFiles.toString());
				RequestHandler.sendDataToDevice(tokenSender, dataPayload);
				
				break;
			case "send-file":
				userID = Integer.parseInt((String) listData.get("user-id"));
				token = DatabaseHandler.getToken(userID);
				String path = (String) listData.get("directory");
				String fileName = (String) listData.get("filename");
				
				dataPayload.put("requestType", "send-file");
				dataPayload.put("filename", fileName);
				dataPayload.put(Util.PAYLOAD_ATTRIBUTE_MESSAGE, path);
				
				RequestHandler.sendDataToDevice(token, dataPayload);
				break;
			case "enable-app":
				login = (String) listData.get("login");
				password = (String) listData.get("password");
				
				if (DatabaseHandler.checkAuthentication(login, password)) {
					dataPayload.put("requestType", "enable-app");
					RequestHandler.sendDataToDevice(tokenID, dataPayload);
				}
				break;
			case "debug": 
				System.out.println("PACKAGE = " + (String) listData.get("package"));
				System.out.println("CLASS = " + (String) listData.get("class"));
				System.out.println("TEXT = " + (String) listData.get("text"));
				break;
			default:
				break;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public static String getListApps(int userID) {
		JSONObject jsonListApps = new JSONObject();
		String appList = DatabaseHandler.getListApps(userID);
		Map<String, Object> mapListApps;
		try {
			mapListApps = (Map<String, Object>) JSONValue.parseWithException(appList);
			for (Map.Entry<String, Object> entry : mapListApps.entrySet()) {
				if (!entry.getKey().equals("requestType") && !entry.getKey().equals("user-id")) {
					jsonListApps.put(entry.getKey(), entry.getValue());
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return jsonListApps.toString();
	}

	public static void sendListDevices(String deviceToken, String login) {
		JSONObject jsonListDevices = new JSONObject();
		// login = (String) listData.get("login");
		Map<Integer, String> listDevices = DatabaseHandler.getDevices(login);
		for (Map.Entry<Integer, String> entry : listDevices.entrySet()) {
			jsonListDevices.put(entry.getKey(), entry.getValue());
		}
		Map<String, String> dataPayload = new HashMap<String, String>();
		dataPayload.put("requestType", "list-devices");
		dataPayload.put(Util.PAYLOAD_ATTRIBUTE_MESSAGE, jsonListDevices.toString());
		RequestHandler.sendDataToDevice(deviceToken, dataPayload);
	}

	/**
	 * Sends data to device it can be request or updated list of applications
	 */
	public static void sendDataToDevice(String deviceReceiver, Map<String, String> dataPayload) {
		String messageId = Util.getUniqueMessageId();

		CcsOutgoingMessage message = new CcsOutgoingMessage(deviceReceiver, messageId, dataPayload);
		String jsonRequest = MessageHelper.createJsonOutMessage(message);
		xmppServer.send(jsonRequest);
	}
}
