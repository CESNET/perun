package cz.metacentrum.perun.auditlogger.service;

import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

public interface AuditLoggerManager {

	/**
	 * Start processing incommming events from Perun Auditer.
	 */
	void startProcessingEvents();

	/**
	 * Stop processing incommming events from Perun Auditer.
	 */
	void stopProcessingEvents();

	public Perun getPerunBl();

	public void setPerunBl(Perun perunBl);

	public PerunSession getPerunSession();

	public PerunPrincipal getPerunPrincipal();

	public void setPerunPrincipal(PerunPrincipal perunPrincipal);

	public void setLastProcessedId(int lastProcessedId);

	String getConsumerName();

	String getStateFile();

}
