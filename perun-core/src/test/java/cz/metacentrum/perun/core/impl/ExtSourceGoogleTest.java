package cz.metacentrum.perun.core.impl;

import com.google.api.services.directory.Directory;
import com.google.api.services.directory.model.Member;
import com.google.api.services.directory.model.Members;
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
public class ExtSourceGoogleTest {

  @Spy
  private static ExtSourceGoogle extSourceGoogle;

  private String domainName;
  private String groupName;

  @Before
  public void setUp() throws Exception {
    extSourceGoogle = new ExtSourceGoogle();

    Members members = fillInMemberList();
    domainName = "parker.sm";
    groupName = "spectacular";

    MockitoAnnotations.initMocks(this);

    // mock google connection
    Directory directory = mock(Directory.class, RETURNS_DEEP_STUBS);
    doReturn(directory).when(extSourceGoogle).getDirectoryService();
    when(directory.members().list(groupName).execute()).thenReturn(members);

  }

  @Test
  public void getUsersSubjectsQueryWithEqualTest() throws Exception {
    System.out.println("getUsersSubjectsQueryWithEqualTest");

    // define needed attributes
    Map<String, String> mapOfAttributes = new HashMap<>();
    mapOfAttributes.put("usersQuery", "id=1");
    mapOfAttributes.put("userEmail", domainName);
    mapOfAttributes.put("domain", domainName);
    mapOfAttributes.put("group", groupName);
    mapOfAttributes.put("googleMapping", "userID={userID},\ndomainName={domainName},\ngroupName={groupName}");
    doReturn(mapOfAttributes).when(extSourceGoogle).getAttributes();

    // create expected subject to get
    List<Map<String, String>> expectedSubjects = new ArrayList<>();
    Map<String, String> subject = new HashMap<>();
    subject.put("userID", "1");
    subject.put("domainName", domainName);
    subject.put("groupName", groupName);
    expectedSubjects.add(subject);

    // test the method
    List<Map<String, String>> actualSubjects = extSourceGoogle.getUsersSubjects();
    assertEquals("subjects should be same", expectedSubjects, actualSubjects);
  }

  @Test
  public void getUsersSubjectsQueryWithContainsAnyTest() throws Exception {
    System.out.println("getUsersSubjectsQueryWithContainsAnyTest");

    // define needed attributes
    Map<String, String> mapOfAttributes = new HashMap<>();
    mapOfAttributes.put("usersQuery", "email contains ");
    mapOfAttributes.put("userEmail", domainName);
    mapOfAttributes.put("domain", domainName);
    mapOfAttributes.put("group", groupName);
    mapOfAttributes.put("googleMapping", "userID={userID},\ndomainName={domainName},\ngroupName={groupName}");
    doReturn(mapOfAttributes).when(extSourceGoogle).getAttributes();

    // create expected subject to get
    List<Map<String, String>> expectedSubjects = new ArrayList<>();
    Map<String, String> subject = new HashMap<>();
    subject.put("userID", "1");
    subject.put("domainName", domainName);
    subject.put("groupName", groupName);
    expectedSubjects.add(subject);
    subject = new HashMap<>();
    subject.put("userID", "2");
    subject.put("domainName", domainName);
    subject.put("groupName", groupName);
    expectedSubjects.add(subject);

    // test the method
    List<Map<String, String>> actualSubjects = extSourceGoogle.getUsersSubjects();
    assertEquals("subjects should be same", expectedSubjects, actualSubjects);
  }

  @Test
  public void getUsersSubjectsQueryWithContainsTest() throws Exception {
    System.out.println("getUsersSubjectsQueryWithContainsTest");

    // define needed attributes
    Map<String, String> mapOfAttributes = new HashMap<>();
    mapOfAttributes.put("usersQuery", "email contains mj");
    mapOfAttributes.put("userEmail", domainName);
    mapOfAttributes.put("domain", domainName);
    mapOfAttributes.put("group", groupName);
    mapOfAttributes.put("googleMapping", "userID={userID},\ndomainName={domainName},\ngroupName={groupName}");
    doReturn(mapOfAttributes).when(extSourceGoogle).getAttributes();

    // create expected subject to get
    List<Map<String, String>> expectedSubjects = new ArrayList<>();
    Map<String, String> subject = new HashMap<>();
    subject.put("userID", "2");
    subject.put("domainName", domainName);
    subject.put("groupName", groupName);
    expectedSubjects.add(subject);

    // test the method
    List<Map<String, String>> actualSubjects = extSourceGoogle.getUsersSubjects();
    assertEquals("subjects should be same", expectedSubjects, actualSubjects);
  }

  private Members fillInMemberList() {
    List<Member> memberList = new ArrayList<>();
    Member member = new Member();
    member.setId("1");
    member.setEmail("peter@parker.sm");
    memberList.add(member);
    member = new Member();
    member.setId("2");
    member.setEmail("mj@parker.sm");
    memberList.add(member);
    Members members = new Members();
    members.setMembers(memberList);

    return members;
  }
}
