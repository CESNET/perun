package cz.metacentrum.perun.ldapc.model;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

import java.util.List;
import java.util.Set;

public interface PerunUser extends PerunEntry<User> {

  /**
   * Create user in ldap.
   *
   * @param user user from perun
   * @throws InternalErrorException if NameNotFoundException occurs
   */
  public void addUser(User user);


  /**
   * Delete existing user from ldap.
   * IMPORTANT Don't need delete members of deleting user from groups, it will depend on messages removeFrom Group
   *
   * @param user
   * @throws InternalErrorException
   */
  public void deleteUser(User user);

  public void updateUser(User user);

  /**
   * Return true if user attribute 'password' in ldap already exists.
   *
   * @param user user in perun
   * @return true if password in ldap exists for user, false if note
   */
  public boolean userPasswordExists(User user);

  public void addAsVoAdmin(User user, Vo vo);

  public void removeFromVoAdmins(User user, Vo vo);

  public void addAsGroupAdmin(User user, Group group);

  public void removeFromGroupAdmins(User user, Group group);

  public void addAsFacilityAdmin(User user, Facility facility);

  public void removeFromFacilityAdmins(User user, Facility facility);

  public void addPrincipal(User user, String login);

  public void removePrincipal(User user, String login);

  public void synchronizeUser(User user, Iterable<Attribute> attrs, Set<Integer> voIds, List<Group> groups,
                              List<UserExtSource> extSources,
                              List<Group> admin_groups, List<Vo> admin_vos, List<Facility> admin_facilities);

  public void synchronizeMembership(User user, Set<Integer> voIds, List<Group> groups);

  public void synchronizePrincipals(User user, List<UserExtSource> extSources);

  public void synchronizeAdminRoles(User user, List<Group> admin_groups, List<Vo> admin_vos,
                                    List<Facility> admin_facilities);
}
