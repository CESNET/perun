package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;

/**
 * Application mail does not exist but something tries to remove it.
 *
 * @author Metodej Klang
 */
public class ApplicationMailAlreadyRemovedException extends PerunException {

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public ApplicationMailAlreadyRemovedException(String message) {
    super(message);
  }
}
