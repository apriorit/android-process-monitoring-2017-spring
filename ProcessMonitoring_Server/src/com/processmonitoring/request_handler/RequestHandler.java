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
			switch (type) {
			case "user-authentication":
				login = (String) listData.get("login");
				password = (String) listData.get("password");
				if (DatabaseHandler.checkAuthentication(login, password)) {
					RequestHandler.sendResponseToDevice(tokenID, "user-authentication", "success");
				} else {
					RequestHandler.sendResponseToDevice(tokenID, "user-authentication", "failed");
				}
				break;
			case "account_registration":
				System.out.println("registrrrrrrrr " + (String) listData.get("key"));
				login = (String) listData.get("login");
				if(DatabaseHandler.checkRegistration(login)) {
					DatabaseHandler.addAccount(login, (String) listData.get("password"), (String) listData.get("key"));
					RequestHandler.sendResponseToDevice(tokenID, "account_registration", "success");
				} else {
					RequestHandler.sendResponseToDevice(tokenID, "account_registration", "failed");
				}
				break;
			case "device_registration":
				System.out.println("device registration " + (String) listData.get("login"));
				DatabaseHandler.addDevice((String) listData.get("login"), (String) listData.get("user_name"), "", "", 0,
						0);
				break;
			case "save-device-info":
				userID = Integer.parseInt((String) listData.get("user-id"));
				DatabaseHandler.updateDeviceInfo(userID, tokenID, data, 0, 0);
				break;
			case "get-list-devices":
				JSONObject jsonListDevices = new JSONObject();
				login = (String) listData.get("login");
				Map<Integer, String> listDevices = DatabaseHandler.getDevices(login);
				for (Map.Entry<Integer, String> entry : listDevices.entrySet()) {
					System.out.println("device: " + entry.getKey() + " " + entry.getValue());
					jsonListDevices.put(entry.getKey(), entry.getValue());
				}
				RequestHandler.sendResponseToDevice(tokenID, "list-devices", jsonListDevices.toString());
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
				RequestHandler.sendResponseToDevice(tokenID, "list-apps", jsonListApps.toString());
				break;
			case "update-blacklist":
				System.out.println("update-blacklist " + listData.entrySet().toString());
				userID = Integer.parseInt((String) listData.get("user-id"));
				System.out.println("userID = " + userID);
				System.out.println("data list  = " + data);
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

				RequestHandler.sendResponseToDevice(token, "update-blacklist", jsonListApps2.toString());

				break;
			case "update-list":
				userID = Integer.parseInt((String) listData.get("user-id"));
				System.out.println("userID: " + userID);
				token = DatabaseHandler.getToken(userID);
				System.out.println("(String) listData.get( " + (String) listData.get("user-id"));
				RequestHandler.sendResponseToDevice(token, "update-list", (String) listData.get("user-id"));
				break;
			case "delete-device":
				System.out.println("delete-device");
				userID = Integer.parseInt((String) listData.get("user-id"));
				DatabaseHandler.deleteDevice(userID);
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

	/**
	 * Sends data to device it can be request or updated list of applications
	 */
	public static void sendResponseToDevice(String deviceToken, String requestType, String data) {
		String messageId = Util.getUniqueMessageId();
		Map<String, String> dataPayload = new HashMap<String, String>();
		dataPayload.put("requestType", requestType);
		dataPayload.put(Util.PAYLOAD_ATTRIBUTE_MESSAGE, data);
		CcsOutgoingMessage message = new CcsOutgoingMessage(deviceToken, messageId, dataPayload);
		String jsonRequest = MessageHelper.createJsonOutMessage(message);
		xmppServer.send(jsonRequest);
	}
}
