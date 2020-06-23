package cz.metacentrum.perun.cabinet.bl.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import cz.metacentrum.perun.audit.events.AuthorshipManagementEvents.AuthorshipCreated;
import cz.metacentrum.perun.audit.events.AuthorshipManagementEvents.AuthorshipDeleted;
import cz.metacentrum.perun.cabinet.bl.CabinetManagerBl;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
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

	private PerunBl perun;
	private PerunSession session;

	// setters ===========================================

	@Autowired
	public void setPerun(PerunBl perun) {
		this.perun = perun;
	}

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
	public Authorship createAuthorship(PerunSession sess, Authorship authorship) throws CabinetException {

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

		synchronized (CabinetManagerBlImpl.class) {
			getCabinetManagerBl().updatePriorityCoefficient(sess, authorship.getUserId(), calculateNewRank(authorship.getUserId()));
		}
		synchronized (ThanksManagerBlImpl.class) {
			getCabinetManagerBl().setThanksAttribute(authorship.getUserId());
		}

		// log
		perun.getAuditer().log(sess,new AuthorshipCreated(authorship));
		return authorship;

	}

	@Override
	public boolean authorshipExists(Authorship authorship) {
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
	public void deleteAuthorship(PerunSession sess, Authorship authorship) throws CabinetException {

		getAuthorshipManagerDao().deleteAuthorship(sess, authorship);
		log.debug("{} deleted.", authorship);

		int userId = authorship.getUserId();

		synchronized (CabinetManagerBlImpl.class) {
			getCabinetManagerBl().updatePriorityCoefficient(sess, userId, calculateNewRank(userId));
		}
		synchronized (ThanksManagerBlImpl.class) {
			getCabinetManagerBl().setThanksAttribute(authorship.getUserId());
		}

		perun.getAuditer().log(sess, new AuthorshipDeleted(authorship));

	}

	@Override
	public Authorship getAuthorshipById(int id) throws CabinetException {
		return getAuthorshipManagerDao().getAuthorshipById(id);
	}

	@Override
	public List<Authorship> getAuthorshipsByUserId(int id) {
		return getAuthorshipManagerDao().getAuthorshipsByUserId(id);
	}

	@Override
	public List<Authorship> getAuthorshipsByPublicationId(int id) {
		return getAuthorshipManagerDao().getAuthorshipsByPublicationId(id);
	}

	@Override
	public Authorship getAuthorshipByUserAndPublicationId(int userId, int publicationId) throws CabinetException {
		return getAuthorshipManagerDao().getAuthorshipByUserAndPublicationId(userId, publicationId);
	}

	@Override
	public double calculateNewRank(int userId) throws CabinetException {
		List<Authorship> reports = getAuthorshipsByUserId(userId);
		return calculateNewRank(reports);
	}

	@Override
	public synchronized double calculateNewRank(List<Authorship> authorships) throws CabinetException {

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
	public Author getAuthorById(int id) throws CabinetException {
		return convertAuthorToAuthorWithAttributes(getAuthorshipManagerDao().getAuthorById(id));
	}

	@Override
	public List<Author> getAllAuthors() {
		return convertAuthorsToAuthorsWithAttributes(getAuthorshipManagerDao().getAllAuthors());
	}

	@Override
	public List<Author> getAuthorsByPublicationId(int id) {
		return convertAuthorsToAuthorsWithAttributes(getAuthorshipManagerDao().getAuthorsByPublicationId(id));
	}

	@Override
	public List<Author> getAuthorsByAuthorshipId(PerunSession sess, int id) throws CabinetException {
		List<Author> result = new ArrayList<Author>();

		Authorship report = getAuthorshipManagerDao().getAuthorshipById(id);
		if (report == null) {
			throw new CabinetException("Authorship with ID: "+id+" doesn't exists!", ErrorCodes.AUTHORSHIP_NOT_EXISTS);
		}

		List<Authorship> publicationReports = getAuthorshipManagerDao().getAuthorshipsByPublicationId(report.getPublicationId());

		for (Authorship r : publicationReports) {
			result.add(getAuthorshipManagerDao().getAuthorById(r.getUserId()));
		}
		return convertAuthorsToAuthorsWithAttributes(result);
	}

	@Override
	public List<Author> findNewAuthors(PerunSession sess, String searchString) throws CabinetException {

		List<String> attrs = Arrays.asList(AttributesManager.NS_USER_ATTR_DEF + ":preferredMail",
				AttributesManager.NS_USER_ATTR_DEF + ":organization");
				//AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:einfra"
		List<Author> authors = new ArrayList<>();

		try {
			List<RichUser> users = perun.getUsersManagerBl().findRichUsersWithAttributes(sess, searchString, attrs);
			for (RichUser user : users) {
				Author author = new Author(user.getId(), user.getFirstName(), user.getLastName(), user.getMiddleName(),
						user.getTitleBefore(), user.getTitleAfter());

				for (Attribute a : user.getUserAttributes()) {
					if (a.getName().equals(AttributesManager.NS_USER_ATTR_DEF + ":preferredMail")) {

						if (a.getValue() != null && !((String)a.getValue()).isEmpty()) {
							String safeMail = ((String) a.getValue()).split("@")[0];

							if (safeMail.length() > 2) {
								safeMail = safeMail.substring(0, 1) + "****" + safeMail.substring(safeMail.length()-1, safeMail.length());
							}

							safeMail += "@"+((String) a.getValue()).split("@")[1];

							a.setValue(safeMail);
						}
					}
				}

				author.setAttributes(user.getUserAttributes());
				authors.add(author);
			}
		} catch (UserNotExistsException e) {
			log.error("Shouldn't really happen.");
		}
		return authors;

	}

	private List<Author> convertAuthorsToAuthorsWithAttributes(List<Author> authors) {
		List<Author> result = new ArrayList<>();
		for (Author author : authors) {
			result.add(convertAuthorToAuthorWithAttributes(author));
		}
		return result;
	}

	private Author convertAuthorToAuthorWithAttributes(Author author) {
		try {
			if (session == null) {
				session = perun.getPerunSession(new PerunPrincipal("perunCabinet", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL), new PerunClient());
			}
			User user = perun.getUsersManagerBl().getUserById(session, author.getId());
			Attribute a = perun.getAttributesManagerBl().getAttribute(session, user, AttributesManager.NS_USER_ATTR_DEF + ":preferredMail");
			Attribute b = perun.getAttributesManagerBl().getAttribute(session, user, AttributesManager.NS_USER_ATTR_DEF + ":organization");
			author.setAttributes(Arrays.asList(a,b));
		} catch (Exception ex) {
			log.error("Unable to get attributes for {}: {}", author, ex);
		}
		return author;
	}

}
