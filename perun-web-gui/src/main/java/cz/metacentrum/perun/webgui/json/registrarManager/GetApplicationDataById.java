package cz.metacentrum.perun.webgui.json.registrarManager;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.applicationresources.RegistrarFormItemGenerator;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallback;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.JsonClient;
import cz.metacentrum.perun.webgui.json.JsonUtils;
import cz.metacentrum.perun.webgui.model.ApplicationFormItemData;
import cz.metacentrum.perun.webgui.model.Attribute;
import cz.metacentrum.perun.webgui.model.PerunError;
import cz.metacentrum.perun.webgui.model.User;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;
import cz.metacentrum.perun.webgui.widgets.Confirm;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Returns data sent by user in the form elements in application form
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class GetApplicationDataById implements JsonCallback {

  // JSON URL
  static private final String JSON_URL = "registrarManager/getApplicationDataById";
  static private final String JSON_URL_GET_USER = "usersManager/getRichUserWithAttributes";
  // Session
  private PerunWebSession session = PerunWebSession.getInstance();
  // VO id
  private int appId;
  // External events
  private JsonCallbackEvents events = new JsonCallbackEvents();

  // Loader image
  private AjaxLoaderImage loaderImage = new AjaxLoaderImage();

  // Content
  private SimplePanel contents = new SimplePanel();

  // list
  private ArrayList<ApplicationFormItemData> applFormItems = new ArrayList<ApplicationFormItemData>();

  // RegistrarFormItemGenerators for getting values
  private ArrayList<RegistrarFormItemGenerator> applFormGenerators = new ArrayList<RegistrarFormItemGenerator>();

  // whether to show hidden information
  private boolean showAdminItems = true;

  private boolean editable = false;

  private int userId;

  private User user;

  private boolean embedded = false;

  /**
   * Creates a new method instance
   *
   * @param id APPLICATION ID
   */
  public GetApplicationDataById(int id) {
    this.appId = id;
    this.contents.setWidget(loaderImage);
  }

  /**
   * Creates a new method instance
   *
   * @param id     APPLICATION ID
   * @param events Custom events
   */
  public GetApplicationDataById(int id, JsonCallbackEvents events) {
    this(id);
    this.events = events;
  }

  public void setEditable(boolean editable) {
    this.editable = editable;
  }

  public void setEmbedded(boolean embedded, User user) {
    this.embedded = embedded;
    this.userId = (user != null) ? user.getId() : 0;
  }

  /**
   * Retrieve data from RPC
   */
  public void retrieveData() {
    String param = "id=" + this.appId;
    JsonClient js = new JsonClient();

    if (this.embedded) {
      js.retrieveData(JSON_URL_GET_USER, "user=" + this.userId, this);
    } else {
      js.retrieveData(JSON_URL, param, this);
    }
  }

  /**
   * Returns contents
   */
  public Widget getContents() {
    return this.contents;
  }


  /**
   * Called when an error occurs.
   */
  public void onError(PerunError error) {
    session.getUiElements().setLogErrorText("Error while loading application form items.");
    events.onError(error);
    loaderImage.loadingError(error);
  }

  /**
   * Called when loading starts.
   */
  public void onLoadingStart() {
    events.onLoadingStart();
  }

  /**
   * Called when loading successfully finishes.
   */
  public void onFinished(JavaScriptObject jso) {

    applFormItems.clear();
    if (!this.embedded) {
      applFormItems.addAll(JsonUtils.<ApplicationFormItemData>jsoAsList(jso));
      // sort by form ordnum (or shortname for delted items)
      Collections.sort(applFormItems, new Comparator<ApplicationFormItemData>() {
        public int compare(ApplicationFormItemData arg0, ApplicationFormItemData arg1) {
          if (arg0.getFormItem() != null && arg1.getFormItem() != null) {
            return arg0.getFormItem().getOrdnum() - arg1.getFormItem().getOrdnum();
          } else {
            // for old data with deleted form items
            return arg0.getShortname().compareTo(arg1.getShortname());
          }
        }
      });
    } else {
      this.user = jso.cast();
    }

    applFormGenerators.clear();

    prepareApplicationForm();

    session.getUiElements()
        .setLogText("Loading application form items in selected VO finished:" + applFormItems.size());
    events.onFinished(jso);
    loaderImage.setEmptyResultMessage("Application doesn't contain any data.");
    loaderImage.loadingFinished();

  }


  /**
   * Prepares the widgets from the items as A DISPLAY FOR THE USER
   */
  public void prepareApplicationForm() {

    FlexTable ft = new FlexTable();
    ft.setWidth("100%");
    ft.setCellPadding(5);
    ft.setCellSpacing(0);
    ft.setStyleName("stripped");
    FlexCellFormatter fcf = ft.getFlexCellFormatter();
    String locale = "en";
    if (!Utils.getNativeLanguage().isEmpty() &&
        !LocaleInfo.getCurrentLocale().getLocaleName().equals("default") &&
        !LocaleInfo.getCurrentLocale().getLocaleName().equals("en")) {
      locale = Utils.getNativeLanguage().get(0);
    }

    if (this.embedded) {
      Attribute attr = this.user.getAttribute("urn:perun:user:attribute-def:def:preferredMail");
      if (attr != null) {
        ft.setHTML(0, 0, "<strong>" + attr.getDisplayName() + "</strong>");
        ft.setWidget(0, 2, new HTML(SafeHtmlUtils.fromString(attr.getValue())));

        // format
        fcf.setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
        fcf.setVerticalAlignment(0, 2, HasVerticalAlignment.ALIGN_TOP);
        fcf.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_RIGHT);
        fcf.setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_LEFT);
        fcf.setWidth(0, 0, "25%");
        fcf.setWidth(0, 2, "75%");
        contents.setWidget(ft);
      }
    } else {
      int i = 0;
      for (final ApplicationFormItemData item : applFormItems) {

        RegistrarFormItemGenerator gen = new RegistrarFormItemGenerator(item.getFormItem(),
            (item.getValue() != null) ? SafeHtmlUtils.fromString(item.getValue()).asString() : null, locale);
        this.applFormGenerators.add(gen);

        // show only visible items - show also hidden to perun admin and vo/group admin
        if (!gen.isVisible() && !(session.isPerunAdmin() || session.isVoAdmin() || session.isGroupAdmin())) {
          continue;
        }

        // if only for admin
        if (!showAdminItems && gen.isVisibleOnlyToAdmin()) {
          continue;
        }


        //ItemTexts itemTexts = item.getFormItem().getItemTexts(locale);

        // WITH LABEL (input box ...)
        if (gen.isLabelShown()) {

          // don't show password
          if (!item.getFormItem().getType().equalsIgnoreCase("PASSWORD")) {

            // 0 = label or shortname
            // FIXME for now, we can only detect that the item was hidden for the 'ALWAYS' option. This will be for the most cases.
            if (item.getFormItem().getHidden().equals("ALWAYS")) {
              // hidden
              ft.setHTML(i, 0, "<strong>" + SafeHtmlUtils.fromString(gen.getLabelOrShortname()).asString() +
                  "</strong><br /><i>(value provided by external source)</i>");
              // FIXME for now, we can only detect that the item was disabled for the 'ALWAYS' option. This will be for the most cases.
            } else if (item.getFormItem().getDisabled().equals("ALWAYS")) {
              // show
              ft.setHTML(i, 0, "<strong>" + SafeHtmlUtils.fromString(gen.getLabelOrShortname()).asString() +
                  "</strong><br /><i>(value provided by external source)</i>");
            } else {
              ft.setHTML(i, 0,
                  "<strong>" + SafeHtmlUtils.fromString(gen.getLabelOrShortname()).asString() + "</strong>");
            }

            if (PerunWebSession.getInstance().isPerunAdmin() && gen.isUpdatable() && editable) {
              final int finalI = i;
              CustomButton editButton =
                  new CustomButton("", "Editor form item data.", SmallIcons.INSTANCE.applicationFormEditIcon(),
                      new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {

                          TextArea content = new TextArea();
                          if (item.getValue() != null) {
                            content.setText(item.getValue());
                          }
                          content.setSize("350px", "100px");

                          Confirm confirm = new Confirm(
                              "Edit item: " + SafeHtmlUtils.fromString(gen.getLabelOrShortname()).asString(), content,
                              new ClickHandler() {
                                @Override
                                public void onClick(ClickEvent event) {
                                  // save old value and push new value
                                  String previousValue = item.getValue();
                                  item.setValue(content.getText());
                                  UpdateFormItemData update = new UpdateFormItemData(new JsonCallbackEvents() {
                                    @Override
                                    public void onFinished(JavaScriptObject jso) {
                                      // update local UI
                                      ft.setWidget(finalI, 2, new HTML((item.getValue() != null) ?
                                          (SafeHtmlUtils.fromString(item.getValue()).asString()) : null));
                                    }

                                    @Override
                                    public void onError(PerunError error) {
                                      // put back old value
                                      item.setValue(previousValue);
                                    }
                                  });
                                  update.updateFormItemData(appId, item);
                                }
                              }, "Update form item", true);
                          confirm.setNonScrollable(true);
                          confirm.show();

                        }
                      });
              ft.setWidget(i, 1, editButton);
            }

            // 1 = value
            ft.setWidget(i, 2,
                new HTML((item.getValue() != null) ? (SafeHtmlUtils.fromString(item.getValue()).asString()) : null));

            // format
            fcf.setVerticalAlignment(i, 0, HasVerticalAlignment.ALIGN_TOP);
            fcf.setVerticalAlignment(i, 2, HasVerticalAlignment.ALIGN_TOP);
            fcf.setHorizontalAlignment(i, 0, HasHorizontalAlignment.ALIGN_RIGHT);
            fcf.setHorizontalAlignment(i, 2, HasHorizontalAlignment.ALIGN_LEFT);
            fcf.setWidth(i, 0, "25%");
            fcf.setWidth(i, 2, "75%");

          }

        }

        i++;

      }

      // set empty text
      if (!applFormItems.isEmpty()) {
        contents.setWidget(ft);
      }
    }


  }

  public ArrayList<ApplicationFormItemData> getList() {
    return this.applFormItems;
  }

  public void setShowAdminItems(boolean b) {
    this.showAdminItems = b;
  }

}
