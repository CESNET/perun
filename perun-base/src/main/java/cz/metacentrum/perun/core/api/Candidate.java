package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.metacentrum.perun.core.api.BeansUtils;

/**
 * Candidate member of a Virtual Organization.
 * @author Michal Prochazka michalp@ics.muni.cz
 * @author Martin Kuba makub@ics.muni.cz
 */
public class Candidate extends User {

	private UserExtSource userExtSource;
	private List<UserExtSource> additionalUserExtSources;
	private Map<String, String> attributes;

	public Candidate() {
	}

	//Special constructor to get candidate from user
	public Candidate(User user, UserExtSource userExtSource) {
		if(user != null) {
			this.setFirstName(user.getFirstName());
			this.setLastName(user.getLastName());
			this.setMiddleName(user.getMiddleName());
			this.setTitleAfter(user.getTitleAfter());
			this.setTitleBefore(user.getTitleBefore());
			this.setServiceUser(user.isServiceUser());
			this.setSponsoredUser(user.isSponsoredUser());
		}
		this.userExtSource = userExtSource;
	}

	public Candidate(UserExtSource userExtSource, Map<String, String> attributes) {
		this();
		this.userExtSource = userExtSource;
		this.attributes = attributes;
	}

	public Candidate(UserExtSource userExtSource, Map<String, String> attributes, List<UserExtSource> additionalUserExtSources) {
		this(userExtSource, attributes);
		this.additionalUserExtSources = additionalUserExtSources;
	}

	public UserExtSource getUserExtSource() {
		return userExtSource;
	}

	public void setUserExtSource(UserExtSource userExtSource) {
		this.userExtSource = userExtSource;
	}

	public Map<String, String> getAttributes() {
		if(attributes == null) return null;
		return Collections.unmodifiableMap(attributes);
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public List<UserExtSource> getAdditionalUserExtSources() {
		if (additionalUserExtSources == null) return null;
		return Collections.unmodifiableList(additionalUserExtSources);
	}

	public void setAdditionalUserExtSources(List<UserExtSource> additionalUserExtSources) {
		this.additionalUserExtSources = additionalUserExtSources;
	}


	// FIXME Temporary method, in the future the candidate shoudl have only this function. userExtSource and additionalUserExtSources will be merged into one userExtSources
	// Beaware that this property is IGNORED in RPC serializers/deserializers
	public List<UserExtSource> getUserExtSources() {
		List<UserExtSource> userExtSources = new ArrayList<UserExtSource>();
		if (this.userExtSource != null) {
			userExtSources.add(this.userExtSource);
		}
		if (this.additionalUserExtSources != null) {
			userExtSources.addAll(this.additionalUserExtSources);
		}
		return Collections.unmodifiableList(userExtSources);
	}

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		String attrNew = BeansUtils.serializeMapToString(attributes);
		List<String> userESNew = new ArrayList<String>();
		List<UserExtSource> userESOld = getAdditionalUserExtSources();
		String sUserESNew;

		if(getAdditionalUserExtSources() == null) sUserESNew = "\\0";
		else {
			for(UserExtSource u: userESOld) {
				userESNew.add(u.serializeToString());
			}
			sUserESNew = userESNew.toString();
		}

		return str.append(this.getClass().getSimpleName()).append(":[" +
			"userExtSource=<").append(getUserExtSource() == null ? "\\0" : getUserExtSource().serializeToString()).append(">" +
			", attributes=<").append(attrNew).append(">" +
			", additionalUserExtSources=<").append(sUserESNew).append(">" +
			']').toString();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		Map<String, String> attrNew = null;
		if(attributes != null) attrNew = new HashMap<String, String>(attributes);
		if(attrNew != null) {
			Set<String> keys = new HashSet<String>(attrNew.keySet());
			for(String s: keys) {
				attrNew.put('\'' + s + '\'', '\'' + attrNew.get(s) + '\'');
				attrNew.remove(s);
			}
		}
		return str.append(getClass().getSimpleName()+":[userExtSource='").append(userExtSource).append("', attributes='"
			+ attrNew).append("', additionalUserExtSources='").append(additionalUserExtSources).append("']").toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attributes == null) ? 0 : attributes.hashCode());
		result = prime * result
				+ ((userExtSource == null) ? 0 : userExtSource.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Candidate other = (Candidate) obj;
		if (attributes == null) {
			if (other.attributes != null) {
				return false;
			}
		} else if (!attributes.equals(other.attributes)) {
			return false;
		}
		if (userExtSource == null) {
			if (other.userExtSource != null) {
				return false;
			}
		} else if (!userExtSource.equals(other.userExtSource)) {
			return false;
		}
		return true;
	}

}
