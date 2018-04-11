package cz.metacentrum.perun.dispatcher;

import cz.metacentrum.perun.controller.service.GeneralServiceManager;
import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.bl.PerunBl;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Abstract base class for all tests defining spring context
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:perun-core.xml","classpath:perun-tasks-lib.xml", "classpath:perun-dispatcher-test.xml" })
@Rollback
@Transactional(transactionManager = "springTransactionManager")
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
	protected Service service2;
	protected Member member1;

	@Before
	public void setUpSess() throws Exception {

		if (sess == null) {
			PerunPrincipal pp = new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
			sess = perun.getPerunSession(pp, new PerunClient());

			// create VO for tests
			vo1 = new Vo(0, "testVo", "testVo");
			vo1 = perun.getVosManager().createVo(sess, vo1);
			// create some group in there
			group1 = new Group("falcon", "desc");
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
			facility1 = new Facility(0, "testFacility", "desc");
			facility1 = perun.getFacilitiesManager().createFacility(sess, facility1);
			// create a resource
			resource1 = new Resource(0, "testResource", "test resource", facility1.getId(), vo1.getId());
			resource1 = perun.getResourcesManager().createResource(sess, resource1, vo1, facility1);
			// assign the group to this resource
			perun.getResourcesManager().assignGroupToResource(sess, group1, resource1);
			// create service
			service1 = new Service(0, "testService", null);
			service1.setDelay(1);
			service1.setScript("/bin/true");
			service1.setEnabled(true);
			service1 = perun.getServicesManager().createService(sess, service1);
			// assign service to the resource
			perun.getResourcesManager().assignService(sess, resource1, service1);
			// create service 2
			service2 = new Service(0, "testService2", null);
			service2.setDelay(1);
			service2.setScript("/bin/true");
			service2.setEnabled(true);
			service2 = perun.getServicesManager().createService(sess, service2);
			// assign service to the resource
			perun.getResourcesManager().assignService(sess, resource1, service2);

		}

	}

}
