package cz.metacentrum.perun.engine.scheduling;

import java.util.List;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.engine.model.Pair;
import cz.metacentrum.perun.taskslib.model.ExecService;

/**
 * 
 * @author Michal Karm Babacek JavaDoc coming soon...
 * 
 */
public interface SchedulingPool {

    /**
     * Add to the pool
     * 
     * @param pair
     * @return current pool size
     */
    int addToPool(Pair<ExecService, Facility> pair);

    /**
     * Get all pairs from the pool.
     * NOTE: This action will empty the pool!
     * 
     * @return
     */
    List<Pair<ExecService, Facility>> emptyPool();

    /**
     * Size
     * 
     * @return current pool size
     */
    int getSize();

    void close();

}
