package cz.metacentrum.perun.webgui.tabs.facilitiestabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetRequiredAttributesV2;
import cz.metacentrum.perun.webgui.json.attributesManager.SetAttributes;
import cz.metacentrum.perun.webgui.json.authzResolver.GetRichAdminsWithAttributes;
import cz.metacentrum.perun.webgui.json.authzResolver.RemoveAdmin;
import cz.metacentrum.perun.webgui.json.facilitiesManager.*;
import cz.metacentrum.perun.webgui.json.servicesManager.*;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.*;
import cz.metacentrum.perun.webgui.tabs.resourcestabs.CreateFacilityResourceTabItem;
import cz.metacentrum.perun.webgui.tabs.userstabs.UserDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * FACILITY ADMIN / PERUN ADMIN - Create facility wizard - page 1
 * !! USE AS INNER TAB ONLY !!
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id: 88d122cf60dd09e8150539ea580d01e8dfcb09e1 $
 */
public class CreateFacilityTabItem implements TabItem, TabItemWithUrl {

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
    private Label titleWidget = new Label("Create facility");

    // created facility
    private Facility facility = null;
    // if created from source
    private Facility sourceFacility = null;


    private int selectedPage = 1; // displayed page of wizard
    private int numberOfPages = 8; // max index of pages

    public final static String URL = "create";

    private boolean visitedBasic = false;

    private String newHostInput = "";

    private ArrayList<Service> selectedServices = new ArrayList<Service>();

    @Override
    public String getUrl() {
        return URL;
    }

    @Override
    public String getUrlWithParameters() {
        return FacilitiesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl();
    }

    /**
     * Creates a tab instance
     */
    public CreateFacilityTabItem(){}

    public boolean isPrepared(){
        return true;
    }

