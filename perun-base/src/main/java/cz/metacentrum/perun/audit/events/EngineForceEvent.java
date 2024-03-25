package cz.metacentrum.perun.audit.events;

/**
 * Marker interface for AuditEvent classes.
 * <p>
 * Classes with this interface will be "forced" for immediate propagation in Dispatcher/Engine
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public interface EngineForceEvent {
}
