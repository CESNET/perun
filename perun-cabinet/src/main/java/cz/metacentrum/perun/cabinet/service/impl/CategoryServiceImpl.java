package cz.metacentrum.perun.cabinet.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.cabinet.dao.ICategoryDao;
import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Category;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.service.CabinetException;
import cz.metacentrum.perun.cabinet.service.IAuthorService;
import cz.metacentrum.perun.cabinet.service.IAuthorshipService;
import cz.metacentrum.perun.cabinet.service.ICategoryService;
import cz.metacentrum.perun.cabinet.service.IPerunService;
import cz.metacentrum.perun.cabinet.service.IPublicationService;
import cz.metacentrum.perun.core.api.PerunSession;

/**
 * Class for handling Category entity in Cabinet.
 * 
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CategoryServiceImpl implements ICategoryService {

	private ICategoryDao categoryDao;
	private IPublicationService publicationService;
	private IPerunService perunService;
	private IAuthorshipService authorshipService;
	private IAuthorService authorService;
	
	private static Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);
	
	// setters ----------------------
	
	public void setCategoryDao(ICategoryDao categoryDao) {
		this.categoryDao = categoryDao;
	}
	
	public void setPublicationService(IPublicationService publicationService) {
		this.publicationService = publicationService;
	}
	
	public void setPerunService(IPerunService perunService) {
		this.perunService = perunService;
	}
	
	public void setAuthorshipService(IAuthorshipService authorshipService) {
		this.authorshipService = authorshipService;
	}
	
	public void setAuthorService(IAuthorService authorService) {
		this.authorService = authorService;
	}

	// methods ----------------------
	
	public int createCategory(Category c) {
		return categoryDao.createCategory(c);
	}

	
	public List<Category> findAllCategories() {
		return categoryDao.findAllCategories();
	}

	
	public Category findCategoryById(Integer categoryId) {
		return categoryDao.findCategoryById(categoryId);
	}

	
	public int getCount() {
		return categoryDao.getCount();
	}

	
	public int updateCategoryById(PerunSession sess, Category category) throws CabinetException {

		// save original category
		Category cat = categoryDao.findCategoryById(category.getId());
		// update
		int result = categoryDao.updateCategoryById(category);
		// was rank changed ?
		if (result > 0 && cat.getRank() != category.getRank()) {
			// yes
			Publication filter = new Publication();
			filter.setCategoryId(category.getId());
			List<Publication> pubs = publicationService.findPublicationsByFilter(filter);
			
			// update coef for all authors of all publications in updated category
			Set<Author> authors = new HashSet<Author>();
			for (Publication p : pubs) {
				authors.addAll(authorService.findAuthorsByPublicationId(p.getId()));
			}
			for (Author a : authors) {
				perunService.updatePriorityCoeficient(sess, a.getId(), authorshipService.calculateNewRank(a.getAuthorships()));
			}
			log.debug("Category: [{}] updated to Category: [{}]", cat, category);
		}

		return result;
		
	}

	public int deleteCategoryById(Integer id) {
		return categoryDao.deleteCategoryById(id);
	}

}