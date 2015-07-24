package cz.metacentrum.perun.dispatcher;

import cz.metacentrum.perun.controller.service.GeneralServiceManager;
import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.taskslib.model.ExecService;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Abstract base class for all tests defining spring context
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:perun-tasks-lib.xml", "classpath:perun-dispatcher-test.xml"})
@TransactionConfiguration(transactionManager = "springTransactionManager", defaultRollback = true)
@Transactional
public abstract class AbstractDispatcherTest {

	@Autowired
	PerunBl perun;

	@Autowired
	GeneralServiceManager generalServiceManager;

	protected PerunSession sess;
	protected Group group1;
	protected Vo vo1;
	protected User user1;
	protected Facility facility1;
	protected Resource resource1;
	protected Service service1;
	protected Owner owner1;
	protected Member member1;
	protected ExecService execservice1;
	protected ExecService execservice2;

	@Before
	public void setUpSess() throws Exception {

		if (sess == null) {

			PerunPrincipal pp = new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
			sess = perun.getPerunSession(pp);

			// create VO for tests
			vo1 = new Vo(0, "testVo", "testVo");
			vo1 = perun.getVosManager().createVo(sess, vo1);
			// create some group in there
			group1 = new Group("falcon", "");
			group1 = perun.getGroupsManager().createGroup(sess, vo1, group1);
			// create user in the VO
			// skip the xEntry (authorization check),
			// could skip the xBl a go directly to xImpl to avoid writing audit
			// log
			user1 = new User(0, "firstName", "lastName", "", "", "");
			user1 = perun.getUsersManagerBl().createUser(sess, user1);
			// make the user the member of the group
			member1 = perun.getMembersManager().createMember(sess, vo1, user1);
			member1.setStatus("VALID");
			perun.getGroupsManager().addMember(sess, group1, member1);
			// now create some facility
			facility1 = new Facility(0, "testFacility", "");
			facility1 = perun.getFacilitiesManager().createFacility(sess, facility1);
			// create a resource
			resource1 = new Resource(0, "testResource", "test resource", facility1.getId(), vo1.getId());
			resource1 = perun.getResourcesManager().createResource(sess, resource1, vo1, facility1);
			// assign the group to this resource
			perun.getResourcesManager().assignGroupToResource(sess, group1, resource1);
			// create owner
			owner1 = new Owner(0, "testOwner", "do not contact me", OwnerType.technical);
			perun.getOwnersManager().createOwner(sess, owner1);
			// create service
			service1 = new Service(0, "testService");
			service1 = perun.getServicesManager().createService(sess, service1, owner1);
			// assign service to the resource
			perun.getResourcesManager().assignService(sess, resource1, service1);
			// create execService
			execservice1 = new ExecService();
			execservice1.setDefaultDelay(1);
			execservice1.setScript("");
			execservice1.setEnabled(true);
			execservice1.setExecServiceType(ExecService.ExecServiceType.SEND);
			execservice1.setService(service1);
			int id = generalServiceManager.insertExecService(sess, execservice1, owner1);
			// stash back the created id (this should be really done somewhere else)
			execservice1.setId(id);
			// create execService
			execservice2 = new ExecService();
			execservice2.setDefaultDelay(1);
			execservice2.setScript("/bin/true");
			execservice2.setEnabled(true);
			execservice2.setExecServiceType(ExecService.ExecServiceType.SEND);
			execservice2.setService(service1);
			id = generalServiceManager.insertExecService(sess, execservice2, owner1);
			// stash back the created id (this should be really done somewhere else)
			execservice2.setId(id);

		}

	}

}