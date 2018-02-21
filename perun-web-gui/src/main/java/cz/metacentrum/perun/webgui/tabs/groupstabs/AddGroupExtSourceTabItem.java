package cz.metacentrum.perun.webgui.tabs.groupstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.PerunSearchEvent;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.extSourcesManager.AddExtSource;
import cz.metacentrum.perun.webgui.json.extSourcesManager.GetExtSources;
import cz.metacentrum.perun.webgui.json.extSourcesManager.GetGroupExtSources;
import cz.metacentrum.perun.webgui.json.extSourcesManager.GetVoExtSources;
import cz.metacentrum.perun.webgui.model.ExtSource;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Provides page with add external source to Group form
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class AddGroupExtSourceTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Add Group ext source");

	//data
	private int groupId;
	private Group group;

	private ArrayList<ExtSource> alreadyAddedList = new ArrayList<ExtSource>();
	private SimplePanel alreadyAdded = new SimplePanel();

	/**
	 * Creates a tab instance
	 *
	 * @param voId ID of Vo to have ext source added
	 */
	public AddGroupExtSourceTabItem(int groupId){
		this.groupId = groupId;
		JsonCallbackEvents events = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				group = jso.cast();
			}
		};
		new GetEntityById(PerunEntity.GROUP, groupId, events).retrieveData();
	}


	/**
	 * Creates a tab instance
	 *
	 * @param group Group to have ext source added
	 */
	public AddGroupExtSourceTabItem(Group group){
		this.groupId = group.getId();
		this.group = group;
	}


	public boolean isPrepared(){
		return !(group == null);
	}

	public Widget draw() {

		titleWidget.setText("Add external source");

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// menu
		TabMenu menu = new TabMenu();
		menu.addWidget(new HTML(""));

		final GetVoExtSources extSources = new GetVoExtSources(group.getVoId());

		// remove already assigned ext sources from offering
		JsonCallbackEvents localEvents = new JsonCallbackEvents() {
			@Override
			public void onFinished(JavaScriptObject jso){
				// second callback
				final GetGroupExtSources alreadyAssigned = new GetGroupExtSources(groupId, new JsonCallbackEvents() {
					public void onFinished(JavaScriptObject jso){
						JsArray<ExtSource> esToRemove = JsonUtils.jsoAsArray(jso);
						for (int i=0; i<esToRemove.length(); i++) {
							extSources.removeFromTable(esToRemove.get(i));
						}
					}
				});
				alreadyAssigned.retrieveData();
			}
		};
		extSources.setEvents(localEvents);

		final ExtendedSuggestBox box = new ExtendedSuggestBox(extSources.getOracle());

		// button
		final CustomButton assignButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addSelectedExtSource());
		final TabItem tab = this;

		assignButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final ArrayList<ExtSource> extSourcesToAdd = extSources.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(extSourcesToAdd)) {
					// FIXME - Should have only one callback to core
					for (int i=0; i<extSourcesToAdd.size(); i++ ) {
						final int n = i;
						AddExtSource request = new AddExtSource(JsonCallbackEvents.disableButtonEvents(assignButton, new JsonCallbackEvents(){
							@Override
							public void onFinished(JavaScriptObject jso) {
								// unselect added person
								extSources.getSelectionModel().setSelected(extSourcesToAdd.get(n), false);
								alreadyAddedList.add(extSourcesToAdd.get(n));
								rebuildAlreadyAddedWidget();
								// clear search
								box.getSuggestBox().setText("");
							}
						}));
						request.addGroupExtSource(groupId, extSourcesToAdd.get(i).getId());
					}
				}
			}
		});

		menu.addFilterWidget(box, new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				extSources.filterTable(text);
			}
		}, "Filter by ext source name or type");

		menu.addWidget(assignButton);

		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CLOSE, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, !alreadyAddedList.isEmpty());
			}
		}));

		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		vp.add(alreadyAdded);

		CellTable<ExtSource> table = extSources.getTable();

		assignButton.setEnabled(false);
		JsonUtils.addTableManagedButton(extSources, table, assignButton);

		table.addStyleName("perun-table");
		table.setWidth("100%");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");
		vp.add(sp);

		// do not use resizePerunTable() when tab is in overlay - wrong width is calculated
		session.getUiElements().resizeSmallTabPanel(sp, 350, this);

		this.contentWidget.setWidget(vp);

		return getWidget();
	}

	/**
	 * Rebuild already added widget based on already added ext sources
	 */
	private void rebuildAlreadyAddedWidget() {

		alreadyAdded.setStyleName("alreadyAdded");
		alreadyAdded.setVisible(!alreadyAddedList.isEmpty());
		alreadyAdded.setWidget(new HTML("<strong>Already added: </strong>"));
		for (int i=0; i<alreadyAddedList.size(); i++) {
			alreadyAdded.getWidget().getElement().setInnerHTML(alreadyAdded.getWidget().getElement().getInnerHTML()+ ((i!=0) ? ", " : "") + SafeHtmlUtils.fromString(alreadyAddedList.get(i).getName()).asString());
		}
	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return  SmallIcons.INSTANCE.addIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1289;
		int result = 1;
		result = prime * result * 135;
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

		AddGroupExtSourceTabItem other = (AddGroupExtSourceTabItem) obj;

		return (other.groupId == this.groupId);
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.GROUP_ADMIN);
		if(group != null){
			session.setActiveGroup(group);
			return;
		}
		session.setActiveGroupId(groupId);
	}


	public boolean isAuthorized() {

		if (session.isVoAdmin(group.getVoId())) {
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

	public String getUrlWithParameters() {
		return GroupsTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + groupId;
	}

	static public AddGroupExtSourceTabItem load(Map<String, String> parameters) {
		int voId = Integer.parseInt(parameters.get("id"));
		return new AddGroupExtSourceTabItem(voId);
	}

}
