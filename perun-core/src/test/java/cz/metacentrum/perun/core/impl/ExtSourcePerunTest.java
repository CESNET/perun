package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.rpc.deserializer.JsonDeserializer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author Metodej Klang
 */
public class ExtSourcePerunTest {

  @Spy
  private static ExtSourcePerun extSourcePerun;

  @Before
  public void setUp() throws Exception {
    extSourcePerun = new ExtSourcePerun();

    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void getUsersSubjectsTest() throws Exception {
    System.out.println("getUsersSubjectsTest");

    // set up list of rich users
    AttributeDefinition attributeDefinition = new AttributeDefinition();
    attributeDefinition.setFriendlyName("firstName");
    attributeDefinition.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
    attributeDefinition.setType(String.class.getName());
    Attribute attribute = new Attribute(attributeDefinition, "metodej");

    ExtSource extSource = new ExtSource();
    extSource.setName("extSourceNameForLogin");

    UserExtSource userExtSource = new UserExtSource();
    userExtSource.setLogin("extSourceNameForLogin");
    userExtSource.setExtSource(extSource);
    RichUser richUser =
        new RichUser(new User(), Collections.singletonList(userExtSource), Collections.singletonList(attribute));

    List<RichUser> richUserList = new ArrayList<>();
    richUserList.add(richUser);

    JsonDeserializer deserializer = mock(JsonDeserializer.class);
    doReturn(deserializer).when(extSourcePerun).call("usersManager", "findRichUsers", "searchString=query");
    doReturn(richUserList).when(deserializer).readList(RichUser.class);

    // define needed attributes
    Map<String, String> mapOfAttributes = new HashMap<>();
    mapOfAttributes.put("usersQuery", "query");
    mapOfAttributes.put("perunUrl", "perunUrl");
    mapOfAttributes.put("username", "username");
    mapOfAttributes.put("password", "password");
    mapOfAttributes.put("extSourceNameForLogin", "extSourceNameForLogin");
    mapOfAttributes.put("xmlMapping", "firstName={urn:perun:user:attribute-def:def:firstName},\nlogin={login}");
    doReturn(mapOfAttributes).when(extSourcePerun).getAttributes();

    // create expected subject to get
    List<Map<String, String>> expectedSubjects = new ArrayList<>();
    Map<String, String> subject = new HashMap<>();
    subject.put("login", "extSourceNameForLogin");
    subject.put("firstName", "metodej");
    expectedSubjects.add(subject);

    // test the method
    List<Map<String, String>> actualSubjects = extSourcePerun.getUsersSubjects();
    assertEquals("subjects should be same", expectedSubjects, actualSubjects);
  }
}
