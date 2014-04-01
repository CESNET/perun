package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;

/**
 * Thrown when initial application for the user already exists.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class DuplicateRegistrationAttemptException extends PerunException {
	private final String actor;
	private final String extSourceName;
	private final int appId;

	public DuplicateRegistrationAttemptException(String message, String actor, String extSourceName, int appId) {
		super(message);
		this.actor = actor;
		this.extSourceName = extSourceName;
		this.appId = appId;
	}

	public String getActor() {
		return actor;
	}

	public String getExtSourceName() {
		return extSourceName;
	}

	public int getApplicationId() {
		return appId;
	}
}
