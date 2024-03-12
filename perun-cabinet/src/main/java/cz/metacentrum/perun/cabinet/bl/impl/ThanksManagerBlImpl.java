package cz.metacentrum.perun.cabinet.bl.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import cz.metacentrum.perun.cabinet.bl.AuthorshipManagerBl;
import cz.metacentrum.perun.cabinet.bl.CabinetManagerBl;
import cz.metacentrum.perun.cabinet.dao.ThanksManagerDao;
import cz.metacentrum.perun.cabinet.model.Author;
import cz.metacentrum.perun.cabinet.model.Thanks;
import cz.metacentrum.perun.cabinet.model.ThanksForGUI;
import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.ErrorCodes;
import cz.metacentrum.perun.cabinet.bl.ThanksManagerBl;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class for handling Thanks entity in Cabinet.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ThanksManagerBlImpl implements ThanksManagerBl {

  private static Logger log = LoggerFactory.getLogger(ThanksManagerBlImpl.class);
  private ThanksManagerDao thanksManagerDao;
  private AuthorshipManagerBl authorshipManagerBl;
  private CabinetManagerBl cabinetManagerBl;

  // setters -------------------------

  public ThanksManagerDao getThanksManagerDao() {
    return thanksManagerDao;
  }

  @Autowired
  public void setThanksManagerDao(ThanksManagerDao thanksManagerDao) {
    this.thanksManagerDao = thanksManagerDao;
  }

  public AuthorshipManagerBl getAuthorshipManagerBl() {
    return authorshipManagerBl;
  }

  @Autowired
  public void setAuthorshipManagerBl(AuthorshipManagerBl authorshipManagerBl) {
    this.authorshipManagerBl = authorshipManagerBl;
  }

  public CabinetManagerBl getCabinetManagerBl() {
    return cabinetManagerBl;
  }

  @Autowired
  public void setCabinetManagerBl(CabinetManagerBl cabinetManagerBl) {
    this.cabinetManagerBl = cabinetManagerBl;
  }


  // methods -------------------------

  public Thanks createThanks(PerunSession sess, Thanks t) throws CabinetException {
    if (t.getCreatedDate() == null) {
      t.setCreatedDate(new Date());
    }
    if (thanksExist(t)) {
      throw new CabinetException("Can't create duplicate thanks.", ErrorCodes.THANKS_ALREADY_EXISTS);
    }

    t = getThanksManagerDao().createThanks(sess, t);
    log.debug("{} created.", t);

    // recalculate thanks for all publication's authors
    List<Author> authors = new ArrayList<Author>();
    authors = getAuthorshipManagerBl().getAuthorsByPublicationId(t.getPublicationId());
    // sort to prevent locking
    synchronized (ThanksManagerBlImpl.class) {
      for (Author a : authors) {
        getCabinetManagerBl().setThanksAttribute(a.getId());
      }
    }
    return t;
  }

  @Override
  public void deleteThanks(PerunSession sess, Thanks thanks) throws CabinetException {

    getThanksManagerDao().deleteThanks(sess, thanks);
    log.debug("{} deleted.", thanks);

    // recalculate thanks for all publication's authors
    List<Author> authors = getAuthorshipManagerBl().getAuthorsByPublicationId(thanks.getPublicationId());

    synchronized (ThanksManagerBlImpl.class) {
      for (Author a : authors) {
        getCabinetManagerBl().setThanksAttribute(a.getId());
      }
    }

  }

  @Override
  public boolean thanksExist(Thanks thanks) {
    return getThanksManagerDao().thanksExist(thanks);
  }

  @Override
  public Thanks getThanksById(int id) throws CabinetException {
    return getThanksManagerDao().getThanksById(id);
  }

  @Override
  public List<Thanks> getThanksByPublicationId(int publicationId) {
    return getThanksManagerDao().getThanksByPublicationId(publicationId);
  }

  @Override
  public List<ThanksForGUI> getRichThanksByPublicationId(int publicationId) {
    return getThanksManagerDao().getRichThanksByPublicationId(publicationId);
  }

  @Override
  public List<ThanksForGUI> getRichThanksByUserId(int userId) {
    return getThanksManagerDao().getRichThanksByUserId(userId);
  }

}
