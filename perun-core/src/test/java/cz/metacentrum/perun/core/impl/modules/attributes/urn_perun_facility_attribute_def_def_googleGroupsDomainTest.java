package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_facility_attribute_def_def_googleGroupsDomainTest {

  private static urn_perun_facility_attribute_def_def_googleGroupsDomain classInstance;
  private static PerunSessionImpl session;
  private static Facility facility;
  private static Attribute attributeToCheck;
  private static Attribute reqAttribute;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_facility_attribute_def_def_googleGroupsDomain();
    session = mock(PerunSessionImpl.class);
    facility = new Facility();
    attributeToCheck = new Attribute();
    reqAttribute = new Attribute();

    PerunBl perunBl = mock(PerunBl.class);
    when(session.getPerunBl()).thenReturn(perunBl);

    AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
    when(perunBl.getAttributesManagerBl()).thenReturn(attributesManagerBl);
  }

  @Test
  public void testCheckAttributeSemanticsCorrect() throws Exception {
    System.out.println("testCheckAttributeSemanticsCorrect()");
    attributeToCheck.setValue("domain");
    when(session.getPerunBl().getAttributesManagerBl().getAttributeDefinition(session,
        AttributesManager.NS_GROUP_ATTR_DEF + ":googleGroupName-namespace:" + "domain")).thenReturn(reqAttribute);

    classInstance.checkAttributeSemantics(session, facility, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsWithNullValue() throws Exception {
    System.out.println("testCheckAttributeSemanticsWithNullValue()");
    attributeToCheck.setValue(null);

    classInstance.checkAttributeSemantics(session, facility, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsWithoutReqAttribute() throws Exception {
    System.out.println("testCheckAttributeSemanticsWithoutReqAttribute()");
    attributeToCheck.setValue("domain");
    when(session.getPerunBl().getAttributesManagerBl().getAttributeDefinition(session,
        AttributesManager.NS_GROUP_ATTR_DEF + ":googleGroupName-namespace:" + "domain")).thenThrow(
          new AttributeNotExistsException(""));

    classInstance.checkAttributeSemantics(session, facility, attributeToCheck);
  }
}
