package cz.metacentrum.perun.ldapc.model.impl;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.ldapc.model.PerunAttribute;
import cz.metacentrum.perun.ldapc.model.PerunFacility;
import cz.metacentrum.perun.ldapc.model.PerunGroup;
import cz.metacentrum.perun.ldapc.model.PerunUser;
import cz.metacentrum.perun.ldapc.model.PerunVO;
import cz.metacentrum.perun.ldapc.service.LdapcManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.naming.Name;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.support.LdapNameBuilder;

/**
 * Provides implementation of operations to modify the User entities in the LDAP directory.
 */
public class PerunUserImpl extends AbstractPerunEntry<User> implements PerunUser {

  private static final Logger LOG = LoggerFactory.getLogger(PerunUserImpl.class);

  private static final Pattern EPPN_EPUID_PATTERN = Pattern.compile("[^@]+@[^@]+");
  @Autowired
  protected LdapcManager ldapcManager;
  @Autowired
  private PerunGroup perunGroup;
  @Autowired
  private PerunVO perunVO;
  @Autowired
  private PerunFacility perunFacility;

  @Override
  public void addAsFacilityAdmin(User user, Facility facility) {
    DirContextOperations entry = findByDN(buildDN(user));
    Name facilityDN = addBaseDN(perunFacility.getEntryDN(String.valueOf(facility.getId())));
    entry.addAttributeValue(PerunAttribute.PerunAttributeNames.LDAP_ATTR_ADMIN_OF_FACILITY, facilityDN.toString());
    ldapTemplate.modifyAttributes(entry);
  }

  @Override
  public void addAsGroupAdmin(User user, Group group) {
    DirContextOperations entry = findByDN(buildDN(user));
    Name groupDN = addBaseDN(perunGroup.getEntryDN(String.valueOf(group.getVoId()), String.valueOf(group.getId())));
    entry.addAttributeValue(PerunAttribute.PerunAttributeNames.LDAP_ATTR_ADMIN_OF_GROUP, groupDN.toString());
    ldapTemplate.modifyAttributes(entry);
  }

  @Override
  public void addAsVoAdmin(User user, Vo vo) {
    DirContextOperations entry = findByDN(buildDN(user));
    Name voDN = addBaseDN(perunVO.getEntryDN(String.valueOf(vo.getId())));
    entry.addAttributeValue(PerunAttribute.PerunAttributeNames.LDAP_ATTR_ADMIN_OF_VO, voDN.toString());
    ldapTemplate.modifyAttributes(entry);
  }

  @Override
  public void addPrincipal(User user, String login) {
    DirContextOperations entry = findByDN(buildDN(user));

    if (isEppnEpuidLogin(login)) {

      String[] oldEppns =
          entry.getStringAttributes(PerunAttribute.PerunAttributeNames.LDAP_ATTR_EDU_PERSON_PRINCIPAL_NAMES);
      if (oldEppns == null) {
        oldEppns = new String[0];
      }
      TreeSet<String> uniqueEppns = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
      uniqueEppns.addAll(Stream.of(oldEppns).collect(Collectors.toSet()));

      if (uniqueEppns.contains(login)) {
        LOG.debug("Same eduPersonPrincipalNames '{}' is already present in entry {}, skipping.", login, buildDN(user));
      } else {
        entry.addAttributeValue(PerunAttribute.PerunAttributeNames.LDAP_ATTR_EDU_PERSON_PRINCIPAL_NAMES, login);
      }

    }

    String[] oldIdentities = entry.getStringAttributes(PerunAttribute.PerunAttributeNames.LDAP_ATTR_USER_IDENTITIES);
    if (oldIdentities == null) {
      oldIdentities = new String[0];
    }
    TreeSet<String> uniqueIdentities = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    uniqueIdentities.addAll(Stream.of(oldIdentities).collect(Collectors.toSet()));

    if (uniqueIdentities.contains(login)) {
      LOG.debug("Same userIdentities '{}' is already present in entry {}, skipping.", login, buildDN(user));
    } else {
      entry.addAttributeValue(PerunAttribute.PerunAttributeNames.LDAP_ATTR_USER_IDENTITIES, login);
    }

    ldapTemplate.modifyAttributes(entry);

  }

  public void addUser(User user) {
    addEntry(user);
  }

  @Override
  protected Name buildDN(User bean) {
    return getEntryDN(String.valueOf(bean.getId()));
  }

