package cz.metacentrum.perun.cabinet.dao.impl;

import java.util.List;

import cz.metacentrum.perun.cabinet.dao.ThanksManagerDao;
import cz.metacentrum.perun.cabinet.dao.mybatis.ThanksExample;
import cz.metacentrum.perun.cabinet.dao.mybatis.ThanksMapper;
import cz.metacentrum.perun.cabinet.model.Thanks;
import cz.metacentrum.perun.cabinet.model.ThanksForGUI;

/**
 * Class of DAO layer for handling Thanks entity.
 * Provides connection to proper mapper.
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ThanksManagerDaoImpl implements ThanksManagerDao {

	private ThanksMapper thanksMapper;

	// setters ----------------------

	public void setThanksMapper(ThanksMapper thanksMapper) {
		this.thanksMapper = thanksMapper;
	}

	// methods ----------------------

	public int createThanks(Thanks t) {
		thanksMapper.insert(t);
		return t.getId();
	}

	public List<Thanks> findThanksByFilter(Thanks t) {
		return thanksMapper.findThanksByFilter(t);
	}

	public Thanks findThanksById(Integer id) {
		return thanksMapper.selectByPrimaryKey(id);
	}

	public int deleteThanksById(Integer id) {
		return thanksMapper.deleteByPrimaryKey(id);
	}

	public List<Thanks> findThanksByPublicationId(Integer id){

		ThanksExample example = new ThanksExample();
		example.createCriteria().andPublicationIdEqualTo(id);
		return thanksMapper.selectByExample(example);

	}

	public List<ThanksForGUI> findRichThanksByPublicationId(Integer id) {

		ThanksExample example = new ThanksExample();
		example.createCriteria().andPublicationIdEqualTo(id);
		return thanksMapper.selectRichByExample(example);

	}

	public List<ThanksForGUI> findAllRichThanksByUserId(Integer id) {

		return thanksMapper.selectAllRichByUserId(id);

	}

}
