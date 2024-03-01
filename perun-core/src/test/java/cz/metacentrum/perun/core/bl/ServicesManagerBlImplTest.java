package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.OwnerType;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServicesManager;
import cz.metacentrum.perun.core.api.ServicesPackage;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.AttributeDefinitionExistsException;
import cz.metacentrum.perun.core.api.exceptions.DestinationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyBannedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.taskslib.model.Task;
import cz.metacentrum.perun.taskslib.model.Task.TaskStatus;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Michal Karm Babacek
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextHierarchy({
    @ContextConfiguration(locations = {"classpath:perun-base.xml", "classpath:perun-core.xml"})
})
@Transactional(transactionManager = "springTransactionManager")
public class ServicesManagerBlImplTest {
  //TODO content of this class should be moved to ServicesManagerEntryIntegrationTest

  @Autowired
  private ServicesManager servicesManager;
  @Autowired
  private DataSource dataSource;
  @Autowired
  private Perun perun;
  private JdbcPerunTemplate jdbcTemplate;
  private PerunSession perunSession;
  private Owner testOwner;
  private Service testService1;
  private Service testService2;
  private int testDestinationId1;
  private int testDestinationId2;
  private int testFacilityId1;
  private int testFacilityId2;
  private Facility facility1;
  private Facility facility2;
  private AttributeDefinition attribute;
  private Resource resource;
  private Vo vo;
  private Destination destination;
  private ServicesPackage servicesPackage;
  private Task task;


  /*
   * Tables with reference to service:
   *   - service_required_attrs
   *   - service_denials
   *   - resource_services
   *   - facility_service_destinations
   *   - service_service_packages
   *   - tasks
   *   - authz
   *
   */
  @Before
  public void setUp() throws Exception {
    perunSession = perun.getPerunSession(
        new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
            ExtSourcesManager.EXTSOURCE_INTERNAL),
        new PerunClient());

    jdbcTemplate = new JdbcPerunTemplate(dataSource);

    facility1 = new Facility();
    facility2 = new Facility();

    // Test Owner
    int newOwnerId = Utils.getNewId(jdbcTemplate, "owners_id_seq");
    testOwner = new Owner();
    testOwner.setContact("Call me babe");
    testOwner.setType(OwnerType.technical);
    testOwner.setName("Tester-" + Long.toHexString(System.currentTimeMillis()));
    testOwner.setId(newOwnerId);
    jdbcTemplate.update("insert into owners(id, name, contact, type) values (?,?,?,?)", newOwnerId, testOwner.getName(),
        testOwner.getContact(), testOwner.getType().toString());

    // Test Service #1
    testService1 = new Service();
    testService1.setName("Test_service_1_" + Long.toHexString(System.currentTimeMillis()));
    testService1.setDelay(1);
    testService1.setRecurrence(1);
    testService1.setEnabled(true);
    testService1.setScript("/hellish/test/script");

    // Test Service #2
    testService2 = new Service();
    testService2.setName("Test_service_2_" + Long.toHexString(System.currentTimeMillis()));
    testService2.setDelay(2);
    testService2.setRecurrence(2);
    testService2.setEnabled(true);
    testService2.setScript("/hellish/test/script2");

    testService1.setId(servicesManager.createService(perunSession, testService1).getId());
    testService2.setId(servicesManager.createService(perunSession, testService2).getId());

    // attribute
    attribute = new AttributeDefinition();
    attribute.setFriendlyName("ServicesManagerTestAttribute");
    attribute.setDescription("TestingAttribute");
    attribute.setNamespace(AttributesManager.NS_ENTITYLESS_ATTR_DEF);
    attribute.setType(String.class.getName());
    attribute = ((PerunBl) perun).getAttributesManagerBl().createAttribute(perunSession, attribute);

    // required attributes
    List<AttributeDefinition> attrlist = new ArrayList<>();
    attrlist.add(attribute);
    ((PerunBl) perun).getServicesManagerBl().addRequiredAttributes(perunSession, testService1, attrlist);

    //
    // Testing Destination #1
    testDestinationId1 = Utils.getNewId(jdbcTemplate, "destinations_id_seq");
    jdbcTemplate.update("insert into destinations(id, destination, type) values (?,?,'host')", testDestinationId1,
        "test.destination." + testDestinationId1);
    // Testing Destination #2
    testDestinationId2 = Utils.getNewId(jdbcTemplate, "destinations_id_seq");
    jdbcTemplate.update("insert into destinations(id, destination, type) values (?,?,'host')", testDestinationId2,
        "test.destination." + testDestinationId2);
    // Testing Facility #1
    testFacilityId1 = Utils.getNewId(jdbcTemplate, "facilities_id_seq");
    jdbcTemplate.update("insert into facilities(id, name) values (?,?)", testFacilityId1, "Cluster_" + testFacilityId1);
    facility1.setId(testFacilityId1);
    // Testing Facility #2
    testFacilityId2 = Utils.getNewId(jdbcTemplate, "facilities_id_seq");
    jdbcTemplate.update("insert into facilities(id, name) values (?,?)", testFacilityId2, "Cluster_" + testFacilityId2);
    facility2.setId(testFacilityId2);

