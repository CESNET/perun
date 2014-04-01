package cz.metacentrum.perun.cabinet.dao;

import java.util.List;

import cz.metacentrum.perun.cabinet.model.PublicationSystem;

/**
 * Interface of DAO layer for handling PublicationSystem entity.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 */
public interface IPublicationSystemDao {

	int createPublicationSystem(PublicationSystem ps);

	int updatePublicationSystem(PublicationSystem ps);

	int deletePublicationSystem(PublicationSystem ps);

	List<PublicationSystem> findPublicationSystemsByFilter(PublicationSystem filter);

	PublicationSystem findPublicationSystemById(Integer publicationSystemId);

	List<PublicationSystem> findAllPublicationSystems();

}
