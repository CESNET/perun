package cz.metacentrum.perun.webgui.widgets;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.PerunSearchEvent;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;

/**
 * TabMenu menu, which provides place for
 * action buttons above tables
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id: 1bc44c081d1159259692e7e4f8bca2d8b3aa2e2f $
 */
public class TabMenu extends Composite {

    // menu content
    private FlexTable menu = new FlexTable();
    private int cellsCount = 0;
    private static ButtonTranslation translation = ButtonTranslation.INSTANCE;
    private static SmallIcons icons = SmallIcons.INSTANCE;

    /**
     * Constructor for tab menu
     */
    public TabMenu() {
        initWidget(menu);
    }

    /**
     * Method which adds any kind of widget into TabMenu
     *
     * @param widget
     */
    public void addWidget(Widget widget){

        // set style
        menu.getFlexCellFormatter().addStyleName(0, cellsCount, "tabMenu");
        if (cellsCount == 0) {
            // set first and last
            menu.getFlexCellFormatter().addStyleName(0, cellsCount, "tabMenu-first");
            menu.getFlexCellFormatter().addStyleName(0, cellsCount, "tabMenu-last");
        } else if (cellsCount > 0) {
            // move last from previous to this
            menu.getFlexCellFormatter().removeStyleName(0, cellsCount-1, "tabMenu-last");
            menu.getFlexCellFormatter().addStyleName(0, cellsCount, "tabMenu-last");
        }

        menu.setWidget(0, cellsCount, widget);

        cellsCount++;

    }

    /**
     * Method which adds any kind of widget into TabMenu on specific position
     * (replace any content on this position)
     *
     * @param widget
     */
    public void addWidget(int position, Widget widget){

        menu.setWidget(0, position, widget);
        // TODO - better handling

    }

    /**
     * Add search widget to tab menu
     *
     * @param searchEvent event triggered when user click on search button
     * @param title displayed on button hover
     */
    public TextBox addSearchWidget(final PerunSearchEvent searchEvent, final String title) {

        final TextBox textBox = new TextBox();

        final CustomButton button = getPredefinedButton(ButtonType.SEARCH, title);

        // trigger search on ENTER
        textBox.addKeyUpHandler(new KeyUpHandler() {
            public void onKeyUp(KeyUpEvent event) {
                if (!textBox.getText().isEmpty()) {
                    button.setEnabled(true);
                    if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER){
                        searchEvent.searchFor(textBox.getText());
                    }
                } else {
                    button.setEnabled(false);
                }
            }
        });

