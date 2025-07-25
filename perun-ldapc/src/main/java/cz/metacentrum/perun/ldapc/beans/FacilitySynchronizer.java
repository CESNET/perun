package cz.metacentrum.perun.ldapc.beans;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.rt.PerunRuntimeException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.ldapc.model.PerunFacility;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.naming.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * The FacilitySynchronizer class is responsible for synchronizing state between Perun facilities
 * and an LDAP server. It retrieves facilities and their attributes from Perun, updates or adds facilities
 * in the LDAP directory, and removes entries that are no longer present in Perun.
 *
 * This class extends AbstractSynchronizer, inheriting utility methods for attribute processing
 * and old entry removal. The synchronization process ensures alignment of facility data between
 * the two systems.
 */
@Component
public class FacilitySynchronizer extends AbstractSynchronizer {

  private static final Logger LOG = LoggerFactory.getLogger(FacilitySynchronizer.class);

  @Autowired
  protected PerunFacility perunFacility;

  public void synchronizeFacilities() {
    PerunBl perun = (PerunBl) ldapcManager.getPerunBl();
    Set<Name> presentFacilities = new HashSet<Name>();
    boolean shouldWriteExceptionLog = true;

    try {
      LOG.debug("Getting list of facilities");

      List<Facility> facilities = perun.getFacilitiesManagerBl().getFacilities(ldapcManager.getPerunSession());

      for (Facility facility : facilities) {

        presentFacilities.add(perunFacility.getEntryDN(String.valueOf(facility.getId())));

        try {
          LOG.debug("Synchronizing facility {}", facility);

          LOG.debug("Getting list of attributes for facility {}", facility.getId());
          List<Attribute> attrs = new ArrayList<Attribute>();
          List<String> attrNames = fillPerunAttributeNames(perunFacility.getPerunAttributeNames());
          try {
            //log.debug("Getting attribute {} for resource {}", attrName, resource.getId());
            attrs.addAll(
                perun.getAttributesManagerBl().getAttributes(ldapcManager.getPerunSession(), facility, attrNames));
          } catch (PerunRuntimeException e) {
            LOG.warn("No attributes {} found for facility {}: {}", attrNames, facility.getId(), e.getMessage());
            shouldWriteExceptionLog = false;
            throw new InternalErrorException(e);
          }
          LOG.debug("Got attributes {}", attrs.toString());

          perunFacility.synchronizeFacility(facility, attrs);

        } catch (PerunRuntimeException e) {
          if (shouldWriteExceptionLog) {
            LOG.error("Error synchronizing facility", e);
          }
          shouldWriteExceptionLog = false;
          throw new InternalErrorException(e);
        }
      }

      try {
        removeOldEntries(perunFacility, presentFacilities, LOG);
      } catch (InternalErrorException e) {
        LOG.error("Error removing old facility entries", e);
        shouldWriteExceptionLog = false;
        throw new InternalErrorException(e);
      }
    } catch (PerunRuntimeException e) {
      if (shouldWriteExceptionLog) {
        LOG.error("Error reading list of facilities", e);
      }
      throw new InternalErrorException(e);
    }


  }

}
