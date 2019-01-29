package cz.metacentrum.perun.rpc;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.CoreConfig;

import javax.ws.rs.core.Response;

import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunRequest;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.rt.PerunRuntimeException;
import cz.metacentrum.perun.core.blImpl.AttributesManagerBlImpl;
import cz.metacentrum.perun.core.impl.AttributesManagerImpl;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import cz.metacentrum.perun.rpc.deserializer.JsonDeserializer;
import cz.metacentrum.perun.rpc.deserializer.UrlDeserializer;
import cz.metacentrum.perun.rpc.serializer.PdfSerializer;
import cz.metacentrum.perun.rpc.serializer.SvgGraphvizSerializer;
import cz.metacentrum.perun.rpc.serializer.TextFileSerializer;
import cz.metacentrum.perun.rpc.serializer.JsonSerializer;
import cz.metacentrum.perun.rpc.serializer.JsonSerializerJSONP;
import cz.metacentrum.perun.rpc.serializer.JsonSerializerJSONSIMPLE;
import cz.metacentrum.perun.rpc.serializer.Serializer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * HTTP servlet wrapping Perun's method calls. Returned values are serialized and sent as an HTTP response.
 * <p>
 * URL format: "/format/class/method"
 *
 * @author Jan Klos &lt;ddd@mail.muni.cz>
 * @author Ales Krenek
 * @author Martin Kuba &lt;makub@ics.muni.cz>
 */
public class Api extends HttpServlet {

	private final static Logger log = LoggerFactory.getLogger(Api.class);

	private final static String APICALLER = "apiCaller";
	private final static String PERUNREQUESTS = "perunRequests";
	private final static String PERUNREQUESTSURL = "getPendingRequests";
	private final static String PERUNSTATUS = "getPerunStatus";
	private final static String PERUNSTATISTICS = "getPerunStatistics";
	private final static String PERUNSYSTEMTIME = "getPerunSystemTimeInMillis";
	private final static String VOOTMANAGER = "vootManager";
	private final static String SCIMMANAGER = "scimManager";
	private final static int timeToLiveWhenDone = 60 * 1000; // in milisec, if requests is done more than this time, remove it from list

	private static final String SHIB_IDENTITY_PROVIDER = "Shib-Identity-Provider";
	private static final String SOURCE_IDP_ENTITY_ID = "sourceIdPEntityID";
	private static final String SSL_CLIENT_VERIFY = "SSL_CLIENT_VERIFY";
	private static final String SSL_CLIENT_ISSUER_DN = "SSL_CLIENT_I_DN";
	private static final String SSL_CLIENT_SUBJECT_DN = "SSL_CLIENT_S_DN";
	private static final String SSL_CLIENT_CERT = "SSL_CLIENT_CERT";
	private static final String SUCCESS = "SUCCESS";
	private static final String OIDC_CLAIM_SUB = "OIDC_CLAIM_sub";
	private static final String OIDC_CLAIM_CLIENT_ID = "OIDC_CLAIM_client_id";
	private static final String OIDC_CLAIM_SCOPE = "OIDC_CLAIM_scope";
	private static final String OIDC_CLAIM_ISS = "OIDC_CLAIM_iss";
	private static final String EXTSOURCE = "EXTSOURCE";
	private static final String EXTSOURCETYPE = "EXTSOURCETYPE";
	private static final String EXTSOURCELOA = "EXTSOURCELOA";
	private static final String ENV_REMOTE_USER = "ENV_REMOTE_USER";
	private static final String DELEGATED_LOGIN = "delegatedLogin";
	private static final String DELEGATED_EXTSOURCE_NAME = "delegatedExtSourceName";
	private static final String DELEGATED_EXTSOURCE_TYPE = "delegatedExtSourceType";
	private static final String LOA = "loa";

	@Override
	public void init() {
		// we do not init anything
	}

	private static String getStringAttribute(HttpServletRequest req, String attributeName) {
		return (String) req.getAttribute(attributeName);
	}

