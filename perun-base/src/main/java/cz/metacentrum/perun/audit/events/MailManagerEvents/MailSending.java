package cz.metacentrum.perun.audit.events.MailManagerEvents;

import cz.metacentrum.perun.registrar.model.ApplicationMail;

public class MailSending {


	private ApplicationMail mail;
	private boolean enabled;
	private String message;
	private String name = this.getClass().getName();

	public MailSending() {
	}

	public MailSending(ApplicationMail mail, boolean enabled) {
		this.mail = mail;
		this.enabled = enabled;
	}

	//constructor

	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "Sending of Mail ID: " + mail.getId() + " " + ((enabled) ? " enabled." : " disabled.");
	}
}
