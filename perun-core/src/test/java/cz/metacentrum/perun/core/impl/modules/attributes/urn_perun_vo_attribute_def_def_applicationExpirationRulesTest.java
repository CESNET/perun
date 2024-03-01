package cz.metacentrum.perun.core.impl.modules.attributes;

import static cz.metacentrum.perun.core.implApi.modules.attributes.AbstractApplicationExpirationRulesModule.APPLICATION_IGNORED_BY_ADMIN_KEY_NAME;
import static cz.metacentrum.perun.core.implApi.modules.attributes.AbstractApplicationExpirationRulesModule.APPLICATION_WAITING_FOR_EMAIL_VERIFICATION_KEY_NAME;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_vo_attribute_def_def_applicationExpirationRulesTest {

  private static final PerunSessionImpl session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
  private static urn_perun_vo_attribute_def_def_applicationExpirationRules classInstance;
  private static Attribute attributeToCheck;
  private static Vo vo;

  @Test
  public void autoExpirationIgnoredByAdminCorrectSyntax() throws Exception {
    Map<String, String> value = new LinkedHashMap<>();
    value.put(APPLICATION_IGNORED_BY_ADMIN_KEY_NAME, "60");
    attributeToCheck.setValue(value);
    classInstance.checkAttributeSyntax(session, vo, attributeToCheck);
  }

  // ------------- Syntax ------------- //

  @Test
  public void autoExpirationIgnoredByAdminIncorrectSyntax() {
    Map<String, String> value = new LinkedHashMap<>();
    value.put(APPLICATION_IGNORED_BY_ADMIN_KEY_NAME, "-60");
    attributeToCheck.setValue(value);
    assertThatExceptionOfType(WrongAttributeValueException.class).isThrownBy(
        () -> classInstance.checkAttributeSyntax(session, vo, attributeToCheck));
  }

  @Test
  public void autoExpirationWaitingForEmailCorrectSyntax() throws Exception {
    Map<String, String> value = new LinkedHashMap<>();
    value.put(APPLICATION_WAITING_FOR_EMAIL_VERIFICATION_KEY_NAME, "14");
    attributeToCheck.setValue(value);
    classInstance.checkAttributeSyntax(session, vo, attributeToCheck);
  }

  @Test
  public void autoExpirationWaitingForEmailIncorrectSyntax() {
    Map<String, String> value = new LinkedHashMap<>();
    value.put(APPLICATION_WAITING_FOR_EMAIL_VERIFICATION_KEY_NAME, "-14");
    attributeToCheck.setValue(value);
    assertThatExceptionOfType(WrongAttributeValueException.class).isThrownBy(
        () -> classInstance.checkAttributeSyntax(session, vo, attributeToCheck));
  }

  @Before
  public void setUp() {
    classInstance = new urn_perun_vo_attribute_def_def_applicationExpirationRules();
    attributeToCheck = new Attribute();
    vo = new Vo();
  }
}
