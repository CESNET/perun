package cz.metacentrum.perun.webgui.tabs.vostabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.vosManager.CreateVo;
import cz.metacentrum.perun.webgui.model.VirtualOrganization;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

/**
 * Create VO tab
 * !! USE AS INNER TAB ONLY !!
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class CreateVoTabItem implements TabItem {

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
	private Label titleWidget = new Label("Create virtual organization");

	private ButtonTranslation buttonTranslation = ButtonTranslation.INSTANCE;

	/**
	 * Creates a tab instance
	 *
	 */
	public CreateVoTabItem(){ }

	public boolean isPrepared(){
		return true;
	}

	public Widget draw() {

		VerticalPanel vp = new VerticalPanel();

		// textboxes which set the class data when updated
		final ExtendedTextBox nameTextBox = new ExtendedTextBox();
		final ExtendedTextBox shortNameTextBox = new ExtendedTextBox();

		nameTextBox.getTextBox().setMaxLength(128);
		shortNameTextBox.getTextBox().setMaxLength(32);

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
		final ExtendedTextBox.TextBoxValidator shortNameValidator = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (shortNameTextBox.getTextBox().getText().trim().isEmpty()) {
					shortNameTextBox.setError("Short name can't be empty.");
				} else if (!shortNameTextBox.getTextBox().getText().trim().matches(Utils.VO_SHORT_NAME_MATCHER)) {
					shortNameTextBox.setError("Short name can contain only letters, numbers, dash and underscore.");
				} else {
					shortNameTextBox.setOk();
					return true;
				}
				return false;
			}
		};

		nameTextBox.setValidator(nameValidator);
		shortNameTextBox.setValidator(shortNameValidator);

		// prepares layout
		FlexTable layout = new FlexTable();
		layout.setStyleName("inputFormFlexTable");
		FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

		// close tab events
		final TabItem tab = this;

		TabMenu menu = new TabMenu();

		// send button
		final CustomButton createButton = TabMenu.getPredefinedButton(ButtonType.CREATE, buttonTranslation.createVo());
		createButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {

				if (!nameValidator.validateTextBox()) return;
				if (!shortNameValidator.validateTextBox()) return;

				CreateVo request = new CreateVo(JsonCallbackEvents.closeTabDisableButtonEvents(createButton, tab, new JsonCallbackEvents() {
					@Override
					public void onFinished(JavaScriptObject jso) {
						// new VO must be editable by user in GUI, because it is already in PERUN
						VirtualOrganization vo = jso.cast();
						session.addEditableVo(vo.getId());
					}
				}));
				request.createVo(nameTextBox.getTextBox().getText().trim(), shortNameTextBox.getTextBox().getText().trim());
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
		layout.setHTML(0, 0, "Full name:");
		layout.setWidget(0, 1, nameTextBox);
		layout.setHTML(1, 0, "Short name:");
		layout.setWidget(1, 1, shortNameTextBox);

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
		final int prime = 1303;
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

	public void open()
	{
	}

	public boolean isAuthorized() {

		if (session.isVoAdmin()) {
			return true;
		} else {
			return false;
		}

	}

}
