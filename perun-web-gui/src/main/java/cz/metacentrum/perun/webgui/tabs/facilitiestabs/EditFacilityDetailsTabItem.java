package cz.metacentrum.perun.webgui.tabs.facilitiestabs;

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
import cz.metacentrum.perun.webgui.json.facilitiesManager.UpdateFacility;
import cz.metacentrum.perun.webgui.model.Facility;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

/**
 * !! USE ONLY AS INNER TAB !!!
 * Edit facility details tab
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id: $
 */
public class EditFacilityDetailsTabItem implements TabItem {

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
    private Facility facility;
    private ButtonTranslation buttonTranslation = ButtonTranslation.INSTANCE;
    private JsonCallbackEvents events;

    /**
     * Creates a tab instance
     *
     * @param facility
     * @param event
     */
    public EditFacilityDetailsTabItem(Facility facility, JsonCallbackEvents event){
        this.facility = facility;
        this.events = event;
    }

    public boolean isPrepared(){
        return true;
    }

    public Widget draw() {

        titleWidget = new Label("Edit: "+ Utils.getStrippedStringWithEllipsis(facility.getName()));

        VerticalPanel vp = new VerticalPanel();

        // textboxes which set the class data when updated
        final ExtendedTextBox nameTextBox = new ExtendedTextBox();
        nameTextBox.getTextBox().setText(facility.getName());

        final TextBox descriptionTextBox = new TextBox();
        //descriptionTextBox.setText(resource.getDescription());

        final ExtendedTextBox.TextBoxValidator validator = new ExtendedTextBox.TextBoxValidator() {
            @Override
            public boolean validateTextBox() {
                if (nameTextBox.getTextBox().getText().trim().isEmpty()) {
                    nameTextBox.setError("Name can't be empty.");
                    return false;
                } else {
                    nameTextBox.setOk();
                    return true;
                }
            }
        };

        nameTextBox.setValidator(validator);

        // prepares layout
        FlexTable layout = new FlexTable();
        layout.setStyleName("inputFormFlexTable");
        FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();

        // close tab events
        final TabItem tab = this;

        TabMenu menu = new TabMenu();

        // send button
        final CustomButton saveButton = TabMenu.getPredefinedButton(ButtonType.SAVE, buttonTranslation.saveFacilityDetails());
        saveButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (validator.validateTextBox()) {
                    facility.setName(nameTextBox.getTextBox().getText().trim());
                    //facility.setDescription(descriptionTextBox.getText().trim());
                    UpdateFacility request = new UpdateFacility(JsonCallbackEvents.closeTabDisableButtonEvents(saveButton, tab, events));
                    request.updateFacility(facility);
                }
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
        layout.setHTML(0, 0, "Name:");
        layout.setWidget(0, 1, nameTextBox);
        //layout.setHTML(1, 0, "Description:");
        //layout.setWidget(1, 1, descriptionTextBox);

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
        final int prime = 31;
        int result = 1;
        result = prime * result + 6786786;
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
        return true;
    }

    public void open() {
    }

    public boolean isAuthorized() {

        if (session.isFacilityAdmin(facility.getId())) {
            return true;
        } else {
            return false;
        }

    }

}