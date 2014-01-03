package cz.metacentrum.perun.webgui.tabs.resourcestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.FillAttributes;
import cz.metacentrum.perun.webgui.json.attributesManager.GetRequiredAttributesV2;
import cz.metacentrum.perun.webgui.json.attributesManager.RemoveAttributes;
import cz.metacentrum.perun.webgui.json.attributesManager.SetAttributes;
import cz.metacentrum.perun.webgui.json.resourcesManager.GetAssignedServices;
import cz.metacentrum.perun.webgui.json.servicesManager.GetServices;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Resource;
import cz.metacentrum.perun.webgui.model.Service;
import cz.metacentrum.perun.webgui.tabs.ResourcesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.attributestabs.SetNewAttributeTabItem;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides page with resource attributes - only required attributes by some service are displayed
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @version $Id$
 */
public class ResourceSettingsTabItem implements TabItem, TabItemWithUrl {

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
    private Label titleWidget = new Label("Service configuration");

    //data
    private int resourceId;
    private int serviceId;
    private Resource resource;
    private Service service;

    private int lastSelectedService = 0;
    private boolean lastOfferAvailableOnly = true;

    /**
     * @param resourceId ID of resource to get attributes for
     * @param serviceId ID of service to get attributes for (0 to display menu for selection)
     */
    public ResourceSettingsTabItem(final int resourceId, final int serviceId){
        this.resourceId = resourceId;
        this.serviceId = serviceId;
        new GetEntityById(PerunEntity.RESOURCE, resourceId, new JsonCallbackEvents(){
            public void onFinished(JavaScriptObject jso){
                resource = jso.cast();
                if (serviceId != 0){
                    new GetEntityById(PerunEntity.SERVICE, serviceId, new JsonCallbackEvents(){
                        public void onFinished(JavaScriptObject jso){
                            service = jso.cast();
                        }
                    }).retrieveData();
                }
            }
        }).retrieveData();
    }

    /**
     * @param resource resource to get attributes for
     * @param service service to get attributes for (null to display menu for selection)
     */
    public ResourceSettingsTabItem(Resource resource, Service service){
        this.resource = resource;
        this.resourceId = resource.getId();
        if (service != null) {
            this.serviceId = service.getId();
            this.service = service;
        } else {
            this.serviceId = 0;
            this.service = null;
        }
    }

    public boolean isPrepared(){
        return !(resource == null && service != null);
    }

