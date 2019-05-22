package cz.metacentrum.perun.webgui.tabs.userstabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.resources.*;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetListOfAttributes;
import cz.metacentrum.perun.webgui.json.attributesManager.SetAttributes;
import cz.metacentrum.perun.webgui.json.usersManager.GetPendingPreferredEmailChanges;
import cz.metacentrum.perun.webgui.json.usersManager.RequestPreferredEmailChange;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.cabinettabs.AddPublicationsTabItem;
import cz.metacentrum.perun.webgui.widgets.*;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Tab with user's personal settings (personal info, contacts)
 *
 * @author Pavel Zlamal <256627&mail.muni.cz>
 */
public class SelfPersonalTabItem implements TabItem {

	PerunWebSession session = PerunWebSession.getInstance();

	private SimplePanel contentWidget = new SimplePanel();
	private Label titleWidget = new Label("Loading user");

	private User user;
	private int userId;
	private ArrayList<Attribute> userAttrs = new ArrayList<Attribute>();

	String resultText = "";
	ArrayList<String> emails = new ArrayList<String>();
	private TabPanelForTabItems tabPanel;

	/**
	 * Creates a tab instance
	 */
	public SelfPersonalTabItem(){
		this.user = session.getActiveUser();
		this.userId = user.getId();
	}

	/**
	 * Creates a tab instance with custom user
	 * @param user
	 */
	public SelfPersonalTabItem(User user){
		this.user = user;
		this.userId = user.getId();
	}

	/**
	 * Creates a tab instance with custom user
	 * @param userId
	 */
	public SelfPersonalTabItem(int userId) {
		this.userId = userId;
		new GetEntityById(PerunEntity.USER, userId, new JsonCallbackEvents(){
			public void onFinished(JavaScriptObject jso) {
				user = jso.cast();
			}
		}).retrieveData();
	}

	public boolean isPrepared(){
		return !(user == null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		// content
		final ScrollPanel sp = new ScrollPanel();
		sp.setSize("100%", "100%");
		sp.setStyleName("perun-tableScrollPanel");
		session.getUiElements().resizeSmallTabPanel(sp, 350, this);

		HorizontalPanel horizontalSplitter = new HorizontalPanel();
		horizontalSplitter.setStyleName("perun-table");
		horizontalSplitter.setSize("100%", "100%");
		final VerticalPanel leftPanel = new VerticalPanel();
		final VerticalPanel rightPanel = new VerticalPanel();

		horizontalSplitter.add(leftPanel);
		horizontalSplitter.add(rightPanel);
		horizontalSplitter.setCellWidth(leftPanel, "50%");
		horizontalSplitter.setCellWidth(rightPanel, "50%");

		final VerticalPanel innerVp = new VerticalPanel();
		innerVp.setSize("100%", "100%");

		final TabMenu menu = new TabMenu();
		innerVp.add(menu);
		innerVp.setCellHeight(menu, "30px");

		menu.addWidget(UiElements.getRefreshButton(this));
		innerVp.add(horizontalSplitter);

		sp.setWidget(innerVp);

		FlexTable quickHeader = new FlexTable();
		quickHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.directionIcon()));
		quickHeader.setHTML(0, 1, "<p class=\"subsection-heading\">Quick links</p>");

		FlexTable prefHeader = new FlexTable();
		prefHeader.setWidget(0, 0, new Image(LargeIcons.INSTANCE.settingToolsIcon()));
		prefHeader.setHTML(0, 1, "<p class=\"subsection-heading\">Global settings</p>");

		leftPanel.add(quickHeader);
		rightPanel.add(prefHeader);

		// widgets
		final ExtendedTextBox preferredEmail = new ExtendedTextBox();
		preferredEmail.getTextBox().setWidth("300px");
		preferredEmail.setWidth("300px");

		final ListBox preferredLanguage = new ListBox();

