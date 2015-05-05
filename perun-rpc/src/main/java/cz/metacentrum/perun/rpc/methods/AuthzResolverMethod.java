package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.Group;
import java.util.List;

import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

public enum AuthzResolverMethod implements ManagerMethod {

	/*#
	 * Returns list of caller's role names.
	 * 
	 * @exampleResponse [ "groupadmin" , "self" , "voadmin" ]
	 * @return List<String> List of roles
	 */
	getPrincipalRoleNames {
		@Override
		public List<String> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return cz.metacentrum.perun.core.api.AuthzResolver.getPrincipalRoleNames(ac.getSession());
		}
	},
	/*#
	 * Get all managers for complementaryObject and role with specified attributes.
	 *
	 * @param role String Expected Role to filter managers by (perunadmin | voadmin | groupadmin | self | facilityadmin | voobserver | topgroupcreator)
	 * @param complementaryObjectId int Property <code>id</code> of complementaryObject to get managers for
	 * @param complementaryObjectName String Property <code>beanName</code> of complementaryObject, meaning object type (Vo | Group | Facility | ... )
	 * @param onlyDirectAdmins boolean When true, return only direct users of the complementary object for role with specific attributes
	 * @param allUserAttributes boolean When true, do not specify attributes through list and return them all in objects richUser. Ignoring list of specific attributes
	 * @return List<RichUser> Administrators for complementary object and role with specify attributes
	 */
	getRichAdmins {
		@Override
		public List<RichUser> call(ApiCaller ac, Deserializer parms) throws PerunException {
			//get role by name
			String roleName = parms.readString("role");
			Role role;
			try {
				role = Role.valueOf(roleName);
			} catch (IllegalArgumentException ex) {
				throw new RpcException(RpcException.Type.WRONG_PARAMETER, "wrong parameter in role, not exists role with this name " + roleName);
			}

			return cz.metacentrum.perun.core.api.AuthzResolver.getRichAdmins(ac.getSession(),
							parms.readInt("complementaryObjectId"),
							parms.readString("complementaryObjectName"),
							parms.readList("specificAttributes", String.class),
							role, parms.readBoolean("onlyDirectAdmins"),
							parms.readBoolean("allUserAttributes"));
		}
	},

	/*#
	 * Get all groups of managers (authorizedGroups) for complementaryObject and role.
	 *
	 * @param role String Expected Role to filter authorizedGroups by (perunadmin | voadmin | groupadmin | self | facilityadmin | voobserver | topgroupcreator)
	 * @param complementaryObjectId int Property <code>id</code> of complementaryObject to get groups of managers for
	 * @param complementaryObjectName String Property <code>beanName</code> of complementaryObject, meaning object type (Vo | Group | Facility | ... )
	 * @return List<Group> List of authorizedGroups for complementaryObject and role
	 */
	getAdminGroups {
		@Override
		public List<Group> call(ApiCaller ac, Deserializer parms) throws PerunException {
		//get role by name
			String roleName = parms.readString("role");
			Role role;
			try {
				role = Role.valueOf(roleName);
			} catch (IllegalArgumentException ex) {
				throw new RpcException(RpcException.Type.WRONG_PARAMETER, "wrong parameter in role, not exists role with this name " + roleName);
			}

			return cz.metacentrum.perun.core.api.AuthzResolver.getAdminGroups(ac.getSession(),
							parms.readInt("complementaryObjectId"),
							parms.readString("complementaryObjectName"),
							role);
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
	 * @param role String Role of user in a session ( perunadmin | voadmin | groupadmin | self | facilityadmin | voobserver | topgroupcreator )
	 * @param user int <code>id</code> of User to set role for
	 * @param complementaryObject Object Object ( vo | group | facility ) to associate role and user with.
	 * @exampleParam role "voadmin"
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
	 * @param role String Role of user in a session ( perunadmin | voadmin | groupadmin | self | facilityadmin | voobserver | topgroupcreator )
	 * @param user int <code>id</code> of User to set role for
	 * @param complementaryObjects List<Object> Objects ( vo | group | facility ) to associate role and user with
	 * @exampleParam role "voadmin"
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
	 * @param role String Role of user in a session ( perunadmin | voadmin | groupadmin | self | facilityadmin | voobserver | topgroupcreator )
	 * @param authorizedGroup int <code>id</code> of Group to set role for
	 * @param complementaryObject Object Object ( vo | group | facility ) to associate role and authorizedGroup with
	 * @exampleParam role "voadmin"
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
	 * @param role String Role of user in a session ( perunadmin | voadmin | groupadmin | self | facilityadmin | voobserver | topgroupcreator )
	 * @param authorizedGroup int <code>id</code> of Group to set role for
	 * @param complementaryObjects List<Object> Objects ( vo | group | facility ) to associate role and authorizedGroup with
	 * @exampleParam role "voadmin"
	 * @exampleParam complementaryObjects [ { "id" : 123 , "name" : "My testing VO" , "shortName" : "test_vo" , "beanName" : "Vo" } , {...} , {...} ]
	 */
	setRole {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			//get role by name
			String roleName = parms.readString("role");
			Role role;
			try {
				role = Role.valueOf(roleName);
			} catch (IllegalArgumentException ex) {
				throw new RpcException(RpcException.Type.WRONG_PARAMETER, "wrong parameter in role, not exists role with this name " + roleName);
			}

			if(parms.contains("user")) {
				if(parms.contains("complementaryObject")) {
					cz.metacentrum.perun.core.api.AuthzResolver.setRole(ac.getSession(),
								ac.getUserById(parms.readInt("user")),
								parms.readPerunBean("complementaryObject"),
								role);
					return null;
				} else if(parms.contains("complementaryObjects")) {
					cz.metacentrum.perun.core.api.AuthzResolver.setRole(ac.getSession(),
								ac.getUserById(parms.readInt("user")),
								role,
								parms.readListPerunBeans("complementaryObjects"));
					return null;
				} else {
					throw new RpcException(RpcException.Type.MISSING_VALUE, "list of complementary objects or complementary object");
				}
			} else if(parms.contains("authorizedGroup")) {
				if(parms.contains("complementaryObject")) {
					cz.metacentrum.perun.core.api.AuthzResolver.setRole(ac.getSession(),
								ac.getGroupById(parms.readInt("authorizedGroup")),
								parms.readPerunBean("complementaryObject"),
								role);
					return null;
				} else if(parms.contains("complementaryObjects")) {
					cz.metacentrum.perun.core.api.AuthzResolver.setRole(ac.getSession(),
								ac.getGroupById(parms.readInt("authorizedGroup")),
								role,
								parms.readListPerunBeans("complementaryObjects"));
					return null;
				} else {
					throw new RpcException(RpcException.Type.MISSING_VALUE, "list of complementary objects or complementary object");
				}
			} else {
				 throw new RpcException(RpcException.Type.MISSING_VALUE, "user or authorizedGroup");
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
	 * @param role String Role of user in a session ( perunadmin | voadmin | groupadmin | self | facilityadmin | voobserver | topgroupcreator )
	 * @param user int <code>id</code> of User to unset role for
	 * @param complementaryObject Object Object ( vo | group | facility ) to remove role for a user
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
	 * @param role String Role of user in a session ( perunadmin | voadmin | groupadmin | self | facilityadmin | voobserver | topgroupcreator )
	 * @param user int <code>id</code> of User to unset role for
	 * @param complementaryObjects List<Object> Objects ( vo | group | facility ) to remove role for a user
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
	 * @param role String Role of user in a session ( perunadmin | voadmin | groupadmin | self | facilityadmin | voobserver | topgroupcreator )
	 * @param authorizedGroup int <code>id</code> of Group to unset role for
	 * @param complementaryObject Object Object ( vo | group | facility ) to remove role for an authorizedGroup
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
	 * @param role String Role of user in a session ( perunadmin | voadmin | groupadmin | self | facilityadmin | voobserver | topgroupcreator )
	 * @param authorizedGroup int <code>id</code> of Group to unset role for
	 * @param complementaryObjects List<Object> Objects ( vo | group | facility ) to remove role for an authorizedGroup
	 * @exampleParam role "voadmin"
	 * @exampleParam complementaryObjects [ { "id" : 123 , "name" : "My testing VO" , "shortName" : "test_vo" , "beanName" : "Vo" } , {...} , {...} ]
	 */
	unsetRole {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();
			//get role by name
			String roleName = parms.readString("role");
			Role role;
			try {
				role = Role.valueOf(roleName);
			} catch (IllegalArgumentException ex) {
				throw new RpcException(RpcException.Type.WRONG_PARAMETER, "wrong parameter in role, not exists role with this name " + roleName);
			}

			if(parms.contains("user")) {
				if(parms.contains("complementaryObject")) {
					cz.metacentrum.perun.core.api.AuthzResolver.unsetRole(ac.getSession(),
								ac.getUserById(parms.readInt("user")),
								parms.readPerunBean("complementaryObject"),
								role);
					return null;
				} else if (parms.contains("complementaryObjects")) {
					cz.metacentrum.perun.core.api.AuthzResolver.unsetRole(ac.getSession(),
								ac.getUserById(parms.readInt("user")),
								role,
								parms.readListPerunBeans("complementaryObjects"));
					return null;
				} else {
					throw new RpcException(RpcException.Type.MISSING_VALUE, "list of complementary objects or complementary object");
				}		
			} else if(parms.contains("authorizedGroup")) {
				if(parms.contains("complementaryObject")) {
					cz.metacentrum.perun.core.api.AuthzResolver.unsetRole(ac.getSession(),
								ac.getGroupById(parms.readInt("authorizedGroup")),
								parms.readPerunBean("complementaryObject"),
								role);
					return null;
				} else if (parms.contains("complementaryObjects")) {
					cz.metacentrum.perun.core.api.AuthzResolver.unsetRole(ac.getSession(),
								ac.getGroupById(parms.readInt("authorizedGroup")),
								role,
								parms.readListPerunBeans("complementaryObjects"));
					return null;
				} else {
					throw new RpcException(RpcException.Type.MISSING_VALUE, "list of complementary objects or complementary object");
				}
				
			} else {
				 throw new RpcException(RpcException.Type.MISSING_VALUE, "user or authorizedGroup");
			}
		}
	},

	/*#
	 * Returns 1 if User has VO manager role (voadmin).
	 * 
	 * @exampleResponse 1
	 * @return int 1 == <code>true</code>, 0 == <code>false</code>
	 */
	isVoAdmin {
		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (cz.metacentrum.perun.core.api.AuthzResolver.isVoAdmin(ac.getSession())) {
				return 1;
			} else return 0;
		}
	},

	/*#
	 * Returns 1 if User has Group manager role (groupadmin).
	 * 
	 * @exampleResponse 1
	 * @return int 1 == <code>true</code>, 0 == <code>false</code>
	 */
	isGroupAdmin {
		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (cz.metacentrum.perun.core.api.AuthzResolver.isGroupAdmin(ac.getSession())) {
				return 1;
			} else return 0;
		}
	},

	/*#
	 * Returns 1 if User has Facility manager role (facilityadmin).
	 * 
	 * @exampleResponse 1
	 * @return int 1 == <code>true</code>, 0 == <code>false</code>
	 */
	isFacilityAdmin {
		@Override
		public Integer call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (cz.metacentrum.perun.core.api.AuthzResolver.isFacilityAdmin(ac.getSession())) {
				return 1;
			} else return 0;
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
	};

}
