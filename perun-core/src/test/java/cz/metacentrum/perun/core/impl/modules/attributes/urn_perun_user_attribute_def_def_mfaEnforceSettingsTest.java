package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class urn_perun_user_attribute_def_def_mfaEnforceSettingsTest {
  private static urn_perun_user_attribute_def_def_mfaEnforceSettings classInstance;
  private static PerunSessionImpl session;
  private static User user;
  private static Attribute attributeToCheck;

  @Before
  public void setUp() throws WrongAttributeAssignmentException, AttributeNotExistsException {
    classInstance = new urn_perun_user_attribute_def_def_mfaEnforceSettings();
    session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    user = new User();
    attributeToCheck = new Attribute();
    attributeToCheck.setFriendlyName("mfaEnforceSettings:test");


    Attribute mockMfaCategories = new Attribute();
    mockMfaCategories.setValue("{" +
        "    \"cat1\":" +
        "    {" +
        "      \"label\": {\"en\": \"cat1_en_label\"}," +
        "      \"rps\":" +
        "      {" +
        "        \"cat1_rps1\": {\"en\":\"cat1_rps1_en_label\"}," +
        "        \"cat1_rps2\": {\"en\":\"cat1_rps2_en_label\"}" +
        "      }" +
        "    }," +
        "    \"cat2\":" +
        "    {" +
        "      \"label\": {\"en\": \"cat2_en_label\"}," +
        "      \"rps\":" +
        "      {" +
        "        \"cat2_rps1\": {\"en\":\"cat2_rps1_en_label\"}" +
        "      }" +
        "    }" + "  " +
        "}");
    when(session.getPerunBl().getAttributesManagerBl().getEntitylessAttributesWithKeys(any(), any(), any()))
        .thenReturn(Collections.singletonMap(attributeToCheck.getFriendlyNameParameter(), mockMfaCategories));
  }

  @Test
  public void testAttributeSyntaxNotValidJSON() {
    System.out.println("testAttributeSyntaxNotValidJSON()");
    attributeToCheck.setValue("plain string");

    try {
      classInstance.checkAttributeSyntax(session, user, attributeToCheck);
    } catch (WrongAttributeValueException e) {
      assertEquals("Attribute value " + attributeToCheck.getValue() + " is not a valid JSON.",
          errorWithoutId(e.getMessage()));
    }
  }

  @Test
  public void testAttributeSyntaxNull() throws Exception {
    System.out.println("testAttributeSyntaxNull()");
    attributeToCheck.setValue(null);

    classInstance.checkAttributeSyntax(session, user, attributeToCheck);
  }

  @Test
  public void testAttributeSyntaxEmpty() throws Exception {
    System.out.println("testAttributeSyntaxEmpty()");
    attributeToCheck.setValue("");

    classInstance.checkAttributeSyntax(session, user, attributeToCheck);
  }

  @Test
  public void testAttributeSyntaxAllTrue() throws Exception {
    System.out.println("testAttributeSyntaxAllTrue()");
    attributeToCheck.setValue("{\"all\": true}");

    classInstance.checkAttributeSyntax(session, user, attributeToCheck);
  }

  @Test
  public void testAttributeSyntaxCategories() throws Exception {
    System.out.println("testAttributeSyntaxCategories()");
    attributeToCheck.setValue("{\"include_categories\":[\"str1\",\"str2\"]}");

    classInstance.checkAttributeSyntax(session, user, attributeToCheck);
  }

  @Test
  public void testAttributeSyntaxCategoriesRps() throws Exception {
    System.out.println("testAttributeSyntaxCategoriesRps()");
    attributeToCheck.setValue("{\"include_categories\":[\"str1\",\"str2\"],\"exclude_rps\":[\"rp1\",\"rp2\"]}");

    classInstance.checkAttributeSyntax(session, user, attributeToCheck);
  }

  @Test
  public void testAttributeSyntaxCategoryNotArray() {
    System.out.println("testAttributeSyntaxCategoryNotArray()");
    attributeToCheck.setValue("{\"include_categories\":\"str\"}");

    try {
      classInstance.checkAttributeSyntax(session, user, attributeToCheck);
    } catch (WrongAttributeValueException e) {
      assertEquals("Property 'include_categories' is not an array.", errorWithoutId(e.getMessage()));
    }
  }

  @Test
  public void testAttributeSyntaxCategoryValueNotString() {
    System.out.println("testAttributeSyntaxCategoryValueNotString()");
    attributeToCheck.setValue("{\"include_categories\":[\"str\", []]}");

    try {
      classInstance.checkAttributeSyntax(session, user, attributeToCheck);
    } catch (WrongAttributeValueException e) {
      assertEquals("Property 'include_categories' has non textual value []", errorWithoutId(e.getMessage()));
    }
  }

  @Test
  public void testAttributeSyntaxWrongFormat() {
    System.out.println("testAttributeSyntaxWrongFormat()");
    attributeToCheck.setValue("{\"all\":true,\"include_categories\":[\"str1\",\"str2\"]}");

    try {
      classInstance.checkAttributeSyntax(session, user, attributeToCheck);
    } catch (WrongAttributeValueException e) {
      assertEquals(
          "Attribute value {\"all\":true,\"include_categories\":[\"str1\",\"str2\"]} has incorrect format." +
              " Allowed values are:" +
              " empty string or null," +
              " {\"all\":true}, {\"include_categories\":[\"str1\",\"str2\"]}," +
              " {\"include_categories\":[\"str1\",\"str2\"],\"exclude_rps\":[\"rp1\",\"rp2\"]}",
          errorWithoutId(e.getMessage()));
    }
  }

  @Test
  public void testCheckAttributeSemanticsCorrect() throws Exception {
    System.out.println("testCheckAttributeSemanticsCorrect()");
    attributeToCheck.setValue(
        "{\"include_categories\":[\"cat1\",\"cat2\"],\"exclude_rps\":[\"cat1_rps2\",\"cat2_rps1\"]}");

    classInstance.checkAttributeSemantics(session, user, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsWrongCategoryReference() throws Exception {
    System.out.println("testCheckAttributeSemanticsWrongCategoryReference()");
    attributeToCheck.setValue(
        "{\"include_categories\":[\"cat1\",\"wrong_cat\"],\"exclude_rps\":[\"cat1_rps2\",\"cat2_rps1\"]}");

    classInstance.checkAttributeSemantics(session, user, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsWrongRpsReference() throws Exception {
    System.out.println("testCheckAttributeSemanticsWrongRpsReference()");
    attributeToCheck.setValue("{\"include_categories\":[\"str1\",\"str2\"],\"exclude_rps\":[\"wrong_rps\",\"rp2\"]}");

    classInstance.checkAttributeSemantics(session, user, attributeToCheck);
  }

  private String errorWithoutId(String msg) {
    return msg.substring(msg.indexOf(":") + 1).trim();
  }
}