  public void deleteUser(User user) {
    deleteEntry(user);
  }

  private void doSynchronizeAdminRoles(DirContextOperations entry, List<Group> adminGroups, List<Vo> adminVos,
                                       List<Facility> adminFacilities) {
    entry.setAttributeValues(PerunAttribute.PerunAttributeNames.LDAP_ATTR_ADMIN_OF_GROUP, adminGroups.stream()
        .map(group -> addBaseDN(perunGroup.getEntryDN(String.valueOf(group.getVoId()), String.valueOf(group.getId()))))
        .toArray(Name[]::new));
    entry.setAttributeValues(PerunAttribute.PerunAttributeNames.LDAP_ATTR_ADMIN_OF_VO,
        adminVos.stream().map(vo -> addBaseDN(perunVO.getEntryDN(String.valueOf(vo.getId())))).toArray(Name[]::new));
    entry.setAttributeValues(PerunAttribute.PerunAttributeNames.LDAP_ATTR_ADMIN_OF_FACILITY,
        adminFacilities.stream().map(facility -> addBaseDN(perunFacility.getEntryDN(String.valueOf(facility.getId()))))
            .toArray(Name[]::new));
  }

  protected void doSynchronizeMembership(DirContextOperations entry, Set<Integer> voIds, List<Group> groups) {
    entry.setAttributeValues(PerunAttribute.PerunAttributeNames.LDAP_ATTR_MEMBER_OF_PERUN_VO,
        voIds.stream().map(id -> String.valueOf(id)).toArray(String[]::new));
    List<Name> memberOfNames = new ArrayList<Name>();
    for (Group group : groups) {
      memberOfNames.add(
          addBaseDN(perunGroup.getEntryDN(String.valueOf(group.getVoId()), String.valueOf(group.getId()))));
    }
    entry.setAttributeValues(PerunAttribute.PerunAttributeNames.LDAP_ATTR_MEMBER_OF, memberOfNames.toArray());
  }

  protected void doSynchronizePrincipals(DirContextOperations entry, List<UserExtSource> extSources) {

    // make sure EPPNs and identities are case-ignore unique for LDAP (we do not have them case-ignore unique in perun).
    TreeSet<String> uniqueEppns = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    uniqueEppns.addAll(
        extSources.stream().filter(this::isIdpUes).map(UserExtSource::getLogin).filter(this::isEppnEpuidLogin)
            .collect(Collectors.toSet()));
    entry.setAttributeValues(PerunAttribute.PerunAttributeNames.LDAP_ATTR_EDU_PERSON_PRINCIPAL_NAMES,
        uniqueEppns.toArray(String[]::new));

    TreeSet<String> uniqueIdentities = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    uniqueIdentities.addAll(
        extSources.stream().filter(this::isIdpUes).map(UserExtSource::getLogin).collect(Collectors.toSet()));
    entry.setAttributeValues(PerunAttribute.PerunAttributeNames.LDAP_ATTR_USER_IDENTITIES,
        uniqueIdentities.toArray(String[]::new));

  }

