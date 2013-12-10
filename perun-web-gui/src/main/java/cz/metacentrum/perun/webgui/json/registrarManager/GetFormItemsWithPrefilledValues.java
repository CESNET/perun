package cz.metacentrum.perun.webgui.json.registrarManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.applicationresources.FormValidator;
import cz.metacentrum.perun.webgui.client.applicationresources.RegistrarFormItemGenerator;
import cz.metacentrum.perun.webgui.client.applicationresources.SendsApplicationForm;
import cz.metacentrum.perun.webgui.client.localization.ApplicationMessages;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.PerunEntity;
import cz.metacentrum.perun.webgui.json.*;
import cz.metacentrum.perun.webgui.model.*;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.CustomButton;

import java.util.ArrayList;

/**
 * Returns the form elements in application form
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @version $Id: c64c2bd51960717f423bbc3820ada416c63cecc8 $
 */
public class GetFormItemsWithPrefilledValues implements JsonCallback {

    // Session
    private PerunWebSession session = PerunWebSession.getInstance();

    // VO / GROUP id
    private int id;

    // JSON URL
    static private final String JSON_URL = "registrarManager/getFormItemsWithPrefilledValues";

    // External events
    private JsonCallbackEvents events = new JsonCallbackEvents();

    // Type
    private String type = "";

    // Loader image
    private AjaxLoaderImage loaderImage = new AjaxLoaderImage();

    // Content
    private SimplePanel contents = new SimplePanel();

    // list
    private ArrayList<ApplicationFormItemWithPrefilledValue> applFormItems = new ArrayList<ApplicationFormItemWithPrefilledValue>();

    // RegistrarFormItemGenerators for getting values
    private ArrayList<RegistrarFormItemGenerator> applFormGenerators = new ArrayList<RegistrarFormItemGenerator>();

    private SendsApplicationForm sendFormHandler;

    private CustomButton sendButton;

    private boolean hidden = false;

    private String locale = "en";

    private PerunEntity entity;

    /**
     * Creates a new method instance
     *
     * @param entity entity
     * @param id entity ID
     */
    public GetFormItemsWithPrefilledValues(PerunEntity entity, int id) {
        this.id = id;
        this.entity = entity;
        this.contents.setWidget(loaderImage);
    }

    /**
     * Creates a new method instance
     *
     * @param entity entity
     * @param id entity ID
     * @param locale locale
     */
    public GetFormItemsWithPrefilledValues(PerunEntity entity, int id, String locale) {
        this(entity, id);
        this.locale = locale;
    }

    /**
     * Creates a new method instance
     *
     * @param entity entity
     * @param id entity ID
     * @param events Custom events
     */
    public GetFormItemsWithPrefilledValues(PerunEntity entity, int id, JsonCallbackEvents events) {
        this(entity, id);
        this.events = events;
    }

    /**
     * Creates a new method instance
     *
     * @param entity entity
     * @param id entity ID
     * @param events Custom events
     * @param locale Locale
     */
    public GetFormItemsWithPrefilledValues(PerunEntity entity, int id, JsonCallbackEvents events, String locale) {
        this(entity, id, events);
        this.locale = locale;
    }

    /**
     * Retrieve data from RPC
     */
    public void retrieveData()
    {

        String param = "";

        if (entity.equals(PerunEntity.VIRTUAL_ORGANIZATION)) {
            param = "vo=" + this.id;
        } else if (entity.equals(PerunEntity.GROUP)) {
            param = "group=" + this.id;
        }

        if(type.length() != 0){
            param += "&type=" + type;
        }

        // locale
        param += "&locale=" + this.locale;

        JsonClient js = new JsonClient();
        js.setHidden(hidden);
        js.retrieveData(JSON_URL, param, this);
    }

    /**
     * Returns contents
     */
    public Widget getContents()
    {
        return this.contents;
    }


    /**
     * Called when an error occurs.
     */
    public void onError(PerunError error) {

        session.getUiElements().setLogErrorText("Error while loading application form items.");
        events.onError(error);
        loaderImage.loadingError(error);

        FlexTable ft = new FlexTable();
        ft.setSize("100%", "300px");

        if (error.getName().equalsIgnoreCase("DuplicateRegistrationAttemptException")) {

            ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.errorIcon())+ApplicationMessages.INSTANCE.duplicateRegistrationAttemptExceptionText());

