package cz.metacentrum.perun.ldapc.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.ldapc.beans.GroupSynchronizer;
import cz.metacentrum.perun.ldapc.beans.LdapProperties;
import cz.metacentrum.perun.ldapc.beans.ResourceSynchronizer;
import cz.metacentrum.perun.ldapc.beans.UserSynchronizer;
import cz.metacentrum.perun.ldapc.beans.VOSynchronizer;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher;
import cz.metacentrum.perun.ldapc.service.LdapcManager;
import cz.metacentrum.perun.rpclib.api.RpcCaller;

@org.springframework.stereotype.Service(value = "ldapcManager")
public class LdapcManagerImpl implements LdapcManager {

	private final static Logger log = LoggerFactory.getLogger(LdapcManagerImpl.class);

	private Thread eventProcessorThread;
	@Autowired
	private EventDispatcher eventDispatcher;
	@Autowired
	private VOSynchronizer voSynchronizer;
	@Autowired
	private ResourceSynchronizer resourceSynchronizer;
	@Autowired
	private GroupSynchronizer groupSynchronizer;
	@Autowired
	private UserSynchronizer userSynchronizer;
	@Autowired
	private LdapProperties ldapProperties;
	
	private RpcCaller rpcCaller;
	private PerunPrincipal perunPrincipal;
	private Perun perunBl;
	private PerunSession perunSession;
	
	public void startProcessingEvents() {
		eventProcessorThread = new Thread(eventDispatcher);
		eventProcessorThread.start();

		log.debug("Event processor thread started.");
		System.out.println("Event processor thread started.");
	}

	public void stopProcessingEvents() {
		eventProcessorThread.interrupt();
		log.debug("Event processor thread interrupted.");
		System.out.println("Event processor thread interrupted.");
	}

	public void synchronize() {
		try {
			voSynchronizer.synchronizeVOs();
			userSynchronizer.synchronizeUsers();
			resourceSynchronizer.synchronizeResources();
			groupSynchronizer.synchronizeGroups();

			int lastProcessedMessageId = getPerunBl().getAuditer().getLastMessageId();
			getPerunBl().getAuditer().setLastProcessedId(ldapProperties.getLdapConsumerName(), lastProcessedMessageId);
		} catch (Exception  e) {
			log.error("Error synchronizing to LDAP", e);
		}
	}
	
	public void setRpcCaller(RpcCaller rpcCaller) {
		this.rpcCaller = rpcCaller;
	}

	public RpcCaller getRpcCaller() {
		return this.rpcCaller;
	}

	public Perun getPerunBl() {
		return perunBl;
	}

	public void setPerunBl(Perun perunBl) {
		this.perunBl = perunBl;
	}

	public PerunSession getPerunSession() throws InternalErrorException {
		if(perunSession == null) {
			this.perunSession = perunBl.getPerunSession(perunPrincipal, new PerunClient());
		}
		return perunSession;
	}

	public PerunPrincipal getPerunPrincipal() {
		return perunPrincipal;
	}

	public void setPerunPrincipal(PerunPrincipal perunPrincipal) {
		this.perunPrincipal = perunPrincipal;
	}
}
