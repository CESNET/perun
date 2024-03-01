package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class urn_perun_user_facility_attribute_def_def_o365AccountExtensionTest {

  private urn_perun_user_facility_attribute_def_def_o365AccountExtension classInstance;
  private Attribute attributeToCheck;
  private Facility facility = new Facility();
  private User user = new User();
  private PerunSessionImpl sess;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_user_facility_attribute_def_def_o365AccountExtension();
    attributeToCheck = new Attribute();
    sess = mock(PerunSessionImpl.class);

  }

  @Test(expected = WrongAttributeValueException.class)
  public void testSyntaxWithWrongFormat() throws Exception {
    System.out.println("testSyntaxWithWrongFormat()");
    attributeToCheck.setValue("2018-31-12");

    classInstance.checkAttributeSyntax(sess, user, facility, attributeToCheck);
  }

  @Test
  public void testSyntaxCorrect() throws Exception {
    System.out.println("testSyntaxCorrect()");
    attributeToCheck.setValue("2018-12-31");

    classInstance.checkAttributeSyntax(sess, user, facility, attributeToCheck);
  }
}