		preferredLanguage.addItem("Not selected", "");
		if (!Utils.getNativeLanguage().isEmpty()) {
			preferredLanguage.addItem(Utils.getNativeLanguage().get(2), Utils.getNativeLanguage().get(0));
		}
		preferredLanguage.addItem("English", "en");

		final ListBox timezone = new ListBox();
		timezone.addItem("Not set", "null");
		for (String zone : Utils.getTimezones()){
			timezone.addItem(zone, zone);
		}

		final PreferredShellsWidget preferredShellsWidget = new PreferredShellsWidget();
		final PreferredUnixGroupNameWidget preferredUnixGroupNameWidget = new PreferredUnixGroupNameWidget();

		// content
		final FlexTable settingsTable = new FlexTable();
		settingsTable.setStyleName("inputFormFlexTableDark");

		settingsTable.setHTML(1, 0, "Preferred&nbsp;mail:");
		settingsTable.setWidget(1, 1, preferredEmail);
		settingsTable.getFlexCellFormatter().setRowSpan(1, 0, 2);
		settingsTable.setHTML(2, 0, "");

		settingsTable.setHTML(3, 0, "Preferred&nbsp;language:");
		settingsTable.setWidget(3, 1, preferredLanguage);
		settingsTable.setHTML(4, 0, "Timezone:");
		settingsTable.setWidget(4, 1, timezone);
		settingsTable.setHTML(5, 0, "Preferred shells:");
		settingsTable.setWidget(5, 1, preferredShellsWidget);
		settingsTable.getFlexCellFormatter().setRowSpan(5, 0, 2);
		settingsTable.setHTML(6, 0, "List of preferred shells ordered from the most preferred to least is used to determine your shell on provided resources. If none of preferred shells is available on resource (or no preferred shell is set), resource's default is used.");
		settingsTable.getFlexCellFormatter().setStyleName(6, 0, "inputFormInlineComment");

		settingsTable.setHTML(7, 0, "Preferred primary unix groups names:");

		for (int i=1; i<settingsTable.getRowCount(); i++) {
			if (i == 2 || i == 6) continue;
			settingsTable.getFlexCellFormatter().addStyleName(i, 0, "itemName");
		}

		// SET SAVE CLICK HANDLER

		final CustomButton save = TabMenu.getPredefinedButton(ButtonType.SAVE, "Save changes in preferences");
		//TabMenu menu = new TabMenu();
		//menu.addWidget(save);

		settingsTable.setWidget(0, 0, save);

		final GetListOfAttributes attrsCall = new GetListOfAttributes();
		// list of wanted attributes
		final ArrayList<String> list = new ArrayList<String>();
		list.add("urn:perun:user:attribute-def:def:preferredLanguage");
		list.add("urn:perun:user:attribute-def:def:preferredMail");
		list.add("urn:perun:user:attribute-def:def:timezone");
		list.add("urn:perun:user:attribute-def:def:preferredShells");

		for (String s : Utils.getNamespacesForPreferredGroupNames()) {
			list.add("urn:perun:user:attribute-def:def:preferredUnixGroupName-namespace:"+s);
		}


		final Map<String,Integer> ids = new HashMap<String,Integer>();
		ids.put("user", userId);

