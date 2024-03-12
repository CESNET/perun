package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.mock;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_facility_attribute_def_def_rtOutputFileNameTest {

  private static urn_perun_facility_attribute_def_def_rtOutputFileName classInstance;
  private static PerunSessionImpl session;
  private static Facility facility;
  private static Attribute attributeToCheck;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_facility_attribute_def_def_rtOutputFileName();
    session = mock(PerunSessionImpl.class);
    facility = new Facility();
    attributeToCheck = new Attribute();
  }

  @Test
  public void testCheckAttributeSyntaxCorrect() throws Exception {
    System.out.println("testCheckAttributeSyntaxCorrect()");
    attributeToCheck.setValue("example");

    classInstance.checkAttributeSyntax(session, facility, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxWithIncorrectValue() throws Exception {
    System.out.println("testCheckAttributeSyntaxWithIncorrectValue()");
    attributeToCheck.setValue("/bad_example");

    classInstance.checkAttributeSyntax(session, facility, attributeToCheck);
  }
}
