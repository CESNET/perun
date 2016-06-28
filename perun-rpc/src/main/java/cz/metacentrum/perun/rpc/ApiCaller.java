package cz.metacentrum.perun.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.oidc.OIDC;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.voot.VOOT;
import org.springframework.web.context.support.WebApplicationContextUtils;

import cz.metacentrum.perun.cabinet.api.ICabinetApi;
import cz.metacentrum.perun.controller.service.GeneralServiceManager;
import cz.metacentrum.perun.controller.service.PropagationStatsReader;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.AuditMessagesManager;
import cz.metacentrum.perun.core.api.BanOnFacility;
import cz.metacentrum.perun.core.api.BanOnResource;
import cz.metacentrum.perun.core.api.DatabaseManager;
import cz.metacentrum.perun.core.api.Destination;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.FacilitiesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.GroupsManager;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.MembersManager;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.OwnersManager;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.RTMessagesManager;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.ResourcesManager;
import cz.metacentrum.perun.core.api.Searcher;
import cz.metacentrum.perun.core.api.SecurityTeam;
import cz.metacentrum.perun.core.api.SecurityTeamsManager;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.ServicesManager;
import cz.metacentrum.perun.core.api.ServicesPackage;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.UsersManager;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.VosManager;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.notif.entities.PerunNotifObject;
import cz.metacentrum.perun.notif.entities.PerunNotifReceiver;
import cz.metacentrum.perun.notif.entities.PerunNotifRegex;
import cz.metacentrum.perun.notif.entities.PerunNotifTemplate;
import cz.metacentrum.perun.notif.entities.PerunNotifTemplateMessage;
import cz.metacentrum.perun.notif.managers.PerunNotifNotificationManager;
import cz.metacentrum.perun.registrar.RegistrarManager;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;
import cz.metacentrum.perun.taskslib.model.ExecService;

/**
 * ApiCaller calls Perun manager methods.
 *
 * @author Jan Klos <ddd@mail.muni.cz>
 * @since 0.1
 */
public class ApiCaller {

	private AuditMessagesManager auditMessagesManager = null;
	private VosManager vosManager = null;
	private MembersManager membersManager = null;
	private UsersManager usersManager = null;
	private GroupsManager groupsManager = null;
	private ExtSourcesManager extSourcesManager = null;
	private ServicesManager servicesManager = null;
	private FacilitiesManager facilitiesManager = null;
	private DatabaseManager databaseManager = null;
	private ResourcesManager resourcesManager = null;
	private AttributesManager attributesManager = null;
	private OwnersManager ownersManager = null;
	private GeneralServiceManager generalServiceManager;
	private RTMessagesManager rtMessagesManager = null;
	private SecurityTeamsManager securityTeamsManager = null;
	private PropagationStatsReader propagationStatsReader;
	private Searcher searcher = null;
	private ICabinetApi cabinetManager;
	private RegistrarManager registrarManager;
	private PerunNotifNotificationManager notificationManager;
	private VOOT vootManager = null;
	private OIDC oidcManager = null;

	private final static String RPCPRINCIPAL = "perunRpc";

	private final PerunSession session;
	private final PerunSession rpcSession;

	public AuditMessagesManager getAuditMessagesManager() {
		if (auditMessagesManager == null){
			auditMessagesManager = rpcSession.getPerun().getAuditMessagesManager();
		}
		return auditMessagesManager;
	}

	public RTMessagesManager getRTMessagesManager() {
		if (rtMessagesManager == null) {
			rtMessagesManager = rpcSession.getPerun().getRTMessagesManager();
		}
		return rtMessagesManager;
	}

	public SecurityTeamsManager getSecurityTeamsManager() {
		if (securityTeamsManager == null) {
			securityTeamsManager = rpcSession.getPerun().getSecurityTeamsManager();
		}
		return securityTeamsManager;
	}

	public Searcher getSearcher() {
		if (searcher == null) {
			searcher = rpcSession.getPerun().getSearcher();
		}
		return searcher;
	}

	public VosManager getVosManager() {
		if (vosManager == null) {
			vosManager = rpcSession.getPerun().getVosManager();
		}
		return vosManager;
	}

	public MembersManager getMembersManager() {
		if (membersManager == null) {
			membersManager = rpcSession.getPerun().getMembersManager();
		}
		return membersManager;
	}

	public UsersManager getUsersManager() {
		if (usersManager == null) {
			usersManager = rpcSession.getPerun().getUsersManager();
		}
		return usersManager;
	}

	public GroupsManager getGroupsManager() {
		if (groupsManager == null) {
			groupsManager = rpcSession.getPerun().getGroupsManager();
		}
		return groupsManager;
	}

	public ExtSourcesManager getExtSourcesManager() {
		if (extSourcesManager == null) {
			extSourcesManager = rpcSession.getPerun().getExtSourcesManager();
		}
		return extSourcesManager;
	}

