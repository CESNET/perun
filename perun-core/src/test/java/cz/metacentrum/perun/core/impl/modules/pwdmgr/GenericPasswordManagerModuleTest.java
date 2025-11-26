package cz.metacentrum.perun.core.impl.modules.pwdmgr;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.exceptions.PasswordStrengthException;
import cz.metacentrum.perun.core.bl.PerunBl;
import org.junit.Before;
import org.junit.Test;

public class GenericPasswordManagerModuleTest extends AbstractPerunIntegrationTest {
  private GenericPasswordManagerModule module;

  @Before
  public void setUp() {
    this.module = (GenericPasswordManagerModule) ((PerunBl) sess.getPerun()).getUsersManagerBl()
      .getPasswordManagerModule(sess, "generic");
  }

  @Test
  public void checkPasswordStrengthPasswordContainsLogin() {
    String password = "jannovak1234";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
        () -> module.checkPasswordStrength(sess, "JanNovak", password));
  }

  @Test
  public void checkPasswordStrengthPasswordContainsLoginBackwards() {
    String password = "kavonnaj1234";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
        () -> module.checkPasswordStrength(sess, "JanNovak", password));
  }

  @Test
  public void checkPasswordStrengthPasswordContainsPartsOfLoginUnderscore() {
    String password = "novak1234567";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
        () -> module.checkPasswordStrength(sess, "Jan_Novak", password));
  }

  @Test
  public void checkPasswordStrengthPasswordContainsPartsOfLoginSlash() {
    String password = "honzanovak123";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
        () -> module.checkPasswordStrength(sess, "Jan-Novak", password));
  }

  @Test
  public void checkPasswordStrengthPasswordContainsTooSmallPartsOfLogin() throws PasswordStrengthException {
    String password = "NotBadPassword123";
    module.checkPasswordStrength(sess, "Jan-No", password);
  }

  @Test
  public void checkPasswordStrengthPasswordContainsPartOfLoginBackwards() {
    String password = "kavonnaj1234";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
        () -> module.checkPasswordStrength(sess, "Jan_No", password));
  }
}
