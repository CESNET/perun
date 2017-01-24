package cz.metacentrum.perun.cabinet.bl.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.metacentrum.perun.cabinet.bl.CabinetManagerBl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import cz.metacentrum.perun.cabinet.dao.AuthorshipManagerDao;
import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Authorship;
import cz.metacentrum.perun.cabinet.model.Category;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.ErrorCodes;
import cz.metacentrum.perun.cabinet.bl.AuthorshipManagerBl;
import cz.metacentrum.perun.cabinet.bl.CategoryManagerBl;
import cz.metacentrum.perun.cabinet.bl.PerunManagerBl;
import cz.metacentrum.perun.cabinet.bl.PublicationManagerBl;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.PerunBl;

/**
 * Class for handling Authorship entity in Cabinet.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AuthorshipManagerBlImpl implements AuthorshipManagerBl {

	private static final double DEFAULT_RANK = 1.0;
	private AuthorshipManagerDao authorshipManagerDao;
	private PublicationManagerBl publicationManagerBl;
	private CategoryManagerBl categoryManagerBl;
	private CabinetManagerBl cabinetManagerBl;
	private static Logger log = LoggerFactory.getLogger(AuthorshipManagerBlImpl.class);

	@Autowired
	private PerunBl perun;

	// setters ===========================================

	@Autowired
	public void setCabinetManagerBl(CabinetManagerBl cabinetManagerBl) {
		this.cabinetManagerBl = cabinetManagerBl;
	}

	@Autowired
	public void setPublicationManagerBl(PublicationManagerBl publicationManagerBl) {
		this.publicationManagerBl = publicationManagerBl;
	}

	@Autowired
	public void setAuthorshipManagerDao(AuthorshipManagerDao authorshipManagerDao) {
		this.authorshipManagerDao = authorshipManagerDao;
	}

	@Autowired
	public void setCategoryManagerBl(CategoryManagerBl categoryManagerBl) {
		this.categoryManagerBl = categoryManagerBl;
	}

	public AuthorshipManagerDao getAuthorshipManagerDao() {
		return authorshipManagerDao;
	}

	public CategoryManagerBl getCategoryManagerBl() {
		return categoryManagerBl;
	}

	public PublicationManagerBl getPublicationManagerBl() {
		return publicationManagerBl;
	}

	public CabinetManagerBl getCabinetManagerBl() {
		return cabinetManagerBl;
	}

	// business methods ===================================

	@Override
	public Authorship createAuthorship(PerunSession sess, Authorship authorship) throws CabinetException, InternalErrorException {

		if (authorshipExists(authorship)) throw new CabinetException(ErrorCodes.AUTHORSHIP_ALREADY_EXISTS);
		if (authorship.getCreatedDate() == null) {
			authorship.setCreatedDate(new Date());
		}
		if (authorship.getCreatedByUid() == 0) {
			authorship.setCreatedByUid(sess.getPerunPrincipal().getUserId());
		}
		try {
			getAuthorshipManagerDao().createAuthorship(sess, authorship);
		} catch (DataIntegrityViolationException e) {
			throw new CabinetException(ErrorCodes.USER_NOT_EXISTS, e);
		}
		log.debug("{} created.", authorship);
		// log
		perun.getAuditer().log(sess, "Authorship {} created.", authorship);

		getCabinetManagerBl().updatePriorityCoefficient(sess, authorship.getUserId(), calculateNewRank(authorship.getUserId()));

		getCabinetManagerBl().setThanksAttribute(authorship.getUserId());

		return authorship;

	}

	@Override
	public boolean authorshipExists(Authorship authorship) throws InternalErrorException {
		if (authorship == null) throw new NullPointerException("Authorship cannot be null");

		if (authorship.getId() != 0) {
			try {
				getAuthorshipManagerDao().getAuthorshipById(authorship.getId());
				return true;
			} catch (CabinetException ex) {
				// return false at the end
			}
		}
		if (authorship.getPublicationId() != 0 && authorship.getUserId() != 0) {
			try {
				getAuthorshipManagerDao().getAuthorshipByUserAndPublicationId(authorship.getUserId(), authorship.getPublicationId());
				return true;
			} catch (CabinetException ex) {
				// return false at the end
			}
		}
		return false;
	}

	@Override
	public void deleteAuthorship(PerunSession sess, Authorship authorship) throws CabinetException, InternalErrorException {

		getAuthorshipManagerDao().deleteAuthorship(sess, authorship);
		log.debug("{} deleted.", authorship);

		int userId = authorship.getUserId();
		getCabinetManagerBl().updatePriorityCoefficient(sess, userId, calculateNewRank(userId));

		perun.getAuditer().log(sess, "Authorship {} deleted.", authorship);

		getCabinetManagerBl().setThanksAttribute(authorship.getUserId());

	}

	@Override
	public Authorship getAuthorshipById(int id) throws CabinetException, InternalErrorException {
		return getAuthorshipManagerDao().getAuthorshipById(id);
	}

	@Override
	public List<Authorship> getAuthorshipsByUserId(int id) throws InternalErrorException {
		return getAuthorshipManagerDao().getAuthorshipsByUserId(id);
	}

	@Override
	public List<Authorship> getAuthorshipsByPublicationId(int id) throws InternalErrorException {
		return getAuthorshipManagerDao().getAuthorshipsByPublicationId(id);
	}

	@Override
	public Authorship getAuthorshipByUserAndPublicationId(int userId, int publicationId) throws CabinetException, InternalErrorException {
		return getAuthorshipManagerDao().getAuthorshipByUserAndPublicationId(userId, publicationId);
	}

	@Override
	public double calculateNewRank(int userId) throws CabinetException, InternalErrorException {
		List<Authorship> reports = getAuthorshipsByUserId(userId);
		return calculateNewRank(reports);
	}

	@Override
	public synchronized double calculateNewRank(List<Authorship> authorships) throws InternalErrorException, CabinetException {

		double rank = DEFAULT_RANK;
		for (Authorship r : authorships) {
			Publication p = getPublicationManagerBl().getPublicationById(r.getPublicationId());
			rank += p.getRank();
			Category c = getCategoryManagerBl().getCategoryById(p.getCategoryId());
			rank += c.getRank();
		}
		return rank;

	}

	@Override
	public Author getAuthorById(int id) throws CabinetException, InternalErrorException {
		return getAuthorshipManagerDao().getAuthorById(id);
	}

	@Override
	public List<Author> getAllAuthors() throws InternalErrorException {
		return getAuthorshipManagerDao().getAllAuthors();
	}

	@Override
	public List<Author> getAuthorsByPublicationId(int id) throws InternalErrorException {
		return getAuthorshipManagerDao().getAuthorsByPublicationId(id);
	}

	@Override
	public List<Author> getAuthorsByAuthorshipId(PerunSession sess, int id) throws CabinetException, InternalErrorException {
		List<Author> result = new ArrayList<Author>();

		Authorship report = getAuthorshipManagerDao().getAuthorshipById(id);
		if (report == null) {
			throw new CabinetException("Authorship with ID: "+id+" doesn't exists!", ErrorCodes.AUTHORSHIP_NOT_EXISTS);
		}

		List<Authorship> publicationReports = getAuthorshipManagerDao().getAuthorshipsByPublicationId(report.getPublicationId());

		for (Authorship r : publicationReports) {
			result.add(getAuthorshipManagerDao().getAuthorById(r.getUserId()));
		}
		return result;
	}

}
