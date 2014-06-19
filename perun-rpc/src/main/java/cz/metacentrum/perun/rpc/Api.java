package cz.metacentrum.perun.rpc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunRequest;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.rt.PerunRuntimeException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.blImpl.AttributesManagerBlImpl;
import cz.metacentrum.perun.core.impl.AttributesManagerImpl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import cz.metacentrum.perun.rpc.deserializer.JsonDeserializer;
import cz.metacentrum.perun.rpc.deserializer.UrlDeserializer;
import cz.metacentrum.perun.rpc.serializer.JsonSerializer;
import cz.metacentrum.perun.rpc.serializer.JsonSerializerJSONP;
import cz.metacentrum.perun.rpc.serializer.Serializer;
import java.util.Date;
import java.sql.Timestamp;

@SuppressWarnings("serial")
/**
 * HTTP servlet wrapping Perun's method calls. Returned values are serialized and sent as an HTTP response.
 * <p>
 * URL format: "/format/class/method"
 *
 * @author Jan Klos <ddd@mail.muni.cz>
 * @author Ales Krenek
 * @since 0.1
 */
public class Api extends HttpServlet {

	private final static String APICALLER = "apiCaller";
	private final static String PERUNREQUESTS = "perunRequests";
	private final static String PERUNREQUESTSURL = "getPendingRequests";
	private final static String PERUNSTATUS = "getPerunStatus";
	private final static Logger log = LoggerFactory.getLogger(ApiCaller.class);
	private final static String VOOTMANAGER = "vootManager";

	@Override
	public void init() {
		if (getServletContext().getAttribute(PERUNREQUESTS) == null) {
			getServletContext().setAttribute(PERUNREQUESTS, new CopyOnWriteArrayList<PerunRequest>());
		}
	}
	protected PerunPrincipal setupPerunPrincipal(HttpServletRequest req) throws InternalErrorException, RpcException, UserNotExistsException {
		return this.setupPerunPrincipal(req, null);
	}

	protected String getExtSourceName(HttpServletRequest req, Deserializer des) throws RpcException {
		if (req.getHeader("Shib-Identity-Provider") != null && !req.getHeader("Shib-Identity-Provider").isEmpty()) {
			return (String) req.getHeader("Shib-Identity-Provider");
		} else if (req.getAttribute("SSL_CLIENT_VERIFY") != null && ((String) req.getAttribute("SSL_CLIENT_VERIFY")).equals("SUCCESS")){
			return (String) req.getAttribute("SSL_CLIENT_I_DN");
		} else if (req.getAttribute("EXTSOURCE") != null) {
			return (String) req.getAttribute("EXTSOURCE");
		} else {
			return (String) des.readString("delegatedExtSourceName");
		}
	}

