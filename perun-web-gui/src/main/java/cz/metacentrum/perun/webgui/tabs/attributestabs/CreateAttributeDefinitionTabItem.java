package cz.metacentrum.perun.webgui.tabs.attributestabs;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import cz.metacentrum.perun.webgui.client.PerunWebSession;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.ButtonType;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;
import cz.metacentrum.perun.webgui.json.JsonCallbackEvents;
import cz.metacentrum.perun.webgui.json.attributesManager.CreateAttribute;
import cz.metacentrum.perun.webgui.tabs.TabItem;
import cz.metacentrum.perun.webgui.widgets.CustomButton;
import cz.metacentrum.perun.webgui.widgets.ExtendedTextBox;
import cz.metacentrum.perun.webgui.widgets.TabMenu;

/**
 * Create attribute definition form
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class CreateAttributeDefinitionTabItem implements TabItem {

  private final CheckBox unique = new CheckBox();
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
  private Label titleWidget = new Label("Create attribute definition");
  private ButtonTranslation buttonTranslation = ButtonTranslation.INSTANCE;

  /**
   * Creates a tab instance
   */
  public CreateAttributeDefinitionTabItem() {
  }

  public boolean isPrepared() {
    return true;
  }

  @Override
  public boolean isRefreshParentOnClose() {
    return false;
  }

  @Override
  public void onClose() {

  }

  public Widget draw() {

    VerticalPanel vp = new VerticalPanel();
    vp.setSize("100%", "100%");

    // creates HTML elements
    final ExtendedTextBox attributeDisplayName = new ExtendedTextBox();
    final ExtendedTextBox attributeName = new ExtendedTextBox();
    final ExtendedTextBox attributeDescription = new ExtendedTextBox();

    final ExtendedTextBox.TextBoxValidator nameValidator = new ExtendedTextBox.TextBoxValidator() {
      @Override
      public boolean validateTextBox() {
        if (attributeName.getTextBox().getText().trim().isEmpty()) {
          attributeName.setError("Name of attribute can't be empty.");
        } else if (!attributeName.getTextBox().getText().trim().matches(Utils.ATTRIBUTE_FRIENDLY_NAME_MATCHER)) {
          attributeName.setError("Name of attribute can contain only letters, numbers, dash and colon.");
        } else {
          attributeName.setOk();
          return true;
        }
        return false;
      }
    };

    final ExtendedTextBox.TextBoxValidator descriptionValidator = new ExtendedTextBox.TextBoxValidator() {
      @Override
      public boolean validateTextBox() {
        if (!attributeDescription.getTextBox().getText().trim().isEmpty()) {
          attributeDescription.setOk();
          return true;
        } else {
          attributeDescription.setError("Description of attribute can't be empty.");
          return false;
        }
      }
    };

    final ExtendedTextBox.TextBoxValidator displayNameValidator = new ExtendedTextBox.TextBoxValidator() {
      @Override
      public boolean validateTextBox() {
        if (!attributeDisplayName.getTextBox().getText().trim().isEmpty()) {
          attributeDisplayName.setOk();
          return true;
        } else {
          attributeDisplayName.setError("Display name of attribute can't be empty.");
          return false;
        }
      }
    };

    attributeName.setValidator(nameValidator);
    attributeDisplayName.setValidator(displayNameValidator);
    attributeDescription.setValidator(descriptionValidator);

    final ListBox entityListBox = new ListBox();
    final ListBox definitionListBox = new ListBox();
    final ListBox typeListBox = new ListBox();

    // fill listboxs with pre-defined values
    entityListBox.addItem("facility", "urn:perun:facility:");
    entityListBox.addItem("resource", "urn:perun:resource:");
    entityListBox.addItem("group", "urn:perun:group:");
    entityListBox.addItem("group_resource", "urn:perun:group_resource:");
    entityListBox.addItem("host", "urn:perun:host:");
    entityListBox.addItem("member", "urn:perun:member:");
    entityListBox.addItem("member_group", "urn:perun:member_group:");
    entityListBox.addItem("member_resource", "urn:perun:member_resource:");
    entityListBox.addItem("user", "urn:perun:user:");
    entityListBox.addItem("user_ext_source", "urn:perun:ues:");
    entityListBox.addItem("user_facility", "urn:perun:user_facility:");
    entityListBox.addItem("vo", "urn:perun:vo:");
    entityListBox.addItem("entityless", "urn:perun:entityless:");

    definitionListBox.addItem("def", "attribute-def:def");
    definitionListBox.addItem("opt", "attribute-def:opt");
    definitionListBox.addItem("virt", "attribute-def:virt");
    definitionListBox.addItem("core", "attribute-def:core");

    typeListBox.addItem("String", "java.lang.String");
    typeListBox.addItem("Integer", "java.lang.Integer");
    typeListBox.addItem("Boolean", "java.lang.Boolean");
    typeListBox.addItem("Array", "java.util.ArrayList");
    typeListBox.addItem("LinkedHashMap", "java.util.LinkedHashMap");

    // prepare layout for this tab
    FlexTable layout = new FlexTable();
    layout.setStyleName("inputFormFlexTable");
    FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();
    TabMenu menu = new TabMenu();

    // BUTTONS

    final CustomButton createButton =
        TabMenu.getPredefinedButton(ButtonType.CREATE, buttonTranslation.createAttributeDefinition());
    menu.addWidget(createButton);

    // close tab events & enable, disable buttons
    final JsonCallbackEvents closeTabEvents = JsonCallbackEvents.closeTabDisableButtonEvents(createButton, this, true);

    createButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {

        if (nameValidator.validateTextBox() && descriptionValidator.validateTextBox() &&
            displayNameValidator.validateTextBox()) {

          String displayName = attributeDisplayName.getTextBox().getText().trim();
          String friendlyName = attributeName.getTextBox().getText().trim();
          String description = attributeDescription.getTextBox().getText().trim();
          String namespace = entityListBox.getValue(entityListBox.getSelectedIndex()) +
              definitionListBox.getValue(definitionListBox.getSelectedIndex());
          String type = typeListBox.getValue(typeListBox.getSelectedIndex());
          boolean isUnique = unique.getValue();

          CreateAttribute request =
              new CreateAttribute(JsonCallbackEvents.disableButtonEvents(createButton, new JsonCallbackEvents() {
                @Override
                public void onFinished(JavaScriptObject jso) {
                  closeTabEvents.onFinished(jso);
                }
              }));
          request.createAttributeDefinition(displayName, friendlyName, description, namespace, type, isUnique);

        }
      }
    });

    // insert layout

    layout.setHTML(0, 0, "Friendly name:");
    layout.setWidget(0, 1, attributeName);
    layout.setHTML(1, 0, "Display name:");
    layout.setWidget(1, 1, attributeDisplayName);
    layout.setHTML(2, 0, "Description:");
    layout.setWidget(2, 1, attributeDescription);
    layout.setHTML(3, 0, "Entity:");
    layout.setWidget(3, 1, entityListBox);
    layout.setHTML(4, 0, "Definition type:");
    layout.setWidget(4, 1, definitionListBox);
    layout.setHTML(5, 0, "Value type:");
    layout.setWidget(5, 1, typeListBox);
    layout.setHTML(6, 0, "Unique:");
    layout.setWidget(6, 1, unique);

    for (int i = 0; i < layout.getRowCount(); i++) {
      cellFormatter.addStyleName(i, 0, "itemName");
    }

    final TabItem tab = this;

    menu.addWidget(TabMenu.getPredefinedButton(ButtonType.CANCEL, "", new ClickHandler() {
      @Override
      public void onClick(ClickEvent clickEvent) {
        session.getTabManager().closeTab(tab, isRefreshParentOnClose());
      }
    }));

    String newGuiAlertContent = session.getNewGuiAlert();
    final FlexTable alert = new FlexTable();
    String alertText =
        "<p>Setting attribute rights is no longer supported in this GUI. In order to set attribute rights please use the New GUI.</p> ";
    if (newGuiAlertContent != null && !newGuiAlertContent.isEmpty()) {
      alertText += newGuiAlertContent;
    }
    alert.setHTML(0, 0, alertText);

    vp.add(layout);
    vp.add(alert);
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
    final int prime = 569;
    int result = 1;
    result = prime * result + 135345;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }

    return true;
  }

  public boolean multipleInstancesEnabled() {
    return false;
  }

  public void open() {
  }

  public boolean isAuthorized() {

    if (session.isPerunAdmin()) {
      return true;
    } else {
      return false;
    }

  }

}
