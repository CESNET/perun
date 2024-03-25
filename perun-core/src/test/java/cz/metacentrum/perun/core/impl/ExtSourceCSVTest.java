package cz.metacentrum.perun.core.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/**
 * @author Metodej Klang
 */
public class ExtSourceCSVTest {

  @Spy
  private static ExtSourceCSV extSourceCSV;

  @Test
  public void getUsersSubjectsQueryWithContainsTest() throws Exception {
    System.out.println("getUsersSubjectsQueryWithContainsTest");

    // create temporal csv file containing new subjects
    File temp = File.createTempFile("temp", ".csv");
    temp.deleteOnExit();

    // define needed attributes
    Map<String, String> mapOfAttributes = new HashMap<>();
    mapOfAttributes.put("usersQuery", "login contains ");
    mapOfAttributes.put("file", temp.getAbsolutePath());
    mapOfAttributes.put("csvMapping", "firstName={firstName},\nlogin={login}");
    doReturn(mapOfAttributes).when(extSourceCSV).getAttributes();

    // fill in the file
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {
      bw.write("\"firstName\",\"login\"\n\"bruce\",\"xwayne\"\n\"batman\",\"xbatman\"");
    }

    // create expected subject to get
    List<Map<String, String>> expectedSubjects = new ArrayList<>();
    Map<String, String> mapOfSubject = new HashMap<>();
    mapOfSubject.put("firstName", "bruce");
    mapOfSubject.put("login", "xwayne");
    expectedSubjects.add(mapOfSubject);
    mapOfSubject = new HashMap<>();
    mapOfSubject.put("firstName", "batman");
    mapOfSubject.put("login", "xbatman");
    expectedSubjects.add(mapOfSubject);

    List<Map<String, String>> actualSubjects = extSourceCSV.getUsersSubjects();
    assertEquals("subjects should be same", expectedSubjects, actualSubjects);
  }

  @Test
  public void getUsersSubjectsQueryWithEqualTest() throws Exception {
    System.out.println("getUsersSubjectsQueryWithEqualTest");

    // create temporal csv file containing new subjects
    File temp = File.createTempFile("temp", ".csv");
    temp.deleteOnExit();

    // define needed attributes
    Map<String, String> mapOfAttributes = new HashMap<>();
    mapOfAttributes.put("usersQuery", "login=xwayne");
    mapOfAttributes.put("file", temp.getAbsolutePath());
    mapOfAttributes.put("csvMapping", "firstName={firstName},\nlogin={login}");
    doReturn(mapOfAttributes).when(extSourceCSV).getAttributes();

    // fill in the file
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {
      bw.write("\"firstName\",\"login\"\n\"bruce\",\"xwayne\"\n\"batman\",\"xbatman\"");
    }

    // create expected subject to get
    List<Map<String, String>> expectedSubjects = new ArrayList<>();
    Map<String, String> mapOfSubject = new HashMap<>();
    mapOfSubject.put("firstName", "bruce");
    mapOfSubject.put("login", "xwayne");
    expectedSubjects.add(mapOfSubject);

    // test the method
    List<Map<String, String>> actualSubjects = extSourceCSV.getUsersSubjects();
    assertEquals("subjects should be same", expectedSubjects, actualSubjects);
  }

  @Before
  public void setUp() throws Exception {
    extSourceCSV = new ExtSourceCSV();

    MockitoAnnotations.initMocks(this);
  }
}
