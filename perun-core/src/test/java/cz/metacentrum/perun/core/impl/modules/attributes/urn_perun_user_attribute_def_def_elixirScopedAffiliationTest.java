package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.mock;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_user_attribute_def_def_elixirScopedAffiliationTest {

  private static urn_perun_user_attribute_def_def_elixirScopedAffiliation classInstance;
  private static PerunSessionImpl session;
  private static User user;
  private static Attribute attributeToCheck;

  @Before
  public void setUp() {
    classInstance = new urn_perun_user_attribute_def_def_elixirScopedAffiliation();
    session = mock(PerunSessionImpl.class);
    user = new User();
    attributeToCheck = new Attribute();
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxWithWrongValue() throws Exception {
    System.out.println("testCheckAttributeSyntaxWithWrongValue()");
    List<String> value = new ArrayList<>();
    value.add("bad@example");
    attributeToCheck.setValue(value);

    classInstance.checkAttributeSyntax(session, user, attributeToCheck);
  }

  @Test
  public void testCorrectSyntax() throws Exception {
    System.out.println("testCheckAttributeSyntax()");
    List<String> value = new ArrayList<>();
    value.add("member@my.example");
    attributeToCheck.setValue(value);

    classInstance.checkAttributeSyntax(session, user, attributeToCheck);
  }
}
