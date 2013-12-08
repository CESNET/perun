package cz.metacentrum.perun.webgui.tabs.registrartabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.UiElements;
import cz.metacentrum.perun.webgui.client.applicationresources.RegistrarFormItemGenerator;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.TableSorter;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.json.attributesManager.GetAttributesDefinition;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * View and edit ApplicationFormItem
 * !! FOR USE IN INNER TAB ONLY !!
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id: ff645a4c253a4f54bdd15e6f1d9bdd255e6647ba $
 */
public class EditFormItemTabItem implements TabItem{

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
    private Label titleWidget = new Label("Application form item");

    /**
     * Item object
     */
    private ApplicationFormItem item;

    private JsonCallbackEvents events;

    /**
     * Inputs
     */
    // Basic
    private TextBox shortNameTextBox = new TextBox();
    private ListBox federationAttributes = new ListBox();
    private CheckBox requiredCheckBox = new CheckBox();
    private ListBox perunDestinationAttributeListBox = new ListBox();
    private TextBox regexTextBox = new TextBox();
    private ArrayList<CheckBox> applicationTypesCheckBoxes = new ArrayList<CheckBox>();


    /**
     * KEY = locale, VALUE = textbox
     */
    private Map<String, TextArea> labelTextBoxes = new HashMap<String, TextArea>();
    private Map<String, TextArea> helpTextBoxes = new HashMap<String, TextArea>();
    private Map<String, TextArea> errorTextBoxes = new HashMap<String, TextArea>();

    /**
     * KEY = locale, VALUE = Map<TextBox, TextBox> (key,value)
     */
    private Map<String,Map<TextBox,TextBox>> optionsBoxes = new HashMap<String,Map<TextBox,TextBox>>();

    private TabItem tab;

    /**
     * Creates a tab instance
     *
     * @param item
     * @param events
     */
    public EditFormItemTabItem(ApplicationFormItem item, JsonCallbackEvents events){
        this.item = item;
        this.events = events;
    }

    public boolean isPrepared(){
        return true;
    }

