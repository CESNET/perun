package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_user_facility_attribute_def_virt_preferredUnixGroupNameTest {

  private urn_perun_user_facility_attribute_def_virt_preferredUnixGroupName classInstance;
  private Attribute attributeToCheck;
  private Facility facility = new Facility();
  private User user = new User();
  private PerunSessionImpl sess;
  private Attribute facilityGroupNameNamespace;
  private Attribute preferredUnixGroupName;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_user_facility_attribute_def_virt_preferredUnixGroupName();
    attributeToCheck = new Attribute();
    facilityGroupNameNamespace = new Attribute();
    facilityGroupNameNamespace.setValue("facility_group_namespace");
    preferredUnixGroupName = new Attribute();
    preferredUnixGroupName.setValue("preferred_unix_group_name");
    sess = mock(PerunSessionImpl.class);

    PerunBl perunBl = mock(PerunBl.class);
    when(sess.getPerunBl()).thenReturn(perunBl);

    AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
    when(sess.getPerunBl().getAttributesManagerBl()).thenReturn(attributesManagerBl);
    when(attributesManagerBl.getAttribute(sess, facility,
        AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGroupName-namespace")).thenReturn(facilityGroupNameNamespace);
    when(attributesManagerBl.getAttribute(sess, user,
        AttributesManager.NS_USER_ATTR_DEF + ":preferredUnixGroupName-namespace:" +
        facilityGroupNameNamespace.valueAsString())).thenReturn(preferredUnixGroupName);
  }

  @Test
  public void testSemanticsCorrect() throws Exception {
    System.out.println("testSemanticsCorrect()");
    attributeToCheck.setValue("name");

    classInstance.checkAttributeSemantics(sess, user, facility, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsWithoutFacilityLoginNamespaceAttribute() throws Exception {
    System.out.println("testSemanticsWithoutFacilityLoginNamespaceAttribute()");
    attributeToCheck.setValue("name");
    facilityGroupNameNamespace.setValue(null);

    classInstance.checkAttributeSemantics(sess, user, facility, attributeToCheck);
  }
}
