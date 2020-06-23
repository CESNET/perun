package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceAlreadyRemovedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotAssignedException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.implApi.ExtSourcesManagerImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

	final static String extSourceMappingSelectQuery = "ext_sources.id as ext_sources_id, ext_sources.name as ext_sources_name, ext_sources.type as ext_sources_type, " +
			"ext_sources.created_at as ext_sources_created_at, ext_sources.created_by as ext_sources_created_by, ext_sources.modified_by as ext_sources_modified_by, " +
			"ext_sources.modified_at as ext_sources_modified_at, ext_sources.modified_by_uid as ext_sources_modified_by_uid, ext_sources.created_by_uid as ext_sources_created_by_uid";

	private final static String extSourceMappingSelectQueryWithAttributes = "ext_sources.id as ext_sources_id, ext_sources.name as ext_sources_name, ext_sources.type as ext_sources_type, " +
			"ext_sources.created_at as ext_sources_created_at, ext_sources.created_by as ext_sources_created_by, ext_sources.modified_by as ext_sources_modified_by, " +
			"ext_sources.modified_at as ext_sources_modified_at, ext_sources.modified_by_uid as ext_sources_modified_by_uid, ext_sources.created_by_uid as ext_sources_created_by_uid, " +
			"ext_sources_attributes.attr_name as attr_name, ext_sources_attributes.attr_value as attr_value";

	// http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/jdbc.html
	private static JdbcPerunTemplate jdbc;

	private static final RowMapper<ExtSource> EXTSOURCE_MAPPER = (rs, i) -> {
		try {
			Class<?> extSourceClass = Class.forName(rs.getString("ext_sources_type"));
			ExtSource es = (ExtSource) extSourceClass.newInstance();

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

			ExtSource es;

			// Get the instance by the type of the extSource
			try {
				Class<?> extSourceClass = Class.forName(extSource.getType());
				es = (ExtSource) extSourceClass.newInstance();
			} catch (ClassNotFoundException e) {
				throw new InternalErrorException(e);
			} catch (InstantiationException | IllegalAccessException e) {
				throw new InternalErrorException(e);
			}

			// Set the properties
			es.setId(extSource.getId());
			es.setName(extSource.getName());
			es.setType(extSource.getType());

			// Now store the attributes
			if (attributes != null) {
				for (String attr_name : attributes.keySet()) {
					jdbc.update("insert into ext_sources_attributes (attr_name, attr_value, ext_sources_id,created_by, created_at, modified_by, modified_at, created_by_uid, modified_by_uid) " +
									"values (?,?,?,?," + Compatibility.getSysdate() + ",?," + Compatibility.getSysdate() + ",?,?)", attr_name, attributes.get(attr_name), extSource.getId(),
							sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getActor(), sess.getPerunPrincipal().getUserId(), sess.getPerunPrincipal().getUserId());
				}
			}

			// Assign newly created extSource
			extSource = es;

			return extSource;
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void deleteExtSource(PerunSession sess, ExtSource extSource) throws ExtSourceAlreadyRemovedException {
		try {
			// Delete associated attributes
			jdbc.update("DELETE FROM ext_sources_attributes WHERE ext_sources_id=?", extSource.getId());
			// Delete the external source
			int numAffected = jdbc.update("DELETE FROM ext_sources WHERE id=?", extSource.getId());
			if (numAffected == 0) throw new ExtSourceAlreadyRemovedException("ExtSource: " + extSource);
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

	private static final ExtSourcesExtractor EXT_SOURCES_EXTRACTOR = new ExtSourcesExtractor();

	private static class ExtSourcesExtractor implements ResultSetExtractor<List<ExtSource>> {

		@Override
		public List<ExtSource> extractData(ResultSet rs) throws SQLException {
			Map<Integer, ExtSource> map = new HashMap<>();
			ExtSource myObject;
			while (rs.next()) {
				// fetch from map by ID
				Integer id = rs.getInt("ext_sources_id");
				myObject = map.get(id);
				if (myObject == null) {
					// if not preset, put in map
					myObject = EXTSOURCE_MAPPER.mapRow(rs, rs.getRow());
					map.put(id, myObject);
				}
			}
			return new ArrayList<>(map.values());
		}
	}

	@Override
	public ExtSource getExtSourceById(PerunSession sess, int id) throws ExtSourceNotExistsException {
		try {
			return jdbc.queryForObject("select " + extSourceMappingSelectQueryWithAttributes + " from ext_sources left join ext_sources_attributes on ext_sources.id=ext_sources_attributes.ext_sources_id where id=?", EXT_SOURCES_EXTRACTOR, id);
		} catch (EmptyResultDataAccessException ex) {
			throw new ExtSourceNotExistsException("ExtSource with ID=" + id + " not exists", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}
	}

	@Override
	public ExtSource getExtSourceByName(PerunSession sess, String name) throws ExtSourceNotExistsException {
		try {
			return jdbc.queryForObject("select " + extSourceMappingSelectQueryWithAttributes +
					" from ext_sources left join ext_sources_attributes on ext_sources.id=ext_sources_attributes.ext_sources_id " +
					"where name=?", EXT_SOURCES_EXTRACTOR, name);
		} catch (EmptyResultDataAccessException ex) {
			throw new ExtSourceNotExistsException("ExtSource with name=" + name + " not exists", ex);
		} catch (RuntimeException ex) {
			throw new InternalErrorException(ex);
		}

	}

	@Override
	public List<ExtSource> getVoExtSources(PerunSession sess, Vo vo) {
		try {
			return jdbc.query("SELECT " + extSourceMappingSelectQueryWithAttributes +
					" FROM vo_ext_sources v INNER JOIN ext_sources ON v.ext_sources_id=ext_sources.id " +
					"   LEFT JOIN ext_sources_attributes ON ext_sources.id=ext_sources_attributes.ext_sources_id " +
					" WHERE v.vo_id=?", EXT_SOURCES_EXTRACTOR, vo.getId());

		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<ExtSource> getGroupExtSources(PerunSession perunSession, Group group) {
		try {
			return jdbc.query("SELECT " + extSourceMappingSelectQueryWithAttributes +
					" FROM group_ext_sources g_exts INNER JOIN ext_sources ON g_exts.ext_source_id=ext_sources.id " +
					"   LEFT JOIN ext_sources_attributes ON ext_sources.id=ext_sources_attributes.ext_sources_id " +
					" WHERE g_exts.group_id=?", EXT_SOURCES_EXTRACTOR, group.getId());

		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public List<ExtSource> getExtSources(PerunSession sess) {
		try {
			return jdbc.query("SELECT " + extSourceMappingSelectQueryWithAttributes +
					" FROM ext_sources LEFT JOIN ext_sources_attributes ON ext_sources.id=ext_sources_attributes.ext_sources_id ", EXT_SOURCES_EXTRACTOR);

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
	public void removeExtSource(PerunSession sess, Vo vo, ExtSource source) throws ExtSourceNotAssignedException, ExtSourceAlreadyRemovedException {
		try {
			if (jdbc.queryForInt("select count('x') from vo_ext_sources where ext_sources_id=? and vo_id=?", source.getId(), vo.getId()) == 0) {
				// Source isn't assigned
				throw new ExtSourceNotAssignedException("ExtSource id='" + source.getId() + "'");
			}

			int numAffected = jdbc.update("DELETE FROM vo_ext_sources WHERE ext_sources_id=? AND vo_id=?", source.getId(), vo.getId());
			if (numAffected != 1) throw new ExtSourceAlreadyRemovedException("ExtSource: " + source + " , Vo: " + vo);
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}

	@Override
	public void removeExtSource(PerunSession perunSession, Group group, ExtSource source) throws ExtSourceNotAssignedException, ExtSourceAlreadyRemovedException {
		try {
			if (jdbc.queryForInt("select count('x') from group_ext_sources where ext_source_id=? and group_id=?", source.getId(), group.getId()) == 0) {
				// Source isn't assigned
				throw new ExtSourceNotAssignedException("ExtSource id='" + source.getId() + "'");
			}

			int numAffected = jdbc.update("DELETE FROM group_ext_sources WHERE ext_source_id=? AND group_id=?", source.getId(), group.getId());
			if (numAffected != 1)
				throw new ExtSourceAlreadyRemovedException("ExtSource: " + source + " , Group: " + group);
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
	public void initialize(PerunSession sess) {
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
	 * Loads the extSources definitions from the XML configuration file.
	 * All data from the extSouces XML file are synchronized with the DB.
	 */
	@Override
	public void loadExtSourcesDefinitions(PerunSession sess) {
		try {
			// Load the XML file
			BufferedInputStream is = new BufferedInputStream(new FileInputStream(ExtSourcesManager.CONFIGURATIONFILE));
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();

			// Check if the root element is "extSources"
			if (!doc.getDocumentElement().getNodeName().equals("extSources")) {
				throw new InternalErrorException("perun-extSources.xml doesn't contain extSources as root element");
			}

			// Get all defined extSources
			NodeList extSourcesNodes = doc.getElementsByTagName("extSource");
			for (int extSourceSeq = 0; extSourceSeq < extSourcesNodes.getLength(); extSourceSeq++) {

				// Get each extSource
				Node extSourceNode = extSourcesNodes.item(extSourceSeq);
				if (extSourceNode.getNodeType() == Node.ELEMENT_NODE) {

					Element extSourceElement = (Element) extSourceNode;

					// Get extSource name
					String extSourceName = extSourceElement.getElementsByTagName("name").item(0).getChildNodes().item(0).getNodeValue();
					if (extSourceName == null) {
						throw new InternalErrorException("extSource doesn't have defined name");
					}

					// Get extSource type
					String extSourceType = extSourceElement.getElementsByTagName("type").item(0).getChildNodes().item(0).getNodeValue();
					if (extSourceType == null) {
						throw new InternalErrorException("extSource " + extSourceName + " doesn't have defined type");
					}

					// Get all extSource attributes
					NodeList attributeNodes = extSourceElement.getElementsByTagName("attribute");

					Map<String, String> attributes = new HashMap<>();
					for (int attributeSeq = 0; attributeSeq < attributeNodes.getLength(); attributeSeq++) {
						Element elem = (Element) attributeNodes.item(attributeSeq);

						if (elem.getNodeType() == Node.ELEMENT_NODE) {
							String attrName = elem.getAttribute("name");
							String attrValue = null;
							if (elem.getChildNodes() != null && elem.getChildNodes().item(0) != null) {
								attrValue = elem.getChildNodes().item(0).getNodeValue();
							}

							attributes.put(attrName, attrValue);
						}
					}

					// Check if the extSource
					try {
						ExtSource extSource;
						try {
							extSource = this.getExtSourceByName(sess, extSourceName);
							extSource.setName(extSourceName);
							extSource.setType(extSourceType);

							// ExtSource exists, so check values and potentionally update it
							self.updateExtSource(sess, extSource, attributes);

						} catch (ExtSourceNotExistsException e) {
							// ExtSource doesn't exist, so create it
							extSource = new ExtSource();
							extSource.setName(extSourceName);
							extSource.setType(extSourceType);
							self.createExtSource(sess, extSource, attributes);
						}
					} catch (RuntimeException e) {
						throw new InternalErrorException(e);
					}
				}
			}
		} catch (FileNotFoundException e) {
			log.warn("No external source configuration file found.");
		} catch (Exception e) {
			log.error("Cannot initialize ExtSourceManager.");
			throw new InternalErrorException(e);
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
	private class AttributesExtractor implements ResultSetExtractor<Map<String, String>> {
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
			return new ArrayList<ExtSource>();
		} catch (RuntimeException e) {
			throw new InternalErrorException(e);
		}
	}
}
