package cz.metacentrum.perun.webgui.tabs.memberstabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.resourcesManager.GetAssignedRichResources;
import cz.metacentrum.perun.webgui.model.RichMember;
import cz.metacentrum.perun.webgui.model.RichResource;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.resourcestabs.ResourceDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

/**
 * Displays members resources
 * !! USE AS INNER TAB ONLY !!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class MemberResourcesTabItem implements TabItem {

	private RichMember member;
	private int memberId;
	private PerunWebSession session = PerunWebSession.getInstance();
	private SimplePanel contentWidget = new SimplePanel();
	private Label titleWidget = new Label("Loading member details");
	private int groupId = 0;

	/**
	 * Constructor
	 *
	 * @param member RichMember object, typically from table
	 */
	public MemberResourcesTabItem(RichMember member, int groupId){
		this.member = member;
		this.memberId = member.getId();
		this.groupId = groupId;
	}

	public boolean isPrepared(){
		return !(member == null);
	}

	public Widget draw() {

		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(member.getUser().getFullNameWithTitles().trim()) + ": resources");

		// main widget panel
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%","100%");

		TabMenu menu = new TabMenu();
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		menu.addWidget(UiElements.getRefreshButton(this));

		CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, true, "Add member to new resource", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				AddMemberToResourceTabItem tab = new AddMemberToResourceTabItem(member.getVoId());
				tab.startAtStageTwo(member);
				session.getTabManager().addTabToCurrentTab(tab);
			}
		});
		if (!session.isVoAdmin(member.getVoId())) addButton.setEnabled(false);
		menu.addWidget(addButton);

		final GetAssignedRichResources resourcesCall = new GetAssignedRichResources(memberId, PerunEntity.MEMBER);
		resourcesCall.setCheckable(false);
		CellTable<RichResource> table = resourcesCall.getTable(new FieldUpdater<RichResource, String>() {
			@Override
			public void update(int i, RichResource resource, String s) {
				if (session.isVoAdmin(resource.getVoId()) || session.isFacilityAdmin(resource.getFacilityId())) {
					session.getTabManager().addTab(new ResourceDetailTabItem(resource, 0));
				} else {
					UiElements.generateInfo("Not privileged", "You are not allowed to manage this resource. You must be VO manager or Facility manager.");
				}
			}
		});

		menu.addFilterWidget(new ExtendedSuggestBox(resourcesCall.getOracle()), new PerunSearchEvent() {
			public void searchFor(String text) {
				resourcesCall.filterTable(text);
			}
		}, ButtonTranslation.INSTANCE.filterResources());

		// JsonUtils.addTableManagedButton(resourcesCall, table, removeButton);
		table.addStyleName("perun-table");
		ScrollPanel sp = new ScrollPanel(table);
		sp.addStyleName("perun-tableScrollPanel");
		session.getUiElements().resizePerunTable(sp, 350, this);

		vp.add(sp);

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
		return SmallIcons.INSTANCE.userGreenIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1459;
		int result = 1;
		result = prime * result + memberId;
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
		MemberResourcesTabItem other = (MemberResourcesTabItem) obj;
		if (memberId != other.memberId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {

	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(member.getVoId()) || session.isVoObserver(member.getVoId()) || session.isGroupAdmin(groupId)) {
			return true;
		} else {
			return false;
		}

	}

}
