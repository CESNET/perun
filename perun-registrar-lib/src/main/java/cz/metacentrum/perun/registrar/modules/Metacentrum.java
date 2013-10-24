package cz.metacentrum.perun.registrar.modules;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.AlreadyMemberException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.registrar.RegistrarModule;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Module for VO Metacentrum
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class Metacentrum implements RegistrarModule {

    final static Logger log = LoggerFactory.getLogger(Metacentrum.class);

    @Override
    public List<ApplicationFormItemData> createApplication(PerunSession user, Application application, List<ApplicationFormItemData> data) throws PerunException {
        return data;
    }

    /**
     * Add all new Metacentrum members to "storage" group.
     */
    @Override
    public Application approveApplication(PerunSession session, Application app) throws PerunException {

        // get perun from session
        Perun perun = session.getPerun();

        if (Application.AppType.INITIAL.equals(app.getType())) {

            Vo vo = app.getVo();
            User user = app.getUser();
            Group group = perun.getGroupsManager().getGroupByName(session, vo, "storage");
            Member mem = perun.getMembersManager().getMemberByUser(session, vo, user);

            try  {
                perun.getGroupsManager().addMember(session, group, mem);
            } catch (AlreadyMemberException ex) {

            }
        }
        return app;

    }

    @Override
    public Application rejectApplication(PerunSession session, Application app, String reason) throws PerunException {
        return app;
    }
}
