package cz.metacentrum.perun.core.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.RowMapper;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.RichDestination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServicesPackage;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyRemovedFromServicePackageException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServicesPackageNotExistsException;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import cz.metacentrum.perun.core.implApi.ServicesManagerImplApi;
import org.springframework.dao.DuplicateKeyException;

/**
 * @author Michal Prochazka <michalp@ics.muni.cz>
 * @author Slavek Licehammer <glory@ics.muni.cz>
 */
public class ServicesManagerImpl implements ServicesManagerImplApi {


	final static Logger log = LoggerFactory.getLogger(ServicesManagerImpl.class);

	// http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/jdbc.html
	private static JdbcPerunTemplate jdbc;

	public ServicesManagerImpl(DataSource perunPool) {
		jdbc = new JdbcPerunTemplate(perunPool);
	}

	public final static String serviceMappingSelectQuery = " services.id as services_id, services.name as services_name, " +
		"services.created_at as services_created_at, services.created_by as services_created_by, " +
		"services.modified_by as services_modified_by, services.modified_at as services_modified_at, " +
		"services.created_by_uid as services_created_by_uid, services.modified_by_uid as services_modified_by_uid";

	public final static String servicePackageMappingSelectQuery = " service_packages.id as service_packages_id, service_packages.description as service_packages_description, " +
		"service_packages.name as service_packages_name, service_packages.created_at as service_packages_created_at, service_packages.created_by as service_packages_created_by, " +
		"service_packages.modified_by as service_packages_modified_by, service_packages.modified_at as service_packages_modified_at, " +
		"service_packages.modified_by_uid as s_packages_modified_by_uid, service_packages.created_by_uid as s_packages_created_by_uid";

	public final static String destinationMappingSelectQuery = " destinations.id as destinations_id, destinations.destination as destinations_destination, " +
		"destinations.type as destinations_type, destinations.created_at as destinations_created_at, destinations.created_by as destinations_created_by, " +
		"destinations.modified_by as destinations_modified_by, destinations.modified_at as destinations_modified_at, " +
		"destinations.modified_by_uid as destinations_modified_by_uid, destinations.created_by_uid as destinations_created_by_uid";

	public final static String facilityDestinationMappingSelectQuery = destinationMappingSelectQuery + ", facility_service_destinations.propagation_type as f_s_des_propagation_type ";
			
	public final static String richDestinationMappingSelectQuery = " destinations.id as destinations_id, destinations.destination as destinations_destination, " +
		"destinations.type as destinations_type, destinations.created_at as destinations_created_at, destinations.created_by as destinations_created_by, " +
		"destinations.modified_by as destinations_modified_by, destinations.modified_at as destinations_modified_at, " +
		"destinations.modified_by_uid as destinations_modified_by_uid, destinations.created_by_uid as destinations_created_by_uid, " +
		"facilities.id as facilities_id, facilities.name as facilities_name, " +
		"facilities.created_at as facilities_created_at, facilities.created_by as facilities_created_by, facilities.modified_at as facilities_modified_at, facilities.modified_by as facilities_modified_by, " +
		"facilities.modified_by_uid as facilities_modified_by_uid, facilities.created_by_uid as facilities_created_by_uid, " +
		"services.id as services_id, services.name as services_name, " +
		"services.created_at as services_created_at, services.created_by as services_created_by, " +
		"services.modified_by as services_modified_by, services.modified_at as services_modified_at, " +
		"services.created_by_uid as services_created_by_uid, services.modified_by_uid as services_modified_by_uid";

	public static final RowMapper<Service> SERVICE_MAPPER = new RowMapper<Service>() {
		public Service mapRow(ResultSet rs, int i) throws SQLException {
			Service service = new Service();
			service.setId(rs.getInt("services_id"));
			service.setName(rs.getString("services_name"));
			service.setCreatedAt(rs.getString("services_created_at"));
			service.setCreatedBy(rs.getString("services_created_by"));
			service.setModifiedAt(rs.getString("services_modified_at"));
			service.setModifiedBy(rs.getString("services_modified_by"));
			if(rs.getInt("services_modified_by_uid") == 0) service.setModifiedByUid(null);
			else service.setModifiedByUid(rs.getInt("services_modified_by_uid"));
			if(rs.getInt("services_created_by_uid") == 0) service.setCreatedByUid(null);
			else service.setCreatedByUid(rs.getInt("services_created_by_uid"));
			return service;

		}
	};

