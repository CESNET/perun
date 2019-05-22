package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetAttributes;
import cz.metacentrum.perun.webgui.json.attributesManager.RemoveAttributes;
import cz.metacentrum.perun.webgui.json.attributesManager.SetAttribute;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Resource;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Inner tab item for shell change
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ShellChangeTabItem implements TabItem {

	private PerunWebSession session = PerunWebSession.getInstance();

	private Label titleWidget = new Label("Select preferred shell");
	private SimplePanel contentWidget = new SimplePanel();

	private Resource resource;
	private Attribute a;
	private int userId;
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Changing shell request
	 *
	 * @param resource
	 * @param userId
	 * @param a Attribute to be changed
	 * @param refreshEvents
	 */
	public ShellChangeTabItem(Resource resource, int userId, Attribute a, JsonCallbackEvents refreshEvents){
		this.resource = resource;
		this.userId = userId;
		this.a = a;
		this.events = refreshEvents;
	}

	public boolean isPrepared(){
		return (userId != 0 && resource != null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		VerticalPanel vp = new VerticalPanel();

		final FlexTable ft = new FlexTable();
		ft.setWidth("350px");
		ft.setStyleName("inputFormFlexTable");
		ft.setHTML(0, 0, "Available shells:");
		ft.getFlexCellFormatter().setStyleName(0, 0, "itemName");
		final ListBox shells = new ListBox();
		shells.setWidth("200px");
		ft.setWidget(0, 1, shells);
		vp.add(ft);
		final CustomButton selectShellButton = TabMenu.getPredefinedButton(ButtonType.SAVE, "Save preferred shell");

		// callback for available shells
		GetAttributes attrs = new GetAttributes(new JsonCallbackEvents(){
			@Override
			public void onError(PerunError error) {
				shells.clear();
				shells.addItem("Error while loading");
			}
		@Override
		public void onFinished(JavaScriptObject jso) {
			shells.clear();
			ArrayList<Attribute> list = JsonUtils.jsoAsList(jso);
			if (list.isEmpty() || list == null) {
				shells.addItem("No shells available");
				return;
			}
			// fill shells
			for (Attribute a : list) {
				if (a.getFriendlyName().equalsIgnoreCase("shells")) {
					for (int i=0; i<a.getValueAsJsArray().length(); i++) {
						// fill shell values
						shells.addItem(a.getValueAsJsArray().get(i));
					}
					break;
				}
			}
			// set selected
			for (int i=0; i<shells.getItemCount() ; i++) {
				if (shells.getValue(i).equals(a.getValue())) {
					shells.setSelectedIndex(i);
					break;
				}
			}
			if (shells.getValue(shells.getSelectedIndex()).equals(a.getValue())) {
				selectShellButton.setEnabled(false);
			} else {
				selectShellButton.setEnabled(true);
			}
		}
		@Override
		public void onLoadingStart() {
			shells.clear();
			shells.addItem("Loading...");
		}
		});

		final TabItem tab = this;

		TabMenu menu = new TabMenu();
		vp.add(menu);
		vp.setCellHorizontalAlignment(menu, HasHorizontalAlignment.ALIGN_RIGHT);

		selectShellButton.setEnabled(false);
		selectShellButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				// OK click button
				// set new value
				a.setValue(shells.getValue(shells.getSelectedIndex()));
				// send request
				Map<String, Integer> ids = new HashMap<String, Integer>();
				ids.put("user", userId);
				ids.put("facility", resource.getFacilityId());
				SetAttribute request = new SetAttribute(JsonCallbackEvents.disableButtonEvents(selectShellButton, new JsonCallbackEvents() {
					@Override
					public void onFinished(JavaScriptObject jso) {
						// refresh only what's necessary
						events.onFinished(jso);
						// don't refresh underlaying tab
						session.getTabManager().closeTab(tab, isRefreshParentOnClose());
					}
				}));
				request.setAttribute(ids, a);
			}
		});
		attrs.getResourceAttributes(resource.getId());
		attrs.retrieveData();

		shells.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent changeEvent) {
				if (shells.getValue(shells.getSelectedIndex()).equals(a.getValue())) {
					selectShellButton.setEnabled(false);
				} else {
					selectShellButton.setEnabled(true);
				}
			}
		});

		final CustomButton defaultButton = new CustomButton("Use default", "", SmallIcons.INSTANCE.lightningIcon());
		defaultButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Map<String, Integer> ids = new HashMap<String, Integer>();
				ids.put("user", userId);
				ids.put("facility", resource.getFacilityId());
				RemoveAttributes request = new RemoveAttributes(JsonCallbackEvents.disableButtonEvents(defaultButton, new JsonCallbackEvents() {
					@Override
					public void onFinished(JavaScriptObject jso) {
						// refresh only what's necessary
						events.onFinished(jso);
						// don't refresh underlaying tab
						session.getTabManager().closeTab(tab, isRefreshParentOnClose());
					}
				}));
				ArrayList<Attribute> list = new ArrayList<Attribute>();
				list.add(a);
				request.removeAttributes(ids, list);
			}
		});

		menu.addWidget(selectShellButton);
		menu.addWidget(defaultButton);
		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, isRefreshParentOnClose());
			}
		}));

		contentWidget.setWidget(vp);
		return getWidget();

	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.settingToolsIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1277;
		int result = 404;
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

		ShellChangeTabItem oth = (ShellChangeTabItem) obj;

		if (oth.userId != this.userId)
			return false;

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {}

	public boolean isAuthorized() {
		return session.isSelf(userId);
	}

}
