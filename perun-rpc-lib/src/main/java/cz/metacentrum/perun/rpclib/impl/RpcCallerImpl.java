package cz.metacentrum.perun.rpclib.impl;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.rpclib.api.Deserializer;
import cz.metacentrum.perun.rpclib.api.RpcCaller;

public class RpcCallerImpl implements RpcCaller {
	private static String format = "json";
	private String perunUrl;
	private PerunPrincipal perunPrincipal;

	private final static Logger log = LoggerFactory.getLogger(RpcCallerImpl.class);

	public RpcCallerImpl(PerunPrincipal perunPrincipal) throws InternalErrorException {
		// Set default Authenticator
		Authenticator.setDefault(new PerunAuthenticator());

		perunUrl = getPropertyFromConfiguration("perun.rpc.lib.perun.url");
		log.debug("Loaded Perun URL [{}]", perunUrl);

		this.perunPrincipal = perunPrincipal;

		// Set system wide cookie manager
		CookieManager cookieManager = new CookieManager();
		CookieHandler.setDefault(cookieManager);
	}

	public Deserializer call(String managerName, String methodName) throws PerunException {
		return this.call(managerName, methodName, null);
	}

	public Deserializer call(String managerName, String methodName, Map<String, Object> params) throws PerunException {

		// Get the connection
		HttpURLConnection conn = this.getHttpURLConnection(managerName, methodName);

		log.debug("Calling RPC method {}.{}, using connection {}", new Object[] {managerName, methodName, conn});

		if (params == null) {
			params = new HashMap<String, Object>();
		}

		// Setup delegated identity
		params.put("delegatedLogin", perunPrincipal.getActor());
		params.put("delegatedExtSourceName", perunPrincipal.getExtSourceName());
		params.put("delegatedExtSourceType", perunPrincipal.getExtSourceType());

		// Send the parameters to the RPC server
		try {
			this.sendParametersToRpcServer(conn.getOutputStream(), params);
		} catch (IOException e) {
			this.processIOException(conn, e);
		}

		// If Perun's RPC is temporarily unavailable, and Apache respond in HTML instead of JSON
		try {
			int responseCode = conn.getResponseCode();
			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw new RpcException(RpcException.Type.PERUN_RPC_SERVER_ERROR_HTTP_CODE, "Perun server on URL: " + perunUrl + " returned HTTP code: " + responseCode);
			}
		} catch (IOException ex) {
			this.processIOException(conn, ex);
		}

		// Get the answer from the server
		InputStream rpcServerAnswer = null;
		try {
			rpcServerAnswer = conn.getInputStream();
		} catch (IOException e) {
			try {
				this.processIOException(conn, e);
			} catch (RpcException e1) {
				this.processRpcServerException(conn.getErrorStream());
			}
		}
		// Initialize deserializer with the data received from the RPC server
		JsonDeserializer des = null;
		try {
			des = new JsonDeserializer(rpcServerAnswer);
		} catch (IOException e) {
			this.processIOException(conn, e);
		}

