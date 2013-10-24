package cz.metacentrum.perun.engine.scheduling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.engine.BaseTest;
import cz.metacentrum.perun.engine.model.Pair;
import cz.metacentrum.perun.taskslib.model.ExecService;

/**
 * @author Michal Karm Babacek
 */
public class SchedulingPoolTest extends BaseTest {

    private final static Logger log = LoggerFactory.getLogger(SchedulingPoolTest.class);

    @Autowired
    private SchedulingPool schedulingPool;
    private List<Pair<ExecService, Facility>> testPairs = new ArrayList<Pair<ExecService, Facility>>();

    @Before
    public void setUp() {
        schedulingPool.emptyPool();
        testPairs.clear();
        for (int i = 0; i < 10; i++) {
            ExecService execService = new ExecService();
            execService.setId(i);

            Facility facility = new Facility();
            facility.setId(i + 1);
            facility.setName(i + "Name" + i);

            testPairs.add(new Pair<ExecService, Facility>(execService, facility));
        }
    }

    @Test
    public void testSimpleAdd() {
        try {
            log.debug("testSimpleAdd: Adding...");

            for (Pair<ExecService, Facility> pair : testPairs) {
                schedulingPool.addToPool(pair);
            }

            assertEquals(10, schedulingPool.getSize());

        } catch (Exception e) {
            log.error(e.toString(), e);
            fail();
        }
    }

    @Test
    public void testDoubleAdd() {
        try {
            log.debug("testDoubleAdd: Adding...");

            for (Pair<ExecService, Facility> pair : testPairs) {
                schedulingPool.addToPool(pair);
            }

            for (Pair<ExecService, Facility> pair : testPairs) {
                schedulingPool.addToPool(pair);
            }
            // Pooling -> one copy of each pair...
            assertEquals(10, schedulingPool.getSize());

        } catch (Exception e) {
            log.error(e.toString(), e);
            fail();
        }
    }

    @Test
    public void testEmpty() {
        try {
            log.debug("testEmpty: Adding...");

            for (Pair<ExecService, Facility> pair : testPairs) {
                schedulingPool.addToPool(pair);
            }

            schedulingPool.emptyPool();
            assertEquals(0, schedulingPool.getSize());

        } catch (Exception e) {
            log.error(e.toString(), e);
            fail();
        }
    }

    @Test
    public void testGetAndCompare() {
        try {
            log.debug("testGetAndCompare: Adding...");

            for (Pair<ExecService, Facility> pair : testPairs) {
                schedulingPool.addToPool(pair);
            }

            Set<Pair<ExecService, Facility>> resultPairs = new HashSet<Pair<ExecService, Facility>>(schedulingPool.emptyPool());
            Set<Pair<ExecService, Facility>> resultSet = new HashSet<Pair<ExecService, Facility>>(testPairs);

            assertEquals(resultSet, resultPairs);
            assertEquals(0, schedulingPool.getSize());

        } catch (Exception e) {
            log.error(e.toString(), e);
            fail();
        }
    }

    public void setSchedulingPool(SchedulingPool schedulingPool) {
        this.schedulingPool = schedulingPool;
    }

    public SchedulingPool getSchedulingPool() {
        return schedulingPool;
    }

}
