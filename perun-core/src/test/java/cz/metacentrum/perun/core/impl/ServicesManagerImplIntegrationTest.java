package cz.metacentrum.perun.core.impl;

import static org.assertj.core.api.Assertions.assertThat;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.RichDestination;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.implApi.ServicesManagerImplApi;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class ServicesManagerImplIntegrationTest extends AbstractPerunIntegrationTest {

  private static final String CLASS_NAME = "ServicesManagerImpl.";
  private ServicesManagerImplApi servicesManagerImpl;

  private Facility facility;
  private Service service;
  private Destination destination;

  @Test
  public void foo() throws Exception {
    servicesManagerImpl.blockServiceOnDestination(sess, service.getId(), destination.getId());
    servicesManagerImpl.blockServiceOnFacility(sess, service.getId(), facility.getId());
  }

  @Test
  public void getAllRichDestinationsCorrectDestinationDenial() throws Exception {
    System.out.println(CLASS_NAME + "getAllRichDestinationsCorrectDestinationDenial");

    servicesManagerImpl.blockServiceOnDestination(sess, service.getId(), destination.getId());

    List<RichDestination> richDestinations = servicesManagerImpl.getAllRichDestinations(sess, facility);

    hasCorrectRichDestinationWithServiceDenial(richDestinations);
  }

  @Test
  public void getAllRichDestinationsCorrectFacilityDenial() throws Exception {
    System.out.println(CLASS_NAME + "getAllRichDestinationsCorrectFacilityDenial");

    servicesManagerImpl.blockServiceOnFacility(sess, service.getId(), facility.getId());

    List<RichDestination> richDestinations = servicesManagerImpl.getAllRichDestinations(sess, facility);

    hasCorrectRichDestinationWithoutServiceDenial(richDestinations);
  }

  @Test
  public void getAllRichDestinationsForServiceCorrectDestinationDenial() throws Exception {
    System.out.println(CLASS_NAME + "getAllRichDestinationsForServiceCorrectDestinationDenial");

    servicesManagerImpl.blockServiceOnDestination(sess, service.getId(), destination.getId());

    List<RichDestination> richDestinations = servicesManagerImpl.getAllRichDestinations(sess, service);

    hasCorrectRichDestinationWithServiceDenial(richDestinations);
  }

  @Test
  public void getRichDestinationsCorrectDestinationDenial() throws Exception {
    System.out.println(CLASS_NAME + "getRichDestinationsCorrectDestinationDenial");

    servicesManagerImpl.blockServiceOnDestination(sess, service.getId(), destination.getId());

    List<RichDestination> richDestinations = servicesManagerImpl.getRichDestinations(sess, facility, service);

    hasCorrectRichDestinationWithServiceDenial(richDestinations);
  }

  private void hasCorrectRichDestinationWithServiceDenial(List<RichDestination> richDestinations) {
    assertThat(richDestinations).hasSize(1);

    RichDestination richDestination = richDestinations.get(0);

    assertThat(richDestination.getId()).isEqualTo(destination.getId());
    assertThat(richDestination.getFacility()).isEqualTo(facility);
    assertThat(richDestination.getService()).isEqualTo(service);
    assertThat(richDestination.isBlocked()).isTrue();
  }

  private void hasCorrectRichDestinationWithoutServiceDenial(List<RichDestination> richDestinations) {

    assertThat(richDestinations).hasSize(1);

    RichDestination richDestination = richDestinations.get(0);

    assertThat(richDestination.getId()).isEqualTo(destination.getId());
    assertThat(richDestination.getFacility()).isEqualTo(facility);
    assertThat(richDestination.getService()).isEqualTo(service);
    assertThat(richDestination.isBlocked()).isFalse();
  }

  @Before
  public void setUp() throws Exception {
    servicesManagerImpl =
        (ServicesManagerImplApi) ReflectionTestUtils.getField(perun.getServicesManagerBl(), "servicesManagerImpl");

    setUpEntities();
  }

  private void setUpDestination() throws Exception {
    destination = new Destination();
    destination.setDestination("destination.host");
    destination.setType(Destination.DESTINATIONHOSTTYPE);
    destination = servicesManagerImpl.createDestination(sess, destination);
  }

  private void setUpEntities() throws Exception {
    setUpFacility();
    setUpService();
    setUpDestination();

    servicesManagerImpl.addDestination(sess, service, facility, destination);
  }

  private void setUpFacility() throws Exception {
    facility = new Facility();
    facility.setName("TestFacility");
    facility.setDescription("des");
    facility = perun.getFacilitiesManagerBl().createFacility(sess, facility);
  }

  private void setUpService() {
    service = new Service();
    service.setName("Test service");
    service = servicesManagerImpl.createService(sess, service);
  }
}
