package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.User;
import java.util.HashMap;
import java.util.Map;

import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import cz.metacentrum.perun.core.impl.AuthzRoles;

/**
 * Identification of the acting person.
 *
 * @author Martin Kuba
 */
public class PerunPrincipal {

	private String actor;
	private String extSourceName;
	private String extSourceType;
	private int extSourceLoa = 0; // 0 by default
	private User user;
	// Contains principal's roles together with objects which specifies the role, e.g. VOADMIN -> list contains VO names
	private volatile AuthzRoles authzRoles = new AuthzRoles();
	// Map contains additional attributes, e.g. from authentication system
	private Map<String, String> additionalInformations = new HashMap<String, String>();
	// Specifies if the principal has initialized authZResolver
	private volatile boolean authzInitialized = false;

	/**
	 * Creates a new instance for a given string.
	 * @param actor string identifying the user in Grouper
	 * @throws InternalErrorRuntimeException actor is null
	 */
	public PerunPrincipal(String actor, String extSourceName, String extSourceType) {
		if (actor == null) throw new InternalErrorRuntimeException(new NullPointerException("actor is null"));
		if (extSourceName == null) throw new InternalErrorRuntimeException(new NullPointerException("extSourceName is null"));
		if (extSourceType == null) throw new InternalErrorRuntimeException(new NullPointerException("extSourceType is null"));

		this.actor = actor;
		this.extSourceName = extSourceName;
		this.extSourceType = extSourceType;
	}

	public PerunPrincipal(String actor, String extSourceName, String extSourceType, User user) {
		this(actor, extSourceName, extSourceType);
		this.user = user;
	}

	public PerunPrincipal(String actor, String extSourceName, String extSourceType, int extSourceLoa) {
		this(actor, extSourceName, extSourceType);
		this.extSourceLoa = extSourceLoa;
	}

	public PerunPrincipal(String actor, String extSourceName, String extSourceType, int extSourceLoa, Map<String, String> additionalInformations) {
		this(actor, extSourceName, extSourceType, extSourceLoa);
		this.additionalInformations = additionalInformations;
	}

	/**
	 * Returns actor string representation.
	 * @return string representing actor
	 */
	public String getActor() {
		return actor;
	}

	@Override
	public String toString() {
		return new StringBuilder(getClass().getSimpleName()).append(":[")
				.append("actor='").append(actor).append("', ")
				.append("user='").append((user != null ? user : "null")).append("', ")
				.append("extSourceName='").append(extSourceName).append("', ")
				.append("authzRoles='").append(authzRoles).append("', ")
				.append("authzInitialized='").append(authzInitialized).append("']").toString();
	}

	public String getExtSourceName() {
		return extSourceName;
	}

	public void setExtSourceName(String extSourceName) {
		this.extSourceName = extSourceName;
	}

	public boolean isAuthzInitialized() {
		return authzInitialized;
	}

	public void setAuthzInitialized(boolean authzInitialized) {
		this.authzInitialized = authzInitialized;
	}

	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public int getUserId() {
		if (this.user == null) {
			return -1;
		}
		return this.user.getId();
	}

	public AuthzRoles getRoles() {
		//return (AuthzRoles) Collections.unmodifiableMap(authzRoles);
		return this.authzRoles;
	}

	public void setRoles(AuthzRoles authzRoles) {
		this.authzRoles = authzRoles;
	}

	public Map<String, String> getAdditionalInformations() {
		return additionalInformations;
	}

	public void setAdditionalInformations(Map<String, String> additionalInformations) {
		this.additionalInformations = additionalInformations;
	}

	public String getExtSourceType() {
		return extSourceType;
	}

	public void setExtSourceType(String extSourceType) {
		this.extSourceType = extSourceType;
	}

	public int getExtSourceLoa() {
		return extSourceLoa;
	}

	public void setExtSourceLoa(int extSourceLoa) {
		this.extSourceLoa = extSourceLoa;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actor == null) ? 0 : actor.hashCode());
		result = prime * result
			+ ((extSourceName == null) ? 0 : extSourceName.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		result = prime * result + extSourceLoa;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PerunPrincipal other = (PerunPrincipal) obj;
		if (actor == null) {
			if (other.actor != null)
				return false;
		} else if (!actor.equals(other.actor))
			return false;
		if (extSourceName == null) {
			if (other.extSourceName != null)
				return false;
		} else if (!extSourceName.equals(other.extSourceName))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}
}
