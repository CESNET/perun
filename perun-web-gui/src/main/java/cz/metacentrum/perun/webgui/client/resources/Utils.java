package cz.metacentrum.perun.webgui.client.resources;

import com.google.gwt.http.client.URL;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebSession;

import java.util.*;

/**
 * Class with support methods used in GUI code
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class Utils {

	private static final int DEFAULT_LENGTH = 25;

	public static final String GROUP_NAME_MATCHER = "^[- a-zA-Z.0-9_:]+$";
	public static final String GROUP_SHORT_NAME_MATCHER = "^[- a-zA-Z.0-9_]+$";
	public static final String VO_SHORT_NAME_MATCHER = "^[-a-zA-Z0-9_.]+$";
	public static final String ATTRIBUTE_FRIENDLY_NAME_MATCHER = "^[-a-zA-Z0-9]+([:][-a-zA-Z0-9]+)?$";
	public static final String LOGIN_VALUE_MATCHER = "^[a-zA-Z0-9_][-A-z0-9_.@/]*$";
	public static final String FACILITY_NAME_MATCHER = "^[ a-zA-Z.0-9_-]+$";
	public static final String SERVICE_NAME_MATCHER = "^[a-zA-Z0-9_]+$";
	public static final String SERVICE_NAME_TO_SCRIP_PATH_MATCHER= "[^a-zA-Z0-9-_]+";

	// Unicode name parsing
	private static final String pL = "A-Za-z\\xAA\\xB5\\xBA\\xC0-\\xD6\\xD8-\\xF6\\xF8-\\u02C1\\u02C6-\\u02D1\\u02E0-\\u02E4\\u02EC\\u02EE\\u0370-\\u0374\\u0376\\u0377\\u037A-\\u037D\\u0386\\u0388-\\u038A\\u038C\\u038E-\\u03A1\\u03A3-\\u03F5\\u03F7-\\u0481\\u048A-\\u0527\\u0531-\\u0556\\u0559\\u0561-\\u0587\\u05D0-\\u05EA\\u05F0-\\u05F2\\u0620-\\u064A\\u066E\\u066F\\u0671-\\u06D3\\u06D5\\u06E5\\u06E6\\u06EE\\u06EF\\u06FA-\\u06FC\\u06FF\\u0710\\u0712-\\u072F\\u074D-\\u07A5\\u07B1\\u07CA-\\u07EA\\u07F4\\u07F5\\u07FA\\u0800-\\u0815\\u081A\\u0824\\u0828\\u0840-\\u0858\\u08A0\\u08A2-\\u08AC\\u0904-\\u0939\\u093D\\u0950\\u0958-\\u0961\\u0971-\\u0977\\u0979-\\u097F\\u0985-\\u098C\\u098F\\u0990\\u0993-\\u09A8\\u09AA-\\u09B0\\u09B2\\u09B6-\\u09B9\\u09BD\\u09CE\\u09DC\\u09DD\\u09DF-\\u09E1\\u09F0\\u09F1\\u0A05-\\u0A0A\\u0A0F\\u0A10\\u0A13-\\u0A28\\u0A2A-\\u0A30\\u0A32\\u0A33\\u0A35\\u0A36\\u0A38\\u0A39\\u0A59-\\u0A5C\\u0A5E\\u0A72-\\u0A74\\u0A85-\\u0A8D\\u0A8F-\\u0A91\\u0A93-\\u0AA8\\u0AAA-\\u0AB0\\u0AB2\\u0AB3\\u0AB5-\\u0AB9\\u0ABD\\u0AD0\\u0AE0\\u0AE1\\u0B05-\\u0B0C\\u0B0F\\u0B10\\u0B13-\\u0B28\\u0B2A-\\u0B30\\u0B32\\u0B33\\u0B35-\\u0B39\\u0B3D\\u0B5C\\u0B5D\\u0B5F-\\u0B61\\u0B71\\u0B83\\u0B85-\\u0B8A\\u0B8E-\\u0B90\\u0B92-\\u0B95\\u0B99\\u0B9A\\u0B9C\\u0B9E\\u0B9F\\u0BA3\\u0BA4\\u0BA8-\\u0BAA\\u0BAE-\\u0BB9\\u0BD0\\u0C05-\\u0C0C\\u0C0E-\\u0C10\\u0C12-\\u0C28\\u0C2A-\\u0C33\\u0C35-\\u0C39\\u0C3D\\u0C58\\u0C59\\u0C60\\u0C61\\u0C85-\\u0C8C\\u0C8E-\\u0C90\\u0C92-\\u0CA8\\u0CAA-\\u0CB3\\u0CB5-\\u0CB9\\u0CBD\\u0CDE\\u0CE0\\u0CE1\\u0CF1\\u0CF2\\u0D05-\\u0D0C\\u0D0E-\\u0D10\\u0D12-\\u0D3A\\u0D3D\\u0D4E\\u0D60\\u0D61\\u0D7A-\\u0D7F\\u0D85-\\u0D96\\u0D9A-\\u0DB1\\u0DB3-\\u0DBB\\u0DBD\\u0DC0-\\u0DC6\\u0E01-\\u0E30\\u0E32\\u0E33\\u0E40-\\u0E46\\u0E81\\u0E82\\u0E84\\u0E87\\u0E88\\u0E8A\\u0E8D\\u0E94-\\u0E97\\u0E99-\\u0E9F\\u0EA1-\\u0EA3\\u0EA5\\u0EA7\\u0EAA\\u0EAB\\u0EAD-\\u0EB0\\u0EB2\\u0EB3\\u0EBD\\u0EC0-\\u0EC4\\u0EC6\\u0EDC-\\u0EDF\\u0F00\\u0F40-\\u0F47\\u0F49-\\u0F6C\\u0F88-\\u0F8C\\u1000-\\u102A\\u103F\\u1050-\\u1055\\u105A-\\u105D\\u1061\\u1065\\u1066\\u106E-\\u1070\\u1075-\\u1081\\u108E\\u10A0-\\u10C5\\u10C7\\u10CD\\u10D0-\\u10FA\\u10FC-\\u1248\\u124A-\\u124D\\u1250-\\u1256\\u1258\\u125A-\\u125D\\u1260-\\u1288\\u128A-\\u128D\\u1290-\\u12B0\\u12B2-\\u12B5\\u12B8-\\u12BE\\u12C0\\u12C2-\\u12C5\\u12C8-\\u12D6\\u12D8-\\u1310\\u1312-\\u1315\\u1318-\\u135A\\u1380-\\u138F\\u13A0-\\u13F4\\u1401-\\u166C\\u166F-\\u167F\\u1681-\\u169A\\u16A0-\\u16EA\\u1700-\\u170C\\u170E-\\u1711\\u1720-\\u1731\\u1740-\\u1751\\u1760-\\u176C\\u176E-\\u1770\\u1780-\\u17B3\\u17D7\\u17DC\\u1820-\\u1877\\u1880-\\u18A8\\u18AA\\u18B0-\\u18F5\\u1900-\\u191C\\u1950-\\u196D\\u1970-\\u1974\\u1980-\\u19AB\\u19C1-\\u19C7\\u1A00-\\u1A16\\u1A20-\\u1A54\\u1AA7\\u1B05-\\u1B33\\u1B45-\\u1B4B\\u1B83-\\u1BA0\\u1BAE\\u1BAF\\u1BBA-\\u1BE5\\u1C00-\\u1C23\\u1C4D-\\u1C4F\\u1C5A-\\u1C7D\\u1CE9-\\u1CEC\\u1CEE-\\u1CF1\\u1CF5\\u1CF6\\u1D00-\\u1DBF\\u1E00-\\u1F15\\u1F18-\\u1F1D\\u1F20-\\u1F45\\u1F48-\\u1F4D\\u1F50-\\u1F57\\u1F59\\u1F5B\\u1F5D\\u1F5F-\\u1F7D\\u1F80-\\u1FB4\\u1FB6-\\u1FBC\\u1FBE\\u1FC2-\\u1FC4\\u1FC6-\\u1FCC\\u1FD0-\\u1FD3\\u1FD6-\\u1FDB\\u1FE0-\\u1FEC\\u1FF2-\\u1FF4\\u1FF6-\\u1FFC\\u2071\\u207F\\u2090-\\u209C\\u2102\\u2107\\u210A-\\u2113\\u2115\\u2119-\\u211D\\u2124\\u2126\\u2128\\u212A-\\u212D\\u212F-\\u2139\\u213C-\\u213F\\u2145-\\u2149\\u214E\\u2183\\u2184\\u2C00-\\u2C2E\\u2C30-\\u2C5E\\u2C60-\\u2CE4\\u2CEB-\\u2CEE\\u2CF2\\u2CF3\\u2D00-\\u2D25\\u2D27\\u2D2D\\u2D30-\\u2D67\\u2D6F\\u2D80-\\u2D96\\u2DA0-\\u2DA6\\u2DA8-\\u2DAE\\u2DB0-\\u2DB6\\u2DB8-\\u2DBE\\u2DC0-\\u2DC6\\u2DC8-\\u2DCE\\u2DD0-\\u2DD6\\u2DD8-\\u2DDE\\u2E2F\\u3005\\u3006\\u3031-\\u3035\\u303B\\u303C\\u3041-\\u3096\\u309D-\\u309F\\u30A1-\\u30FA\\u30FC-\\u30FF\\u3105-\\u312D\\u3131-\\u318E\\u31A0-\\u31BA\\u31F0-\\u31FF\\u3400-\\u4DB5\\u4E00-\\u9FCC\\uA000-\\uA48C\\uA4D0-\\uA4FD\\uA500-\\uA60C\\uA610-\\uA61F\\uA62A\\uA62B\\uA640-\\uA66E\\uA67F-\\uA697\\uA6A0-\\uA6E5\\uA717-\\uA71F\\uA722-\\uA788\\uA78B-\\uA78E\\uA790-\\uA793\\uA7A0-\\uA7AA\\uA7F8-\\uA801\\uA803-\\uA805\\uA807-\\uA80A\\uA80C-\\uA822\\uA840-\\uA873\\uA882-\\uA8B3\\uA8F2-\\uA8F7\\uA8FB\\uA90A-\\uA925\\uA930-\\uA946\\uA960-\\uA97C\\uA984-\\uA9B2\\uA9CF\\uAA00-\\uAA28\\uAA40-\\uAA42\\uAA44-\\uAA4B\\uAA60-\\uAA76\\uAA7A\\uAA80-\\uAAAF\\uAAB1\\uAAB5\\uAAB6\\uAAB9-\\uAABD\\uAAC0\\uAAC2\\uAADB-\\uAADD\\uAAE0-\\uAAEA\\uAAF2-\\uAAF4\\uAB01-\\uAB06\\uAB09-\\uAB0E\\uAB11-\\uAB16\\uAB20-\\uAB26\\uAB28-\\uAB2E\\uABC0-\\uABE2\\uAC00-\\uD7A3\\uD7B0-\\uD7C6\\uD7CB-\\uD7FB\\uF900-\\uFA6D\\uFA70-\\uFAD9\\uFB00-\\uFB06\\uFB13-\\uFB17\\uFB1D\\uFB1F-\\uFB28\\uFB2A-\\uFB36\\uFB38-\\uFB3C\\uFB3E\\uFB40\\uFB41\\uFB43\\uFB44\\uFB46-\\uFBB1\\uFBD3-\\uFD3D\\uFD50-\\uFD8F\\uFD92-\\uFDC7\\uFDF0-\\uFDFB\\uFE70-\\uFE74\\uFE76-\\uFEFC\\uFF21-\\uFF3A\\uFF41-\\uFF5A\\uFF66-\\uFFBE\\uFFC2-\\uFFC7\\uFFCA-\\uFFCF\\uFFD2-\\uFFD7\\uFFDA-\\uFFDC";
	private static final RegExp titleBeforePattern = RegExp.compile("^((["+pL+"]+[.])|(et))$");
	private static final RegExp firstNamePattern = RegExp.compile("^(["+pL+"'-]+)$");
	private static final RegExp lastNamePattern = RegExp.compile("^((["+pL+"'-]+)|(["+pL+"][.]))$");

	public final static String largeStringClassName = "java.lang.LargeString";
	public final static String largeArrayListClassName = "java.util.LargeArrayList";

	/**
	 * Try to parse rawName to keys: "titleBefore" "firstName" "lastName" "titleAfter"
	 *
	 * If rawName is null or empty, return map with empty values of all keys.
	 *
	 * Parsing procedure:
	 * 1] prepare array of parts by replacing all characters "," and "_" by spaces
	 * 2] change all sequence of invisible characters (space, tabulator etc.) to one space
	 * 3] one by one try to parsing parts from array
	 *  - A] try to find all titleBefore parts
	 *  - B] try to find one firstName part
	 *  - C] try to find all lastName parts
	 *  - D] if the rest is not lastName so save it to the title after
	 *
	 * Example of parsing rawName:
	 * 1] rawName = "Mgr. et Mgr.    Petr_Jiri R. Sojka, Ph.D., CSc."
	 * 2] convert all ',' and '_' to spaces: rawName = "Mgr. et Mgr.    Petr Jiri R. Sojka  Ph.D.  CSc."
	 * 3] convert more than 1 invisible char to 1 space: rawName = "Mgr. et Mgr. Petr Jiri R. Sojka Ph.D. CSc."
	 * 4] parse string to array of parts by space: ArrayOfParts= ["Mgr.","et","Mgr.","Petr","Jiri","R.","Sojka","Ph.D.","CSc."]
	 * 5] first fill everything what can be in title before: titleBefore="Mgr. et Mgr."
	 * 6] then fill everything what can be in first name (maximum 1 part): firstName="Petr"
	 * 7] then fill everything what can be in last name: lastName="Jiri R. Sojka"
	 * 8] everything else put to the title after: titleAfter="Ph.D. CSc."
	 * 9] put these variables to map like key=value, for ex.: Map["titleBefore"="Mgr. et Mgr.",firstName="Petr", ... ] and return this map
	 *
	 * @param rawName
	 * @return map string to string where are 4 keys (titleBefore,titleAfter,firstName and lastName) with their values (value can be null)
	 */
	public static Map<String, String> parseCommonName(String rawName) {
		//prepare variables and map
		Map<String, String> parsedName = new HashMap<String, String>();
		String titleBefore = "";
		String firstName = "";
		String lastName = "";
		String titleAfter = "";

		//if raw name is null or empty, skip this part and only return map with null values for keys
		if(rawName!=null && !rawName.isEmpty()) {
			// all characters ',' replace by ' ' for rawName
			rawName = rawName.replaceAll(",", " ").trim();
			// all characters '_' replace by ' ' for rawName
			rawName = rawName.replaceAll("_", " ").trim();
			// replace all invisible chars in row for
			rawName = rawName.replaceAll("\\s+", " ").trim();

			//split parts by space
			String[] nameParts = rawName.split(" ");

			//if length of nameParts is 1, save it to the lastName
			if(nameParts.length == 1) {
				lastName = nameParts[0];
				//if length of nameParts is more than 1, try to choose which part belong to which value
			} else {
				//variables for states
				boolean titleBeforeDone = false;
				boolean firstNameDone = false;
				boolean lastNameDone = false;

				//for every part try to get which one it is
				for(int i=0;i<nameParts.length;i++) {

					String part = nameParts[i];
					//trim this value (remove spaces before and after string)
					part = part.trim();

					//if titleBeforeDone is false, this string can be title before
					if(!titleBeforeDone) {
						MatchResult titleBeforeMatcher = titleBeforePattern.exec(part);
						//if title before matches
						if(titleBeforeMatcher != null) {
							//add space if this title is not first title before
							if(titleBefore.isEmpty()) titleBefore+= part;
							else titleBefore+= " " + part;
							//go on next part
							continue;
						} else {
							//this is not title before, so end part of title before and go next
							titleBeforeDone = true;
						}
					}

					//if firstNameDone is false, this string can be first name
					if(!firstNameDone) {
						MatchResult firstNameMatcher = firstNamePattern.exec(part);
						//if first name matches
						if(firstNameMatcher != null) {
							//first name can be only one
							firstName = part;
							//go on next part
							firstNameDone = true;
							continue;
						}
						//if this is not firstName skip firstName because only first word after titleBefore can be firstName
						firstNameDone = true;
					}

					//if lastNameDone is false, this string can be lastName
					if(!lastNameDone) {
						MatchResult lastNameMatcher = lastNamePattern.exec(part);
						//if last name matches
						if(lastNameMatcher != null) {

							//add space if this name is not first last name
							if(lastName.isEmpty()) lastName += part;
							else lastName+= " " + part;
							//go on next part
							continue;
							//if last name not matches
						} else {
							//because last name can't be empty, save this part to lastName even if not matches
							if(lastName.isEmpty()) {
								lastName = part;
								lastNameDone = true;
								//go on next part
								continue;
							} else {
								//if there is already something in lastName, go on title after
								lastNameDone = true;
							}
						}
					}

					//rest of parts if lastName exists go to the title after
					if(lastNameDone) {
						//add space if this is not first title after
						if(titleAfter.isEmpty()) titleAfter+= part;
						else titleAfter+= " " + part;
					}
				}
			}
		}

		//empty string means null, add variables to map
		if (titleBefore.isEmpty()) titleBefore = null;
		parsedName.put("titleBefore", titleBefore);
		if (firstName.isEmpty()) firstName = null;
		parsedName.put("firstName", firstName);
		if (lastName.isEmpty()) lastName = null;
		parsedName.put("lastName", lastName);
		if (titleAfter.isEmpty()) titleAfter = null;
		parsedName.put("titleAfter", titleAfter);

		return parsedName;
	}

	/**
	 * Return stripped string with ellipsis of custom length
	 * (whitespace before ellipsis is removed)
	 *
	 * @param text text to strip
	 * @param maxLength maximum text length
	 * @return stripped string with ellipsis
	 */
	public static String getStrippedStringWithEllipsis(String text, int maxLength) {

		if (maxLength >= text.length()) {
			return text;
		} else {
			return text.substring(0, maxLength).trim()+"\u2026";
		}
	}

	/**
	 * Return stripped string with ellipsis of default length
	 * (whitespace before ellipsis is removed)
	 *
	 * @param text text to strip
	 * @return stripped string with ellipsis
	 */
	public static String getStrippedStringWithEllipsis(String text) {

		if (DEFAULT_LENGTH >= text.length()) {
			return text;
		} else {
			return text.substring(0, DEFAULT_LENGTH).trim()+"\u2026";
		}
	}

	/**
	 * Return URL to identity consolidator GUI
	 * with optional ?target= param having current Perun GUI URL.
	 *
	 * @param target TRUE if use ?target= param in identity consolidator URL / FALSE otherwise
	 * @return URL to identity consolidator
	 */
	public static String getIdentityConsolidatorLink(boolean target) {

		String value = PerunWebSession.getInstance().getConfiguration().getCustomProperty("getIdentityConsolidatorUrl");

		if (value != null && !value.isEmpty()) {

			if (target) {
				// FIXME - ENCODE QUERY STRING 2 TIMES BECAUSE OF CONSOLIDATOR APP
				value += "?target_url=" + Window.Location.getProtocol() + "//" + Window.Location.getHost() + Window.Location.getPath() +  URL.encodeQueryString(URL.encodeQueryString(Window.Location.getQueryString()));
			}
			return value;

		} else {

			// always use URL of machine, where GUI runs
			String baseUrl = Window.Location.getProtocol() + "//" + Window.Location.getHost();

			String url = baseUrl + "/" + PerunWebSession.getInstance().getRpcServer() + "/ic/";

			if (target) {
				// FIXME - ENCODE QUERY STRING 2 TIMES BECAUSE OF CONSOLIDATOR APP
				url += "?target_url=" + Window.Location.getProtocol() + "//" + Window.Location.getHost() + Window.Location.getPath() +  URL.encodeQueryString(URL.encodeQueryString(Window.Location.getQueryString()));
			}

			return url;

		}

	}

	/**
	 * Return URL to identity consolidator GUI
	 * with optional ?target= param having current Perun GUI URL.
	 *
	 * @param authz destination authorization ("fed", "krb", "cert")
	 * @param target TRUE if use ?target= param in identity consolidator URL / FALSE otherwise
	 * @return URL to identity consolidator
	 */
	public static String getIdentityConsolidatorLink(String authz, boolean target) {

		// wrong authz, return basic
		if (authz == null || authz.isEmpty()) return getIdentityConsolidatorLink(target);

		String value = PerunWebSession.getInstance().getConfiguration().getCustomProperty("getIdentityConsolidatorUrl");

		if (value != null && !value.isEmpty()) {

			if (target) {
				// FIXME - ENCODE QUERY STRING 2 TIMES BECAUSE OF CONSOLIDATOR APP
				value += "?target_url=" + Window.Location.getProtocol() + "//" + Window.Location.getHost() + Window.Location.getPath() +  URL.encodeQueryString(URL.encodeQueryString(Window.Location.getQueryString()));
			}
			return value;

		} else {

			// always use URL of machine, where GUI runs
			String baseUrl = Window.Location.getProtocol() + "//" + Window.Location.getHost();

			String url = baseUrl + "/" + authz + "/ic/";

			if (target) {
				// FIXME - ENCODE QUERY STRING 2 TIMES BECAUSE OF CONSOLIDATOR APP
				url += "?target_url=" + Window.Location.getProtocol() + "//" + Window.Location.getHost() + Window.Location.getPath() + URL.encodeQueryString(URL.encodeQueryString(Window.Location.getQueryString()));
			}

			return url;
		}

	}

	public static ArrayList<String> getVosToSkipCaptchaFor() {

		ArrayList<String> result = new ArrayList<String>();
		String skip = PerunWebSession.getInstance().getConfiguration().getCustomProperty("vosToSkipCaptchaFor");
		for (int i=0; i<skip.split(",").length; i++) {
			result.add(skip.split(",")[i]);
		}
		return result;

	}

	public static ArrayList<String> getNamespacesForPreferredGroupNames() {

		ArrayList<String> result = new ArrayList<String>();
		String skip = PerunWebSession.getInstance().getConfiguration().getCustomProperty("namespacesForPreferredGroupNames");
		for (int i=0; i<skip.split(",").length; i++) {
			result.add(skip.split(",")[i]);
		}
		return result;

	}

	/**
	 * Return URL to Password change GUI for selected namespace
	 *
	 * @param namespace namespace where we want to reset password
	 * @return URL to password reset GUI
	 */
	public static String getPasswordResetLink(String namespace) {

		String value = PerunWebSession.getInstance().getConfiguration().getCustomProperty("getPasswordResetUrl");

		if (value != null && !value.isEmpty()) {

			// PWD-RESET URL IS CONFIGURED AS FIXED
			if (namespace != null && !namespace.isEmpty()) {
				return value+"?login-namespace="+namespace;
			} else {
				return value;
			}

		} else {

			// USE RELATIVE PWD-RESET URL

			String baseUrl = Window.Location.getProtocol()+"//"+ Window.Location.getHost();

			if (!Utils.isDevel()) {

				// VALID URL FOR PRODUCTION

				String rpc = "";
				if (PerunWebSession.getInstance().getRpcServer() != null) {
					rpc = PerunWebSession.getInstance().getRpcServer();
				}

				if (rpc.equalsIgnoreCase("krb")) {
					baseUrl+="/krb";
				} else if (rpc.equalsIgnoreCase("fed")) {
					baseUrl+="/fed";
				} else if (rpc.equalsIgnoreCase("cert")) {
					baseUrl+="/cert";
				} else if (rpc.equalsIgnoreCase("einfra")) {
					baseUrl+="/krb-einfra";
				} else {
					// KRB AS BACKUP - "default"
					baseUrl+="/krb";
				}

				baseUrl+="/pwd-reset/";

			} else {

				// VALID URL FOR DEVEL
				baseUrl+="/PasswordResetKrb.html";

			}

			if (namespace != null && !namespace.isEmpty()) {
				return baseUrl+"?login-namespace="+namespace;
			} else {
				return baseUrl;
			}

		}
	}

	/**
	 * Returns list of supported namespaces names for password change / reset
	 *
	 * @return list of supported namespaces names
	 */
	public static ArrayList<String> getSupportedPasswordNamespaces() {

		ArrayList<String> attributes = new ArrayList<String>();
		if (PerunWebSession.getInstance().getConfiguration() != null) {
			String value = PerunWebSession.getInstance().getConfiguration().getCustomProperty("getSupportedPasswordNamespaces");
			if (value != null && !value.isEmpty()) {
				String[] urns = value.split(",");
				for (int i=0; i<urns.length; i++) {
					attributes.add(urns[i]);
				}
			}
		}
		return attributes;

	}

	/**
	 * Returns public key part of Re-Captcha widget (by GOOGLE)
	 * which is used for anonymous access to application form.
	 *
	 * If public key is not present, return null
	 *
	 * @return Re-Captcha public key
	 */
	public static String getReCaptchaPublicKey() {

		if (PerunWebSession.getInstance().getConfiguration() != null) {
			String value = PerunWebSession.getInstance().getConfiguration().getCustomProperty("getReCaptchaPublicKey");
			if (value != null && !value.isEmpty()) {
				return value;
			}
		}
		return null;

	}

	/**
	 * Returns TRUE if logged to Devel instance of Perun
	 *
	 * @return TRUE if instance of Perun is Devel / FALSE otherwise
	 */
	public static boolean isDevel() {

		if (PerunWebSession.getInstance().getConfiguration() != null) {
			String value = PerunWebSession.getInstance().getConfiguration().getCustomProperty("isDevel");
			if (value != null && !value.isEmpty()) {
				return Boolean.parseBoolean(value);
			}
		}
		return false;

	}

	/**
	 * Returns URL of Perun`s logo
	 *
	 * if not set, use default: "img/logo.png"
	 *
	 * @return URL of Perun`s logo
	 */
	public static String logoUrl() {

		if (PerunWebSession.getInstance().getConfiguration() != null) {
			String value = PerunWebSession.getInstance().getConfiguration().getCustomProperty("logoUrl");
			if (value != null && !value.isEmpty()) {
				return value;
			}
		}
		return "img/logo.png";

	}

	/**
	 * Returns list of Strings representing native language in order "code","native name","english name"
	 *
	 * e,g. "cs","Česky","Czech" for Czech language.
	 *
	 * @return list of strings representing native language
	 */
	public static ArrayList<String> getNativeLanguage() {

		ArrayList<String> list = new ArrayList<String>();
		if (PerunWebSession.getInstance().getConfiguration() != null) {
			String value = PerunWebSession.getInstance().getConfiguration().getCustomProperty("nativeLanguage");
			if (value != null && !value.isEmpty()) {
				String[] parts = value.split(",");
				for (int i=0; i<parts.length; i++) {
					list.add(parts[i]);
				}
			}
		}
		return list;

	}

	/**
	 * Returns address to perun support
	 *
	 * @return support email as string
	 */
	public static String perunReportEmailAddress() {

		if (PerunWebSession.getInstance().getConfiguration() != null) {
			String value = PerunWebSession.getInstance().getConfiguration().getCustomProperty("perunReportEmailAddress");
			if (value != null && !value.isEmpty()) {
				return value;
			}
		}
		return "";

	}

	/**
	 * Returns name of Perun Instance if present in config or "UNKNOWN INSTANCE" if not present.
	 *
	 * @return name of Perun Instance as string
	 */
	public static String perunInstanceName() {

		if (PerunWebSession.getInstance().getConfiguration() != null) {
			String value = PerunWebSession.getInstance().getConfiguration().getCustomProperty("perunInstanceName");
			if (value != null && !value.isEmpty()) {
				return value;
			}
		}
		return "UNKNOWN INSTANCE";

	}

	/**
	 * Returns default RT queue for errors reporting
	 *
	 * @return default RT queue for errors reporting as string
	 */
	public static String defaultRtQueue() {

		if (PerunWebSession.getInstance().getConfiguration() != null) {
			String value = PerunWebSession.getInstance().getConfiguration().getCustomProperty("defaultRtQueue");
			if (value != null && !value.isEmpty()) {
				return value;
			}
		}
		return "";

	}

	/**
	 * Returns default RT queue for errors reporting
	 *
	 * @return default RT queue for errors reporting as string
	 */
	public static String vosManagerMembersGroup() {

		if (PerunWebSession.getInstance().getConfiguration() != null) {
			String value = PerunWebSession.getInstance().getConfiguration().getCustomProperty("vosManagerMembersGroup");
			if (value != null && !value.isEmpty()) {
				return value;
			}
		}
		return "";

	}

	/**
	 * Clear all cookies provided by federation IDP in user's browser.
	 */
	public static void clearFederationCookies() {

		final String SHIBBOLETH_COOKIE_FORMAT = "^_shib.+$";

		// retrieves all the cookies
		Collection<String> cookies = Cookies.getCookieNames();

		// regexp
		RegExp regExp = RegExp.compile(SHIBBOLETH_COOKIE_FORMAT);

		for(String cookie : cookies) {
			// shibboleth cookie?
			MatchResult matcher = regExp.exec(cookie);
			boolean matchFound = (matcher != null); // equivalent to regExp.test(inputStr);
			if(matchFound){
				// remove it
				Cookies.removeCookieNative(cookie, "/");
			}
		}

	}

	public static final native String unAccent(String str) /*-{
		var defaultDiacriticsRemovalMap = [
			{'base':'A', 'letters':'\u0041\u24B6\uFF21\u00C0\u00C1\u00C2\u1EA6\u1EA4\u1EAA\u1EA8\u00C3\u0100\u0102\u1EB0\u1EAE\u1EB4\u1EB2\u0226\u01E0\u00C4\u01DE\u1EA2\u00C5\u01FA\u01CD\u0200\u0202\u1EA0\u1EAC\u1EB6\u1E00\u0104\u023A\u2C6F'},
			{'base':'AA','letters':'\uA732'},
			{'base':'AE','letters':'\u00C6\u01FC\u01E2'},
			{'base':'AO','letters':'\uA734'},
			{'base':'AU','letters':'\uA736'},
			{'base':'AV','letters':'\uA738\uA73A'},
			{'base':'AY','letters':'\uA73C'},
			{'base':'B', 'letters':'\u0042\u24B7\uFF22\u1E02\u1E04\u1E06\u0243\u0182\u0181'},
			{'base':'C', 'letters':'\u0043\u24B8\uFF23\u0106\u0108\u010A\u010C\u00C7\u1E08\u0187\u023B\uA73E'},
			{'base':'D', 'letters':'\u0044\u24B9\uFF24\u1E0A\u010E\u1E0C\u1E10\u1E12\u1E0E\u0110\u018B\u018A\u0189\uA779'},
			{'base':'DZ','letters':'\u01F1\u01C4'},
			{'base':'Dz','letters':'\u01F2\u01C5'},
			{'base':'E', 'letters':'\u0045\u24BA\uFF25\u00C8\u00C9\u00CA\u1EC0\u1EBE\u1EC4\u1EC2\u1EBC\u0112\u1E14\u1E16\u0114\u0116\u00CB\u1EBA\u011A\u0204\u0206\u1EB8\u1EC6\u0228\u1E1C\u0118\u1E18\u1E1A\u0190\u018E'},
			{'base':'F', 'letters':'\u0046\u24BB\uFF26\u1E1E\u0191\uA77B'},
			{'base':'G', 'letters':'\u0047\u24BC\uFF27\u01F4\u011C\u1E20\u011E\u0120\u01E6\u0122\u01E4\u0193\uA7A0\uA77D\uA77E'},
			{'base':'H', 'letters':'\u0048\u24BD\uFF28\u0124\u1E22\u1E26\u021E\u1E24\u1E28\u1E2A\u0126\u2C67\u2C75\uA78D'},
			{'base':'I', 'letters':'\u0049\u24BE\uFF29\u00CC\u00CD\u00CE\u0128\u012A\u012C\u0130\u00CF\u1E2E\u1EC8\u01CF\u0208\u020A\u1ECA\u012E\u1E2C\u0197'},
			{'base':'J', 'letters':'\u004A\u24BF\uFF2A\u0134\u0248'},
			{'base':'K', 'letters':'\u004B\u24C0\uFF2B\u1E30\u01E8\u1E32\u0136\u1E34\u0198\u2C69\uA740\uA742\uA744\uA7A2'},
			{'base':'L', 'letters':'\u004C\u24C1\uFF2C\u013F\u0139\u013D\u1E36\u1E38\u013B\u1E3C\u1E3A\u0141\u023D\u2C62\u2C60\uA748\uA746\uA780'},
			{'base':'LJ','letters':'\u01C7'},
			{'base':'Lj','letters':'\u01C8'},
			{'base':'M', 'letters':'\u004D\u24C2\uFF2D\u1E3E\u1E40\u1E42\u2C6E\u019C'},
			{'base':'N', 'letters':'\u004E\u24C3\uFF2E\u01F8\u0143\u00D1\u1E44\u0147\u1E46\u0145\u1E4A\u1E48\u0220\u019D\uA790\uA7A4'},
			{'base':'NJ','letters':'\u01CA'},
			{'base':'Nj','letters':'\u01CB'},
			{'base':'O', 'letters':'\u004F\u24C4\uFF2F\u00D2\u00D3\u00D4\u1ED2\u1ED0\u1ED6\u1ED4\u00D5\u1E4C\u022C\u1E4E\u014C\u1E50\u1E52\u014E\u022E\u0230\u00D6\u022A\u1ECE\u0150\u01D1\u020C\u020E\u01A0\u1EDC\u1EDA\u1EE0\u1EDE\u1EE2\u1ECC\u1ED8\u01EA\u01EC\u00D8\u01FE\u0186\u019F\uA74A\uA74C'},
			{'base':'OI','letters':'\u01A2'},
			{'base':'OO','letters':'\uA74E'},
			{'base':'OU','letters':'\u0222'},
			{'base':'P', 'letters':'\u0050\u24C5\uFF30\u1E54\u1E56\u01A4\u2C63\uA750\uA752\uA754'},
			{'base':'Q', 'letters':'\u0051\u24C6\uFF31\uA756\uA758\u024A'},
			{'base':'R', 'letters':'\u0052\u24C7\uFF32\u0154\u1E58\u0158\u0210\u0212\u1E5A\u1E5C\u0156\u1E5E\u024C\u2C64\uA75A\uA7A6\uA782'},
			{'base':'S', 'letters':'\u0053\u24C8\uFF33\u1E9E\u015A\u1E64\u015C\u1E60\u0160\u1E66\u1E62\u1E68\u0218\u015E\u2C7E\uA7A8\uA784'},
			{'base':'T', 'letters':'\u0054\u24C9\uFF34\u1E6A\u0164\u1E6C\u021A\u0162\u1E70\u1E6E\u0166\u01AC\u01AE\u023E\uA786'},
			{'base':'TZ','letters':'\uA728'},
			{'base':'U', 'letters':'\u0055\u24CA\uFF35\u00D9\u00DA\u00DB\u0168\u1E78\u016A\u1E7A\u016C\u00DC\u01DB\u01D7\u01D5\u01D9\u1EE6\u016E\u0170\u01D3\u0214\u0216\u01AF\u1EEA\u1EE8\u1EEE\u1EEC\u1EF0\u1EE4\u1E72\u0172\u1E76\u1E74\u0244'},
			{'base':'V', 'letters':'\u0056\u24CB\uFF36\u1E7C\u1E7E\u01B2\uA75E\u0245'},
			{'base':'VY','letters':'\uA760'},
			{'base':'W', 'letters':'\u0057\u24CC\uFF37\u1E80\u1E82\u0174\u1E86\u1E84\u1E88\u2C72'},
			{'base':'X', 'letters':'\u0058\u24CD\uFF38\u1E8A\u1E8C'},
			{'base':'Y', 'letters':'\u0059\u24CE\uFF39\u1EF2\u00DD\u0176\u1EF8\u0232\u1E8E\u0178\u1EF6\u1EF4\u01B3\u024E\u1EFE'},
			{'base':'Z', 'letters':'\u005A\u24CF\uFF3A\u0179\u1E90\u017B\u017D\u1E92\u1E94\u01B5\u0224\u2C7F\u2C6B\uA762'},
			{'base':'a', 'letters':'\u0061\u24D0\uFF41\u1E9A\u00E0\u00E1\u00E2\u1EA7\u1EA5\u1EAB\u1EA9\u00E3\u0101\u0103\u1EB1\u1EAF\u1EB5\u1EB3\u0227\u01E1\u00E4\u01DF\u1EA3\u00E5\u01FB\u01CE\u0201\u0203\u1EA1\u1EAD\u1EB7\u1E01\u0105\u2C65\u0250'},
			{'base':'aa','letters':'\uA733'},
			{'base':'ae','letters':'\u00E6\u01FD\u01E3'},
			{'base':'ao','letters':'\uA735'},
			{'base':'au','letters':'\uA737'},
			{'base':'av','letters':'\uA739\uA73B'},
			{'base':'ay','letters':'\uA73D'},
			{'base':'b', 'letters':'\u0062\u24D1\uFF42\u1E03\u1E05\u1E07\u0180\u0183\u0253'},
			{'base':'c', 'letters':'\u0063\u24D2\uFF43\u0107\u0109\u010B\u010D\u00E7\u1E09\u0188\u023C\uA73F\u2184'},
			{'base':'d', 'letters':'\u0064\u24D3\uFF44\u1E0B\u010F\u1E0D\u1E11\u1E13\u1E0F\u0111\u018C\u0256\u0257\uA77A'},
			{'base':'dz','letters':'\u01F3\u01C6'},
			{'base':'e', 'letters':'\u0065\u24D4\uFF45\u00E8\u00E9\u00EA\u1EC1\u1EBF\u1EC5\u1EC3\u1EBD\u0113\u1E15\u1E17\u0115\u0117\u00EB\u1EBB\u011B\u0205\u0207\u1EB9\u1EC7\u0229\u1E1D\u0119\u1E19\u1E1B\u0247\u025B\u01DD'},
			{'base':'f', 'letters':'\u0066\u24D5\uFF46\u1E1F\u0192\uA77C'},
			{'base':'g', 'letters':'\u0067\u24D6\uFF47\u01F5\u011D\u1E21\u011F\u0121\u01E7\u0123\u01E5\u0260\uA7A1\u1D79\uA77F'},
			{'base':'h', 'letters':'\u0068\u24D7\uFF48\u0125\u1E23\u1E27\u021F\u1E25\u1E29\u1E2B\u1E96\u0127\u2C68\u2C76\u0265'},
			{'base':'hv','letters':'\u0195'},
			{'base':'i', 'letters':'\u0069\u24D8\uFF49\u00EC\u00ED\u00EE\u0129\u012B\u012D\u00EF\u1E2F\u1EC9\u01D0\u0209\u020B\u1ECB\u012F\u1E2D\u0268\u0131'},
			{'base':'j', 'letters':'\u006A\u24D9\uFF4A\u0135\u01F0\u0249'},
			{'base':'k', 'letters':'\u006B\u24DA\uFF4B\u1E31\u01E9\u1E33\u0137\u1E35\u0199\u2C6A\uA741\uA743\uA745\uA7A3'},
			{'base':'l', 'letters':'\u006C\u24DB\uFF4C\u0140\u013A\u013E\u1E37\u1E39\u013C\u1E3D\u1E3B\u017F\u0142\u019A\u026B\u2C61\uA749\uA781\uA747'},
			{'base':'lj','letters':'\u01C9'},
			{'base':'m', 'letters':'\u006D\u24DC\uFF4D\u1E3F\u1E41\u1E43\u0271\u026F'},
			{'base':'n', 'letters':'\u006E\u24DD\uFF4E\u01F9\u0144\u00F1\u1E45\u0148\u1E47\u0146\u1E4B\u1E49\u019E\u0272\u0149\uA791\uA7A5'},
			{'base':'nj','letters':'\u01CC'},
			{'base':'o', 'letters':'\u006F\u24DE\uFF4F\u00F2\u00F3\u00F4\u1ED3\u1ED1\u1ED7\u1ED5\u00F5\u1E4D\u022D\u1E4F\u014D\u1E51\u1E53\u014F\u022F\u0231\u00F6\u022B\u1ECF\u0151\u01D2\u020D\u020F\u01A1\u1EDD\u1EDB\u1EE1\u1EDF\u1EE3\u1ECD\u1ED9\u01EB\u01ED\u00F8\u01FF\u0254\uA74B\uA74D\u0275'},
			{'base':'oi','letters':'\u01A3'},
			{'base':'ou','letters':'\u0223'},
			{'base':'oo','letters':'\uA74F'},
			{'base':'p','letters':'\u0070\u24DF\uFF50\u1E55\u1E57\u01A5\u1D7D\uA751\uA753\uA755'},
			{'base':'q','letters':'\u0071\u24E0\uFF51\u024B\uA757\uA759'},
			{'base':'r','letters':'\u0072\u24E1\uFF52\u0155\u1E59\u0159\u0211\u0213\u1E5B\u1E5D\u0157\u1E5F\u024D\u027D\uA75B\uA7A7\uA783'},
			{'base':'s','letters':'\u0073\u24E2\uFF53\u00DF\u015B\u1E65\u015D\u1E61\u0161\u1E67\u1E63\u1E69\u0219\u015F\u023F\uA7A9\uA785\u1E9B'},
			{'base':'t','letters':'\u0074\u24E3\uFF54\u1E6B\u1E97\u0165\u1E6D\u021B\u0163\u1E71\u1E6F\u0167\u01AD\u0288\u2C66\uA787'},
			{'base':'tz','letters':'\uA729'},
			{'base':'u','letters': '\u0075\u24E4\uFF55\u00F9\u00FA\u00FB\u0169\u1E79\u016B\u1E7B\u016D\u00FC\u01DC\u01D8\u01D6\u01DA\u1EE7\u016F\u0171\u01D4\u0215\u0217\u01B0\u1EEB\u1EE9\u1EEF\u1EED\u1EF1\u1EE5\u1E73\u0173\u1E77\u1E75\u0289'},
			{'base':'v','letters':'\u0076\u24E5\uFF56\u1E7D\u1E7F\u028B\uA75F\u028C'},
			{'base':'vy','letters':'\uA761'},
			{'base':'w','letters':'\u0077\u24E6\uFF57\u1E81\u1E83\u0175\u1E87\u1E85\u1E98\u1E89\u2C73'},
			{'base':'x','letters':'\u0078\u24E7\uFF58\u1E8B\u1E8D'},
			{'base':'y','letters':'\u0079\u24E8\uFF59\u1EF3\u00FD\u0177\u1EF9\u0233\u1E8F\u00FF\u1EF7\u1E99\u1EF5\u01B4\u024F\u1EFF'},
			{'base':'z','letters':'\u007A\u24E9\uFF5A\u017A\u1E91\u017C\u017E\u1E93\u1E95\u01B6\u0225\u0240\u2C6C\uA763'}
		];
		var diacriticsMap = {};
		for (var i=0; i < defaultDiacriticsRemovalMap.length; i++){
			var letters = defaultDiacriticsRemovalMap[i].letters.split("");
			for (var j=0; j < letters.length ; j++){
				diacriticsMap[letters[j]] = defaultDiacriticsRemovalMap[i].base;
			}
		}
		var letters = str.split("");
		var newStr = "";
		for(var i=0; i< letters.length; i++) {
			newStr += letters[i] in diacriticsMap ? diacriticsMap[letters[i]] : letters[i];
		}
		return newStr;
	}-*/;

	public static ArrayList<String> getTimezones() {

	/*
	 * Database of timezones from http://en.wikipedia.org/wiki/List_of_tz_database_time_zones.
	 */
		final ArrayList<String> timezones = new ArrayList<String>(Arrays.asList(
				"Africa/Abidjan",
				"Africa/Accra",
				"Africa/Addis_Ababa",
				"Africa/Algiers",
				"Africa/Asmara",
				"Africa/Asmera",
				"Africa/Bamako",
				"Africa/Bangui",
				"Africa/Banjul",
				"Africa/Bissau",
				"Africa/Blantyre",
				"Africa/Brazzaville",
				"Africa/Bujumbura",
				"Africa/Cairo",
				"Africa/Casablanca",
				"Africa/Ceuta",
				"Africa/Conakry",
				"Africa/Dakar",
				"Africa/Dar_es_Salaam",
				"Africa/Djibouti",
				"Africa/Douala",
				"Africa/El_Aaiun",
				"Africa/Freetown",
				"Africa/Gaborone",
				"Africa/Harare",
				"Africa/Johannesburg",
				"Africa/Juba",
				"Africa/Kampala",
				"Africa/Khartoum",
				"Africa/Kigali",
				"Africa/Kinshasa",
				"Africa/Lagos",
				"Africa/Libreville",
				"Africa/Lome",
				"Africa/Luanda",
				"Africa/Lubumbashi",
				"Africa/Lusaka",
				"Africa/Malabo",
				"Africa/Maputo",
				"Africa/Maseru",
				"Africa/Mbabane",
				"Africa/Mogadishu",
				"Africa/Monrovia",
				"Africa/Nairobi",
				"Africa/Ndjamena",
				"Africa/Niamey",
				"Africa/Nouakchott",
				"Africa/Ouagadougou",
				"Africa/Porto-Novo",
				"Africa/Sao_Tome",
				"Africa/Timbuktu",
				"Africa/Tripoli",
				"Africa/Tunis",
				"Africa/Windhoek",
				"AKST9AKDT",
				"America/Adak",
				"America/Anchorage",
				"America/Anguilla",
				"America/Antigua",
				"America/Araguaina",
				"America/Argentina/Buenos_Aires",
				"America/Argentina/Catamarca",
				"America/Argentina/ComodRivadavia",
				"America/Argentina/Cordoba",
				"America/Argentina/Jujuy",
				"America/Argentina/La_Rioja",
				"America/Argentina/Mendoza",
				"America/Argentina/Rio_Gallegos",
				"America/Argentina/Salta",
				"America/Argentina/San_Juan",
				"America/Argentina/San_Luis",
				"America/Argentina/Tucuman",
				"America/Argentina/Ushuaia",
				"America/Aruba",
				"America/Asuncion",
				"America/Atikokan",
				"America/Atka",
				"America/Bahia",
				"America/Bahia_Banderas",
				"America/Barbados",
				"America/Belem",
				"America/Belize",
				"America/Blanc-Sablon",
				"America/Boa_Vista",
				"America/Bogota",
				"America/Boise",
				"America/Buenos_Aires",
				"America/Cambridge_Bay",
				"America/Campo_Grande",
				"America/Cancun",
				"America/Caracas",
				"America/Catamarca",
				"America/Cayenne",
				"America/Cayman",
				"America/Chicago",
				"America/Chihuahua",
				"America/Coral_Harbour",
				"America/Cordoba",
				"America/Costa_Rica",
				"America/Creston",
				"America/Cuiaba",
				"America/Curacao",
				"America/Danmarkshavn",
				"America/Dawson",
				"America/Dawson_Creek",
				"America/Denver",
				"America/Detroit",
				"America/Dominica",
				"America/Edmonton",
				"America/Eirunepe",
				"America/El_Salvador",
				"America/Ensenada",
				"America/Fort_Wayne",
				"America/Fortaleza",
				"America/Glace_Bay",
				"America/Godthab",
				"America/Goose_Bay",
				"America/Grand_Turk",
				"America/Grenada",
				"America/Guadeloupe",
				"America/Guatemala",
				"America/Guayaquil",
				"America/Guyana",
				"America/Halifax",
				"America/Havana",
				"America/Hermosillo",
				"America/Indiana/Indianapolis",
				"America/Indiana/Knox",
				"America/Indiana/Marengo",
				"America/Indiana/Petersburg",
				"America/Indiana/Tell_City",
				"America/Indiana/Vevay",
				"America/Indiana/Vincennes",
				"America/Indiana/Winamac",
				"America/Indianapolis",
				"America/Inuvik",
				"America/Iqaluit",
				"America/Jamaica",
				"America/Jujuy",
				"America/Juneau",
				"America/Kentucky/Louisville",
				"America/Kentucky/Monticello",
				"America/Knox_IN",
				"America/Kralendijk",
				"America/La_Paz",
				"America/Lima",
				"America/Los_Angeles",
				"America/Louisville",
				"America/Lower_Princes",
				"America/Maceio",
				"America/Managua",
				"America/Manaus",
				"America/Marigot",
				"America/Martinique",
				"America/Matamoros",
				"America/Mazatlan",
				"America/Mendoza",
				"America/Menominee",
				"America/Merida",
				"America/Metlakatla",
				"America/Mexico_City",
				"America/Miquelon",
				"America/Moncton",
				"America/Monterrey",
				"America/Montevideo",
				"America/Montreal",
				"America/Montserrat",
				"America/Nassau",
				"America/New_York",
				"America/Nipigon",
				"America/Nome",
				"America/Noronha",
				"America/North_Dakota/Beulah",
				"America/North_Dakota/Center",
				"America/North_Dakota/New_Salem",
				"America/Ojinaga",
				"America/Panama",
				"America/Pangnirtung",
				"America/Paramaribo",
				"America/Phoenix",
				"America/Port_of_Spain",
				"America/Port-au-Prince",
				"America/Porto_Acre",
				"America/Porto_Velho",
				"America/Puerto_Rico",
				"America/Rainy_River",
				"America/Rankin_Inlet",
				"America/Recife",
				"America/Regina",
				"America/Resolute",
				"America/Rio_Branco",
				"America/Rosario",
				"America/Santa_Isabel",
				"America/Santarem",
				"America/Santiago",
				"America/Santo_Domingo",
				"America/Sao_Paulo",
				"America/Scoresbysund",
				"America/Shiprock",
				"America/Sitka",
				"America/St_Barthelemy",
				"America/St_Johns",
				"America/St_Kitts",
				"America/St_Lucia",
				"America/St_Thomas",
				"America/St_Vincent",
				"America/Swift_Current",
				"America/Tegucigalpa",
				"America/Thule",
				"America/Thunder_Bay",
				"America/Tijuana",
				"America/Toronto",
				"America/Tortola",
				"America/Vancouver",
				"America/Virgin",
				"America/Whitehorse",
				"America/Winnipeg",
				"America/Yakutat",
				"America/Yellowknife",
				"Antarctica/Casey",
				"Antarctica/Davis",
				"Antarctica/DumontDUrville",
				"Antarctica/Macquarie",
				"Antarctica/Mawson",
				"Antarctica/McMurdo",
				"Antarctica/Palmer",
				"Antarctica/Rothera",
				"Antarctica/South_Pole",
				"Antarctica/Syowa",
				"Antarctica/Vostok",
				"Arctic/Longyearbyen",
				"Asia/Aden",
				"Asia/Almaty",
				"Asia/Amman",
				"Asia/Anadyr",
				"Asia/Aqtau",
				"Asia/Aqtobe",
				"Asia/Ashgabat",
				"Asia/Ashkhabad",
				"Asia/Baghdad",
				"Asia/Bahrain",
				"Asia/Baku",
				"Asia/Bangkok",
				"Asia/Beirut",
				"Asia/Bishkek",
				"Asia/Brunei",
				"Asia/Calcutta",
				"Asia/Choibalsan",
				"Asia/Chongqing",
				"Asia/Chungking",
				"Asia/Colombo",
				"Asia/Dacca",
				"Asia/Damascus",
				"Asia/Dhaka",
				"Asia/Dili",
				"Asia/Dubai",
				"Asia/Dushanbe",
				"Asia/Gaza",
				"Asia/Harbin",
				"Asia/Hebron",
				"Asia/Ho_Chi_Minh",
				"Asia/Hong_Kong",
				"Asia/Hovd",
				"Asia/Irkutsk",
				"Asia/Istanbul",
				"Asia/Jakarta",
				"Asia/Jayapura",
				"Asia/Jerusalem",
				"Asia/Kabul",
				"Asia/Kamchatka",
				"Asia/Karachi",
				"Asia/Kashgar",
				"Asia/Kathmandu",
				"Asia/Katmandu",
				"Asia/Kolkata",
				"Asia/Krasnoyarsk",
				"Asia/Kuala_Lumpur",
				"Asia/Kuching",
				"Asia/Kuwait",
				"Asia/Macao",
				"Asia/Macau",
				"Asia/Magadan",
				"Asia/Makassar",
				"Asia/Manila",
				"Asia/Muscat",
				"Asia/Nicosia",
				"Asia/Novokuznetsk",
				"Asia/Novosibirsk",
				"Asia/Omsk",
				"Asia/Oral",
				"Asia/Phnom_Penh",
				"Asia/Pontianak",
				"Asia/Pyongyang",
				"Asia/Qatar",
				"Asia/Qyzylorda",
				"Asia/Rangoon",
				"Asia/Riyadh",
				"Asia/Saigon",
				"Asia/Sakhalin",
				"Asia/Samarkand",
				"Asia/Seoul",
				"Asia/Shanghai",
				"Asia/Singapore",
				"Asia/Taipei",
				"Asia/Tashkent",
				"Asia/Tbilisi",
				"Asia/Tehran",
				"Asia/Tel_Aviv",
				"Asia/Thimbu",
				"Asia/Thimphu",
				"Asia/Tokyo",
				"Asia/Ujung_Pandang",
				"Asia/Ulaanbaatar",
				"Asia/Ulan_Bator",
				"Asia/Urumqi",
				"Asia/Vientiane",
				"Asia/Vladivostok",
				"Asia/Yakutsk",
				"Asia/Yekaterinburg",
				"Asia/Yerevan",
				"Atlantic/Azores",
				"Atlantic/Bermuda",
				"Atlantic/Canary",
				"Atlantic/Cape_Verde",
				"Atlantic/Faeroe",
				"Atlantic/Faroe",
				"Atlantic/Jan_Mayen",
				"Atlantic/Madeira",
				"Atlantic/Reykjavik",
				"Atlantic/South_Georgia",
				"Atlantic/St_Helena",
				"Atlantic/Stanley",
				"Australia/ACT",
				"Australia/Adelaide",
				"Australia/Brisbane",
				"Australia/Broken_Hill",
				"Australia/Canberra",
				"Australia/Currie",
				"Australia/Darwin",
				"Australia/Eucla",
				"Australia/Hobart",
				"Australia/LHI",
				"Australia/Lindeman",
				"Australia/Lord_Howe",
				"Australia/Melbourne",
				"Australia/North",
				"Australia/NSW",
				"Australia/Perth",
				"Australia/Queensland",
				"Australia/South",
				"Australia/Sydney",
				"Australia/Tasmania",
				"Australia/Victoria",
				"Australia/West",
				"Australia/Yancowinna",
				"Brazil/Acre",
				"Brazil/DeNoronha",
				"Brazil/East",
				"Brazil/West",
				"Canada/Atlantic",
				"Canada/Central",
				"Canada/Eastern",
				"Canada/East-Saskatchewan",
				"Canada/Mountain",
				"Canada/Newfoundland",
				"Canada/Pacific",
				"Canada/Saskatchewan",
				"Canada/Yukon",
				"CET",
				"Chile/Continental",
				"Chile/EasterIsland",
				"CST6CDT",
				"Cuba",
				"EET",
				"Egypt",
				"Eire",
				"EST",
				"EST5EDT",
				"Etc/GMT",
				"Etc/GMT+0",
				"Etc/UCT",
				"Etc/Universal",
				"Etc/UTC",
				"Etc/Zulu",
				"Europe/Amsterdam",
				"Europe/Andorra",
				"Europe/Athens",
				"Europe/Belfast",
				"Europe/Belgrade",
				"Europe/Berlin",
				"Europe/Bratislava",
				"Europe/Brussels",
				"Europe/Bucharest",
				"Europe/Budapest",
				"Europe/Chisinau",
				"Europe/Copenhagen",
				"Europe/Dublin",
				"Europe/Gibraltar",
				"Europe/Guernsey",
				"Europe/Helsinki",
				"Europe/Isle_of_Man",
				"Europe/Istanbul",
				"Europe/Jersey",
				"Europe/Kaliningrad",
				"Europe/Kiev",
				"Europe/Lisbon",
				"Europe/Ljubljana",
				"Europe/London",
				"Europe/Luxembourg",
				"Europe/Madrid",
				"Europe/Malta",
				"Europe/Mariehamn",
				"Europe/Minsk",
				"Europe/Monaco",
				"Europe/Moscow",
				"Europe/Nicosia",
				"Europe/Oslo",
				"Europe/Paris",
				"Europe/Podgorica",
				"Europe/Prague",
				"Europe/Riga",
				"Europe/Rome",
				"Europe/Samara",
				"Europe/San_Marino",
				"Europe/Sarajevo",
				"Europe/Simferopol",
				"Europe/Skopje",
				"Europe/Sofia",
				"Europe/Stockholm",
				"Europe/Tallinn",
				"Europe/Tirane",
				"Europe/Tiraspol",
				"Europe/Uzhgorod",
				"Europe/Vaduz",
				"Europe/Vatican",
				"Europe/Vienna",
				"Europe/Vilnius",
				"Europe/Volgograd",
				"Europe/Warsaw",
				"Europe/Zagreb",
				"Europe/Zaporozhye",
				"Europe/Zurich",
				"GB",
				"GB-Eire",
				"GMT",
				"GMT+0",
				"GMT0",
				"GMT-0",
				"Greenwich",
				"Hongkong",
				"HST",
				"Iceland",
				"Indian/Antananarivo",
				"Indian/Chagos",
				"Indian/Christmas",
				"Indian/Cocos",
				"Indian/Comoro",
				"Indian/Kerguelen",
				"Indian/Mahe",
				"Indian/Maldives",
				"Indian/Mauritius",
				"Indian/Mayotte",
				"Indian/Reunion",
				"Iran",
				"Israel",
				"Jamaica",
				"Japan",
				"JST-9",
				"Kwajalein",
				"Libya",
				"MET",
				"Mexico/BajaNorte",
				"Mexico/BajaSur",
				"Mexico/General",
				"MST",
				"MST7MDT",
				"Navajo",
				"NZ",
				"NZ-CHAT",
				"Pacific/Apia",
				"Pacific/Auckland",
				"Pacific/Chatham",
				"Pacific/Chuuk",
				"Pacific/Easter",
				"Pacific/Efate",
				"Pacific/Enderbury",
				"Pacific/Fakaofo",
				"Pacific/Fiji",
				"Pacific/Funafuti",
				"Pacific/Galapagos",
				"Pacific/Gambier",
				"Pacific/Guadalcanal",
				"Pacific/Guam",
				"Pacific/Honolulu",
				"Pacific/Johnston",
				"Pacific/Kiritimati",
				"Pacific/Kosrae",
				"Pacific/Kwajalein",
				"Pacific/Majuro",
				"Pacific/Marquesas",
				"Pacific/Midway",
				"Pacific/Nauru",
				"Pacific/Niue",
				"Pacific/Norfolk",
				"Pacific/Noumea",
				"Pacific/Pago_Pago",
				"Pacific/Palau",
				"Pacific/Pitcairn",
				"Pacific/Pohnpei",
				"Pacific/Ponape",
				"Pacific/Port_Moresby",
				"Pacific/Rarotonga",
				"Pacific/Saipan",
				"Pacific/Samoa",
				"Pacific/Tahiti",
				"Pacific/Tarawa",
				"Pacific/Tongatapu",
				"Pacific/Truk",
				"Pacific/Wake",
				"Pacific/Wallis",
				"Pacific/Yap",
				"Poland",
				"Portugal",
				"PRC",
				"PST8PDT",
				"ROC",
				"ROK",
				"Singapore",
				"Turkey",
				"UCT",
				"Universal",
				"US/Alaska",
				"US/Aleutian",
				"US/Arizona",
				"US/Central",
				"US/Eastern",
				"US/East-Indiana",
				"US/Hawaii",
				"US/Indiana-Starke",
				"US/Michigan",
				"US/Mountain",
				"US/Pacific",
				"US/Pacific-New",
				"US/Samoa",
				"UTC",
				"WET",
				"W-SU",
				"Zulu"
		));

		return timezones;

	}

	/**
	 * If passed string is DN of certificate(recognized by "/CN=") then returns only CN part with unescaped chars.
	 * If passed string is not DN of certificate, original string is returned.
	 *
	 * @param toConvert
	 * @return
	 */
	static public String convertCertCN(String toConvert) {

		if (toConvert.contains("/CN=")) {
			String[] splitted = toConvert.split("/");
			for (String s : splitted) {
				if (s.startsWith("CN=")) {
					return unescapeDN(s.substring(3));
				}
			}
		}
		return toConvert;

	}

	static public final native String unescapeDN(String string) /*-{

		return decodeURIComponent(string.replace(/\\x/g, '%'))

	}-*/;

	public static String translateIdp(String name) {

		HashMap<String, String> orgs = new HashMap<String, String>();
		orgs.put("https://idp.upce.cz/idp/shibboleth", "University in Pardubice");
		orgs.put("https://idp.slu.cz/idp/shibboleth", "University in Opava");
		orgs.put("https://login.feld.cvut.cz/idp/shibboleth", "Faculty of Electrical Engineering, Czech Technical University In Prague");
		orgs.put("https://www.vutbr.cz/SSO/saml2/idp", "Brno University of Technology");
		orgs.put("https://shibboleth.nkp.cz/idp/shibboleth", "The National Library of the Czech Republic");
		orgs.put("https://idp2.civ.cvut.cz/idp/shibboleth", "Czech Technical University In Prague");
		orgs.put("https://shibbo.tul.cz/idp/shibboleth", "Technical University of Liberec");
		orgs.put("https://idp.mendelu.cz/idp/shibboleth", "Mendel University in Brno");
		orgs.put("https://cas.cuni.cz/idp/shibboleth", "Charles University in Prague");
		orgs.put("https://wsso.vscht.cz/idp/shibboleth", "Institute of Chemical Technology Prague");
		orgs.put("https://idp.vsb.cz/idp/shibboleth", "VSB – Technical University of Ostrava");
		orgs.put("https://whoami.cesnet.cz/idp/shibboleth", "CESNET");
		orgs.put("https://helium.jcu.cz/idp/shibboleth", "University of South Bohemia");
		orgs.put("https://idp.ujep.cz/idp/shibboleth", "Jan Evangelista Purkyne University in Usti nad Labem");
		orgs.put("https://idp.amu.cz/idp/shibboleth", "Academy of Performing Arts in Prague");
		orgs.put("https://idp.lib.cas.cz/idp/shibboleth", "Academy of Sciences Library");
		orgs.put("https://shibboleth.mzk.cz/simplesaml/metadata.xml", "Moravian  Library");
		orgs.put("https://idp2.ics.muni.cz/idp/shibboleth", "Masaryk University");
		orgs.put("https://idp.upol.cz/idp/shibboleth", "Palacky University, Olomouc");
		orgs.put("https://idp.fnplzen.cz/idp/shibboleth", "FN Plzen");
		orgs.put("https://id.vse.cz/idp/shibboleth", "University of Economics, Prague");
		orgs.put("https://shib.zcu.cz/idp/shibboleth", "University of West Bohemia");
		orgs.put("https://idptoo.osu.cz/simplesaml/saml2/idp/metadata.php", "University of Ostrava");
		orgs.put("https://login.ics.muni.cz/idp/shibboleth", "MetaCentrum");
		orgs.put("https://idp.hostel.eduid.cz/idp/shibboleth", "eduID.cz Hostel");
		orgs.put("https://shibboleth.techlib.cz/idp/shibboleth", "National Library of Technology");
		orgs.put("https://eduid.jamu.cz/idp/shibboleth", "Janacek Academy of Music and Performing Arts in Brno");
		orgs.put("https://marisa.uochb.cas.cz/simplesaml/saml2/idp/metadata.php", "Institute of Organic Chemistry and Biochemistry AS CR");
		orgs.put("https://shibboleth.utb.cz/idp/shibboleth", "Tomas Bata University in Zlin");
		orgs.put("https://engine.elixir-czech.org/authentication/idp/metadata", "Elixir Europe");
		orgs.put("https://login.elixir-czech.org/idp", "Elixir Czech");
		orgs.put("https://mojeid.cz/saml/idp.xml", "MojeID");
		orgs.put("https://www.egi.eu/idp/shibboleth", "EGI SSO");

		orgs.put("@google.extidp.cesnet.cz", "Google");
		orgs.put("@facebook.extidp.cesnet.cz", "Facebook");
		orgs.put("@mojeid.extidp.cesnet.cz", "MojeID");
		orgs.put("@linkedin.extidp.cesnet.cz", "LinkedIn");
		orgs.put("@twitter.extidp.cesnet.cz", "Twitter");
		orgs.put("@seznam.extidp.cesnet.cz", "Seznam");
		orgs.put("@elixir-europe.org", "Elixir Europe");
		orgs.put("@github.extidp.cesnet.cz", "GitHub");
		orgs.put("@orcid.extidp.cesnet.cz", "OrcID");

		if (orgs.get(name) != null) {
			return orgs.get(name);
		} else {
			return name;
		}

	}

}
