package cz.metacentrum.perun.webgui.client.applicationresources.pages;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import cz.metacentrum.perun.webgui.client.ApplicationFormGui;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.applicationresources.SendsApplicationForm;
import cz.metacentrum.perun.webgui.client.localization.ApplicationMessages;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetLogins;
import cz.metacentrum.perun.webgui.json.registrarManager.CreateApplication;
import cz.metacentrum.perun.webgui.json.registrarManager.GetApplicationForm;
import cz.metacentrum.perun.webgui.json.registrarManager.GetFormItemsWithPrefilledValues;
import cz.metacentrum.perun.webgui.json.usersManager.FindCompleteRichUsers;
import cz.metacentrum.perun.webgui.json.usersManager.FindUsersByName;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.widgets.Confirm;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Page with VO application form
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class ApplicationFormPage extends ApplicationPage {

	/**
	 * Main body contents
	 */
	private SimplePanel bodyContents = new SimplePanel();
	private VerticalPanel formContent = new VerticalPanel();

	/**
	 * Data
	 */
	private ArrayList<ApplicationFormItemData> data;

	/**
	 * VO
	 */
	private VirtualOrganization vo;

	/**
	 * Group
	 */
	private Group group;

	/**
	 * session
	 */
	private PerunWebSession session = PerunWebSession.getInstance();

	/**
	 * Form type
	 */
	private String type = "INITIAL";

	private ApplicationForm form;

	/**
	 * Switch languages button
	 */
	private PushButton languageButtonCzech;
	private PushButton languageButtonEnglish;

	private boolean submittedOrError = false;


	/**
	 * Creates a new page with application form
	 *
	 * @param vo
	 * @param type INITIAL or EXTENSION
	 */
	public ApplicationFormPage(VirtualOrganization vo, Group group, String type) {
		this.initWidget(bodyContents);
		this.vo = vo;
		this.group = group;
		this.type = type;

		bodyContents.setSize("100%", "100%");
		bodyContents.setStyleName("formContent");

	}

	/**
	 * Prepares the buttons for local languages
	 */

	private void prepareToggleLanguageButton() {

		languageButtonCzech = new PushButton(new Image(SmallIcons.INSTANCE.flagCzechRepublicIcon()));
		languageButtonCzech.setTitle(ApplicationMessages.INSTANCE.changeLanguageToCzech());
		languageButtonCzech.setStyleName("gwt-Button");
		languageButtonCzech.setPixelSize(17, 17);

		languageButtonEnglish = new PushButton(new Image(SmallIcons.INSTANCE.flagGreatBritainIcon()));
		languageButtonEnglish.setTitle(ApplicationMessages.INSTANCE.changeLanguageToEnglish());
		languageButtonEnglish.setStyleName("gwt-Button");
		languageButtonEnglish.setPixelSize(17, 17);

		languageButtonCzech.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				Confirm conf = new Confirm(languageButtonCzech.getTitle(), new HTML(ApplicationMessages.INSTANCE.changeLanguageText()), new ClickHandler(){
					public void onClick(ClickEvent event) {
						// on OK
						UrlBuilder builder = Location.createUrlBuilder().setParameter("locale", "cs");
						Window.Location.replace(builder.buildString());

					}}, new ClickHandler(){
					public void onClick(ClickEvent event) {
						// on CANCEL
					}
				}, true);
				conf.setNonScrollable(true);
				conf.show();
			}
		});

		languageButtonEnglish.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				Confirm conf = new Confirm(languageButtonEnglish.getTitle(), new HTML(ApplicationMessages.INSTANCE.changeLanguageText()), new ClickHandler(){
					public void onClick(ClickEvent event) {
						// on OK
						UrlBuilder builder = Location.createUrlBuilder().setParameter("locale", "en");
						Window.Location.replace(builder.buildString());

					}}, new ClickHandler(){
					public void onClick(ClickEvent event) {
						// on CANCEL
					}
				}, true);
				conf.setNonScrollable(true);
				conf.show();
			}
		});

	}

	/**
	 * Prepares a VO form
	 */
	protected void prepareVoForm() {

		bodyContents.setWidget(formContent);
		formContent.setStyleName("formContentTable");
		submittedOrError = false;

		// try to get user for VOs initial application (only for authz origin)
		if (session.getUser() == null && !session.getPerunPrincipal().getExtSource().equalsIgnoreCase("LOCAL")) {
			tryToFindUserByName(null);
		}

		FlexTable header = new FlexTable();
		header.setWidth("100%");
		header.setCellPadding(5);

		int row = 0;

		// display VO logo if present in attribute
		for (int i=0; i<vo.getAttributes().length(); i++) {
			if (vo.getAttributes().get(i).getFriendlyName().equalsIgnoreCase("voLogoURL")) {
				header.setWidget(row, 0, new Image(vo.getAttributes().get(i).getValue()));
				row++;
			}
		}

		String headerString = "";

		// display application header
		if (type.equalsIgnoreCase("INITIAL")) {
			if (group != null) {
				headerString = ApplicationMessages.INSTANCE.applicationFormForGroup(group.getName());
			} else {
				headerString = ApplicationMessages.INSTANCE.applicationFormForVo(vo.getName());
			}
		} else if (type.equalsIgnoreCase("EXTENSION")) {
			if (group != null) {
				headerString = ApplicationMessages.INSTANCE.membershipExtensionForGroup(group.getName());
			} else {
				headerString = ApplicationMessages.INSTANCE.membershipExtensionForVo(vo.getName());
			}
		}
		header.setHTML(row, 0, "<h1>" + headerString + "</h1>");

		// language button
		prepareToggleLanguageButton();

		FlexTable lang = new FlexTable();
		lang.setWidget(0, 1, languageButtonCzech);
		lang.setWidget(0, 2, languageButtonEnglish);
		header.setWidget(0, 1, lang);
		header.getFlexCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);

		formContent.add(header);

		JsonCallbackEvents jqueryEvent = new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				Scheduler.get().scheduleDeferred(new Command() {
					@Override
					public void execute() {
						positionLinker();
					}
				});
			}
		};

		final GetFormItemsWithPrefilledValues fitems;
		if (group != null) {
			fitems = new GetFormItemsWithPrefilledValues(PerunEntity.GROUP, group.getId(), jqueryEvent);
		} else {
			fitems = new GetFormItemsWithPrefilledValues(PerunEntity.VIRTUAL_ORGANIZATION, vo.getId(), jqueryEvent);
		}

		// pass valid app type in URL or use default

		if (Location.getParameter("type") != null &&
				(Location.getParameter("type").equalsIgnoreCase("INITIAL") || Location.getParameter("type").equalsIgnoreCase("EXTENSION"))) {
			fitems.setType(Location.getParameter("type").toUpperCase());
		} else {
			fitems.setType(type);
		}

		fitems.setHidden(true);

		fitems.setSendFormHandler(new SendsApplicationForm() {
			public void sendApplicationForm(CustomButton button) {
				data = fitems.getValues();
				sendForm(button);
			}
		});

		fitems.retrieveData();

		formContent.add(fitems.getContents());

		formContent.add(new HTML("<br /><br /><br />"));

		JsonCallbackEvents formEvent = new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				form = jso.cast();
			}
		};

		GetApplicationForm formRequest;

		if (group != null) {
			formRequest = new GetApplicationForm(PerunEntity.GROUP, group.getId(), formEvent);
		} else {
			formRequest = new GetApplicationForm(PerunEntity.VIRTUAL_ORGANIZATION, vo.getId(), formEvent);
		}
		formRequest.retrieveData();

	}

	/**
	 * Send form
	 */
	protected void sendForm(final CustomButton button) {

		PerunPrincipal pp = session.getPerunPrincipal();

		// fed info
		String fedInfo = "";
		fedInfo += "{";
		fedInfo += " displayName=\"" + pp.getAdditionInformations("displayName")+"\"";
		fedInfo += " commonName=\"" + pp.getAdditionInformations("cn")+"\"";
		fedInfo += " givenName=\"" + pp.getAdditionInformations("givenName")+"\"";
		fedInfo += " sureName=\"" + pp.getAdditionInformations("sn")+"\"";
		fedInfo += " loa=\"" + pp.getAdditionInformations("loa")+"\"";
		fedInfo += " mail=\"" + pp.getAdditionInformations("mail")+"\"";
		fedInfo += " organization=\"" + pp.getAdditionInformations("o")+"\"";
		fedInfo += " }";

		Application app = Application.construct(vo, group, type, fedInfo, pp.getActor(), pp.getExtSource(), pp.getExtSourceType(), pp.getExtSourceLoa());

		if (session.getUser() != null) {
			// set user association if known from perun
			app.setUser(session.getUser());
		}

		// loading
		final PopupPanel loadingBox = session.getUiElements().perunLoadingBox(ApplicationMessages.INSTANCE.processing());

		// Create application request
		CreateApplication ca = new CreateApplication(JsonCallbackEvents.disableButtonEvents(button, new JsonCallbackEvents() {
			@Override
			public void onLoadingStart() {
				// show loading box
				loadingBox.show();
			}
			@Override
			public void onFinished(JavaScriptObject jso) {
				loadingBox.hide();
				formOk(jso);
			}
			@Override
			public void onError(PerunError err) {
				loadingBox.hide();
				formError(err);
			}
		}));

		// Send the request
		ca.createApplication(app, data);
	}

	/**
	 * Form OK
	 */
	protected void formOk(JavaScriptObject jso) {

		ApplicationFormGui.redrawGuiWithMenu();

		submittedOrError = true;

		if (session.getUser() == null && !session.getPerunPrincipal().getExtSource().equalsIgnoreCase("LOCAL")) {
			// if not yet user of perun, retry search for similar users after app submit
			// only for VO initial applications
			tryToFindUserByName(jso);
		}

		Boolean autoApproval = false;
		if (form != null) {
			if (type.equalsIgnoreCase("INITIAL")) {
				autoApproval = form.getAutomaticApproval();
			} else if (type.equalsIgnoreCase("EXTENSION")) {
				autoApproval = form.getAutomaticApprovalExtension();
			}
		}
		String approveText = "";
		if (autoApproval) {
			approveText = ApplicationMessages.INSTANCE.autoApprovalApplicationText();
		} else {
			approveText = ApplicationMessages.INSTANCE.nonAutoApprovalApplicationText();
		}

		approveText = "<p><strong>" + approveText + "</strong></p>";

		// conditional sending of validation email
		String validationText = "";
		ArrayList<ApplicationFormItemData> data = JsonUtils.jsoAsList(jso);
		for (ApplicationFormItemData item : data) {
			if ("VALIDATED_EMAIL".equalsIgnoreCase(item.getFormItem().getType())) {
				// if not verified, email was sent
				if (!"1".equalsIgnoreCase(item.getAssuranceLevel())) {
					validationText = "<p>" + ApplicationMessages.INSTANCE.ifEmailProvidedCheckInbox() + "</p>";
				}
			}
		}

		if (type.equalsIgnoreCase("INITIAL")) {

			FlexTable ft = new FlexTable();
			ft.setSize("100%", "300px");

			String succSendText = ApplicationMessages.INSTANCE.applicationSuccessfullySent(vo.getName());
			if (group != null) {
				succSendText = ApplicationMessages.INSTANCE.applicationSuccessfullySent(group.getName());
			}

			ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.acceptIcon())+"<h2>"+ succSendText +"</h2>" + validationText + approveText);
			ft.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
			ft.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
			bodyContents.setWidget(ft);

			if (Location.getParameter("targetnew") != null) {
				Location.replace(Location.getParameter("targetnew"));
			}

		} else if (type.equalsIgnoreCase("EXTENSION")) {

			FlexTable ft = new FlexTable();
			ft.setSize("100%", "300px");
			ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.acceptIcon())+"<h2>" + ApplicationMessages.INSTANCE.membershipExtensionSuccessfullySent(vo.getName()) + "</h2>" +
					approveText);
			ft.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
			ft.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
			bodyContents.setWidget(ft);

			if (autoApproval) {
				// automatically extended
				if (Location.getParameter("targetextended") != null) {
					Location.replace(Location.getParameter("targetextended"));
				}
			} else {
				// TODO - only when user have valid account
				if (Location.getParameter("targetexisting") != null) {
					Location.replace(Location.getParameter("targetexisting"));
				}
			}

		}

	}

	/**
	 * Form error
	 */
	protected void formError(PerunError error) {

		if (error != null ) {

			submittedOrError = true;

			FlexTable ft = new FlexTable();
			ft.setSize("100%", "300px");
			bodyContents.setWidget(ft);

			if (error.getName().equalsIgnoreCase("ApplicationNotCreatedException")) {

				// application WAS NOT SAVED
				ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.errorIcon())+"<h2>" + ApplicationMessages.INSTANCE.errorWhileCreatingApplication() + "</h2>" +
						"<p><strong>" + ApplicationMessages.INSTANCE.errorWhileCreatingApplicationMessage() + "</strong></p>");

				// back to form button to prevent user from losing data
				final CustomButton back = new CustomButton("Back", "Back to application form", SmallIcons.INSTANCE.arrowLeftIcon());

				ft.setWidget(1, 0, back);
				ft.getFlexCellFormatter().setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_CENTER);
				ft.getFlexCellFormatter().setVerticalAlignment(1, 0, HasVerticalAlignment.ALIGN_MIDDLE);

				back.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						submittedOrError = false;
						bodyContents.setWidget(formContent);
					}
				});

			} else {
				// some minor error - application WAS SAVED
				ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.errorIcon())+"<h2>" + ApplicationMessages.INSTANCE.errorWhileCreatingApplication() + "</h2>" +
						"<p><strong>" + ApplicationMessages.INSTANCE.voAdministratorWasNotified() + "</strong>"+
						"<p>" + ApplicationMessages.INSTANCE.ifEmailProvidedCheckInbox() + "</p>");
			}

			ft.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
			ft.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);

		}

	}

	@Override
	public void menuClick() {

		// load application only once and also when submitted or on error
		if (formContent.getWidgetCount() == 0 || submittedOrError) {
			prepareVoForm();
		} else {
			// do nothing
		}

	}

	/**
	 * Try to find user by name
	 *
	 * If user found, message box shown
	 *
	 * @param jso returned data
	 */
	private void tryToFindUserByName(JavaScriptObject jso) {

		PerunPrincipal pp = session.getPerunPrincipal();
		String displayName = pp.getAdditionInformations("displayName");

		// before app submitted
		if(displayName.equals("")){
			displayName = pp.getAdditionInformations("cn");
		}

		if (jso != null) {

			// after Application is submitted
			ArrayList<ApplicationFormItemData> data = JsonUtils.jsoAsList(jso);

			for (ApplicationFormItemData item : data) {
				if ("urn:perun:user:attribute-def:core:lastName".equalsIgnoreCase(item.getFormItem().getPerunDestinationAttribute())) {
					// set name
					if (item.getValue() != null && !item.getValue().isEmpty()) displayName = item.getValue();
					break;
				}
			}

			for (ApplicationFormItemData item : data) {
				if ("urn:perun:user:attribute-def:core:displayName".equalsIgnoreCase(item.getFormItem().getPerunDestinationAttribute())) {
					// set name
					if (item.getValue() != null && !item.getValue().isEmpty()) displayName = item.getValue();
					break;
				}
			}

		}

		if(displayName.equals("")){
			// name empty
			return;
		}

		// try to find
		JsonPostClient jspc = new JsonPostClient(new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				ArrayList<User> users = JsonUtils.<User>jsoAsList(jso);
				if (users != null && !users.isEmpty()) similarUsersFound(JsonUtils.<User>jsoAsList(jso));
			}
		});
		JSONObject query = new JSONObject();

		query.put("voId", new JSONNumber(vo.getId()));
		if (group != null) {
			query.put("groupId", new JSONNumber(group.getId()));
		} else {
			query.put("groupId", new JSONNumber(0));
		}
		query.put("type", new JSONString(type));

		jspc.sendData("registrarManager/checkForSimilarUsers", query);

	}

	/**
	 * When similar users found, display a message
	 *
	 * @param users
	 */
	protected void similarUsersFound(ArrayList<User> users) {

		FlexTable ft = new FlexTable();

		ft.setWidth("600px");
		FlexCellFormatter ftf = ft.getFlexCellFormatter();

		ft.setHTML(0, 0, ApplicationMessages.INSTANCE.similarUsersFoundIsItYou() + "<br /><br />");
		ftf.setColSpan(0, 0, 3);

		ft.setHTML(1, 0, "<strong>" + ApplicationMessages.INSTANCE.name() + "</strong>");
		ft.setHTML(1, 1, "<strong>" + ApplicationMessages.INSTANCE.email() +"</strong>");
		ft.setHTML(1, 2, "<strong>" + ApplicationMessages.INSTANCE.organization() +"</strong>");

		int i = 2;

		for (User user : users) {

			// skip service users
			if (!user.isServiceUser()) {

				ft.setHTML(i, 0, user.getFullNameWithTitles());

				if (user.getAttribute("urn:perun:user:attribute-def:def:preferredMail").getValue() == null ||
						user.getAttribute("urn:perun:user:attribute-def:def:preferredMail").getValue().isEmpty()) {
					ft.setHTML(i, 1, "N/A");
				} else {
					ft.setHTML(i, 1, user.getAttribute("urn:perun:user:attribute-def:def:preferredMail").getValue());
				}

				if (user.getAttribute("urn:perun:user:attribute-def:def:organization").getValue() == null ||
						user.getAttribute("urn:perun:user:attribute-def:def:organization").getValue().isEmpty()) {
					ft.setHTML(i, 2, "N/A");
				} else {
					ft.setHTML(i, 2, user.getAttribute("urn:perun:user:attribute-def:def:organization").getValue());
				}

				i++;

			}

		}

		// confirm element
		final Confirm confirm = new Confirm(ApplicationMessages.INSTANCE.similarUsersFound(), ft, new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				Window.Location.replace(Utils.getIdentityConsolidatorLink(true));
			}
		}, true);

		confirm.setOkButtonText(ApplicationMessages.INSTANCE.joinIdentity());
		confirm.setOkIcon(SmallIcons.INSTANCE.userGreenIcon());
		confirm.setCancelButtonText(ApplicationMessages.INSTANCE.notJoinIdentity());
		confirm.show();

	}

	private final native void positionLinker() /*-{

        var linker = $wnd.jQuery("#cesnet_linker_placeholder");

        if (!$('#cesnet_linker_placeholder').is(':empty')){

            $wnd.jQuery("#cesnet_linker_placeholder").remove();
            $wnd.jQuery("body").append(linker);
            $wnd.jQuery("#appFormGUI").parent().parent().css({"position":"absolute" , "left":"0px" , "top":"40px" , "right":"0px" , "bottom":"0px"});
			//$wnd.jQuery("#appFormGUI").css({"position":"absolute" , "left":"0px" , "top":"40px" , "right":"0px" , "bottom":"0px"});
            $wnd.jQuery("body").append("<script src=\"https://linker.cesnet.cz/linker.js\" async type=\"text/javascript\">");

        }

    }-*/;

}