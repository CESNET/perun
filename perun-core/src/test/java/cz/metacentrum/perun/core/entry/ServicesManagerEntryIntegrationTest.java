package cz.metacentrum.perun.core.entry;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.GenDataNode;
import cz.metacentrum.perun.core.api.GenResourceDataNode;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.HashedGenData;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MemberGroupStatus;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichDestination;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServicesPackage;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyBannedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAttributesCannotExtend;
import cz.metacentrum.perun.core.api.exceptions.ServiceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceIsNotBannedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServicesPackageExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServicesPackageNotExistsException;
import cz.metacentrum.perun.core.impl.AuthzRoles;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests of ServicesManager.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ServicesManagerEntryIntegrationTest extends AbstractPerunIntegrationTest {

	private final static String CLASS_NAME = "ServicesManager.";

	private final static String A_F_C_NAME = "urn:perun:facility:attribute-def:core:name";
	private final static String A_R_C_NAME = "urn:perun:resource:attribute-def:core:name";
	private final static String A_G_C_NAME = "urn:perun:group:attribute-def:core:name";
	private final static String A_M_C_ID = "urn:perun:member:attribute-def:core:id";
	private final static String A_V_C_ID = "urn:perun:vo:attribute-def:core:id";

	// these are in DB only after setUp"Type"() method and must be set up in right order.
	private Service service;
	private ServicesPackage servicesPackage;
	private Vo vo;
	private Resource resource;
	private Facility facility;
	private AttributeDefinition attribute;
	private Destination destination;
	private Member member;
	private Group group;

	// SetUp moved to every method to speed up.

	@Test
	public void createService() throws Exception {
		System.out.println(CLASS_NAME + "createService");

		Service service = new Service();
		service.setName("ServicesManagerTestService");
		service = perun.getServicesManager().createService(sess, service);
		assertNotNull("unable to create Service",service);

	}

	@Test (expected=InternalErrorException.class)
	public void createServiceWhenServiceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "createServiceWhenServiceNotExists");

		perun.getServicesManager().createService(sess, new Service());
		// shouldn't be able to create service in DB (InternalError) when it's not valid Service object

	}

	@Test (expected=ServiceExistsException.class)
	public void createServiceWhenServiceExists() throws Exception {
		System.out.println(CLASS_NAME + "createService");

		Service service = new Service();
		service.setName("ServicesManagerTestService");
		service = perun.getServicesManager().createService(sess, service);
		perun.getServicesManager().createService(sess, service);
		// shouldn't create same service twice

	}

	@Test (expected=ServiceNotExistsException.class)
	public void deleteService() throws Exception {
		System.out.println(CLASS_NAME + "deleteService");

		service = setUpService();
		assertNotNull("unable to create service before deletion",service);
		perun.getServicesManager().deleteService(sess, service, false);
		perun.getServicesManager().getServiceById(sess, service.getId());
		// shouldn't find deleted service

	}

	@Test (expected=ServiceNotExistsException.class)
	public void deleteServiceWhenServiceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "deleteServiceWhenServiceNotExists");

		perun.getServicesManager().deleteService(sess, new Service(), false);
		// shouldn't find service

	}


	@Test (expected=RelationExistsException.class)
	public void deleteServiceWhenRelationExists() throws Exception {
		System.out.println(CLASS_NAME + "deleteServiceWhenWhenRelationExists");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		service = setUpService();

		perun.getResourcesManager().assignService(sess, resource, service);

		perun.getServicesManager().deleteService(sess, service, false);
		// shouldn't deleted service assigned to resource

	}

	@Test
	public void deleteServices() throws Exception {
		System.out.println(CLASS_NAME + "deleteServices");

		service = setUpService();
		Service service2 = new Service();
		service2.setName("ServicesManagerTestService2");
		service2 = perun.getServicesManager().createService(sess, service2);
		assertNotNull("unable to create service2",service2);
		Service service3 = new Service();
		service3.setName("ServicesManagerTestService3");
		service3 = perun.getServicesManager().createService(sess, service3);
		assertNotNull("unable to create service3",service3);


		List<Service> services = Arrays.asList(service, service2, service3);
		int initialCountOfServices = perun.getServicesManager().getServices(sess).size();

		perun.getServicesManager().deleteServices(sess, services, false);

		Assertions.assertThatExceptionOfType(ServiceNotExistsException.class).isThrownBy(
			() -> perun.getServicesManager().getServiceById(sess, service.getId()));
		int service2Id = service2.getId();
		Assertions.assertThatExceptionOfType(ServiceNotExistsException.class).isThrownBy(
			() -> perun.getServicesManager().getServiceById(sess, service2Id));
		int service3Id = service3.getId();
		Assertions.assertThatExceptionOfType(ServiceNotExistsException.class).isThrownBy(
			() -> perun.getServicesManager().getServiceById(sess, service3Id));

		assertEquals("Initial count of services minus three deleted services should be presented", initialCountOfServices - 3, perun.getServicesManager().getServices(sess).size());
	}

	@Test
	public void updateService() throws Exception {
		System.out.println(CLASS_NAME + "updateService");

		service = setUpService();
		assertNotNull("unable to create service before update",service);

		service.setName("ServicesManagerTestUpdServ");
		perun.getServicesManager().updateService(sess, service);

		Service returnedService = perun.getServicesManager().getServiceById(sess, service.getId());
		assertEquals("service not updated",returnedService,service);

	}

	@Test (expected=ServiceNotExistsException.class)
	public void updateServiceWhenServiceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "updateServiceWhenServiceNotExists");

		perun.getServicesManager().updateService(sess, new Service());
		// shouldn't find Service

	}

	@Test
	public void getServiceById() throws Exception {
		System.out.println(CLASS_NAME + "getServiceById");

		service = setUpService();
		assertNotNull("unable to create service",service);

		Service returnedService = perun.getServicesManager().getServiceById(sess, service.getId());
		assertEquals("cannot get service by ID",returnedService,service);

	}

	@Test (expected=ServiceNotExistsException.class)
	public void getServiceByIdWhenServiceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getServiceByIdWhenServiceNotExists");

		perun.getServicesManager().getServiceById(sess, 0);
		// shouldn't find service with ID 0

	}

	@Test
	public void getServiceByName() throws Exception {
		System.out.println(CLASS_NAME + "getServiceByName");

		service = setUpService();
		assertNotNull("unable to create service",service);

		Service returnedService = perun.getServicesManager().getServiceByName(sess, service.getName());
		assertEquals("cannot get service by Name",returnedService,service);

	}

	@Test (expected=ServiceNotExistsException.class)
	public void getServiceByNameWhenServiceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getServiceByNameWhenServiceNotExists");

		perun.getServicesManager().getServiceByName(sess, "");
		// shouldn't find service with empty name

	}

	@Test
	public void getServices() throws Exception {
		System.out.println(CLASS_NAME + "getServices");

		service = setUpService();

		List<Service> services = perun.getServicesManager().getServices(sess);
		assertTrue("there should be at leas 1 service (we added)",services.size() >= 1);
		assertTrue("our service should be between all services",services.contains(service));

	}

	@Test
	public void getServicesByAttributeDefinition() throws Exception {
		System.out.println(CLASS_NAME + "getServicesByAttributeDefinition");

		service = setUpService();

		List<AttributeDefinition> attributes = setUpRequiredAttribute();
		perun.getServicesManager().addRequiredAttributes(sess, service, attributes);

		attribute = attributes.get(0);
		List<Service> services = perun.getServicesManager().getServicesByAttributeDefinition(sess, attribute);

		assertTrue("there should be at least 1 service (we added service with certain attribute)",services.size() >= 1);
		assertTrue("our service should be between gotten services", services.contains(service));
	}

	@Test
	public void getAssignedResources() throws Exception {
		System.out.println(CLASS_NAME + "getAssignedResources");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		service = setUpService();

		perun.getResourcesManager().assignService(sess, resource, service);

		List<Resource> resources = perun.getServicesManager().getAssignedResources(sess, service);
		assertTrue("there should be at leas 1 resource (we added)",resources.size() >= 1);
		assertTrue("our resource should be between all service resources",resources.contains(resource));

	}

	@Test (expected=ServiceNotExistsException.class)
	public void getAssignedResourcesWhenServiceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getAssignedResourcesWhenServiceNotExists");

		perun.getServicesManager().getAssignedResources(sess, new Service());
		// shouldn't find service

	}

	@Test
	public void getServicesPackages() throws Exception {
		System.out.println(CLASS_NAME + "getServicesPackages");

		service = setUpService();
		servicesPackage = setUpServicesPackage(service);

		List<ServicesPackage> packages = perun.getServicesManager().getServicesPackages(sess);
		assertTrue("there should be at least 1 services package we added",packages.size() >= 1);
		assertTrue("our package should be between others",packages.contains(servicesPackage));

	}

	@Test
	public void getServicesPackageById() throws Exception {
		System.out.println(CLASS_NAME + "getServicesPackageById");

		service = setUpService();
		servicesPackage = setUpServicesPackage(service);

		ServicesPackage returnedPackage = perun.getServicesManager().getServicesPackageById(sess, servicesPackage.getId());
		assertNotNull("unable to get services package by ID",returnedPackage);
		assertEquals("returned services package should be the same we added",returnedPackage,servicesPackage);

	}

	@Test (expected=ServicesPackageNotExistsException.class)
	public void getServicesPackageByIdWhenPackageNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getServicesPackageByIdWhenPackageNotExists");

		perun.getServicesManager().getServicesPackageById(sess, 0);
		// shouldn't find package

	}

	@Test
	public void getServicesPackageByName() throws Exception {
		System.out.println(CLASS_NAME + "getServicesPackageByName");

		service = setUpService();
		servicesPackage = setUpServicesPackage(service);

		ServicesPackage retServPack = perun.getServicesManager().getServicesPackageByName(sess, servicesPackage.getName());
		assertNotNull("unable to get services package by name",retServPack);
		assertEquals("returned services package is not same as stored",servicesPackage,retServPack);

	}

	@Test (expected=ServicesPackageNotExistsException.class)
	public void getServicesPackageByNameWhenServPackNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getServicesPackageByNameWhenServPackNotExists");

		perun.getServicesManager().getServicesPackageByName(sess, "notExists");
		// shouldn't find services package

	}

	@Test
	public void createServicesPackage() throws Exception {
		System.out.println(CLASS_NAME + "createServicesPackage");

		service = setUpService();
		servicesPackage = setUpServicesPackage(service);
		assertNotNull("unable to create services package",servicesPackage);

		ServicesPackage returnedPackage = perun.getServicesManager().getServicesPackageById(sess, servicesPackage.getId());
		assertEquals("returned services package should be the same we added",returnedPackage,servicesPackage);

	}

	@Test (expected=ServicesPackageExistsException.class)
	public void createServicesPackageWhenServicePackageExists() throws Exception {
		System.out.println(CLASS_NAME + "createServicesPackageWhenServicePackageExists");

		service = setUpService();
		servicesPackage = setUpServicesPackage(service);
		assertNotNull("unable to create services package",servicesPackage);
		ServicesPackage returnedPackage = perun.getServicesManager().getServicesPackageById(sess, servicesPackage.getId());
		perun.getServicesManager().createServicesPackage(sess, returnedPackage);
		// shouldn't add service package twice

	}

	@Test (expected=ServicesPackageNotExistsException.class)
	public void deleteServicesPackage() throws Exception {
		System.out.println(CLASS_NAME + "deleteServicesPackage");

		service = setUpService();
		assertNotNull("unable to create service in DB",service);
		servicesPackage = setUpServicesPackage(service);
		assertNotNull("unable to create services package before deletion",servicesPackage);

		perun.getServicesManager().removeServiceFromServicesPackage(sess, servicesPackage, service);
		// remove service from package so it can be deleted
		perun.getServicesManager().deleteServicesPackage(sess, servicesPackage);
		// finally delete package
		perun.getServicesManager().getServicesPackageById(sess, servicesPackage.getId());
		// shouldn't find services package in DB after deletion

	}

	@Test (expected=ServicesPackageNotExistsException.class)
	public void deleteServicesPackageWhenPackageNotExist() throws Exception {
		System.out.println(CLASS_NAME + "deleteServicesPackageWhenPackageNotExist");

		perun.getServicesManager().deleteServicesPackage(sess, new ServicesPackage());
		// shouldn't find services package in DB

	}

	@Test (expected=RelationExistsException.class)
	public void deleteServicesPackageWhenRealtionExists() throws Exception {
		System.out.println(CLASS_NAME + "deleteServicesPackage");

		service = setUpService();
		servicesPackage = setUpServicesPackage(service);

		perun.getServicesManager().deleteServicesPackage(sess, servicesPackage);
		// shouldn't delete package with service inside

	}

	@Test
	public void addServiceToServicesPackage() throws Exception {
		System.out.println(CLASS_NAME + "addServiceToServicesPackage");

		service = setUpService();
		assertNotNull("unable to create service in DB",service);

		ServicesPackage servicesPackage = new ServicesPackage();
		servicesPackage.setName("ServicesManagerTestSP");
		servicesPackage.setDescription("TestingPackage");
		perun.getServicesManager().createServicesPackage(sess, servicesPackage);

		perun.getServicesManager().addServiceToServicesPackage(sess, servicesPackage, service);

		List<Service> services = perun.getServicesManager().getServicesFromServicesPackage(sess, servicesPackage);
		assertTrue("there should be at leas 1 service in package",services.size() >= 1);
		assertTrue("our service should be between package services",services.contains(service));

	}

	@Test (expected=ServicesPackageNotExistsException.class)
	public void addServiceToServicesPackageWhenPackageNotExists() throws Exception {
		System.out.println(CLASS_NAME + "addServiceToServicesPackageWhenPackageNotExists");

		service = setUpService();
		assertNotNull("unable to create service in DB",service);

		perun.getServicesManager().addServiceToServicesPackage(sess, new ServicesPackage(), service);
		// shouldn't find services package

	}

	@Test (expected=ServiceNotExistsException.class)
	public void addServiceToServicesPackageWhenServiceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "addServiceToServicesPackageWhenServiceNotExists");

		ServicesPackage servicesPackage = new ServicesPackage();
		servicesPackage.setName("ServicesManagerTestSP");
		servicesPackage.setDescription("TestingPackage");
		perun.getServicesManager().createServicesPackage(sess, servicesPackage);

		perun.getServicesManager().addServiceToServicesPackage(sess, servicesPackage, new Service());
		// shouldn't find services package

	}

	@Test
	public void removeServiceFromServicesPackage() throws Exception {
		System.out.println(CLASS_NAME + "removeServiceFromServicesPackage");

		service = setUpService();
		assertNotNull("unable to create service in DB",service);
		servicesPackage = setUpServicesPackage(service);

		perun.getServicesManager().removeServiceFromServicesPackage(sess, servicesPackage, service);

		List<Service> services = perun.getServicesManager().getServicesFromServicesPackage(sess, servicesPackage);
		assertTrue("unable to remove service from services package",services.isEmpty());

	}

	@Test (expected=ServicesPackageNotExistsException.class)
	public void removeServiceFromServicesPackageWhenPackageNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeServiceFromServicesPackageWhenPackageNotExists");

		service = setUpService();
		assertNotNull("unable to create service in DB",service);

		perun.getServicesManager().removeServiceFromServicesPackage(sess, new ServicesPackage(), service);
		// shouldn't find services package

	}

	@Test (expected=ServiceNotExistsException.class)
	public void removeServiceFromServicesPackageWhenServiceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeServiceFromServicesPackageWhenServiceNotExists");

		service = setUpService();
		assertNotNull("unable to create service in DB",service);
		servicesPackage = setUpServicesPackage(service);

		perun.getServicesManager().removeServiceFromServicesPackage(sess, servicesPackage, new Service());
		// shouldn't find service

	}

	@Test
	public void addRequiredAttribute() throws Exception {
		System.out.println(CLASS_NAME + "addRequiredAttribute");

		service = setUpService();
		attribute = setUpAttribute();

		perun.getServicesManager().addRequiredAttribute(sess, service, attribute);

		assertEquals("req attribute was not added",perun.getAttributesManager().getRequiredAttributesDefinition(sess, service).get(0),attribute);

	}

	@Test (expected=AttributeNotExistsException.class)
	public void addRequiredAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "addRequiredAttributeWhenAttributeNotExists");

		service = setUpService();
		attribute = setUpAttribute();
		attribute.setId(0);

		perun.getServicesManager().addRequiredAttribute(sess, service, attribute);
		// shouldn't find attribute

	}

	@Test (expected=ServiceNotExistsException.class)
	public void addRequiredAttributeWhenServiceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "addRequiredAttributeWhenServiceNotExists");

		attribute = setUpAttribute();

		perun.getServicesManager().addRequiredAttribute(sess, new Service(), attribute);
		// shouldn't find service

	}

	@Test (expected=AttributeAlreadyAssignedException.class)
	public void addRequiredAttributeWhenAttributeAlreadyAssigned() throws Exception {
		System.out.println(CLASS_NAME + "addRequiredAttributeWhenAttributeAlreadyAssigned");

		service = setUpService();
		attribute = setUpAttribute();

		perun.getServicesManager().addRequiredAttribute(sess, service, attribute);
		perun.getServicesManager().addRequiredAttribute(sess, service, attribute);
		// shouldn't add same attribute twice

	}

	@Test (expected=ServiceAttributesCannotExtend.class)
	public void addRequiredUserAttributeOnEnabledService() throws Exception {
		System.out.println(CLASS_NAME + "addRequiredUserAttributeOnEnabledService");

		vo = setUpVo();
		service = setUpService();
		// creates consent hub with enforce flag == true
		facility = setUpFacility();
		resource = setUpResource();
		// add user-related attribute to service
		attribute = setUpUserAttribute();
		perun.getResourcesManagerBl().assignService(sess, resource, service);

		boolean forceConsents = BeansUtils.getCoreConfig().getForceConsents();

		try {
			// force consents for test but set original settings afterwards
			BeansUtils.getCoreConfig().setForceConsents(true);
			perun.getServicesManager().addRequiredAttribute(sess, service, attribute);
		} finally {
			BeansUtils.getCoreConfig().setForceConsents(forceConsents);
		}
	}

	@Test
	public void addRequiredAttributes() throws Exception {
		System.out.println(CLASS_NAME + "addRequiredAttributes");

		service = setUpService();
		List<AttributeDefinition> attributes = setUpRequiredAttribute();

		perun.getServicesManager().addRequiredAttributes(sess, service, attributes);
		assertTrue("service should have 1 req. attribute",perun.getAttributesManager().getRequiredAttributesDefinition(sess, service).size() == 1);
		assertEquals("returned req attribute is not same as stored",attributes,perun.getAttributesManager().getRequiredAttributesDefinition(sess, service));

	}

	@Test (expected=AttributeNotExistsException.class)
	public void addRequiredAttributesWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "addRequiredAttributesWhenAttributeNotExists");

		service = setUpService();
		List<AttributeDefinition> attributes = setUpRequiredAttribute();
		attributes.get(0).setId(0);

		perun.getServicesManager().addRequiredAttributes(sess, service, attributes);
		// shouldn't find attribute

	}

	@Test (expected=ServiceNotExistsException.class)
	public void addRequiredAttributesWhenServiceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "addRequiredAttributesWhenServiceNotExists");

		List<AttributeDefinition> attributes = setUpRequiredAttribute();

		perun.getServicesManager().addRequiredAttributes(sess, new Service(), attributes);
		// shouldn't find service

	}

	@Test (expected=AttributeAlreadyAssignedException.class)
	public void addRequiredAttributesWhenAttributeAlreadyAssigned() throws Exception {
		System.out.println(CLASS_NAME + "addRequiredAttributeWhenAttributeAlreadyAssigned");

		service = setUpService();
		List<AttributeDefinition> attributes = setUpRequiredAttribute();

		perun.getServicesManager().addRequiredAttributes(sess, service, attributes);
		perun.getServicesManager().addRequiredAttributes(sess, service, attributes);
		// shouldn't add same attribute twice

	}

	@Test
	public void removeRequiredAttribute() throws Exception {
		System.out.println(CLASS_NAME + "removeRequiredAttribute");

		service = setUpService();
		attribute = setUpAttribute();
		perun.getServicesManager().addRequiredAttribute(sess, service, attribute);

		perun.getServicesManager().removeRequiredAttribute(sess, service, attribute);
		assertTrue("req attribute was not deleted",perun.getAttributesManager().getRequiredAttributesDefinition(sess, service).isEmpty());

	}

	@Test (expected=AttributeNotExistsException.class)
	public void removeRequiredAttributeWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeRequiredAttributeWhenAttributeNotExists");

		service = setUpService();
		attribute = setUpAttribute();
		attribute.setId(0);

		perun.getServicesManager().removeRequiredAttribute(sess, service, attribute);
		// shouldn't find attribute

	}

	@Test (expected=ServiceNotExistsException.class)
	public void removeRequiredAttributeWhenServiceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeRequiredAttributeWhenServiceNotExists");

		attribute = setUpAttribute();

		perun.getServicesManager().removeRequiredAttribute(sess, new Service(), attribute);
		// shouldn't find service

	}

	@Test (expected=AttributeNotAssignedException.class)
	public void removeRequiredAttributeWhenAttributeNotAssigned() throws Exception {
		System.out.println(CLASS_NAME + "removeRequiredAttributeWhenAttributeNotAssigned");

		service = setUpService();
		attribute = setUpAttribute();

		perun.getServicesManager().removeRequiredAttribute(sess, service, attribute);
		// shouldn't remove not assigned attribute

	}

	@Test
	public void removeRequiredAttributes() throws Exception {
		System.out.println(CLASS_NAME + "removeRequiredAttributes");

		service = setUpService();
		List<AttributeDefinition> attributes = setUpRequiredAttribute();
		perun.getServicesManager().addRequiredAttribute(sess, service, attributes.get(0));
		// add 1 required attribute
		perun.getServicesManager().removeRequiredAttributes(sess, service, attributes);
		assertTrue("req attribute was not deleted",perun.getAttributesManager().getRequiredAttributesDefinition(sess, service).isEmpty());

	}

	@Test (expected=AttributeNotExistsException.class)
	public void removeRequiredAttributesWhenAttributeNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeRequiredAttributesWhenAttributeNotExists");

		service = setUpService();
		List<AttributeDefinition> attributes = setUpRequiredAttribute();
		attributes.get(0).setId(0);

		perun.getServicesManager().removeRequiredAttributes(sess, service, attributes);
		// shouldn't find attribute

	}

	@Test (expected=ServiceNotExistsException.class)
	public void removeRequiredAttributesWhenServiceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeRequiredAttributesWhenServiceNotExists");

		List<AttributeDefinition> attributes = setUpRequiredAttribute();

		perun.getServicesManager().removeRequiredAttributes(sess, new Service(), attributes);
		// shouldn't find service

	}

	@Test (expected=AttributeNotAssignedException.class)
	public void removeRequiredAttributesWhenAttributeNotAssigned() throws Exception {
		System.out.println(CLASS_NAME + "removeRequiredAttributesWhenAttributeNotAssigned");

		service = setUpService();
		List<AttributeDefinition> attributes = setUpRequiredAttribute();

		perun.getServicesManager().removeRequiredAttributes(sess, service, attributes);
		// shouldn't remove not assigned attribute

	}

	@Test
	public void removeAllRequiredAttributes() throws Exception {
		System.out.println(CLASS_NAME + "removeAllRequiredAttributes");

		service = setUpService();
		attribute = setUpAttribute();
		perun.getServicesManager().addRequiredAttribute(sess, service, attribute);

		perun.getServicesManager().removeAllRequiredAttributes(sess, service);
		assertTrue("req attribute was not deleted",perun.getAttributesManager().getRequiredAttributesDefinition(sess, service).isEmpty());

	}

	@Test (expected=ServiceNotExistsException.class)
	public void removeAllRequiredAttributesWhenServiceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeAllRequiredAttributesWhenServiceNotExists");

		perun.getServicesManager().removeAllRequiredAttributes(sess, new Service());
		// shouldn't find service

	}

	@Test
	public void addDestination() throws Exception {
		System.out.println(CLASS_NAME + "addDestination");

		service = setUpService();
		facility = setUpFacility();
		destination = setUpDestination();

		perun.getServicesManager().addDestination(sess, service, facility, destination);

		List<Destination> destinations = perun.getServicesManager().getDestinations(sess, service, facility);
		assertTrue("service should have 1 destination",destinations.size() == 1);
		assertTrue("our destination should be assigned to service",destinations.contains(destination));

	}

	@Test
	public void getDestinationsCount() throws Exception {
		System.out.println(CLASS_NAME + "getDestinationsCount");

		service = setUpService();
		facility = setUpFacility();
		destination = setUpDestination();
		perun.getServicesManager().addDestination(sess, service, facility, destination);

		int count = perun.getServicesManager().getDestinationsCount(sess);
		assertTrue(count>0);
	}

	@Test
	public void addDestinationForMoreThanOneService() throws Exception {
		System.out.println(CLASS_NAME + "addDestinationForMoreThanOneService");

		List<Service> services = setUpServices();
		facility = setUpFacility();
		destination = setUpDestination();
		perun.getServicesManager().addDestination(sess, services, facility, destination);

		List<RichDestination> destinations = perun.getServicesManager().getRichDestinations(sess, facility, services.get(0));
		destinations.addAll(perun.getServicesManager().getRichDestinations(sess, facility, services.get(1)));
		assertTrue("service should have 1 destination",destinations.size() == 2);

		for(RichDestination rd: destinations) {
			assertTrue("destination in richDestination need to be our destination", rd.getDestination().equals(destination.getDestination()));
			assertTrue("type of destination need to be our type of destination", rd.getType().equals(destination.getType()));
			assertTrue("richDestination has service from our list of services", services.contains(rd.getService()));
		}
	}

	@Test (expected=ServiceNotExistsException.class)
	public void addDestinationWhenServiceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "addDestinationWhenServiceNotExists");

		facility = setUpFacility();
		destination = setUpDestination();

		perun.getServicesManager().addDestination(sess, new Service(), facility, destination);
		// shouldn't find service

	}

	@Test (expected=FacilityNotExistsException.class)
	public void addDestinationWhenFacilityNotExists() throws Exception {
		System.out.println(CLASS_NAME + "addDestinationWhenFacilityNotExists");

		service = setUpService();
		destination = setUpDestination();

		perun.getServicesManager().addDestination(sess, service, new Facility(), destination);
		// shouldn't find facility

	}

	@Test (expected=DestinationAlreadyAssignedException.class)
	public void addDestinationWhenDestinationAlreadyAssigned() throws Exception {
		System.out.println(CLASS_NAME + "addDestinationWhenDestinationAlreadyAssigned");

		service = setUpService();
		facility = setUpFacility();
		destination = setUpDestination();

		perun.getServicesManager().addDestination(sess, service, facility, destination);
		perun.getServicesManager().addDestination(sess, service, facility, destination);
		// shouldn't add same destination twice

	}

	@Test
	public void addDestinationsDefinedByHostsOnFacility() throws Exception {
		System.out.println(CLASS_NAME + "addDestinationsDefinedByHostsOnFacility");

		service = setUpService();
		facility = setUpNonClusterFacilityWithTwoHosts();

		List<Destination> newDestinations = perun.getServicesManager().addDestinationsDefinedByHostsOnFacility(sess, service, facility);

		assertTrue("addDestinationsDefinedByHostsOnFacility should create 2 destination",newDestinations.size() == 2);

		List<Destination> destinations = perun.getServicesManager().getDestinations(sess, service, facility);
		assertTrue("service should have 2 destinations",destinations.size() == 2);
	}

	@Test
	public void addDestinationsDefinedByHostsOnFacilityWithListOfServices() throws Exception {
		System.out.println(CLASS_NAME + "addDestinationsDefinedByHostsOnFacilityWithListOfServices");

		List<Service> services = setUpServices();
		facility = setUpNonClusterFacilityWithTwoHosts();

		List<Destination> newDestinations = perun.getServicesManager().addDestinationsDefinedByHostsOnFacility(sess, services, facility);

		assertTrue("addDestinationsDefinedByHostsOnFacility should create 4 destination",newDestinations.size() == 4);

		List<RichDestination> destinations = perun.getServicesManager().getRichDestinations(sess, facility, services.get(0));
		destinations.addAll(perun.getServicesManager().getRichDestinations(sess, facility, services.get(1)));

		assertTrue("service should have 4 destinations",destinations.size() == 4);
	}

	@Test
	public void addDestinationsDefinedByHostsOnFacilityForAssignedListOfServices() throws Exception {
		System.out.println(CLASS_NAME + "addDestinationsDefinedByHostsOnFacilityForAssignedListOfServices");

		List<Service> services = setUpServices();

		vo = setUpVo();
		facility = setUpNonClusterFacilityWithTwoHosts();
		resource = setUpResource();
		assignServicesOnResource(resource, services);

		assertTrue("There are 2 assigned services on resource.", perun.getServicesManagerBl().getAssignedServices(sess, facility).size() == 2);

		List<Destination> newDestinations = perun.getServicesManager().addDestinationsDefinedByHostsOnFacility(sess, facility);

		assertTrue("addDestinationsDefinedByHostsOnFacility should create 4 destination",newDestinations.size() == 4);

		List<RichDestination> destinations = perun.getServicesManager().getRichDestinations(sess, facility, services.get(0));
		destinations.addAll(perun.getServicesManager().getRichDestinations(sess, facility, services.get(1)));

		assertTrue("service should have 4 destinations",destinations.size() == 4);
	}

	@Test
	public void addDestinationsForAllServicesOnFacility() throws Exception {
		System.out.println(CLASS_NAME + "addDestinationsForAllServicesOnFacility");

		service = setUpService();
		facility = setUpFacility();
		destination = setUpDestination();
		vo = setUpVo();
		resource = setUpResource();

		// Assign service to the resource
		perun.getResourcesManager().assignService(sess, resource, service);

		List<Destination> newDestinations = perun.getServicesManager().addDestinationsForAllServicesOnFacility(sess, facility, destination);

		assertTrue("addDestinationsForAllServicesOnFacility should create 1 destination",newDestinations.size() == 1);

		List<Destination> destinations = perun.getServicesManager().getDestinations(sess, service, facility);
		assertTrue("service should have 1 destination",destinations.size() == 1);
		assertTrue("our destination should be assigned to service",destinations.contains(destination));

	}

	@Test
	public void removeDestination() throws Exception {
		System.out.println(CLASS_NAME + "removeDestination");

		service = setUpService();
		facility = setUpFacility();
		destination = setUpDestination();

		perun.getServicesManager().addDestination(sess, service, facility, destination);

		perun.getServicesManager().removeDestination(sess, service, facility, destination);

		List<Destination> destinations = perun.getServicesManager().getDestinations(sess, service, facility);
		assertTrue("there shoudln't be any detinations",destinations.isEmpty());

	}

	@Test
	public void removeDestinationDeletesDestination() throws Exception {
		System.out.println(CLASS_NAME + "removeDestinationDeletesDestination");

		List<Service> services = setUpServices();
		facility = setUpFacility();
		destination = setUpDestination();

		destination = perun.getServicesManager().addDestination(sess, services.get(0), facility, destination);
		perun.getServicesManager().addDestination(sess, services.get(1), facility, destination);

		// service denials should be deleted too
		perun.getServicesManager().blockServiceOnDestination(sess, services.get(0), destination.getId());

		perun.getServicesManager().removeDestination(sess, services.get(0), facility, destination);
		List<Destination> destinations = perun.getServicesManager().getDestinations(sess, services.get(0), facility);
		assertTrue("there shouldn't be any destinations", destinations.isEmpty());
		// shouldn't throw exception - destination should still exist
		perun.getServicesManager().getDestinationById(sess, destination.getId());

		perun.getServicesManager().removeDestination(sess, services.get(1), facility, destination);
		destinations = perun.getServicesManager().getDestinations(sess, services.get(1), facility);
		assertTrue("there shouldn't be any destinations", destinations.isEmpty());
		// destination should be deleted because it is no longer used
		assertThatExceptionOfType(DestinationNotExistsException.class)
			.isThrownBy(() -> perun.getServicesManager().getDestinationById(sess, destination.getId()));
	}

	@Test
	public void removeAllDestinationsWithFacility() throws Exception {
		System.out.println(CLASS_NAME + "removeAllDestinationsWithFacility");

		List<Service> services = setUpServices();
		facility = setUpFacility();
		destination = setUpDestination();

		Destination dest1 = perun.getServicesManager().addDestination(sess, services.get(0), facility, destination);
		Destination dest2 = perun.getServicesManager().addDestination(sess, services.get(1), facility, destination);

		List<Destination> destinations = perun.getServicesManagerBl().getDestinations(sess, facility);
		assertTrue("There need to be dest1", destinations.contains(dest1));
		assertTrue("There need to be dest2", destinations.contains(dest2));

		perun.getServicesManagerBl().removeAllDestinations(sess, facility);
		destinations = perun.getServicesManagerBl().getDestinations(sess, facility);
		assertTrue("All destinations should be removed", destinations.isEmpty());
	}

	@Test
	public void removeAllDestinationsWithFacilityDeletesDestination() throws Exception {
		System.out.println(CLASS_NAME + "removeAllDestinationsWithFacilityDeletesDestination");

		List<Service> services = setUpServices();
		facility = setUpFacility();
		destination = setUpDestination();

		Destination dest1 = perun.getServicesManager().addDestination(sess, services.get(0), facility, destination);
		Destination dest2 = perun.getServicesManager().addDestination(sess, services.get(1), facility, destination);

		List<Destination> destinations = perun.getServicesManagerBl().getDestinations(sess, facility);
		assertTrue("There need to be dest1", destinations.contains(dest1));
		assertTrue("There need to be dest2", destinations.contains(dest2));

		// service denials should be deleted too
		perun.getServicesManager().blockServiceOnDestination(sess, services.get(0), dest1.getId());

		perun.getServicesManagerBl().removeAllDestinations(sess, facility);
		destinations = perun.getServicesManagerBl().getDestinations(sess, facility);
		assertTrue("All destinations should be removed", destinations.isEmpty());

		// destination should be deleted because it is no longer used
		assertThatExceptionOfType(DestinationNotExistsException.class)
			.isThrownBy(() -> perun.getServicesManager().getDestinationById(sess, dest1.getId()));
	}

	@Test (expected=ServiceNotExistsException.class)
	public void removeDestinationWhenServiceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeDestinationWhenServiceNotExists");

		facility = setUpFacility();
		destination = setUpDestination();

		perun.getServicesManager().removeDestination(sess, new Service(), facility, destination);
		// shouldn't find service

	}

	@Test (expected=FacilityNotExistsException.class)
	public void removeDestinationWhenFacilityNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeDestinationWhenFacilityNotExists");

		service = setUpService();
		destination = setUpDestination();

		perun.getServicesManager().removeDestination(sess, service, new Facility(), destination);
		// shouldn't find facility

	}

	@Test (expected=DestinationAlreadyRemovedException.class)
	public void removeDestinationWhenDestinationAlreadyRemoved() throws Exception {
		System.out.println(CLASS_NAME + "removeDestinationWhenDestinationAlreadyRemoved");

		service = setUpService();
		facility = setUpFacility();
		destination = setUpDestination();

		perun.getServicesManager().removeDestination(sess, service, facility, destination);
		// shouldn't remove not added destination

	}

	@Test
	public void removeAllDestinations() throws Exception {
		System.out.println(CLASS_NAME + "removeAllDestinations");

		service = setUpService();
		facility = setUpFacility();
		destination = setUpDestination();

		perun.getServicesManager().addDestination(sess, service, facility, destination);

		perun.getServicesManager().removeAllDestinations(sess, service, facility);

		List<Destination> destinations = perun.getServicesManager().getDestinations(sess, service, facility);
		assertTrue("there shoudln't be any detinations",destinations.isEmpty());
	}

	@Test
	public void removeAllDestinationsDeletesDestination() throws Exception {
		System.out.println(CLASS_NAME + "removeAllDestinationsDeletesDestination");

		List<Service> services = setUpServices();
		facility = setUpFacility();
		destination = setUpDestination();

		destination = perun.getServicesManager().addDestination(sess, services.get(0), facility, destination);
		perun.getServicesManager().addDestination(sess, services.get(1), facility, destination);
		// service denials should be deleted too
		perun.getServicesManager().blockServiceOnDestination(sess, services.get(1), destination.getId());

		perun.getServicesManager().removeAllDestinations(sess, services.get(0), facility);
		// shouldn't throw exception - destination should still exist
		perun.getServicesManager().getDestinationById(sess, destination.getId());
		List<Destination> destinations = perun.getServicesManager().getDestinations(sess, services.get(0), facility);
		assertTrue("there shoudln't be any detinations",destinations.isEmpty());

		perun.getServicesManager().removeAllDestinations(sess, services.get(1), facility);
		destinations = perun.getServicesManager().getDestinations(sess, services.get(1), facility);
		assertTrue("there shouldn't be any destinations", destinations.isEmpty());
		// destination should be deleted because it is no longer used
		assertThatExceptionOfType(DestinationNotExistsException.class)
			.isThrownBy(() -> perun.getServicesManager().getDestinationById(sess, destination.getId()));
	}

	@Test (expected=ServiceNotExistsException.class)
	public void removeAllDestinationsWhenServiceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeAllDestinationsWhenServiceNotExists");

		facility = setUpFacility();

		perun.getServicesManager().removeAllDestinations(sess, new Service(), facility);
		// shouldn't find service

	}

	@Test (expected=FacilityNotExistsException.class)
	public void removeAllDestinationsWhenFacilityNotExists() throws Exception {
		System.out.println(CLASS_NAME + "removeAllDestinationsWhenFacilityNotExists");

		service = setUpService();

		perun.getServicesManager().removeAllDestinations(sess, service, new Facility());
		// shouldn't find facility

	}

	@Test
	public void removeDestinations() throws Exception {
		System.out.println(CLASS_NAME + "removeDestinations");

		facility = setUpFacility();
		service = setUpService();
		Service service2 = new Service();
		service2.setName("ServicesManagerTestService2");
		service2 = perun.getServicesManager().createService(sess, service2);
		List<Destination> destinations = setUpDestinations();

		Destination destination1 = perun.getServicesManager().addDestination(sess, service, facility, destinations.get(0));
		Destination destination2 = perun.getServicesManager().addDestination(sess, service2, facility, destinations.get(1));
		Destination destination3 = perun.getServicesManager().addDestination(sess, service2, facility, destinations.get(2));

		RichDestination richDestination1 = new RichDestination(destination1, facility, service);
		RichDestination richDestination2 = new RichDestination(destination2, facility, service2);
		RichDestination richDestination3 = new RichDestination(destination3, facility, service2);

		List<RichDestination> facilityDestinations = perun.getServicesManager().getAllRichDestinations(sess, facility);
		perun.getServicesManager().getAllRichDestinations(sess, facility);
		assertTrue("There need to be richDestination1", facilityDestinations.contains(richDestination1));
		assertTrue("There need to be richDestination2", facilityDestinations.contains(richDestination2));
		assertTrue("There need to be richDestination3", facilityDestinations.contains(richDestination3));

		List<RichDestination> destinationsToRemove = Arrays.asList(richDestination1, richDestination2);
		perun.getServicesManager().removeDestinationsByRichDestinations(sess, destinationsToRemove);
		facilityDestinations = perun.getServicesManagerBl().getAllRichDestinations(sess, facility);

		assertTrue("Service contains richDestination3", facilityDestinations.contains(richDestination3));
		assertEquals("Service has exactly 1 richDestination", 1, facilityDestinations.size());
		// destination and destination2 should be deleted
		assertThatExceptionOfType(DestinationNotExistsException.class)
			.isThrownBy(() -> perun.getServicesManager().getDestinationById(sess, destination1.getId()));
		assertThatExceptionOfType(DestinationNotExistsException.class)
			.isThrownBy(() -> perun.getServicesManager().getDestinationById(sess, destination2.getId()));
	}

	@Test
	public void blockAndUnblockServiceOnDestinations() throws Exception {
		System.out.println(CLASS_NAME + "blockAndUnblockServiceOnDestinations");

		facility = setUpFacility();
		service = setUpService();
		Service service2 = new Service();
		service2.setName("ServicesManagerTestService2");
		service2 = perun.getServicesManager().createService(sess, service2);
		List<Destination> destinations = setUpDestinations();

		Destination destination1 = perun.getServicesManager().addDestination(sess, service, facility, destinations.get(0));
		Destination destination2 = perun.getServicesManager().addDestination(sess, service2, facility, destinations.get(1));
		Destination destination3 = perun.getServicesManager().addDestination(sess, service2, facility, destinations.get(2));

		RichDestination richDestination1 = new RichDestination(destination1, facility, service);
		RichDestination richDestination2 = new RichDestination(destination2, facility, service2);
		RichDestination richDestination3 = new RichDestination(destination3, facility, service2);

		List<RichDestination> serviceDestinations = perun.getServicesManagerBl().getAllRichDestinations(sess, facility);
		assertTrue("There need to be richDestination1", serviceDestinations.contains(richDestination1));
		assertTrue("There need to be richDestination2", serviceDestinations.contains(richDestination2));
		assertTrue("There need to be richDestination3", serviceDestinations.contains(richDestination3));

		List<RichDestination> destinationsToBlock = Arrays.asList(richDestination1, richDestination2);
		perun.getServicesManager().blockServicesOnDestinations(sess, destinationsToBlock);

		assertTrue("Service should be blocked on the richDestination1", perun.getServicesManager().isServiceBlockedOnDestination(sess, service, destination1.getId()));
		assertTrue("Service should be blocked on the richDestination2", perun.getServicesManager().isServiceBlockedOnDestination(sess, service2, destination2.getId()));
		assertFalse("Service should NOT be blocked on the richDestination3", perun.getServicesManager().isServiceBlockedOnDestination(sess, service2, destination3.getId()));
	}

	@Test
	public void blockAndUnblockServicesOnFacility() throws Exception {
		System.out.println(CLASS_NAME + "blockAndUnblockServicesOnFacility");

		facility = setUpFacility();
		service = setUpService();
		Service service2 = new Service();
		service2.setName("testService");
		service2 = perun.getServicesManager().createService(sess, service2);

		perun.getServicesManager().blockServicesOnFacility(sess, List.of(service, service2), facility);
		assertTrue(perun.getServicesManager().isServiceBlockedOnFacility(sess, service, facility));
		assertTrue(perun.getServicesManager().isServiceBlockedOnFacility(sess, service2, facility));

		perun.getServicesManager().unblockServicesOnFacility(sess, List.of(service, service2), facility);
		assertFalse(perun.getServicesManager().isServiceBlockedOnFacility(sess, service, facility));
		assertFalse(perun.getServicesManager().isServiceBlockedOnFacility(sess, service2, facility));
	}

	@Test (expected=ServiceAlreadyBannedException.class)
	public void blockServicesOnFacilityWhenAlreadyBlocked() throws Exception {
		System.out.println(CLASS_NAME + "blockServicesOnFacilityWhenAlreadyBlocked");

		facility = setUpFacility();
		service = setUpService();
		Service service2 = new Service();
		service2.setName("testService");
		service2 = perun.getServicesManager().createService(sess, service2);

		perun.getServicesManager().blockServiceOnFacility(sess, service, facility);
		assertTrue(perun.getServicesManager().isServiceBlockedOnFacility(sess, service, facility));
		assertFalse(perun.getServicesManager().isServiceBlockedOnFacility(sess, service2, facility));

		perun.getServicesManager().blockServicesOnFacility(sess, List.of(service, service2), facility);
	}

	@Test (expected= ServiceIsNotBannedException.class)
	public void unblockServicesOnFacilityWhenNotBlocked() throws Exception {
		System.out.println(CLASS_NAME + "blockServicesOnFacilityWhenAlreadyBlocked");

		facility = setUpFacility();
		service = setUpService();
		Service service2 = new Service();
		service2.setName("testService");
		service2 = perun.getServicesManager().createService(sess, service2);

		perun.getServicesManager().blockServiceOnFacility(sess, service, facility);
		assertTrue(perun.getServicesManager().isServiceBlockedOnFacility(sess, service, facility));
		assertFalse(perun.getServicesManager().isServiceBlockedOnFacility(sess, service2, facility));

		perun.getServicesManager().unblockServicesOnFacility(sess, List.of(service, service2), facility);
	}

	@Test
	public void getAllDestinationsWithFacility() throws Exception {
		System.out.println(CLASS_NAME + "getAllDestinationsWithFacility");
		service = setUpService();
		facility = setUpFacility();
		destination = setUpDestination();
		perun.getServicesManagerBl().addDestination(sess, service, facility, destination);
		List<Destination> destinations = perun.getServicesManagerBl().getDestinations(sess, facility);
		assertTrue("there shoudl be one detination",!destinations.isEmpty());
		Destination dest = destinations.get(0);
		assertTrue("there is the right destination in the richDestination", dest.getDestination().equals(destination.getDestination()));
	}

	@Test
	public void getAllRichDestinationsWithFacility() throws Exception {
		System.out.println(CLASS_NAME + "getAllRichDestinationsWithFacility");
		service = setUpService();
		facility = setUpFacility();
		destination = setUpDestination();
		perun.getServicesManagerBl().addDestination(sess, service, facility, destination);
		List<RichDestination> richDestinations = perun.getServicesManager().getAllRichDestinations(sess, facility);
		assertTrue("there shoudl be one detination",!richDestinations.isEmpty());
		RichDestination richDestination = richDestinations.get(0);
		assertTrue("there is the right facility in the richDestination",richDestination.getFacility().equals(facility));
		assertTrue("there is the right service in the richDestination",richDestination.getService().equals(service));
		assertTrue("there is the right destination in the richDestination",richDestination.getDestination().equals(destination.getDestination()));
	}

	@Test
	public void getAllRichDestinationsWithService() throws Exception {
		System.out.println(CLASS_NAME + "getAllRichDestinationsWithService");
		service = setUpService();
		facility = setUpFacility();
		destination = setUpDestination();
		perun.getServicesManagerBl().addDestination(sess, service, facility, destination);
		List<RichDestination> richDestinations = perun.getServicesManager().getAllRichDestinations(sess, service);
		assertTrue("there shoudl be one detination",!richDestinations.isEmpty());
		RichDestination richDestination = richDestinations.get(0);
		assertTrue("there is the right facility in the richDestination",richDestination.getFacility().equals(facility));
		assertTrue("there is the right service in the richDestination",richDestination.getService().equals(service));
		assertTrue("there is the right destination in the richDestination",richDestination.getDestination().equals(destination.getDestination()));
	}

	@Test
	public void getRichDestinations() throws Exception {
		System.out.println(CLASS_NAME + "getRichDestinations");
		service = setUpService();
		facility = setUpFacility();
		destination = setUpDestination();
		perun.getServicesManagerBl().addDestination(sess, service, facility, destination);
		List<RichDestination> richDestinations = perun.getServicesManager().getRichDestinations(sess, facility, service);
		assertTrue("there shoudl be one detination",!richDestinations.isEmpty());
		RichDestination richDestination = richDestinations.get(0);
		assertTrue("there is the right facility in the richDestination",richDestination.getFacility().equals(facility));
		assertTrue("there is the right service in the richDestination",richDestination.getService().equals(service));
		assertTrue("there is the right destination in the richDestination",richDestination.getDestination().equals(destination.getDestination()));
	}

	@Test
	public void getDestinations() throws Exception {
		System.out.println(CLASS_NAME + "getDestinations");

		service = setUpService();
		facility = setUpFacility();

		List<Destination> destinations = perun.getServicesManager().getDestinations(sess, service, facility);
		assertTrue("there shoudln't be any detinations",destinations.isEmpty());

	}

	@Test
	public void getDestinationsWithPerunSession() throws Exception {
		System.out.println(CLASS_NAME + "getDestinations");

		Service service1 = setUpService();
		Facility facility1 = setUpFacility();
		Destination destination = setUpDestination();
		perun.getServicesManager().addDestination(sess, service1, facility1, destination);

		List<Destination> destinations = perun.getServicesManager().getDestinations(sess);
		assertTrue("there should be at least one destination", destinations.size() >= 1);
		assertTrue("our destination should be between all destinations", destinations.contains(destination));
	}

	@Test (expected=ServiceNotExistsException.class)
	public void getDestinationsWhenServiceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getDestinationsWhenServiceNotExists");

		facility = setUpFacility();

		perun.getServicesManager().getDestinations(sess, new Service(), facility);
		// shouldn't find Service

	}

	@Test (expected=FacilityNotExistsException.class)
	public void getDestinationsWhenFacilityNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getDestinationsWhenFacilityNotExists");

		service = setUpService();

		perun.getServicesManager().getDestinations(sess, service, new Facility());
		// shouldn't find facility

	}

	@Test
	public void getFacilitiesDestinations() throws Exception {
		System.out.println(CLASS_NAME + "getFacilitiesDestinations");
		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		service = setUpService();
		destination = setUpDestination();
		perun.getServicesManagerBl().addDestination(sess, service, facility, destination);
		List<Destination> destinations = perun.getServicesManager().getFacilitiesDestinations(sess, vo);
		assertTrue("There should be one destination.",destinations.size() == 1);
	}

	@Test(expected = PrivilegeException.class)
	public void addDestinationSameDestinationDifferentAdmin() throws Exception {
		System.out.println(CLASS_NAME + "addDestinationSameDestinationDifferentAdmin");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		service = setUpService();
		Destination testDestination = new Destination(0, "test.destination", Destination.DESTINATIONHOSTTYPE);
		member = setUpMember();
		Member memberTwo = setUpMember();

		// Creates second facility
		Facility secondFacility = new Facility(0, "TestSecondFacility", "TestDescriptionText");
		assertNotNull(perun.getFacilitiesManager().createFacility(sess, secondFacility));

		// Set users as admins of different facilities
		User userOne = perun.getUsersManagerBl().getUserByMember(sess, member);
		perun.getFacilitiesManager().addAdmin(sess, facility, userOne);
		User userTwo = perun.getUsersManagerBl().getUserByMember(sess, memberTwo);
		perun.getFacilitiesManager().addAdmin(sess, secondFacility, userTwo);

		// Sets userOne as actor in this test with role facility admin for facility
		AuthzRoles authzRoles = new AuthzRoles(Role.FACILITYADMIN, facility);
		sess.getPerunPrincipal().setRoles(authzRoles);
		sess.getPerunPrincipal().setUser(userOne);
		// Adds destination to facility
		perun.getServicesManager().addDestination(sess, service, facility, testDestination);
		assertTrue(perun.getServicesManager().getDestinations(sess, service, facility).size() == 1);

		// Change actor in this test to userTwo
		authzRoles = new AuthzRoles(Role.FACILITYADMIN, secondFacility);
		sess.getPerunPrincipal().setRoles(authzRoles);
		sess.getPerunPrincipal().setUser(userTwo);
		// Adds same destination to secondFacility -> should throw exception
 		perun.getServicesManager().addDestination(sess, service, secondFacility, testDestination);
	}

	@Test
	public void getHashedDataWithGroups() throws Exception {
		System.out.println(CLASS_NAME + "getHashedDataWithGroups");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		service = setUpService();
		member = setUpMember();
		group = setUpGroup();
		perun.getGroupsManager().addMember(sess, group, member);
		perun.getResourcesManager().assignGroupToResource(sess, group, resource, false, false, false);

		// set element's name/id as required attributes to get some attributes for every element
		Attribute reqFacAttr;
		reqFacAttr = perun.getAttributesManager().getAttribute(sess, facility, A_F_C_NAME);
		perun.getServicesManager().addRequiredAttribute(sess, service, reqFacAttr);
		Attribute reqResAttr;
		reqResAttr = perun.getAttributesManager().getAttribute(sess, resource, A_R_C_NAME);
		perun.getServicesManager().addRequiredAttribute(sess, service, reqResAttr);
		Attribute reqGrpAttr;
		reqGrpAttr = perun.getAttributesManager().getAttribute(sess, group, A_G_C_NAME);
		perun.getServicesManager().addRequiredAttribute(sess, service, reqGrpAttr);
		Attribute reqMemAttr;
		reqMemAttr = perun.getAttributesManager().getAttribute(sess, member, A_M_C_ID);
		perun.getServicesManager().addRequiredAttribute(sess, service, reqMemAttr);
		Attribute reqVoAttr;
		reqVoAttr = perun.getAttributesManager().getAttribute(sess, vo, A_V_C_ID);
		perun.getServicesManager().addRequiredAttribute(sess, service, reqVoAttr);

		// finally assign service
		perun.getResourcesManager().assignService(sess, resource, service);

		// create second (but same) resource
		Resource resource2 = new Resource();
		resource2.setName("HierarchDataResource");
		resource2 = perun.getResourcesManager().createResource(sess, resource2, vo, facility);
		perun.getResourcesManager().assignGroupToResource(sess, group, resource2, false, false, false);
		perun.getResourcesManager().assignService(sess, resource2, service);

		//create third resource but without service
		Resource resource3 = new Resource();
		resource3.setName("HierarchDataResource2");
		resource3 = perun.getResourcesManager().createResource(sess, resource3, vo, facility);

		HashedGenData data = perun.getServicesManagerBl().getHashedDataWithGroups(sess, service, facility, false);
		assertThat(data.getAttributes()).isNotEmpty();

		Map<String, Map<String, Object>> attributes = data.getAttributes();

		String facilityAttrsHash = "f-" + facility.getId();
		String memberAttrsHash = "m-" + member.getId();
		String groupAttrsHash = "g-" + group.getId();
		String voAttrsHash = "v-" + vo.getId();
		String resource1AttrsHash = "r-" + resource.getId();
		String resource2AttrsHash = "r-" + resource2.getId();

		// Verify that the list of all attributes contains correct attributes
		assertThat(attributes).containsOnlyKeys(facilityAttrsHash, memberAttrsHash, resource1AttrsHash, groupAttrsHash,
				resource2AttrsHash, voAttrsHash);

		Map<String, Object> facilityAttributes = attributes.get(facilityAttrsHash);
		assertThat(facilityAttributes).hasSize(1);
		assertThat(facilityAttributes.get(A_F_C_NAME)).isEqualTo(facility.getName());

		Map<String, Object> memberAttributes = attributes.get(memberAttrsHash);
		assertThat(memberAttributes).hasSize(1);
		assertThat(memberAttributes.get(A_M_C_ID)).isEqualTo(member.getId());

		Map<String, Object> groupAttributes = attributes.get(groupAttrsHash);
		assertThat(groupAttributes).hasSize(1);
		assertThat(groupAttributes.get(A_G_C_NAME)).isEqualTo(group.getName());

		Map<String, Object> resource1Attributes = attributes.get(resource1AttrsHash);
		assertThat(resource1Attributes).hasSize(1);
		assertThat(resource1Attributes.get(A_R_C_NAME)).isEqualTo(resource.getName());

		Map<String, Object> voAttributes = attributes.get(voAttrsHash);
		assertThat(voAttributes).hasSize(1);
		assertThat(voAttributes.get(A_V_C_ID)).isEqualTo(vo.getId());

		Map<String, Object> resource2Attributes = attributes.get(resource2AttrsHash);
		assertThat(resource2Attributes).hasSize(1);
		assertThat(resource2Attributes.get(A_R_C_NAME)).isEqualTo(resource2.getName());

		// verify hierarchy
		GenDataNode facilityNode = data.getHierarchy().get(facility.getId());
		assertThat(facilityNode.getMembers()).hasSize(1);
		assertThat(facilityNode.getChildren()).hasSize(2);

		GenDataNode res1Node = facilityNode.getChildren().get(resource.getId());
		assertThat(res1Node.getMembers()).hasSize(1);
		assertThat(res1Node.getChildren()).hasSize(1);
		assertThat(((GenResourceDataNode)res1Node).getVoId()).isEqualTo(vo.getId());
		assertThat(res1Node.getMembers()).containsKey(member.getId());

		GenDataNode res2Node = facilityNode.getChildren().get(resource2.getId());
		assertThat(res2Node.getMembers()).hasSize(1);
		assertThat(res2Node.getChildren()).hasSize(1);
		assertThat(((GenResourceDataNode)res2Node).getVoId()).isEqualTo(vo.getId());
		assertThat(res2Node.getMembers()).containsKey(member.getId());

		GenDataNode res1GroupNode = res1Node.getChildren().get(group.getId());
		assertThat(res1GroupNode.getChildren()).isEmpty();
		assertThat(res1GroupNode.getMembers()).hasSize(1);
		assertThat(res1GroupNode.getMembers()).containsKey(member.getId());

		GenDataNode res2GroupNode = res2Node.getChildren().get(group.getId());
		assertThat(res2GroupNode.getChildren()).isEmpty();
		assertThat(res2GroupNode.getMembers()).hasSize(1);
		assertThat(res2GroupNode.getMembers()).containsKey(member.getId());
	}

	@Test
	public void getHashedDataWithGroupsWithoutExpiredMembers() throws Exception {
		System.out.println(CLASS_NAME + "getHashedDataWithGroupsWithoutExpiredMembers");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		service = setUpService();
		member = setUpMember();
		Member expiredMember = setUpMember();
		group = setUpGroup();
		perun.getGroupsManager().addMember(sess, group, member);
		perun.getGroupsManager().addMember(sess, group, expiredMember);
		perun.getResourcesManager().assignGroupToResource(sess, group, resource, false, false, false);

		// expire member
		perun.getGroupsManager().setMemberGroupStatus(sess, expiredMember, group, MemberGroupStatus.EXPIRED);

		// set member's id as required attribute
		Attribute reqMemAttr;
		reqMemAttr = perun.getAttributesManager().getAttribute(sess, member, A_M_C_ID);
		perun.getServicesManager().addRequiredAttribute(sess, service, reqMemAttr);

		// finally assign service
		perun.getResourcesManager().assignService(sess, resource, service);

		HashedGenData data = perun.getServicesManagerBl().getHashedDataWithGroups(sess, service, facility, false);
		assertThat(data.getAttributes()).isNotEmpty();

		Map<String, Map<String, Object>> attributes = data.getAttributes();

		String memberAttrsHash = "m-" + member.getId();
		String expiredMemberAttrsHash = "m-" + expiredMember.getId();

		// Verify that the list of all attributes contains correct attributes
		assertThat(attributes).containsOnlyKeys(memberAttrsHash, expiredMemberAttrsHash);

		// set service to not use expired members
		service.setUseExpiredMembers(false);
		perun.getServicesManagerBl().updateService(sess, service);

		data = perun.getServicesManagerBl().getHashedDataWithGroups(sess, service, facility, false);
		assertThat(data.getAttributes()).isNotEmpty();

		attributes = data.getAttributes();

		// Verify that the list of all attributes contains correct attributes
		assertThat(attributes).containsOnlyKeys(memberAttrsHash);

		Map<String, Object> memberAttributes = attributes.get(memberAttrsHash);
		assertThat(memberAttributes).hasSize(1);
		assertThat(memberAttributes.get(A_M_C_ID)).isEqualTo(member.getId());

		// verify hierarchy
		GenDataNode facilityNode = data.getHierarchy().get(facility.getId());
		assertThat(facilityNode.getMembers()).hasSize(1);
		assertThat(facilityNode.getMembers()).containsKey(member.getId());
		assertThat(facilityNode.getChildren()).hasSize(1);

		GenDataNode res1Node = facilityNode.getChildren().get(resource.getId());
		assertThat(res1Node.getMembers()).hasSize(1);
		assertThat(res1Node.getMembers()).containsKey(member.getId());
		assertThat(res1Node.getChildren()).hasSize(1);
		assertThat(((GenResourceDataNode)res1Node).getVoId()).isEqualTo(vo.getId());

		GenDataNode res1GroupNode = res1Node.getChildren().get(group.getId());
		assertThat(res1GroupNode.getChildren()).isEmpty();
		assertThat(res1GroupNode.getMembers()).hasSize(1);
		assertThat(res1GroupNode.getMembers()).containsKey(member.getId());
	}

	@Test(expected = FacilityNotExistsException.class)
	public void getHashedDataWithGroupsWhenFacilityNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getHashedDataWithGroupsWhenFacilityNotExists");
		perun.getServicesManager().getHashedDataWithGroups(sess, setUpService(), new Facility(), false);
	}

	@Test(expected = ServiceNotExistsException.class)
	public void getHashedDataWithGroupsWhenServiceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getHashedDataWithGroupsWhenServiceNotExists");
		perun.getServicesManager().getHashedDataWithGroups(sess, new Service(), setUpFacility(), false);
	}

	@Test
	public void getHashedHierarchicalData() throws Exception {
		System.out.println(CLASS_NAME + "getHashedHierarchicalData");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		service = setUpService();
		member = setUpMember();
		group = setUpGroup();
		perun.getGroupsManager().addMember(sess, group, member);
		perun.getResourcesManager().assignGroupToResource(sess, group, resource, false, false, false);

		// set element's name/id as required attributes to get some attributes for every element
		Attribute reqFacAttr;
		reqFacAttr = perun.getAttributesManager().getAttribute(sess, facility, A_F_C_NAME);
		perun.getServicesManager().addRequiredAttribute(sess, service, reqFacAttr);
		Attribute reqResAttr;
		reqResAttr = perun.getAttributesManager().getAttribute(sess, resource, A_R_C_NAME);
		perun.getServicesManager().addRequiredAttribute(sess, service, reqResAttr);
		Attribute reqGrpAttr;
		reqGrpAttr = perun.getAttributesManager().getAttribute(sess, group, A_G_C_NAME);
		perun.getServicesManager().addRequiredAttribute(sess, service, reqGrpAttr);
		Attribute reqMemAttr;
		reqMemAttr = perun.getAttributesManager().getAttribute(sess, member, A_M_C_ID);
		perun.getServicesManager().addRequiredAttribute(sess, service, reqMemAttr);
		Attribute reqVoAttr;
		reqVoAttr = perun.getAttributesManager().getAttribute(sess, vo, A_V_C_ID);
		perun.getServicesManager().addRequiredAttribute(sess, service, reqVoAttr);

		// finally assign service
		perun.getResourcesManager().assignService(sess, resource, service);

		// create second (but same) resource
		Resource resource2 = new Resource();
		resource2.setName("HierarchDataResource");
		resource2 = perun.getResourcesManager().createResource(sess, resource2, vo, facility);
		perun.getResourcesManager().assignGroupToResource(sess, group, resource2, false, false, false);
		perun.getResourcesManager().assignService(sess, resource2, service);

		//create third resource but without service
		Resource resource3 = new Resource();
		resource3.setName("HierarchDataResource2");
		resource3 = perun.getResourcesManager().createResource(sess, resource3, vo, facility);

		HashedGenData data = perun.getServicesManagerBl().getHashedHierarchicalData(sess, service, facility, false);
		assertThat(data.getAttributes()).isNotEmpty();

		Map<String, Map<String, Object>> attributes = data.getAttributes();

		String facilityAttrsHash = "f-" + facility.getId();
		String memberAttrsHash = "m-" + member.getId();
		String groupAttrsHash = "g-" + group.getId();
		String voAttrsHash = "v-" + vo.getId();
		String resource1AttrsHash = "r-" + resource.getId();
		String resource2AttrsHash = "r-" + resource2.getId();
		String resource3AttrsHash = "r-" + resource3.getId();

		// Verify that the list of all attributes contains correct attributes
		assertThat(attributes).containsOnlyKeys(facilityAttrsHash, memberAttrsHash, resource1AttrsHash,
				resource2AttrsHash, voAttrsHash);

		Map<String, Object> facilityAttributes = attributes.get(facilityAttrsHash);
		assertThat(facilityAttributes).hasSize(1);
		assertThat(facilityAttributes.get(A_F_C_NAME)).isEqualTo(facility.getName());

		Map<String, Object> memberAttributes = attributes.get(memberAttrsHash);
		assertThat(memberAttributes).hasSize(1);
		assertThat(memberAttributes.get(A_M_C_ID)).isEqualTo(member.getId());

		Map<String, Object> voAttributes = attributes.get(voAttrsHash);
		assertThat(voAttributes).hasSize(1);
		assertThat(voAttributes.get(A_V_C_ID)).isEqualTo(vo.getId());

		Map<String, Object> resource1Attributes = attributes.get(resource1AttrsHash);
		assertThat(resource1Attributes).hasSize(1);
		assertThat(resource1Attributes.get(A_R_C_NAME)).isEqualTo(resource.getName());

		Map<String, Object> resource2Attributes = attributes.get(resource2AttrsHash);
		assertThat(resource2Attributes).hasSize(1);
		assertThat(resource2Attributes.get(A_R_C_NAME)).isEqualTo(resource2.getName());

		// verify hierarchy
		GenDataNode facilityNode = data.getHierarchy().get(facility.getId());
		assertThat(facilityNode.getMembers()).hasSize(1);
		assertThat(facilityNode.getChildren()).hasSize(2);

		GenDataNode res1Node = facilityNode.getChildren().get(resource.getId());
		assertThat(res1Node.getMembers().keySet()).hasSize(1);
		assertThat(((GenResourceDataNode)res1Node).getVoId()).isEqualTo(vo.getId());
		assertThat(res1Node.getChildren()).isEmpty();
		assertThat(res1Node.getMembers()).containsKey(member.getId());

		GenDataNode res2Node = facilityNode.getChildren().get(resource2.getId());
		assertThat(((GenResourceDataNode)res2Node).getVoId()).isEqualTo(vo.getId());
		assertThat(res2Node.getMembers()).hasSize(1);
		assertThat(res2Node.getChildren()).isEmpty();
		assertThat(res2Node.getMembers()).containsKey(member.getId());
	}

	@Test
	public void getHashedHierarchicalDataWithoutExpiredMembers() throws Exception {
		System.out.println(CLASS_NAME + "getHashedHierarchicalDataWithoutExpiredMembers");

		vo = setUpVo();
		facility = setUpFacility();
		resource = setUpResource();
		service = setUpService();
		member = setUpMember();
		Member expiredMember = setUpMember();

		group = setUpGroup();
		perun.getGroupsManager().addMember(sess, group, member);
		perun.getGroupsManager().addMember(sess, group, expiredMember);
		perun.getResourcesManager().assignGroupToResource(sess, group, resource, false, false, false);

		// expire member
		perun.getGroupsManager().setMemberGroupStatus(sess, expiredMember, group, MemberGroupStatus.EXPIRED);

		// set member's id as required attribute
		Attribute reqMemAttr;
		reqMemAttr = perun.getAttributesManager().getAttribute(sess, member, A_M_C_ID);
		perun.getServicesManager().addRequiredAttribute(sess, service, reqMemAttr);

		// finally assign service
		perun.getResourcesManager().assignService(sess, resource, service);

		HashedGenData data = perun.getServicesManager().getHashedHierarchicalData(sess, service, facility, false);
		assertThat(data.getAttributes()).isNotEmpty();

		Map<String, Map<String, Object>> attributes = data.getAttributes();

		String memberAttrsHash = "m-" + member.getId();
		String expiredMemberAttrsHash = "m-" + expiredMember.getId();

		// Verify that the list of all attributes contains correct attributes
		assertThat(attributes).containsOnlyKeys(memberAttrsHash, expiredMemberAttrsHash);

		// set serivce to not use expired members
		service.setUseExpiredMembers(false);
		perun.getServicesManagerBl().updateService(sess, service);
		data = perun.getServicesManager().getHashedHierarchicalData(sess, service, facility, false);

		// verify only active member's attribute are present
		assertThat(data.getAttributes()).containsOnlyKeys(memberAttrsHash);

		// verify only active member is present
		GenDataNode facilityNode = data.getHierarchy().get(facility.getId());
		assertThat(facilityNode.getMembers()).hasSize(1);
		assertThat(facilityNode.getMembers()).containsKey(member.getId());
		assertThat(facilityNode.getChildren()).hasSize(1);

		GenDataNode res1Node = facilityNode.getChildren().get(resource.getId());
		assertThat(res1Node.getMembers().keySet()).hasSize(1);
		assertThat(res1Node.getMembers()).containsKey(member.getId());
		assertThat(((GenResourceDataNode)res1Node).getVoId()).isEqualTo(vo.getId());
		assertThat(res1Node.getChildren()).isEmpty();
	}

	@Test(expected = FacilityNotExistsException.class)
	public void getHashedHierarchicalDataWhenFacilityNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getHashedHierarchicalDataWhenFacilityNotExists");
		perun.getServicesManager().getHashedHierarchicalData(sess, setUpService(), new Facility(), false);
	}

	@Test(expected = ServiceNotExistsException.class)
	public void getHashedHierarchicalDataWhenServiceNotExists() throws Exception {
		System.out.println(CLASS_NAME + "getHashedHierarchicalDataWhenServiceNotExists");
		perun.getServicesManager().getHashedHierarchicalData(sess, new Service(), setUpFacility(), false);
	}

	@Test
	public void getAssignedServicesVo() throws Exception {
		System.out.println(CLASS_NAME + "getAssignedServicesVo");

		facility = setUpFacility();
		List<Service> services = setUpServices();

		vo = setUpVo();
		resource = setUpResource();
		Service service1 = services.get(0);
		perun.getResourcesManagerBl().assignService(sess, resource, service1);

		Vo vo2 = new Vo(0, "ServicesManagerTestVo2", "RMTestVo2");
		vo2 = perun.getVosManager().createVo(sess, vo2);
		Resource resource2 = new Resource();
		resource2.setName("ServicesManagerTestResource2");
		resource2.setDescription("Testovaci");
		resource2 = perun.getResourcesManager().createResource(sess, resource2, vo2, facility);
		Service service2 = services.get(1);
		perun.getResourcesManagerBl().assignService(sess, resource2, service2);

		assertThat(perun.getServicesManagerBl().getAssignedServices(sess, facility)).contains(service1, service2);
		assertThat(perun.getServicesManagerBl().getAssignedServices(sess, facility, vo)).containsExactly(service1);
	}

	// PRIVATE METHODS ----------------------------------------------------

	private Service setUpService() throws Exception {

		Service service = new Service();
		service.setName("ServicesManagerTestService");
		service = perun.getServicesManager().createService(sess, service);
		assertNotNull("unable to create service",service);

		return service;

	}

	private List<Service> setUpServices() throws Exception {

		Service service1 = new Service();
		service1.setName("ServicesManagerTestService01");
		service1 = perun.getServicesManager().createService(sess, service1);
		assertNotNull("unable to create service",service1);

		Service service2 = new Service();
		service2.setName("ServicesManagerTestService02");
		service2 = perun.getServicesManager().createService(sess, service2);
		assertNotNull("unable to create service",service2);

		List<Service> services = new ArrayList<>();
		services.add(service1);
		services.add(service2);

		return services;
	}

	private void assignServicesOnResource(Resource resource, List<Service> services) throws Exception {
		for(Service s: services) {
			perun.getResourcesManagerBl().assignService(sess, resource, s);
		}
	}

	private ServicesPackage setUpServicesPackage(Service service) throws Exception {

		ServicesPackage servicesPackage = new ServicesPackage();
		servicesPackage.setName("ResourcesManagertTestSP");
		servicesPackage.setDescription("testingServicePackage");
		servicesPackage = perun.getServicesManager().createServicesPackage(sess, servicesPackage);
		perun.getServicesManager().addServiceToServicesPackage(sess, servicesPackage, service);

		return servicesPackage;

	}

	private Vo setUpVo() throws Exception {

		Vo newVo = new Vo(0, "ServicesManagerTestVo", "RMTestVo");
		Vo returnedVo = perun.getVosManager().createVo(sess, newVo);
		assertNotNull("unable to create testing Vo",returnedVo);
		return returnedVo;

	}

	private Facility setUpFacility() throws Exception {

		Facility facility = new Facility();
		facility.setName("ServicesManagerTestFacility");
		facility = perun.getFacilitiesManager().createFacility(sess, facility);

		return facility;

	}

	private Facility setUpClusterFacility() throws Exception {

		Facility facility = new Facility();
		facility.setName("ServicesManagerTestClusterFacility");
		facility = perun.getFacilitiesManager().createFacility(sess, facility);

		// add one host
		Host host = new Host();
		host.setHostname("test.test");

		List<Host> hosts = new ArrayList<>();
		hosts.add(host);

		perun.getFacilitiesManager().addHosts(sess, hosts, facility);
		return facility;

	}

	private Facility setUpNonClusterFacilityWithTwoHosts() throws Exception {

		Facility facility = new Facility();
		facility.setName("ServicesManagerTestNonClusterFacility");
		facility = perun.getFacilitiesManager().createFacility(sess, facility);

		// add first host
		Host host1 = new Host();
		host1.setHostname("testing.host1");

		// add second host
		Host host2 = new Host();
		host2.setHostname("testing.host2");

		List<Host> hosts = new ArrayList<>();
		hosts.add(host1);
		hosts.add(host2);
		perun.getFacilitiesManager().addHosts(sess, hosts, facility);
		return facility;
	}

	private Resource setUpResource() throws Exception {

		Resource resource = new Resource();
		resource.setName("ServicesManagerTestResource");
		resource.setDescription("Testovaci");
		resource = perun.getResourcesManager().createResource(sess, resource, vo, facility);
		return resource;

	}

	private AttributeDefinition setUpAttribute() throws Exception {

		attribute = new AttributeDefinition();
		attribute.setFriendlyName("ServicesManagerTestAttribute");
		attribute.setDescription("TestingAttribute");
		attribute.setNamespace(AttributesManager.NS_ENTITYLESS_ATTR_DEF);
		attribute.setType(String.class.getName());

		attribute = perun.getAttributesManager().createAttribute(sess, attribute);

		return attribute;

	}

	private AttributeDefinition setUpUserAttribute() throws Exception {

		attribute = new AttributeDefinition();
		attribute.setFriendlyName("ServicesManagerTestUserAttribute");
		attribute.setDescription("TestingUserAttribute");
		attribute.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attribute.setType(String.class.getName());

		attribute = perun.getAttributesManager().createAttribute(sess, attribute);

		return attribute;

	}

	private List<AttributeDefinition> setUpRequiredAttribute() throws Exception {

		List<AttributeDefinition> attrList = new ArrayList<>();
		attrList.add(setUpAttribute());

		return attrList;

	}

	private Destination setUpDestination() {

		Destination destination = new Destination();
		destination.setDestination("testDestination");
		destination.setType("service-specific");
		//destination = perun.getServicesManager().addDestination(sess, service, facility, destination);

		return destination;

	}

	private List<Destination> setUpDestinations() {

		Destination destination1 = new Destination();
		destination1.setDestination("testDestination");
		destination1.setType("service-specific");
		Destination destination2 = new Destination();
		destination2.setDestination("testDestination2");
		destination2.setType("service-specific");
		Destination destination3 = new Destination();
		destination3.setDestination("testDestination3");
		destination3.setType("service-specific");

		return Arrays.asList(destination1, destination2, destination3);

	}

	private Destination setUpHostDestination() {

		Destination destination = new Destination();
		destination.setDestination("test.test");
		destination.setType(Destination.DESTINATIONHOSTTYPE);
		//destination = perun.getServicesManager().addDestination(sess, service, facility, destination);

		return destination;

	}

	private Member setUpMember() throws Exception {

		String userFirstName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String userLastName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String extLogin = Long.toHexString(Double.doubleToLongBits(Math.random()));              // his login in external source

		Candidate candidate;
		candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName(userFirstName);
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName(userLastName);
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		UserExtSource ues = new UserExtSource(new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal"), extLogin);
		candidate.setUserExtSource(ues);
		candidate.setAttributes(new HashMap<>());

		Member createdMember = perun.getMembersManagerBl().createMemberSync(sess, vo, candidate);
		assertNotNull("No member created", createdMember);
		usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, createdMember));
		// save user for deletion after test
		return createdMember;

	}

	private Member setUpMemberInVo2(Vo vo2) throws Exception {

		String userFirstName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String userLastName = Long.toHexString(Double.doubleToLongBits(Math.random()));
		String extLogin = Long.toHexString(Double.doubleToLongBits(Math.random()));              // his login in external source

		Candidate candidate;
		candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName(userFirstName);
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName(userLastName);
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		UserExtSource ues = new UserExtSource(new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal"), extLogin);
		candidate.setUserExtSource(ues);
		candidate.setAttributes(new HashMap<>());

		Member createdMember = perun.getMembersManagerBl().createMemberSync(sess, vo2, candidate);
		assertNotNull("No member created", createdMember);
		usersForDeletion.add(perun.getUsersManager().getUserByMember(sess, createdMember));
		// save user for deletion after test
		return createdMember;

	}

	private Group setUpGroup() throws Exception {

		group = new Group("GroupsManagerTestGroup1","testovaci1");
		Group returnedGroup = perun.getGroupsManager().createGroup(sess, vo, group);
		assertNotNull("unable to create a group",returnedGroup);
		return returnedGroup;

	}

}
