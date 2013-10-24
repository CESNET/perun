package cz.metacentrum.perun.cabinet.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import cz.metacentrum.perun.cabinet.dao.IAuthorshipDao;
import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Authorship;
import cz.metacentrum.perun.cabinet.model.Category;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.service.CabinetException;
import cz.metacentrum.perun.cabinet.service.ErrorCodes;
import cz.metacentrum.perun.cabinet.service.IAuthorService;
import cz.metacentrum.perun.cabinet.service.IAuthorshipService;
import cz.metacentrum.perun.cabinet.service.ICategoryService;
import cz.metacentrum.perun.cabinet.service.IPerunService;
import cz.metacentrum.perun.cabinet.service.IPublicationService;
import cz.metacentrum.perun.cabinet.service.SortParam;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.bl.PerunBl;

/**
 * Class for handling Authorship entity in Cabinet.
 * 
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class AuthorshipServiceImpl implements IAuthorshipService {
	
	private static final double DEFAULT_RANK = 1.0;
	private IAuthorshipDao authorshipDao;
	private IPublicationService publicationService;
	private ICategoryService categoryService;
	private IAuthorService authorService;
	private IPerunService perunService;
	private static Logger log = LoggerFactory.getLogger(AuthorshipServiceImpl.class);
	
	@Autowired
	private PerunBl perun;
	
	// setters ===========================================
	
	public void setAuthorService(IAuthorService authorService) {
		this.authorService = authorService;
	}

	public void setPerunService(IPerunService perunService) {
		this.perunService = perunService;
	}

	public void setCategoryService(ICategoryService categoryService) {
		this.categoryService = categoryService;
	}

	public void setPublicationService(IPublicationService publicationService) {
		this.publicationService = publicationService;
	}
	
	public void setAuthorshipDao(IAuthorshipDao authorshipDao) {
		this.authorshipDao = authorshipDao;
	}
	
	// business methods ===================================
	
	
	public int createAuthorship(PerunSession sess, Authorship authorship) throws CabinetException {
		if (authorshipExists(authorship)) throw new CabinetException(ErrorCodes.AUTHORSHIP_ALREADY_EXISTS);
		if (authorship.getCreatedDate() == null) {
			authorship.setCreatedDate(new Date());
		}
		int id;
		try {
			id = authorshipDao.create(authorship);
		} catch (DataIntegrityViolationException e) {
			throw new CabinetException(ErrorCodes.USER_NOT_EXISTS, e);
		}
		log.debug("Authorship: [{}] created.", authorship);
		// log
		try {
			perun.getAuditer().log(sess, "Authorship {} created.", authorship);
		} catch (InternalErrorException ex) {
			log.error("Unable to log message authorship created to Auditer for");
		}
		perunService.updatePriorityCoeficient(sess, authorship.getUserId(), calculateNewRank(authorship.getUserId()));
		
		perunService.setThanksAttribute(authorship.getUserId());
		
		return id;
	}

	
	public boolean authorshipExists(Authorship authorship) {
		if (authorship == null) throw new NullPointerException("Authorship cannot be null");

		if (authorship.getId() != 0) {
			return authorshipDao.findById(authorship.getId()) != null;
		}
		if (authorship.getPublicationId() != null && authorship.getUserId() != null) {
			Authorship filter = new Authorship();
			filter.setPublicationId(authorship.getPublicationId());
			filter.setUserId(authorship.getUserId());
			return authorshipDao.findByFilter(filter, null).size() > 0;
		}
		return false;
	}

	public Double calculateNewRank(Integer userId) {
		
		List<Authorship> reports = findAuthorshipsByUserId(userId);
		return calculateNewRank(reports);
		
	}
	
	public synchronized Double calculateNewRank(List<Authorship> authorships) {
		
		Double rank = DEFAULT_RANK;
		for (Authorship r : authorships) {
			Publication p = publicationService.findPublicationById(r.getPublicationId());
			rank += p.getRank();
			Category c = categoryService.findCategoryById(p.getCategoryId());
			rank += c.getRank();
		}
		return rank;
		
	}

	
	public List<Authorship> findAuthorshipsByFilter(Authorship filter) {
		return authorshipDao.findByFilter(filter);
	}
	
	public Date getLastCreatedAuthorshipDate(Integer userId) {
		Authorship report = authorshipDao.findLastestOfUser(userId);
		return (report != null) ? report.getCreatedDate() : null;
	}
	
	
	public List<Author> findAuthorsByAuthorshipId(PerunSession sess, Integer id) throws CabinetException {
		List<Author> result = new ArrayList<Author>();
		
		Authorship report = authorshipDao.findById(id);
		if (report == null) {
			throw new CabinetException("Authorship with ID: "+id+" doesn't exists!", ErrorCodes.AUTHORSHIP_NOT_EXISTS);
		}
		
		Authorship filter = new Authorship();
		filter.setPublicationId(report.getPublicationId());
		
		List<Authorship> publicationReports = authorshipDao.findByFilter(filter, null);
		
		for (Authorship r : publicationReports) {
			result.add(authorService.findAuthorByUserId(r.getUserId()));
		}
		return result;
	}
	
	public List<Authorship> findAllAuthorships() {
		return authorshipDao.findAll();
	}
	
	
	public int getAuthorshipsCount() {
		return authorshipDao.getCount();
	}
	
	public int getAuthorshipsCountForUser(Integer userId) {
		return authorshipDao.getCountForUser(userId);
	}

	public List<Authorship> findAuthorshipsByFilter(Authorship report, SortParam sortParam) {
		if (sortParam == null) return findAuthorshipsByFilter(report);
		if (! sortParam.getProperty().toString().matches("[a-z,A-Z,_,0-9]*")) throw new IllegalArgumentException("sortParam.property is not allowed: "+sortParam.getProperty());
		return authorshipDao.findByFilter(report, sortParam);
	}
	
	
	public Authorship findAuthorshipById(Integer id) {
		return authorshipDao.findById(id);
	}
	
	
	public List<Authorship> findAuthorshipsByPublicationId(Integer id) {
		return authorshipDao.findByPublicationId(id);
	}
	
	
	public List<Authorship> findAuthorshipsByUserId(Integer id) {
		return authorshipDao.findByUserId(id);
	}
	
	
	public int updateAuthorship(PerunSession sess, Authorship report) throws CabinetException {

		// check if such authorship exists
		Authorship r = authorshipDao.findById(report.getId());
		if (r == null) {
			throw new CabinetException("Authorship with ID: "+report.getId()+" doesn't exists.", ErrorCodes.AUTHORSHIP_NOT_EXISTS);
		}
		// check if "new" authorship already exist before update
		Authorship filter = new Authorship();
		filter.setPublicationId(report.getPublicationId());
		filter.setUserId(report.getUserId());
		List<Authorship> list = authorshipDao.findByFilter(filter);
		for (Authorship a : list) {
			if (a.getId() != report.getId()) {
				throw new CabinetException("Can't update authorship ID="+report.getId()+", same authorship already exists under ID="+a.getId(), ErrorCodes.AUTHORSHIP_ALREADY_EXISTS);
			} 
		}
		// update
		int rows = authorshipDao.update(report);
		
		// if updated
		if (rows > 0) {
			if (report.getPublicationId() != r.getPublicationId()) {
				// If authorship moved to another publication
				Set<Author> authors = new HashSet<Author>();
				// get authors of both publications
				authors.addAll(authorService.findAuthorsByPublicationId(report.getPublicationId()));
				authors.addAll(authorService.findAuthorsByPublicationId(r.getPublicationId()));
				// process them
				for (Author a : authors) {
					perunService.updatePriorityCoeficient(sess, a.getId(), calculateNewRank(a.getAuthorships()));
				}
				// calculate thanks for original user
				perunService.setThanksAttribute(r.getUserId());
				if (r.getUserId() != report.getUserId()) {
					// if user changed along side publication - calculate thanks for second user
					perunService.setThanksAttribute(report.getUserId());
				}
			} else if (r.getUserId() != report.getUserId()) {
				// if user (author) changed, update for both of them
				perunService.updatePriorityCoeficient(sess, report.getUserId(), calculateNewRank(report.getUserId()));
				perunService.setThanksAttribute(report.getId());
				perunService.updatePriorityCoeficient(sess, report.getUserId(), calculateNewRank(r.getUserId()));
				perunService.setThanksAttribute(r.getUserId());
			}
			log.debug("Authorship: [{}] updated to Authorship: [{}].", r, report);
		}
		return rows;
		
	}
	
	
	public int deleteAuthorshipById(PerunSession sess, Integer id) throws CabinetException {

		Authorship a = findAuthorshipById(id);
		if (a == null) {
			throw new CabinetException("Authorship with ID: "+id+" doesn't exists.", ErrorCodes.AUTHORSHIP_NOT_EXISTS);
		}
		// authorization TODO - better place ??
		// To delete authorship user must me either PERUNADMIN
		// or user who created record (authorship.createdBy property)
		// or user which is concerned by record (authorship.userId property)
		try {
			if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN) && (
				!a.getCreatedBy().equalsIgnoreCase(sess.getPerunPrincipal().getActor()) || !a.getUserId().equals(sess.getPerunPrincipal().getUser().getId()) )) {
				throw new CabinetException("You are not allowed to delete authorships you didn't created or which doesn't concern you.", ErrorCodes.NOT_AUTHORIZED);
			}
		} catch (PerunException pe) {
			throw new CabinetException(ErrorCodes.PERUN_EXCEPTION, pe);
		}
		// delete
		int rows = authorshipDao.deleteById(id);
		
		// if deleted
		if (rows > 0) {
			// update coefficient
			int userId = a.getUserId();
			perunService.updatePriorityCoeficient(sess, userId, calculateNewRank(userId));
			log.debug("Authorship: [{}] deleted.", a);
			try {
				perun.getAuditer().log(sess, "Authorship {} deleted.", a);
			} catch (InternalErrorException ex) {
				log.error("Unable to log message authorship deleted to Auditer.");
			}
			
			perunService.setThanksAttribute(a.getUserId());
			
		}
	
		return rows;
	
	}

}