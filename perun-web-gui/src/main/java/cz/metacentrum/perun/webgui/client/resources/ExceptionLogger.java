package cz.metacentrum.perun.webgui.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.web.bindery.event.shared.UmbrellaException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Logs all uncaught exceptions to the browser console.
 *
 * @author Pavel Zlámal <zlamal@cesnet.cz>
 */
public class ExceptionLogger implements GWT.UncaughtExceptionHandler {

	private static Logger rootLogger = Logger.getLogger("");

	@Override
	public void onUncaughtException(Throwable wrapped) {

		Throwable t = unwrapUmbrella(wrapped);
		rootLogger.log(Level.SEVERE, "", t);

	}

	private Throwable unwrapUmbrella(Throwable e) {
		if(e instanceof UmbrellaException) {
			UmbrellaException ue = (UmbrellaException) e;
			if(ue.getCauses().size() == 1) {
				return unwrapUmbrella(ue.getCauses().iterator().next());
			}
		}
		return e;
	}

}
