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
		super(user.getId(), user.getUuid(), user.getFirstName(), user.getLastName(), user.getMiddleName(),
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
			", uuid=<").append(getUuid()).append(">").append(
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

		return str.append("RichUser:[id='").append(getId()).append("', uuid='").append(getUuid()).append("', titleBefore='").append(getTitleBefore()).append("', firstName='").append(getFirstName()).append("', lastName='").append(getLastName()).append(
			"', middleName='").append(getMiddleName()).append("', titleAfter='").append(getTitleAfter()).append("', userExtSources='").append(userExtSources).append("', userAttributes='").append(userAttributes).append("']").toString();
	}
}
