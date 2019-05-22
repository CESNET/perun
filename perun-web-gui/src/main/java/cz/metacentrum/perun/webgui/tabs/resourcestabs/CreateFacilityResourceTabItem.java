package cz.metacentrum.perun.webgui.tabs.resourcestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.resourcesManager.CreateResource;
import cz.metacentrum.perun.webgui.json.vosManager.GetVos;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.Resource;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.ListBoxWithObjects;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;

/**
 * FACILITY ADMINISTRATOR - create Resource wizard - page 1
 * Creating resource definition
 *
 * !! USE AS INNER TAB ONLY !!
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CreateFacilityResourceTabItem implements TabItem {

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
	private Label titleWidget = new Label("Create new Resource");

	//data
	private Facility facility;
	private int facilityId;

	/**
	 * @param facility facility which should have resource added
	 */
	public CreateFacilityResourceTabItem(Facility facility){
		this.facility = facility;
		this.facilityId = facility.getId();
	}

	/**
	 * @param facilityId facility which should have resource added
	 */
	public CreateFacilityResourceTabItem(int facilityId){
		this.facilityId = facilityId;
		new GetEntityById(PerunEntity.FACILITY, facilityId, new JsonCallbackEvents() {
			public void onFinished(JavaScriptObject jso){
				facility = jso.cast();
			}
		}).retrieveData();
	}

	public boolean isPrepared(){
		return !(facility == null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		titleWidget.setText("Create resource");

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// form inputs
		final ExtendedTextBox nameTextBox = new ExtendedTextBox();
		final TextBox descriptionTextBox = new TextBox();

		final ListBoxWithObjects<VirtualOrganization> vosDropDown = new ListBoxWithObjects<VirtualOrganization>();

		// send button
		final CustomButton createButton = TabMenu.getPredefinedButton(ButtonType.CREATE, ButtonTranslation.INSTANCE.createResource());

		// local events fills the listbox of Vos and Slds
		JsonCallbackEvents event = new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso){
				// fill VOs listbox
				vosDropDown.clear();
				ArrayList<VirtualOrganization> vos = JsonUtils.jsoAsList(jso);
				vos = new TableSorter<VirtualOrganization>().sortByName(vos);
				for (VirtualOrganization vo : vos) {
					vosDropDown.addItem(vo);
				}
				if (!vos.isEmpty()) createButton.setEnabled(true);
			}
			@Override
			public void onLoadingStart() {
				vosDropDown.clear();
				vosDropDown.addItem("Loading...");
				createButton.setEnabled(false);
			}
			@Override
			public void onError(PerunError error) {
				vosDropDown.clear();
				vosDropDown.addItem("Error while loading");
				createButton.setEnabled(false);
			}
		};
		// load available VOs
		final GetVos vos = new GetVos(event);
		vos.setForceAll(true);
		vos.retrieveData();

		// layout
		FlexTable layout = new FlexTable();
		layout.setStyleName("inputFormFlexTable");
		FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

		// Add some standard form options
		layout.setHTML(0, 0, "On facility:");
		layout.setHTML(0, 1, SafeHtmlUtils.fromString((facility.getName() != null) ? facility.getName() : "").asString());
		layout.setHTML(1, 0, "For VO:");
		layout.setWidget(1, 1, vosDropDown);
		layout.setHTML(2, 0, "Name:");
		layout.setWidget(2, 1, nameTextBox);
		layout.setHTML(3, 0, "Description:");
		layout.setWidget(3, 1, descriptionTextBox);

		for (int i=0; i<layout.getRowCount(); i++) {
			cellFormatter.addStyleName(i, 0, "itemName");
		}

		layout.setWidth("350px");

		TabMenu menu = new TabMenu();

		final ExtendedTextBox.TextBoxValidator validator = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (nameTextBox.getTextBox().getText().trim().isEmpty()) {
					nameTextBox.setError("Name can't be empty.");
					return false;
				}
				nameTextBox.setOk();
				return true;
			}
		};
		nameTextBox.setValidator(validator);

		createButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				// loads new tab when creating successful, also disable button
				JsonCallbackEvents localEvents = new JsonCallbackEvents(){
					public void onLoadingStart() {
						(JsonCallbackEvents.disableButtonEvents(createButton)).onLoadingStart();
					}
					public void onFinished(JavaScriptObject jso){
						(JsonCallbackEvents.disableButtonEvents(createButton)).onFinished(jso);
						Resource res = (Resource)jso;
						session.getTabManager().addTabToCurrentTab(new CreateFacilityResourceManageServicesTabItem(facility, res), true);
					}
					public void onError(PerunError error) {
						(JsonCallbackEvents.disableButtonEvents(createButton)).onError(error);
					}
				};
				if (validator.validateTextBox()) {
					// request
					CreateResource request = new CreateResource(localEvents);
					request.createResource(nameTextBox.getTextBox().getText().trim(), descriptionTextBox.getText().trim(), facility.getId(), vosDropDown.getSelectedObject().getId());
				}
			}
		});

		menu.addWidget(createButton);

		final TabItem tab = this;
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
		return SmallIcons.INSTANCE.addIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1021;
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
		CreateFacilityResourceTabItem other = (CreateFacilityResourceTabItem) obj;
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

		if (session.isFacilityAdmin(facility.getId())) {
			return true;
		} else {
			return false;
		}

	}

}
