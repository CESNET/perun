package cz.metacentrum.perun.core.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Member of a Virtual Organization.
 *
 * @author Michal Prochazka michalp@ics.muni.cz
 * @author Martin Kuba makub@ics.muni.cz
 */
public class RichMember extends Member implements Comparable<PerunBean> {
  private User user;
  private List<UserExtSource> userExtSources;
  private List<Attribute> userAttributes;
  private List<Attribute> memberAttributes;

  public RichMember() {
  }

  public RichMember(User user, Member member, List<UserExtSource> userExtSources) {
    super(member.getId(), member.getUserId(), member.getVoId(), member.getStatus(), member.getCreatedAt(),
        member.getCreatedBy(), member.getModifiedAt(), member.getModifiedBy(), member.getCreatedByUid(),
        member.getModifiedByUid());
    setMembershipType(member.getMembershipType());
    setDualMembership(member.isDualMembership());
    setSponsored(member.isSponsored());
    this.setGroupsStatuses(member.getGroupStatuses());
    this.user = user;
    this.userExtSources = userExtSources;
    this.userAttributes = null;
    this.memberAttributes = null;
  }

  public RichMember(User user, Member member, List<UserExtSource> userExtSources, List<Attribute> userAttributes,
                    List<Attribute> memberAttributes) {
    this(user, member, userExtSources);
    this.userAttributes = userAttributes;
    this.memberAttributes = memberAttributes;
  }

  public void addMemberAttribute(Attribute attribute) {
    if (this.memberAttributes == null) {
      this.memberAttributes = new ArrayList<>();
    }
    this.memberAttributes.add(attribute);
  }

  public void addUserAttribute(Attribute attribute) {
    if (this.userAttributes == null) {
      this.userAttributes = new ArrayList<>();
    }
    this.userAttributes.add(attribute);
  }

  public void addUserExtSource(UserExtSource userExtSource) {
    if (this.userExtSources == null) {
      this.userExtSources = new ArrayList<>();
    }
    this.userExtSources.add(userExtSource);
  }

  @Override
  public int compareTo(PerunBean perunBean) {
    if (perunBean == null) {
      throw new NullPointerException("PerunBean to compare with is null.");
    }
    if (perunBean instanceof RichMember) {
      User user = ((RichMember) perunBean).getUser();
      if (this.getUser() == null && user != null) {
        return -1;
      }
      if (user == null && this.getUser() != null) {
        return 1;
      }
      if (this.getUser() == null && user == null) {
        return 0;
      }
      return this.getUser().compareTo(user);
    } else {
      return (this.getId() - perunBean.getId());
    }
  }

  public List<Attribute> getMemberAttributes() {
    return memberAttributes;
  }

  public void setMemberAttributes(List<Attribute> memberAttributes) {
    this.memberAttributes = memberAttributes;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public List<Attribute> getUserAttributes() {
    return userAttributes;
  }

  public void setUserAttributes(List<Attribute> userAttributes) {
    this.userAttributes = userAttributes;
  }

  public List<UserExtSource> getUserExtSources() {
    return userExtSources;
  }

  public void setUserExtSources(List<UserExtSource> userExtSources) {
    this.userExtSources = userExtSources;
  }

  @Override
  public String serializeToString() {
    List<UserExtSource> userESOld = getUserExtSources();
    List<Attribute> memberAttrOld = getMemberAttributes();
    List<Attribute> userAttrOld = getUserAttributes();
    List<String> userESNew = new ArrayList<String>();
    List<String> memberAttrNew = new ArrayList<String>();
    List<String> userAttrNew = new ArrayList<String>();
    String stringUserESNew;
    String stringMemberAttrNew;
    String stringUserAttrNew;

    if (getUserExtSources() == null) {
      stringUserESNew = "\\0";
    } else {
      for (UserExtSource u : userESOld) {
        userESNew.add(u.serializeToString());
      }
      stringUserESNew = userESNew.toString();
    }
    if (getMemberAttributes() == null) {
      stringMemberAttrNew = "\\0";
    } else {
      for (Attribute a : memberAttrOld) {
        memberAttrNew.add(a.serializeToString());
      }
      stringMemberAttrNew = memberAttrNew.toString();
    }
    if (getUserAttributes() == null) {
      stringUserAttrNew = "\\0";
    } else {
      for (Attribute a : userAttrOld) {
        userAttrNew.add(a.serializeToString());
      }
      stringUserAttrNew = userAttrNew.toString();
    }

    return this.getClass().getSimpleName() + ":[" + "id=<" + getId() + ">" + ", userId=<" + getUserId() + ">" +
           ", voId=<" + getVoId() + ">" + ", status=<" +
           (getStatus() == null ? "\\0" : BeansUtils.createEscaping(getStatus().toString())) + ">" + ", type=<" +
           (getMembershipType() == null ? "\\0" : BeansUtils.createEscaping(getMembershipType().toString())) + ">" +
           ", dualMembership=<" + isDualMembership() + ">" +
           ", sourceGroupId=<" + (getSourceGroupId() == null ? "\\0" : getSourceGroupId().toString()) + ">" +
           ", sponsored=<" + isSponsored() + ">" + ", user=<" +
           (getUser() == null ? "\\0" : getUser().serializeToString()) + ">" + ", userExtSources=<" + stringUserESNew +
           ">" + ", userAttributes=<" + stringUserAttrNew + ">" + ", memberAttributes=<" + stringMemberAttrNew + ">" +
           ']';
  }

  @Override
  public String toString() {

    return "RichMember:[id='" + getId() + "', userId='" + getUserId() + "', voId='" + getVoId() + "', status='" +
           getStatus() + "', type='" + getMembershipType() + "', dualMembership='" + isDualMembership() +
               "', sourceGroupId='" + getSourceGroupId() +
           "', sponsored='" + isSponsored() + "', user='" + user + "', userExtSources='" + userExtSources +
           "', userAttributes='" + userAttributes + "', memberAttributes='" + memberAttributes + "']";
  }
}
