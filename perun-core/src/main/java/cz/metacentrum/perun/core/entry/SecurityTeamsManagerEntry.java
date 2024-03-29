package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AlreadyAdminException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.IllegalArgumentException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeManagedException;
import cz.metacentrum.perun.core.api.exceptions.RoleCannotBeSetException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamExistsException;
import cz.metacentrum.perun.core.api.exceptions.SecurityTeamNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserAlreadyBlacklistedException;
import cz.metacentrum.perun.core.api.exceptions.UserAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.UserNotAdminException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.SecurityTeamsManagerBl;
import cz.metacentrum.perun.core.impl.Utils;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class SecurityTeamsManagerEntry implements cz.metacentrum.perun.core.api.SecurityTeamsManager {

  private SecurityTeamsManagerBl securityTeamsManagerBl;
  private PerunBl perunBl;

  public SecurityTeamsManagerEntry(PerunBl perunBl) {
    this.perunBl = perunBl;
    this.securityTeamsManagerBl = perunBl.getSecurityTeamsManagerBl();
  }

  public SecurityTeamsManagerEntry() {
  }

  @Override
  public void addAdmin(PerunSession sess, SecurityTeam securityTeam, User user)
          throws PrivilegeException, SecurityTeamNotExistsException, UserNotExistsException, AlreadyAdminException,
          RoleCannotBeManagedException, RoleCannotBeSetException {
    Utils.checkPerunSession(sess);
    getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);
    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

    getSecurityTeamsManagerBl().checkUserIsNotSecurityAdmin(sess, securityTeam, user);

    AuthzResolver.setRole(sess, user, securityTeam, Role.SECURITYADMIN);
  }

  @Override
  public void addAdmin(PerunSession sess, SecurityTeam securityTeam, Group group)
          throws PrivilegeException, SecurityTeamNotExistsException, GroupNotExistsException, AlreadyAdminException,
          RoleCannotBeManagedException, RoleCannotBeSetException {
    Utils.checkPerunSession(sess);
    getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);
    getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

    getSecurityTeamsManagerBl().checkGroupIsNotSecurityAdmin(sess, securityTeam, group);

    AuthzResolver.setRole(sess, group, securityTeam, Role.SECURITYADMIN);

  }

  @Override
  public void addUserToBlacklist(PerunSession sess, SecurityTeam securityTeam, User user, String description)
      throws PrivilegeException, SecurityTeamNotExistsException, UserNotExistsException,
      UserAlreadyBlacklistedException {
    Utils.checkPerunSession(sess);

    getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);
    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

    //Authorization
    if (!AuthzResolver.authorizedInternal(sess, "addUserToBlacklist_SecurityTeam_User_String_policy",
        Arrays.asList(securityTeam, user))) {
      throw new PrivilegeException(sess, "addUserToBlacklist");
    }

    getSecurityTeamsManagerBl().checkUserIsNotInBlacklist(sess, securityTeam, user);

    // do not store empty description
    if (description != null && description.trim().isEmpty()) {
      description = null;
    }

    getSecurityTeamsManagerBl().addUserToBlacklist(sess, securityTeam, user, description);
  }

  @Override
  public SecurityTeam createSecurityTeam(PerunSession sess, SecurityTeam securityTeam)
      throws PrivilegeException, SecurityTeamExistsException {
    Utils.checkPerunSession(sess);
    Utils.notNull(securityTeam, "securityTeam");
    Utils.notNull(securityTeam.getName(), "securityTeam.name");

    //Authorization
    if (!AuthzResolver.authorizedInternal(sess, "createSecurityTeam_SecurityTeam_policy")) {
      throw new PrivilegeException(sess, "createSecurityTeam");
    }

    if (securityTeam.getName().length() > 128) {
      throw new IllegalArgumentException("Security Team name is too long, >128 characters");
    }

    if (!securityTeam.getName().matches("^[-_a-zA-z0-9.]{1,128}$")) {
      throw new IllegalArgumentException(
          "Wrong Security name - must matches [-_a-zA-z0-9.]+ and not be longer than 128 characters.");
    }

    getSecurityTeamsManagerBl().checkSecurityTeamNotExists(sess, securityTeam);
    getSecurityTeamsManagerBl().checkSecurityTeamUniqueName(sess, securityTeam);

    if (securityTeam.getDescription() != null && securityTeam.getDescription().trim().isEmpty()) {
      securityTeam.setDescription(null);
    }

    return getSecurityTeamsManagerBl().createSecurityTeam(sess, securityTeam);
  }

  @Override
  public void deleteSecurityTeam(PerunSession sess, SecurityTeam securityTeam)
      throws PrivilegeException, SecurityTeamNotExistsException, RelationExistsException {
    this.deleteSecurityTeam(sess, securityTeam, false);
  }

  @Override
  public void deleteSecurityTeam(PerunSession sess, SecurityTeam securityTeam, boolean forceDelete)
      throws PrivilegeException, SecurityTeamNotExistsException, RelationExistsException {
    Utils.checkPerunSession(sess);
    getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);

    //Authorization
    if (!AuthzResolver.authorizedInternal(sess, "deleteSecurityTeam_SecurityTeam__boolean_policy", securityTeam)) {
      throw new PrivilegeException(sess, "deleteSecurityTeam");
    }

    getSecurityTeamsManagerBl().deleteSecurityTeam(sess, securityTeam, forceDelete);
  }

  @Override
  public List<Group> getAdminGroups(PerunSession sess, SecurityTeam securityTeam)
      throws SecurityTeamNotExistsException, PrivilegeException {
    Utils.checkPerunSession(sess);

    getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);

    // Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getAdminGroups_SecurityTeam_policy", securityTeam)) {
      throw new PrivilegeException(sess, "getAdminGroups");
    }

    return getSecurityTeamsManagerBl().getAdminGroups(sess, securityTeam);
  }

  @Override
  public List<User> getAdmins(PerunSession sess, SecurityTeam securityTeam, boolean onlyDirectAdmins)
      throws PrivilegeException, SecurityTeamNotExistsException {
    Utils.checkPerunSession(sess);
    getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);

    //Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getAdmins_SecurityTeam_policy", securityTeam)) {
      throw new PrivilegeException(sess, "getAdmins");
    }

    return getSecurityTeamsManagerBl().getAdmins(sess, securityTeam, onlyDirectAdmins);
  }

  @Override
  public List<SecurityTeam> getAllSecurityTeams(PerunSession sess) throws PrivilegeException {
    Utils.checkPerunSession(sess);

    //Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getAllSecurityTeams_policy")) {
      throw new PrivilegeException(sess, "getAllSecurityTeams");
    }

    return getSecurityTeamsManagerBl().getAllSecurityTeams(sess);
  }

  @Override
  public List<User> getBlacklist(PerunSession sess, SecurityTeam securityTeam)
      throws PrivilegeException, SecurityTeamNotExistsException {
    Utils.checkPerunSession(sess);
    getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);

    //Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getBlacklist_SecurityTeam_policy", securityTeam)) {
      throw new PrivilegeException(sess, "getBlacklist");
    }

    return getSecurityTeamsManagerBl().getBlacklist(sess, securityTeam);
  }

  @Override
  public List<User> getBlacklist(PerunSession sess, Facility facility)
      throws PrivilegeException, FacilityNotExistsException {
    Utils.checkPerunSession(sess);
    getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

    //Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getBlacklist_Facility_policy", facility)) {
      throw new PrivilegeException(sess, "getBlacklist");
    }

    return getSecurityTeamsManagerBl().getBlacklist(sess, facility);
  }

  @Override
  public List<Pair<User, String>> getBlacklistWithDescription(PerunSession sess, SecurityTeam securityTeam)
      throws PrivilegeException, SecurityTeamNotExistsException {
    Utils.checkPerunSession(sess);
    getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);

    //Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getBlacklistWithDescription_SecurityTeam_policy", securityTeam)) {
      throw new PrivilegeException(sess, "getBlacklistWithDescription");
    }

    return getSecurityTeamsManagerBl().getBlacklistWithDescription(sess, securityTeam);
  }

  @Override
  public List<Pair<User, String>> getBlacklistWithDescription(PerunSession sess, Facility facility)
      throws PrivilegeException, FacilityNotExistsException {
    Utils.checkPerunSession(sess);
    getPerunBl().getFacilitiesManagerBl().checkFacilityExists(sess, facility);

    //Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getBlacklistWithDescription_Facility_policy", facility)) {
      throw new PrivilegeException(sess, "getBlacklistWithDescription");
    }

    return getSecurityTeamsManagerBl().getBlacklistWithDescription(sess, facility);
  }

  public PerunBl getPerunBl() {
    return this.perunBl;
  }

  @Override
  public SecurityTeam getSecurityTeamById(PerunSession sess, int id)
      throws PrivilegeException, SecurityTeamNotExistsException {
    Utils.checkPerunSession(sess);

    //Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getSecurityTeamById_int_policy")) {
      throw new PrivilegeException(sess, "getSecurityTeamById");
    }

    return getSecurityTeamsManagerBl().getSecurityTeamById(sess, id);
  }

  @Override
  public SecurityTeam getSecurityTeamByName(PerunSession sess, String name)
      throws PrivilegeException, SecurityTeamNotExistsException {
    Utils.checkPerunSession(sess);
    Utils.notNull(name, "name");

    //Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getSecurityTeamByName_String_policy")) {
      throw new PrivilegeException(sess, "getSecurityTeamByName");
    }

    return getSecurityTeamsManagerBl().getSecurityTeamByName(sess, name);
  }

  @Override
  public List<SecurityTeam> getSecurityTeams(PerunSession sess) throws PrivilegeException {
    Utils.checkPerunSession(sess);

    //Authorization
    if (!AuthzResolver.authorizedInternal(sess, "getSecurityTeams_policy")) {
      throw new PrivilegeException("getSecurityTeams");
    } else {
      List<SecurityTeam> securityTeams = getSecurityTeamsManagerBl().getAllSecurityTeams(sess);
      securityTeams.removeIf(st -> !AuthzResolver.authorizedInternal(sess, "filter-getSecurityTeams_policy", st));
      return securityTeams;
    }
  }

  public SecurityTeamsManagerBl getSecurityTeamsManagerBl() {
    return this.securityTeamsManagerBl;
  }

  @Override
  public void removeAdmin(PerunSession sess, SecurityTeam securityTeam, User user)
      throws PrivilegeException, SecurityTeamNotExistsException, UserNotExistsException, UserNotAdminException,
      RoleCannotBeManagedException {
    Utils.checkPerunSession(sess);
    getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);
    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);

    getSecurityTeamsManagerBl().checkUserIsSecurityAdmin(sess, securityTeam, user);

    AuthzResolver.unsetRole(sess, user, securityTeam, Role.SECURITYADMIN);
  }

  @Override
  public void removeAdmin(PerunSession sess, SecurityTeam securityTeam, Group group)
      throws PrivilegeException, SecurityTeamNotExistsException, GroupNotExistsException, GroupNotAdminException,
      RoleCannotBeManagedException {
    Utils.checkPerunSession(sess);
    getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);
    getPerunBl().getGroupsManagerBl().checkGroupExists(sess, group);

    getSecurityTeamsManagerBl().checkGroupIsSecurityAdmin(sess, securityTeam, group);

    AuthzResolver.unsetRole(sess, group, securityTeam, Role.SECURITYADMIN);
  }

  @Override
  public void removeUserFromBlacklist(PerunSession sess, SecurityTeam securityTeam, User user)
      throws PrivilegeException, SecurityTeamNotExistsException, UserNotExistsException, UserAlreadyRemovedException {
    Utils.checkPerunSession(sess);
    getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);
    getPerunBl().getUsersManagerBl().checkUserExists(sess, user);
    getSecurityTeamsManagerBl().checkUserIsInBlacklist(sess, securityTeam, user);

    //Authorization
    if (!AuthzResolver.authorizedInternal(sess, "removeUserFromBlacklist_SecurityTeam_User_policy",
        Arrays.asList(securityTeam, user))) {
      throw new PrivilegeException(sess, "removeUserFromBlacklist");
    }

    getSecurityTeamsManagerBl().removeUserFromBlacklist(sess, securityTeam, user);
  }

  public void setPerunBl(PerunBl perunBl) {
    this.perunBl = perunBl;
  }

  public void setSecurityTeamsManagerBl(SecurityTeamsManagerBl securityTeamsManagerBl) {
    this.securityTeamsManagerBl = securityTeamsManagerBl;
  }

  @Override
  public SecurityTeam updateSecurityTeam(PerunSession sess, SecurityTeam securityTeam)
      throws PrivilegeException, SecurityTeamNotExistsException, SecurityTeamExistsException {
    Utils.checkPerunSession(sess);
    Utils.notNull(securityTeam, "securityTeam");
    Utils.notNull(securityTeam.getName(), "securityTeam.name");

    getSecurityTeamsManagerBl().checkSecurityTeamExists(sess, securityTeam);

    //Authorization
    if (!AuthzResolver.authorizedInternal(sess, "updateSecurityTeam_SecurityTeam_policy", securityTeam)) {
      throw new PrivilegeException(sess, "updateSecurityTeam");
    }

    if (securityTeam.getName().length() > 128) {
      throw new IllegalArgumentException("Security Team name is too long, >128 characters");
    }

    if (!securityTeam.getName().matches("^[-_a-zA-z0-9.]{1,128}$")) {
      throw new IllegalArgumentException(
          "Wrong Security name - must matches [-_a-zA-z0-9.]+ and not be longer than 128 characters.");
    }

    try {
      SecurityTeam existingTeam = getSecurityTeamsManagerBl().getSecurityTeamByName(sess, securityTeam.getName());
      if (existingTeam != null && existingTeam.getId() != securityTeam.getId()) {
        throw new SecurityTeamExistsException(
            "SecurityTeam with name='" + securityTeam.getName() + "' already exists.");
      }
    } catch (SecurityTeamNotExistsException ex) {
      // OK since we are renaming security team to non-taken value
    }

    // don't store empty description
    if (securityTeam.getDescription() != null && securityTeam.getDescription().trim().isEmpty()) {
      securityTeam.setDescription(null);
    }

    return getSecurityTeamsManagerBl().updateSecurityTeam(sess, securityTeam);
  }
}
