Upgrade notes

## [29.0.0](https://github.com/CESNET/perun/compare/v28.0.2...v29.0.0) (2023-09-04)


### ⚠ BREAKING CHANGES

* Auditlogger no longer writes audit messages to the syslog. All configuration
related to usage of syslog is ignored and can be removed from /etc/perun/perun-auditlogger
and /etc/perun/perun-auditlogger.properties. Make sure journald is present and configured
on the machine before deploying.
* **core:** added new role 'PERUNADMINBA'

* 🎸 Filter our embedded groups where user is member ([1968093](https://github.com/CESNET/perun/commit/1968093fae20715fa9688663e9a9a9ac1f21e72e))
* 🎸 RPC groupsManager/getGroupsWhereUserIsActiveMember ([baf35f7](https://github.com/CESNET/perun/commit/baf35f771d675745d56fb1ebf78836ccecb03d3a))
* **core:** added new role ([9c55b3a](https://github.com/CESNET/perun/commit/9c55b3a5b35dcde3bf6f7e1e08e645d7381f9fe7))
* **core:** allow perun observer to call getAllNamespaces method ([a75e080](https://github.com/CESNET/perun/commit/a75e08063eb9d261cf072cdde2e3a69d97481607))
* **core:** attribute module for microsoft mails ([26b530d](https://github.com/CESNET/perun/commit/26b530d57857641ceaa4765c97694aa37f868f6a))
* **core:** check open applications ([fe13f87](https://github.com/CESNET/perun/commit/fe13f87a046d5f0b1e6fd8ae6b67372178547dae))
* **core:** enforce mfa modul - correctly retrieve mfa categories ([dafdc82](https://github.com/CESNET/perun/commit/dafdc828e3fb2410290d294d44a9f3fefaad0fd4))
* **core:** free logins when deleting login namespace attribute ([1d5f537](https://github.com/CESNET/perun/commit/1d5f537852c7ab634bc0de3684e7c47ceb6bdc58))
* **core:** restrict deletion of the attribute definition ([b562024](https://github.com/CESNET/perun/commit/b562024ce9be507b38fe7bb53c432716bd2a31f9))
* **core:** richgroup is not supported ([3089fba](https://github.com/CESNET/perun/commit/3089fba1b6974cee0a81669050f081f70c39c2d6))
* **deps:** update dependency com.google.apis:google-api-services-admin-directory to directory_v1-rev20230814-2.0.0 ([980708a](https://github.com/CESNET/perun/commit/980708a5bad768300eb7c6035abf2c26ba5cfaa4))
* **registrar:** disable member invitation for incorrect setup ([c482ddc](https://github.com/CESNET/perun/commit/c482ddca4835cb2eecb9318c9cc3679f1e3fd939))
* use journald instead of syslog in perun-auditlogger ([fdd9e54](https://github.com/CESNET/perun/commit/fdd9e5466348766fd43f060eb16d0678708ad62b))

## [28.0.2](https://github.com/CESNET/perun/compare/v28.0.1...v28.0.2) (2023-08-28)

## [28.0.1](https://github.com/CESNET/perun/compare/v28.0.0...v28.0.1) (2023-08-15)

## [28.0.0](https://github.com/CESNET/perun/compare/v27.1.0...v28.0.0) (2023-08-10)


### ⚠ BREAKING CHANGES

* authz table was updated
ALTER TABLE authz ADD COLUMN created_at timestamp default statement_timestamp() not null;
ALTER TABLE authz ADD column created_by varchar default user not null;
UPDATE configurations set value='3.2.16' WHERE property='DATABASE VERSION';
* **core:** column 'global' was added to the attribute_critical_actions table
Database changelog:
ALTER TABLE attribute_critical_actions ADD COLUMN global boolean default false not null;
UPDATE configurations SET value='3.2.17' WHERE property='DATABASE VERSION';
* **core:** Added created_at and created_by columns to authz table.
* **core:** New property 'appAllowedRoles' added to the CoreConfig. In perun.properties define 'perun.appAllowedRoles.apps' as a list of names of apps where role limitation is necessary.
For each app name, define regex which maps to the Referer header of the request coming from the given app and a list of allowed roles. For example:
perun.appAllowedRoles.apps=registrar
perun.appAllowedRoles.registrar.reg=^.*/registrar/.*$
perun.appAllowedRoles.registrar.roles=SELF,MFA
* **core:** Make sure following registration modules are not used on your instance - Ceitec, EduGain, Elixircz, Sitola and WeNMR.

* fixup! feat(core): extend authz table with audit attributes ([a85de71](https://github.com/CESNET/perun/commit/a85de7144b8022e316aa585f4fbbc8c202bf4bf7))
* **core:** removed unused registration modules ([32bbba5](https://github.com/CESNET/perun/commit/32bbba58c9d13eca4ae1a316ad69de7ee3a0c16e))


### New features and notable changes

* 🎸 BBMRIResources reg. module ([8cee9f6](https://github.com/CESNET/perun/commit/8cee9f607bb73631b565c472b956a678f1964619))
* 🎸 new RPC method membersManager/sendUsernameReminder ([60eccd0](https://github.com/CESNET/perun/commit/60eccd088924090fc78b71857f2cd4a286c39e94))
* **core:** allow to set attribute action as globally critical ([da3d1eb](https://github.com/CESNET/perun/commit/da3d1eb24553b11933d259cd33438fe9287a710b))
* **core:** attribute modul for mfaEnforceSettings ([6de84b7](https://github.com/CESNET/perun/commit/6de84b7e71e1141b91058fadb1fd4abcfa320389))
* **core:** extend authz table with audit attributes ([1608da5](https://github.com/CESNET/perun/commit/1608da50bd1d62842e5b6d18475bee9f273a63b2))
* **core:** filter getMembersPage ([9d52d58](https://github.com/CESNET/perun/commit/9d52d5857d0b253218fc2e86a39b85a3df88e534))
* **core:** last successful propagation ([56d6722](https://github.com/CESNET/perun/commit/56d672243a6b1ebe2733bcc5e153c5af6fcad11e))
* **core:** remove not allowed roles ([c3654b6](https://github.com/CESNET/perun/commit/c3654b63092e99c212823d60e40f67dbf8e15871))
* **core:** skip MFA for internal components ([259e284](https://github.com/CESNET/perun/commit/259e284b0e39f89a22983c3d1f2eb153107d2b24))
* enable facility search for SP reg role ([9274d3c](https://github.com/CESNET/perun/commit/9274d3cb2edb65e1f8e8479b2ea899266bc7a055))
