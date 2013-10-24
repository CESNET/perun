package cz.metacentrum.perun.core.implApi;

import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.impl.AuthzRoles;

/**
* This interface represents AuthzResolver methods.
*
* @author Michal Prochazka
 * @version $Id$
*/

public interface AuthzResolverImplApi {

  /**
   * Returns all user's roles.
   * 
   * @param user
   * @return AuthzRoles object which contains all roles with perunbeans
   */
  AuthzRoles getRoles(User user) throws InternalErrorException;
  
  /**
   * Removes all authz entries for the user.
   * 
   * 
   * @param user
   */
  void removeAllUserAuthz(PerunSession sess, User user) throws InternalErrorException;
}
