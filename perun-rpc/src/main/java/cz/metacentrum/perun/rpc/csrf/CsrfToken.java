package cz.metacentrum.perun.rpc.csrf;

import java.util.UUID;

/**
 * CsrfToken is used to prevent CSRF attack in web browsers.
 * This class simply generates token value and holds it within self.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class CsrfToken {

  private String value = null;

  /**
   * Create CSRF token with new random value
   */
  public CsrfToken() {
    this.value = UUID.randomUUID().toString();
  }

  /**
   * Create CSRF token with specified value
   *
   * @param value Value of the Token
   */
  public CsrfToken(String value) {
    this.value = value;
  }

  /**
   * Get value of CSRF token.
   *
   * @return value of CSRF token
   */
  public String getValue() {
    return value;
  }

  /**
   * Set value of CSRF token
   *
   * @param value value of CSRF token
   */
  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "CsrfToken:[" +
        "value='" + value + '\'' +
        ']';
  }

}
