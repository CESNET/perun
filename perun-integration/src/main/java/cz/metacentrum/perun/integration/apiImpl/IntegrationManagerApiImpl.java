package cz.metacentrum.perun.integration.apiImpl;

import cz.metacentrum.perun.core.api.AuthzResolver;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.integration.api.IntegrationManagerApi;
import cz.metacentrum.perun.integration.bl.IntegrationManagerBl;
import cz.metacentrum.perun.integration.model.GroupMemberData;

public class IntegrationManagerApiImpl implements IntegrationManagerApi {

  private IntegrationManagerBl integrationManagerBl;
  private Perun perun;

  @Override
  public GroupMemberData getGroupMemberData(PerunSession sess) throws PrivilegeException {
    Utils.checkPerunSession(sess);

    if (!AuthzResolver.authorizedInternal(sess, "getGroupMemberData_policy")) {
      throw new PrivilegeException("getGroupMemberData");
    }

    return integrationManagerBl.getGroupMemberData(sess);
  }

  public IntegrationManagerBl getIntegrationManagerBl() {
    return integrationManagerBl;
  }

  public Perun getPerun() {
    return perun;
  }

  public void setIntegrationManagerBl(IntegrationManagerBl integrationManagerBl) {
    this.integrationManagerBl = integrationManagerBl;
  }

  public void setPerun(Perun perun) {
    this.perun = perun;
  }
}
