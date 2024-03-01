package cz.metacentrum.perun.cabinet.bl.impl;

import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.cabinet.bl.ErrorCodes;
import cz.metacentrum.perun.cabinet.bl.PublicationSystemManagerBl;
import cz.metacentrum.perun.cabinet.dao.PublicationSystemManagerDao;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.bl.PerunBl;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class for handling PublicationSystem entity in Cabinet.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class PublicationSystemManagerBlImpl implements PublicationSystemManagerBl {

  private static Logger LOG = LoggerFactory.getLogger(PublicationSystemManagerBlImpl.class);

  private PublicationSystemManagerDao publicationSystemManagerDao;
  private PerunBl perunBl;

  public PublicationSystem createPublicationSystem(PerunSession session, PublicationSystem ps) {
    PublicationSystem newps = getPublicationSystemManagerDao().createPublicationSystem(session, ps);
    LOG.debug("{} created.", newps);
    return newps;
  }

  public void deletePublicationSystem(PublicationSystem ps) throws CabinetException {
    getPublicationSystemManagerDao().deletePublicationSystem(ps);
    LOG.debug("{} deleted.", ps);
  }

  public PublicationSystem getPublicationSystemById(int id) throws CabinetException {
    return getPublicationSystemManagerDao().getPublicationSystemById(id);
  }

  // methods -----------------------------

  public PublicationSystem getPublicationSystemByName(String name) throws CabinetException {
    return getPublicationSystemManagerDao().getPublicationSystemByName(name);
  }

  public PublicationSystem getPublicationSystemByNamespace(String namespace) throws CabinetException {
    return getPublicationSystemManagerDao().getPublicationSystemByNamespace(namespace);
  }

  public PublicationSystemManagerDao getPublicationSystemManagerDao() {
    return publicationSystemManagerDao;
  }

  public List<PublicationSystem> getPublicationSystems() {
    return getPublicationSystemManagerDao().getPublicationSystems();
  }

  /**
   * Checks if INTERNAL publication system is present in DB. If not, creates one.
   */
  protected void initialize() throws CabinetException {
    // search for internal system
    try {
      getPublicationSystemManagerDao().getPublicationSystemByName("INTERNAL");
    } catch (CabinetException ex) {
      if (ErrorCodes.PUBLICATION_SYSTEM_NOT_EXISTS.equals(ex.getType())) {
        PerunSession session = perunBl.getPerunSession(
            new PerunPrincipal("perunCabinet", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
                ExtSourcesManager.EXTSOURCE_INTERNAL), new PerunClient());
        LOG.error("Internal PS not exists: {}", ex);
        // create internal if not exists
        PublicationSystem record = new PublicationSystem();
        record.setFriendlyName("INTERNAL");
        record.setLoginNamespace("empty");
        record.setType("empty");
        record.setUrl("empty");
        record.setPassword(null);
        record.setUsername(null);
        createPublicationSystem(session, record);
      }
    } catch (Exception ex) {
      LOG.error("Unable to determine if Internal PS exists: {}", ex);
    }
  }

  @Autowired
  public void setPerunBl(PerunBl perun) {
    this.perunBl = perun;
  }

  @Autowired
  public void setPublicationSystemManagerDao(PublicationSystemManagerDao publicationSystemManagerDao) {
    this.publicationSystemManagerDao = publicationSystemManagerDao;
  }

  public PublicationSystem updatePublicationSystem(PerunSession session, PublicationSystem ps) throws CabinetException {
    PublicationSystem upps = getPublicationSystemManagerDao().updatePublicationSystem(session, ps);
    LOG.debug("{} updated.", upps);
    return upps;
  }

}
