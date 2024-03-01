package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.audit.events.OwnersManagerEvents.OwnerCreated;
import cz.metacentrum.perun.audit.events.OwnersManagerEvents.OwnerDeleted;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.OwnerAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.OwnerNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.bl.OwnersManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.implApi.OwnersManagerImplApi;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OwnersManagerBlImpl implements OwnersManagerBl {

  static final Logger LOG = LoggerFactory.getLogger(OwnersManagerBlImpl.class);

  private final OwnersManagerImplApi ownersManagerImpl;
  private PerunBl perunBl;

  /**
   * Constructor.
   */
  public OwnersManagerBlImpl(OwnersManagerImplApi ownersManagerImpl) {
    this.ownersManagerImpl = ownersManagerImpl;
  }

  @Override
  public void checkOwnerExists(PerunSession sess, Owner owner) throws OwnerNotExistsException {
    getOwnersManagerImpl().checkOwnerExists(sess, owner);
  }

  @Override
  public Owner createOwner(PerunSession sess, Owner owner) {
    getPerunBl().getAuditer().log(sess, new OwnerCreated(owner));
    return getOwnersManagerImpl().createOwner(sess, owner);
  }

  @Override
  public void deleteOwner(PerunSession sess, Owner owner, boolean forceDelete)
      throws RelationExistsException, OwnerAlreadyRemovedException {
    // Check if the owner is assigned to some facility
    List<Facility> facilities = getPerunBl().getFacilitiesManagerBl().getOwnerFacilities(sess, owner);

    if (facilities != null && facilities.size() > 0) {
      if (!forceDelete) {
        throw new RelationExistsException("Owner own " + facilities.size() + " facilities");
      } else {
        for (Facility facility : facilities) {
          try {
            getPerunBl().getFacilitiesManagerBl().removeOwner(sess, facility, owner);
          } catch (OwnerAlreadyRemovedException e) {
            throw new InternalErrorException(e);
          }
        }
      }
    }

    getOwnersManagerImpl().deleteOwner(sess, owner);
    getPerunBl().getAuditer().log(sess, new OwnerDeleted(owner));
  }

  @Override
  public void deleteOwner(PerunSession sess, Owner owner) throws RelationExistsException, OwnerAlreadyRemovedException {
    this.deleteOwner(sess, owner, false);
  }

  @Override
  public Owner getOwnerById(PerunSession sess, int id) throws OwnerNotExistsException {
    return getOwnersManagerImpl().getOwnerById(sess, id);
  }

  @Override
  public Owner getOwnerByName(PerunSession sess, String name) throws OwnerNotExistsException {
    return getOwnersManagerImpl().getOwnerByName(sess, name);
  }

  @Override
  public List<Owner> getOwners(PerunSession sess) {
    return getOwnersManagerImpl().getOwners(sess);
  }

  /**
   * Gets the owners manager
   *
   * @return The ownersManagerImpl.
   */
  public OwnersManagerImplApi getOwnersManagerImpl() {
    return this.ownersManagerImpl;
  }

  /**
   * Gets the perunBl.
   *
   * @return The perunBl.
   */
  public PerunBl getPerunBl() {
    return this.perunBl;
  }

  public void setPerunBl(PerunBl perunBl) {
    this.perunBl = perunBl;
  }
}
