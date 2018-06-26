package cz.metacentrum.perun.rpclib.impl;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.rpclib.api.Deserializer;
import cz.metacentrum.perun.rpclib.api.RpcCaller;

/**
 * Implementation of Java Client calling remote Perun RPC using HTTP POST requests.
 * Client delegate local credentials.
 *
 * @author Michal Šťava
 * @author Pavel Zlámal
 */
public class RpcCallerImpl implements RpcCaller {

	private static String format = "json";
	private String perunUrl;
	private PerunPrincipal perunPrincipal;
	private BasicCookieStore cookieStore = new BasicCookieStore();
	private String username;
	private String password;
	private int timeout = 0;

	private final static Logger log = LoggerFactory.getLogger(RpcCallerImpl.class);

	/**
	 * Create instance of Java RPC API client
	 *
	 * @param perunPrincipal Credential to delegate
	 * @throws InternalErrorException If initialization fails
	 */
	public RpcCallerImpl(PerunPrincipal perunPrincipal) throws InternalErrorException {

		perunUrl = getPropertyFromConfiguration("perun.rpc.lib.perun.url");
		log.debug("Loaded Perun URL [{}]", perunUrl);

		try {
			username = getPropertyFromConfiguration("perun.rpc.lib.username");
			password = getPropertyFromConfiguration("perun.rpc.lib.password");
		} catch (InternalErrorException e) {
			log.error("Cannot load credentials for the RPC from the configuration file");
		}

		this.perunPrincipal = perunPrincipal;

	}

	/**
	 * Create instance of Java RPC API client
	 *
	 * @param perunPrincipal Credential to delegate
	 * @param timeout timeout for all callbacks
	 * @throws InternalErrorException If initialization fails
	 */
	public RpcCallerImpl(PerunPrincipal perunPrincipal, int timeout) throws InternalErrorException {

		this(perunPrincipal);
		this.timeout = timeout;

	}

	public Deserializer call(String managerName, String methodName) throws PerunException {
		return this.call(managerName, methodName, null);
	}

	public Deserializer call(String managerName, String methodName, Map<String, Object> params) throws PerunException {

		// Prepare sending message
		HttpResponse response;
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		if (this.timeout > 0) {
			// if timeout specified
			RequestConfig requestConfig = RequestConfig.custom()
					.setConnectionRequestTimeout(this.timeout)
					.setConnectTimeout(this.timeout)
					.setSocketTimeout(this.timeout)
					.build();
			httpClientBuilder.setDefaultRequestConfig(requestConfig);
		}
		httpClientBuilder.setDefaultCookieStore(cookieStore);
		HttpClient httpClient = httpClientBuilder.build();

		String commandUrl = perunUrl + format + "/" + managerName + "/" + methodName;
		log.debug("Calling [{}]", commandUrl);

		HttpPost post = new HttpPost(commandUrl);
		post.setHeader("Content-Type", "application/json");
		post.setHeader("charset", "utf-8");
		post.setHeader("Connection", "Close");

		// XSRF protection
		URI domainUri = null;
		try {
			domainUri = new URI(perunUrl);
			log.debug("URI: {}", domainUri);
		} catch (URISyntaxException e) {
			log.error("Can't parse perunUrl property to URI: {}", perunUrl);
		}
		if (domainUri != null) {

			List<Cookie> cookies = cookieStore.getCookies();

			for (Cookie cookie : cookies) {
				if (Objects.equals(cookie.getDomain(), domainUri.getHost()) &&
						Objects.equals(cookie.getName(), "XSRF-TOKEN")) {
					post.setHeader("X-XSRF-TOKEN", cookie.getValue());
					log.debug("XSFR header set.");
					break;
				}
			}
		}

		// Authz
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(username, password);
		try {
			post.addHeader(new BasicScheme().authenticate(creds, post, null));
		} catch (AuthenticationException e) {
			log.error("User '{}' can't authenticate to {}", username, perunUrl);
			return null;
		}


		if (params == null) {
			params = new HashMap<>();
		}

		params.put("delegatedLogin", perunPrincipal.getActor());
		params.put("delegatedExtSourceName", perunPrincipal.getExtSourceName());
		params.put("delegatedExtSourceType", perunPrincipal.getExtSourceType());

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JsonSerializer ser = null;

		try {
			ser = new JsonSerializer(out);
			ser.write(params);
			out.flush();
		} catch (IOException e) {
			this.processIOException(e);
			return null;
		}

		post.setEntity(new ByteArrayEntity(out.toByteArray()));

		InputStream rpcServerAnswer = null;

		try {
			response = httpClient.execute(post);

			// If Perun's RPC is temporarily unavailable, and Apache respond in HTML instead of JSON
			int responseCode = response.getStatusLine().getStatusCode();
			if (responseCode != 200) {
				throw new RpcException(RpcException.Type.PERUN_RPC_SERVER_ERROR_HTTP_CODE, "Perun server on URL: " + perunUrl + " returned HTTP code: " + responseCode);
			}

			rpcServerAnswer = response.getEntity().getContent();

		} catch(IOException ex) {
			this.processIOException(ex);
		}

		// Initialize deserializer with the data received from the RPC server
		JsonDeserializer des = null;
		try {
			des = new JsonDeserializer(rpcServerAnswer);

			// Error occur, read the Exception if it is in response
			if (des.contains("errorId")) {

				String errorId = des.readString("errorId");
				if (errorId != null ) {

					String exceptionName = des.readString("name");
					if (!exceptionName.equals(RpcException.class.getSimpleName())) {
						String errorClass = des.readString("name");
						String errorInfo = des.readString("message");
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
						String errorClass = des.readString("type");
						String errorInfo = des.readString("errorInfo");
						throw new RpcException(errorClass, errorInfo);
					}
				}

			}

		} catch (IOException e) {
			this.processIOException(e);
		}

		return des;
	}

	protected void processIOException(Throwable e) throws RpcException {

		if (e instanceof ProtocolException) {
			throw new RpcException(RpcException.Type.COMMUNICATION_ERROR_WITH_PERUN_RPC_SERVER, "Communication problem with Perun server on URL: " + perunUrl, e);
		} else if (e instanceof UnknownHostException) {
			throw new RpcException(RpcException.Type.UNKNOWN_PERUN_RPC_SERVER, "Perun server cannot be contacted on URL: " + perunUrl, e);
		}
		log.error("IO Exception: {}", e);

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

}
