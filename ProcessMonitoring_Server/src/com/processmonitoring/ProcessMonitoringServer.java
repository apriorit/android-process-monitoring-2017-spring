package com.processmonitoring;

import org.jivesoftware.smack.XMPPException;

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
	private static final String toRegId = "fDDIaq0l3v8:APA91bEPi_DVCtyw7K4DC25ZGy_PGSHevLTE2QdezXeug-N7Dvhk46ZHBuB63Yt7mh2E9VMcnRFo5GplJ3hiDpJsKNOco30X2nrmTTAzuL08rK_dH-lPaElYx8dA6cXUd6jWC4RNbJly";

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