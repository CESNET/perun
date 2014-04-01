package cz.metacentrum.perun.core.impl;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.blImpl.RTMessagesManagerBlImpl;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public class PerunAuthenticatorImpl extends Authenticator {
	private final static Logger log = LoggerFactory.getLogger(PerunAuthenticatorImpl.class);

	@GuardedBy("itself")
	private ConcurrentMap<String, PasswordAuthentication> passwordAuthenticationsForURL = new ConcurrentHashMap<String, PasswordAuthentication>();

	@GuardedBy("PerunAuthenticatorImpl.class")
	private static PerunAuthenticatorImpl instance;

	synchronized public static PerunAuthenticatorImpl getPerunAuthenticator() {
		if(instance == null) {
			instance = new PerunAuthenticatorImpl();
			Authenticator.setDefault(instance);
		}
		return instance;
	}

	private PerunAuthenticatorImpl() {
		super();
	}

	/**
	 * {@inheritDoc}
	 * @see Authenticator#getPasswordAuthentication()
	 */
	protected PasswordAuthentication getPasswordAuthentication() {
		URL url = getRequestingURL();
		PasswordAuthentication passwordAuthentication = passwordAuthenticationsForURL.get(url.toExternalForm());
		if(passwordAuthentication == null) log.info("No authenticator provided for url {}", url);
		return passwordAuthentication;
	}

	/**
	 * Register passwordAuthentication for specific URL. If there is some passwordAuthentication already registered for the url it is replaced by the new one.
	 *
	 * @param url
	 * @param passwordAuthentication
	 * @return true if there is no passwordAuthentication already registered for the URL, false otherwise
	 */
	public boolean registerAuthenticationForURL(URL url, PasswordAuthentication passwordAuthentication) {
		log.info("Authenticator registered for url {}", url);
		return passwordAuthenticationsForURL.put(url.toExternalForm(), passwordAuthentication) == null;
	}

	/**
	 * Unregister passwordAuthentication for specific URL.
	 *
	 * @param url
	 * @return true if there was passwordAuthentication registered for the URL
	 */
	public boolean unRegisterAuthenticationForURL(URL url) {
		log.info("Authenticator unregistered for url {}", url);
		return passwordAuthenticationsForURL.remove(url.toExternalForm()) == null;
	}

}