    public Widget draw() {

        if (service != null) {
            titleWidget.setText(Utils.getStrippedStringWithEllipsis(resource.getName()) + ": " + service.getName() + " configuration");
        } else {
            titleWidget.setText(Utils.getStrippedStringWithEllipsis(resource.getName()) + ": service configuration");
        }

        final VerticalPanel vp = new VerticalPanel();
        vp.setSize("100%", "100%");

        // menu
        TabMenu menu = new TabMenu();
        vp.add(menu);
        vp.setCellHeight(menu, "30px");

        // callback
        final GetRequiredAttributesV2 resAttrs = new GetRequiredAttributesV2();
        final Map<String,Integer> ids = new HashMap<String,Integer>();
        ids.put("resource", resourceId);
        resAttrs.setIds(ids);

        // puts first table
        final CellTable<Attribute> table = resAttrs.getEmptyTable();

        final ListBoxWithObjects<Service> servList = new ListBoxWithObjects<Service>();

        // switch between assigned and all
        final CheckBox chb = new CheckBox();
        chb.setText(WidgetTranslation.INSTANCE.offerAvailableServicesOnly());
        chb.setTitle(WidgetTranslation.INSTANCE.offerAvailableServicesOnlyTitle());
        chb.setValue(lastOfferAvailableOnly);   // default true

        // event which fills the listbox and call getRequiredAttributes
        JsonCallbackEvents event = new JsonCallbackEvents(){
            @Override
            public void onFinished(JavaScriptObject jso){
                // clear services list
                if (chb.getValue() == false) servList.removeAllOption();
                servList.clear();
                // process services
                ArrayList<Service> srv = JsonUtils.jsoAsList(jso);
                srv = new TableSorter<Service>().sortByName(srv);
                for (int i=0; i<srv.size(); i++){
                    servList.addItem(srv.get(i)); // fill listbox
                }
                if (servList.isEmpty()) {
                    servList.addItem("No service available");
                    ((AjaxLoaderImage)table.getEmptyTableWidget()).loadingFinished();
                    return;
                }
                if (lastSelectedService != 0) {
                    if (chb.getValue()==true) servList.addAllOption();
                    for (Service s : servList.getAllObjects()) {
                        if (s.getId() == lastSelectedService) {
                            // last time was service selected
                            servList.setSelected(s, true);
                            ids.put("service", s.getId());
                        }
                    }
                } else {
                    // last time service was not selected
                    if (chb.getValue()==true) { // for available serv. only select default all
                        servList.addAllOption();
                        servList.setSelectedIndex(0);
                        ids.remove("service");
                    } else { // for all services select first
                        ids.put("service", servList.getSelectedObject().getId()); // take first service as default
                    }

                }
                // make call
                resAttrs.setIds(ids);
                resAttrs.retrieveData();
            };
            @Override
            public void onError(PerunError error){
                servList.clear();
                servList.addItem("Error while loading");
            };
            @Override
            public void onLoadingStart() {
                resAttrs.clearTable();
                servList.clear();
                servList.addItem("Loading...");
            }
        };
        final GetAssignedServices services = new GetAssignedServices(resourceId, event);
        final GetServices allServices = new GetServices(event);

        // offer services selection
        if (serviceId == 0) {
            // services listbox
            servList.setTitle("Services");
            // on change of service update table
            servList.addChangeHandler(new ChangeHandler() {
                public void onChange(ChangeEvent event) {
                    // if service selected
                    if (chb.getValue() == true && servList.getSelectedIndex() > 0) {
                        ids.put("service",servList.getSelectedObject().getId());
                        lastSelectedService = servList.getSelectedObject().getId();
                    } else if (chb.getValue() == false) {
                        ids.put("service",servList.getSelectedObject().getId());
                        lastSelectedService = servList.getSelectedObject().getId();
                    } else {
                        lastSelectedService = 0;
                        ids.remove("service");
                    }
                    lastOfferAvailableOnly = chb.getValue();
                    resAttrs.setIds(ids);
                    resAttrs.clearTable();
                    resAttrs.retrieveData();
                }
            });
            if (chb.getValue() == false) {
                allServices.retrieveData();
            } else {
                services.retrieveData();
            }
        } else {
            // retrieve data for selected service only
            lastSelectedService = serviceId;
            lastOfferAvailableOnly = chb.getValue();
            ids.put("service", serviceId);
            resAttrs.setIds(ids);
            resAttrs.retrieveData();
        }

        // refresh table envent - refresh only on finished / on error keep selected
        final JsonCallbackEvents refreshTable = new JsonCallbackEvents(){
            public void onFinished(JavaScriptObject jso) {
                resAttrs.clearTable();
                resAttrs.retrieveData();
            }
        };

        // add save changes to menu
        final CustomButton saveChangesButton = TabMenu.getPredefinedButton(ButtonType.SAVE, ButtonTranslation.INSTANCE.saveChangesInAttributes());
        menu.addWidget(saveChangesButton);

        // set button disable event
        final JsonCallbackEvents saveChangesButtonEvent = JsonCallbackEvents.disableButtonEvents(saveChangesButton, refreshTable);

        saveChangesButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {

                ArrayList<Attribute> list = resAttrs.getTableSelectedList();
                if (UiElements.cantSaveEmptyListDialogBox(list)) {
                    Map<String, Integer> ids = new HashMap<String,Integer>();
                    ids.put("resource", resourceId);
                    SetAttributes request = new SetAttributes(saveChangesButtonEvent);
                    request.setAttributes(ids, list);
                }
            }
        });

        // add set new to menu
        Button setNewAttributeButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.setNewAttributes(), new ClickHandler() {
            public void onClick(ClickEvent event) {
                Map<String, Integer> ids = new HashMap<String, Integer>();
                ids.put("resource", resourceId);
                session.getTabManager().addTabToCurrentTab(new SetNewAttributeTabItem(ids, resAttrs.getList()), true);
            }
        });
        menu.addWidget(setNewAttributeButton);

        // fill button
        final CustomButton fillDefaultButton = TabMenu.getPredefinedButton(ButtonType.FILL, ButtonTranslation.INSTANCE.fillResourceAttributes());
        menu.addWidget(fillDefaultButton);

        // remove attr button
        final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeAttributes());

        // remove button event
        final JsonCallbackEvents removeButtonEvent = JsonCallbackEvents.disableButtonEvents(removeButton, refreshTable);

        removeButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {

                ArrayList<Attribute> list = resAttrs.getTableSelectedList();
                if (UiElements.cantSaveEmptyListDialogBox(list)) {
                    Map<String, Integer> ids = new HashMap<String,Integer>();
                    ids.put("resource", resourceId);
                    RemoveAttributes request = new RemoveAttributes(removeButtonEvent);
                    request.removeAttributes(ids, list);
                }

            }
        });

        menu.addWidget(removeButton);

        // add service selection to menu and switcher
        if (serviceId == 0) {
            menu.addWidget(new HTML("<strong>Selected&nbsp;service: </strong>"));
            menu.addWidget(servList);
            menu.addWidget(chb);
        }

        // checkbox switcher on click
        chb.addClickHandler(new ClickHandler(){
            public void onClick(ClickEvent event) {
                if (chb.getValue() == false) {
                    lastOfferAvailableOnly = false;
                    allServices.retrieveData();
                } else {
                    lastOfferAvailableOnly = true;
                    services.retrieveData();
                }
            }
        });

        fillDefaultButton.addClickHandler(new ClickHandler(){
            public void onClick(ClickEvent event) {
                // automatically try to fill all attributes
                final ArrayList<Attribute> list = resAttrs.getList();
                if (UiElements.cantSaveEmptyListDialogBox(list)) {
                    Map<String, Integer> ids = new HashMap<String,Integer>();
                    ids.put("resource", resourceId);
                    FillAttributes request = new FillAttributes(JsonCallbackEvents.disableButtonEvents(fillDefaultButton, new JsonCallbackEvents(){
                        @Override
                        public void onFinished(JavaScriptObject jso) {
                            // remove attribute from original table and put new ones
                            ArrayList<Attribute> newList = JsonUtils.jsoAsList(jso);
                            for (Attribute a : newList) {
                                for (Attribute oldA : list) {
                                    // deselect old
                                    if (a.getId() == oldA.getId()) {
                                        resAttrs.getSelectionModel().setSelected(oldA, false);
                                        resAttrs.removeFromTable(oldA);
                                    }
                                }
                                //add new
                                resAttrs.addToTable(a);
                                // select returned
                                resAttrs.getSelectionModel().setSelected(a, true);
                            }
                            resAttrs.sortTable();
                        }
                    }));
                    request.fillAttributes(ids, list);
                }
            }
        });
		
		/* TODO - not implemented
		
		Button checkValuesButton = new CustomButton("Check values", SmallIcons.INSTANCE.scriptGearIcon());
		menu.addWidget(checkValuesButton);
		checkValuesButton.setTitle("Checks inserted values against current Perun state - nothing is saved unless you click on 'Save changes'");
		
		
		checkValuesButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				Window.alert("not yet implemented");
			}
		});
		
		*/

        table.addStyleName("perun-table");
        table.setWidth("100%");
        ScrollPanel sp = new ScrollPanel(table);
        sp.addStyleName("perun-tableScrollPanel");
        session.getUiElements().resizePerunTable(sp, 350, this);
        vp.add(sp);

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
        return SmallIcons.INSTANCE.settingToolsIcon();
    }

    @Override
    public int hashCode() {
        final int prime = 3;
        int result = 1;
        result = prime * result + resourceId;
        result = prime * result + serviceId;
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
        ResourceSettingsTabItem other = (ResourceSettingsTabItem) obj;
        if (resourceId != other.resourceId)
            return false;
        if (serviceId != other.serviceId)
            return false;
        return true;
    }

    public boolean multipleInstancesEnabled() {
        return false;
    }

    public void open()
    {
        session.setActiveVoId(resource.getVoId());
        session.setActiveFacilityId(resource.getFacilityId());
    }

    public boolean isAuthorized() {

        if (session.isVoAdmin(resource.getVoId()) || session.isFacilityAdmin(resource.getFacilityId())) {
            return true;
        } else {
            return false;
        }

    }

    public final static String URL = "servsettings";

    public String getUrl()
    {
        return URL;
    }

    public String getUrlWithParameters()
    {
        return ResourcesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + resourceId + "&service=" + serviceId;
    }

    static public ResourceSettingsTabItem load(Map<String, String> parameters)
    {
        int id = Integer.parseInt(parameters.get("id"));
        int srv = Integer.parseInt(parameters.get("service"));
        return new ResourceSettingsTabItem(id, srv);
    }

}