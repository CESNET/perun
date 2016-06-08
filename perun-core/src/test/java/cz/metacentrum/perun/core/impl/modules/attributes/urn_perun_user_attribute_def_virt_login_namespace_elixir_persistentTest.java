package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Attr;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Testing class for login-namespace elixir-persistent attribute
 *
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class urn_perun_user_attribute_def_virt_login_namespace_elixir_persistentTest {

    private static urn_perun_user_attribute_def_virt_login_namespace_elixir_persistent classInstance;
    private static PerunSessionImpl session;
    private static User user;

    @Before
    public void SetUp() throws AttributeNotExistsException, InternalErrorException, WrongAttributeAssignmentException {
        classInstance = new urn_perun_user_attribute_def_virt_login_namespace_elixir_persistent();
        session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
        user = new User();
        user.setId(123456);
    }

    @Test
    public void testAutoGenerateWithGetMethod() throws Exception {
        System.out.println("testAutoGenerateWithGetMethod()");

        Attribute attribute = new Attribute(classInstance.getAttributeDefinition());

        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(User.class), anyString())).thenReturn(new Attribute() {
            {
                setNamespace(AttributesManager.NS_USER_ATTR_DEF);
                setFriendlyName("login-namespace:elixir-persistent-shadow");
                setType("def");
            }
        });

        when(session.getPerunBl().getAttributesManagerBl().fillAttribute(any(PerunSession.class), any(User.class), any(Attribute.class))).thenReturn(new Attribute() {
            {
                setNamespace(AttributesManager.NS_USER_ATTR_DEF);
                setFriendlyName("login-namespace:elixir-persistent-shadow");
                setType("def");
                setValue("879a224546cf11fe53863737de037d2d39640258@elixir-europe.org");
            }
        });

        Attribute output = classInstance.getAttributeValue(session, user, attribute);
        assertEquals("879a224546cf11fe53863737de037d2d39640258@elixir-europe.org", output.getValue());
    }

}
