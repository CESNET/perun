# Perun mini applications #

Mini applications are small one-purpose JS web apps. They are meant for end-users (members of VOs) to configure their settings in user-friendly way.
Apps can be easily styled, so user can identify application directly with each VO without even knowing about Perun system.

## Apps structure ##

Common files used by all mini-apps are stored in /apps-include folder and are meant to be accessible without authorization. If your app require own specific files, please put them as sub-folders of your miniapp. You can base you own mini-apps on examples contained in /apps folder.

Index files of each app are expected to be behind authorization. Since apps do support multiple types of authorization (kerberos, IdP Shibboleth, certificates), they must be referenced relatively to currently used authorization. Expected format of apps URL is ```hostname/apps/[appname]/[authz_type]/``` where [authz-type] can be one of non, krb, fed, cert.

Apps are configured by /apps-include/js/Configuration.js file, which stores URL to Perun's RPC (relative to hostname by default). It can be overridden by same files included within your app. More info about RPC interface can be found at: https://wiki.metacentrum.cz/wiki/Perun_remote_interfaces

## Used external libraries ##

- Bootstrap 3.2.0
- jQuery 1.11.1
- modernizr 2.6.2
- respond 1.1.0

