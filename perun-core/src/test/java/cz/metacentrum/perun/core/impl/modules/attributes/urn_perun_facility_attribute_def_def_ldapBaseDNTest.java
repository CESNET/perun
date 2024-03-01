package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * Created by Oliver Mrázik on 3. 7. 2014.
 * author: Oliver Mrázik
 * version: 2014-07-03
 */
public class urn_perun_facility_attribute_def_def_ldapBaseDNTest {

  private static urn_perun_facility_attribute_def_def_ldapBaseDN classInstance;
  private static PerunSessionImpl session;
  private static Facility facility;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_facility_attribute_def_def_ldapBaseDN();
    session = mock(PerunSessionImpl.class);
    facility = new Facility();
  }

  @Test
  public void testCheckAttributeSyntaxCorrect() throws Exception {
    Attribute attribute = new Attribute(classInstance.getAttributeDefinition());
    attribute.setValue("dc=example,dc=domain");

    classInstance.checkAttributeSyntax(session, facility, attribute);

    attribute.setValue("ou=example,dc=domain");

    classInstance.checkAttributeSyntax(session, facility, attribute);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxEmptyString() throws Exception {
    Attribute attribute = new Attribute(classInstance.getAttributeDefinition());
    attribute.setValue("");

    classInstance.checkAttributeSyntax(session, facility, attribute);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxFailsLessChars() throws Exception {
    Attribute attribute = new Attribute(classInstance.getAttributeDefinition());
    attribute.setValue("ou");

    classInstance.checkAttributeSyntax(session, facility, attribute);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSemanticsWrongChars() throws Exception {
    Attribute attribute = new Attribute(classInstance.getAttributeDefinition());
    attribute.setValue("cn=example,dc=domain");

    classInstance.checkAttributeSyntax(session, facility, attribute);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsWithNullValue() throws Exception {
    Attribute attribute = new Attribute(classInstance.getAttributeDefinition());

    classInstance.checkAttributeSemantics(session, facility, attribute);
  }

  @Test
  public void testCheckAttributeSemanticsCorrect() throws Exception {
    Attribute attribute = new Attribute(classInstance.getAttributeDefinition());
    attribute.setValue("dc=example,dc=domain");

    classInstance.checkAttributeSemantics(session, facility, attribute);
  }
}
