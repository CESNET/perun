package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.registrar.RegistrarModule;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.List;

/**
 * Application module for ELIXIR purpose
 * 
 * @author Michal Prochazka
 * 
 * Module which generates userExtSource containing generated DN for ELIXIR CILogon service. 
 * More details are available at https://wiki.geant.org/display/AARC/RCauth.eu-CILogon-like-TTS-pilot 
 */
public class ELIXIRCILogonDNGenerator implements RegistrarModule {

	final static Logger log = LoggerFactory.getLogger(ELIXIRCILogonDNGenerator.class);
	
	final static String ELIXIRSCOPE = "@elixir-europe.org";
	final static String LOGINATTRIBUTE = "urn:perun:user:attribute-def:def:login-namespace:elixir";
	final static String DISPLAYNAMEATTRIBUTE = "urn:perun:user:attribute-def:def:displayName";
	final static String DNPREFIX = "/DC=eu/DC=rcauth/DC=rcauth-clients/O=elixir-europe.org/CN=";
	final static String CADN = "/DC=eu/DC=rcauth/O=Certification Authorities/CN=Research and Collaboration Authentication Pilot G1 CA";

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
			Perun perun = session.getPerun();

			User user = app.getUser();
			// Get user login
			String login = (String) perun.getAttributesManager().getAttribute(session, user, LOGINATTRIBUTE).getValue();
			// Create ELIXIR login from user login and scope
			String elixirLogin = login + ELIXIRSCOPE;
			
			// Get user displayName
			String utfDisplayName = (String) perun.getAttributesManager().getAttribute(session, user, DISPLAYNAMEATTRIBUTE).getValue();
			// Convert to ASCII only
			String displayName = Utils.utftoasci(utfDisplayName);
			
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
			Encoder enc = Base64.getEncoder();
			String hash = enc.encodeToString(digest);
			// Get just first 16 bytes as is described in EU CILogon - RCauth.eu CA requirements
			String CILogonHash = hash.substring(0, 16);
			
			// Generate the DN, it must look like /DC=eu/DC=rcauth/DC=rcauth-clients/O=elixir-europe.org/CN=Michal Prochazka rdkfo3rdkfo3kdo
			String dn = DNPREFIX + displayName + " " + CILogonHash;

			// Store the userExtSource
			ExtSource extSource = perun.getExtSourcesManager().getExtSourceByName(session, CADN);
		
			// TODO This must be run under perunBL, user doesn't have rights to create an extSource
			perun.getExtSourcesManager().checkOrCreateExtSource(session, CADN, ExtSourcesManager.EXTSOURCE_X509);
				
			UserExtSource userExtSource = new UserExtSource(extSource, dn);
			try {
				perun.getUsersManager().addUserExtSource(session, user, userExtSource);
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

}
