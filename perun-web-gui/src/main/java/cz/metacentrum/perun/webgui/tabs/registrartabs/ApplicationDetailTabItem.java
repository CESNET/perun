package cz.metacentrum.perun.webgui.tabs.registrartabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.GetEntityById;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.membersManager.GetNewExtendMembership;
import cz.metacentrum.perun.webgui.json.membersManager.GetNewExtendMembershipLoa;
import cz.metacentrum.perun.webgui.json.registrarManager.GetApplicationDataById;
import cz.metacentrum.perun.webgui.json.registrarManager.HandleApplication;
import cz.metacentrum.perun.webgui.json.registrarManager.ResendNotification;
import cz.metacentrum.perun.webgui.model.Application;
import cz.metacentrum.perun.webgui.model.ApplicationMail;
import cz.metacentrum.perun.webgui.model.BasicOverlayType;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.tabs.TabItemWithUrl;
import cz.metacentrum.perun.webgui.tabs.UrlMapper;
import cz.metacentrum.perun.webgui.tabs.VosTabs;
import cz.metacentrum.perun.webgui.widgets.Confirm;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextArea;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.Map;

/**
 * Application detail to review user submitted data
 * before ACCEPT / REJECT by VO admin
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ApplicationDetailTabItem implements TabItem, TabItemWithUrl{

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
	private Label titleWidget = new Label("Application detail");

	// data
	private int appId;
	private TabItem tab;
	private Application app = null;
	private int row = 0;

	/**
	 * Creates a tab instance
	 *
	 * @param app
	 */
	public ApplicationDetailTabItem(Application app){
		this.app = app;
		this.appId = app.getId();
	}

	/**
	 * Creates a tab instance
	 *
	 * @param appId
	 */
	public ApplicationDetailTabItem(int appId){
		this.appId = appId;
		new GetEntityById(PerunEntity.APPLICATION, appId, new JsonCallbackEvents() {
			public void onFinished(JavaScriptObject jso) {
				app = jso.cast();
			}
		}).retrieveData();
	}

	public boolean isPrepared(){
		return (app != null);
	}

	public Widget draw() {

		tab = this;

		boolean buttonsEnabled = ((session.isVoAdmin(app.getVo().getId())) ||
				(app.getGroup() != null && session.isGroupAdmin(app.getGroup().getId())));

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%", "100%");

		TabMenu menu = new TabMenu();
		vp.add(menu);
		vp.setCellHeight(menu, "30px");

		final FlexTable ft = new FlexTable();

		String text = "<strong>Submitted by:</strong> ";
		if (app.getUser() != null) {
			text += app.getUser().getFullNameWithTitles() + " (" + app.getCreatedBy() + ")";
		} else {
			text += app.getCreatedBy();
		}
		text += " <strong>from External Source:</strong> " + app.getExtSourceName()+" <strong>with Level of Assurance:</strong> " + app.getExtSourceLoa();
		text += " <strong>on: </strong> " + app.getCreatedAt().split("\\.")[0];
		ft.setHTML(row, 0, text);
		ft.setCellSpacing(5);

		row++;

		if (app.getGroup() != null) {
			ft.setHTML(row, 0, "<strong>Application for group: </strong>"+app.getGroup().getName() + "<strong> in VO: </strong>"+app.getVo().getName());
		} else {
			ft.setHTML(row, 0, "<strong>Application for VO: </strong>"+app.getVo().getName());
		}

		if (app.getState().equalsIgnoreCase("APPROVED")) {
			row++;
			ft.setHTML(row, 0, "<strong>Approved by:</strong> " + ((app.getModifiedBy().equalsIgnoreCase("perunRegistrar")) ? "automatically" : Utils.convertCertCN(app.getModifiedBy())) + " <strong>on: </strong> " + app.getModifiedAt().split("\\.")[0]);
		}
		if (app.getState().equalsIgnoreCase("REJECTED")) {
			row++;
			ft.setHTML(row, 0, "<strong>Rejected by:</strong> " + ((app.getModifiedBy().equalsIgnoreCase("perunRegistrar")) ? "automatically" : Utils.convertCertCN(app.getModifiedBy())) + " <strong>on: </strong> " + app.getModifiedAt().split("\\.")[0]);
		}

		// for extension in VO if not approved or rejected
		if (app.getType().equalsIgnoreCase("EXTENSION") && app.getUser() != null &&
				!app.getState().equalsIgnoreCase("APPROVED") && !app.getState().equalsIgnoreCase("REJECTED")) {

			GetNewExtendMembership ex = new GetNewExtendMembership(app.getVo().getId(), app.getUser().getId(), new JsonCallbackEvents(){
				@Override
				public void onFinished(JavaScriptObject jso) {
					if (jso != null) {
						BasicOverlayType basic = jso.cast();
						row++;
						ft.setHTML(row, 0, "<strong>New membership expiration:</strong> "+basic.getString());
					}
				}
			});
			ex.retrieveData();
		}

		// for initial in VO, if not approved or rejected
		if (app.getType().equalsIgnoreCase("INITIAL") &&
				app.getGroup() == null &&
				!app.getState().equalsIgnoreCase("APPROVED") &&
				!app.getState().equalsIgnoreCase("REJECTED")) {

			GetNewExtendMembershipLoa ex = new GetNewExtendMembershipLoa(app.getVo().getId(), app.getExtSourceLoa(), new JsonCallbackEvents(){
				@Override
				public void onFinished(JavaScriptObject jso) {
					if (jso != null) {
						BasicOverlayType basic = jso.cast();
						row++;
						ft.setHTML(row, 0, "<strong>New membership expiration:</strong> "+basic.getString());
					}
				}
			});
			ex.retrieveData();
		}

		vp.add(ft);
		vp.add(new HTML("<hr size=\"1\" style=\"color: #ccc;\"/>"));

		// only NEW apps can be Verified
		if (app.getState().equals("NEW")) {
			if (session.isPerunAdmin()) {
				// verify button
				final CustomButton verify = TabMenu.getPredefinedButton(ButtonType.VERIFY, ButtonTranslation.INSTANCE.verifyApplication());
				menu.addWidget(verify);
				verify.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						HandleApplication request = new HandleApplication(JsonCallbackEvents.disableButtonEvents(verify, new JsonCallbackEvents() {
							@Override
							public void onFinished(JavaScriptObject jso) {
								app = jso.cast();
								draw();
							}
						}));
						request.verifyApplication(appId);
					}
				});
			}
		}

		// only VERIFIED apps can be approved/rejected
		if (app.getState().equals("VERIFIED") || app.getState().equals("NEW")) {

			// accept button
			final CustomButton approve = TabMenu.getPredefinedButton(ButtonType.APPROVE, ButtonTranslation.INSTANCE.approveApplication());
			approve.setEnabled(buttonsEnabled);
			menu.addWidget(approve);
			approve.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					HandleApplication request = new HandleApplication(JsonCallbackEvents.disableButtonEvents(approve, new JsonCallbackEvents(){
						@Override
						public void onFinished(JavaScriptObject jso) {
							session.getTabManager().closeTab(tab, true);
						}
					}));
					request.approveApplication(app);
				}
			});

			//reject button
			final CustomButton reject = TabMenu.getPredefinedButton(ButtonType.REJECT, ButtonTranslation.INSTANCE.rejectApplication());
			reject.setEnabled(buttonsEnabled);
			menu.addWidget(reject);
			reject.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					// confirm content
					FlexTable content = new FlexTable();
					content.setCellSpacing(10);
					content.setHTML(0, 0, "Please specify reason of rejection to let user know why was application rejected.");
					content.getFlexCellFormatter().setColSpan(0, 0, 2);
					final TextArea reason = new TextArea();
					reason.setSize("300px", "150px");
					content.setHTML(1, 0, "<strong>Reason: </strong>");
					content.setWidget(1, 1, reason);

					Confirm c = new Confirm("Specify reason", content, new ClickHandler(){
						public void onClick(ClickEvent event) {
							HandleApplication request = new HandleApplication(JsonCallbackEvents.disableButtonEvents(reject, new JsonCallbackEvents(){
								@Override
								public void onFinished(JavaScriptObject jso) {
									session.getTabManager().closeTab(tab, true);
								}
							}));
							request.rejectApplication(appId, reason.getText());
						}
					}, true);
					c.show();
				}
			});

		}

		if (app.getState().equals("NEW") || app.getState().equals("REJECTED")) {

			// delete button
			final CustomButton delete = TabMenu.getPredefinedButton(ButtonType.DELETE, ButtonTranslation.INSTANCE.deleteApplication());
			delete.setEnabled(buttonsEnabled);
			menu.addWidget(delete);
			delete.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					HandleApplication request = new HandleApplication(JsonCallbackEvents.disableButtonEvents(delete, new JsonCallbackEvents(){
						@Override
						public void onFinished(JavaScriptObject jso) {
							session.getTabManager().closeTab(tab, true);
						}
					}));
					request.deleteApplication(appId);
				}
			});

		}

		// NOTIFICATION SENDER

		final CustomButton send = new CustomButton("Re-send notifications...", SmallIcons.INSTANCE.emailIcon());
		send.setEnabled(buttonsEnabled);
		menu.addWidget(send);
		send.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				final FlexTable ft = new FlexTable();
				ft.setStyleName("inputFormFlexTable");
				ft.setWidth("400px");

				final ListBox selection = new ListBox();
				selection.setWidth("200px");

				if (session.isPerunAdmin()) {

					// add all except user invite
					for (ApplicationMail.MailType mail : ApplicationMail.MailType.values()) {
						if (!mail.equals(ApplicationMail.MailType.USER_INVITE)) {
							selection.addItem(ApplicationMail.getTranslatedMailType(mail.toString()), mail.toString());
						}
					}

				} else {

					// add TYPEs based on actual APP-STATE
					if (app.getState().equals("NEW")) {

						selection.addItem(ApplicationMail.getTranslatedMailType("APP_CREATED_USER"), "APP_CREATED_USER");
						selection.addItem(ApplicationMail.getTranslatedMailType("APP_CREATED_VO_ADMIN"), "APP_CREATED_VO_ADMIN");
						selection.addItem(ApplicationMail.getTranslatedMailType("MAIL_VALIDATION"), "MAIL_VALIDATION");

					} else if (app.getState().equals("VERIFIED")) {

						selection.addItem(ApplicationMail.getTranslatedMailType("APP_CREATED_USER"), "APP_CREATED_USER");
						selection.addItem(ApplicationMail.getTranslatedMailType("APP_CREATED_VO_ADMIN"), "APP_CREATED_VO_ADMIN");

					} else if (app.getState().equals("APPROVED")) {

						selection.addItem(ApplicationMail.getTranslatedMailType("APP_APPROVED_USER"), "APP_APPROVED_USER");

					} else if (app.getState().equals("REJECTED")) {

						selection.addItem(ApplicationMail.getTranslatedMailType("APP_REJECTED_USER"), "APP_REJECTED_USER");

					}

				}

				ft.setHTML(0, 0, "Select notification:");
				ft.getFlexCellFormatter().setStyleName(0, 0, "itemName");
				ft.setWidget(0, 1, selection);

				final TextArea reason = new TextArea();
				reason.setSize("250px", "100px");

				selection.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent event) {

						if (selection.getValue(selection.getSelectedIndex()).equals("APP_REJECTED_USER")) {
							ft.setHTML(1, 0, "Reason:");
							ft.getFlexCellFormatter().setStyleName(1, 0, "itemName");
							ft.setWidget(1, 1, reason);
						} else {
							ft.setHTML(1, 0, "");
							ft.setHTML(1, 1, "");
						}

					}
				});

				// if pre-selected
				if (selection.getValue(selection.getSelectedIndex()).equals("APP_REJECTED_USER")) {
					ft.setHTML(1, 0, "Reason:");
					ft.getFlexCellFormatter().setStyleName(1, 0, "itemName");
					ft.setWidget(1, 1, reason);
				} else {
					ft.setHTML(1, 0, "");
					ft.setHTML(1, 1, "");
				}

				final Confirm c = new Confirm("Re-send notifications", ft, null, true);
				c.setOkIcon(SmallIcons.INSTANCE.emailIcon());
				c.setOkButtonText("Send");
				c.setNonScrollable(true);
				c.setOkClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {

						ResendNotification call = new ResendNotification(appId, JsonCallbackEvents.disableButtonEvents(c.getOkButton(), new JsonCallbackEvents() {
							@Override
							public void onFinished(JavaScriptObject jso) {
								c.hide();
							}
						}));

						if (selection.getValue(selection.getSelectedIndex()).equals("APP_REJECTED_USER")) {
							call.resendNotification(selection.getValue(selection.getSelectedIndex()), reason.getValue().trim());
						} else {
							call.resendNotification(selection.getValue(selection.getSelectedIndex()), null);
						}

					}
				});
				c.setCancelClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						c.hide();
					}
				});
				c.show();

			}
		});

		menu.addWidget(new HTML("<strong>TYPE: </strong>"+Application.getTranslatedType(app.getType())));
		menu.addWidget(new HTML("<strong>STATE: </strong>"+Application.getTranslatedState(app.getState())));

		GetApplicationDataById data = new GetApplicationDataById(appId);
		data.retrieveData();
		ScrollPanel sp = new ScrollPanel(data.getContents());
		sp.setSize("100%", "100%");
		vp.add(sp);
		vp.setCellHorizontalAlignment(sp, HasHorizontalAlignment.ALIGN_CENTER);

		session.getUiElements().resizePerunTable(sp, 400, this);

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
		return SmallIcons.INSTANCE.applicationFromStorageIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1487;
		int result = 1;
		result = prime * result + appId;
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
		ApplicationDetailTabItem other = (ApplicationDetailTabItem) obj;
		if (appId != other.appId)
			return false;
		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open(){ }

	public boolean isAuthorized() {

		if (session.isVoAdmin() || session.isVoObserver() || session.isGroupAdmin()) {
			return true;
		} else {
			return false;
		}

	}

	public final static String URL = "appdetail";

	public String getUrl()
	{
		return URL;
	}

	public String getUrlWithParameters() {
		return VosTabs.URL + UrlMapper.TAB_NAME_SEPARATOR + getUrl() + "?id=" + appId;
	}

	static public ApplicationDetailTabItem load(Map<String, String> parameters) {
		int appId = Integer.parseInt(parameters.get("id"));
		return new ApplicationDetailTabItem(appId);
	}

}
