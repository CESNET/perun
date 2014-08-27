package cz.metacentrum.perun.rpc.methods;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunBean;
import java.util.List;

import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.RpcException;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

public enum AuthzResolverMethod implements ManagerMethod {

	/*#
	 * Returns principal role names.
	 * @return List<String> Roles
	 */
	getPrincipalRoleNames {
		@Override
		public List<String> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return cz.metacentrum.perun.core.api.AuthzResolver.getPrincipalRoleNames(ac.getSession());
		}
	},

	/*# Set role for user or authorized group and complementary object or objects
	 *
	 * If some complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore complementary object.
	 *
	 * IMPORTANT: refresh authz only if user in session is affected
	 */
	setRole {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
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
				} else if(parms.contains("complementaryObjects[]")) {
					cz.metacentrum.perun.core.api.AuthzResolver.setRole(ac.getSession(),
								ac.getUserById(parms.readInt("user")),
								role,
								parms.readListPerunBeans("complementaryObjects[]"));
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
				} else if(parms.contains("complementaryObjects[]")) {
					cz.metacentrum.perun.core.api.AuthzResolver.setRole(ac.getSession(),
								ac.getGroupById(parms.readInt("authorizedGroup")),
								role,
								parms.readListPerunBeans("complementaryObjects[]"));
					return null;
				} else {
					throw new RpcException(RpcException.Type.MISSING_VALUE, "list of complementary objects or complementary object");
				}
			} else {
				 throw new RpcException(RpcException.Type.MISSING_VALUE, "user or authorizedGroup");
			}
		}
	},

	/*# Unset role for user or authorized group and complementary object or objects
	 *
	 * If some complementary object is wrong for the role, throw an exception.
	 * For role "perunadmin" ignore complementary object.
	 *
	 * IMPORTANT: refresh authz only if user in session is affected
	 */
	unsetRole {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
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
				} else if (parms.contains("complementaryObjects[]")) {
					cz.metacentrum.perun.core.api.AuthzResolver.unsetRole(ac.getSession(),
								ac.getUserById(parms.readInt("user")),
								role,
								parms.readListPerunBeans("complementaryObjects[]"));
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
				} else if (parms.contains("complementaryObjects[]")) {
					cz.metacentrum.perun.core.api.AuthzResolver.unsetRole(ac.getSession(),
								ac.getGroupById(parms.readInt("authorizedGroup")),
								role,
								parms.readListPerunBeans("complementaryObjects[]"));
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
	 * Returns 1 if user is a VO admin.
	 * @return int 1 = true, 0 = false
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
	 * Returns 1 if user is a Group admin.
	 * @return int 1 = true, 0 = false
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
	 * Returns 1 if user is a Facility admin.
	 * @return int 1 = true, 0 = false
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
	 * Returns 1 if user is a Perun admin.
	 * @return int 1 = true, 0 = false
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
	 * Returns currently logged User object.
	 * @return User Current user
	 */
	getLoggedUser {
		@Override
		public User call(ApiCaller ac, Deserializer parms) throws PerunException {
			return cz.metacentrum.perun.core.api.AuthzResolver.getLoggedUser(ac.getSession());
		}
	},

	/*#
	 * Returns current PerunPrincipal object.
	 * @return PerunPrincipal PerunPrincipal
	 */
	getPerunPrincipal {
		@Override
		public PerunPrincipal call(ApiCaller ac, Deserializer parms) throws PerunException {
			return cz.metacentrum.perun.core.api.AuthzResolver.getPerunPrincipal(ac.getSession());
		}
	},

	/*#
	 * Returns "OK" string.
	 * Helper method for GUI to keep connection alive
	 *
	 * @return String "OK"
	 */
	keepAlive {
		@Override
		public String call(ApiCaller ac, Deserializer parms) throws PerunException {
			return "OK";
		}
	};

}
