package cz.metacentrum.perun.engine.dao.jdbc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import cz.metacentrum.perun.engine.dao.EngineDao;
import cz.metacentrum.perun.engine.exceptions.DispatcherNotConfiguredException;
import cz.metacentrum.perun.engine.exceptions.EngineNotConfiguredException;

/**
 * EngineDaoJdbc
 *
 * @author Michal Karm Babacek
 *
 */
@Transactional
public class EngineDaoJdbc extends JdbcDaoSupport implements EngineDao {
	private final static Logger log = LoggerFactory.getLogger(EngineDaoJdbc.class);

	@Autowired
	private Properties propertiesBean;
	private SimpleDateFormat formater = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
	private int engineId;

	public void initialize() {
		this.engineId = Integer.parseInt(propertiesBean.getProperty("engine.unique.id"));
	}

	@Override
	public void registerEngine() throws EngineNotConfiguredException {
		if (this.getJdbcTemplate().queryForInt("select count(*) from engines where id = ?", engineId) != 1) {
			throw new EngineNotConfiguredException("This Perun-engine instance marked with ID[" + engineId
					+ "] is not configured in the Perun Database.");
		}

		this.getJdbcTemplate().update("update engines set ip_address = ?, port = ?, last_check_in = to_date(?,'YYYYMMDD HH24:MI:SS') where id = ?",
				propertiesBean.getProperty("engine.ip.address"), Integer.parseInt(propertiesBean.getProperty("engine.port")),
				formater.format(new Date(System.currentTimeMillis())),
				engineId);
	}

	@Override
	public void checkIn() {
		this.getJdbcTemplate().update("update engines set last_check_in = to_date(?,'YYYYMMDD HH24:MI:SS') where id = ?", formater.format(new Date(System.currentTimeMillis())),
				engineId);
	}

	@Override
	public void loadDispatcherAddress() throws DispatcherNotConfiguredException {
		String ipAddress = this.getJdbcTemplate().queryForObject("select ip_address from dispatcher_settings", String.class);
		if (ipAddress == null) {
			throw new DispatcherNotConfiguredException("It looks like the Dispatcher's IP address is null. That's not gonna work bro :-)");
		}

		int port = this.getJdbcTemplate().queryForObject("select port from dispatcher_settings", Integer.class);
		if (port > 65535 || port < 1) {
			throw new DispatcherNotConfiguredException("It looks like the Dispatcher's port is a really weird number (" + port + "), that's not gonna work bro :-)");
		}

		if (log.isDebugEnabled()) {
			log.debug("We gonna set following properties: dispatcher.ip.address:" + ipAddress + ", dispatcher.port:" + port);
		}

		propertiesBean.put("dispatcher.ip.address", ipAddress);
		propertiesBean.put("dispatcher.port", "" + port);
	}

	public void setPropertiesBean(Properties propertiesBean) {
		this.propertiesBean = propertiesBean;
	}

	public Properties getPropertiesBean() {
		return propertiesBean;
	}
}
