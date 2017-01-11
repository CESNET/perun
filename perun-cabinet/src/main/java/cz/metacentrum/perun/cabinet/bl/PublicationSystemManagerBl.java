package cz.metacentrum.perun.cabinet.bl;

import java.util.List;

import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

/**
 * Interface for handling PublicationSystem entity in Cabinet.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public interface PublicationSystemManagerBl {

	PublicationSystem createPublicationSystem(PublicationSystem ps) throws CabinetException, InternalErrorException;

	PublicationSystem updatePublicationSystem(PublicationSystem ps) throws CabinetException, InternalErrorException;

	void deletePublicationSystem(PublicationSystem ps) throws CabinetException, InternalErrorException;

	PublicationSystem getPublicationSystemById(int publicationSystemId) throws InternalErrorException, CabinetException;

	PublicationSystem getPublicationSystemByName(String name) throws InternalErrorException, CabinetException;

	PublicationSystem getPublicationSystemByNamespace(String namespace) throws InternalErrorException, CabinetException;

	List<PublicationSystem> getPublicationSystems() throws InternalErrorException;

}
