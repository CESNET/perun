package cz.metacentrum.perun.cabinet.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.cabinet.dao.IPublicationSystemDao;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.service.CabinetException;
import cz.metacentrum.perun.cabinet.service.IPublicationSystemService;

/**
 * Class for handling PublicationSystem entity in Cabinet.
 * 
 * @author Jiri Harazim <harazim@mail.muni.cz>
 */
public class PublicationSystemServiceImpl implements IPublicationSystemService {

	@SuppressWarnings("unused")
	private Logger log = LoggerFactory.getLogger(getClass());

	private IPublicationSystemDao publicationSystemDao;

	// setters ----------------------
	
	public void setPublicationSystemDao(IPublicationSystemDao publicationSystemDao) {
		this.publicationSystemDao = publicationSystemDao;
	}
	
	// methods ----------------------

	public int createPublicationSystem(PublicationSystem ps) {
		return publicationSystemDao.createPublicationSystem(ps);
	}

	public int updatePublicationSystem(PublicationSystem ps) {
		return publicationSystemDao.updatePublicationSystem(ps);
	}
	
	public int deletePublicationSystem(PublicationSystem ps) {
		return publicationSystemDao.deletePublicationSystem(ps);
	}

	public List<PublicationSystem> findPublicationSystemsByFilter(PublicationSystem filter) {
		return publicationSystemDao.findPublicationSystemsByFilter(filter);
	}

	public PublicationSystem findPublicationSystemById(Integer publicationSystemId) {
		return publicationSystemDao.findPublicationSystemById(publicationSystemId);
	}

	public List<PublicationSystem> findAllPublicationSystems() {
		return publicationSystemDao.findAllPublicationSystems();
	}
	
	/**
	 * Checks if INTERNAL publication system is present in DB and if is unique
	 * 
	 * if there are more same INTERNAL systems throw error
	 * if no INTERNAL system is present, creates one.
	 */
	protected void initialize() throws CabinetException {
		
		// search for internal system
		PublicationSystem example = new PublicationSystem();
		example.setFriendlyName("INTERNAL");
		List<PublicationSystem> list = publicationSystemDao.findPublicationSystemsByFilter(example);
		
		// not present, creates one
		if (list.isEmpty()) {
			PublicationSystem record = new PublicationSystem();
			record.setFriendlyName("INTERNAL");
			record.setLoginNamespace("empty");
			record.setType("empty");
			record.setUrl("empty");
			record.setPassword(null);
			record.setUsername(null);
			publicationSystemDao.createPublicationSystem(record);
		}
		
		// more then one is present - throw exception (not sure which delete) ?
		
		if (list.size() > 1) {
			throw new CabinetException("There are more than one INTERNAL publication systems. Only one is allowed. Please delete some of them in database and transfer it's publications to correct one.");
		}
		
	}

}