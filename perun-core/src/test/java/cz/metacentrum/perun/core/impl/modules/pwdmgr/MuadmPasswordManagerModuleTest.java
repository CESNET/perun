package cz.metacentrum.perun.core.impl.modules.pwdmgr;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.exceptions.PasswordStrengthException;
import cz.metacentrum.perun.core.bl.PerunBl;
import org.junit.Before;
import org.junit.Test;

public class MuadmPasswordManagerModuleTest extends AbstractPerunIntegrationTest {

  private MuadmPasswordManagerModule module;

  @Test
  public void passwordContainsLoginTest() {
    String password = "1234test567890";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
            () -> module.checkPasswordStrength(sess, "test", password))
        .withMessageContaining("must not match/contain login or backwards login");
  }

  @Test
  public void passwordContainsReverseLoginTest() {
    String password = "1234tset567890";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
            () -> module.checkPasswordStrength(sess, "test", password))
        .withMessageContaining("must not match/contain login or backwards login");
  }

  @Test
  public void passwordDigitsSpecialTest() {
    String password = "265?!8+789#_=^";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
            () -> module.checkPasswordStrength(sess, "test", password))
        .withMessageContaining("It has to contain character from at least 3 of these categories");
  }

  @Test
  public void passwordLowerDigitsTest() {
    String password = "afgr487xiy2m63";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
            () -> module.checkPasswordStrength(sess, "test", password))
        .withMessageContaining("It has to contain character from at least 3 of these categories");
  }

  @Test
  public void passwordLowerSpecialTest() {
    String password = "!lop@#twoep&*/";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
            () -> module.checkPasswordStrength(sess, "test", password))
        .withMessageContaining("It has to contain character from at least 3 of these categories");
  }

  @Test
  public void passwordLowerUpperDigitsSpecialTest() {
    String password = "jfsUOIW5798+-*";
    assertThatNoException().isThrownBy(() -> module.checkPasswordStrength(sess, "login", password));
  }

  @Test
  public void passwordLowerUpperDigitsTest() {
    String password = "bcvjoEWIQO!@#$";
    assertThatNoException().isThrownBy(() -> module.checkPasswordStrength(sess, "login", password));
  }

  @Test
  public void passwordLowerUpperSpecialTest() {
    String password = "viwuq24736/?+-";
    assertThatNoException().isThrownBy(() -> module.checkPasswordStrength(sess, "login", password));
  }

  @Test
  public void passwordLowerUpperTest() {
    String password = "AkoHCNqrXMsuqm";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
            () -> module.checkPasswordStrength(sess, "test", password))
        .withMessageContaining("It has to contain character from at least 3 of these categories");
  }

  @Test
  public void passwordOnlyDigitTest() {
    String password = "18746982365414";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
            () -> module.checkPasswordStrength(sess, "test", password))
        .withMessageContaining("It has to contain character from at least 3 of these categories");
  }

  @Test
  public void passwordOnlyLowerTest() {
    String password = "eakfjklsjweiml";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
            () -> module.checkPasswordStrength(sess, "test", password))
        .withMessageContaining("It has to contain character from at least 3 of these categories");
  }

  @Test
  public void passwordOnlySpecialTest() {
    String password = "!@#$%^&*_-+=?/";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
            () -> module.checkPasswordStrength(sess, "test", password))
        .withMessageContaining("It has to contain character from at least 3 of these categories");
  }

  @Test
  public void passwordOnlyUpperTest() {
    String password = "LOSFNXHRKJANCS";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
            () -> module.checkPasswordStrength(sess, "test", password))
        .withMessageContaining("It has to contain character from at least 3 of these categories");
  }

  @Test
  public void passwordTooShortTestLength6() {
    String password = "123456";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
        () -> module.checkPasswordStrength(sess, "test", password)).withMessageContaining("is too short");
  }

  @Test
  public void passwordTooShortTestLength13() {
    String password = "1234567890123";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
        () -> module.checkPasswordStrength(sess, "test", password)).withMessageContaining("is too short");
  }

  @Test
  public void passwordUpperDigitsSpecialTest() {
    String password = "WMKCO03877@#/*";
    assertThatNoException().isThrownBy(() -> module.checkPasswordStrength(sess, "login", password));
  }

  @Test
  public void passwordUpperDigitsTest() {
    String password = "RT672HWZ1068KK";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
            () -> module.checkPasswordStrength(sess, "test", password))
        .withMessageContaining("It has to contain character from at least 3 of these categories");
  }

  @Test
  public void passwordUpperSpecialTest() {
    String password = "A!@#KDSAJ?/NMO";
    assertThatExceptionOfType(PasswordStrengthException.class).isThrownBy(
            () -> module.checkPasswordStrength(sess, "test", password))
        .withMessageContaining("It has to contain character from at least 3 of these categories");
  }

  @Before
  public void setUp() {
    this.module = (MuadmPasswordManagerModule) ((PerunBl) sess.getPerun()).getUsersManagerBl()
        .getPasswordManagerModule(sess, "mu-adm");
  }
}
