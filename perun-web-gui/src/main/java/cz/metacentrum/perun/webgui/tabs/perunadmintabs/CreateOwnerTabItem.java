package cz.metacentrum.perun.webgui.tabs.perunadmintabs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.localization.ObjectTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.ownersManager.CreateOwner;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

/**
 * Provides page wit create owner page
 * !! USE AS INNER TAB ONLY !!
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class CreateOwnerTabItem implements TabItem {

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
	private Label titleWidget = new Label("Create owner");

	/**
	 * Creates a tab instance
	 *
	 */
	public CreateOwnerTabItem(){ }

	public boolean isPrepared(){
		return true;
	}

	public Widget draw() {

		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%","100%");

		// textboxes which set the class data when updated
		final ExtendedTextBox ownerNameTextBox = new ExtendedTextBox();
		final ExtendedTextBox ownerContactTextBox = new ExtendedTextBox();

		final ListBox ownerType = new ListBox();
		ownerType.addItem(ObjectTranslation.INSTANCE.ownerTypeAdministrative(),"administrative");
		ownerType.addItem(ObjectTranslation.INSTANCE.ownerTypeTechnical(),"technical");

		final ExtendedTextBox.TextBoxValidator nameValidator = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (ownerNameTextBox.getTextBox().getText().trim().isEmpty()) {
					ownerNameTextBox.setError("Name can't be empty.");
					return false;
				}
				ownerNameTextBox.setOk();
				return true;
			}
		};

		final ExtendedTextBox.TextBoxValidator contactValidator = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (ownerContactTextBox.getTextBox().getText().trim().isEmpty()) {
					ownerContactTextBox.setError("Contact can't be empty.");
					return false;
				}
				ownerContactTextBox.setOk();
				return true;
			}
		};

		ownerNameTextBox.setValidator(nameValidator);
		ownerContactTextBox.setValidator(contactValidator);

		// layout
		FlexTable layout = new FlexTable();
		layout.setStyleName("inputFormFlexTable");
		FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

		// Add some standard form options
		layout.setHTML(0, 0, "Name:");
		layout.setWidget(0, 1, ownerNameTextBox);
		layout.setHTML(1, 0, "Contact:");
		layout.setWidget(1, 1, ownerContactTextBox);
		layout.setHTML(2, 0, "Type:");
		layout.setWidget(2, 1, ownerType);

		for (int i=0; i<layout.getRowCount(); i++) {
			cellFormatter.addStyleName(i, 0, "itemName");
		}

		// buttons
		TabMenu menu = new TabMenu();
		final TabItem tab = this;

		final CustomButton createButton = TabMenu.getPredefinedButton(ButtonType.CREATE, ButtonTranslation.INSTANCE.createOwner());

		createButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (nameValidator.validateTextBox() && contactValidator.validateTextBox()) {
					CreateOwner request = new CreateOwner(JsonCallbackEvents.closeTabDisableButtonEvents(createButton, tab));
					request.createOwner(ownerNameTextBox.getTextBox().getText().trim(), ownerContactTextBox.getTextBox().getText().trim(), ownerType.getValue(ownerType.getSelectedIndex()));
				}
			}
		});

		menu.addWidget(createButton);
		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				session.getTabManager().closeTab(tab, false);
			}
		}));

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
		final int prime = 919;
		int result = 1;
		result = prime * result + 678186;
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

		if (session.isPerunAdmin()) {
			return true;
		} else {
			return false;
		}

	}

}