    /**
     * Returns flex table with settings for the language
     *
     * @param locale
     * @return
     */
    private Widget itemTextTab(String locale) {

        ItemTexts itemTexts = item.getItemTexts(locale);
        if(itemTexts == null){
            // create empty item texts
            JSONObject itemTextJson = new JSONObject();
            itemTextJson.put("label", new JSONString(""));
            itemTextJson.put("help", new JSONString(""));
            itemTextJson.put("errorMessage", new JSONString(""));
            itemTextJson.put("options", new JSONString(""));
        }
        item.setItemTexts(locale, itemTexts);

        TextArea labelTextBox = new TextArea();
        labelTextBoxes.put(locale, labelTextBox);

        TextArea helpTextBox = new TextArea();
        helpTextBoxes.put(locale, helpTextBox);

        TextArea errorTextBox = new TextArea();
        errorTextBoxes.put(locale, errorTextBox);

        // layout
        final FlexTable ft = new FlexTable();
        ft.setStyleName("inputFormFlexTable");
        ft.setSize("550px", "100%");
        final FlexCellFormatter ftf = ft.getFlexCellFormatter();

        // sizes
        labelTextBox.setWidth("440px");
        helpTextBox.setWidth("440px");
        errorTextBox.setWidth("440px");

        // fill values
        labelTextBox.setText(itemTexts.getLabel());
        helpTextBox.setText(itemTexts.getHelp());
        errorTextBox.setText(itemTexts.getErrorMessage());

        // adding to table
        int row = 0;

        Label label = new Label("Label:");
        ft.setWidget(row, 0, label);
        ft.setWidget(row, 1, labelTextBox);

        row++;
        ft.setHTML(row, 1, "Label displayed to users to identify item on application form. If empty, \"Short name\" from basic settings is used as fallback. For HTML_COMMENT type is used as content to display.");
        ftf.setStyleName(row, 1, "inputFormInlineComment");

        row++;

        Label helpLabel = new Label("Help:");
        ft.setWidget(row, 0, helpLabel);
        ft.setWidget(row, 1, helpTextBox);

        row++;
        ft.setHTML(row, 1, "Help text displayed to user along with input widget.");
        ftf.setStyleName(row, 1, "inputFormInlineComment");

        row++;

        Label errorLabel = new Label("Error:");
        ft.setWidget(row, 0, errorLabel);
        ft.setWidget(row, 1, errorTextBox);

        row++;
        ft.setHTML(row, 1, "Error message displayed to user when inputs wrong value.");
        ftf.setStyleName(row, 1, "inputFormInlineComment");


        // style
        for(int i = 0; i<ft.getRowCount(); i++){
            ftf.setStyleName(i, 0, "itemName");
        }

        // box items table
        final FlexTable boxItemTable = new FlexTable();

        // final layout
        VerticalPanel vp  = new VerticalPanel();
        vp.add(ft);

        // values for selection and combobox
        if ("SELECTIONBOX".equalsIgnoreCase(item.getType())|| "COMBOBOX".equalsIgnoreCase(item.getType())) {

            boxItemTable.setStyleName("inputFormFlexTable");
            boxItemTable.setWidth("550px");

            int boxRow = 0;

            // clear options boxes
            final Map<TextBox, TextBox> currentOptions = new HashMap<TextBox, TextBox>();
            optionsBoxes.put(locale, currentOptions);

            boxRow++;

            Label boxContentLabel = new Label("Selectionbox / Combobox options:");
            boxItemTable.setWidget(boxRow, 0, boxContentLabel);
            boxItemTable.getFlexCellFormatter().setStyleName(boxRow, 0, "itemName");
            boxItemTable.getFlexCellFormatter().setColSpan(boxRow, 0, 4);

            boxRow++;

            HTML comment = new HTML("Define possible options for selection in SELECTIONBOX or COMBOBOX widget. Empty options are not used.");
            comment.setStyleName("inputFormInlineComment");
            boxItemTable.setWidget(boxRow, 0, comment);
            boxItemTable.getFlexCellFormatter().setColSpan(boxRow, 0, 4);

            boxRow++;
            final Map<String, String> values = new HashMap<String, String>();

            // parse values from the item
            String options = itemTexts.getOptions();
            if(options != null) {
                // for each value, add key-value
                values.putAll(RegistrarFormItemGenerator.parseSelectionBox(options));
            }

            // for each add new row
            for(Map.Entry<String, String> entry : values.entrySet()){

                final TextBox keyTextBox = new TextBox();
                final TextBox valueTextBox = new TextBox();

                currentOptions.put(keyTextBox, valueTextBox);

                keyTextBox.setText(entry.getKey());
                valueTextBox.setText(entry.getValue());

                boxItemTable.setHTML(boxRow, 0, "Value:");
                boxItemTable.getFlexCellFormatter().setStyleName(boxRow, 0, "itemName");
                boxItemTable.setWidget(boxRow, 1, keyTextBox);
                boxItemTable.setHTML(boxRow, 2, "Label:");
                boxItemTable.getFlexCellFormatter().setStyleName(boxRow, 2, "itemName");
                boxItemTable.setWidget(boxRow, 3, valueTextBox);

                boxRow++;

            }

            // button for adding new
            CustomButton addNewButton = new CustomButton(ButtonTranslation.INSTANCE.addButton(), ButtonTranslation.INSTANCE.addNewSelectionBoxItem(), SmallIcons.INSTANCE.addIcon(), new ClickHandler() {
                public void onClick(ClickEvent event) {

                    final int r = boxItemTable.getRowCount();

                    final TextBox keyTextBox = new TextBox();
                    final TextBox valueTextBox = new TextBox();

                    currentOptions.put(keyTextBox, valueTextBox);

                    boxItemTable.insertRow(r-1);

                    boxItemTable.setHTML(r-1, 0, "Value:");
                    boxItemTable.getFlexCellFormatter().setStyleName(r-1, 0, "itemName");
                    boxItemTable.setWidget(r-1, 1, keyTextBox);
                    boxItemTable.setHTML(r-1, 2, "Label:");
                    boxItemTable.getFlexCellFormatter().setStyleName(r-1, 2, "itemName");
                    boxItemTable.setWidget(r-1, 3, valueTextBox);

                    UiElements.runResizeCommands(tab);

                }
            });
            boxItemTable.setWidget(boxRow, 0, addNewButton);
            boxItemTable.getFlexCellFormatter().setColSpan(boxRow, 0, 2);

            vp.add(boxItemTable);

        }

        vp.addStyleName("perun-table");

        // scroll panel
        ScrollPanel sp = new ScrollPanel(vp);
        sp.addStyleName("perun-tableScrollPanel");
        sp.setSize("560px", "100%");

        return sp;

    }

