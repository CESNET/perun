package cz.metacentrum.perun.integration.impl;

import cz.metacentrum.perun.integration.implApi.IntegrationManagerImplApi;
import org.springframework.jdbc.core.JdbcPerunTemplate;

import javax.sql.DataSource;

public class IntegrationManagerImpl implements IntegrationManagerImplApi {

	private JdbcPerunTemplate jdbc;

	public JdbcPerunTemplate getJdbc() {
		return jdbc;
	}

	public void setJdbc(JdbcPerunTemplate jdbc) {
		this.jdbc = jdbc;
	}

	public void setDataSource(DataSource dataSource) {
		this.jdbc = new JdbcPerunTemplate(dataSource);
	}
}
