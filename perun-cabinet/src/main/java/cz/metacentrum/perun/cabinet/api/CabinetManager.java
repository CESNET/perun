package cz.metacentrum.perun.cabinet.api;

import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Authorship;
import cz.metacentrum.perun.cabinet.model.Category;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.core.api.PerunSession;

import java.util.List;

/**
 * Top-level API for Publication management in Perun.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public interface CabinetManager {

	// publication systems

	PublicationSystem createPublicationSystem(PerunSession session, PublicationSystem system) throws CabinetException;

	PublicationSystem updatePublicationSystem(PerunSession session, PublicationSystem system) throws CabinetException;

	PublicationSystem getPublicationSystemById(PerunSession session, int id) throws CabinetException;

	List<PublicationSystem> getPublicationSystems(PerunSession session) throws CabinetException;

	void deletePublicationSystem(PerunSession session, int id) throws CabinetException;

	// category

	Category createCategory(PerunSession session, Category category) throws CabinetException;

	Category updateCategory(PerunSession session, Category category) throws CabinetException;

	Category getCategoryById(PerunSession session, int id) throws CabinetException;

	List<Category> getCategories(PerunSession session) throws CabinetException;

	void deleteCategory(PerunSession session, int id) throws CabinetException;

	// authorship

	Authorship createAuthorship(PerunSession session, Authorship authorship) throws CabinetException;

	Authorship updateAuthorship(PerunSession session, Authorship authorship) throws CabinetException;

	Authorship getAuthorshipById(PerunSession session, int id) throws CabinetException;

	List<Authorship> getAuthorships(PerunSession session) throws CabinetException;

	List<Authorship> getAuthorshipsForPublication(PerunSession session, Publication publication) throws CabinetException;

	List<Authorship> getAuthorshipsForAuthor(PerunSession session, Author author) throws CabinetException;

	void deleteAuthorship(PerunSession session, int id) throws CabinetException;

	void deleteAuthorshipsForPublication(PerunSession session, Publication publication) throws CabinetException;

	void deleteAuthorshipsForAuthor(PerunSession session, Author author) throws CabinetException;

	// thanks

	// Authors (listing of users)

	// publication

	Publication createPublication(PerunSession session, Publication publication) throws CabinetException;

	Publication updatePublication(PerunSession session, Publication publication) throws CabinetException;

	Publication getPublicationById(PerunSession session, int id) throws CabinetException;

	List<Publication> getPublications(PerunSession session) throws CabinetException;

	List<Publication> findPublications(PerunSession session) throws CabinetException;

	void deletePublication(PerunSession session, int id) throws CabinetException;

	// search external system

	List<Publication> findPublicationsInExtSource(PerunSession session, int userId, int yearSince, int yearTill, String pubSysNamespace) throws CabinetException;

}
