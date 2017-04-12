package com.processmonitoring.server;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONValue;

import com.processmonitoring.bean.CcsIncomingMessage;
import com.processmonitoring.bean.CcsOutgoingMessage;

/**
 * Helper for the transformation of JSON messages to attribute maps and vice
 * versa in the XMPP Server
 */

public class MessageHelper {

	/**
	 * Creates a JSON from a FCM outgoing message attributes
	 */
	public static String createJsonOutMessage(CcsOutgoingMessage outMessage) {
		return createJsonMessage(createAttributeMap(outMessage));
	}

	/**
	 * Creates a JSON encoded ACK message for a received upstream message
	 */
	public static String createJsonAck(String to, String messageId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("message_type", "ack");
		map.put("to", to);
		map.put("message_id", messageId);
		return createJsonMessage(map);
	}

	public static String createJsonMessage(Map<String, Object> jsonMap) {
		return JSONValue.toJSONString(jsonMap);
	}

	/**
	 * Creates a MAP from a FCM outgoing message attributes
	 */
	public static Map<String, Object> createAttributeMap(CcsOutgoingMessage msg) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (msg.getTo() != null) {
			map.put("to", msg.getTo());
		}
		if (msg.getMessageId() != null) {
			map.put("message_id", msg.getMessageId());
		}
		map.put("data", msg.getDataPayload());
		if (msg.getCondition() != null) {
			map.put("condition", msg.getCondition());
		}
		return map;
	}

	/**
	 * Creates an incoming message according the bean
	 */
	@SuppressWarnings("unchecked")
	public static CcsIncomingMessage createCcsInMessage(Map<String, Object> jsonMap) {
		String from = jsonMap.get("from").toString();
		// Package name of the application that sent this message
		String category = jsonMap.get("category").toString();
		// Unique id of this message
		String messageId = jsonMap.get("message_id").toString();
		Map<String, String> dataPayload = (Map<String, String>) jsonMap.get("data");
		CcsIncomingMessage msg = new CcsIncomingMessage(from, category, messageId, dataPayload);
		return msg;
	}
}