	protected PerunPrincipal setupPerunPrincipal(HttpServletRequest req, Deserializer des) throws InternalErrorException, RpcException, UserNotExistsException {
		String extSourceLoaString = null;
		String extLogin = null;
		String extSourceName = null;
		String extSourceType = null;
		int extSourceLoa = 0;
		Map<String, String> additionalInformations = new HashMap<String, String>();

		// If we have header Shib-Identity-Provider, then the user uses identity federation to authenticate
		if (req.getHeader("Shib-Identity-Provider") != null && !req.getHeader("Shib-Identity-Provider").isEmpty()) {
			extSourceName = (String) req.getHeader("Shib-Identity-Provider");
			extSourceType = ExtSourcesManager.EXTSOURCE_IDP;
			if (req.getHeader("loa") != null && ! req.getHeader("loa").isEmpty()) {
				extSourceLoaString = req.getHeader("loa");
			} else {
				extSourceLoaString = "2";
			}
			// FIXME: find better place where do the operation with attributes from federation
			if (req.getHeader("eppn") != null && ! req.getHeader("eppn").isEmpty()) {
				try {
					String eppn = new String(req.getHeader("eppn").getBytes("ISO-8859-1"));

					// Remove scope from the eppn attribute
					additionalInformations.put("eppnwoscope", eppn.replaceAll("(.*)@.*", "$1"));
				} catch (UnsupportedEncodingException e) {
					log.error("Cannot encode header eppn with value from ISO-8859-1.");
				}
			}
			if (req.getRemoteUser() != null && !req.getRemoteUser().isEmpty()) {
				extLogin = req.getRemoteUser();
			}
		}

		// X509 cert was used
		else if (req.getAttribute("SSL_CLIENT_VERIFY") != null && ((String) req.getAttribute("SSL_CLIENT_VERIFY")).equals("SUCCESS")){
			extSourceName = (String) req.getAttribute("SSL_CLIENT_I_DN");
			extSourceType = ExtSourcesManager.EXTSOURCE_X509;
			extSourceLoaString = (String) req.getAttribute("EXTSOURCELOA");
			extLogin = (String) req.getAttribute("SSL_CLIENT_S_DN");

			// Store X509 certificate in the additionalInformations structure
			additionalInformations.put("userCertificates",
					AttributesManagerBlImpl.escapeMapAttributeValue((String) req.getAttribute("SSL_CLIENT_S_DN")) + AttributesManagerImpl.KEY_VALUE_DELIMITER +
					AttributesManagerBlImpl.escapeMapAttributeValue((String) req.getAttribute("SSL_CLIENT_CERT")));
			additionalInformations.put("userCertDNs",
					AttributesManagerBlImpl.escapeMapAttributeValue((String) req.getAttribute("SSL_CLIENT_S_DN")) + AttributesManagerImpl.KEY_VALUE_DELIMITER +
					AttributesManagerBlImpl.escapeMapAttributeValue((String) req.getAttribute("SSL_CLIENT_I_DN")));

			// Store X509
			additionalInformations.put("SSL_CLIENT_S_DN", (String) req.getAttribute("SSL_CLIENT_S_DN"));
			additionalInformations.put("dn", (String) req.getAttribute("SSL_CLIENT_S_DN"));

			// Get the X.509 certificate object
			X509Certificate[] certs = (X509Certificate[]) req.getAttribute("javax.servlet.request.X509Certificate");

			// Get the emails
			if (certs != null && certs.length > 0 && certs[0] != null) {
				String emails = "";

				Collection<List<?>> altNames;
				try {
					altNames = certs[0].getSubjectAlternativeNames();
					if (altNames != null) {
						for (List<?> entry: altNames) {
							if (((Integer) entry.get(0)) == 1) {
								emails = (String) entry.get(1);
							}
						}
					}
				} catch (CertificateParsingException e) {
					log.error("Error during parsing certificate {}", certs);
				}

				additionalInformations.put("mail", emails);

				// Get organization from the certificate
				String oRegExpPattern = "(o|O)(\\s)*=([^+,])*";
				Pattern oPattern = Pattern.compile(oRegExpPattern);
				Matcher oMatcher = oPattern.matcher(certs[0].getSubjectX500Principal().getName());
				if (oMatcher.find()) {
					String[] org = oMatcher.group().split("=");
					if (org[1] != null && !org[1].isEmpty()) {
						additionalInformations.put("o", org[1]);
					}
				}
			}
		}
		// EXT_SOURCE was defined in Apache configuration
		else if (req.getAttribute("EXTSOURCE") != null) {
			extSourceName = (String) req.getAttribute("EXTSOURCE");
			extSourceType = (String) req.getAttribute("EXTSOURCETYPE");
			extSourceLoaString = (String) req.getAttribute("EXTSOURCELOA");

			if (req.getRemoteUser() != null && !req.getRemoteUser().isEmpty()) {
				extLogin = req.getRemoteUser();
			} else if (req.getAttribute("ENV_REMOTE_USER") != null && !((String) req.getAttribute("ENV_REMOTE_USER")).isEmpty()) {
				extLogin = (String) req.getAttribute("ENV_REMOTE_USER");
			} else if (extSourceName.equals(cz.metacentrum.perun.core.api.ExtSourcesManager.EXTSOURCE_LOCAL)) {
				/** LOCAL EXTSOURCE **/
				// If ExtSource is LOCAL then generate REMOTE_USER name on the fly
				extLogin = Long.toString(System.currentTimeMillis());
			}
		}

		// Read all headers and store them in additionalInformation
		String headerName = "";
		for(Enumeration<String> headerNames = req.getHeaderNames(); headerNames.hasMoreElements();){
			headerName = (String)headerNames.nextElement();
			// Tomcat expects all headers are in ISO-8859-1
			try {
				additionalInformations.put(headerName, new String(req.getHeader(headerName).getBytes("ISO-8859-1")));
			} catch (UnsupportedEncodingException e) {
				log.error("Cannot encode header {} with value from ISO-8859-1.", headerName, req.getHeader(headerName));
			}
		}

		// If the RPC was called by the user who can do delegation and delegatedLogin is set, set the values sent in the request
		if (des != null && extLogin != null) {
			List<String> powerUsers = new ArrayList<String>(Arrays.asList(Utils.getPropertyFromConfiguration("perun.rpc.powerusers").split("[ \t]*,[ \t]*")));
			if (powerUsers.contains(extLogin) && des.contains("delegatedLogin")) {
				// Rewrite the remoteUser and extSource
				extLogin = (String) des.readString("delegatedLogin");
				extSourceName = (String) des.readString("delegatedExtSourceName");
				extSourceType = (String) des.readString("delegatedExtSourceType");
				// Clear additionalInformations because they were valid only to the user who can do delegation
				additionalInformations.clear();
			}
		}

		// extSourceLoa must be number, if any specified then set to 0
		if (extSourceLoaString == null || extSourceLoaString.isEmpty()) {
			extSourceLoa = 0;
		} else {
			try {
				extSourceLoa = Integer.parseInt(extSourceLoaString);
			} catch (NumberFormatException ex) {
				extSourceLoa = 0;
			}
		}

		// Check if any of authentication system returns extLogin and extSourceName
		if (extLogin == null || extLogin.isEmpty() || extSourceName == null || extSourceName.isEmpty()) {
			throw new UserNotExistsException("extLogin or extSourceName is empty");
		}

		return new PerunPrincipal(extLogin, extSourceName, extSourceType, extSourceLoa, additionalInformations);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if (req.getPathInfo() == null || req.getPathInfo().equals("/")) {
			resp.setContentType("text/plain; charset=utf-8");
			Writer wrt = resp.getWriter();

			PerunPrincipal perunPrincipal;
			try {
				perunPrincipal = setupPerunPrincipal(req);
				wrt.write("OK! Version: " + PerunBl.PERUNVERSION + ", User: " + perunPrincipal.getActor() + ", extSource: " + perunPrincipal.getExtSourceName());
			} catch (InternalErrorException e) {
				wrt.write("ERROR! Exception " + e.getMessage());
			} catch (RpcException e) {
				wrt.write("ERROR! Exception " + e.getMessage());
			} catch (UserNotExistsException e) {
				wrt.write("ERROR! Exception " + e.getMessage());
			}

			wrt.write("\n");

			wrt.close();
		} else {
			serve(req, resp, true);
		}
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		serve(req, resp, false);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		serve(req, resp, false);
	}

