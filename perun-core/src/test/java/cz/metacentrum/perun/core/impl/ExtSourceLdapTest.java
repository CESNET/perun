package cz.metacentrum.perun.core.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
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
public class ExtSourceLdapTest {

  @Spy
  private static ExtSourceLdap extSourceLdap;

  @Before
  public void setUp() throws Exception {
    extSourceLdap = new ExtSourceLdap();

    MockitoAnnotations.initMocks(this);

    extSourceLdap.mapping = new HashMap<>();
    extSourceLdap.mapping.put("cn", "{firstName}");
    extSourceLdap.mapping.put("dc", "{dc}");
  }

  @Test
  public void getUsersSubjectsNullQueryTest() throws Exception {
    System.out.println("getUsersSubjectsNullQueryTest");

    // define needed attributes
    String base = "cn=firstName,dc=dc";
    Map<String, String> mapOfAttributes = new HashMap<>();
    mapOfAttributes.put("usersQuery", null);
    mapOfAttributes.put("base", base);
    doReturn(mapOfAttributes).when(extSourceLdap).getAttributes();

    // mock connection and define received attributes
    DirContext dirContext = mock(DirContext.class);
    doReturn(dirContext).when(extSourceLdap).getContext();
    Attributes attributes = new BasicAttributes();
    attributes.put(new BasicAttribute("firstName", "josef"));
    attributes.put(new BasicAttribute("dc", "cz"));
    doReturn(attributes).when(dirContext).getAttributes(base);

    // create expected subject to get
    List<Map<String, String>> expectedSubjects = new ArrayList<>();
    Map<String, String> subject = new HashMap<>();
    subject.put("cn", "josef");
    subject.put("dc", "cz");
    expectedSubjects.add(subject);

    // test the method
    List<Map<String, String>> actualSubjects = extSourceLdap.getUsersSubjects();
    assertEquals("subjects should be same", expectedSubjects, actualSubjects);
  }

  @Test
  public void getUsersSubjectsTest() throws Exception {
    System.out.println("getUsersSubjectsTest");

    // define needed attributes
    String base = "cn=firstName,dc=dc";
    String usersQuery = "dc=cz";
    Map<String, String> mapOfAttributes = new HashMap<>();
    mapOfAttributes.put("usersQuery", usersQuery);
    mapOfAttributes.put("base", base);
    doReturn(mapOfAttributes).when(extSourceLdap).getAttributes();

    // mock connection and define received attributes
    DirContext dirContext = mock(DirContext.class);
    doReturn(dirContext).when(extSourceLdap).getContext();
    Attribute attribute = new BasicAttribute("firstName", "josef");
    Attribute attribute2 = new BasicAttribute("dc", "cz");
    Attributes attributes = new BasicAttributes();
    attributes.put(attribute);
    attributes.put(attribute2);
    NamingEnumeration<SearchResult> namingEnumeration = mock(NamingEnumeration.class);
    doReturn(namingEnumeration).when(dirContext).search(anyString(), anyString(), any());
    doReturn(true, false).when(namingEnumeration).hasMore();
    SearchResult searchResult = new SearchResult("name", namingEnumeration, attributes);
    doReturn(searchResult).when(namingEnumeration).next();

    // create expected subject to get
    List<Map<String, String>> expectedSubjects = new ArrayList<>();
    Map<String, String> subject = new HashMap<>();
    subject.put("cn", "josef");
    subject.put("dc", "cz");
    expectedSubjects.add(subject);

    // test the method
    List<Map<String, String>> actualSubjects = extSourceLdap.getUsersSubjects();
    assertEquals("subjects should be same", expectedSubjects, actualSubjects);
  }
}
