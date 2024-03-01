package cz.metacentrum.perun.core.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author Metodej Klang
 */
public class ExtSourceEGISSOTest {

  @Spy
  private static ExtSourceEGISSO extSourceEGISSO;

  @Before
  public void setUp() throws Exception {
    extSourceEGISSO = new ExtSourceEGISSO();

    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getUsersSubjectsTest() throws Exception {
    System.out.println("getUsersSubjectsTest");

    // define needed attributes
    String usersQuery = "firstName=josef";
    Map<String, String> mapOfAttributes = new HashMap<>();
    mapOfAttributes.put("usersQuery", usersQuery);
    doReturn(mapOfAttributes).when(extSourceEGISSO).getAttributes();

    // mock connection and define received attributes
    DirContext dirContext = mock(DirContext.class);
    doReturn(dirContext).when(extSourceEGISSO).getContext();
    Attributes attributes = new BasicAttributes();
    attributes.put(new BasicAttribute("cn", "josef"));
    NamingEnumeration<SearchResult> namingEnumeration = mock(NamingEnumeration.class);
    doReturn(namingEnumeration).when(dirContext).search(anyString(), anyString(), any());
    doReturn(true, false).when(namingEnumeration).hasMore();
    SearchResult searchResult = new SearchResult("name", namingEnumeration, attributes);
    doReturn(searchResult).when(namingEnumeration).next();
    extSourceEGISSO.mapping = new HashMap<>();
    extSourceEGISSO.mapping.put("cn", "cn");

    // create expected subject to get
    List<Map<String, String>> expectedSubjects = new ArrayList<>();
    Map<String, String> subject = new HashMap<>();
    subject.put("cn", "josef");
    expectedSubjects.add(subject);

    // test the method
    List<Map<String, String>> actualSubjects = extSourceEGISSO.getUsersSubjects();
    assertEquals("subjects should be same", expectedSubjects, actualSubjects);
  }
}
