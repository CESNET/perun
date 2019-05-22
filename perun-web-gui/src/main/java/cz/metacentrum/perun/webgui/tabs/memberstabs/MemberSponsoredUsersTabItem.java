package cz.metacentrum.perun.webgui.tabs.memberstabs;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.usersManager.GetSpecificUsersByUser;
import cz.metacentrum.perun.webgui.json.usersManager.GetUsersBySpecificUser;
import cz.metacentrum.perun.webgui.model.RichMember;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.userstabs.UserDetailTabItem;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

/**
 * Displays members service identities or associated users for service members.
 *
 * @author Pavel Zlamal <zlamal@cesnet.cz>
 */
public class MemberSponsoredUsersTabItem implements TabItem {

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
	public MemberSponsoredUsersTabItem(RichMember member, int groupId){
		this.member = member;
		this.memberId = member.getId();
		this.groupId = groupId;
	}

	public boolean isPrepared() {
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

		if (member.getUser().isSponsoredUser()) {
			this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(member.getUser().getFullNameWithTitles().trim()) + ": sponsors");
		} else {
			this.titleWidget.setText(Utils.getStrippedStringWithEllipsis(member.getUser().getFullNameWithTitles().trim()) + ": sponsored users");
		}

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		// MENU
		TabMenu menu = new TabMenu();
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		menu.addWidget(UiElements.getRefreshButton(this));

		if (member.getUser().isSponsoredUser()) {

			// request
			final GetUsersBySpecificUser request = new GetUsersBySpecificUser(member.getUserId());
			request.setCheckable(false);

			// table
			CellTable<User> table;
			if (session.isPerunAdmin()) {
				table = request.getTable(new FieldUpdater<User, String>() {
					public void update(int i, User user, String s) {
						session.getTabManager().addTab(new UserDetailTabItem(user));
					}
				});
			} else {
				table = request.getTable();
			}

			table.addStyleName("perun-table");
			table.setWidth("100%");
			ScrollPanel sp = new ScrollPanel(table);
			sp.addStyleName("perun-tableScrollPanel");

			vp.add(sp);

		} else {

			final GetSpecificUsersByUser request = new GetSpecificUsersByUser(member.getUserId());
			request.setCheckable(false);
			request.setHideService(true);

			// table
			CellTable<User> table;
			if (session.isPerunAdmin()) {
				table = request.getTable(new FieldUpdater<User, String>() {
					public void update(int i, User user, String s) {
						session.getTabManager().addTab(new UserDetailTabItem(user));
					}
				});
			} else {
				table = request.getTable();
			}

			table.addStyleName("perun-table");
			table.setWidth("100%");
			ScrollPanel sp = new ScrollPanel(table);
			sp.addStyleName("perun-tableScrollPanel");

			vp.add(sp);

		}

		contentWidget.setWidget(vp);

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
		final int prime = 17389;
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
		MemberSponsoredUsersTabItem other = (MemberSponsoredUsersTabItem) obj;
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
