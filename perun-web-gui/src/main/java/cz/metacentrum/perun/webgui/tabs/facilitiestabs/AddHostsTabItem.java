package cz.metacentrum.perun.webgui.tabs.facilitiestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextArea;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

/**
 * Provides page with add hosts to cluster form
 * !! USE AS INNER TAB ONLY !!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
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

		final ExtendedTextArea newHosts = new ExtendedTextArea();
		newHosts.getTextArea().setSize("335px", "150px");

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

		final CustomButton addHostsButton = TabMenu.getPredefinedButton(ButtonType.ADD, ButtonTranslation.INSTANCE.addHost());

		// close tab, disable button
		final TabItem tab = this;

		addHostsButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (validator.validateTextArea()) {
					String hostnames = newHosts.getTextArea().getText().trim();
					String hosts[] = hostnames.split("\n");
					// trim whitespace
					for (int i = 0; i< hosts.length; i++) {
						hosts[i] = hosts[i].trim();
					}
					AddHosts request = new AddHosts(facility.getId(), JsonCallbackEvents.closeTabDisableButtonEvents(addHostsButton, tab));
					request.addHosts(hosts);
				}
			}
		});

		TabMenu menu = new TabMenu();

		menu.addWidget(addHostsButton);
		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, false);
			}
		}));

		// layout
		final FlexTable layout = new FlexTable();
		layout.setWidth("350px");
		layout.setStyleName("inputFormFlexTable");
		FlexTable.FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

		layout.setHTML(0, 0, "Hostnames:");
		layout.setWidget(1, 0, newHosts);
		cellFormatter.addStyleName(0, 0, "itemName");

		layout.setHTML(2, 0, "Enter one host per line. You can use \"[x-y]\" in hostname to generate hosts with numbers from x to y. This replacer can be specified multiple times in one hostname to generate MxN combinations.");
		cellFormatter.addStyleName(2, 0, "inputFormInlineComment");

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
		final int prime = 661;
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
