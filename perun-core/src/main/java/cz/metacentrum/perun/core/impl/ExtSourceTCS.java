package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceUnsupportedOperationException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidCertificateException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import cz.metacentrum.perun.core.implApi.ExtSourceApi;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.io.pem.PemObject;
import java.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class ExtSource for TCS
 * Synchronize certificates from defined address to Perun for existing users (skip not existing users)
 *
 * @author Michal Stava stavamichal@gmail.com
 */
public class ExtSourceTCS extends ExtSource implements ExtSourceApi {

	private static PerunBlImpl perunBl;

	private static final String attrLoginMUName = "urn:perun:user:attribute-def:def:login-namespace:mu";
	private static final String attrUserCertificates = "urn:perun:user:attribute-def:def:userCertificates";
	private final static Pattern loginPattern = Pattern.compile("^.*\\s([0-9]+)$");
	private final static Pattern wrongLoginPattern = Pattern.compile("^.*\\s[0-9]+\\s[0-9]+$");

	// filled by spring (perun-core.xml)
	public static PerunBlImpl setPerunBlImpl(PerunBlImpl perun) {
		perunBl = perun;
		return perun;
	}

	@Override
	public List<Map<String,String>> findSubjectsLogins(String searchString) throws ExtSourceUnsupportedOperationException {
		return findSubjectsLogins(searchString, 0);
	}

