# Perun mini applications #

Mini applications are small one-purpose JS web apps. They are meant for end-users (members of VOs) to configure their settings in user-friendly way.
Apps can be easily styled, so user can identify application directly with each VO without even knowing about Perun system.

## Apps structure ##

You can base you own mini-apps on examples contained in /apps folder. We strongly recommend to look at *user-profile* app.

Common files used by all mini-apps are stored in ``/apps-include`` folder and are meant to be accessible without authorization. You can reference them relatively to hostname like:

```
<link href="/apps-include/bootstrap/css/bootstrap.min.css"  rel="stylesheet">
```

If your app require own specific files, please put them in sub-folders of your miniapp and use relative link with ``/non/`` as a part of URL:

```
<link href="../non/css/main.css" rel="stylesheet">
```

HTML files of each app are expected to be behind authz. Since apps do support multiple types of authentication (kerberos, IdP Shibboleth, certificates), all files must be referenced relatively to currently used authentication. Also if application requires authenticated access, you must offer proper way of "redirection" to users, which visited the site without authentication.

Expected format of apps URL is ```https://hostname/[apps]/[app_name]/[authz_type]/``` where 

* [apps] is a string which can be configured for each apache instance (default is apps)
* [app_name] is a same as app folder name in sources structure ``/apps/user-profile ==> user-profile``
* [authz-type] can be anything, but there common options: non, krb, fed and cert which refer to the way of authentication (or non for non-authz access).

Apps are configured by ``/apps-include/js/Configuration.js`` file, which stores URL to Perun's RPC (relative to hostname by default). It can be overridden by same files included within your app. More info about RPC interface can be found at: https://wiki.metacentrum.cz/wiki/Perun_remote_interfaces

Usually first call your app does is to get info about logged user, there is a method ``getPerunPrincipal`` in ``/apps-include/js/PerunUserLoader.js``. There is also utility function to test, if Perun (backend) is alive and user is logged, just include ``/apps-include/js/PerunConnectionTester.js``.

## Used external libraries ##

- Bootstrap 3.2.0
- jQuery 1.11.1
- modernizr 2.6.2
- respond 1.1.0