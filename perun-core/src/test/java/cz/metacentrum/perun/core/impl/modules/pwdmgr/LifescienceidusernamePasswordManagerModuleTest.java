package cz.metacentrum.perun.core.impl.modules.pwdmgr;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.exceptions.PasswordStrengthException;
import cz.metacentrum.perun.core.bl.PerunBl;
import org.junit.Before;
import org.junit.Test;

public class LifescienceidusernamePasswordManagerModuleTest extends AbstractPerunIntegrationTest {
  private LifescienceidusernamePasswordManagerModule module;


  @Test
  public void passwordShouldPass() throws Exception {
    String password = "Testetest123!";
    module.checkPasswordStrength(sess, "meaningfullogin", password);
  }

  @Test
  public void passwordSpecialCharacterMissing() {
    String password = "Testetest123";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
        () -> module.checkPasswordStrength(sess, "test", password));
  }

  @Test
  public void passwordLowerCharacterMissing() {
    String password = "TESTETEST123!";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
        () -> module.checkPasswordStrength(sess, "test", password));
  }

  @Test
  public void passwordUpperCharacterMissing() {
    String password = "testetest123";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
        () -> module.checkPasswordStrength(sess, "test", password));
  }

  @Test
  public void passwordDigitCharacterMissing() {
    String password = "Testetest!";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
        () -> module.checkPasswordStrength(sess, "test", password));
  }

  @Test
  public void passwordOnlySpecialTest() {
    String password = "!@#$%^&*_-+=?/";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
        () -> module.checkPasswordStrength(sess, "test", password));
  }

  @Test
  public void passwordOnlyLowerTest() {
    String password = "eakfjklsjweiml";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
        () -> module.checkPasswordStrength(sess, "test", password));
  }

  @Test
  public void passwordOnlyDigitTest() {
    String password = "18746982365414";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
        () -> module.checkPasswordStrength(sess, "test", password));
  }

  @Test
  public void passwordLowerUpperTest() {
    String password = "AkoHCNqrXMsuqm";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
        () -> module.checkPasswordStrength(sess, "test", password));
  }

  @Before
  public void setUp() {
    this.module = (LifescienceidusernamePasswordManagerModule) ((PerunBl) sess.getPerun()).getUsersManagerBl()
                                                                   .getPasswordManagerModule(sess,
                                                                       "lifescienceid-username");
  }
}
