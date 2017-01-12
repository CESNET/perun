package cz.metacentrum.perun.cabinet.api.impl;

import cz.metacentrum.perun.cabinet.api.CabinetManager;
import cz.metacentrum.perun.cabinet.bl.AuthorshipManagerBl;
import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.CategoryManagerBl;
import cz.metacentrum.perun.cabinet.bl.PublicationSystemManagerBl;
import cz.metacentrum.perun.cabinet.bl.ThanksManagerBl;
import cz.metacentrum.perun.cabinet.model.Authorship;
import cz.metacentrum.perun.cabinet.model.Category;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.model.Thanks;
import cz.metacentrum.perun.cabinet.model.ThanksForGUI;
import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Role;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
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

}
