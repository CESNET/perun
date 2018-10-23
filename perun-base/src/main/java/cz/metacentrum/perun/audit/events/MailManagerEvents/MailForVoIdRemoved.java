package cz.metacentrum.perun.audit.events.MailManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.registrar.model.ApplicationMail;

public class MailForVoIdRemoved implements AuditEvent {
	private ApplicationMail mail;
	private Vo vo;
	private String name = this.getClass().getName();
	private String message;

	public MailForVoIdRemoved(ApplicationMail mail, Vo vo) {
		this.mail = mail;
		this.vo = vo;
	}

	public MailForVoIdRemoved() {
	}

	@Override
	public String getMessage() {
		return toString();
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ApplicationMail getMail() {
		return mail;
	}

	public void setMail(ApplicationMail mail) {
		this.mail = mail;
	}

	public Vo getVo() {
		return vo;
	}

	public void setVo(Vo vo) {
		this.vo = vo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Mail ID: " + mail.getId() + " of Type: " + mail.getMailType() + "/" + mail.getAppType() + " removed for VO ID: " + vo.getId() + ".";
	}
}
