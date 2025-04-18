package cz.metacentrum.perun.webgui.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayInteger;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.TabManager;
import cz.metacentrum.perun.webgui.tabs.facilitiestabs.FacilityDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.groupstabs.GroupDetailTabItem;
import cz.metacentrum.perun.webgui.tabs.vostabs.VoDetailTabItem;

import java.util.ArrayList;

/**
 * Class for session of the Web GUI.
 * Session is passed to almost all other classes in constructor.
 * <p>
 * Contains main Client classes like UiElements, WebGui, MainMenu.
 * Also stores user's information returned from RPC during login.
 * Provides authorization by keeping list of editable entities for user.
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class PerunWebSession {

  // User roles constants
  static public final String PERUN_ADMIN_PRINCIPAL_ROLE = "PERUNADMIN";
  static public final String GROUP_ADMIN_PRINCIPAL_ROLE = "GROUPADMIN";
  static public final String VO_ADMIN_PRINCIPAL_ROLE = "VOADMIN";
  static public final String FACILITY_ADMIN_PRINCIPAL_ROLE = "FACILITYADMIN";
  static public final String USER_ROLE = "SELF";
  static public final String VO_OBSERVER_PRINCIPAL_ROLE = "VOOBSERVER";
  static public final String VO_CREATOR_PRINCIPAL_ROLE = "VOCREATOR";
  // Only instance
  static private PerunWebSession INSTANCE;
  // Main GUI classes
  private UiElements uiElements;
  private TabManager tabManager;
  private WebGui webGui;
  // User's authz data
  private PerunPrincipal perunPrincipal = null; // contain all users auth returned from RPC
  //Keepers of user's roles
  private boolean perunAdmin = false;
  private boolean groupAdmin = false;
  private boolean voAdmin = false;
  private boolean facilityAdmin = false;
  private boolean voObserver = false; // is not vo admin
  private boolean self = false; // is not admin
  private boolean sponsor = false; // sponsor
  private boolean voCreator = false;
  // Entities which can the user edit
  private ArrayList<Integer> editableGroups = new ArrayList<Integer>();
  private ArrayList<Integer> editableVos = new ArrayList<Integer>();
  private ArrayList<Integer> editableFacilities = new ArrayList<Integer>();
  private ArrayList<Integer> editableUsers = new ArrayList<Integer>();
  private ArrayList<Integer> editableSecTeams = new ArrayList<Integer>();
  private ArrayList<Integer> editableSponsoredUsers = new ArrayList<Integer>();
  // entities which user can view (Observer role)
  private ArrayList<Integer> viewableVos = new ArrayList<Integer>();
  // Currently active entities - user is editing them now
  private VirtualOrganization activeVo;
  private Group activeGroup;
  private Facility activeFacility;
  private User activeUser;
  // History of entities which user edited
  private ArrayList<GeneralObject> entitiesHistoryList = new ArrayList<GeneralObject>();
  private BasicOverlayType configuration;
  private String newGuiAlert;
  // RPC URL
  private String rpcUrl = "";

  /**
   * Creates new instance of the Session
   */
  private PerunWebSession() {
  }

  /**
   * Returns the instance of PerunWebSession
   */

  static public PerunWebSession getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new PerunWebSession();
    }
    return INSTANCE;
  }

  /**
   * Returns the URL of the RPC
   *
   * @return URL
   */
  public String getRpcUrl() {

    if (!rpcUrl.isEmpty()) {
      return rpcUrl;
    }

    String rpcType = getRpcServer();
    if (rpcType == null) {
      UiElements.generateAlert("Unable to find Perun server", "Path to Perun server can't be determined, you" +
          "probably used wrong URL.");
    }

    String modifier = PerunWebConstants.INSTANCE.perunRpcUrlModifier();
    if (modifier == null || modifier.equalsIgnoreCase("@gui.url.modifier@")) {
      rpcUrl = "/" + rpcType + "/rpc/jsonp/";
    } else {
      rpcUrl = "/" + rpcType + "/rpc" + modifier + "/jsonp/";
    }

    return rpcUrl;

  }

  /**
   * Returns RPC type determined as first part of path in current page URL.
   *
   * @return RPC type
   */
  public native String getRpcServer() /*-{
		return $wnd.RPC_SERVER;
	}-*/;

  /**
   * Returns version of RPC
   *
   * @return RPC versions
   */
  public native String getRpcVersion() /*-{
		return $wnd.RPC_VERSION;
	}-*/;

  /**
   * Returns the UI elements
   *
   * @return return class which contains UI elements - menus,log,pages,tabs
   */
  public UiElements getUiElements() {
    return this.uiElements;
  }

  /**
   * Sets the UI elements handler class.
   * Use only once during loading GUI!
   *
   * @param uiElements handler for session
   */
  public void setUiElements(UiElements uiElements) {
    this.uiElements = uiElements;
  }

  /**
   * Returns TabManager for handling tabs
   *
   * @return TabManager
   */
  public TabManager getTabManager() {
    return this.tabManager;
  }

  /**
   * Sets tab manager for handling all GUI tabs.
   * Use only once during loading GUI!
   *
   * @param tabManager the tabManager to set
   */
  public void setTabManager(TabManager tabManager) {
    this.tabManager = tabManager;
  }

  /**
   * Return WebGui class (GUI entry point)
   *
   * @return webGui GUI entry point
   */
  public WebGui getWebGui() {
    return webGui;
  }

  /**
   * Sets WebGUI class (GUI entry point)
   * Call only once during loading GUI!
   *
   * @param webGui WebGui entry point to set
   */
  public void setWebGui(WebGui webGui) {
    this.webGui = webGui;
  }

  /**
   * Returns current PerunPrincipal of user logged to RPC
   *
   * @return PerunPrincipal
   */
  public PerunPrincipal getPerunPrincipal() {
    return this.perunPrincipal;
  }

  /**
   * Sets PerunPrincipal for this session
   *
   * @param pp PerunPrincipal received from RPC login
   */
  public void setPerunPrincipal(PerunPrincipal pp) {
    this.perunPrincipal = pp;
  }

  /**
   * Returns User object from PerunPrincipal
   * of person currently logged to RPC.
   *
   * @return User which is logged to RPC
   */
  public User getUser() {
    return perunPrincipal.getUser();
  }

  /**
   * True if the user is perun admin
   *
   * @return true if perun admin
   */
  public boolean isPerunAdmin() {
    return this.perunAdmin;
  }

  /**
   * True if the user is VO admin.
   * TRUE for PerunAdmin too.
   *
   * @return true if VO admin
   */
  public boolean isVoAdmin() {

    if (this.perunAdmin) {
      return this.perunAdmin;
    }
    return this.voAdmin;
  }

  /**
   * True if the user is vo admin of a specified VO.
   * TRUE for PerunAdmin too.
   *
   * @param id ID of VO to check admin status for
   * @return true if user is VO's admin
   */
  public boolean isVoAdmin(int id) {
    if (this.perunAdmin) {
      return this.perunAdmin;
    } else if (this.voAdmin) {
      return editableVos.contains(id);
    }
    return false;
  }

  /**
   * True if the user is VO observer.
   * TRUE for PerunAdmin too.
   *
   * @return true if VO observer
   */
  public boolean isVoObserver() {

    if (this.perunAdmin) {
      return this.perunAdmin;
    }
    return this.voObserver;
  }

  /**
   * True if the user is vo observer of a specified VO.
   * TRUE for PerunAdmin too.
   *
   * @param id ID of VO to check observer status for
   * @return true if user is VO's observer
   */
  public boolean isVoObserver(int id) {
    if (this.perunAdmin) {
      return this.perunAdmin;
    } else if (this.voObserver) {
      return viewableVos.contains(id);
    }
    return false;
  }

  /**
   * True if the user is VO creator.
   * TRUE for PerunAdmin too.
   *
   * @return true if VO creator
   */
  public boolean isVoCreator() {
    if (this.perunAdmin) {
      return this.perunAdmin;
    }
    return this.voCreator;
  }

  /**
   * True if the user is group admin.
   * TRUE for PerunAdmin too.
   *
   * @return true if group admin
   */
  public boolean isGroupAdmin() {
    if (this.perunAdmin) {
      return this.perunAdmin;
    }
    return this.groupAdmin;
  }

  /**
   * True if the user is group admin of a specified group.
   * TRUE for PerunAdmin too.
   *
   * @param id ID of group to check admin status for
   * @return true if user is Group's admin
   */
  public boolean isGroupAdmin(int id) {
    if (this.perunAdmin) {
      return this.perunAdmin;
    } else if (this.groupAdmin) {
      return editableGroups.contains(id);
    }
    return false;
  }

  /**
   * True if the user is facility admin.
   * TRUE for PerunAdmin too.
   *
   * @return true if facility admin
   */
  public boolean isFacilityAdmin() {
    if (this.perunAdmin) {
      return this.perunAdmin;
    }
    return this.facilityAdmin;
  }

  /**
   * True if the user is facility admin of a specified Facility.
   * TRUE for PerunAdmin too.
   *
   * @param id ID of Facility to check admin status for
   * @return true if user is Facility admin
   */
  public boolean isFacilityAdmin(int id) {
    if (this.perunAdmin) {
      return this.perunAdmin;
    } else if (this.facilityAdmin) {
      return editableFacilities.contains(id);
    }
    return false;
  }

  /**
   * True if the user is Sponsor.
   * TRUE for PerunAdmin too.
   *
   * @return true if Sponsor
   */
  public boolean isSponsor() {

    if (this.perunAdmin) {
      return this.perunAdmin;
    }
    return this.sponsor;
  }

  /**
   * True if the user is sponsor of a specified user.
   * TRUE for PerunAdmin too.
   *
   * @param id ID of sponsored user to check sponsorship status for
   * @return true if user is Users sponsor
   */
  public boolean isSponsor(int id) {
    if (this.perunAdmin) {
      return this.perunAdmin;
    } else if (this.sponsor) {
      return editableSponsoredUsers.contains(id);
    }
    return false;
  }

  /**
   * True if the user is also user.
   * TRUE for PerunAdmin too.
   *
   * @return true if also self
   */
  public boolean isSelf() {
    if (this.perunAdmin) {
      return this.perunAdmin;
    }
    return this.self;
  }

  /**
   * True if the passed userID is same
   * as userId stored in SELF role.
   * TRUE for PerunAdmin too.
   *
   * @return true if self / false otherwise
   */
  public boolean isSelf(int id) {
    if (this.perunAdmin) {
      return this.perunAdmin;
    } else if (this.self) {
      return editableUsers.contains(id);
    }
    return false;
  }

  /**
   * Add a VO, which user can edit
   *
   * @param voId VO, which can user edit
   */
  public void addEditableVo(int voId) {
    if (!this.editableVos.contains(voId)) {
      this.editableVos.add(voId);
    }
  }

  /**
   * Add a VO, which user can view (for VO observer role)
   *
   * @param voId VO, which can user view
   */
  public void addViewableVo(int voId) {
    if (!this.viewableVos.contains(voId)) {
      this.viewableVos.add(voId);
    }
  }

  /**
   * Add a group, which user can edit
   *
   * @param groupId group which can user edit
   */
  public void addEditableGroup(int groupId) {
    if (!this.editableGroups.contains(groupId)) {
      this.editableGroups.add(groupId);
    }
  }

  /**
   * Add a Facility, which user can edit
   *
   * @param facilityId Facility, which can user edit
   */
  public void addEditableFacility(int facilityId) {
    if (!this.editableFacilities.contains(facilityId)) {
      this.editableFacilities.add(facilityId);
    }
  }

  /**
   * Add a SponsoredUser, which user can edit
   *
   * @param sponsoredUser SponsoredUser, which can user edit
   */
  public void addEditableSponsoredUsers(int sponsoredUser) {
    if (!this.editableSponsoredUsers.contains(sponsoredUser)) {
      this.editableSponsoredUsers.add(sponsoredUser);
    }
  }

  /**
   * Add a User, which user can edit
   *
   * @param userId User, which can user edit
   */
  public void addEditableUser(int userId) {
    if (!this.editableUsers.contains(userId)) {
      this.editableUsers.add(userId);
    }
  }

  /**
   * Return list of editable groups IDs
   *
   * @return groups
   */
  public ArrayList<Integer> getEditableGroups() {
    return editableGroups;
  }

  /**
   * Return list of editable vos IDs
   *
   * @return vos
   */
  public ArrayList<Integer> getEditableVos() {
    return editableVos;
  }

  /**
   * Return list of viewable vos IDs
   *
   * @return vos
   */
  public ArrayList<Integer> getViewableVos() {
    return viewableVos;
  }

  /**
   * Return list of editable facilities IDs
   *
   * @return facilities
   */
  public ArrayList<Integer> getEditableFacilities() {
    return editableFacilities;
  }

  /**
   * Return list of editable sponsored users IDs
   *
   * @return sponsored users
   */
  public ArrayList<Integer> getEditableSponsoredUsers() {
    return editableSponsoredUsers;
  }


  /**
   * Return list of editable users IDs
   *
   * @return users
   */
  public ArrayList<Integer> getEditableUsers() {
    return editableUsers;
  }

  /**
   * Returns VO, which user currently edits
   *
   * @return VO
   */
  public VirtualOrganization getActiveVo() {
    return activeVo;
  }

  /**
   * Sets currently active VO (refresh links in menu)
   *
   * @param vo VO which user is editing now
   */
  public void setActiveVo(VirtualOrganization vo) {
    this.activeVo = vo;
    addObjectToEntitiesHistory(vo.cast());
    // change default from list to active vo
    getUiElements().getMenu().setMenuTabItem(MainMenu.VO_ADMIN, new VoDetailTabItem(vo));
    // vo must refresh both
    getUiElements().getMenu().updateLinks(MainMenu.VO_ADMIN);
    getUiElements().getMenu().updateLinks(MainMenu.GROUP_ADMIN);
  }

  /**
   * Returns Group, which user currently edits
   *
   * @return Group
   */
  public Group getActiveGroup() {
    return activeGroup;
  }

  /**
   * Sets currently active Group (refresh links in menu)
   *
   * @param group Group which user is editing now
   */
  public void setActiveGroup(Group group) {
    this.activeGroup = group;
    addObjectToEntitiesHistory(group.cast());
    getUiElements().getMenu().setMenuTabItem(MainMenu.GROUP_ADMIN, new GroupDetailTabItem(group));
    getUiElements().getMenu().updateLinks(MainMenu.GROUP_ADMIN);
  }

  /**
   * Returns Facility, which user currently edits
   *
   * @return Facility
   */
  public Facility getActiveFacility() {
    return activeFacility;
  }

  /**
   * Sets currently active Facility (refresh links in menu)
   *
   * @param facility Facility which user is editing now
   */
  public void setActiveFacility(Facility facility) {
    this.activeFacility = facility;
    addObjectToEntitiesHistory(facility.cast());
    getUiElements().getMenu().setMenuTabItem(MainMenu.FACILITY_ADMIN, new FacilityDetailTabItem(facility));
    getUiElements().getMenu().updateLinks(MainMenu.FACILITY_ADMIN);
  }

  /**
   * Returns User, which user currently edits
   *
   * @return User
   */
  public User getActiveUser() {
    if (activeUser == null) {
      return getUser();
    }
    return activeUser;
  }

  /**
   * Sets currently active User (SELF role) (refresh links in menu)
   *
   * @param user User which user is editing now
   */
  public void setActiveUser(User user) {
    this.activeUser = user;
    addObjectToEntitiesHistory(user.cast());
    // default is ok when user menu
    getUiElements().getMenu().updateLinks(MainMenu.USER);
  }

  /**
   * Adds GeneralObject which user edited last time (now)
   * into entities history.
   *
   * @param go generalObject
   */
  private void addObjectToEntitiesHistory(GeneralObject go) {
    // try remove old
    for (int i = 0; i < entitiesHistoryList.size(); i++) {
      if (go.getId() == entitiesHistoryList.get(i).getId() &&
          go.getObjectType().equals(entitiesHistoryList.get(i).getObjectType())) {
        entitiesHistoryList.remove(i);
        break;
      }
    }
    entitiesHistoryList.add(go);
    //uiElements.getBreadcrumbs().update();
  }

  /**
   * Adds object (VO,Group,Facility) which user edited last time (now)
   * into entities history.
   *
   * @param jso any JS object
   */
  private void addObjectToEntitiesHistory(JavaScriptObject jso) {
    GeneralObject go = jso.cast();
    addObjectToEntitiesHistory(go);
  }

  /**
   * Return list of entities which user edited
   *
   * @return list of edited entities (as GeneralObjects)
   */
  public ArrayList<GeneralObject> getEntitiesHistoryList() {
    return this.entitiesHistoryList;
  }

  /**
   * Sets currently active VO (refresh links in menu)
   * when only ID is provided.
   *
   * @param voId ID of VO which user is editing now
   */
  public void setActiveVoId(final int voId) {
    new GetEntityById(PerunEntity.VIRTUAL_ORGANIZATION, voId, new JsonCallbackEvents() {
      public void onFinished(JavaScriptObject jso) {
        VirtualOrganization vo = jso.cast();
        setActiveVo(vo);
      }
    }).retrieveData();
  }

  /**
   * Sets currently active Group (refresh links in menu)
   * when only ID is provided.
   *
   * @param groupId ID of groupwhich user is editing now
   */
  public void setActiveGroupId(int groupId) {
    new GetEntityById(PerunEntity.GROUP, groupId, new JsonCallbackEvents() {
      public void onFinished(JavaScriptObject jso) {
        Group group = jso.cast();
        setActiveGroup(group);
      }
    }).retrieveData();
  }

  /**
   * Sets currently active Facility (refresh links in menu)
   * when only ID is provided.
   *
   * @param facilityId ID of facility which user is editing now
   */
  public void setActiveFacilityId(int facilityId) {
    new GetEntityById(PerunEntity.FACILITY, facilityId, new JsonCallbackEvents() {
      public void onFinished(JavaScriptObject jso) {
        Facility f = jso.cast();
        setActiveFacility(f);
      }
    }).retrieveData();
  }

  /**
   * Sets user's roles and editable entities received from RPC
   * within PerunPrincipal into Session
   * <p>
   * Call only once when loading GUI!
   *
   * @param roles roles returned from PerunPrincipal
   */
  public void setRoles(Roles roles) {

    this.perunAdmin = roles.hasRole(PERUN_ADMIN_PRINCIPAL_ROLE);
    this.voAdmin = roles.hasRole(VO_ADMIN_PRINCIPAL_ROLE);
    this.facilityAdmin = roles.hasRole(FACILITY_ADMIN_PRINCIPAL_ROLE);
    this.groupAdmin = roles.hasRole(GROUP_ADMIN_PRINCIPAL_ROLE);
    this.self = roles.hasRole(USER_ROLE);
    this.voObserver = roles.hasRole(VO_OBSERVER_PRINCIPAL_ROLE);
    this.voCreator = roles.hasRole(VO_CREATOR_PRINCIPAL_ROLE);

    JsArrayInteger array = roles.getEditableEntities("VOADMIN", "Vo");
    for (int i = 0; i < array.length(); i++) {
      addEditableVo(array.get(i));
    }
    JsArrayInteger array2 = roles.getEditableEntities("SELF", "User");
    for (int i = 0; i < array2.length(); i++) {
      addEditableUser(array2.get(i));
    }
    JsArrayInteger array3 = roles.getEditableEntities("FACILITYADMIN", "Facility");
    for (int i = 0; i < array3.length(); i++) {
      addEditableFacility(array3.get(i));
    }
    JsArrayInteger array4 = roles.getEditableEntities("GROUPADMIN", "Group");
    for (int i = 0; i < array4.length(); i++) {
      addEditableGroup(array4.get(i));
    }
    JsArrayInteger array5 = roles.getEditableEntities("VOOBSERVER", "Vo");
    for (int i = 0; i < array5.length(); i++) {
      addViewableVo(array5.get(i));
    }
    JsArrayInteger array7 = roles.getEditableEntities("SPONSOR", "SponsoredUser");
    for (int i = 0; i < array7.length(); i++) {
      addEditableSponsoredUsers(array7.get(i));
    }

  }

  /**
   * Return string with authz information
   * <p>
   * eg. voadmin=21,41,35;groupadmin=23,45,78 etc.
   *
   * @return string with authz info
   */
  public String getRolesString() {

    String result = "";

    if (perunAdmin) {
      result += "PerunAdmin; ";
    }
    if (self) {
      result += "Self=" + editableUsers;
    }
    if (voAdmin) {
      result += "; VoManager=" + editableVos;
    }
    if (voObserver) {
      result += "; VoObserver=" + viewableVos;
    }
    if (groupAdmin) {
      result += "; GroupManager=" + editableGroups;
    }
    if (facilityAdmin) {
      result += "; FacilityManager=" + editableFacilities;
    }
    if (sponsor) {
      result += "; Sponsor=" + editableSponsoredUsers;
    }

    return result;

  }

  public BasicOverlayType getConfiguration() {
    return configuration;
  }

  public void setConfiguration(BasicOverlayType configuration) {
    this.configuration = configuration;
  }

  public String getNewGuiAlert() {
    return newGuiAlert;
  }

  public void setNewGuiAlert(String newGuiAlert) {
    this.newGuiAlert = newGuiAlert;
  }
}
