package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_group_attribute_def_def_groupSynchronizationFilenameTest {

  private static PerunSessionImpl session;
  private static urn_perun_group_attribute_def_def_groupSynchronizationFilename classInstance;
  private static Group group;

  @Before
  public void setUp() {
    classInstance = new urn_perun_group_attribute_def_def_groupSynchronizationFilename();
    group = new Group();
    session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
  }

  @Test
  public void testCheckAttributeSyntax() throws Exception {
    System.out.println("testCheckAttributeSyntax()");

    Attribute attributeToCheck = new Attribute();

    attributeToCheck.setValue("filename.txt");
    classInstance.checkAttributeSemantics(session, group, attributeToCheck);

    attributeToCheck.setValue("filename.json");
    classInstance.checkAttributeSemantics(session, group, attributeToCheck);

    attributeToCheck.setValue("Random_FILENAME_123456");
    classInstance.checkAttributeSemantics(session, group, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxWithWrongValueRootDirectory() throws Exception {
    System.out.println("testCheckAttributeSyntaxWithWrongValueRootDirectory()");

    Attribute attributeToCheck = new Attribute();

    attributeToCheck.setValue("/filename");
    classInstance.checkAttributeSyntax(session, group, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxWithWrongValueDirectoryTraversal() throws Exception {
    System.out.println("testCheckAttributeSyntaxWithWrongValueDirectoryTraversal()");

    Attribute attributeToCheck = new Attribute();

    attributeToCheck.setValue("../filename");
    classInstance.checkAttributeSyntax(session, group, attributeToCheck);
  }
}
