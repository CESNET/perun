package cz.metacentrum.perun.core.bl;

import com.google.common.collect.Sets;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichAttribute;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.blImpl.AttributesManagerBlImpl;
import cz.metacentrum.perun.core.blImpl.ServicesManagerBlImpl;
import cz.metacentrum.perun.core.impl.AttributesManagerImpl;
import cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_virt_loa;
import cz.metacentrum.perun.core.impl.modules.attributes.urn_perun_user_attribute_def_virt_userCertDNs;
import cz.metacentrum.perun.core.implApi.modules.attributes.AttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserAttributesModuleImplApi;
import cz.metacentrum.perun.core.implApi.modules.attributes.VirtualAttributesModuleImplApi;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static cz.metacentrum.perun.core.api.AttributesManager.NS_USER_ATTR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Class with unit tests for attributesManagerBlImpl.
 * <p>
 * This class is also used for testing the initialization of module dependencies.
 * The double arrow 'A => B' means that A is strongly dependent on B.
 * The single arrow 'A -> B' means that A is dependent on B.
 *
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributesManagerBlImplUnitTests {

  private final static String CLASS_NAME = "AttributesManagerBlImplUnitTests.";
  /**
   * Partial mock.
   * <p>
   * Use when you need to mock only some methods.
   */
  private final AttributesManagerBlImpl attrManagerBlImplMock = mock(AttributesManagerBlImpl.class);
  private final ServicesManagerBlImpl serviceManagerBlImplMock = mock(ServicesManagerBlImpl.class);
  private final AttributesManagerImpl attrManagerImplMock = mock(AttributesManagerImpl.class);
  private final PerunSession sessionMock = mock(PerunSession.class);
  private final PerunBl perunBlMock = mock(PerunBl.class, RETURNS_DEEP_STUBS);
  private AttributesManagerBlImpl attrManagerBlImpl;
  private int idCounter = 1;

  @Before
  public void setUp() {
    ReflectionTestUtils.setField(attrManagerBlImplMock, "attributesManagerImpl", attrManagerImplMock);
    attrManagerBlImpl = new AttributesManagerBlImpl(attrManagerImplMock);
    attrManagerBlImpl.setPerunBl(perunBlMock);
  }

  @After
  public void tearDown() {
    Mockito.reset(attrManagerBlImplMock, attrManagerImplMock, sessionMock, perunBlMock);
  }

  @Test
  public void getRequiredAttributes_services_f_r_u_m() throws Exception {
    System.out.println(CLASS_NAME + "getRequiredAttributes_services_f_r_u_m");

    Service s1 = new Service(1, "s1");
    Service s2 = new Service(2, "s2");
    List<Service> services = Arrays.asList(s1, s2);
    Facility facility = new Facility(1, "f");
    Resource resource = new Resource(2, "r", "", facility.getId());
    User user = new User(3, "a", "b", "", "", "");
    Member member = new Member(4, user.getId());

    Attribute a1 = new Attribute(new AttributeDefinition(), 1);
    Attribute a2 = new Attribute(new AttributeDefinition(), 2);
    Attribute a3 = new Attribute(new AttributeDefinition(), 3);

    List<Attribute> s1Attributes = Arrays.asList(a1, a2);
    List<Attribute> s2Attributes = Arrays.asList(a2, a3);

    doCallRealMethod()
        .when(attrManagerBlImplMock).getRequiredAttributes(sessionMock, services, facility, resource, user, member);

    when(attrManagerBlImplMock.getRequiredAttributes(sessionMock, s1, facility, resource, user, member))
        .thenReturn(s1Attributes);
    when(attrManagerBlImplMock.getRequiredAttributes(sessionMock, s2, facility, resource, user, member))
        .thenReturn(s2Attributes);
    List<Attribute> returnedAttributes = attrManagerBlImplMock
        .getRequiredAttributes(sessionMock, services, facility, resource, user, member);

    assertThat(returnedAttributes).containsExactlyInAnyOrder(a1, a2, a3);
  }

  @Test
  public void setRequiredAttributes_services_f_r_u_m() throws Exception {
    System.out.println(CLASS_NAME + "setRequiredAttributes_services_f_r_u_m");

    Service s1 = new Service(1, "s1");
    Service s2 = new Service(2, "s2");
    List<Service> services = Arrays.asList(s1, s2);
    Facility facility = new Facility(1, "f");
    Resource resource = new Resource(2, "r", "", facility.getId());
    User user = new User(3, "a", "b", "", "", "");
    Member member = new Member(4, user.getId());

    List<Attribute> attributes = new ArrayList<>();

    doCallRealMethod()
        .when(attrManagerBlImplMock)
        .setRequiredAttributes(any(), anyList(), any(), any(), any(), any(), anyBoolean());

    when(attrManagerBlImplMock.getRequiredAttributes(sessionMock, services, facility, resource, user, member))
        .thenReturn(attributes);

    attrManagerBlImplMock.setRequiredAttributes(sessionMock, services, facility, resource, user, member, true);

    verify(attrManagerBlImplMock, times(1))
        .setRequiredAttributes(eq(sessionMock), eq(facility), eq(resource), eq(user), eq(member), eq(attributes),
            eq(true));
  }

  @Test
  public void getRequiredAttributes_services_resource_group() throws Exception {
    System.out.println(CLASS_NAME + "getRequiredAttributes_services_resource_group");

    List<Service> services = new ArrayList<>();
    Resource resource = new Resource();
    Group group = new Group();

    Attribute groupAttribute = new Attribute();
    Attribute groupResourceAttribute = new Attribute();

    when(attrManagerImplMock.getRequiredAttributes(sessionMock, services, resource, group))
        .thenReturn(new ArrayList<>(Collections.singletonList(groupResourceAttribute)));
    when(attrManagerImplMock.getRequiredAttributes(sessionMock, services, group))
        .thenReturn(new ArrayList<>(Collections.singletonList(groupAttribute)));

    List<Attribute> attributes =
        attrManagerBlImpl.getRequiredAttributes(sessionMock, services, resource, group, true);

    assertThat(attributes).containsOnly(groupAttribute, groupResourceAttribute);
  }

  @Test
  public void getRequiredAttributes_services_resource_group_OnlyGroupResourceAttributes() throws Exception {
    System.out.println(CLASS_NAME + "getRequiredAttributes_services_resource_group_OnlyGroupResourceAttributes");

    List<Service> services = new ArrayList<>();
    Resource resource = new Resource();
    Group group = new Group();

    Attribute groupAttribute = new Attribute();
    Attribute groupResourceAttribute = new Attribute();

    when(attrManagerImplMock.getRequiredAttributes(sessionMock, services, resource, group))
        .thenReturn(Collections.singletonList(groupResourceAttribute));
    when(attrManagerImplMock.getRequiredAttributes(sessionMock, services, group))
        .thenReturn(Collections.singletonList(groupAttribute));

    List<Attribute> attributes =
        attrManagerBlImpl.getRequiredAttributes(sessionMock, services, resource, group, false);

    assertThat(attributes).containsOnly(groupResourceAttribute);
  }

  @Test
  public void initializeModuleDependenciesSetsCorrectDependenciesMap() throws Exception {
    System.out.println(CLASS_NAME + "initializeModuleDependenciesSetsCorrectDependenciesMap");

    Method initializeModuleDependenciesMethod =
        getPrivateMethodFromAtrManager("initializeModuleDependencies", PerunSession.class, Set.class);

    AttributeDefinition A = setUpAttributeDefinition("A");
    AttributeDefinition B = setUpAttributeDefinition("B");

    // Set dependencies:
    //    A -> B
    setUpModuleMock(A, Collections.singletonList(B.getName()));
    setUpModuleMock(B, new ArrayList<>());

    Set<AttributeDefinition> allDefinitions = new HashSet<>();
    allDefinitions.add(A);
    allDefinitions.add(B);

    initializeModuleDependenciesMethod.invoke(attrManagerBlImpl, sessionMock, allDefinitions);
    Map<AttributeDefinition, Set<AttributeDefinition>> dependencies = getDependencies();

    assertThat(dependencies.keySet()).containsOnly(A, B);
    assertThat(dependencies.get(A)).containsOnly(B);
    assertThat(dependencies.get(B)).isEmpty();
  }

  @Test
  public void initializeModuleDependenciesSetsCorrectStrongDependenciesMap() throws Exception {
    System.out.println(CLASS_NAME + "initializeModuleDependenciesSetsCorrectStrongDependenciesMap");

    Method initializeModuleDependenciesMethod =
        getPrivateMethodFromAtrManager("initializeModuleDependencies", PerunSession.class, Set.class);

    AttributeDefinition A = setUpAttributeDefinition("A");
    AttributeDefinition B = setUpAttributeDefinition("B");

    // Set dependencies:
    //    A => B
    setUpVirtualModuleMock(A, new ArrayList<>(), Collections.singletonList(B.getName()));
    setUpVirtualModuleMock(B, new ArrayList<>(), new ArrayList<>());

    Set<AttributeDefinition> allDefinitions = new HashSet<>();
    allDefinitions.add(A);
    allDefinitions.add(B);

    initializeModuleDependenciesMethod.invoke(attrManagerBlImpl, sessionMock, allDefinitions);
    Map<AttributeDefinition, Set<AttributeDefinition>> dependencies = getStrongDependencies();

    assertThat(dependencies.keySet()).containsOnly(A, B);
    assertThat(dependencies.get(A)).containsOnly(B);
    assertThat(dependencies.get(B)).isEmpty();
  }

  @Test
  public void initializeModuleDependenciesSetsCorrectInverseDependenciesMap() throws Exception {
    System.out.println(CLASS_NAME + "initializeModuleDependenciesSetsCorrectInverseDependencies");

    Method initializeModuleDependenciesMethod =
        getPrivateMethodFromAtrManager("initializeModuleDependencies", PerunSession.class, Set.class);

    AttributeDefinition A = setUpAttributeDefinition("A");
    AttributeDefinition B = setUpAttributeDefinition("B");

    // Set dependencies:
    //    A -> B
    setUpModuleMock(A, Collections.singletonList(B.getName()));
    setUpModuleMock(B, new ArrayList<>());

    Set<AttributeDefinition> allDefinitions = new HashSet<>();
    allDefinitions.add(A);
    allDefinitions.add(B);

    initializeModuleDependenciesMethod.invoke(attrManagerBlImpl, sessionMock, allDefinitions);
    Map<AttributeDefinition, Set<AttributeDefinition>> inverseDependencies = getInverseDependencies();

    assertThat(inverseDependencies.keySet()).containsOnly(A, B);
    assertThat(inverseDependencies.get(B)).contains(A);
    assertThat(inverseDependencies.get(A)).isEmpty();
  }

  @Test
  public void initializeModuleDependenciesSetsCorrectInverseStrongDependenciesMap() throws Exception {
    System.out.println(CLASS_NAME + "initializeModuleDependenciesSetsCorrectInverseStrongDependencies");

    Method initializeModuleDependenciesMethod =
        getPrivateMethodFromAtrManager("initializeModuleDependencies", PerunSession.class, Set.class);

    AttributeDefinition A = setUpAttributeDefinition("A");
    AttributeDefinition B = setUpAttributeDefinition("B");

    // Set dependencies:
    //    A => B
    setUpVirtualModuleMock(A, new ArrayList<>(), Collections.singletonList(B.getName()));
    setUpVirtualModuleMock(B, new ArrayList<>(), new ArrayList<>());

    Set<AttributeDefinition> allDefinitions = new HashSet<>();
    allDefinitions.add(A);
    allDefinitions.add(B);

    initializeModuleDependenciesMethod.invoke(attrManagerBlImpl, sessionMock, allDefinitions);
    Map<AttributeDefinition, Set<AttributeDefinition>> inverseStrongDependencies = getInverseStrongDependencies();

    assertThat(inverseStrongDependencies.keySet()).containsOnly(A, B);
    assertThat(inverseStrongDependencies.get(B)).containsOnly(A);
    assertThat(inverseStrongDependencies.get(A)).isEmpty();
  }

  @Test
  public void initializeModuleDependenciesSetsCorrectAllDependenciesMap() throws Exception {
    System.out.println(CLASS_NAME + "initializeModuleDependenciesSetsCorrectAllDependencies");

    Method initializeModuleDependenciesMethod =
        getPrivateMethodFromAtrManager("initializeModuleDependencies", PerunSession.class, Set.class);

    AttributeDefinition A = setUpAttributeDefinition("A");
    AttributeDefinition B = setUpAttributeDefinition("B");
    AttributeDefinition C = setUpAttributeDefinition("C");

    // Set dependencies:
    //    B -> A
    //    C => A
    setUpVirtualModuleMock(A, new ArrayList<>(), new ArrayList<>());
    setUpVirtualModuleMock(B, Collections.singletonList(A.getName()), new ArrayList<>());
    setUpVirtualModuleMock(C, new ArrayList<>(), Collections.singletonList(A.getName()));

    Set<AttributeDefinition> allDefinitions = new HashSet<>();
    allDefinitions.add(A);
    allDefinitions.add(B);
    allDefinitions.add(C);

    initializeModuleDependenciesMethod.invoke(attrManagerBlImpl, sessionMock, allDefinitions);
    Map<AttributeDefinition, Set<AttributeDefinition>> allDependencies = getAllDependencies();

    assertThat(allDependencies.keySet()).containsOnly(A, B, C);
    assertThat(allDependencies.get(A)).containsOnly(B, C);
    assertThat(allDependencies.get(B)).isEmpty();
    assertThat(allDependencies.get(C)).isEmpty();
  }

  @Test
  public void deleteAttributeRemovesDependenciesForDeletedAttributeWhenSucceeded() throws Exception {
    System.out.println(CLASS_NAME + "deleteAttributeRemovesDependenciesForDeletedAttributeWhenSucceeded");

    Method initializeModuleDependenciesMethod =
        getPrivateMethodFromAtrManager("initializeModuleDependencies", PerunSession.class, Set.class);

    // set up initial data for attribute
    AttributeDefinition A = setUpAttributeDefinition("A");
    setUpModuleMock(A, Collections.emptyList());
    Set<AttributeDefinition> allDefinitions = Collections.singleton(A);
    initializeModuleDependenciesMethod.invoke(attrManagerBlImpl, sessionMock, allDefinitions);

    // enable deletion of attribute with relation to service
    mockEnableAttributeDeletionWithRelationToService(A);

    // delete attribute
    attrManagerBlImpl.deleteAttribute(sessionMock, A);

    Map<AttributeDefinition, Set<AttributeDefinition>> dependencies = getDependencies();
    Map<AttributeDefinition, Set<AttributeDefinition>> strongDependencies = getStrongDependencies();
    Map<AttributeDefinition, Set<AttributeDefinition>> inverseDependencies = getInverseDependencies();
    Map<AttributeDefinition, Set<AttributeDefinition>> inverseStrongDependencies = getInverseStrongDependencies();
    Map<AttributeDefinition, Set<AttributeDefinition>> allDependencies = getAllDependencies();

    assertThat(dependencies.keySet()).doesNotContain(A);
    assertThat(strongDependencies.keySet()).doesNotContain(A);
    assertThat(inverseDependencies.keySet()).doesNotContain(A);
    assertThat(inverseStrongDependencies.keySet()).doesNotContain(A);
    assertThat(allDependencies.keySet()).doesNotContain(A);
  }

  @Test
  public void deleteAttributeRemovesDeletedAttributeFromDependenciesWhenSucceeded() throws Exception {
    System.out.println(CLASS_NAME + "deleteAttributeRemovesDeletedAttributeFromDependenciesWhenSucceeded");

    Method initializeModuleDependenciesMethod =
        getPrivateMethodFromAtrManager("initializeModuleDependencies", PerunSession.class, Set.class);

    AttributeDefinition A = setUpAttributeDefinition("A");
    AttributeDefinition B = setUpAttributeDefinition("B");

    // Set dependencies:
    //    A -> B
    setUpModuleMock(A, Collections.singletonList(B.getName()));
    setUpModuleMock(B, Collections.emptyList());
    Set<AttributeDefinition> allDefinitions = Sets.newHashSet(A, B);
    initializeModuleDependenciesMethod.invoke(attrManagerBlImpl, sessionMock, allDefinitions);

    // enable deletion of attribute with relation to service
    mockEnableAttributeDeletionWithRelationToService(B);

    attrManagerBlImpl.deleteAttribute(sessionMock, B);

    Map<AttributeDefinition, Set<AttributeDefinition>> dependencies = getDependencies();

    assertThat(dependencies.get(A)).doesNotContain(B);
  }

  @Test
  public void deleteAttributeRemovesDeletedAttributeFromStrongDependenciesWhenSucceeded() throws Exception {
    System.out.println(CLASS_NAME + "deleteAttributeRemovesDeletedAttributeFromStrongDependenciesWhenSucceeded");

    Method initializeModuleDependenciesMethod =
        getPrivateMethodFromAtrManager("initializeModuleDependencies", PerunSession.class, Set.class);

    AttributeDefinition A = setUpAttributeDefinition("A");
    AttributeDefinition B = setUpAttributeDefinition("B");

    // Set dependencies:
    //    A => B
    setUpVirtualModuleMock(A, Collections.emptyList(), Collections.singletonList(B.getName()));
    setUpVirtualModuleMock(B, Collections.emptyList(), Collections.emptyList());
    Set<AttributeDefinition> allDefinitions = Sets.newHashSet(A, B);
    initializeModuleDependenciesMethod.invoke(attrManagerBlImpl, sessionMock, allDefinitions);

    // enable deletion of attribute with relation to service
    mockEnableAttributeDeletionWithRelationToService(B);

    attrManagerBlImpl.deleteAttribute(sessionMock, B);

    Map<AttributeDefinition, Set<AttributeDefinition>> strongDependencies = getStrongDependencies();

    assertThat(strongDependencies.get(A)).doesNotContain(B);
  }

  @Test
  public void deleteAttributeDoesNotRemoveDependenciesForDeletedAttributeWhenFails() throws Exception {
    System.out.println(CLASS_NAME + "deleteAttributeDoesNotRemoveDependenciesForDeletedAttributeWhenFails");

    Method initializeModuleDependenciesMethod =
        getPrivateMethodFromAtrManager("initializeModuleDependencies", PerunSession.class, Set.class);

    // set up DB failure
    doThrow(new RuntimeException())
        .when(attrManagerImplMock)
        .deleteAttribute(any(), any());

    // set up initial data for attribute
    AttributeDefinition A = setUpAttributeDefinition("A");
    setUpModuleMock(A, Collections.emptyList());
    Set<AttributeDefinition> allDefinitions = Collections.singleton(A);
    initializeModuleDependenciesMethod.invoke(attrManagerBlImpl, sessionMock, allDefinitions);

    // enable deletion of attribute with relation to service
    mockEnableAttributeDeletionWithRelationToService(A);

    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> attrManagerBlImpl.deleteAttribute(sessionMock, A));

    Map<AttributeDefinition, Set<AttributeDefinition>> dependencies = getDependencies();
    Map<AttributeDefinition, Set<AttributeDefinition>> strongDependencies = getStrongDependencies();
    Map<AttributeDefinition, Set<AttributeDefinition>> inverseDependencies = getInverseDependencies();
    Map<AttributeDefinition, Set<AttributeDefinition>> inverseStrongDependencies = getInverseStrongDependencies();
    Map<AttributeDefinition, Set<AttributeDefinition>> allDependencies = getAllDependencies();

    assertThat(dependencies.keySet()).contains(A);
    assertThat(strongDependencies.keySet()).contains(A);
    assertThat(inverseDependencies.keySet()).contains(A);
    assertThat(inverseStrongDependencies.keySet()).contains(A);
    assertThat(allDependencies.keySet()).contains(A);
  }

  @Test
  public void deleteAttributeDoesNotRemoveDeletedAttributeFromDependenciesWhenFails() throws Exception {
    System.out.println(CLASS_NAME + "deleteAttributeDoesNotRemoveDeletedAttributeFromDependenciesWhenFails");

    Method initializeModuleDependenciesMethod =
        getPrivateMethodFromAtrManager("initializeModuleDependencies", PerunSession.class, Set.class);

    // set up DB failure
    doThrow(new RuntimeException())
        .when(attrManagerImplMock)
        .deleteAttribute(any(), any());

    AttributeDefinition A = setUpAttributeDefinition("A");
    AttributeDefinition B = setUpAttributeDefinition("B");

    // Set dependencies:
    //    A -> B
    setUpModuleMock(A, Collections.singletonList(B.getName()));
    setUpModuleMock(B, Collections.emptyList());
    Set<AttributeDefinition> allDefinitions = Sets.newHashSet(A, B);
    initializeModuleDependenciesMethod.invoke(attrManagerBlImpl, sessionMock, allDefinitions);

    // enable deletion of attribute with relation to service
    mockEnableAttributeDeletionWithRelationToService(B);

    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> attrManagerBlImpl.deleteAttribute(sessionMock, B));

    Map<AttributeDefinition, Set<AttributeDefinition>> dependencies = getDependencies();

    assertThat(dependencies.get(A)).contains(B);
  }

  @Test
  public void deleteAttributeDoesNotRemoveDeletedAttributeFromStrongDependenciesWhenFails() throws Exception {
    System.out.println(CLASS_NAME + "deleteAttributeDoesNotRemoveDeletedAttributeFromStrongDependenciesWhenFails");

    Method initializeModuleDependenciesMethod =
        getPrivateMethodFromAtrManager("initializeModuleDependencies", PerunSession.class, Set.class);

    // set up DB failure
    doThrow(new RuntimeException())
        .when(attrManagerImplMock)
        .deleteAttribute(any(), any());

    AttributeDefinition A = setUpAttributeDefinition("A");
    AttributeDefinition B = setUpAttributeDefinition("B");

    // Set dependencies:
    //    A => B
    setUpVirtualModuleMock(A, Collections.emptyList(), Collections.singletonList(B.getName()));
    setUpVirtualModuleMock(B, Collections.emptyList(), Collections.emptyList());
    Set<AttributeDefinition> allDefinitions = Sets.newHashSet(A, B);
    initializeModuleDependenciesMethod.invoke(attrManagerBlImpl, sessionMock, allDefinitions);

    // enable deletion of attribute with relation to service
    mockEnableAttributeDeletionWithRelationToService(B);

    assertThatExceptionOfType(RuntimeException.class)
        .isThrownBy(() -> attrManagerBlImpl.deleteAttribute(sessionMock, B));

    Map<AttributeDefinition, Set<AttributeDefinition>> strongDependencies = getStrongDependencies();

    assertThat(strongDependencies.get(A)).contains(B);
  }

  @Test
  public void checkAttributeDependenciesDoesntSkipAttributesWithValueCheck() throws Exception {
    System.out.println(CLASS_NAME + "checkAttributeDependenciesDoesntSkipAttributesWithValueCheck");
    // set the mock to call the real tested method
    doCallRealMethod().when(attrManagerBlImplMock).checkAttributeDependencies(any(), any());

    UserAttributesModuleImplApi module = new urn_perun_user_attribute_def_virt_loa();

    RichAttribute<User, Void> dependencyAttribute =
        setUpDependencyBetweenTwoAttributesWhereTheDependantHasModule(module);

    attrManagerBlImplMock.checkAttributeDependencies(sessionMock, dependencyAttribute);
    verify(attrManagerBlImplMock, times(1)).checkAttributeSemantics(any(), (User) any(), any());
  }

  @Test
  public void checkAttributeDependenciesSkipAttributesWithoutValueCheck() throws Exception {
    System.out.println(CLASS_NAME + "checkAttributeDependenciesSkipAttributesWithoutValueCheck");
    // set the mock to call the real tested method
    doCallRealMethod().when(attrManagerBlImplMock).checkAttributeDependencies(any(), any());
    when(attrManagerImplMock.isVirtAttribute(any(), any())).thenReturn(true);

    UserAttributesModuleImplApi module = new urn_perun_user_attribute_def_virt_userCertDNs();

    RichAttribute<User, Void> dependencyAttribute =
        setUpDependencyBetweenTwoAttributesWhereTheDependantHasModule(module);

    attrManagerBlImplMock.checkAttributeDependencies(sessionMock, dependencyAttribute);
    verify(attrManagerBlImplMock, times(0)).checkAttributeSemantics(any(), (User) any(), any());
  }

  // ## ----------- PRIVATE METHODS ------------ ##

  /**
   * Sets up test environment for testing of (not)skipping value calculation during dependency check.
   * <p>
   * Sets dependency A -> B. The given {@code module} is used as a module of the attribute A.
   * The attribute B is returned, so it can be used to run tested methods.
   *
   * @param module module for A attribute
   * @return B attribute
   * @throws Exception any exception
   */
  private RichAttribute<User, Void> setUpDependencyBetweenTwoAttributesWhereTheDependantHasModule(
      AttributesModuleImplApi module
  ) throws Exception {
    RichAttribute<User, Void> A = setUpVirtualRichAttribute(new User(), null, "A");
    RichAttribute<User, Void> B = setUpVirtualRichAttribute(new User(), null, "B");

    // Set dependency(inverse):
    //    A -> B
    Map<AttributeDefinition, Set<AttributeDefinition>> dependencies = new HashMap<>();
    dependencies.put(
        new AttributeDefinition(B.getAttribute()),
        Collections.singleton(new AttributeDefinition(A.getAttribute()))
    );
    when(attrManagerBlImplMock.getAllDependencies()).thenReturn(dependencies);

    when(attrManagerImplMock.getAttributesModule(any(), eq(new AttributeDefinition(A.getAttribute()))))
        .thenReturn(module);
    when(attrManagerBlImplMock.getRichAttributesWithHoldersForAttributeDefinition(any(), any(), any()))
        .thenReturn(Collections.singletonList(A));
    when(attrManagerImplMock.isFromNamespace(any(), eq(NS_USER_ATTR)))
        .thenReturn(true);

    return B;
  }

  /**
   * Sets up a virtual RichAttribute with given name and given holders.
   *
   * @param holder1 primary holder
   * @param holder2 secondary holder
   * @param name    name
   * @param <T>     type of first holder
   * @param <V>     type of secondary holder
   * @return created rich attribute
   * @throws Exception any exception
   */
  private <T, V> RichAttribute<T, V> setUpVirtualRichAttribute(
      T holder1,
      V holder2,
      String name
  ) throws Exception {
    AttributeDefinition attributeDefinition = new AttributeDefinition();
    attributeDefinition.setNamespace("urn:perun:user:attribute-def:virt");
    attributeDefinition.setFriendlyName(name);
    Attribute attribute = new Attribute(attributeDefinition);
    return new RichAttribute<>(holder1, holder2, attribute);
  }

  @SuppressWarnings("unchecked")
  private Map<AttributeDefinition, Set<AttributeDefinition>> getDependencies() throws Exception {
    Field field = AttributesManagerBlImpl.class.getDeclaredField("dependencies");
    field.setAccessible(true);
    return (Map<AttributeDefinition, Set<AttributeDefinition>>) field.get(attrManagerBlImpl);
  }

  @SuppressWarnings("unchecked")
  private Map<AttributeDefinition, Set<AttributeDefinition>> getStrongDependencies() throws Exception {
    Field field = AttributesManagerBlImpl.class.getDeclaredField("strongDependencies");
    field.setAccessible(true);
    return (Map<AttributeDefinition, Set<AttributeDefinition>>) field.get(attrManagerBlImpl);
  }

  @SuppressWarnings("unchecked")
  private Map<AttributeDefinition, Set<AttributeDefinition>> getInverseDependencies() throws Exception {
    Field field = AttributesManagerBlImpl.class.getDeclaredField("inverseDependencies");
    field.setAccessible(true);
    return (Map<AttributeDefinition, Set<AttributeDefinition>>) field.get(attrManagerBlImpl);
  }

  @SuppressWarnings("unchecked")
  private Map<AttributeDefinition, Set<AttributeDefinition>> getInverseStrongDependencies() throws Exception {
    Field field = AttributesManagerBlImpl.class.getDeclaredField("inverseStrongDependencies");
    field.setAccessible(true);
    return (Map<AttributeDefinition, Set<AttributeDefinition>>) field.get(attrManagerBlImpl);
  }

  @SuppressWarnings("unchecked")
  private Map<AttributeDefinition, Set<AttributeDefinition>> getAllDependencies() throws Exception {
    Field field = AttributesManagerBlImpl.class.getDeclaredField("allDependencies");
    field.setAccessible(true);
    return (Map<AttributeDefinition, Set<AttributeDefinition>>) field.get(attrManagerBlImpl);
  }

  /**
   * Create attribute definition with given name and updates the mock of attributesManagerImpl.
   *
   * @param name name of the new attributeDefinition
   * @return newly created AttributeDefinition
   * @throws Exception any exception
   */
  private AttributeDefinition setUpAttributeDefinition(String name) throws Exception {
    AttributeDefinition ad = new AttributeDefinition();
    ad.setNamespace("ns");
    ad.setFriendlyName(name);
    ad.setId(idCounter++);
    when(attrManagerImplMock.getAttributeDefinition(any(), eq(ad.getName()))).thenReturn(ad);
    return ad;
  }

  /**
   * Set mock of an attribute module.
   *
   * @param definition   definition for which should be the mocked module set
   * @param dependencies dependencies which should be used for the mock
   * @throws Exception any exception
   */
  private void setUpModuleMock(AttributeDefinition definition,
                               List<String> dependencies
  ) throws Exception {
    AttributesModuleImplApi mockedModule = mock(AttributesModuleImplApi.class);
    when(mockedModule.getDependencies()).thenReturn(dependencies);
    when(attrManagerImplMock.getAttributesModule(any(), eq(definition))).thenReturn(mockedModule);
  }

  /**
   * Set mock of a virtual attribute module.
   *
   * @param definition         definition for which should be the mocked module set
   * @param dependencies       dependencies which should be used for the mock
   * @param strongDependencies strong dependencies which should be used for the mock
   * @throws Exception any exception
   */
  private void setUpVirtualModuleMock(AttributeDefinition definition,
                                      List<String> dependencies,
                                      List<String> strongDependencies
  ) throws Exception {
    VirtualAttributesModuleImplApi mockedModule = mock(VirtualAttributesModuleImplApi.class);
    when(mockedModule.getDependencies()).thenReturn(dependencies);
    when(mockedModule.getStrongDependencies()).thenReturn(strongDependencies);
    when(attrManagerImplMock.getAttributesModule(any(), eq(definition))).thenReturn(mockedModule);
  }

  /**
   * Get private method from attributesManagerBlImpl.
   *
   * @param methodName name of the requested method
   * @param argClasses classes of arguments of the requested method
   * @return reference to the method
   * @throws Exception any exception
   */
  private Method getPrivateMethodFromAtrManager(String methodName, Class<?>... argClasses) throws Exception {
    Method method = AttributesManagerBlImpl.class.getDeclaredMethod(methodName, argClasses);
    method.setAccessible(true);
    return method;
  }

  /**
   * Mock the getServicesByAttributeDefinition method for the given attribute to return an empty list
   * This way we want to enable mocked deletion also for attribute which is required for any service
   *
   * @param attr attribute definition which should be deleted
   */
  private void mockEnableAttributeDeletionWithRelationToService(AttributeDefinition attr) {
    PerunBl perunBl = attrManagerBlImpl.getPerunBl();
    when(perunBl.getServicesManagerBl()).thenReturn(serviceManagerBlImplMock);
    when(serviceManagerBlImplMock.getServicesByAttributeDefinition(eq(sessionMock), eq(attr)))
        .thenReturn(Collections.emptyList());
  }
}
