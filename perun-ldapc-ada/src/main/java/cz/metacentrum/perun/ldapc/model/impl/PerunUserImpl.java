package cz.metacentrum.perun.ldapc.model.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.naming.Name;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.support.LdapNameBuilder;


import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.ldapc.model.PerunAttribute;
import cz.metacentrum.perun.ldapc.model.PerunGroup;
import cz.metacentrum.perun.ldapc.model.PerunUser;

public class PerunUserImpl extends AbstractPerunEntry<User> implements PerunUser {

	private final static Logger log = LoggerFactory.getLogger(PerunUserImpl.class);

	@Autowired
	private PerunGroup perunGroup;
	
	@Override
	protected List<String> getDefaultUpdatableAttributes() {
		return Arrays.asList(
				PerunAttribute.PerunAttributeNames.ldapAttrSurname,
				PerunAttribute.PerunAttributeNames.ldapAttrCommonName,
				PerunAttribute.PerunAttributeNames.ldapAttrGivenName);
	}

	@Override
	protected List<PerunAttribute<User>> getDefaultAttributeDescriptions() {
		return Arrays.asList(
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrEntryStatus, 
						PerunAttribute.REQUIRED, 
						(PerunAttribute.SingleValueExtractor<User>)(user, attrs) -> "active"
						),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrSurname, 
						PerunAttribute.REQUIRED, 
						(PerunAttribute.SingleValueExtractor<User>)(user, attrs) -> user.getLastName()
						),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrGivenName, 
						PerunAttribute.OPTIONAL, 
						(PerunAttribute.SingleValueExtractor<User>)(user, attrs) -> user.getFirstName()
						),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrCommonName, 
						PerunAttribute.REQUIRED, 
						(PerunAttribute.SingleValueExtractor<User>)(user, attrs) -> {
							String firstName = user.getFirstName();
							String lastName = user.getLastName();
							String commonName = "";
							if(firstName == null || firstName.isEmpty()) firstName = "";
							else commonName+= firstName + " ";
							if(lastName == null || lastName.isEmpty()) lastName = "N/A";
							commonName+= lastName;
							return commonName;
						}
						),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrPerunUserId, 
						PerunAttribute.REQUIRED, 
						(PerunAttribute.SingleValueExtractor<User>)(user, attrs) -> String.valueOf(user.getId())
						),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrIsServiceUser, 
						PerunAttribute.REQUIRED, 
						(PerunAttribute.SingleValueExtractor<User>)(user, attrs) -> user.isServiceUser() ? "1" : "0"
						),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrIsSponsoredUser, 
						PerunAttribute.REQUIRED, 
						(PerunAttribute.SingleValueExtractor<User>)(user, attrs) -> user.isSponsoredUser() ? "1" : "0"
						),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrPreferredMail, 
						PerunAttribute.OPTIONAL, 
						(PerunAttribute.SingleValueExtractor<User>)(user, attrs) -> user.isSponsoredUser() ? "1" : "0"
						)
				);
	}

	public void addUser(User user) throws InternalErrorException {
		addEntry(user);
	}

	public void deleteUser(User user) throws InternalErrorException {
		deleteEntry(user);
	}

	@Override
	public void updateUser(User user) throws InternalErrorException {
		modifyEntry(user);
	}

	public boolean userPasswordExists(User user) {
		return entryAttributeExists(user, PerunAttribute.PerunAttributeNames.ldapAttrUserPassword);
	}

	@Override
	public void addPrincipal(User user, String login) throws InternalErrorException {
		DirContextOperations entry = findByDN(buildDN(user));
		entry.addAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrEduPersonPrincipalNames, login);
		ldapTemplate.modifyAttributes(entry);
	}

	@Override
	public void removePrincipal(User user, String login) throws InternalErrorException {
		DirContextOperations entry = findByDN(buildDN(user));
		entry.removeAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrEduPersonPrincipalNames, login);
		ldapTemplate.modifyAttributes(entry);
	}

	@Override
	public void synchronizeMembership(User user, Set<Integer> voIds, List<Group> groups) {
		DirContextOperations entry = findByDN(buildDN(user));
		entry.setAttributeValues(PerunAttribute.PerunAttributeNames.ldapAttrMemberOfPerunVo, voIds.stream().map(id -> String.valueOf(id)).toArray(String[]::new));
		List<Name> memberOfNames = new ArrayList<Name>();
		for(Group group: groups) {
			memberOfNames.add(addBaseDN(perunGroup.getEntryDN(String.valueOf(group.getVoId()), String.valueOf(group.getId()))));
		}
		entry.setAttributeValues(PerunAttribute.PerunAttributeNames.ldapAttrMemberOf, memberOfNames.toArray());
		ldapTemplate.modifyAttributes(entry);
	}

	@Override
	public void synchronizePrincipals(User user, List<UserExtSource> extSources) {
		DirContextOperations entry = findByDN(buildDN(user));
		entry.setAttributeValues(PerunAttribute.PerunAttributeNames.ldapAttrEduPersonPrincipalNames, 
				extSources.stream()
					.filter(ues -> ues != null && ues.getExtSource() != null 
						&& ues.getExtSource().getType() != null 
						&& ues.getExtSource().getType().equals(ExtSourcesManager.EXTSOURCE_IDP))
					.map(ues ->  ues.getLogin())
					.filter(login -> login != null)
					.toArray(String[]::new)
					);
		ldapTemplate.modifyAttributes(entry);
	}

	@Override
	protected Name buildDN(User bean) {
		return getEntryDN(String.valueOf(bean.getId()));
	}

	@Override
	protected void mapToContext(User bean, DirContextOperations context) throws InternalErrorException {
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
	public Name getEntryDN(String ...userId) {
		return LdapNameBuilder.newInstance()
				.add(PerunAttribute.PerunAttributeNames.organizationalUnitPeople)
				.add(PerunAttribute.PerunAttributeNames.ldapAttrPerunUserId, userId[0])
				.build();
	}

}
