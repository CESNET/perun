package cz.metacentrum.perun.webgui.json;

import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.model.PerunError;

import java.util.ArrayList;

/**
 * Unified callback class to get any PerunEntity by it's ID with optional cache support
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetEntityById implements JsonCallback, JsonCallbackWithCache {

	// SESSION
	private PerunWebSession session = PerunWebSession.getInstance();

	// PARAMS
	private JsonCallbackEvents events = new JsonCallbackEvents();
	private int entityId;
	private PerunEntity entity;
	private boolean cached = false;

	// URLs
	static private final String URL_MEMBER = "membersManager/getMemberById";
	static private final String URL_RICH_MEMBER = "membersManager/getRichMember";
	static private final String URL_RICH_MEMBER_WITH_ATTRS = "membersManager/getRichMemberWithAttributes";

	static private final String URL_VO = "vosManager/getVoById";
	static private final String URL_FACILITY = "facilitiesManager/getFacilityById";
	static private final String URL_GROUP = "groupsManager/getGroupById";
	static private final String URL_GROUP_PARENT = "groupsManager/getParentGroup";
	static private final String URL_RICH_GROUP = "groupsManager/getRichGroupByIdWithAttributesByNames";

	static private final String URL_RESOURCE = "resourcesManager/getResourceById";
	static private final String URL_RICH_RESOURCE = "resourcesManager/getRichResourceById";
	static private final String URL_PUBLICATION = "cabinetManager/findPublicationById";
	static private final String URL_SERVICE = "servicesManager/getServiceById";

	static private final String URL_USER = "usersManager/getUserById";
	static private final String URL_RICH_USER = "usersManager/getRichUser";
	static private final String URL_RICH_USER_WITH_ATTRS = "usersManager/getRichUserWithAttributes";

	static private final String URL_TASK = "propagationStatsReader/getTaskById";
	static private final String URL_APP_MAIL = "registrarManager/getApplicationMailById";
	static private final String URL_APP = "registrarManager/getApplicationById";

	static private final String URL_SECURITY_TEAM = "securityTeamsManager/getSecurityTeamById";
	static private final String URL_USER_EXT_SRC = "usersManager/getUserExtSourceById";

	/**
	 * New callback instance
	 *
	 * @param entity Perun entity to get
	 * @param entityId entity's ID
	 */
	public GetEntityById(PerunEntity entity, int entityId) {
		this.entity = entity;
		this.entityId = entityId;
	}

	/**
	 * New callback instance
	 *
	 * @param entity Perun entity to get
	 * @param entityId entity's ID
	 * @param events custom events for callback
	 */
	public GetEntityById(PerunEntity entity, int entityId, JsonCallbackEvents events) {
		this.entity = entity;
		this.entityId = entityId;
		this.events = events;
	}

	@Override
	public void retrieveData() {

		String param = "id=" + this.entityId;
		JsonClient js = new JsonClient();
		js.setCacheEnabled(cached);

		if (PerunEntity.MEMBER.equals(entity)) {
			js.retrieveData(URL_MEMBER, param, this);
		} else if (PerunEntity.RICH_MEMBER.equals(entity)) {
			js.retrieveData(URL_RICH_MEMBER, param, this);
		} else if (PerunEntity.RICH_MEMBER_WITH_ATTRS.equals(entity)) {
			js.retrieveData(URL_RICH_MEMBER_WITH_ATTRS, param, this);
		} else if (PerunEntity.VIRTUAL_ORGANIZATION.equals(entity)) {
			js.retrieveData(URL_VO, param, this);
		} else if (PerunEntity.FACILITY.equals(entity)) {
			js.retrieveData(URL_FACILITY, param, this);
		} else if (PerunEntity.GROUP.equals(entity)) {
			js.retrieveData(URL_GROUP, param, this);
		} else if (PerunEntity.GROUP_PARENT.equals(entity)) {
			param = "group="+entityId;
			js.retrieveData(URL_GROUP_PARENT, param, this);
		} else if (PerunEntity.RICH_GROUP.equals(entity)) {
			param = "groupId="+entityId;
			ArrayList<String> attrNames = new ArrayList<>();
			attrNames.add("urn:perun:group:attribute-def:def:synchronizationEnabled");
			attrNames.add("urn:perun:group:attribute-def:def:synchronizationInterval");
			attrNames.add("urn:perun:group:attribute-def:def:lastSynchronizationState");
			attrNames.add("urn:perun:group:attribute-def:def:lastSynchronizationTimestamp");
			attrNames.add("urn:perun:group:attribute-def:def:lastSuccessSynchronizationTimestamp");
			attrNames.add("urn:perun:group:attribute-def:def:authoritativeGroup");
			for (String value : attrNames) {
				param += "&attrNames[]="+value;
			}
			js.retrieveData(URL_RICH_GROUP, param, this);
		} else if (PerunEntity.PUBLICATION.equals(entity)) {
			js.retrieveData(URL_PUBLICATION, param, this);
		} else if (PerunEntity.RESOURCE.equals(entity)) {
			js.retrieveData(URL_RESOURCE, param, this);
		} else if (PerunEntity.RICH_RESOURCE.equals(entity)) {
			js.retrieveData(URL_RICH_RESOURCE, param, this);
		} else if (PerunEntity.USER.equals(entity)) {
			js.retrieveData(URL_USER, param, this);
		} else if (PerunEntity.RICH_USER.equals(entity)) {
			param = "user="+entityId;
			js.retrieveData(URL_RICH_USER, param, this);
		} else if (PerunEntity.RICH_USER_WITH_ATTRS.equals(entity)) {
			param = "user="+entityId;
			js.retrieveData(URL_RICH_USER_WITH_ATTRS, param, this);
		} else if (PerunEntity.SERVICE.equals(entity)) {
			js.retrieveData(URL_SERVICE, param, this);
		} else if (PerunEntity.TASK.equals(entity)) {
			js.retrieveData(URL_TASK, param, this);
		} else if (PerunEntity.APPLICATION_MAIL.equals(entity)) {
			js.retrieveData(URL_APP_MAIL, param, this);
		} else if (PerunEntity.APPLICATION.equals(entity)) {
			js.retrieveData(URL_APP, param, this);
		} else if (PerunEntity.SECURITY_TEAM.equals(entity)) {
			js.retrieveData(URL_SECURITY_TEAM, param, this);
		} else if (PerunEntity.USER_EXT_SOURCE.equals(entity)) {
			param = "userExtSource="+entityId;
			js.retrieveData(URL_USER_EXT_SRC, param, this);
		} else {
			// UNSUPPORTED COMBINATION
		}

	}

	@Override
	public void onFinished(JavaScriptObject jso) {
		session.getUiElements().setLogText("Loading "+entity+": " + this.entityId+" finished.");
		events.onFinished(jso);
	}

	@Override
	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading "+entity+": " + this.entityId);
		events.onError(error);
	}

	@Override
	public void onLoadingStart() {
		events.onLoadingStart();
	}

	@Override
	public boolean isCacheEnabled() {
		return cached;
	}

	@Override
	public void setCacheEnabled(boolean cache) {
		this.cached = cache;
	}

}
