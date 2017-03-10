package com.processmonitoring;

import org.jivesoftware.smack.XMPPException;

import com.processmonitoring.server.ProcessMonitoringHttpServer;
import com.processmonitoring.server.XMPPServer;;

/**
 * Entry point for application
 * Initializes XMPP server
 */
public class ProcessMonitoringServer {
	private static XMPPServer xmppServer;
	private static final String fcmProjectSenderId ="892245693124";
	private static final String fcmServerKey = "AAAAz7380sQ:APA91bGXXDdkpeMUnVgqQXpCQ5IVa0FM_EmEFKSp5CmrTA1l8IJ7b_dxeK-BJJet6yC1U1P_NYiolOpoqvSLF021K0JSL28E-roNCVqBWiiTmshJgiHiwB0jVz38jQUFIJC1w5DidFDq";
	private static final String toRegId = "dKUzBIZ_Lb8:APA91bFjwsAWlAopFBbk_dwjeXfQkP3wcjJr77Vxw-FBKGpgRwnWyq9S6_sQ-RfYesw7i0xASW3Pla4SMoz9jBaoWTpwvjzfl3W96KaK4Z_nMv8IWgnuou4E6R8kXezrJvSLE3a32pjH";

	public static void main(String[] args) {
		xmppServer = XMPPServer.prepareClient(fcmProjectSenderId, fcmServerKey, true);

		try {
			xmppServer.connect();
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	    ProcessMonitoringHttpServer httpServer = new ProcessMonitoringHttpServer();
	    httpServer.create();
	}
}