package cz.metacentrum.perun.webgui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;

/**
 * Interface to access common GUI settings like RPC URL, callback timeouts,...
 * Settings are stored in {devel,production}/resources/PerunWebConstants.properties
 *
 * @author Vaclav Mach <374430@mail.muni.cz>
 */
public interface PerunWebConstants extends Constants {


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
    String perunRpcUrlForceAuthFed();

    /**
     * Kerberos authz with EINFRA namespace
     *
     * @return RPC URL string
     */
    String perunRpcUrlKrbEinfra();

    /**
     * Base timeout for all callbacks to RPC when callback is
     * removed from page and marked as ServerError (request timeout exceeded)
     *
     * @return Callbacks timeout (time in milliseconds)
     */
    int jsonTimeout();

    /**
     * Default interval for refreshing window with pending requests
     *
     * @return time in milliseconds
     */
    int pendingRequestsRefreshInterval();

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
     * Copyright text in footer
     *
     * @return
     */
    String guiVersion();

}