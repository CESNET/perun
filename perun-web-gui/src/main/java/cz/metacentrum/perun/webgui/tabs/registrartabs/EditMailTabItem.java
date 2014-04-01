package cz.metacentrum.perun.webgui.tabs.registrartabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
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
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.registrarManager.UpdateApplicationMail;
import cz.metacentrum.perun.webgui.model.Application;
import cz.metacentrum.perun.webgui.model.ApplicationMail;
import cz.metacentrum.perun.webgui.model.MailText;
import cz.metacentrum.perun.webgui.tabs.RegistrarTabs;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
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
public class EditMailTabItem implements TabItem, TabItemWithUrl {

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

	private ApplicationMail appMail;
	private int appMailId;

	/**
	 * DATA FOR SENDING
	 */
	private CheckBox sendingEnabledCheckBox = new CheckBox();
	private Map<String, TextArea> messagesTextAreas = new HashMap<String, TextArea>();
	private Map<String, TextBox> messagesSubjects = new HashMap<String, TextBox>();

	private CustomButton saveButton;
    private TabLayoutPanel tabPanel = new TabLayoutPanel(35, Unit.PX);

	/**
	 * Creates a tab instance
	 *
     * @param appMail
     */
	public EditMailTabItem(ApplicationMail appMail){
		this.appMail = appMail;
		this.appMailId = appMail.getId();
	}

	/**
	 * Creates a tab instance
	 *
     * @param appMailId
     */
	public EditMailTabItem(int appMailId){
		this.appMailId = appMailId;
		new GetEntityById(PerunEntity.APPLICATION_MAIL, appMailId, new JsonCallbackEvents(){
            public void onFinished(JavaScriptObject jso) {
                appMail = jso.cast();
            }
        }).retrieveData();
	}

	public boolean isPrepared(){
		return (appMail != null);
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
        ftf.setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);

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

		// fill values
		MailText message = appMail.getMessage(locale);
		if(message != null){
			ta.setText(message.getText());
			tb.setText(message.getSubject());
		}

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

        // basic info
        int row = 0;

        ft.setHTML(row, 0, "E-mail type:");
        ft.setHTML(row, 1, ApplicationMail.getTranslatedMailType(appMail.getMailType()));
        ftf.setStyleName(row, 0, "itemName");

        row++;
        ft.setHTML(row, 1, "Type of notification (action which trigger sending and who is notified).");
        ftf.setStyleName(row, 1, "inputFormInlineComment");

        row++;

        ft.setHTML(row, 0, "Application type: ");
        ft.setHTML(row, 1, Application.getTranslatedType(appMail.getAppType()));
        ftf.setStyleName(row, 0, "itemName");
        ftf.setWidth(row, 0, "120px");

        row++;
        ft.setHTML(row, 1, "Application type which will trigger sending.");
        ftf.setStyleName(row, 1, "inputFormInlineComment");

        row++;

