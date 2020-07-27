package cz.metacentrum.perun.cabinet.api.impl;

import cz.metacentrum.perun.cabinet.api.CabinetManager;
import cz.metacentrum.perun.cabinet.bl.AuthorshipManagerBl;
import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.CabinetManagerBl;
import cz.metacentrum.perun.cabinet.bl.CategoryManagerBl;
import cz.metacentrum.perun.cabinet.bl.PublicationManagerBl;
import cz.metacentrum.perun.cabinet.bl.PublicationSystemManagerBl;
import cz.metacentrum.perun.cabinet.bl.ThanksManagerBl;
import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Authorship;
import cz.metacentrum.perun.cabinet.model.Category;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationForGUI;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.model.Thanks;
import cz.metacentrum.perun.cabinet.model.ThanksForGUI;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import cz.metacentrum.perun.core.impl.Utils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static cz.metacentrum.perun.cabinet.bl.ErrorCodes.NOT_AUTHORIZED;

/**
 * Top-level API implementation for Publication management in Perun.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class CabinetManagerImpl implements CabinetManager {

	private CategoryManagerBl categoryManagerBl;
	private PublicationSystemManagerBl publicationSystemManagerBl;
	private ThanksManagerBl thanksManagerBl;
	private AuthorshipManagerBl authorshipManagerBl;
	private PublicationManagerBl publicationManagerBl;
	private CabinetManagerBl cabinetManagerBl;

	@Autowired
	public void setCategoryManagerBl(CategoryManagerBl categoryManagerBl) {
		this.categoryManagerBl = categoryManagerBl;
	}

	@Autowired
	public void setPublicationSystemManagerBl(PublicationSystemManagerBl publicationSystemManagerBl) {
		this.publicationSystemManagerBl = publicationSystemManagerBl;
	}

	@Autowired
	public void setThanksManagerBl(ThanksManagerBl thanksManagerBl) {
		this.thanksManagerBl = thanksManagerBl;
	}

	@Autowired
	public void setAuthorshipManagerBl(AuthorshipManagerBl authorshipManagerBl) {
		this.authorshipManagerBl = authorshipManagerBl;
	}

	@Autowired
	public void setPublicationManagerBl(PublicationManagerBl publicationManagerBl) {
		this.publicationManagerBl = publicationManagerBl;
	}

	@Autowired
	public void setCabinetManagerBl(CabinetManagerBl cabinetManagerBl) {
		this.cabinetManagerBl = cabinetManagerBl;
	}

	public CategoryManagerBl getCategoryManagerBl() {
		return categoryManagerBl;
	}

	public PublicationSystemManagerBl getPublicationSystemManagerBl() {
		return publicationSystemManagerBl;
	}

	public ThanksManagerBl getThanksManagerBl() {
		return thanksManagerBl;
	}

	public AuthorshipManagerBl getAuthorshipManagerBl() {
		return authorshipManagerBl;
	}

	public PublicationManagerBl getPublicationManagerBl() {
		return publicationManagerBl;
	}

	public CabinetManagerBl getCabinetManagerBl() {
		return cabinetManagerBl;
	}

	// PublicationSystem methods --------------------------


	@Override
	public PublicationSystem createPublicationSystem(PerunSession session, PublicationSystem ps) throws PrivilegeException {

		//Authorization
		if (!AuthzResolver.authorizedInternal(session, "createPublicationSystem_PublicationSystem_policy")) {
			throw new PrivilegeException("createPublicationSystem");
		}

		return getPublicationSystemManagerBl().createPublicationSystem(session, ps);
	}

	@Override
	public PublicationSystem updatePublicationSystem(PerunSession session, PublicationSystem ps) throws CabinetException, PrivilegeException {
		Utils.notNull(ps, "ps");

		//Authorization
		if (!AuthzResolver.authorizedInternal(session, "updatePublicationSystem_PublicationSystem_policy", Collections.singletonList(ps))) {
			throw new PrivilegeException("updatePublicationSystem");
		}

		return getPublicationSystemManagerBl().updatePublicationSystem(session, ps);
	}

	@Override
	public void deletePublicationSystem(PerunSession sess, PublicationSystem ps) throws CabinetException, PrivilegeException {
		Utils.notNull(ps, "ps");

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "deletePublicationSystem_PublicationSystem_policy", Collections.singletonList(ps))) {
			throw new PrivilegeException("deletePublicationSystem");
		}

		getPublicationSystemManagerBl().deletePublicationSystem(ps);
	}

	@Override
	public PublicationSystem getPublicationSystemById(int id) throws CabinetException {
		return getPublicationSystemManagerBl().getPublicationSystemById(id);
	}

	@Override
	public PublicationSystem getPublicationSystemByName(String name) throws CabinetException {
		return getPublicationSystemManagerBl().getPublicationSystemByName(name);
	}

	@Override
	public PublicationSystem getPublicationSystemByNamespace(String namespace) throws CabinetException {
		return getPublicationSystemManagerBl().getPublicationSystemByNamespace(namespace);
	}

	@Override
	public List<PublicationSystem> getPublicationSystems(PerunSession session) {

		List<PublicationSystem> systems = getPublicationSystemManagerBl().getPublicationSystems();

		//Authorization
		if (!AuthzResolver.authorizedInternal(session, "getPublicationSystems_policy")) {
			// clear authz for non-perun admins
			for (PublicationSystem system : systems) {
				system.setUsername(null);
				system.setPassword(null);
			}
		}

		return systems;

	}


	// Category methods --------------------------


	@Override
	public Category createCategory(PerunSession sess, Category category) throws CabinetException, PrivilegeException {

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "createCategory_Category_policy")) {
			throw new PrivilegeException("createCategory");
		}

		return getCategoryManagerBl().createCategory(sess, category);
	}

	@Override
	public Category updateCategory(PerunSession sess, Category category) throws CabinetException, PrivilegeException {
		Utils.notNull(category, "category");

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "updateCategory_Category_policy", Collections.singletonList(category))) {
			throw new PrivilegeException("updateCategory");
		}

		return getCategoryManagerBl().updateCategory(sess, category);
	}

	@Override
	public void deleteCategory(PerunSession sess, Category category) throws CabinetException, PrivilegeException {
		Utils.notNull(category, "category");

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "deleteCategory_Category_policy", Collections.singletonList(category))) {
			throw new PrivilegeException("deleteCategory");
		}

		getCategoryManagerBl().deleteCategory(sess, category);
	}

	@Override
	public List<Category> getCategories() {
		return getCategoryManagerBl().getCategories();
	}

	@Override
	public Category getCategoryById(int id) throws CabinetException {
		return getCategoryManagerBl().getCategoryById(id);
	}


	// Thanks methods ------------------------------


	@Override
	public Thanks createThanks(PerunSession sess, Thanks thanks) throws CabinetException, PrivilegeException {

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "createThanks_Thanks_policy")) {
			throw new PrivilegeException("createThanks");
		}

		return getThanksManagerBl().createThanks(sess, thanks);
	}

	@Override
	public void deleteThanks(PerunSession sess, Thanks thanks) throws CabinetException, PrivilegeException {
		Utils.notNull(thanks, "thanks");

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "deleteThanks_Thanks_policy", Collections.singletonList(thanks)) &&
				(!thanks.getCreatedBy().equalsIgnoreCase(sess.getPerunPrincipal().getActor())) &&
				(thanks.getCreatedByUid() !=(sess.getPerunPrincipal().getUserId()))) {
			throw new PrivilegeException("deleteThanks");
		}

		getThanksManagerBl().deleteThanks(sess, thanks);
	}

	@Override
	public boolean thanksExist(Thanks thanks) {
		return getThanksManagerBl().thanksExist(thanks);
	}

	@Override
	public Thanks getThanksById(int id) throws CabinetException {
		return getThanksManagerBl().getThanksById(id);
	}

	@Override
	public List<Thanks> getThanksByPublicationId(int publicationId) throws CabinetException {
		return getThanksManagerBl().getThanksByPublicationId(publicationId);
	}

	@Override
	public List<ThanksForGUI> getRichThanksByPublicationId(int publicationId) throws CabinetException {
		return getThanksManagerBl().getRichThanksByPublicationId(publicationId);
	}

	@Override
	public List<ThanksForGUI> getRichThanksByUserId(int userId) throws CabinetException {
		return getThanksManagerBl().getRichThanksByUserId(userId);
	}


	// Authorship methods -------------------------------


	@Override
	public Authorship createAuthorship(PerunSession sess, Authorship authorship) throws CabinetException {
		return getAuthorshipManagerBl().createAuthorship(sess, authorship);
	}

	@Override
	public boolean authorshipExists(Authorship authorship) {
		return getAuthorshipManagerBl().authorshipExists(authorship);
	}

	@Override
	public void deleteAuthorship(PerunSession sess, Authorship authorship) throws CabinetException, PrivilegeException {
		Utils.notNull(authorship, "authorship");

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "deleteAuthorship_Authorship_policy", Collections.singletonList(authorship)) &&
				!authorship.getCreatedBy().equalsIgnoreCase(sess.getPerunPrincipal().getActor()) &&
				!authorship.getUserId().equals(sess.getPerunPrincipal().getUser().getId()) &&
				authorship.getCreatedByUid() != sess.getPerunPrincipal().getUserId()) {
			throw new PrivilegeException("You are not allowed to delete authorships you didn't created or which doesn't concern you.");
		}

		getAuthorshipManagerBl().deleteAuthorship(sess, authorship);
	}

	@Override
	public Authorship getAuthorshipById(int id) throws CabinetException {
		return getAuthorshipManagerBl().getAuthorshipById(id);
	}

	@Override
	public List<Authorship> getAuthorshipsByUserId(int id) {
		return getAuthorshipManagerBl().getAuthorshipsByUserId(id);
	}

	@Override
	public List<Authorship> getAuthorshipsByPublicationId(int id) {
		return getAuthorshipManagerBl().getAuthorshipsByPublicationId(id);
	}

	@Override
	public Authorship getAuthorshipByUserAndPublicationId(int userId, int publicationId) throws CabinetException {
		return getAuthorshipManagerBl().getAuthorshipByUserAndPublicationId(userId, publicationId);
	}

	@Override
	public double getRank(int userId) throws CabinetException {
		return getAuthorshipManagerBl().calculateNewRank(userId);
	}

	@Override
	public Author getAuthorById(int id) throws CabinetException {
		return getAuthorshipManagerBl().getAuthorById(id);
	}

	@Override
	public List<Author> getAllAuthors(PerunSession sess) throws CabinetException {

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "getAllAuthors_policy")) {
			throw new CabinetException("You are not authorized to list all authors.", NOT_AUTHORIZED);
		}

		return getAuthorshipManagerBl().getAllAuthors();
	}

	@Override
	public List<Author> getAuthorsByPublicationId(PerunSession session, int id) throws PrivilegeException, CabinetException {

		List<Author> authors = getAuthorshipManagerBl().getAuthorsByPublicationId(id);
		boolean oneOfAuthors = false;
		for (Author author : authors) {
			if (author.getId() == session.getPerunPrincipal().getUserId()) {
				oneOfAuthors = true;
				break;
			}
		}

		//Authorization
		if (AuthzResolver.authorizedInternal(session, "getAuthorsByPublicationId_int_policy")) {
			oneOfAuthors = true;
		}

		if (!oneOfAuthors) {
			// not author, but check if user created publication, then he can list current authors
			Publication publication = getPublicationManagerBl().getPublicationById(id);
			if ((publication.getCreatedByUid() != session.getPerunPrincipal().getUserId()) &&
					!(Objects.equals(publication.getCreatedBy(), session.getPerunPrincipal().getActor()))) {
				throw new PrivilegeException("You are not allowed to see authors of publications you didn't created.");
			}
		}

		return authors;

	}

	@Override
	public List<Author> getAuthorsByAuthorshipId(PerunSession sess, int id) throws CabinetException {
		return getAuthorshipManagerBl().getAuthorsByAuthorshipId(sess, id);
	}

	@Override
	public List<Author> findNewAuthors(PerunSession sess, String searchString) throws CabinetException {

		// Authorization
		if (!AuthzResolver.authorizedInternal(sess, "findNewAuthors_String_policy")) {
			throw new CabinetException("You are not authorized to search for new authors.", NOT_AUTHORIZED);
		}

		return getAuthorshipManagerBl().findNewAuthors(sess, searchString);
	}

	// Publications ----------------------------------------


	@Override
	public Publication createPublication(PerunSession sess, Publication publication) throws CabinetException {
		return getPublicationManagerBl().createPublication(sess, publication);
	}

	@Override
	public boolean publicationExists(Publication publication) {
		return getPublicationManagerBl().publicationExists(publication);
	}

	@Override
	public Publication updatePublication(PerunSession sess, Publication publication) throws CabinetException, PrivilegeException {
		Utils.notNull(publication, "publication");

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "updatePublication_Publication_policy", Collections.singletonList(publication)) &&
				!publication.getCreatedBy().equalsIgnoreCase(sess.getPerunPrincipal().getActor()) &&
				publication.getCreatedByUid() != sess.getPerunPrincipal().getUserId()) {
			// not perun admin or author of record
			try {
				getAuthorsByPublicationId(sess, publication.getId());
			} catch (PrivilegeException ex) {
				throw new PrivilegeException("You are not allowed to update publications you didn't created.");
			}
		}

		return getPublicationManagerBl().updatePublication(sess, publication);
	}

	@Override
	public void deletePublication(PerunSession sess, Publication publication) throws CabinetException, PrivilegeException {
		Utils.notNull(publication, "publication");

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "deletePublication_Publication_policy", Collections.singletonList(publication)) &&
				!publication.getCreatedBy().equalsIgnoreCase(sess.getPerunPrincipal().getActor()) &&
				publication.getCreatedByUid() != sess.getPerunPrincipal().getUserId()) {
			// not perun admin or author of record
			throw new PrivilegeException("You are not allowed to delete publications you didn't created. If you wish, you can remove yourself from authors instead.");
		}

		getPublicationManagerBl().deletePublication(sess, publication);
	}

	@Override
	public Publication getPublicationById(int id) throws CabinetException {
		return getPublicationManagerBl().getPublicationById(id);
	}

	@Override
	public Publication getPublicationByExternalId(int externalId, int publicationSystem) throws CabinetException {
		return getPublicationManagerBl().getPublicationByExternalId(externalId, publicationSystem);
	}

	@Override
	public List<Publication> getPublicationsByCategoryId(int categoryId) {
		return getPublicationManagerBl().getPublicationsByCategoryId(categoryId);
	}

	@Override
	public List<Publication> getPublicationsByFilter(int userId, int yearSince, int yearTill) {
		if (userId < 1) {
			throw new InternalErrorException("ID of publication author must be > 0.");
		}
		if (yearSince > 0 && yearTill > 0) {
			if (yearSince > yearTill) throw new InternalErrorException("yearSince must be before yearTill");
		}
		return getPublicationManagerBl().getPublicationsByFilter(userId, yearSince, yearTill);
	}

	@Override
	public PublicationForGUI getRichPublicationById(int id) throws CabinetException {
		return getPublicationManagerBl().getRichPublicationById(id);
	}

	@Override
	public PublicationForGUI getRichPublicationByExternalId(int externalId, int publicationSystem) throws CabinetException {
		return getPublicationManagerBl().getRichPublicationByExternalId(externalId, publicationSystem);
	}

	@Override
	public List<PublicationForGUI> getRichPublicationsByFilter(Publication p, int userId, int yearSince, int yearTill) {
		return getPublicationManagerBl().getRichPublicationsByFilter(p, userId, yearSince, yearTill);
	}

	@Override
	public void lockPublications(PerunSession sess, boolean lockState, List<Publication> publications) throws PrivilegeException {

		// check input
		if (publications == null || publications.isEmpty()) {
			throw new InternalErrorException("Publications to lock/unlock can't be null");
		}

		//Authorization
		if (!AuthzResolver.authorizedInternal(sess, "lockPublications_boolean_List<Publication>_policy", new ArrayList<>(publications))) {
			throw new PrivilegeException("lockPublications");
		}

		getPublicationManagerBl().lockPublications(lockState, publications);

	}

	@Override
	public List<Publication> findExternalPublications(PerunSession sess, int userId, int yearSince, int yearTill, String pubSysNamespace) throws CabinetException {
		return getCabinetManagerBl().findExternalPublicationsOfUser(sess, userId, yearSince, yearTill, pubSysNamespace);
	}

}