  @Override
  protected List<PerunAttribute<User>> getDefaultAttributeDescriptions() {
    return Arrays.asList(
        new PerunAttributeDesc<>(PerunAttribute.PerunAttributeNames.LDAP_ATTR_ENTRY_STATUS, PerunAttribute.REQUIRED,
            (PerunAttribute.SingleValueExtractor<User>) (user, attrs) -> "active"),
        new PerunAttributeDesc<>(PerunAttribute.PerunAttributeNames.LDAP_ATTR_SURNAME, PerunAttribute.REQUIRED,
            (PerunAttribute.SingleValueExtractor<User>) (user, attrs) -> (StringUtils.isBlank(user.getLastName()) ?
                "N/A" : user.getLastName())),
        new PerunAttributeDesc<>(PerunAttribute.PerunAttributeNames.LDAP_ATTR_GIVEN_NAME, PerunAttribute.OPTIONAL,
            (PerunAttribute.SingleValueExtractor<User>) (user, attrs) -> user.getFirstName()),
        new PerunAttributeDesc<>(PerunAttribute.PerunAttributeNames.LDAP_ATTR_COMMON_NAME, PerunAttribute.REQUIRED,
            (PerunAttribute.SingleValueExtractor<User>) (user, attrs) -> {
              String firstName = user.getFirstName();
              String lastName = user.getLastName();
              String commonName = "";
              if (firstName == null || firstName.isEmpty()) {
                firstName = "";
              } else {
                commonName += firstName + " ";
              }
              if (lastName == null || lastName.isEmpty()) {
                lastName = "N/A";
              }
              commonName += lastName;
              return commonName;
            }),
        new PerunAttributeDesc<>(PerunAttribute.PerunAttributeNames.LDAP_ATTR_DISPLAY_NAME, PerunAttribute.OPTIONAL,
            (PerunAttribute.SingleValueExtractor<User>) (user, attrs) -> {
              String displayName = user.getDisplayName();
              if (StringUtils.isBlank(displayName)) {
                return null;
              } else {
                return displayName;
              }
            }),
        new PerunAttributeDesc<>(PerunAttribute.PerunAttributeNames.LDAP_ATTR_PERUN_USER_ID, PerunAttribute.REQUIRED,
            (PerunAttribute.SingleValueExtractor<User>) (user, attrs) -> String.valueOf(user.getId())),
        new PerunAttributeDesc<>(PerunAttribute.PerunAttributeNames.LDAP_ATTR_IS_SERVICE_USER, PerunAttribute.REQUIRED,
            (PerunAttribute.SingleValueExtractor<User>) (user, attrs) -> user.isServiceUser() ? "1" : "0"),
        new PerunAttributeDesc<>(PerunAttribute.PerunAttributeNames.LDAP_ATTR_IS_SPONSORED_USER,
            PerunAttribute.REQUIRED,
            (PerunAttribute.SingleValueExtractor<User>) (user, attrs) -> user.isSponsoredUser() ? "1" : "0"),
        new PerunAttributeDesc<>(PerunAttribute.PerunAttributeNames.LDAP_ATTR_UUID, PerunAttribute.OPTIONAL,
            (PerunAttribute.SingleValueExtractor<User>) (user, attrs) -> user.getUuid().toString()));
  }

  @Override
  protected List<String> getDefaultUpdatableAttributes() {
    return Arrays.asList(PerunAttribute.PerunAttributeNames.LDAP_ATTR_SURNAME,
        PerunAttribute.PerunAttributeNames.LDAP_ATTR_COMMON_NAME,
        PerunAttribute.PerunAttributeNames.LDAP_ATTR_GIVEN_NAME,
        PerunAttribute.PerunAttributeNames.LDAP_ATTR_DISPLAY_NAME);
  }

  /**
   * Get User DN using user id.
   *
   * @param userId user id
   * @return DN in Name
   */
  @Override
  public Name getEntryDN(String... userId) {
    return LdapNameBuilder.newInstance().add(PerunAttribute.PerunAttributeNames.ORGANIZATIONAL_UNIT_PEOPLE)
        .add(PerunAttribute.PerunAttributeNames.LDAP_ATTR_PERUN_USER_ID, userId[0]).build();
  }

  private boolean isEppnEpuidLogin(String login) {
    return login != null && EPPN_EPUID_PATTERN.matcher(login).matches();
  }

  private boolean isIdpUes(UserExtSource ues) {
    return ues != null && ues.getExtSource() != null && ues.getExtSource().getType() != null &&
           ues.getExtSource().getType().equals(ExtSourcesManager.EXTSOURCE_IDP);
  }

  @Override
  public List<Name> listEntries() {
    return ldapTemplate.search(query().where("objectclass").is(PerunAttribute.PerunAttributeNames.OBJECT_CLASS_PERSON),
        getNameMapper());
  }

  @Override
  protected void mapToContext(User bean, DirContextOperations context) {
    context.setAttributeValues(PerunAttribute.PerunAttributeNames.LDAP_ATTR_OBJECT_CLASS,
        Arrays.asList(PerunAttribute.PerunAttributeNames.OBJECT_CLASS_PERSON,
            PerunAttribute.PerunAttributeNames.OBJECT_CLASS_ORGANIZATIONAL_PERSON,
            PerunAttribute.PerunAttributeNames.OBJECT_CLASS_INET_ORG_PERSON,
            PerunAttribute.PerunAttributeNames.OBJECT_CLASS_PERUN_USER,
            PerunAttribute.PerunAttributeNames.OBJECT_CLASS_TEN_OPER_ENTRY,
            PerunAttribute.PerunAttributeNames.OBJECT_CLASS_INET_USER).toArray());
    mapToContext(bean, context, getAttributeDescriptions());
  }

