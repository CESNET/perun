package cz.metacentrum.perun.webgui.widgets;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;

/**
 *  Extended TextArea widget which can validate it's content
 *  and display error message.
 *
 *  @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ExtendedTextArea extends Composite {

	private Label errorText = new Label();
	private TextAreaValidator validator;
	private SimplePanel sp = new SimplePanel();
	private TextArea box = new PasteTextArea();


	private static int counter = 0;

	/**
	 * Create ExtendedTextBox
	 */
	public ExtendedTextArea() {
		this.initWidget(sp);
		buildWidget();
	}

	/**
	 * Create ExtendedTextBox with validator
	 */
	public ExtendedTextArea(TextAreaValidator validator) {
		this();
		this.validator = validator;
	}

	/**
	 * Build widget itself
	 */
	private void buildWidget() {

		box.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (validator != null) {
					validator.validateTextArea();
				}
			}
		});

		box.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (validator != null) {
					validator.validateTextArea();
				}
			}
		});

		box.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				if (validator != null) {
					validator.validateTextArea();
				}
			}
		});

		box.getElement().setClassName("textarea"+counter++);
		setCutCopyPasteHandler("textarea"+counter);

		errorText.setVisible(false);
		errorText.setStyleName("inputFormInlineComment");
		errorText.addStyleName("serverResponseLabelError");
		errorText.addStyleName("input-status-error-padding");

		sp.setWidget(box);
		sp.getElement().appendChild(errorText.getElement());

	}

	/**
	 * Sets TextBox to OK state (hide any error message)
	 */
	public void setOk() {

		errorText.setVisible(false);

		box.removeStyleName("input-text-error-border");

		sp.setWidget(box);
		sp.getElement().appendChild(errorText.getElement());

	}

	/**
	 * Set TextBox to error state and display custom message under TextBox.
	 *
	 * @param message message to display
	 */
	public void setError(String message) {

		if (message != null && !message.isEmpty()) {
			errorText.getElement().setInnerHTML(message);
			errorText.setVisible(true);
		} else {
			errorText.getElement().setInnerHTML("");
			errorText.setVisible(false);
		}

		// set error message max-width based on size of box
		int width = box.getOffsetWidth();
		errorText.getElement().setAttribute("style", errorText.getElement().getAttribute("style")+" max-width: "+width+"px;");

		box.addStyleName("input-text-error-border");

		sp.setWidget(box);
		sp.getElement().appendChild(errorText.getElement());

	}

	/**
	 * Get TextBox associated with ExtendedTextBoxWidgets
	 *
	 * @return TextBox
	 */
	public TextArea getTextArea() {
		return this.box;
	}

	/**
	 * Set custom content validator for ExtendedTextBox
	 *
	 * @param validator validator to set
	 */
	public void setValidator(TextAreaValidator validator) {
		this.validator = validator;
	}

	/**
	 * Set copy & cut & paste javascript handlers to textbox by class
	 *
	 * @param id class of textbox to assign handlers to
	 */
	private final native void setCutCopyPasteHandler(String id) /*-{
				$wnd.jQuery.ready(function() {
		$wnd.jQuery('#'+id).bind('cut', function(e) {
		$wnd.jQuery('#'+id).onkeyup()
		});
		$wnd.jQuery('#'+id).bind('copy', function(e) {
		$wnd.jQuery('#'+id).onkeyup()
		});
		$wnd.jQuery('#'+id).bind('paste', function(e) {
		$wnd.jQuery('#'+id).onkeyup()
		});
		});
			}-*/;


		/**
		 * Interface defining TextAreaValidator class
		 */
		public interface TextAreaValidator {

			/**
			 * Validate TextArea content and make graphics changes if not valid
			 *
			 * @return TRUE - value in text area is valid / FALSE - value is not valid (switch text area display);
			 */
			public boolean validateTextArea();

		}

	private class PasteTextArea extends TextArea {

		public PasteTextArea() {
			super();
			sinkEvents(Event.ONPASTE);
		}

		@Override
		public void onBrowserEvent(Event event) {
			super.onBrowserEvent(event);
			switch (DOM.eventGetType(event)) {
				case Event.ONPASTE:
					Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
						@Override
						public void execute() {
							ValueChangeEvent.fire(PasteTextArea.this, getText());
						}
					});
					break;
			}
		}

	}


}
