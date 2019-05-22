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
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunSearchEvent;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.groupsManager.GetMemberGroups;
import cz.metacentrum.perun.webgui.json.groupsManager.RemoveMember;
import cz.metacentrum.perun.webgui.model.Group;
import cz.metacentrum.perun.webgui.model.RichMember;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.groupstabs.GroupDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedSuggestBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;

/**
 * Displays members groups
 * !! USE AS INNER TAB ONLY !!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class MemberGroupsTabItem implements TabItem {

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
	public MemberGroupsTabItem(RichMember member, int groupId){
		this.member = member;
		this.memberId = member.getId();
		this.groupId = groupId;
	}

	public boolean isPrepared(){
		return !(member == null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(member.getUser().getFullNameWithTitles().trim()) + ": groups");

		// main widget panel
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%","100%");

		TabMenu menu = new TabMenu();
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		final GetMemberGroups groupsCall = new GetMemberGroups(memberId);

		menu.addWidget(UiElements.getRefreshButton(this));

		CustomButton addButton = TabMenu.getPredefinedButton(ButtonType.ADD, true, "Add member to new group", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().addTabToCurrentTab(new MemberAddToGroupTabItem(member), true);
			}
		});
		if (session.isVoObserver(member.getVoId()) && !session.isVoAdmin(member.getVoId())) {
			addButton.setEnabled(false);
			groupsCall.setCheckable(false);
		}
		menu.addWidget(addButton);

		final CustomButton removeButton = TabMenu.getPredefinedButton(ButtonType.REMOVE, "Remove member from selected group(s)", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				final ArrayList<Group> list = groupsCall.getTableSelectedList();
				String confirmText = member.getUser().getFullName()+ " will be removed from following groups.";
				UiElements.showDeleteConfirm(list, confirmText, new ClickHandler() {
					@Override
					public void onClick(ClickEvent clickEvent) {
						// TODO - should have only one callback to core
						for (int i=0; i<list.size(); i++) {
							if (i == list.size()-1) {
								RemoveMember request = new RemoveMember(JsonCallbackEvents.refreshTableEvents(groupsCall));
								request.removeMemberFromGroup(list.get(i), member);
							} else {
								RemoveMember request = new RemoveMember();
								request.removeMemberFromGroup(list.get(i), member);
							}
						}
					}
				});
			}
		});
		removeButton.setEnabled(false);
		menu.addWidget(removeButton);

		menu.addFilterWidget(new ExtendedSuggestBox(groupsCall.getOracle()), new PerunSearchEvent() {
			@Override
			public void searchFor(String text) {
				groupsCall.filterTable(text);
			}
		}, ButtonTranslation.INSTANCE.filterGroup());

		CellTable<Group> table = groupsCall.getTable(new FieldUpdater<Group, String>() {
			@Override
			public void update(int i, Group group, String s) {
				if (session.isVoAdmin(group.getVoId()) || session.isVoObserver(group.getVoId()) || session.isGroupAdmin(group.getId())) {
					session.getTabManager().addTab(new GroupDetailTabItem(group));
				} else {
					UiElements.generateInfo("Not privileged", "You are not manager of selected group or it's VO.");
				}
			}
		});

		if (session.isVoAdmin(member.getVoId()) || session.isGroupAdmin(groupId)) JsonUtils.addTableManagedButton(groupsCall, table, removeButton);
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
		final int prime = 1451;
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
		MemberGroupsTabItem other = (MemberGroupsTabItem) obj;
		if (memberId != other.memberId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() { }

	public boolean isAuthorized() {

		if (session.isVoAdmin(member.getVoId()) || session.isVoObserver(member.getVoId()) || session.isGroupAdmin(groupId)) {
			return true;
		} else {
			return false;
		}

	}

}
