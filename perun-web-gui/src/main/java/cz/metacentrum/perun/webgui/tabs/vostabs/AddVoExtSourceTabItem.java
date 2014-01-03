package cz.metacentrum.perun.webgui.tabs.vostabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
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
import cz.metacentrum.perun.webgui.json.extSourcesManager.AddExtSource;
import cz.metacentrum.perun.webgui.json.extSourcesManager.GetExtSources;
import cz.metacentrum.perun.webgui.json.extSourcesManager.GetVoExtSources;
import cz.metacentrum.perun.webgui.model.ExtSource;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.VosTabs;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Provides page with add external source to VO form
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @version $Id$
 */
public class AddVoExtSourceTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Add VO ext source");

	//data
	private int voId;

	private VirtualOrganization vo;

	/**
	 * Creates a tab instance
	 *
     * @param voId ID of Vo to have ext source added
     */
	public AddVoExtSourceTabItem(int voId){
		this.voId = voId;
		JsonCallbackEvents events = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				vo = jso.cast();
			}
		};
        new GetEntityById(PerunEntity.VIRTUAL_ORGANIZATION, voId, events).retrieveData();
	}
	
	
	/**
	 * Creates a tab instance
	 *
     * @param vo VO to have ext source added
     */
	public AddVoExtSourceTabItem(VirtualOrganization vo){
		this.voId = vo.getId();
		this.vo = vo; 
	}
	
	
	public boolean isPrepared(){
		return !(vo == null);
	}
	
	public Widget draw() {
		
		titleWidget.setText(Utils.getStrippedStringWithEllipsis(vo.getName())+": add ext. source");

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// menu
		TabMenu menu = new TabMenu();

		final GetExtSources extSources = new GetExtSources();

		// remove already assigned ext sources from offering
		JsonCallbackEvents localEvents = new JsonCallbackEvents() {
			public void onFinished(JavaScriptObject jso){
				// second callback
				final GetVoExtSources alreadyAssigned = new GetVoExtSources(voId, new JsonCallbackEvents() {
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

		// button
		CustomButton assignButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addSelectedExtSource());
        final TabItem tab = this;
		final JsonCallbackEvents events = JsonCallbackEvents.closeTabDisableButtonEvents(assignButton, tab);
		assignButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ArrayList<ExtSource> extSourcesToAdd= extSources.getTableSelectedList();
				if (UiElements.cantSaveEmptyListDialogBox(extSourcesToAdd)) {
                    for (int i=0; i<extSourcesToAdd.size(); i++ ) {
                        if (i != extSourcesToAdd.size()-1) {	                 // call json normaly
                            AddExtSource request = new AddExtSource();
                            request.addExtSource(voId, extSourcesToAdd.get(i).getId());
                        } else {                                                // last change - call json with update
                            AddExtSource request = new AddExtSource(events);
                            request.addExtSource(voId, extSourcesToAdd.get(i).getId());
                        }
                    }
                }
			}
		});

		menu.addWidget(assignButton);
        menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                session.getTabManager().closeTab(tab, false);
            }
        }));

		vp.add(menu);
		vp.setCellHeight(menu, "30px");

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
		final int prime = 31;
		int result = 1;
		result = prime * result * 135;
		return result;
	}

	/**
	 * @param obj
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		AddVoExtSourceTabItem other = (AddVoExtSourceTabItem) obj;

		return (other.voId == this.voId);
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open()
	{
		session.getUiElements().getMenu().openMenu(MainMenu.VO_ADMIN);
		if(vo != null){
			session.setActiveVo(vo);
			return;
		}
		session.setActiveVoId(voId);
	}


	public boolean isAuthorized() {

		if (session.isVoAdmin(voId)) {
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
		return VosTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + voId;
	}
	
	static public AddVoExtSourceTabItem load(Map<String, String> parameters)
	{
		int voId = Integer.parseInt(parameters.get("id"));
		return new AddVoExtSourceTabItem(voId);
	}

}