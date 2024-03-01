package cz.metacentrum.perun.core.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;

/**
 * @author Metodej Klang
 */
public class ExtSourceXMLTest {

  @Spy
  private static ExtSourceXML extSourceXML;

  @Before
  public void setUp() throws Exception {
    extSourceXML = new ExtSourceXML();

    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getUsersSubjectsTest() throws Exception {
    System.out.println("getUsersSubjectsTest");

    // create temporal csv file containing new subjects
    File temp = File.createTempFile("temp", ".xml");
    temp.deleteOnExit();

    // define needed attributes
    Map<String, String> mapOfAttributes = new HashMap<>();
    mapOfAttributes.put("usersQuery", "/Users/User[@id='42']");
    mapOfAttributes.put("file", temp.getAbsolutePath());
    mapOfAttributes.put("xmlMapping", "firstName=firstName,\nlogin=login");
    doReturn(mapOfAttributes).when(extSourceXML).getAttributes();

    // fill in the file
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {
      bw.write("<Users>\n<User id='42'>\n<firstName>arthur</firstName>\n<login>xdent</login>\n</User>\n</Users>");
    }

    // create expected subject to get
    List<Map<String, String>> expectedSubjects = new ArrayList<>();
    Map<String, String> mapOfSubject = new HashMap<>();
    mapOfSubject.put("firstName", "arthur");
    mapOfSubject.put("login", "xdent");
    expectedSubjects.add(mapOfSubject);

    // test the method
    List<Map<String, String>> actualSubjects = extSourceXML.getUsersSubjects();
    assertEquals("subjects should be same", expectedSubjects, actualSubjects);
  }
}
