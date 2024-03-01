package cz.metacentrum.perun.core.bl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.audit.events.ResourceManagerEvents.ServiceAssignedToResource;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.blImpl.ResourcesManagerBlImpl;
import cz.metacentrum.perun.core.impl.Auditer;
import cz.metacentrum.perun.core.impl.ResourcesManagerImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class ResourcesManagerBlImplUnitTest {

  private static final String CLASS_NAME = "ResourcesManagerBlImplUnitTest.";
  /**
   * Partial mock. Use it when you need to mock other methods from the manager.
   */
  private final ResourcesManagerBlImpl resourcesManagerBlMock = mock(ResourcesManagerBlImpl.class);
  private final ResourcesManagerImpl resourcesManagerImplMock = mock(ResourcesManagerImpl.class);
  private final AttributesManagerBl attributesManagerBlMock = mock(AttributesManagerBl.class);
  private final PerunBl perunBlMock = mock(PerunBl.class, RETURNS_DEEP_STUBS);
  private final Auditer auditerMock = mock(Auditer.class);
  private final PerunSession sessionMock = mock(PerunSession.class);
  private ResourcesManagerBlImpl resourcesManagerBl;

  @Test
  public void assignServices() throws Exception {
    System.out.println(CLASS_NAME + "assignServices");

    Resource resource = new Resource(1, "r1", "", -1);
    Service s1 = new Service(1, "s1");
    Service s2 = new Service(2, "s2");
    List<Service> services = Arrays.asList(s1, s2);

    doCallRealMethod().when(resourcesManagerBlMock).assignServices(sessionMock, resource, services);

    resourcesManagerBlMock.assignServices(sessionMock, resource, services);

    for (Service service : services) {
      verify(resourcesManagerImplMock, times(1)).assignService(sessionMock, resource, service);
      verify(auditerMock, times(1)).log(sessionMock, new ServiceAssignedToResource(service, resource));
    }
  }

  @Test
  public void checkSemanticsOfFacilityAndResourceRequiredAttributes() throws Exception {
    System.out.println(CLASS_NAME + "checkSemanticsOfFacilityAndResourceRequiredAttributes");

    Facility facility = new Facility(1, "Facility");
    Resource resource = new Resource(2, "r", "", facility.getId());

    List<Attribute> facilityAttributes = Collections.singletonList(new Attribute());
    List<Attribute> resourceAttributes = Arrays.asList(new Attribute(), new Attribute());

    when(perunBlMock.getFacilitiesManagerBl().getFacilityById(sessionMock, facility.getId())).thenReturn(facility);
    when(attributesManagerBlMock.getRequiredAttributes(sessionMock, facility)).thenReturn(facilityAttributes);
    when(attributesManagerBlMock.getRequiredAttributes(sessionMock, resource)).thenReturn(resourceAttributes);


    resourcesManagerBl.checkSemanticsOfFacilityAndResourceRequiredAttributes(sessionMock, resource);

    verify(attributesManagerBlMock, times(1)).checkAttributesSemantics(sessionMock, facility, facilityAttributes);
    verify(attributesManagerBlMock, times(1)).checkAttributesSemantics(sessionMock, resource, resourceAttributes);
  }

  @Test
  public void fillAndSetRequiredAttributesForGroups() throws Exception {
    System.out.println(CLASS_NAME + "fillAndSetRequiredAttributesForGroups");

    Resource resource = new Resource();
    List<Service> services = new ArrayList<>();
    Group g1 = new Group("G1", "G1");
    Group g2 = new Group("G2", "G2");
    List<Group> groups = new ArrayList<>();
    groups.add(g1);
    groups.add(g2);

    List<Attribute> requiredAttributes = Collections.singletonList(mock(Attribute.class));
    List<Attribute> filledAttributes = Collections.singletonList(mock(Attribute.class));

    when(resourcesManagerBlMock.getAssignedGroups(sessionMock, resource)).thenReturn(groups);
    when(attributesManagerBlMock.getRequiredAttributes(any(), anyList(), any(), any(), anyBoolean())).thenReturn(
        requiredAttributes);
    when(attributesManagerBlMock.fillAttributes(any(), any(Resource.class), any(Group.class), anyList(),
        anyBoolean())).thenReturn(filledAttributes);

    doCallRealMethod().when(resourcesManagerBlMock).fillAndSetRequiredAttributesForGroups(any(), anyList(), any());

    resourcesManagerBlMock.fillAndSetRequiredAttributesForGroups(sessionMock, services, resource);

    for (Group group : groups) {
      verify(attributesManagerBlMock, times(1)).fillAttributes(sessionMock, resource, group, requiredAttributes, true);
      verify(attributesManagerBlMock, times(1)).setAttributes(sessionMock, resource, group, filledAttributes, true);
    }
  }

  @Before
  public void setUp() {
    ReflectionTestUtils.setField(resourcesManagerBlMock, "resourcesManagerImpl", resourcesManagerImplMock);
    resourcesManagerBl = new ResourcesManagerBlImpl(resourcesManagerImplMock);
    resourcesManagerBl.setPerunBl(perunBlMock);
    when(perunBlMock.getAttributesManagerBl()).thenReturn(attributesManagerBlMock);
    when(resourcesManagerBlMock.getResourcesManagerImpl()).thenReturn(resourcesManagerImplMock);
    when(resourcesManagerBlMock.getPerunBl()).thenReturn(perunBlMock);
    when(perunBlMock.getAuditer()).thenReturn(auditerMock);
  }

  @After
  public void tearDown() {
    Mockito.reset(resourcesManagerBlMock, resourcesManagerImplMock, attributesManagerBlMock, perunBlMock, auditerMock);
  }

  @Test
  public void updateAllRequiredAttributesForAllowedMembers() throws Exception {
    System.out.println(CLASS_NAME + "updateAllRequiredAttributesForAllowedMembers");

    Facility facility = new Facility(1, "Facility");
    Resource resource = new Resource(9, "", "", facility.getId());
    Member m1 = new Member(1);
    Member m2 = new Member(2);
    User u1 = new User(1, "", "", "", "", "");
    User u2 = new User(2, "", "", "", "", "");
    List<Service> services = new ArrayList<>();
    List<Member> members = Arrays.asList(m1, m2);
    List<User> users = Arrays.asList(u1, u2);

    for (int i = 0; i < users.size(); i++) {
      when(perunBlMock.getUsersManagerBl().getUserByMember(sessionMock, members.get(i))).thenReturn(users.get(i));
    }
    when(perunBlMock.getUsersManagerBl().getUserByMember(sessionMock, m2)).thenReturn(u2);
    when(perunBlMock.getFacilitiesManagerBl().getFacilityById(sessionMock, facility.getId())).thenReturn(facility);
    when(resourcesManagerImplMock.getAllowedMembers(sessionMock, resource)).thenReturn(members);

    resourcesManagerBl.updateAllRequiredAttributesForAllowedMembers(sessionMock, resource, services);

    for (int i = 0; i < users.size(); i++) {
      verify(attributesManagerBlMock, times(1)).setRequiredAttributes(sessionMock, services, facility, resource,
          users.get(i), members.get(i), true);
    }
  }
}
