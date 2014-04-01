package cz.metacentrum.perun.rpc.methods;

import java.util.List;

import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
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

    /*#
     * Returns 1 whether user is a VO admin.
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
     * Returns 1 whether user is a Group admin.
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
     * Returns 1 whether user is a Facility admin.
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
     * Returns 1 whether user is a Perun admin.
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
     */
    keepAlive {
        @Override
        public String call(ApiCaller ac, Deserializer parms) throws PerunException {
            return "OK";
        }
    };

}
