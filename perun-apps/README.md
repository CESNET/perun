# Perun mini applications #

Mini applications are small one-purpose JS web apps. They are meant for end-users (members of VOs) to configure their settings in user-friendly way.
Apps can be easily styled, so user can identify application directly with each VO without even knowing about Perun system.

## Apps structure ##

Common files used by all mini-apps are stored in /bootstrap /css /img and /js folders. If your app require own specific files, please put them as sub-folders of your miniapp. You can base you own mini-apps on example apps used for purpose of MetaCentrum VO (folder /metacentrum).

Apps are configured by /js/Configuration.js file, which stores URL to Perun's RPC. More info about RPC interface can be found at: https://wiki.metacentrum.cz/wiki/Perun_remote_interfaces

File /js/PerunLoader.js then contains shared pre-defined ajax calls, which retrieve information about user and perform keep-alive checking to Perun server.

## Used external libraries ##

- Bootstrap 3.0.2
- jQuery 1.8.2

