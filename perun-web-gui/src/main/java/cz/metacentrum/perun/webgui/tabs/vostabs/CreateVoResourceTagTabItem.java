package cz.metacentrum.perun.webgui.tabs.vostabs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.resourcesManager.CreateResourceTag;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

/**
 * Create VO Resource tag tab
 * !! USE AS INNER TAB ONLY !!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CreateVoResourceTagTabItem implements TabItem {

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
	private Label titleWidget = new Label("Create resource tag");

	private ButtonTranslation buttonTranslation = ButtonTranslation.INSTANCE;

	private int voId = 0;

	/**
	 * Creates a tab instance
	 *
	 * @param voId ID of VO to create resource tag for
	 */
	public CreateVoResourceTagTabItem(int voId){
		this.voId = voId;
	}

	public boolean isPrepared(){
		return true;
	}

	public Widget draw() {

		VerticalPanel vp = new VerticalPanel();

		// textboxes which set the class data when updated
		final ExtendedTextBox nameTextBox = new ExtendedTextBox();

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
		final CustomButton createButton = TabMenu.getPredefinedButton(ButtonType.CREATE, buttonTranslation.createResourceTag());
		createButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (!nameValidator.validateTextBox()) return;
				CreateResourceTag request = new CreateResourceTag(JsonCallbackEvents.closeTabDisableButtonEvents(createButton, tab));
				request.createResourceTag(nameTextBox.getTextBox().getText().trim(), voId);
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
		layout.setHTML(0, 0, "Tag name:");
		layout.setWidget(0, 1, nameTextBox);

		for (int i=0; i<layout.getRowCount(); i++) {
			cellFormatter.addStyleName(i, 0, "itemName");
		}

		menu.addWidget(createButton);
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
		return SmallIcons.INSTANCE.addIcon();
	}


	@Override
	public int hashCode() {
		final int prime = 1301;
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

		return true;
	}

	public boolean multipleInstancesEnabled() {
		return false;
	}

	public void open() {
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin(voId)) {
			return true;
		} else {
			return false;
		}

	}

}