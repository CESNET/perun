/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Milan Halenar <255818@mail.muni.cz>
 * @date 23.11.2011
 */
public class urn_perun_user_facility_attribute_def_def_accountExpirationTimeTest {

  private static urn_perun_user_facility_attribute_def_def_accountExpirationTime classInstance;
  private static PerunSessionImpl session;
  private static Attribute attributeToCheck;

  @Before
  public void setUp() {
    classInstance = new urn_perun_user_facility_attribute_def_def_accountExpirationTime();
    session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    attributeToCheck = new Attribute();
  }

  @Test
  public void testCheckAttributeSemantics() throws Exception {
    System.out.println("testCheckAttributeSemantics()");
    attributeToCheck.setValue(1000);
    Attribute attribute = new Attribute();
    attribute.setValue(1500);
    when(session.getPerunBl().getAttributesManagerBl()
        .getAttribute(any(PerunSession.class), any(Facility.class), anyString())).thenReturn(attribute);
    classInstance.checkAttributeSemantics(session, new User(), new Facility(), attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsHigherValueThanFacilityTime() throws Exception {
    System.out.println("testCheckAttributeSemanticsHigherValueThanFacilityTime()");
    attributeToCheck.setValue(1000);
    Attribute attribute = new Attribute();
    attribute.setValue(999);
    when(session.getPerunBl().getAttributesManagerBl()
        .getAttribute(any(PerunSession.class), any(Facility.class), anyString())).thenReturn(attribute);
    classInstance.checkAttributeSemantics(session, new User(), new Facility(), attributeToCheck);
    fail("Assigning lower accountExpirationTime than the time set at facility should throw exception.");

  }

  @Test
  public void testFillAttributeValue() throws Exception {
    System.out.println("testFillAttributeValue()");
    Attribute attribute1 = new Attribute();
    attribute1.setValue(999);
    Attribute attribute2 = new Attribute();
    attribute2.setValue(1000);
    Attribute attribute3 = new Attribute();
    attribute3.setValue(1001);
    when(session.getPerunBl().getAttributesManagerBl()
        .getAttribute(any(PerunSession.class), any(Facility.class), anyString())).thenReturn(attribute1);
    when(session.getPerunBl().getAttributesManagerBl()
        .getAttribute(any(PerunSession.class), any(Resource.class), anyString())).thenReturn(attribute2, attribute3);
    attributeToCheck = classInstance.fillAttribute(session, new User(), new Facility(), attributeToCheck);
    assertEquals("Filled attribute should be the lowest from all resource and facility values", 999,
        attributeToCheck.getValue());
  }

}

