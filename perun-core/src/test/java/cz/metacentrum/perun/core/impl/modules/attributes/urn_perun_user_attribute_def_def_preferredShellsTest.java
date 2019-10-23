package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.bl.ModulesUtilsBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_user_attribute_def_def_preferredShellsTest {

	private static urn_perun_user_attribute_def_def_preferredShells classInstance;
	private static PerunSessionImpl session;
	private static User user;
	private static Attribute attributeToCheck;

	@Before
	public void setUp() {
		classInstance = new urn_perun_user_attribute_def_def_preferredShells();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
		user = new User();
		attributeToCheck = new Attribute();

		PerunBl perunBl = mock(PerunBl.class);
		when(session.getPerunBl()).thenReturn(perunBl);

		ModulesUtilsBl modulesUtilsBl = mock(ModulesUtilsBl.class);
		when(perunBl.getModulesUtilsBl()).thenReturn(modulesUtilsBl);
	}

	@Test
	public void testCheckAttributeSyntax() throws Exception {
		System.out.println("testCheckAttributeSyntax()");
		List<String> value = new ArrayList<>();
		value.add("example");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeSyntaxWithWrongValue() throws Exception {
		System.out.println("testCheckAttributeSyntaxWithWrongValue()");
		List<String> value = new ArrayList<>();
		value.add(null);
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}
}