	public ServicesManager getServicesManager() {
		if (servicesManager == null) {
			servicesManager = rpcSession.getPerun().getServicesManager();
		}
		return servicesManager;
	}

	public FacilitiesManager getFacilitiesManager() {
		if (facilitiesManager == null) {
			facilitiesManager = rpcSession.getPerun().getFacilitiesManager();
		}
		return facilitiesManager;
	}

	public DatabaseManager getDatabaseManager() {
		if (databaseManager == null) {
			databaseManager = rpcSession.getPerun().getDatabaseManager();
		}
		return databaseManager;
	}

	public ResourcesManager getResourcesManager() {
		if (resourcesManager == null) {
			resourcesManager = rpcSession.getPerun().getResourcesManager();
		}
		return resourcesManager;
	}

	public AttributesManager getAttributesManager() {
		if (attributesManager == null) {
			attributesManager = rpcSession.getPerun().getAttributesManager();
		}
		return attributesManager;
	}

	public OwnersManager getOwnersManager() {
		if (ownersManager == null) {
			ownersManager = rpcSession.getPerun().getOwnersManager();
		}
		return ownersManager;
	}

	public GeneralServiceManager getGeneralServiceManager() {
		return generalServiceManager;
	}

	public PropagationStatsReader getPropagationStatsReader() {
		return propagationStatsReader;
	}

	public ICabinetApi getCabinetManager() {
		return cabinetManager;
	}

	public RegistrarManager getRegistrarManager() {
		return registrarManager;
	}

	public PerunNotifNotificationManager getNotificationManager() {
		return notificationManager;
	}

	public VOOT getVOOTManager(){
		return vootManager;
	}

	public OIDC getOIDCManager(){
		return oidcManager;
	}

	public Vo getVoById(int id) throws PerunException {
		return getVosManager().getVoById(rpcSession, id);
	}

	public Member getMemberById(int id) throws PerunException {
		return getMembersManager().getMemberById(rpcSession, id);
	}

	public User getUserById(int id) throws PerunException {
		return getUsersManager().getUserById(rpcSession, id);
	}

	public Group getGroupById(int id) throws PerunException {
		return getGroupsManager().getGroupById(rpcSession, id);
	}

	public ExtSource getExtSourceById(int id) throws PerunException {
		return getExtSourcesManager().getExtSourceById(rpcSession, id);
	}

	public Service getServiceById(int id) throws PerunException {
		return getServicesManager().getServiceById(rpcSession, id);
	}

	public ServicesPackage getServicesPackageById(int id) throws PerunException {
		return getServicesManager().getServicesPackageById(rpcSession, id);
	}

	public Facility getFacilityById(int id) throws PerunException {
		return getFacilitiesManager().getFacilityById(rpcSession, id);
	}

	public Facility getFacilityByName(String name) throws PerunException {
		return getFacilitiesManager().getFacilityByName(rpcSession, name);
	}

	public Resource getResourceById(int id) throws PerunException {
		return getResourcesManager().getResourceById(rpcSession, id);
	}

	public Host getHostById(int id) throws PerunException {
		return getFacilitiesManager().getHostById(rpcSession, id);
	}

	public Owner getOwnerById(int id) throws PerunException {
		return getOwnersManager().getOwnerById(rpcSession, id);
	}

	public Application getApplicationById(int id) throws PerunException {
		return getRegistrarManager().getApplicationById(rpcSession, id);
	}

	public SecurityTeam getSecurityTeamById(int id) throws PerunException {
		return getSecurityTeamsManager().getSecurityTeamById(rpcSession, id);
	}

	public BanOnFacility getBanOnFacility(int id) throws PerunException {
		return getFacilitiesManager().getBanById(rpcSession, id);
	}

	public BanOnResource getBanOnResource(int id) throws PerunException {
		return getResourcesManager().getBanById(rpcSession, id);
	}

	public AttributeDefinition getAttributeDefinitionById(int id) throws PerunException {
		return getAttributesManager().getAttributeDefinitionById(rpcSession, id);
	}

	public Attribute getAttributeById(Facility facility, int id) throws PerunException {
		return getAttributesManager().getAttributeById(rpcSession, facility, id);
	}

	public Attribute getAttributeById(Vo vo, int id) throws PerunException {
		return getAttributesManager().getAttributeById(rpcSession, vo, id);
	}

	public Attribute getAttributeById(Resource resource, int id) throws PerunException {
		return getAttributesManager().getAttributeById(rpcSession, resource, id);
	}

	public Attribute getAttributeById(Resource resource, Member member, int id) throws PerunException {
		return getAttributesManager().getAttributeById(rpcSession, resource, member, id);
	}

	public Attribute getAttributeById(Member member, Group group, int id) throws PerunException {
		return getAttributesManager().getAttributeById(rpcSession, member, group, id);
	}

