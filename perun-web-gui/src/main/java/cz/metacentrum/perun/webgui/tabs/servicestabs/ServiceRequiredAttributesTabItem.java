package cz.metacentrum.perun.webgui.tabs.servicestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetServiceRequiredAttributes;
import cz.metacentrum.perun.webgui.json.servicesManager.RemoveRequiredAttribute;
import cz.metacentrum.perun.webgui.model.AttributeDefinition;
import cz.metacentrum.perun.webgui.model.Service;
import cz.metacentrum.perun.webgui.tabs.ServicesTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.Map;

/**
 * Tab with required attributes management for selected service
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class ServiceRequiredAttributesTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Required attributes");

	// data
	private int serviceId;
	private Service service;

	/**
	 * Tab with required attributes management for selected service
	 *
	 * @param serviceId ID of service to get required attributes for
	 */
	public ServiceRequiredAttributesTabItem(int serviceId){
		this.serviceId = serviceId;
		new GetEntityById(PerunEntity.SERVICE, serviceId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				service = jso.cast();
			}
		}).retrieveData();
	}

	/**
	 * Tab with required attributes management for selected service
	 *
	 * @param service service to get required attributes for
	 */
	public ServiceRequiredAttributesTabItem(Service service) {
		this.service = service;
		this.serviceId = service.getId();
	}

	public boolean isPrepared(){
		return !(service == null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}


	public Widget draw() {

		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(service.getName()) + ": Required attributes");

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// create new json call for required attributes of service with id=?
		final GetServiceRequiredAttributes servReqAttr = new GetServiceRequiredAttributes(serviceId);

		// menu
		TabMenu menu = new TabMenu();
		menu.addWidget(UiElements.getRefreshButton(this));

		// custom event
		final JsonCallbackEvents events = JsonCallbackEvents.refreshTableEvents(servReqAttr);

		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.ADD, true, ButtonTranslation.INSTANCE.addRequiredAttribute(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().addTabToCurrentTab(new AddRequiredAttributesTabItem(service), true);
			}
		}));

		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, ButtonTranslation.INSTANCE.removeSelectedRequiredAttributes());
		removeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				final ArrayList<AttributeDefinition> attrsForRemoving = servReqAttr.getTableSelectedList();
				UiElements.showDeleteConfirm(attrsForRemoving, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						// TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
						for (int i=0; i<attrsForRemoving.size(); i++ ) {
							if (i == attrsForRemoving.size()-1) {
								RemoveRequiredAttribute request = new RemoveRequiredAttribute(JsonCallbackEvents.disableButtonEvents(removeButton, events));
								request.removeRequiredAttribute(serviceId, attrsForRemoving.get(i).getId());
							} else {
								RemoveRequiredAttribute request = new RemoveRequiredAttribute(JsonCallbackEvents.disableButtonEvents(removeButton));
								request.removeRequiredAttribute(serviceId, attrsForRemoving.get(i).getId());
							}

						}
					}
				});
			}
		});

		menu.addWidget(removeButton);

		final ExtendedSuggestBox box = new ExtendedSuggestBox(servReqAttr.getOracle());
		menu.addFilterWidget(box, new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				servReqAttr.filterTable(text);
			}
		}, "Filter required attributes by name");

		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		// get table = make call
		CellTable<AttributeDefinition> reqAttrTable = servReqAttr.getTable();

		// create scroll panel for table
		reqAttrTable.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(reqAttrTable);
		sp.addStyleName("perun-tableScrollPanel");
		sp.setWidth("100%");
		vp.add(sp);
		vp.setCellHeight(sp, "100%");

		removeButton.setEnabled(false);
		JsonUtils.addTableManagedButton(servReqAttr, reqAttrTable, removeButton);

		session.getUiElements().resizePerunTable(sp, 350, this);

		// add tabs to the main panel
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
		return SmallIcons.INSTANCE.trafficLightsIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1103;
		int result = 1;
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
		ServiceRequiredAttributesTabItem other = (ServiceRequiredAttributesTabItem) obj;
		if (serviceId != other.serviceId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open()
	{

	}

	public boolean isAuthorized() {

		if (session.isPerunAdmin()) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "req-attrs";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		return ServicesTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + serviceId;
	}

	static public ServiceRequiredAttributesTabItem load(Map<String, String> parameters)
	{
		int id = Integer.parseInt(parameters.get("id"));
		return new ServiceRequiredAttributesTabItem(id);
	}

}
