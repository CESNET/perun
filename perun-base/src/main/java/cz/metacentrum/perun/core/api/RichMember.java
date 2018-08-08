package cz.metacentrum.perun.core.api;

import java.util.List;
import java.util.ArrayList;

/**
 * Member of a Virtual Organization.
 * @author Michal Prochazka michalp@ics.muni.cz
 * @author Martin Kuba makub@ics.muni.cz
 */
public class RichMember extends Member implements Comparable<PerunBean> {
	private User user;
	private List<UserExtSource> userExtSources;
	private List<Attribute> userAttributes;
	private List<Attribute> memberAttributes;

	public RichMember(){
	}

	public RichMember(User user, Member member, List<UserExtSource> userExtSources) {
		super(member.getId(), member.getUserId(), member.getVoId(), member.getStatus(), member.getCreatedAt(),
				member.getCreatedBy(), member.getModifiedAt(), member.getModifiedBy(),
				member.getCreatedByUid(), member.getModifiedByUid());
		setMembershipType(member.getMembershipType());
		setSponsored(member.isSponsored());
		this.user = user;
		this.userExtSources = userExtSources;
		this.userAttributes = null;
		this.memberAttributes = null;
	}

	public RichMember(User user, Member member, List<UserExtSource> userExtSources, List<Attribute> userAttributes, List<Attribute> memberAttributes) {
		this(user, member, userExtSources);
		this.userAttributes = userAttributes;
		this.memberAttributes = memberAttributes;
	}

	public User getUser() {
		return user;
	}

	public List<UserExtSource> getUserExtSources() {
		return userExtSources;
	}


	public List<Attribute> getUserAttributes() {
		return userAttributes;
	}

	public List<Attribute> getMemberAttributes() {
		return memberAttributes;
	}

	public void setUserAttributes(List<Attribute> userAttributes) {
		this.userAttributes = userAttributes;
	}

	public void setMemberAttributes(List<Attribute> memberAttributes) {
		this.memberAttributes = memberAttributes;
	}

	public void setUserExtSources(List<UserExtSource> userExtSources) {
		this.userExtSources = userExtSources;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String serializeToString() {
		StringBuilder str = new StringBuilder();

		List<UserExtSource> userESOld = getUserExtSources();
		List<Attribute> memberAttrOld = getMemberAttributes();
		List<Attribute> userAttrOld = getUserAttributes();
		List<String> userESNew = new ArrayList<String>();
		List<String> memberAttrNew = new ArrayList<String>();
		List<String> userAttrNew = new ArrayList<String>();
		String sUserESNew;
		String sMemberAttrNew;
		String sUserAttrNew;

		if(getUserExtSources() == null) sUserESNew = "\\0";
		else {
			for(UserExtSource u: userESOld) {
				userESNew.add(u.serializeToString());
			}
			sUserESNew = userESNew.toString();
		}
		if(getMemberAttributes() == null) sMemberAttrNew = "\\0";
		else {
			for(Attribute a: memberAttrOld) {
				memberAttrNew.add(a.serializeToString());
			}
			sMemberAttrNew = memberAttrNew.toString();
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
			", userId=<").append(getUserId()).append(">").append(
			", voId=<").append(getVoId()).append(">").append(
			", status=<").append(getStatus() == null ? "\\0" : BeansUtils.createEscaping(getStatus().toString())).append(">").append(
			", type=<").append(getMembershipType()== null ? "\\0" : BeansUtils.createEscaping(getMembershipType().toString())).append(">").append(
			", sourceGroupId=<").append(getSourceGroupId()== null ? "\\0" : getSourceGroupId().toString()).append(">").append(
			", sponsored=<").append(isSponsored()).append(">").append(
			", user=<").append(getUser() == null ? "\\0" : getUser().serializeToString()).append(">").append(
			", userExtSources=<").append(sUserESNew).append(">").append(
			", userAttributes=<").append(sUserAttrNew).append(">").append(
			", memberAttributes=<").append(sMemberAttrNew).append(">").append(
			']').toString();
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		return str.append("RichMember:[id='").append(getId()).append("', userId='").append(getUserId()).append("', voId='").append(getVoId()).append("', status='").append(getStatus()).append("', type='").append(getMembershipType()).append("', sourceGroupId='").append(getSourceGroupId()).append("', sponsored='").append(isSponsored()).append( "', user='").append(user).append("', userExtSources='").append(userExtSources).append(
			"', userAttributes='").append(userAttributes).append("', memberAttributes='").append(memberAttributes).append("']").toString();
	}

	@Override
	public int compareTo(PerunBean perunBean) {
		if(perunBean == null) throw new NullPointerException("PerunBean to compare with is null.");
		if(perunBean instanceof RichMember) {
			User user = ((RichMember) perunBean).getUser();
			if (this.getUser()== null && user != null) return -1;
			if (user == null && this.getUser() != null) return 1;
			if (this.getUser()== null && user == null) return 0;
			return this.getUser().compareTo(user);
		} else {
			return (this.getId() - perunBean.getId());
		}
	}
}
