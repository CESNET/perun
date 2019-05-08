package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

/**
 * Test of UserExtSourcePriority
 *
 * @author Pavel Vyskocil <vyskocilpavel@muni.cz>
 */
public class urn_perun_ues_attribute_def_def_priorityTest {

	private static PerunSessionImpl session;
	private static UserExtSource userExtSource;
	private static urn_perun_ues_attribute_def_def_priority classInstance;

	@Before
	public void setUp() {
		classInstance = new urn_perun_ues_attribute_def_def_priority();
		userExtSource = new UserExtSource();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
	}

	@Test
	public void testCheckAttributeValue() throws Exception {
		System.out.println("testCheckAttributeValue()");

		Attribute attributeToCheck = new Attribute();

		attributeToCheck.setValue(null);
		classInstance.checkAttributeValue(session, userExtSource, attributeToCheck);

		attributeToCheck.setValue(0);
		classInstance.checkAttributeValue(session, userExtSource, attributeToCheck);

		attributeToCheck.setValue(10);
		classInstance.checkAttributeValue(session, userExtSource, attributeToCheck);

		attributeToCheck.setValue(521);
		classInstance.checkAttributeValue(session, userExtSource, attributeToCheck);
	}

	@Test (expected = WrongAttributeValueException.class)
	public void testCheckAttributeValueWithWrongValue() throws Exception {
		System.out.println("testCheckAttributeValueWithWrongAttribute()");

		Attribute attributeToCheck = new Attribute();

		attributeToCheck.setValue(-1);
		classInstance.checkAttributeValue(session, userExtSource, attributeToCheck);
	}
}
