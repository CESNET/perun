package cz.metacentrum.perun.auditlogger.logger;

import cz.metacentrum.perun.audit.events.AuditEvent;

public interface EventLogger extends Runnable {

	public int logEvent(AuditEvent event);

	public void setLastProcessedIdNumber(int lastProcessedId);
}
