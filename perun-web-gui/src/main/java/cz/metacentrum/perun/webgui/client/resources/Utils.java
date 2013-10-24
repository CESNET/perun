package cz.metacentrum.perun.webgui.client.resources;

import com.google.gwt.user.client.Window;
import cz.metacentrum.perun.webgui.client.PerunWebConstants;
import cz.metacentrum.perun.webgui.client.PerunWebSession;

import java.util.ArrayList;

/**
 * Class with support methods used in GUI code
 *
 * @author Pavel Zlamal <256627@mail.muni.cz>
 * @version $Id$
 */
public class Utils {

    private static final int DEFAULT_LENGTH = 25;

    public static final String GROUP_NAME_MATCHER = "^[- a-zA-Z.0-9_:]+$";
    public static final String VO_SHORT_NAME_MATCHER = "^[-a-zA-Z0-9_]+$";
    public static final String ATTRIBUTE_FRIENDLY_NAME_MATCHER = "^[-a-zA-Z:]+$";

    /**
     * Return stripped string with ellipsis of custom length
     * (whitespace before ellipsis is removed)
     *
     * @param text text to strip
     * @param maxLength maximum text length
     * @return stripped string with ellipsis
     */
    public static String getStrippedStringWithEllipsis(String text, int maxLength) {

        if (maxLength >= text.length()) {
            return text;
        } else {
            return text.substring(0, maxLength).trim()+"\u2026";
        }
    }

    /**
     * Return stripped string with ellipsis of default length
     * (whitespace before ellipsis is removed)
     *
     * @param text text to strip
     * @return stripped string with ellipsis
     */
    public static String getStrippedStringWithEllipsis(String text) {

        if (DEFAULT_LENGTH >= text.length()) {
            return text;
        } else {
            return text.substring(0, DEFAULT_LENGTH).trim()+"\u2026";
        }
    }

    /**
     * Return URL to identity consolidator GUI
     * with optional ?target= param having current Perun GUI URL.
     *
     * @param target TRUE if use ?target= param in identity consolidator URL / FALSE otherwise
     * @return URL to identity consolidator
     */
    public static String getIdentityConsolidatorLink(boolean target) {

        final String URL_KRB = "https://perun.metacentrum.cz/perun-identity-consolidator-krb/";
        final String URL_FED = "https://perun.metacentrum.cz/perun-identity-consolidator-fed/";
        final String URL_CERT = "https://perun.metacentrum.cz/perun-identity-consolidator-cert/";
        String rpc = "";
        String link = "";

        if (PerunWebSession.getInstance().getRpcServer() != null) {
            rpc = PerunWebSession.getInstance().getRpcServer();
        }

        if (rpc.equalsIgnoreCase("krb")) {
            link = URL_KRB;
        } else if (rpc.equalsIgnoreCase("fed")) {
            link = URL_FED;
        } else if (rpc.equalsIgnoreCase("cert")) {
            link = URL_CERT;
        } else {
            // KRB AS BACKUP - "default"
            link = URL_KRB;
        }

        if (target) {
            link += "?target="+Window.Location.getHref();
        }

        return link;

    }

    /**
     * Return URL to Password change GUI for selected namespace
     *
     * @param namespace namespace where we want to reset password
     * @return URL to password reset GUI
     */
    public static String getPasswordResetLink(String namespace) {

        String baseLink = "";
        if (PerunWebConstants.INSTANCE.isDevel()) {
            baseLink = "https://alcor.ics.muni.cz/perun-gui-devel/PasswordReset.html";
        } else {
            baseLink = "https://perun.metacentrum.cz/perun-password-reset/";
        }

        if (namespace != null && !namespace.isEmpty()) {
            return baseLink+"?login-namespace="+namespace;
        } else {
            return baseLink;
        }

    }

    /**
     * Returns list of supported namespaces names for password change / reset
     *
     * @return list of supported namespaces names
     */
    public static ArrayList<String> getSupportedPasswordNamespaces() {

        ArrayList<String> supported = new ArrayList<String>();
        for (String s : PerunWebConstants.INSTANCE.getSupportedPasswordNamespaces()) {
            supported.add(s);
        }
        return supported;

    }

}
