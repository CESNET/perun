package cz.metacentrum.perun.cabinet.api.impl;

import java.util.Date;
import java.util.List;

import cz.metacentrum.perun.cabinet.api.ICabinetApi;
import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Authorship;
import cz.metacentrum.perun.cabinet.model.Category;
import cz.metacentrum.perun.cabinet.model.PublicationForGUI;
import cz.metacentrum.perun.cabinet.model.ThanksForGUI;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.model.Thanks;
import cz.metacentrum.perun.cabinet.service.CabinetException;
import cz.metacentrum.perun.cabinet.service.ErrorCodes;
import cz.metacentrum.perun.cabinet.service.IAuthorService;
import cz.metacentrum.perun.cabinet.service.IAuthorshipService;
import cz.metacentrum.perun.cabinet.service.ICabinetService;
import cz.metacentrum.perun.cabinet.service.ICategoryService;
import cz.metacentrum.perun.cabinet.service.IPerunService;
import cz.metacentrum.perun.cabinet.service.IPublicationService;
import cz.metacentrum.perun.cabinet.service.IPublicationSystemService;
import cz.metacentrum.perun.cabinet.service.IThanksService;
import cz.metacentrum.perun.cabinet.service.SortParam;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.PerunException;

/**
 * Default implementation of ICabinetApi interface. It delegates all requests to
 * appropriate services managers.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CabinetApiImpl implements ICabinetApi {

	private static final long serialVersionUID = 1L;

	private ICabinetService cabinetService;
	private IPublicationService publicationService;
	private IAuthorService authorService;
	private IAuthorshipService authorshipService;
	private IThanksService thanksService;
	private ICategoryService categoryService;
	private IPerunService perunService;
	private IPublicationSystemService publicationSystemService;

	// setters ==============================================

	public void setCabinetService(ICabinetService cabinetService) {
		this.cabinetService = cabinetService;
	}

	public void setPublicationService(IPublicationService publicationService) {
		this.publicationService = publicationService;
	}

	public void setAuthorService(IAuthorService authorService) {
		this.authorService = authorService;
	}

	public void setAuthorshipService(IAuthorshipService authorshipService) {
		this.authorshipService = authorshipService;
	}

	public void setThanksService(IThanksService thanksService) {
		this.thanksService = thanksService;
	}

	public void setCategoryService(ICategoryService categoryService) {
		this.categoryService = categoryService;
	}

	public void setPerunService(IPerunService perunService) {
		this.perunService = perunService;
	}

	public void setPublicationSystemService(IPublicationSystemService publicationSystemService) {
		this.publicationSystemService = publicationSystemService;
	}

	// delegate methods =======================================


	public List<Publication> findExternalPublications(PerunSession sess, int userId, int yearSince, int yearTill, String pubSysNamespace) throws CabinetException {
		return cabinetService.findExternalPublicationsOfUser(sess, userId, yearSince, yearTill, pubSysNamespace);
	}

	public List<Owner> findAllOwners(PerunSession sess) throws CabinetException {
		return perunService.findAllOwners(sess);
	}

	public int createPublication(PerunSession sess, Publication p) throws CabinetException {
		return this.publicationService.createPublication(sess, p);
	}

	public int createAuthorship(PerunSession sess, Authorship r) throws CabinetException {
		return authorshipService.createAuthorship(sess, r);
	}

	public List<Category> findAllCategories() {
		return categoryService.findAllCategories();
	}

	public boolean publicationExists(Publication p) {
		return publicationService.publicationExists(p);
	}

	public boolean authorExists(Author a) {
		return authorService.authorExists(a);
	}

	public boolean authorshipExists(Authorship report) {
		return authorshipService.authorshipExists(report);
	}

	public List<Publication> findPublicationsByFilter(Publication p) {
		return publicationService.findPublicationsByFilter(p);
	}

	public List<PublicationForGUI> findRichPublicationsByFilter(Publication p, Integer userId) {
		return publicationService.findRichPublicationsByFilter(p, userId);
	}

	public List<PublicationForGUI> findRichPublicationsByGUIFilter(Publication p, Integer userId, int yearSince, int yearTill) {
		return publicationService.findRichPublicationsByGUIFilter(p, userId, yearSince, yearTill);
	}

	public boolean thanksExists(Thanks t) {
		return thanksService.thanksExists(t);
	}

	public int createThanks(PerunSession sess, Thanks t) throws CabinetException {
		return thanksService.createThanks(sess, t);
	}

	public List<Authorship> findAuthorshipsByFilter(Authorship report) {
		return authorshipService.findAuthorshipsByFilter(report);
	}

	public List<PublicationForGUI> findRichPublicationsOfAuthor(Integer id) throws CabinetException {
		return publicationService.findRichPublicationsByGUIFilter(null, id, 0, 0);
	}

	public List<Thanks> findThanksByFilter(Thanks t) {
		return thanksService.findThanksByFilter(t);
	}

	public List<Thanks> findThanksByPublicationId(int id) {
		return thanksService.findThanksByPublicationId(id);
	}

	public List<ThanksForGUI> findRichThanksByPublicationId(int id) {
		return thanksService.findRichThanksByPublicationId(id);
	}

	public Thanks findThanksById(int id) {
		return thanksService.findThanksById(id);
	}

	public Category findCategoryById(Integer categoryId) {
		return categoryService.findCategoryById(categoryId);
	}

	public Double getRank(Integer userId){
		return authorshipService.calculateNewRank(userId);
	}

	public Date getLastReportDate(Integer userId) {
		return authorshipService.getLastCreatedAuthorshipDate(userId);
	}

	public Owner findOwnerById(PerunSession sess, Integer id) throws CabinetException {
		return perunService.findOwnerById(sess, id);
	}

	public List<Author> findAuthorsByAuthorshipId(PerunSession sess, Integer id) throws CabinetException {
		return authorshipService.findAuthorsByAuthorshipId(sess, id);
	}

	public List<Author> findAuthorsByPublicationId(Integer id) {
		return authorService.findAuthorsByPublicationId(id);
	}

	public List<Authorship> findAllAuthorships() {
		return authorshipService.findAllAuthorships();
	}

	public int getAuthorshipsCount() {
		return authorshipService.getAuthorshipsCount();
	}

	public List<Authorship> findAuthorshipsByFilter(Authorship report, SortParam sortParam) {
		return authorshipService.findAuthorshipsByFilter(report, sortParam);
	}

	public Author findAuthorById(Integer userId) {
		return authorService.findAuthorByUserId(userId);
	}

	public Publication findPublicationById(Integer publicationId) {
		return publicationService.findPublicationById(publicationId);
	}

	public PublicationForGUI findRichPublicationById(Integer publicationId) {
		return publicationService.findRichPublicationById(publicationId);
	}

	public Authorship findAuthorshipById(Integer id) {
		return authorshipService.findAuthorshipById(id);
	}

	public int updateAuthorship(PerunSession sess, Authorship report) throws CabinetException {
		return authorshipService.updateAuthorship(sess, report);
	}

	public int deleteAuthorshipById(PerunSession sess, Integer id) throws CabinetException {
		return authorshipService.deleteAuthorshipById(sess, id);
	}

	public List<Publication> findAllPublications() {
		return publicationService.findAllPublications();
	}

	public List<PublicationForGUI> findAllRichPublications() throws CabinetException {
		return publicationService.findAllRichPublications();
	}

	public List<Publication> findPublicationsByFilter(Publication publication,
			SortParam sp) {
		return publicationService.findPublicationsByFilter(publication, sp);
	}

	public int getPublicationsCount() {
		return publicationService.getPublicationsCount();
	}

	public PublicationSystem findPublicationSystemById(
			Integer publicationSystemId) {
		return publicationSystemService.findPublicationSystemById(publicationSystemId);
			}

	public int updatePublicationById(PerunSession sess, Publication publication) throws CabinetException {
		return publicationService.updatePublicationById(sess, publication);
	}

	public int deletePublicationById(PerunSession sess, Integer id) throws CabinetException {
		return publicationService.deletePublicationById(sess, id);
	}

	public int getCategoriesCount() {
		return categoryService.getCount();
	}

	public int updateCategoryById(PerunSession sess, Category category) throws CabinetException {
		return categoryService.updateCategoryById(sess, category);
	}

	public int deleteCategoryById(Integer id) {
		return categoryService.deleteCategoryById(id);
	}

	public int createCategory(Category category) {
		return categoryService.createCategory(category);
	}

	public List<Author> findAllAuthors() {
		return authorService.findAllAuthors();
	}

	public int getAuthorsCount() {
		return authorService.getAuthorsCount();
	}

	public int deleteThanksById(PerunSession sess, Integer id) throws CabinetException {
		return thanksService.deleteThanksById(sess, id);
	}

	public List<PublicationSystem> findAllPublicationSystems() {
		return publicationSystemService.findAllPublicationSystems();
	}

	public int lockPublications(PerunSession sess, boolean lockState, List<Publication> pubs) throws CabinetException {
		return publicationService.lockPublications(sess, lockState, pubs);
	}

	@Override
	public void recalculateThanksAttribute(PerunSession sess) throws CabinetException {

		try {
			if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
				throw new CabinetException("You are not allowed to delete authorships you didn't created or which doesn't concern you.", ErrorCodes.NOT_AUTHORIZED);
			}
		} catch (PerunException pe) {
			throw new CabinetException(ErrorCodes.PERUN_EXCEPTION, pe);
		}

		List<Author> list = authorService.findAllAuthors();
		for (Author a : list) {
			perunService.setThanksAttribute(a.getId());
		}

	}

}
