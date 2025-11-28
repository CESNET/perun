package cz.metacentrum.perun.core.impl.modules.pwdmgr;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.PasswordStrengthException;
import cz.metacentrum.perun.core.bl.PerunBl;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Radoslav Čerhák <r.cerhak@gmail.com>
 */
public class AdminmetaPasswordManagerModuleTest extends AbstractPerunIntegrationTest {

  private AdminmetaPasswordManagerModule module;

  @Test
  public void loginTooLongTest() {
    assertThatExceptionOfType(InvalidLoginException.class).isThrownBy(
        () -> module.checkLoginFormat(sess, "aaaaaaaaaaaa1234"));
  }

  @Test
  public void loginValidTest() {
    assertThatNoException().isThrownBy(() -> module.checkLoginFormat(sess, "abc123"));
  }

  @Test
  public void loginWithInvalidCharTest() {
    assertThatExceptionOfType(InvalidLoginException.class).isThrownBy(() -> module.checkLoginFormat(sess, "abc123["));
  }

  @Test
  public void passwordContainsLoginTest() {
    String password = "1234test567890";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
        () -> module.checkPasswordStrength(sess, "test", password));
  }

  @Test
  public void passwordContainsReverseLoginTest() {
    String password = "1234tset567890";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
        () -> module.checkPasswordStrength(sess, "test", password));
  }

  @Test
  public void passwordIsValidTest() {
    String password = "viwuq24736/?+-";
    assertThatNoException().isThrownBy(() -> module.checkPasswordStrength(sess, "login", password));
  }

  @Test
  public void passwordIsWeakTest() {
    String password = "bcvjoEWIQOfdsa";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
        () -> module.checkPasswordStrength(sess, "login", password));
  }

  @Test
  public void passwordTooShortTestLength6() {
    String password = "123456";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
        () -> module.checkPasswordStrength(sess, "test", password));
  }

  @Test
  public void passwordTooShortTestLength11() {
    String password = "12345678901";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
        () -> module.checkPasswordStrength(sess, "test", password));
  }

  @Before
  public void setUp() {
    this.module = (AdminmetaPasswordManagerModule) ((PerunBl) sess.getPerun()).getUsersManagerBl()
        .getPasswordManagerModule(sess, "admin-meta");
  }
}
