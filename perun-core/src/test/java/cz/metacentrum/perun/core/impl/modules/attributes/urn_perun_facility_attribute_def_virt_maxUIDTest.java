package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_facility_attribute_def_virt_maxUIDTest {

  private static urn_perun_facility_attribute_def_virt_maxUID classInstance;
  private static PerunSessionImpl session;
  private static Facility facility;
  private static Attribute attributeToCheck;
  private static Attribute reqAttribute;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_facility_attribute_def_virt_maxUID();
    session = mock(PerunSessionImpl.class);
    facility = new Facility();
    attributeToCheck = new Attribute();
    attributeToCheck.setFriendlyName("friendly_name");
    reqAttribute = new Attribute();

    PerunBl perunBl = mock(PerunBl.class);
    when(session.getPerunBl()).thenReturn(perunBl);

    AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
    when(perunBl.getAttributesManagerBl()).thenReturn(attributesManagerBl);
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

  @Test
  public void testCheckAttributeSemanticsCorrect() throws Exception {
    System.out.println("testCheckAttributeSemanticsCorrect()");
    reqAttribute.setValue("example");
    when(session.getPerunBl().getAttributesManagerBl()
        .getAttribute(session, facility, AttributesManager.NS_FACILITY_ATTR_DEF + ":uid-namespace")).thenReturn(
        reqAttribute);

    classInstance.checkAttributeSemantics(session, facility, attributeToCheck);
  }
}
