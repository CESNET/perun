package cz.metacentrum.perun.webgui.tabs.vostabs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.vosManager.UpdateVo;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

/**
 * !! USE ONLY AS INNER TAB !!!
 * Edit vo details tab
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class EditVoDetailsTabItem implements TabItem {

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
	private Label titleWidget = new Label("Edit: ");

	/**
	 * Data
	 */
	private VirtualOrganization vo;
	private JsonCallbackEvents events;

	/**
	 * Creates a tab instance
	 *
	 * @param vo
	 * @param event
	 */
	public EditVoDetailsTabItem(VirtualOrganization vo, JsonCallbackEvents event){
		this.vo = vo;
		this.events = event;
	}

	public boolean isPrepared(){
		return (vo != null);
	}

	@Override
	public boolean isRefreshParentOnClose() {
		return false;
	}

	@Override
	public void onClose() {

	}

	public Widget draw() {

		titleWidget = new Label("Edit VO name");

		VerticalPanel vp = new VerticalPanel();

		// textboxes which set the class data when updated
		final ExtendedTextBox nameTextBox = new ExtendedTextBox();
		nameTextBox.getTextBox().setText(vo.getName());
		nameTextBox.getTextBox().setMaxLength(128);

		final ExtendedTextBox.TextBoxValidator nameValidator = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (!nameTextBox.getTextBox().getText().trim().isEmpty()) {
					nameTextBox.setOk();
					return true;
				} else {
					nameTextBox.setError("Name can't be empty.");
					return false;
				}
			}
		};

		nameTextBox.setValidator(nameValidator);

		// prepares layout
		FlexTable layout = new FlexTable();
		layout.setStyleName("inputFormFlexTable");
		FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

		// close tab events
		final TabItem tab = this;

		TabMenu menu = new TabMenu();

		// send button
		final CustomButton saveButton = TabMenu.getPredefinedButton(ButtonType.SAVE, "Save changes of VO name");
		saveButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (!nameValidator.validateTextBox()) return;
				VirtualOrganization v = JsonUtils.clone(vo).cast();
				v.setName(nameTextBox.getTextBox().getText().trim());
				UpdateVo request = new UpdateVo(JsonCallbackEvents.closeTabDisableButtonEvents(saveButton, tab, events));
				request.updateVo(v);
			}
		});

		// cancel button
		final CustomButton cancelButton = TabMenu.getPredefinedButton(ButtonType.CANCEL, "");
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, isRefreshParentOnClose());
			}
		});

		// Add some standard form options
		layout.setHTML(0, 0, "Short name:");
		layout.setHTML(0, 1, SafeHtmlUtils.fromString((vo.getShortName() != null) ? vo.getShortName() : "").asString());
		layout.setHTML(1, 0, "Name:");
		layout.setWidget(1, 1, nameTextBox);

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
		final int prime = 1579;
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
		EditVoDetailsTabItem other = (EditVoDetailsTabItem) obj;
		if (vo != other.vo)
			return false;

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() { }

	public boolean isAuthorized() {

		if (session.isVoAdmin(vo.getId())) {
			return true;
		} else {
			return false;
		}

	}

}
