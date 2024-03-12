package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.mock;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_member_group_attribute_def_virt_groupStatusIndirectTest extends AbstractPerunIntegrationTest {
  private static urn_perun_member_group_attribute_def_virt_groupStatusIndirect classInstance;
  private static PerunSessionImpl session;
  private static Attribute attributeToCheck;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_member_group_attribute_def_virt_groupStatusIndirect();
    attributeToCheck = new Attribute(classInstance.getAttributeDefinition());
    session = mock(PerunSessionImpl.class);
  }

  @Test
  public void testCheckAttributeValueCorrect() throws Exception {
    System.out.println("testCheckAttributeValueCorrect()");

    attributeToCheck.setValue(true);
    classInstance.checkAttributeSyntax(session, new Member(), new Group(), attributeToCheck);
  }

  @Test
  public void testCheckAttributeValueNull() throws Exception {
    System.out.println("testCheckAttributeValueNull()");
    attributeToCheck.setValue(null);

    classInstance.checkAttributeSyntax(session, new Member(), new Group(), attributeToCheck);
  }

}
