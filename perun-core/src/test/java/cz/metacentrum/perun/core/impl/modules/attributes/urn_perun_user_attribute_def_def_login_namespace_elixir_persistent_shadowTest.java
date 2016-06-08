package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;

/**
 * Testing class for login-namespace elixir persistent shadow attribute
 *
 * @author Sona Mastrakova <sona.mastrakova@gmail.com>
 * @date 03.07.2015
 */
public class urn_perun_user_attribute_def_def_login_namespace_elixir_persistent_shadowTest {

    private static urn_perun_user_attribute_def_def_login_namespace_elixir_persistent_shadow classInstance;
    private static PerunSessionImpl session;
    private static User user;

    @Before
    public void SetUp() throws AttributeNotExistsException, InternalErrorException, WrongAttributeAssignmentException {
        classInstance = new urn_perun_user_attribute_def_def_login_namespace_elixir_persistent_shadow();
        session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
        user = new User();
        user.setId(123456);
    }

    @Test
    public void testCheckAttributeValueElixirNamespace() throws Exception {
        System.out.println("testCheckAttributeValue()");
        Attribute attribute = new Attribute();
        attribute.setFriendlyName("login-namespace:elixir-persistent");
        attribute.setValue("28c5353b8bb34984a8bd4169ba94c606@elixir-europe.org");

        when(session.getPerunBl().getUsersManagerBl().getUsersByAttribute(any(PerunSession.class), any(Attribute.class))).thenReturn(new ArrayList<User>() {
            {
                add(user);
            }
        });

        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(User.class), anyString())).thenReturn(new Attribute() {
            {
                setValue("28c5353b8bb34984a8bd4169ba94c606@elixir-europe.org");
            }
        });

        assertEquals(attribute.getValue().toString(), "28c5353b8bb34984a8bd4169ba94c606@elixir-europe.org");
        classInstance.checkAttributeValue(session, user, attribute);
    }

    @Test
    public void testFillAttributeValueElixirNamespace() throws Exception {
        System.out.println("testFillAttributeValue()");

        Attribute attribute = new Attribute();
        attribute.setFriendlyName("login-namespace:elixir-persistent-shadow");
        attribute.setValue("879a224546cf11fe53863737de037d2d39640258@elixir-europe.org");

        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(User.class), anyString())).thenReturn(new Attribute() {
            {
                setValue("879a224546cf11fe53863737de037d2d39640258@elixir-europe.org");
            }
        });

        Attribute output = classInstance.fillAttribute(session, user, attribute);
        assertEquals("879a224546cf11fe53863737de037d2d39640258@elixir-europe.org", output.getValue());
    }

    @Ignore
    @Test
    public void testChangedAttributeHookElixirNamespace() throws Exception {
        System.out.println("testChangedAttributeHook()");
    }
}
