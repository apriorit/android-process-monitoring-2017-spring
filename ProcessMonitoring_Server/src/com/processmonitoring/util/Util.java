package com.processmonitoring.util;

import java.util.UUID;

/**
 * Util class for constants and generic methods
 */

public class Util {

	// For the GCM connection
	public static final String FCM_SERVER = "fcm-xmpp.googleapis.com";
	//public static final String FCM_SERVER = "gcm-xmpp.googleapis.com";
	public static final int FCM_PORT = 5236;
	public static final String FCM_ELEMENT_NAME = "gcm";
	public static final String FCM_NAMESPACE = "google:mobile:data";
	public static final String FCM_SERVER_CONNECTION = "gcm.googleapis.com";
	public static final String PAYLOAD_ATTRIBUTE_MESSAGE = "LIST_APPS";
	
	/**
	 * Returns a random message id to uniquely identify a message
	 */
	public static String getUniqueMessageId() {
		return "m-" + UUID.randomUUID().toString();
	}
}