	@Override
	public List<Map<String, String>> findSubjects(String searchString) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException();
	}

	@Override
	public List<Map<String, String>> findSubjects(String searchString, int maxResults) throws InternalErrorException, ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException();
	}

	@Override
	public List<Map<String, String>> findSubjectsLogins(String searchString, int maxResults) throws ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException();
	}

	@Override
	public List<Map<String, String>> getSubjectGroups(Map<String, String> attributes) throws ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException();
	}

	@Override
	public List<Map<String, String>> getUsersSubjects() throws ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException();
	}

	@Override
	public Map<String, String> getSubjectByLogin(String login) throws ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException();
	}

	@Override
	public void close() throws ExtSourceUnsupportedOperationException {
		throw new ExtSourceUnsupportedOperationException();
	}

	@Override
	public List<Map<String, String>> getGroupSubjects(Map<String, String> attributes) throws InternalErrorException {
		//get pem file from url and parse it
		String url = attributes.get(GroupsManager.GROUPMEMBERSQUERY_ATTRNAME);

		//Prepare structure of all valid certificates mapped by login
		Map<String, Pair<X509CertificateHolder, String>> validCertificatesForLogin = prepareStructureOfValidCertificates(url);

		List<Map<String, String>> subjects = new ArrayList<>();
		//get subjects from map of valid certificates (every certificate is for 1 person in Perun)
		for(String login: validCertificatesForLogin.keySet()) {
			Map<String, String> subject = new HashMap<>();
			subject.put("login", login);

			//certificate need to be saved as map so we need to parse it correctly
			Map<String, String> certificate = new LinkedHashMap<>();
			certificate.put(validCertificatesForLogin.get(login).getLeft().getSubject().toString(), validCertificatesForLogin.get(login).getRight());
			subject.put(attrUserCertificates, BeansUtils.attributeValueToString(certificate, LinkedHashMap.class.getName()));

			//map on existing extSource with MU login
			subject.put("additionalues_1", "https://idp2.ics.muni.cz/idp/shibboleth|cz.metacentrum.perun.core.impl.ExtSourceIdp|" + login + "@muni.cz|2");
			subjects.add(subject);
		}

		return subjects;
	}

	//Private methods

	/**
	 * Create perunSession for ExtSourceTCS
	 *
	 * @return perun session for extSource TCS
	 * @throws InternalErrorException if there is any problem to create perun session
	 */
	private PerunSession getSession() throws InternalErrorException {
		final PerunPrincipal pp = new PerunPrincipal("ExtSourceTCS", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
		try {
			return perunBl.getPerunSession(pp, new PerunClient());
		} catch (InternalErrorException e) {
			throw new InternalErrorException("Failed to get session for ExtSourceTCS.", e);
		}
	}

	/**
	 * Read all logins for specific attr name - for example "urn:perun:user:attribute-def:def:login-namespace:mu" for MU namespace
	 *
	 * @param loginAttrName name of attribute from which logins should be get
	 * @return list of logins from specific attribute
	 * @throws InternalErrorException if attribute of specific login not exists or assignemnt of such attribute is wrong
	 */
	private List<String> getLoginsFromPerun(String loginAttrName) throws InternalErrorException {
		PerunSession sess = this.getSession();
		List<String> allLogins = new ArrayList<>();
		try {
			AttributeDefinition attrDefLoginInMu = perunBl.getAttributesManagerBl().getAttributeDefinition(sess, loginAttrName);
			perunBl.getAttributesManagerBl().getAllValues(sess, attrDefLoginInMu).forEach( value -> allLogins.add((String) value));
		} catch (AttributeNotExistsException | WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		}
		return allLogins;
	}

	/**
	 * For every certificate from pem file (get from url address) parse only valid certificates.
	 * Valid certificate means:
	 *  - it is not expired
	 *  - login of the owner can be parsed from certificate subject
	 *  - every valid owner already exists in perun as User
	 *  - if more than 1 certificate exists for same owner, choose the one with later expiration
	 *
	 * @param url address of url to parse pem file from
	 * @return map of logins (in key) to pair of parsed certificate in the left part and certificate in base64 in the right part
	 * @throws InternalErrorException If there is any IO problem with parsing and processing the certificate
	 */
	private Map<String, Pair<X509CertificateHolder, String>> prepareStructureOfValidCertificates(String url) throws InternalErrorException {
		Map<String, Pair<X509CertificateHolder, String>> validCertificatesForLogin = new HashMap<>();

		//prepare all already known logins from Perun
		List<String> allLogins = getLoginsFromPerun(attrLoginMUName);

		HttpURLConnection con = null;
		try {
			//prepare html connection
			URL myURL = new URL(url);
			con = (HttpURLConnection) myURL.openConnection();
			con.setDoOutput(true);
			con.setRequestProperty("Content-Type", "multipart/form-data;");

			//get stream of data from the connection
			try (InputStream is = con.getInputStream()) {
				//parse pem file
				PEMParser pemParser = new PEMParser(new InputStreamReader(is));

				//read all certificates from pem file
				PemObject pemObject = pemParser.readPemObject();
				while (pemObject != null) {
					//get certificate holder from pemObject
					X509CertificateHolder certificateHolder = new X509CertificateHolder(pemObject.getContent());

					String login;
					try {
						login = checkCertAndGetLogin(certificateHolder, allLogins);
					} catch (InvalidCertificateException e) {
						//read next pemObject and skip this one
						pemObject = pemParser.readPemObject();
						continue;
					}

					if (validCertificatesForLogin.get(login) == null) {
						//this certificate is new, saved it
						String extractedCert = exportBase64Certificate(pemObject);
						//For this user, add his valid certificate to the map
						validCertificatesForLogin.put(login, new Pair<>(certificateHolder, extractedCert));
					} else {
						//there is already a certificate for this user, you need to compare them on expiration date
						Date dayOfExpirationOfTheNewCertificate = certificateHolder.getNotAfter();
						Date dayOfExpirationOfSavedCertificate = validCertificatesForLogin.get(login).getLeft().getNotAfter();
						//If certificate saved in the structure expires sooner than this certificate, replace it with this one
						if (dayOfExpirationOfTheNewCertificate.after(dayOfExpirationOfSavedCertificate)) {
							String extractedCert = exportBase64Certificate(pemObject);
							validCertificatesForLogin.put(login, new Pair<>(certificateHolder, extractedCert));
						}
					}
					//read another certificate (or null if this was the last one)
					pemObject = pemParser.readPemObject();
				}
			}
		} catch (IOException ex) {
			throw new InternalErrorException(ex);
		} finally {
			if(con != null) {
				con.disconnect();
			}
		}

		return validCertificatesForLogin;
	}

	/**
	 * Check if x509Certificate is valid and return login parsed from subject of such valid certificate.
	 * Throw an exception if certificate is not valid or check can't be done correctly.
	 *
	 * Valid certificate means:
	 *  - it is not expired
	 *  - login of the owner can be parsed from certificate subject
	 *  - every valid owner already exists in perun as User
	 *
	 * @param x509CertificateHolder certificate in x509Holder object
	 * @return if certificate is valid, return login of owner from it's subject
	 * @throws InternalErrorException if there is any problem with getting all logins from Perun (to check existence of owner in Perun)
	 * @throws InvalidCertificateException if certificate is not valid, throw an exception
	 */
	private String checkCertAndGetLogin(X509CertificateHolder x509CertificateHolder, List<String> allLoginsFromPerun) throws InternalErrorException, InvalidCertificateException {
		Date now = new Date();
		//skip expired certificates
		Date dayOfCertificateExpiration = x509CertificateHolder.getNotAfter();
		if (dayOfCertificateExpiration.before(now)) throw new InvalidCertificateException("Certificate is already expired.");

		//skip wrong certificates and parse UCO from the subject
		String subject = x509CertificateHolder.getSubject().toString();
		Matcher loginMatcher = loginPattern.matcher(subject);
		Matcher wrongLoginMatcher = wrongLoginPattern.matcher(subject);
		if (wrongLoginMatcher.matches()) throw new InvalidCertificateException("There is more than one login in certificate's subject.");
		if (!loginMatcher.matches()) throw new InvalidCertificateException("There is missing login in certificate's subject.");

		String login = loginMatcher.group(1);

		//throw an exception for user, who is not in Perun (defined by login)
		if (!allLoginsFromPerun.contains(login)) throw new InvalidCertificateException("Not found User in Perun for this login get from the certificate.");

		return login;
	}

	/**
	 * Export certificate in Base64 from byte array.
	 * Such extracted certificate can be saved much easier.
	 *
	 * Example:
	 * -----BEGIN CERTIFICATE-----
	 * MIICYzCCAcygAwIBAgIBADANBgkqhkiG9...
	 * -----END CERTIFICATE-----
	 *
	 * @param pemObject original parsed certificate from pem file
	 * @return certificate encoded in base64
	 * @throws InternalErrorException if there is any problem with certificate or exporting
	 */
	private String exportBase64Certificate(PemObject pemObject) throws InternalErrorException {
		String exportedCert;
		try (ByteArrayInputStream bis = new ByteArrayInputStream(pemObject.getContent())) {
			CertificateFactory certFact = CertificateFactory.getInstance("X.509");
			Certificate certificate = certFact.generateCertificate(bis);

			//Add begin and end of the certificate in base64
			exportedCert = "-----BEGIN CERTIFICATE-----" + "\n";
			exportedCert += Base64.getMimeEncoder().encodeToString(certificate.getEncoded());
			exportedCert += "\n" + "-----END CERTIFICATE-----";
		} catch (CertificateException | IOException ex) {
			throw new InternalErrorException(ex);
		}
		return exportedCert;
	}
}
