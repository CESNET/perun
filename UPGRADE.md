Upgrade notes

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
