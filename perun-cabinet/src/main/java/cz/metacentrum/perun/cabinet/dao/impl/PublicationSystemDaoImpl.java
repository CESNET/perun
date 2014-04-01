package cz.metacentrum.perun.cabinet.dao.impl;

import java.util.List;

import cz.metacentrum.perun.cabinet.dao.IPublicationSystemDao;
import cz.metacentrum.perun.cabinet.dao.mybatis.PublicationSystemExample;
import cz.metacentrum.perun.cabinet.dao.mybatis.PublicationSystemMapper;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;

/**
 * Class of DAO layer for handling PublicationSystem entity.
 * Provides connection to proper mapper.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 */
public class PublicationSystemDaoImpl implements IPublicationSystemDao {

	private PublicationSystemMapper publicationSystemMapper;

	// setters ----------------------

	public void setPublicationSystemMapper(PublicationSystemMapper publicationSystemMapper) {
		this.publicationSystemMapper = publicationSystemMapper;
	}

	// methods ----------------------

	public List<PublicationSystem> findPublicationSystemsByFilter(PublicationSystem filter) {
		return publicationSystemMapper.findPublicationSystemsByFilter(filter);
	}

	public PublicationSystem findPublicationSystemById(Integer publicationSystemId) {
		return publicationSystemMapper.selectByPrimaryKey(publicationSystemId);
	}

	public List<PublicationSystem> findAllPublicationSystems() {
		return publicationSystemMapper.selectByExample(new PublicationSystemExample());
	}

	public int createPublicationSystem(PublicationSystem ps) {
		return publicationSystemMapper.insert(ps);
	}

	public int updatePublicationSystem(PublicationSystem ps) {
		return publicationSystemMapper.updateByPrimaryKey(ps);
	}

	public int deletePublicationSystem(PublicationSystem ps) {
		return publicationSystemMapper.deleteByPrimaryKey(ps.getId());
	}

}