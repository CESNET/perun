package cz.metacentrum.perun.core.api;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * RoleManagementRules represents a set of rules which is used to determine principal's access rights for managing and reading a role.
 * Moreover, it contains allowed combinations of object and entity to/from which will be the role un/assigned
 * and related roles which can also read attribute value if the role can.
 * Each object and entity also contains a mapping to the specific column in the authz table,
 * so the database query can be created and executed more generally.
 *
 * roleName is role's unique identification which is used in the configuration file perun-roles.yml
 * primaryObject serves to determine with which object is the role primarily connected. Other objects are just complementary.
 * privilegedRolesToManage is a list of maps where each map entry consists from a role name as a key and a role object as a value.
 *            Relation between each map in the list is logical OR and relation between each entry in the map is logical AND.
 *            Example list - (Map1, Map2...)
 *            Example map - key: VOADMIN ; value: Vo
 *                          key: GROUPADMIN ; value: Group
 * privilegedRolesToRead is same as the privilegedRolesToManage, but its purpose is to determine which roles have rights to read the roleName.
 * entitiesToManage is a map of entities which can be set to the role. Key is a entity name and value is mapping to the database.
 *            Example entry: key: User; value: user_id
 * assignedObjects is a map of objects which can be assigned with the role. Key is a object name and value is mapping to the database.
 *            Example entry: key: Resource; value: resource_id
 * assignmentCheck is a list of maps defining which of the assigned objects should be checked for being critical (requiring MFA) when setting the role.
 *            Example entry: key: MFA; value: Resource <- If resource is critical, MFA is required
 *            Example entry: {} <- No MFA is required to set this role
 *            Example entry: Key: MFA; value: <- No value means MFA is always required to set this role
 * associatedReadRoles is a list of related roles which are authorized to read attribute value if the main role is authorized.
 *            Example list for groupadmin role - value: [GROUPOBSERVER]
 * assignableToAttributes is a flag that determines whether the role can appear in attribute policies.
 * systemRole is a flag that whether the role is a Perun system role.
 *
 */
public class RoleManagementRules {

	private String roleName;
	private String primaryObject;
	private List<Map<String, String>> privilegedRolesToManage;
	private List<Map<String, String>> privilegedRolesToRead;
	private Map<String, String> entitiesToManage;
	private Map<String, String> assignedObjects;
	private List<Map<String, String>> assignmentCheck;
	private List<String> associatedReadRoles;
	private boolean assignableToAttributes;
	private boolean systemRole;

	public RoleManagementRules(String roleName, String primaryObject, List<Map<String, String>> privilegedRolesToManage, List<Map<String, String>> privilegedRolesToRead, Map<String, String> entitiesToManage, Map<String, String> assignedObjects, List<Map<String, String>> assignmentCheck, List<String> associatedReadRoles, boolean assignableToAttributes, boolean systemRole) {
		this.roleName = roleName;
		this.primaryObject = primaryObject;
		this.privilegedRolesToManage = privilegedRolesToManage;
		this.privilegedRolesToRead = privilegedRolesToRead;
		this.entitiesToManage = entitiesToManage;
		this.assignedObjects = assignedObjects;
		this.assignmentCheck = assignmentCheck;
		this.associatedReadRoles = associatedReadRoles;
		this.assignableToAttributes = assignableToAttributes;
		this.systemRole = systemRole;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getPrimaryObject() {
		return primaryObject;
	}

	public void setPrimaryObject(String primaryObject) {
		this.primaryObject = primaryObject;
	}

	public List<Map<String, String>> getPrivilegedRolesToManage() {
		return privilegedRolesToManage;
	}

	public void setPrivilegedRolesToManage( List<Map<String, String>> privilegedRolesToManage) {
		this.privilegedRolesToManage = privilegedRolesToManage;
	}

	public List<Map<String, String>> getPrivilegedRolesToRead() {
		return privilegedRolesToRead;
	}

	public void setPrivilegedRolesToRead( List<Map<String, String>> privilegedRolesToRead) {
		this.privilegedRolesToRead = privilegedRolesToRead;
	}

	public Map<String, String> getEntitiesToManage() {
		return entitiesToManage;
	}

	public void setEntitiesToManage(Map<String, String> entitiesToManage) {
		this.entitiesToManage = entitiesToManage;
	}

	public Map<String, String> getAssignedObjects() {
		return assignedObjects;
	}

	public void setAssignedObjects(Map<String, String> assignedObjects) {
		this.assignedObjects = assignedObjects;
	}

	public List<Map<String, String>> getAssignmentCheck() {
		return assignmentCheck;
	}

	public void setAssignmentCheck(List<Map<String, String>> assignmentCheck) {
		this.assignmentCheck = assignmentCheck;
	}

	public List<String> getAssociatedReadRoles() {
		return associatedReadRoles;
	}

	public void setAssociatedReadRoles(List<String> associatedReadRoles) {
		this.associatedReadRoles = associatedReadRoles;
	}

	public boolean isAssignableToAttributes() {
		return assignableToAttributes;
	}

	public void setAssignableToAttributes(boolean assignableToAttributes) {
		this.assignableToAttributes = assignableToAttributes;
	}

	public boolean isSystemRole() {
		return systemRole;
	}

	public void setSystemRole(boolean systemRole) {
		this.systemRole = systemRole;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RoleManagementRules that = (RoleManagementRules) o;
		return Objects.equals(roleName, that.roleName) &&
			Objects.equals(primaryObject, that.primaryObject) &&
			Objects.equals(privilegedRolesToManage, that.privilegedRolesToManage) &&
			Objects.equals(privilegedRolesToRead, that.privilegedRolesToRead) &&
			Objects.equals(entitiesToManage, that.entitiesToManage) &&
			Objects.equals(assignedObjects, that.assignedObjects) &&
			Objects.equals(assignmentCheck, that.assignmentCheck) &&
			Objects.equals(associatedReadRoles, that.associatedReadRoles) &&
			Objects.equals(assignableToAttributes, that.assignableToAttributes) &&
			Objects.equals(systemRole, that.systemRole);
	}

	@Override
	public int hashCode() {
		return Objects.hash(roleName, primaryObject, privilegedRolesToManage, privilegedRolesToRead, entitiesToManage, assignedObjects, assignmentCheck, associatedReadRoles, assignableToAttributes, systemRole);
	}

	@Override
	public String toString() {
		return "RoleManagementRules{" +
			"roleName='" + roleName + '\'' +
			", primaryObject='" + primaryObject + '\'' +
			", privilegedRolesToManage=" + privilegedRolesToManage +
			", privilegedRolesToRead=" + privilegedRolesToRead +
			", entitiesToManage=" + entitiesToManage +
			", assignedObjects=" + assignedObjects +
			", assignmentCheck=" + assignmentCheck +
			", associatedReadRoles=" + associatedReadRoles +
			", assignableToAttributes=" + assignableToAttributes +
			", systemRole=" + systemRole +
			'}';
	}
}
