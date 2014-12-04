package cz.metacentrum.perun.webgui.tabs.servicestabs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.generalServiceManager.UpdateExecService;
import cz.metacentrum.perun.webgui.model.ExecService;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

/**
 * !! USE ONLY AS INNER TAB !!!
 * Edit ExecService details tab
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class EditExecServiceTabItem implements TabItem {

	private PerunWebSession session = PerunWebSession.getInstance();
	private SimplePanel contentWidget = new SimplePanel();
	private Label titleWidget = new Label("Edit: ");

	private ExecService execService;
	private JsonCallbackEvents events;

	/**
	 * Creates a tab instance
	 *
	 * @param execService
	 * @param event
	 */
	public EditExecServiceTabItem(ExecService execService, JsonCallbackEvents event){
		this.execService = execService;
		this.events = event;
	}

	public boolean isPrepared(){
		return (execService != null);
	}

	public Widget draw() {

		titleWidget = new Label("Edit ExecService");

		VerticalPanel vp = new VerticalPanel();

		final CheckBox enabled = new CheckBox();
		enabled.setText("Enabled / Disabled");
		enabled.setValue(execService.isEnabled());

		final ExtendedTextBox delay = new ExtendedTextBox();
		delay.getTextBox().setText(""+execService.getDefaultDelay());

		final ExtendedTextBox.TextBoxValidator delayValidator = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (!JsonUtils.checkParseInt(delay.getTextBox().getText().trim())) {
					delay.setError("Delay must be a number (time in minutes) !");
					return false;
				} else {
					delay.setOk();
					return true;
				}
			}
		};
		delay.setValidator(delayValidator);

		final ExtendedTextBox scriptPath = new ExtendedTextBox();
		scriptPath.getTextBox().setValue(execService.getScriptPath());

		final ExtendedTextBox.TextBoxValidator scriptValidator = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (scriptPath.getTextBox().getText().trim().isEmpty()) {
					scriptPath.setError("Script path can't be empty !");
					return false;
				} else {
					scriptPath.setOk();
					return true;
				}
			}
		};
		scriptPath.setValidator(scriptValidator);

		// prepares layout
		FlexTable layout = new FlexTable();
		layout.setStyleName("inputFormFlexTable");
		FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

		// close tab events
		final TabItem tab = this;

		TabMenu menu = new TabMenu();

		// send button
		final CustomButton saveButton = TabMenu.getPredefinedButton(ButtonType.SAVE, "Save changes of ExecService");
		saveButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (!scriptValidator.validateTextBox() && !delayValidator.validateTextBox()) return;
				ExecService es = JsonUtils.clone(execService).cast();
				es.setEnabled(enabled.getValue());
				es.setDefaultDelay(Integer.parseInt(delay.getTextBox().getValue().trim()));
				es.setScriptPath(scriptPath.getTextBox().getValue().trim());
				UpdateExecService request = new UpdateExecService(JsonCallbackEvents.closeTabDisableButtonEvents(saveButton, tab, events));
				request.updateExecService(es);
			}
		});

		// cancel button
		final CustomButton cancelButton = TabMenu.getPredefinedButton(ButtonType.CANCEL, "");
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, false);
			}
		});

		// Add some standard form options
		layout.setHTML(0, 0, "Enabled:");
		layout.setWidget(0, 1, enabled);
		layout.setHTML(1, 0, "Script path:");
		layout.setWidget(1, 1, scriptPath);
		layout.setHTML(2, 0, "Delay:");
		layout.setWidget(2, 1, delay);

		for (int i=0; i<layout.getRowCount(); i++) {
			cellFormatter.addStyleName(i, 0, "itemName");
		}

		menu.addWidget(saveButton);
		menu.addWidget(cancelButton);

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
		return SmallIcons.INSTANCE.applicationFormEditIcon();
	}

	@Override
	public int hashCode() {
		final int prime = 1553;
		int result = 1;
		result = prime * result + 6786786;
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
		EditExecServiceTabItem other = (EditExecServiceTabItem) obj;
		if (execService.getId() != execService.getId())
			return false;

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() { }

	public boolean isAuthorized() {

		if (session.isPerunAdmin()) {
			return true;
		} else {
			return false;
		}

	}

}