    /**
     * Returns flex table with basic information and textboxes
     *
     * @return
     */
    private Widget basicInformationTab() {

        // item application types
        ArrayList<String> itemApplicationTypes = JsonUtils.listFromJsArrayString(item.getApplicationTypes());

        // federation attributes to select from
        federationAttributes.addItem("No item selected (empty value)", "");
        federationAttributes.addItem("Display name", "displayName");
        federationAttributes.addItem("Common name", "cn");
        federationAttributes.addItem("Mail", "mail");
        federationAttributes.addItem("Organization", "o");
        federationAttributes.addItem("Level of Assurance (LoA)", "loa");
        federationAttributes.addItem("First name", "givenName");
        federationAttributes.addItem("Sure name", "sn");
        federationAttributes.addItem("EPPN", "eppn");

        // application types
        GetAttributesDefinition attrDef = new GetAttributesDefinition(new JsonCallbackEvents(){
            @Override
            public void onError(PerunError error) {
                perunDestinationAttributeListBox.clear();
                perunDestinationAttributeListBox.addItem("No item selected (empty value)", "");
                if (item.getPerunDestinationAttribute() != null && !item.getPerunDestinationAttribute().isEmpty()) {
                    // add and select returned perun dest attr
                    perunDestinationAttributeListBox.addItem(item.getPerunDestinationAttribute(), item.getPerunDestinationAttribute());
                    perunDestinationAttributeListBox.setSelectedIndex(1);
                }
            }
            @Override
            public void onFinished(JavaScriptObject jso) {
                // clear
                perunDestinationAttributeListBox.clear();
                // set empty possibility
                perunDestinationAttributeListBox.addItem("No item selected (empty value)", "");

                ArrayList<AttributeDefinition> list = JsonUtils.jsoAsList(jso);
                if (list != null && !list.isEmpty()) {
                    // sort
                    list = new TableSorter<AttributeDefinition>().sortByFriendlyName(list);
                    for (AttributeDefinition def : list) {
                        // add only member and user attributes
                        if (def.getEntity().equalsIgnoreCase("user") || def.getEntity().equalsIgnoreCase("member")) {
                            perunDestinationAttributeListBox.addItem(def.getFriendlyName()+" ("+def.getEntity()+" / "+def.getDefinition()+")", def.getName());
                        }
                    }
                } else {
                    // no attr def loaded, keep as it is set
                    if (item.getPerunDestinationAttribute() != null && !item.getPerunDestinationAttribute().isEmpty()) {
                        perunDestinationAttributeListBox.addItem(item.getPerunDestinationAttribute(), item.getPerunDestinationAttribute());
                    }
                }
                // set selected
                for (int i=0; i<perunDestinationAttributeListBox.getItemCount(); i++) {
                    // set proper value as "selected"
                    if (perunDestinationAttributeListBox.getValue(i).equalsIgnoreCase(item.getPerunDestinationAttribute())) {
                        perunDestinationAttributeListBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
            @Override
            public void onLoadingStart() {
                perunDestinationAttributeListBox.addItem("Loading...");
            }
        });

        // layout
        FlexTable ft = new FlexTable();
        ft.setStyleName("inputFormFlexTable");
        FlexCellFormatter ftf = ft.getFlexCellFormatter();

        // fill values
        shortNameTextBox.setText(item.getShortname());
        for (int i=0; i<federationAttributes.getItemCount(); i++) {
            if (federationAttributes.getValue(i).equals(item.getFederationAttribute())) {
                federationAttributes.setSelectedIndex(i);
                break;
            }
        }
        requiredCheckBox.setValue(item.isRequired());
        regexTextBox.setText(item.getRegex());

        for(Application.ApplicationType type : Application.ApplicationType.values()){
            CheckBox cb = new CheckBox();
            boolean checked = itemApplicationTypes.contains(type.toString());
            cb.setValue(checked);
            applicationTypesCheckBoxes.add(cb);
        }

        if (item.getType().equals("VALIDATED_EMAIL")) {
            regexTextBox.setEnabled(false);
        }

        // sizes
        shortNameTextBox.setWidth("200px");
        federationAttributes.setWidth("200px");
        perunDestinationAttributeListBox.setWidth("300px");
        regexTextBox.setWidth("200px");

        // basic info
        int row = 0;

        Label shortNameLabel = new Label("Short name:");
        ft.setWidget(row, 0, shortNameLabel);
        ft.setWidget(row, 1, shortNameTextBox);

        row++;
        ft.setHTML(row, 1, "Internal item identification (used as fallback when you forgot to set \"Label\" for some language).");
        ftf.setStyleName(row, 1, "inputFormInlineComment");

        row++;

        Label inputLabel = new Label("Input widget:");
        ft.setWidget(row, 0, inputLabel);
        ft.setHTML(row, 1, item.getType());

        row++;
        ft.setHTML(row, 1, "Specify what input widget is used for this item.");
        ftf.setStyleName(row, 1, "inputFormInlineComment");

        // set colspan for tops
        for (int i=0; i<ft.getRowCount(); i++) {
            ftf.setColSpan(i, 1, 2);
        }

        row++;

        Label l = new Label("Display on application:");
        l.setTitle("");
        ft.setWidget(row, 0, l);
        ftf.setWidth(row, 0, "160px");

        Application.ApplicationType.values();

        int i = 0;
        for (Application.ApplicationType type : Application.ApplicationType.values()) {
            CheckBox cb = applicationTypesCheckBoxes.get(i);
            cb.setText(Application.getTranslatedType(type.toString()));
            if (type.equals(Application.ApplicationType.INITIAL)) {
                cb.setTitle("If checked, display form item on INITIAL application");
            } else {
                cb.setTitle("If checked, display form item on EXTENSION application");
            }
            ft.setWidget(row, i+1, cb);
            i++;
        }

        row++;
        ft.setHTML(row, 1, "Define on which application types is this item displayed.");
        ftf.setStyleName(row, 1, "inputFormInlineComment");
        ftf.setColSpan(row, 1, 2);

        row++;

        // IF BUTTON OR COMMENT, don't show these
        if(!item.getType().equals("SUBMIT_BUTTON") && !item.getType().equals("HTML_COMMENT")) {

            // load attr defs only when showed
            attrDef.retrieveData();

            Label requiredLabel = new Label("Required:");
            ft.setWidget(row, 0, requiredLabel);
            ft.setWidget(row, 1, requiredCheckBox);
            ftf.setColSpan(row, 1, 2);

            row++;
            ft.setHTML(row, 1, "If checked, user can`t submit empty value.");
            ftf.setStyleName(row, 1, "inputFormInlineComment");
            ftf.setColSpan(row, 1, 2);

            row++;

            Label destAttrLabel = new Label("Destination attribute:");
            ft.setWidget(row, 0, destAttrLabel);
            ft.setWidget(row, 1, perunDestinationAttributeListBox);
            ftf.setColSpan(row, 1, 2);

            row++;
            ft.setHTML(row, 1, "Select attribute, where will be submitted value stored after accepting user`s application and where is taken to pre-fill the form.");
            ftf.setStyleName(row, 1, "inputFormInlineComment");
            ftf.setColSpan(row, 1, 2);

            row++;

            Label fedAttrLabel = new Label("Federation attribute:");
            ft.setWidget(row, 0, fedAttrLabel);
            ft.setWidget(row, 1, federationAttributes);
            ftf.setColSpan(row, 1, 2);

            row++;
            ft.setHTML(row, 1, "Select federation attribute to get pre-filed value from.");
            ftf.setStyleName(row, 1, "inputFormInlineComment");
            ftf.setColSpan(row, 1, 2);

            row++;

            Label regexLabel = new Label("Regular expression:");
            ft.setWidget(row, 0, regexLabel);
            ft.setWidget(row, 1, regexTextBox);
            ftf.setColSpan(row, 1, 2);

            row++;
            ft.setHTML(row, 1, "Regular expression used for item value validation (before submitting by user).");
            ftf.setStyleName(row, 1, "inputFormInlineComment");
            ftf.setColSpan(row, 1, 2);

        }

        // set styles
        for (int n=0; n<ft.getRowCount(); n++) {
            ftf.setStyleName(n, 0, "itemName");
        }

        // scroll panel
        ScrollPanel sp = new ScrollPanel(ft);
        sp.addStyleName("perun-tableScrollPanel");
        sp.setSize("560px", "320px");

        sp.scrollToTop();

        return sp;
    }

    public Widget draw() {

        this.tab = this;

        this.titleWidget.setText("Application form item: "+item.getShortname());

        // languages
        ArrayList<String> languages = new ArrayList<String>();
        languages.add("cs");
        languages.add("en");

        // vertical panel
        VerticalPanel vp = new VerticalPanel();
        vp.setWidth("570px");
        vp.setHeight("375px");

        // tab panel
        TabLayoutPanel tabPanel = new TabLayoutPanel(30, Unit.PX);
        tabPanel.addStyleName("smallTabPanel");
        tabPanel.setHeight("350px");

        // basic settings
        tabPanel.add(basicInformationTab(), "Basic settings");

        // for each locale add tab
        for(String locale : languages){
            tabPanel.add(itemTextTab(locale), "Lang: "+locale);
        }

        // add menu
        final TabItem tab = this;
        TabMenu tabMenu = new TabMenu();
        tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.SAVE, ButtonTranslation.INSTANCE.saveFormItem(), new ClickHandler() {
            public void onClick(ClickEvent event) {
                //save item and reload local content
                saveItem();
                events.onFinished(item);
                // do not reload content from RPC !!
                session.getTabManager().closeTab(tab, false);
            }
        }));
        tabMenu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                session.getTabManager().closeTab(tab, false);
            }
        }));

        // add tab panel to main panel
        vp.add(tabPanel);
        vp.setCellHeight(tabPanel, "350px");
        vp.setCellWidth(tabPanel, "570px");

        //session.getUiElements().resizeSmallTabPanel(tabPanel.getWidget(0), 350, 60, tab);

        vp.add(tabMenu);
        vp.setCellHeight(tabMenu, "30px");
        vp.setCellHorizontalAlignment(tabMenu, HasHorizontalAlignment.ALIGN_RIGHT);

        this.contentWidget.setWidget(vp);

        return getWidget();
    }

    /**
     * Saves the values back to the item
     */
    protected void saveItem() {

        // TODO set only when actual change happens
        item.setEdited(true);

        // shortName is required item !!
        if (shortNameTextBox.getText() == null || (shortNameTextBox.getText().isEmpty())) {
            Window.alert("shortName is required parameter and can't be empty !");
            return;
        }

        item.setFederationAttribute(federationAttributes.getValue(federationAttributes.getSelectedIndex()));

        if (perunDestinationAttributeListBox.getSelectedIndex() > 0) {
            // some value set
            item.setPerunDestinationAttribute(perunDestinationAttributeListBox.getValue(perunDestinationAttributeListBox.getSelectedIndex()));
        } else {
            // empty value set
            item.setPerunDestinationAttribute(null);
        }

        item.setRegex(regexTextBox.getText().trim());
        item.setRequired(requiredCheckBox.getValue());
        item.setShortname(shortNameTextBox.getText().trim());

        JSONArray newApplicationTypesJson = new JSONArray();
        int pointer = 0;
        int i = 0;
        for (Application.ApplicationType type : Application.ApplicationType.values()) {
            CheckBox cb = applicationTypesCheckBoxes.get(i);
            if(cb.getValue()){
                newApplicationTypesJson.set(pointer, new JSONString(type.toString()));
                pointer++;
            }
            i++;
        }
        item.setApplicationTypes(newApplicationTypesJson.getJavaScriptObject());
		
		
		/* LANGUAGE */

        // item texts
        Map<String, ItemTexts> itemTextsMap = new HashMap<String, ItemTexts>();

        // help
        for(Map.Entry<String, TextArea> entry : helpTextBoxes.entrySet()){
            String locale = entry.getKey();

            ItemTexts itemTexts;

            // if already
            if(itemTextsMap.containsKey(locale)){
                itemTexts = itemTextsMap.get(locale);
            }else{
                itemTexts = new JSONObject().getJavaScriptObject().cast();
            }

            // set help
            itemTexts.setHelp(entry.getValue().getValue().trim());

            // update
            itemTextsMap.put(locale, itemTexts);
        }

        // label
        for(Map.Entry<String, TextArea> entry : labelTextBoxes.entrySet()){
            String locale = entry.getKey();

            ItemTexts itemTexts;

            // if already
            if(itemTextsMap.containsKey(locale)){
                itemTexts = itemTextsMap.get(locale);
            }else{
                itemTexts = new JSONObject().getJavaScriptObject().cast();
            }

            // set help
            itemTexts.setLabel(entry.getValue().getValue().trim());

            // update
            itemTextsMap.put(locale, itemTexts);
        }

        // error
        for(Map.Entry<String, TextArea> entry : errorTextBoxes.entrySet()){
            String locale = entry.getKey();

            ItemTexts itemTexts;

            // if already
            if(itemTextsMap.containsKey(locale)){
                itemTexts = itemTextsMap.get(locale);
            }else{
                itemTexts = new JSONObject().getJavaScriptObject().cast();
            }

            // set help
            itemTexts.setErrorMessage(entry.getValue().getValue().trim());

            // update
            itemTextsMap.put(locale, itemTexts);
        }

        // OPTIONS
        for(Map.Entry<String, Map<TextBox, TextBox>> localeTextboxes : optionsBoxes.entrySet())
        {
            String locale = localeTextboxes.getKey();
            Map<String, String> keyValue = new HashMap<String, String>();

            // iterate over textboxes
            for(Map.Entry<TextBox, TextBox> textBoxes : localeTextboxes.getValue().entrySet())
            {
                String key = textBoxes.getKey().getText();
                String value = textBoxes.getValue().getText();

                if(!key.equals("") && !value.equals("")){
                    keyValue.put(key.trim(), value.trim());
                }
            }

            // serialize key-value
            String options = RegistrarFormItemGenerator.serializeSelectionBox(keyValue);

            ItemTexts itemTexts;

            // if already
            if(itemTextsMap.containsKey(locale)){
                itemTexts = itemTextsMap.get(locale);
            }else{
                itemTexts = new JSONObject().getJavaScriptObject().cast();
            }

            // set options
            itemTexts.setOptions(options);

            // update
            itemTextsMap.put(locale, itemTexts);
        }

        // FOR EACH ITEM TEXT, save it
        for(Map.Entry<String, ItemTexts> entry : itemTextsMap.entrySet()){
            String locale = entry.getKey();
            ItemTexts itemTexts = entry.getValue();

            session.getUiElements().setLogText(itemTexts.toSource());

            // save it
            this.item.setItemTexts(locale, itemTexts);
        }

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