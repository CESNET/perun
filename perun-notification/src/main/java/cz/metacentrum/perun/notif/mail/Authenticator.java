package cz.metacentrum.perun.notif.mail;

import javax.mail.PasswordAuthentication;

/**
 * Implementation of javax.mail.Authenticator, hodls holder of
 * passwordAuthentication
 *
 * @author tomas.tunkl
 *
 */
public class Authenticator extends javax.mail.Authenticator {

	private PasswordAuthentication passwordAuthentication;

	public Authenticator(String username, String password) {
		passwordAuthentication = new PasswordAuthentication(username, password);
	}

	protected PasswordAuthentication getPasswordAuthentication() {
		return passwordAuthentication;
	}
}
