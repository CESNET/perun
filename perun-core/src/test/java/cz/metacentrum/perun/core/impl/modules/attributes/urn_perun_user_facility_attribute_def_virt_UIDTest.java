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

public class urn_perun_user_facility_attribute_def_virt_UIDTest {

  private urn_perun_user_facility_attribute_def_virt_UID classInstance;
  private Attribute attributeToCheck;
  private Facility facility = new Facility();
  private User user = new User();
  private PerunSessionImpl sess;
  private Attribute reqAttribute;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_user_facility_attribute_def_virt_UID();
    attributeToCheck = new Attribute();
    reqAttribute = new Attribute();
    reqAttribute.setValue("namespace");
    sess = mock(PerunSessionImpl.class);

    PerunBl perunBl = mock(PerunBl.class);
    when(sess.getPerunBl()).thenReturn(perunBl);

    AttributesManagerBl attributesManagerBl = mock(AttributesManagerBl.class);
    when(sess.getPerunBl().getAttributesManagerBl()).thenReturn(attributesManagerBl);
    when(attributesManagerBl.getAttribute(sess, facility,
        AttributesManager.NS_FACILITY_ATTR_DEF + ":uid-namespace")).thenReturn(reqAttribute);
    when(attributesManagerBl.getAttribute(sess, user,
        AttributesManager.NS_USER_ATTR_DEF + ":uid-namespace:" + reqAttribute.getValue())).thenReturn(reqAttribute);
  }

  @Test
  public void testSemanticsCorrect() throws Exception {
    System.out.println("testSemanticsCorrect()");
    attributeToCheck.setValue("example");

    classInstance.checkAttributeSemantics(sess, user, facility, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsWithoutReqAttribute() throws Exception {
    System.out.println("testSemanticsWithoutReqAttribute()");
    attributeToCheck.setValue("example");
    reqAttribute.setValue(null);

    classInstance.checkAttributeSemantics(sess, user, facility, attributeToCheck);
  }
}
