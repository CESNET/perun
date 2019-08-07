package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    public void SetUp() {
        classInstance = new urn_perun_user_attribute_def_def_login_namespace_elixir_persistent_shadow();
        session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
        user = new User();
        user.setId(123456);
    }

    @Test
    public void testCheckAttributeSemanticsElixirNamespace() throws Exception {
        System.out.println("testCheckAttributeSemantics()");
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
        classInstance.checkAttributeSemantics(session, user, attribute);
    }

    @Test
    public void testFillAttributeValueElixirNamespace() throws Exception {
        System.out.println("testFillAttributeValue()");

        Attribute attribute = new Attribute();
        attribute.setFriendlyName("login-namespace:elixir-persistent-shadow");
        attribute.setValue("903cb3444a89107fdd6b6198bd26712860f36ebb@elixir-europe.org");

        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(User.class), anyString())).thenReturn(new Attribute() {
            {
                setValue("903cb3444a89107fdd6b6198bd26712860f36ebb@elixir-europe.org");
            }
        });

        Attribute output = classInstance.fillAttribute(session, user, attribute);
        assertEquals("903cb3444a89107fdd6b6198bd26712860f36ebb@elixir-europe.org", output.getValue());
    }

    @Ignore
    @Test
    public void testChangedAttributeHookElixirNamespace() {
        System.out.println("testChangedAttributeHook()");
    }
}