	private void serve(HttpServletRequest req, HttpServletResponse resp, boolean isGet) throws IOException {
		Serializer ser = null;
		String manager = "N/A";
		String method = "N/A";
		boolean isJsonp = false;
		PerunRequest perunRequest = null;
		ApiCaller caller;


		long timeStart = System.currentTimeMillis();
		caller = (ApiCaller) req.getSession(true).getAttribute(APICALLER);

		OutputStream out = resp.getOutputStream();

		// Check if it is request for list of pending operations.
		if (req.getPathInfo().equals("/jsonp/" + PERUNREQUESTSURL)) {
			JsonSerializerJSONP serializer = new JsonSerializerJSONP(out, req, resp);
			resp.setContentType(serializer.getContentType());
			try {
				// Create a copy of the PERUNREQUESTS and then pass it to the serializer
				List<PerunRequest> perunRequests = (List<PerunRequest>) getServletContext().getAttribute(PERUNREQUESTS);
				serializer.write(perunRequests.subList(0, perunRequests.size()));
			} catch (RpcException e) {
				serializer.writePerunException(e);
			}
			out.close();
			return;
		} 

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

				ser = selectSerializer(fcm[0], out, req, resp);

				// is the output JSONP?
				if("jsonp".equalsIgnoreCase(fcm[0])){
					isJsonp = true;
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
				caller = new ApiCaller(getServletContext(), setupPerunPrincipal(req, des));
				// Store the current session
				req.getSession(true).setAttribute(APICALLER, caller);
			} else if (!caller.getSession().getPerunPrincipal().getExtSourceName().equals(this.getExtSourceName(req, des))) {
				// If the user is coming from the URL protected by different authN mechanism, destroy and create session again
				caller = new ApiCaller(getServletContext(), setupPerunPrincipal(req, des));
				req.getSession(true).setAttribute(APICALLER, caller);
			}

			// Does user want to logout from perun?
			if("utils".equals(manager) && "logout".equals(method)) {
				if (req.getSession(false) != null) {
					req.getSession().removeAttribute(APICALLER);


					// deletes the cookies
					Cookie[] cookies = req.getCookies();
					if( cookies != null)
					{
						final String SHIBBOLETH_COOKIE_FORMAT = "^_shib.+$";

						for (int i = 0; i < cookies.length; i++) {
							Cookie c = cookies[i];
							// if shibboleth cookie
							if(c.getName().matches(SHIBBOLETH_COOKIE_FORMAT))
							{
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

				ser.write(new String("Logout"));
				// closes the request
				out.close();
				return;

			} else if ("utils".equals(manager) && "getGuiConfiguration".equals(method)) {

				ser.write(Utils.getAllPropertiesFromCustomConfiguration("perun-web-gui.properties"));
				// closes the request
				out.close();
				return;

			} else if("utils".equals(manager) && PERUNSTATUS.equals(method)) {
				perunRequest = new PerunRequest(req.getSession().getId(), caller.getSession().getPerunPrincipal(), "DatabaseManager", "getCurrentDatabaseVersion", des.readAll());
				((CopyOnWriteArrayList<PerunRequest>) getServletContext().getAttribute(PERUNREQUESTS)).add(perunRequest);
				Date date = new Date();
				Timestamp timestamp = new Timestamp(date.getTime());
				
				List<String> perunStatus = new ArrayList<>();
				perunStatus.add("Version of PerunDB: " + caller.call("databaseManager", "getCurrentDatabaseVersion", des));
				perunStatus.add("Version of Servlet: " + getServletContext().getServerInfo());
				perunStatus.add("Version of DB-driver: " + caller.call("databaseManager", "getDatabaseDriverInformation", des));
				perunStatus.add("Version of DB: " + caller.call("databaseManager", "getDatabaseInformation", des));
				perunStatus.add("Version of Java platform: " + System.getProperty("java.version"));
				perunStatus.add("Timestamp: " + timestamp);

				ser.write(perunStatus);
								
				out.close();
				return;
			}

			// In case of GET requests (read ones) set changing state to false
			caller.setStateChanging(!isGet);

			// Store identification of the request
			perunRequest = new PerunRequest(req.getSession().getId(), caller.getSession().getPerunPrincipal(),
					manager, method, des.readAll());
			// Add perunRequest into the queue of the requests
			((CopyOnWriteArrayList<PerunRequest>) getServletContext().getAttribute(PERUNREQUESTS)).add(perunRequest);

			// Process request and sent the response back
			if (VOOTMANAGER.equals(manager)) {
				// Process VOOT protocol
				ser.write(caller.getVOOTManager().process(caller.getSession(), method, des.readAll()));
			} else {
				ser.write(caller.call(manager, method, des));
			}
		} catch (PerunException pex) {
			// If the output is JSONP, it cannot send the HTTP 400 code, because the web browser wouldn't accept this
			if(!isJsonp) {
				resp.setStatus(400);
			}
			ser.writePerunException(pex);
		} catch (PerunRuntimeException prex) {
			// If the output is JSONP, it cannot send the HTTP 400 code, because the web browser wouldn't accept this
			if(!isJsonp) {
				resp.setStatus(400);
			}
			ser.writePerunRuntimeException(prex);
		} catch (IOException ioex) { //IOException gets logged and is rethrown
			new RpcException(RpcException.Type.UNCATCHED_EXCEPTION, ioex);
			throw ioex;
		} catch (Exception ex) {
			// If the output is JSONP, it cannot send the HTTP 400 code, because the web browser wouldn't accept this
			if(!isJsonp) {
				resp.setStatus(500);
			}
			ser.writePerunException(new RpcException(RpcException.Type.UNCATCHED_EXCEPTION, ex));
		} finally {
			if (perunRequest != null) {
				// Remove PerunRequest from the queue
				((CopyOnWriteArrayList<PerunRequest>) getServletContext().getAttribute(PERUNREQUESTS)).remove(perunRequest);
			}
		}

		out.close();

		log.debug("Method {}.{} called by {} from {}, duration {} ms.", new Object[] {manager, method, caller.getSession().getPerunPrincipal().getActor(), caller.getSession().getPerunPrincipal().getExtSourceName(), (System.currentTimeMillis()-timeStart)});
	}

	private Serializer selectSerializer(String format, OutputStream out, HttpServletRequest req, HttpServletResponse resp) throws IOException, RpcException {
		switch (Formats.match(format)) {
			case json:
				return new JsonSerializer(out);
			case jsonp:
				return new JsonSerializerJSONP(out, req, resp);
			case urlinjsonout:
				return new JsonSerializer(out);
			case voot:
				return new JsonSerializer(out);
			default:
				throw new RpcException(RpcException.Type.UNKNOWN_SERIALIZER_FORMAT, format);
		}
	}

	private Deserializer selectDeserializer(String format, HttpServletRequest req) throws IOException, RpcException {
		switch (Formats.match(format)) {
			case json:
			case jsonp:
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
			voot;


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
