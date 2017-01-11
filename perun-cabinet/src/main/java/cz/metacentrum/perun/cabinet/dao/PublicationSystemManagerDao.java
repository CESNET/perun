package cz.metacentrum.perun.cabinet.dao;

import java.util.List;

import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.bl.CabinetException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

/**
 * Interface of DAO layer for handling PublicationSystem entity.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public interface PublicationSystemManagerDao {

	PublicationSystem createPublicationSystem(PublicationSystem ps) throws CabinetException, InternalErrorException;

	PublicationSystem updatePublicationSystem(PublicationSystem ps) throws CabinetException, InternalErrorException;

	void deletePublicationSystem(PublicationSystem ps) throws CabinetException, InternalErrorException;

	List<PublicationSystem> getPublicationSystems() throws InternalErrorException;

	PublicationSystem getPublicationSystemById(int id) throws CabinetException, InternalErrorException;

	PublicationSystem getPublicationSystemByName(String name) throws CabinetException, InternalErrorException;

	PublicationSystem getPublicationSystemByNamespace(String namespace) throws CabinetException, InternalErrorException;

}
