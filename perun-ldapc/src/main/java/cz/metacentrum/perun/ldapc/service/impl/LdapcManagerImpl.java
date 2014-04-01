package cz.metacentrum.perun.ldapc.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.ldapc.processor.EventProcessor;
import cz.metacentrum.perun.ldapc.service.LdapcManager;
import cz.metacentrum.perun.rpclib.api.RpcCaller;

@org.springframework.stereotype.Service(value = "ldapcManager")
public class LdapcManagerImpl implements LdapcManager {

	private final static Logger log = LoggerFactory.getLogger(LdapcManagerImpl.class);

	private Thread eventProcessorThread;
	@Autowired
	private EventProcessor eventProcessor;
	private RpcCaller rpcCaller;

	public void startProcessingEvents() {
		eventProcessorThread = new Thread(eventProcessor);
		eventProcessorThread.start();

		log.debug("Event processor thread started.");
		System.out.println("Event processor thread started.");
	}

	public void stopProcessingEvents() {
		eventProcessorThread.interrupt();
		log.debug("Event processor thread interrupted.");
		System.out.println("Event processor thread interrupted.");
	}

	public void setRpcCaller(RpcCaller rpcCaller) {
		this.rpcCaller = rpcCaller;
	}

	public RpcCaller getRpcCaller() {
		return this.rpcCaller;
	}
}