        // button click triggers action
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                if(UiElements.searchStingCantBeEmpty(textBox.getText())){
                    searchEvent.searchFor(textBox.getText());
                }
            }
        });

        // button is active only if there is "search string"
        button.setEnabled(false);

        this.addWidget(textBox);
        this.addWidget(button);

        // always focus search widget
        Scheduler.get().scheduleDeferred(new Command() {
            @Override
            public void execute() {
                textBox.setFocus(true);
            }
        });

        return textBox;

    }

    /**
     * Add search widget to tab menu
     *
     * @param searchEvent event triggered when user click on search button
     * @param button button to handle search
     */
    public TextBox addSearchWidget(final PerunSearchEvent searchEvent, final CustomButton button) {

        final TextBox textBox = new TextBox();

        // trigger search on ENTER
        textBox.addKeyUpHandler(new KeyUpHandler() {
            public void onKeyUp(KeyUpEvent event) {
                if (!textBox.getText().isEmpty()) {
                    button.setEnabled(true);
                    if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER){
                        searchEvent.searchFor(textBox.getText());
                    }
                } else {
                    button.setEnabled(false);
                }
            }
        });

        // button click triggers action
        button.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                if(UiElements.searchStingCantBeEmpty(textBox.getText())){
                    searchEvent.searchFor(textBox.getText());
                }
            }
        });

        // button is active only if there is "search string"
        button.setEnabled(false);

        this.addWidget(textBox);
        this.addWidget(button);

        // always focus search widget
        Scheduler.get().scheduleDeferred(new Command() {
            @Override
            public void execute() {
                textBox.setFocus(true);
            }
        });

        return textBox;

    }

    /**
     *
     * @param box suggest box with oracle
     * @param filterEvent filtering event
     * @param title
     */
    public MultiWordSuggestOracle addFilterWidget(SuggestBox box, final PerunSearchEvent filterEvent, final String title) {

        final SuggestBox suggest = box;

        // search box on enter
        suggest.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER){
                    filterEvent.searchFor(suggest.getText());
                } else if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
                    suggest.hideSuggestionList();
                }
            }
        });

        // search box on selected
        suggest.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
            public void onSelection(SelectionEvent<SuggestOracle.Suggestion> event) {
                filterEvent.searchFor(event.getSelectedItem().getReplacementString());
            }
        });

        // search box on value changed
        suggest.addValueChangeHandler(new ValueChangeHandler<String>() {
            public void onValueChange(ValueChangeEvent<String> event) {

            }
        });

        // search button
        CustomButton filterButton = getPredefinedButton(ButtonType.FILTER, title, new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                filterEvent.searchFor(suggest.getText());
            }
        });

        this.addWidget(suggest);
        this.addWidget(filterButton);

        // always focus filter widget
        Scheduler.get().scheduleDeferred(new Command() {
            @Override
            public void execute() {
                suggest.setFocus(true);
            }
        });

        return ((MultiWordSuggestOracle)box.getSuggestOracle());

    }

    /**
     * Return defined button without clickHandler
     *
     * @param type selected button type
     * @param title text displayed on button hover
     * @return created button
     */
    public static CustomButton getPredefinedButton(ButtonType type, String title) {

        return getPredefinedButton(type, title, null);

    }

    /**
     * Return defined button with own click handler
     *
     * @param type selected button type
     * @param title text displayed on button hover
     * @param clickAction ClickHandler / if null it's not added
     * @return create button
     */
    public static CustomButton getPredefinedButton(ButtonType type, String title, ClickHandler clickAction) {

        CustomButton b = new CustomButton();

        // icon first
        b.setIcon(getButtonIconByType(type));
        // then text

        b.setText(getButtonTextByType(type));

        if (title != null && !title.isEmpty()) {
            b.setTitle(title);
        }

        if (clickAction != null) {
            b.addClickHandler(clickAction);
        }

        if (ButtonType.CONTINUE.equals(type)) {
            b.setImageAlign(true);
        }

        return b;

    }


    /**
     * Return translatable button text by button type
     *
     * @param type button type
     * @return translatable button text
     */
    private static String getButtonTextByType(ButtonType type) {

        if (ButtonType.ADD.equals(type)) {
            return translation.addButton();
        } else if (ButtonType.CREATE.equals(type)) {
            return translation.createButton();
        } else if (ButtonType.REMOVE.equals(type)) {
            return translation.removeButton();
        } else if (ButtonType.DELETE.equals(type)) {
            return translation.deleteButton();
        } else if (ButtonType.SAVE.equals(type)) {
            return translation.saveButton();
        } else if (ButtonType.SEARCH.equals(type)) {
            return translation.searchButton();
        } else if (ButtonType.FILTER.equals(type)) {
            return translation.filterButton();
        } else if (ButtonType.OK.equals(type)) {
            return translation.okButton();
        } else if (ButtonType.CANCEL.equals(type)) {
            return translation.cancelButton();
        } else if (ButtonType.CLOSE.equals(type)) {
            return translation.closeButton();
        } else if (ButtonType.CONTINUE.equals(type)) {
            return translation.continueButton();
        } else if (ButtonType.FINISH.equals(type)) {
            return translation.finishButton();
        } else if (ButtonType.BACK.equals(type)) {
            return translation.backButton();
        } else if (ButtonType.FILL.equals(type)) {
            return translation.fillButton();
        } else if (ButtonType.LIST_ALL_MEMBERS.equals(type)) {
            return translation.listAllMembersButton();
        } else if (ButtonType.LIST_ALL_USERS.equals(type)) {
            return translation.listAllUsersButton();
        } else if (ButtonType.VERIFY.equals(type)) {
            return translation.verifyButton();
        } else if (ButtonType.APPROVE.equals(type)) {
            return translation.approveButton();
        } else if (ButtonType.REJECT.equals(type)) {
            return translation.rejectButton();
        } else if (ButtonType.REFRESH.equals(type)) {
            return translation.refreshButton();
        } else if (ButtonType.PREVIEW.equals(type)) {
            return translation.previewButton();
        } else if (ButtonType.ENABLE.equals(type)) {
            return translation.enableButton();
        } else if (ButtonType.DISABLE.equals(type)) {
            return translation.disableButton();
        } else if (ButtonType.SETTINGS.equals(type)) {
            return translation.settingsButton();
        } else {
            return "";
        }

    }

    /**
     * Return translatable button text by button type
     *
     * @param type button type
     * @return translatable button text
     */
    private static ImageResource getButtonIconByType(ButtonType type) {

        if (ButtonType.ADD.equals(type)) {
            return icons.addIcon();
        } else if (ButtonType.CREATE.equals(type)) {
            return icons.addIcon();
        } else if (ButtonType.REMOVE.equals(type)) {
            return icons.deleteIcon();
        } else if (ButtonType.DELETE.equals(type)) {
            return icons.deleteIcon();
        } else if (ButtonType.SAVE.equals(type)) {
            return icons.diskIcon();
        } else if (ButtonType.SEARCH.equals(type)) {
            return icons.findIcon();
        } else if (ButtonType.FILTER.equals(type)) {
            return icons.filterIcon();
        } else if (ButtonType.OK.equals(type)) {
            return icons.acceptIcon();
        } else if (ButtonType.CANCEL.equals(type)) {
            return icons.stopIcon();
        } else if (ButtonType.CLOSE.equals(type)) {
            return icons.crossIcon();
        } else if (ButtonType.CONTINUE.equals(type)) {
            return icons.arrowRightIcon();
        } else if (ButtonType.FINISH.equals(type)) {
            return icons.arrowRightIcon();
        } else if (ButtonType.BACK.equals(type)) {
            return icons.arrowLeftIcon();
        } else if (ButtonType.FILL.equals(type)) {
            return icons.lightningIcon();
        } else if (ButtonType.LIST_ALL_MEMBERS.equals(type)) {
            return icons.userGreenIcon();
        } else if (ButtonType.LIST_ALL_USERS.equals(type)) {
            return icons.userGrayIcon();
        } else if (ButtonType.VERIFY.equals(type)) {
            return icons.userGreenIcon();
        } else if (ButtonType.APPROVE.equals(type)) {
            return icons.acceptIcon();
        } else if (ButtonType.REJECT.equals(type)) {
            return icons.cancelIcon();
        } else if (ButtonType.REFRESH.equals(type)) {
            return icons.updateIcon();
        } else if (ButtonType.PREVIEW.equals(type)) {
            return icons.applicationFormMagnifyIcon();
        } else if (ButtonType.ENABLE.equals(type)) {
            return icons.acceptIcon();
        } else if (ButtonType.DISABLE.equals(type)) {
            return icons.cancelIcon();
        } else if (ButtonType.SETTINGS.equals(type)) {
            return icons.cogIcon();
        } else {
            // default
            return icons.errorIcon();
        }

    }

    /**
     * Clear all menu content (discard all widgets) !!
     */
    public void clear() {
        menu.clear();
    }

}
