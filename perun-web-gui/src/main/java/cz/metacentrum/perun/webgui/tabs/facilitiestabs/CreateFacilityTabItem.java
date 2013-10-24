package cz.metacentrum.perun.webgui.tabs.facilitiestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.facilitiesManager.AddHosts;
import cz.metacentrum.perun.webgui.json.facilitiesManager.AddOwner;
import cz.metacentrum.perun.webgui.json.facilitiesManager.CreateFacility;
import cz.metacentrum.perun.webgui.json.ownersManager.GetOwners;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.Owner;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.tabs.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;

/**
 * FACILITY ADMIN / PERUN ADMIN - Create facility wizard - page 1
 * !! USE AS INNER TAB ONLY !!
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class CreateFacilityTabItem implements TabItem {

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
    private boolean callDone = false;
	
	/**
	 * Creates a tab instance
     */
	public CreateFacilityTabItem(){}
	
	public boolean isPrepared(){
		return true;
	}

	public Widget draw() {

        final VerticalPanel vp = new VerticalPanel();
        vp.setSize("100%", "100%");

        FlexTable layout = new FlexTable();
        layout.setStyleName("inputFormFlexTable");
        FlexTable.FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

		// widgets
		final TextBox name = new TextBox();
		final TextBox hosts = new TextBox();
		final ListBox type = new ListBox();
		final ListBoxWithObjects<Owner> ownersList = new ListBoxWithObjects<Owner>();
		
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
        layout.setHTML(2, 0, "Owner:");
        layout.setWidget(2, 1, ownersList);
        layout.setHTML(3, 0, "Hosts:");
        layout.setWidget(3, 1, hosts);
        layout.setHTML(4, 1, "For \"cluster\" & \"vcluster\" only<br/>(names of hosts separated by comas).");
        cellFormatter.setStyleName(4, 1, "inputFormInlineComment");

        for (int i=0; i<layout.getRowCount(); i++) {
            cellFormatter.addStyleName(i, 0, "itemName");
        }

        final CustomButton createButton = TabMenu.getPredefinedButton(ButtonType.CREATE, ButtonTranslation.INSTANCE.createFacility());

        // fill owners
		GetOwners ownerCallback = new GetOwners(new JsonCallbackEvents(){
			public void onError(PerunError error) {
                ownersList.clear();
				ownersList.addItem("Error while loading");
                callDone = false;
			}
			public void onFinished(JavaScriptObject jso) {
				ownersList.clear();
                ArrayList<Owner> own = JsonUtils.jsoAsList(jso);
				if (own.isEmpty() || own == null) {
					ownersList.addItem("No owners available");
					return;
				}
                own = new TableSorter<Owner>().sortByName(own);
				for (int i=0; i<own.size(); i++){
					ownersList.addItem(own.get(i));
				}
				ownersList.addNotSelectedOption();
				ownersList.setSelectedIndex(0);
                callDone = true;
                if (!name.getText().isEmpty()) {
                    createButton.setEnabled(true);
                } else {
                    createButton.setEnabled(false);
                }
			}
            public void onLoadingStart() {
                ownersList.addItem("Loading...");
                callDone = false;
                createButton.setEnabled(false);
            }
		});
		ownerCallback.retrieveData();

        // menu
        TabMenu menu = new TabMenu();
        vp.add(layout);
        vp.add(menu);
        vp.setCellHorizontalAlignment(menu, HasHorizontalAlignment.ALIGN_RIGHT);

        final TabItem tab = this;
        menu.addWidget(createButton);
        menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                session.getTabManager().closeTab(tab, false);
            }
        }));

		// click handler for first button
		createButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {

                // after successful creation of facility add selected owner to it
                JsonCallbackEvents localEvents = JsonCallbackEvents.disableButtonEvents(createButton, new JsonCallbackEvents() {
                    public void onFinished(JavaScriptObject jso) {

                        facility = jso.cast();  // handle returned facility from creating

                        if (ownersList.getSelectedIndex() > 0) {
                            AddOwner request = new AddOwner();
                            request.addOwner(facility.getId(), ownersList.getSelectedObject().getId());
                        } else {
                            session.getUiElements().setLogText("No owner selected or null. Adding owner to facility skipped.");
                        }

                        // if cluster type - add hosts entered in text field
                        if (type.getValue(type.getSelectedIndex()).equalsIgnoreCase("cluster") || type.getValue(type.getSelectedIndex()).equalsIgnoreCase("vcluster")) {
                            if (!hosts.getText().equalsIgnoreCase("")) {
                                String hostnames = hosts.getText();
                                String hosts[] = hostnames.split(",");
                                // trim whitespace
                                for (int i = 0; i < hosts.length; i++) {
                                    hosts[i] = hosts[i].trim();
                                }
                                AddHosts request2 = new AddHosts(facility.getId());
                                request2.addHosts(hosts);
                            } else {
                                session.getUiElements().setLogText("No hostnames entered, option skipped.");
                            }
                        }

                        vp.clear();

                        session.getTabManager().changeStyleOfInnerTab(true);

                        // add button to top
                        TabMenu menu2 = new TabMenu();
                        CustomButton finishButton = TabMenu.getPredefinedButton(ButtonType.FINISH, ButtonTranslation.INSTANCE.finishFacilityConfiguration(), new ClickHandler() {
                            public void onClick(ClickEvent event) {
                                session.getTabManager().closeTab(tab);
                            }
                        });
                        menu2.addWidget(finishButton);

                        vp.add(menu2);
                        vp.setCellHeight(menu2, "30px");

                        // add services attributes configuration form with hidden services switch
                        FacilitySettingsTabItem settingsTab = new FacilitySettingsTabItem(facility);
                        settingsTab.hideServicesSwitch(true);
                        vp.add(settingsTab.draw());

                    }
                });
                // create facility
                CreateFacility request = new CreateFacility(localEvents);
                request.createFacility(name.getText().trim(), type.getValue(type.getSelectedIndex()));
            }
        });

        name.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (callDone && !name.getText().isEmpty()) {
                    createButton.setEnabled(true);
                } else {
                    createButton.setEnabled(false);
                }
            }
        });

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
	}

	public boolean isAuthorized() {

		if (session.isPerunAdmin()) { 
			return true; 
		} else {
			return false;
		}

	}

}