package cz.metacentrum.perun.webgui.widgets;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Image;

/**
 * Custom button widget used in TabMenu
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @author Pavel Zlamal <2566627@mail.muni.cz>
 * @version $Id: 265b90fe70c5723a829fc56061806391a29dd697 $
 */
public class CustomButton extends Button {

	/**
	 * Button text
	 */
	private String text;
	
	/**
	 * Button image
	 */
	private Image image;

    /* If button is processing it's action */
    private boolean processing = false;
    /* action buttons do not have ellipsis (default) */
    private boolean isActionButton = true;

    private Image processingImage = new Image(AjaxLoaderImage.SMALL_IMAGE_URL);
    private Image backupImage;
    private String backupText;
    private boolean imageRight = false;


	/**
	 * Creates a new button
	 */
	public CustomButton() {
		super();
	}
	
	/**
	 * Creates a new button
	 * @param text
	 * @param image
	 */
	public CustomButton(String text, Image image)
	{
		this();
		this.image = image;
		this.setText(text);

	}
	
	/**
	 * Creates a new button
	 * @param text
	 * @param imgres
	 */
	public CustomButton(String text, ImageResource imgres){
		this(text, new Image(imgres));
	}
	
	/**
	 * Creates a new button
	 * @param text
	 * @param title
     * @param imgres
     */
	public CustomButton(String text, String title, ImageResource imgres){
		this(text, new Image(imgres));
		this.setTitle(title);
	}
	
	/**
	 * Creates a new button
	 * @param text
	 * @param image
     * @param clickHandler
	 */
	public CustomButton(String text, Image image, ClickHandler clickHandler){
		this(text, image);
		super.addClickHandler(clickHandler);
	}
	
	/**
	 * Creates a new button
	 * @param text
	 * @param imgres
	 * @param clickHandler
	 */
	public CustomButton(String text, ImageResource imgres, ClickHandler clickHandler){
		this(text, new Image(imgres), clickHandler);
	}

    /**
     * Creates a new button
     * @param text
     * @param title
     * @param imgres
     * @param clickHandler
     */
    public CustomButton(String text, String title, ImageResource imgres, ClickHandler clickHandler){
        this(text, new Image(imgres), clickHandler);
        this.setTitle(title);
    }
	
	/**
	 * Adds the image to the button
	 */
	private void updateImage() {

        if (this.isEnabled()) {
            image.getElement().removeClassName("customButtonImageDisabled");
            image.getElement().addClassName("customButtonImage");
        } else {
            image.getElement().removeClassName("customButtonImage");
            image.getElement().addClassName("customButtonImageDisabled");
        }

        if (imageRight) {
            DOM.appendChild(getElement(), image.getElement());
        } else {
            DOM.insertBefore(getElement(), image.getElement(), DOM.getFirstChild(getElement()));
        }

	}

    /**
     * Set new icon to the button
     */
    public void setIcon(ImageResource resource) {

        image = new Image(resource);
        updateImage();

    }

	/**
	 * Sets the text, if empty, look like "only image" icon
	 * @param text Text on the button
	 */
	@Override
	public void setText(String text) {
		
		DOM.setInnerHTML(getElement(), ""); // cleans the HTML

        if (!text.equals("")) {

            // set new text if present
            this.text = text;
            Element span = DOM.createElement("span");
            span.setInnerText(text);

            if (imageRight) {
                span.addClassName("customButtonTextLeft");
            } else {
                span.addClassName("customButtonText");
            }
            DOM.insertChild(getElement(), span, 0);

        }
        // refresh image HTML
		updateImage();
	}

	/**
	 * Returns the text on the button
	 * @return text on button
	 */
	@Override
	public String getText() {
		return this.text;
	}

    /**
     * Make also button image look like disabled
     * @param enabled
     */
    @Override
    public void setEnabled(boolean enabled) {

        // call super implementation
        super.setEnabled(enabled);
        // update image style along with text
        updateImage();

    }

    /**
     * Switch button to processing mode (with different text and image)
     * and back to normal
     *
     * @param processing TRUE = processing / FALSE = normal
     */
    public void setProcessing(boolean processing) {

        if (this.processing != processing) {

            this.processing = processing;

            if (processing) {

                backupText = this.getText();
                backupImage = this.image;

                image = processingImage;
                setText(backupText);

                setEnabled(false);

            } else {

                this.image = backupImage;
                setText(backupText);

                setEnabled(true);

            }

        }

    }

    /**
     * Set TRUE if image should be right of button text
     *
     * @param right TRUE = image on right / FALSE = image on left (default)
     */
    public void setImageAlign(boolean right) {
        imageRight = right;
        this.setText(this.getText());
    }

}