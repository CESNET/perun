package cz.metacentrum.perun.webgui.tabs.registrartabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.registrarManager.AddApplicationMail;
import cz.metacentrum.perun.webgui.json.registrarManager.GetApplicationForm;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Types of emails for application
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class CreateMailTabItem implements TabItem {

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
	private Label titleWidget = new Label("Application e-mails");

	// data
	private VirtualOrganization vo;
	private int voId = 0;
	private Group group;
	private int groupId = 0;
	private ApplicationForm form;
	private PerunEntity entity;
	private int entityId;
	private TabLayoutPanel tabPanel;

	/**
	 * DATA FOR SENDING
	 */
	private CheckBox sendingEnabledCheckBox = new CheckBox();
	private Map<String, TextArea> messagesTextAreas = new HashMap<String, TextArea>();
	private Map<String, TextBox> messagesSubjects = new HashMap<String, TextBox>();

	private CustomButton saveButton;

	// inputs
	private ListBox applicationTypeListbox = new ListBox();
	private ListBox mailTypeListbox = new ListBox();

	/**
	 * Creates a tab instance
	 *
	 * @param vo
	 * @param group (null = only VO form)
	 * @param form
	 */
	public CreateMailTabItem(VirtualOrganization vo, Group group, ApplicationForm form){
		this.vo = vo;
		this.voId = vo.getId();
		this.groupId = 0;
		if (group != null) {
			this.group = group;
			this.groupId = group.getId();
		}
		this.form = form;
		tabPanel = new TabLayoutPanel(30, Unit.PX);
	}

	/**
	 * Creates a tab instance
	 *
	 * @param voId
	 * @param groupId
	 */
	public CreateMailTabItem(int voId, int groupId){
		this.voId = voId;
		JsonCallbackEvents events = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				vo = jso.cast();
			}
		};
		new GetEntityById(PerunEntity.VIRTUAL_ORGANIZATION, voId, events).retrieveData();

		JsonCallbackEvents events2 = new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				form = jso.cast();
			}
		};
		this.groupId = groupId;
		if (groupId != 0) {
			new GetApplicationForm(PerunEntity.GROUP, groupId, events2).retrieveData();
			JsonCallbackEvents events3 = new JsonCallbackEvents(){
				public void onFinished(JavaScriptObject jso) {
					group = jso.cast();
				}
			};
			new GetEntityById(PerunEntity.GROUP, groupId, events3);
		} else {
			new GetApplicationForm(PerunEntity.VIRTUAL_ORGANIZATION, voId, events2).retrieveData();
		}
		tabPanel = new TabLayoutPanel(30, Unit.PX);
	}

	public boolean isPrepared(){
		if (groupId == 0) {
			return (vo != null && form != null);
		} else {
			return (vo != null && form != null && group != null);
		}
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	/**
	 * Returns message texarea
	 *
	 * @param locale
	 * @return
	 */
	private Widget messageTab(String locale) {

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("500px", "350px");
		vp.setSpacing(10);

		// layout
		FlexTable ft = new FlexTable();
		ft.setStyleName("inputFormFlexTable");
		FlexCellFormatter ftf = ft.getFlexCellFormatter();

		// inputs
		TextBox tb = new TextBox();
		tb.setWidth("385px");
		messagesSubjects.put(locale, tb);

		TextArea ta = new TextArea();
		ta.setSize("450px", "190px");
		messagesTextAreas.put(locale, ta);

		// adds inputs to the flex table
		ft.setHTML(0, 0, "Subject:");
		ftf.setStyleName(0, 0, "itemName");
		ft.setWidget(0, 1, tb);

		ft.setHTML(1, 0, "Text:");
		ftf.setStyleName(1, 0, "itemName");
		//ftf.setColSpan(1, 0, 2);

		Anchor a = new Anchor("see available tags >>");
		a.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				// select last tab
				tabPanel.selectTab(tabPanel.getWidgetCount()-1);
			}
		});
		ft.setWidget(1, 1, a);

		ft.setWidget(2, 0, ta);
		ftf.setColSpan(2, 0, 2);

		vp.add(ft);
		vp.add(new HTML("&nbsp;"));

		return vp;

	}

	/**
	 * Returns flex table with basic information
	 *
	 * @return
	 */
	private Widget basicInformationTab() {

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("500px", "350px");
		vp.setSpacing(10);

		// layout
		FlexTable ft = new FlexTable();
		ft.setStyleName("inputFormFlexTable");
		FlexCellFormatter ftf = ft.getFlexCellFormatter();

		vp.add(ft);
		vp.add(new HTML("&nbsp;"));

		// inputs - filling
		mailTypeListbox.clear();
		for (ApplicationMail.MailType type : ApplicationMail.MailType.values()) {
			if (group == null && type.equals(ApplicationMail.MailType.APPROVABLE_GROUP_APP_USER)) {
				// This notification is relevant only for the groups, so skip it from the offering
				continue;
			}
			mailTypeListbox.addItem(ApplicationMail.getTranslatedMailType(type.toString()), type.toString());
		}

		int initialIndex = 0;
		applicationTypeListbox.clear();
		for (Application.ApplicationType type : Application.ApplicationType.values()) {
			applicationTypeListbox.addItem(Application.getTranslatedType(type.toString()), type.toString());
			if (type.equals(Application.ApplicationType.INITIAL)) {
				initialIndex = applicationTypeListbox.getItemCount()-1;
			}
		}

		final int initIndex = initialIndex;
		mailTypeListbox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				if (mailTypeListbox.getValue(mailTypeListbox.getSelectedIndex()).equals("USER_INVITE")) {
					applicationTypeListbox.setEnabled(false);
					applicationTypeListbox.setSelectedIndex(initIndex);
				} else {
					applicationTypeListbox.setEnabled(true);
				}
			}
		});

		// basic info
		int row = 0;

		ft.setHTML(row, 0, "E-mail type:");
		ft.setWidget(row, 1, mailTypeListbox);
		ftf.setStyleName(row, 0, "itemName");

		row++;
		ft.setHTML(row, 1, "Selected type of notification (action which trigger sending and who is notified).");
		ftf.setStyleName(row, 1, "inputFormInlineComment");

		row++;

		ft.setHTML(row, 0, "Application type: ");
		ft.setWidget(row, 1, applicationTypeListbox);
		ftf.setStyleName(row, 0, "itemName");
		ftf.setWidth(row, 0, "120px");

		row++;
		ft.setHTML(row, 1, "Select which application type will trigger sending.");
		ftf.setStyleName(row, 1, "inputFormInlineComment");

		row++;

		sendingEnabledCheckBox.setValue(true);
		ft.setHTML(row, 0, "Sending enabled:");
		ft.setWidget(row, 1, sendingEnabledCheckBox);
		ftf.setStyleName(row, 0, "itemName");

		row++;
		ft.setHTML(row, 1, "If checked, notification will be sent. Un-check it to temporary disable sending.");
		ftf.setStyleName(row, 1, "inputFormInlineComment");

		return vp;

	}

	/**
	 * Returns flex table with available tags
	 *
	 * @return
	 */
	private Widget availableTagsTab() {

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("500px", "350px");
		vp.setSpacing(10);

		// layout
		FlexTable ft = new FlexTable();
		ft.setStyleName("inputFormFlexTable");
		FlexCellFormatter ftf = ft.getFlexCellFormatter();

		ScrollPanel sp = new ScrollPanel(ft);
		sp.addStyleName("perun-tableScrollPanel");
		sp.setSize("100%", "300px");

		vp.add(sp);
		vp.add(new HTML("&nbsp;"));

		ft.setHTML(0, 0, "Following tags can be used in mail's subject and text and are replaced by actual data on sending. Just copy/paste tags from here to input form. When no data for tag is found, it's replaced by whitespace.<p><strong>For user invitation email there is only limited set of tags, see end of this help.</strong>");
		ftf.addStyleName(0, 0, "inputFormInlineComment");

		HTML text = new HTML("<strong><u>Application related:</u></strong>" +

				"<p><strong>{appId}</strong> - application ID" +
				"<br/><strong>{actor}</strong> - user's login used when submitting application" +
				"<br/><strong>{extSource}</strong> - user's identity provider when submitting application" +
				"<br/><strong>{voName}</strong> - name of VO of application form" +
				"<br/><strong>{groupName}</strong> - name of group, if application form is for group membership" +
				"<br/><strong>{mailFooter}</strong> - common mail footer defined by VO" +
				"<br/><strong>{errors}</strong> - errors description, what happened while processing new application. Useful for VO administrators." +
				"<br/><strong>{customMessage}</strong> - optional message passed by administrators when rejecting an application" +
				"<br/><strong>{autoApproveError}</strong> -  error that caused automatic approval failure" +
				"<br/><strong>{fromApp-itemName}</strong> - value of a form item in user's application. You MUST specify the itemName, e.g. {fromApp-mail} will print value of item with short name 'mail' from user's application." +

				"<p><strong><u>User related:</u></strong>" +

				"<p><strong>{firstName}</strong> - users first name taken from application form or Perun" +
				"<br/><strong>{lastName}</strong> - users last name taken from application form or Perun" +
				"<br/><strong>{displayName}</strong> - users display name taken from application form or Perun" +
				"<br/><strong>{mail}</strong> - users preferred mail from application form or Perun" +
				"<br/><strong>{login-<i>namespace</i>}</strong> - user's login in selected namespace, taken from registration form or Perun. You MUST specify the namespace, e.g. <i>{login-einfra}</i> will print user's login in einfra namespace." +
				"<br/><strong>{membershipExpiration}</strong> - membership expiration date decided after membership creation or extension." +

				"<p><strong><u>Validation links for users:</u></strong>" +

				"<p><span class=\"inputFormInlineComment\">Works only for \"Mail validation / user \" mail type! Used to verify email address provided by users =&gt; verify application.</span>" +

				"<p><strong>{validationLink}</strong> - link for email address verification. Please make sure you set \"Registrar URL\" setting of your VO/group. " +
				"If you don't specify authorization in \"Registrar URL\", you can use following options: " +

				"<p><strong>{validationLink-krb}</strong> - link for Kerberos authentication" +
				"<br/><strong>{validationLink-fed}</strong> - link for Shibboleth IdP (federation) authentication" +
				"<br/><strong>{validationLink-cert}</strong> - link for personal certificate authentication" +
				"<br/><strong>{validationLink-non}</strong> - link without any authentication" +

				"<p><strong>{redirectUrl}</strong> - contains the URL parameter which was present during the application submission. If the application was initial, contains the value of the 'targetnew' URL parameter. If the application was extensional, contains the value of the 'targetexisting' parameter." +

				"<p><strong><u>Application GUI links for users:</u></strong>" +

				"<p><span class=\"inputFormInlineComment\">Used to navigate users to the list of theirs applications.</span>" +

				"<p><strong>{appGuiUrl}</strong> - link to overview of submitted registrations for users. Please make sure you set \"Registrar URL\" setting of your VO/group. " +
				"If you don't specify authorization in \"Registrar URL\", you can use following options: " +

				"<p><strong>{appGuiUrl-krb}</strong> - link for Kerberos authentication" +
				"<br/><strong>{appGuiUrl-fed}</strong> - link for Shibboleth IdP (federation) authentication" +
				"<br/><strong>{appGuiUrl-cert}</strong> - link for personal certificate authentication" +
				"<br/><strong>{appGuiUrl-non}</strong> - link without any authentication" +

				"<p><strong><u>Application GUI links for administrators:</u></strong>" +

				"<p><span class=\"inputFormInlineComment\">Used to navigate administrators to the registration detail, where they can check and approve or reject the application.</span>" +

				"<p><strong>{appDetailUrl}</strong> - link to registration detail in administrative GUI. Please make sure you set \"Registrar URL\" setting of your VO/group. " +
				"If you don't specify authorization in \"Registrar URL\", you can use following options: " +

				"<p><strong>{appDetailUrl-krb}</strong> - link for Kerberos authentication" +
				"<br/><strong>{appDetailUrl-fed}</strong> - link for Shibboleth IdP (federation) authentication" +
				"<br/><strong>{appDetailUrl-cert}</strong> - link for personal certificate authentication" +
				"<br/><strong>{appDetailUrl-newGUI}</strong> - link for new admin GUI" +

				"<p><strong><u>Perun GUI links for administrators:</u></strong>" +

				"<p><span class=\"inputFormInlineComment\">Used to navigate administrators to the administrative GUI of Perun. Can be used for users to locate user detail too.</span>" +

				"<p><strong>{perunGuiUrl}</strong> - link to administrative GUI. Please make sure you set \"Registrar URL\" setting of your VO/group. " +
				"If you don't specify authorization in \"Registrar URL\", you can use following options: " +

				"<p><strong>{perunGuiUrl-krb}</strong> - link for Kerberos authentication" +
				"<br/><strong>{perunGuiUrl-fed}</strong> - link for Shibboleth IdP (federation) authentication" +
				"<br/><strong>{perunGuiUrl-cert}</strong> - link for personal certificate authentication" +
				"<br/><strong>{perunGuiUrl-newGUI}</strong> - link for new admin GUI" +

				"<p><strong><u>User invitations:</u></strong>" +

				"<p><span class=\"inputFormInlineComment\">Following tags can be used on user invitation template</span>" +

				"<p><strong>{voName}</strong> - name of VO to invite user into" +
				"<br/><strong>{groupName}</strong> - name of Group to invite user into" +
				"<br/><strong>{displayName}</strong> - invited user's name" +
				"<br/><strong>{mailFooter}</strong> - common mail footer defined by VO" +

				"<p><strong>{invitationLink}</strong> - link to registration form. Please make sure you set \"Registrar URL\" setting of your VO/group. " +
				"If you don't specify authorization in \"Registrar URL\", you can use following options: " +

				"<p><strong>{invitationLink-krb}</strong> - link for Kerberos authentication" +
				"<br/><strong>{invitationLink-fed}</strong> - link for Shibboleth IdP (federation) authentication" +
				"<br/><strong>{invitationLink-cert}</strong> - link for personal certificate authentication"+
				"<br/><strong>{invitationLink-non}</strong> - link without any authentication"

		);

		text.getElement().setAttribute("style", "line-height: 1.5em !important;");

		ft.setWidget(1, 0, text);

		return vp;

	}

	public Widget draw() {

		String title = "";
		if (group != null) {
			title = group.getName();
			entity = PerunEntity.GROUP;
			entityId = groupId;
		} else {
			title = vo.getName();
			entity = PerunEntity.VIRTUAL_ORGANIZATION;
			entityId = voId;
		}
		this.titleWidget.setText("Add email notification");

		// languages
		ArrayList<String> languages = new ArrayList<String>();
		if (!Utils.getNativeLanguage().isEmpty()) {
			languages.add(Utils.getNativeLanguage().get(0));
		}
		languages.add(0, "en");

		// vertical panel
		VerticalPanel vp = new VerticalPanel();
		vp.setWidth("500px");
		vp.setHeight("350px");

		// tab panel
		tabPanel.addStyleName("smallTabPanel");
		tabPanel.addStyleName("smallTabPanelWithBorder");
		tabPanel.setHeight("350px");

		// basic settings
		tabPanel.add(basicInformationTab(), "Basic settings");

		// for each locale add tab
		for(String locale : languages){
			tabPanel.add(messageTab(locale), "Lang: "+locale);
		}

		tabPanel.add(availableTagsTab(), "Available tags");

		// add menu
		final TabItem tab = this;
		TabMenu tabMenu = new TabMenu();
		saveButton = TabMenu.getPredefinedButton(ButtonType.CREATE, ButtonTranslation.INSTANCE.createEmailNotificationForApplication(), new ClickHandler() {
			public void onClick(ClickEvent event) {

				String appType = applicationTypeListbox.getValue(applicationTypeListbox.getSelectedIndex());
				String mailType = mailTypeListbox.getValue(mailTypeListbox.getSelectedIndex());
				boolean send = sendingEnabledCheckBox.getValue();

				HashMap<String, MailText> messages = new HashMap<String, MailText>();

				// messages
				for(Map.Entry<String, TextArea> entry : messagesTextAreas.entrySet()) {
					String locale = entry.getKey();
					String subject = messagesSubjects.get(entry.getKey()).getValue();
					String text = entry.getValue().getText();
					MailText mt = MailText.construct(locale, subject, text, false); // we don't support HTML in old GUI

					messages.put(locale, mt);
				}

				// create object
				ApplicationMail appMail = ApplicationMail.construct(appType, form.getId(), mailType, send, messages);

				// request
				AddApplicationMail req = new AddApplicationMail(entity, JsonCallbackEvents.closeTabDisableButtonEvents(saveButton, tab, true));
				req.addMail(appMail, entityId);

			}
		});
		tabMenu.addWidget(saveButton);
		tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, isRefreshParentOnClose());
			}
		}));

		// add tab panel to main panel
		vp.add(tabPanel);
		vp.setCellWidth(tabPanel, "500px");
		vp.setCellHeight(tabPanel, "350px");

		vp.add(tabMenu);
		vp.setCellHeight(tabMenu, "30px");
		vp.setCellHorizontalAlignment(tabMenu, HasHorizontalAlignment.ALIGN_RIGHT);

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
		return SmallIcons.INSTANCE.emailAddIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1481;
		int result = 3;
		result = prime * result + voId + groupId;
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
		CreateMailTabItem other = (CreateMailTabItem) obj;
		if (voId != other.voId || groupId != other.groupId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}


	public void open() {

	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(voId) || session.isGroupAdmin(groupId)) {
			return true;
		} else {
			return false;
		}

	}

}
