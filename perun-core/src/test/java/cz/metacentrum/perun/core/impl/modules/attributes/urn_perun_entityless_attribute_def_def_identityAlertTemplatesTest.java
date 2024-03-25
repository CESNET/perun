package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.mock;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.LinkedHashMap;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_entityless_attribute_def_def_identityAlertTemplatesTest {
  private static urn_perun_entityless_attribute_def_def_identityAlertsTemplates classInstance;
  private static PerunSessionImpl session;
  private static Attribute attributeToCheck;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_entityless_attribute_def_def_identityAlertsTemplates();
    session = mock(PerunSessionImpl.class);
    attributeToCheck = new Attribute();
  }

  @Test
  public void testAllowedKey() {
    System.out.println("testAllowedKey");

    assertThatNoException().isThrownBy(() -> classInstance.checkAttributeSyntax(session, "en", attributeToCheck));
  }

  @Test
  public void testAllowedTemplate() {
    System.out.println("testAllowedTemplate");

    var value = new LinkedHashMap<String, String>();
    value.put("uesAddedPreferredMail", "Hi!");
    attributeToCheck.setValue(value);

    assertThatNoException().isThrownBy(() -> classInstance.checkAttributeSyntax(session, "en", attributeToCheck));
  }

  @Test
  public void testNotAllowedKey() {
    System.out.println("testNotAllowedKey");

    assertThatExceptionOfType(WrongAttributeValueException.class).isThrownBy(
        () -> classInstance.checkAttributeSyntax(session, "cc", attributeToCheck)).withMessageContaining("Invalid key");
  }

  @Test
  public void testNotAllowedTemplate() {
    System.out.println("testNotAllowedTemplate");

    var value = new LinkedHashMap<String, String>();
    value.put("uesEditedPreferredMail", "Hi!");
    attributeToCheck.setValue(value);

    assertThatExceptionOfType(WrongAttributeValueException.class).isThrownBy(
            () -> classInstance.checkAttributeSyntax(session, "en", attributeToCheck))
        .withMessageContaining("'uesEditedPreferredMail' is not allowed");
  }
}
