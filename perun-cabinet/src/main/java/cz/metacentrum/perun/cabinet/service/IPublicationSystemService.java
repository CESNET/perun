package cz.metacentrum.perun.cabinet.service;

import java.util.List;

import cz.metacentrum.perun.cabinet.model.PublicationSystem;
/**
 * Interface for handling PublicationSystem entity in Cabinet.
 * 
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @version $Id$
 */
public interface IPublicationSystemService {

	int createPublicationSystem(PublicationSystem ps);

	int updatePublicationSystem(PublicationSystem ps);

	int deletePublicationSystem(PublicationSystem ps);

	List<PublicationSystem> findPublicationSystemsByFilter(PublicationSystem filter);

	PublicationSystem findPublicationSystemById(Integer publicationSystemId);

	List<PublicationSystem> findAllPublicationSystems();

}