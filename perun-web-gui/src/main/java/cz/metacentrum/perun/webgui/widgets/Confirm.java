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
	
	/**
	 * Icons for OK / Cancle buttons
	 */
	private static final ImageResource OK_ICON = SmallIcons.INSTANCE.acceptIcon();
	private static final ImageResource CANCEL_ICON = SmallIcons.INSTANCE.stopIcon();
	private ImageResource okIcon;
	private ImageResource cancelIcon;
	
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
	
	/**
	 * @return the value
	 */
	public boolean getValue(){
		return this.value;
	}
	
	/**
	 * Shows the confirm window
	 */
	public void show()
	{
		this.eventCalled = false;
		this.buildWidget();
	}
	
	
	/**
	 * Whether should be the content non-scrollable 
	 * @param nonScrollable
	 */
	public void setNonScrollable(boolean nonScrollable)
	{
		this.nonScrollable = nonScrollable;
	}
	
	
	/**
	 * Creates and displays the widget
	 */
	private void buildWidget()
	{
		// menu panel
		TabMenu menu = new TabMenu();

		// OK BUTTON
		if(this.okButtonText.length() > 0)
		{
			// build with icons
			if (useIcons == true) {
				// if no icon set - use default
				if (okIcon == null) { okIcon = OK_ICON; }
				
				final Button btn = new CustomButton(this.okButtonText, okIcon, new ClickHandler() {
					public void onClick(ClickEvent event) {
						value = true;
						okClickHandler.onClick(event);
						eventCalled = true;
						dialogBox.hide();
					}
				});
				// disable on click
				btn.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						btn.setEnabled(false);
					}
				});

                menu.addWidget(btn);
			// build without icons
			} else {
				
				final Button btn = new Button(this.okButtonText, new ClickHandler() {
					public void onClick(ClickEvent event) {
						value = true;
						okClickHandler.onClick(event);
						eventCalled = true;
						dialogBox.hide();
					}
				});

                menu.addWidget(btn);
				
				// disable on click
				btn.addClickHandler(new ClickHandler() {
					public void onClick(ClickEvent event) {
						btn.setEnabled(false);
					}
				});

			}
		}
		
		// CANCEL BUTTON
		if(this.cancelButtonText.length() > 0)
		{
			// build with icons
			if (useIcons == true ) {
				// if no icon set - use default
				if (cancelIcon == null) { cancelIcon = CANCEL_ICON; }
                menu.addWidget(new CustomButton(this.cancelButtonText, cancelIcon, new ClickHandler() {
					public void onClick(ClickEvent event) {
						value = false;
						cancelClickHandler.onClick(event);
						eventCalled = true;
						dialogBox.hide();
					}
				}));
			// build without icons	
			} else {
                menu.addWidget(new Button(this.cancelButtonText, new ClickHandler() {
					public void onClick(ClickEvent event) {
						value = false;
						cancelClickHandler.onClick(event);
						eventCalled = true;
						dialogBox.hide();
					}
				}));
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
		dialogBox.show();
		
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

}