	public Attribute getAttributeById(Host host, int id) throws PerunException {
		return getAttributesManager().getAttributeById(rpcSession, host, id);
	}

	public Attribute getAttributeById(Group group, int id) throws PerunException {
		return getAttributesManager().getAttributeById(rpcSession, group, id);
	}

	public Attribute getAttributeById(Resource resource, Group group, int id) throws PerunException {
		return getAttributesManager().getAttributeById(rpcSession, resource, group, id);
	}

	public Attribute getAttributeById(User user, int id) throws PerunException {
		return getAttributesManager().getAttributeById(rpcSession, user, id);
	}

	public Attribute getAttributeById(Member member, int id) throws PerunException {
		return getAttributesManager().getAttributeById(session, member, id);
	}

	public Attribute getAttributeById(Facility facility, User user, int id) throws PerunException {
		return getAttributesManager().getAttributeById(session, facility, user, id);
	}

	public UserExtSource getUserExtSourceById(int id) throws PerunException {
		return getUsersManager().getUserExtSourceById(rpcSession, id);
	}

	public ExecService getExecServiceById(int id) throws PerunException {
		return getGeneralServiceManager().getExecService(rpcSession, id);
	}

	public PerunNotifObject getPerunNotifObjectById(int id) throws PerunException {
		return getNotificationManager().getPerunNotifObjectById(id);
	}

	public PerunNotifReceiver getPerunNotifReceiverById(int id) throws PerunException {
		return getNotificationManager().getPerunNotifReceiverById(rpcSession, id);
	}

	public PerunNotifRegex getPerunNotifRegexById(int id) throws PerunException {
		return getNotificationManager().getPerunNotifRegexById(rpcSession, id);
	}

	public PerunNotifTemplateMessage getPerunNotifTemplateMessageById(int id) throws PerunException {
		return getNotificationManager().getPerunNotifTemplateMessageById(rpcSession, id);
	}

	public PerunNotifTemplate getPerunNotifTemplateById(int id) throws PerunException {
		return getNotificationManager().getPerunNotifTemplateById(rpcSession, id);
	}

	public Destination getDestination(String destination, String type) throws PerunException {
		Destination d = new Destination();
		d.setDestination(destination);
		d.setType(type);
		return d;
	}

	public List<Object> convertGroupsWithHierarchy(Group group, Map<Group, Object> groups) {
		if (group != null) {
			List<Object> groupHierarchy = new ArrayList<Object>();
			groupHierarchy.add(0, group);

			if (groups != null) {
				for (Group subGroup: groups.keySet()) {
					groupHierarchy.add(convertGroupsWithHierarchy(subGroup, (Map<Group, Object>) groups.get(subGroup)));
				}
			}

			return groupHierarchy;
		}
		return null;
	}

	public ApiCaller(ServletContext context, PerunPrincipal perunPrincipal, PerunClient client) throws InternalErrorException {
		Perun perun = WebApplicationContextUtils.getWebApplicationContext(context).getBean("perun", Perun.class);

		PerunPrincipal rpcPrincipal = new PerunPrincipal(RPCPRINCIPAL, ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
		this.rpcSession = perun.getPerunSession(rpcPrincipal, new PerunClient());

		// Initialize serviceManager
		this.generalServiceManager = WebApplicationContextUtils.getWebApplicationContext(context).getBean("generalServiceManager", GeneralServiceManager.class);

		// Initialize PropagationStatsReader
		this.propagationStatsReader = WebApplicationContextUtils.getWebApplicationContext(context).getBean("propagationStatsReader", PropagationStatsReader.class);

		// Initialize ICabinetApi (cabinet manager)
		this.cabinetManager = WebApplicationContextUtils.getWebApplicationContext(context).getBean("cabinetApi", ICabinetApi.class);

		// Initialize RegistrarManager
		this.registrarManager = WebApplicationContextUtils.getWebApplicationContext(context).getBean("registrarManager", RegistrarManager.class);

		// Initialize Notifications
		this.notificationManager = WebApplicationContextUtils.getWebApplicationContext(context).getBean("perunNotifNotificationManager", PerunNotifNotificationManager.class);

		// Initialize VOOT Manager
		this.vootManager = new VOOT();

		// Initialize OIDC Manager
		this.oidcManager = new OIDC();

		this.session = perun.getPerunSession(perunPrincipal, client);
	}

	public PerunSession getSession() {
		return session;
	}

	private boolean stateChanging = true;

	public boolean isStateChanging() {
		return stateChanging;
	}

	public void setStateChanging(boolean stateChanging) {
		this.stateChanging = stateChanging;
	}

	public void stateChangingCheck() throws RpcException {
		if (!stateChanging) {
			throw new RpcException(RpcException.Type.STATE_CHANGING_CALL);
		}
	}

	public Object call(String managerName, String methodName, Deserializer parms) throws PerunException {
		return PerunManager.call(managerName, methodName, this, parms);
	}
}
