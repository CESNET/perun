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
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.facilitiesManager.AddHosts;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.tabs.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

/**
 * Provides page with add hosts to cluster form
 * !! USE AS INNER TAB ONLY !!
 * 
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz> 
 * @version $Id$
 */
public class AddHostsTabItem implements TabItem {

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
	private Label titleWidget = new Label("Add hosts");

	// data
	private int facilityId;
	private Facility facility;

	/**
	 * Creates a tab instance
     * @param facilityId
     */
	public AddHostsTabItem(int facilityId){
		this.facilityId = facilityId;
        new GetEntityById(PerunEntity.FACILITY, facilityId, new JsonCallbackEvents(){
            public void onFinished(JavaScriptObject jso){
                facility = jso.cast();
            }
        }).retrieveData();
	}
	
	/**
	 * Creates a tab instance
     * @param facility
     */
	public AddHostsTabItem(Facility facility){
		this.facility = facility;
		this.facilityId = facility.getId();
	}

	public boolean isPrepared() {
		return !(facility == null);
	}
	
	public Widget draw() {

		titleWidget.setText(Utils.getStrippedStringWithEllipsis(facility.getName())+": add hosts");
		
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		final TextBox hosts = new TextBox();

        TabMenu menu = new TabMenu();
        final CustomButton addHostsButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addHost());
	
		// close tab, disable button
		final JsonCallbackEvents closeTabEvents = JsonCallbackEvents.closeTabDisableButtonEvents(addHostsButton, this);
					
		addHostsButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				String hostnames = hosts.getText().trim();
				String hosts[] = hostnames.split(",");
				// trim whitespace
				for (int i = 0; i< hosts.length; i++) {
					hosts[i] = hosts[i].trim();
				}
				AddHosts request = new AddHosts(facilityId, closeTabEvents);
				request.addHosts(hosts);
			}
		});

        menu.addWidget(addHostsButton);
        final TabItem tab = this;
        menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                session.getTabManager().closeTab(tab, false);
            }
        }));

        addHostsButton.setEnabled(false);
        hosts.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                if (!hosts.getText().isEmpty()) {
                    addHostsButton.setEnabled(true);
                } else {
                    addHostsButton.setEnabled(false);
                }
            }
        });

        // layout
        FlexTable layout = new FlexTable();
        layout.setStyleName("inputFormFlexTable");
        FlexTable.FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

        layout.setHTML(0, 0, "Hostnames:");
        layout.setWidget(0, 1, hosts);
        cellFormatter.addStyleName(0, 0, "itemName");

        cellFormatter.setColSpan(1, 0, 2);
        layout.setHTML(1, 0, "Enter hostnames separated by comas.");
        cellFormatter.addStyleName(1, 0, "inputFormInlineComment");

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
		final int prime = 31;
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
		AddHostsTabItem other = (AddHostsTabItem) obj;
		if (facilityId != other.facilityId)
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

		if (session.isFacilityAdmin(facilityId)) {
			return true; 
		} else {
			return false;
		}

	}

}