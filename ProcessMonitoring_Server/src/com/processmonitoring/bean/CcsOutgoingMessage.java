package com.processmonitoring.bean;

import java.util.Map;

import com.processmonitoring.util.Util;

/**
 * Represents an outgoing message to FCM CCS
 */
public class CcsOutgoingMessage {

	// Sender registration ID
	private String to;
	// Condition that determines the message target
	private String condition;
	// Unique id for this message
	private String messageId;
	// Payload data. A String in JSON format
	private Map<String, String> dataPayload;

	public CcsOutgoingMessage(String to, String messageId, Map<String, String> dataPayload) {
		System.out.println("CcsOutMessage: " + to + "  " + messageId + "  " + dataPayload.get(Util.PAYLOAD_ATTRIBUTE_MESSAGE));
		this.to = to;
		this.messageId = messageId;
		this.dataPayload = dataPayload;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public Map<String, String> getDataPayload() {
		return dataPayload;
	}

	public void setDataPayload(Map<String, String> dataPayload) {
		this.dataPayload = dataPayload;
	}
}
