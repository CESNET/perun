package cz.metacentrum.perun.registrar;

import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceExistsException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.registrar.impl.InvitationsManagerImpl;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  ExtSource testExtSource;

  @Before
  public void setupTest() throws Exception {

    if (vo == null || session == null) {

      session = perun.getPerunSession(new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
          ExtSourcesManager.EXTSOURCE_INTERNAL), new PerunClient());

      if (testExtSource == null) {
        testExtSource =
            perun.getExtSourcesManagerBl().createExtSource(session,
                new ExtSource(0, "registrarTestExtSource", ExtSourcesManager.EXTSOURCE_INTERNAL), null);
      }

      // create test VO
      vo = new Vo(0, "registrarTestVO", "regTestVO");
      vo = perun.getVosManagerBl().createVo(session, vo);

    }

  }

  @After
  public void cleanTest() throws Exception {

    //perun.getVosManagerBl().deleteVo(session, vo, true);

  }

  protected PerunSession getUserSession(User user) {
    UserExtSource userExtSource = new UserExtSource();
    userExtSource.setExtSource(testExtSource);
    userExtSource.setLogin("user-" + user.getId());
    try {
      perun.getUsersManagerBl().addUserExtSource(session, user, userExtSource);
    } catch (UserExtSourceExistsException ex) {
      // user identity already registered in previous test, session can still be created
    } catch (PerunException ex) {
      throw new RuntimeException(ex);
    }
    return new PerunSessionImpl(session.getPerun(),
        new PerunPrincipal("user-" + user.getId(), testExtSource.getName(),
            testExtSource.getType(), user), session.getPerunClient());
  }

  protected PerunSession getNoUserSession(String extSourceName, String extSourceType) {
    return new PerunSessionImpl(session.getPerun(),
        new PerunPrincipal(session.getPerunPrincipal().getActor(), extSourceName, extSourceType),
        session.getPerunClient());
  }

  protected static Map<Integer, String> getItemDataMap(List<ApplicationFormItemData> data) {
    Map<Integer, String> map = new HashMap<>();
    for (ApplicationFormItemData item : data) {
      map.put(item.getFormItem().getId(), item.getValue());
    }
    return map;
  }

}
