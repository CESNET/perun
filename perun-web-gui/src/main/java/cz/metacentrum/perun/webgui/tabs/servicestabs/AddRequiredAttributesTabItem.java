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
import cz.metacentrum.perun.webgui.json.attributesManager.GetAttributesDefinition;
import cz.metacentrum.perun.webgui.json.servicesManager.AddRequiredAttribute;
import cz.metacentrum.perun.webgui.model.AttributeDefinition;
import cz.metacentrum.perun.webgui.model.Service;
import cz.metacentrum.perun.webgui.tabs.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;

/**
 * Tab with form for adding attribute definition as required attribute to service
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @version $Id$
 */
public class AddRequiredAttributesTabItem implements TabItem {

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
	private Label titleWidget = new Label("Loading service");
	
	
	// data
	private int serviceId;
	private Service service;

	/**
	 * Tab with form for adding attribute definition as required attribute to service
	 *
     * @param serviceId ID of service to add req. attribute for
     */
	public AddRequiredAttributesTabItem(int serviceId){
		this.serviceId = serviceId;
		new GetEntityById(PerunEntity.SERVICE, serviceId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso){
				service = jso.cast();
			}
		}).retrieveData();
	}
	
	/**
	 * Tab with form for adding attribute definition as required attribute to service
	 *
     * @param service Service to add req. attribute for
     */
	public AddRequiredAttributesTabItem(Service service){
		this.service = service;
		this.serviceId = service.getId();
	}
	
	public boolean isPrepared(){
		return (service != null);
	}
	
	public Widget draw() {
		
		titleWidget.setText(Utils.getStrippedStringWithEllipsis(service.getName()) + ": add required attributes");

		VerticalPanel mainTab = new VerticalPanel();
		mainTab.setSize("100%","100%");

		final GetAttributesDefinition attrDefs = new GetAttributesDefinition();
		CellTable<AttributeDefinition> table = attrDefs.getTable();

		TabMenu menu = new TabMenu();
        final TabItem tab = this;

        final CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addSelectedRequiredAttribute());
        menu.addWidget(addButton);

        // cancel button
        menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                session.getTabManager().closeTab(tab, false);
            }
        }));

        menu.addFilterWidget(new SuggestBox(attrDefs.getOracle()), new PerunSearchEvent() {
            @Override
            public void searchFor(String text) {
                attrDefs.filterTable(text);
            }
        }, ButtonTranslation.INSTANCE.filterAttributeDefinition());

        addButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
                ArrayList<AttributeDefinition> attributesToAdd = attrDefs.getTableSelectedList();
                if (UiElements.cantSaveEmptyListDialogBox(attributesToAdd)) {
                    // TODO - SHOULD HAVE ONLY ONE CALLBACK TO CORE
                    for (int i=0; i<attributesToAdd.size(); i++ ) {
                        if (i == attributesToAdd.size()-1) {
                            AddRequiredAttribute request = new AddRequiredAttribute(JsonCallbackEvents.closeTabDisableButtonEvents(addButton, tab));
                            request.addRequiredAttribute(serviceId, attributesToAdd.get(i).getId());
                        } else {
                            AddRequiredAttribute request = new AddRequiredAttribute(JsonCallbackEvents.disableButtonEvents(addButton));
                            request.addRequiredAttribute(serviceId, attributesToAdd.get(i).getId());
                        }
                    }


                }
			}
		});

        addButton.setEnabled(false);
        JsonUtils.addTableManagedButton(attrDefs, table, addButton);

		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");

		session.getUiElements().resizeSmallTabPanel(sp, 350, this);

		mainTab.add(menu);
		mainTab.setCellHeight(menu, "30px");
		mainTab.add(sp);
		mainTab.setCellHeight(sp, "100%");
				
		// add tabs to the main panel
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
		return SmallIcons.INSTANCE.addIcon(); 
	}

	@Override
	public int hashCode() {
		final int prime = 43;
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
		AddRequiredAttributesTabItem other = (AddRequiredAttributesTabItem) obj;
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

}
