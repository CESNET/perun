package cz.metacentrum.perun.cabinet.bl.impl;

import java.util.List;

import cz.metacentrum.perun.cabinet.bl.ErrorCodes;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.cabinet.dao.PublicationSystemManagerDao;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.PublicationSystemManagerBl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class for handling PublicationSystem entity in Cabinet.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class PublicationSystemManagerBlImpl implements PublicationSystemManagerBl {

	private static Logger log = LoggerFactory.getLogger(PublicationSystemManagerBlImpl.class);

	private PublicationSystemManagerDao publicationSystemManagerDao;

	@Autowired
	public void setPublicationSystemManagerDao(PublicationSystemManagerDao publicationSystemManagerDao) {
		this.publicationSystemManagerDao = publicationSystemManagerDao;
	}

	public PublicationSystem createPublicationSystem(PublicationSystem ps) throws CabinetException, InternalErrorException {
		return publicationSystemManagerDao.createPublicationSystem(ps);
	}

	public PublicationSystem updatePublicationSystem(PublicationSystem ps) throws CabinetException, InternalErrorException {
		return publicationSystemManagerDao.updatePublicationSystem(ps);
	}

	public void deletePublicationSystem(PublicationSystem ps) throws CabinetException, InternalErrorException {
		publicationSystemManagerDao.deletePublicationSystem(ps);
	}

	public PublicationSystem getPublicationSystemById(int publicationSystemId) throws InternalErrorException, CabinetException {
		return publicationSystemManagerDao.getPublicationSystemById(publicationSystemId);
	}

	public PublicationSystem getPublicationSystemByName(String name) throws InternalErrorException, CabinetException {
		return publicationSystemManagerDao.getPublicationSystemByName(name);
	}

	public PublicationSystem getPublicationSystemByNamespace(String namespace) throws InternalErrorException, CabinetException {
		return publicationSystemManagerDao.getPublicationSystemByNamespace(namespace);
	}

	public List<PublicationSystem> getPublicationSystems() throws InternalErrorException {
		return publicationSystemManagerDao.getPublicationSystems();
	}

	/**
	 * Checks if INTERNAL publication system is present in DB. If not, creates one.
	 */
	protected void initialize() throws CabinetException, InternalErrorException {
		// search for internal system
		try {
			PublicationSystem ps = publicationSystemManagerDao.getPublicationSystemByName("INTERNAL");
		} catch (CabinetException ex) {
			if (ErrorCodes.PUBLICATION_SYSTEM_NOT_EXISTS.equals(ex.getType())) {
				log.error("Internal PS not exists: {}", ex);
				// create internal if not exists
				PublicationSystem record = new PublicationSystem();
				record.setFriendlyName("INTERNAL");
				record.setLoginNamespace("empty");
				record.setType("empty");
				record.setUrl("empty");
				record.setPassword(null);
				record.setUsername(null);
				publicationSystemManagerDao.createPublicationSystem(record);
			}
		}
	}

}
