package cz.metacentrum.perun.auditlogger.logger;

import cz.metacentrum.perun.core.api.AuditMessage;

public interface EventLogger extends Runnable {

  public int logMessage(AuditMessage message);

  public void setLastProcessedIdNumber(int lastProcessedId);
}
