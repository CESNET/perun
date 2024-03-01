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
public class ExtSourceUnityTest {

  @Spy
  private static ExtSourceUnity extSourceUnity;

  @Before
  public void setUp() throws Exception {
    extSourceUnity = new ExtSourceUnity();

    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getUsersSubjectsTest() throws Exception {
    System.out.println("getUsersSubjectsTest");

    // mock connection
    HttpURLConnection http = mock(HttpURLConnection.class);
    doReturn(http).when(extSourceUnity).createConnection("uriAll");
    String input = "{\"members\":[1]}";
    InputStream is = new ByteArrayInputStream(input.getBytes());
    doReturn(is).when(http).getInputStream();

    HttpURLConnection http2 = mock(HttpURLConnection.class);
    doReturn(http2).when(extSourceUnity).createConnection("uriEntity/1/");
    String input2 =
        "{\"id\":1,\"name\":miles,\"state\":valid,\"identities\":[{\"typeId\":identifier,\"translationProfile\":profile,\"value\":xmorales}]}";
    InputStream is2 = new ByteArrayInputStream(input2.getBytes());
    doReturn(is2).when(http2).getInputStream();

    HttpURLConnection http3 = mock(HttpURLConnection.class);
    doReturn(http3).when(extSourceUnity).createConnection("uriEntity/1/attributes");
    String input3 = "[{\"name\":name,\"values\":[miles morales]}]";
    InputStream is3 = new ByteArrayInputStream(input3.getBytes());
    doReturn(is3).when(http3).getInputStream();

    // define needed attributes
    Map<String, String> mapOfAttributes = new HashMap<>();
    mapOfAttributes.put("usersQuery", "login=xmorales");
    mapOfAttributes.put("uriAll", "uriAll");
    mapOfAttributes.put("uriEntity", "uriEntity");
    mapOfAttributes.put("unityMapping", "login={login},\nfirstName={firstName}");
    doReturn(mapOfAttributes).when(extSourceUnity).getAttributes();

    // create expected subject to get
    List<Map<String, String>> expectedSubjects = new ArrayList<>();
    Map<String, String> subject = new HashMap<>();
    subject.put("login", "xmorales");
    expectedSubjects.add(subject);

    // test the method
    List<Map<String, String>> actualSubjects = extSourceUnity.getUsersSubjects();
    assertEquals("subjects should be same", expectedSubjects, actualSubjects);
  }
}
