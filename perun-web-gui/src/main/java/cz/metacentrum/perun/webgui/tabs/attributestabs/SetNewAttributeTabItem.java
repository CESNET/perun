package cz.metacentrum.perun.webgui.tabs.attributestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetAttributesDefinitionWithRights;
import cz.metacentrum.perun.webgui.json.attributesManager.SetAttributes;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.TabMenu;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

import java.util.*;

/**
 * !! USE AS INNER TAB ONLY !!
 *
 * Common form for setting new attributes values to any kind
 * of entity (Facility, Resource, Vo, ...).
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @version $Id$
 */
public class SetNewAttributeTabItem implements TabItem {

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
    private Label titleWidget = new Label("Set new attributes");

    /**
     * Names and IDs of entities to set values for
     */
    private Map<String, Integer> ids;
    private ArrayList<Attribute> inUse = new ArrayList<Attribute>();
    private ButtonTranslation buttonTranslation = ButtonTranslation.INSTANCE;

    /**
     * Display common set new attribute value form for any entity
     *
     * @param ids names and IDs of entities to set values for
     */
    public SetNewAttributeTabItem(Map<String, Integer> ids){
        this.ids = ids;
    }

    /**
     * Display common set new attribute value form for any entity and do not show
     * already set attributes
     *
     * @param ids names and IDs of entities to set values for
     * @param inUse list of attributes to check if they are already in use
     */
    public SetNewAttributeTabItem(Map<String, Integer> ids, ArrayList<Attribute> inUse){
        this.ids = ids;
        this.inUse = inUse;
    }

    public boolean isPrepared(){
        return true;
    }

    public Widget draw() {

        VerticalPanel mainTab = new VerticalPanel();
        mainTab.setSize("100%","100%");

        // correct IDS
        if (ids.containsKey("resourceToGetServicesFrom")) {
            if (!ids.containsKey("resource")){
                ids.put("resource", ids.get("resourceToGetServicesFrom"));
                ids.remove("resourceToGetServicesFrom");
            }
        }
        if (ids.containsKey("workWithUserAttributes")) {
            ids.remove("workWithUserAttributes");
        }

        // set tab name
        String label = titleWidget .getText() + " (";
        for(Map.Entry<String, Integer> entry : ids.entrySet()){
            label += entry.getKey() + ":" + String.valueOf(entry.getValue()) + " ";
        }
        label = label.substring(0, label.length() - 1) + ")";
        titleWidget.setText(label);

        // helper text
        String entityIds = "";
        for (Map.Entry<String, Integer> item : ids.entrySet()) {
            entityIds = entityIds.concat(" "+item.getKey() + ": "+item.getValue());
        }
        HTML helper = new HTML();
        String helperInside = "<p>Enter new values inside textboxes and press Enter key. Confirm changes by clicking on \"Save changes\" button. Values will be set for<strong>"+entityIds+".</strong></p>";
        helper.setHTML(helperInside);

        // callback
        final GetAttributesDefinitionWithRights attrDef = new GetAttributesDefinitionWithRights(ids);

        // remove already used attributes from offering
        JsonCallbackEvents localEvents = new JsonCallbackEvents() {
            public void onFinished(JavaScriptObject jso){
                if (!inUse.isEmpty()) {
                    for (Attribute a : inUse) {
                        attrDef.removeFromTable(a);
                    }
                }
            }
        };
        attrDef.setEvents(localEvents);

        // filter core attributes
        attrDef.switchCore();

        // which entities to show
        Set<String> namespaces = getNamespacesForEntities(ids.keySet());

        // show only these attributes
        attrDef.setEntities(namespaces);

        // get table
        CellTable<Attribute> table = attrDef.getTable();
        table.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);

        table.addStyleName("perun-table");
        table.setWidth("100%");

        // add save button
        TabMenu menu = new TabMenu();

