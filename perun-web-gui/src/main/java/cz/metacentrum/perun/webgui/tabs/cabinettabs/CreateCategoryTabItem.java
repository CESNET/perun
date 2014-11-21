package cz.metacentrum.perun.webgui.tabs.cabinettabs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.mainmenu.MainMenu;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.cabinetManager.CreateCategory;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

/**
 * Tab for adding new Category
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class CreateCategoryTabItem implements TabItem {

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
	private Label titleWidget = new Label("Add category");

	/**
	 * Creates a tab instance
	 */
	public CreateCategoryTabItem(){}

	public boolean isPrepared(){
		return true;
	}

	public Widget draw() {

		final VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%","100%");

		TabMenu menu = new TabMenu();

		final CustomButton addCategory = TabMenu.getPredefinedButton(ButtonType.ADD, "Add category");
		menu.addWidget(addCategory);

		FlexTable table = new FlexTable();
		table.setStyleName("inputFormFlexTable");

		// textboxes which set the class data when updated
		final ExtendedTextBox nameTextBox = new ExtendedTextBox();
		final ExtendedTextBox rankTextBox = new ExtendedTextBox();

		final ExtendedTextBox.TextBoxValidator validator = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (nameTextBox.getTextBox().getText().trim().isEmpty()) {
					nameTextBox.setError("Name can't be empty.");
					return false;
				}
				nameTextBox.setOk();
				return true;
			}
		};

		final ExtendedTextBox.TextBoxValidator validator2 = new ExtendedTextBox.TextBoxValidator() {
			@Override
			public boolean validateTextBox() {
				if (rankTextBox.getTextBox().getText().trim().isEmpty()) {
					rankTextBox.setError("Rank value can't be empty.");
					return false;
				} else {
					try{
						Double.parseDouble(rankTextBox.getTextBox().getText().trim());
					}catch(Exception e){
						rankTextBox.setError("Value must be in like: 0.5, 1.0 etc.");
						return false;
					}
				}
				rankTextBox.setOk();
				return true;
			}
		};

		nameTextBox.setValidator(validator);
		rankTextBox.setValidator(validator2);

		final TabItem tab = this;

		addCategory.addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				if (validator.validateTextBox() && validator2.validateTextBox()) {
					CreateCategory request = new CreateCategory(JsonCallbackEvents.closeTabDisableButtonEvents(addCategory, tab));
					request.createCategory(nameTextBox.getTextBox().getText().trim(), Double.parseDouble(rankTextBox.getTextBox().getText().trim()));
				}
			}
		});

		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				session.getTabManager().closeTab(tab, false);
			}
		}));

		vp.add(table);
		vp.add(menu);
		vp.setCellHeight(menu, "30px");
		vp.setCellHorizontalAlignment(menu, HasHorizontalAlignment.ALIGN_RIGHT);

		table.setHTML(0, 0, "Name:");
		table.setWidget(0, 1, nameTextBox);
		table.setHTML(1, 0, "Rank:");
		table.setWidget(1, 1, rankTextBox);

		for (int i=0; i<table.getRowCount(); i++) {
			table.getFlexCellFormatter().setStyleName(i, 0, "itemName");
		}

		table.setHTML(2, 1, "Values like: 0.0 , 0.5 , 1.2 , etc.");
		table.getFlexCellFormatter().setStyleName(2, 1, "inputFormInlineComment");

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
		final int prime = 601;
		int result = 1;
		result = prime * result + 6786;
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
		session.getUiElements().getMenu().openMenu(MainMenu.PERUN_ADMIN);
	}

	public boolean isAuthorized() {

		if (session.isPerunAdmin()) {
			return true;
		} else {
			return false;
		}

	}

}
