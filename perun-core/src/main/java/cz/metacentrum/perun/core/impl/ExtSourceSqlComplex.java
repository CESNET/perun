package cz.metacentrum.perun.core.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.SubjectNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.rt.InternalErrorRuntimeException;
import cz.metacentrum.perun.core.blImpl.AttributesManagerBlImpl;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
import cz.metacentrum.perun.core.api.BeansUtils;

/**
 * @author Michal Stava stavamichal@gmail.com
 */
public class ExtSourceSqlComplex extends ExtSourceSql implements ExtSourceApi {

	private final static Logger log = LoggerFactory.getLogger(ExtSourceSqlComplex.class);

	@Override
	public List<Map<String, String>> findSubjects(String searchString) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		return findSubjects(searchString, 0);
	}

	@Override
	public List<Map<String, String>> findSubjects(String searchString, int maxResults) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		return findSubjectsLogins(searchString, maxResults);
	}

}
