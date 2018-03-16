package cz.metacentrum.perun.engine.scheduling.impl;

import cz.metacentrum.perun.engine.exceptions.TaskExecutionException;
import cz.metacentrum.perun.engine.scheduling.EngineWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 *
 *
 */
public abstract class AbstractWorker<V> implements EngineWorker<V> {

	private final static Logger log = LoggerFactory.getLogger(AbstractWorker.class);
	private File directory;
	private Integer returnCode = -1;
	private String stdout = null;
	private String stderr = null;

	@Override
	public abstract V call() throws TaskExecutionException;

	@Override
	public File getDirectory() {
		return directory;
	}

	@Override
	public void setDirectory(File directory) {
		this.directory = directory;
	}

	public Integer getReturnCode() {
		return returnCode;
	}

	public String getStdout() {
		return (stdout == null) ? "" : stdout;
	}

	public String getStderr() {
		return (stderr == null) ? "" : stderr;
	}

	/**
	 * Execute script defined by java ProcessBuilder
	 *
	 * @param pb ProcessBuilder
	 * @throws InterruptedException Usually when Engine shuts down.
	 * @throws IOException If running script was terminated by outer process.
	 */
	protected void execute(ProcessBuilder pb) throws InterruptedException, IOException {

		log.debug("The directory for the worker will be [{}]", getDirectory());
		if (getDirectory() != null) {
			// set path relative to current working dir
			pb.directory(getDirectory());
		}

		Process process = pb.start();

		StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());
		StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());

		errorGobbler.start();
		outputGobbler.start();

		returnCode = process.waitFor();

		while (errorGobbler.isAlive() || outputGobbler.isAlive()) Thread.sleep(50);

		stderr = errorGobbler.getSb();
		stdout = outputGobbler.getSb();

	}

}
