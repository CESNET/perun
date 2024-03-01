package cz.metacentrum.perun.core.impl.modules.attributes;

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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_user_facility_attribute_def_virt_loginTest {

  private urn_perun_user_facility_attribute_def_virt_login classInstance;
  private Attribute attributeToCheck;
  private Facility facility = new Facility();
  private User user = new User();
  private PerunSessionImpl sess;
  private Attribute userLoginNamespace;
  private Attribute facilityLoginNamespace;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_user_facility_attribute_def_virt_login();
    attributeToCheck = new Attribute();
    userLoginNamespace = new Attribute();
    userLoginNamespace.setValue("user_login_namespace");
    facilityLoginNamespace = new Attribute();
    facilityLoginNamespace.setValue("facility_login_namespace");
    sess = mock(PerunSessionImpl.class);

    PerunBl perunBl = mock(PerunBl.class);
    when(sess.getPerunBl()).thenReturn(perunBl);

    AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
    when(sess.getPerunBl().getAttributesManagerBl()).thenReturn(attributesManagerBl);
    when(attributesManagerBl.getAttribute(sess, facility,
        AttributesManager.NS_FACILITY_ATTR_DEF + ":login-namespace")).thenReturn(facilityLoginNamespace);
    when(attributesManagerBl.getAttribute(sess, user,
        AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:" + facilityLoginNamespace.getValue())).thenReturn(
        userLoginNamespace);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsWithoutFacilityLoginNamespaceAttribute() throws Exception {
    System.out.println("testSemanticsWithoutFacilityLoginNamespaceAttribute()");
    attributeToCheck.setValue("login");
    facilityLoginNamespace.setValue(null);

    classInstance.checkAttributeSemantics(sess, user, facility, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsWithoutUserLoginNamespaceAttribute() throws Exception {
    System.out.println("testSemanticsWithoutUserLoginNamespaceAttribute()");
    attributeToCheck.setValue(null);

    classInstance.checkAttributeSemantics(sess, user, facility, attributeToCheck);
  }

  @Test
  public void testSemanticsCorrect() throws Exception {
    System.out.println("testSemanticsCorrect()");
    attributeToCheck.setValue("login");

    classInstance.checkAttributeSemantics(sess, user, facility, attributeToCheck);
  }
}
