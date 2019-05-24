package cz.metacentrum.perun.ldapc.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.ldapc.beans.LdapProperties;
import cz.metacentrum.perun.ldapc.service.LdapcManager;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LdapcStarter {
	private final static Logger log = LoggerFactory.getLogger(LdapcStarter.class);

	private LdapcManager ldapcManager;
	private AbstractApplicationContext springCtx;
	private PerunPrincipal perunPrincipal;
	private Perun perunBl;
	private LdapProperties ldapProperties;

	public LdapcStarter() {
		this.perunPrincipal = new PerunPrincipal("perunLdapc", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
		springCtx = new ClassPathXmlApplicationContext("/perun-ldapc.xml");
		this.ldapcManager = springCtx.getBean("ldapcManager", LdapcManager.class);
		this.ldapProperties = springCtx.getBean("ldapProperties", LdapProperties.class);
		this.perunBl = springCtx.getBean("perun", PerunBl.class);
	}

	/**
	 * Main method of ldapc
	 *
	 * @param args (the only argument can be id of message to set Consumer on)
	 */
	public static void main(String[] args) {
		System.out.println("Starting Perun-Ldapc...");

		int lastProcessedIdToSet = 0;
		boolean doSync = false;

		if(args.length == 0) {
			//This is normal behavior, do nothing special, just start ldapc
		} else if (args.length == 1) {
			//This behavior is special, set lastProcessedId
			String argument = args[0];
			if(argument.equals("--sync"))
				doSync = true;
			else
				lastProcessedIdToSet = Integer.valueOf(argument);
		} else {
			System.out.println("Too much arguments, can't understand what to do, exit starting!");
			return;
		}



		try {
			LdapcStarter ldapcStarter = new LdapcStarter();

			// RpcCaller rpcCaller = new RpcCallerImpl(ldapcStarter.perunPrincipal);

			// Just for the Spring IoC to exit gracefully...
			ldapcStarter.springCtx.registerShutdownHook();

			// Sets RPC Caller and Perun
			// ldapcStarter.ldapcManager.setRpcCaller(rpcCaller);
			ldapcStarter.ldapcManager.setPerunPrincipal(ldapcStarter.perunPrincipal);
			ldapcStarter.ldapcManager.setPerunBl(ldapcStarter.perunBl);

			// Synchronize before starting the audit consumer
			if(doSync)
				ldapcStarter.ldapcManager.synchronize();
			else {

				//Set lastProcessedIdToSet if bigger than 0
				if(lastProcessedIdToSet > 0) {
					//Rpc.AuditMessagesManager.setLastProcessedId(rpcCaller, "ldapcConsumer", lastProcessedIdToSet);
					ldapcStarter.perunBl.getAuditMessagesManager().setLastProcessedId(ldapcStarter.ldapcManager.getPerunSession(),
							ldapcStarter.ldapProperties.getLdapConsumerName(), lastProcessedIdToSet);
				}
			}

			// Start processing events (run method in EventProcessorImpl)
			ldapcStarter.ldapcManager.startProcessingEvents();
		} catch (Exception e) {
			log.error(e.toString(), e);
		}

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		log.info(dateFormat.format(date) + ": Done. Perun-Ldapc has started.");
		System.out.println(dateFormat.format(date) + ": Done. Perun-Ldapc has started.");
	}
}