	private static String getExtSourceName(HttpServletRequest req, Deserializer des) throws InternalErrorException {
		String shibIdentityProvider = getStringAttribute(req, SHIB_IDENTITY_PROVIDER);
		String sourceIdpEntityId = getStringAttribute(req, SOURCE_IDP_ENTITY_ID);
		if (isNotEmpty(shibIdentityProvider)) {
			return getOriginalIdP(shibIdentityProvider, sourceIdpEntityId);
		} else {
			if (isNotEmpty(req.getHeader(OIDC_CLAIM_SUB))) {
				String iss = req.getHeader(OIDC_CLAIM_ISS);
				if(iss!=null) {
					String extSourceName = BeansUtils.getCoreConfig().getOidcIssuersExtsourceNames().get(iss);
					if(extSourceName!=null) {
						return extSourceName;
					}
				}
				throw new InternalErrorException("OIDC issuer "+iss+" not configured");
			} else if (Objects.equals(req.getAttribute(SSL_CLIENT_VERIFY), SUCCESS)) {
				return getStringAttribute(req, SSL_CLIENT_ISSUER_DN);
			} else {
				String extSource = getStringAttribute(req, EXTSOURCE);
				return extSource != null ? extSource : des.readString(DELEGATED_EXTSOURCE_NAME);
			}
		}
	}

	private static String getOriginalIdP(String shibIdentityProvider, String sourceIdpEntityId) {
		// If IdP is proxy and we want to save original source IdP behind Proxy.
		List<String> proxyIdPs = BeansUtils.getCoreConfig().getProxyIdPs();
		if (isNotEmpty(sourceIdpEntityId)) {
			if (proxyIdPs.contains(shibIdentityProvider)) {
				return sourceIdpEntityId;
			} else {
				log.warn("sourceIdPEntityID attrribute found with value " + sourceIdpEntityId +
						" in request but IdP with entityID: '" + shibIdentityProvider +
						"' was not found in perun configuration property 'perun.proxyIdPs'=" + proxyIdPs +
						". serving classical entityID instead of sourceIdPEntityID.");
				return shibIdentityProvider;
			}
		} else {
			return shibIdentityProvider;
		}
	}

	private static String getExtLogin(HttpServletRequest req,String extSourceName,String remoteUser) {
		if (isNotEmpty(remoteUser)) {
			return remoteUser;
		} else {
			String envRemoteUser = getStringAttribute(req, ENV_REMOTE_USER);
			if (isNotEmpty(envRemoteUser)) {
				return envRemoteUser;
			} else if (extSourceName.equals(ExtSourcesManager.EXTSOURCE_NAME_LOCAL)) {
				// LOCAL EXTSOURCE - If ExtSource is LOCAL then generate REMOTE_USER name on the fly
				return Long.toString(System.currentTimeMillis());
			} else {
				return null;
			}
		}
	}

	private static String getActor(HttpServletRequest req, Deserializer des) throws InternalErrorException {
		String actor = null;
		String remoteUser = req.getRemoteUser();
		if (isNotEmpty(getStringAttribute(req, (SHIB_IDENTITY_PROVIDER)))) {
			if (isNotEmpty(remoteUser)) {
				actor = remoteUser;
			}
		} else if (isNotEmpty(req.getHeader(OIDC_CLAIM_SUB))) {
			actor = remoteUser;
		} else if (Objects.equals(req.getAttribute(SSL_CLIENT_VERIFY), SUCCESS)) {
			actor = getStringAttribute(req, SSL_CLIENT_SUBJECT_DN);
		} else {
			String extSourceName = getStringAttribute(req, EXTSOURCE);
			if (extSourceName != null) {
				actor = getExtLogin(req, extSourceName, remoteUser);
			}
		}

		if (des != null && actor != null) {
			if (BeansUtils.getCoreConfig().getRpcPowerusers().contains(actor) && des.contains(DELEGATED_LOGIN)) {
				// Rewrite the remoteUser and extSource
				actor = des.readString(DELEGATED_LOGIN);
			}
		}

		return actor;

	}

