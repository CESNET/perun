package cz.metacentrum.perun.ldapc.model;

import java.util.List;
import java.util.Set;

import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.ModificationItem;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

public interface PerunUser extends PerunEntry<User> {

	/**
	 * Create user in ldap.
	 *
	 * @param user user from perun
	 * @throws InternalErrorException if NameNotFoundException occurs
	 */
	public void addUser(User user) throws InternalErrorException;


	/**
	 * Delete existing user from ldap.
	 * IMPORTANT Don't need delete members of deleting user from groups, it will depend on messages removeFrom Group
	 *
	 * @param user
	 * @throws InternalErrorException
	 */
	public void deleteUser(User user) throws InternalErrorException;

	public void updateUser(User user) throws InternalErrorException;
	
	/**
	 * Return true if user attribute 'password' in ldap already exists.
	 *
	 * @param user user in perun
	 * @return true if password in ldap exists for user, false if note
	 */
	public boolean userPasswordExists(User user);


	public void addPrincipal(User user, String login) throws InternalErrorException;


	public void removePrincipal(User user, String login) throws InternalErrorException;


	public void synchronizeMembership(User user, Set<Integer> voIds, List<Group> groups);
	
	public void synchronizePrincipals(User user, List<UserExtSource> extSources);

}
