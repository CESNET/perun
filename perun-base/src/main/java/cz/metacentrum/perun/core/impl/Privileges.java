package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.Role;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static cz.metacentrum.perun.core.api.Role.CABINETADMIN;
import static cz.metacentrum.perun.core.api.Role.FACILITYADMIN;
import static cz.metacentrum.perun.core.api.Role.GROUPADMIN;
import static cz.metacentrum.perun.core.api.Role.PERUNADMIN;
import static cz.metacentrum.perun.core.api.Role.PERUNOBSERVER;
import static cz.metacentrum.perun.core.api.Role.RESOURCEADMIN;
import static cz.metacentrum.perun.core.api.Role.RESOURCESELFSERVICE;
import static cz.metacentrum.perun.core.api.Role.SECURITYADMIN;
import static cz.metacentrum.perun.core.api.Role.SPONSOR;
import static cz.metacentrum.perun.core.api.Role.TOPGROUPCREATOR;
import static cz.metacentrum.perun.core.api.Role.VOADMIN;
import static cz.metacentrum.perun.core.api.Role.VOOBSERVER;

/**
 * This class defines role setting privileges.
 *
 * This class contains information about which roles can set given role.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class Privileges {

	private static Map<Role, Set<Role>> rolePrivileges = new HashMap<>();

	static {
		initialize();
	}

	private static void initialize() {
		role(FACILITYADMIN).canBeManagedBy(
			FACILITYADMIN
		);

		role(GROUPADMIN).canBeManagedBy(
			GROUPADMIN,
			VOADMIN
		);

		role(PERUNADMIN).canBeManagedBy(
			PERUNADMIN
		);

		role(RESOURCEADMIN).canBeManagedBy(
			RESOURCEADMIN,
			VOADMIN
		);

		role(RESOURCESELFSERVICE).canBeManagedBy(
			RESOURCEADMIN,
			VOADMIN
		);

		role(SPONSOR).canBeManagedBy(
			VOADMIN
		);

		role(TOPGROUPCREATOR).canBeManagedBy(
			VOADMIN
		);

		role(VOADMIN).canBeManagedBy(
			VOADMIN
		);

		role(VOOBSERVER).canBeManagedBy(
			VOOBSERVER,
			VOADMIN
		);

		role(PERUNOBSERVER).canBeManagedBy(
			PERUNOBSERVER
		);

		role(SECURITYADMIN).canBeManagedBy(
			SECURITYADMIN
		);

		role(CABINETADMIN).canBeManagedBy(
			CABINETADMIN
		);
	}

	/**
	 * Returns roles which are allowed to set given role.
	 *
	 * @param role role
	 * @return set of roles which can set the given role, null if no configuration is set for given role
	 */
	public static Set<Role> getRolesWhichCanManageRole(Role role) {
		return rolePrivileges.get(role);
	}
	/**
	 * Helper method for fluent code.
	 */
	private static RoleSet role(Role role) {
		return new RoleSet(role);
	}

	/**
	 * Helper class for fluent code.
	 *
	 * Represents state of privileges configuration.
	 */
	private static class RoleSet {
		private Role role;

		private RoleSet(Role role) {
			this.role = role;
		}

		/**
		 * Defines which role can set previously specified role.
		 *
		 * @param allowedRoles roles that are allowed to set the previously specified role.
		 */
		private void canBeManagedBy(Role... allowedRoles) {
			if (!rolePrivileges.containsKey(role)) {
				rolePrivileges.put(role, new HashSet<>());
			}
			for (Role allowedRole : allowedRoles) {
				rolePrivileges.get(role).add(allowedRole);
			}
		}
	}
}
