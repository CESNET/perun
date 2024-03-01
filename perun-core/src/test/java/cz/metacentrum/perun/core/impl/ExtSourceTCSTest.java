package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.Pair;
import org.bouncycastle.cert.X509CertificateHolder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Metodej Klang
 */
public class ExtSourceTCSTest {

  @Spy
  private static ExtSourceTCS extSourceTCS;

  @Before
  public void setUp() throws Exception {
    extSourceTCS = new ExtSourceTCS();

    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getUsersSubjects() {
    // mock certificates
    X509CertificateHolder certificateHolder = mock(X509CertificateHolder.class, RETURNS_DEEP_STUBS);
    when(certificateHolder.getSubject().toString()).thenReturn("certificate");
    Map<String, Pair<X509CertificateHolder, String>> validCertificatesForLogin = new HashMap<>();
    validCertificatesForLogin.put("123456", new Pair<>(certificateHolder, "value"));
    doReturn(validCertificatesForLogin).when(extSourceTCS).prepareStructureOfValidCertificates("url");

    // define needed attributes
    Map<String, String> mapOfAttributes = new HashMap<>();
    mapOfAttributes.put("usersQuery", "url");
    mapOfAttributes.put("googleMapping", "userID={userID},\ndomainName={domainName},\ngroupName={groupName}");
    doReturn(mapOfAttributes).when(extSourceTCS).getAttributes();

    // create expected subject to get
    List<Map<String, String>> expectedSubjects = new ArrayList<>();
    Map<String, String> subject = new HashMap<>();
    subject.put("login", "123456");
    subject.put("additionalues_1",
        "https://idp2.ics.muni.cz/idp/shibboleth|cz.metacentrum.perun.core.impl.ExtSourceIdp|123456@muni.cz|2");
    subject.put("urn:perun:user:attribute-def:def:userCertificates", "certificate:value,");
    expectedSubjects.add(subject);

    // test the method
    List<Map<String, String>> actualSubjects = extSourceTCS.getUsersSubjects();
    assertEquals("subjects should be same", expectedSubjects, actualSubjects);
  }
}
