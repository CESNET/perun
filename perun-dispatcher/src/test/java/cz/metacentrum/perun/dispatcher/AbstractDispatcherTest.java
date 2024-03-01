package cz.metacentrum.perun.dispatcher;

import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
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
@Rollback
@Transactional(transactionManager = "springTransactionManager")
@ContextConfiguration(locations = {"classpath:perun-core.xml", "classpath:perun-dispatcher-test.xml"})
public abstract class AbstractDispatcherTest {

  protected PerunSession sess = null;
  protected Group group1;
  protected Vo vo1;
  protected User user1;
  protected Facility facility1;
  protected Resource resource1;
  protected Service service1;
  protected Service service2;
  protected Member member1;
  @Autowired
  PerunBl perun;

  @Before
  public void setupTests() throws Exception {

    if (sess == null) {
      PerunPrincipal pp = new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
          ExtSourcesManager.EXTSOURCE_INTERNAL);
      sess = perun.getPerunSession(pp, new PerunClient());

      // create VO for tests
      vo1 = new Vo(0, "testVo", "testVo");
      vo1 = perun.getVosManager().createVo(sess, vo1);

      // now create some facility
      facility1 = new Facility(0, "testFacility", "desc");
      facility1 = perun.getFacilitiesManagerBl().createFacility(sess, facility1);
      // create a resource
      resource1 = new Resource(0, "testResource", "test resource", facility1.getId(), vo1.getId());
      resource1 = perun.getResourcesManagerBl().createResource(sess, resource1, vo1, facility1);

      // create service
      service1 = new Service(0, "testService", null);
      service1.setDelay(1);
      service1.setScript("/bin/true");
      service1.setEnabled(true);
      service1 = perun.getServicesManager().createService(sess, service1);
      // assign service to the resource
      perun.getResourcesManagerBl().assignService(sess, resource1, service1);
      // create service 2
      service2 = new Service(0, "testService2", null);
      service2.setDelay(1);
      service2.setScript("/bin/true");
      service2.setEnabled(true);
      service2 = perun.getServicesManager().createService(sess, service2);
      // assign service to the resource
      perun.getResourcesManagerBl().assignService(sess, resource1, service2);

    }

  }

}
