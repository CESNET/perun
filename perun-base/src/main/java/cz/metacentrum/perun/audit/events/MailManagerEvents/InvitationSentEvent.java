package cz.metacentrum.perun.audit.events.MailManagerEvents;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;

public class InvitationSentEvent extends AuditEvent {

	private User sender;
	private String email;
	private String language;
	private String message;
	private Vo vo;
	private Group group;

	@SuppressWarnings("unused") // used by jackson mapper
	public InvitationSentEvent() {
	}

	public InvitationSentEvent(User user, String mail, String language, Group group, Vo vo) {
		this.sender = user;
		this.email = mail;
		this.language = language;
		this.group = group;
		this.vo = vo;

		this.message = formatMessage("Invitation to %s, sent by %s, to email: %s, in language: %s.",
			(group != null) ? group : vo, sender, email, language);

	}

	public User getSender() {
		return sender;
	}

	public String getEmail() {
		return email;
	}

	public String getLanguage() {
		return language;
	}

	public Vo getVo() {
		return vo;
	}

	public Group getGroup() {
		return group;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return message;
	}
}