	private static PerunPrincipal setupPerunPrincipal(HttpServletRequest req, Deserializer des) throws InternalErrorException, UserNotExistsException {
		String extSourceLoaString = null;
		String extLogin = null;
		String extSourceName = null;
		String extSourceType = null;
		int extSourceLoa;
		Map<String, String> additionalInformations = new HashMap<>();

		String shibIdentityProvider = getStringAttribute(req, SHIB_IDENTITY_PROVIDER);
		String sourceIdpEntityId = getStringAttribute(req, SOURCE_IDP_ENTITY_ID);
		String remoteUser = req.getRemoteUser();

		CoreConfig config = BeansUtils.getCoreConfig();

		// If we have header Shib-Identity-Provider, then the user uses identity federation to authenticate
		if (isNotEmpty(shibIdentityProvider)) {
			extSourceName = getOriginalIdP(shibIdentityProvider, sourceIdpEntityId);
			extSourceType = ExtSourcesManager.EXTSOURCE_IDP;
			extSourceLoaString = getStringAttribute(req, LOA);
			if(isEmpty(extSourceLoaString)) extSourceLoaString = "2";

			// FIXME: find better place where do the operation with attributes from federation
			String eppn = getStringAttribute(req, "eppn");
			if (isNotEmpty(eppn)) {
				// Remove scope from the eppn attribute
				additionalInformations.put("eppnwoscope", StringUtils.substringBefore(eppn, "@"));
			}

			// Store IdP used by user to session, since for IdentityConsolidator and Registrar we need to know,
			// if user logged in through proxy or not - we provide different links etc.
			additionalInformations.put("originIdentityProvider", shibIdentityProvider);

			if (isNotEmpty(remoteUser)) {
				extLogin = remoteUser;
			}
		}

		// If OIDC_CLAIM_sub header is present, it means user authenticated via OAuth2 with MITRE.
		else if (isNotEmpty(req.getHeader(OIDC_CLAIM_SUB))) {
			extLogin = req.getHeader(OIDC_CLAIM_SUB);
			//this is configurable, as the OIDC server has the source of sub claim also configurable
			String iss = req.getHeader(OIDC_CLAIM_ISS);
			if (iss != null) {
				extSourceName = BeansUtils.getCoreConfig().getOidcIssuersExtsourceNames().get(iss);
				extSourceType = BeansUtils.getCoreConfig().getOidcIssuersExtsourceTypes().get(iss);
				if (extSourceName == null || extSourceType == null) {
					throw new InternalErrorException("OIDC issuer " + iss + " not configured");
				}
			} else {
				throw new InternalErrorException("OIDC issuer not send by Authorization Server");
			}
			extSourceLoaString = "-1";
			log.debug("detected OIDC/OAuth2 client for sub={},iss={}",extLogin,iss);
		}

		// EXT_SOURCE was defined in Apache configuration (e.g. Kerberos or Local)
		else if (req.getAttribute(EXTSOURCE) != null) {
			extSourceName = getStringAttribute(req, EXTSOURCE);
			extSourceType = getStringAttribute(req, EXTSOURCETYPE);
			extSourceLoaString = getStringAttribute(req, EXTSOURCELOA);
			extLogin = getExtLogin(req, extSourceName, remoteUser);
		}

		// X509 cert was used
		// Cert must be last since Apache asks for certificate everytime and fills cert properties even when Kerberos is in place.
		else if (Objects.equals(req.getAttribute(SSL_CLIENT_VERIFY), SUCCESS)) {
			String certDN = getStringAttribute(req, SSL_CLIENT_SUBJECT_DN);
			String caDN = getStringAttribute(req, SSL_CLIENT_ISSUER_DN);
			String wholeCert = getStringAttribute(req, SSL_CLIENT_CERT);
			extSourceName = caDN;
			extSourceType = ExtSourcesManager.EXTSOURCE_X509;
			extSourceLoaString = getStringAttribute(req,EXTSOURCELOA);
			extLogin = certDN;

			// Store X509 certificate in the additionalInformations structure
			//FIXME: duplicit
			additionalInformations.put("userCertificates",
					AttributesManagerBlImpl.escapeMapAttributeValue(certDN) + AttributesManagerImpl.KEY_VALUE_DELIMITER +
							AttributesManagerBlImpl.escapeMapAttributeValue(wholeCert));
			additionalInformations.put("userCertDNs",
					AttributesManagerBlImpl.escapeMapAttributeValue(certDN) + AttributesManagerImpl.KEY_VALUE_DELIMITER +
							AttributesManagerBlImpl.escapeMapAttributeValue(caDN));
			additionalInformations.put(SSL_CLIENT_SUBJECT_DN, certDN);

			// Store X509
			additionalInformations.put("dn", certDN);
			additionalInformations.put("cadn", caDN);
			additionalInformations.put("certificate", wholeCert);

			// Get organization from the certificate
			Pattern p = Pattern.compile("[oO]\\s*=\\s*([^/]*)");
			Matcher m = p.matcher(certDN);
			if(m.find()) {
				additionalInformations.put("o", m.group(1));
			}
			// Get CN from the certificate
			Pattern p2 = Pattern.compile("CN=([^/]*)");
			Matcher m2 = p2.matcher(certDN);
			if(m2.find()) {
				additionalInformations.put("cn", m2.group(1));
			}

			// Get the X.509 certificate object
			X509Certificate[] certs = (X509Certificate[]) req.getAttribute("javax.servlet.request.X509Certificate");

			// Get the emails
			if (certs != null && certs.length > 0 && certs[0] != null) {
				String emails = "";

				Collection<List<?>> altNames;
				try {
					altNames = certs[0].getSubjectAlternativeNames();
					if (altNames != null) {
						for (List<?> entry : altNames) {
							if (((Integer) entry.get(0)) == 1) {
								emails = (String) entry.get(1);
							}
						}
					}
				} catch (CertificateParsingException e) {
					log.error("Error during parsing certificate {}", Arrays.asList(certs));
				}

				additionalInformations.put("mail", emails);
			}
		}

		//store selected attributes for update
		for (AttributeDefinition attr : config.getAttributesForUpdate().getOrDefault(extSourceType,Collections.emptyList())) {
			String attrValue = (String) req.getAttribute(attr.getFriendlyName());
			if(attrValue!=null) {
				//fix shibboleth encoding
				if (ExtSourcesManager.EXTSOURCE_IDP.equals(extSourceType)) {
					try {
						attrValue = new String(attrValue.getBytes("iso-8859-1"), "utf-8");
					} catch (UnsupportedEncodingException e) {
						log.error("utf-8 is not known");
					}
				}
				log.debug("storing {}={} to additionalInformations", attr.getFriendlyName(), attrValue);
				additionalInformations.put(attr.getFriendlyName(), attrValue);
			}
		}

		// If the RPC was called by the user who can do delegation and delegatedLogin is set, set the values sent in the request
		if (des != null && extLogin != null) {
			List<String> powerUsers = config.getRpcPowerusers();
			if (powerUsers.contains(extLogin) && des.contains(DELEGATED_LOGIN)) {
				// Rewrite the remoteUser and extSource
				extLogin = des.readString(DELEGATED_LOGIN);
				extSourceName = des.readString(DELEGATED_EXTSOURCE_NAME);
				extSourceType = des.readString(DELEGATED_EXTSOURCE_TYPE);
				// Clear additionalInformations because they were valid only to the user who can do delegation
				additionalInformations.clear();
			}
		}

		// extSourceLoa must be number, if any specified then set to 0
		if (isEmpty(extSourceLoaString)) {
			extSourceLoa = 0;
		} else {
			try {
				extSourceLoa = Integer.parseInt(extSourceLoaString);
			} catch (NumberFormatException ex) {
				extSourceLoa = 0;
			}
		}

		// Check if any of authentication system returns extLogin and extSourceName
		if (isEmpty(extLogin) || isEmpty(extSourceName)) {
			throw new UserNotExistsException("extLogin or extSourceName is empty");
		}
		log.trace("creating PerunPrincipal(actor={},extSourceName={},extSourceType={},extSourceLoa={},additionalInformations={})",extLogin,extSourceName, extSourceType, extSourceLoa, additionalInformations);
		return new PerunPrincipal(extLogin, extSourceName, extSourceType, extSourceLoa, additionalInformations);
	}

