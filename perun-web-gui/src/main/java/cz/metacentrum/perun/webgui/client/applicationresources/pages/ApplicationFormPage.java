package cz.metacentrum.perun.webgui.client.applicationresources.pages;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.i18n.client.LocaleInfo;
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
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonPostClient;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.registrarManager.CreateApplication;
import cz.metacentrum.perun.webgui.json.registrarManager.GetApplicationForm;
import cz.metacentrum.perun.webgui.json.registrarManager.GetFormItemsWithPrefilledValues;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.widgets.Confirm;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

import java.util.ArrayList;
import java.util.HashMap;

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
	private ArrayList<Identity> foundUsers = new ArrayList<Identity>();

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
	private PushButton languageButton;

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

		languageButton = new PushButton(new Image(SmallIcons.INSTANCE.locateIcon()));

		// translation not supported
		if (Utils.getNativeLanguage().isEmpty()) {
			languageButton.setEnabled(false);
			languageButton.setVisible(false);
			return;
		}

		if (!LocaleInfo.getCurrentLocale().getLocaleName().equals(Utils.getNativeLanguage().get(0))) {
			languageButton.setTitle(WidgetTranslation.INSTANCE.changeLanguageToCzech(Utils.getNativeLanguage().get(2)));
		} else {
			languageButton.setTitle(WidgetTranslation.INSTANCE.changeLanguageToEnglish());
		}
		languageButton.setStyleName("gwt-Button");
		languageButton.setPixelSize(17, 17);

		languageButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				Confirm conf = new Confirm(languageButton.getTitle(), new HTML(ApplicationMessages.INSTANCE.changeLanguageText()), new ClickHandler() {
					public void onClick(ClickEvent event) {

						String localeName = LocaleInfo.getCurrentLocale().getLocaleName();
						if (!localeName.equals(Utils.getNativeLanguage().get(0))) {
							UrlBuilder builder = Location.createUrlBuilder().setParameter("locale", Utils.getNativeLanguage().get(0));
							Window.Location.replace(builder.buildString());
						} else {
							UrlBuilder builder = Location.createUrlBuilder().setParameter("locale", "en");
							Window.Location.replace(builder.buildString());
						}
						// on OK

					}
				}, new ClickHandler() {
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

		if (submittedOrError) formContent.clear();
		bodyContents.setWidget(formContent);
		formContent.setStyleName("formContentTable");
		submittedOrError = false;

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

		/*

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

		*/

		// language button
		prepareToggleLanguageButton();

		FlexTable lang = new FlexTable();
		lang.setWidget(0, 1, languageButton);
		header.setWidget(0, 1, lang);
		header.getFlexCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);

		formContent.add(header);

		final GetFormItemsWithPrefilledValues fitems;
		if (group != null) {
			fitems = new GetFormItemsWithPrefilledValues(PerunEntity.GROUP, group.getId());
		} else {
			fitems = new GetFormItemsWithPrefilledValues(PerunEntity.VIRTUAL_ORGANIZATION, vo.getId());
		}

		fitems.setEvents(new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {

				// when form is auto-submitting
				ArrayList<ApplicationFormItemWithPrefilledValue> items = JsonUtils.<ApplicationFormItemWithPrefilledValue>jsoAsList(jso);
				for (ApplicationFormItemWithPrefilledValue item : items) {
					if (item.getFormItem().getType().equals("AUTO_SUBMIT_BUTTON")) {
						if (fitems.getSendButton() != null) {
							// enforce first click to validate and submit the form
							fitems.getSendButton().click();
						}
					}
				}

				// try to get user for VOs initial application (only for authz origin when form is defined)
				if (items != null && !items.isEmpty()) {
					if (session.getUser() == null && !session.getPerunPrincipal().getExtSource().equalsIgnoreCase("LOCAL")) {
						tryToFindUserByName(null);
					}
				}

				Scheduler.get().scheduleDeferred(new Command() {
					@Override
					public void execute() {
						positionLinker();
					}
				});

			}
		});

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

		// try to find
		JsonPostClient jspc = new JsonPostClient(new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {
				ArrayList<Identity> users = JsonUtils.<Identity>jsoAsList(jso);
				if (users != null && !users.isEmpty()) similarUsersFound(users);
			}
		});
		JSONObject query = new JSONObject();

		if (jso == null) {

			// before app submission
			jspc.sendData("registrarManager/checkForSimilarUsers", query);

		} else {

			// after app submission

			query.put("voId", new JSONNumber(vo.getId()));
			if (group != null) {
				query.put("groupId", new JSONNumber(group.getId()));
			} else {
				query.put("groupId", new JSONNumber(0));
			}
			query.put("type", new JSONString(type));

			jspc.sendData("registrarManager/checkForSimilarUsers", query);

		}


	}

	/**
	 * When similar users found, display a message
	 *
	 * @param users
	 */
	protected void similarUsersFound(ArrayList<Identity> users) {

		boolean foundNew = false;
		for (Identity u : users) {
			boolean foundOld = false;
			for (Identity user : foundUsers) {
				if (user.getId() == u.getId()) {
					// was already found
					foundOld = true;
					break;
				}
			}
			// user was not found in old ones
			if (!foundOld) {
				foundNew = true;
				break;
			}
		}

		if (!foundNew) return;

		foundUsers = users;

		FlexTable ft = new FlexTable();

		ft.setWidth("600px");
		FlexCellFormatter ftf = ft.getFlexCellFormatter();

		ft.setHTML(0, 0, ApplicationMessages.INSTANCE.similarUsersFoundIsItYou() + "<br /><br />");
		ftf.setColSpan(0, 0, 3);

		ft.setHTML(1, 0, "<strong>" + ApplicationMessages.INSTANCE.name() + "</strong>");
		ft.setHTML(1, 1, "<strong>" + ApplicationMessages.INSTANCE.email() +"</strong>");
		ft.setHTML(1, 2, "<strong>" + ApplicationMessages.INSTANCE.organization() +"</strong>");

		//ft.setHTML(1, 1, "<strong>" + ApplicationMessages.INSTANCE.identities() +"</strong>");

		int i = 2;

		for (Identity user : users) {

			ft.setHTML(i, 0, user.getName());
			ft.setHTML(i, 1, (user.getEmail() != null && !user.getEmail().isEmpty()) ? user.getEmail() : "N/A");
			ft.setHTML(i, 2, (user.getOrganization() != null && !user.getOrganization().isEmpty()) ? user.getOrganization() : "N/A");

			/*

			ft.setHTML(i, 0, "<strong>"+user.getName()+"</strong><br/>"+user.getOrganization()+"<br/><i>"+user.getEmail()+"</i>");

			//ft.setHTML(i, 1, user.getEmail());
			//ft.setHTML(i, 2, user.getOrganization());

			FlowPanel idents = new FlowPanel();
			ft.setWidget(i, 1, idents);
			idents.setStyleName("identsTable");

			boolean certFound = false;
			for (int n=0; n<user.getExternalIdentities().length(); n++) {

				ExtSource source = user.getExternalIdentities().get(n);

				if (source.getType().equals("cz.metacentrum.perun.core.impl.ExtSourceX509") && !certFound) {
					certFound = true;

					String name[] = source.getName().split("\\/");
					for (String cn : name) {
						if (cn.startsWith("CN=")) {
							idents.insert(new CustomButton(cn.substring(3), SmallIcons.INSTANCE.documentSignatureIcon(), new ClickHandler(){
								@Override
								public void onClick(ClickEvent event) {
									// TODO - get safe hash of own for non-authz
									Window.Location.replace(Utils.getIdentityConsolidatorLink("cert", true));
								}
							}), 0);
							break;
						}
					}
				} else if (source.getType().equals("cz.metacentrum.perun.core.impl.ExtSourceIdp")) {

					idents.add(new CustomButton(translateIdp(source.getName()), SmallIcons.INSTANCE.userGreenIcon(), new ClickHandler(){
						@Override
						public void onClick(ClickEvent event) {
							// TODO - get safe hash of own for non-authz
							Window.Location.replace(Utils.getIdentityConsolidatorLink("fed", true));
						}
					}));

				}

			}

			if (idents.getWidgetCount() == 0) {
				idents.add(new HTML(ApplicationMessages.INSTANCE.noIdentsFound()));
			}

			*/

			i++;

		}

		// confirm element
		final Confirm confirm = new Confirm(ApplicationMessages.INSTANCE.similarUsersFound(), ft, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Window.Location.replace(Utils.getIdentityConsolidatorLink(true));
			}
		}, true);

		confirm.setOkButtonText(ApplicationMessages.INSTANCE.joinIdentity());
		confirm.setOkIcon(SmallIcons.INSTANCE.userGreenIcon());
		confirm.setCancelButtonText(ApplicationMessages.INSTANCE.notJoinIdentity());
		//confirm.setOkIcon(SmallIcons.INSTANCE.stopIcon());
		confirm.show();

	}

	private String translateIdp(String name) {

		HashMap<String, String> orgs = new HashMap<String, String>();
		orgs.put("https://idp.upce.cz/idp/shibboleth", "University in Pardubice");
		orgs.put("https://idp.slu.cz/idp/shibboleth", "University in Opava");
		orgs.put("https://login.feld.cvut.cz/idp/shibboleth", "Faculty of Electrical Engineering, Czech Technical University In Prague");
		orgs.put("https://www.vutbr.cz/SSO/saml2/idp", "Brno University of Technology");
		orgs.put("https://shibboleth.nkp.cz/idp/shibboleth", "The National Library of the Czech Republic");
		orgs.put("https://idp2.civ.cvut.cz/idp/shibboleth", "Czech Technical University In Prague");
		orgs.put("https://shibbo.tul.cz/idp/shibboleth", "Technical University of Liberec");
		orgs.put("https://idp.mendelu.cz/idp/shibboleth", "Mendel University in Brno");
		orgs.put("https://cas.cuni.cz/idp/shibboleth", "Charles University in Prague");
		orgs.put("https://wsso.vscht.cz/idp/shibboleth", "Institute of Chemical Technology Prague");
		orgs.put("https://idp.vsb.cz/idp/shibboleth", "VSB – Technical University of Ostrava");
		orgs.put("https://whoami.cesnet.cz/idp/shibboleth", "CESNET");
		orgs.put("https://helium.jcu.cz/idp/shibboleth", "University of South Bohemia");
		orgs.put("https://idp.ujep.cz/idp/shibboleth", "Jan Evangelista Purkyne University in Usti nad Labem");
		orgs.put("https://idp.amu.cz/idp/shibboleth", "Academy of Performing Arts in Prague");
		orgs.put("https://idp.lib.cas.cz/idp/shibboleth", "Academy of Sciences Library");
		orgs.put("https://shibboleth.mzk.cz/simplesaml/metadata.xml", "Moravian  Library");
		orgs.put("https://idp2.ics.muni.cz/idp/shibboleth", "Masaryk University");
		orgs.put("https://idp.upol.cz/idp/shibboleth", "Palacky University, Olomouc");
		orgs.put("https://idp.fnplzen.cz/idp/shibboleth", "FN Plzen");
		orgs.put("https://id.vse.cz/idp/shibboleth", "University of Economics, Prague");
		orgs.put("https://shib.zcu.cz/idp/shibboleth", "University of West Bohemia");
		orgs.put("https://idptoo.osu.cz/simplesaml/saml2/idp/metadata.php", "University of Ostrava");
		orgs.put("https://login.ics.muni.cz/idp/shibboleth", "MetaCentrum");
		orgs.put("https://idp.hostel.eduid.cz/idp/shibboleth", "eduID.cz Hostel");
		orgs.put("https://shibboleth.techlib.cz/idp/shibboleth", "National Library of Technology");

		orgs.put("@google.extidp.cesnet.cz", "Google");
		orgs.put("@facebook.extidp.cesnet.cz", "Facebook");
		orgs.put("@mojeid.extidp.cesnet.cz", "mojeID");
		orgs.put("@linkedin.extidp.cesnet.cz", "LinkedIn");
		orgs.put("@twitter.extidp.cesnet.cz", "Twitter");
		orgs.put("@seznam.extidp.cesnet.cz", "Seznam");

		if (orgs.get(name) != null) {
			return orgs.get(name);
		} else {
			return name;
		}

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