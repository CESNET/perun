package cz.metacentrum.perun.cabinet.bl.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.metacentrum.perun.cabinet.dao.ThanksManagerDao;
import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Thanks;
import cz.metacentrum.perun.cabinet.model.ThanksForGUI;
import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.ErrorCodes;
import cz.metacentrum.perun.cabinet.bl.AuthorManagerBl;
import cz.metacentrum.perun.cabinet.bl.PerunManagerBl;
import cz.metacentrum.perun.cabinet.bl.ThanksManagerBl;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.AuthzResolver;

/**
 * Class for handling Thanks entity in Cabinet.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ThanksManagerBlImpl implements ThanksManagerBl {

	private ThanksManagerDao thanksManagerDao;
	private AuthorManagerBl authorService;
	private PerunManagerBl perunService;

	// setters -------------------------

	public void setThanksManagerDao(ThanksManagerDao thanksManagerDao) {
		this.thanksManagerDao = thanksManagerDao;
	}

	public void setAuthorService(AuthorManagerBl authorService) {
		this.authorService = authorService;
	}

	public void setPerunService(PerunManagerBl perunService) {
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

		int id = thanksManagerDao.createThanks(t);

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
			return thanksManagerDao.findThanksById(t.getId()) != null;
		}
		if (t.getOwnerId() != null && t.getPublicationId() != null) {
			Thanks filter = new Thanks();
			filter.setOwnerId(t.getOwnerId());
			filter.setPublicationId(t.getPublicationId());
			return thanksManagerDao.findThanksByFilter(filter).size() > 0;
		}
		return false;
	}


	public List<Thanks> findThanksByFilter(Thanks t) {
		return thanksManagerDao.findThanksByFilter(t);
	}


	public int deleteThanksById(PerunSession sess, Integer id) throws CabinetException {

		Thanks t = findThanksById(id);
		// authorization TODO - better place ??
		// To delete thanks user must me either PERUNADMIN
		// or user who created record (thanks.createdBy property)
		try {
			if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN) &&
					(!t.getCreatedBy().equalsIgnoreCase(sess.getPerunPrincipal().getActor())) &&
					(!t.getCreatedByUid().equals(sess.getPerunPrincipal().getUserId()))) {
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

		return thanksManagerDao.deleteThanksById(id);
	}

	public List<Thanks> findThanksByPublicationId(int id){
		return thanksManagerDao.findThanksByPublicationId(id);
	}

	public Thanks findThanksById(int id){
		return thanksManagerDao.findThanksById(id);
	}

	public List<ThanksForGUI> findRichThanksByPublicationId(int id) {
		return thanksManagerDao.findRichThanksByPublicationId(id);
	}

	public List<ThanksForGUI> findAllRichThanksByUserId(Integer id) {
		return thanksManagerDao.findAllRichThanksByUserId(id);
	}

}
