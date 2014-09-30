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

	/*# Set role for user and all complementary objects
	 * If list of complementary objects is empty, set general role instead (for no concrete objects)
	 *
	 * IMPORTANT: not refreshing authz, afected user is not the perunAdmin who call this method
	 */
	/*# Set role for auhtorizedGroup and all complementary objects
	 * If list of complementary objects is empty, set general role instead (for no concrete objects)
	 *
	 * IMPORTANT: not refreshing authz, afected group is not for the perunAdmin who call this method
	 */
	setRole {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			if(parms.contains("user")) {
				if(parms.contains("complementaryObject")) {
					cz.metacentrum.perun.core.api.AuthzResolver.setRole(ac.getSession(),
								parms.read("user", User.class),
								parms.read("complementaryObject", PerunBean.class),
								parms.read("role", Role.class));
					return null;
				} else if(parms.contains("complementaryObjects[]")) {
					cz.metacentrum.perun.core.api.AuthzResolver.setRole(ac.getSession(),
								parms.read("user", User.class),
								parms.read("role", Role.class),
								parms.readList("complementaryObjects", PerunBean.class));
					return null;
				} else {
					throw new RpcException(RpcException.Type.MISSING_VALUE, "list of complementary objects or complementary object");
				}
			} else if(parms.contains("authorizedGroup")) {
				if(parms.contains("complementaryObject")) {
					cz.metacentrum.perun.core.api.AuthzResolver.setRole(ac.getSession(),
								parms.read("authorizedGroup", Group.class),
								parms.read("complementaryObject", PerunBean.class),
								parms.read("role", Role.class));
					return null;
				} else if(parms.contains("complementaryObjects[]")) {
					cz.metacentrum.perun.core.api.AuthzResolver.setRole(ac.getSession(),
								parms.read("authorizedGroup", Group.class),
								parms.read("role", Role.class),
								parms.readList("complementaryObjects", PerunBean.class));
					return null;
				} else {
					throw new RpcException(RpcException.Type.MISSING_VALUE, "list of complementary objects or complementary object");
				}
			} else {
				 throw new RpcException(RpcException.Type.MISSING_VALUE, "user or authorizedGroup");
			}
		}
	},

	/*# Unset role for user and all complementary objects
	 * If list of complementary objects is empty, remove general role isntead (role without concrete objects)
	 *
	 * IMPORTANT: not refreshing authz, afected user is not the perunADmin who call this method
	 */
	/*# Unset role for group and all complementary objects
	 * If list of complementary objects is empty, set general role instead (for no concrete objects)
	 *
	 * IMPORTANT: not refreshing authz, afected group is not for the perunAdmin who call this method
	 */
	unsetRole {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			if(parms.contains("user")) {
				if(parms.contains("complementaryObject")) {
					cz.metacentrum.perun.core.api.AuthzResolver.unsetRole(ac.getSession(),
								parms.read("user", User.class),
								parms.read("complementaryObject", PerunBean.class),
								parms.read("role", Role.class));
					return null;
				} else if (parms.contains("complementaryObjects[]")) {
					cz.metacentrum.perun.core.api.AuthzResolver.unsetRole(ac.getSession(),
								parms.read("user", User.class),
								parms.read("role", Role.class),
								parms.readList("complementaryObjects", PerunBean.class));
					return null;
				} else {
					throw new RpcException(RpcException.Type.MISSING_VALUE, "list of complementary objects or complementary object");
				}		
			} else if(parms.contains("authorizedGroup")) {
				if(parms.contains("complementaryObject")) {
					cz.metacentrum.perun.core.api.AuthzResolver.unsetRole(ac.getSession(),
								parms.read("authorizedGroup", Group.class),
								parms.read("complementaryObject", PerunBean.class),
								parms.read("role", Role.class));
					return null;
				} else if (parms.contains("complementaryObjects[]")) {
					cz.metacentrum.perun.core.api.AuthzResolver.unsetRole(ac.getSession(),
								parms.read("authorizedGroup", Group.class),
								parms.read("role", Role.class),
								parms.readList("complementaryObjects", PerunBean.class));
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
