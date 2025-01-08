package cz.metacentrum.perun.rpc;

import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import cz.metacentrum.perun.rpc.methods.AttributesManagerMethod;
import cz.metacentrum.perun.rpc.methods.AuditMessagesManagerMethod;
import cz.metacentrum.perun.rpc.methods.AuthzResolverMethod;
import cz.metacentrum.perun.rpc.methods.CabinetManagerMethod;
import cz.metacentrum.perun.rpc.methods.ConfigManagerMethod;
import cz.metacentrum.perun.rpc.methods.ConsentsManagerMethod;
import cz.metacentrum.perun.rpc.methods.DatabaseManagerMethod;
import cz.metacentrum.perun.rpc.methods.ExtSourcesManagerMethod;
import cz.metacentrum.perun.rpc.methods.FacilitiesManagerMethod;
import cz.metacentrum.perun.rpc.methods.GroupsManagerMethod;
import cz.metacentrum.perun.rpc.methods.IntegrationManagerMethod;
import cz.metacentrum.perun.rpc.methods.InvitationsManagerMethod;
import cz.metacentrum.perun.rpc.methods.MembersManagerMethod;
import cz.metacentrum.perun.rpc.methods.NotificationManagerMethod;
import cz.metacentrum.perun.rpc.methods.OwnersManagerMethod;
import cz.metacentrum.perun.rpc.methods.RTMessagesManagerMethod;
import cz.metacentrum.perun.rpc.methods.RegistrarManagerMethod;
import cz.metacentrum.perun.rpc.methods.ResourcesManagerMethod;
import cz.metacentrum.perun.rpc.methods.SearcherMethod;
import cz.metacentrum.perun.rpc.methods.ServicesManagerMethod;
import cz.metacentrum.perun.rpc.methods.TasksManagerMethod;
import cz.metacentrum.perun.rpc.methods.UsersManagerMethod;
import cz.metacentrum.perun.rpc.methods.VosManagerMethod;

public enum PerunManager {

  vosManager {
    @Override
    public ManagerMethod getMethod(String methodName) {
      return VosManagerMethod.valueOf(methodName);
    }
  }, rtMessagesManager {
    @Override
    public ManagerMethod getMethod(String methodName) {
      return RTMessagesManagerMethod.valueOf(methodName);
    }
  }, searcher {
    @Override
    public ManagerMethod getMethod(String methodName) {
      return SearcherMethod.valueOf(methodName);
    }
  }, membersManager {
    @Override
    public ManagerMethod getMethod(String methodName) {
      return MembersManagerMethod.valueOf(methodName);
    }
  }, groupsManager {
    @Override
    public ManagerMethod getMethod(String methodName) {
      return GroupsManagerMethod.valueOf(methodName);
    }
  }, usersManager {
    @Override
    public ManagerMethod getMethod(String methodName) {
      return UsersManagerMethod.valueOf(methodName);
    }
  }, attributesManager {
    @Override
    public ManagerMethod getMethod(String methodName) {
      return AttributesManagerMethod.valueOf(methodName);
    }
  }, extSourcesManager {
    @Override
    public ManagerMethod getMethod(String methodName) {
      return ExtSourcesManagerMethod.valueOf(methodName);
    }
  }, facilitiesManager {
    @Override
    public ManagerMethod getMethod(String methodName) {
      return FacilitiesManagerMethod.valueOf(methodName);
    }
  }, consentsManager {
    @Override
    public ManagerMethod getMethod(String methodName) {
      return ConsentsManagerMethod.valueOf(methodName);
    }
  }, configManager {
    @Override
    public ManagerMethod getMethod(String methodName) {
      return ConfigManagerMethod.valueOf(methodName);
    }
  }, databaseManager {
    @Override
    public ManagerMethod getMethod(String methodName) {
      return DatabaseManagerMethod.valueOf(methodName);
    }
  }, resourcesManager {
    @Override
    public ManagerMethod getMethod(String methodName) {
      return ResourcesManagerMethod.valueOf(methodName);
    }
  }, servicesManager {
    @Override
    public ManagerMethod getMethod(String methodName) {
      return ServicesManagerMethod.valueOf(methodName);
    }
  }, ownersManager {
    @Override
    public ManagerMethod getMethod(String methodName) {
      return OwnersManagerMethod.valueOf(methodName);
    }
  }, authzResolver {
    @Override
    public ManagerMethod getMethod(String methodName) {
      return AuthzResolverMethod.valueOf(methodName);
    }
  }, tasksManager {
    @Override
    public ManagerMethod getMethod(String methodName) {
      return TasksManagerMethod.valueOf(methodName);
    }
  }, cabinetManager {
    @Override
    public ManagerMethod getMethod(String methodName) {
      return CabinetManagerMethod.valueOf(methodName);
    }
  }, auditMessagesManager {
    @Override
    public ManagerMethod getMethod(String methodName) {
      return AuditMessagesManagerMethod.valueOf(methodName);
    }
  }, registrarManager {
    @Override
    public ManagerMethod getMethod(String methodName) {
      return RegistrarManagerMethod.valueOf(methodName);
    }
  }, invitationsManager {
    @Override
    public ManagerMethod getMethod(String methodName) {
      return InvitationsManagerMethod.valueOf(methodName);
    }
  },

  integrationManager {
    @Override
    protected ManagerMethod getMethod(String methodName) {
      return IntegrationManagerMethod.valueOf(methodName);
    }
  },

  notificationManager {
    @Override
    public ManagerMethod getMethod(String methodName) {
      return NotificationManagerMethod.valueOf(methodName);
    }
  };

  public static Object call(String managerName, String methodName, ApiCaller ac, Deserializer parms)
      throws PerunException {
    PerunManager manager;
    try {
      manager = PerunManager.valueOf(managerName);
    } catch (IllegalArgumentException ex) {
      throw new RpcException(RpcException.Type.UNKNOWN_MANAGER, managerName);
    }
    return manager.call(methodName, ac, parms);
  }

  public Object call(String methodName, ApiCaller ac, Deserializer parms) throws PerunException {
    ManagerMethod method;
    try {
      method = getMethod(methodName);
    } catch (IllegalArgumentException ex) {
      throw new RpcException(RpcException.Type.UNKNOWN_METHOD, this.name() + "." + methodName + "()");
    }
    return method.call(ac, parms);
  }

  protected abstract ManagerMethod getMethod(String methodName);
}
