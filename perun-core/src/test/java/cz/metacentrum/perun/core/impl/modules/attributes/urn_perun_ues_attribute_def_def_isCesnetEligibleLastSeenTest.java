package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeDefinitionExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_ues_attribute_def_def_isCesnetEligibleLastSeenTest extends AbstractPerunIntegrationTest {
  private static final String A_USER_DEF_IS_CESNET_ELIGIBLE_LAST_SEEN =
      AttributesManager.NS_USER_ATTR_DEF + ":isCesnetEligibleLastSeen";
  private urn_perun_ues_attribute_def_def_isCesnetEligibleLastSeen classInstance;
  private Attribute uesAttribute;
  private User user;
  private PerunSessionImpl mockedSession;
  private UserExtSource userExtSource;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_ues_attribute_def_def_isCesnetEligibleLastSeen();
    mockedSession = mock(PerunSessionImpl.class);
    uesAttribute = new Attribute();
    setUpUser();
    userExtSource = new UserExtSource();
    userExtSource.setUserId(user.getId());

    AttributeDefinition def = new AttributeDefinition();
    def.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    def.setType(String.class.getName());
    def.setDescription("isCesnetEligibleLastSeen");
    def.setDisplayName("isCesnetEligibleLastSeen");
    def.setFriendlyName("isCesnetEligibleLastSeen");
    try {
      perun.getAttributesManagerBl().createAttribute(sess, def);
    } catch (AttributeDefinitionExistsException ex) {
      // OK
    }
  }

  private void setUpUser() {
    user = new User();
    user.setFirstName("Firstname");
    user.setLastName("Lastname");
    user = perun.getUsersManagerBl().createUser(sess, user);
    assertNotNull(user);
  }

  private void setUpUserIsCesnetEligibleAttribute(String value) throws Exception {
    Attribute attr = new Attribute(perun.getAttributesManagerBl()
        .getAttributeDefinition(sess, AttributesManager.NS_USER_ATTR_DEF + ":isCesnetEligibleLastSeen"));
    attr.setValue(value);
    perun.getAttributesManagerBl().setAttribute(sess, user, attr);
  }

  @Test
  public void testChangedAttributeHookAttributeWasRemoved() throws Exception {
    System.out.println("testChangedAttributeHookAttributeWasRemoved()");

    uesAttribute.setValue(null); // attribute was removed
    String timestamp = "2019-06-17 17:18:28";
    setUpUserIsCesnetEligibleAttribute(timestamp);

    classInstance.changedAttributeHook((PerunSessionImpl) sess, userExtSource, uesAttribute);
    Attribute userAttribute =
        perun.getAttributesManagerBl().getAttribute(sess, user, A_USER_DEF_IS_CESNET_ELIGIBLE_LAST_SEEN);
    assertEquals(userAttribute.getValue(), timestamp);
  }

  @Test
  public void testChangedAttributeHookUserAttributeHasNullValue() throws Exception {
    System.out.println("testChangedAttributeHookUserAttributeHasNullValue()");

    uesAttribute.setValue("2019-06-17 17:18:28.5");

    classInstance.changedAttributeHook((PerunSessionImpl) sess, userExtSource, uesAttribute);
    Attribute userAttribute =
        perun.getAttributesManagerBl().getAttribute(sess, user, A_USER_DEF_IS_CESNET_ELIGIBLE_LAST_SEEN);
    assertEquals(userAttribute.getValue(), uesAttribute.getValue());
  }

  @Test
  public void testChangedAttributeHookValueIsMoreRecent() throws Exception {
    System.out.println("testChangedAttributeHookValueIsMoreRecent()");

    uesAttribute.setValue("2020-06-17 17:18:28");
    setUpUserIsCesnetEligibleAttribute("2019-06-17 17:18:28");

    classInstance.changedAttributeHook((PerunSessionImpl) sess, userExtSource, uesAttribute);
    Attribute userAttribute =
        perun.getAttributesManagerBl().getAttribute(sess, user, A_USER_DEF_IS_CESNET_ELIGIBLE_LAST_SEEN);
    assertEquals(userAttribute.getValue(), uesAttribute.getValue());
  }

  @Test
  public void testChangedAttributeHookValueIsNotMoreRecent() throws Exception {
    System.out.println("testChangedAttributeHookValueIsNotMoreRecent()");

    uesAttribute.setValue("2018-06-17 17:18:28");
    String timestamp = "2019-06-17 17:18:28";
    setUpUserIsCesnetEligibleAttribute(timestamp);

    classInstance.changedAttributeHook((PerunSessionImpl) sess, userExtSource, uesAttribute);
    Attribute userAttribute =
        perun.getAttributesManagerBl().getAttribute(sess, user, A_USER_DEF_IS_CESNET_ELIGIBLE_LAST_SEEN);
    assertEquals(userAttribute.getValue(), timestamp);
  }

  @Test
  public void testCheckAttributeSyntaxCorrect() throws Exception {
    System.out.println("testCheckAttributeSyntaxCorrect()");

    uesAttribute.setValue("2019-06-17 17:18:28");

    classInstance.checkAttributeSyntax(mockedSession, userExtSource, uesAttribute);
  }

  @Test
  public void testCheckAttributeSyntaxCorrectWithMilliseconds() throws Exception {
    System.out.println("testCheckAttributeSyntaxCorrectWithMilliseconds()");

    uesAttribute.setValue("2019-06-17 17:18:28.22");

    classInstance.checkAttributeSyntax(mockedSession, userExtSource, uesAttribute);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxIncorrect() throws Exception {
    System.out.println("testCheckAttributeSyntaxIncorrect()");

    uesAttribute.setValue("incorrect");

    classInstance.checkAttributeSyntax(mockedSession, userExtSource, uesAttribute);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxIncorrectMinutes() throws Exception {
    System.out.println("testCheckAttributeSyntaxIncorrectMinutes()");

    uesAttribute.setValue("2019-12-17 17:68:28");

    classInstance.checkAttributeSyntax(mockedSession, userExtSource, uesAttribute);
  }

  @Test(expected = WrongAttributeValueException.class)
  public void testCheckAttributeSyntaxIncorrectMonths() throws Exception {
    System.out.println("testCheckAttributeSyntaxIncorrectMonths()");

    uesAttribute.setValue("2019-13-17 17:18:28");

    classInstance.checkAttributeSyntax(mockedSession, userExtSource, uesAttribute);
  }

  @Test
  public void testCheckAttributeSyntaxNull() throws Exception {
    System.out.println("testCheckAttributeSyntaxNull()");

    uesAttribute.setValue(null);

    classInstance.checkAttributeSyntax(mockedSession, userExtSource, uesAttribute);
  }
}
