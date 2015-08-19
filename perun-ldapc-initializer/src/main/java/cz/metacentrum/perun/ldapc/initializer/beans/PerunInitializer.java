package cz.metacentrum.perun.ldapc.initializer.beans;

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

	private PerunBl perunBl;
	private AbstractApplicationContext springCtx;
	private PerunSession perunSession;
	private PerunPrincipal perunPrincipal;
	private BufferedWriter outputWriter;
	private boolean newVersionOfLDAP;
	private final String consumerName = "ldapcConsumer";

	public PerunInitializer() throws InternalErrorException, FileNotFoundException {
		//null means STDOUT
		this(false, null);
	}

	public PerunInitializer(boolean newVersionOfLDAP) throws InternalErrorException, FileNotFoundException {
		//null means STDOUT
		this(newVersionOfLDAP, null);
	}

	public PerunInitializer(String outputFileName) throws InternalErrorException, FileNotFoundException {
		this(false, outputFileName);
	}

	public PerunInitializer(boolean newVersionOfLDAP, String outputFileName) throws InternalErrorException, FileNotFoundException {
		this.perunPrincipal = new PerunPrincipal("perunLdapInitializer", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
		this.springCtx = new ClassPathXmlApplicationContext("perun-core.xml", "perun-core-jdbc.xml", "perun-core-transaction-manager.xml");
		this.perunBl = springCtx.getBean("perun", PerunBl.class);
		this.perunSession = perunBl.getPerunSession(perunPrincipal);
		this.outputWriter = new BufferedWriter(Utils.getWriterForOutput(outputFileName));
		this.newVersionOfLDAP = newVersionOfLDAP;
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

	public boolean isNewVersionOfLDAP() {
		return newVersionOfLDAP;
	}

	public String getConsumerName() {
		return consumerName;
	}

	public void closeWriter() throws IOException {
		this.outputWriter.close();
	}
}

