package cz.metacentrum.perun.cabinet.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.metacentrum.perun.cabinet.dao.IThanksDao;
import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Thanks;
import cz.metacentrum.perun.cabinet.model.ThanksForGUI;
import cz.metacentrum.perun.cabinet.service.CabinetException;
import cz.metacentrum.perun.cabinet.service.ErrorCodes;
import cz.metacentrum.perun.cabinet.service.IAuthorService;
import cz.metacentrum.perun.cabinet.service.IPerunService;
import cz.metacentrum.perun.cabinet.service.IThanksService;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.AuthzResolver;

/**
 * Class for handling Thanks entity in Cabinet.
 * 
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class ThanksServiceImpl implements IThanksService {
	
	private IThanksDao thanksDao;
	private IAuthorService authorService;
	private IPerunService perunService;
    
    // setters -------------------------
    
	public void setThanksDao(IThanksDao thanksDao) {
		this.thanksDao = thanksDao;
	}
	
	public void setAuthorService(IAuthorService authorService) {
		this.authorService = authorService;
	}
	
	public void setPerunService(IPerunService perunService) {
		this.perunService = perunService;
	}
	
	// methods -------------------------
	
	public int createThanks(PerunSession sess, Thanks t) throws CabinetException {
		if (t.getCreatedDate() == null) {
			t.setCreatedDate(new Date());
		}
		if (thanksExists(t)) {
			throw new CabinetException("Can't create duplicite thanks.", ErrorCodes.THANKS_ALREADY_EXISTS);
		}

		int id = thanksDao.createThanks(t);

		// recalculate thanks for all publication's authors
		List<Author> authors = new ArrayList<Author>();
		authors = authorService.findAuthorsByPublicationId(t.getPublicationId());
		for (Author a : authors) {
			perunService.setThanksAttribute(a.getId());			
		}
		
		return id;
		
	}
	
	public boolean thanksExists(Thanks t) {
		if (t.getId() != null) {
			return thanksDao.findThanksById(t.getId()) != null;
		}
		if (t.getOwnerId() != null && t.getPublicationId() != null) {
			Thanks filter = new Thanks();
			filter.setOwnerId(t.getOwnerId());
			filter.setPublicationId(t.getPublicationId());
			return thanksDao.findThanksByFilter(filter).size() > 0;
		}
		return false;
	}

	
	public List<Thanks> findThanksByFilter(Thanks t) {
		return thanksDao.findThanksByFilter(t);
	}

	
	public int deleteThanksById(PerunSession sess, Integer id) throws CabinetException {
		
		Thanks t = findThanksById(id);
		// authorization TODO - better place ??
		// To delete thanks user must me either PERUNADMIN
		// or user who created record (thanks.createdBy property)
		try {
			if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN) && (!t.getCreatedBy().equalsIgnoreCase(sess.getPerunPrincipal().getActor()))) {
				throw new CabinetException("You are not allowed to delete thanks you didn't created.", ErrorCodes.NOT_AUTHORIZED);
			}
		} catch (PerunException pe) {
			throw new CabinetException(ErrorCodes.PERUN_EXCEPTION, pe);
		}
		
		// recalculate thanks for all publication's authors
		List<Author> authors = authorService.findAuthorsByPublicationId(t.getPublicationId());
		for (Author a : authors) {
			perunService.setThanksAttribute(a.getId());			
		}

		return thanksDao.deleteThanksById(id);
	}
	
	public List<Thanks> findThanksByPublicationId(int id){
		return thanksDao.findThanksByPublicationId(id);
	}
	
	public Thanks findThanksById(int id){
		return thanksDao.findThanksById(id);
	}

	public List<ThanksForGUI> findRichThanksByPublicationId(int id) {
		return thanksDao.findRichThanksByPublicationId(id);
	}
	
	public List<ThanksForGUI> findAllRichThanksByUserId(Integer id) {
		return thanksDao.findAllRichThanksByUserId(id);
	}
	
}