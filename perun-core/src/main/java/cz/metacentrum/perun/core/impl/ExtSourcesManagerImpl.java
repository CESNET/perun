package cz.metacentrum.perun.core.impl;

import com.zaxxer.hikari.HikariDataSource;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.implApi.ExtSourcesManagerImplApi;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtSourcesManagerImpl implements ExtSourcesManagerImplApi {

	private final static Logger log = LoggerFactory.getLogger(ExtSourcesManagerImpl.class);
	public final static String USEREXTSOURCEMAPPING = "additionalues_";

	private ExtSourcesManagerImplApi self;
	private PerunBl perunBl;

	final static String extSourceMappingSelectQuery = "ext_sources.id as ext_sources_id, ext_sources.name as ext_sources_name, ext_sources.type as ext_sources_type, " +
			"ext_sources.created_at as ext_sources_created_at, ext_sources.created_by as ext_sources_created_by, ext_sources.modified_by as ext_sources_modified_by, " +
			"ext_sources.modified_at as ext_sources_modified_at, ext_sources.modified_by_uid as ext_sources_modified_by_uid, ext_sources.created_by_uid as ext_sources_created_by_uid";

	// http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/jdbc.html
	private final JdbcPerunTemplate jdbc;

	private final RowMapper<ExtSource> EXTSOURCE_MAPPER = (rs, i) -> {
		try {
			Class<?> extSourceClass = Class.forName(rs.getString("ext_sources_type"));
			ExtSourceImpl es = (ExtSourceImpl) extSourceClass.newInstance();
			es.setPerunBl(perunBl);

			es.setId(rs.getInt("ext_sources_id"));
			es.setName(rs.getString("ext_sources_name"));
			es.setType(rs.getString("ext_sources_type"));
			es.setCreatedAt(rs.getString("ext_sources_created_at"));
			es.setCreatedBy(rs.getString("ext_sources_created_by"));
			es.setModifiedAt(rs.getString("ext_sources_modified_at"));
			es.setModifiedBy(rs.getString("ext_sources_modified_by"));
			if (rs.getInt("ext_sources_modified_by_uid") == 0) es.setModifiedByUid(null);
			else es.setModifiedByUid(rs.getInt("ext_sources_modified_by_uid"));
			if (rs.getInt("ext_sources_created_by_uid") == 0) es.setCreatedByUid(null);
			else es.setCreatedByUid(rs.getInt("ext_sources_created_by_uid"));
			return es;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | RuntimeException e) {
			throw new InternalErrorException(e);
		}
	};

	public ExtSourcesManagerImpl(DataSource perunPool) {
		jdbc = new JdbcPerunTemplate(perunPool);
		jdbc.setQueryTimeout(BeansUtils.getCoreConfig().getQueryTimeout());
	}

	public void setSelf(ExtSourcesManagerImplApi self) {
		this.self = self;
	}

	@Override
	public ExtSource createExtSource(PerunSession sess, ExtSource extSource, Map<String, String> attributes) throws ExtSourceExistsException {
		Utils.notNull(extSource.getName(), "extSource.getName()");
		Utils.notNull(extSource.getType(), "extSource.getType()");

		try {
			// Check if the extSources already exists
			if (0 < jdbc.queryForInt("select count(id) from ext_sources where name=? and type=?", extSource.getName(), extSource.getType())) {
				throw new ExtSourceExistsException(extSource);
			}

			// Get a new Id
			int newId = Utils.getNewId(jdbc, "ext_sources_id_seq");

			jdbc.update("insert into ext_sources (id, name, type, created_by,created_at,modified_by,modified_at,created_by_uid,modified_by_uid) " +
							"values (?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", newId, extSource.getName(),
					extSource.getType(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(),
					sess.getPerunPrincipal().getUserId());
			extSource.setId(newId);

			// Now store the attributes
			if (attributes != null) {
				for (String attr_name : attributes.keySet()) {
					jdbc.update("insert into ext_sources_attributes (attr_name, attr_value, ext_sources_id,created_by, created_at, modified_by, modified_at, created_by_uid, modified_by_uid) " +
									"values (?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", attr_name, attributes.get(attr_name), newId,
							sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
				}
			}

			return getExtSourceById(sess, newId);
		} catch (RuntimeException | ExtSourceNotExistsException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void deleteExtSource(PerunSession sess, ExtSource extSource) {
		try {
			// Delete associated attributes
			jdbc.update("DELETE FROM ext_sources_attributes WHERE ext_sources_id=?", extSource.getId());
			// Delete the external source
			jdbc.update("DELETE FROM ext_sources WHERE id=?", extSource.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}


	@Override
	public void updateExtSource(PerunSession sess, ExtSource extSource, Map<String, String> attributes) throws ExtSourceNotExistsException {
		ExtSource extSourceDb;

		extSourceDb = this.getExtSourceById(sess, extSource.getId());


		// Check the name
		if (!extSourceDb.getName().equals(extSource.getName())) {
			try {
				jdbc.update("update ext_sources set name=? ,modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where id=?", extSource.getName(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), extSource.getId());
			} catch (RuntimeException e) {
				throw new InternalErrorException(e);
			}
		}

		// Check the type
		if (!extSourceDb.getType().equals(extSource.getType())) {
			try {
				jdbc.update("update ext_sources set type=?, modified_by=?, modified_by_uid=?, modified_at=" + Compatibility.getSysdate() + " where id=?", extSource.getType(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), extSource.getId());
			} catch (RuntimeException e) {
				throw new InternalErrorException(e);
			}
		}

		// Check the attributes
		if (!getAttributes(extSourceDb).equals(attributes)) {
			log.debug("There is a change in attributes for {}", extSource);
			try {
				// Firstly delete all attributes, then store new ones
				jdbc.update("DELETE FROM ext_sources_attributes WHERE ext_sources_id=?", extSource.getId());

				for (String attrName : attributes.keySet()) {
					jdbc.update("insert into ext_sources_attributes (ext_sources_id, attr_name, attr_value, created_by, created_at, modified_by, modified_at, created_by_uid, modified_by_uid) " +
									"values (?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", extSource.getId(), attrName,
							attributes.get(attrName), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(),
							sess.getPerunPrincipal().getUserId());
				}
			} catch (RuntimeException e) {
				throw new InternalErrorException(e);
			}
		}
	}

	@Override
	public ExtSource getExtSourceById(PerunSession sess, int id) throws ExtSourceNotExistsException {
		try {
			return jdbc.queryForObject("select " + extSourceMappingSelectQuery + " from ext_sources where id=?", EXTSOURCE_MAPPER, id);
		} catch (EmptyResultDataAccessException ex) {
			throw new ExtSourceNotExistsException("ExtSource with ID=" + id + " not exists", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public ExtSource getExtSourceByName(PerunSession sess, String name) throws ExtSourceNotExistsException {
		try {
			return jdbc.queryForObject("select " + extSourceMappingSelectQuery + " from ext_sources where name=?", EXTSOURCE_MAPPER, name);
		} catch (EmptyResultDataAccessException ex) {
			throw new ExtSourceNotExistsException("ExtSource with name=" + name + " not exists", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

	}

	@Override
	public List<Integer> getVoExtSourcesIds(PerunSession sess, Vo vo) throws InternalErrorException {
		try {
			return jdbc.queryForList("SELECT ext_sources.id " +
					" FROM vo_ext_sources v JOIN ext_sources ON v.ext_sources_id=ext_sources.id " +
					" WHERE v.vo_id=?", Integer.class, vo.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Integer> getGroupExtSourcesIds(PerunSession perunSession, Group group) throws InternalErrorException {
		try {
			return jdbc.queryForList("SELECT ext_sources.id " +
					" FROM group_ext_sources g_exts JOIN ext_sources ON g_exts.ext_source_id=ext_sources.id " +
					" WHERE g_exts.group_id=?", Integer.class, group.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<ExtSource> getExtSources(PerunSession sess) {
		try {
			return jdbc.query("SELECT " + extSourceMappingSelectQuery + " FROM ext_sources", EXTSOURCE_MAPPER);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void addExtSource(PerunSession sess, Vo vo, ExtSource source) throws ExtSourceAlreadyAssignedException {
		try {
			if (0 < jdbc.queryForInt("select count('x') from vo_ext_sources where ext_sources_id=? and vo_id=?", source.getId(), vo.getId())) {
				throw new ExtSourceAlreadyAssignedException(source);
			}

			jdbc.update("insert into vo_ext_sources (ext_sources_id, vo_id, created_by, created_at, modified_by, modified_at, created_by_uid, modified_by_uid) " +
							"values (?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", source.getId(), vo.getId(),
					sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());

		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void addExtSource(PerunSession sess, Group group, ExtSource source) throws ExtSourceAlreadyAssignedException {
		try {
			if (0 < jdbc.queryForInt("select count('x') from group_ext_sources where ext_source_id=? and group_id=?", source.getId(), group.getId())) {
				throw new ExtSourceAlreadyAssignedException(source);
			}

			jdbc.update("insert into group_ext_sources (ext_source_id, group_id, created_by, created_at, modified_by, modified_at, created_by_uid, modified_by_uid) " +
							"values (?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", source.getId(), group.getId(),
					sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());

		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeExtSource(PerunSession sess, Vo vo, ExtSource source) throws ExtSourceNotAssignedException {
		try {
			if (jdbc.queryForInt("select count('x') from vo_ext_sources where ext_sources_id=? and vo_id=?", source.getId(), vo.getId()) == 0) {
				// Source isn't assigned
				throw new ExtSourceNotAssignedException("ExtSource id='" + source.getId() + "'");
			}

			jdbc.update("DELETE FROM vo_ext_sources WHERE ext_sources_id=? AND vo_id=?", source.getId(), vo.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeExtSource(PerunSession perunSession, Group group, ExtSource source) throws ExtSourceNotAssignedException {
		try {
			if (jdbc.queryForInt("select count('x') from group_ext_sources where ext_source_id=? and group_id=?", source.getId(), group.getId()) == 0) {
				// Source isn't assigned
				throw new ExtSourceNotAssignedException("ExtSource id='" + source.getId() + "'");
			}

			jdbc.update("DELETE FROM group_ext_sources WHERE ext_source_id=? AND group_id=?", source.getId(), group.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<Integer> getAssociatedUsersIdsWithExtSource(PerunSession sess, ExtSource source) {
		try {
			return jdbc.query("SELECT user_id FROM user_ext_sources WHERE ext_sources_id=?", Utils.ID_MAPPER, source.getId());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	/**
	 * Routine which initialize the extSourcesManager.
	 */
	@Override
	public void initialize(PerunSession sess, PerunBl perunBl) {
		this.perunBl = perunBl;
		if (sess.getPerun().isPerunReadOnly()) log.debug("Loading extSource manager init in readOnly version.");

		//In read only just test if extSource Perun exists
		if (sess.getPerun().isPerunReadOnly()) {
			try {
				this.getExtSourceByName(sess, ExtSourcesManager.EXTSOURCE_NAME_PERUN);
			} catch (ExtSourceNotExistsException ex) {
				log.error("Default Perun extSource not exists.");
			} catch (InternalErrorException ex) {
				log.error("Cannot get default PERUN extSource.");
			}
			//Load ExtSource only if this perun is not read only
		} else {
			this.loadExtSourcesDefinitions(sess);

			// Check if default extSource PERUN exists
			try {
				this.getExtSourceByName(sess, ExtSourcesManager.EXTSOURCE_NAME_PERUN);
			} catch (ExtSourceNotExistsException e) {
				ExtSource es = new ExtSource(ExtSourcesManager.EXTSOURCE_NAME_PERUN, ExtSourcesManager.EXTSOURCE_INTERNAL);
				try {
					this.createExtSource(sess, es, null);
				} catch (ExtSourceExistsException e1) {
					log.error("Trying to create default PERUN extSource which already exists.");
				} catch (InternalErrorException e1) {
					log.error("Cannot create default PERUN extSource.");
				}
			} catch (InternalErrorException e) {
				log.error("Cannot get default PERUN extSource.");
			}
		}
	}

	/**
	 * Cleans up allocated resources.
	 */
	@Override
	public void destroy() {
		for (DataSource dataSource : dataSourceMap.values()) {
			if (dataSource instanceof HikariDataSource) {
				((HikariDataSource) dataSource).close();
			}
		}
	}

	/**
	 * Map of db connection pools created for all ExtSourceSql in perun-extSources.xml
	 */
	private final Map<String, DataSource> dataSourceMap = new HashMap<>();

	@Override
	public DataSource getDataSource(String poolName) {
			return dataSourceMap.get(poolName);
	}



	/**
	 * Loads the extSources definitions from the XML configuration file.
	 * All data from the extSouces XML file are synchronized with the DB.
	 * <P>Example:</P>
	 * <PRE>
	 * <extSources>
	 *   <dbpools>
	 *     <dbpool>
	 *       <name>MAIN</name>
	 *       <main/><!-- indicates the main pool used by Perun -->
	 *     </dbpool>
	 *     <dbpool>
	 *       <name>PEOPLEDB</name><!-- required -->
	 *       <url>jdbc:oracle:thin:@//oracle.example.com:1521/people</url><!-- required -->
	 *       <user>perun</user><!-- optional -->
	 *       <password>******</password><!-- optional -->
	 *       <driver>oracle.jdbc.OracleDriver</driver><!-- optional for postgresql, oracle, sqlite, mysql; required for others -->
	 *       <connectionTimeout>300000</connectionTimeout><!-- optional -->
	 *       <maxLifetime>600000</maxLifetime><!-- optional -->
	 *       <maximumPoolSize>10</maximumPoolSize><!-- optional -->
	 *       <minimumIdle>1</minimumIdle><!-- optional -->
	 *       <idleTimeout>300000</idleTimeout><!-- optional -->
	 *       <leakDetectionThreshold>30000</leakDetectionThreshold><!-- optional -->
	 *     </dbpool>
	 *   </dbpools>
	 *   <extSource>
	 *     <name>INET</name>
	 *     <type>cz.metacentrum.perun.core.impl.ExtSourceSql</type>
	 *     <description></description><!-- ignored -->
	 *     <attributes>
	 *       <attribute name="dbpool">INETDB</attribute>
	 *       <attribute name="query">...</query>
	 *       <attribute name="loginQuery">...</query>
	 *     </attributes>
	 *   </extSource>
	 * </PRE>
	 */
	@Override
	public void loadExtSourcesDefinitions(PerunSession sess) {
		File file = new File(ExtSourcesManager.CONFIGURATIONFILE);
		if (!Files.isReadable(file.toPath())) {
			log.warn("File " + file + " does not exist or is not readable");
			return;
		}
		try {
			// Load the XML file
			log.debug("loading file {}", file);
			Document document = new SAXReader().read(file);
			Element extSources = document.getRootElement();
			if (!extSources.getName().equals("extSources")) {
				throw new InternalErrorException("perun-extSources.xml doesn't contain extSources as root element");
			}

			//shared database pools
			Element dbpools = extSources.element("dbpools");
			if (dbpools != null) {
				for (Element dbpool : dbpools.elements("dbpool")) {
					String poolName = dbpool.element("name").getText();
					Element main = dbpool.element("main");
					if (main != null) {
						//use main pool
						dataSourceMap.put(poolName, this.jdbc.getDataSource());
						log.debug("creating ExtSourceSql dbpool {} referencing the main pool", poolName);
					} else {
						String url = dbpool.element("url").getText();
						String driver = dbpool.element("driver") != null ? dbpool.element("driver").getText() : null;
						String user = dbpool.element("user") != null ? dbpool.element("user").getText() : null;
						String password = dbpool.element("password") != null ? dbpool.element("password").getText() : null;
						HikariDataSource hds = createHikariPool(poolName, url, driver, user, password);
						Integer maximumPoolSize = parsePoolIntProperty(dbpool, "maximumPoolSize");
						if (maximumPoolSize != null) hds.setMaximumPoolSize(maximumPoolSize);
						Integer minimumIdle = parsePoolIntProperty(dbpool, "minimumIdle");
						if (minimumIdle != null) hds.setMinimumIdle(minimumIdle);
						Integer connectionTimeout = parsePoolIntProperty(dbpool, "connectionTimeout");
						if (connectionTimeout != null) hds.setConnectionTimeout(connectionTimeout);
						Integer maxLifetime = parsePoolIntProperty(dbpool, "maxLifetime");
						if (maxLifetime != null) hds.setMaxLifetime(maxLifetime);
						Integer idleTimeout = parsePoolIntProperty(dbpool, "idleTimeout");
						if (idleTimeout != null) hds.setIdleTimeout(idleTimeout);
						Integer leakDetectionThreshold = parsePoolIntProperty(dbpool, "leakDetectionThreshold");
						if (leakDetectionThreshold != null) hds.setLeakDetectionThreshold(leakDetectionThreshold);
						log.debug("creating ExtSourceSql dbpool {} for user {} and db {}", poolName, user, url);
						dataSourceMap.put(poolName, hds);
					}
				}
			}

			// Get each extSource
			for (Element extSourceElement : extSources.elements("extSource")) {
				// Get extSource name
				String extSourceName = extSourceElement.element("name").getText();
				if (StringUtils.isBlank(extSourceName)) {
					throw new InternalErrorException("extSource doesn't have defined name");
				}
				// Get extSource type
				String extSourceType = extSourceElement.element("type").getText();
				if (StringUtils.isBlank(extSourceType)) {
					throw new InternalErrorException("extSource doesn't have defined type");
				}
				// Get all extSource attributes
				Map<String, String> attributes = new HashMap<>();
				Element attrs = extSourceElement.element("attributes");
				if (attrs != null) {
					for (Element attribute : attrs.elements("attribute")) {
						String attrName = attribute.attribute("name").getValue();
						String attrValue = attribute.getText();
						attributes.put(attrName, attrValue);
					}
				}
				// Check if the extSource exists
				ExtSource extSource;
				try {
					extSource = this.getExtSourceByName(sess, extSourceName);
					extSource.setName(extSourceName);
					extSource.setType(extSourceType);
					// ExtSource exists, so check values and potentially update it
					self.updateExtSource(sess, extSource, attributes);
				} catch (ExtSourceNotExistsException e) {
					// ExtSource doesn't exist, so create it
					extSource = new ExtSource();
					extSource.setName(extSourceName);
					extSource.setType(extSourceType);
					self.createExtSource(sess, extSource, attributes);
				}
				log.debug("ExtSource {} of type {} read from XML file", extSourceName, extSourceType);
			}
		} catch (DocumentException e) {
			log.error("File " + file + " was not loaded", e);
		} catch (Exception e) {
			log.error("Cannot initialize ExtSourceManager.");
			throw new InternalErrorException(e);
		}

		// check connection attributes for all ExtSourcesSql present in the database (may be more than in perun-extSources.xml)
		for (ExtSource extSource : getExtSources(sess)) {
			if (extSource instanceof ExtSourceSql) {
				Map<String, String> attributes = getAttributes(extSource);
				// pools in attributes
				String dbpool = attributes.get(ExtSourceSql.DBPOOL);
				String url = attributes.get(ExtSourceSql.URL);
				if (dbpool != null) {
					log.debug("ExtSource {} uses dbpool {}", extSource.getName(), dbpool);
					if (dataSourceMap.get(dbpool) == null) {
						log.error("ExtSource {} references non-existing pool {}", extSource.getName(), dbpool);
					}
				} else if (url != null) {
					//for backward compatibility, create pools based on user and url
					String user = attributes.get(ExtSourceSql.USER);
					String password = attributes.get("password");
					String driver = attributes.get("driver");
					String poolKey = "db-" + user + "-" + url;
					dataSourceMap.computeIfAbsent(poolKey, s -> {
							log.debug("creating dbpool from legacy attributes for user \"{}\" and url \"{}\"", user, url);
							return createHikariPool(poolKey, url, driver, user, password);
						}
					);
					log.debug("ExtSource {} uses legacy dbpool {}", extSource.getName(), poolKey);
				}
			}
		}
	}

	private static HikariDataSource createHikariPool(String name, String url, String driver, String user, String password) {
		HikariDataSource hds = new HikariDataSource();
		hds.setPoolName(name);
		hds.setJdbcUrl(url);
		if (StringUtils.isNotBlank(driver)) {
			hds.setDriverClassName(driver);
		} else if (url.startsWith("jdbc:oracle")) {
			hds.setDriverClassName("oracle.jdbc.OracleDriver");
		} else if (url.startsWith("jdbc:postgresql")) {
			hds.setDriverClassName("org.postgresql.Driver");
		} else if (url.startsWith("jdbc:sqlite")) {
			hds.setDriverClassName("org.sqlite.JDBC");
		} else if (url.startsWith("jdbc:mysql")) {
			hds.setDriverClassName("com.mysql.jdbc.Driver");
		} else {
			log.error("Unknown JDBC driver for " + url + " pool " + name);
		}
		if (StringUtils.isNotBlank(user)) {
			hds.setUsername(user);
		}
		if (StringUtils.isNotBlank(password)) {
			hds.setPassword(password);
		}
		hds.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
		hds.setReadOnly(true);
		if(url.startsWith("jdbc:sqlite")) {
			hds.addDataSourceProperty("open_mode","1");
		}
		return hds;
	}

	private static Integer parsePoolIntProperty(Element dbpool, String propName) {
		Element element = dbpool.element(propName);
		if (element == null) return null;
		try {
			return Integer.parseInt(element.getText());
		} catch (NumberFormatException e) {
			log.error("<" + propName + "> of <dbpool name=\"" + dbpool.getName() + "\"> does not contain a number: " + element.getText());
			return null;
		}
	}

	@Override
	public boolean extSourceExists(PerunSession perunSession, ExtSource extSource) {
		Utils.notNull(extSource, "extSource");
		try {
			int numberOfExistences = jdbc.queryForInt("select count(1) from ext_sources where id=?", extSource.getId());
			if (numberOfExistences == 1) {
				return true;
			} else if (numberOfExistences > 1) {
				throw new ConsistencyErrorException("ExtSource " + extSource + " exists more than once.");
			}
			return false;
		} catch (EmptyResultDataAccessException ex) {
			return false;
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public void checkExtSourceExists(PerunSession perunSession, ExtSource es) throws ExtSourceNotExistsException {
		if (!extSourceExists(perunSession, es)) throw new ExtSourceNotExistsException("ExtSource: " + es);
	}

	/**
	 * Result Set Extractor for Attributes map
	 */
	private static class AttributesExtractor implements ResultSetExtractor<Map<String, String>> {
		@Override
		public Map<String, String> extractData(ResultSet rs) throws SQLException {
			Map<String, String> attributes = new HashMap<>();
			while (rs.next()) {
				attributes.put(rs.getString("attr_name"), rs.getString("attr_value"));
			}
			return attributes;
		}
	}

	@Override
	public Map<String, String> getAttributes(ExtSource extSource) {
		try {
			return jdbc.query("select attr_name, attr_value from ext_sources_attributes where ext_sources_id = " + extSource.getId(), new AttributesExtractor());
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<ExtSource> getExtSourcesToSynchronize(PerunSession sess) {
		try {
			return jdbc.query("select " + extSourceMappingSelectQuery + " from ext_sources, ext_sources_attributes where ext_sources.id=ext_sources_attributes.ext_sources_id and ext_sources_attributes.attr_name=? and ext_sources_attributes.attr_value=true", EXTSOURCE_MAPPER, ExtSourcesManager.EXTSOURCE_SYNCHRONIZATION_ENABLED_ATTRNAME);
		} catch (EmptyResultDataAccessException e) {
			return new ArrayList<>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}
}
