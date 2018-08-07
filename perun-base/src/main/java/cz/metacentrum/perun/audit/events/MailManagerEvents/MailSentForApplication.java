package cz.metacentrum.perun.audit.events.MailManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.registrar.model.ApplicationMail;

public class MailSentForApplication extends AuditEvent {

	private final ApplicationMail.MailType mailType;
	private final int appId;
	private final String message;

	public MailSentForApplication(ApplicationMail.MailType mailType, int appId) {
		this.mailType = mailType;
		this.appId = appId;
		this.message = String.format("Mail of Type: %s sent for Application: %d", mailType, appId);
	}

	@Override
	public String getMessage() {
		return message;
	}

	public ApplicationMail.MailType getMailType() {
		return mailType;
	}

	public int getAppId() {
		return appId;
	}

	@Override
	public String toString() {
		return message;
	}
}
