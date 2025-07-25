package cz.metacentrum.perun.core.impl;

import static cz.metacentrum.perun.core.impl.FacilitiesManagerImpl.FACILITY_MAPPER;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichDestination;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServiceDenial;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyBannedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import cz.metacentrum.perun.core.implApi.ServicesManagerImplApi;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * @author Michal Prochazka <michalp@ics.muni.cz>
 * @author Slavek Licehammer <glory@ics.muni.cz>
 */
public class ServicesManagerImpl implements ServicesManagerImplApi {


  public static final String SERVICE_MAPPING_SELECT_QUERY =
      " services.id as services_id, services.name as services_name, " +
      "services.description as services_description, services.delay as services_delay, services.recurrence as " +
      "services_recurrence, " +
      "services.enabled as services_enabled, services.script as services_script, services.use_expired_members as " +
      "services_use_expired_members, " + "services.use_expired_vo_members as services_use_expired_vo_members, " +
      "services.use_banned_members as services_use_banned_members, " +
      "services.created_at as services_created_at, services.created_by as services_created_by, " +
      "services.modified_by as services_modified_by, services.modified_at as services_modified_at, " +
      "services.created_by_uid as services_created_by_uid, services.modified_by_uid as services_modified_by_uid";
  public static final String SERVICE_DENIAL_MAPPING_SELECT_QUERY = " service_denials.id as service_denials_id, " +
                                                               "service_denials.service_id as " +
                                                               "service_denials_service_id, service_denials" +
                                                               ".facility_id as " +
                                                               "service_denials_facility_id, " +
                                                               "service_denials.destination_id as " +
                                                               "service_denials_destination_id, " +
                                                               "service_denials.created_at as " +
                                                               "service_denials_created_at, service_denials" +
                                                               ".created_by as " +
                                                               "service_denials_created_by, " +
                                                               "service_denials.modified_by as " +
                                                               "service_denials_modified_by, service_denials" +
                                                               ".modified_at as " +
                                                               "service_denials_modified_at, " +
                                                               "service_denials.created_by_uid as " +
                                                               "service_denials_created_by_uid, service_denials" +
                                                               ".modified_by_uid as " +
                                                               "service_denials_modified_by_uid";
  public static final String DESTINATION_MAPPING_SELECT_QUERY =
      " destinations.id as destinations_id, destinations.destination as destinations_destination, " +
      "destinations.type as destinations_type, destinations.created_at as destinations_created_at, destinations" +
      ".created_by as destinations_created_by, " +
      "destinations.modified_by as destinations_modified_by, destinations.modified_at as " +
      "destinations_modified_at, " +
      "destinations.modified_by_uid as destinations_modified_by_uid, destinations.created_by_uid as " +
      "destinations_created_by_uid";
  public static final String FACILITY_DESTINATION_MAPPING_SELECT_QUERY = DESTINATION_MAPPING_SELECT_QUERY +
                                     ", facility_service_destinations.propagation_type as f_s_des_propagation_type ";
  public static final String RICH_DESTINATION_MAPPING_SELECT_QUERY = " " + DESTINATION_MAPPING_SELECT_QUERY + ", " +
                                                                 "facilities.id as facilities_id, facilities.uu_id as" +
                                                                 " facilities_uu_id, facilities.name as " +
                                                                 "facilities_name, " +
                                                                 "facilities.dsc as facilities_dsc, " +
                                                                 "facilities.created_at as facilities_created_at, " +
                                                                 "facilities.created_by as facilities_created_by, " +
                                                                 "facilities" +
                                                                 ".modified_at as facilities_modified_at, facilities" +
                                                                 ".modified_by as facilities_modified_by, " +
                                                                 "facilities.modified_by_uid as " +
                                                                 "facilities_modified_by_uid, facilities" +
                                                                 ".created_by_uid as " +
                                                                 "facilities_created_by_uid, " +
                                                                     SERVICE_MAPPING_SELECT_QUERY + ", " +
                                                                     SERVICE_DENIAL_MAPPING_SELECT_QUERY + ", " +
                                                                 "facility_service_destinations.propagation_type as " +
                                                                 "f_s_des_propagation_type ";
  public static final String RICH_DESTINATION_WITH_LAST_PROPAGATION_MAPPING_SELECT_QUERY =
      " " + DESTINATION_MAPPING_SELECT_QUERY + ", " +
      "facilities.id as facilities_id, facilities.uu_id as facilities_uu_id, facilities.name as facilities_name, " +
      "facilities.dsc as facilities_dsc, " +
      "facilities.created_at as facilities_created_at, facilities.created_by as facilities_created_by, facilities" +
      ".modified_at as facilities_modified_at, facilities.modified_by as facilities_modified_by, " +
      "facilities.modified_by_uid as facilities_modified_by_uid, facilities.created_by_uid as " +
      "facilities_created_by_uid, " + SERVICE_MAPPING_SELECT_QUERY + ", " + SERVICE_DENIAL_MAPPING_SELECT_QUERY + ", " +
      "facility_service_destinations.propagation_type as f_s_des_propagation_type, " +
      "last_propagation.success_at as success_at, " +
      "last_propagation.attempt_at as attempt_at";
  public static final RowMapper<Service> SERVICE_MAPPER = (resultSet, i) -> {
    Service service = new Service();
    service.setId(resultSet.getInt("services_id"));
    service.setName(resultSet.getString("services_name"));
    service.setDescription(resultSet.getString("services_description"));
    service.setDelay(resultSet.getInt("services_delay"));
    service.setRecurrence(resultSet.getInt("services_recurrence"));
    service.setEnabled(resultSet.getBoolean("services_enabled"));
    service.setScript(resultSet.getString("services_script"));
    service.setUseExpiredMembers(resultSet.getBoolean("services_use_expired_members"));
    service.setUseExpiredVoMembers(resultSet.getBoolean("services_use_expired_vo_members"));
    service.setUseBannedMembers(resultSet.getBoolean("services_use_banned_members"));
    service.setCreatedAt(resultSet.getString("services_created_at"));
    service.setCreatedBy(resultSet.getString("services_created_by"));
    service.setModifiedAt(resultSet.getString("services_modified_at"));
    service.setModifiedBy(resultSet.getString("services_modified_by"));
    if (resultSet.getInt("services_modified_by_uid") == 0) {
      service.setModifiedByUid(null);
    } else {
      service.setModifiedByUid(resultSet.getInt("services_modified_by_uid"));
    }
    if (resultSet.getInt("services_created_by_uid") == 0) {
      service.setCreatedByUid(null);
    } else {
      service.setCreatedByUid(resultSet.getInt("services_created_by_uid"));
    }
    return service;

  };
  public static final RowMapper<ServiceDenial> SERVICE_DENIAL_MAPPER = (resultSet, i) -> {
    if (resultSet.getInt("service_denials_id") == 0) {
      return null;
    }
    ServiceDenial serviceDenial = new ServiceDenial();
    serviceDenial.setId(resultSet.getInt("service_denials_id"));
    serviceDenial.setFacilityId(resultSet.getInt("service_denials_facility_id"));
    serviceDenial.setDestinationId(resultSet.getInt("service_denials_destination_id"));
    serviceDenial.setServiceId(resultSet.getInt("service_denials_service_id"));
    serviceDenial.setCreatedAt(resultSet.getString("service_denials_created_at"));
    serviceDenial.setCreatedBy(resultSet.getString("service_denials_created_by"));
    serviceDenial.setModifiedAt(resultSet.getString("service_denials_modified_at"));
    serviceDenial.setModifiedBy(resultSet.getString("service_denials_modified_by"));
    if (resultSet.getInt("service_denials_modified_by_uid") == 0) {
      serviceDenial.setModifiedByUid(null);
    } else {
      serviceDenial.setModifiedByUid(resultSet.getInt("service_denials_modified_by_uid"));
    }
    if (resultSet.getInt("service_denials_created_by_uid") == 0) {
      serviceDenial.setCreatedByUid(null);
    } else {
      serviceDenial.setCreatedByUid(resultSet.getInt("service_denials_created_by_uid"));
    }
    return serviceDenial;
  };
  public static final RowMapper<Destination> DESTINATION_MAPPER = (resultSet, i) -> {
    Destination destination = new Destination();
    destination.setId(resultSet.getInt("destinations_id"));
    destination.setDestination(resultSet.getString("destinations_destination"));
    destination.setType(resultSet.getString("destinations_type"));
    destination.setCreatedAt(resultSet.getString("destinations_created_at"));
    destination.setCreatedBy(resultSet.getString("destinations_created_by"));
    destination.setModifiedAt(resultSet.getString("destinations_modified_at"));
    destination.setModifiedBy(resultSet.getString("destinations_modified_by"));
    try { // do not mind if the column is not in the results
      String ptype = resultSet.getString("f_s_des_propagation_type");
      if (ptype.equals(Destination.PROPAGATIONTYPE_SERIAL) || ptype.equals(Destination.PROPAGATIONTYPE_PARALLEL) ||
          ptype.equals(Destination.PROPAGATIONTYPE_DUMMY)) {
        destination.setPropagationType(ptype);
      } else {
        destination.setPropagationType(Destination.PROPAGATIONTYPE_PARALLEL);
      }
    } catch (SQLException e) {
      destination.setPropagationType(Destination.PROPAGATIONTYPE_PARALLEL);
    }
    if (resultSet.getInt("destinations_modified_by_uid") == 0) {
      destination.setModifiedByUid(null);
    } else {
      destination.setModifiedByUid(resultSet.getInt("destinations_modified_by_uid"));
    }
    if (resultSet.getInt("destinations_created_by_uid") == 0) {
      destination.setCreatedByUid(null);
    } else {
      destination.setCreatedByUid(resultSet.getInt("destinations_created_by_uid"));
    }
    return destination;
  };
  public static final RowMapper<RichDestination> RICH_DESTINATION_MAPPER = (resultSet, i) -> {
    Destination destination = new Destination();
    destination.setId(resultSet.getInt("destinations_id"));
    destination.setDestination(resultSet.getString("destinations_destination"));
    destination.setType(resultSet.getString("destinations_type"));
    destination.setCreatedAt(resultSet.getString("destinations_created_at"));
    destination.setCreatedBy(resultSet.getString("destinations_created_by"));
    destination.setModifiedAt(resultSet.getString("destinations_modified_at"));
    destination.setModifiedBy(resultSet.getString("destinations_modified_by"));
    try { // do not mind if the column is not in the results
      String ptype = resultSet.getString("f_s_des_propagation_type");
      if (ptype.equals(Destination.PROPAGATIONTYPE_SERIAL) || ptype.equals(Destination.PROPAGATIONTYPE_PARALLEL) ||
          ptype.equals(Destination.PROPAGATIONTYPE_DUMMY)) {
        destination.setPropagationType(ptype);
      } else {
        destination.setPropagationType(Destination.PROPAGATIONTYPE_PARALLEL);
      }
    } catch (SQLException e) {
      destination.setPropagationType(Destination.PROPAGATIONTYPE_PARALLEL);
    }
    if (resultSet.getInt("destinations_modified_by_uid") == 0) {
      destination.setModifiedByUid(null);
    } else {
      destination.setModifiedByUid(resultSet.getInt("destinations_modified_by_uid"));
    }
    if (resultSet.getInt("destinations_created_by_uid") == 0) {
      destination.setCreatedByUid(null);
    } else {
      destination.setCreatedByUid(resultSet.getInt("destinations_created_by_uid"));
    }

    Facility facility = FACILITY_MAPPER.mapRow(resultSet, i);

    Service service = SERVICE_MAPPER.mapRow(resultSet, i);

    ServiceDenial serviceDenial = SERVICE_DENIAL_MAPPER.mapRow(resultSet, i);

    // if success_at or attempt_at column is missing in results, use null value
    Timestamp lastSuccessfulPropagation;
    Timestamp lastAttemptedPropagation;
    try {
      lastSuccessfulPropagation = resultSet.getTimestamp("success_at");
    } catch (SQLException ex) {
      lastSuccessfulPropagation = null;
    }
    try {
      lastAttemptedPropagation = resultSet.getTimestamp("attempt_at");
    } catch (SQLException ex) {
      lastAttemptedPropagation = null;
    }

    return new RichDestination(destination, facility, service, serviceDenial != null,
                               lastSuccessfulPropagation, lastAttemptedPropagation);
  };
  static final Logger LOG = LoggerFactory.getLogger(ServicesManagerImpl.class);
  // http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/jdbc.html
  private static JdbcPerunTemplate jdbc;

