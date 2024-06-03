package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_facility_attribute_def_def_fsScratchDirsTest {
  private static urn_perun_facility_attribute_def_def_fsScratchDirs classInstance;
  private static PerunSessionImpl session;
  private static Attribute attribute;

  @Before
  public void setUp() {
    classInstance = new urn_perun_facility_attribute_def_def_fsScratchDirs();
    session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    attribute = new Attribute();
  }

  @Test
  public void testCheckAttributeSemanticsCorrect() throws Exception {
    System.out.println("testCheckAttributeSemanticsCorrect()");

    Map<String, String> scratchDirs = new LinkedHashMap<>();
    scratchDirs.put("/mnt/mymountpoint1", "0700");
    attribute.setValue(scratchDirs);

    classInstance.checkAttributeSemantics(session, new Facility(), attribute);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testCheckAttributeSemanticsEmptyAttribute() throws Exception {
    System.out.println("testCheckAttributeSemanticsEmptyAttribute()");

    classInstance.checkAttributeSemantics(session, new Facility(), attribute);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxScratchDoubleSlash() throws Exception {
    System.out.println("testCheckAttributeSyntaxScratchDoubleSlash()");

    Map<String, String> scratchDirs = new LinkedHashMap<>();
    scratchDirs.put("/mnt/mymountpoint1", "0700");
    scratchDirs.put("/mnt//mymountpoint1", "0700");
    attribute.setValue(scratchDirs);

    classInstance.checkAttributeSyntax(session, new Facility(), attribute);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxScratchTrailingSlash() throws Exception {
    System.out.println("testCheckAttributeSyntaxScratchTrailingSlash()");

    Map<String, String> scratchDirs = new LinkedHashMap<>();
    scratchDirs.put("/mnt/mymountpoint1", "0700");
    scratchDirs.put("/mnt/mymountpoint1/", "0700");
    attribute.setValue(scratchDirs);

    classInstance.checkAttributeSyntax(session, new Facility(), attribute);
  }


  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxWrongHomePoint() throws Exception {
    System.out.println("testCheckAttributeSyntaxWrongHomePoint()");

    Map<String, String> scratchDirs = new LinkedHashMap<>();
    scratchDirs.put("/mnt/mymountpoint1@@s", "0700");
    attribute.setValue(scratchDirs);

    classInstance.checkAttributeSyntax(session, new Facility(), attribute);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxWrongPermissions() throws Exception {
    System.out.println("testCheckAttributeSyntaxWrongPermissions()");

    Map<String, String> scratchDirs = new LinkedHashMap<>();
    scratchDirs.put("/mnt/mymountpoint1", "0x700");
    attribute.setValue(scratchDirs);

    classInstance.checkAttributeSyntax(session, new Facility(), attribute);
  }

  @Test
  public void testCheckAttributeSyntaxCorrect() throws Exception {
    System.out.println("testCheckAttributeSyntaxCorrect()");

    Map<String, String> scratchDirs = new LinkedHashMap<>();
    scratchDirs.put("/mnt/mymountpoint1", "0700");
    scratchDirs.put("/mnt/mymountpoint2", "713");
    attribute.setValue(scratchDirs);

    classInstance.checkAttributeSyntax(session, new Facility(), attribute);
  }
}
