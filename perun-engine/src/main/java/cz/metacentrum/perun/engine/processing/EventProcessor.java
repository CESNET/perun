package cz.metacentrum.perun.engine.processing;

/**
 * Takes care of receiving string events, adding the resulting Tasks into the SchedulingPool or modifying existing
 * Tasks parameters.
 *
 * @author Michal Karm Babacek
 */
public interface EventProcessor {

	public void receiveEvent(String event);

}
