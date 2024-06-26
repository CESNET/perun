package cz.metacentrum.perun.cabinet.bl.impl;

import cz.metacentrum.perun.cabinet.bl.AuthorshipManagerBl;
import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.CabinetManagerBl;
import cz.metacentrum.perun.cabinet.bl.CategoryManagerBl;
import cz.metacentrum.perun.cabinet.bl.ErrorCodes;
import cz.metacentrum.perun.cabinet.bl.PublicationManagerBl;
import cz.metacentrum.perun.cabinet.bl.PublicationSystemManagerBl;
import cz.metacentrum.perun.cabinet.bl.ThanksManagerBl;
import cz.metacentrum.perun.cabinet.dao.PublicationManagerDao;
import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Authorship;
import cz.metacentrum.perun.cabinet.model.Category;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationForGUI;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.model.Thanks;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Class for handling Publication entity in Cabinet.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class PublicationManagerBlImpl implements PublicationManagerBl {

  private static Logger LOG = LoggerFactory.getLogger(PublicationManagerBlImpl.class);
  private PublicationManagerDao publicationManagerDao;
  private AuthorshipManagerBl authorshipManagerBl;
  private PublicationSystemManagerBl publicationSystemManagerBl;
  private CabinetManagerBl cabinetManagerBl;
  private CategoryManagerBl categoryManagerBl;
  private ThanksManagerBl thanksManagerBl;

  // setters ----------------------------------------

  @Override
  public Publication createPublication(PerunSession sess, Publication p) throws CabinetException {

    if (p.getCreatedDate() == null) {
      p.setCreatedDate(new Date());
    }
    p.setCreatedByUid(sess.getPerunPrincipal().getUserId());

    // check existence
    if (publicationExists(p)) {
      throw new CabinetException("Cannot create duplicate publication: " + p, ErrorCodes.PUBLICATION_ALREADY_EXISTS);
    }

    Publication createdPublication;
    if (p.getExternalId() == 0 && p.getPublicationSystemId() == 0) {
      // get internal pub. system
      PublicationSystem ps = getPublicationSystemManagerBl().getPublicationSystemByName("INTERNAL");
      // There is only one internal system so, get(0) is safe
      p.setPublicationSystemId(ps.getId());
    }

    // if category not present => publication is being imported, set default category id
    if (p.getCategoryId() == 0) {
      int defaultCategoryId = 0;
      List<Category> categories = getCategoryManagerBl().getCategories();
      for (Category c : categories) {
        if (c.getName().equalsIgnoreCase("Ke kontrole")) {
          defaultCategoryId = c.getId();
        }
      }
      if (defaultCategoryId == 0) {
        defaultCategoryId = categories.get(0).getId();
      }
      p.setCategoryId(defaultCategoryId);
    }

    stripLongParams(p);
    createdPublication = getPublicationManagerDao().createPublication(sess, p);

    LOG.debug("{} created.", createdPublication);
    return createdPublication;

  }

  @Override
  public void deletePublication(PerunSession sess, Publication publication) throws CabinetException {

    try {

      // delete authors
      for (Authorship a : getAuthorshipManagerBl().getAuthorshipsByPublicationId(publication.getId())) {
        getAuthorshipManagerBl().deleteAuthorship(sess, a);
      }
      // delete thanks
      for (Thanks t : getThanksManagerBl().getThanksByPublicationId(publication.getId())) {
        getThanksManagerBl().deleteThanks(sess, t);
      }

      // delete publication
      getPublicationManagerDao().deletePublication(publication);
      LOG.debug("{} deleted.", publication);

      // publications without authors are: "to be deleted by perun admin"

    } catch (DataIntegrityViolationException ex) {
      throw new CabinetException(
          "Can't delete publication with Authors or Thanks. Please remove them first in order to delete publication.",
          ErrorCodes.PUBLICATION_HAS_AUTHORS_OR_THANKS);
    } catch (PerunException ex) {
      throw new CabinetException(ErrorCodes.PERUN_EXCEPTION, ex);
    }

  }

  public AuthorshipManagerBl getAuthorshipManagerBl() {
    return authorshipManagerBl;
  }

  public CabinetManagerBl getCabinetManagerBl() {
    return cabinetManagerBl;
  }

  public CategoryManagerBl getCategoryManagerBl() {
    return categoryManagerBl;
  }

  @Override
  public Publication getPublicationByExternalId(int externalId, int publicationSystem) throws CabinetException {
    return getPublicationManagerDao().getPublicationByExternalId(externalId, publicationSystem);
  }

  @Override
  public Publication getPublicationById(int id) throws CabinetException {
    return getPublicationManagerDao().getPublicationById(id);
  }

  public PublicationManagerDao getPublicationManagerDao() {
    return publicationManagerDao;
  }

  public PublicationSystemManagerBl getPublicationSystemManagerBl() {
    return publicationSystemManagerBl;
  }

  @Override
  public List<Publication> getPublicationsByCategoryId(int categoryId) {
    return getPublicationManagerDao().getPublicationsByCategoryId(categoryId);
  }

  @Override
  public List<Publication> getPublicationsByFilter(int userId, int yearSince, int yearTill) {
    List<Publication> publications = getPublicationManagerDao().getPublicationsByFilter(userId, yearSince, yearTill);
    for (Publication pub : publications) {
      pub.setAuthors(getAuthorshipManagerBl().getAuthorsByPublicationId(pub.getId()));
    }
    return publications;
  }

  // business methods --------------------------------

  @Override
  public PublicationForGUI getRichPublicationByExternalId(int externalId, int publicationSystem)
      throws CabinetException {
    return getPublicationManagerDao().getRichPublicationByExternalId(externalId, publicationSystem);
  }

  @Override
  public PublicationForGUI getRichPublicationById(int id) throws CabinetException {
    return getPublicationManagerDao().getRichPublicationById(id);
  }

  @Override
  public List<PublicationForGUI> getRichPublicationsByFilter(Publication p, int userId, int yearSince, int yearTill) {
    List<PublicationForGUI> publications =
        getPublicationManagerDao().getRichPublicationsByFilter(p, userId, yearSince, yearTill);
    if (userId != 0) {
      // add rest of publication authors, which are omitted by select conditions (userId)
      for (PublicationForGUI pub : publications) {
        pub.setAuthors(getAuthorshipManagerBl().getAuthorsByPublicationId(pub.getId()));
      }
    }
    return publications;
  }

  public ThanksManagerBl getThanksManagerBl() {
    return thanksManagerBl;
  }

  @Override
  public void lockPublications(boolean lockState, List<Publication> publications) {
    getPublicationManagerDao().lockPublications(lockState, publications);
  }

  @Override
  public boolean publicationExists(Publication publication) {
    if (publication.getId() != 0) {
      try {
        getPublicationManagerDao().getPublicationById(publication.getId());
        return true;
      } catch (CabinetException ex) {
        // ignore
      }
    }
    if (publication.getExternalId() != 0 && publication.getPublicationSystemId() != 0) {
      try {
        getPublicationManagerDao().getPublicationByExternalId(publication.getExternalId(),
            publication.getPublicationSystemId());
        return true;
      } catch (CabinetException ex) {
        // ignore
      }
    }
    return false;
  }

  @Autowired
  public void setAuthorshipManagerBl(AuthorshipManagerBl authorshipManagerBl) {
    this.authorshipManagerBl = authorshipManagerBl;
  }

  @Autowired
  public void setCabinetManagerBl(CabinetManagerBl cabinetManagerBl) {
    this.cabinetManagerBl = cabinetManagerBl;
  }

  @Autowired
  public void setCategoryManagerBl(CategoryManagerBl categoryManagerBl) {
    this.categoryManagerBl = categoryManagerBl;
  }

  @Autowired
  public void setPublicationManagerDao(PublicationManagerDao publicationManagerDao) {
    this.publicationManagerDao = publicationManagerDao;
  }

  @Autowired
  public void setPublicationSystemManagerBl(PublicationSystemManagerBl pubService) {
    this.publicationSystemManagerBl = pubService;
  }

  @Autowired
  public void setThanksManagerBl(ThanksManagerBl thanksManagerBl) {
    this.thanksManagerBl = thanksManagerBl;
  }

  /**
   * Strip long params in publication object to prevent SQL errors on columns
   *
   * @param p publication to check
   */
  private void stripLongParams(Publication p) {

    if (p.getTitle() != null && p.getTitle().length() > 1024) {
      p.setTitle(p.getTitle().substring(0, 1024));
    }
    if (p.getMain() != null && p.getMain().length() > 4000) {
      p.setMain(p.getMain().substring(0, 4000));
    }
    if (p.getIsbn() != null && p.getIsbn().length() > 32) {
      p.setIsbn(p.getIsbn().substring(0, 32));
    }
    if (p.getDoi() != null && p.getDoi().length() > 256) {
      p.setDoi(p.getDoi().substring(0, 256));
    }
  }

  @Override
  public Publication updatePublication(PerunSession sess, Publication publication) throws CabinetException {

    if (publication.getId() == 0 || publication.getExternalId() == 0 || publication.getPublicationSystemId() == 0) {
      // such publication can't exists
      throw new CabinetException("Publication doesn't exists: " + publication, ErrorCodes.PUBLICATION_NOT_EXISTS);
    }

    // strip long params in new publication
    stripLongParams(publication);

    //don't create already existing publication (same id or externalId&&pubSysId)
    Publication dbPublication =
        getPublicationByExternalId(publication.getExternalId(), publication.getPublicationSystemId());
    if (publication.getId() != (dbPublication.getId())) {
      throw new CabinetException("Cannot update to duplicate publication: " + publication,
          ErrorCodes.PUBLICATION_ALREADY_EXISTS);
    }

    // save old pub
    Publication oldPub = getPublicationById(publication.getId());

    // update publication in DB
    getPublicationManagerDao().updatePublication(sess, publication);
    LOG.debug("{} updated.", publication);

    // if updated and rank or category was changed
    if ((oldPub.getRank() != publication.getRank()) || (oldPub.getCategoryId() != publication.getCategoryId())) {
      synchronized (CabinetManagerBlImpl.class) {
        // update coefficient for all it's authors
        List<Author> authors = getAuthorshipManagerBl().getAuthorsByPublicationId(oldPub.getId());
        for (Author a : authors) {
          getCabinetManagerBl().updatePriorityCoefficient(sess, a.getId(),
              getAuthorshipManagerBl().calculateNewRank(a.getAuthorships()));
        }
      }
    }

    return publication;

  }

}
