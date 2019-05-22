package cz.metacentrum.perun.webgui.tabs.facilitiestabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.*;
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
import cz.metacentrum.perun.webgui.json.authzResolver.GetAdminGroups;
import cz.metacentrum.perun.webgui.json.authzResolver.GetRichAdminsWithAttributes;
import cz.metacentrum.perun.webgui.json.authzResolver.RemoveAdmin;
import cz.metacentrum.perun.webgui.json.facilitiesManager.*;
import cz.metacentrum.perun.webgui.json.servicesManager.*;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.FacilitiesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.groupstabs.GroupDetailTabItem;
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
	private boolean visitedAdmins = false;
	private boolean visitedOwners = false;
	private boolean visitedConfigure = false;
	private int selectedDropDownIndex = 0;

	private String newHostInput = "";

	private ArrayList<Service> selectedServices = new ArrayList<Service>();
	private ArrayList<Facility> facilitiesToCopyFrom = new ArrayList<Facility>();
	private ArrayList<Host> newFacilityHosts = new ArrayList<Host>();

	private JsonCallbackEvents eventsOnClose = new JsonCallbackEvents();

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

	/**
	 * Creates a tab instance
	 *
	 * @param facsToCopyFrom list of facilities to allow copy from
	 */
	public CreateFacilityTabItem(ArrayList<Facility> facsToCopyFrom){
		this.facilitiesToCopyFrom = facsToCopyFrom;
	}

	/**
	 * Creates a tab instance
	 *
	 * @param facsToCopyFrom list of facilities to allow copy from
	 * @param eventsOnClose when closed trigger onFinished() event
	 */
	public CreateFacilityTabItem(ArrayList<Facility> facsToCopyFrom, JsonCallbackEvents eventsOnClose){
		this.facilitiesToCopyFrom = facsToCopyFrom;
		if (eventsOnClose != null) {
			this.eventsOnClose = eventsOnClose;
		}
	}

	public boolean isPrepared(){
		return true;
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {
		if (eventsOnClose != null) eventsOnClose.onFinished(null);
	}

	public Widget draw() {

		final TabItemWithUrl tab = this;

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// FOOTER
		FlexTable header = new FlexTable();
		header.setStyleName("wizardHeader");

		final CustomButton back = TabMenu.getPredefinedButton(ButtonType.BACK, ButtonTranslation.INSTANCE.backButton());
		final CustomButton next = TabMenu.getPredefinedButton(ButtonType.CONTINUE, ButtonTranslation.INSTANCE.continueButton());

		back.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// skipped services selection
				if (selectedPage == 8 && selectedServices.isEmpty()) {
					selectedPage = 5;
				} else if (selectedPage >2) {
					selectedPage--;
				}
				draw();
			}
		});

		int column = 0;

		header.setWidget(0, 0, new Image(LargeIcons.INSTANCE.databaseServerIcon()));
		column++;

		Label title = new Label();
		title.getElement().setAttribute("style", "font-size: 1.35em;");

		header.setWidget(0, column, title);
		column++;

		// do not get back to first page
		if (selectedPage > 2) {
			header.setWidget(0, column, back);
			column++;
		}

		if (selectedPage > 1 && selectedPage != numberOfPages) {
			header.setWidget(0, column, next);
		}

		// MAIN CONTENT

		SimplePanel content = new SimplePanel();
		content.getElement().setId("centered-wrapper-outer");

		if (selectedPage == 1) {

			// header
			title.setText("Create facility "+selectedPage+" of "+numberOfPages+": Create definition");

			// content
			FlexTable layout = new FlexTable();
			layout.setStyleName("inputFormFlexTable");
			FlexTable.FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

			// widgets
			final ExtendedTextBox name = new ExtendedTextBox();
			final ExtendedTextBox description = new ExtendedTextBox();
			final ListBoxWithObjects<Facility> copyOfFacility = new ListBoxWithObjects<Facility>();

			final ExtendedTextBox.TextBoxValidator validator = new ExtendedTextBox.TextBoxValidator() {
				@Override
				public boolean validateTextBox() {
					if (name.getTextBox().getText().trim().isEmpty()) {
						name.setError("Facility name can't be empty.");
						return false;
					} else if (!name.getTextBox().getText().trim().matches(Utils.FACILITY_NAME_MATCHER)) {
						name.setError("Name can contain only letters, numbers, dash, dot and underscore.");
						return false;
					} else {
						name.setOk();
						return true;
					}
				}
			};
			name.setValidator(validator);

			// default
			if (facilitiesToCopyFrom != null && !facilitiesToCopyFrom.isEmpty()) {
				copyOfFacility.addNotSelectedOption();
				copyOfFacility.addAllItems(new TableSorter<Facility>().sortByName(facilitiesToCopyFrom));
				copyOfFacility.setSelectedIndex(0);
			}

			// Add some standard form options
			layout.setHTML(0, 0, "Name:");
			layout.setWidget(0, 1, name);
			layout.setHTML(1, 0, "Description:");
			layout.setWidget(1, 1, description);
			layout.setHTML(2, 0, "As copy of:");
			layout.setWidget(2, 1, copyOfFacility);

			final CustomButton create = TabMenu.getPredefinedButton(ButtonType.CREATE, "Create new facility");
			TabMenu menu = new TabMenu();
			menu.addWidget(create);
			menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					UiElements.generateAlert("Confirmation", "Do you really want to exit create facility wizard ?", new ClickHandler() {
						@Override
						public void onClick(ClickEvent clickEvent) {
							session.getTabManager().closeTab(tab);
						}
					});
				}
			}));

			VerticalPanel innerContent = new VerticalPanel();

			innerContent.add(layout);
			innerContent.add(menu);
			innerContent.setCellHorizontalAlignment(menu, HasHorizontalAlignment.ALIGN_RIGHT);

			content.setWidget(innerContent);

			for (int i=0; i<layout.getRowCount(); i++) {
				cellFormatter.addStyleName(i, 0, "itemName");
			}

			final GetFacilities getFacs = new GetFacilities(false, new JsonCallbackEvents(){
				public void onFinished(JavaScriptObject jso) {
					copyOfFacility.removeNotSelectedOption();
					copyOfFacility.clear();
					ArrayList<Facility> fac = JsonUtils.jsoAsList(jso);
					if (fac.isEmpty() || fac == null) {
						copyOfFacility.addItem("No facilities available");
						return;
					}
					fac = new TableSorter<Facility>().sortByName(fac);
					facilitiesToCopyFrom.addAll(fac);
					copyOfFacility.addNotSelectedOption();
					copyOfFacility.addAllItems(fac);
					copyOfFacility.setSelectedIndex(0);
					next.setEnabled(true);
				}
				public void onError(PerunError error){
					next.setEnabled(true);
					copyOfFacility.removeNotSelectedOption();
					copyOfFacility.clear();
					copyOfFacility.addItem("Error while loading");

				}
				public void onLoadingStart(){
					next.setEnabled(false);
					copyOfFacility.removeNotSelectedOption();
					copyOfFacility.clear();
					copyOfFacility.addItem("Loading...");
				}
			});

			// load facilities if empty
			if (facilitiesToCopyFrom == null || facilitiesToCopyFrom.isEmpty()) {
				getFacs.retrieveData();
			}

			// next button
			create.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {

					if (!visitedBasic) {
						if (validator.validateTextBox()) {
							CreateFacility request = new CreateFacility(JsonCallbackEvents.disableButtonEvents(create, new JsonCallbackEvents() {
								public void onFinished(JavaScriptObject jso) {
									facility = jso.cast();
									// set new facility as editable in GUI
									session.getEditableFacilities().add(facility.getId());
									if (copyOfFacility.getSelectedIndex() > 0) {
										sourceFacility = copyOfFacility.getSelectedObject();
									}
									visitedBasic = true;
									selectedPage++;
									draw();
								}

								public void onLoadingStart() {
									copyOfFacility.setEnabled(false);
								}

								public void onError(PerunError error) {
									copyOfFacility.setEnabled(true);
								}
							}));
							request.createFacility(name.getTextBox().getText().trim(), description.getTextBox().getText().trim());
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
				if (facility.getDescription() != null) {
					description.getTextBox().setValue(facility.getDescription());
				}

				name.getTextBox().setEnabled(false);
				description.getTextBox().setEnabled(false);
				copyOfFacility.setEnabled(false);

				if (sourceFacility != null) {
					copyOfFacility.setVisible(true);
					copyOfFacility.addItem(sourceFacility);
					copyOfFacility.setSelected(sourceFacility, true);
				}

			}

		} else if (selectedPage == 2) {

			// header
			title.setText("Create facility "+selectedPage+" of "+numberOfPages+": Add managers");

			// content
			VerticalPanel innerContent = new VerticalPanel();
			innerContent.setSize("100%", "100%");

			// HORIZONTAL MENU
			final TabMenu menu = new TabMenu();

			final ListBox box = new ListBox();
			box.addItem("Users");
			box.addItem("Groups");
			box.setSelectedIndex(selectedDropDownIndex);

			final ScrollPanel sp = new ScrollPanel();
			sp.addStyleName("perun-tableScrollPanel");

			// request
			final GetRichAdminsWithAttributes admins = new GetRichAdminsWithAttributes(PerunEntity.FACILITY, facility.getId(), null);
			final GetAdminGroups adminGroups = new GetAdminGroups(PerunEntity.FACILITY, facility.getId());

			box.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {

					if (box.getSelectedIndex() == 0) {
						selectedDropDownIndex = 0;
						sp.setWidget(fillContentUsers(admins, menu));
					} else {
						selectedDropDownIndex = 1;
						sp.setWidget(fillContentGroups(adminGroups, menu));
					}

				}
			});

			// if first and copy from
			if (!visitedAdmins && sourceFacility != null) {

				CopyManagers copy = new CopyManagers(new JsonCallbackEvents(){
					@Override
					public void onFinished(JavaScriptObject jso) {
						if (selectedDropDownIndex == 0) {
							sp.setWidget(fillContentUsers(admins, menu));
						} else {
							sp.setWidget(fillContentGroups(adminGroups, menu));
						}
						menu.addWidget(2, new HTML("<strong>Select mode: </strong>"));
						menu.addWidget(3, box);
						menu.addWidget(4, new Image(SmallIcons.INSTANCE.helpIcon()));
						menu.addWidget(5, new HTML("<strong>People with privilege to manage this facility in Perun.</strong>"));
					}
				@Override
				public void onError(PerunError error) {
					if (selectedDropDownIndex == 0) {
						sp.setWidget(fillContentUsers(admins, menu));
					} else {
						sp.setWidget(fillContentGroups(adminGroups, menu));
					}
					menu.addWidget(2, new HTML("<strong>Select mode: </strong>"));
					menu.addWidget(3, box);
					menu.addWidget(4, new Image(SmallIcons.INSTANCE.helpIcon()));
					menu.addWidget(5, new HTML("<strong>People with privilege to manage this facility in Perun.</strong>"));
				}
				});
				copy.copyFacilityManagers(sourceFacility.getId(), facility.getId());

			} else {

				if (selectedDropDownIndex == 0) {
					sp.setWidget(fillContentUsers(admins, menu));
				} else {
					sp.setWidget(fillContentGroups(adminGroups, menu));
				}
				menu.addWidget(2, new HTML("<strong>Select mode: </strong>"));
				menu.addWidget(3, box);
				menu.addWidget(4, new Image(SmallIcons.INSTANCE.helpIcon()));
				menu.addWidget(5, new HTML("<strong>People with privilege to manage this facility in Perun. They aren't automatically \"roots\" on machine.</strong>"));

			}

			visitedAdmins = true;

			session.getUiElements().resizePerunTable(sp, 300, this);

			// add menu and the table to the main panel
			innerContent.add(menu);
			innerContent.setCellHeight(menu, "30px");
			innerContent.add(sp);

			content.setWidget(innerContent);

			next.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					selectedPage++;
					draw();
				}
			});

		} else if (selectedPage == 3) {

			// header
			title.setText("Create facility "+selectedPage+" of "+numberOfPages+": Add owners");

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
			CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.addNewOwners());
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

			// TABLE
			CellTable<Owner> table = jsonCallback.getEmptyTable();
			table.addStyleName("perun-table");
			ScrollPanel sp = new ScrollPanel(table);
			sp.addStyleName("perun-tableScrollPanel");

			innerContent.add(sp);

			// call for data
			if (!visitedOwners && sourceFacility != null) {
				// copy owners
				CopyOwners copy = new CopyOwners(new JsonCallbackEvents(){
					@Override
					public void onFinished(JavaScriptObject jso) {
						jsonCallback.retrieveData();
					}
				@Override
				public void onError(PerunError error) {
					jsonCallback.retrieveData();
				}
				});
				copy.copyFacilityOwners(sourceFacility.getId(), facility.getId());
			} else {
				jsonCallback.retrieveData();
			}

			removeButton.setEnabled(false);
			JsonUtils.addTableManagedButton(jsonCallback, table, removeButton);

			session.getUiElements().resizePerunTable(sp, 300, this);

			visitedOwners = true;

			next.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					selectedPage++;
					draw();
				}
			});
			content.add(innerContent);

		} else if (selectedPage == 4) {

			// header
			title.setText("Create facility " + selectedPage + " of " + numberOfPages + ": Add hosts");

			// SPLIT
			FlexTable hp = new FlexTable();
			hp.setSize("100%", "100%");

			// HOSTS WIDGET

			VerticalPanel hostsWidget = new VerticalPanel();
			hostsWidget.setSize("100%","100%");

			final GetHosts hosts = new GetHosts(facility.getId(), new JsonCallbackEvents(){
				@Override
				public void onFinished(JavaScriptObject jso) {
					// store hosts for future use
					newFacilityHosts.clear();
					newFacilityHosts.addAll(JsonUtils.<Host>jsoAsList(jso));
				}
			});
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

			final ExtendedTextArea newHosts = new ExtendedTextArea();
			newHosts.getTextArea().setSize("335px", "200px");
			newHosts.getTextArea().setText(newHostInput);

			final ExtendedTextArea.TextAreaValidator validator = new ExtendedTextArea.TextAreaValidator() {
				@Override
				public boolean validateTextArea() {
					if (newHosts.getTextArea().getText().trim().isEmpty()) {
						newHosts.setError("Please enter at least one hostname to add it to facility.");
						return false;
					} else {
						newHosts.setOk();
						return true;
					}
				}
			};
			newHosts.setValidator(validator);

			newHosts.getTextArea().addKeyUpHandler(new KeyUpHandler() {
				@Override
				public void onKeyUp(KeyUpEvent event) {
					newHostInput = newHosts.getTextArea().getText();
				}
			});

			final CustomButton addHostsButton = new CustomButton("Add", ButtonTranslation.INSTANCE.addHost(), SmallIcons.INSTANCE.arrowRightIcon());
			addHostsButton.setImageAlign(true);
			addHostsButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					if (validator.validateTextArea()) {
						String hostnames = newHosts.getTextArea().getText().trim();
						String hosts[] = hostnames.split("\n");
						// trim whitespace
						for (int i = 0; i< hosts.length; i++) {
							hosts[i] = hosts[i].trim();
						}
						AddHosts request = new AddHosts(facility.getId(), JsonCallbackEvents.mergeEvents(JsonCallbackEvents.disableButtonEvents(addHostsButton, events), new JsonCallbackEvents(){
							public void onFinished(JavaScriptObject jso) {
								// clear input
								newHosts.getTextArea().setText("");
								newHostInput = "";
							}
						}));
						request.addHosts(hosts);
					}
				}
			});

			// layout
			FlexTable layout = new FlexTable();
			layout.setStyleName("inputFormFlexTable");
			FlexTable.FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

			layout.setHTML(0, 0, "Hostnames:");
			layout.setWidget(1, 0, newHosts);
			cellFormatter.addStyleName(0, 0, "itemName");

			layout.setHTML(2, 0, "Enter one host per line. You can use \"[x-y]\" in hostname to generate hosts with numbers from x to y. This replacer can be specified multiple times in one hostname to generate MxN combinations.");
			cellFormatter.addStyleName(2, 0, "inputFormInlineComment");

			cellFormatter.setHorizontalAlignment(3, 0, HasHorizontalAlignment.ALIGN_RIGHT);
			layout.setWidget(3, 0, addHostsButton);
			addHostsButton.getElement().setAttribute("style", addHostsButton.getElement().getAttribute("style")+" float: right;");

			// FILL LAYOUT

			hp.setWidget(0, 0, layout);
			hp.setWidget(0, 1, hostsWidget);
			hp.getFlexCellFormatter().setWidth(0, 0, "350px");
			hp.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
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

			// header
			title.setText("Create facility " + selectedPage + " of " + numberOfPages + ": Select services");

			// SPLIT
			FlexTable hp = new FlexTable();
			hp.setSize("100%", "100%");

			// create widget for the whole page
			VerticalPanel mainTab = new VerticalPanel();
			mainTab.setSize("100%", "100%");

			// get services
			final GetServices services = new GetServices();
			services.setEvents(new JsonCallbackEvents(){
				public void onFinished(JavaScriptObject jso){
					if (selectedServices != null && !selectedServices.isEmpty()) {
						for (Service s : selectedServices) {
							services.getSelectionModel().setSelected(s, true);
						}
					}
				}
				public void onLoadingStart() {

				}
				public void onError(PerunError error) {

				}
			});

			// get the table of services with custom field updater (lines are clickable and open service details)
			CellTable<Service> table = services.getTable();

			// add styling to table with services
			table.addStyleName("perun-table");
			ScrollPanel sp = new ScrollPanel(table);
			sp.addStyleName("perun-tableScrollPanel");
			mainTab.add(sp);

			final VerticalPanel helpWidget = new VerticalPanel();
			helpWidget.setSpacing(5);
			final FlowPanel fw = new FlowPanel();

			CustomButton clearButton = new CustomButton("Clear selection", "Clear services selection.", SmallIcons.INSTANCE.deleteIcon());
			clearButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					services.clearTableSelectedSet();
				}
			});
			clearButton.addStyleName("margin");
			fw.add(clearButton);
			fw.add(new AjaxLoaderImage(true));

			GetServicesPackages packs = new GetServicesPackages(new JsonCallbackEvents(){
				@Override
				public void onFinished(JavaScriptObject jso) {
					// remove loader
					fw.remove(1);
					// fill buttons based on packages
					ArrayList<ServicesPackage> packages = JsonUtils.jsoAsList(jso);
					for (final ServicesPackage pack : packages) {
						final CustomButton button = new CustomButton(pack.getName(), pack.getDescription(), SmallIcons.INSTANCE.addIcon());
						button.addStyleName("margin");
						fw.add(button);
						button.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								GetServicesFromServicesPackage serv = new GetServicesFromServicesPackage(pack.getId(), JsonCallbackEvents.disableButtonEvents(button, new JsonCallbackEvents(){
									@Override
									public void onFinished(JavaScriptObject jso) {
										for (Service s : JsonUtils.<Service>jsoAsList(jso)) {
											// select services from pack in table
											services.getSelectionModel().setSelected(s, true);
										}
									}
								}));
								serv.retrieveData();
							}
						});
					}
					if (packages == null || packages.isEmpty()) {
						fw.add(new HTML("There are no services packages defined in Perun. Use manual selection."));
					}
				}
			@Override
			public void onError(PerunError error) {
				// remove loader
				fw.remove(1);
				fw.add(new HTML("Error when loading services packages defined in Perun. Use manual selection."));
			}
			});
			packs.retrieveData();

			HTML helpText = new HTML("Please select set of services, which will be managed by Perun. You can use buttons below to help you select proper set of services.");
			helpText.setStyleName("inputFormInlineComment");
			helpWidget.add(helpText);
			helpWidget.add(fw);

			// select by source if not already set
			if (sourceFacility != null && (selectedServices == null || selectedServices.isEmpty())) {

				GetFacilityAssignedServices getCall = new GetFacilityAssignedServices(sourceFacility.getId(), new JsonCallbackEvents() {
					@Override
					public void onFinished(JavaScriptObject jso) {
						ArrayList<Service> serv = JsonUtils.jsoAsList(jso);
						services.clearTableSelectedSet();
						for (Service s : serv) {
							services.getSelectionModel().setSelected(s, true);
						}
					}
				});
				getCall.retrieveData();

			}

			hp.setWidget(0, 0, helpWidget);
			hp.setWidget(0, 1, mainTab);
			hp.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
			hp.getFlexCellFormatter().setWidth(0, 0, "350px");
			hp.getFlexCellFormatter().setStyleName(0, 0, "border-right");

			content.setWidget(hp);

			next.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (services.getTableSelectedList() == null || services.getTableSelectedList().isEmpty()) {

						FlexTable layout = new FlexTable();

						layout.setWidget(0, 0, new HTML("<p>" + new Image(LargeIcons.INSTANCE.informationIcon())));
						layout.setHTML(0, 1, "<p>" + "You didn't select any service. Do you wish to skip services configuration ?");

						layout.getFlexCellFormatter().setAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
						layout.getFlexCellFormatter().setAlignment(0, 1, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_TOP);
						layout.getFlexCellFormatter().setStyleName(0, 0, "alert-box-image");

						final Confirm c = new Confirm("No service selected", layout, true);
						c.setOkButtonText("Yes");
						c.setCancelButtonText("No");
						c.setOkClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								selectedServices.clear();
								// draw
								selectedPage = 8;
								draw();
							}
						});
						c.setCancelClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								c.hide();
							}
						});
						c.setNonScrollable(true);
						c.show();


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

			session.getUiElements().resizePerunTable(sp, 350, this);

		} else if (selectedPage == 6) {

			// header
			title.setText("Create facility " + selectedPage + " of " + numberOfPages + ": Configure services");

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
						// set to new facility
						ids.put("facility", facility.getId());
						request.setAttributes(ids, reqAttrs.getTableSelectedList());
					}
				}
			});

			// clear
			servList.removeAllOption();
			servList.clear();
			// fill
			if (selectedServices == null || selectedServices.isEmpty()) {
				// wizard shouldn't allow to get there
				servList.addItem("No service selected");
				servList.setEnabled(false);
			} else if (!visitedConfigure && sourceFacility != null) {
				// first visit and copy
				ids.put("facility", sourceFacility.getId());
				servList.addAllItems(selectedServices);
				servList.addAllOption();
				// default all
				servList.setSelectedIndex(0);
				reqAttrs.setEvents(new JsonCallbackEvents(){
					@Override
					public void onFinished(JavaScriptObject jso) {
						for (Attribute a : reqAttrs.getList()) {
							if (!a.getDefinition().equals("virt") && a.getValue() != null && !a.getValue().isEmpty()) {
								// pre-select non-virt attributes with some value
								reqAttrs.getSelectionModel().setSelected(a, true);
							} else {
								reqAttrs.getSelectionModel().setSelected(a, false);
							}
						}
					}
				});
				reqAttrs.setServicesToGetAttributesFor(servList.getAllObjects());
				reqAttrs.setIds(ids);
				reqAttrs.clearTable();
				reqAttrs.retrieveData();
				// inform user about it
				UiElements.generateInfo("Pre-filled values", "Services configuration was pre-filled from facility you selected to copy. <p><strong>Nothing is saved to new facility until you click on \"Save\" button.</strong>");
			} else {
				servList.addAllItems(selectedServices);
				servList.addAllOption();
				// default all
				servList.setSelectedIndex(0);
				reqAttrs.setServicesToGetAttributesFor(servList.getAllObjects());
				reqAttrs.setIds(ids);
				reqAttrs.clearTable();
				reqAttrs.retrieveData();
			}

			servList.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					if (servList.getSelectedIndex() == 0) {
						ids.remove("service");
						reqAttrs.setServicesToGetAttributesFor(servList.getAllObjects());
					} else {
						ids.put("service", servList.getSelectedObject().getId());
						reqAttrs.setServicesToGetAttributesFor(null);
					}
					reqAttrs.setIds(ids);
					reqAttrs.clearTable();
					reqAttrs.retrieveData();
				}
			});

			visitedConfigure = true;

			menu.addWidget(saveChangesButton);

			menu.addWidget(new HTML("<strong>Filter view by Service: </strong>"));
			menu.addWidget(servList);

			table.addStyleName("perun-table");
			ScrollPanel sp = new ScrollPanel(table);
			sp.addStyleName("perun-tableScrollPanel");

			// add menu and the table to the main panel
			settingsTab.add(menu);
			settingsTab.setCellHeight(menu, "30px");
			settingsTab.add(sp);

			session.getUiElements().resizePerunTable(sp, 350, this);

			content.add(settingsTab);

			next.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					selectedPage++;
					draw();
				}
			});

		} else if (selectedPage == 7) {

			// header
			title.setText("Create facility " + selectedPage + " of " + numberOfPages + ": Configure service destinations");

			// SPLIT
			FlexTable hp = new FlexTable();
			hp.setSize("100%", "100%");

			FlexTable layout = new FlexTable();
			layout.setStyleName("inputFormFlexTable");
			FlexTable.FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();
			layout.setWidth("350px");

			final ExtendedSuggestBox destination = new ExtendedSuggestBox();
			final ListBox type = new ListBox();
			type.addItem("HOST","host");
			type.addItem("USER@HOST", "user@host");
			type.addItem("USER@HOST:PORT", "user@host:port");
			type.addItem("URL","url");
			type.addItem("MAIL","email");
			type.addItem("SIGNED MAIL","semail");
			type.addItem("SERVICE SPECIFIC","service-specific");

			final ListBox propTypeSelect = new ListBox();
			propTypeSelect.addItem("PARALLEL");
			propTypeSelect.addItem("DUMMY");
			//propTypeSelect.addItem("SERIAL"); TODO - will we ever use it ?
			final HTML propTypeHelp = new HTML("PARALLEL - Data for all destinations of one service are pushed in parallel.");

			destination.getSuggestOracle().clear();
			for (Host h : newFacilityHosts) {
				destination.getSuggestOracle().add(h.getName());
			}

			final Label destinationLabel = new Label();
			destinationLabel.getElement().setInnerHTML("<strong>Host:</strong>");

			final CustomButton addButton = new CustomButton("Add", ButtonTranslation.INSTANCE.addDestination(), SmallIcons.INSTANCE.arrowRightIcon());
			addButton.setImageAlign(true);

			final ListBoxWithObjects<Service> services = new ListBoxWithObjects<Service>();
			final CheckBox useHosts = new CheckBox(WidgetTranslation.INSTANCE.useFacilityHostnames(), false);
			useHosts.setTitle(WidgetTranslation.INSTANCE.useFacilityHostnamesTitle());


			final ExtendedSuggestBox.SuggestBoxValidator validator = new ExtendedSuggestBox.SuggestBoxValidator() {
				@Override
				public boolean validateSuggestBox() {
					if (destination.getSuggestBox().getText().trim().isEmpty() && useHosts.getValue() == false) {
						destination.setError("Destination value can't be empty.");
						return false;
					}
					// check as email
					if (type.getSelectedIndex() > 3 && type.getSelectedIndex() < 6) {
						if (!JsonUtils.isValidEmail(destination.getSuggestBox().getText().trim())) {
							destination.setError("Not valid email address.");
							return false;
						} else {
							destination.setOk();
							return true;
						}
					}
					destination.setOk();
					return true;
				}
			};
			destination.setValidator(validator);

			type.addChangeHandler(new ChangeHandler(){
				public void onChange(ChangeEvent event) {
					// if hosts - checkbox visible
					if (type.getSelectedIndex() == 0) {
						useHosts.setVisible(true);
					} else {
						useHosts.setVisible(false);
						useHosts.setValue(false);
						destination.getSuggestBox().setEnabled(true);
					}

					if (type.getSelectedIndex() < 3) {
						destination.getSuggestOracle().clear();
						for (Host h : newFacilityHosts) {
							destination.getSuggestOracle().add(h.getName());
						}
					} else {
						destination.getSuggestOracle().clear();
					}

					// set label
					if (type.getSelectedIndex() == 0) {
						destinationLabel.getElement().setInnerHTML("<strong>Host:</strong>");
					} else if (type.getSelectedIndex() == 1) {
						destinationLabel.getElement().setInnerHTML("<strong>User@host:</strong>");
					} else if (type.getSelectedIndex() == 2) {
						destinationLabel.getElement().setInnerHTML("<strong>User@host:port:</strong>");
					} else if (type.getSelectedIndex() == 3) {
						destinationLabel.getElement().setInnerHTML("<strong>URL:</strong>");
					} else if (type.getSelectedIndex() == 4) {
						destinationLabel.getElement().setInnerHTML("<strong>Mail:</strong>");
					} else if (type.getSelectedIndex() == 5) {
						destinationLabel.getElement().setInnerHTML("<strong>Signed mail:</strong>");
					} else if (type.getSelectedIndex() == 6) {
						destinationLabel.getElement().setInnerHTML("<strong>Service specific:</strong>");
					}

				}
			});

			useHosts.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					if (useHosts.getValue() == true) {
						destination.getSuggestBox().setEnabled(false);
						destination.setOk();
						propTypeSelect.setSelectedIndex(0); // use only PARALLEL since API doesn't read it
						propTypeHelp.setHTML("PARALLEL - Data for all destinations and one service are pushed in parallel.");
					} else {
						destination.getSuggestBox().setEnabled(true);
					}
				}
			});

			propTypeSelect.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent changeEvent) {

					if (propTypeSelect.getSelectedIndex() == 0) {
						propTypeHelp.setHTML("PARALLEL - Data for all destinations and one service are pushed in parallel.");
					} else {
						propTypeHelp.setHTML("DUMMY - Service provisioning data is generated by Perun, but not pushed to destination. Destinations can pull data by themselves.");
						// allow to set custom value - can't use facility hosts
						useHosts.setValue(false);
						destination.getSuggestBox().setEnabled(true);
					}

				}
			});

			services.removeAllOption();
			services.clear();
			if (selectedServices == null || selectedServices.isEmpty()) {
				// wizard shouldn't allow
				services.addItem("No service available");
			} else {
				services.addAllItems(selectedServices);
				services.addAllOption();
				services.setSelectedIndex(0);
			}

			cellFormatter.setColSpan(0, 0, 2);
			HTML text = new HTML("Please add destinations for service configuration delivery. New service configuration can be performed directly on facility (dest. type HOST) or sent to URL or by an email.");
			text.setStyleName("inputFormInlineComment");
			layout.setWidget(0, 0, text);

			layout.setHTML(1, 0, "Service:");
			layout.setWidget(1, 1, services);

			layout.setHTML(2, 0, "Type:");
			layout.setWidget(2, 1, type);

			layout.setWidget(3, 0, destinationLabel);
			layout.setWidget(3, 1, destination);

			layout.setWidget(4, 1, useHosts);

			layout.setHTML(5, 0, "Propagation:");
			layout.setWidget(5, 1, propTypeSelect);

			for (int i=1; i<layout.getRowCount(); i++) {
				cellFormatter.addStyleName(i, 0, "itemName");
			}

			propTypeHelp.setStyleName("inputFormInlineComment");
			layout.setWidget(6, 0, propTypeHelp);
			cellFormatter.setColSpan(6, 0, 2);

			//callback
			final GetAllRichDestinations callback = new GetAllRichDestinations(facility, null);

			addButton.addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event) {
					if (services.isEmpty()) {
						// no services available
						addButton.setEnabled(false);
					}

					if (validator.validateSuggestBox()) {

						if (services.getSelectedIndex() == 0) {
							// selected all
							if (useHosts.getValue() == true){
								// auto by hosts
								AddDestinationsByHostsOnFacility request = new AddDestinationsByHostsOnFacility(facility, JsonCallbackEvents.refreshTableEvents(callback));
								request.addDestinationByHosts(services.getAllObjects());
							} else {
								// default
								// FIXME - there are no services on facility at the moment, use list of services
								AddDestination request = new AddDestination(facility, JsonCallbackEvents.refreshTableEvents(callback));
								request.addDestination(destination.getSuggestBox().getText().trim(), type.getValue(type.getSelectedIndex()), services.getAllObjects(), propTypeSelect.getSelectedValue());
							}
						} else {
							// selected one
							if (useHosts.getValue() == true){
								// auto by hosts
								AddDestinationsByHostsOnFacility request = new AddDestinationsByHostsOnFacility(facility, JsonCallbackEvents.refreshTableEvents(callback));
								request.addDestinationByHosts(services.getSelectedObject());
							} else {
								// default
								AddDestination request = new AddDestination(facility, JsonCallbackEvents.refreshTableEvents(callback));
								request.addDestination(destination.getSuggestBox().getText().trim(), type.getValue(type.getSelectedIndex()), services.getSelectedObject(), propTypeSelect.getSelectedValue());
							}
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
			menu.addFilterWidget(new ExtendedSuggestBox(callback.getOracle()), new PerunSearchEvent() {
				public void searchFor(String text) {
					callback.filterTable(text);
				}
			}, ButtonTranslation.INSTANCE.filterDestination());


			// FILL LAYOUT

			hp.setWidget(0, 0, layout);
			hp.setWidget(0, 1, destWidget);
			hp.getFlexCellFormatter().setWidth(0, 0, "350px");
			hp.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
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

			// header
			title.setText("Create facility " + selectedPage + " of " + numberOfPages + ": Finish");

			SimplePanel sp = new SimplePanel();
			sp.getElement().setId("centered-wrapper-inner");

			session.getUiElements().resizePerunTable(sp, 350, this);

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

				CustomButton createResource = new CustomButton("Create new resource…", SmallIcons.INSTANCE.addIcon());
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

				CustomButton createResource = new CustomButton("Create new resource…", SmallIcons.INSTANCE.addIcon());
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

			ft.setWidget(3, 0, finish);

			sp.add(ft);
			content.add(sp);

		}

		vp.add(header);
		vp.add(new HTML("<hr size=\"1\" color=\"#ccc\">"));
		vp.add(content);
		vp.setCellHeight(content, "100%");

		this.contentWidget.setWidget(vp);

		return getWidget();
	}

	private Widget fillContentUsers(final GetRichAdminsWithAttributes jsonCallback, TabMenu menu) {

		jsonCallback.clearTableSelectedSet();

		// get the table
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

		menu.addWidget(0, TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.addManagerToFacility(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new AddFacilityManagerTabItem(facility), true);
			}
		}));

		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeManagerFromFacility());
		menu.addWidget(1, removeButton);
		removeButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				final ArrayList<User> list = jsonCallback.getTableSelectedList();
				String text = "Following users won't be facility managers anymore and won't be able to manage this facility in Perun.";
				UiElements.showDeleteConfirm(list, text, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
						for (int i=0; i<list.size(); i++) {
							if (i == list.size()-1) {
								RemoveAdmin request = new RemoveAdmin(PerunEntity.FACILITY, JsonCallbackEvents.disableButtonEvents(removeButton, JsonCallbackEvents.refreshTableEvents(jsonCallback)));
								request.removeFacilityAdmin(facility, list.get(i));
							} else {
								RemoveAdmin request = new RemoveAdmin(PerunEntity.FACILITY, JsonCallbackEvents.disableButtonEvents(removeButton));
								request.removeFacilityAdmin(facility, list.get(i));
							}
						}
					}
				});
			}
		});

		removeButton.setEnabled(false);
		JsonUtils.addTableManagedButton(jsonCallback, table, removeButton);

		table.addStyleName("perun-table");

		return table;

	}

	private Widget fillContentGroups(final GetAdminGroups jsonCallback, TabMenu menu) {

		jsonCallback.clearTableSelectedSet();

		// get the table
		CellTable<Group> table = jsonCallback.getTable(new FieldUpdater<Group, String>() {
			@Override
			public void update(int i, Group grp, String s) {
				session.getTabManager().addTab(new GroupDetailTabItem(grp));
			}
		});

		menu.addWidget(0, TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.addManagerGroupToFacility(), new ClickHandler() {
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new AddFacilityManagerGroupTabItem(facility, JsonCallbackEvents.refreshTableEvents(jsonCallback)), true);
			}
		}));

		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeManagerGroupFromFacility());
		menu.addWidget(1, removeButton);
		removeButton.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				final ArrayList<Group> list = jsonCallback.getTableSelectedList();
				String text = "Members of following groups won't be facility managers anymore and won't be able to manage this facility in Perun.";
				UiElements.showDeleteConfirm(list, text, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE !!
						for (int i=0; i<list.size(); i++) {
							if (i == list.size()-1) {
								RemoveAdmin request = new RemoveAdmin(PerunEntity.FACILITY, JsonCallbackEvents.disableButtonEvents(removeButton, JsonCallbackEvents.refreshTableEvents(jsonCallback)));
								request.removeFacilityAdminGroup(facility, list.get(i));
							} else {
								RemoveAdmin request = new RemoveAdmin(PerunEntity.FACILITY, JsonCallbackEvents.disableButtonEvents(removeButton));
								request.removeFacilityAdminGroup(facility, list.get(i));
							}
						}
					}
				});
			}
		});

		removeButton.setEnabled(false);
		JsonUtils.addTableManagedButton(jsonCallback, table, removeButton);

		table.addStyleName("perun-table");

		return table;

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
		final int prime = 673;
		int result = 1;
		result = prime * result + 12341;
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
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
		session.getUiElements().getMenu().openMenu(MainMenu.FACILITY_ADMIN);
		session.getUiElements().getBreadcrumbs().setLocation(MainMenu.FACILITY_ADMIN, "Create facility", "");
	}

	public boolean isAuthorized() {

		if (session.isFacilityAdmin()) {
			return true;
		} else {
			return false;
		}

	}

	static public CreateFacilityTabItem load(Map<String, String> parameters) {
		return new CreateFacilityTabItem();
	}

}
