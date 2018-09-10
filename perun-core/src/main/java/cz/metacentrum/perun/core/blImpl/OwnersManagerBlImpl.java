package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.core.api.ContactGroup;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.ArrayList;
import java.util.Arrays;


public class OwnersManagerBlImpl implements OwnersManagerBl {

	final static Logger log = LoggerFactory.getLogger(OwnersManagerBlImpl.class);

	private OwnersManagerImplApi ownersManagerImpl;
	private PerunBl perunBl;

	/**
	 * Constructor.
	 *
	 */
	public OwnersManagerBlImpl(OwnersManagerImplApi ownersManagerImpl) {
		this.ownersManagerImpl = ownersManagerImpl;
	}

	@Override
	public Owner createOwner(PerunSession sess, Owner owner) throws InternalErrorException {
		getPerunBl().getAuditer().log(sess, "{} created.", owner);
		return getOwnersManagerImpl().createOwner(sess, owner);
	}

	@Override
	public void deleteOwner(PerunSession sess, Owner owner) throws InternalErrorException, RelationExistsException, OwnerAlreadyRemovedException {
		this.deleteOwner(sess, owner, false);
	}

	@Override
	public void deleteOwner(PerunSession sess, Owner owner, boolean forceDelete) throws InternalErrorException, RelationExistsException, OwnerAlreadyRemovedException {
		// Check if the owner is assigned to some facility
		List<Facility> facilities = getPerunBl().getFacilitiesManagerBl().getOwnerFacilities(sess, owner);

		if (facilities != null && facilities.size() > 0) {
			if (!forceDelete) {
				throw new RelationExistsException("Owner own " + facilities.size() + " facilities");
			} else {
				for (Facility facility: facilities) {
					try {
						getPerunBl().getFacilitiesManagerBl().removeOwner(sess, facility, owner);
					} catch (OwnerAlreadyRemovedException e) {
						throw new InternalErrorException(e);
					}
				}
			}
		}

		//Remove all information about owner on facilities (facilities contacts)
		List<ContactGroup> ownerContactGroups = getPerunBl().getFacilitiesManagerBl().getFacilityContactGroups(sess, owner);
		if(!ownerContactGroups.isEmpty()) {
			if(forceDelete) {
				getPerunBl().getFacilitiesManagerBl().removeAllOwnerContacts(sess, owner);
			} else {
				throw new RelationExistsException("Owner has still some facilities contacts : " + ownerContactGroups);
			}
		}

		getOwnersManagerImpl().deleteOwner(sess, owner);
		getPerunBl().getAuditer().log(sess, "{} deleted.", owner);
	}

	@Override
	public Owner getOwnerById(PerunSession sess, int id) throws InternalErrorException, OwnerNotExistsException {
		return getOwnersManagerImpl().getOwnerById(sess, id);
	}

	@Override
	public List<Owner> getOwners(PerunSession sess) throws InternalErrorException {
		return getOwnersManagerImpl().getOwners(sess);
	}

	@Override
	public void checkOwnerExists(PerunSession sess, Owner owner) throws InternalErrorException, OwnerNotExistsException {
		getOwnersManagerImpl().checkOwnerExists(sess, owner);
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
