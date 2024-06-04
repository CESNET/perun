package cz.metacentrum.perun.registrar;

import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.impl.InvitationsManagerImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base registrar-lib test class
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:perun-core.xml", "classpath:perun-registrar-lib.xml"})
@Transactional(transactionManager = "perunTransactionManager")
public abstract class AbstractRegistrarIntegrationTest {
	@Autowired
	PerunBl perun;
	@Autowired
	RegistrarManager registrarManager;
	@Autowired
	MailManager mailManager;
	@Autowired
	InvitationsManagerImpl invitationsManager;
	PerunSession session;
	Vo vo;

	@Before
	public void setupTest() throws Exception {

		if (vo == null || session == null) {

			session = perun.getPerunSession(new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
					ExtSourcesManager.EXTSOURCE_INTERNAL), new PerunClient());

			// create test VO
			vo = new Vo(0, "registrarTestVO", "regTestVO");
			vo = perun.getVosManagerBl().createVo(session, vo);

		}

	}

	@After
	public void cleanTest() throws Exception {

		//perun.getVosManagerBl().deleteVo(session, vo, true);

	}

}
