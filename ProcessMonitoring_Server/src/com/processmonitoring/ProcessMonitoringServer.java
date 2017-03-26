package com.processmonitoring;

import org.jivesoftware.smack.XMPPException;

import com.processmonitoring.request_handler.RequestHandler;
import com.processmonitoring.server.ProcessMonitoringHttpServer;
import com.processmonitoring.server.XMPPServer;

/**
 * Entry point for application
 * Initializes XMPP server
 */
public class ProcessMonitoringServer {
	private static XMPPServer xmppServer;
	private static final String fcmProjectSenderId ="892245693124";
	private static final String fcmServerKey = "AAAAz7380sQ:APA91bGXXDdkpeMUnVgqQXpCQ5IVa0FM_EmEFKSp5CmrTA1l8IJ7b_dxeK-BJJet6yC1U1P_NYiolOpoqvSLF021K0JSL28E-roNCVqBWiiTmshJgiHiwB0jVz38jQUFIJC1w5DidFDq";
	
	public static void main(String[] args) {
		xmppServer = XMPPServer.prepareClient(fcmProjectSenderId, fcmServerKey, true);

		try {
			xmppServer.connect();
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		
	    ProcessMonitoringHttpServer httpServer = new ProcessMonitoringHttpServer();
	    httpServer.create();
	    
	    RequestHandler.setServer(xmppServer);
	}
}