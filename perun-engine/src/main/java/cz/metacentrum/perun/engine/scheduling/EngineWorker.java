package cz.metacentrum.perun.engine.scheduling;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * Common interface for EngineWorkers, which are used to start gen/send scripts.
 *
 * @author David Šarman
 *
 * @param <V> Type Task Worker handles (Task,SendTask).
 */
public interface EngineWorker<V> extends Callable<V> {

	@Override
	V call() throws Exception;

	/**
	 * Get Directory to look for scripts
	 *
	 * @return Directory
	 */
	File getDirectory();

	/**
	 * Set Directory to look for scripts
	 *
	 * @param directory Directory to look for scripts
	 */
	void setDirectory(File directory);

}
