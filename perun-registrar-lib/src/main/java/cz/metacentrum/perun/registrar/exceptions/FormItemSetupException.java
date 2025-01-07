package cz.metacentrum.perun.registrar.exceptions;

import cz.metacentrum.perun.core.api.exceptions.PerunException;

/**
 * Exception to indicate an inconsistency in an application form item setup.
 */
public class FormItemSetupException extends PerunException {

  public FormItemSetupException(String message) {
    super(message);
  }

  public FormItemSetupException(String message, Throwable cause) {
    super(message, cause);
  }
}
