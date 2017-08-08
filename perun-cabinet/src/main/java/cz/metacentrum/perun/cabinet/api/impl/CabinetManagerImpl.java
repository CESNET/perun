package cz.metacentrum.perun.cabinet.api.impl;

import cz.metacentrum.perun.cabinet.api.CabinetManager;
import cz.metacentrum.perun.cabinet.bl.AuthorshipManagerBl;
import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.CabinetManagerBl;
import cz.metacentrum.perun.cabinet.bl.CategoryManagerBl;
import cz.metacentrum.perun.cabinet.bl.ErrorCodes;
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
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.blImpl.AuthzResolverBlImpl;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

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
	public PublicationSystem createPublicationSystem(PerunSession session, PublicationSystem ps) throws InternalErrorException, PrivilegeException {
		if (!AuthzResolverBlImpl.isAuthorized(session, Role.PERUNADMIN)) {
			throw new PrivilegeException("createPublicationSystem");
		}
		return getPublicationSystemManagerBl().createPublicationSystem(session, ps);
	}

	@Override
	public PublicationSystem updatePublicationSystem(PerunSession session, PublicationSystem ps) throws CabinetException, InternalErrorException, PrivilegeException {
		if (!AuthzResolverBlImpl.isAuthorized(session, Role.PERUNADMIN)) {
			throw new PrivilegeException("updatePublicationSystem");
		}
		return getPublicationSystemManagerBl().updatePublicationSystem(session, ps);
	}

	@Override
	public void deletePublicationSystem(PerunSession sess, PublicationSystem ps) throws CabinetException, InternalErrorException, PrivilegeException {
		if (!AuthzResolverBlImpl.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException("deletePublicationSystem");
		}
		getPublicationSystemManagerBl().deletePublicationSystem(ps);
	}

	@Override
	public PublicationSystem getPublicationSystemById(int id) throws InternalErrorException, CabinetException {
		return getPublicationSystemManagerBl().getPublicationSystemById(id);
	}

	@Override
	public PublicationSystem getPublicationSystemByName(String name) throws InternalErrorException, CabinetException {
		return getPublicationSystemManagerBl().getPublicationSystemByName(name);
	}

	@Override
	public PublicationSystem getPublicationSystemByNamespace(String namespace) throws InternalErrorException, CabinetException {
		return getPublicationSystemManagerBl().getPublicationSystemByNamespace(namespace);
	}

	@Override
	public List<PublicationSystem> getPublicationSystems() throws InternalErrorException {
		return getPublicationSystemManagerBl().getPublicationSystems();
	}


	// Category methods --------------------------


	@Override
	public Category createCategory(PerunSession sess, Category category) throws InternalErrorException, CabinetException, PrivilegeException {
		if (!AuthzResolverBlImpl.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException("createCategory");
		}
		return getCategoryManagerBl().createCategory(sess, category);
	}

	@Override
	public Category updateCategory(PerunSession sess, Category category) throws InternalErrorException, CabinetException, PrivilegeException {
		if (!AuthzResolverBlImpl.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException("updateCategory");
		}
		return getCategoryManagerBl().updateCategory(sess, category);
	}

	@Override
	public void deleteCategory(PerunSession sess, Category category) throws InternalErrorException, CabinetException, PrivilegeException {
		if (!AuthzResolverBlImpl.isAuthorized(sess, Role.PERUNADMIN)) {
			throw new PrivilegeException("deleteCategory");
		}
		getCategoryManagerBl().deleteCategory(sess, category);
	}

	@Override
	public List<Category> getCategories() throws InternalErrorException {
		return getCategoryManagerBl().getCategories();
	}

	@Override
	public Category getCategoryById(int id) throws CabinetException, InternalErrorException {
		return getCategoryManagerBl().getCategoryById(id);
	}


	// Thanks methods ------------------------------


	@Override
	public Thanks createThanks(PerunSession sess, Thanks thanks) throws InternalErrorException, CabinetException, PrivilegeException {
		if (!AuthzResolverBlImpl.isAuthorized(sess, Role.SELF)) {
			throw new PrivilegeException("createThanks");
		}
		return getThanksManagerBl().createThanks(sess, thanks);
	}

	@Override
	public void deleteThanks(PerunSession sess, Thanks thanks) throws InternalErrorException, CabinetException, PrivilegeException {
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN) &&
				(!thanks.getCreatedBy().equalsIgnoreCase(sess.getPerunPrincipal().getActor())) &&
				(thanks.getCreatedByUid() !=(sess.getPerunPrincipal().getUserId()))) {
			throw new PrivilegeException("deleteThanks");
		}
		getThanksManagerBl().deleteThanks(sess, thanks);
	}

	@Override
	public boolean thanksExist(Thanks thanks) throws InternalErrorException {
		return getThanksManagerBl().thanksExist(thanks);
	}

	@Override
	public Thanks getThanksById(int id) throws CabinetException, InternalErrorException {
		return getThanksManagerBl().getThanksById(id);
	}

	@Override
	public List<Thanks> getThanksByPublicationId(int publicationId) throws CabinetException, InternalErrorException {
		return getThanksManagerBl().getThanksByPublicationId(publicationId);
	}

	@Override
	public List<ThanksForGUI> getRichThanksByPublicationId(int publicationId) throws CabinetException, InternalErrorException {
		return getThanksManagerBl().getRichThanksByPublicationId(publicationId);
	}

	@Override
	public List<ThanksForGUI> getRichThanksByUserId(int userId) throws CabinetException, InternalErrorException {
		return getThanksManagerBl().getRichThanksByUserId(userId);
	}


	// Authorship methods -------------------------------


	@Override
	public Authorship createAuthorship(PerunSession sess, Authorship authorship) throws CabinetException, InternalErrorException {
		return getAuthorshipManagerBl().createAuthorship(sess, authorship);
	}

	@Override
	public boolean authorshipExists(Authorship authorship) throws InternalErrorException {
		return getAuthorshipManagerBl().authorshipExists(authorship);
	}

	@Override
	public void deleteAuthorship(PerunSession sess, Authorship authorship) throws InternalErrorException, CabinetException, PrivilegeException {
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN) &&
				!authorship.getCreatedBy().equalsIgnoreCase(sess.getPerunPrincipal().getActor()) &&
				!authorship.getUserId().equals(sess.getPerunPrincipal().getUser().getId()) &&
				authorship.getCreatedByUid() != sess.getPerunPrincipal().getUserId()) {
			throw new PrivilegeException("You are not allowed to delete authorships you didn't created or which doesn't concern you.");
		}
		getAuthorshipManagerBl().deleteAuthorship(sess, authorship);
	}

	@Override
	public Authorship getAuthorshipById(int id) throws CabinetException, InternalErrorException {
		return getAuthorshipManagerBl().getAuthorshipById(id);
	}

	@Override
	public List<Authorship> getAuthorshipsByUserId(int id) throws InternalErrorException {
		return getAuthorshipManagerBl().getAuthorshipsByUserId(id);
	}

	@Override
	public List<Authorship> getAuthorshipsByPublicationId(int id) throws InternalErrorException {
		return getAuthorshipManagerBl().getAuthorshipsByPublicationId(id);
	}

	@Override
	public Authorship getAuthorshipByUserAndPublicationId(int userId, int publicationId) throws CabinetException, InternalErrorException {
		return getAuthorshipManagerBl().getAuthorshipByUserAndPublicationId(userId, publicationId);
	}

	@Override
	public double getRank(int userId) throws InternalErrorException, CabinetException {
		return getAuthorshipManagerBl().calculateNewRank(userId);
	}

	@Override
	public Author getAuthorById(int id) throws CabinetException, InternalErrorException {
		return getAuthorshipManagerBl().getAuthorById(id);
	}

	@Override
	public List<Author> getAllAuthors() throws InternalErrorException {
		return getAuthorshipManagerBl().getAllAuthors();
	}

	@Override
	public List<Author> getAuthorsByPublicationId(int id) throws InternalErrorException {
		return getAuthorshipManagerBl().getAuthorsByPublicationId(id);
	}

	@Override
	public List<Author> getAuthorsByAuthorshipId(PerunSession sess, int id) throws CabinetException, InternalErrorException {
		return getAuthorshipManagerBl().getAuthorsByAuthorshipId(sess, id);
	}


	// Publications ----------------------------------------


	@Override
	public Publication createPublication(PerunSession sess, Publication publication) throws CabinetException, InternalErrorException {
		return getPublicationManagerBl().createPublication(sess, publication);
	}

	@Override
	public boolean publicationExists(Publication publication) throws InternalErrorException {
		return getPublicationManagerBl().publicationExists(publication);
	}

	@Override
	public Publication updatePublication(PerunSession sess, Publication publication) throws CabinetException, InternalErrorException, PrivilegeException {
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN) &&
				!publication.getCreatedBy().equalsIgnoreCase(sess.getPerunPrincipal().getActor()) &&
				publication.getCreatedByUid() != sess.getPerunPrincipal().getUserId()) {
			// not perun admin or author of record

			List<Author> authors = getAuthorsByPublicationId(publication.getId());
			boolean oneOfAuthors = false;
			for (Author author : authors) {
				if (author.getId() == sess.getPerunPrincipal().getUserId()) {
					oneOfAuthors = true;
					break;
				}
			}

			if (!oneOfAuthors) throw new PrivilegeException("You are not allowed to update publications you didn't created.");

		}
		return getPublicationManagerBl().updatePublication(sess, publication);
	}

	@Override
	public void deletePublication(PerunSession sess, Publication publication) throws CabinetException, InternalErrorException, PrivilegeException {
		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN) &&
				!publication.getCreatedBy().equalsIgnoreCase(sess.getPerunPrincipal().getActor()) &&
				publication.getCreatedByUid() != sess.getPerunPrincipal().getUserId()) {
			// not perun admin or author of record
			throw new PrivilegeException("You are not allowed to delete publications you didn't created. If you wish, you can remove yourself from authors instead.");
		}
		getPublicationManagerBl().deletePublication(sess, publication);
	}

	@Override
	public Publication getPublicationById(int id) throws CabinetException, InternalErrorException {
		return getPublicationManagerBl().getPublicationById(id);
	}

	@Override
	public Publication getPublicationByExternalId(int externalId, int publicationSystem) throws CabinetException, InternalErrorException {
		return getPublicationManagerBl().getPublicationByExternalId(externalId, publicationSystem);
	}

	@Override
	public List<Publication> getPublicationsByCategoryId(int categoryId) throws InternalErrorException {
		return getPublicationManagerBl().getPublicationsByCategoryId(categoryId);
	}

	@Override
	public PublicationForGUI getRichPublicationById(int id) throws CabinetException, InternalErrorException {
		return getPublicationManagerBl().getRichPublicationById(id);
	}

	@Override
	public PublicationForGUI getRichPublicationByExternalId(int externalId, int publicationSystem) throws CabinetException, InternalErrorException {
		return getPublicationManagerBl().getRichPublicationByExternalId(externalId, publicationSystem);
	}

	@Override
	public List<PublicationForGUI> getRichPublicationsByFilter(Publication p, int userId, int yearSince, int yearTill) throws InternalErrorException {
		return getPublicationManagerBl().getRichPublicationsByFilter(p, userId, yearSince, yearTill);
	}

	@Override
	public void lockPublications(PerunSession sess, boolean lockState, List<Publication> publications) throws InternalErrorException, PrivilegeException {

		if (!AuthzResolver.isAuthorized(sess, Role.PERUNADMIN)) {
				throw new PrivilegeException("lockPublications");
		}

		// check input
		if (publications == null || publications.isEmpty()) {
			throw new InternalErrorException("Publications to lock/unlock can't be null");
		}

		getPublicationManagerBl().lockPublications(lockState, publications);

	}

	@Override
	public List<Publication> findExternalPublications(PerunSession sess, int userId, int yearSince, int yearTill, String pubSysNamespace) throws CabinetException, InternalErrorException {
		return getCabinetManagerBl().findExternalPublicationsOfUser(sess, userId, yearSince, yearTill, pubSysNamespace);
	}

}
