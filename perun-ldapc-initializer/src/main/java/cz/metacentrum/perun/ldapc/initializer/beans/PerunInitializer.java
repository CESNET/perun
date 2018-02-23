package cz.metacentrum.perun.ldapc.initializer.beans;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.ldapc.initializer.utils.Utils;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.PerunBl;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Main settings for whole module need to be set in this object before
 * starting generation of ldif.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class PerunInitializer {

	private final PerunBl perunBl;
	private final AbstractApplicationContext springCtx;
	private final PerunSession perunSession;
	private final PerunPrincipal perunPrincipal;
	private final BufferedWriter outputWriter;
	private final String consumerName = "ldapcConsumer";
	private final String ldapcPropertyFile = "perun-ldapc.properties";

	public PerunInitializer(String outputFileName) throws InternalErrorException, FileNotFoundException {
		this.perunPrincipal = new PerunPrincipal("perunLdapInitializer", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
		this.springCtx = new ClassPathXmlApplicationContext("perun-core.xml");
		this.perunBl = springCtx.getBean("perun", PerunBl.class);
		this.perunSession = perunBl.getPerunSession(perunPrincipal, new PerunClient());
		this.outputWriter = new BufferedWriter(Utils.getWriterForOutput(outputFileName));
	}

	public PerunBl getPerunBl() {
		return perunBl;
	}

	public AbstractApplicationContext getSpringCtx() {
		return springCtx;
	}

	public PerunSession getPerunSession() {
		return perunSession;
	}

	public PerunPrincipal getPerunPrincipal() {
		return perunPrincipal;
	}

	public BufferedWriter getOutputWriter() {
		return outputWriter;
	}

	public String getConsumerName() {
		return consumerName;
	}

	public String getLdapBase() throws InternalErrorException {
		return BeansUtils.getPropertyFromCustomConfiguration(ldapcPropertyFile, "ldap.base");
	}

	public String getLoginNamespace() throws InternalErrorException {
		return BeansUtils.getPropertyFromCustomConfiguration(ldapcPropertyFile, "ldap.loginNamespace");
	}

	public void closeWriter() throws IOException {
		this.outputWriter.close();
	}
}

