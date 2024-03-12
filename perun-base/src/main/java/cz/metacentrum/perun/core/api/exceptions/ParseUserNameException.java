package cz.metacentrum.perun.core.api.exceptions;

/**
 * This exception is thrown when there is missing important part after parsing user name. Mandatory parts of user name
 * in perun is "first name" and "last name".
 *
 * @author Michal Stava stavamichal@gmail.com
 */
public class ParseUserNameException extends IllegalArgumentException {

  private String unparsedName;

  /**
   * Simple constructor with a message
   *
   * @param message message with details about the cause
   */
  public ParseUserNameException(String message) {
    super(message);
  }

  /**
   * Constructor with a message and unparsed user name.
   *
   * @param message      message with details about the cause
   * @param unparsedName name in raw format before parsing
   */
  public ParseUserNameException(String message, String unparsedName) {
    super(message);
    this.unparsedName = unparsedName;
  }

  /**
   * Getter for the unparsed name
   *
   * @return unparsed name in raw text format
   */
  public String getUnparsedName() {
    return unparsedName;
  }

  /**
   * Setter for the unparsed name
   *
   * @param unparsed name in raw text format
   */
  public void setUnparsedName(String unparsedName) {
    this.unparsedName = unparsedName;
  }
}
