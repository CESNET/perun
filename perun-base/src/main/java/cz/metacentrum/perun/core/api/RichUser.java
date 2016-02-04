package cz.metacentrum.perun.core.api;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.BeansUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michal Šťava <stavamichal@gmail.com>
 */
public class RichUser extends User {
	private List<UserExtSource> userExtSources;
	private List<Attribute> userAttributes;

	public RichUser(){
	}

	public RichUser(User user, List<UserExtSource> userExtSources) {
		super(user.getId(), user.getFirstName(), user.getLastName(), user.getMiddleName(),
				user.getTitleBefore(), user.getTitleAfter(), user.getCreatedAt(), user.getCreatedBy(),
				user.getModifiedAt(), user.getModifiedBy(), user.isServiceUser(), user.isSponsoredUser(), user.getCreatedByUid(), user.getModifiedByUid());
		this.userExtSources = userExtSources;
		this.userAttributes = null;
	}

	public RichUser(User user, List<UserExtSource> userExtSources, List<Attribute> userAttributes) {
		this(user, userExtSources);
		this.userAttributes = userAttributes;
	}

	public List<UserExtSource> getUserExtSources() {
		return userExtSources;
	}


	public List<Attribute> getUserAttributes() {
		return userAttributes;
	}

	public void setUserAttributes(List<Attribute> userAttributes) {
		this.userAttributes = userAttributes;
	}

	public void setUserExtSources(List<UserExtSource> userExtSources) {
		this.userExtSources = userExtSources;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
			+ ((userAttributes == null) ? 0 : userAttributes.hashCode());
		result = prime * result
			+ ((userExtSources == null) ? 0 : userExtSources.hashCode());
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
		RichUser other = (RichUser) obj;
		if (getId() != other.getId()) return false;
		if (userAttributes == null) {
			if (other.userAttributes != null) {
				return false;
			}
		} else if (!userAttributes.equals(other.userAttributes)) {
			return false;
		}
		if (userExtSources == null) {
			if (other.userExtSources != null) {
				return false;
			}
		} else if (!userExtSources.equals(other.userExtSources)) {
			return false;
		}
		return true;
	}

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		List<UserExtSource> userESOld = getUserExtSources();
		List<Attribute> userAttrOld = getUserAttributes();
		List<String> userESNew = new ArrayList<String>();
		List<String> userAttrNew = new ArrayList<String>();
		String sUserESNew;
		String sUserAttrNew;

		if(getUserExtSources() == null) sUserESNew = "\\0";
		else {
			for(UserExtSource u: userESOld) {
				userESNew.add(u.serializeToString());
			}
			sUserESNew = userESNew.toString();
		}
		if(getUserAttributes() == null) sUserAttrNew = "\\0";
		else {
			for(Attribute a: userAttrOld) {
				userAttrNew.add(a.serializeToString());
			}
			sUserAttrNew = userAttrNew.toString();
		}
		return str.append(this.getClass().getSimpleName()).append(":[").append(
			"id=<").append(getId()).append(">").append(
			", titleBefore=<").append(getTitleBefore() == null ? "\\0" : BeansUtils.createEscaping(getTitleBefore())).append(">").append(
			", firstName=<").append(getFirstName() == null ? "\\0" : BeansUtils.createEscaping(getFirstName())).append(">").append(
			", lastName=<").append(getLastName() == null ? "\\0" : BeansUtils.createEscaping(getLastName())).append(">").append(
			", middleName=<").append(getMiddleName() == null ? "\\0" : BeansUtils.createEscaping(getMiddleName())).append(">").append(
			", titleAfter=<").append(getTitleAfter() == null ? "\\0" : BeansUtils.createEscaping(getTitleAfter())).append(">").append(
			", userExtSources=<").append(sUserESNew).append(">").append(
			", userAttributes=<").append(sUserAttrNew).append(">").append(
			']').toString();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append("RichUser:[id='").append(getId()).append("', titleBefore='").append(getTitleBefore()).append("', firstName='").append(getFirstName()).append("', lastName='").append(getLastName()).append(
			"', middleName='").append(getMiddleName()).append("', titleAfter='").append(getTitleAfter()).append("', userExtSources='").append(userExtSources).append("', userAttributes='").append(userAttributes).append("']").toString();
	}
}
