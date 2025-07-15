package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.Date;
import java.util.LinkedHashMap;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_member_attribute_def_def_lifecycleTimestampsTest {
  private static urn_perun_member_attribute_def_def_lifecycleTimestamps classInstance;
  private static PerunSessionImpl session;
  private static Attribute attributeToCheck;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_member_attribute_def_def_lifecycleTimestamps();
    session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    attributeToCheck = new Attribute();
  }


  @Test
  public void testCheckAttributeSyntaxCorrect() throws Exception {
    System.out.println("testCheckAttributeSyntaxCorrect()");
    LinkedHashMap<String, String> value = new LinkedHashMap<>();
    value.put("expiredAt", "2001-12-25");
    attributeToCheck.setValue(value);
    classInstance.checkAttributeSyntax(session, null, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxIncorrectValue() throws Exception {
    System.out.println("testCheckAttributeSyntaxIncorrectValue()");
    LinkedHashMap<String, String> value = new LinkedHashMap<>();
    value.put("expiredAt", "Tue 2001-12-25");
    attributeToCheck.setValue(value);
    classInstance.checkAttributeSyntax(session, null, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxIncorrectKey() throws Exception {
    System.out.println("testCheckAttributeSyntaxIncorrectKey()");
    LinkedHashMap<String, String> value = new LinkedHashMap<>();
    value.put("exbibibi", "2001-12-25");
    attributeToCheck.setValue(value);
    classInstance.checkAttributeSyntax(session, null, attributeToCheck);
  }

  @Test
  public void testCheckAttributeSemanticsCorrect() throws Exception {
    System.out.println("testCheckAttributeSemanticsCorrect()");
    LinkedHashMap<String, String> value = new LinkedHashMap<>();
    value.put("expiredAt", "2001-12-25");
    attributeToCheck.setValue(value);
    classInstance.checkAttributeSemantics(session, null, attributeToCheck);
  }

  @Test
  public void testCheckAttributeSemanticsCorrectToday() throws Exception {
    System.out.println("testCheckAttributeSemanticsCorrectToday()");
    LinkedHashMap<String, String> value = new LinkedHashMap<>();
    Date today = new Date();
    value.put("expiredAt", BeansUtils.getDateFormatterWithoutTime().format(today));
    attributeToCheck.setValue(value);
    classInstance.checkAttributeSemantics(session, null, attributeToCheck);
  }

  @Test(expected = WrongAttributeAssignmentException.class)
  public void testCheckAttributeSemanticsDateInFuture() throws Exception {
    System.out.println("testCheckAttributeSemanticsDateInFuture()");
    LinkedHashMap<String, String> value = new LinkedHashMap<>();
    value.put("expiredAt", "2031-12-25");
    attributeToCheck.setValue(value);
    classInstance.checkAttributeSemantics(session, null, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsDateNullValueExpired() throws Exception {
    System.out.println("testCheckAttributeSemanticsDateNullValueExpired()");
    LinkedHashMap<String, String> value = new LinkedHashMap<>();
    value.put("expiredAt", null);
    attributeToCheck.setValue(value);
    classInstance.checkAttributeSemantics(session, null, attributeToCheck);
  }


  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsDateNullValueArchived() throws Exception {
    System.out.println("testCheckAttributeSemanticsDateNullValueArchived()");
    LinkedHashMap<String, String> value = new LinkedHashMap<>();
    value.put("archivedAt", null);
    attributeToCheck.setValue(value);
    classInstance.checkAttributeSemantics(session, null, attributeToCheck);
  }
}
