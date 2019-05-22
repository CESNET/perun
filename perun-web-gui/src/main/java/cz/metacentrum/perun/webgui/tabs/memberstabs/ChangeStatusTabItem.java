package cz.metacentrum.perun.webgui.tabs.memberstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.membersManager.SetStatus;
import cz.metacentrum.perun.webgui.model.Member;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;
import cz.metacentrum.perun.webgui.widgets.TabPanelForTabItems;


/**
 * Inner tab for changing members membership status
 * !! USE AS INNER TAB ONLY !!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ChangeStatusTabItem implements TabItem {

	private Member member;
	private int memberId;
	private PerunWebSession session = PerunWebSession.getInstance();
	private SimplePanel contentWidget = new SimplePanel();
	private Label titleWidget = new Label("Loading member");
	TabPanelForTabItems tabPanel;
	private JsonCallbackEvents events = new JsonCallbackEvents();

	/**
	 * Constructor
	 *
	 * @param member Member object, typically from table
	 * @param events Events triggered when status is changed
	 */
	public ChangeStatusTabItem(Member member, JsonCallbackEvents events){
		this.member = member;
		this.memberId = member.getId();
		this.events = events;
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

		this.titleWidget.setText("Change member status");

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("300px", "100%");

		FlexTable layout = new FlexTable();
		layout.setSize("100%","100%");

		layout.setStyleName("inputFormFlexTable");

		final HTML text = new HTML("");
		final ListBox lb = new ListBox(false);
		lb.addItem("VALID", "VALID");
		lb.addItem("INVALID", "INVALID");
		lb.addItem("SUSPENDED", "SUSPENDED");
		lb.addItem("EXPIRED", "EXPIRED");
		lb.addItem("DISABLED", "DISABLED");

		layout.setHTML(0, 0, "Current status:");
		layout.getFlexCellFormatter().setStyleName(0, 0, "itemName");
		layout.setHTML(0, 1, SafeHtmlUtils.fromString(member.getStatus()).asString());

		if (member.getStatus().equalsIgnoreCase("VALID")) {
			layout.setHTML(1, 0, "Member is properly configured and have access on provided resources.");
		} else if (member.getStatus().equalsIgnoreCase("INVALID")) {
			layout.setHTML(1, 0, "Member have configuration error and DON'T have access on provided resources. You can check what is wrong by changing member's status to VALID. If possible, procedure will configure all necessary settings by itself.");
		} else if (member.getStatus().equalsIgnoreCase("SUSPENDED")) {
			layout.setHTML(1, 0, "Member violated some rules and DON'T have access on provided resources.");
		} else if (member.getStatus().equalsIgnoreCase("EXPIRED")) {
			layout.setHTML(1, 0, "Member didn't extend membership and DON'T have access on provided resources.");
		} else if (member.getStatus().equalsIgnoreCase("DISABLED")) {
			layout.setHTML(1, 0, "Member didn't extend membership long time ago or was manually disabled and DON'T have access on provided resources.");
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
			if (lb.getItemText(i).equalsIgnoreCase(member.getStatus())) {
				lb.setSelectedIndex(i);
			}
		}

		TabMenu menu = new TabMenu();
		final TabItem tab = this;

		Label description = new Label();
		description.setText("Reason for suspension:");
		description.setVisible(false);

		TextArea messageArea = new TextArea();
		messageArea.setWidth("95%");
		messageArea.setVisible(false);

		final CustomButton changeButton = new CustomButton("Change status", SmallIcons.INSTANCE.diskIcon());
		// by default false
		changeButton.setEnabled(false);
		changeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				SetStatus request = new SetStatus(memberId, JsonCallbackEvents.disableButtonEvents(changeButton, JsonCallbackEvents.mergeEvents(events, new JsonCallbackEvents(){
					@Override
					public void onFinished(JavaScriptObject jso) {
						// close without refresh
						session.getTabManager().closeTab(tab, isRefreshParentOnClose());
					}
				})), messageArea.getText());
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

				if (lb.getValue(lb.getSelectedIndex()).equalsIgnoreCase(member.getStatus())) {
					changeButton.setEnabled(false);
				} else {
					changeButton.setEnabled(true);
				}

				// clear
				text.setHTML("");

				if (lb.getSelectedIndex() == 0) {
					// VALIDATING NOTICE
					if (!member.getStatus().equalsIgnoreCase("VALID")) text.setHTML("Changing status to VALID <strong>will trigger automatic configuration</strong> for provided resources. <br/><strong>If successful</strong>, member will have access on provided resources. <br /><strong>If not</strong>, see displayed error message and do manual configuration on 'settings' tab on members detail.");
					messageArea.setVisible(false);
					description.setVisible(false);
				} else {
					// INVALIDATING NOTICE
					if (member.getStatus().equalsIgnoreCase("VALID")) text.setHTML("Changing status to "+lb.getValue(lb.getSelectedIndex())+" will <strong>prevent member from access to provided resources (based on provided service's rules)</strong>.<br /><br />");
				}

				// SET INFO
				if (lb.getSelectedIndex() == 1) {
					text.setHTML(text.getHTML()+"INVALID status means there is configuration error, which prevents him from access on provided resources.");
					messageArea.setVisible(false);
					description.setVisible(false);
				} else if (lb.getSelectedIndex() == 2) {
					text.setHTML(text.getHTML()+"SUSPENDED status means, that member did something bad (against VO rules).");
					messageArea.setVisible(true);
					description.setVisible(true);
				} else if (lb.getSelectedIndex() == 3) {
					text.setHTML(text.getHTML()+"EXPIRED status means, that member didn't extend his membership in VO, but it's still possible for him to do so.");
					messageArea.setVisible(false);
					description.setVisible(false);
				} else if (lb.getSelectedIndex() == 4) {
					text.setHTML(text.getHTML()+"DISABLED status means, that member didn't extend his membership long ago or was manually disabled by administrator. Member can't enable/extend membership by himself.");
					messageArea.setVisible(false);
					description.setVisible(false);
				}

			}
		});

		vp.add(layout);
		vp.add(description);
		vp.add(messageArea);
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
		final int prime = 863;
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
		ChangeStatusTabItem other = (ChangeStatusTabItem) obj;
		if (memberId != other.memberId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() { }

	public boolean isAuthorized() {

		if (session.isVoAdmin(member.getVoId())) {
			return true;
		} else {
			return false;
		}

	}

}
