package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.ExtendMembershipException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.registrar.model.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This module ensures, that all new VO members which goes through registrations
 * are also added to common VO "e-INFRA CZ".
 * <p>
 * It should be only used by VOs, which belong to this infrastructure!
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class Einfracz extends DefaultRegistrarModule {

  private final static Logger log = LoggerFactory.getLogger(Einfracz.class);

  /**
   * Add approved VO members into e-INFRA CZ VO.
   */
  @Override
  public Application approveApplication(PerunSession session, Application app)
      throws WrongReferenceAttributeValueException, WrongAttributeValueException {

    PerunBl perun = (PerunBl) session.getPerun();
    User user = app.getUser();
    Vo vo = app.getVo();

    // For INITIAL VO APPLICATIONS
    if (Application.AppType.INITIAL.equals(app.getType()) && app.getGroup() == null) {
      try {
        Vo einfraVo = perun.getVosManagerBl().getVoByShortName(session, "e-infra.cz");
        Member einfraMember = perun.getMembersManagerBl().createMember(session, einfraVo, user);
        log.debug("{} member added to \"e-INFRA CZ\": {}", vo.getName(), einfraMember);
        perun.getMembersManagerBl().validateMemberAsync(session, einfraMember);
      } catch (VoNotExistsException e) {
        log.warn("e-INFRA CZ VO doesn't exists, {} member can't be added into it.", vo.getName());
      } catch (AlreadyMemberException ignore) {
        // user is already in e-INFRA CZ
      } catch (ExtendMembershipException e) {
        // can't be member of e-INFRA CZ, shouldn't happen
        log.error("{} member can't be added to \"e-INFRA CZ\": {}", vo.getName(), e);
      }
    }

    return app;

  }

}
