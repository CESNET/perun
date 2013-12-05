package cz.metacentrum.perun.webgui.widgets;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import cz.metacentrum.perun.webgui.client.localization.WidgetTranslation;
import cz.metacentrum.perun.webgui.model.PerunError;

/**
 * Custom GWT widget for ajax loader image.
 * 
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @version $Id: 67ed94e30cb88ef5798a46940f7bd07d2d260404 $
 */
public class AjaxLoaderImage extends Composite {

	static public final String IMAGE_URL = "img/ajax-loader.gif";
	static public final String SMALL_IMAGE_URL = "img/ajax-loader-small.gif";
	
	private final String NO_DATA = WidgetTranslation.INSTANCE.noItemsFound();
	private final String ERROR = WidgetTranslation.INSTANCE.requestTimeout();
	private final String RESPOND_ERROR = WidgetTranslation.INSTANCE.serverRespondedWithError();
	private final String CLICK_TO_SEARCH = WidgetTranslation.INSTANCE.emptySearch();

    // Text displayed on empty result
    private String result = NO_DATA;
	
	final private SimplePanel loaderPanel = new SimplePanel();
	
	private boolean small = false;
	
	/**
	 * Creates a new loader image
	 */
	public AjaxLoaderImage()
	{
		this(false, "");
	}
	
	/**
	 * Creates a new loader image with SMALL ICON
	 * @param isSmall Whether the icon should be small
	 */
	public AjaxLoaderImage(boolean isSmall)
	{
		this(false, "", isSmall);
	}

	/**
	 * Creates a new loader image
     *
	 * @param search When true, the search text is shown
	 * @param customMessage Custom message text
	 */
	public AjaxLoaderImage(boolean search, String customMessage)
	{
		this(search, customMessage, false);
	}

	
	/**
	 * Creates a new loader image
     *
	 * @param search When true, the search text is shown
	 * @param customMessage Custom message text
	 * @param small Whether loader should be small
	 */
	public AjaxLoaderImage(boolean search, String customMessage, boolean small) {
		this.initWidget(loaderPanel);

		if(search){
			prepareToSearch(customMessage);			
		}else{
			loadingStart();
		}
		
		this.small = small;
	}
	
	/**
	 * When called, the value is changed to NO_DATA
	 */
	public AjaxLoaderImage loadingFinished()
	{
		Scheduler.get().scheduleDeferred(new Command() { 
			public void execute() {
				loaderPanel.setWidget(getMessage(result));
			}
		});
		return this;
	}
	
	/**
	 * When called, the value is changed to ERROR
	 * @param error
	 */
	public AjaxLoaderImage loadingError(final PerunError error)
	{
		Scheduler.get().scheduleDeferred(new Command() { 
			public void execute() {
				
				if (error == null) {
					loaderPanel.setWidget(getMessage(ERROR));
				} else {
					
					HTML errWidget = getMessage(RESPOND_ERROR);
					errWidget.addStyleName("serverResponseLabelError");
					loaderPanel.setWidget(errWidget);
				}
				
			}
		});
		return this;
	}

	/**
	 * When loading starts
	 */
	public AjaxLoaderImage loadingStart() {
		Scheduler.get().scheduleDeferred(new Command() { 
			public void execute() {
				
				Image image;
				if(!small){
					image = new Image(IMAGE_URL);
				}else{
					image = new Image(SMALL_IMAGE_URL);
				}
				
				loaderPanel.setWidget(image);
			}
		});
		return this;
	}
	
	/**
	 * Sets empty widget when searching. You can specify custom message.
	 * If message is empty, default message is used.
	 */
	public AjaxLoaderImage prepareToSearch(final String customMessage)
	{
		Scheduler.get().scheduleDeferred(new Command() { 
			public void execute() {
				if (customMessage == "") {
					// default message
					loaderPanel.setWidget(getMessage(CLICK_TO_SEARCH));
				} else {
					// custom message
					loaderPanel.setWidget(getMessage(customMessage));	
				}
			}
		});
		return this;
	}

    /**
     * Sets empty result message
     *
     * @param result
     */
    public void setEmptyResultMessage(String result) {
        if (result == null) {
            this.result = NO_DATA;
        } else {
            this.result = result;
        }
    }
	
	
	/**
	 * Returns formatted message to use in the widget
	 * Different size for small and large loader
	 *  
	 * @param text
	 * @return
	 */
	private HTML getMessage(String text){
		
		if(small){
			return new HTML("<p><strong>" + text + "</strong></p>");
		}
		
		return new HTML("<h2 class=\"now-managing\">" + text + "</h2>");
	}
	
}