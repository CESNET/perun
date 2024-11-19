package cz.metacentrum.perun.core.api.exceptions.rt;

public class RolesConfigurationException extends PerunRuntimeException {

  public RolesConfigurationException(String message) {
    super(message);
  }

  public RolesConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }

  public RolesConfigurationException(Throwable cause) {
    super(cause);
  }
}
