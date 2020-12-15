package cz.metacentrum.perun.ldapc.model.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.ldapc.model.PerunAttribute;
import cz.metacentrum.perun.ldapc.model.PerunFacility;
import cz.metacentrum.perun.ldapc.model.PerunGroup;
import cz.metacentrum.perun.ldapc.model.PerunUser;
import cz.metacentrum.perun.ldapc.model.PerunVO;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

public class PerunUserImpl extends AbstractPerunEntry<User> implements PerunUser {

	private final static Logger log = LoggerFactory.getLogger(PerunUserImpl.class);

	private static final Pattern EPPN_EPUID_PATTERN = Pattern.compile("[^@]+@[^@]+");

	@Autowired
	private PerunGroup perunGroup;
	@Autowired
	private PerunVO perunVO;
	@Autowired
	private PerunFacility perunFacility;

	@Override
	protected List<String> getDefaultUpdatableAttributes() {
		return Arrays.asList(
				PerunAttribute.PerunAttributeNames.ldapAttrSurname,
				PerunAttribute.PerunAttributeNames.ldapAttrCommonName,
				PerunAttribute.PerunAttributeNames.ldapAttrGivenName,
				PerunAttribute.PerunAttributeNames.ldapAttrDisplayName
		);
	}

	@Override
	protected List<PerunAttribute<User>> getDefaultAttributeDescriptions() {
		return Arrays.asList(
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrEntryStatus,
						PerunAttribute.REQUIRED,
						(PerunAttribute.SingleValueExtractor<User>) (user, attrs) -> "active"
				),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrSurname,
						PerunAttribute.REQUIRED,
						(PerunAttribute.SingleValueExtractor<User>) (user, attrs) -> (StringUtils.isBlank(user.getLastName()) ? "N/A" : user.getLastName())
				),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrGivenName,
						PerunAttribute.OPTIONAL,
						(PerunAttribute.SingleValueExtractor<User>) (user, attrs) -> user.getFirstName()
				),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrCommonName,
						PerunAttribute.REQUIRED,
						(PerunAttribute.SingleValueExtractor<User>) (user, attrs) -> {
							String firstName = user.getFirstName();
							String lastName = user.getLastName();
							String commonName = "";
							if (firstName == null || firstName.isEmpty()) firstName = "";
							else commonName += firstName + " ";
							if (lastName == null || lastName.isEmpty()) lastName = "N/A";
							commonName += lastName;
							return commonName;
						}
				),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrDisplayName,
						PerunAttribute.OPTIONAL,
						(PerunAttribute.SingleValueExtractor<User>) (user, attrs) -> {
							String displayName = user.getDisplayName();
							if (StringUtils.isBlank(displayName)) {
								return null;
							} else {
								return displayName;
							}
						}
				),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrPerunUserId,
						PerunAttribute.REQUIRED,
						(PerunAttribute.SingleValueExtractor<User>) (user, attrs) -> String.valueOf(user.getId())
				),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrIsServiceUser,
						PerunAttribute.REQUIRED,
						(PerunAttribute.SingleValueExtractor<User>) (user, attrs) -> user.isServiceUser() ? "1" : "0"
				),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrIsSponsoredUser,
						PerunAttribute.REQUIRED,
						(PerunAttribute.SingleValueExtractor<User>) (user, attrs) -> user.isSponsoredUser() ? "1" : "0"
				),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrUuid,
						PerunAttribute.REQUIRED,
						(PerunAttribute.SingleValueExtractor<User>) (user, attrs) -> user.getUuid().toString()
				)
		);
	}

	public void addUser(User user) {
		addEntry(user);
	}

	public void deleteUser(User user) {
		deleteEntry(user);
	}

	@Override
	public void updateUser(User user) {
		modifyEntry(user);
	}

	public boolean userPasswordExists(User user) {
		return entryAttributeExists(user, PerunAttribute.PerunAttributeNames.ldapAttrUserPassword);
	}

	@Override
	public void addPrincipal(User user, String login) {
		DirContextOperations entry = findByDN(buildDN(user));

		if (isEppnEpuidLogin(login)) {
			entry.addAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrEduPersonPrincipalNames, login);
		}

		entry.addAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrUserIdentities, login);
		ldapTemplate.modifyAttributes(entry);
	}

	@Override
	public void removePrincipal(User user, String login) {
		DirContextOperations entry = findByDN(buildDN(user));

		if (isEppnEpuidLogin(login)) {
			entry.removeAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrEduPersonPrincipalNames, login);
		}

		entry.removeAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrUserIdentities, login);
		ldapTemplate.modifyAttributes(entry);
	}

	@Override
	public void addAsVoAdmin(User user, Vo vo) {
		DirContextOperations entry = findByDN(buildDN(user));
		Name voDN = addBaseDN(perunVO.getEntryDN(String.valueOf(vo.getId())));
		entry.addAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrAdminOfVo, voDN.toString());
		ldapTemplate.modifyAttributes(entry);
	}

	@Override
	public void removeFromVoAdmins(User user, Vo vo) {
		DirContextOperations entry = findByDN(buildDN(user));
		Name voDN = addBaseDN(perunVO.getEntryDN(String.valueOf(vo.getId())));
		entry.removeAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrAdminOfVo, voDN.toString());
		ldapTemplate.modifyAttributes(entry);
	}

	@Override
	public void addAsGroupAdmin(User user, Group group) {
		DirContextOperations entry = findByDN(buildDN(user));
		Name groupDN = addBaseDN(perunGroup.getEntryDN(String.valueOf(group.getVoId()), String.valueOf(group.getId())));
		entry.addAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrAdminOfGroup, groupDN.toString());
		ldapTemplate.modifyAttributes(entry);
	}

	@Override
	public void removeFromGroupAdmins(User user, Group group) {
		DirContextOperations entry = findByDN(buildDN(user));
		Name groupDN = addBaseDN(perunGroup.getEntryDN(String.valueOf(group.getVoId()), String.valueOf(group.getId())));
		entry.removeAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrAdminOfGroup, groupDN.toString());
		ldapTemplate.modifyAttributes(entry);
	}

	@Override
	public void addAsFacilityAdmin(User user, Facility facility) {
		DirContextOperations entry = findByDN(buildDN(user));
		Name facilityDN = addBaseDN(perunFacility.getEntryDN(String.valueOf(facility.getId())));
		entry.addAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrAdminOfFacility, facilityDN.toString());
		ldapTemplate.modifyAttributes(entry);
	}

	@Override
	public void removeFromFacilityAdmins(User user, Facility facility) {
		DirContextOperations entry = findByDN(buildDN(user));
		Name facilityDN = addBaseDN(perunFacility.getEntryDN(String.valueOf(facility.getId())));
		entry.removeAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrAdminOfFacility, facilityDN.toString());
		ldapTemplate.modifyAttributes(entry);
	}

	protected void doSynchronizeMembership(DirContextOperations entry, Set<Integer> voIds, List<Group> groups) {
		entry.setAttributeValues(PerunAttribute.PerunAttributeNames.ldapAttrMemberOfPerunVo, voIds.stream().map(id -> String.valueOf(id)).toArray(String[]::new));
		List<Name> memberOfNames = new ArrayList<Name>();
		for (Group group : groups) {
			memberOfNames.add(addBaseDN(perunGroup.getEntryDN(String.valueOf(group.getVoId()), String.valueOf(group.getId()))));
		}
		entry.setAttributeValues(PerunAttribute.PerunAttributeNames.ldapAttrMemberOf, memberOfNames.toArray());
	}

	protected void doSynchronizePrincipals(DirContextOperations entry, List<UserExtSource> extSources) {
		entry.setAttributeValues(PerunAttribute.PerunAttributeNames.ldapAttrEduPersonPrincipalNames,
				extSources.stream()
						.filter(this::isIdpUes)
						.map(UserExtSource::getLogin)
						.filter(this::isEppnEpuidLogin)
						.toArray(String[]::new)
		);
		entry.setAttributeValues(PerunAttribute.PerunAttributeNames.ldapAttrUserIdentities,
				extSources.stream()
						.filter(this::isIdpUes)
						.map(UserExtSource::getLogin)
						.toArray(String[]::new));
	}

	private void doSynchronizeAdminRoles(DirContextOperations entry, List<Group> admin_groups, List<Vo> admin_vos, List<Facility> admin_facilities) {
		entry.setAttributeValues(PerunAttribute.PerunAttributeNames.ldapAttrAdminOfGroup,
				admin_groups.stream()
						.map(group -> addBaseDN(perunGroup.getEntryDN(String.valueOf(group.getVoId()), String.valueOf(group.getId()))))
						.toArray(Name[]::new)
		);
		entry.setAttributeValues(PerunAttribute.PerunAttributeNames.ldapAttrAdminOfVo,
				admin_vos.stream()
						.map(vo -> addBaseDN(perunVO.getEntryDN(String.valueOf(vo.getId()))))
						.toArray(Name[]::new)
		);
		entry.setAttributeValues(PerunAttribute.PerunAttributeNames.ldapAttrAdminOfFacility,
				admin_facilities.stream()
						.map(facility -> addBaseDN(perunFacility.getEntryDN(String.valueOf(facility.getId()))))
						.toArray(Name[]::new)
		);
	}

	@Override
	public void synchronizeUser(User user, Iterable<Attribute> attrs, Set<Integer> voIds, List<Group> groups,
	                            List<UserExtSource> extSources,
	                            List<Group> admin_groups, List<Vo> admin_vos, List<Facility> admin_facilities) {
		SyncOperation syncOp = beginSynchronizeEntry(user, attrs);
		doSynchronizeMembership(syncOp.getEntry(), voIds, groups);
		doSynchronizePrincipals(syncOp.getEntry(), extSources);
		doSynchronizeAdminRoles(syncOp.getEntry(), admin_groups, admin_vos, admin_facilities);
		commitSyncOperation(syncOp);
		//ldapTemplate.modifyAttributes(entry);
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
	public void synchronizeAdminRoles(User user, List<Group> admin_groups, List<Vo> admin_vos, List<Facility> admin_facilities) {
		DirContextOperations entry = findByDN(buildDN(user));
		doSynchronizeAdminRoles(entry, admin_groups, admin_vos, admin_facilities);
		ldapTemplate.modifyAttributes(entry);
	}

	@Override
	protected Name buildDN(User bean) {
		return getEntryDN(String.valueOf(bean.getId()));
	}

	@Override
	protected void mapToContext(User bean, DirContextOperations context) {
		context.setAttributeValues(PerunAttribute.PerunAttributeNames.ldapAttrObjectClass,
				Arrays.asList(PerunAttribute.PerunAttributeNames.objectClassPerson,
						PerunAttribute.PerunAttributeNames.objectClassOrganizationalPerson,
						PerunAttribute.PerunAttributeNames.objectClassInetOrgPerson,
						PerunAttribute.PerunAttributeNames.objectClassPerunUser,
						PerunAttribute.PerunAttributeNames.objectClassTenOperEntry,
						PerunAttribute.PerunAttributeNames.objectClassInetUser).toArray());
		mapToContext(bean, context, getAttributeDescriptions());
	}

	/**
	 * Get User DN using user id.
	 *
	 * @param userId user id
	 * @return DN in Name
	 */
	@Override
	public Name getEntryDN(String... userId) {
		return LdapNameBuilder.newInstance()
				.add(PerunAttribute.PerunAttributeNames.organizationalUnitPeople)
				.add(PerunAttribute.PerunAttributeNames.ldapAttrPerunUserId, userId[0])
				.build();
	}

	@Override
	public List<Name> listEntries() {
		return ldapTemplate.search(query().
						where("objectclass").is(PerunAttribute.PerunAttributeNames.objectClassPerson),
				getNameMapper());
	}

	private boolean isEppnEpuidLogin(String login) {
		return login != null && EPPN_EPUID_PATTERN.matcher(login).matches();
	}

	private boolean isIdpUes(UserExtSource ues) {
		return ues != null && ues.getExtSource() != null
				&& ues.getExtSource().getType() != null
				&& ues.getExtSource().getType().equals(ExtSourcesManager.EXTSOURCE_IDP);
	}
}
