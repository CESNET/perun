package cz.metacentrum.perun.webgui.tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.model.GeneralObject;
import cz.metacentrum.perun.webgui.model.SecurityTeam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for parsion URL and loading proper tabs
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class UrlMapper {

	private PerunWebSession session = PerunWebSession.getInstance();

	static public final String TAB_SEPARATOR = ";";
	static public final String TAB_NAME_SEPARATOR = "/";
	static private boolean parsingRunning = false;

	/**
	 * UrlMapper instance
	 */
	public UrlMapper() {}

	/**
	 * Parses URL, and calls the ***Tabs classes.
	 *
	 * URL EXAMPLE:
	 * vo-admin/detail?vo=123&dummy=blabla;perun-admin/users?searchString=Vaclav;group-admin/members
	 *
	 * @param url
	 */
	public void parseUrl(String url) {
		// disable multiple parsing
		if(parsingRunning) return;

		parsingRunning = true;
		session.getUiElements().setLogText("Parsing url:" + url);

		String[] parts = url.split(TAB_SEPARATOR);

		// if url leads to tabs without any "active", add it to first of them
		if (parts.length >= 1 && !url.contains("active=1")) {

			if (parts[0].contains("?")) {
				parts[0] = parts[0]+"&active=1";
			} else {
				parts[0] = parts[0]+"?active=1";
			}

		}

		for(String tabString : parts) {
			parseTab(tabString);
		}
		parsingRunning = false;

		// reloads the menu
		session.getUiElements().getMenu().updateLinks();
	}


	/**
	 * Called
	 */
	private boolean parseTab(String tabString) {

		String[] parts = tabString.split("\\?", 2);

		String[] packageSlashName = parts[0].split("/", 2);
		if (packageSlashName.length != 2) {
			return false;
		}
		if(packageSlashName[0] == null || packageSlashName[1] == null) {
			return false;
		}

		String tabPackage = packageSlashName[0];
		String tabName = packageSlashName[1];

		Map<String, String> parameters;
		if(parts.length == 2) {
			parameters = parseParameters(parts[1], "&");
		} else {
			parameters = new HashMap<String, String>();
		}

		// try to open tab
		try {
			if (getAndOpenTab(tabPackage, tabName, parameters)) {
				return true;
			}
		} catch (RuntimeException e) {
			UiElements.generateAlert("URL parsing error", "This tab couldn't be opened: " + tabPackage + " / " + tabName + ". ");
		}

		session.getUiElements().setLogText("No entry exists for: " + tabPackage + " | " + tabName);

		return false;
	}

	private boolean getAndOpenTab(String tabPackage, String tabName, Map<String, String> parameters) {

		// homepage, info
		if(tabPackage.equals(OtherTabs.URL)) {
			OtherTabs tabs = new OtherTabs();
			tabs.loadTab(tabName, parameters);
		}

		// VO
		if(tabPackage.equals(VosTabs.URL)) {
			VosTabs tabs = new VosTabs();
			tabs.loadTab(tabName, parameters);
			return true;
		}

		// Group
		if(tabPackage.equals(GroupsTabs.URL)) {
			GroupsTabs tabs = new GroupsTabs();
			tabs.loadTab(tabName, parameters);
			return true;
		}

		// Facilities
		if(tabPackage.equals(FacilitiesTabs.URL)) {
			FacilitiesTabs tabs = new FacilitiesTabs();
			tabs.loadTab(tabName, parameters);
			return true;
		}

		// Perun
		if(tabPackage.equals(PerunAdminTabs.URL)) {
			PerunAdminTabs tabs = new PerunAdminTabs();
			tabs.loadTab(tabName, parameters);
			return true;
		}

		// Users
		if(tabPackage.equals(UsersTabs.URL)) {
			UsersTabs tabs = new UsersTabs();
			tabs.loadTab(tabName, parameters);
			return true;
		}

		// Cabinet
		if(tabPackage.equals(CabinetTabs.URL)) {
			CabinetTabs tabs = new CabinetTabs();
			tabs.loadTab(tabName, parameters);
			return true;
		}

		// Attributes
		if(tabPackage.equals(AttributesTabs.URL)) {
			AttributesTabs tabs = new AttributesTabs();
			tabs.loadTab(tabName, parameters);
			return true;
		}

		// Services
		if(tabPackage.equals(ServicesTabs.URL)) {
			ServicesTabs tabs = new ServicesTabs();
			tabs.loadTab(tabName, parameters);
			return true;
		}

		// Resources
		if(tabPackage.equals(ResourcesTabs.URL)) {
			ResourcesTabs tabs = new ResourcesTabs();
			tabs.loadTab(tabName, parameters);
			return true;
		}

		// member
		if(tabPackage.equals(MembersTabs.URL)) {
			MembersTabs tabs = new MembersTabs();
			tabs.loadTab(tabName, parameters);
			return true;
		}

		// registrar
		if(tabPackage.equals(RegistrarTabs.URL)) {
			RegistrarTabs tabs = new RegistrarTabs();
			tabs.loadTab(tabName, parameters);
			return true;
		}

		// security
		if(tabPackage.equals(SecurityTabs.URL)) {
			SecurityTabs tabs = new SecurityTabs();
			tabs.loadTab(tabName, parameters);
			return true;
		}

		// test
		if(tabPackage.equals(TestTabs.URL)) {
			TestTabs tabs = new TestTabs();
			tabs.loadTab(tabName, parameters);
			return true;
		}

		return false;
	}


	/**
	 * Parses the parameters
	 *
	 * @param parameters String with parameters to parse
	 * @param character Character, which splits the multiple parameters - usually &
	 * @return
	 */
	private Map<String, String> parseParameters(String parameters, String character){
		Map<String, String> paramMap = new HashMap<String, String>();

		if(parameters == null){
			return paramMap;
		}

		String[] pairs = parameters.split(character);
		for(int i = 0; i < pairs.length; i++)
		{
			String[] keyValue = pairs[i].split("=", 2);
			if(keyValue.length == 2){
				paramMap.put(keyValue[0], keyValue[1]);
			}
		}

		return paramMap;
	}

	/**
	 * Parses a list of objects from URL parameters.
	 * The returned list is NOT COMPLETE immediately after the method finishes,
	 * it is still loading the entities.
	 * The list's length must be equal to the number returned by method:
	 * "parseListLengthFromUrl"
	 *
	 *
	 * @param paramName Parameter name
	 * @param entity Entity which load from IDs
	 * @param parameters
	 * @return
	 */
	static public <T extends JavaScriptObject> ArrayList<T> parseListFromUrl(String paramName, PerunEntity entity, Map<String, String> parameters){

		ArrayList<T> list = new ArrayList<T>();

		int i = 0;
		String param = paramName + "[" + i + "]";
		while(parameters.containsKey(param)){

			String idStr = parameters.get(param);
			int id = Integer.parseInt(idStr);

			addEntityToList(entity, id, i, list);

			i++;
			param = paramName + "[" + i + "]";
		}

		return list;
	}

	/**
	 * Returns the number of entities in URL.
	 *
	 *
	 * @param paramName
	 * @param parameters
	 * @return
	 */
	static public int parseListLengthFromUrl(String paramName, Map<String, String> parameters){
		int i = 0;
		String param = paramName + "[" + i + "]";
		while(parameters.containsKey(param)){
			i++;
			param = paramName + "[" + i + "]";
		}
		return i;
	}

	/**
	 * Private helper method
	 * @param entity
	 * @param id
	 * @param i
	 * @param list
	 */
	static private <T extends JavaScriptObject> void addEntityToList(PerunEntity entity, int id, final int i, final ArrayList<T> list) {

		JsonCallbackEvents events = new JsonCallbackEvents(){
			@SuppressWarnings("unchecked")
			public void onFinished(JavaScriptObject jso){
				JavaScriptObject obj = jso.cast();
				list.add(i, (T) obj.cast());
			}

		};

		switch (entity) {
			case USER:
				new GetEntityById(PerunEntity.USER, id, events).retrieveData();
				break;

			case VIRTUAL_ORGANIZATION:
				new GetEntityById(PerunEntity.VIRTUAL_ORGANIZATION, id, events).retrieveData();
				break;

			case GROUP:
				new GetEntityById(PerunEntity.GROUP, id, events).retrieveData();
				break;

			case MEMBER:
				new GetEntityById(PerunEntity.MEMBER, id, events).retrieveData();
				break;

			case FACILITY:
				new GetEntityById(PerunEntity.FACILITY, id, events).retrieveData();
				break;

			case RESOURCE:
				new GetEntityById(PerunEntity.RESOURCE, id, events).retrieveData();
				break;

			case SERVICE:
				new GetEntityById(PerunEntity.SERVICE, id, events).retrieveData();
				break;

			default:
				throw new RuntimeException("This entity is not supported.");
		}

	}

	/**
	 * Returns a URL from the list of perun entities
	 *
	 * @param paramName
	 * @param list
	 * @return
	 */
	static public <T extends JavaScriptObject> String getUrlFromList(String paramName, ArrayList<T> list) {
		String url = "";
		int i = 0;
		for(T obj : list) {
			GeneralObject go = obj.cast();
			url += paramName + "[" + i + "]=" + go.getId() + "&";
			i++;
		}
		return url.substring(0, url.length() - 1);
	}

}
