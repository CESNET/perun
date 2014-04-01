package cz.metacentrum.perun.webgui.client.applicationresources;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import cz.metacentrum.perun.webgui.client.resources.SmallIcons;
import cz.metacentrum.perun.webgui.widgets.AjaxLoaderImage;

/**
 * Shows the status input
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public class FormInputStatusWidget extends Composite{

	public enum Status { OK, ERROR, LOADING };

	private Status status;

	private String message;

	private FlexTable ft = new FlexTable();


	public FormInputStatusWidget(String message){
		this(message, Status.OK);
	}

	public FormInputStatusWidget(String message, Status status){
		this.status = status;
		this.message = message;
		this.initWidget(ft);
		build();

	}

	private void build() {

		ft.clear();
		FlexCellFormatter ftf = ft.getFlexCellFormatter();

		Image img;
		String classname = "";

		if(status == Status.OK){
			img = new Image(SmallIcons.INSTANCE.acceptIcon());
			classname = "input-status-ok";
		}else if(status == Status.ERROR){
			img = new Image(SmallIcons.INSTANCE.exclamationIcon());
			classname = "input-status-error";
		} else{
			img = new Image(AjaxLoaderImage.SMALL_IMAGE_URL);
			classname = "input-status-loading";
		}

		Label label = new Label(message);
		label.addStyleName(classname);
		label.getElement().setId(classname);

		ft.setWidget(0, 0, img);
		ft.setWidget(0, 1, label);

		ftf.setWidth(0, 0, "25px");
		ftf.setHeight(0, 0, "25px");
		ftf.setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
		ftf.setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_MIDDLE);
	}
}

