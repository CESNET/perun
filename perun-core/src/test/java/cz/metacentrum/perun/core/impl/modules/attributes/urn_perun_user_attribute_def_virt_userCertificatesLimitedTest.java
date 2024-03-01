package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.blImpl.ModulesUtilsBlImpl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class urn_perun_user_attribute_def_virt_userCertificatesLimitedTest {

  private static urn_perun_user_attribute_def_virt_userCertificatesLimited classInstance;
  private static PerunSessionImpl session;
  private static User user;
  private static Attribute mockedCertificateAttribute;
  private static Attribute mockedExpirationsAttribute;
  private static Attribute virtAttributeCertificates;
  private static String newestCertificate = "-----BEGIN CERTIFICATE-----\n" +
      "MIIDrTCCApWgAwIBAgIUFTbqf5jA2frptU28GUTkSBxrOBgwDQYJKoZIhvcNAQEL\n" +
      "BQAwZjELMAkGA1UEBhMCQVUxEzARBgNVBAgMClNvbWUtU3RhdGUxITAfBgNVBAoM\n" +
      "GEludGVybmV0IFdpZGdpdHMgUHR5IEx0ZDEfMB0GCSqGSIb3DQEJARYQdGVzdEBl\n" +
      "eGFtcGxlLm9yZzAeFw0yMDA1MTkwODU4NDRaFw0yMTA1MTkwODU4NDRaMGYxCzAJ\n" +
      "BgNVBAYTAkFVMRMwEQYDVQQIDApTb21lLVN0YXRlMSEwHwYDVQQKDBhJbnRlcm5l\n" +
      "dCBXaWRnaXRzIFB0eSBMdGQxHzAdBgkqhkiG9w0BCQEWEHRlc3RAZXhhbXBsZS5v\n" +
      "cmcwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDCO7ZeN17P22MiANgc\n" +
      "NTubgaq21P/prLMPmmN8nIBNkX0Dcc3CxHglx279kcdd8l9VG7f09hsWlnMp/0Xc\n" +
      "Iu8KGnQ/Q2IqRhmlUjHi0LMR069b/hbF6LPr5qJvQ9B9FK8DB0cWSm5zJIQPktce\n" +
      "GHGCxAzMYcnGf4yQYyVbM/AiGlwJJlT12mwksw7Ao+KxkFA6oqUz2fsFs6d20cBf\n" +
      "sRRRh0f4e6Z9ryAK5/bGWnSpdT/pSkNH/QB1ouHmyHWaiL31uUm5RbDRZ6hyUfZP\n" +
      "roI0K/8aM0crzhkE5Rfznzx/LoxhWY/QzR6duDtjioMVqrqug2SGBD50DYC4UVDP\n" +
      "PNUzAgMBAAGjUzBRMB0GA1UdDgQWBBRnyiUGT1e2PDLOXkiN//LgVTEPlzAfBgNV\n" +
      "HSMEGDAWgBRnyiUGT1e2PDLOXkiN//LgVTEPlzAPBgNVHRMBAf8EBTADAQH/MA0G\n" +
      "CSqGSIb3DQEBCwUAA4IBAQAMqbSmVvelG+tlx14/wVCIgvY5oBJ4ianz9HUJvO/W\n" +
      "42TPACCtAnDHv0f9oez7osydP6ZnFXVOMavC8XNAivyapvxtnlfMW7siNB/i+0dP\n" +
      "IV61Wp2bzkFM+CP2Bz/khwdbnjQ8PrBeRJUw54P2Be96GEVONHlxMBY5RkFRnmGT\n" +
      "LCntxIT7tYW+HbpwCG3tbB5tZKnunEUUka9QWu1Ddj0IqOVsw4tCwbmNKFzMATBN\n" +
      "scfCfD7TXfR77CKS7LJxqZofHWp25+4ybNYV2LjvTL1W9EiMgX+RXSR+crpQ5X69\n" +
      "rO+rrGm1X0tBAiDz1CVvAEX8d8PxKaVd7MTWG42Jjqd8\n" +
      "-----END CERTIFICATE-----";
  private static String oldestCertificate = "-----BEGIN CERTIFICATE-----\n" +
      "MIIDrTCCApWgAwIBAgIUIUUTLFEn1nXnGwjjdmQPnNSqhrUwDQYJKoZIhvcNAQEL\n" +
      "BQAwZjELMAkGA1UEBhMCQVUxEzARBgNVBAgMClNvbWUtU3RhdGUxITAfBgNVBAoM\n" +
      "GEludGVybmV0IFdpZGdpdHMgUHR5IEx0ZDEfMB0GCSqGSIb3DQEJARYQdGVzdEBl\n" +
      "eGFtcGxlLmNvbTAeFw0yMDA1MTkwOTAwMjBaFw0yMTAzMTUwOTAwMjBaMGYxCzAJ\n" +
      "BgNVBAYTAkFVMRMwEQYDVQQIDApTb21lLVN0YXRlMSEwHwYDVQQKDBhJbnRlcm5l\n" +
      "dCBXaWRnaXRzIFB0eSBMdGQxHzAdBgkqhkiG9w0BCQEWEHRlc3RAZXhhbXBsZS5j\n" +
      "b20wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCtAf09xp+fdMWZ0KNq\n" +
      "Z09sbF/CX5FdYgQJnv0WSqqvZ+zZr3ubnKQWCQxUdc95nerBedmZXwRrJ4cK3O66\n" +
      "J3+XjmB0hpHXYKtvdEkv7ZjSOLHu16HdFNqlMqJQiBN5O4bXQneyQ1JhGIDn/waY\n" +
      "L8101D5j17N37h6jcvmMBnwH73pX//CBuy8mJgLlJ+wA4TQSvBRIaeNy6x5vgXa7\n" +
      "XZYIB8jv7Ha5hbwkoOr4wlK7NNhFSH/gRilNi9FfjK5JT75nOS+zsYSzT9k5I5q/\n" +
      "NUTKCyfdmmKg6UlcMF9Mj4BjoUQwoJjKqcF1fZ8VT/YqMGtvn5bu411W2NTP+3uF\n" +
      "fJ+BAgMBAAGjUzBRMB0GA1UdDgQWBBRkx2Or10CqbtqupNj68hXXQHDEbTAfBgNV\n" +
      "HSMEGDAWgBRkx2Or10CqbtqupNj68hXXQHDEbTAPBgNVHRMBAf8EBTADAQH/MA0G\n" +
      "CSqGSIb3DQEBCwUAA4IBAQALSOwB8PlOaJUIZ+D6tqFhv+BTUMSeGfKytRF34fVS\n" +
      "hVu690aOC5zS4NPVp/67bZxG60MY/NG5k1PUzfSpZ0ys4wYDUmY72PlZ+JosPu9I\n" +
      "2EN5gYRrhScEuoO2LZO9O+HPxqpr6h2+9tCU7ise3yxnqqt9GZmlrfeC32eAJ9HC\n" +
      "43FDhimZitLMbFOcywtqBFKEMtZicSDszdO9fWSx5Q2NZ2sVvwEv813B2ld2VUE+\n" +
      "0+pyHkZfX26B2mwtqbcq6QBfDmR4PVYs6aIF9j5GP3JZvYNEovSNeIXfDMkql9Xh\n" +
      "Xk4L/AAyBQfje3i9pwveR6So3MUB4ikDH/elRTPT45rB\n" +
      "-----END CERTIFICATE-----";
  private static Map<String, String> moreCertificates = generateMoreThanTenCertificates();
  private static Map<String, String> moreCertsExpiration =
      ModulesUtilsBlImpl.retrieveCertificatesExpiration(moreCertificates);
  private static Map<String, String> lessCertificates = generateLessThanTenCertificates();
  private static Map<String, String> lessCertsExpiration =
      ModulesUtilsBlImpl.retrieveCertificatesExpiration(lessCertificates);
  private AttributesManagerBl am;

  private static Map<String, String> generateLessThanTenCertificates() {
    Map<String, String> resultMap = new LinkedHashMap<>();

    resultMap.put("DN_1", newestCertificate);
    resultMap.put("DN_2", oldestCertificate);
    resultMap.put("DN_3", newestCertificate);

    return resultMap;
  }

  private static Map<String, String> generateMoreThanTenCertificates() {
    Map<String, String> resultMap = new LinkedHashMap<>();

    resultMap.put("DN_1", newestCertificate);
    resultMap.put("DN_2", newestCertificate);
    resultMap.put("DN_3", newestCertificate);
    resultMap.put("DN_4", newestCertificate);
    resultMap.put("DN_5", oldestCertificate);
    resultMap.put("DN_6", newestCertificate);
    resultMap.put("DN_7", newestCertificate);
    resultMap.put("DN_8", newestCertificate);
    resultMap.put("DN_9", newestCertificate);
    resultMap.put("DN_10", newestCertificate);
    resultMap.put("DN_11", newestCertificate);

    return resultMap;
  }

  @Before
  public void setUp() throws Exception {
    String certificateAttributeName = AttributesManager.NS_USER_ATTR_DEF + ":userCertificates";
    String expirationsAttributeName = AttributesManager.NS_USER_ATTR_VIRT + ":userCertExpirations";
    classInstance = new urn_perun_user_attribute_def_virt_userCertificatesLimited();
    user = new User();

    //prepare mocks
    session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
    PerunBl perunBl = mock(PerunBl.class);
    am = mock(AttributesManagerBl.class);
    mockedCertificateAttribute = mock(Attribute.class);
    mockedExpirationsAttribute = mock(Attribute.class);
    when(session.getPerunBl()).thenReturn(perunBl);
    when(perunBl.getAttributesManagerBl()).thenReturn(am);
    when(am.getAttribute(session, user, certificateAttributeName)).thenReturn(mockedCertificateAttribute);
    when(am.getAttribute(session, user, expirationsAttributeName)).thenReturn(mockedExpirationsAttribute);
    virtAttributeCertificates = new Attribute(classInstance.getAttributeDefinition());
    virtAttributeCertificates.setId(101);
  }

  @Test
  public void testGetAttributeValueWithMoreThanTen() {
    System.out.println("testGetAttributeValueWithMoreThanTen()");

    when(mockedCertificateAttribute.valueAsMap()).thenReturn((LinkedHashMap<String, String>) moreCertificates);
    when(mockedExpirationsAttribute.valueAsMap()).thenReturn((LinkedHashMap<String, String>) moreCertsExpiration);

    List<String> result = classInstance.getAttributeValue(session, user, virtAttributeCertificates).valueAsList();

    assertFalse(result.contains(oldestCertificate));
  }

  @Test
  public void testGetAttributeValueWithLessThanTen() {
    System.out.println("testGetAttributeValueWithLessThanTen()");

    when(mockedCertificateAttribute.valueAsMap()).thenReturn((LinkedHashMap<String, String>) lessCertificates);
    when(mockedExpirationsAttribute.valueAsMap()).thenReturn((LinkedHashMap<String, String>) lessCertsExpiration);

    assertEquals(3, classInstance.getAttributeValue(session, user, virtAttributeCertificates).valueAsList().size());
  }

}