    public Widget draw() {

        final TabItemWithUrl tab = this;

        VerticalPanel vp = new VerticalPanel();
        vp.setSize("100%", "100%");

        // FOOTER
        AbsolutePanel footer = new AbsolutePanel();
        footer.setStyleName("wizardFooter");

        final CustomButton back = TabMenu.getPredefinedButton(ButtonType.BACK, ButtonTranslation.INSTANCE.backButton());
        final CustomButton next = TabMenu.getPredefinedButton(ButtonType.CONTINUE, ButtonTranslation.INSTANCE.continueButton());
        final CustomButton exit = TabMenu.getPredefinedButton(ButtonType.CANCEL, ButtonTranslation.INSTANCE.cancelButton());

        exit.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                UiElements.generateAlert("Confirmation", "Do you really want to exit create facility wizard ?", new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        session.getTabManager().closeTab(tab);
                    }
                });
            }
        });

        back.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (selectedPage >1) selectedPage--;
                draw();
            }
        });

        if (selectedPage == 1) {
            footer.add(exit);
            exit.getElement().setAttribute("style", "position: absolute; left: 5px; bottom: 10px;");
        } else {
            footer.add(back);
            back.getElement().setAttribute("style", "position: absolute; left: 5px; bottom: 10px;");
        }

        if (selectedPage != numberOfPages) {

            footer.add(next);
            next.getElement().setAttribute("style", "position: absolute; right: 5px; bottom: 10px;");

        }

        // MAIN CONTENT

        SimplePanel content = new SimplePanel();
        content.getElement().setId("centered-wrapper-outer");

        if (selectedPage == 1) {


            SimplePanel innerContent = new SimplePanel();
            innerContent.getElement().setId("centered-wrapper-inner");

            session.getUiElements().resizePerunTable(innerContent, 350, 50, this);

            // header
            session.getUiElements().getBreadcrumbs().setLocation(MainMenu.FACILITY_ADMIN, "Create facility "+selectedPage+" of "+numberOfPages+": Definition", "");

            // content
            FlexTable layout = new FlexTable();
            layout.setStyleName("inputFormFlexTable");
            FlexTable.FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

            // widgets
            final ExtendedTextBox name = new ExtendedTextBox();
            final ListBox type = new ListBox();
            final CheckBox asCopy = new CheckBox("Copy settings");
            final ListBoxWithObjects<Facility> copyOfFacility = new ListBoxWithObjects<Facility>();

            final ExtendedTextBox.TextBoxValidator validator = new ExtendedTextBox.TextBoxValidator() {
                @Override
                public boolean validateTextBox() {
                    if (name.getTextBox().getText().trim().isEmpty()) {
                        name.setError("Facility name can't be empty.");
                        return false;
                    } else {
                        name.setOk();
                        return true;
                    }
                }
            };
            name.setValidator(validator);

            // facility-type values
            type.addItem("General", "general");
            type.addItem("Storage", "storage");
            type.addItem("Cluster", "cluster");
            type.addItem("Host", "host");
            type.addItem("V-Cluster", "vcluster");
            type.addItem("V-Host", "vhost");

            // Add some standard form options
            layout.setHTML(0, 0, "Name:");
            layout.setWidget(0, 1, name);
            layout.setHTML(1, 0, "Type:");
            layout.setWidget(1, 1, type);
            layout.setHTML(2, 0, "As copy:");
            layout.setWidget(2, 1, asCopy);
            final HTML cp = new HTML("Source:");
            layout.setWidget(3, 0, cp);
            layout.setWidget(3, 1, copyOfFacility);

            layout.getFlexCellFormatter().setWidth(0, 1, "300px");
            layout.getElement().setId("centered-content");
            innerContent.setWidget(layout);
            content.add(innerContent);

            cp.setVisible(false);
            copyOfFacility.setVisible(false);

            for (int i=0; i<layout.getRowCount(); i++) {
                cellFormatter.addStyleName(i, 0, "itemName");
            }

            final GetFacilities getFacs = new GetFacilities(false, new JsonCallbackEvents(){
                public void onFinished(JavaScriptObject jso) {
                    copyOfFacility.clear();
                    ArrayList<Facility> fac = JsonUtils.jsoAsList(jso);
                    if (fac.isEmpty() || fac == null) {
                        copyOfFacility.addItem("No facilities available");
                        return;
                    }
                    fac = new TableSorter<Facility>().sortByName(fac);
                    for (int i=0; i<fac.size(); i++){
                        copyOfFacility.addItem(fac.get(i));
                    }
                    next.setEnabled(true);
                }
                public void onError(PerunError error){
                    next.setEnabled(true);
                    copyOfFacility.clear();
                    copyOfFacility.addItem("Error while loading");

                }
                public void onLoadingStart(){
                    next.setEnabled(false);
                    copyOfFacility.clear();
                    copyOfFacility.addItem("Loading...");
                }
            });

            asCopy.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> booleanValueChangeEvent) {
                    if (asCopy.getValue() == true) {
                        cp.setVisible(true);
                        copyOfFacility.setVisible(true);
                        if (getFacs.getList().isEmpty()) {
                            getFacs.retrieveData();
                        }
                    } else {
                        cp.setVisible(false);
                        copyOfFacility.setVisible(false);
                        next.setEnabled(true);
                    }
                }
            });

            // next button

            next.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {

                    if (!visitedBasic) {

                        if (validator.validateTextBox()) {
                            CreateFacility request = new CreateFacility(JsonCallbackEvents.disableButtonEvents(next, new JsonCallbackEvents(){
                                public void onFinished(JavaScriptObject jso) {
                                    facility = jso.cast();
                                    if (asCopy.getValue() == true) {
                                        sourceFacility = copyOfFacility.getSelectedObject();
                                    }
                                    visitedBasic = true;
                                    selectedPage++;
                                    draw();
                                }
                                public void onLoadingStart() {
                                    asCopy.setEnabled(false);
                                    copyOfFacility.setEnabled(false);
                                }
                                public void onError(PerunError error) {
                                    asCopy.setEnabled(true);
                                    copyOfFacility.setEnabled(true);
                                }
                            }));
                            request.createFacility(name.getTextBox().getText().trim(), type.getValue(type.getSelectedIndex()));

                        }

                    } else {
                        // facility created - continue
                        selectedPage++;
                        draw();
                    }

                }
            });

            if (visitedBasic) {

                // if tab was visited
                name.getTextBox().setValue(facility.getName());

                for (int i=0; i< type.getItemCount(); i++) {
                    if (type.getValue(i).equals(facility.getType())) {
                        type.setSelectedIndex(i);
                        break;
                    }
                }

                name.getTextBox().setEnabled(false);
                type.setEnabled(false);
                asCopy.setEnabled(false);
                copyOfFacility.setEnabled(false);

                if (sourceFacility != null) {
                    asCopy.setValue(true);
                    copyOfFacility.setVisible(true);
                    cp.setVisible(true);
                    copyOfFacility.addItem(sourceFacility);
                    copyOfFacility.setSelected(sourceFacility, true);
                }

            }

        } else if (selectedPage == 2) {

            session.getUiElements().getBreadcrumbs().setLocation(MainMenu.FACILITY_ADMIN, "Create facility "+selectedPage+" of "+numberOfPages+": Add managers", "");

            VerticalPanel innerContent = new VerticalPanel();
            innerContent.setSize("100%", "100%");

            // Menu
            TabMenu menu = new TabMenu();

            // get the table
            final GetRichAdminsWithAttributes jsonCallback = new GetRichAdminsWithAttributes(PerunEntity.FACILITY, facility.getId(), null);
            CellTable<User> table;
            if (session.isPerunAdmin()) {
                table = jsonCallback.getTable(new FieldUpdater<User, String>() {
                    @Override
                    public void update(int i, User user, String s) {
                        session.getTabManager().addTab(new UserDetailTabItem(user));
                    }
                });
            } else {
                table = jsonCallback.getTable();
            }

            menu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addManagerToFacility(), new ClickHandler() {
                public void onClick(ClickEvent event) {
                    session.getTabManager().addTabToCurrentTab(new AddFacilityManagerTabItem(facility), true);
                }
            }));

            final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeManagerFromFacility());
            menu.addWidget(removeButton);
            removeButton.addClickHandler(new ClickHandler(){
                public void onClick(ClickEvent event) {
                    final ArrayList<User> list = jsonCallback.getTableSelectedList();
                    String text = "Following users won't be facility managers anymore and won't be able to manage this facility in Perun.";
                    UiElements.showDeleteConfirm(list, text, new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent clickEvent) {
                            // TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
                            for (int i = 0; i < list.size(); i++) {
                                if (i == list.size() - 1) {
                                    RemoveAdmin request = new RemoveAdmin(PerunEntity.FACILITY, JsonCallbackEvents.disableButtonEvents(removeButton, JsonCallbackEvents.refreshTableEvents(jsonCallback)));
                                    request.removeAdmin(facility.getId(), list.get(i).getId());
                                } else {
                                    RemoveAdmin request = new RemoveAdmin(PerunEntity.FACILITY, JsonCallbackEvents.disableButtonEvents(removeButton));
                                    request.removeAdmin(facility.getId(), list.get(i).getId());
                                }
                            }
                        }
                    });
                }
            });

            final CustomButton fill = new CustomButton("Copy from source facility", SmallIcons.INSTANCE.copyIcon());
            fill.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    CopyManagers copy = new CopyManagers(JsonCallbackEvents.disableButtonEvents(fill, JsonCallbackEvents.refreshTableEvents(jsonCallback)));
                    copy.copyFacilityManagers(sourceFacility.getId(), facility.getId());
                }
            });

            if (sourceFacility != null) {
                menu.addWidget(fill);
            }

            menu.addWidget(new Image(SmallIcons.INSTANCE.helpIcon()));
            menu.addWidget(new HTML("<strong>People with privilege to manage this facility in Perun. They aren't automatically \"roots\" on machine.</strong>"));

            innerContent.add(menu);
            innerContent.setCellHeight(menu, "30px");

            removeButton.setEnabled(false);
            JsonUtils.addTableManagedButton(jsonCallback, table, removeButton);

            table.addStyleName("perun-table");
            ScrollPanel sp = new ScrollPanel(table);
            sp.addStyleName("perun-tableScrollPanel");

            innerContent.add(sp);

            session.getUiElements().resizePerunTable(sp, 300, 50, this);

            content.setWidget(innerContent);

            next.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    selectedPage++;
                    draw();
                }
            });

        } else if (selectedPage == 3) {

            session.getUiElements().getBreadcrumbs().setLocation(MainMenu.FACILITY_ADMIN, "Create facility "+selectedPage+" of "+numberOfPages+": Add owners", "");

            // CONTENT
            VerticalPanel innerContent = new VerticalPanel();
            innerContent.setSize("100%", "100%");

            // MENU
            TabMenu menu = new TabMenu();

            // CALLBACK
            final GetFacilityOwners jsonCallback = new GetFacilityOwners(facility);

            innerContent.add(menu);
            innerContent.setCellHeight(menu, "30px");

            // add button
            CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addNewOwners());
            addButton.addClickHandler(new ClickHandler(){
                public void onClick(ClickEvent event) {
                    session.getTabManager().addTabToCurrentTab(new AddFacilityOwnerTabItem(facility), true);
                }
            });

            // remove button
            final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeSelectedOwners());
            removeButton.addClickHandler(new ClickHandler(){
                public void onClick(ClickEvent event) {
                    final ArrayList<Owner> list = jsonCallback.getTableSelectedList();
                    UiElements.showDeleteConfirm(list, new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent clickEvent) {
                            // TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
                            for (int i=0; i<list.size(); i++) {
                                if (i == list.size()-1) {
                                    RemoveOwner request = new RemoveOwner(JsonCallbackEvents.disableButtonEvents(removeButton, JsonCallbackEvents.refreshTableEvents(jsonCallback)));
                                    request.removeFacilityOwner(facility.getId(), list.get(i).getId());
                                } else {
                                    RemoveOwner request = new RemoveOwner(JsonCallbackEvents.disableButtonEvents(removeButton));
                                    request.removeFacilityOwner(facility.getId(), list.get(i).getId());
                                }
                            }
                        }
                    });
                }
            });

            menu.addWidget(addButton);
            menu.addWidget(removeButton);

            final CustomButton fill = new CustomButton("Copy from source facility", SmallIcons.INSTANCE.copyIcon());
            fill.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    CopyOwners copy = new CopyOwners(JsonCallbackEvents.disableButtonEvents(fill, JsonCallbackEvents.refreshTableEvents(jsonCallback)));
                    copy.copyFacilityOwners(sourceFacility.getId(), facility.getId());
                }
            });

            if (sourceFacility != null) {
                menu.addWidget(fill);
            }

            // TABLE
            CellTable<Owner> table = jsonCallback.getTable();
            table.addStyleName("perun-table");
            ScrollPanel sp = new ScrollPanel(table);
            sp.addStyleName("perun-tableScrollPanel");

            innerContent.add(sp);

            removeButton.setEnabled(false);
            JsonUtils.addTableManagedButton(jsonCallback, table, removeButton);

            session.getUiElements().resizePerunTable(sp, 300, 50, this);

            next.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    selectedPage++;
                    draw();
                }
            });
            content.add(innerContent);

        } else if (selectedPage == 4) {

            session.getUiElements().getBreadcrumbs().setLocation(MainMenu.FACILITY_ADMIN, "Create facility "+selectedPage+" of "+numberOfPages+": Add hosts", "");

            // SPLIT
            FlexTable hp = new FlexTable();
            hp.setSize("100%", "100%");

            // HOSTS WIDGET

            VerticalPanel hostsWidget = new VerticalPanel();
            hostsWidget.setSize("100%","100%");

            final GetHosts hosts = new GetHosts(facility.getId());
            final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(hosts);

            // menu
            TabMenu menu = new TabMenu();

            final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeHosts());
            removeButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    final ArrayList<Host> hostsForRemoving = hosts.getTableSelectedList();
                    String text = "Following hosts will be removed from facility.";
                    UiElements.showDeleteConfirm(hostsForRemoving, text, new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent clickEvent) {
                            // TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
                            for (int i = 0; i < hostsForRemoving.size(); i++) {
                                if (i == hostsForRemoving.size()-1) {
                                    RemoveHosts request = new RemoveHosts(facility.getId(), JsonCallbackEvents.disableButtonEvents(removeButton, events));
                                    request.removeHost(hostsForRemoving.get(i).getId());
                                } else {
                                    RemoveHosts request = new RemoveHosts(facility.getId(), JsonCallbackEvents.disableButtonEvents(removeButton));
                                    request.removeHost(hostsForRemoving.get(i).getId());
                                }
                            }
                        }});
                }
            });
            menu.addWidget(removeButton);

            // Hosts table
            CellTable<Host> table = hosts.getTable(new FieldUpdater<Host, String>() {
                @Override
                public void update(int index, Host object, String value) {
                    session.getTabManager().addTab(new FacilityHostsSettingsTabItem(facility, object));
                }
            });

            removeButton.setEnabled(false);
            JsonUtils.addTableManagedButton(hosts, table, removeButton);

            // add a class to the table and wrap it into scroll panel
            table.addStyleName("perun-table");
            ScrollPanel sp = new ScrollPanel(table);
            sp.addStyleName("perun-tableScrollPanel");

            // add menu and the table to the main panel
            hostsWidget.add(menu);
            hostsWidget.setCellHeight(menu, "30px");
            hostsWidget.add(sp);

            session.getUiElements().resizePerunTable(sp, 300, 50, this);

            // ADD WIDGET

            final TextArea newHosts = new TextArea();
            newHosts.setSize("335px", "200px");
            newHosts.setText(newHostInput);

            // TODO - create extended text area
            newHosts.addKeyUpHandler(new KeyUpHandler() {
                @Override
                public void onKeyUp(KeyUpEvent event) {
                    newHostInput = newHosts.getText();
                }
            });

            final CustomButton addHostsButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addHost());

            addHostsButton.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    String hostnames = newHosts.getText().trim();
                    if (hostnames.isEmpty()) {
                        UiElements.generateAlert("Empty input", "Please enter at least one hostname to add it to facility.");
                        return;
                    }
                    String hosts[] = hostnames.split(",");
                    // trim whitespace
                    for (int i = 0; i< hosts.length; i++) {
                        hosts[i] = hosts[i].trim();
                    }
                    AddHosts request = new AddHosts(facility.getId(), JsonCallbackEvents.mergeEvents(JsonCallbackEvents.disableButtonEvents(addHostsButton, events), new JsonCallbackEvents(){
                        public void onFinished(JavaScriptObject jso) {
                            // clear input
                            newHosts.setText("");
                            newHostInput = "";
                        }
                    }));
                    request.addHosts(hosts);
                }
            });

            // layout
            FlexTable layout = new FlexTable();
            layout.setStyleName("inputFormFlexTable");
            FlexTable.FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

            layout.setHTML(0, 0, "Hostnames:");
            layout.setWidget(1, 0, newHosts);
            cellFormatter.addStyleName(0, 0, "itemName");

            layout.setHTML(2, 0, "Enter hostnames separated by comas.");
            cellFormatter.addStyleName(2, 0, "inputFormInlineComment");

            cellFormatter.setHorizontalAlignment(3, 0, HasHorizontalAlignment.ALIGN_RIGHT);
            layout.setWidget(3, 0, addHostsButton);
            addHostsButton.getElement().setAttribute("style", addHostsButton.getElement().getAttribute("style")+" float: right;");

            // FILL LAYOUT

            hp.setWidget(0, 0, layout);
            hp.setWidget(0, 1, hostsWidget);
            hp.getFlexCellFormatter().setWidth(0, 0, "350px");
            hp.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
            hp.getFlexCellFormatter().setStyleName(0, 0, "border-right");

            content.add(hp);

            next.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    selectedPage++;
                    draw();
                }
            });

        } else if (selectedPage == 5) {

            session.getUiElements().getBreadcrumbs().setLocation(MainMenu.FACILITY_ADMIN, "Create facility "+selectedPage+" of "+numberOfPages+": Select services", "");

            // SPLIT
            FlexTable hp = new FlexTable();
            hp.setSize("100%", "100%");

            // create widget for the whole page
            VerticalPanel mainTab = new VerticalPanel();
            mainTab.setSize("100%", "100%");

            final CustomButton copyFromSource = new CustomButton("Copy selection from source facility", SmallIcons.INSTANCE.copyIcon());
            final CustomButton fillDefault = new CustomButton("Use default selection for unix account", "Select services required to manage unix accounts on facility", SmallIcons.INSTANCE.lightningIcon());

            // get services
            final GetServices services = new GetServices();
            services.setEvents(new JsonCallbackEvents(){
                public void onFinished(JavaScriptObject jso){
                    if (selectedServices != null && !selectedServices.isEmpty()) {
                        for (Service s : selectedServices) {
                            services.getSelectionModel().setSelected(s, true);
                        }
                    }
                    copyFromSource.setEnabled(true);
                    fillDefault.setEnabled(true);
                }
                public void onLoadingStart() {
                    copyFromSource.setEnabled(false);
                    fillDefault.setEnabled(false);
                }
                public void onError(PerunError error) {
                    copyFromSource.setEnabled(true);
                    fillDefault.setEnabled(true);
                }
            });

            // get the table of services with custom field updater (lines are clickable and open service details)
            CellTable<Service> table = services.getTable();

            // add styling to table with services
            table.addStyleName("perun-table");
            ScrollPanel sp = new ScrollPanel(table);
            sp.addStyleName("perun-tableScrollPanel");
            mainTab.add(sp);

            VerticalPanel helpWidget = new VerticalPanel();
            helpWidget.setSpacing(5);
            //helpWidget.setHeight("100%");

            helpWidget.add(new HTML("<p>Please select set of services on your facility, which will be managed by Perun."));

            copyFromSource.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    GetFacilityAssignedServices getCall = new GetFacilityAssignedServices(sourceFacility.getId(), JsonCallbackEvents.disableButtonEvents(copyFromSource, new JsonCallbackEvents(){
                        @Override
                        public void onFinished(JavaScriptObject jso) {
                            ArrayList<Service> serv = JsonUtils.jsoAsList(jso);
                            services.clearTableSelectedSet();
                            for (Service s : serv) {
                                services.getSelectionModel().setSelected(s, true);
                            }
                        }
                    }));
                    getCall.retrieveData();
                }
            });

            if (sourceFacility != null) {
                helpWidget.add(copyFromSource);
                helpWidget.setCellHorizontalAlignment(copyFromSource, HasHorizontalAlignment.ALIGN_CENTER);
                helpWidget.setCellVerticalAlignment(copyFromSource, HasVerticalAlignment.ALIGN_MIDDLE);
            }

            fillDefault.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    for (Service s : services.getList()) {

                        if (s.getName().equalsIgnoreCase("passwd")) {
                            services.getSelectionModel().setSelected(s, true);
                        } else if (s.getName().equalsIgnoreCase("group")) {
                            services.getSelectionModel().setSelected(s, true);
                        } else if (s.getName().equalsIgnoreCase("mailaliases")) {
                            services.getSelectionModel().setSelected(s, true);
                        } else if (s.getName().equalsIgnoreCase("fs_home")) {
                            services.getSelectionModel().setSelected(s, true);
                        } else {
                            // unselect others
                            services.getSelectionModel().setSelected(s, false);
                        }

                    }
                }
            });

            helpWidget.add(fillDefault);
            helpWidget.setCellHorizontalAlignment(fillDefault, HasHorizontalAlignment.ALIGN_CENTER);
            helpWidget.setCellVerticalAlignment(fillDefault, HasVerticalAlignment.ALIGN_MIDDLE);

            hp.setWidget(0, 0, helpWidget);
            hp.setWidget(0, 1, mainTab);
            hp.getFlexCellFormatter().setWidth(0, 0, "350px");
            hp.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
            hp.getFlexCellFormatter().setStyleName(0, 0, "border-right");

            content.setWidget(hp);

            next.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (services.getTableSelectedList() == null || services.getTableSelectedList().isEmpty()) {
                        UiElements.generateAlert("No service selected", "In order to continue please select some services, which will be configured by Perun.");
                    } else {
                        // set selected services
                        selectedServices.clear();
                        for (Service s : new TableSorter<Service>().sortByName(services.getTableSelectedList())) {
                            selectedServices.add(s);
                        }
                        // draw
                        selectedPage++;
                        draw();
                    }
                }
            });

            session.getUiElements().resizePerunTable(sp, 300, 50, this);

        } else if (selectedPage == 6) {

            session.getUiElements().getBreadcrumbs().setLocation(MainMenu.FACILITY_ADMIN, "Create facility "+selectedPage+" of "+numberOfPages+": Configure services", "");

            // content
            VerticalPanel settingsTab = new VerticalPanel();
            settingsTab.setSize("100%","100%");

            // HORIZONTAL MENU
            TabMenu menu = new TabMenu();

            // Get Attributes method
            final GetRequiredAttributesV2 reqAttrs = new GetRequiredAttributesV2();
            final ListBoxWithObjects<Service> servList = new ListBoxWithObjects<Service>();

            // get empty table
            final CellTable<Attribute> table = reqAttrs.getEmptyTable();

            // ids to retrieve data from rpc
            final Map<String,Integer> ids = new HashMap<String, Integer>();
            ids.put("facility",facility.getId());

            final CustomButton saveChangesButton = TabMenu.getPredefinedButton(ButtonType.SAVE, ButtonTranslation.INSTANCE.saveChangesInAttributes());
            final JsonCallbackEvents refreshEvents = JsonCallbackEvents.refreshTableEvents(reqAttrs);
            final JsonCallbackEvents saveChangesButtonEvent = JsonCallbackEvents.disableButtonEvents(saveChangesButton, refreshEvents);
            saveChangesButton.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    ArrayList<Attribute> list = reqAttrs.getTableSelectedList();
                    if (UiElements.cantSaveEmptyListDialogBox(list)) {
                        SetAttributes request = new SetAttributes(saveChangesButtonEvent);
                        request.setAttributes(ids, reqAttrs.getTableSelectedList());
                    }
                }
            });

            if (selectedServices == null || selectedServices.isEmpty()) {
                servList.addItem("No service selected");
                servList.setEnabled(false);
            } else {
                for (Service serv : selectedServices) {
                    servList.addItem(serv);
                }
                ids.put("service", servList.getSelectedObject().getId());
                reqAttrs.setIds(ids);
                reqAttrs.clearTable();
                reqAttrs.retrieveData();
            }

            servList.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    ids.put("service", servList.getSelectedObject().getId());
                    reqAttrs.setIds(ids);
                    reqAttrs.clearTable();
                    reqAttrs.retrieveData();
                }
            });

            final CustomButton copyFromSource = new CustomButton("Copy settings from source facility", SmallIcons.INSTANCE.copyIcon());
            copyFromSource.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    CopyAttributes copy = new CopyAttributes(JsonCallbackEvents.disableButtonEvents(copyFromSource, refreshEvents));
                    copy.copyFacilityAttributes(sourceFacility.getId(), facility.getId());
                }
            });



            menu.addWidget(saveChangesButton);
            if (sourceFacility != null) {
                menu.addWidget(copyFromSource);
            }
            menu.addWidget(new HTML("<strong>Filter view by Service: </strong>"));
            menu.addWidget(servList);

            table.addStyleName("perun-table");
            ScrollPanel sp = new ScrollPanel(table);
            sp.addStyleName("perun-tableScrollPanel");

            // add menu and the table to the main panel
            settingsTab.add(menu);
            settingsTab.setCellHeight(menu, "30px");
            settingsTab.add(sp);

            session.getUiElements().resizePerunTable(sp, 300, 50, this);

            content.add(settingsTab);

            next.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    selectedPage++;
                    draw();
                }
            });

        } else if (selectedPage == 7) {

            session.getUiElements().getBreadcrumbs().setLocation(MainMenu.FACILITY_ADMIN, "Create facility "+selectedPage+" of "+numberOfPages+": Add service destinations", "");

            // SPLIT
            FlexTable hp = new FlexTable();
            hp.setSize("100%", "100%");

            FlexTable layout = new FlexTable();
            layout.setStyleName("inputFormFlexTable");
            FlexTable.FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();
            layout.setWidth("350px");

            final TextBox destination = new TextBox();
            final ListBox type = new ListBox();
            type.addItem("HOST","host");
            type.addItem("USER@HOST", "user@host");
            type.addItem("USER@HOST:PORT", "user@host:port");
            type.addItem("URL","url");
            type.addItem("MAIL","email");
            type.addItem("SIGNED MAIL","semail");

            final CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addDestination());

            final ListBoxWithObjects<Service> services = new ListBoxWithObjects<Service>();
            final CheckBox useHosts = new CheckBox(WidgetTranslation.INSTANCE.useFacilityHostnames(), false);
            useHosts.setTitle(WidgetTranslation.INSTANCE.useFacilityHostnamesTitle());

            type.addChangeHandler(new ChangeHandler(){
                public void onChange(ChangeEvent event) {
                    // if hosts - checkbox visible
                    if (type.getSelectedIndex() == 0) {
                        useHosts.setVisible(true);
                    } else {
                        useHosts.setVisible(false);
                        useHosts.setValue(false);
                        destination.setEnabled(true);
                    }
                }
            });

            useHosts.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    if (useHosts.getValue() == true) {
                        destination.setEnabled(false);
                    } else {
                        destination.setEnabled(true);
                    }
                }
            });

            services.clear();
            if (selectedServices == null || selectedServices.isEmpty()) {
                services.addItem("No service available");
            } else {
                for (Service s : selectedServices) {
                    services.addItem(s);
                }
                services.addAllOption();
            }

            cellFormatter.setColSpan(0, 0, 2);
            layout.setHTML(0, 0, "<p>Please add destinations for service configuration delivery. New service configuration can be performed directly on facility (dest. type HOST) or sent to URL or by email.");

            layout.setHTML(1, 0, "Destination:");
            layout.setWidget(1, 1, destination);

            layout.setHTML(2, 0, "Type:");
            layout.setWidget(2, 1, type);

            layout.setHTML(3, 0, "Service:");
            layout.setWidget(3, 1, services);

            layout.setWidget(4, 1, useHosts);

            for (int i=1; i<layout.getRowCount(); i++) {
                cellFormatter.addStyleName(i, 0, "itemName");
            }

            //callback
            final GetAllRichDestinations callback = new GetAllRichDestinations(facility, null);

            addButton.addClickHandler(new ClickHandler(){
                public void onClick(ClickEvent event) {
                    if (services.isEmpty()) {
                        // no services available
                        addButton.setEnabled(false);
                    }
                    if (destination.getText().equalsIgnoreCase("") && useHosts.getValue() == false) {
                        Confirm c = new Confirm("Wrong value", new Label("'Destination' can't be empty."), true);
                        c.show();
                        return;
                    }
                    if (services.getSelectedIndex() == 0) {
                        // selected all
                        if (useHosts.getValue() == true){
                            // auto by hosts
                            AddDestinationsByHostsOnFacility request = new AddDestinationsByHostsOnFacility(facility.getId(), JsonCallbackEvents.refreshTableEvents(callback));
                            request.addDestinationByHosts(services.getAllObjects());
                        } else {
                            // default
                            // FIXME - there are no services on facility at the moment, use list of services
                            for (int i=0; i<services.getAllObjects().size(); i++) {
                                if (i == services.getAllObjects().size()-1) {
                                    AddDestination request = new AddDestination(facility.getId(), services.getAllObjects().get(i).getId(), JsonCallbackEvents.refreshTableEvents(callback));
                                    request.addDestination(destination.getText().trim(), type.getValue(type.getSelectedIndex()));
                                } else {
                                    AddDestination request = new AddDestination(facility.getId(), services.getAllObjects().get(i).getId());
                                    request.addDestination(destination.getText().trim(), type.getValue(type.getSelectedIndex()));
                                }
                            }
                        }
                    } else {
                        // selected one
                        if (useHosts.getValue() == true){
                            // auto by hosts
                            AddDestinationsByHostsOnFacility request = new AddDestinationsByHostsOnFacility(facility.getId(), JsonCallbackEvents.refreshTableEvents(callback));
                            request.addDestinationByHosts(services.getSelectedObject());
                        } else {
                            // default
                            AddDestination request = new AddDestination(facility.getId(), services.getSelectedObject().getId(), JsonCallbackEvents.refreshTableEvents(callback));
                            request.addDestination(destination.getText().trim(), type.getValue(type.getSelectedIndex()));
                        }
                    }
                }
            });
            cellFormatter.setColSpan(5, 0, 2);
            layout.setWidget(5, 0, addButton);
            addButton.getElement().setAttribute("style", addButton.getElement().getAttribute("style")+" float: right;");

            // DESTINATIONS WIDGET

            // main content
            final VerticalPanel destWidget = new VerticalPanel();
            destWidget.setSize("100%", "100%");

            // menu
            final TabMenu menu = new TabMenu();
            destWidget.add(menu);
            destWidget.setCellHeight(menu, "30px");


            final CellTable<Destination> table = callback.getTable(); // do not make callback yet

            // refresh table events
            final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(callback);

            // style table
            table.addStyleName("perun-table");
            ScrollPanel sp = new ScrollPanel(table);
            sp.addStyleName("perun-tableScrollPanel");

            destWidget.add(sp);
            session.getUiElements().resizePerunTable(sp, 300, 50, this);

            final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeSelectedDestinations());
            menu.addWidget(removeButton);
            removeButton.addClickHandler(new ClickHandler(){
                public void onClick(ClickEvent event) {
                    final ArrayList<Destination> destForRemoving = callback.getTableSelectedList();
                    String text = "Following destinations will be removed. <strong>Removing destination will stop propagation of service configuration for this destination/service.</strong>";
                    UiElements.showDeleteConfirm(destForRemoving, text, new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent clickEvent) {
                            // TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
                            for (int i=0; i<destForRemoving.size(); i++ ) {
                                if (i == destForRemoving.size()-1) {
                                    RemoveDestination request = new RemoveDestination(facility.getId(), destForRemoving.get(i).getService().getId(), JsonCallbackEvents.disableButtonEvents(removeButton, events));
                                    request.removeDestination(destForRemoving.get(i).getDestination(), destForRemoving.get(i).getType());
                                } else {
                                    RemoveDestination request = new RemoveDestination(facility.getId(), destForRemoving.get(i).getService().getId(), JsonCallbackEvents.disableButtonEvents(removeButton));
                                    request.removeDestination(destForRemoving.get(i).getDestination(), destForRemoving.get(i).getType());
                                }
                            }
                        }
                    });
                }
            });

            removeButton.setEnabled(false);
            JsonUtils.addTableManagedButton(callback, table, removeButton);

            // filter box
            menu.addFilterWidget(new SuggestBox(callback.getOracle()), new PerunSearchEvent() {
                public void searchFor(String text) {
                    callback.filterTable(text);
                }
            }, ButtonTranslation.INSTANCE.filterDestination());


            // FILL LAYOUT

            hp.setWidget(0, 0, layout);
            hp.setWidget(0, 1, destWidget);
            hp.getFlexCellFormatter().setWidth(0, 0, "350px");
            hp.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
            hp.getFlexCellFormatter().setStyleName(0, 0, "border-right");

            content.add(hp);

            next.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    selectedPage++;
                    draw();
                }
            });

        } else if (selectedPage == 8) {

            session.getUiElements().getBreadcrumbs().setLocation(MainMenu.FACILITY_ADMIN, "Create facility "+selectedPage+" of "+numberOfPages+": Finish", "");

            SimplePanel sp = new SimplePanel();
            sp.getElement().setId("centered-wrapper-inner");

            session.getUiElements().resizePerunTable(sp, 350, 50, this);

            FlexTable ft = new FlexTable();
            ft.setSize("100%","100%");
            ft.setCellPadding(5);
            ft.getElement().setId("centered-content");

            HTML text = new HTML("Your facility was created and configured.<p>You can exit or continue by creating resource(s) for VOs.");
            text.setStyleName("now-managing");
            ft.setWidget(0, 0, text);

            if (sourceFacility != null) {

                CustomButton copyResources = new CustomButton("Copy resources from source facility", SmallIcons.INSTANCE.copyIcon());
                copyResources.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        //session.getTabManager().closeTab(tab);
                    }
                });

                CustomButton createResource = new CustomButton("Create new resource", SmallIcons.INSTANCE.addIcon());
                createResource.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        session.getTabManager().addTabToCurrentTab(new CreateFacilityResourceTabItem(facility), false);
                        //session.getTabManager().closeTab(tab);
                    }
                });

                // TODO - when there will be new copy resources wizard
                //ft.setWidget(1, 0, copyResources);
                ft.setWidget(2, 0, createResource);



            } else {

                CustomButton createResource = new CustomButton("Create new resource", SmallIcons.INSTANCE.addIcon());
                createResource.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        session.getTabManager().addTabToCurrentTab(new CreateFacilityResourceTabItem(facility), false);
                        //session.getTabManager().closeTab(tab);
                    }
                });

                ft.setWidget(1, 0, createResource);

            }

            CustomButton finish = new CustomButton("Exit", "Exit wizard", SmallIcons.INSTANCE.doorOutIcon());
            finish.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    session.getTabManager().closeTab(tab);
                }
            });

            footer.add(finish);
            finish.getElement().setAttribute("style", "position: absolute; right: 5px; bottom: 10px;");

            sp.add(ft);
            content.add(sp);

        }

        vp.add(content);
        vp.setCellHeight(content, "100%");
        vp.add(footer);

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
        final int prime = 31;
        int result = 1;
        result = prime * result + 12341;
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


        return true;
    }

    public boolean multipleInstancesEnabled() {
        return false;
    }

    public void open()
    {
        session.getUiElements().getMenu().openMenu(MainMenu.FACILITY_ADMIN);

        if (selectedPage == 1) {

            session.getUiElements().getBreadcrumbs().setLocation(MainMenu.FACILITY_ADMIN, "Create facility "+selectedPage+" of "+numberOfPages+": Definition", "");

        } else if (selectedPage == 2) {

            session.getUiElements().getBreadcrumbs().setLocation(MainMenu.FACILITY_ADMIN, "Create facility "+selectedPage+" of "+numberOfPages+": Add managers", "");

        } else if (selectedPage == 3) {

            session.getUiElements().getBreadcrumbs().setLocation(MainMenu.FACILITY_ADMIN, "Create facility "+selectedPage+" of "+numberOfPages+": Add owners", "");

        } else if (selectedPage == 4) {

            session.getUiElements().getBreadcrumbs().setLocation(MainMenu.FACILITY_ADMIN, "Create facility "+selectedPage+" of "+numberOfPages+": Add Host", "");

        } else if (selectedPage == 5) {

            session.getUiElements().getBreadcrumbs().setLocation(MainMenu.FACILITY_ADMIN, "Create facility "+selectedPage+" of "+numberOfPages+": Select services", "");

        } else if (selectedPage == 6) {

            session.getUiElements().getBreadcrumbs().setLocation(MainMenu.FACILITY_ADMIN, "Create facility "+selectedPage+" of "+numberOfPages+": Configure services", "");

        } else if (selectedPage == 7) {

            session.getUiElements().getBreadcrumbs().setLocation(MainMenu.FACILITY_ADMIN, "Create facility "+selectedPage+" of "+numberOfPages+": Add service destinations", "");

        } else if (selectedPage == 8) {

            session.getUiElements().getBreadcrumbs().setLocation(MainMenu.FACILITY_ADMIN, "Create facility "+selectedPage+" of "+numberOfPages+": Finish", "");

        }

    }

    public boolean isAuthorized() {

        if (session.isFacilityAdmin()) {
            return true;
        } else {
            return false;
        }

    }

    static public CreateFacilityTabItem load(Map<String, String> parameters)
    {
        return new CreateFacilityTabItem();
    }

}