package cz.metacentrum.perun.engine.unit;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import cz.metacentrum.perun.engine.AbstractEngineTest;
import cz.metacentrum.perun.engine.scheduling.DependenciesResolver;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.taskslib.model.Task;

/**
 * Test of DependenciesResolverImpl which is a local storage of Task dependencies.
 *
 * @author Michal Karm Babacek
 * @author Michal Voců
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class DependenciesResolverImplTest extends AbstractEngineTest {

	@Autowired
	private SchedulingPool schedulingPool;
	@Autowired
	private DependenciesResolver dependenciesResolver;

	@Before
	public void setup() throws Exception {
		super.setup();
		schedulingPool.addToPool(task1);
		dependenciesResolver.addDependency(task2, task1.getId());
	}

	@Test
	public void getDependencyTest() {
		System.out.println("DependenciesResolverImplTest.getDependency");

		List<Task> dependencies = dependenciesResolver.getDependencies(task2);
		Assert.isTrue(dependencies.size() == 1, "size is 1");
		Assert.isTrue(dependencies.get(0) == task1, "depends on task1");
		dependencies = dependenciesResolver.getDependencies(task1);
		Assert.isTrue(dependencies.size() == 0, "no dependencies");
	}

	@Test
	public void getDependantsTest() {
		System.out.println("DependenciesResolverImplTest.getDependants");

		List<Task> dependants = dependenciesResolver.getDependants(task1);
		Assert.isTrue(dependants.size() == 1, "size is 1");
		Assert.isTrue(dependants.get(0) == task2, "task2 depends");
		List<Task> dependants2 = dependenciesResolver.getDependants(task2);
		Assert.isTrue(dependants2.size() == 0, "size is 0");
	}

	@After
	public void cleanup() {
		schedulingPool.removeTask(task1);
	}

}
