package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.mock;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_user_facility_attribute_def_def_shell_passwd_scpTest {

  private urn_perun_user_facility_attribute_def_def_shell_passwd_scp classInstance;
  private Attribute attributeToCheck;
  private Facility facility = new Facility();
  private User user = new User();
  private PerunSessionImpl sess;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_user_facility_attribute_def_def_shell_passwd_scp();
    attributeToCheck = new Attribute();
    sess = mock(PerunSessionImpl.class);

  }

  @Test
  public void testSemanticsCorrect() throws Exception {
    System.out.println("testSemanticsCorrect()");
    attributeToCheck.setValue("/example");

    classInstance.checkAttributeSemantics(sess, user, facility, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsWithNullValue() throws Exception {
    System.out.println("testSemanticsWithNullValue()");
    attributeToCheck.setValue(null);

    classInstance.checkAttributeSemantics(sess, user, facility, attributeToCheck);
  }

  @Test
  public void testSyntaxCorrect() throws Exception {
    System.out.println("testSyntaxCorrect()");
    attributeToCheck.setValue("/example");

    classInstance.checkAttributeSyntax(sess, user, facility, attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testSyntaxWithWrongFormat() throws Exception {
    System.out.println("testSyntaxWithWrongFormat()");
    attributeToCheck.setValue("bad_example");

    classInstance.checkAttributeSyntax(sess, user, facility, attributeToCheck);
  }
}
