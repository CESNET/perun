package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.extSourcesManager.GetExtSources;
import cz.metacentrum.perun.webgui.json.usersManager.AddUserExtSource;
import cz.metacentrum.perun.webgui.model.ExtSource;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.UsersTabs;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Provides page for adding external identity to use (user ext source)
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AddUserExtSourceTabItem implements TabItem, TabItemWithUrl {

	/**
	 * User id
	 */
	private int userId;
	/**
	 * User
	 */
	private User user;

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
	private Label titleWidget = new Label("Loading user");
	private boolean callDone = false;

	/**
	 * Creates a new instance
	 *
	 * @param userId
	 */
	public AddUserExtSourceTabItem(int userId){
		this.userId = userId;
		new GetEntityById(PerunEntity.USER, userId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				user = jso.cast();
			}
		}).retrieveData();
	}

	/**
	 * Creates a new instance
	 *
	 * @param user
	 */
	public AddUserExtSourceTabItem(User user){
		this.userId = user.getId();
		this.user = user;
	}

	public boolean isPrepared(){
		return !(user == null);
	}

	public Widget draw() {

		titleWidget.setText("Add ext. identity");

		VerticalPanel vp = new VerticalPanel();

		// get available ext sources
		final ListBoxWithObjects<ExtSource> extSourcesDropDown = new ListBoxWithObjects<ExtSource>();

		final TextBox externalLogin = new TextBox();
		final TextBox loaTextBox = new TextBox();
		final CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, "Add external identity to user");

		// fill listbox
		JsonCallbackEvents fillEvent = new JsonCallbackEvents(){
			@Override
			public void onError(PerunError error) {
				extSourcesDropDown.clear();
				extSourcesDropDown.addItem("Error while loading");
				callDone = false;
			}
			@Override
			public void onFinished(JavaScriptObject jso) {
				extSourcesDropDown.clear();
				ArrayList<ExtSource> list = JsonUtils.jsoAsList(jso);
				list = new TableSorter<ExtSource>().sortByName(list);
				if (list == null || list.isEmpty()) {
					extSourcesDropDown.addItem("No external sources available");
					return;
				}
				for (ExtSource ex : list) {
					extSourcesDropDown.addItem(ex);
				}
				callDone = true;
				if (!externalLogin.getText().isEmpty() && !extSourcesDropDown.isEmpty() && JsonUtils.checkParseInt(loaTextBox.getText()) && callDone) {
					addButton.setEnabled(true);
				}
			}
			@Override
			public void onLoadingStart() {
				extSourcesDropDown.clear();
				extSourcesDropDown.addItem("Loading...");
				callDone = false;
			}
		};

		// callback
		final GetExtSources extSources = new GetExtSources(fillEvent);
		extSources.retrieveData();

		// create layout
		FlexTable layout = new FlexTable();
		layout.setStyleName("inputFormFlexTable");

		layout.setHTML(0, 0, "External login:");
		layout.setWidget(0, 1, externalLogin);
		layout.setHTML(1, 0, "External source:");
		layout.setWidget(1, 1, extSourcesDropDown);
		layout.setHTML(2, 0, "Level of Assurance:");
		layout.setWidget(2, 1, loaTextBox);

		FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();
		for (int i=0; i<layout.getRowCount(); i++) {
			cellFormatter.setStyleName(i, 0, "itemName");
		}

		cellFormatter.setStyleName(3, 1, "inputFormInlineComment");
		layout.setHTML(3, 1, "0 - not verified = default</br>1 - verified email</br>2 - verified identity</br>3 - verified identity, strict password strength");

		TabMenu menu = new TabMenu();

		// close tab events
		final JsonCallbackEvents addExtSrcEvents = JsonCallbackEvents.closeTabDisableButtonEvents(addButton, this);
		addButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				ExtSource selected = extSourcesDropDown.getObjectAt(extSourcesDropDown.getSelectedIndex());
				String login = externalLogin.getText();
				AddUserExtSource request = new AddUserExtSource(addExtSrcEvents);
				int loa = 0;

				if (JsonUtils.checkParseInt(loaTextBox.getText())) {
					loa = Integer.parseInt(loaTextBox.getText());
				} else {
					JsonUtils.cantParseIntConfirm("Level of Assurance", loaTextBox.getText());
					return;
				}
				request.addUserExtSource(userId, login.trim(), selected, loa);

			}
		});
		addButton.setEnabled(false);
		menu.addWidget(addButton);

		final TabItem tab = this;
		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				session.getTabManager().closeTab(tab);
			}
		}));

		KeyUpHandler handler = new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (!externalLogin.getText().isEmpty() && !extSourcesDropDown.isEmpty() && JsonUtils.checkParseInt(loaTextBox.getText()) && callDone) {
					addButton.setEnabled(true);
				} else {
					addButton.setEnabled(false);
				}
			}
		};
		externalLogin.addKeyUpHandler(handler);
		loaTextBox.addKeyUpHandler(handler);

		vp.add(layout);
		vp.add(menu);
		vp.setCellHorizontalAlignment(menu, HasHorizontalAlignment.ALIGN_RIGHT);

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
		return SmallIcons.INSTANCE.addIcon();
	}


	@Override
	public int hashCode() {
		final int prime = 1181;
		int result = 1;
		result = prime * result + userId;
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
		AddUserExtSourceTabItem other = (AddUserExtSourceTabItem) obj;
		if (userId != other.userId)
			return false;

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open()
	{

	}

	public boolean isAuthorized() {

		// TODO consider more privileges ??
		if (session.isPerunAdmin()) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "add-ext-src";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		return UsersTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + userId;
	}

	static public AddUserExtSourceTabItem load(Map<String, String> parameters)
	{
		int uid = Integer.parseInt(parameters.get("id"));
		return new AddUserExtSourceTabItem(uid);
	}

}
