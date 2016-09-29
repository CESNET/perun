package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.registrar.RegistrarManager;
import cz.metacentrum.perun.registrar.RegistrarModule;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * Application module for ELIXIR purpose
 * 
 * @author Michal Prochazka
 * @author Pavel Zlámal
 * @author Tamás Balogh
 * @see <a href="https://rcauth.eu/policy">https://rcauth.eu/policy</a>
 * 
 * Module which generates userExtSource containing generated DN for ELIXIR CILogon service. 
 * More details are available at https://wiki.geant.org/display/AARC/RCauth.eu-CILogon-like-TTS-pilot
 *
 * Implementation must be kept in sync with: https://github.com/ttomttom/aarc-delegation-server/blob/master/src/main/java/org/delegserver/oauth2/generator/DNGenerator.java#L483
 */
public class ELIXIRCILogonDNGenerator implements RegistrarModule {

	final static Logger log = LoggerFactory.getLogger(ELIXIRCILogonDNGenerator.class);
	
	final static String LOGINATTRIBUTE = "urn:perun:user:attribute-def:virt:login-namespace:elixir-persistent";
	final static String DNPREFIX = "/DC=eu/DC=rcauth/DC=rcauth-clients/O=elixir-europe.org/CN=";
	final static String CADN = "/DC=eu/DC=rcauth/O=Certification Authorities/CN=Research and Collaboration Authentication Pilot G1 CA";

	public static String RDN_TRUNCATE_SIGN = "...";
	public static int RDN_MAX_SIZE = 64;

	private RegistrarManager registrarManager;

	@Override
	public void setRegistrar(RegistrarManager registrar) {
		this.registrarManager = registrar;
	}

	@Override
	public List<ApplicationFormItemData> createApplication(PerunSession user, Application application, List<ApplicationFormItemData> data) throws PerunException {
		return data;
	}

	/**
	 * All new members will get new userExtSource with generated DN according to the CILogon rules:
	 * echo -n "eppn" | openssl dgst -sha256 -binary | base64 | head -c16
	 * where eppn is eduPersonPrincipalName
	 */
	@Override
	public Application approveApplication(PerunSession session, Application app) throws PerunException {

		if (Application.AppType.INITIAL.equals(app.getType())) {
			
			// get perun from session
			PerunBl perun = (PerunBl) session.getPerun();

			User user = app.getUser();
			// Get user ELIXIR persistent login
			String elixirLogin = (String) perun.getAttributesManagerBl().getAttribute(session, user, LOGINATTRIBUTE).getValue();

			// Get user displayName
			String utfDisplayName = user.getCommonName();
			// Remove all non-ascii chars and replace them for "X"
			String displayName = Utils.toASCII(utfDisplayName, "X".charAt(0));
			displayName = truncate(displayName, RDN_MAX_SIZE);

			// Compute hash
			MessageDigest md;
			try {
				md = MessageDigest.getInstance("SHA-256");
			} catch (NoSuchAlgorithmException e) {
				throw new InternalErrorException(e);
			}

			try {
				md.update(elixirLogin.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new InternalErrorException(e);
			} 
			
			byte[] digest = md.digest();
			String hash = Base64.encodeBase64String(digest);
			// Get just first 16 bytes as is described in EU CILogon - RCauth.eu CA requirements
			String CILogonHash = hash.substring(0, 16);
			// Based on the RCauth.eu policy, every '/' must be replaced with '-'
			CILogonHash = CILogonHash.replaceAll("/","-");
			
			// Generate the DN, it must look like /DC=eu/DC=rcauth/DC=rcauth-clients/O=elixir-europe.org/CN=Michal Prochazka rdkfo3rdkfo3kdo
			String dn = DNPREFIX + displayName + " " + CILogonHash;

			// Store the userExtSource
			ExtSource extSource = perun.getExtSourcesManagerBl().checkOrCreateExtSource(session, CADN, ExtSourcesManager.EXTSOURCE_X509);
				
			UserExtSource userExtSource = new UserExtSource(extSource, dn);
			try {
				perun.getUsersManagerBl().addUserExtSource(session, user, userExtSource);
			} catch (UserExtSourceExistsException e) {
				// This can happen, so we can ignore it.
			}
		}

		return app;

	}

	@Override
	public Application rejectApplication(PerunSession session, Application app, String reason) throws PerunException {
		return app;
	}

	@Override
	public Application beforeApprove(PerunSession session, Application app) throws PerunException {
		return app;
	}

	@Override
	public void canBeApproved(PerunSession session, Application app) throws PerunException {
	}

	@Override
	public void canBeSubmitted(PerunSession session, Map<String, String> params) throws PerunException {

	}


	/**
	 * Implementation of the general truncating rule outlined in the RCauth Policy Document
	 * ( https://rcauth.eu/policy ) in section 3.1.2. It takes an RDN as input and checks its
	 * UTF-8 encoded byte size. In case it's larger than the size provided in the parameters,
	 * the RDN will get truncated to 61 UTF-8 bytes (or less in case the bordering byte is
	 * in the middle of a UTF-8 character definition) with RDN_TRUNCATE_SIGN appended to the
	 * end.
	 *
	 * @param rdn Input RDN to be truncated in case it's too large
	 * @param size The size to which the RDN should be truncated. This value defaults to
	 * RDN_MAX_SIZE (64 bytes) in case the size provided is less then or equal to 0
	 * @return Truncated RDN
	 */
	protected String truncate(String rdn, int size) {

		if ( size <= 0 ) {
			size = RDN_MAX_SIZE;
		}

		Charset defaultCharset = Charset.forName("UTF-8");

		// only truncate if the RDN exceeds the maximum allowed size
		if ( rdn.getBytes(defaultCharset).length > size ) {

			int truncatedSize = size - RDN_TRUNCATE_SIGN.getBytes(defaultCharset).length;

			CharsetDecoder cd = defaultCharset.newDecoder();
			byte[] sba = rdn.getBytes(defaultCharset);

			// Ensure truncating by having byte buffer = DB_FIELD_LENGTH
			ByteBuffer bb = ByteBuffer.wrap(sba, 0, truncatedSize); // len in [B]
			CharBuffer cb = CharBuffer.allocate(truncatedSize); // len in [char] <= # [B]

			// Ignore an incomplete character
			cd.onMalformedInput(CodingErrorAction.IGNORE);
			cd.decode(bb, cb, true);
			cd.flush(cb);

			rdn = new String(cb.array(), 0, cb.position()) + RDN_TRUNCATE_SIGN;

		}

		return rdn;

	}

}