    // vo
    vo = new Vo(0, "ServicesManagerTestVo", "RMTestVo");
    vo = ((PerunBl) perun).getVosManagerBl().createVo(perunSession, vo);

    // resource
    resource = new Resource();
    resource.setName("ServicesManagerTestResource");
    resource.setDescription("Testovaci");
    resource = ((PerunBl) perun).getResourcesManagerBl().createResource(perunSession, resource, vo, facility1);

    // resource services
    ((PerunBl) perun).getResourcesManagerBl().assignService(perunSession, resource, testService1);

    // facility_service_destinations
    destination = ((PerunBl) perun).getServicesManagerBl().getDestinationById(perunSession, testDestinationId1);
    ((PerunBl) perun).getServicesManagerBl().addDestination(perunSession, testService1, facility1, destination);

    // service package
    servicesPackage = new ServicesPackage();
    servicesPackage.setName("ResourcesManagertTestSP");
    servicesPackage.setDescription("testingServicePackage");
    servicesPackage = ((PerunBl) perun).getServicesManagerBl().createServicesPackage(perunSession, servicesPackage);

    // service_service_packages
    ((PerunBl) perun).getServicesManagerBl().addServiceToServicesPackage(perunSession, servicesPackage, testService1);

    // tasks
    task = new Task();
    task.setFacility(facility1);
    task.setService(testService1);
    task.setSchedule(0L);
    task.setStatus(TaskStatus.DONE);
    List<Destination> destinationsList = new ArrayList<>();
    destinationsList.add(destination);
    task.setDestinations(destinationsList);
    ((PerunBl) perun).getTasksManagerBl().insertTask(perunSession, task);

