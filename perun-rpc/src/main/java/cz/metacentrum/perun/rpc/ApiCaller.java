package cz.metacentrum.perun.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import cz.metacentrum.perun.cabinet.api.CabinetManager;
import cz.metacentrum.perun.cabinet.model.Category;
import cz.metacentrum.perun.cabinet.model.Publication;
import cz.metacentrum.perun.cabinet.model.PublicationSystem;
import cz.metacentrum.perun.cabinet.model.Thanks;
import cz.metacentrum.perun.core.api.PerunClient;
import cz.metacentrum.perun.core.api.TasksManager;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.voot.VOOT;
import org.springframework.web.context.support.WebApplicationContextUtils;
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
import cz.metacentrum.perun.scim.SCIM;

/**
 * ApiCaller calls Perun manager methods.
 *
 * @author Jan Klos <ddd@mail.muni.cz>
 * @author Pavel Zlámal <zlamal@cesnet.cz>
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
	private RTMessagesManager rtMessagesManager = null;
	private SecurityTeamsManager securityTeamsManager = null;
	private TasksManager tasksManager = null;
	private Searcher searcher = null;
	private CabinetManager cabinetManager;
	private RegistrarManager registrarManager;
	private PerunNotifNotificationManager notificationManager;
	private VOOT vootManager = null;
	private SCIM scimManager = null;

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

	public TasksManager getTasksManager() {
		if (tasksManager == null) {
			tasksManager = rpcSession.getPerun().getTasksManager();
		}
		return tasksManager;
	}

	public OwnersManager getOwnersManager() {
		if (ownersManager == null) {
			ownersManager = rpcSession.getPerun().getOwnersManager();
		}
		return ownersManager;
	}

	public CabinetManager getCabinetManager() {
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

	public SCIM getSCIMManager(){
		return scimManager;
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

	public ExtSource getExtSourceByName(String extSourceName) throws PerunException {
		return getExtSourcesManager().getExtSourceByName(rpcSession, extSourceName);
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

	public Owner getOwnerByName(String name) throws PerunException {
		return getOwnersManager().getOwnerByName(rpcSession, name);
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
		return getAttributesManager().getAttributeById(rpcSession, member, resource, id);
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

	public Attribute getAttributeById(UserExtSource ues, int id) throws PerunException {
		return getAttributesManager().getAttributeById(session, ues, id);
	}

	public UserExtSource getUserExtSourceById(int id) throws PerunException {
		return getUsersManager().getUserExtSourceById(rpcSession, id);
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

	public Destination getDestination(String destination, String type, String propagationType) throws PerunException {
		Destination d = new Destination();
		d.setDestination(destination);
		d.setType(type);
		d.setPropagationType(propagationType);
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

	public Category getCategoryById(int id) throws PerunException {
		return getCabinetManager().getCategoryById(id);
	}

	public Thanks getThanksById(int id) throws PerunException {
		return getCabinetManager().getThanksById(id);
	}

	public Publication getPublicationById(int id) throws PerunException {
		return getCabinetManager().getPublicationById(id);
	}

	public PublicationSystem getPublicationSystemById(int id) throws PerunException {
		return getCabinetManager().getPublicationSystemById(id);
	}

	public ApiCaller(ServletContext context, PerunPrincipal perunPrincipal, PerunClient client) {
		Perun perun = WebApplicationContextUtils.getWebApplicationContext(context).getBean("perun", Perun.class);

		PerunPrincipal rpcPrincipal = new PerunPrincipal(RPCPRINCIPAL, ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
		this.rpcSession = perun.getPerunSession(rpcPrincipal, new PerunClient());

		// Initialize CabinetManager
		this.cabinetManager = WebApplicationContextUtils.getWebApplicationContext(context).getBean("cabinetManager", CabinetManager.class);

		// Initialize RegistrarManager
		this.registrarManager = WebApplicationContextUtils.getWebApplicationContext(context).getBean("registrarManager", RegistrarManager.class);

		// Initialize Notifications
		this.notificationManager = WebApplicationContextUtils.getWebApplicationContext(context).getBean("perunNotifNotificationManager", PerunNotifNotificationManager.class);

		// Initialize VOOT Manager
		this.vootManager = new VOOT();

		// Initialize SCIM Manager
		this.scimManager = new SCIM();

		this.session = perun.getPerunSession(perunPrincipal, client);
	}

	public PerunSession getSession() {
		return session;
	}

	public Object call(String managerName, String methodName, Deserializer parms) throws PerunException {
		return PerunManager.call(managerName, methodName, this, parms);
	}

	/**
	 * Returns a view of the portion of this list between the specified fromIndex, inclusive, and toIndex, inclusive.
	 *
	 * If list of objects is null or empty, it will return empty list as sublist.
	 * If fromIndex is lower than 0, it will be set to 0.
	 * If toIndex is bigger than size of an input array, it will be set to the size of an array.
	 * If fromIndex and toIndex are same, it will return array with exactly 1 object on this position.
	 * If fromIndex is bigger than toIndex, it will return empty array.
	 *
	 * @param listOfObjects original list of objects from which we will get the sublist view by indexes
	 * @param fromIndex index of the object from original list, which will be the first object in the sublist view (included)
	 * @param toIndex index of the object from original list, which will be the last object in the sublist view (included)
	 *
	 * @return a view of the portion of the listOfObjects between fromIndex and toIndex (both inclusive), for example
	 * if fromIndex=0 and toIndex=10 it will return view on first 11 objects of the input list if they exist. If
	 * there are only 6 objects, it will return view on all these objects.
	 */
	public <E> List<E> getSublist(List<E> listOfObjects, int fromIndex, int toIndex) {
		//if list if empty or null, return empty list of objects (there is no sublist to be find)
		if(listOfObjects == null || listOfObjects.isEmpty()) return new ArrayList<>();

		//toIndex should be included in the subList, so we will work with toIndex+1
		toIndex = toIndex + 1;

		//From index can't be lower than 0
		if(fromIndex<0) fromIndex = 0;

		//To index can't be bigger than size of an array
		if(toIndex>listOfObjects.size()) toIndex = listOfObjects.size();

		//To index can't be lower than from index
		if(toIndex<fromIndex) toIndex = fromIndex;

		return listOfObjects.subList(fromIndex, toIndex);

	}
}
