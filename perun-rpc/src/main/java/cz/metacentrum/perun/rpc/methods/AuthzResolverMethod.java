package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Group;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunPolicy;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.impl.AuthzRoles;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

public enum AuthzResolverMethod implements ManagerMethod {

	/*#
	 * Returns list of caller's role names.
	 *
	 * Perun system uses role names in the upper case format.
	 * However, for now, they are converted to the lower case format because of the compatibility with external systems.
	 *
	 * @exampleResponse [ "groupadmin" , "self" , "voadmin" ]
	 * @return List<String> List of roles
	 */
	getPrincipalRoleNames {
		@Override
		public List<String> call(ApiCaller ac, Deserializer parms) throws PerunException {
			List<String> roles = cz.metacentrum.perun.core.api.AuthzResolver.getPrincipalRoleNames(ac.getSession());
			roles.replaceAll(String::toLowerCase);
			return roles;
		}
	},
	/*#
	 * Returns list of user's role names.
	 *
	 * Perun system uses role names in the upper case format.
	 * However, for now, they are converted to the lower case format because of the compatibility with external systems.
	 *
	 * @exampleResponse [ "groupadmin" , "self" , "voadmin" ]
	 * @return List<String> List of roles
	 */
	getUserRoleNames {
		@Override
		public List<String> call(ApiCaller ac, Deserializer parms ) throws PerunException {
			List<String> roles = cz.metacentrum.perun.core.api.AuthzResolver.getUserRoleNames(ac.getSession(), ac.getUserById(parms.readInt("user")));
			roles.replaceAll(String::toLowerCase);
			return roles;
		}
	},
	/*#
	 * Returns all roles as an AuthzRoles object for a given user.
	 *
	 * @param userId int Id of a user
	 * @return AuthzRoles Object which contains all roles with perunbeans
	 * @exampleResponse {"FACILITYADMIN":{"Facility":[32]},"SELF":{"Member":[4353,12324],"User":[2552,2252]},"SPONSOR":{"SponsoredUser":[54750]},"VOADMIN":{"Vo":[356]},"PERUNADMIN":{}}
	 */
	getUserRoles {
		@Override
		public AuthzRoles call(ApiCaller ac, Deserializer parms) throws PerunException {
			return cz.metacentrum.perun.core.api.AuthzResolver.getUserRoles(ac.getSession(), parms.readInt("userId"));
		}
	},
	/*#
	 * Returns list of group's role names.
	 *
	 * Perun system uses role names in the upper case format.
	 * However, for now, they are converted to the lower case format because of the compatibility with external systems.
	 *
	 * @exampleResponse [ "groupadmin" , "self" , "voadmin" ]
	 * @return List<String> List of roles
	 */
	getGroupRoleNames {
		@Override
		public List<String> call(ApiCaller ac, Deserializer parms ) throws PerunException {
			List<String> roles = cz.metacentrum.perun.core.api.AuthzResolver.getGroupRoleNames(ac.getSession(), ac.getGroupById(parms.readInt("group")));
			roles.replaceAll(String::toLowerCase);
			return roles;
		}
	},
	/*#
	 * Returns all roles as an AuthzRoles object for a given group.
	 *
	 * @param groupId int Id of a group
	 * @return AuthzRoles Object which contains all roles with perunbeans
	 * @exampleResponse {"FACILITYADMIN":{"Facility":[3682,3826]},"GROUPADMIN":{"Group":[9082,12093],"Vo":[3794,201]},"VOADMIN":{"Vo":[2561,1541,2061,1041,3601]}}
	 */
	getGroupRoles {
		@Override
		public AuthzRoles call(ApiCaller ac, Deserializer parms) throws PerunException {
			return cz.metacentrum.perun.core.api.AuthzResolver.getGroupRoles(ac.getSession(), parms.readInt("groupId"));
		}
	},
	/*#
	 * Get all managers for complementaryObject and role with specified attributes.
	 *
	 * @param role String Expected Role to filter managers by
	 * @param complementaryObjectId int Property <code>id</code> of complementaryObject to get managers for
	 * @param complementaryObjectName String Property <code>beanName</code> of complementaryObject, meaning object type (Vo | Group | Facility | ... )
	 * @param specificAttributes List<String> list of specified attributes which are needed in object richUser
	 * @param onlyDirectAdmins boolean When true, return only direct users of the complementary object for role with specific attributes
	 * @param allUserAttributes boolean When true, do not specify attributes through list and return them all in objects richUser. Ignoring list of specific attributes
	 * @return List<RichUser> Administrators for complementary object and role with specify attributes
	 */
	getRichAdmins {
		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
			//get role by name
			String roleName = parms.readString("role");
			if (!cz.metacentrum.perun.core.api.AuthzResolver.roleExists(roleName)) {
				throw new RpcException(RpcException.Type.WRONG_PARAMETER, "Role with name " + roleName + " does not exist.");
			}
			roleName = roleName.toUpperCase();
			int complementaryObjectId = parms.readInt("complementaryObjectId");
			String complementaryObjectName = parms.readString("complementaryObjectName");

			PerunBean bean = null;
			try {
				bean = (PerunBean) Class.forName("cz.metacentrum.perun.core.api." + complementaryObjectName).getConstructor().newInstance();
				bean.setId(complementaryObjectId);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new InternalErrorException(e);
			} catch (ClassNotFoundException e) {
				throw new RpcException(RpcException.Type.WRONG_PARAMETER, "Object with name " + complementaryObjectName + " does not exist.");
			}

			return cz.metacentrum.perun.core.api.AuthzResolver.getRichAdmins(ac.getSession(),
							bean,
							parms.readList("specificAttributes", String.class),
							roleName,
							parms.readBoolean("onlyDirectAdmins"),
							parms.readBoolean("allUserAttributes"));
		}
	},

	/*#
	 * Get all groups of managers (authorizedGroups) for complementaryObject and role.
	 *
	 * @param role String Expected Role to filter authorizedGroups by
	 * @param complementaryObjectId int Property <code>id</code> of complementaryObject to get groups of managers for
	 * @param complementaryObjectName String Property <code>beanName</code> of complementaryObject, meaning object type (Vo | Group | Facility | ... )
	 * @return List<Group> List of authorizedGroups for complementaryObject and role
	 */
	getAdminGroups {
		@Override
		public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {
		//get role by name
			String roleName = parms.readString("role");
			if (!cz.metacentrum.perun.core.api.AuthzResolver.roleExists(roleName)) {
				throw new RpcException(RpcException.Type.WRONG_PARAMETER, "Role with name " + roleName + " does not exist.");
			}
			roleName = roleName.toUpperCase();
			int complementaryObjectId = parms.readInt("complementaryObjectId");
			String complementaryObjectName = parms.readString("complementaryObjectName");

			PerunBean bean = null;
			try {
				bean = (PerunBean) Class.forName("cz.metacentrum.perun.core.api." + complementaryObjectName).getConstructor().newInstance();
				bean.setId(complementaryObjectId);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new InternalErrorException(e);
			} catch (ClassNotFoundException e) {
				throw new RpcException(RpcException.Type.WRONG_PARAMETER, "Object with name " + complementaryObjectName + " does not exist.");
			}

			return cz.metacentrum.perun.core.api.AuthzResolver.getAdminGroups(ac.getSession(),
							bean,
							roleName);
		}
	},

	/*#
	 * Set role for user and complementaryObject.
	 *
	 * If some complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore <code>complementaryObject</code> param.
	 *
	 * IMPORTANT: Refresh authz only if user in session is affected.
	 *
	 * @param role String Role which will be set for given users ( FACILITYADMIN | GROUPADMIN | PERUNADMIN | RESOURCEADMIN | RESOURCESELFSERVICE | SPONSOR | TOPGROUPCREATOR | VOADMIN | VOOBSERVER | PERUNOBSERVER | SECURITYADMIN | CABINETADMIN )
	 * @param user int <code>id</code> of User to set role for
	 * @param complementaryObject Object Object (e.g.: vo | group | facility ) to associate role and user with.
	 * @exampleParam role "VOADMIN"
	 * @exampleParam complementaryObject { "id" : 123 , "name" : "My testing VO" , "shortName" : "test_vo" , "beanName" : "Vo" }
	 */
	/*#
	 * Set role for user and complementaryObjects.
	 *
	 * If some complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore <code>complementaryObjects</code> param.
	 *
	 * IMPORTANT: Refresh authz only if user in session is affected.
	 *
	 * @param role String Role which will be set for given users ( FACILITYADMIN | GROUPADMIN | PERUNADMIN | RESOURCEADMIN | RESOURCESELFSERVICE | SPONSOR | TOPGROUPCREATOR | VOADMIN | VOOBSERVER | PERUNOBSERVER | SECURITYADMIN | CABINETADMIN )
	 * @param user int <code>id</code> of User to set role for
	 * @param complementaryObjects List<Object> Objects (e.g.: vo | group | facility ) to associate role and user with
	 * @exampleParam role "VOADMIN"
	 * @exampleParam complementaryObjects [ { "id" : 123 , "name" : "My testing VO" , "shortName" : "test_vo" , "beanName" : "Vo" } , {...} , {...} ]
	 */
	/*#
	 * Set role for authorizedGroup and complementaryObject.
	 *
	 * If some complementaryObject is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore <code>complementaryObject</code> param.
	 *
	 * IMPORTANT: Refresh authz only if user in session is affected.
	 *
	 * @param role String Role which will be set for given users ( FACILITYADMIN | GROUPADMIN | PERUNADMIN | RESOURCEADMIN | RESOURCESELFSERVICE | SPONSOR | TOPGROUPCREATOR | VOADMIN | VOOBSERVER | PERUNOBSERVER | SECURITYADMIN | CABINETADMIN )
	 * @param authorizedGroup int <code>id</code> of Group to set role for
	 * @param complementaryObject Object Object (e.g.: vo | group | facility ) to associate role and authorizedGroup with
	 * @exampleParam role "VOADMIN"
	 * @exampleParam complementaryObject { "id" : 123 , "name" : "My testing VO" , "shortName" : "test_vo" , "beanName" : "Vo" }
	 */
	/*#
	 * Set role for authorizedGroup and complementaryObjects.
	 *
	 * If some complementaryObject is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore <code>complementaryObjects</code> param.
	 *
	 * IMPORTANT: Refresh authz only if user in session is affected.
	 *
	 * @param role String Role which will be set for given users ( FACILITYADMIN | GROUPADMIN | PERUNADMIN | RESOURCEADMIN | RESOURCESELFSERVICE | SPONSOR | TOPGROUPCREATOR | VOADMIN | VOOBSERVER | PERUNOBSERVER | SECURITYADMIN | CABINETADMIN )
	 * @param authorizedGroup int <code>id</code> of Group to set role for
	 * @param complementaryObjects List<Object> Objects (e.g.: vo | group | facility ) to associate role and authorizedGroup with
	 * @exampleParam role "VOADMIN"
	 * @exampleParam complementaryObjects [ { "id" : 123 , "name" : "My testing VO" , "shortName" : "test_vo" , "beanName" : "Vo" } , {...} , {...} ]
	 */
	/*#
	 * Set role for users and complementaryObject.
	 *
	 * If complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore complementary object.
	 *
	 * IMPORTANT: Refresh authz only if user in session is affected.
	 *
	 * @param role String Role which will be set for given users ( FACILITYADMIN | GROUPADMIN | PERUNADMIN | RESOURCEADMIN | RESOURCESELFSERVICE | SPONSOR | TOPGROUPCREATOR | VOADMIN | VOOBSERVER | PERUNOBSERVER | SECURITYADMIN | CABINETADMIN )
	 * @param users int[] <code>ids</code> of users for which is the role set
	 * @param complementaryObject Object Object (e.g.: vo | group | facility ) to associate role and users with
	 * @exampleParam role "VOADMIN"
	 * @exampleParam complementaryObject { "id" : 123 , "name" : "My testing VO" , "shortName" : "test_vo" , "beanName" : "Vo" }
	 */
	/*#
	 * Set role for authorizedGroups and complementaryObject.
	 *
	 * If complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore complementary object.
	 *
	 * IMPORTANT: Refresh authz only if user in session is affected.
	 *
	 * @param role String Role which will be set for given groups ( FACILITYADMIN | GROUPADMIN | PERUNADMIN | RESOURCEADMIN | RESOURCESELFSERVICE | SPONSOR | TOPGROUPCREATOR | VOADMIN | VOOBSERVER | PERUNOBSERVER | SECURITYADMIN | CABINETADMIN )
	 * @param authorizedGroups int[] <code>ids</code> of groups for which is the role set
	 * @param complementaryObject Object Object (e.g.: vo | group | facility ) to associate role and authorizedGroups with
	 * @exampleParam role "VOADMIN"
	 * @exampleParam complementaryObject { "id" : 123 , "name" : "My testing VO" , "shortName" : "test_vo" , "beanName" : "Vo" }
	 */
	setRole {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();
			//get role by name
			String roleName = parms.readString("role");
			if (!cz.metacentrum.perun.core.api.AuthzResolver.roleExists(roleName)) {
				throw new RpcException(RpcException.Type.WRONG_PARAMETER, "Role with name " + roleName + " does not exist.");
			}
			roleName = roleName.toUpperCase();

			if(parms.contains("user")) {
				if(parms.contains("complementaryObject")) {
					cz.metacentrum.perun.core.api.AuthzResolver.setRole(ac.getSession(),
								ac.getUserById(parms.readInt("user")),
								parms.readPerunBean("complementaryObject"),
								roleName);
					return null;
				} else if(parms.contains("complementaryObjects")) {
					cz.metacentrum.perun.core.api.AuthzResolver.setRole(ac.getSession(),
								ac.getUserById(parms.readInt("user")),
								roleName,
								parms.readListPerunBeans("complementaryObjects"));
					return null;
				} else {
					throw new RpcException(RpcException.Type.MISSING_VALUE, "list of complementary objects or complementary object");
				}
			} else if (parms.contains("users")) {
				if (parms.contains("complementaryObject")) {
					int[] userIds = parms.readArrayOfInts("users");
					List<User> users = new ArrayList<>(userIds.length);
					for (int userId : userIds) {
						users.add(ac.getUserById(userId));
					}
					cz.metacentrum.perun.core.api.AuthzResolver.setRole(ac.getSession(),
								users,
								roleName,
								parms.readPerunBean("complementaryObject"));
					return null;
				} else {
					throw new RpcException(RpcException.Type.MISSING_VALUE, "complementaryObject");
				}
			} else if (parms.contains("authorizedGroups")) {
				if (parms.contains("complementaryObject")) {
					int[] groupIds = parms.readArrayOfInts("authorizedGroups");
					List<Group> groups = new ArrayList<>(groupIds.length);
					for (int groupId : groupIds) {
						groups.add(ac.getGroupById(groupId));
					}
					cz.metacentrum.perun.core.api.AuthzResolver.setRole(ac.getSession(),
								groups,
								parms.readPerunBean("complementaryObject"),
								roleName);
					return null;
				} else {
					throw new RpcException(RpcException.Type.MISSING_VALUE, "complementaryObject");
				}
			} else if(parms.contains("authorizedGroup")) {
				if(parms.contains("complementaryObject")) {
					cz.metacentrum.perun.core.api.AuthzResolver.setRole(ac.getSession(),
								ac.getGroupById(parms.readInt("authorizedGroup")),
								parms.readPerunBean("complementaryObject"),
								roleName);
					return null;
				} else if(parms.contains("complementaryObjects")) {
					cz.metacentrum.perun.core.api.AuthzResolver.setRole(ac.getSession(),
								ac.getGroupById(parms.readInt("authorizedGroup")),
								roleName,
								parms.readListPerunBeans("complementaryObjects"));
					return null;
				} else {
					throw new RpcException(RpcException.Type.MISSING_VALUE, "list of complementary objects or complementary object");
				}
			} else {
				 throw new RpcException(RpcException.Type.MISSING_VALUE, "user(s) or authorizedGroup(s)");
			}
		}
	},

	/*#
	 * Unset role for user and complementaryObject.
	 *
	 * If some complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore <code>complementaryObject</code> param.
	 *
	 * IMPORTANT: Refresh authz only if user in session is affected.
	 *
 	 * @param role String Role which will be set for given users ( FACILITYADMIN | GROUPADMIN | PERUNADMIN | RESOURCEADMIN | RESOURCESELFSERVICE | SPONSOR | TOPGROUPCREATOR | VOADMIN | VOOBSERVER | PERUNOBSERVER | SECURITYADMIN | CABINETADMIN )
	 * @param user int <code>id</code> of User to unset role for
	 * @param complementaryObject Object Object (e.g.: vo | group | facility ) to remove role for a user
	 * @exampleParam role "voadmin"
	 * @exampleParam complementaryObject { "id" : 123 , "name" : "My testing VO" , "shortName" : "test_vo" , "beanName" : "Vo" }
	 */
	/*#
	 * Unset role for user and complementaryObjects.
	 *
	 * If some complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore <code>complementaryObjects</code> param.
	 *
	 * IMPORTANT: Refresh authz only if user in session is affected.
	 *
	 * @param role String Role which will be set for given users ( FACILITYADMIN | GROUPADMIN | PERUNADMIN | RESOURCEADMIN | RESOURCESELFSERVICE | SPONSOR | TOPGROUPCREATOR | VOADMIN | VOOBSERVER | PERUNOBSERVER | SECURITYADMIN | CABINETADMIN )
	 * @param user int <code>id</code> of User to unset role for
	 * @param complementaryObjects List<Object> Objects (e.g.: vo | group | facility ) to remove role for a user
	 * @exampleParam role "voadmin"
	 * @exampleParam complementaryObjects [ { "id" : 123 , "name" : "My testing VO" , "shortName" : "test_vo" , "beanName" : "Vo" } , {...} , {...} ]
	 */
	/*#
	 * Unset role for authorizedGroup and complementaryObject.
	 *
	 * If some complementaryObject is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore <code>complementaryObject</code> param.
	 *
	 * IMPORTANT: Refresh authz only if user in session is affected.
	 *
	 * @param role String Role which will be set for given users ( FACILITYADMIN | GROUPADMIN | PERUNADMIN | RESOURCEADMIN | RESOURCESELFSERVICE | SPONSOR | TOPGROUPCREATOR | VOADMIN | VOOBSERVER | PERUNOBSERVER | SECURITYADMIN | CABINETADMIN )
	 * @param authorizedGroup int <code>id</code> of Group to unset role for
	 * @param complementaryObject Object Object (e.g.: vo | group | facility ) to remove role for an authorizedGroup
	 * @exampleParam role "voadmin"
	 * @exampleParam complementaryObject { "id" : 123 , "name" : "My testing VO" , "shortName" : "test_vo" , "beanName" : "Vo" }
	 */
	/*#
	 * Unset role for authorizedGroup and complementaryObjects.
	 *
	 * If some complementaryObject is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore <code>complementaryObjects</code> param.
	 *
	 * IMPORTANT: Refresh authz only if user in session is affected.
	 *
	 * @param role String Role which will be set for given users ( FACILITYADMIN | GROUPADMIN | PERUNADMIN | RESOURCEADMIN | RESOURCESELFSERVICE | SPONSOR | TOPGROUPCREATOR | VOADMIN | VOOBSERVER | PERUNOBSERVER | SECURITYADMIN | CABINETADMIN )
	 * @param authorizedGroup int <code>id</code> of Group to unset role for
	 * @param complementaryObjects List<Object> Objects (e.g.: vo | group | facility ) to remove role for an authorizedGroup
	 * @exampleParam role "voadmin"
	 * @exampleParam complementaryObjects [ { "id" : 123 , "name" : "My testing VO" , "shortName" : "test_vo" , "beanName" : "Vo" } , {...} , {...} ]
	 */
	/*#
	 * Unset role for users and complementaryObject.
	 *
	 * If complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore complementary object.
	 *
	 * IMPORTANT: Refresh authz only if user in session is affected.
	 *
	 * @param role String Role which will be unset for given users ( FACILITYADMIN | GROUPADMIN | PERUNADMIN | RESOURCEADMIN | RESOURCESELFSERVICE | SPONSOR | TOPGROUPCREATOR | VOADMIN | VOOBSERVER | PERUNOBSERVER | SECURITYADMIN | CABINETADMIN )
	 * @param users int[] <code>ids</code> of users for which is the role set
	 * @param complementaryObject Object Object (e.g.: vo | group | facility ) to associate role and users with
	 * @exampleParam role "VOADMIN"
	 * @exampleParam complementaryObject { "id" : 123 , "name" : "My testing VO" , "shortName" : "test_vo" , "beanName" : "Vo" }
	 */
	/*#
	 * Unset role for authorizedGroups and complementaryObject.
	 *
	 * If complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore complementary object.
	 *
	 * IMPORTANT: Refresh authz only if user in session is affected.
	 *
	 * @param role String Role which will be set for given groups ( FACILITYADMIN | GROUPADMIN | PERUNADMIN | RESOURCEADMIN | RESOURCESELFSERVICE | SPONSOR | TOPGROUPCREATOR | VOADMIN | VOOBSERVER | PERUNOBSERVER | SECURITYADMIN | CABINETADMIN )
	 * @param authorizedGroups int[] <code>ids</code> of groups for which is the role set
	 * @param complementaryObject Object Object (e.g.: vo | group | facility ) to associate role and authorizedGroups with
	 * @exampleParam role "VOADMIN"
	 * @exampleParam complementaryObject { "id" : 123 , "name" : "My testing VO" , "shortName" : "test_vo" , "beanName" : "Vo" }
	 */
	unsetRole {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			parms.stateChangingCheck();
			//get role by name
			String roleName = parms.readString("role");
			if (!cz.metacentrum.perun.core.api.AuthzResolver.roleExists(roleName)) {
				throw new RpcException(RpcException.Type.WRONG_PARAMETER, "Role with name " + roleName + " does not exist.");
			}
			roleName = roleName.toUpperCase();

			if(parms.contains("user")) {
				if(parms.contains("complementaryObject")) {
					cz.metacentrum.perun.core.api.AuthzResolver.unsetRole(ac.getSession(),
								ac.getUserById(parms.readInt("user")),
								parms.readPerunBean("complementaryObject"),
								roleName);
					return null;
				} else if (parms.contains("complementaryObjects")) {
					cz.metacentrum.perun.core.api.AuthzResolver.unsetRole(ac.getSession(),
								ac.getUserById(parms.readInt("user")),
								roleName,
								parms.readListPerunBeans("complementaryObjects"));
					return null;
				} else {
					throw new RpcException(RpcException.Type.MISSING_VALUE, "list of complementary objects or complementary object");
				}
			} else if (parms.contains("users")) {
				if (parms.contains("complementaryObject")) {
					int[] userIds = parms.readArrayOfInts("users");
					List<User> users = new ArrayList<>(userIds.length);
					for (int userId : userIds) {
						users.add(ac.getUserById(userId));
					}
					cz.metacentrum.perun.core.api.AuthzResolver.unsetRole(ac.getSession(),
						users,
						roleName,
						parms.readPerunBean("complementaryObject"));
					return null;
				} else {
					throw new RpcException(RpcException.Type.MISSING_VALUE, "complementaryObject");
				}
			} else if (parms.contains("authorizedGroups")) {
				if (parms.contains("complementaryObject")) {
					int[] groupIds = parms.readArrayOfInts("authorizedGroups");
					List<Group> groups = new ArrayList<>(groupIds.length);
					for (int groupId : groupIds) {
						groups.add(ac.getGroupById(groupId));
					}
					cz.metacentrum.perun.core.api.AuthzResolver.unsetRole(ac.getSession(),
						groups,
						parms.readPerunBean("complementaryObject"),
						roleName);
					return null;
				} else {
					throw new RpcException(RpcException.Type.MISSING_VALUE, "complementaryObject");
				}
			} else if(parms.contains("authorizedGroup")) {
				if(parms.contains("complementaryObject")) {
					cz.metacentrum.perun.core.api.AuthzResolver.unsetRole(ac.getSession(),
								ac.getGroupById(parms.readInt("authorizedGroup")),
								parms.readPerunBean("complementaryObject"),
								roleName);
					return null;
				} else if (parms.contains("complementaryObjects")) {
					cz.metacentrum.perun.core.api.AuthzResolver.unsetRole(ac.getSession(),
								ac.getGroupById(parms.readInt("authorizedGroup")),
								roleName,
								parms.readListPerunBeans("complementaryObjects"));
					return null;
				} else {
					throw new RpcException(RpcException.Type.MISSING_VALUE, "list of complementary objects or complementary object");
				}

			} else {
				 throw new RpcException(RpcException.Type.MISSING_VALUE, "user(s) or authorizedGroup(s)");
			}
		}
	},

	/*#
	 * Returns 1 if User has VO manager role (VOADMIN) for specific VO defined by ID.
	 *
	 * @param vo int <code>id</code> of object VO
	 * @exampleResponse 1
	 * @return int 1 == <code>true</code>, 0 == <code>false</code>
	 */
	/*#
	 * Returns 1 if User has VO manager role (VOADMIN).
	 *
	 * @exampleResponse 1
	 * @return int 1 == <code>true</code>, 0 == <code>false</code>
	 */
	isVoAdmin {
		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			if(parms.contains("vo")) {
				if(ac.getSession().getPerunPrincipal().getRoles().hasRole(
					Role.VOADMIN, ac.getVoById(parms.readInt("vo")))) return 1;
				else return 0;
			} else {
				if (cz.metacentrum.perun.core.api.AuthzResolver.isVoAdmin(ac.getSession())) return 1;
				else return 0;
			}
		}
	},

	/*#
	 * Returns 1 if User has Group manager role (GROUPADMIN) for specific Group defined by ID.
	 *
	 * @param group int <code>id</code> of object Group
	 * @exampleResponse 1
	 * @return int 1 == <code>true</code>, 0 == <code>false</code>
	 */
	/*#
	 * Returns 1 if User has Group manager role (GROUPADMIN).
	 *
	 * @exampleResponse 1
	 * @return int 1 == <code>true</code>, 0 == <code>false</code>
	 */
	isGroupAdmin {
		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("group")){
				if(ac.getSession().getPerunPrincipal().getRoles().hasRole(
					Role.GROUPADMIN, ac.getGroupById(parms.readInt("group")))) return 1;
				else return 0;
			} else{
				if (cz.metacentrum.perun.core.api.AuthzResolver.isGroupAdmin(ac.getSession())) return 1;
				else return 0;
			}
		}
	},

	/*#
	 * Returns 1 if User has Facility manager role (FACILITYADMIN) for specific Facility defined by ID.
	 *
	 * @param facility int <code>id</code> of object Facility
	 * @exampleResponse 1
	 * @return int 1 == <code>true</code>, 0 == <code>false</code>
	 */
	/*#
	 * Returns 1 if User has Facility manager role (FACILITYADMIN).
	 *
	 * @exampleResponse 1
	 * @return int 1 == <code>true</code>, 0 == <code>false</code>
	 */
	isFacilityAdmin {
		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			if(parms.contains("facility")) {
				if(ac.getSession().getPerunPrincipal().getRoles().hasRole(
					Role.FACILITYADMIN, ac.getFacilityById(parms.readInt("facility")))) return 1;
				else return 0;
			} else {
				if (cz.metacentrum.perun.core.api.AuthzResolver.isFacilityAdmin(ac.getSession())) return 1;
				else return 0;
			}
		}
	},

	/*#
	 * Returns 1 if User has Perun admin role (perunadmin).
	 *
	 * @exampleResponse 1
	 * @return int 1 == <code>true</code>, 0 == <code>false</code>
	 */
	isPerunAdmin {
		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (cz.metacentrum.perun.core.api.AuthzResolver.isPerunAdmin(ac.getSession())) {
				return 1;
			} else return 0;
		}
	},

	/*#
	 * Returns User which is associated with credentials used to log-in to Perun.
	 *
	 * @return User Currently logged user
	 */
	getLoggedUser {
		@Override
		public User call(ApiCaller ac, Deserializer parms) throws PerunException {
			return cz.metacentrum.perun.core.api.AuthzResolver.getLoggedUser(ac.getSession());
		}
	},

	/*#
	 * Returns PerunPrincipal object associated with current session. It contains necessary information,
	 * including user identification, authorization and metadata. Each call of this method refresh the
	 * session including authorization data.
	 *
	 * @return PerunPrincipal PerunPrincipal object
	 */
	getPerunPrincipal {
		@Override
		public PerunPrincipal call(ApiCaller ac, Deserializer parms) throws PerunException {
			return cz.metacentrum.perun.core.api.AuthzResolver.getPerunPrincipal(ac.getSession());
		}
	},

	/*#
	 * Returns "OK" string. Helper method for GUI check if connection is alive.
	 *
	 * @exampleResponse "OK"
	 * @return String "OK"
	 */
	keepAlive {
		@Override
		public String call(ApiCaller ac, Deserializer parms) throws PerunException {
			return "OK";
		}
	},

	/*#
	 * Return all loaded perun policies.
	 *
	 * @return List<PerunPolicy> all loaded policies
	 */
	getAllPolicies {
		@Override
		public List<PerunPolicy> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return AuthzResolver.getAllPolicies();
		}
	},

	/*#
	 * Load perun roles and policies from the configuration file perun-roles.yml.
	 * Roles are loaded to the database and policies are loaded to the PerunPoliciesContainer.
	 */
	loadAuthorizationComponents {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			cz.metacentrum.perun.core.api.AuthzResolver.loadAuthorizationComponents(ac.getSession());
			return null;
		}
	};
}
