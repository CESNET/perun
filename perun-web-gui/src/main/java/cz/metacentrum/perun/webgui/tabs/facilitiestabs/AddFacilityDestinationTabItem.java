package cz.metacentrum.perun.webgui.tabs.facilitiestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.facilitiesManager.GetHosts;
import cz.metacentrum.perun.webgui.json.servicesManager.*;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.Host;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Service;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

import java.util.ArrayList;

/**
 * Provides tab for adding destination to facility
 *
 * !! USE AS INNER TAB ONLY !!
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class AddFacilityDestinationTabItem implements TabItem {

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
	private Label titleWidget = new Label("Add destination");

	// data
	private Facility facility;
	private int facilityId;

	private ArrayList<Host> hosts = new ArrayList<Host>();

	/**
	 * Creates a tab instance
	 * @param facility facility to get services from / destination to add
	 */
	public AddFacilityDestinationTabItem(Facility facility){
		this.facility = facility;
		this.facilityId = facility.getId();
	}

	public boolean isPrepared() {
		return (facility != null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		titleWidget.setText("Add destination");

		final VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		FlexTable layout = new FlexTable();
		layout.setStyleName("inputFormFlexTable");
		FlexTable.FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();
		layout.setWidth("350px");

		final ExtendedSuggestBox destination = new ExtendedSuggestBox();
		final ListBox type = new ListBox();
		type.addItem("HOST","host");
		type.addItem("USER@HOST", "user@host");
		type.addItem("USER@HOST:PORT", "user@host:port");
		type.addItem("USER@HOST-WINDOWS","user@host-windows");
		type.addItem("HOST-WINDOWS-PROXY","host-windows-proxy");
		type.addItem("URL","url");
		type.addItem("MAIL","email");
		type.addItem("SIGNED MAIL","semail");
		type.addItem("SERVICE SPECIFIC","service-specific");

		final ListBox propTypeSelect = new ListBox();
		propTypeSelect.addItem("PARALLEL");
		propTypeSelect.addItem("DUMMY");
		//propTypeSelect.addItem("SERIAL"); TODO - will we ever use it ?
		final HTML propTypeHelp = new HTML("PARALLEL - Data for all destinations of one service are pushed in parallel.");

		final ListBoxWithObjects<Service> services = new ListBoxWithObjects<Service>();
		final CheckBox useHosts = new CheckBox(WidgetTranslation.INSTANCE.useFacilityHostnames(), false);
		useHosts.setTitle(WidgetTranslation.INSTANCE.useFacilityHostnamesTitle());

		final CheckBox onlyAssignedServices = new CheckBox("Show only services on facility", false);
		onlyAssignedServices.setTitle("Click to show all possible services");
		onlyAssignedServices.setValue(true);

		final CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addDestination());

		// fill oracle with hosts of facility
		GetHosts getHosts = new GetHosts(facilityId, new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				ArrayList<Host> ho = JsonUtils.jsoAsList(jso);
				for (Host h : ho) {
					hosts.addAll(ho);
					destination.getSuggestOracle().add(h.getName());
				}
			}
		});
		getHosts.retrieveData();

		JsonCallbackEvents fillAssignedServices = new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				services.removeAllOption();
				services.clear();
				ArrayList<Service> ses = JsonUtils.jsoAsList(jso);
				if (ses != null && !ses.isEmpty()) {
					ses = new TableSorter<Service>().sortByName(ses);
					services.addAllItems(ses);
					services.addAllOption();
					services.setSelectedIndex(0);
				} else {
					services.addItem("No service available");
				}
				addButton.setEnabled(true);
				type.setEnabled(true);
			}
			@Override
			public void onError(PerunError error){
				services.removeAllOption();
				services.clear();
				services.addItem("Error while loading");
				addButton.setEnabled(true);
				type.setEnabled(true);
			}
			@Override
			public void onLoadingStart(){
				services.removeAllOption();
				services.clear();
				services.addItem("Loading...");
				addButton.setEnabled(false);
				type.setEnabled(false);
			}
		};

		final GetFacilityAssignedServices getAssignedServices = new GetFacilityAssignedServices(facility.getId(), fillAssignedServices);
		getAssignedServices.retrieveData();

		final GetServices getAllServices = new GetServices(fillAssignedServices);

		onlyAssignedServices.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				if (onlyAssignedServices.getValue() == false) {
					onlyAssignedServices.setTitle("Click to show only services on facility");
					getAllServices.retrieveData();
				} else {
					onlyAssignedServices.setTitle("Click to show all possible services");
					getAssignedServices.retrieveData();
				}
			}
		});

		final Label destinationLabel = new Label();
		destinationLabel.getElement().setInnerHTML("<strong>Host:</strong>");

		final ExtendedSuggestBox.SuggestBoxValidator validator = new ExtendedSuggestBox.SuggestBoxValidator() {
			@Override
			public boolean validateSuggestBox() {
				if (destination.getSuggestBox().getText().trim().isEmpty() && useHosts.getValue() == false) {
					destination.setError("Destination value can't be empty.");
					return false;
				}
				// check as email
				if (type.getSelectedIndex() > 5 && type.getSelectedIndex() < 8) {
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

				if (type.getSelectedIndex() < 5) {
					destination.getSuggestOracle().clear();
					for (Host h : hosts) {
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
					destinationLabel.getElement().setInnerHTML("<strong>User@host-windows:</strong>");
				} else if (type.getSelectedIndex() == 4) {
					destinationLabel.getElement().setInnerHTML("<strong>Host-Windows-Proxy:</strong>");
				} else if (type.getSelectedIndex() == 5) {
					destinationLabel.getElement().setInnerHTML("<strong>URL:</strong>");
				} else if (type.getSelectedIndex() == 6) {
					destinationLabel.getElement().setInnerHTML("<strong>Mail:</strong>");
				} else if (type.getSelectedIndex() == 7) {
					destinationLabel.getElement().setInnerHTML("<strong>Signed mail:</strong>");
				} else if (type.getSelectedIndex() == 8) {
					destinationLabel.getElement().setInnerHTML("<strong>Service specific:</strong>");
				}

				// run validation
				validator.validateSuggestBox();

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

		cellFormatter.setColSpan(0, 0, 2);
		HTML text = new HTML("Please add destinations for service configuration delivery. New service configuration can be performed directly on facility (dest. type HOST) or sent to URL or by an email.");
		text.setStyleName("inputFormInlineComment");
		layout.setWidget(0, 0, text);

		layout.setHTML(1, 0, "Service:");
		layout.setWidget(1, 1, services);

		layout.setWidget(2, 1, onlyAssignedServices);

		layout.setHTML(3, 0, "Type:");
		layout.setWidget(3, 1, type);

		layout.setWidget(4, 0, destinationLabel);
		layout.setWidget(4, 1, destination);

		layout.setWidget(5, 1, useHosts);

		layout.setHTML(6, 0, "Propagation:");
		layout.setWidget(6, 1, propTypeSelect);

		for (int i=1; i<layout.getRowCount(); i++) {
			cellFormatter.addStyleName(i, 0, "itemName");
		}

		propTypeHelp.setStyleName("inputFormInlineComment");
		layout.setWidget(7, 0, propTypeHelp);
		cellFormatter.setColSpan(7, 0, 2);

		final TabItem tab = this;
		TabMenu menu = new TabMenu();

		menu.addWidget(addButton);

		addButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (services.isEmpty()) {
					// no services available
					addButton.setEnabled(false);
				}

				if (validator.validateSuggestBox()) {

					if (services.getSelectedIndex() == 0) {
						// selected all
						if (useHosts.getValue() == true) {
							// auto by hosts
							AddDestinationsByHostsOnFacility request = new AddDestinationsByHostsOnFacility(facility, JsonCallbackEvents.closeTabDisableButtonEvents(addButton, tab));
							request.addDestinationByHosts(services.getAllObjects());
						} else {
							// default
							AddDestination request = new AddDestination(facility, JsonCallbackEvents.closeTabDisableButtonEvents(addButton, tab));
							request.addDestination(destination.getSuggestBox().getText().trim(), type.getValue(type.getSelectedIndex()), services.getAllObjects(), propTypeSelect.getSelectedValue());
						}
					} else {
						// selected one
						if (useHosts.getValue() == true) {
							// auto by hosts
							AddDestinationsByHostsOnFacility request = new AddDestinationsByHostsOnFacility(facility, JsonCallbackEvents.closeTabDisableButtonEvents(addButton, tab));
							request.addDestinationByHosts(services.getSelectedObject());
						} else {
							// default
							AddDestination request = new AddDestination(facility, JsonCallbackEvents.closeTabDisableButtonEvents(addButton, tab));
							request.addDestination(destination.getSuggestBox().getText().trim(), type.getValue(type.getSelectedIndex()), services.getSelectedObject(), propTypeSelect.getSelectedValue());
						}
					}

				}

			}
		});

		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, isRefreshParentOnClose());
			}
		}));

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
		return SmallIcons.INSTANCE.serverGoIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 641;
		int result = 1;
		result = prime * result + facilityId;
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
		AddFacilityDestinationTabItem other = (AddFacilityDestinationTabItem) obj;
		if (facilityId != other.facilityId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
	}

	public boolean isAuthorized() {

		if (session.isFacilityAdmin(facilityId)) {
			return true;
		} else {
			return false;
		}

	}

}
