package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.bl.PerunBl;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Implementation of connection used in Perun to catch connection events.
 * Methods of Connection object from constructor are fully used.
 * Some methods are enriched by auditer transaction methods.
 *
 * @author Jiri Mauritz <jirmauritz at gmail dot com>
 */
public class PerunConnection implements Connection {

	private Auditer auditer;
	private CacheManager cacheManager;
	private final Connection connectionImpl;

	// Constructor
	public PerunConnection(Connection connectionImpl, Auditer auditer, CacheManager cacheManager) {
		this.connectionImpl = connectionImpl;
		this.auditer = auditer;
		this.cacheManager = cacheManager;
	}


	// Methods enriched by auditer and cachemanager transaction methods

	@Override
	public Savepoint setSavepoint() throws SQLException {
		auditer.newNestedTransaction();
		if (BeansUtils.getCoreConfig().isCacheEnabled()) {
			cacheManager.newNestedTransaction();
		}
		return connectionImpl.setSavepoint();
	}

	@Override
	public Savepoint setSavepoint(String string) throws SQLException {
		auditer.newNestedTransaction();
		if (BeansUtils.getCoreConfig().isCacheEnabled()) {
			cacheManager.newNestedTransaction();
		}
		return connectionImpl.setSavepoint(string);
	}

	@Override
	public void rollback(Savepoint svpnt) throws SQLException {
		auditer.cleanNestedTransation();
		if (BeansUtils.getCoreConfig().isCacheEnabled()) {
			cacheManager.cleanNestedTransaction();
		}
		connectionImpl.rollback(svpnt);

	}

	@Override
	public void releaseSavepoint(Savepoint svpnt) throws SQLException {
		auditer.flushNestedTransaction();
		if (BeansUtils.getCoreConfig().isCacheEnabled()) {
			cacheManager.flushNestedTransaction();
		}
		connectionImpl.releaseSavepoint(svpnt);
	}


	// Other methods uses only connectionImpl

	@Override
	public Statement createStatement() throws SQLException {
		return connectionImpl.createStatement();
	}

	@Override
	public PreparedStatement prepareStatement(String string) throws SQLException {
		return connectionImpl.prepareStatement(string);
	}

	@Override
	public CallableStatement prepareCall(String string) throws SQLException {
		return connectionImpl.prepareCall(string);
	}

	@Override
	public String nativeSQL(String string) throws SQLException {
		return connectionImpl.nativeSQL(string);
	}

	@Override
	public void setAutoCommit(boolean bln) throws SQLException {
		connectionImpl.setAutoCommit(bln);
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		return connectionImpl.getAutoCommit();
	}

	@Override
	public void commit() throws SQLException {
		connectionImpl.commit();
	}

	@Override
	public void rollback() throws SQLException {
		connectionImpl.rollback();
	}

	@Override
	public void close() throws SQLException {
		connectionImpl.close();
	}

	@Override
	public boolean isClosed() throws SQLException {
		return connectionImpl.isClosed();
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return connectionImpl.getMetaData();
	}

	@Override
	public void setReadOnly(boolean bln) throws SQLException {
		connectionImpl.setReadOnly(bln);
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return connectionImpl.isReadOnly();
	}

	@Override
	public void setCatalog(String string) throws SQLException {
		connectionImpl.setCatalog(string);
	}

	@Override
	public String getCatalog() throws SQLException {
		return connectionImpl.getCatalog();
	}

	@Override
	public void setTransactionIsolation(int i) throws SQLException {
		connectionImpl.setTransactionIsolation(i);
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		return connectionImpl.getTransactionIsolation();
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return connectionImpl.getWarnings();
	}

	@Override
	public void clearWarnings() throws SQLException {
		connectionImpl.clearWarnings();
	}

	@Override
	public Statement createStatement(int i, int i1) throws SQLException {
		return connectionImpl.createStatement(i,i1);
	}

	@Override
	public PreparedStatement prepareStatement(String string, int i, int i1) throws SQLException {
		return connectionImpl.prepareStatement(string, i, i1);
	}

	@Override
	public CallableStatement prepareCall(String string, int i, int i1) throws SQLException {
		return connectionImpl.prepareCall(string, i, i1);
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return connectionImpl.getTypeMap();
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		connectionImpl.setTypeMap(map);
	}

	@Override

	public void setHoldability(int i) throws SQLException {
		connectionImpl.setHoldability(i);
	}

	@Override
	public int getHoldability() throws SQLException {
		return connectionImpl.getHoldability();
	}

	@Override
	public Statement createStatement(int i, int i1, int i2) throws SQLException {
		return connectionImpl.createStatement(i, i1, i2);
	}

	@Override
	public PreparedStatement prepareStatement(String string, int i, int i1, int i2) throws SQLException {
		return connectionImpl.prepareStatement(string, i, i1, i2);
	}

	@Override
	public CallableStatement prepareCall(String string, int i, int i1, int i2) throws SQLException {

		return connectionImpl.prepareCall(string, i, i1, i2);
	}

	@Override
	public PreparedStatement prepareStatement(String string, int i) throws SQLException {
		return connectionImpl.prepareStatement(string, i);
	}

	@Override
	public PreparedStatement prepareStatement(String string, int[] ints) throws SQLException {
		return connectionImpl.prepareStatement(string, ints);
	}

	@Override

	public PreparedStatement prepareStatement(String string, String[] strings) throws SQLException {
		return connectionImpl.prepareStatement(string, strings);
	}

	@Override
	public Clob createClob() throws SQLException {
		return connectionImpl.createClob();
	}

	@Override
	public Blob createBlob() throws SQLException {
		return connectionImpl.createBlob();
	}

	@Override
	public NClob createNClob() throws SQLException {
		return connectionImpl.createNClob();
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		return connectionImpl.createSQLXML();
	}

	@Override
	public boolean isValid(int i) throws SQLException {
		return connectionImpl.isValid(i);
	}

	@Override
	public void setClientInfo(String string, String string1) throws SQLClientInfoException {
		connectionImpl.setClientInfo(string, string1);
	}

	@Override
	public void setClientInfo(Properties prprts) throws SQLClientInfoException {
		connectionImpl.setClientInfo(prprts);
	}

	@Override
	public String getClientInfo(String string) throws SQLException {
		return connectionImpl.getClientInfo(string);
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		return connectionImpl.getClientInfo();
	}

	@Override
	public Array createArrayOf(String string, Object[] os) throws SQLException {
		return connectionImpl.createArrayOf(string, os);
	}

	@Override
	public Struct createStruct(String string, Object[] os) throws SQLException {
		return connectionImpl.createStruct(string, os);
	}

	@Override
	public void setSchema(String string) throws SQLException {
		connectionImpl.setSchema(string);
	}

	@Override
	public String getSchema() throws SQLException {
		return connectionImpl.getSchema();
	}

	@Override
	public void abort(Executor exctr) throws SQLException {
		connectionImpl.abort(exctr);
	}

	@Override
	public void setNetworkTimeout(Executor exctr, int i) throws SQLException {
		connectionImpl.setNetworkTimeout(exctr, i);
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		return connectionImpl.getNetworkTimeout();
	}

	@Override
	public <T> T unwrap(Class<T> type) throws SQLException {
		return connectionImpl.unwrap(type);
	}

	@Override
	public boolean isWrapperFor(Class<?> type) throws SQLException {
		return connectionImpl.isWrapperFor(type);
	}
}

