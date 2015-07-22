package cz.metacentrum.perun.webgui.widgets;

import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.ui.*;
import cz.metacentrum.perun.webgui.client.resources.LargeIcons;
import cz.metacentrum.perun.webgui.client.resources.Utils;

/**
 * Widget which is displayed when user is not authorized to view GUI
 * => user not found by provided KRB/FED credentials.
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 */

public class NotUserOfPerunWidget extends Composite {

	/**
	 * Creates a new instance of NotUserOfPerunWidget
	 */
	public NotUserOfPerunWidget() {

		FlexTable layout = new FlexTable();

		String text = "";

		if (Utils.getNativeLanguage().isEmpty() || (!Utils.getNativeLanguage().isEmpty() && !LocaleInfo.getCurrentLocale().getLocaleName().equals(Utils.getNativeLanguage().get(0)))) {
			// english for all
			text = "<h3>You are not user of Perun or you don't have registered identity you used to log in.</h3><h3>For joining your identities please go to:</h3>";

		} else if (Utils.getNativeLanguage().get(0).equals("cs")) {
			// czech for czech
			text = "<h3>Buď nejste evidováni v systemu Perun a nebo nemáte zaregistrovanou Vaši identitu, kterou jste se teď přihlasil(a).</h3>" +
				"<h3>Pro spárování identit pokračujte na:</h3>";
		}

		String link = Utils.getIdentityConsolidatorLink(true);
		Anchor hp = new Anchor("<h4>"+link+"</h4>", true, link);

		layout.setWidget(0, 0, new HTML(new Image(LargeIcons.INSTANCE.errorIcon())+text+hp));

		layout.setSize("100%", "100%");
		layout.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
		this.initWidget(layout);

	}

}
