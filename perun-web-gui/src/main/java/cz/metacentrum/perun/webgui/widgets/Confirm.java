package cz.metacentrum.perun.webgui.widgets;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.localization.ButtonTranslation;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;

/**
 * Custom confirm widget class
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class Confirm {

	/**
	 * Whether the dialog blocks the browser shortcut keys
	 */
	static private boolean BLOCK_KEYS = false;

	/**
	 * Whether show black page overlay
	 */
	static private boolean OVERLAY_ENABLED = true;


	/**
	 * The inner content
	 */
	private Widget content;

	/**
	 * The widget itself
	 */
	private DialogBox dialogBox;

	/**
	 * OK button text
	 */
	private String okButtonText = ButtonTranslation.INSTANCE.okButton();

	/**
	 * OK click event
	 */
	private ClickHandler okClickHandler = new ClickHandler() {
		public void onClick(ClickEvent event) {
			// do nothing
		}
	};

	/**
	 * Cancel button text
	 */
	private String cancelButtonText = ButtonTranslation.INSTANCE.cancelButton();

	/**
	 * Cancel click event
	 */
	private ClickHandler cancelClickHandler = new ClickHandler() {
		public void onClick(ClickEvent event) {
			// do nothing
		}
	};

	/**
	 * Build widget with Icons ??
	 */
	private boolean useIcons = false;
	private boolean autoHide = true;
	private boolean hideOnClick = true;


	/**
	 * Icons for OK / Cancel buttons
	 */
	private CustomButton okButton;
	private CustomButton cancelButton;
	private static final ImageResource OK_ICON = SmallIcons.INSTANCE.acceptIcon();
	private static final ImageResource CANCEL_ICON = SmallIcons.INSTANCE.stopIcon();
	private ImageResource okIcon;
	private ImageResource cancelIcon;

	// if OK button should be focused instead of cancel (by default).
	private boolean focusOkButton = false;

	/**
	 * Confirm box caption
	 */
	private String caption = "Confirmation";

	/**
	 * Value
	 */
	private boolean value = false;

	/**
	 * Event called
	 */
	private boolean eventCalled = false;

	/**
	 * Whether should the content be non scrollable
	 */
	private boolean nonScrollable = false;

	/**
	 * Confirm, which does nothing - alert
	 *
	 * @param caption
	 * @param content
	 */
	public Confirm(String caption, Widget content, boolean useIcons) {
		this.content = content;
		this.caption = caption;
		this.cancelButtonText = "";
		this.useIcons = useIcons;
	}

	/**
	 * Creates the new instance of the Confirm widget just with OK.
	 * Cancel does nothing.
	 *
	 * @param caption
	 * @param content
	 * @param okClickHandler
	 * @param useIcons (uses icons in widget) - if no icon set, use default
	 */
	public Confirm(String caption, Widget content, ClickHandler okClickHandler, boolean useIcons) {
		this.content = content;
		this.okClickHandler = okClickHandler;
		this.caption = caption;
		this.useIcons = useIcons;
	}

	/**
	 * Creates the new instance of the Confirm widget just with OK button and custom OK text.
	 * Cancel does nothing.
	 *
	 * @param caption
	 * @param content
	 * @param okClickHandler
	 * @param useIcons (uses icons in widget) - if no icon set, use default
	 */
	public Confirm(String caption, Widget content, ClickHandler okClickHandler, String okButtonText, boolean useIcons) {
		this.content = content;
		this.okClickHandler = okClickHandler;
		this.caption = caption;
		this.okButtonText = okButtonText;
		this.useIcons = useIcons;
	}

	/**
	 * Creates the new instance of the Confirm widget
	 * @param caption
	 * @param content
	 * @param okClickHandler
	 * @param cancelClickHandler
	 * @param useIcons (uses icons in widget) - if no icon set, use default
	 */
	public Confirm(String caption, Widget content, ClickHandler okClickHandler, ClickHandler cancelClickHandler, boolean useIcons) {
		this.content = content;
		this.okClickHandler = okClickHandler;
		this.cancelClickHandler = cancelClickHandler;
		this.caption = caption;
		this.useIcons = useIcons;
	}

	/**
	 * Creates the new instance of the Confirm widget
	 * @param caption
	 * @param content
	 * @param okButton
	 * @param cancelButton
	 * @param useIcons (uses icons in widget) - if no icon set, use default
	 */
	public Confirm(String caption, Widget content, CustomButton okButton, CustomButton cancelButton, boolean useIcons) {
		this.content = content;
		this.okButton = okButton;
		this.cancelButton = cancelButton;
		this.caption = caption;
		this.useIcons = useIcons;
	}

	/**
	 * Creates the new instance of the Confirm widget with custom button labels
	 * @param caption
	 * @param content
	 * @param okClickHandler
	 * @param cancelClickHandler
	 * @param okButtonText
	 * @param cancelButtonText
	 * @param useIcons (uses icons in widget) - if no icon set, use default
	 */
	public Confirm(String caption, Widget content, ClickHandler okClickHandler, ClickHandler cancelClickHandler, String okButtonText, String cancelButtonText, boolean useIcons) {
		this.content = content;
		this.okButtonText = okButtonText;
		this.cancelButtonText = cancelButtonText;
		this.okClickHandler = okClickHandler;
		this.cancelClickHandler = cancelClickHandler;
		this.caption = caption;
		this.useIcons = useIcons;
	}

	/**
	 * @return the content
	 */
	public Widget getContent() {
		return content;
	}

	/**
	 * @return the okButtonText
	 */
	public String getOkButtonText() {
		return okButtonText;
	}

	/**
	 * @return the cancelButtonText
	 */
	public String getCancelButtonText() {
		return cancelButtonText;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setContent(Widget content) {
		this.content = content;
	}

	/**
	 * @param okButtonText
	 *            the okButtonText to set
	 */
	public void setOkButtonText(String okButtonText) {
		this.okButtonText = okButtonText;
	}

	/**
	 * @return the okClickHandler
	 */
	public ClickHandler getOkClickHandler() {
		return okClickHandler;
	}

	/**
	 * @return the cancelClickHandler
	 */
	public ClickHandler getCancelClickHandler() {
		return cancelClickHandler;
	}

	/**
	 * @param okClickHandler the okClickHandler to set
	 */
	public void setOkClickHandler(ClickHandler okClickHandler) {
		this.okClickHandler = okClickHandler;
	}

	/**
	 * @param cancelClickHandler the cancelClickHandler to set
	 */
	public void setCancelClickHandler(ClickHandler cancelClickHandler) {
		this.cancelClickHandler = cancelClickHandler;
	}

	/**
	 * @param cancelButtonText
	 *            the cancelButtonText to set
	 */
	public void setCancelButtonText(String cancelButtonText) {
		this.cancelButtonText = cancelButtonText;
	}

	/**
	 * @param okIcon
	 */
	public void setOkIcon(ImageResource okIcon) {
		this.okIcon = okIcon;
	}

	/**
	 * @param cancelIcon
	 */
	public void setCancelIcon(ImageResource cancelIcon) {
		this.cancelIcon = cancelIcon;
	}

	public boolean isFocusOkButton() {
		return focusOkButton;
	}

	/**
	 * Set to TRUE if OK button should be focused instead of cancel (default action).
	 *
	 * If OK button is only one in Confirm widget, then it's focused without
	 * regarding this value.
	 *
	 * @param focusOkButton TRUE to focus OK button
	 */
	public void setFocusOkButton(boolean focusOkButton) {
		this.focusOkButton = focusOkButton;
	}

	/**
	 * @return the value
	 */
	public boolean getValue() {
		return this.value;
	}

	/**
	 * Shows the confirm window
	 */
	public void show() {
		this.eventCalled = false;
		this.buildWidget();
		dialogBox.show();

		// focus on default action
		Scheduler.get().scheduleDeferred(new Command() {
			@Override
			public void execute() {
				if (cancelButton != null) cancelButton.setFocus(true);
				if ((cancelButton == null && okButton != null) || focusOkButton) okButton.setFocus(true);
			}
		});

	}


	/**
	 * Whether should be the content non-scrollable
	 * @param nonScrollable
	 */
	public void setNonScrollable(boolean nonScrollable) {
		this.nonScrollable = nonScrollable;
	}


	/**
	 * Creates and displays the widget
	 */
	public void buildWidget() {
		// menu panel
		TabMenu menu = new TabMenu();

		if (okButton != null) {

			menu.addWidget(okButton);

		} else {

			// OK BUTTON
			if(this.okButtonText.length() > 0) {

				// build with icons
				if (useIcons == true) {
					// if no icon set - use default
					if (okIcon == null) { okIcon = OK_ICON; }

					okButton = new CustomButton(this.okButtonText, okIcon, new ClickHandler() {
						public void onClick(ClickEvent event) {
							value = true;
							okClickHandler.onClick(event);
							eventCalled = true;
							if (hideOnClick) dialogBox.hide();
						}
					});

					// build without icons
				} else {

					okButton = new CustomButton(this.okButtonText, "", null, new ClickHandler() {
						public void onClick(ClickEvent event) {
							value = true;
							okClickHandler.onClick(event);
							eventCalled = true;
							if (hideOnClick) dialogBox.hide();
						}
					});

				}

				menu.addWidget(okButton);

			}

		}

		if (cancelButton != null) {

			menu.addWidget(cancelButton);

		} else {

			// CANCEL BUTTON
			if(this.cancelButtonText.length() > 0) {

				// build with icons
				if (useIcons == true ) {
					// if no icon set - use default
					if (cancelIcon == null) { cancelIcon = CANCEL_ICON; }
					cancelButton = new CustomButton(this.cancelButtonText, cancelIcon, new ClickHandler() {
						public void onClick(ClickEvent event) {
							value = false;
							cancelClickHandler.onClick(event);
							eventCalled = true;
							if (hideOnClick) dialogBox.hide();
						}
					});
					// build without icons
				} else {
					cancelButton = new CustomButton(this.cancelButtonText, "", null, new ClickHandler() {
						public void onClick(ClickEvent event) {
							value = false;
							cancelClickHandler.onClick(event);
							eventCalled = true;
							if (hideOnClick) dialogBox.hide();
						}
					});
				}

				menu.addWidget(cancelButton);

			}

		}

		// widget panel
		VerticalPanel dialogBoxInside = new VerticalPanel();

		final Widget contentWrapper;
		if (nonScrollable) {
			contentWrapper = new SimplePanel(this.content);
		} else {
			contentWrapper = new ScrollPanel(this.content);
		}

		dialogBoxInside.add(contentWrapper);
		dialogBoxInside.add(menu);
		dialogBoxInside.setCellHorizontalAlignment(menu, HasHorizontalAlignment.ALIGN_RIGHT);

		dialogBox = new DialogBox(true);
		dialogBox.setModal(BLOCK_KEYS);
		dialogBox.setGlassEnabled(OVERLAY_ENABLED);
		dialogBox.setAutoHideEnabled(autoHide);

		// on close call cancel
		dialogBox.addCloseHandler(new CloseHandler<PopupPanel>() {

			public void onClose(CloseEvent<PopupPanel> event) {
				// if event not called already
				if(!eventCalled){
					cancelClickHandler.onClick(null);
				}
			}
		});

		dialogBox.ensureDebugId("cwDialogBox");
		dialogBox.setWidget(dialogBoxInside);
		dialogBox.setText(caption);
		dialogBox.center();

		// generates the resize command
		Command c = new Command() {
			public void execute() {

				if(content.getParent() == null) return;

				if(!nonScrollable){
					if (content.getOffsetHeight() > 250) {
						contentWrapper.setHeight("250px");
					} else {
						contentWrapper.setHeight(content.getOffsetHeight()+"px");
					}
				}
				dialogBox.center();
			}
		};
		Scheduler.get().scheduleDeferred(c);

	}

	/**
	 * Hide the box
	 */
	public void hide() {
		dialogBox.hide();
	}

	/**
	 * Set TRUE if confirm should autohide (perform cancel action)
	 * if user click outside of the box.
	 *
	 * @param autoHide TRUE to autohide / FALSE otherwise
	 */
	public void setAutoHide(boolean autoHide) {
		this.autoHide = autoHide;
	}

	public CustomButton getOkButton() {
		return okButton;
	}

	public CustomButton getCancelButton() {
		return cancelButton;
	}

	/**
	 * TRUE if dialog box is currently displayed,
	 * FALSE otherwise.
	 *
	 * @return
	 */
	public boolean isShowing() {

		if (dialogBox == null) return false;
		return this.dialogBox.isShowing();
	}

	/**
	 * TRUE if dialog box should hide on
	 * clicking any button, FALSE otherwise.
	 *
	 * @param hide TRUE to hide confirm widget on button click
	 */
	public void setHideOnButtonClick(boolean hide) {
		this.hideOnClick = hide;
	}

}
