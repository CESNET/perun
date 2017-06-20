package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetAttributesV2;
import cz.metacentrum.perun.webgui.json.attributesManager.RemoveAttributes;
import cz.metacentrum.perun.webgui.json.attributesManager.SetAttributes;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.UserExtSource;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.UsersTabs;
import cz.metacentrum.perun.webgui.tabs.attributestabs.SetNewAttributeTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.HashMap;
import java.util.Map;

/**
 * Page with Users UserExtSource settings for Perun Admin
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class UserExtSourceSettingsTabItem implements TabItem, TabItemWithUrl {

	/**
	 * Perun web session
	 */
	private PerunWebSession session = PerunWebSession.getInstance();

	/**
	 * Content widget - should be simple panel
	 */
	private SimplePanel contentWidget = new SimplePanel();

	/**
	 * Title widget
	 */
	private Label titleWidget = new Label("UserExtSource detail");

	private int userExtSourceId = 0;
	private UserExtSource userExtSource;

	/**
	 * Creates a tab instance
	 */
	public UserExtSourceSettingsTabItem(int userExtSourceId) {
		this.userExtSourceId = userExtSourceId;
		new GetEntityById(PerunEntity.USER_EXT_SOURCE, userExtSourceId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				userExtSource = jso.cast();
			}
		}).retrieveData();
	}

	/**
	 * Creates a tab instance
	 */
	public UserExtSourceSettingsTabItem(UserExtSource ues) {
		this.userExtSourceId = ues.getId();
		this.userExtSource = ues;
	}

	public boolean isPrepared(){
		return !(userExtSource == null);
	}

	public Widget draw() {

		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(userExtSource.getLogin().trim()));

		// MAIN TAB PANEL
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// MENU
		TabMenu menu = new TabMenu();
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		final GetAttributesV2 callback = new GetAttributesV2(true);
		callback.getUserExtSourceAttributes(userExtSource.getId());
		final CellTable<Attribute> table = callback.getEmptyTable();

		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");
		session.getUiElements().resizePerunTable(sp, 350, this);

		menu.addWidget(UiElements.getRefreshButton(this));

		// save changes in attributes
		final CustomButton saveChangesButton = TabMenu.getPredefinedButton(ButtonType.SAVE, ButtonTranslation.INSTANCE.saveChangesInAttributes());
		saveChangesButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (UiElements.cantSaveEmptyListDialogBox(callback.getTableSelectedList())) {
					SetAttributes request = new SetAttributes(JsonCallbackEvents.disableButtonEvents(saveChangesButton, new JsonCallbackEvents() {
						public void onFinished(JavaScriptObject jso) {
							callback.clearTable();
							callback.getUserExtSourceAttributes(userExtSourceId);
							callback.retrieveData();
						}
					}));
					final Map<String, Integer> ids = new HashMap<String, Integer>();
					ids.put("userExtSource", userExtSourceId);
					request.setAttributes(ids, callback.getTableSelectedList());
				}
			}
		});

		menu.addWidget(saveChangesButton);

		// buttons
		CustomButton setNewMemberAttributeButton = TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.setNewAttributes(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				Map<String, Integer> ids = new HashMap<String,Integer>();
				ids.put("userExtSource", userExtSource.getId());
				session.getTabManager().addTabToCurrentTab(new SetNewAttributeTabItem(ids), true);
			}
		});
		menu.addWidget(setNewMemberAttributeButton);

		// REMOVE ATTRIBUTES BUTTON
		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeAttributes());
		removeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				// if selected
				if (UiElements.cantSaveEmptyListDialogBox(callback.getTableSelectedList())) {

					Map<String, Integer> ids = new HashMap<String,Integer>();
					ids.put("userExtSource", userExtSource.getId());

					RemoveAttributes request = new RemoveAttributes(JsonCallbackEvents.disableButtonEvents(removeButton, new JsonCallbackEvents(){
						public void onFinished(JavaScriptObject jso) {
							callback.clearTable();
							callback.getUserExtSourceAttributes(userExtSource.getId());
							callback.retrieveData();
						}
					}));

					// make removeAttributes call
					request.removeAttributes(ids, callback.getTableSelectedList());

				}}});

		removeButton.setEnabled(false);
		JsonUtils.addTableManagedButton(callback, table, removeButton);
		menu.addWidget(removeButton);

		callback.retrieveData();

		// ATTRIBUTES TABLE
		vp.add(sp);
		vp.setCellHeight(sp, "100%");

		this.contentWidget.setWidget(vp);

		return getWidget();
	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.worldIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 5303;
		int result = 432;
		result = prime * result;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserExtSourceSettingsTabItem other = (UserExtSourceSettingsTabItem) obj;
		if (userExtSourceId != other.userExtSourceId)
			return false;

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.PERUN_ADMIN, true);
		session.getUiElements().getBreadcrumbs().setLocation(MainMenu.PERUN_ADMIN, "Users", UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl(), "UserExtSource settings", getUrlWithParameters());
	}

	public boolean isAuthorized() {

		if (session.isPerunAdmin()) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "user-ext-src-settings";

	public String getUrl() {
		return URL;
	}

	public String getUrlWithParameters() {
		return UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + userExtSourceId;
	}

	static public UserExtSourceSettingsTabItem load(Map<String, String> parameters) {
		int uesId = 0;
		if (parameters.containsKey("id")) {
			uesId = Integer.parseInt(parameters.get("id"));
		}
		return new UserExtSourceSettingsTabItem(uesId);
	}

}
