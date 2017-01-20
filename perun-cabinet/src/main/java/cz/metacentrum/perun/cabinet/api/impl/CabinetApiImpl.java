package cz.metacentrum.perun.cabinet.api.impl;

import java.util.List;

import cz.metacentrum.perun.cabinet.api.CabinetApi;
import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.PublicationForGUI;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.AuthorshipManagerBl;
import cz.metacentrum.perun.cabinet.bl.CabinetManagerBl;
import cz.metacentrum.perun.cabinet.bl.PerunManagerBl;
import cz.metacentrum.perun.cabinet.bl.PublicationManagerBl;
import cz.metacentrum.perun.cabinet.bl.SortParam;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

/**
 * Default implementation of ICabinetApi interface. It delegates all requests to
 * appropriate services managers.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CabinetApiImpl implements CabinetApi {

	private static final long serialVersionUID = 1L;

	private CabinetManagerBl cabinetService;
	private PublicationManagerBl publicationService;
	private AuthorshipManagerBl authorshipService;
	private PerunManagerBl perunService;

	// setters ==============================================

	public void setCabinetService(CabinetManagerBl cabinetService) {
		this.cabinetService = cabinetService;
	}

	public void setPublicationService(PublicationManagerBl publicationService) {
		this.publicationService = publicationService;
	}

	public void setAuthorshipService(AuthorshipManagerBl authorshipService) {
		this.authorshipService = authorshipService;
	}

	public void setPerunService(PerunManagerBl perunService) {
		this.perunService = perunService;
	}

	// delegate methods =======================================


	public List<Publication> findExternalPublications(PerunSession sess, int userId, int yearSince, int yearTill, String pubSysNamespace) throws CabinetException, InternalErrorException {
		return cabinetService.findExternalPublicationsOfUser(sess, userId, yearSince, yearTill, pubSysNamespace);
	}

	public List<Owner> findAllOwners(PerunSession sess) throws CabinetException {
		return perunService.findAllOwners(sess);
	}

	public boolean publicationExists(Publication p) {
		return publicationService.publicationExists(p);
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

	public List<PublicationForGUI> findRichPublicationsOfAuthor(Integer id) throws CabinetException {
		return publicationService.findRichPublicationsByGUIFilter(null, id, 0, 0);
	}

	public Owner findOwnerById(PerunSession sess, Integer id) throws CabinetException {
		return perunService.findOwnerById(sess, id);
	}

	public List<Author> findAuthorsByAuthorshipId(PerunSession sess, Integer id) throws CabinetException, InternalErrorException {
		return authorshipService.getAuthorsByAuthorshipId(sess, id);
	}

	public Publication findPublicationById(Integer publicationId) {
		return publicationService.findPublicationById(publicationId);
	}

	public PublicationForGUI findRichPublicationById(Integer publicationId) {
		return publicationService.findRichPublicationById(publicationId);
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

	public int updatePublicationById(PerunSession sess, Publication publication) throws CabinetException, InternalErrorException {
		return publicationService.updatePublicationById(sess, publication);
	}

	public int deletePublicationById(PerunSession sess, Integer id) throws CabinetException {
		return publicationService.deletePublicationById(sess, id);
	}

	public int lockPublications(PerunSession sess, boolean lockState, List<Publication> pubs) throws CabinetException {
		return publicationService.lockPublications(sess, lockState, pubs);
	}

}