	public static final RowMapper<ServicesPackage> SERVICESPACKAGE_MAPPER = new RowMapper<ServicesPackage>() {

		public ServicesPackage mapRow(ResultSet rs, int i) throws SQLException {

			ServicesPackage sPackage = new ServicesPackage();
			sPackage.setId(rs.getInt("service_packages_id"));
			sPackage.setDescription(rs.getString("service_packages_description"));
			sPackage.setName(rs.getString("service_packages_name"));
			sPackage.setCreatedAt(rs.getString("service_packages_created_at"));
			sPackage.setCreatedBy(rs.getString("service_packages_created_by"));
			sPackage.setModifiedAt(rs.getString("service_packages_modified_at"));
			sPackage.setModifiedBy(rs.getString("service_packages_modified_by"));
			if(rs.getInt("s_packages_modified_by_uid") == 0) sPackage.setModifiedByUid(null);
			else sPackage.setModifiedByUid(rs.getInt("s_packages_modified_by_uid"));
			if(rs.getInt("s_packages_created_by_uid") == 0) sPackage.setCreatedByUid(null);
			else sPackage.setCreatedByUid(rs.getInt("s_packages_created_by_uid"));
			return sPackage;
		}

	};

	public static final RowMapper<Destination> DESTINATION_MAPPER = new RowMapper<Destination>() {
		public Destination mapRow(ResultSet rs, int i) throws SQLException {
			Destination destination = new Destination();
			destination.setId(rs.getInt("destinations_id"));
			destination.setDestination(rs.getString("destinations_destination"));
			destination.setType(rs.getString("destinations_type"));
			destination.setCreatedAt(rs.getString("destinations_created_at"));
			destination.setCreatedBy(rs.getString("destinations_created_by"));
			destination.setModifiedAt(rs.getString("destinations_modified_at"));
			destination.setModifiedBy(rs.getString("destinations_modified_by"));
			try { // do not mind if the column is not in the results
				if(rs.getString("f_s_des_propagation_type").equals(Destination.PROPAGATIONTYPE_SERIAL)) {
					destination.setPropagationType(Destination.PROPAGATIONTYPE_SERIAL);
				} else {
					destination.setPropagationType(Destination.PROPAGATIONTYPE_PARALLEL);
				}
			} catch (SQLException e) {
				destination.setPropagationType(Destination.PROPAGATIONTYPE_PARALLEL);
			}
			if(rs.getInt("destinations_modified_by_uid") == 0) destination.setModifiedByUid(null);
			else destination.setModifiedByUid(rs.getInt("destinations_modified_by_uid"));
			if(rs.getInt("destinations_created_by_uid") == 0) destination.setCreatedByUid(null);
			else destination.setCreatedByUid(rs.getInt("destinations_created_by_uid"));
			return destination;
		}
	};

	public static final RowMapper<RichDestination> RICH_DESTINATION_MAPPER = new RowMapper<RichDestination>() {
		public RichDestination mapRow(ResultSet rs, int i) throws SQLException {
			Destination destination = new Destination();
			destination.setId(rs.getInt("destinations_id"));
			destination.setDestination(rs.getString("destinations_destination"));
			destination.setType(rs.getString("destinations_type"));
			destination.setCreatedAt(rs.getString("destinations_created_at"));
			destination.setCreatedBy(rs.getString("destinations_created_by"));
			destination.setModifiedAt(rs.getString("destinations_modified_at"));
			destination.setModifiedBy(rs.getString("destinations_modified_by"));
			if(rs.getInt("destinations_modified_by_uid") == 0) destination.setModifiedByUid(null);
			else destination.setModifiedByUid(rs.getInt("destinations_modified_by_uid"));
			if(rs.getInt("destinations_created_by_uid") == 0) destination.setCreatedByUid(null);
			else destination.setCreatedByUid(rs.getInt("destinations_created_by_uid"));

			Facility facility = new Facility();
			facility.setId(rs.getInt("facilities_id"));
			facility.setName(rs.getString("facilities_name"));
			facility.setCreatedAt(rs.getString("facilities_created_at"));
			facility.setCreatedBy(rs.getString("facilities_created_by"));
			facility.setModifiedAt(rs.getString("facilities_modified_at"));
			facility.setModifiedBy(rs.getString("facilities_modified_by"));
			if(rs.getInt("facilities_modified_by_uid") == 0) facility.setModifiedByUid(null);
			else facility.setModifiedByUid(rs.getInt("facilities_modified_by_uid"));
			if(rs.getInt("facilities_created_by_uid") == 0) facility.setCreatedByUid(null);
			else facility.setCreatedByUid(rs.getInt("facilities_created_by_uid"));

			Service service = new Service();
			service.setId(rs.getInt("services_id"));
			service.setName(rs.getString("services_name"));
			service.setCreatedAt(rs.getString("services_created_at"));
			service.setCreatedBy(rs.getString("services_created_by"));
			service.setModifiedAt(rs.getString("services_modified_at"));
			service.setModifiedBy(rs.getString("services_modified_by"));
			if(rs.getInt("services_modified_by_uid") == 0) service.setModifiedByUid(null);
			else service.setModifiedByUid(rs.getInt("services_modified_by_uid"));
			if(rs.getInt("services_created_by_uid") == 0) service.setCreatedByUid(null);
			else service.setCreatedByUid(rs.getInt("services_created_by_uid"));

			RichDestination richDestination = new RichDestination(destination, facility, service);
			return richDestination;
		}
	};

