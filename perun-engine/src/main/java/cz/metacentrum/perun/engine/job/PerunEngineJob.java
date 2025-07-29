package cz.metacentrum.perun.engine.job;

/**
 * Interface for scheduled engine jobs to be defined in `perun-engine-scheduler.xml` and perform automated tasks.
 *
 * @author Michal Karm Babacek
 */
public interface PerunEngineJob {

  void doTheJob();

}
