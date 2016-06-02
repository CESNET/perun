package cz.metacentrum.perun.dispatcher.dao.jdbc;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import cz.metacentrum.perun.dispatcher.dao.EngineDao;
import cz.metacentrum.perun.dispatcher.exceptions.EngineNotConfiguredException;

/**
 * EngineDaoJdbc
 * 
 * @author Michal Karm Babacek
 * 
 */
@Transactional
public class EngineDaoJdbc extends JdbcDaoSupport implements EngineDao {
	private final static Logger log = LoggerFactory.getLogger(EngineDaoJdbc.class);

	private SimpleDateFormat formater = new SimpleDateFormat(
			"yyyyMMdd HH:mm:ss");


	@Override
	public void registerEngine(int engineId, String engineIpAddress, int enginePort) throws EngineNotConfiguredException {
		if (this.getJdbcTemplate().queryForInt(
				"select count(*) from engines where id = ?", engineId) != 1) {
			throw new EngineNotConfiguredException("This Perun-engine instance marked with ID["
					+ engineId
					+ "] is not configured in the Perun Database.");
		} else {

			this.getJdbcTemplate()
					.update("update engines set ip_address = ?, port = ?, last_check_in = to_date(?,'YYYYMMDD HH24:MI:SS') where id = ?",
							engineIpAddress,
							enginePort,
							formater.format(new Date(System.currentTimeMillis())),
							engineId);
		}

	}

	@Override
	public void checkIn(int engineId) {
		this.getJdbcTemplate()
				.update("update engines set last_check_in = to_date(?,'YYYYMMDD HH24:MI:SS') where id = ?",
						formater.format(new Date(System.currentTimeMillis())),
						engineId);
	}

}
