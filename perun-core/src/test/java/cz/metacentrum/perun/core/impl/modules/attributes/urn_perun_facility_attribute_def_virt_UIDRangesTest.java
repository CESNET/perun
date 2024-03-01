package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_facility_attribute_def_virt_UIDRangesTest {

  private static urn_perun_facility_attribute_def_virt_UIDRanges classInstance;
  private static PerunSessionImpl session;
  private static Facility facility;
  private static Attribute attributeToCheck;
  private static Attribute reqAttribute;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_facility_attribute_def_virt_UIDRanges();
    session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    facility = new Facility();
    attributeToCheck = new Attribute();
    attributeToCheck.setFriendlyName("friendly_name");
    reqAttribute = new Attribute();
  }

  @Test
  public void testCheckAttributeSemanticsCorrect() throws Exception {
    System.out.println("testCheckAttributeSemanticsCorrect()");
    reqAttribute.setValue("example");
    when(session.getPerunBl().getAttributesManagerBl()
        .getAttribute(session, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":uid-namespace")).thenReturn(
        reqAttribute);

    classInstance.checkAttributeSemantics(session, facility, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsWithReqAttributeWithNullValue() throws Exception {
    System.out.println("testCheckAttributeSemanticsWithReqAttributeWithNullValue()");
    reqAttribute.setValue(null);
    when(session.getPerunBl().getAttributesManagerBl()
        .getAttribute(session, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":uid-namespace")).thenReturn(
        reqAttribute);

    classInstance.checkAttributeSemantics(session, facility, attributeToCheck);
  }
}