	private PerunClient setupPerunClient(HttpServletRequest req) {

		if (isNotEmpty(req.getHeader(OIDC_CLAIM_SUB))) {
			String clientId = req.getHeader(OIDC_CLAIM_CLIENT_ID);
			List<String> scopes = Arrays.asList(req.getHeader(OIDC_CLAIM_SCOPE).split(" "));
			log.debug("detected OIDC/OAuth2 client {} with scopes {} for sub {}", clientId, scopes, req.getHeader(OIDC_CLAIM_SUB));
			return new PerunClient(clientId, scopes);
		}

		// If no OIDC header is present means it is not OAuth2 scenario => return trustful internal client.
		return new PerunClient();
	}


	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		checkOriginHeader(req,resp);
		if (req.getPathInfo() == null || req.getPathInfo().equals("/")) {
			resp.setContentType("text/plain; charset=utf-8");
			Writer wrt = resp.getWriter();

			PerunPrincipal perunPrincipal;
			try {
				perunPrincipal = setupPerunPrincipal(req,null);
				wrt.write("OK! Version: " + getPerunRpcVersion() + ", User: " + perunPrincipal.getActor() + ", extSource: " + perunPrincipal.getExtSourceName());
			} catch (InternalErrorException | UserNotExistsException e) {
				wrt.write("ERROR! Exception " + e.getMessage());
			}

			wrt.write("\n");

			wrt.close();
		} else {
			serve(req, resp, true, false);
		}
	}

	private static final String PERUN_RPC_POM_FILE = "/META-INF/maven/cz.metacentrum.perun/perun-rpc/pom.properties";
	private String version = null;

	private synchronized String getPerunRpcVersion() {
		if (version == null) {
			try {
				Properties p = new Properties();
				p.load(getServletContext().getResourceAsStream(PERUN_RPC_POM_FILE));
				version = p.getProperty("version");
			} catch (IOException e) {
				log.error("cannot read file " + PERUN_RPC_POM_FILE, e);
				version = "UNKNOWN";
			}
		}
		return version;
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		checkOriginHeader(req,resp);
		serve(req, resp, false, true);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		checkOriginHeader(req,resp);
		serve(req, resp, false, false);
	}

	/**
	 * OPTIONS method is called by CORS pre-flight requests made by JavaScript clients running in browsers.
	 * The response must set CORS headers that allow the next request.
	 * @param req HTTP request
	 * @param resp HTTP response
	 */
	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (checkOriginHeader(req,resp)) {
			resp.setHeader("Access-Control-Allow-Methods","GET, POST, OPTIONS");
			resp.setHeader("Access-Control-Allow-Headers","Authorization, Content-Type");
			resp.setIntHeader("Access-Control-Max-Age",86400);
		}
		resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}

	/**
	 * Check Origin header, if it's between allowed domains for CORS
	 *
	 * @param req HttpServletRequest to check
	 * @param resp HttpServletResponse to modify
	 */
	private boolean checkOriginHeader(HttpServletRequest req, HttpServletResponse resp) {
		String origin = req.getHeader("Origin");
		log.trace("Incoming Origin header: {}", origin);

		if (origin != null) {
			List<String> allowedDomains = BeansUtils.getCoreConfig().getAllowedCorsDomains();
			if (allowedDomains.contains(origin)) {
				log.trace("Adding header Access-Control-Allow-Origin to response: {}", origin);
				resp.setHeader("Access-Control-Allow-Origin",origin);
				resp.setHeader("Vary","Origin");
				return true;
			}
		} else {
			// no origin, don't modify header
			// origin = "*";
		}

		return false;
	}

	@SuppressWarnings("ConstantConditions")
	private void serve(HttpServletRequest req, HttpServletResponse resp, boolean isGet, boolean isPut) throws IOException {
		Serializer ser = null;
		String manager = "N/A";
		String method = "N/A";
		boolean isJsonp = false;
		PerunRequest perunRequest = null;
		ApiCaller caller;
		String callbackName = req.getParameter("callback");

		long timeStart = System.currentTimeMillis();
		caller = (ApiCaller) req.getSession(true).getAttribute(APICALLER);

		OutputStream out = resp.getOutputStream();

		// init pending request in HTTP session
		if (req.getSession().getAttribute(PERUNREQUESTS) == null) {
			req.getSession().setAttribute(PERUNREQUESTS, new ConcurrentSkipListMap<String, PerunRequest>());
		}

		// store pending requests locally, because accessing it from session object after response is written would cause IllegalStateException
		@SuppressWarnings("unchecked")
		ConcurrentSkipListMap<String, PerunRequest> pendingRequests = ((ConcurrentSkipListMap<String, PerunRequest>) req.getSession().getAttribute(PERUNREQUESTS));

		// Check if it is request for list of pending operations.
		if (req.getPathInfo().equals("/jsonp/" + PERUNREQUESTSURL)) {
			// name used to identify pending request
			String callbackId = req.getParameter("callbackId");
			JsonSerializerJSONP serializer = new JsonSerializerJSONP(out, req, resp);
			resp.setContentType(serializer.getContentType());
			try {
				// Create a copy of the PERUNREQUESTS and then pass it to the serializer
				if (callbackId != null) {
					// return single entry
					serializer.write(pendingRequests.get(callbackId));
				} else {
					// return all pending requests
					serializer.write(Arrays.asList(pendingRequests.values().toArray()));
				}
			} catch (RpcException e) {
				serializer.writePerunException(e);
			}
			out.close();
			return;
		}

		//prepare result object
		Object result = null;

		try {
			String[] fcm; //[0] format, [1] class, [2] method
			try {
				if (req.getPathInfo() == null) {
					throw new RpcException(RpcException.Type.NO_PATHINFO);
				}

				fcm = req.getPathInfo().substring(1).split("/", 3);
				if (fcm.length != 3 || fcm[2].isEmpty()) {
					throw new RpcException(RpcException.Type.INVALID_URL, req.getPathInfo());
				}
				manager = fcm[1];
				method = fcm[2];

				ser = selectSerializer(fcm[0], manager, method, out, req, resp);

				// what is the output format?
				if ("jsonp".equalsIgnoreCase(fcm[0])) {
					isJsonp = true;
				}

				if (ser instanceof TextFileSerializer) {
					resp.addHeader("Content-Disposition", "attachment; filename=\"output.txt\"");
				} else if (ser instanceof SvgGraphvizSerializer) {
					resp.addHeader("Content-Disposition", "attachment; filename=\"output.svg\"");
				} else if (ser instanceof PdfSerializer) {
					resp.addHeader("Content-Disposition", "attachment; filename=\"output.pdf\"");
				}

				resp.setContentType(ser.getContentType());
			} catch (RpcException rex) {
				//selects the default serializer (json) before throwing the exception
				ser = new JsonSerializer(out);
				resp.setContentType(ser.getContentType());
				throw rex;
			}

			// Initialize deserializer
			Deserializer des;
			if (isGet) {
				des = new UrlDeserializer(req);
			} else {
				des = selectDeserializer(fcm[0], req);
			}

			// We have new request, so do the whole auth/authz stuff
			if (caller == null) {
				caller = new ApiCaller(getServletContext(), setupPerunPrincipal(req, des), setupPerunClient(req));
				// Store the current session
				req.getSession(true).setAttribute(APICALLER, caller);
			} else if (!Objects.equals(caller.getSession().getPerunPrincipal().getExtSourceName(), getExtSourceName(req, des))) {
				// If the user is coming from the URL protected by different authN mechanism, destroy and create session again
				caller = new ApiCaller(getServletContext(), setupPerunPrincipal(req, des), setupPerunClient(req));
				req.getSession(true).setAttribute(APICALLER, caller);
			} else if (!Objects.equals(caller.getSession().getPerunPrincipal().getActor(), getActor(req, des)) &&
					!caller.getSession().getPerunPrincipal().getExtSourceName().equals(ExtSourcesManager.EXTSOURCE_NAME_LOCAL)) {
				// prevent cookie stealing (if remote user changed, rebuild session)
				caller = new ApiCaller(getServletContext(), setupPerunPrincipal(req, des), setupPerunClient(req));
				req.getSession(true).setAttribute(APICALLER, caller);
			}
			// Does user want to logout from perun?
			if ("utils".equals(manager) && "logout".equals(method)) {
				if (req.getSession(false) != null) {
					req.getSession().removeAttribute(APICALLER);

					// deletes the cookies
					Cookie[] cookies = req.getCookies();
					if (cookies != null) {
						final String SHIBBOLETH_COOKIE_FORMAT = "^_shib.+$";

						for (Cookie c : cookies) {
							// if shibboleth cookie
							if (c.getName().matches(SHIBBOLETH_COOKIE_FORMAT)) {
								// remove it
								c.setValue("0");
								c.setMaxAge(0);
								// add updated cookie to the response
								resp.addCookie(c);
							}
						}
					}
					// Invalidate session

					req.getSession().invalidate();
				}

				ser.write("Logout");
				// closes the request
				out.close();
				return;

			} else if ("utils".equals(manager) && "getGuiConfiguration".equals(method)) {

				ser.write(BeansUtils.getAllPropertiesFromCustomConfiguration("perun-web-gui.properties"));
				// closes the request
				out.close();
				return;

			} else if ("utils".equals(manager) && PERUNSTATUS.equals(method)) {
				Date date = new Date();
				Timestamp timestamp = new Timestamp(date.getTime());

				Map<String, Integer> auditerConsumers;
				//noinspection unchecked
				auditerConsumers = (Map<String, Integer>) caller.call("auditMessagesManager", "getAllAuditerConsumers", des);

				List<String> perunStatus = new ArrayList<>();
				perunStatus.add("Version of PerunDB: " + caller.call("databaseManager", "getCurrentDatabaseVersion", des));
				perunStatus.add("Version of Servlet: " + getServletContext().getServerInfo());
				perunStatus.add("Version of DB-driver: " + caller.call("databaseManager", "getDatabaseDriverInformation", des));
				perunStatus.add("Version of DB: " + caller.call("databaseManager", "getDatabaseInformation", des));
				perunStatus.add("Version of Java platform: " + System.getProperty("java.version"));
				for (String consumerName : auditerConsumers.keySet()) {
					Integer lastProcessedId = auditerConsumers.get(consumerName);
					perunStatus.add("AuditerConsumer: '" + consumerName + "' with last processed id='" + lastProcessedId + "'");
				}
				perunStatus.add("LastMessageId: " + caller.call("auditMessagesManager", "getLastMessageId", des));
				perunStatus.add("Timestamp: " + timestamp);
				ser.write(perunStatus);

				out.close();
				return;
			} else if ("utils".equals(manager) && PERUNSTATISTICS.equals(method)) {
				Date date = new Date();
				Timestamp timestamp = new Timestamp(date.getTime());

				List<String> perunStatistics = new ArrayList<>();
				perunStatistics.add("Timestamp: '" + timestamp + "'");
				perunStatistics.add("USERS: '" + caller.call("usersManager", "getUsersCount", des) + "'");
				perunStatistics.add("FACILITIES: '" + caller.call("facilitiesManager", "getFacilitiesCount", des) + "'");
				perunStatistics.add("DESTINATIONS: '" + caller.call("servicesManager", "getDestinationsCount", des) + "'");
				perunStatistics.add("VOS: '" + caller.call("vosManager", "getVosCount", des) + "'");
				perunStatistics.add("RESOURCES: '" + caller.call("resourcesManager", "getResourcesCount", des) + "'");
				perunStatistics.add("GROUPS: '" + caller.call("groupsManager", "getGroupsCount", des) + "'");
				perunStatistics.add("AUDITMESSAGES: '" + caller.call("auditMessagesManager", "getAuditerMessagesCount", des) + "'");
				ser.write(perunStatistics);

				out.close();
				return;
			} else if ("utils".equals(manager) && PERUNSYSTEMTIME.equals(method)) {
				long systemTimeInMillis = System.currentTimeMillis();
				ser.write(systemTimeInMillis);
				out.close();
			}

			// In case of GET requests (read ones) set changing state to false
			caller.setStateChanging(!isGet);

			// Store identification of the request only if supported by app (it passed unique callbackName)
			if (callbackName != null) {

				perunRequest = new PerunRequest(caller.getSession().getPerunPrincipal(), callbackName,
						manager, method, des.readAll());

				// Add perunRequest into the queue of the requests for POST only
				if (!isGet && !isPut) {
					pendingRequests.put(callbackName, perunRequest);
				}

			}

			PerunClient perunClient = caller.getSession().getPerunClient();
			if(perunClient.getType() == PerunClient.Type.OAUTH) {
				if(!perunClient.getScopes().contains(PerunClient.PERUN_API_SCOPE)) {
					//user has not consented to scope perun_api for the client on the OAuth Authorization Server
					throw new PrivilegeException("Scope "+PerunClient.PERUN_API_SCOPE+" is missing, either the client app "+perunClient.getId()+" has not asked for it, or the user has not granted it.");
				}
			}

			// Process request and sent the response back
			if (VOOTMANAGER.equals(manager)) {
				// Process VOOT protocol
				result = caller.getVOOTManager().process(caller.getSession(), method, des.readAll());
				if (perunRequest != null) perunRequest.setResult(result);
				ser.write(result);
			} else if (SCIMMANAGER.equals(manager)) {
				// Process SCIM protocol
				result = caller.getSCIMManager().process(caller.getSession(), method, des.readAll());
				if (perunRequest != null) perunRequest.setResult(result);
				if (!(result instanceof Response)) throw new InternalErrorException("SCIM manager returned unexpected result: " + result);
				resp.setStatus(((Response) result).getStatus());
				String response = (String) ((Response) result).getEntity();
				PrintWriter printWriter = new PrintWriter(resp.getOutputStream());
				printWriter.println(response);
				printWriter.flush();
				printWriter.close();
			} else {
				//Save only exceptions from caller to result
				try {
					result = caller.call(manager, method, des);
					if (perunRequest != null) perunRequest.setResult(result);
				} catch (Exception ex) {
					result = ex;
					throw ex;
				}
				ser.write(result);
			}
		} catch (PerunException pex) {
			// If the output is JSONP, it cannot send the HTTP 400 code, because the web browser wouldn't accept this
			if (!isJsonp) {
				resp.setStatus(400);
			}
			ser.writePerunException(pex);
		} catch (PerunRuntimeException prex) {
			// If the output is JSONP, it cannot send the HTTP 400 code, because the web browser wouldn't accept this
			if (!isJsonp) {
				resp.setStatus(400);
			}
			ser.writePerunRuntimeException(prex);
		} catch (IOException ioex) { //IOException gets logged and is rethrown
			//noinspection ThrowableNotThrown
			new RpcException(RpcException.Type.UNCATCHED_EXCEPTION, ioex);
			throw ioex;
		} catch (Exception ex) {
			// If the output is JSONP, it cannot send the HTTP 400 code, because the web browser wouldn't accept this
			if (!isJsonp) {
				resp.setStatus(500);
			}
			ser.writePerunException(new RpcException(RpcException.Type.UNCATCHED_EXCEPTION, ex));
		} finally {
			if (!isGet && !isPut && perunRequest != null) {
				//save result of this perunRequest
				perunRequest.setEndTime(System.currentTimeMillis());
				if (result instanceof Exception) perunRequest.setResult(result);
				perunRequest.setEndTime(System.currentTimeMillis());
			}
			//Check all resolved requests and remove them if they are old than timeToLiveWhenDone
			Iterator<String> iterator = pendingRequests.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				PerunRequest value = pendingRequests.get(key);
				if (value != null) {
					if (value.getEndTime() < 0) continue;
					if (System.currentTimeMillis() - value.getEndTime() > timeToLiveWhenDone) {
						iterator.remove();
					}
				}
			}
		}

		out.close();

		log.debug("Method {}.{} called by {} from {}, duration {} ms.", manager, method, caller.getSession().getPerunPrincipal().getActor(), caller.getSession().getPerunPrincipal().getExtSourceName(), (System.currentTimeMillis() - timeStart));
	}

	private Serializer selectSerializer(String format, String manager, String method, OutputStream out,
		                                HttpServletRequest req, HttpServletResponse resp) throws IOException, RpcException {
		Serializer serializer;

		switch (Formats.match(format)) {
			case json:
				serializer = new JsonSerializer(out);
				break;
			case jsonp:
				serializer = new JsonSerializerJSONP(out, req, resp);
				break;
			case urlinjsonout:
				serializer = new JsonSerializer(out);
				break;
			case voot:
				serializer = new JsonSerializer(out);
				break;
			case jsonsimple:
				serializer = new JsonSerializerJSONSIMPLE(out);
				break;
			case txt:
				serializer = new TextFileSerializer(out);
				break;
			default:
				throw new RpcException(RpcException.Type.UNKNOWN_SERIALIZER_FORMAT, format);
		}

		// handle special cases of returning file attachments to certain methods

		if ("attributesManager".equals(manager)) {
			if ("getAttributeModulesDependenciesGraphText".equals(method)) {
				serializer = new TextFileSerializer(out);
			} else if ("getAttributeModulesDependenciesGraphImage".equals(method)) {
				serializer = new SvgGraphvizSerializer(out);
			}
		}

		if ("usersManager".equals(manager)) {
			if ("changePasswordRandom".equals(method)) {
				serializer = new PdfSerializer(out);
			}
		}

		return serializer;
	}

	private Deserializer selectDeserializer(String format, HttpServletRequest req) throws IOException, RpcException {
		switch (Formats.match(format)) {
			case json:
			case jsonp:
			case jsonsimple:
				return new JsonDeserializer(req);
			case urlinjsonout:
			case voot:
				return new UrlDeserializer(req);
			default:
				throw new RpcException(RpcException.Type.UNKNOWN_DESERIALIZER_FORMAT, format);
		}
	}

	/**
	 * This enum represents possible request/response content formats.
	 */
	public enum Formats {

		NOMATCH,
		urlinjsonout,
		json,
		jsonp,
		voot,
		txt,
		svg,
		jsonsimple;

		/**
		 * Matches a string with the enum's values.
		 *
		 * @param str the string to match
		 * @return the matched value or {@code NOMATCH} if no matching value is found
		 * @throws NullPointerException if {@code str} is null
		 */
		public static Formats match(String str) {
			try {
				return valueOf(str);
			} catch (IllegalArgumentException ex) {
				return NOMATCH;
			}
		}
	}

}