	public Service createService(PerunSession sess, Service service) throws InternalErrorException {
		try {
			int newId = Utils.getNewId(jdbc, "services_id_seq");

			jdbc.update("insert into services(id,name,created_by,created_at,modified_by,modified_at,created_by_uid, modified_by_uid) " +
					"values (?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", newId, service.getName(),
					sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
			log.info("Service created: {}", service);

			service.setId(newId);

			return service;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void deleteService(PerunSession sess, Service service) throws InternalErrorException, ServiceAlreadyRemovedException {
		try {
			// Delete authz entries for this service
			AuthzResolverBlImpl.removeAllAuthzForService(sess, service);

			int numAffected = jdbc.update("delete from services where id=?", service.getId());
			if(numAffected == 0) throw new ServiceAlreadyRemovedException("Service: " + service);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void updateService(PerunSession sess, Service service) throws InternalErrorException {
		try {
			jdbc.update("update services set name=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + "  where id=?", service.getName(),
					sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), service.getId());
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Service getServiceByName(PerunSession sess, String name) throws InternalErrorException, ServiceNotExistsException {
		try {
			return jdbc.queryForObject("select " + serviceMappingSelectQuery + " from services where name=?", SERVICE_MAPPER, name);
		} catch(EmptyResultDataAccessException ex) {
			throw new ServiceNotExistsException("Service not exists. name=" + name);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Service getServiceById(PerunSession sess, int id) throws InternalErrorException, ServiceNotExistsException {
		try {
			return jdbc.queryForObject("select " + serviceMappingSelectQuery + " from services where id=?", SERVICE_MAPPER, id);
		} catch(EmptyResultDataAccessException ex) {
			throw new ServiceNotExistsException("Service not exists. Id=" + id);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Service> getServices(PerunSession sess) throws InternalErrorException {
		try {
			return jdbc.query("select " + serviceMappingSelectQuery + " from services", SERVICE_MAPPER);
		} catch (EmptyResultDataAccessException ex) {
			log.info("ServicesManager.getAllServices: No service found.");
			return new ArrayList<Service>();
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public List<Service> getServicesByAttributeDefinition(PerunSession sess, AttributeDefinition attributeDefinition) throws InternalErrorException {
		try {
			return jdbc.query("select " + serviceMappingSelectQuery + " from services join service_required_attrs on services.id=service_required_attrs.service_id "
					+ "where service_required_attrs.attr_id=?", SERVICE_MAPPER, attributeDefinition.getId());
		} catch (EmptyResultDataAccessException ex) {
			log.info("ServicesManager.getServicesByAttributeDefinition: No service found.");
			return new ArrayList<Service>();
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Resource> getAssignedResources(PerunSession sess, Service service) throws InternalErrorException {
		try {
			return jdbc.query("select " + ResourcesManagerImpl.resourceMappingSelectQuery + " from resource_services join resources on " +
					"resource_services.resource_id=resources.id  where service_id=?", ResourcesManagerImpl.RESOURCE_MAPPER, service.getId());
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<ServicesPackage> getServicesPackages(PerunSession sess) throws InternalErrorException {
		try {
			List<ServicesPackage> servicesPackages = jdbc.query("select " + servicePackageMappingSelectQuery + " from service_packages", SERVICESPACKAGE_MAPPER);
			return servicesPackages;
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public ServicesPackage getServicesPackageById(PerunSession sess, int servicesPackageId) throws InternalErrorException, ServicesPackageNotExistsException {
		try {
			ServicesPackage servicesPackage = jdbc.queryForObject("select " + servicePackageMappingSelectQuery + " from service_packages where id = ?", SERVICESPACKAGE_MAPPER,  new Integer(servicesPackageId));

			return servicesPackage;
		} catch (EmptyResultDataAccessException ex) {
			throw new ServicesPackageNotExistsException("ServicesPackage with id '" + servicesPackageId + "' desn't exists", ex);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public ServicesPackage createServicesPackage(PerunSession sess, ServicesPackage servicesPackage) throws InternalErrorException {
		try {

			int newId = Utils.getNewId(jdbc, "service_packages_id_seq");

			jdbc.update("insert into service_packages (id, name, description, created_by,created_at,modified_by,modified_at,created_by_uid,modified_by_uid) " +
					"values (?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", newId, servicesPackage.getName(),
					servicesPackage.getDescription(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
			servicesPackage.setId(newId);

			return servicesPackage;
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void updateServicesPackage(PerunSession sess, ServicesPackage servicesPackage) throws InternalErrorException {
		try {
			jdbc.update("update service_packages set description = ?, name = ?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + "  where id = ?",
					servicesPackage.getDescription(), servicesPackage.getName(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), servicesPackage.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void deleteServicesPackage(PerunSession sess, ServicesPackage servicesPackage) throws InternalErrorException {
		try {
			jdbc.update("delete from service_packages where id = ?", servicesPackage.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void addServiceToServicesPackage(PerunSession sess, ServicesPackage servicesPackage, Service service) throws InternalErrorException, ServiceAlreadyAssignedException {
		try {
			jdbc.update("insert into service_service_packages (package_id, service_id, created_by,created_at,modified_by,modified_at,created_by_uid,modified_by_uid) " +
					"values (?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", servicesPackage.getId(), service.getId(),
					sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
		} catch (DuplicateKeyException e) {
			throw new ServiceAlreadyAssignedException("Service with id " + service.getId() + " is already assigned to the service package with id " + servicesPackage.getId(), e);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void removeServiceFromServicesPackage(PerunSession sess, ServicesPackage servicesPackage, Service service) throws InternalErrorException, ServiceAlreadyRemovedFromServicePackageException {
		try {
			int numAffected = jdbc.update("delete from service_service_packages where package_id=? and service_id=?", servicesPackage.getId(), service.getId());
			if(numAffected == 0) throw new ServiceAlreadyRemovedFromServicePackageException("Service: " + service + " , ServicePackage: " + servicesPackage);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public ServicesPackage getServicesPackageByName(PerunSession sess, String name) throws InternalErrorException, ServicesPackageNotExistsException {
		try {
			return jdbc.queryForObject("select " + servicePackageMappingSelectQuery + " from service_packages where name=?", SERVICESPACKAGE_MAPPER, name);
		} catch(EmptyResultDataAccessException ex) {
			throw new ServicesPackageNotExistsException("ServicesPackage not exists. name=" + name);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Service> getServicesFromServicesPackage(PerunSession sess, ServicesPackage servicesPackage) throws InternalErrorException {
		try {
			List<Service> services = new ArrayList<Service>();
			List<Integer> servicesId = jdbc.query("select service_id as id from service_service_packages where package_id=?", Utils.ID_MAPPER, servicesPackage.getId());
			for(Integer serviceId: servicesId) {
				try {
					services.add(getServiceById(sess, serviceId));
				} catch(ServiceNotExistsException ex) {
					throw new InternalErrorException(ex);
				}
			}
			return services;
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public void addRequiredAttribute(PerunSession sess, Service service, AttributeDefinition attribute) throws InternalErrorException, AttributeAlreadyAssignedException {
		try {
			if (0 < jdbc.queryForInt("select count(*) from service_required_attrs where service_id=? and attr_id=?", service.getId(), attribute.getId())) {
				throw new AttributeAlreadyAssignedException("Service: " + service  + " already required attribute " + attribute);
			}
			jdbc.update("insert into service_required_attrs(service_id, attr_id, created_by,created_at,modified_by,modified_at,created_by_uid,modified_by_uid) " +
					"values (?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", service.getId(), attribute.getId(),
					sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
		} catch(RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void addRequiredAttributes(PerunSession sess, Service service, List<? extends AttributeDefinition> attributes) throws InternalErrorException, AttributeAlreadyAssignedException {
		for(AttributeDefinition attribute : attributes) addRequiredAttribute(sess, service, attribute);
	}

	public void removeRequiredAttribute(PerunSession sess, Service service, AttributeDefinition attribute) throws InternalErrorException, AttributeNotAssignedException {
		try {
			if(0 == jdbc.update("delete from service_required_attrs where service_id=? and attr_id=?", service.getId(), attribute.getId())) {
				throw new AttributeNotAssignedException(attribute);
			}
		} catch(RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void removeRequiredAttributes(PerunSession sess, Service service, List<? extends AttributeDefinition> attributes) throws InternalErrorException, AttributeNotAssignedException {
		for(AttributeDefinition attribute : attributes) removeRequiredAttribute(sess, service, attribute);
	}

	public void removeAllRequiredAttributes(PerunSession sess, Service service) throws InternalErrorException {
		try {
			jdbc.update("delete from service_required_attrs where service_id=?", service.getId());
		} catch(RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public boolean serviceExists(PerunSession sess, Service service) throws InternalErrorException {
		try {
			return 1 == jdbc.queryForInt("select 1 from services where id=?", service.getId());
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

	}

	public void checkServiceExists(PerunSession sess, Service service) throws InternalErrorException, ServiceNotExistsException {
		if(!serviceExists(sess, service)) throw new ServiceNotExistsException("Service not exists: " + service);
	}

	public void checkServicesPackageExists(PerunSession sess, ServicesPackage servicesPackage) throws InternalErrorException, ServicesPackageNotExistsException {
		if(!servicesPackageExists(sess, servicesPackage)) throw new ServicesPackageNotExistsException("ServicesPackage not exists: " + servicesPackage);
	}

	public boolean servicesPackageExists(PerunSession sess, ServicesPackage servicesPackage) throws InternalErrorException {
		try {
			return 1 == jdbc.queryForInt("select 1 from service_packages where id=?", servicesPackage.getId());
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

	}

	public Destination getDestinationById(PerunSession sess, int id) throws InternalErrorException, DestinationNotExistsException {
		try {
			return jdbc.queryForObject("select " + destinationMappingSelectQuery + " from destinations where id=?", DESTINATION_MAPPER, id);
		} catch (EmptyResultDataAccessException e) {
			throw new DestinationNotExistsException("Destination.id=" + id, e);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Deprecated
	public int getDestinationIdByName(PerunSession sess, String name) throws InternalErrorException, DestinationNotExistsException {
		try {
			return jdbc.queryForInt("select id from destinations where destination=?", name);
		} catch (EmptyResultDataAccessException e) {
			throw new DestinationNotExistsException(e);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void addDestination(PerunSession sess, Service service, Facility facility, Destination destination) throws InternalErrorException {
		try {
			jdbc.update("insert into facility_service_destinations (service_id, facility_id, destination_id, created_by,created_at,modified_by,modified_at,created_by_uid, modified_by_uid) " +
					"values (?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", service.getId(), facility.getId(), destination.getId(),
					sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void removeDestination(PerunSession sess, Service service,  Facility facility, Destination destination) throws InternalErrorException, DestinationAlreadyRemovedException {
		try {
			if (0 == jdbc.update("delete from facility_service_destinations where service_id=? and facility_id=? and destination_id=?", service.getId(), facility.getId(), destination.getId())) {
				throw new DestinationAlreadyRemovedException(destination);
			}
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public List<Destination> getDestinations(PerunSession sess, Service service, Facility facility) throws InternalErrorException {
		try {
			return jdbc.query("select " + facilityDestinationMappingSelectQuery + " from facility_service_destinations join destinations on destinations.id=facility_service_destinations.destination_id " +
					"where service_id=? and facility_id=? order by destinations.destination", DESTINATION_MAPPER, service.getId(), facility.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Destination> getDestinations(PerunSession perunSession) throws InternalErrorException {
		try {
			return jdbc.query("select " + destinationMappingSelectQuery + " from destinations", DESTINATION_MAPPER);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public List<Destination> getDestinations(PerunSession perunSession, Facility facility) throws InternalErrorException {
		try {
			return jdbc.query("select " + destinationMappingSelectQuery + " from facility_service_destinations " +
					"join destinations on destinations.id=facility_service_destinations.destination_id " +
					"where facility_id=? order by destinations.destination", DESTINATION_MAPPER, facility.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public List<RichDestination> getAllRichDestinations(PerunSession perunSession, Facility facility) throws InternalErrorException {
		try {
			return jdbc.query("select " + richDestinationMappingSelectQuery + " from facility_service_destinations " +
					"join destinations on destinations.id=facility_service_destinations.destination_id " +
					"join services on services.id=facility_service_destinations.service_id " +
					"join facilities on facilities.id=facility_service_destinations.facility_id " +
					"where facility_id=? order by destinations.destination", RICH_DESTINATION_MAPPER, facility.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public List<RichDestination> getAllRichDestinations(PerunSession perunSession, Service service) throws InternalErrorException {
		try {
			return jdbc.query("select " + richDestinationMappingSelectQuery + " from facility_service_destinations " +
					"join destinations on destinations.id=facility_service_destinations.destination_id " +
					"join services on services.id=facility_service_destinations.service_id " +
					"join facilities on facilities.id=facility_service_destinations.facility_id " +
					"where service_id=? order by destinations.destination", RICH_DESTINATION_MAPPER, service.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public List<RichDestination> getRichDestinations(PerunSession perunSession, Facility facility, Service service) throws InternalErrorException{
		try {
			return jdbc.query("select " + richDestinationMappingSelectQuery + " from facility_service_destinations " +
					"join destinations on destinations.id=facility_service_destinations.destination_id " +
					"join services on services.id=facility_service_destinations.service_id " +
					"join facilities on facilities.id=facility_service_destinations.facility_id " +
					"where facility_id=? and service_id=? order by destinations.destination", RICH_DESTINATION_MAPPER, facility.getId(), service.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}


	public void removeAllDestinations(PerunSession sess, Service service, Facility facility) throws InternalErrorException {
		try {
			jdbc.update("delete from facility_service_destinations where service_id=? and facility_id=?", service.getId(), facility.getId());
			//TODO remove from table destinations?
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public void removeAllDestinations(PerunSession sess, Facility facility) throws InternalErrorException {
		try {
			jdbc.update("delete from facility_service_destinations where facility_id=?", facility.getId());
			//TODO remove from table destinations?
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public boolean destinationExists(PerunSession sess, Service service, Facility facility, Destination destination) throws InternalErrorException {
		try {
			return 1 == jdbc.queryForInt("select 1 from facility_service_destinations fsd join destinations d on fsd.destination_id = d.id where fsd.service_id=? and fsd.facility_id=? and d.destination=? and d.type=?", service.getId(), facility.getId(), destination.getDestination(), destination.getType());
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public boolean destinationExists(PerunSession sess, Destination destination) throws InternalErrorException {
		try {
			return 1 == jdbc.queryForInt("select 1 from destinations where id=? and destination=? and type=?", destination.getId(), destination.getDestination(), destination.getType());
		} catch(EmptyResultDataAccessException ex) {
			return false;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Service> getAssignedServices(PerunSession perunSession, Facility facility) throws InternalErrorException {
		try {
			return jdbc.query("select distinct " + serviceMappingSelectQuery + " from services join resource_services on services.id = resource_services.service_id join resources on resource_services.resource_id = resources.id where resources.facility_id=?", SERVICE_MAPPER, facility.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public Destination getDestination(PerunSession sess, String destination, String type) throws InternalErrorException, DestinationNotExistsException {
		try {
			return jdbc.queryForObject("select " + destinationMappingSelectQuery + " from destinations where destination=? and type=?", DESTINATION_MAPPER, destination, type);
		} catch(EmptyResultDataAccessException ex) {
			throw new DestinationNotExistsException(ex);
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public Destination createDestination(PerunSession sess, Destination destination) throws InternalErrorException {
		try {
			int destinationId = Utils.getNewId(jdbc, "destinations_id_seq");
			jdbc.update("insert into destinations (id, destination, type, created_by,created_at,modified_by,modified_at,created_by_uid, modified_by_uid) " +
					"values (?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", destinationId, destination.getDestination(), destination.getType(),
					sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
			destination.setId(destinationId);
			return destination;
		} catch(RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	public List<Destination> getFacilitiesDestinations(PerunSession sess, Vo vo) throws InternalErrorException {
		try {
			return jdbc.query("select distinct " + destinationMappingSelectQuery + " from destinations, facility_service_destinations, facilities, resources, vos " +
					"where destinations.id = facility_service_destinations.destination_id and facilities.id = facility_service_destinations.facility_id " +
					"and facilities.id = resources.facility_id and resources.vo_id = ?", DESTINATION_MAPPER, vo.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	public int getDestinationsCount(PerunSession sess) throws InternalErrorException {
		try {
			return jdbc.queryForInt("select count(*) from destinations");
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}
}