		save.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {

				ArrayList<Attribute> toSend = new ArrayList<Attribute>(); // will be set

				for (final Attribute a : userAttrs) {

					String oldValue = a.getValue();
					String newValue = "";

					if (a.getFriendlyName().equalsIgnoreCase("preferredLanguage")) {
						newValue = preferredLanguage.getValue(preferredLanguage.getSelectedIndex());
					} else if (a.getFriendlyName().equalsIgnoreCase("timezone")) {
						newValue = timezone.getValue(timezone.getSelectedIndex());
					} else if (a.getFriendlyName().equalsIgnoreCase("preferredMail")) {
						newValue = preferredEmail.getTextBox().getValue().trim();
					} else if (a.getFriendlyName().equalsIgnoreCase("preferredShells")) {
						String s = preferredShellsWidget.getAttribute().getValue();
						newValue = (!s.equalsIgnoreCase("null")) ? s : "";
					} else if (a.getBaseFriendlyName().equals("preferredUnixGroupName-namespace")) {
						String s = preferredUnixGroupNameWidget.getAttribute(a.getName()).getValue();
						newValue = (!s.equalsIgnoreCase("null")) ? s : "";
					} else {
						continue; // other than contact attributes must be skipped
					}

					if (oldValue.equals(newValue) || (oldValue.equalsIgnoreCase("null") && ("").equals(newValue))) {
						// if both values are the same or both are "empty"
						continue; // skip this cycle
					} else {
						if (("").equals(newValue) || ("null").equals(newValue)) {
							Attribute newA = JsonUtils.clone(a).cast();
							newA.setValueAsJso(null); // set value
							toSend.add(newA); // value was cleared
							// preferred email can't be ever removed from here
						} else {
							if (a.getFriendlyName().equalsIgnoreCase("preferredMail")) {
								RequestPreferredEmailChange call = new RequestPreferredEmailChange(JsonCallbackEvents.disableButtonEvents(save));
								call.requestChange(user, newValue);
							} else {
								Attribute newA = JsonUtils.clone(a).cast();
								newA.setValue(newValue); // set value
								toSend.add(newA); // value was changed / added
							}
						}
					}
				}

				// ids
				Map<String, Integer> localIds = new HashMap<String, Integer>();
				localIds.put("user", userId);

				// requests
				SetAttributes request = new SetAttributes(JsonCallbackEvents.disableButtonEvents(save, new JsonCallbackEvents(){
					@Override
					public void onFinished(JavaScriptObject jso) {
						attrsCall.getListOfAttributes(ids, list);
					}
				}));
				// send if not empty
				if (!toSend.isEmpty()) {
					request.setAttributes(localIds, toSend);
				}
			}
		});

		// GET USER ATTRIBUTES BY NAME
		attrsCall.setEvents(new JsonCallbackEvents(){
			@Override
			public void onFinished(JavaScriptObject jso) {

				userAttrs = JsonUtils.jsoAsList(jso);

				settingsTable.setWidget(1, 1, preferredEmail);
				settingsTable.setWidget(3, 1, preferredLanguage);
				settingsTable.setWidget(4, 1, timezone);
				settingsTable.setWidget(5, 1, preferredShellsWidget);
				settingsTable.setWidget(7, 1, preferredUnixGroupNameWidget);

				// clear on re-init
				preferredUnixGroupNameWidget.clear();

				for (final Attribute a : userAttrs) {

					if (a.getValue() == null || a.getValue().equalsIgnoreCase("null")) {
						if (a.getFriendlyName().equalsIgnoreCase("preferredShells")) {
							// don't skip this null attribute
							preferredShellsWidget.setAttribute(a);
						}
						if (a.getBaseFriendlyName().equalsIgnoreCase("preferredUnixGroupName-namespace")) {
							// don't skip this null attribute
							preferredUnixGroupNameWidget.setAttribute((Attribute)JsonUtils.clone(a).cast());
						}
						// skip null attributes
						continue;
					}

					if (a.getBaseFriendlyName().equalsIgnoreCase("preferredUnixGroupName-namespace")) {
						// don't skip this null attribute
						preferredUnixGroupNameWidget.setAttribute((Attribute)JsonUtils.clone(a).cast());
					} else if (a.getFriendlyName().equalsIgnoreCase("preferredLanguage")) {
						if (!Utils.getNativeLanguage().isEmpty() && a.getValue().equals(Utils.getNativeLanguage().get(0))) {
							preferredLanguage.setSelectedIndex(1);
						} else if (a.getValue().equals("en")) {
							preferredLanguage.setSelectedIndex(2);
						}
					} else if (a.getFriendlyName().equalsIgnoreCase("preferredMail")) {

						preferredEmail.getTextBox().setText(a.getValue());

						// display notice if there is any valid pending change request
						GetPendingPreferredEmailChanges get = new GetPendingPreferredEmailChanges(user.getId(), new JsonCallbackEvents(){
							@Override
							public void onFinished(JavaScriptObject jso) {
								//save.setEnabled(true);
								// process returned value
								if (jso != null) {
									BasicOverlayType basic = jso.cast();
									emails = basic.getListOfStrings();
									if (!emails.isEmpty()) {

										for (String s : emails) {
											if (!s.equals(preferredEmail.getTextBox().getText().trim())) {
												resultText += s+", ";
											}
										}
										if (resultText.length() >= 2) resultText = resultText.substring(0, resultText.length()-2);

										settingsTable.setHTML(2, 0, "You have pending change request. Please check inbox of: "+ SafeHtmlUtils.fromString(resultText).asString()+" for validation email.");
										settingsTable.getFlexCellFormatter().setStyleName(2, 0, "inputFormInlineComment serverResponseLabelError");
									}

								}

								// set validator with respect to returned values
								preferredEmail.setValidator(new ExtendedTextBox.TextBoxValidator() {
									@Override
									public boolean validateTextBox() {
										if (preferredEmail.getTextBox().getText().trim().isEmpty()) {
											preferredEmail.setError("Preferred email address can't be empty.");
											return false;
										} else if (!JsonUtils.isValidEmail(preferredEmail.getTextBox().getText().trim())) {
											preferredEmail.setError("Not valid email address format.");
											return false;
										}
										// update notice under textbox on any cut/past/type action
										if (!preferredEmail.getTextBox().getText().trim().equals(a.getValue())) {
											settingsTable.setHTML(2, 0, "No changes are saved, until new address is validated. After change please check your inbox for validation mail." +
													((!resultText.isEmpty()) ? "<p><span class=\"serverResponseLabelError\">You have pending change request. Please check inbox of: "+resultText+" for validation email.</span>" : ""));
											settingsTable.getFlexCellFormatter().setStyleName(2, 0, "inputFormInlineComment");
										} else {
											settingsTable.setHTML(2, 0, (!resultText.isEmpty()) ? "You have pending change request. Please check inbox of: "+SafeHtmlUtils.fromString(resultText).asString()+" for validation email." : "");
											settingsTable.getFlexCellFormatter().setStyleName(2, 0, "inputFormInlineComment serverResponseLabelError");
										}
										preferredEmail.setOk();
										return true;
									}
								});

							}
							@Override
							public void onError(PerunError error) {
								//save.setEnabled(true);
								// add basic validator even if there is any error
								preferredEmail.setValidator(new ExtendedTextBox.TextBoxValidator() {
									@Override
									public boolean validateTextBox() {
										if (preferredEmail.getTextBox().getText().trim().isEmpty()) {
											preferredEmail.setError("Preferred email address can't be empty.");
											return false;
										} else if (!JsonUtils.isValidEmail(preferredEmail.getTextBox().getText().trim())) {
											preferredEmail.setError("Not valid email address format.");
											return false;
										} else {
											preferredEmail.setOk();
											return true;
										}
									}
								});
							}
							@Override
							public void onLoadingStart() {
								//save.setEnabled(false);
							}
						});
						get.retrieveData();

					} else if (a.getFriendlyName().equalsIgnoreCase("timezone")) {
						for (int i=0; i<timezone.getItemCount(); i++) {
							if (timezone.getValue(i).equals(a.getValue())) {
								timezone.setSelectedIndex(i);
							}
						}
					} else if (a.getFriendlyName().equalsIgnoreCase("preferredShells")) {
						// set attribute and display value
						preferredShellsWidget.setAttribute(a);
					}
				}
			}
			@Override
			public void onLoadingStart() {
				settingsTable.setWidget(1, 1, new AjaxLoaderImage(true));
				settingsTable.setWidget(3, 1, new AjaxLoaderImage(true));
				settingsTable.setWidget(4, 1, new AjaxLoaderImage(true));
				settingsTable.setWidget(5, 1, new AjaxLoaderImage(true));
				settingsTable.setWidget(7, 1, new AjaxLoaderImage(true));
			}
			@Override
			public void onError(PerunError error) {
				settingsTable.setWidget(1, 1, new AjaxLoaderImage(true).loadingError(error));
				settingsTable.setWidget(3, 1, new AjaxLoaderImage(true).loadingError(error));
				settingsTable.setWidget(4, 1, new AjaxLoaderImage(true).loadingError(error));
				settingsTable.setWidget(5, 1, new AjaxLoaderImage(true).loadingError(error));
				settingsTable.setWidget(7, 1, new AjaxLoaderImage(true).loadingError(error));
			}
		});

		attrsCall.getListOfAttributes(ids, list);


		FlexTable quickLinks = new FlexTable();
		quickHeader.setStyleName("inputFormFlexTable");

		String span = "<span style=\"font-weight: bold; padding-left: 25px; line-height: 2;\">";

		Anchor name = new Anchor(span+"Edit name titles</span>", true);
		name.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				session.getTabManager().addTabToCurrentTab(new EditUserDetailsTabItem(user, new JsonCallbackEvents(){
					@Override
					public void onFinished(JavaScriptObject jso) {
						// refresh parent tab
						SelfDetailTabItem item = (SelfDetailTabItem)session.getTabManager().getActiveTab();
						item.setUser((User)jso);
						item.open();
						item.draw();
					}
				}));
			}
		});
		quickLinks.setWidget(0, 0, name);

		Anchor password = new Anchor(span+"Change / reset password</span>", true);
		password.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				tabPanel.selectTab(tabPanel.getSelectedIndex()+3);
			}
		});
		quickLinks.setWidget(1, 0, password);

		Anchor cert = new Anchor(span+"<a href=\""+ Utils.getIdentityConsolidatorLink(false)+"\" target=\"_blank\">Add certificate</a></span>", true);
		quickLinks.setWidget(2, 0, cert);

		Anchor ssh = new Anchor(span+"Manage SSH keys</span>", true);
		ssh.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				tabPanel.selectTab(tabPanel.getSelectedIndex()+3);
			}
		});
		quickLinks.setWidget(3, 0, ssh);

		Anchor report = new Anchor(span+"Report new publication</span>", true);
		report.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				session.getTabManager().addTab(new AddPublicationsTabItem(user));
			}
		});
		quickLinks.setWidget(4, 0, report);

		Anchor request = new Anchor(span+"Request data/files quota change</span>", true);
		request.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				tabPanel.selectTab(tabPanel.getSelectedIndex() + 2);
			}
		});
		quickLinks.setWidget(5, 0, request);

		if (session.getEditableUsers().size() > 1) {
			Anchor serv = new Anchor(span+"Manage service identities</span>", true);
			serv.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					tabPanel.selectTab(tabPanel.getSelectedIndex()+6);
				}
			});
			quickLinks.setWidget(6, 0, serv);
		}

		rightPanel.add(settingsTable);
		leftPanel.add(quickLinks);

		Scheduler.get().scheduleDeferred(new Command() {
			@Override
			public void execute() {
				sp.scrollToTop();
			}
		});

		this.contentWidget.setWidget(sp);
		return getWidget();

	}

	public Widget getWidget() {
		return this.contentWidget;
	}

	public Widget getTitle() {
		return this.titleWidget;
	}

	public ImageResource getIcon() {
		return SmallIcons.INSTANCE.userGrayIcon();
	}

	public void setParentPanel(TabPanelForTabItems panel) {
		this.tabPanel = panel;
	}

	@Override
	public int hashCode() {
		final int prime = 1567;
		int result = 432;
		result = prime * result * userId;
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
		if (this.userId != ((SelfPersonalTabItem)obj).userId)
			return false;

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {}

	public boolean isAuthorized() {

		if (session.isSelf(userId)) {
			return true;
		} else {
			return false;
		}

	}

}
