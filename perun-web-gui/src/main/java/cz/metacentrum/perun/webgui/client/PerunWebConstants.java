package cz.metacentrum.perun.webgui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;

import java.util.ArrayList;

/**
 * Interface to access common GUI settings like RPC URL, callback timeouts,...
 * Settings are stored in {devel,production}/resources/PerunWebConstants.properties
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 * @version $Id$
 */
public interface PerunWebConstants extends Constants{


    public static final PerunWebConstants INSTANCE =  GWT.create(PerunWebConstants.class);

    /**
     * Default RPC URL
     * @return RPC URL string
     */
    String perunRpcUrl();

    /**
     * Federation RPC URL
     * @return RPC URL string
     */
    String perunRpcUrlFed();

    /**
     * Certificate RPC URL
     * @return RPC URL string
     */
    String perunRpcUrlCert();


    /**
     * Kerberos RPC URL
     * @return RPC URL string
     */
    String perunRpcUrlKrb();

    /**
     * Federation RPC URL for forceAuthn
     * @return RPC URL string
     */
    String perunRpcUrlForceAuthnFed();


    /**
     * Base timeout for all callbacks to RPC when callback is
     * removed from page and marked as ServerError (request timeout exceeded)
     *
     * @return Callbacks timeout (time in miliseconds)
     */
    int jsonTimeout();

    /**
     * The e-mail address the reports should be send to.
     *
     * @return e-mail address
     */
    String perunReportEmailAddress();

    /**
     * Default interval for refreshing window with pending requests
     *
     * @return time in milliseconds
     */
    int pendingRequestsRefreshInterval();

    /**
     * Default RT queue for tickets
     *
     * @return RT queue
     */
    String defaultRtQueue();

    /**
     * VO members group name, default "members"
     *
     * @return
     */
    String vosManagerMembersGroup();

    /**
     * Link in the footer of GUI
     *
     * @return
     */
    String footerPerunLink();

    /**
     * License text in footer
     *
     * @return
     */
    String footerPerunLicense();

    /**
     * Copyright text in footer
     *
     * @return
     */
    String footerPerunCopyright();

    /**
     * Returns true if built on developer server
     * @return
     */
    boolean isDevel();


    /**
     * Copyright text in footer
     *
     * @return
     */
    String guiVersion();

    /**
     * Returns array of attribute URNs
     * They are used to get members of group/vo with specific attributes
     *
     * @return array of URNs
     */
    @DefaultStringArrayValue({"urn:perun:member:attribute-def:def:organization", "urn:perun:user:attribute-def:def:organization", "urn:perun:user:attribute-def:def:preferredMail", "urn:perun:member:attribute-def:def:mail", "urn:perun:user:attribute-def:def:login-namespace:einfra", "urn:perun:user:attribute-def:def:login-namespace:meta", "urn:perun:user:attribute-def:def:login-namespace:mu", "urn:perun:user:attribute-def:def:login-namespace:sitola", "urn:perun:user:attribute-def:def:login-namespace:cesnet", "urn:perun:user:attribute-def:def:login-namespace:egi-ui", "urn:perun:user:attribute-def:def:login-namespace:einfra-services"})
    String[] getAttributesListForMemberTables();

    /**
     * Returns array of attribute URNs
     * They are used to get users of group/vo with specific attributes
     *
     * @return array of URNs
     */
    @DefaultStringArrayValue({"urn:perun:user:attribute-def:def:organization", "urn:perun:user:attribute-def:def:preferredMail", "urn:perun:user:attribute-def:def:login-namespace:einfra", "urn:perun:user:attribute-def:def:login-namespace:meta", "urn:perun:user:attribute-def:def:login-namespace:mu", "urn:perun:user:attribute-def:def:login-namespace:sitola", "urn:perun:user:attribute-def:def:login-namespace:cesnet", "urn:perun:user:attribute-def:def:login-namespace:egi-ui", "urn:perun:user:attribute-def:def:login-namespace:einfra-services"})
    String[] getAttributesListForUserTables();

    /**
     * Returns array of names of supported namespaces for password change / reset
     *
     * @return array of supported namespaces names
     */
    @DefaultStringArrayValue({"einfra", "einfra-services", "sitola", "egi-ui"})
    String[] getSupportedPasswordNamespaces();

}