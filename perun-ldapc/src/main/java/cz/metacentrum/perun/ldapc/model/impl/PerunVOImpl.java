package cz.metacentrum.perun.ldapc.model.impl;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.ldapc.model.PerunAttribute;
import cz.metacentrum.perun.ldapc.model.PerunUser;
import cz.metacentrum.perun.ldapc.model.PerunVO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.naming.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.support.LdapNameBuilder;

/**
 * Provides implementation of operations to modify the VO entities in the LDAP directory.
 */
public class PerunVOImpl extends AbstractPerunEntry<Vo> implements PerunVO {

  private static final Logger LOG = LoggerFactory.getLogger(PerunVOImpl.class);

  @Autowired
  @Lazy
  private PerunUser user;

  @Override
  public void addMemberToVO(int voId, Member member) {
    DirContextOperations voEntry = findById(String.valueOf(voId));
    Name memberDN = user.getEntryDN(String.valueOf(member.getUserId()));
    voEntry.addAttributeValue(PerunAttribute.PerunAttributeNames.LDAP_ATTR_UNIQUE_MEMBER,
        addBaseDN(memberDN).toString());
    ldapTemplate.modifyAttributes(voEntry);
    DirContextOperations userEntry = findByDN(memberDN);
    userEntry.addAttributeValue(PerunAttribute.PerunAttributeNames.LDAP_ATTR_MEMBER_OF_PERUN_VO, String.valueOf(voId));
    ldapTemplate.modifyAttributes(userEntry);
  }

  public void addVo(Vo vo) {
    addEntry(vo);
  }

  @Override
  protected Name buildDN(Vo bean) {
    return getEntryDN(String.valueOf(bean.getId()));
  }

  @Override
  public void deleteEntry(Name dn) {
    // first find and remove entries in subtree
    List<Name> subentries = ldapTemplate.search(
        query().base(dn).where("objectclass").not().is(PerunAttribute.PerunAttributeNames.OBJECT_CLASS_PERUN_VO),
        getNameMapper());
    for (Name entrydn : subentries) {
      ldapTemplate.unbind(entrydn);
    }
    // then remove this entry
    super.deleteEntry(dn);
  }

  public void deleteVo(Vo vo) {
    deleteEntry(vo);
  }

  protected void doSynchronizeMembers(DirContextOperations voEntry, List<Member> members) {
    List<Name> memberList = new ArrayList<Name>(members.size());
    for (Member member : members) {
      memberList.add(addBaseDN(user.getEntryDN(String.valueOf(member.getUserId()))));
    }
    voEntry.setAttributeValues(PerunAttribute.PerunAttributeNames.LDAP_ATTR_UNIQUE_MEMBER,
        memberList.stream().map(name -> name.toString()).toArray(String[]::new));
  }

  @Override
  protected List<PerunAttribute<Vo>> getDefaultAttributeDescriptions() {
    return Arrays.asList(
        new PerunAttributeDesc<>(PerunAttribute.PerunAttributeNames.LDAP_ATTR_ORGANIZATION, PerunAttribute.REQUIRED,
            (PerunAttribute.SingleValueExtractor<Vo>) (vo, attrs) -> vo.getShortName()),
        new PerunAttributeDesc<>(PerunAttribute.PerunAttributeNames.LDAP_ATTR_DESCRIPTION, PerunAttribute.REQUIRED,
            (PerunAttribute.SingleValueExtractor<Vo>) (vo, attrs) -> vo.getName()),
        new PerunAttributeDesc<>(PerunAttribute.PerunAttributeNames.LDAP_ATTR_PERUN_VO_ID, PerunAttribute.REQUIRED,
            (PerunAttribute.SingleValueExtractor<Vo>) (vo, attrs) -> String.valueOf(vo.getId())));
  }

  @Override
  protected List<String> getDefaultUpdatableAttributes() {
    return Arrays.asList(PerunAttribute.PerunAttributeNames.LDAP_ATTR_DESCRIPTION);
  }

  @Override
  public Name getEntryDN(String... voId) {
    return LdapNameBuilder.newInstance().add(PerunAttribute.PerunAttributeNames.LDAP_ATTR_PERUN_VO_ID, voId[0]).build();
  }

  public String getVoShortName(int voId) {
    DirContextOperations voEntry = findById(String.valueOf(voId));
    String[] voShortNameInformation =
        voEntry.getStringAttributes(PerunAttribute.PerunAttributeNames.LDAP_ATTR_ORGANIZATION);
    String voShortName = null;
    if (voShortNameInformation == null || voShortNameInformation[0] == null) {
      throw new InternalErrorException("There is no shortName in ldap for vo with id=" + voId);
    }
    if (voShortNameInformation.length != 1) {
      throw new InternalErrorException(
          "There is not exactly one short name of vo with id=" + voId + " in ldap. Count of shortnames is " +
          voShortNameInformation.length);
    }
    voShortName = voShortNameInformation[0];
    return voShortName;
  }

  @Override
  public List<Name> listEntries() {
    return ldapTemplate.search(
        query().where("objectclass").is(PerunAttribute.PerunAttributeNames.OBJECT_CLASS_PERUN_VO), getNameMapper());
  }

  @Override
  protected void mapToContext(Vo bean, DirContextOperations context) {
    context.setAttributeValues("objectclass", Arrays.asList(PerunAttribute.PerunAttributeNames.OBJECT_CLASS_PERUN_VO,
        PerunAttribute.PerunAttributeNames.OBJECT_CLASS_ORGANIZATION).toArray());
    mapToContext(bean, context, getAttributeDescriptions());
  }

  @Override
  public void removeMemberFromVO(int voId, Member member) {
    DirContextOperations voEntry = findById(String.valueOf(voId));
    Name memberDN = user.getEntryDN(String.valueOf(member.getUserId()));
    voEntry.removeAttributeValue(PerunAttribute.PerunAttributeNames.LDAP_ATTR_UNIQUE_MEMBER,
        addBaseDN(memberDN).toString());
    ldapTemplate.modifyAttributes(voEntry);
    DirContextOperations userEntry = findByDN(memberDN);
    userEntry.removeAttributeValue(PerunAttribute.PerunAttributeNames.LDAP_ATTR_MEMBER_OF_PERUN_VO,
        String.valueOf(voId));
    ldapTemplate.modifyAttributes(userEntry);
  }

  @Override
  public void synchronizeMembers(Vo vo, List<Member> members) {
    DirContextOperations voEntry = findByDN(buildDN(vo));
    doSynchronizeMembers(voEntry, members);
    ldapTemplate.modifyAttributes(voEntry);
    // user attributes are set when synchronizing users
  }

  @Override
  public void synchronizeVo(Vo vo, Iterable<Attribute> attrs, List<Member> members) {
    SyncOperation syncOp = beginSynchronizeEntry(vo, attrs);
    doSynchronizeMembers(syncOp.getEntry(), members);
    commitSyncOperation(syncOp);
  }

  @Override
  public void updateVo(Vo vo) {
    modifyEntry(vo);
  }

}
