package cz.metacentrum.perun.webgui.widgets;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.*;

/**
 *  Extended SuggestBox widget which can validate it's content
 *  and display error message.
 *
 *  @author Pavel Zlamal <256627@mail.muni.cz>
 */
public class ExtendedSuggestBox extends Composite {

	private Label errorText = new Label();
	private SuggestBoxValidator validator;
	private SimplePanel sp = new SimplePanel();
	private UnaccentMultiWordSuggestOracle oracle = new UnaccentMultiWordSuggestOracle();
	private SuggestBox box = new PasteSuggestBox(oracle);

	private static int counter = 0;

	/**
	 * Create ExtendedTextBox
	 */
	public ExtendedSuggestBox() {
		this.initWidget(sp);
		buildWidget();
	}

	/**
	 * Create ExtendedTextBox with validator
	 */
	public ExtendedSuggestBox(SuggestBoxValidator validator) {
		this();
		this.validator = validator;
	}

	/**
	 * Create ExtendedTextBox with validator
	 */
	public ExtendedSuggestBox(UnaccentMultiWordSuggestOracle oracle) {
		this.initWidget(sp);
		box = new PasteSuggestBox(oracle);
		buildWidget();
	}

	/**
	 * Build widget itself
	 */
	private void buildWidget() {

		box.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (validator != null) {
					validator.validateSuggestBox();
				}
			}
		});

		box.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (validator != null) {
					validator.validateSuggestBox();
				}
			}
		});

		box.getValueBox().addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				if (validator != null) {
					validator.validateSuggestBox();
				}
			}
		});

		box.getElement().setClassName("gwt-SuggestBox suggestbox"+counter++);
		setCutCopyPasteHandler("suggestbox"+counter);

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
	 * Get SuggestionBox associated with ExtendedSuggestBoxWidgets
	 *
	 * @return SuggestBox
	 */
	public SuggestBox getSuggestBox() {
		return this.box;
	}

	/**
	 * Set suggestion oracle to suggest box
	 *
	 * @param oracle
	 */
	public void setSuggestOracle(UnaccentMultiWordSuggestOracle oracle) {
		this.oracle = oracle;
	}

	/**
	 * Get suggestion oracle from suggest box
	 *
	 * @return suggestion oracle
	 */
	public UnaccentMultiWordSuggestOracle getSuggestOracle() {
		return this.oracle;
	}


	/**
	 * Set custom content validator for ExtendedTextBox
	 *
	 * @param validator validator to set
	 */
	public void setValidator(SuggestBoxValidator validator) {
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
		 * Interface defining TextBoxValidator class
		 */
		public interface SuggestBoxValidator {

			/**
			 * Validate suggestbox content and make graphics changes if not valid
			 *
			 * @return TRUE - value in suggestbox is valid / FALSE - value is not valid (switch suggestbox display);
			 */
			public boolean validateSuggestBox();

		}

	private class PasteSuggestBox extends SuggestBox {

		public PasteSuggestBox() {
			super();
			sinkEvents(Event.ONPASTE);
		}

		public PasteSuggestBox(UnaccentMultiWordSuggestOracle oracle) {
			super(oracle);
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
							DomEvent.fireNativeEvent(Document.get().createKeyUpEvent(false, false, false, false, KeyCodes.KEY_DOWN), PasteSuggestBox.this);
						}
					});
					break;
			}
		}

	}


}
