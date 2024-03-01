package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.mock;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_user_facility_attribute_def_virt_groupStatusTest {

  private urn_perun_user_facility_attribute_def_virt_groupStatus classInstance;
  private Attribute attributeToCheck;
  private Facility facility = new Facility();
  private User user = new User();
  private PerunSessionImpl sess;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_user_facility_attribute_def_virt_groupStatus();
    attributeToCheck = new Attribute();
    sess = mock(PerunSessionImpl.class);

  }

  @Test
  public void testSyntaxCorrect() throws Exception {
    System.out.println("testSyntaxCorrect()");

    attributeToCheck.setValue("VALID");
    classInstance.checkAttributeSyntax(sess, user, facility, attributeToCheck);

    attributeToCheck.setValue("EXPIRED");
    classInstance.checkAttributeSyntax(sess, user, facility, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testSyntaxWithWrongStatus() throws Exception {
    System.out.println("testSyntaxWithWrongStatus()");
    attributeToCheck.setValue("bad_example");

    classInstance.checkAttributeSyntax(sess, user, facility, attributeToCheck);
  }
}
