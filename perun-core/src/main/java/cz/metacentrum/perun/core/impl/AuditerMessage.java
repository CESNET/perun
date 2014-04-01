package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.PerunSession;

public class AuditerMessage {
	private String message;
	private PerunSession originaterPerunSession;

	public AuditerMessage(PerunSession sess, String message) {
		this.message = message;
		this.originaterPerunSession = sess;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public PerunSession getOriginaterPerunSession() {
		return this.originaterPerunSession;
	}

	public void setOriginaterPerunSession(PerunSession originaterPerunSession) {
		this.originaterPerunSession = originaterPerunSession;
	}

	@Override
	public String toString() {
		return "AuditerMessage:[message='" + getMessage() + "]";
	}

}
