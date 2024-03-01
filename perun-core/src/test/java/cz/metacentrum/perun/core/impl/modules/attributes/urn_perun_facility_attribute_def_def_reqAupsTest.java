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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_facility_attribute_def_def_reqAupsTest {

  private static urn_perun_facility_attribute_def_def_reqAups classInstance;
  private static PerunSessionImpl session;
  private static Facility facility;
  private static Attribute attributeToCheck;
  private static Attribute reqAttribute;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_facility_attribute_def_def_reqAups();
    session = mock(PerunSessionImpl.class);
    facility = new Facility();
    attributeToCheck = new Attribute();
    reqAttribute = new Attribute();

    PerunBl perunBl = mock(PerunBl.class);
    when(session.getPerunBl()).thenReturn(perunBl);

    AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
    when(perunBl.getAttributesManagerBl()).thenReturn(attributesManagerBl);
    when(session.getPerunBl().getAttributesManagerBl()
        .getEntitylessAttributes(session, AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":orgAups")).thenReturn(
        Collections.singletonList(reqAttribute));

  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsWithoutAUPInOrganization() throws Exception {
    System.out.println("testCheckAttributeSyntaxWithoutAUPInOrganization()");
    List<String> value = new ArrayList<>();
    value.add("bad_example");
    attributeToCheck.setValue(value);
    Map<String, String> value2 = new LinkedHashMap<>();
    value2.put("example", "example");
    reqAttribute.setValue(value2);

    classInstance.checkAttributeSemantics(session, facility, attributeToCheck);
  }

  @Test
  public void testCheckAttributeSemanticsCorrect() throws Exception {
    System.out.println("testCheckAttributeSemanticsCorrect()");
    List<String> value = new ArrayList<>();
    value.add("example");
    attributeToCheck.setValue(value);
    Map<String, String> value2 = new LinkedHashMap<>();
    value2.put("example", "example");
    reqAttribute.setValue(value2);

    classInstance.checkAttributeSemantics(session, facility, attributeToCheck);
  }
}
