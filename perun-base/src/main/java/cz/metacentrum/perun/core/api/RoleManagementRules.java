package cz.metacentrum.perun.core.api;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * RoleManagementRules represents a set of rules which is used to determine principal's access rights for managing and reading a role.
 * Moreover, it contains a allowed combinations of object and entity to/from which will be the role un/assigned.
 * Each object and entity also contains a mapping to the specific column in the authz table,
 * so the database query can be created and executed more generally.
 *
 * roleName is role's unique identification which is used in the configuration file perun-roles.yml
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
 *
 */
public class RoleManagementRules {

	private String roleName;
	private List<Map<String, String>> privilegedRolesToManage;
	private List<Map<String, String>> privilegedRolesToRead;
	private Map<String, String> entitiesToManage;
	private Map<String, String> assignedObjects;

	public RoleManagementRules(String roleName, List<Map<String, String>> privilegedRolesToManage, List<Map<String, String>> privilegedRolesToRead, Map<String, String> entitiesToManage, Map<String, String> assignedObjects) {
		this.roleName = roleName;
		this.privilegedRolesToManage = privilegedRolesToManage;
		this.privilegedRolesToRead = privilegedRolesToRead;
		this.entitiesToManage = entitiesToManage;
		this.assignedObjects = assignedObjects;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RoleManagementRules that = (RoleManagementRules) o;
		return Objects.equals(roleName, that.roleName) &&
			Objects.equals(privilegedRolesToManage, that.privilegedRolesToManage) &&
			Objects.equals(privilegedRolesToRead, that.privilegedRolesToRead) &&
			Objects.equals(entitiesToManage, that.entitiesToManage) &&
			Objects.equals(assignedObjects, that.assignedObjects);
	}

	@Override
	public int hashCode() {
		return Objects.hash(roleName, privilegedRolesToManage, privilegedRolesToRead, entitiesToManage, assignedObjects);
	}

	@Override
	public String toString() {
		return "RoleManagementRules{" +
			"roleName='" + roleName + '\'' +
			", privilegedRolesToManage=" + privilegedRolesToManage +
			", privilegedRolesToRead=" + privilegedRolesToRead +
			", entitiesToManage=" + entitiesToManage +
			", assignedObjects=" + assignedObjects +
			'}';
	}
}
