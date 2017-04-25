package com.processmonitoring.server;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.xmlpull.v1.XmlPullParser;

import com.processmonitoring.bean.CcsIncomingMessage;
import com.processmonitoring.request_handler.RequestHandler;
import com.processmonitoring.util.Util;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocketFactory;

/**
 * The XMPPServer class manages the connection and the message processing
 */
public class XMPPServer implements PacketListener {

	public static final Logger logger = Logger.getLogger(XMPPServer.class.getName());

	private static XMPPServer sInstance = null;
	private XMPPConnection connection;
	private ConnectionConfiguration config;
	private String mApiKey = null;
	private String mProjectId = null;
	private boolean mDebuggable = false;
	private String fcmServerUsername = null;

	public static XMPPServer getInstance() {
		if (sInstance == null) {
			throw new IllegalStateException("You have to prepare the client first");
		}
		return sInstance;
	}

	public static XMPPServer prepareClient(String projectId, String apiKey, boolean debuggable) {
		synchronized (XMPPServer.class) {
			if (sInstance == null) {
				sInstance = new XMPPServer(projectId, apiKey, debuggable);
			}
		}
		return sInstance;
	}

	private XMPPServer(String projectId, String apiKey, boolean debuggable) {
		this();
		mApiKey = apiKey;
		mProjectId = projectId;
		mDebuggable = debuggable;
		fcmServerUsername = mProjectId + "@" + Util.FCM_SERVER_CONNECTION;
	}

	private XMPPServer() {
		// Add GcmPacketExtension
		ProviderManager.getInstance().addExtensionProvider(Util.FCM_ELEMENT_NAME, Util.FCM_NAMESPACE,
				new PacketExtensionProvider() {

					@Override
					public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
						String json = parser.nextText();
						GcmPacketExtension packet = new GcmPacketExtension(json);
						return packet;
					}
				});
	}

	/**
	 * Connects to FCM Cloud Connection Server using the supplied credentials
	 */
	public void connect() throws XMPPException {
		config = new ConnectionConfiguration(Util.FCM_SERVER, Util.FCM_PORT);
		config.setSecurityMode(SecurityMode.enabled);
		config.setReconnectionAllowed(true);
		config.setSocketFactory(SSLSocketFactory.getDefault());
		// Launch a window with info about packets sent and received
		config.setDebuggerEnabled(mDebuggable);

		connection = new XMPPConnection(config);
		connection.connect();

		connection.addConnectionListener(new ConnectionListener() {

			@Override
			public void reconnectionSuccessful() {
				logger.log(Level.INFO, "Reconnection successful ...");
			}

			@Override
			public void reconnectionFailed(Exception e) {
				logger.log(Level.INFO, "Reconnection failed: ", e.getMessage());
			}

			@Override
			public void reconnectingIn(int seconds) {
				logger.log(Level.INFO, "Reconnecting in %d secs", seconds);
			}

			@Override
			public void connectionClosedOnError(Exception e) {
				logger.log(Level.INFO, "Connection closed on error");
			}

			@Override
			public void connectionClosed() {
				logger.log(Level.INFO, "Connection closed");
			}
		});
		

		// Handle incoming packets (the class implements the PacketListener)
		connection.addPacketListener(this, new PacketTypeFilter(Message.class));
	
		// Log all outgoing packets
		connection.addPacketWriterInterceptor(new PacketInterceptor() {
			@Override
			public void interceptPacket(Packet packet) {
				System.out.println("INTERCEPT PACKAGE: " + packet.toXML());
			}
		}, new PacketTypeFilter(Message.class));
		connection.login(fcmServerUsername, mApiKey);
	}

	public void reconnect() {
		while (true) {
			try {
				connect();
				return;
			} catch (XMPPException e) {
				logger.log(Level.INFO, "Connecting again to FCM (manual reconnection)");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	/**
	 * Handles incoming messages
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void processPacket(Packet packet) {
		Message incomingMessage = (Message) packet;
		GcmPacketExtension gcmPacket = (GcmPacketExtension) incomingMessage.getExtension(Util.FCM_NAMESPACE);
		String json = gcmPacket.getJson();
		
		try {
			Map<String, Object> jsonMap = (Map<String, Object>) JSONValue.parseWithException(json);
			//get device token which will be saved in database
			String deviceToken = jsonMap.get("from").toString();
			 
			//retrieves data from android device
			String data = jsonMap.get("data").toString();
			
			//Handle request from android device
			RequestHandler.handleRequest(deviceToken, data);
			
			Object messageType = jsonMap.get("message_type");
		 
            if (messageType == null) {
                CcsIncomingMessage inMessage = MessageHelper.createCcsInMessage(jsonMap);
               
                handleUpstreamMessage(inMessage); // normal upstream message
                return;
            }
		} catch (ParseException e) {
			logger.log(Level.INFO, "Error parsing JSON: " + json, e.getMessage());
		} 
	}
	/**
     * Handles an upstream message from a device client through FCM
     */
    private void handleUpstreamMessage(CcsIncomingMessage inMessage) {
        // Send ACK to FCM
        String ack = MessageHelper.createJsonAck(inMessage.getFrom(), inMessage.getMessageId());
        send(ack);
    }
	/**
	 * Sends a downstream message to FCM server
	 */
	public void send(String jsonRequest) {
		Packet request = new GcmPacketExtension(jsonRequest).toPacket();
		connection.sendPacket(request);
	}
}