		return des;
	}

	protected void processRpcServerException(InputStream errorStream) throws RpcException, InternalErrorException, PerunException {
		JsonDeserializer errDes;
		try {
			errDes = new JsonDeserializer(errorStream);
		} catch (IOException e) {
			this.processIOException(e);
			return;
		}

		// Error occured, read the Exception if it is in response
		String errorId = errDes.readString("errorId");
		if (errorId != null ) {

			String exceptionName = errDes.readString("name");
			if (!exceptionName.equals(RpcException.class.getSimpleName())) {
				String errorClass = errDes.readString("name");
				String errorInfo = errDes.readString("message");
				try {
					Class<?> exceptionClass = Class.forName("cz.metacentrum.perun.core.api.exceptions." + errorClass);

					Class<?> constructorParams[] = new Class[1];
					constructorParams[0] = String.class;
					Constructor<?> exceptionConstructor = exceptionClass.getConstructor(constructorParams);
					Object arglist[] = new Object[1];
					arglist[0] = errorInfo;
					PerunException exception = (PerunException) exceptionConstructor.newInstance(arglist);
					exception.setErrorId(errorId);

					throw exception;
				} catch (ClassNotFoundException e1) {
					throw new InternalErrorException(e1);
				} catch (InstantiationException e1) {
					throw new InternalErrorException(e1);
				} catch (IllegalAccessException e1) {
					throw new InternalErrorException(e1);
				} catch (IllegalArgumentException e1) {
					throw new InternalErrorException(e1);
				} catch (InvocationTargetException e1) {
					throw new InternalErrorException(e1);
				} catch (NoSuchMethodException e1) {
					throw new InternalErrorException(e1);
				}
			} else {
				// RPC Exception
				String errorClass = errDes.readString("type");
				String errorInfo = errDes.readString("errorInfo");
				throw new RpcException(errorClass, errorInfo);
			}
		}
	}

	protected void sendParametersToRpcServer(OutputStream out, Map<String, Object> params) throws RpcException, InternalErrorException {
		JsonSerializer ser = null;

		try {
			ser = new JsonSerializer(out);
			ser.write(params);
			out.flush();
		} catch (IOException e) {
			this.processIOException(e);
			return;
		}
	}

	protected HttpURLConnection getHttpURLConnection(String managerName, String methodName) throws InternalErrorException {
		// Compose an URL
		String commandUrl = perunUrl + format + "/" + managerName + "/" + methodName;
		log.debug("Calling [{}]", commandUrl);

		URL url;
		try {
			url = new URL(commandUrl);
		} catch (MalformedURLException e) {
			throw new InternalErrorException(e);
		}

		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();

			conn.setRequestProperty("content-type", "application/json; charset=utf-8");
			// Try Keep-Alive
			conn.setRequestProperty("Connection", "Close");
			// We will send output
			conn.setDoOutput(true);
			// Using POST
			conn.setRequestMethod("POST");

			log.debug("Connection opened");
		} catch (IOException e) {
			this.processIOException(conn, e);
		}

		return conn;
	}

	protected void processIOException(Throwable e) throws RpcException {
		this.processIOException(null, e);
	}

	protected void processIOException(HttpURLConnection conn, Throwable e) throws RpcException {
		// Process known IOExceptions
		if (e instanceof ProtocolException) {
			throw new RpcException(RpcException.Type.COMMUNICATION_ERROR_WITH_PERUN_RPC_SERVER, "Communication problem with Perun server on URL: " + perunUrl, e);
		} else if (e instanceof UnknownHostException) {
			throw new RpcException(RpcException.Type.UNKNOWN_PERUN_RPC_SERVER, "Perun server cannot be contacted on URL: " + perunUrl, e);
		}

		// If the connection has been provided, check the responseCode
		if (conn != null) {
			// Check return code
			int responseCode;
			try {
				responseCode = conn.getResponseCode();

				if (responseCode != HttpURLConnection.HTTP_OK) {
					throw new RpcException(RpcException.Type.PERUN_RPC_SERVER_ERROR_HTTP_CODE, "Perun server on URL: " + perunUrl + " returned HTTP code: " + responseCode, e);
				}
			} catch (IOException e1) {
				throw new RpcException(RpcException.Type.UNKNOWN_EXCEPTION, "Failed to contact Perun server on URL: " + perunUrl, e1);
			}
		}
	}

	protected static String getPropertyFromConfiguration(String propertyName) throws InternalErrorException {
		// Load properties file with configuration
		Properties properties = new Properties();
		try {
			// Get the path to the perun.properties file
			// FIXME - DO NOT USE ABSOLUTE PATH !!
			BufferedInputStream is = new BufferedInputStream(new FileInputStream("/etc/perun/perun-rpc-lib.properties"));
			properties.load(is);
			is.close();

			if (propertyName == null) {
				throw new InternalErrorException("propertyName cannot by null");
			}
			String property = properties.getProperty(propertyName);
			if (property == null) {
				throw new InternalErrorException("Property " + propertyName + " cannot be found in the configuration file");
			}
			return property;
		} catch (FileNotFoundException e) {
			throw new InternalErrorException("Cannot find perun-rpc-lib.properties file", e);
		} catch (IOException e) {
			throw new InternalErrorException("Cannot read perun-rpc-lib.properties file", e);
		}
	}

	static class PerunAuthenticator extends Authenticator {
		public PasswordAuthentication getPasswordAuthentication() {
			String username = null;
			String password = null;

			try {
				username = getPropertyFromConfiguration("perun.rpc.lib.username");
				password = getPropertyFromConfiguration("perun.rpc.lib.password");
			} catch (InternalErrorException e) {
				log.error("Cannot load credentials for the RPC from the configuration file");
			}

			return new PasswordAuthentication(username, password.toCharArray());
		}
	}
}
