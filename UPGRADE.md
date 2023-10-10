Upgrade notes

## [30.1.1](https://github.com/CESNET/perun/compare/v30.1.0...v30.1.1) (2023-10-10)


### Bug Fixes

* **core:** authorization in removeBan() methods ([25d2f5a](https://github.com/CESNET/perun/commit/25d2f5a29495108633b2bc43be82fe58fefc37f4))

## [30.1.0](https://github.com/CESNET/perun/compare/v30.0.0...v30.1.0) (2023-10-10)


### Features

* **core:** add getAssociatedResources to RPC and openapi ([0c07203](https://github.com/CESNET/perun/commit/0c07203ba4a6ad89c0b1e6027e21b4708b46b354))
* **core:** mfa categories use namespace as key ([6096bf6](https://github.com/CESNET/perun/commit/6096bf6e1860fdf76734e0a52b5316fd841d4081))
* **core:** new scopedLogin_mu virtual attribute ([08e8eb6](https://github.com/CESNET/perun/commit/08e8eb64d415e9e1880edae53635d956776c0cc7))


### Bug Fixes

* **core:** add right for GROUPMEMBERSHIPMANAGER to invite members ([2c83cab](https://github.com/CESNET/perun/commit/2c83cab007571403669f209bec692fb2d713050b))
* **core:** initialize missing unixGID-namespace facility attribute ([237371e](https://github.com/CESNET/perun/commit/237371e2898eb553a3f8f38232b9d02274fd2f9a))
* **core:** user:virt:voPersonExternalAffiliation forces to lowercase ([3facb22](https://github.com/CESNET/perun/commit/3facb2248b49c387f5218c23e705758805ede3eb))
* **deps:** update dependency net.jodah:expiringmap to v0.5.11 ([f6ca050](https://github.com/CESNET/perun/commit/f6ca05035e8e9869e36186416461d8beb5c7c620))
* **deps:** update dependency org.springframework.boot:spring-boot-starter-parent to v2.7.16 ([cfc7adc](https://github.com/CESNET/perun/commit/cfc7adc5c1b3e1debf8dca9d10dc3c3829f94985))
* **deps:** update dependency org.xhtmlrenderer:flying-saucer-pdf to v9.3.1 ([3c40522](https://github.com/CESNET/perun/commit/3c4052282007f27dd8351b4d1aed05c3a2155cca))

## [30.0.0](https://github.com/CESNET/perun/compare/v29.1.0...v30.0.0) (2023-09-27)


### ⚠ BREAKING CHANGES

* 🧨 ApplicationForm bean property `moduleClassName` replaced with
`moduleClassNames`. Type has changed from String to List<String>. Includes
database version update and column `module_name` of `application_form` table
being renamed to `module_names`.
* requires database update. UI version have to work with
updated model of ApplicationForm (`moduleClassName` replaced with
field `moduleClassNames`).
* **core:** the groupMembershipExpiration attribute needs to have a new READ policy collection created with the SELF - USER policy
* Changed behaviour might cause sending notifications to
managers or configured TO recipients in parent group rather than to VO.

### Features

* 🎸 Allow multiple reg. modules to be configured ([b807877](https://github.com/CESNET/perun/commit/b807877de45c6bdaf6437a6791a1de72ab183909))
* 🎸 Cascade to parent gr. when deciding gr. TO recipients ([8adea84](https://github.com/CESNET/perun/commit/8adea845a43887cbca713463aaaf55de4fff1df9))
* **cli:** added getRichMember method to the perl client API ([1c53692](https://github.com/CESNET/perun/commit/1c536925f9bd68f93e8aeb1e476e3e76a1964895))
* **core:** allow members to read their group expiration ([811b217](https://github.com/CESNET/perun/commit/811b2173cd7b673677c8f49b10a26126b52ed4da))
* **core:** allow resource managers to read subgroup managers ([ba1bb15](https://github.com/CESNET/perun/commit/ba1bb156580afffc4714343c6d4c834113762219))
* **core:** new ExtSource type for IT4I ([28d6f87](https://github.com/CESNET/perun/commit/28d6f8777365fe96b6190400b841b9c802f91310))
* **core:** sort users by IDs when synchronizing LDAP ([cf542ed](https://github.com/CESNET/perun/commit/cf542ed0c16a652b14e58b5244ef90689d261c03))
* **core:** support authoritative groups in group structure synchronization ([9bc9d14](https://github.com/CESNET/perun/commit/9bc9d1419b5753950531d8b2f8940b27dd561c19))


### Bug Fixes

* **core:** properly resolve members removal from authoritative groups ([26de9ab](https://github.com/CESNET/perun/commit/26de9abf70368b5fd2109cdd4ac47f1b283e1b96))
* **deps:** update dependency com.google.apis:google-api-services-admin-directory to directory_v1-rev20230822-2.0.0 ([f3bee32](https://github.com/CESNET/perun/commit/f3bee326dd92665acf5b2af4ec85a1280549bf54))
* **deps:** update dependency org.xhtmlrenderer:flying-saucer-pdf to v9.2.2 ([f20ec3d](https://github.com/CESNET/perun/commit/f20ec3dd4664c89eb563e1003d9ae023afd5367d))
* fixed definition of logback in perun-auditlogger ([0f0ea39](https://github.com/CESNET/perun/commit/0f0ea390338dd58e23e2dbc34e5853ef5f9243b4))
* minimize default logging for perun-auditlogger ([f46ba67](https://github.com/CESNET/perun/commit/f46ba67d3c3504c6f2c07b34c46aedf3a76e21fe))

## [29.1.0](https://github.com/CESNET/perun/compare/v29.0.0...v29.1.0) (2023-09-11)


### Features

* **core:** approve applications method ([6fdaf33](https://github.com/CESNET/perun/commit/6fdaf337f9bf1d16e1af1cf6a1b9c196457c3469))
* **core:** delete applications method ([a873ae5](https://github.com/CESNET/perun/commit/a873ae5e5e3c81a2fd5067ec1112323790ae7a48))
* **core:** new enabledO365MailForward virtual attribute ([fcb256f](https://github.com/CESNET/perun/commit/fcb256f5c5bf8d42bae964ace804f3f12d58c651))
* **core:** reject applications method ([a2d270c](https://github.com/CESNET/perun/commit/a2d270c0b5b24202a854185cad072be74acc151b))
* **core:** resend notifications method ([352d9e2](https://github.com/CESNET/perun/commit/352d9e256d3d74cc7fe871aec4eadb9003c6957a))


### Bug Fixes

* **core:** approve applications order ([019acfe](https://github.com/CESNET/perun/commit/019acfe3f7cbb5c7ffa876782be7f1535337f773))
* **deps:** update dependency org.springframework.boot:spring-boot-starter-parent to v2.7.15 ([7d389b9](https://github.com/CESNET/perun/commit/7d389b9f3949fa480b4f4669382d0975bd3d16b9))

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
