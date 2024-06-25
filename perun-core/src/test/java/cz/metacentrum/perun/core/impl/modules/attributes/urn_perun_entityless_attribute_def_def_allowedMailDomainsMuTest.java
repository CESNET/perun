package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import static org.mockito.Mockito.mock;

public class urn_perun_entityless_attribute_def_def_allowedMailDomainsMuTest {
  private static urn_perun_entityless_attribute_def_def_allowedMailDomains_mu classInstance;
  private static PerunSessionImpl session;
  private static Attribute attributeToCheck;

  @Before
  public void setUp() {
    classInstance = new urn_perun_entityless_attribute_def_def_allowedMailDomains_mu();
    session = mock(PerunSessionImpl.class);
    attributeToCheck = new Attribute();
  }

  @Test
  public void checkCorrectSyntax() throws Exception {
    System.out.println("correctSyntax");

    ArrayList<String> val = new ArrayList<>();
    val.add("/[@|\\.]kypo\\.cz/i");
    attributeToCheck.setValue(val);

    classInstance.checkAttributeSyntax(session, "test", attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void checkCorrectRegexIncorrectFilter() throws Exception {
    System.out.println("checkCorrectRegexIncorrectFilter");

    ArrayList<String> val = new ArrayList<>();
    val.add("/random.regex/i");
    attributeToCheck.setValue(val);

    classInstance.checkAttributeSyntax(session, "test", attributeToCheck);
  }

  @Test
  public void checkCorrectRegexCorrectFilter() throws Exception {
    System.out.println("checkCorrectRegexIncorrectFilter");

    ArrayList<String> val = new ArrayList<>();
    // This is technically correct, as this could match top level domains
    val.add("/[@|\\.]random/i");
    attributeToCheck.setValue(val);

    classInstance.checkAttributeSyntax(session, "test", attributeToCheck);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void checkIncorrectSyntax() throws WrongAttributeValueException {
    System.out.println("incorrectSyntax");

    ArrayList<String> val = new ArrayList<>();
    val.add("[");
    attributeToCheck.setValue(val);

    classInstance.checkAttributeSyntax(session, "test", attributeToCheck);
  }
}
