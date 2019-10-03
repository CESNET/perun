package cz.metacentrum.perun.ldapc.beans;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.Name;

import cz.metacentrum.perun.core.api.exceptions.rt.PerunRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.ldapc.model.PerunFacility;


@Component
public class FacilitySynchronizer extends AbstractSynchronizer {

	private final static Logger log = LoggerFactory.getLogger(FacilitySynchronizer.class);

	@Autowired
	protected PerunFacility perunFacility;

	public void synchronizeFacilities() throws InternalErrorException {
		PerunBl perun = (PerunBl)ldapcManager.getPerunBl();
		Set<Name> presentFacilities = new HashSet<Name>();
		boolean shouldWriteExceptionLog = true;

		try {
			log.debug("Getting list of facilities");

			List<Facility> facilities = perun.getFacilitiesManagerBl().getFacilities(ldapcManager.getPerunSession());

			for(Facility facility : facilities) {

				presentFacilities.add(perunFacility.getEntryDN(String.valueOf(facility.getId())));

				try {
					log.debug("Synchronizing facility {}", facility);

					log.debug("Getting list of attributes for facility {}", facility.getId());
					List<Attribute> attrs = new ArrayList<Attribute>();
					List<String> attrNames = fillPerunAttributeNames(perunFacility.getPerunAttributeNames());
					try {
						//log.debug("Getting attribute {} for resource {}", attrName, resource.getId());
						attrs.addAll(perun.getAttributesManagerBl().getAttributes(ldapcManager.getPerunSession(), facility, attrNames));
					} catch (PerunRuntimeException e) {
						log.warn("No attributes {} found for facility {}: {}", attrNames, facility.getId(), e.getMessage());
						shouldWriteExceptionLog = false;
						throw new InternalErrorException(e);
					}
					log.debug("Got attributes {}", attrs.toString());

					perunFacility.synchronizeFacility(facility, attrs);

				} catch (PerunRuntimeException e) {
					if (shouldWriteExceptionLog)  {
						log.error("Error synchronizing facility", e);
					}
					shouldWriteExceptionLog = false;
					throw new InternalErrorException(e);
				}
			}

			try {
				removeOldEntries(perunFacility, presentFacilities, log);
			} catch (InternalErrorException e) {
				log.error("Error removing old facility entries", e);
				shouldWriteExceptionLog = false;
				throw new InternalErrorException(e);
			}
		} catch (PerunRuntimeException e) {
			if (shouldWriteExceptionLog) {
				log.error("Error reading list of facilities", e);
			}
			throw new InternalErrorException(e);
		}


	}

}