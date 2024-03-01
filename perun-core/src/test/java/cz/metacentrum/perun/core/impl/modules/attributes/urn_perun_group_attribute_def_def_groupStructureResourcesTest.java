package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class urn_perun_group_attribute_def_def_groupStructureResourcesTest {

  private static final String CLASS_NAME =
      urn_perun_group_attribute_def_def_groupStructureResources.class.getName() + ".";
  private urn_perun_group_attribute_def_def_groupStructureResources classInstance;
  private Attribute attribute;
  private PerunSessionImpl sess;
  private Group group;
  private Resource validResource;
  private Vo vo;

  @Before
  public void setUp() throws Exception {
    group = new Group("group", "description");
    vo = new Vo(10, "vo", "vo");
    group.setVoId(vo.getId());
    classInstance = new urn_perun_group_attribute_def_def_groupStructureResources();
    attribute = new Attribute(classInstance.getAttributeDefinition());
    sess = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);

    validResource = new Resource(19, "resource", "", -1);
    validResource.setVoId(vo.getId());

    when(sess.getPerunBl().getVosManagerBl().getVoById(sess, vo.getId()))
        .thenReturn(vo);

    when(sess.getPerunBl().getResourcesManagerBl().getResources(sess, vo))
        .thenReturn(Collections.singletonList(validResource));
  }

  // ---- Syntax ---- //

  @Test
  public void testGroupLoginsInvalidSingleEscape() throws Exception {
    System.out.println(CLASS_NAME + "testGroupLoginsInvalidSingleEscape");
    testInvalidGroupLoginsSyntax("\\");
  }

  @Test
  public void testGroupLoginsInvalidEndEscape() throws Exception {
    System.out.println(CLASS_NAME + "testGroupLoginsInvalidEndEscape");
    testInvalidGroupLoginsSyntax("login1,\\");
  }

  @Test
  public void testGroupLoginsInvalidStartEscape() throws Exception {
    System.out.println(CLASS_NAME + "testGroupLoginsInvalidStartEscape");
    testInvalidGroupLoginsSyntax("\\login1,");
  }

  @Test
  public void testGroupLoginsInvalidEscape() throws Exception {
    System.out.println(CLASS_NAME + "testGroupLoginsInvalidEscape");
    testInvalidGroupLoginsSyntax("lo\\gin1,");
  }

  @Test
  public void testGroupLoginsInvalidEscapeAfterCorrectBackslashEscape() throws Exception {
    System.out.println(CLASS_NAME + "testGroupLoginsInvalidEscapeAfterCorrectBackslashEscape");
    testInvalidGroupLoginsSyntax("lo\\\\\\gin1,");
  }

  @Test
  public void testGroupLoginsInvalidEscapeAfterCorrectCommaEscape() throws Exception {
    System.out.println(CLASS_NAME + "testGroupLoginsInvalidEscapeAfterCorrectCommaEscape");
    testInvalidGroupLoginsSyntax("lo\\,\\gin1,");
  }

  @Test
  public void testGroupLoginsNoCommaAtTheEnd() throws Exception {
    System.out.println(CLASS_NAME + "testGroupLoginsNoCommaAtTheEnd");
    testInvalidGroupLoginsSyntax("login1,login2");
  }

  @Test
  public void testGroupLoginsValidCommaAfterBackSlash() throws Exception {
    System.out.println(CLASS_NAME + "testGroupLoginsValidCommaAfterBackSlash");
    testValidGroupLoginsSyntax("login1\\\\\\,login2,");
  }

  @Test
  public void testGroupLoginsValidCommaAfterTwoBackSlashes() throws Exception {
    System.out.println(CLASS_NAME + "testGroupLoginsValidCommaAfterTwoBackSlashes");
    testValidGroupLoginsSyntax("login1\\\\\\\\,login2,");
  }

  @Test
  public void testGroupLoginsValidBackslashEscape() throws Exception {
    System.out.println(CLASS_NAME + "testGroupLoginsValidBackslashEscape");
    testValidGroupLoginsSyntax("lo\\\\gin1,");
  }

  @Test
  public void testGroupLoginsValidCommaEscape() throws Exception {
    System.out.println(CLASS_NAME + "testGroupLoginsValidCommaEscape");
    testValidGroupLoginsSyntax("lo\\,gin1,");
  }

  @Test
  public void testGroupLoginsValidCommaAfterEscapeCommaEscape() throws Exception {
    System.out.println(CLASS_NAME + "testGroupLoginsValidCommaAfterEscapeCommaEscape");
    testValidGroupLoginsSyntax("lo\\,,gin1,");
  }

  @Test
  public void testGroupLoginsValidMultipleLogins() throws Exception {
    System.out.println(CLASS_NAME + "testGroupLoginsValidMultipleLogins");
    testValidGroupLoginsSyntax("gro\\,up1,group2,group3,group4,");
  }

  @Test
  public void testGroupLoginsValidEmptyValue() throws Exception {
    System.out.println(CLASS_NAME + "testGroupLoginsValidEmptyValue");
    testValidGroupLoginsSyntax("");
  }

  @Test
  public void testInvalidGroupLoginAfterValidOne() throws Exception {
    System.out.println(CLASS_NAME + "testInvalidGroupLoginAfterValidOne");
    testInvalidMultipleGroupLoginsSyntax("", "missingComma");
  }

  @Test
  public void testValidMultipleGroupLogins() throws Exception {
    System.out.println(CLASS_NAME + "testValidMultipleGroupLogins");
    testValidMultipleGroupLoginsSyntax("", "valid,");
  }

  @Test
  public void testNullValueIsValidInSyntax() throws Exception {
    System.out.println(CLASS_NAME + "testNullValueIsValidInSyntax");
    attribute.setValue(null);
    classInstance.checkAttributeSyntax(sess, group, attribute);
  }

  @Test
  public void testValidResourceIdSyntax() throws Exception {
    System.out.println(CLASS_NAME + "testValidResourceIdSyntax");
    testValidResourceIdsSyntax("123");
  }

  @Test
  public void testMultipleValidResourceIdSyntax() throws Exception {
    System.out.println(CLASS_NAME + "testMultipleValidResourceIdSyntax");
    testValidResourceIdsSyntax("123", "23");
  }

  @Test
  public void testInValidCharInResourceIdSyntax() throws Exception {
    System.out.println(CLASS_NAME + "testInValidCharInResourceIdSyntax");
    testInValidResourceIdsSyntax("#123");
  }

  @Test
  public void testInValidResourceIdWithValidSyntax() throws Exception {
    System.out.println(CLASS_NAME + "testInValidResourceIdWithValidSyntax");
    testInValidResourceIdsSyntax("123", "#123");
  }

  @Test
  public void testInValidResourceIdEmptySyntax() throws Exception {
    System.out.println(CLASS_NAME + "testInValidResourceIdEmptySyntax");
    testInValidResourceIdsSyntax("");
  }


  // ---- Semantics ---- //


  @Test
  public void testNullValueIsValidInSemantics() throws Exception {
    System.out.println(CLASS_NAME + "testNullValueIsValidInSemantics");
    attribute.setValue(null);
    classInstance.checkAttributeSemantics(sess, group, attribute);
  }

  @Test
  public void testValidResourceIdInSemantics() throws Exception {
    System.out.println(CLASS_NAME + "testValidResourceIdInSemantics");
    testValidResourceSemantics(String.valueOf(validResource.getId()));
  }

  @Test
  public void testInValidResourceIdInSemantics() throws Exception {
    System.out.println(CLASS_NAME + "testInValidResourceIdInSemantics");
    testInValidResourceSemantics("2344");
  }

  @Test
  public void testInValidResourceIdWithValidInSemantics() throws Exception {
    System.out.println(CLASS_NAME + "testInValidResourceIdWithValidInSemantics");
    testInValidResourceSemantics(String.valueOf(validResource.getId()), "2344");
  }


  // ---- Private methods ---- //


  private void testValidResourceSemantics(String... values) throws Exception {
    HashMap<String, String> attrValue = new LinkedHashMap<>();
    for (String value : values) {
      attrValue.put(value, "login,");
    }
    attribute.setValue(attrValue);

    classInstance.checkAttributeSemantics(sess, group, attribute);
  }

  private void testInValidResourceSemantics(String... values) throws Exception {
    HashMap<String, String> attrValue = new LinkedHashMap<>();
    for (String value : values) {
      attrValue.put(value, "login,");
    }
    attribute.setValue(attrValue);

    assertThatExceptionOfType(WrongReferenceAttributeValueException.class)
        .isThrownBy(() -> classInstance.checkAttributeSemantics(sess, group, attribute));
  }

  private void testValidResourceIdsSyntax(String... values) throws Exception {
    HashMap<String, String> attrValue = new LinkedHashMap<>();
    for (String value : values) {
      attrValue.put(value, "login,");
    }
    attribute.setValue(attrValue);

    classInstance.checkAttributeSyntax(sess, group, attribute);
  }

  private void testInValidResourceIdsSyntax(String... values) throws Exception {
    HashMap<String, String> attrValue = new LinkedHashMap<>();
    for (String value : values) {
      attrValue.put(value, "login,");
    }
    attribute.setValue(attrValue);

    assertThatExceptionOfType(WrongAttributeValueException.class)
        .isThrownBy(() -> classInstance.checkAttributeSyntax(sess, group, attribute));
  }

  private void testValidGroupLoginsSyntax(String value) throws Exception {
    HashMap<String, String> attrValue = new LinkedHashMap<>();
    attrValue.put("1", value);
    attribute.setValue(attrValue);

    classInstance.checkAttributeSyntax(sess, group, attribute);
  }

  private void testInvalidGroupLoginsSyntax(String value) {
    HashMap<String, String> attrValue = new LinkedHashMap<>();
    attrValue.put("1", value);
    attribute.setValue(attrValue);

    assertThatExceptionOfType(WrongAttributeValueException.class)
        .isThrownBy(() -> classInstance.checkAttributeSyntax(sess, group, attribute));
  }

  private void testInvalidMultipleGroupLoginsSyntax(String... values) {
    HashMap<String, String> attrValue = new LinkedHashMap<>();
    int resourceId = 1;

    for (String value : values) {
      attrValue.put(String.valueOf(resourceId++), value);
      attribute.setValue(attrValue);
    }

    assertThatExceptionOfType(WrongAttributeValueException.class)
        .isThrownBy(() -> classInstance.checkAttributeSyntax(sess, group, attribute));
  }

  private void testValidMultipleGroupLoginsSyntax(String... values) throws WrongAttributeValueException {
    HashMap<String, String> attrValue = new LinkedHashMap<>();
    int resourceId = 1;

    for (String value : values) {
      attrValue.put(String.valueOf(resourceId), value);
      attribute.setValue(attrValue);
    }

    classInstance.checkAttributeSyntax(sess, group, attribute);
  }
}
