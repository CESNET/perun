package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;

/**
 * Set of tests for class urn_perun_resource_attribute_def_def_shells
 *
 * @author Lukas Pravda  <luky.pravda@gmail.com>
 * @date 19.5.2011 14:41:23
 */
public class urn_perun_resource_attribute_def_def_shellsTest {

  private static final Attribute listOfShells = new Attribute();
  private static urn_perun_resource_attribute_def_def_shells classInstance;
  private static PerunSessionImpl session;

  @Before
  public void setUp() {
    classInstance = new urn_perun_resource_attribute_def_def_shells();
    session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);

    ArrayList<String> shells = new ArrayList<>();
    shells.add("/bin/bash");
    shells.add("/bin/csh");
    listOfShells.setValue(shells);
  }

  /**
   * Test of checkAttributeSemantics method, of class urn_perun_resource_attribute_def_def_shells. with all parameters
   * properly set.
   */
  @Test
  public void testCheckAttributeSemantics() throws Exception {
    System.out.println("testCheckAttributeSemantics()");

    Attribute attributeToCheck = new Attribute();
    attributeToCheck.setValue(new ArrayList<String>() {{
      add("/bin/bash");
    }});

    when(session.getPerunBl().getAttributesManagerBl()
        .getAttribute(any(PerunSession.class), any(Facility.class), anyString())).thenReturn(listOfShells);

    classInstance.checkAttributeSemantics(session, new Resource(), attributeToCheck);
  }

  /**
   * Test of checkAttributeSemantics method, of class urn_perun_resource_attribute_def_def_shells. with empty
   * attribute.
   */
  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsEmptyAttribute() throws Exception {
    System.out.println("testCheckAttributeSemanticsEmptyAttribute()");

    Attribute attributeToCheck = new Attribute();
    //when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(Facility.class),
    // anyString())).thenReturn(listOfShells);
    classInstance.checkAttributeSemantics(session, new Resource(), attributeToCheck);
    fail("Attribute without value has not thrown WrongAttributeSemanticsException.");
  }

  /**
   * Test of checkAttributeSemantics method, of class urn_perun_resource_attribute_def_def_shells. attempting to set
   * shell which is not available at that particular resource.
   */
  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsUnknownShell() throws Exception {
    System.out.println("testCheckAttributeSemanticsUnknownShell()");

    Attribute attributeToCheck = new Attribute();
    attributeToCheck.setValue(new ArrayList<String>() {{
      add("/bin/bash");
      add("/hypershell");
    }});

    when(session.getPerunBl().getAttributesManagerBl()
        .getAttribute(any(PerunSession.class), any(Facility.class), anyString())).thenReturn(listOfShells);

    classInstance.checkAttributeSemantics(session, new Resource(), attributeToCheck);

    fail("Unknown shell at facility was approved");
  }

  /**
   * Test of fillAttribute method, of class urn_perun_resource_attribute_def_def_shells. with all parameters properly
   * set.
   */
  @Test
  public void testFillAttribute() throws Exception {
    System.out.println("testFillAttribute()");

    when(session.getPerunBl().getAttributesManagerBl()
        .getAttribute(any(PerunSession.class), any(Facility.class), anyString())).thenReturn(listOfShells);
    final Attribute result = classInstance.fillAttribute(session, new Resource(), new AttributeDefinition());

    assertEquals("fillAttribute has filled different shells than expected", listOfShells, result);
  }
}
