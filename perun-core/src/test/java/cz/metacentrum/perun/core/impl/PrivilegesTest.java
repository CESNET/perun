package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.Role;
import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class PrivilegesTest {

	@Test
	public void testGetRolesWhichCanManageRole() {
		Set<Role> allowedRoles = Privileges.getRolesWhichCanManageRole(Role.GROUPADMIN);

		assertThat(allowedRoles).containsOnly(Role.GROUPADMIN, Role.VOADMIN);
	}
}
