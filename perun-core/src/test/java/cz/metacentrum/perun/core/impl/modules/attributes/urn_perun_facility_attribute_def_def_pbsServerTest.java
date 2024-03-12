package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.FacilitiesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.ArrayList;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_facility_attribute_def_def_pbsServerTest {

  private static urn_perun_facility_attribute_def_def_pbsServer classInstance;
  private static PerunSessionImpl session;
  private static Facility facility;
  private static Attribute attributeToCheck;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_facility_attribute_def_def_pbsServer();
    session = mock(PerunSessionImpl.class);
    facility = new Facility();
    attributeToCheck = new Attribute();
    Attribute name = new Attribute();

    PerunBl perunBl = mock(PerunBl.class);
    when(session.getPerunBl()).thenReturn(perunBl);

    FacilitiesManagerBl facilitiesManagerBl = mock(FacilitiesManagerBl.class);
    when(perunBl.getFacilitiesManagerBl()).thenReturn(facilitiesManagerBl);

    AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
    when(perunBl.getAttributesManagerBl()).thenReturn(attributesManagerBl);
    when(attributesManagerBl.getAttributeDefinition(session,
        AttributesManager.NS_FACILITY_ATTR_CORE + ":name")).thenReturn(name);
  }

  @Test
  public void testCheckAttributeSemanticsCorrect() throws Exception {
    System.out.println("testCheckAttributeSemanticsCorrect()");
    attributeToCheck.setValue("example");
    Facility facilityToReturn = new Facility();
    facilityToReturn.setName("example");
    when(session.getPerunBl().getFacilitiesManagerBl().getFacilities(session)).thenReturn(
        Collections.singletonList(facilityToReturn));

    classInstance.checkAttributeSyntax(session, facility, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsWithNullValue() throws Exception {
    System.out.println("testCheckAttributeSemanticsWithNullValue()");
    attributeToCheck.setValue(null);

    classInstance.checkAttributeSemantics(session, facility, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsWithoutSameNameFacility() throws Exception {
    System.out.println("testCheckAttributeSemanticsWithoutSameNameFacility()");
    attributeToCheck.setValue("domain");
    when(session.getPerunBl().getFacilitiesManagerBl().getFacilities(session)).thenReturn(new ArrayList<>());

    classInstance.checkAttributeSemantics(session, facility, attributeToCheck);
  }
}