            if (Location.getParameter("targetnew") != null) {
                Location.replace(Location.getParameter("targetnew"));
            }

        } else if (error.getName().equalsIgnoreCase("ExtendMembershipException")) {

            if ("NOUSERLOA".equalsIgnoreCase(error.getReason())) {
                ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.errorIcon())+ApplicationMessages.INSTANCE.noUserLoa());
            } else if ("INSUFFICIENTLOA".equalsIgnoreCase(error.getReason())) {
                ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.errorIcon())+ApplicationMessages.INSTANCE.insufficientLoa());
            } else if ("INSUFFICIENTLOAFOREXTENSION".equalsIgnoreCase(error.getReason())) {
                ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.errorIcon())+ApplicationMessages.INSTANCE.insufficientLoaForExtension());
            } else if ("OUTSIDEEXTENSIONPERIOD".equalsIgnoreCase(error.getReason())) {
                ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.errorIcon())+ApplicationMessages.INSTANCE.outsideExtensionPeriod());

                // redirect if possible
                if (Location.getParameter("targetexisting") != null) {
                    Location.replace(Location.getParameter("targetexisting"));
                }

            }

        } else {
            ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.errorIcon())+"<h2>"+error.getErrorInfo()+"</h2>");
        }

        ft.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
        ft.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
        contents.setWidget(ft);

    }

    /**
     * Called when loading starts.
     */
    public void onLoadingStart() {
        session.getUiElements().setLogText("Loading application form items in selected VO started.");
        events.onLoadingStart();
    }

    /**
     * Called when loading successfully finishes.
     */
    public void onFinished(JavaScriptObject jso) {

        applFormItems.clear();
        applFormItems.addAll(JsonUtils.<ApplicationFormItemWithPrefilledValue>jsoAsList(jso));
        applFormGenerators.clear();

        if (applFormItems == null || applFormItems.isEmpty()) {

            // when there are no application form items
            FlexTable ft = new FlexTable();
            ft.setSize("100%", "300px");
            ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.errorIcon())+ApplicationMessages.INSTANCE.noFormDefined());
            ft.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
            ft.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);

            contents.setWidget(ft);

        } else if (PerunEntity.VIRTUAL_ORGANIZATION.equals(entity) && "EXTENSION".equalsIgnoreCase(type) && Location.getParameter("targetexisting") != null) {

            // FIXME - this is probably not good, since it prevents vo extension when targetexisting is specified.
            if (Location.getParameter("targetexisting") != null) {
                Location.replace(Location.getParameter("targetexisting"));
            }

            // when there are no application form items
            FlexTable ft = new FlexTable();
            ft.setSize("100%", "300px");
            ft.setHTML(0, 0, new Image(LargeIcons.INSTANCE.errorIcon())+ApplicationMessages.INSTANCE.alreadyVoMember());
            ft.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
            ft.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);

            contents.setWidget(ft);

        } else {

            // create application form
            prepareApplicationForm();

        }

        session.getUiElements().setLogText("Loading " + type + " application form items in selected VO finished:" + applFormItems.size());
        events.onFinished(jso);
        loaderImage.loadingFinished();

    }


    /**
     * Prepares the widgets from the items as A DISPLAY FOR THE USER
     */
    public void prepareApplicationForm(){

        FlexTable ft = new FlexTable();
        ft.setCellPadding(10);
        FlexCellFormatter fcf = ft.getFlexCellFormatter();
        String locale;

        if (!LocaleInfo.getCurrentLocale().getLocaleName().equals("cs")) {
            locale = "en";
        } else {
            locale = "cs";
        }

        int i = 0;
        for(final ApplicationFormItemWithPrefilledValue item : applFormItems){


            RegistrarFormItemGenerator gen = new RegistrarFormItemGenerator(item, locale);
            this.applFormGenerators.add(gen);
            gen.addValidationTrigger(new FormValidator() {

                public void triggerValidation() {
                    validateFormValues(false);
                }
            });


            // if button, add onclick
            if(item.getFormItem().getType().equals("SUBMIT_BUTTON")){

                this.sendButton = (CustomButton) gen.getWidget();
                sendButton.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {

                        // revalidate again, with force validation
                        if(!validateFormValues(true)){
                            Element elem = DOM.getElementById("input-status-error");
                            elem.scrollIntoView();
                            return;
                        }

                        if(sendFormHandler != null){
                            sendFormHandler.sendApplicationForm(sendButton);
                        }
                    }
                });
            }

            // get localized texts
            ItemTexts itemTexts = item.getFormItem().getItemTexts(locale);

            if(!gen.isVisible()){
                continue;
            }

            // WITH LABEL (input box ...)
            if(gen.isLabelShown()){

                // 0 = label
                if (item.getFormItem().isRequired() == true) {
                    // required
                    ft.setHTML(i, 0, "<strong>" + gen.getLabelOrShortname() + "*</strong>");
                } else {
                    // optional
                    ft.setHTML(i, 0, "<strong>" + gen.getLabelOrShortname() + "</strong>");
                }

                // 1 = widget
                Widget w = gen.getWidget();
                w.setTitle(itemTexts.getHelp());
                ft.setWidget(i, 1, w);

                // 2 = status
                ft.setWidget(i, 2, gen.getStatusWidget());

                // 3 = HELP
                if(itemTexts.getHelp() != null && itemTexts.getHelp().length() > 0)
                {
                    Label help = new Label(itemTexts.getHelp());
                    ft.setWidget(i, 3, help);
                }

                // format
                fcf.setStyleName(i, 0, "applicationFormLabel");
                fcf.setStyleName(i, 1, "applicationFormWidget");
                fcf.setStyleName(i, 2, "applicationFormCheck");
                fcf.setStyleName(i, 3, "applicationFormHelp");
                ft.setWidth("100%");




                // ELSE HTML COMMENT
            }else{

                ft.setWidget(i, 0, gen.getWidget());

                // colspan = 2
                fcf.setColSpan(i, 0, 4);
                fcf.setHorizontalAlignment(i, 0, HasHorizontalAlignment.ALIGN_LEFT);
                fcf.setVerticalAlignment(i, 0, HasVerticalAlignment.ALIGN_MIDDLE);

            }

            i++;

        }

        contents.setWidget(ft);
    }

    /**
     * Validates the form values
     */
    protected boolean validateFormValues(boolean forcedValidation) {

        if(sendButton == null) return false;

        boolean valid = true;

        sendButton.setEnabled(true);
        for(RegistrarFormItemGenerator gen : applFormGenerators){
            if(gen.getInputChecker().isValidating() || !gen.getInputChecker().isValid(forcedValidation)){
                sendButton.setEnabled(false);
                valid = false;

                if(!forcedValidation){
                    return false;
                }
            }
        }
        return valid;

    }

    /**
     * Generates the values from the form
     * @return
     */
    public ArrayList<ApplicationFormItemData> getValues()
    {
        ArrayList<ApplicationFormItemData> formItemDataList = new ArrayList<ApplicationFormItemData>();

        // goes through all the item generators and retrieves the value
        for(RegistrarFormItemGenerator gen : applFormGenerators){

            String value = gen.getValue();
            String prefilled = gen.getPrefilledValue();
            JSONObject formItemJSON = new JSONObject(gen.getFormItem());

            // remove text (locale), saves data transfer & removes problem with parsing locale
            formItemJSON.put("i18n", new JSONObject());

            // cast form item back
            ApplicationFormItem formItem = formItemJSON.getJavaScriptObject().cast();

            // prepare package with data
            ApplicationFormItemData data = ApplicationFormItemData.construct(formItem, formItem.getShortname(), value, prefilled, gen.getAssuranceLevel() != null ? gen.getAssuranceLevel() : "");

            formItemDataList.add(data);
        }
        return formItemDataList;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<ApplicationFormItemWithPrefilledValue> getList() {
        return this.applFormItems;
    }

    public void setSendFormHandler(SendsApplicationForm sendsApplicationForm) {
        this.sendFormHandler = sendsApplicationForm;
    }

    /**
     * Set callback as hidden (no popup on exception)
     * @param hidden true = hidden
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

}