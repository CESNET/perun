package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_group_attribute_def_def_applicationAutoRejectMessagesTest {

  private static urn_perun_group_attribute_def_def_applicationAutoRejectMessages classInstance;
  private static PerunSessionImpl session;
  private static Attribute attributeToCheck;
  private static Group group;

  @Before
  public void setUp() {
    classInstance = new urn_perun_group_attribute_def_def_applicationAutoRejectMessages();
    session = mock(PerunSessionImpl.class);
    attributeToCheck = new Attribute();
    group = new Group();
  }

  @Test
  public void testInvalidKey() {
    HashMap<String, String> value = new LinkedHashMap<>();
    value.put("emailVerification-", "message");
    attributeToCheck.setValue(value);

    assertThatExceptionOfType(WrongAttributeValueException.class).isThrownBy(
        () -> classInstance.checkAttributeSyntax(session, group, attributeToCheck));
  }

  @Test
  public void testValidAdminIgnoredKey() throws Exception {
    HashMap<String, String> value = new LinkedHashMap<>();
    value.put("ignoredByAdmin-cs", "message");
    attributeToCheck.setValue(value);

    classInstance.checkAttributeSyntax(session, group, attributeToCheck);
  }

  @Test
  public void testValidAdminIgnoredKeyDefault() throws Exception {
    HashMap<String, String> value = new LinkedHashMap<>();
    value.put("ignoredByAdmin", "message");
    attributeToCheck.setValue(value);

    classInstance.checkAttributeSyntax(session, group, attributeToCheck);
  }

  @Test
  public void testValidMailVerificationKey() throws Exception {
    HashMap<String, String> value = new LinkedHashMap<>();
    value.put("emailVerification-en", "message");
    attributeToCheck.setValue(value);

    classInstance.checkAttributeSyntax(session, group, attributeToCheck);
  }

  @Test
  public void testValidMailVerificationKeyDefault() throws Exception {
    HashMap<String, String> value = new LinkedHashMap<>();
    value.put("emailVerification", "message");
    attributeToCheck.setValue(value);

    classInstance.checkAttributeSyntax(session, group, attributeToCheck);
  }
}
