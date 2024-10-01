package cz.metacentrum.perun.core.api.exceptions;

public class ServiceOnlyRoleAssignedException extends PerunException {

  /**
   * Exception thrown when trying to remove service user flag from user with service only role.
   * @param message message with details about the cause
   */
  public ServiceOnlyRoleAssignedException(String message) {
    super(message);
  }

}
