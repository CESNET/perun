package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

/**
 * Test of IP Addresses attribute.
 *
 * @author Metodej Klang <metodej.klang@gmail.com>
 */
public class urn_perun_user_attribute_def_def_IPAddressesTest {

	private static urn_perun_user_attribute_def_def_IPAddresses classInstance;
	private static PerunSessionImpl session;
	private static User user;

	@Before
	public void setUp() {
		classInstance = new urn_perun_user_attribute_def_def_IPAddresses();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
		user = new User();
	}

	@Test
	public void testCheckAttributeValue() throws Exception {
		System.out.println("testCheckAttributeValue()");

		Attribute attributeToCheck = new Attribute();
		List ipAddresses = new ArrayList<>();
		ipAddresses.add("255.255.255.255");
		ipAddresses.add("192.168.1.0");
		ipAddresses.add("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff");
		ipAddresses.add("2001:db8:85a3:8d3:1319:8a2e:370:7348");
		ipAddresses.add("2001:db8::2:1");

		attributeToCheck.setValue(ipAddresses);
		classInstance.checkAttributeValue(session, user, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeValueWithWrongValue() throws Exception {
		System.out.println("testCheckAttributeValueWithWrongValue()");

		Attribute attributeToCheck = new Attribute();
		List ipAddresses = new ArrayList<>();
		ipAddresses.add("123");
		attributeToCheck.setValue(ipAddresses);

		classInstance.checkAttributeValue(session, user, attributeToCheck);
	}
}
