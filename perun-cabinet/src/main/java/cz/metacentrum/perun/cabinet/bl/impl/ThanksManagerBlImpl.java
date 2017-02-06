package cz.metacentrum.perun.cabinet.bl.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.metacentrum.perun.cabinet.bl.AuthorshipManagerBl;
import cz.metacentrum.perun.cabinet.bl.CabinetManagerBl;
import cz.metacentrum.perun.cabinet.dao.ThanksManagerDao;
import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Thanks;
import cz.metacentrum.perun.cabinet.model.ThanksForGUI;
import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.ErrorCodes;
import cz.metacentrum.perun.cabinet.bl.PerunManagerBl;
import cz.metacentrum.perun.cabinet.bl.ThanksManagerBl;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class for handling Thanks entity in Cabinet.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ThanksManagerBlImpl implements ThanksManagerBl {

	private ThanksManagerDao thanksManagerDao;
	private AuthorshipManagerBl authorshipManagerBl;
	private CabinetManagerBl cabinetManagerBl;

	private static Logger log = LoggerFactory.getLogger(ThanksManagerBlImpl.class);

	// setters -------------------------

	@Autowired
	public void setThanksManagerDao(ThanksManagerDao thanksManagerDao) {
		this.thanksManagerDao = thanksManagerDao;
	}

	@Autowired
	public void setAuthorshipManagerBl(AuthorshipManagerBl authorshipManagerBl) {
		this.authorshipManagerBl = authorshipManagerBl;
	}

	@Autowired
	public void setCabinetManagerBl(CabinetManagerBl cabinetManagerBl) {
		this.cabinetManagerBl = cabinetManagerBl;
	}

	public ThanksManagerDao getThanksManagerDao() {
		return thanksManagerDao;
	}

	public AuthorshipManagerBl getAuthorshipManagerBl() {
		return authorshipManagerBl;
	}

	public CabinetManagerBl getCabinetManagerBl() {
		return cabinetManagerBl;
	}


// methods -------------------------

	public Thanks createThanks(PerunSession sess, Thanks t) throws CabinetException, InternalErrorException {
		if (t.getCreatedDate() == null) {
			t.setCreatedDate(new Date());
		}
		if (thanksExist(t)) {
			throw new CabinetException("Can't create duplicate thanks.", ErrorCodes.THANKS_ALREADY_EXISTS);
		}

		t = getThanksManagerDao().createThanks(sess, t);
		log.debug("{} created.", t);

		// recalculate thanks for all publication's authors
		List<Author> authors = new ArrayList<Author>();
		authors = getAuthorshipManagerBl().getAuthorsByPublicationId(t.getPublicationId());
		for (Author a : authors) {
			getCabinetManagerBl().setThanksAttribute(a.getId());
		}
		return t;
	}

	@Override
	public void deleteThanks(PerunSession sess, Thanks thanks) throws InternalErrorException, CabinetException {
		// recalculate thanks for all publication's authors
		List<Author> authors = getAuthorshipManagerBl().getAuthorsByPublicationId(thanks.getPublicationId());
		for (Author a : authors) {
			getCabinetManagerBl().setThanksAttribute(a.getId());
		}

		getThanksManagerDao().deleteThanks(sess, thanks);
		log.debug("{} deleted.", thanks);
	}

	@Override
	public boolean thanksExist(Thanks thanks) throws InternalErrorException {
		return getThanksManagerDao().thanksExist(thanks);
	}

	@Override
	public Thanks getThanksById(int id) throws CabinetException, InternalErrorException {
		return getThanksManagerDao().getThanksById(id);
	}

	@Override
	public List<Thanks> getThanksByPublicationId(int publicationId) throws InternalErrorException {
		return getThanksManagerDao().getThanksByPublicationId(publicationId);
	}

	@Override
	public List<ThanksForGUI> getRichThanksByPublicationId(int publicationId) throws InternalErrorException {
		return getThanksManagerDao().getRichThanksByPublicationId(publicationId);
	}

	@Override
	public List<ThanksForGUI> getRichThanksByUserId(int userId) throws InternalErrorException {
		return getThanksManagerDao().getRichThanksByUserId(userId);
	}

}
