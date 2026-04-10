package cz.metacentrum.perun.core.api.exceptions.rt;

/**
 * Thrown when the required OIDC claims are not sent by new registrar.
 */
public class MissingOidcAttributesRuntimeException extends PerunRuntimeException {

  public MissingOidcAttributesRuntimeException(String message) {
    super(message);
  }

  public MissingOidcAttributesRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  public MissingOidcAttributesRuntimeException(Throwable cause) {
    super(cause);
  }
}
