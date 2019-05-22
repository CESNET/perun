package cz.metacentrum.perun.webgui.tabs.memberstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.groupsManager.SetGroupsMemberStatus;
import cz.metacentrum.perun.webgui.model.RichMember;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;
import cz.metacentrum.perun.webgui.widgets.TabPanelForTabItems;

/**
 * Inner tab for changing members group membership status
 * !! USE AS INNER TAB ONLY !!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ChangeGroupStatusTabItem implements TabItem {

	private RichMember member;
	private int memberId;
	private PerunWebSession session = PerunWebSession.getInstance();
	private SimplePanel contentWidget = new SimplePanel();
	private Label titleWidget = new Label("Loading member");
	TabPanelForTabItems tabPanel;
	private JsonCallbackEvents events = new JsonCallbackEvents();
	private int groupId;

	/**
	 * Constructor
	 *
	 * @param member RichMember object, typically from table
	 * @param groupId ID of group to change status in
	 * @param events Events triggered when status is changed
	 */
	public ChangeGroupStatusTabItem(RichMember member, int groupId, JsonCallbackEvents events){
		this.member = member;
		this.memberId = member.getId();
		this.events = events;
		this.groupId = groupId;
		this.tabPanel = new TabPanelForTabItems(this);
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

		this.titleWidget.setText("Change group member status");

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("300px", "100%");

		FlexTable layout = new FlexTable();
		layout.setSize("100%","100%");

		layout.setStyleName("inputFormFlexTable");

		final HTML text = new HTML("");
		final ListBox lb = new ListBox(false);
		lb.addItem("VALID", "VALID");
		lb.addItem("EXPIRED", "EXPIRED");

		layout.setHTML(0, 0, "Current status:");
		layout.getFlexCellFormatter().setStyleName(0, 0, "itemName");
		layout.setHTML(0, 1, SafeHtmlUtils.fromString(member.getGroupStatus()).asString());

		if (member.getGroupStatus().equalsIgnoreCase("VALID")) {
			layout.setHTML(1, 0, "Member is properly configured and have access on provided resources.");
		} else if (member.getGroupStatus().equalsIgnoreCase("EXPIRED")) {
			layout.setHTML(1, 0, "Member didn't extend membership and DON'T have access on provided resources.");
		}
		layout.getFlexCellFormatter().setColSpan(1, 0, 2);
		layout.getFlexCellFormatter().setStyleName(1, 0, "inputFormInlineComment");


		layout.setHTML(2, 0, "New status:");
		layout.getFlexCellFormatter().setStyleName(2, 0, "itemName");
		layout.setWidget(2, 1, lb);

		layout.setWidget(3, 0, text);
		layout.getFlexCellFormatter().setColSpan(3, 0, 2);
		layout.getFlexCellFormatter().setStyleName(3, 0, "inputFormInlineComment");

		// pick which one is already set
		for (int i=0; i<lb.getItemCount(); i++) {
			if (lb.getItemText(i).equalsIgnoreCase(member.getGroupStatus())) {
				lb.setSelectedIndex(i);
			}
		}

		TabMenu menu = new TabMenu();
		final TabItem tab = this;

		final CustomButton changeButton = new CustomButton("Change group status", ButtonTranslation.INSTANCE.changeStatus(member.getUser().getFullName()), SmallIcons.INSTANCE.diskIcon());
		changeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				SetGroupsMemberStatus request = new SetGroupsMemberStatus(groupId, memberId, JsonCallbackEvents.disableButtonEvents(changeButton, JsonCallbackEvents.mergeEvents(events, new JsonCallbackEvents(){
					@Override
					public void onFinished(JavaScriptObject jso) {
						// close without refresh
						session.getTabManager().closeTab(tab, isRefreshParentOnClose());
					}
				})));
				request.setStatus(lb.getValue(lb.getSelectedIndex()));
			}
		});
		menu.addWidget(changeButton);

		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, ButtonTranslation.INSTANCE.cancelButton(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, isRefreshParentOnClose());
			}
		}));

		// listbox change handler
		lb.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent changeEvent) {

				// clear
				text.setHTML("");

				if (lb.getSelectedIndex() == 0) {
					// VALIDATING NOTICE
					if (!member.getGroupStatus().equalsIgnoreCase("VALID")) text.setHTML("Changing status to VALID will give member access on provided resources.<br /><br />");
				} else {
					// INVALIDATING NOTICE
					if (member.getGroupStatus().equalsIgnoreCase("VALID")) text.setHTML("Changing status to "+lb.getValue(lb.getSelectedIndex())+" will <strong>prevent member from access to provided resources (based on provided service's rules)</strong>.<br /><br />");
				}

				// SET INFO
				if (lb.getSelectedIndex() == 1) {
					text.setHTML(text.getHTML()+"EXPIRED status means, that member didn't extend his membership in Group, but it's still possible for him to do so.");
				}

			}
		});

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
		return SmallIcons.INSTANCE.userGreenIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 13487;
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
		ChangeGroupStatusTabItem other = (ChangeGroupStatusTabItem) obj;
		if (memberId != other.memberId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() { }

	public boolean isAuthorized() {

		if (session.isVoAdmin(member.getVoId()) || session.isGroupAdmin(groupId)) {
			return true;
		} else {
			return false;
		}

	}

}
