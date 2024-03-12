package cz.metacentrum.perun.scim.api.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;

/**
 * Exception for SCIM module.
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 08.10.2016
 */
public class SCIMException extends PerunException {

  public SCIMException() {
    super();
  }

  public SCIMException(String message) {
    super(message);
  }

  public SCIMException(Throwable cause) {
    super(cause);
  }

  public SCIMException(String message, Throwable cause) {
    super(message, cause);
  }
}