  @Override
  public void removeFromFacilityAdmins(User user, Facility facility) {
    DirContextOperations entry = findByDN(buildDN(user));
    Name facilityDN = addBaseDN(perunFacility.getEntryDN(String.valueOf(facility.getId())));
    entry.removeAttributeValue(PerunAttribute.PerunAttributeNames.LDAP_ATTR_ADMIN_OF_FACILITY, facilityDN.toString());
    ldapTemplate.modifyAttributes(entry);
  }

  @Override
  public void removeFromGroupAdmins(User user, Group group) {
    DirContextOperations entry = findByDN(buildDN(user));
    Name groupDN = addBaseDN(perunGroup.getEntryDN(String.valueOf(group.getVoId()), String.valueOf(group.getId())));
    entry.removeAttributeValue(PerunAttribute.PerunAttributeNames.LDAP_ATTR_ADMIN_OF_GROUP, groupDN.toString());
    ldapTemplate.modifyAttributes(entry);
  }

  @Override
  public void removeFromVoAdmins(User user, Vo vo) {
    DirContextOperations entry = findByDN(buildDN(user));
    Name voDN = addBaseDN(perunVO.getEntryDN(String.valueOf(vo.getId())));
    entry.removeAttributeValue(PerunAttribute.PerunAttributeNames.LDAP_ATTR_ADMIN_OF_VO, voDN.toString());
    ldapTemplate.modifyAttributes(entry);
  }

  @Override
  public void removePrincipal(User user, String login) {

    // Because of the different case equality in LDAP and Perun we can't simply remove passed "login" value from the
    // entry.
    // We perform full sync instead, since we would still have to query perun for all the values to check on.
    List<UserExtSource> currentUeses = ((PerunBl) ldapcManager.getPerunBl()).getUsersManagerBl()
        .getUserExtSources(ldapcManager.getPerunSession(), user);
    LOG.debug("Synchronize all principals on removal of ExtSource login '{}' from {}.", login, user);
    synchronizePrincipals(user, currentUeses);
    /*
        DirContextOperations entry = findByDN(buildDN(user));
        if (isEppnEpuidLogin(login)) {
            entry.removeAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrEduPersonPrincipalNames, login);
        }
        entry.removeAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrUserIdentities, login);
        ldapTemplate.modifyAttributes(entry);
        */

  }

  @Override
  public void synchronizeAdminRoles(User user, List<Group> adminGroups, List<Vo> adminVos,
                                    List<Facility> adminFacilities) {
    DirContextOperations entry = findByDN(buildDN(user));
    doSynchronizeAdminRoles(entry, adminGroups, adminVos, adminFacilities);
    ldapTemplate.modifyAttributes(entry);
  }

  @Override
  public void synchronizeMembership(User user, Set<Integer> voIds, List<Group> groups) {
    DirContextOperations entry = findByDN(buildDN(user));
    doSynchronizeMembership(entry, voIds, groups);
    ldapTemplate.modifyAttributes(entry);
  }

  @Override
  public void synchronizePrincipals(User user, List<UserExtSource> extSources) {
    DirContextOperations entry = findByDN(buildDN(user));
    doSynchronizePrincipals(entry, extSources);
    ldapTemplate.modifyAttributes(entry);
  }

  @Override
  public void synchronizeUser(User user, Iterable<Attribute> attrs, Set<Integer> voIds, List<Group> groups,
                              List<UserExtSource> extSources, List<Group> adminGroups, List<Vo> adminVos,
                              List<Facility> adminFacilities) {
    SyncOperation syncOp = beginSynchronizeEntry(user, attrs);
    doSynchronizeMembership(syncOp.getEntry(), voIds, groups);
    doSynchronizePrincipals(syncOp.getEntry(), extSources);
    doSynchronizeAdminRoles(syncOp.getEntry(), adminGroups, adminVos, adminFacilities);
    commitSyncOperation(syncOp);
    //ldapTemplate.modifyAttributes(entry);
  }

  @Override
  public void updateUser(User user) {
    modifyEntry(user);
  }

  public boolean userPasswordExists(User user) {
    return entryAttributeExists(user, PerunAttribute.PerunAttributeNames.LDAP_ATTR_USER_PASSWORD);
  }
}
