package cz.metacentrum.perun.core.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author Metodej Klang
 */
public class ExtSourceISMUTest {

  @Spy
  private static ExtSourceISMU extSourceISMU;

  @Before
  public void setUp() throws Exception {
    extSourceISMU = new ExtSourceISMU();

    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getUsersSubjectsTest() throws Exception {
    System.out.println("getUsersSubjectsTest");
    String usersQuery = "https://some.url.cz/";

    // define needed attributes
    Map<String, String> mapOfAttributes = new HashMap<>();
    mapOfAttributes.put("usersQuery", usersQuery);
    doReturn(mapOfAttributes).when(extSourceISMU).getAttributes();

    // create expected subject to get
    List<Map<String, String>> expectedSubjects = new ArrayList<>();
    Map<String, String> subject = new HashMap<>();
    subject.put("login", "123456");
    subject.put("titleBefore", null);
    subject.put("firstName", "Metodej");
    subject.put("lastName", "Klang");
    subject.put("additionalues_1",
        "https://idp2.ics.muni.cz/idp/shibboleth|cz.metacentrum.perun.core.impl.ExtSourceIdp|123456@muni.cz|2");
    subject.put("titleAfter", null);
    expectedSubjects.add(subject);

    // mock connection
    HttpURLConnection http = mock(HttpURLConnection.class);
    doReturn(http).when(extSourceISMU).getHttpConnection(usersQuery, null);
    String input = "123456;;\"Metodej Klang\";Klang;Metodej;";
    InputStream is = new ByteArrayInputStream(input.getBytes());
    doReturn(is).when(http).getInputStream();

    // test the method
    List<Map<String, String>> actualSubjects = extSourceISMU.getUsersSubjects();
    assertEquals("subjects should be same", expectedSubjects, actualSubjects);
  }
}
