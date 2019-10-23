package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public class urn_perun_user_attribute_def_def_userCertificatesTest {

	private static urn_perun_user_attribute_def_def_userCertificates classInstance;
	private static PerunSessionImpl session;
	private static User user;
	private static Attribute attributeToCheck;

	@Before
	public void setUp() {
		classInstance = new urn_perun_user_attribute_def_def_userCertificates();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
		user = new User();
		attributeToCheck = new Attribute();
	}

	@Test
	public void testCheckAttributeSyntaxCorrect() throws Exception {
		System.out.println("testCheckAttributeSyntaxCorrect()");
		Map<String, String> value = new LinkedHashMap<>();
		value.put("example", "-----BEGIN CERTIFICATE-----\n" +
			"MIIBwzCCAWqgAwIBAgIRAIi5QRl9kz1wb+SUP20gB1kwCgYIKoZIzj0EAwIwGzEZ\n" +
			"MBcGA1UEAxMQTDVkIFRlc3QgUm9vdCBDQTAeFw0xODExMDYyMjA0MDNaFw0yODEx\n" +
			"MDMyMjA0MDNaMCMxITAfBgNVBAMTGEw1ZCBUZXN0IEludGVybWVkaWF0ZSBDQTBZ\n" +
			"MBMGByqGSM49AgEGCCqGSM49AwEHA0IABAST8h+JftPkPocZyuZ5CVuPUk3vUtgo\n" +
			"cgRbkYk7Ong7ey/fM5fJdRNdeW6SouV5h3nF9JvYKEXuoymSNjGbKomjgYYwgYMw\n" +
			"DgYDVR0PAQH/BAQDAgGmMB0GA1UdJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjAS\n" +
			"BgNVHRMBAf8ECDAGAQH/AgEAMB0GA1UdDgQWBBRc+LHppFk8sflIpm/XKpbNMwx3\n" +
			"SDAfBgNVHSMEGDAWgBTirEpzC7/gexnnz7ozjWKd71lz5DAKBggqhkjOPQQDAgNH\n" +
			"ADBEAiAejDEfua7dud78lxWe9eYxYcM93mlUMFIzbWlOJzg+rgIgcdtU9wIKmn5q\n" +
			"FU3iOiRP5VyLNmrsQD3/ItjUN1f1ouY=\n" +
			"-----END CERTIFICATE-----");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeSyntaxWithWrongBase64Value() throws Exception {
		System.out.println("testCheckAttributeSyntaxWithWrongBase64Value()");
		Map<String, String> value = new LinkedHashMap<>();
		value.put("bad_example", "bad_example");
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void testCheckAttributeSyntaxWithWrongCertificateValue() throws Exception {
		System.out.println("testCheckAttributeSyntaxWithWrongCertificateValue()");
		Map<String, String> value = new LinkedHashMap<>();
		value.put("bad_example", Base64.encodeBase64String("bad_example".getBytes()));
		attributeToCheck.setValue(value);

		classInstance.checkAttributeSyntax(session, user, attributeToCheck);
	}
}
