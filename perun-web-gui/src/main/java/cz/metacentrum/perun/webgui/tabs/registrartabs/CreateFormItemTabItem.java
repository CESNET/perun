package cz.metacentrum.perun.webgui.tabs.registrartabs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.applicationresources.RegistrarFormItemGenerator;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.model.ApplicationFormItem;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;

/**
 * View ApplicationFormItem
 * !! USE IN INNER TAB ONLY !!
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @version $Id: d6dc172b2a7cbfa25ca94ba0f964bc6c9b6f508c $
 */
public class CreateFormItemTabItem implements TabItem{

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
	private Label titleWidget = new Label("Create new application form item");

	/** 
	 * List with inputs
	 */
	private ArrayList<ApplicationFormItem> sourceList;

	private JsonCallbackEvents events;
	
	/**
	 * Input types
	 */
	static private final String[] INPUT_TYPES = {"TEXTFIELD", "TEXTAREA", "SELECTIONBOX", "COMBOBOX", "CHECKBOX", "USERNAME", "PASSWORD", "VALIDATED_EMAIL", "SUBMIT_BUTTON", "HTML_COMMENT", "FROM_FEDERATION_HIDDEN", "FROM_FEDERATION_SHOW"};
	
	/**
	 * Cropping length in select box after which item to add item
	 */
	static private final int CROP_LABEL_LENGTH = 25; 
	
	/**
	 * Creates a tab instance
	 *
     *
	 */
	public CreateFormItemTabItem(ArrayList<ApplicationFormItem> sourceList, JsonCallbackEvents events){
		this.sourceList = sourceList;
		this.events = events;
	}
	
	public boolean isPrepared(){
		return true;
	}
	
	public Widget draw() {
		
		// vertical panel
		VerticalPanel vp = new VerticalPanel();
		vp.setSize("100%","100%");

		// flex table
		FlexTable layout = new FlexTable();
        layout.setStyleName("inputFormFlexTable");
		FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();
		
		// select widget short name
		final ExtendedTextBox shortNameTextBox = new ExtendedTextBox();
		shortNameTextBox.setWidth("200px");

        final ExtendedTextBox.TextBoxValidator validator = new ExtendedTextBox.TextBoxValidator() {
            @Override
            public boolean validateTextBox() {
                if (shortNameTextBox.getTextBox().getText().trim().isEmpty()) {
                    shortNameTextBox.setError("Short name can't be empty.");
                    return false;
                } else {
                    shortNameTextBox.setOk();
                    return true;
                }
            }
        };
        shortNameTextBox.setValidator(validator);
		
		// select widget type
		final ListBox typeListBox = new ListBox();
		for(int i = 0; i < INPUT_TYPES.length; i++){
			String type = INPUT_TYPES[i];
			typeListBox.addItem(type, type);
		}
		
		// insert after
		final ListBox insertAfterListBox = new ListBox();
		insertAfterListBox.addItem(" - insert to the beginning - ", 0 + "");
		for(int i = 0; i < sourceList.size(); i++){
			ApplicationFormItem item = sourceList.get(i);
			RegistrarFormItemGenerator gen = new RegistrarFormItemGenerator(item, ""); // with default en locale
			String label = gen.getFormItem().getShortname();
			
			// crop length
			if(label.length() > CROP_LABEL_LENGTH){
				label = label.substring(0, CROP_LABEL_LENGTH);
			}
			
			// add to box
			insertAfterListBox.addItem(label, (i + 1) + "");
		}

        layout.setHTML(0, 0, "Short name:");
        layout.setWidget(0, 1, shortNameTextBox);
        layout.setHTML(1, 0, "Input widget:");
        layout.setWidget(1, 1, typeListBox);
		layout.setHTML(2, 0, "Insert after:");
		layout.setWidget(2, 1, insertAfterListBox);

        for (int i=0; i<layout.getRowCount(); i++) {
            cellFormatter.addStyleName(i, 0, "itemName");
        }

        TabMenu menu = new TabMenu();

		// create button
		menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CREATE, ButtonTranslation.INSTANCE.createFormItem(), new ClickHandler() {

            public void onClick(ClickEvent event) {

                if (validator.validateTextBox()) {

                    int positionToAdd = Integer.parseInt(insertAfterListBox.getValue(insertAfterListBox.getSelectedIndex()));
                    String type = typeListBox.getValue(typeListBox.getSelectedIndex());
                    String shortName = shortNameTextBox.getTextBox().getText().trim();
                    createItem(shortName, type, positionToAdd);

                }

            }
        }));

        final TabItem tab = this;
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

	/**
	 * Creates the item
	 * 
	 * @param shortname
	 * @param type
	 * @param positionToAdd
	 */
	protected void createItem(String shortname, String type, int positionToAdd) {

		ApplicationFormItem item = RegistrarFormItemGenerator.generateFormItem(shortname, type);
		
		// set also position
		item.setOrdnum(positionToAdd);
		sourceList.add(positionToAdd, item);
		
		session.getTabManager().addTabToCurrentTab(new EditFormItemTabItem(item, events));
		
		events.onFinished(item);

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
		final int prime = 31;
		int result = 1;
		result = prime * result + 672;
		return result;
	}

	/**
	 * @param obj
	 */
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

		if (session.isVoAdmin() || session.isGroupAdmin()) {
			return true; 
		} else {
			return false;
		}

	}
}