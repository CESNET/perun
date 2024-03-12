package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.mock;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_facility_attribute_def_def_rpProtocolTest {
  private urn_perun_facility_attribute_def_def_rpProtocol classInstance;
  private Attribute attributeToCheck;
  private Facility facility;
  private PerunSessionImpl session;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_facility_attribute_def_def_rpProtocol();
    session = mock(PerunSessionImpl.class);
    attributeToCheck = new Attribute();
    facility = new Facility();
  }

  @Test
  public void testCheckAttributeSyntaxCorrect() throws Exception {
    System.out.println("testCheckAttributeSyntaxCorrect()");

    attributeToCheck.setValue("SAML");
    classInstance.checkAttributeSyntax(session, facility, attributeToCheck);

    attributeToCheck.setValue("OIDC");
    classInstance.checkAttributeSyntax(session, facility, attributeToCheck);
  }

  @Test
  public void testCheckAttributeSyntaxNullValue() throws Exception {
    System.out.println("testCheckAttributeSyntaxNullValue()");
    attributeToCheck.setValue(null);

    classInstance.checkAttributeSyntax(session, facility, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxWrongValue() throws Exception {
    System.out.println("testCheckAttributeSyntaxWrongValue()");
    attributeToCheck.setValue("wrong value");

    classInstance.checkAttributeSyntax(session, facility, attributeToCheck);
  }
}
