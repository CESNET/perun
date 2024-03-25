package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * Test of unix permission mask attribute.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class urn_perun_facility_attribute_def_def_homeDirUmaskTest {

  private static PerunSessionImpl session;
  private static Facility facility;
  private static urn_perun_facility_attribute_def_def_homeDirUmask classInstance;

  @Before
  public void setUp() {
    classInstance = new urn_perun_facility_attribute_def_def_homeDirUmask();
    facility = new Facility();
    session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
  }

  @Test
  public void testCheckAttributeSyntax() throws Exception {
    System.out.println("testCheckAttributeSyntax()");

    Attribute attributeToCheck = new Attribute();

    attributeToCheck.setValue(null);
    classInstance.checkAttributeSyntax(session, facility, attributeToCheck);

    attributeToCheck.setValue("0542");
    classInstance.checkAttributeSyntax(session, facility, attributeToCheck);

    attributeToCheck.setValue("215");
    classInstance.checkAttributeSyntax(session, facility, attributeToCheck);

    attributeToCheck.setValue("0521");
    classInstance.checkAttributeSyntax(session, facility, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxWithWrongValue() throws Exception {
    System.out.println("testCheckAttributeSyntaxWithWrongValue()");

    Attribute attributeToCheck = new Attribute();

    attributeToCheck.setValue("5891");
    classInstance.checkAttributeSyntax(session, facility, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxWithWrongValueLength() throws Exception {
    System.out.println("testCheckAttributeSyntaxWithWrongValueLength()");

    Attribute attributeToCheck = new Attribute();

    attributeToCheck.setValue("12");
    classInstance.checkAttributeSyntax(session, facility, attributeToCheck);
  }
}