        final TabItem tab = this;
        final CustomButton saveButton = TabMenu.getPredefinedButton(ButtonType.SAVE, buttonTranslation.saveNewAttributes());
        final JsonCallbackEvents events = JsonCallbackEvents.closeTabDisableButtonEvents(saveButton, tab);
        saveButton.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {

                // Save the attributes
                ArrayList<Attribute> list = attrDef.getTableSelectedList();
                if (UiElements.cantSaveEmptyListDialogBox(list)) {

                    // separated list by entity
                    ArrayList<Attribute> facilityList = new ArrayList<Attribute>();
                    ArrayList<Attribute> userFacilityList = new ArrayList<Attribute>();
                    ArrayList<Attribute> userList = new ArrayList<Attribute>();
                    ArrayList<Attribute> memberList = new ArrayList<Attribute>();
                    ArrayList<Attribute> memberResourceList = new ArrayList<Attribute>();
                    ArrayList<Attribute> resourceList = new ArrayList<Attribute>();
                    ArrayList<Attribute> groupList = new ArrayList<Attribute>();
                    ArrayList<Attribute> groupResourceList = new ArrayList<Attribute>();
                    ArrayList<Attribute> hostList = new ArrayList<Attribute>();
                    ArrayList<Attribute> voList = new ArrayList<Attribute>();

                    for (Attribute a : list) {

                        if (a.getEntity().equalsIgnoreCase("facility")) {
                            facilityList.add(a);
                        } else if (a.getEntity().equalsIgnoreCase("user_facility")) {
                            userFacilityList.add(a);
                        } else if (a.getEntity().equalsIgnoreCase("resource")) {
                            resourceList.add(a);
                        } else if (a.getEntity().equalsIgnoreCase("user")) {
                            userList.add(a);
                        } else if (a.getEntity().equalsIgnoreCase("member_resource")) {
                            memberResourceList.add(a);
                        } else if (a.getEntity().equalsIgnoreCase("member")) {
                            memberList.add(a);
                        } else if (a.getEntity().equalsIgnoreCase("group")) {
                            groupList.add(a);
                        } else if (a.getEntity().equalsIgnoreCase("group_resource")) {
                            groupResourceList.add(a);
                        } else if (a.getEntity().equalsIgnoreCase("host")) {
                            hostList.add(a);
                        } else if (a.getEntity().equalsIgnoreCase("vo")) {
                            voList.add(a);
                        }

                    }

                    // GROUPING BY IDS

                    // GROUPING BY IDS

                    SetAttributes request = new SetAttributes(events);

                    if (ids.size() == 4 && ids.containsKey("facility") && ids.containsKey("user") && ids.containsKey("member") && ids.containsKey("resource")) {

                        ArrayList sendList = new ArrayList();
                        sendList.addAll(memberList);
                        sendList.addAll(userList);
                        sendList.addAll(memberResourceList);
                        sendList.addAll(userFacilityList);

                        request.setAttributes(ids, sendList);


                    } else if (ids.size() == 2 &&ids.containsKey("facility") && ids.containsKey("user")) {

                        ArrayList sendList = new ArrayList();
                        sendList.addAll(userList);
                        sendList.addAll(userFacilityList);

                        request.setAttributes(ids, sendList);

                    } else if (ids.size() == 2 && ids.containsKey("member") && ids.containsKey("resource")) {

                        ArrayList sendList = new ArrayList();
                        sendList.addAll(memberList);
                        sendList.addAll(memberResourceList);

                        request.setAttributes(ids, sendList);

                    } else if (ids.size() == 2 && ids.containsKey("group") && ids.containsKey("resource")) {

                        ArrayList sendList = new ArrayList();
                        sendList.addAll(groupList);
                        sendList.addAll(groupResourceList);

                        request.setAttributes(ids, sendList);

                    } else if (ids.size() == 2 && ids.containsKey("member") && ids.containsKey("user")) {

                        ArrayList sendList = new ArrayList();
                        sendList.addAll(memberList);
                        sendList.addAll(userList);
                        // call proper method in RPC
                        ids.put("workWithUserAttributes", 1);
                        request.setAttributes(ids, sendList);

                    } else if (ids.size() == 1 && ids.containsKey("group")) {

                        request.setAttributes(ids, groupList);

                    } else if (ids.size() == 1 && ids.containsKey("resource")) {

                        request.setAttributes(ids, resourceList);

                    } else if (ids.size() == 1 && ids.containsKey("facility")) {

                        request.setAttributes(ids, facilityList);

                    } else if (ids.size() == 1 && ids.containsKey("vo")) {

                        request.setAttributes(ids, voList);

                    } else if (ids.size() == 1 && ids.containsKey("host")) {

                        request.setAttributes(ids, hostList);

                    } else if (ids.size() == 1 && ids.containsKey("member")) {

                        request.setAttributes(ids, memberList);

                    } else if (ids.size() == 1 && ids.containsKey("user")) {

                        request.setAttributes(ids, userList);

                    } else {

                        // TODO
                        Window.alert("Unsupported combination of attributes to set.");

                    }

                }

            }
        });

        menu.addWidget(saveButton);
        menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                session.getTabManager().closeTab(tab, false);
            }
        }));
        menu.addWidget(helper);

        mainTab.add(menu);
        mainTab.setCellHeight(menu, "30px");

        // table wrapper
        ScrollPanel sp = new ScrollPanel(table);
        sp.addStyleName("perun-tableScrollPanel");
        // do not resize like perun table to prevent wrong width in inner tab
        session.getUiElements().resizeSmallTabPanel(sp, 350, this);

        // add table to the page
        mainTab.add(sp);

        //saveButton.setEnabled(false);
        //JsonUtils.addTableManagedButton(attrDef, table, saveButton);

        this.contentWidget.setWidget(mainTab);


        return getWidget();
    }

    public Widget getWidget() {
        return this.contentWidget;
    }

    public Widget getTitle() {
        return this.titleWidget;
    }

    public ImageResource getIcon() {
        return SmallIcons.INSTANCE.scriptAddIcon();
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + 135345;
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

        SetNewAttributeTabItem other = (SetNewAttributeTabItem) obj;

        return (this.ids.equals(other.ids));
    }

    public boolean multipleInstancesEnabled() {
        return false;
    }



    /**
     * Returns the namespaces available for the entities (it's not exact complete set,
     * predefined selection is based on privileges)
     *
     * @param ids collection of entities types to resolve what combinations to use
     * @return set of correct namespaces
     */
    static private Set<String> getNamespacesForEntities(Collection<String> ids){
        Set<String> namespaces = new HashSet<String>();

        // resource relative
        if (ids.contains("resource")) {
            if (ids.contains("member")) {
                namespaces.add("member");
                namespaces.add("member_resource");
            } else if (ids.contains("group")) {
                namespaces.add("group");
                namespaces.add("group_resource");
            } else {
                namespaces.add("resource");
            }
        }
        // facility relative
        if (ids.contains("facility")) {
            if (ids.contains("user")) {
                namespaces.add("user_facility");
            } else {
                namespaces.add("facility");
            }
        }
        // others
        if (ids.contains("member")) {
            namespaces.add("member");
        }
        if (ids.contains("user")) {
            namespaces.add("user");
        }
        if (ids.contains("host")) {
            namespaces.add("host");
        }
        if (ids.contains("vo")) {
            namespaces.add("vo");
        }
        if (ids.contains("group")) {
            namespaces.add("group");
        }

        return namespaces;
    }


    public void open()
    {

    }

    public boolean isAuthorized() {

        // TODO better auth based on passed IDS from constructor ??
        if (session.isVoAdmin() || session.isGroupAdmin() || session.isFacilityAdmin()) {
            return true;
        } else {
            return false;
        }

    }

}