  public ServicesManagerImpl(DataSource perunPool) {
    jdbc = new JdbcPerunTemplate(perunPool);
    jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
  }

  @Override
  public void addDestination(PerunSession sess, Service service, Facility facility, Destination destination)
      throws DestinationAlreadyAssignedException {
    try {
      jdbc.update(
          "insert into facility_service_destinations (service_id, facility_id, destination_id, propagation_type, " +
          "created_by,created_at,modified_by,modified_at,created_by_uid, modified_by_uid) " + "values (?,?,?,?,?," +
          Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", service.getId(), facility.getId(),
          destination.getId(), destination.getPropagationType(), sess.getPerunPrincipal().getActor(),
          sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(),
          sess.getPerunPrincipal().getUserId());
    } catch (DuplicateKeyException e) {
      throw new DestinationAlreadyAssignedException(
          "combination of  facility: " + facility.getName() + " service: " + service.getName() + " destination: " +
          destination.getDestination() + " is not unique.");
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void addRequiredAttribute(PerunSession sess, Service service, AttributeDefinition attribute)
      throws AttributeAlreadyAssignedException {
    try {
      if (0 < jdbc.queryForInt("select count(*) from service_required_attrs where service_id=? and attr_id=?",
          service.getId(), attribute.getId())) {
        throw new AttributeAlreadyAssignedException("Service: " + service + " already required attribute " + attribute);
      }
      jdbc.update(
          "insert into service_required_attrs(service_id, attr_id, created_by,created_at,modified_by,modified_at," +
          "created_by_uid,modified_by_uid) " + "values (?,?,?," + Compatibility.getSysdate() + ",?," +
          Compatibility.getSysdate() + ",?,?)", service.getId(), attribute.getId(), sess.getPerunPrincipal().getActor(),
          sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(),
          sess.getPerunPrincipal().getUserId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void addRequiredAttributes(PerunSession sess, Service service, List<? extends AttributeDefinition> attributes)
      throws AttributeAlreadyAssignedException {
    for (AttributeDefinition attribute : attributes) {
      addRequiredAttribute(sess, service, attribute);
    }
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public void blockServiceOnDestination(PerunSession sess, int serviceId, int destinationId)
      throws ServiceAlreadyBannedException {
    try {
      int newBanId = Utils.getNewId(jdbc, "service_denials_id_seq");
      jdbc.update(
          "insert into service_denials(id, destination_id, service_id, created_by, modified_by, created_by_uid, " +
          "modified_by_uid) values (?,?,?,?,?,?,?)", newBanId, destinationId, serviceId,
          sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(),
          sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
    } catch (DuplicateKeyException ex) {
      throw new ServiceAlreadyBannedException(
          String.format("Service with id %d is already banned on the destination with id %d", serviceId,
              destinationId));
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public void blockServiceOnFacility(PerunSession sess, int serviceId, int facilityId)
      throws ServiceAlreadyBannedException {
    int newBanId = Utils.getNewId(jdbc, "service_denials_id_seq");
    try {
      jdbc.update("insert into service_denials(id, facility_id, service_id, created_by, modified_by, created_by_uid, " +
                  "modified_by_uid) values (?,?,?,?,?,?,?)", newBanId, facilityId, serviceId,
          sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(),
          sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
    } catch (DuplicateKeyException ex) {
      throw new ServiceAlreadyBannedException(
          String.format("Service with id %d is already banned on the facility with id %d", serviceId, facilityId));
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public void checkServiceExists(PerunSession sess, Service service) throws ServiceNotExistsException {
    if (!serviceExists(sess, service)) {
      throw new ServiceNotExistsException("Service not exists: " + service);
    }
  }

  @Override
  public Destination createDestination(PerunSession sess, Destination destination) {
    Utils.checkDestination(destination);
    try {
      int destinationId = Utils.getNewId(jdbc, "destinations_id_seq");
      jdbc.update("insert into destinations (id, destination, type, created_by,created_at,modified_by,modified_at," +
                  "created_by_uid, modified_by_uid) " + "values (?,?,?,?," + Compatibility.getSysdate() + ",?," +
                  Compatibility.getSysdate() + ",?,?)", destinationId, destination.getDestination(),
          destination.getType(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(),
          sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
      destination.setId(destinationId);
      return destination;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Service createService(PerunSession sess, Service service) {
    try {
      int newId = Utils.getNewId(jdbc, "services_id_seq");
      // if not set, make sure script path is set based on service name
      if (service.getScript() == null || service.getScript().isEmpty()) {
        service.setScript("./" + service.getName());
      }
      jdbc.update(
          "insert into services(id,name,description,delay,recurrence,enabled,script,use_expired_members," +
              "use_expired_vo_members, use_banned_members, created_by,created_at,modified_by,modified_at," +
              "created_by_uid, modified_by_uid) " +
              "values (?,?,?,?,?,?,?,?,?,?,?," +
          Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", newId, service.getName(),
          service.getDescription(), service.getDelay(), service.getRecurrence(), service.isEnabled(),
          service.getScript(), service.isUseExpiredMembers(), service.isUseExpiredVoMembers(),
          service.isUseBannedMembers(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(),
          sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());

      service.setId(newId);

      return service;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public void deleteDestination(PerunSession sess, Destination destination)
      throws DestinationAlreadyRemovedException, RelationExistsException {
    try {
      if (0 == jdbc.update("delete from destinations where id = ?", destination.getId())) {
        throw new DestinationAlreadyRemovedException("Destination: " + destination);
      }
    } catch (DataIntegrityViolationException ex) {
      throw new RelationExistsException("Destination: " + destination + " has existing relations.", ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public void deleteService(PerunSession sess, Service service) throws ServiceAlreadyRemovedException {
    try {
      // Delete authz entries for this service
      AuthzResolverBlImpl.removeAllAuthzForService(sess, service);

      int numAffected = jdbc.update("delete from services where id=?", service.getId());
      if (numAffected == 0) {
        throw new ServiceAlreadyRemovedException("Service: " + service);
      }
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public boolean destinationExists(PerunSession sess, Service service, Facility facility, Destination destination) {
    try {
      int numberOfExistences = jdbc.queryForInt(
          "select count(1) from facility_service_destinations fsd join destinations d on fsd.destination_id = d.id " +
          "where fsd.service_id=? and fsd.facility_id=? and d.destination=? and d.type=?", service.getId(),
          facility.getId(), destination.getDestination(), destination.getType());
      if (numberOfExistences == 1) {
        return true;
      } else if (numberOfExistences > 1) {
        throw new ConsistencyErrorException(
            "Destination " + destination + " of service " + service + " and facility " + facility +
            " exists more than once.");
      }
      return false;
    } catch (EmptyResultDataAccessException ex) {
      return false;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public boolean destinationExists(PerunSession sess, Destination destination) {
    try {
      int numberOfExistences =
          jdbc.queryForInt("select count(1) from destinations where id=? and destination=? and type=?",
              destination.getId(), destination.getDestination(), destination.getType());
      if (numberOfExistences == 1) {
        return true;
      } else if (numberOfExistences > 1) {
        throw new ConsistencyErrorException("Destination " + destination + " exists more than once.");
      }
      return false;
    } catch (EmptyResultDataAccessException ex) {
      return false;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<RichDestination> getAllRichDestinations(PerunSession perunSession, Facility facility) {
    try {
      return jdbc.query("select " + RICH_DESTINATION_WITH_LAST_PROPAGATION_MAPPING_SELECT_QUERY +
                        " from facility_service_destinations " +
                        "join destinations on destinations.id=facility_service_destinations.destination_id " +
                        "join services on services.id=facility_service_destinations.service_id " +
                        "join facilities on facilities.id=facility_service_destinations.facility_id " +
                        "left join service_denials on services.id = service_denials.service_id and " +
                        "                        destinations.id = service_denials.destination_id " +

                        "left join (select destination_id, services.id as service_id, facilities.id as facility_id, " +
                        "max(timestamp) as attempt_at, " +
                        "max(case when status = 'DONE' then timestamp end) as success_at from tasks_results " +
                        "join services on services.id = (select service_id from tasks where id = tasks_results" +
                        ".task_id) " +
                        "join facilities on facilities.id = (select facility_id from tasks where id = tasks_results" +
                        ".task_id) " +
                        "group by destination_id, services.id, facilities.id) as last_propagation " +
                        "on last_propagation.destination_id = facility_service_destinations.destination_id and" +
                        " last_propagation.service_id = services.id and last_propagation.facility_id = facilities.id " +

                        "where facility_service_destinations.facility_id=? order by destinations.destination",
          RICH_DESTINATION_MAPPER, facility.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<RichDestination> getAllRichDestinations(PerunSession perunSession, Service service) {
    try {
      return jdbc.query("select " + RICH_DESTINATION_MAPPING_SELECT_QUERY + " from facility_service_destinations " +
                        "join destinations on destinations.id=facility_service_destinations.destination_id " +
                        "join services on services.id=facility_service_destinations.service_id " +
                        "join facilities on facilities.id=facility_service_destinations.facility_id " +
                        "left join service_denials on services.id = service_denials.service_id and " +
                        "                        destinations.id = service_denials.destination_id " +
                        "where facility_service_destinations.service_id=? " + "order by destinations.destination",
          RICH_DESTINATION_MAPPER, service.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<Resource> getAssignedResources(PerunSession sess, Service service) {
    try {
      return jdbc.query("select " + ResourcesManagerImpl.RESOURCE_MAPPING_SELECT_QUERY +
                        " from resource_services join resources on " +
                        "resource_services.resource_id=resources.id  where service_id=?",
          ResourcesManagerImpl.RESOURCE_MAPPER, service.getId());
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Service> getAssignedServices(PerunSession perunSession, Facility facility) {
    try {
      return jdbc.query("select distinct " + SERVICE_MAPPING_SELECT_QUERY +
                        " from services join resource_services on services.id = resource_services.service_id join " +
                        "resources on " +
                        "resource_services.resource_id = resources.id where resources.facility_id=?", SERVICE_MAPPER,
          facility.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<Service> getAssignedServices(PerunSession perunSession, Facility facility, Vo vo) {
    try {
      return jdbc.query("select distinct " + SERVICE_MAPPING_SELECT_QUERY +
                        " from services join resource_services on services.id = resource_services.service_id join " +
                        "resources on " +
                        "resource_services.resource_id = resources.id where resources.facility_id=? and resources" +
                        ".vo_id=?",
          SERVICE_MAPPER, facility.getId(), vo.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public Destination getDestination(PerunSession sess, String destination, String type)
      throws DestinationNotExistsException {
    try {
      return jdbc.queryForObject(
          "select " + DESTINATION_MAPPING_SELECT_QUERY + " from destinations where destination=? and type=?",
          DESTINATION_MAPPER, destination, type);
    } catch (EmptyResultDataAccessException ex) {
      throw new DestinationNotExistsException(ex);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Destination getDestinationById(PerunSession sess, int id) throws DestinationNotExistsException {
    try {
      return jdbc.queryForObject("select " + DESTINATION_MAPPING_SELECT_QUERY + " from destinations where id=?",
          DESTINATION_MAPPER, id);
    } catch (EmptyResultDataAccessException e) {
      throw new DestinationNotExistsException("Destination.id=" + id, e);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<Destination> getDestinations(PerunSession sess, Service service, Facility facility) {
    try {
      return jdbc.query("select " + FACILITY_DESTINATION_MAPPING_SELECT_QUERY +
                        " from facility_service_destinations join destinations on destinations" +
                        ".id=facility_service_destinations" +
                        ".destination_id " + "where service_id=? and facility_id=? order by destinations.destination",
          DESTINATION_MAPPER, service.getId(), facility.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<Destination> getDestinations(PerunSession perunSession) {
    try {
      return jdbc.query("select " + DESTINATION_MAPPING_SELECT_QUERY + " from destinations", DESTINATION_MAPPER);
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<Destination> getDestinations(PerunSession perunSession, Facility facility) {
    try {
      return jdbc.query("select " + FACILITY_DESTINATION_MAPPING_SELECT_QUERY + " from facility_service_destinations " +
                        "join destinations on destinations.id=facility_service_destinations.destination_id " +
                        "where facility_id=? order by destinations.destination", DESTINATION_MAPPER, facility.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public int getDestinationsCount(PerunSession sess) {
    try {
      return jdbc.queryForInt("select count(*) from destinations");
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Destination> getFacilitiesDestinations(PerunSession sess, Vo vo) {
    try {
      return jdbc.query("select distinct " + DESTINATION_MAPPING_SELECT_QUERY +
                        " from destinations, facility_service_destinations, facilities, resources " +
                        "where destinations.id = facility_service_destinations.destination_id and facilities.id = " +
                        "facility_service_destinations.facility_id " +
                        "and facilities.id = resources.facility_id and resources.vo_id = ?", DESTINATION_MAPPER,
          vo.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public List<RichDestination> getRichDestinations(PerunSession perunSession, Facility facility, Service service) {
    try {
      return jdbc.query("select " + RICH_DESTINATION_MAPPING_SELECT_QUERY + " from facility_service_destinations " +
                        "join destinations on destinations.id=facility_service_destinations.destination_id " +
                        "join services on services.id=facility_service_destinations.service_id " +
                        "join facilities on facilities.id=facility_service_destinations.facility_id " +
                        "left join service_denials on services.id = service_denials.service_id and " +
                        "                        destinations.id = service_denials.destination_id " +
                        "where facility_service_destinations.facility_id=? and facility_service_destinations" +
                        ".service_id=? " +
                        "order by destinations.destination", RICH_DESTINATION_MAPPER, facility.getId(),
          service.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public Service getServiceById(PerunSession sess, int id) throws ServiceNotExistsException {
    try {
      return jdbc.queryForObject("select " + SERVICE_MAPPING_SELECT_QUERY + " from services where id=?", SERVICE_MAPPER,
          id);
    } catch (EmptyResultDataAccessException ex) {
      throw new ServiceNotExistsException("Service not exists. Id=" + id);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public Service getServiceByName(PerunSession sess, String name) throws ServiceNotExistsException {
    try {
      return jdbc.queryForObject("select " + SERVICE_MAPPING_SELECT_QUERY +
                                     " from services where name=?", SERVICE_MAPPER,
          name);
    } catch (EmptyResultDataAccessException ex) {
      throw new ServiceNotExistsException("Service not exists. name=" + name);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Service> getServices(PerunSession sess) {
    try {
      return jdbc.query("select " + SERVICE_MAPPING_SELECT_QUERY + " from services", SERVICE_MAPPER);
    } catch (EmptyResultDataAccessException ex) {
      LOG.info("ServicesManager.getAllServices: No service found.");
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public List<Service> getServicesBlockedOnDestination(int destinationId) {
    try {
      return jdbc.query("select " + ServicesManagerImpl.SERVICE_MAPPING_SELECT_QUERY +
                        " from services left join service_denials on service_denials.service_id = services.id where " +
                        "service_denials.destination_id = ?", ServicesManagerImpl.SERVICE_MAPPER, destinationId);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public List<Service> getServicesBlockedOnFacility(int facilityId) {
    try {
      return jdbc.query("select " + ServicesManagerImpl.SERVICE_MAPPING_SELECT_QUERY +
                        " from services left join service_denials on service_denials.service_id = services.id where " +
                        "service_denials.facility_id = ?", ServicesManagerImpl.SERVICE_MAPPER, facilityId);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Service> getServicesByAttributeDefinition(PerunSession sess, AttributeDefinition attributeDefinition) {
    try {
      return jdbc.query("select " + SERVICE_MAPPING_SELECT_QUERY +
                        " from services join service_required_attrs on services.id=service_required_attrs.service_id " +
                        "where service_required_attrs.attr_id=?", SERVICE_MAPPER, attributeDefinition.getId());
    } catch (EmptyResultDataAccessException ex) {
      LOG.info("ServicesManager.getServicesByAttributeDefinition: No service found.");
      return new ArrayList<>();
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public List<Service> getServicesFromDestination(int destinationId) {
    try {
      @SuppressWarnings("ConstantConditions") List<Service> servicesFromDestination = jdbc.query(
          "select distinct " + ServicesManagerImpl.SERVICE_MAPPING_SELECT_QUERY +
          " from services join facility_service_destinations on facility_service_destinations.service_id = " +
          "services.id" + " where facility_service_destinations.destination_id = ?", ServicesManagerImpl.SERVICE_MAPPER,
          destinationId);
      return servicesFromDestination;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public boolean isServiceAssignedToFacility(PerunSession sess, Facility facility, Service service) {
    try {
      int count = jdbc.queryForInt(
          "select count(1) " + "from resources" + " join (select * " + "from resource_services " +
          "where resource_services.service_id = ?) as resource_services on resource_services.resource_id = " +
          "resources.id" + " where resources.facility_id=?", service.getId(), facility.getId());
      return count != 0;
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public boolean isServiceBlockedOnDestination(int serviceId, int destinationId) {
    int denials =
        this.queryForInt("select count(*) from service_denials where service_id = ? and destination_id = ?", serviceId,
            destinationId);
    if (denials > 0) {
      return true;
    }
    return false;
  }

  @Override
  public boolean isServiceBlockedOnFacility(int serviceId, int facilityId) {
    int denials =
        this.queryForInt("select count(*) from service_denials where service_id = ? and facility_id = ?", serviceId,
            facilityId);
    if (denials > 0) {
      return true;
    }
    return false;
  }

  private int queryForInt(String sql, Object... args) {
    try {
      @SuppressWarnings("ConstantConditions") Integer i = jdbc.queryForObject(sql, args, Integer.class);
      return (i != null ? i : 0);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public void removeAllDestinations(PerunSession sess, Service service, Facility facility) {
    try {
      jdbc.update("delete from facility_service_destinations where service_id=? and facility_id=?", service.getId(),
          facility.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void removeAllDestinations(PerunSession sess, Facility facility) {
    try {
      jdbc.update("delete from facility_service_destinations where facility_id=?", facility.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void removeAllRequiredAttributes(PerunSession sess, Service service) {
    try {
      jdbc.update("delete from service_required_attrs where service_id=?", service.getId());
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void removeDestination(PerunSession sess, Service service, Facility facility, Destination destination)
      throws DestinationAlreadyRemovedException {
    try {
      if (0 == jdbc.update(
          "delete from facility_service_destinations where service_id=? and facility_id=? and destination_id=?",
          service.getId(), facility.getId(), destination.getId())) {
        throw new DestinationAlreadyRemovedException(destination);
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void removeRequiredAttribute(PerunSession sess, Service service, AttributeDefinition attribute)
      throws AttributeNotAssignedException {
    try {
      if (0 == jdbc.update("delete from service_required_attrs where service_id=? and attr_id=?", service.getId(),
          attribute.getId())) {
        throw new AttributeNotAssignedException(attribute);
      }
    } catch (RuntimeException e) {
      throw new InternalErrorException(e);
    }
  }

  @Override
  public void removeRequiredAttributes(PerunSession sess, Service service,
                                       List<? extends AttributeDefinition> attributes)
      throws AttributeNotAssignedException {
    for (AttributeDefinition attribute : attributes) {
      removeRequiredAttribute(sess, service, attribute);
    }
  }

  @Override
  public boolean serviceExists(PerunSession sess, Service service) {
    try {
      int numberOfExistences = jdbc.queryForInt("select count(1) from services where id=?", service.getId());
      if (numberOfExistences == 1) {
        return true;
      } else if (numberOfExistences > 1) {
        throw new ConsistencyErrorException("Service " + service + " exists more than once.");
      }
      return false;
    } catch (EmptyResultDataAccessException ex) {
      return false;
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }

  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public void unblockAllServicesOnDestination(int destinationId) {
    try {
      jdbc.update("delete from service_denials where destination_id = ?", destinationId);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public void unblockAllServicesOnFacility(int facilityId) {
    try {
      jdbc.update("delete from service_denials where facility_id = ?", facilityId);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public void unblockService(int serviceId) {
    try {
      jdbc.update("delete from service_denials where service_id = ?", serviceId);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public void unblockServiceOnDestination(int serviceId, int destinationId) {
    try {
      jdbc.update("delete from service_denials where destination_id = ? and service_id = ?", destinationId, serviceId);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public void unblockServiceOnFacility(int serviceId, int facilityId) {
    try {
      jdbc.update("delete from service_denials where facility_id = ? and service_id = ?", facilityId, serviceId);
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }

  @Override
  public void updateService(PerunSession sess, Service service) {
    try {
      // if not set, make sure script path is set based on new service name
      if (service.getScript() == null || service.getScript().isEmpty()) {
        service.setScript("./" + service.getName());
      }
      jdbc.update("update services set name=?, description=?, delay=?, recurrence=?, enabled=?, script=?, " +
                  "use_expired_members=?," + "use_expired_vo_members=?," + "use_banned_members=?," +
                      "modified_by=?, modified_by_uid=?," +
                      " modified_at=" + Compatibility.getSysdate() + "  where id=?", service.getName(),
          service.getDescription(), service.getDelay(), service.getRecurrence(), service.isEnabled(),
          service.getScript(), service.isUseExpiredMembers(), service.isUseExpiredVoMembers(),
          service.isUseBannedMembers(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(),
          service.getId());
    } catch (RuntimeException ex) {
      throw new InternalErrorException(ex);
    }
  }
}