    // authz
    // authz entries for service are removed in ServicesManagerImpl::deleteService(),
    // no point in testing here
  }

  @Test
  public void testDeleteService() throws Exception {
    System.out.println("ServicesManagerBlImplTest.testDeleteService");

    // service denials (set up here, otherwise it breaks other test)
    ((PerunBl) perun).getServicesManagerBl().blockServiceOnDestination(perunSession, testService1, testDestinationId1);

    ((PerunBl) perun).getServicesManagerBl().deleteService(perunSession, testService1, true);
  }

  @Test
  public void testIsServiceDeniedOnFacility() {
    System.out.println("ServiceDenialDaoTest.isServiceBlockedOnFacility");

    assertFalse(((PerunBl) perun).getServicesManagerBl().isServiceBlockedOnFacility(testService1, facility1));

  }

  @Test
  public void testIsServiceDeniedOnDestination() {
    System.out.println("ServiceDenialDaoTest.isServiceBlockedOnDestination");

    assertFalse(
        ((PerunBl) perun).getServicesManagerBl().isServiceBlockedOnDestination(testService1, testDestinationId1));

  }

  @Test
  public void testBanServiceOnFacility() throws Exception {
    System.out.println("ServiceDenialDaoTest.blockServiceOnFacility");

    ((PerunBl) perun).getServicesManagerBl().blockServiceOnFacility(perunSession, testService1, facility1);
    assertTrue(((PerunBl) perun).getServicesManagerBl().isServiceBlockedOnFacility(testService1, facility1));

  }

  @Test
  public void testBanServiceOnDestination() throws Exception {
    System.out.println("ServiceDenialDaoTest.blockServiceOnDestination");

    ((PerunBl) perun).getServicesManagerBl().blockServiceOnDestination(perunSession, testService1, testDestinationId1);
    assertTrue(
        ((PerunBl) perun).getServicesManagerBl().isServiceBlockedOnDestination(testService1, testDestinationId1));

  }

  @Test
  public void testListDenialsForFacility() throws Exception {
    System.out.println("ServiceDenialDaoTest.getServicesBlockedOnFacility");
    ((PerunBl) perun).getServicesManagerBl().blockServiceOnFacility(perunSession, testService1, facility1);
    ((PerunBl) perun).getServicesManagerBl().blockServiceOnFacility(perunSession, testService2, facility1);

    List<Service> deniedServices =
        ((PerunBl) perun).getServicesManagerBl().getServicesBlockedOnFacility(perunSession, facility1);
    assertNotNull(deniedServices);
    assertEquals(deniedServices.size(), 2);

    assertEquals(((PerunBl) perun).getServicesManagerBl().getServicesBlockedOnFacility(perunSession, facility2).size(),
        0);

  }

  @Test
  public void testListDenialsForDestination() throws Exception {
    System.out.println("ServiceDenialDaoTest.getServicesBlockedOnDestination");

    ((PerunBl) perun).getServicesManagerBl().blockServiceOnDestination(perunSession, testService1, testDestinationId1);
    ((PerunBl) perun).getServicesManagerBl().blockServiceOnDestination(perunSession, testService2, testDestinationId1);

    List<Service> deniedServices =
        ((PerunBl) perun).getServicesManagerBl().getServicesBlockedOnDestination(perunSession, testDestinationId1);
    assertNotNull(deniedServices);
    assertEquals(deniedServices.size(), 2);

    assertEquals(
        ((PerunBl) perun).getServicesManagerBl().getServicesBlockedOnDestination(perunSession, testDestinationId2)
            .size(), 0);

  }

  @Test
  public void testFreeAllDenialsOnFacility() throws Exception {
    System.out.println("ServiceDenialDaoTest.unblockAllServicesOnFacility");

    ((PerunBl) perun).getServicesManagerBl().blockServiceOnFacility(perunSession, testService1, facility1);
    ((PerunBl) perun).getServicesManagerBl().blockServiceOnFacility(perunSession, testService2, facility1);
    ((PerunBl) perun).getServicesManagerBl().unblockAllServicesOnFacility(perunSession, facility1);
    assertFalse(((PerunBl) perun).getServicesManagerBl().isServiceBlockedOnFacility(testService1, facility1));
    assertFalse(((PerunBl) perun).getServicesManagerBl().isServiceBlockedOnFacility(testService2, facility1));

  }

  @Test
  public void testFreeAllDenialsOnDestination() throws Exception {
    System.out.println("ServiceDenialDaoTest.unblockAllServicesOnDestination");

    ((PerunBl) perun).getServicesManagerBl().blockServiceOnDestination(perunSession, testService1, testDestinationId1);
    ((PerunBl) perun).getServicesManagerBl().blockServiceOnDestination(perunSession, testService2, testDestinationId1);

    ((PerunBl) perun).getServicesManagerBl().unblockAllServicesOnDestination(perunSession, testDestinationId1);
    assertFalse(
        ((PerunBl) perun).getServicesManagerBl().isServiceBlockedOnDestination(testService1, testDestinationId1));
    assertFalse(
        ((PerunBl) perun).getServicesManagerBl().isServiceBlockedOnDestination(testService2, testDestinationId1));

  }

  @Test
  public void testFreeDenialOfServiceOnFacility() throws Exception {
    System.out.println("ServiceDenialDaoTest.unblockServiceOnFacility");

    ((PerunBl) perun).getServicesManagerBl().blockServiceOnFacility(perunSession, testService1, facility1);
    ((PerunBl) perun).getServicesManagerBl().blockServiceOnFacility(perunSession, testService2, facility1);

    ((PerunBl) perun).getServicesManagerBl().unblockServiceOnFacility(perunSession, testService2, facility1);
    assertTrue(((PerunBl) perun).getServicesManagerBl().isServiceBlockedOnFacility(testService1, facility1));
    assertFalse(((PerunBl) perun).getServicesManagerBl().isServiceBlockedOnFacility(testService2, facility1));

  }

  @Test
  public void testFreeDenialOfServiceOnDestination() throws Exception {
    System.out.println("ServiceDenialDaoTest.unblockServiceOnDestination");

    ((PerunBl) perun).getServicesManagerBl().blockServiceOnDestination(perunSession, testService1, testDestinationId1);
    ((PerunBl) perun).getServicesManagerBl().blockServiceOnDestination(perunSession, testService2, testDestinationId1);

    ((PerunBl) perun).getServicesManagerBl()
        .unblockServiceOnDestination(perunSession, testService2, testDestinationId1);
    assertTrue(
        ((PerunBl) perun).getServicesManagerBl().isServiceBlockedOnDestination(testService1, testDestinationId1));
    assertFalse(
        ((PerunBl) perun).getServicesManagerBl().isServiceBlockedOnDestination(testService2, testDestinationId1));

  }

  public ServicesManager getServicesManager() {
    return servicesManager;
  }

  public void setServicesManager(ServicesManager servicesManager) {
    this.servicesManager = servicesManager;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public Perun getPerun() {
    return perun;
  }

  public void setPerun(Perun perun) {
    this.perun = perun;
  }

}
