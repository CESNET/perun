package cz.metacentrum.perun.engine.unit;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.util.Assert;

import cz.metacentrum.perun.engine.TestBase;
import cz.metacentrum.perun.engine.scheduling.DependenciesResolver;
import cz.metacentrum.perun.engine.scheduling.SchedulingPool;
import cz.metacentrum.perun.taskslib.model.Task;

public class DependenciesResolverImplTest extends TestBase {

	@Autowired
	private Task task1;
	@Autowired
	private Task task2;
	@Autowired
	private SchedulingPool schedulingPool;
	@Autowired
	private DependenciesResolver dependenciesResolver;
	
	// it is not possible to test addDependency alone
	
	@Before
	public void setup() {
		schedulingPool.addToPool(task1);
		dependenciesResolver.addDependency(task2, task1.getId());
	}
	
    @IfProfileValue(name="perun.test.groups", values=("unit-tests"))
	@Test
	public void getDependencyTest() {
		List<Task> dependencies = dependenciesResolver.getDependencies(task2);
		Assert.isTrue(dependencies.size() == 1, "size is 1");
		Assert.isTrue(dependencies.get(0) == task1, "depends on task1");
		dependencies = dependenciesResolver.getDependencies(task1);
		Assert.isTrue(dependencies.size() == 0, "no dependencies");
	}
	
    @IfProfileValue(name="perun.test.groups", values=("unit-tests"))
	@Test
	public void getDependantsTest() {
		List<Task> dependants = dependenciesResolver.getDependants(task1);
		Assert.isTrue(dependants.size() == 1, "size is 1");
		Assert.isTrue(dependants.get(0) == task2, "task2 depends");
		dependants = dependenciesResolver.getDependants(task2);
		Assert.isTrue(dependants.size() == 0, "size is 0");
	}
	
	@After
	public void cleanup() {
		schedulingPool.removeTask(task1);
	}
}