        sendingEnabledCheckBox.setValue(appMail.isSend());
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
    private Widget availableTagsTab()
    {

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

        ft.setHTML(0, 0, "Following tags can be used in mail's subject and text and are replaced by actual data on sending. Just copy/paste tags from here to input form. When no data for tag is found, it's replaced by whitespace.");
        ftf.addStyleName(0, 0, "inputFormInlineComment");

        HTML text = new HTML("<strong><u>Application related:</u></strong><br/>" +

                "</br><strong>{appId}</strong> - application ID" +
                "<br/><strong>{actor}</strong> - user's login used when submitting application" +
                "<br/><strong>{extSource}</strong> - user's identity provider when submitting application" +
                "<br/><strong>{voName}</strong> - name of VO of application form" +
                "<br/><strong>{groupName}</strong> - name of group, if application form is for group membership" +
                "<br/><strong>{mailFooter}</strong> - common mail footer defined by VO" +
                "<br/><strong>{errors}</strong> - errors description, what happened while processing new application. Useful for VO administrators." +
                "<br/><strong>{customMessage}</strong> - optional message passed by administrators when rejecting an application" +

                "<br/></br><strong><u>User related:</u></strong><br/>" +

                "<br/><strong>{firstName}</strong> - user's first name taken from application form or Perun" +
                "<br/><strong>{lastName}</strong> - user's last name taken from application form or Perun" +
                "<br/><strong>{displayName}</strong> - user's display name taken from application form or Perun" +
                "<br/><strong>{login-<i>namespace</i>}</strong> - user's login in selected namespace, taken from application form or Perun. You MUST specify namespace, e.g. <i>{login-einfra}</i> will print user's login in einfra namespace." +
                "<br/><strong>{membershipExpiration}</strong> - membership expiration date decided after membership creation or extension." +

                "<br/></br><strong><u>Validation links for users:</u></strong><br/>" +

                "<br/><span class=\"inputFormInlineComment\">Works only for \"Mail validation / user \" mail type! Used to verify email address provided by users =&gt; verify application.</span><br/>" +

                "<br/><strong>{validationLink}</strong> - link with federation authz" +
                "<br/><strong>{validationLinkKrb}</strong> - link with kerberos authz" +
                "<br/><strong>{validationLinkCert}</strong> - link with IGTF certificate authz" +
                "<br/><strong>{validationLinkNon}</strong> - link without any authz" +

                "<br/></br><strong><u>Application GUI links for users:</u></strong><br/>" +

                "<br/><span class=\"inputFormInlineComment\">Used to navigate users to the list of theirs applications.</span><br/>" +

                "<br/><strong>{appGuiUrl}</strong> - link with federation authz" +
                "<br/><strong>{appGuiUrlKrb}</strong> - link with kerberos authz" +
                "<br/><strong>{appGuiUrlCert}</strong> - link with IGTF certificate authz" +
                "<br/><strong>{appGuiUrlNon}</strong> - link without any authz" +

                "<br/></br><strong><u>Application GUI links for administrators:</u></strong><br/>" +

                "<br/><span class=\"inputFormInlineComment\">Used to navigate administrators to the application detail, where they can check and approve or reject application.</span><br/>" +

                "<br/><strong>{appDetailUrlFed}</strong> - link with federation authz" +
                "<br/><strong>{appDetailUrlKrb}</strong> - link with kerberos authz" +
                "<br/><strong>{appDetailUrlCert}</strong> - link with IGTF certificate authz");

        text.getElement().setAttribute("style", "line-height: 1.5em !important;");

        ft.setWidget(1, 0, text);

        return vp;

    }

	public Widget draw() {

		this.titleWidget.setText("Edit notification");

		// languages
		ArrayList<String> languages = new ArrayList<String>();
		languages.add("cs");
		languages.add("en");

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
		saveButton = TabMenu.getPredefinedButton(ButtonType.SAVE, ButtonTranslation.INSTANCE.saveEmailNotificationForApplication(), new ClickHandler() {
            public void onClick(ClickEvent event) {
                // values
                boolean send = sendingEnabledCheckBox.getValue();
                appMail.setSend(send);

                // messages
                for(Map.Entry<String, TextArea> entry : messagesTextAreas.entrySet())
                {
                    String locale = entry.getKey();
                    String subject = messagesSubjects.get(entry.getKey()).getValue();
                    String text = entry.getValue().getText();
                    MailText mt = MailText.construct(locale, subject, text);

                    appMail.setMessage(locale, mt);
                }

                // request
                UpdateApplicationMail req = new UpdateApplicationMail(JsonCallbackEvents.closeTabDisableButtonEvents(saveButton, tab));
                req.updateMail(appMail);
            }
        });
        tabMenu.addWidget(saveButton);
        tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                session.getTabManager().closeTab(tab, false);
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
		final int prime = 61;
		int result = 1;
		result = prime * result + appMailId;
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
		EditMailTabItem other = (EditMailTabItem) obj;
		if (appMailId != other.appMailId)
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

		if (session.isVoAdmin() || session.isGroupAdmin()) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "app-mail-edit";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters()
	{
		return RegistrarTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?appMail=" + appMailId;
	}

	static public EditMailTabItem load(Map<String, String> parameters)
	{
		int appMailId = Integer.parseInt(parameters.get("appMail"));
		return new EditMailTabItem(appMailId);
	}

}