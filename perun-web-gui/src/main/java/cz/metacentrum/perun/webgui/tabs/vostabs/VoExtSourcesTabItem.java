package cz.metacentrum.perun.webgui.tabs.vostabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.extSourcesManager.GetVoExtSources;
import cz.metacentrum.perun.webgui.json.extSourcesManager.RemoveExtSource;
import cz.metacentrum.perun.webgui.model.ExtSource;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.VosTabs;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * VO ext. sources management page
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class VoExtSourcesTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Loading vo ext sources");

	// data
	private VirtualOrganization vo;
	private int voId;

	/**
	 * Creates a tab instance
	 *
	 * @param vo
	 */
	public VoExtSourcesTabItem(VirtualOrganization vo){
		this.vo = vo;
		this.voId = vo.getId();
	}

	/**
	 * Creates a tab instance
	 *
	 * @param voId
	 */
	public VoExtSourcesTabItem(int voId){
		this.voId = voId;
		JsonCallbackEvents events = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				vo = jso.cast();
			}
		};
		new GetEntityById(PerunEntity.VIRTUAL_ORGANIZATION, voId, events).retrieveData();
	}

	public boolean isPrepared(){
		return !(vo == null);
	}

	public Widget draw() {

		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(vo.getName())+": "+"ext sources");

		// main panel
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// HORIZONTAL MENU
		TabMenu menu = new TabMenu();
		// refresh
		menu.addWidget(UiElements.getRefreshButton(this));

		// get VO resources
		final GetVoExtSources extSources = new GetVoExtSources(voId);

		// refresh table event
		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(extSources);

		// create ext source button
		CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.addExtSource(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new AddVoExtSourceTabItem(voId), true);
			}
		});
		menu.addWidget(addButton);

		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeExtSource());
		removeButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				final ArrayList<ExtSource> extSourcesToRemove = extSources.getTableSelectedList();
				String text = "Following external sources will be removed from VO. You won't be able to import members from them anymore.";
				UiElements.showDeleteConfirm(extSourcesToRemove, text, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
						for (int i=0; i<extSourcesToRemove.size(); i++) {
							RemoveExtSource request;
							if (i == extSourcesToRemove.size()-1) {
								request = new RemoveExtSource(JsonCallbackEvents.disableButtonEvents(removeButton, events));
							} else {
								request = new RemoveExtSource(JsonCallbackEvents.disableButtonEvents(removeButton));
							}
							request.removeVoExtSource(voId, extSourcesToRemove.get(i).getId());
						}
					}
				});
			}
		});
		menu.addWidget(removeButton);

		// authorization - enable buttons for perun admin only.
		if (!session.isPerunAdmin()) {
			addButton.setEnabled(false);
			removeButton.setEnabled(false);
			extSources.setCheckable(false);
		}

		menu.addFilterWidget(new ExtendedSuggestBox(extSources.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				extSources.filterTable(text);
			}
		}, "Filter external sources by name or type");

		// add menu to the main panel
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		CellTable<ExtSource> table = extSources.getTable();

		if (session.isPerunAdmin()) {
			removeButton.setEnabled(false);
			JsonUtils.addTableManagedButton(extSources, table, removeButton);
		}

		table.addStyleName("perun-table");
		table.setWidth("100%");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		vp.add(sp);

		session.getUiElements().resizePerunTable(sp, 350, this);

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
		final int prime = 1601;
		int result = 1;
		result = prime * result + voId;
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
		VoExtSourcesTabItem other = (VoExtSourcesTabItem) obj;
		if (voId != other.voId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.VO_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(vo, "External sources", getUrlWithParameters());
		if(vo != null){
			session.setActiveVo(vo);
			return;
		}
		session.setActiveVoId(voId);
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(voId) || session.isVoObserver(voId)) {
			return true;
		} else {
			return false;
		}
	}

	public final static String URL = "ext-sources";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return VosTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + voId;
	}

	static public VoExtSourcesTabItem load(Map<String, String> parameters) {
		int voId = Integer.parseInt(parameters.get("id"));
		return new VoExtSourcesTabItem(voId);
	}

}
