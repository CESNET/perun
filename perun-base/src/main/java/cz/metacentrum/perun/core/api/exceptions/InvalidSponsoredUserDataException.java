package cz.metacentrum.perun.core.api.exceptions;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class InvalidSponsoredUserDataException extends PerunException {

  static final long serialVersionUID = 0;

  public InvalidSponsoredUserDataException() {
  }

  public InvalidSponsoredUserDataException(String message) {
    super(message);
  }

  public InvalidSponsoredUserDataException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidSponsoredUserDataException(Throwable cause) {
    super(cause);
  }

  @Override
  public String getFriendlyMessageTemplate() {
    return "Invalid data provided for the creation of a sponsored user.";
  }
}
