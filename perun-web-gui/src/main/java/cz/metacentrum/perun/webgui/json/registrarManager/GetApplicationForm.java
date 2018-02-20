package cz.metacentrum.perun.webgui.json.registrarManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.model.ApplicationForm;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.widgets.Confirm;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

/**
 * Retrieves form for VO / GROUP to determine if it's auto approve or not.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetApplicationForm implements JsonCallback {

	// Session
	private PerunWebSession session = PerunWebSession.getInstance();

	// VO / GROUP id
	private int id;
	private PerunEntity entity;
	private boolean hidden = false;

	// JSON URL
	static private final String JSON_URL = "registrarManager/getApplicationForm";

	// External events
	private JsonCallbackEvents events = new JsonCallbackEvents();

	private FlexTable content = new FlexTable();

	/**
	 * Creates a new method instance
	 *
	 * @param entity entity
	 * @param id VO/GROUP ID
	 */
	public GetApplicationForm(PerunEntity entity, int id) {
		this.entity = entity;
		this.id = id;
	}

	/**
	 * Creates a new getResources method instance
	 *
	 * @param entity entity
	 * @param id VO/GROUP ID
	 * @param events Custom events
	 */
	public GetApplicationForm(PerunEntity entity, int id, JsonCallbackEvents events) {
		this(entity, id);
		this.events = events;
	}

	public void onFinished(JavaScriptObject jso) {

		ApplicationForm form = jso.cast();
		createWidget(form);
		session.getUiElements().setLogText("Loading application form settings for "+entity+" finished.");
		events.onFinished(form);

	}

	public void onError(PerunError error) {
		session.getUiElements().setLogErrorText("Error while loading application form settings for "+entity+".");
		createWidget(null);
		events.onError(error);
	}

	public void onLoadingStart() {
		session.getUiElements().setLogText("Loading application form settings started.");
		events.onLoadingStart();
	}

	public void retrieveData() {

		String param = "";

		if (PerunEntity.VIRTUAL_ORGANIZATION.equals(entity)) {
			param = "vo=" + this.id;
		} else if (PerunEntity.GROUP.equals(entity)) {
			param = "group=" + this.id;
		}

		JsonClient js = new JsonClient();
		js.setHidden(hidden);
		js.retrieveData(JSON_URL, param, this);

	}

	/**
	 * Create approval style change widget
	 *
	 * @param form
	 */
	private void createWidget(final ApplicationForm form) {

		final CustomButton button = TabMenu.getPredefinedButton(ButtonType.SETTINGS, ButtonTranslation.INSTANCE.changeAppFormSettings());

		if (form != null) {

			// create click handler
			ClickHandler ch = new ClickHandler(){
				public void onClick(ClickEvent event) {

					// layout
					FlexTable ft = new FlexTable();
					ft.setCellSpacing(10);

					ft.setHTML(0, 0, "<strong>INITIAL: </strong>");
					ft.setHTML(1, 0, "<strong>EXTENSION: </strong>");
					ft.setHTML(2, 0, "<strong>Module name: </strong>");

					// widgets
					final ListBox lbInit = new ListBox();
					final ListBox lbExt = new ListBox();
					final TextBox className = new TextBox();

					lbInit.addItem("Automatic", "true");
					lbInit.addItem("Manual", "false");
					lbExt.addItem("Automatic", "true");
					lbExt.addItem("Manual", "false");

					if (form.getAutomaticApproval()==true) {
						lbInit.setSelectedIndex(0);
					} else {
						lbInit.setSelectedIndex(1);
					}
					if (form.getAutomaticApprovalExtension()==true) {
						lbExt.setSelectedIndex(0);
					} else {
						lbExt.setSelectedIndex(1);
					}
					className.setText(form.getModuleClassName());

					ft.setWidget(0, 1, lbInit);
					ft.setWidget(1, 1, lbExt);
					ft.setWidget(2, 1, className);

					// click on save
					ClickHandler click = new ClickHandler(){
						public void onClick(ClickEvent event) {
							// switch and send request
							UpdateForm request = new UpdateForm(new JsonCallbackEvents(){
								public void onFinished(JavaScriptObject jso) {
									// recreate same widget
									content.clear();
									ApplicationForm newForm = jso.cast();
									createWidget(newForm);
								};
							});
							form.setAutomaticApproval(Boolean.parseBoolean(lbInit.getValue(lbInit.getSelectedIndex())));
							form.setAutomaticApprovalExtension(Boolean.parseBoolean(lbExt.getValue(lbExt.getSelectedIndex())));
							form.setModuleClassName(className.getText().trim());
							request.updateForm(form);
						}
					};

					Confirm c = new Confirm("Change application form settings", ft, click, true);
					c.show();

				}
			};
			button.addClickHandler(ch);

			String appStyle = "<strong>Approval style: </strong>";
			String module = "</br><strong>Module name: </strong>" + SafeHtmlUtils.fromString(form.getModuleClassName()).asString();

			if (form.getAutomaticApproval()==true) {
				appStyle = appStyle + "<span style=\"color:red;\">Automatic</span> (INITIAL)";
			} else {
				appStyle = appStyle + "<span style=\"color:red;\">Manual</span> (INITIAL)";
			}
			if (form.getAutomaticApprovalExtension()==true) {
				appStyle = appStyle + " <span style=\"color:red;\">Automatic</span> (EXTENSION)";
			} else {
				appStyle = appStyle + " <span style=\"color:red;\">Manual</span> (EXTENSION)";
			}

			if (form.getGroup() == null && !session.isVoAdmin(form.getVo().getId())) button.setEnabled(false);
			if (form.getGroup() != null && (!session.isGroupAdmin(form.getGroup().getId()) && !session.isVoAdmin(form.getVo().getId()))) button.setEnabled(false);

			content.setHTML(0, 0, appStyle + module);
			content.setWidget(0, 1, button);
			content.getFlexCellFormatter().getElement(0, 0).setAttribute("style", "padding-right: 10px");

		} else {

			button.setEnabled(false);
			String appStyle = "<strong>Approval style: </strong> Form doesn't exists.";
			String module = "</br><strong>Module name: </strong> Form doesn't exists.";

			content.setHTML(0, 0, appStyle + module);
			content.setWidget(0, 1, button);
			content.getFlexCellFormatter().getElement(0, 0).setAttribute("style", "padding-right: 10px");

		}

	}

	public Widget getApprovalWidget() {

		return this.content;

	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

}
