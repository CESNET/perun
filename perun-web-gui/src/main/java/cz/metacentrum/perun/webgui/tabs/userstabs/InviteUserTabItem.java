package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.registrarManager.SendInvitation;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

/**
 * Inner tab item for inviting user into your VO or Group
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class InviteUserTabItem implements TabItem {

	private PerunWebSession session = PerunWebSession.getInstance();

	private Label titleWidget = new Label("Invite member");
	private SimplePanel contentWidget = new SimplePanel();

	private VirtualOrganization vo;
	private Group group;
	private int groupId = 0;

	/**
	 * Invite user to VO or Group
	 *
	 * @param vo invite to VO
	 * @param group (if not null, invite to Group)
	 */
	public InviteUserTabItem(VirtualOrganization vo, Group group){
		this.vo = vo;
		this.group = group;
		if (group != null) groupId = group.getId();
	}

	/**
	 * Invite user to VO or Group
	 *
	 * @param voId invite to VO
	 * @param group (if not null, invite to Group)
	 */
	public InviteUserTabItem(int voId, Group group){
		JsonCallbackEvents events = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				vo = jso.cast();
			}
		};
		new GetEntityById(PerunEntity.VIRTUAL_ORGANIZATION, voId, events).retrieveData();
		this.group = group;
		if (group != null) groupId = group.getId();
	}

	public boolean isPrepared(){
		return (vo != null);
	}

	public Widget draw() {

		VerticalPanel vp = new VerticalPanel();

		final FlexTable ft = new FlexTable();
		ft.setWidth("350px");
		ft.setStyleName("inputFormFlexTable");

		if (groupId == 0) {
			ft.setHTML(0, 0, "Person you are inviting will receive an email with link to VOs application form. You can set up an email template in: Vo manager (advanced) -> Application form -> Notifications.");
		} else {
			ft.setHTML(0, 0, "Person you are inviting will receive an email with link to groups application form. You can set up an email template in: Group manager (advanced) -> Application form -> Notifications.");
		}

		ft.getFlexCellFormatter().setColSpan(0, 0, 2);
		ft.getFlexCellFormatter().addStyleName(0, 0, "inputFormInlineComment");

		ft.setHTML(1, 0, "Name:");
		ft.setHTML(2, 0, "Email:");
		ft.setHTML(3, 0, "Language:");

		ft.getFlexCellFormatter().setStyleName(1, 0, "itemName");
		ft.getFlexCellFormatter().setStyleName(2, 0, "itemName");
		ft.getFlexCellFormatter().setStyleName(3, 0, "itemName");

		final ListBox languages = new ListBox();
		languages.setWidth("200px");
		//languages.addItem("Czech", "cs");
		languages.addItem("English", "en");
		languages.setSelectedIndex(1);
		if (!Utils.getNativeLanguage().isEmpty()) {
			languages.addItem(Utils.getNativeLanguage().get(2), Utils.getNativeLanguage().get(0));
		}

		final ExtendedTextBox name = new ExtendedTextBox();
		final ExtendedTextBox email = new ExtendedTextBox();

		final ExtendedTextBox.TextBoxValidator nameValidator = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (name.getTextBox().getText().trim().isEmpty()) {
					name.setError("Name can't be empty.");
					return false;
				} else {
					name.setOk();
					return true;
				}
			}
		};

		final ExtendedTextBox.TextBoxValidator emailValidator = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (email.getTextBox().getText().trim().isEmpty()) {
					email.setError("Email can't be empty.");
					return false;
				} else if (!JsonUtils.isValidEmail(email.getTextBox().getText().trim())) {
					email.setError("Not valid email address format.");
					return false;
				} else {
					email.setOk();
					return true;
				}
			}
		};

		// Name is now optional
		// name.setValidator(nameValidator);
		email.setValidator(emailValidator);

		ft.setWidget(1, 1, name);
		ft.setWidget(2, 1, email);
		ft.setWidget(3, 1, languages);
		vp.add(ft);

		final TabItem tab = this;

		final CustomButton sendInvitationButton = new CustomButton("Send invitation", SmallIcons.INSTANCE.emailIcon());
		sendInvitationButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				// Name is now optional
				//if (!nameValidator.validateTextBox() || !emailValidator.validateTextBox()) return;
				if (!emailValidator.validateTextBox()) return;

				SendInvitation invite = new SendInvitation(vo.getId(), groupId, JsonCallbackEvents.closeTabDisableButtonEvents(sendInvitationButton, tab));
				invite.inviteUser(email.getTextBox().getText().trim(), name.getTextBox().getText().trim(), languages.getValue(languages.getSelectedIndex()));

			}
		});

		TabMenu menu = new TabMenu();
		vp.add(menu);
		vp.setCellHorizontalAlignment(menu, HasHorizontalAlignment.ALIGN_RIGHT);

		menu.addWidget(sendInvitationButton);
		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, false);
			}
		}));

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
		return SmallIcons.INSTANCE.emailAddIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1213;
		int result = 404;
		result = prime * result;
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

		InviteUserTabItem oth = (InviteUserTabItem) obj;

		if (oth.vo.getId() != this.vo.getId())
			return false;

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {}

	public boolean isAuthorized() {

		if (group == null) return session.isVoAdmin(vo.getId());
		return (session.isVoAdmin(vo.getId()) || session.isGroupAdmin(group.getId()));

	}

}