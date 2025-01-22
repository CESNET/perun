Upgrade notes

## [47.0.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v46.1.0...v47.0.0) (2025-01-22)


### ⚠ BREAKING CHANGES

* **core:** attributes_authz and action_types tables will be dropped on commit deploy.
Removed all deprecated atttributes authorizaton methods.

### Features

* **registrar:** add application form submit button presence validation ([f901d51](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/f901d51fe4a9c71e5a745f7f232d060263b3690c))


### Bug Fixes

* **core:** trim search string ([21f102d](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/21f102d3e20796c821666d71cc07ee4e52055873))
* support also remote DBs in scripts for v45.0.0 deployment ([b72b4a0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/b72b4a0582cd6cef401d8285451855ca7bb92b3a))


### Others

* **core:** remove deprecated attributes authz logic ([bfe327f](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/bfe327f9d4dc8e3c6646a2ed2f586095a9ca2632))

## [46.1.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v46.0.0...v46.1.0) (2025-01-09)


### Features

* update `groupNames` attribute in ldap on group update ([e6c7fdb](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/e6c7fdb11ebeb8fa1516b29987f37c8ffb147e07))

## [46.0.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v45.0.0...v46.0.0) (2025-01-09)


### ⚠ BREAKING CHANGES

* **openapi:** Owner object is deprecated. Do not use.
* removed security teams and all related functionality. Removed role SECURITYADMIN.
Removed user_facility virt_blacklisted attribute.
All of the related tables and data will be DELETED and DROPPED on commit deploy!
* **core:** reserved_logins table was deleted and will be dropped
on commit deploy
* **gui:** removed security teams, blacklists and security admin role

### Features

* **core:** new regex for mail address validation ([57f7c1a](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/57f7c1ae77a2a7460f4b168bba7a9494f0c915e5))
* **registrar:** mandatory destination attribute for form item of type password ([231a75d](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/231a75da420a99e60becc02f98a1482c17bd6707))


### Bug Fixes

* **deps:** update dependency org.apache.commons:commons-text to v1.13.0 ([3f075ac](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/3f075ac2d8a3116ae94dfc79406c14b45bd9ae1e))
* **deps:** update dependency org.bouncycastle:bcmail-jdk18on to v1.79 ([88f7890](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/88f78901ff3c0188e46e8a8a60cfb975996bc9fe))
* **deps:** update dependency org.json.json to 20240303 ([e9876ae](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/e9876ae056029267b925d5242cbe007dc7694f59))
* **deps:** update dependency org.springframework.boot:spring-boot-starter-parent to v3.4.1 ([9ab621d](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/9ab621d73a77a558716e096586e7685fbebf8a5c))
* **deps:** update google-api-services-admin-directory ([fa8255d](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/fa8255d394416a08e83decee136b78f6bad6ad2c))
* **openapi:** fixed typos in manager names ([3856d26](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/3856d264407e248d873844fee6e58a52911d4532))


### Others

* **core:** remove reserved_logins table ([6e51381](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/6e513811b600e8f594611232427c39c25e5957f9))
* **gui:** remove security teams and all related functionality ([481a03f](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/481a03fdb21e205401658fe84db9e34fee485f1c))
* **openapi:** owner deprecated ([8faf952](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/8faf952c0514ef41f275902c65ecc221e9222006))
* remove security teams and related functionality ([85b20df](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/85b20dff414ec7dfff726a3a8136ced1af0b91b7))

## [45.0.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v44.2.0...v45.0.0) (2024-12-16)


### ⚠ BREAKING CHANGES

* **core:** addOrganizationMembershipManagerPolicies.pl needs to
be run to fill the policies for the new role
* **core:** db update

### Features

* add canBeApproved call to `approveApplications` ([cdf6b57](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/cdf6b57fade137d1acd26667eec6f550b428e1ea))
* **core:** creator roles assignable to groups ([33fc2cc](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/33fc2cc90fd350469c566cb2987ea374fafe4f66))
* **core:** new elterid login modules ([45a6838](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/45a6838b3503e1ac222a3a86a11b94f41e750b4b))
* **core:** organization membership manager role ([aa567a2](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/aa567a283d5d3cd388bd1977d49362081bd4b0db))
* **core:** suspend persistent ([71a3a3e](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/71a3a3e35f01d0f1eea41d0af24577f5abd4e871))
* **registrar:** run validateMember after commit ([662f03c](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/662f03ca52c134bcfb20e14ab9f9e90a3651f7e2))


### Bug Fixes

* **core:** correctly call `validateMemberAsync()` ([7dfdcbf](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/7dfdcbfced66f785b389d212e23fa61756023c47))
* **core:** skip nonexistent AuditEvents ([e329910](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/e3299107139e1a417893a41daf1783b2d559597a))
* **deps:** update dependency google-api-services-admin-directory ([d3ad0d9](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/d3ad0d9598e77b1b4b2836c9bf3a731e9b8e5b90))
* **deps:** update dependency google-api-services-admin-directory to directory_v1-rev20241113-2.0.0 ([676c884](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/676c884536887ef640acf8eda22e882f54eac38b))
* **deps:** update dependency org.springframework.boot:spring-boot-starter-parent to v3.4.0 ([fd02f0c](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/fd02f0c6adbf68c127b863d3936fc3f3c7e0812a))
* **deps:** update dependency org.xhtmlrenderer:flying-saucer-pdf to v9.11.1 ([8de2796](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/8de2796ebadb36ab52f396d8c40b853dac70b524))
* **deps:** update dependency org.xhtmlrenderer:flying-saucer-pdf to v9.11.2 ([cc90c1b](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/cc90c1b81dcc6829dc323af97d78d16dd00740a5))
* **registrar:** wrong user check in IT4I module ([3fdee02](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/3fdee02c2ffb18035ea45ccd7f3d3cb8a7833552))

## [44.2.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v44.1.0...v44.2.0) (2024-12-13)


### Features

* add canBeApproved call to `approveApplications` ([47f5345](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/47f5345d7b20336794508185f4abc757587ac8aa))


### Bug Fixes

* **registrar:** wrong user check in IT4I module ([34ce3f7](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/34ce3f78687f1509c116e4e22b396bfa2420bb28))

## [44.1.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v44.0.0...v44.1.0) (2024-11-21)


### ⚠ BREAKING CHANGES

* **core:** Added new config properties for personal data change:
enableLinkedName,
enableCustomName,
customNameRequiresApprove,
enableLinkedOrganization,
enableCustomOrganization,
customOrganizationRequiresApprove,
enableLinkedEmail,
enableCustomEmail,
customEmailRequiresVerification.

To preserve the current behavior, set only the following properties to true:

perun.enableCustomEmail=true
perun.customEmailRequiresVerification=true
* **core:** Perun startup will now fail if roles.yml is incorrect

### Features

* **core:** add url-json destination ([2af53a6](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/2af53a650c7cb891a637ee8c8aba9a750b0be847))
* **core:** change personal data ([53b68a1](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/53b68a17477408e4c5eeb559b46629c5394c265b))
* **core:** check correctnes of role.yml when loading roles ([ccfd4ef](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/ccfd4eff2d2a41450e61d2a00750e9d5ad9905a1))
* **core:** getUserAssignments method ([1887dfd](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/1887dfddb9832cecef178eced81e11a9e004da41))
* **core:** getUserRelations method ([f029c79](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/f029c79f388db18164a35d7a2114998fedcdcfe6))
* **core:** getUserRoles filtered by caller rights ([7f00701](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/7f007015140e7f20d6d0fcf2ce046dc8812d105f))


### Bug Fixes

* **core:** changed roles to pass the newly added lint ([f802bb8](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/f802bb8fab231b3f12f39690005bd0466469b3fb))
* **deps:** update dependency org.xhtmlrenderer:flying-saucer-pdf to v9.11.0 ([a666bd9](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/a666bd9f85f572f751542db123e5f26d5ba0e86c))
* **deps:** update gwtversion to v2.12.1 ([b692ec7](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/b692ec76336dddeadc951da5b57a9c09a58c20bb))
* **registrar:** add info about problematic form items to the exception messages ([6cb54c3](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/6cb54c30950934e0e2ea501d3b84bb4f116c0be8))

## [44.0.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v43.0.0...v44.0.0) (2024-11-08)


### ⚠ BREAKING CHANGES

* **core:** `createFacility`can now only be called by newly created FACILITYCREATOR role
* **gui:** Configuration property "disableCreateVo" is no longer used and can be removed from the perun-web-gui.properties.

### Features

* **core:** add s3 destination ([045a7da](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/045a7da013cdd7b65be0fbbd84bbee5b52d257a6))
* **core:** logging of last manager, and retrieving non-anonymized users for last manager check ([30642f8](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/30642f81e17028a5e2decf17c4e7bfaa3e0a9f02))
* **core:** new FACILITYCREATOR role ([9b9c95b](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/9b9c95b7c273ce607e083d3568b82f605b45d051))
* **gui:** support new vocreator role ([5f784e0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/5f784e057e80f2f68a3aeae5d1bf819270d1ee86))


### Bug Fixes

* **core:** broken transactional behaviour on sync methods ([daf7538](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/daf7538246243a2660f8fb107b6c909be53e7555))
* **core:** initialize empty MailText with htmlFormat flag set ([d83e9cb](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/d83e9cb63324ddf2195d8fe139fdb4b9a84389b3))
* **deps:** update dependency org.springframework.boot:spring-boot-starter-parent to v3.3.5 ([fd77eba](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/fd77eba06bd89d02835b4095117b7d73639e4b2a))
* **deps:** update dependency org.xhtmlrenderer:flying-saucer-pdf to v9.10.0 ([c19c5d0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/c19c5d0792cc610e72301d96734f07e10710ba35))
* **deps:** update dependency org.xhtmlrenderer:flying-saucer-pdf to v9.10.2 ([68a61bd](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/68a61bdd105272a4cf26544f74021f9087fa9270))
* **deps:** update google-api-services-admin-directory ([3dd2a22](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/3dd2a226a95929705ddb8ed98144cddf39bd44e0))
* **deps:** update gwtversion to v2.12.0 ([689f8fc](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/689f8fc9e2600ee8d075b52f5d366b8b667df646))
* **registrar:** extendInvitationExpirationNoExpirationDate test ([0f41f7c](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/0f41f7cc9f98850d2cdb9cac0943822638fcb628))
* **registrar:** remove policy check on canInvitationBeAccepted ([2d8c878](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/2d8c878c100061ff90fd5476f05ac9a7ffd052ec))

## [43.0.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v42.0.1...v43.0.0) (2024-10-24)


### ⚠ BREAKING CHANGES

* **core:** `createVo`can now only be called by newly created VOCREATOR role
* **core:** removed `user_facility:virt:bucketQuota` attribute
* **core:** db update, new Service property modifying propagation
* New spring boot and framework version

### Features

* **core:** add option to not propagate expired VO members in services ([2bb47bf](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/2bb47bfe7b815da173f0e09e92c8c00ade3c747b))
* **core:** blockBucketCreation resource attribute ([3f0efa7](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/3f0efa7711bf5054751cf9b04013e32b24a38c38))
* **core:** new role EXEMPTEDFROMMFA ([3ce1e94](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/3ce1e94859fd61834eb4217b03d08f7ef7a76a38))
* **core:** new VOCREATOR role ([87cb7f2](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/87cb7f261651cd521274670c8cbf238a4befb817))
* **core:** suspendGroupSynchronization ([789b6b1](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/789b6b1ce5c9ad65e1420d153bd24012c12bcfe2))
* **gui:** support setting additional flags in service object ([68d8647](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/68d8647b99680313fa76de684fef62fefca7b4e8))
* **registrar:** resendInvitation method ([c0ce2bf](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/c0ce2bfe2b6ed2a86e68dc3122693fbbc30e638f))


### Bug Fixes

* **core:** correctly load logback config ([762fb09](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/762fb09d622c90b247d5bec79e89f471aced1e91))
* **gui:** vo/group mismatch in variable names ([c909552](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/c909552256604a8b32ef603ca019c5de57133b47))
* **openapi:** fixed dependencies for perun-openapi after upgrade to Spring Boot 3 ([5617fa3](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/5617fa3b35af891e96a7e2fc3a16fb71f3033e3a))


### Build

* migration of spring boot to v3 ([27552a5](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/27552a5f2b345bca358492e0a918ca1a0d4363c0))

## [42.0.1](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v42.0.0...v42.0.1) (2024-10-17)


### Bug Fixes

* **core:** correctly load logback config ([c6ba0ee](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/c6ba0ee358ed51d33fe92118e69ff9d2ce5fcfa7))

## [42.0.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v41.0.0...v42.0.0) (2024-10-15)


### ⚠ BREAKING CHANGES

* New spring boot and framework version

(cherry picked from commit 27552a5f2b345bca358492e0a918ca1a0d4363c0)

### Build

* migration of spring boot to v3 ([efa9c4e](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/efa9c4eddf81254c5b58fa6bb17befcd3f1050cc))

## [41.0.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v40.0.0...v41.0.0) (2024-10-12)


### ⚠ BREAKING CHANGES

* **core:** db update

### Features

* added upper limit for invite and extend invitation methods ([29aee60](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/29aee608656216dbd630a76def2235e15912111d))
* **cli:** added object and API definitions for applications mails ([a39d7e3](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/a39d7e31f025827e712d9a98e411fb5b62b8abdb))
* **core:** reply_to in perun notifications ([6331123](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/6331123df2789087e7442166492c28295914d683))


### Bug Fixes

* **deps:** update dependency google-api-services-admin-directory ([5d9a3e1](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/5d9a3e114bef7cb192dabd78fb50f8970e98c8fa))
* **deps:** update dependency org.xhtmlrenderer:flying-saucer-pdf to v9.9.5 ([b2d7cee](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/b2d7cee8d0327372c76bc08aa81edddb4fc695be))
* **openapi:** richApplication and RichUserExtSource discriminator without property beanName ([9837edd](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/9837eddc3415ad86d00ce77283df8559e0cfb19a))

## [40.0.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v39.2.0...v40.0.0) (2024-09-26)


### ⚠ BREAKING CHANGES

* **registrar:** 'inviteToGroup' still return the whole Invitation object, RPC docs updated
* **registrar:** 'inviteToGroup' now returns id of the invitation instead of whole object

### Features

* **registrar:** inviteToGroup return Invitation id ([b3a74e2](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/b3a74e2012eebebd4db3540f30c0efaac50c136f))
* **registrar:** revert inviteToGroup return value ([0112827](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/0112827818e90d52e9168847f42e2fb53f6c74d8))


### Bug Fixes

* **core:** correctly check for MFA timeout when un/setting roles ([1dbbee0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/1dbbee06bde16da0ad5648ca7247c0fdc4f0c9c7))
* **registrar:** get preapproved invitation url from attributes ([ca76a1d](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/ca76a1dbf806485b2b916941b298e77eb71c3675))

## [39.2.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v39.1.0...v39.2.0) (2024-09-20)


### Features

* **core:** added attribute module for optional login in egi ([9d1ca2d](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/9d1ca2d56de3dd83432850d6c2d41b2b74f61eee))
* **registrar:** copyForm idempotent variant ([36a730f](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/36a730f66fff489f32a39379ec737bfbc3b15b6e))


### Bug Fixes

* **deps:** update dependency org.xhtmlrenderer:flying-saucer-pdf to v9.9.4 ([e0cad22](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/e0cad222044d998f1e090be7a575bc4d75068e07))
* **deps:** update google-api-services-admin-directory to rev20240917-2.0.0 ([a409d3a](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/a409d3ada8414a54d5a02d60143a16c1d7f86aa2))
* **registrar:** create empty form when copying to group if not existing ([4f50588](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/4f50588ea38e699c0e0f9426ed5ffbed6c44ebba))
* **registrar:** fix copyForm parameter validation ([b895eda](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/b895edadf6b3ccea1b0c81242cf54dbda9578a6e))
* **registrar:** prefill vo/group attr values to form items for non-users ([31ebd42](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/31ebd42b11e1102d8de61e33bf6f581bb198e2f5))

## [39.1.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v39.0.0...v39.1.0) (2024-09-13)


### Features

* **cli:** added egi instance, support kerberos endpoints ([df2a24c](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/df2a24c24bec4e454f3d5f85fb68595bf982b77f))
* **core:** audit log last admin removed from Vo/Facility ([136366b](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/136366b864a2bbab2b19707d172f6a9d43035e9d))
* **core:** methods to check whether group/user removal leaves VO/Facility without manager ([c92f6da](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/c92f6da84f944ceb0109aa2c3608f4640ab06ec5))
* **registrar:** added funcionality to getinvitationbyid method ([347d21e](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/347d21eb3266f8a76d8358b23992124f4df11847))
* **registrar:** new getInvitationByApplication method ([2107bd3](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/2107bd305628faef4d09e1ce578ab089091b0ea9))


### Bug Fixes

* **deps:** update dependency commons-cli:commons-cli to v1.9.0 ([71ca524](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/71ca524cc28b5d5384ca37c15128ffac61b4a0b2))
* **deps:** update dependency google-api-services-admin-directory ([63d98c3](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/63d98c3c01097a00b7b9bbd7be424a7790416883))
* **deps:** update dependency org.xhtmlrenderer:flying-saucer-pdf to v9.9.1 ([db9f70c](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/db9f70c7decd71cbf8996d1e2dd24624c75ba220))
* **deps:** update dependency org.xhtmlrenderer:flying-saucer-pdf to v9.9.2 ([f0be829](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/f0be829a2cc84e77d3d71fcc2bd3e445e425989d))
* fixed typer lib usage for python version of perun_cli ([88a976f](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/88a976ff9c9585203fbf25dfd6f9f46a14f23e81))
* **openapi:** change checkHtmlInput to PUT request to avoid parameter size restrictions ([09ef9a9](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/09ef9a903975abd67841eafea242ad1748d2b566))

## [39.0.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v38.0.1...v39.0.0) (2024-08-28)


### ⚠ BREAKING CHANGES

* **registrar:** new mail_type value added into DB

### Features

* **registrar:** concat group item checkbox values with disabled flag ([83c58a0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/83c58a058dc33a1e8e56b12f0f1b34b2688264be))
* **registrar:** preapproved invitation mail type ([5aa15bd](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/5aa15bdcf91fcd3a7c1cca817d13bfae708e4943))


### Bug Fixes

* **core:** fixed semantic check in login-namespace:admin-meta ([38277bb](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/38277bb3a12a6057f5bdffc9acaa5975b6aaa0cd))
* **registrar:** additional changes squash later ([7591f67](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/7591f67ea9350c1512d7a1069aa927489b06b7a8))
* **registrar:** changed revoke invitation param name to be consistent with openapi ([a1e2d6b](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/a1e2d6b421202f035747b763003bc9a6bb2f8f81))

## [38.0.1](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v38.0.0...v38.0.1) (2024-08-23)


### Bug Fixes

* **core:** fixed semantic check in login-namespace:admin-meta ([4b3e414](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/4b3e4143461ba7367d249d30ecbb8bd3ba226ced))
* **registrar:** changed revoke invitation param name to be consistent with openapi ([70444f9](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/70444f913ea5b4885006d4232cce48f66eec467f))

## [38.0.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v37.7.0...v38.0.0) (2024-08-15)


### ⚠ BREAKING CHANGES

* **core:** edited invitations_status type in DB

### Features

* **core:** allow removal of multiple group relations at once ([f4aeb2f](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/f4aeb2fd92e6a2200d75f3f05a7281066110f77f))
* **core:** attribute modules for eosc beyond login ([25f8fc8](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/25f8fc8de57ad4c69454b6c8c1329fbe4a81a720))
* **core:** getInvitationsPage ([e5a6f32](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/e5a6f329785ffe130bbc46e2f3c1360191630905))
* **registrar:** invitation automatic expiration ([c623c30](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/c623c301af61a69ad18b437982ad4d4af768b711))


### Bug Fixes

* **core:** group sync interval attributes syntax check ([c9605b3](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/c9605b389eb0cb17c547a7e34288eb6951141703))
* **core:** proper fix for group structure sync ([274d816](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/274d8160960e29d3701080302d75fd91d6ebb195))
* **registrar:** application bulk operations now correctly handle MFA exception ([b4d3557](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/b4d3557d5b48e9f6624ec6ca51d5d25c14fc3ea6))

## [37.7.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v37.6.0...v37.7.0) (2024-08-12)


### Features

* **core:** attribute modules for eosc beyond login ([69196ab](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/69196aba679c265f4ab340d44d760883bf2a3645))

## [37.6.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v37.5.0...v37.6.0) (2024-08-02)


### Features

* add RPC methods for revoking an invitation ([323c01b](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/323c01bffc78d2d3cd6ca26db8a95cb245f68b55))
* **core:** add attribute to LDAP scheme and LDAPc mapping ([afe7d46](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/afe7d46114946581b4421727cfb43c616661c5b3))
* **registrar:** add logs to InvitationsManagerBlImpl ([a98564e](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/a98564eb2384f59f65eb1c1d19817e4112b4b4e5))
* **registrar:** add methods for sending out pre-approved invites ([8162957](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/81629579374412b9cdf414d3b995f517a58469ce))
* **registrar:** auto approve application from pre-approved invitation ([832ce0f](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/832ce0ff3e74f4e16e907f2832b0a4cfe4f55a34))
* **registrar:** change authentication in preapproved invitation based on attribute ([bc7aca2](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/bc7aca295b31e75ca97bac82fac116063e797f5b))
* **registrar:** check invitation and pair it with an application ([398e47f](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/398e47f7d75e50c1ff2679a52a372c9d13b34ca9))
* **registrar:** method to extend expiration date of an invitation ([274d90e](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/274d90e1d9eb15f1dd04e7feec6e8963ad2aec9e))


### Bug Fixes

* **core:** remove security image ([a1809b2](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/a1809b2e0c9dc16ee09df3ce5ac605ba66041501))
* **deps:** update dependency google-api-services-admin-directory ([a4e0a2b](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/a4e0a2b682fd46054bf39f9258077cd94bb424f8))
* **deps:** update dependency org.xhtmlrenderer:flying-saucer-pdf to v9.9.0 ([520b311](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/520b3114610883e0ffdb510b5def3fbb8e389872))
* **registrar:** invitation checked to match the group from the application ([19901be](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/19901be8c974d555e6554e38255062d25ed3b8fd))

## [37.5.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v37.4.0...v37.5.0) (2024-07-24)


### Features

* **core:** add attribute to LDAP scheme and LDAPc mapping ([5760f43](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/5760f435e52f60050790b8b750cf2b51c3c61159))

## [37.4.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v37.3.0...v37.4.0) (2024-07-18)


### Features

* **core:** add attributes to LDAP scheme and LDAPc mapping ([2586b16](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/2586b16903c63ddcc9d03ada4edc55ea22e70cba))

## [37.3.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v37.2.0...v37.3.0) (2024-07-18)


### Features

* **registrar:** add new entity Invitation and related manager logic ([70b2d91](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/70b2d91802d71cc4371263f0fd09bb6b6efc0b24))


### Bug Fixes

* **core:** password reset error error formatting in lifescienceid-username ([7fc30f3](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/7fc30f30c2d0f2e6415340386cc15f013a93b1f5))
* **deps:** update dependency commons-cli:commons-cli to v1.8.0 ([ec8cd8e](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/ec8cd8e2b2405ad925a61cd7e4b726a1c4daeb71))
* **deps:** update dependency org.apache.commons:commons-text to v1.12.0 ([6f8af63](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/6f8af6311cfe22cc7526ed16b400c2ba68f8c993))

## [37.2.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v37.1.0...v37.2.0) (2024-06-26)


### Features

* **core:** virtual attribute groudDetailUrl ([4ae2dd7](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/4ae2dd76140a262f4868a9b0c4b366f92413a5d4))


### Bug Fixes

* **core:** properly handle filtering of tcsMails:mu attribute ([745d495](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/745d495dab3df9ddc562bd94a3f72ebb0a9bfe0f))

## [37.1.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v37.0.0...v37.1.0) (2024-06-25)


### ⚠ BREAKING CHANGES

* **registrar:** Add Metacentrum VO as member VO of e-INFRA CZ
* **registrar:** Add IT4I VO as member VO of e-INFRA CZ.
* **core:** updated privilages on existing core attributes
 need to be manually updated on each instance
* **core:** delete user:def:sshPublicAdminKey attribute

### Features

* **cli:** added support for oidc-agent to Python CLI ([cd27222](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/cd27222b8afa4ce353d8f0b27e2d7c1bad5b012e))
* **cli:** added tools for vo hierarchy ([85e6c06](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/85e6c06339f8a052ce297d02cb31f799c26cafd8))
* **core:** fsScratchDirs attribute modules ([6eb2c4e](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/6eb2c4e980cc6753dedb5505cb720ac583c412a5))
* **core:** initialize Perun with UUID attributes ([f293973](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/f293973a5b89d6e3ce31eec6db19e0d52fbda0fa))
* **core:** virtual attribute allowedMailDomains ([d46ac0f](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/d46ac0f84a5a68b6d7fe357a89cb7b62bb9032e5))


### Bug Fixes

* **core:** advertize lifecycle is not alterable in ExtendMembershipException ([5a7bdd4](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/5a7bdd482e5a8dda22eff283aea3b7234134bc22))
* **core:** correctly pass params in WrongReferenceAttributeValueException ([25c954e](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/25c954eceffbe9ccbf4ef2c118b1ae57af10c4cf))
* **core:** hierarchical member status not propagating upwards ([669523b](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/669523b0a5ce0636796fe4d458718daa41428d59))
* **core:** mails_namespace attributes now extend userVirtualAttributeCollectedFromUes ([5517970](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/5517970030672f034b857db04cd9fcaac1dbd7d7))
* **core:** skip logging removing perunadmin when no perunadmin ([29a6957](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/29a69576ba1ee9ef9dc194e1ea257f6a2342ec46))
* **core:** use correct identity to resolve optional mu login ([79d7388](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/79d73888589929902c71c366d127a98a869387dc))
* **deps:** update dependency google-api-services-admin-directory ([084ede0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/084ede002aaa5c5611a4494f10ac7f24a14160f3))
* **deps:** update dependency google-api-services-admin-directory ([a4a9cbd](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/a4a9cbd3843333cf8dc51cae560ce2935366a59e))
* **deps:** update dependency google-api-services-admin-directory ([ce080b7](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/ce080b7ff08812d8a4e05e4183196b4d411b9427))
* **dispatcher:** trigger provisioning on disabling member event ([2d88c43](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/2d88c43d537cc534fcbcf7ddc75719e76bbcbbf8))
* **registrar:** do not add new members of IT4I to e-INFRA CZ ([347e41d](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/347e41dfeffb37cddfea4761c5e2f30bd4c75985))
* **registrar:** do not add new members of Metacentrum to e-INFRA CZ ([51ba470](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/51ba4705c1fa5ac2a172ec57cb6d4d8d841d189f))


### Refactoring

* **core:** remove unused sshPublicAdminKey attribute module ([b5a6fa2](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/b5a6fa2730f20f4c4b8ddf2744f480b264ee89ff))

## [37.0.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v36.0.1...v37.0.0) (2024-06-05)


### ⚠ BREAKING CHANGES

* **core:** all searcher methods now match the strings case-insensitive.
Previously it would depend on the attribute's type.
* db update

### Features

* check admin-meta login is same as einfra ([fa93bd0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/fa93bd0fd0903fd9aee0e5d7e596ad2b07b0d2ad))
* **cli:** changed metadata_url for idm instance in Python CLI ([4727a6c](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/4727a6c39ebf776b34318e9e68d867a29bb809fd))
* **core:** add logging when provisioning user ids to services ([6616c77](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/6616c77b3eb27850968e0355136d626746906114))
* **core:** bucket quota attribute modules ([579fbf8](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/579fbf87e04824b5a8ae7bbb03464d93e9588e96))
* **core:** getMembers searcher method ([63b4720](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/63b4720753461a135c93abe0b76f6f8a7be51339))
* **core:** support basic user search by core attributes ([12d6dad](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/12d6dad519a27729e295f64ee572a52b56d296a7))
* task propagation run logging ([c23dcdb](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/c23dcdbb3ca3e5c6638e14e6c1502a2212244fd9))


### Bug Fixes

* **core:** add missing password strength check in lifescienceid-username ([9bbbd8d](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/9bbbd8df15557d13c1a1eced71c93bb13d77aebf))
* **core:** use correct identity to resolve uco in mu password manager ([fd67204](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/fd672049345a48fed4989447be61ddd3388c0e1f))
* **deps:** update dependency org.xhtmlrenderer:flying-saucer-pdf to v9.8.0 ([8dd7a27](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/8dd7a27b905a16754ee90c9ac506ee847c909cb6))

## [36.0.1](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v36.0.0...v36.0.1) (2024-05-31)


### Bug Fixes

* **core:** use correct identity to resolve uco in mu password manager ([6120f76](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/6120f76d12838bfc8dc6c2e63ed7fa5ad9caec56))

## [36.0.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v35.0.1...v36.0.0) (2024-05-23)


### ⚠ BREAKING CHANGES

* **engine:** new configuration option `perun.archiveSpool` which enables gen file logging
* **core:** Removed module for "user:virt:organizationsWithLoa", delete attr before deploy.

### Features

* **cli:** changed metadata_url for idm_test instance in Python CLI ([5318e3b](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/5318e3b41e2174fbf9bf220eb7ba131ca3cb51b2))
* **core:** allow sending parameters in req body for bulk operations ([96848aa](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/96848aa2a94984a0f9ece87314138ec2d5a079b2))
* **engine:** configurable gen file archival ([cca9914](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/cca991420577ec7d73255668ee4072784ff1f5e0))


### Bug Fixes

* **cli:** printing users common name ([c4d2f4b](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/c4d2f4b9d6035e4c1a8a0814257e0b6c806414dc))
* **core:** addMemberVo mfa check ([24ea162](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/24ea1629f8e93cc847407c4bb9f40f7a03f933c1))
* **core:** cast to Integer exception ([a5cbbee](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/a5cbbeec72394e474d28c8376c8bbccbf6102019))
* **core:** revert offset changes ([8fee705](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/8fee705e7a3a67e956dfd60d51d5c95b779f322a))
* serialization of audit messages ([ebeeccb](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/ebeeccbd2eaddd0d06000aa94e974fd415bac381))


### Refactoring

* **core:** removed user:virt:organizationsWithLoa attribute ([233b1a4](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/233b1a4e2fc22e149344ca082e6b8f484bdb6f62))

## [35.0.1](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v35.0.0...v35.0.1) (2024-05-16)


### Bug Fixes

* serialization of audit messages ([eb9c414](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/eb9c414c57716da4d85cfb42083c301af876b62c))

## [35.0.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v34.4.0...v35.0.0) (2024-05-10)


### ⚠ BREAKING CHANGES

* **core:** new role 'SERVICEACCOUNTCREATOR'
* **core:** All values of libraryIDs attribute will be deleted in LDAP.
* Methods returning new audit messages/events for auditer
consumers now limit maximum number of returned messages in one call
to 10000. Limit is configurable and can be set by
`perun.auditlogReadLimit` property in `perun.properties`.
* **core:** authzResolver/refreshMfa() removed from the API

### Features

* ban manager roles ([19f28ea](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/19f28ea1676d0761d0df111ff98b8205121acda5))
* **cli:** added config for perun-api.acc.aai.e-infra.cz ([4129351](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/41293518e40e55193aad7b35f20858ee3f43f201))
* **core:** new MU eduPersonUniqueId module ([9b9b75a](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/9b9b75a189a5a2e8742e7408dc952f6fbe5f6221))
* **core:** new role SERVICEACCOUNTCREATOR for creating service accounts ([a1a69ce](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/a1a69cebadcc00bee05c73eef7db79e651574647))
* **core:** new sponsor role ([d72640d](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/d72640dbb27afbb92ccde0629bcc015835ec6f8b))
* **gui:** support custom analytics in MU template ([85b562d](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/85b562d5ecb48a22be0a4d2a3b3aae2113e019c9))
* limit max. number of returned audit events/messages in API ([77842fc](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/77842fc827fe9820534dd48f29e7d886b9653f87))
* **registrar:** added module for generating admin-meta login ([226d996](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/226d996ab90a3b1e09255c1848207db1c78ad579))
* **registrar:** mail notification language fallback ([56b9e2a](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/56b9e2a7cf3ab00741e7f60dd18cffd0a8cc02ca))


### Bug Fixes

* **core:** deleteVo correctly throws MFA exception ([b527272](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/b527272b0d634a4ca90a6d6dab8cf0b3ae0c01de))
* **core:** fixed `auth_time` null pointer ([7ad36fe](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/7ad36fe49d6a86ed13c572be6d996aef44359505))
* **core:** optimised DB calls for Users Page ([5add975](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/5add97575cb6bb7924257fa85f149bb060124cd9))
* **core:** the offset for paginated data now adjusts according to the total count of filtered data ([6ed50fc](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/6ed50fc5e849f682ef76b2775a9f36ef2610caf3))
* **core:** throw exception when pwdreset token is not UUID ([a5b53cf](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/a5b53cf5846a19771488abf36f906f9acdf7ca75))
* **deps:** update dependency google-api-services-admin-directory ([74a63dd](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/74a63dd86561309b61e7e01eb9d142c76e894fe3))
* **deps:** update dependency google-api-services-admin-directory ([5a9fc48](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/5a9fc484525b0c665c9741f4a8f72a336e359d98))
* **deps:** update dependency google-api-services-admin-directory ([f7b966c](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/f7b966c6fcb17e36137084ccfb12a11110f2d675))
* **deps:** update dependency org.bouncycastle:bcmail-jdk18on to v1.78.1 ([f223f42](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/f223f427588206d56b62f358935a307d1cd6785b))
* **gui:** allow setting files quota ([0c3593d](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/0c3593d737126b91119a151b897e083f3ca9f365))
* **gui:** handle MFA exceptions in GUI ([101a3cd](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/101a3cdd2075de266798e30680e3a706362c638d))


### Refactoring

* **core:** mark LDAP attribute libraryIDs as deleted ([361186d](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/361186dfe416af4f789299d570737f2879452023))
* **core:** remove unused refreshMfa() ([d38829f](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/d38829f775f4cbe4703ac7c1df53121ef54acec0))

## [34.4.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v34.3.3...v34.4.0) (2024-04-15)


### Features

* **core:** extend eduPersonORCID attribute ([259c617](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/259c6173af8d42c7075cbaa0682a4c167f5893ff))
* **core:** sponsor optimization ([a444fb7](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/a444fb7951c8812bdc8983a86491dc64a94e6d1b))
* **openapi:** described methods for getting and updating a form item ([48cbaf4](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/48cbaf46474ca82d221815ca70919fdcd5dcd073))


### Bug Fixes

* **core:** assign category for publications with categoryId=0 ([8abcf3b](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/8abcf3bb1e2bb2fdbc0c1f493e81108567dfd5bd))
* **core:** do not check mfa for not yet created ues ([16d55a3](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/16d55a32640c17a7a194de126bce622eb1806193))
* **core:** json mapping for groups in ExtSourceIT4I ([6befecb](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/6befecb3b6643b0f306b44d877597f0bba641904))
* **core:** json mapping in ExtSourceIT4I ([9a7c4a7](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/9a7c4a70a48dbe4225085024d769412b9bfeef8f))
* **core:** log last processed message ID in LDAPc and Auditlogger ([1739194](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/1739194d496513829497d1b3bd2384d777ea913b))
* **core:** proper authorization when updating group ([0ea8092](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/0ea809246ebbcef1406e928595b849b88ce61c8a))
* **deps:** update dependency org.xhtmlrenderer:flying-saucer-pdf to v9.7.2 ([e486e06](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/e486e069dce1db1f87be5fbe2133afcee1c0e2c0))
* **gui:** handle both 503 a 502 http codes the same ([418f408](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/418f408d2dfcf0a6038764d18c4a9af06a3a3d79))

## [34.3.3](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v34.3.2...v34.3.3) (2024-04-05)


### Bug Fixes

* **core:** json mapping for groups in ExtSourceIT4I ([33a396b](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/33a396b7b868cd46329de193f0271c7a67375d4d))

## [34.3.2](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v34.3.1...v34.3.2) (2024-04-04)


### Bug Fixes

* **core:** json mapping in ExtSourceIT4I ([f69ced3](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/f69ced36ee62ded1e0a502bc1f2671886e0ab319))

## [34.3.1](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v34.3.0...v34.3.1) (2024-03-25)


### ⚠ BREAKING CHANGES

* **core:** Remove unused perun-cabinet.properties files and config.

### Bug Fixes

* **core:** mfa skippable roles ([5d10c84](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/5d10c8447a750face5f75b0dbbba7b51ad35acff))
* **deps:** update dependency google-api-services-admin-directory ([796d3e1](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/796d3e137448e99684a669215c53f62721ff9fc9))
* **deps:** update dependency io.swagger:swagger-annotations to v1.6.13 ([9cdf042](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/9cdf042b53075facc6c8a05184f8520004005bb5))
* **deps:** update dependency io.swagger:swagger-annotations to v1.6.14 ([fafa0b1](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/fafa0b1f36d57ea321ca693905fd02ebb549803c))
* **deps:** update dependency org.xhtmlrenderer:flying-saucer-pdf to v9.6.1 ([783de60](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/783de6085f3abf4739e631c6d0888834b3bc40a8))
* **deps:** update dependency org.xhtmlrenderer:flying-saucer-pdf to v9.7.0 ([ca50d9e](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/ca50d9ed20aced737f2142529b89d754700fc832))
* **deps:** update dependency org.xhtmlrenderer:flying-saucer-pdf to v9.7.1 ([4e2af7e](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/4e2af7ed24a3ca19c19d3abe9c72538325180cd1))


### Refactoring

* **core:** removed usage of perun-cabinet.properties ([3acddd4](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/3acddd4a2f940c870333e8738fdd7cc092ed8704))

## [34.3.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v34.2.0...v34.3.0) (2024-03-11)


### ⚠ BREAKING CHANGES

* **core:** Existing Metacentrum IdP UES entries should be removed from all users.

### Features

* **core:** new extSource type JSON ([113ec07](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/113ec07fcc2e9586cd4d10b447290eac1ca70f08))


### Bug Fixes

* **core:** limit throwing BanNotExistsException ([598d728](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/598d7285cf572e706b858c4a8ae691b4724917f4))


### Refactoring

* **core:** do not generate metacentrum idp identity ([aac6d21](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/aac6d21e00705ae7d7db301dc49ce66c4cab91c5))

## [34.2.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v34.1.1...v34.2.0) (2024-03-01)


### Features

* **core:** add attributes to LDAP scheme and Perun initialization ([a48ff3c](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/a48ff3cbd2c6cbb438c82daae9983d74d48c952a))
* **core:** attribute module for eosc login ([94c8398](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/94c8398c2c71232e8ddfda781f2f0f9e3e75666f))
* **registrar:** escape app items before HTML notification ([62f58aa](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/62f58aae75014365885688b0d36c159a36dfd3b7))


### Bug Fixes

* **cli:** removed duplicities in getMethods ([e51b0f0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/e51b0f0e50bd1a38b2d387c2908d1dccff56987e))
* **core:** changed response of forceServicePropagationForHostname ([6ebdf35](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/6ebdf353790fb5ea09075fa8a734bbee0848bb6a))
* **core:** check syntax of expiration attributes for VSUP ([7edea8e](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/7edea8e94c8e02fcda87d9cc565c535f8a55da6c))
* **core:** filter policies ([986a4ec](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/986a4ecd3d0ab00b628eded60f3bf608eaf8c620))
* **registrar:** fix affiliation autoapprove without VO or Group regex ([59757d4](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/59757d42fca2230c9ef48b18c1e1fffe17368dff))

## [34.1.1](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v34.1.0...v34.1.1) (2024-02-28)


### Bug Fixes

* **core:** changed response of forceServicePropagationForHostname ([9191ab4](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/9191ab417955296f0da7cfc4b18935669f33b631))

## [34.1.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v34.0.0...v34.1.0) (2024-02-16)


### Features

* **core:** attribute module for eosc login ([a6e7c2f](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/a6e7c2fbba4d86628b5ac06d1e03d28dfa6401b4))

## [34.0.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v33.3.0...v34.0.0) (2024-02-16)


### ⚠ BREAKING CHANGES

* **cli:** Add methods to HashedGenData module
* **core:** (reject/approve/delete)Applications now return a list of
ApplicationOperationResult
* **registrar:** Remove unused properties `perunUrl` and `fedAuthz` from `perun-registrar-lib.properties`.

### Features

* **cli:** getGroupIdsForMember for hashGenData ([ccb88e6](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/ccb88e6bdc9954709a9003cf0a0d6b0e624d20e9))
* **core:** allow partial success for bulk operations with applications ([1ca1e0c](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/1ca1e0ce419be18fff67f0f546af6a2a1b1ab8da))
* **core:** check for trailing and leading spaces when creating entities ([c8c608b](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/c8c608ba03b39129cf8b3912c265adb2d3ed0ba5))
* **core:** new API method forceServicePropagationForHostname() ([83f79bb](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/83f79bb22cfc59263d016e1898aa86575a0840c3))


### Bug Fixes

* **registrar:** inform about autocompleting/changing the HTML input during the sanitization ([5949f9d](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/5949f9d23e0e1a73c0f8bd03311a15e8106bb474))
* **registrar:** mailManager not escaping html properly ([7e19f57](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/7e19f5776b007b934ac1c81ccbea4cc9e035cdda))
* **registrar:** use internal session when checking form items ([268d27c](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/268d27c87b58b06d41785f373234ed8f0fd9689d))


### Refactoring

* **registrar:** removed perunUrl and fedAuthz from registrar config ([3828d12](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/3828d12e5232f06b607efd3379c0d00d4f17f73b))

## [33.3.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v33.2.0...v33.3.0) (2024-01-30)


### ⚠ BREAKING CHANGES

* **registrar:** registrar config property perunUrl isn't used anymore as url replacement fallback in notifications (default brand is used instead)
* **core:** Principal `perunController` removed from default perun admins.

### Features

* **core:** auto approve registrar module ([6a71961](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/6a71961b042af8c1b1f986ccc74dce9c27c6eef8))
* **core:** methods for creating ues and uesAttributes in one call ([d2cc0b1](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/d2cc0b1c9c4d7e028df7573bb800f41f55895ad6))
* **core:** new envri id login module ([2362e48](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/2362e4867c4619b585d7f877b2ea4b15c30a71eb))
* **registrar:** make the method for creating an invitation URL available in RPC/openapi ([0bedde4](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/0bedde487c007c9f40f516d4d2f72cb33ecd0b52))
* **registrar:** module to approve based on affiliation ([f02437f](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/f02437f8194c8898b8e56eafdb434da3ce973e25))
* **registrar:** publish method for async html input check from GUI ([0d414d0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/0d414d0aad0c7374f026d0fcd14225a0c6117a28))


### Bug Fixes

* correct print in Python CLI ([aeaa624](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/aeaa6248374a5a4d422644aa258840054f8b42c2))
* **deps:** update dependency google-api-services-admin-directory to directory_v1-rev20240102-2.0.0 ([63b3bb0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/63b3bb0a3030e7fc16597ed10547f17ee2489102))
* **deps:** update dependency org.jboss.javaee:jboss-jms-api to v1.1.0.20070913080910 ([634f2d6](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/634f2d67cf95fe1c6aff8ed676c0bcb1b4df1de9))
* **deps:** update dependency org.reflections:reflections to v0.9.12 ([7abd1fd](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/7abd1fdfdc6e0bc7be95bd7e1b440bfdcca4da28))
* **deps:** update dependency org.xhtmlrenderer:flying-saucer-pdf to v9.5.1 ([fae8f4c](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/fae8f4c2a74f9a98c55e17398586d1cc6a7e9e5a))
* mark securityImage as deleted in LDAP ([41f5fdd](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/41f5fddfd304b1efb1c37ce5e9954b9e25538b14))


### Others

* **core:** removed perunController principal from default perun admins ([35f330c](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/35f330ce9656ee9baabd05e57c7b86a65e4c777a))

## [33.2.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v33.1.0...v33.2.0) (2024-01-24)


### ⚠ BREAKING CHANGES

* **registrar:** registrar config property perunUrl isn't used anymore as url replacement fallback in notifications (default brand is used instead)

(cherry picked from commit 0bedde487c007c9f40f516d4d2f72cb33ecd0b52)

### Features

* **core:** new envri id login module ([d03573a](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/d03573ac5edb87d9ecdc7e0601e5beccfaebb079))
* **registrar:** make the method for creating an invitation URL available in RPC/openapi ([9596e4a](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/9596e4af4201db404afc41701584e76cefd51740))


### Bug Fixes

* correct print in Python CLI ([5e4291b](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/5e4291bf6b1f996dd46fd4aa84b884028742d285))

## [33.1.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v33.0.2...v33.1.0) (2024-01-15)


### ⚠ BREAKING CHANGES

* **registrar:** HTML is now allowed for checkbox labels, check existing form items for potential HTML. Config property forceHTMLSanitization needs to be true for sanitization

### Features

* **core:** create method that retrieves RichMembers for all members from vo who are service users ([9c329c0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/9c329c06efcd42e5e6a8d4dbe1a939a0acabb63d))
* **registrar:** allow and sanitize html in checkbox labels ([f21a51e](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/f21a51ec07239af25fce69dcb44dc8fe74470386))


### Bug Fixes

* 🐛 URL building in Registrar lib ([554d467](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/554d467f8541baa807c394e042c8d1d778cdf6c3))
* **core:** concurrency modification exception on reading authz roles keys ([3a9be37](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/3a9be37fe1e9b60db727bca42f2f26d4fb179799))
* **core:** disallow combination of sync and expiration rules ([8e102cd](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/8e102cd220f00eb8eceeb44c7631bf42d747735a))
* **core:** getting sponsored members throwing exception when none present ([bbe9069](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/bbe9069e73987fb46804aa739ad14d6fed29fecf))
* **core:** group with any manager roles is now correctly deleted only with force delete ([f33c6d5](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/f33c6d509c2ac09c170a40152841fa870adaf2eb))
* **core:** reinitialize dependencies on attribute creation ([273db18](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/273db1877f6ec399c9486764e9e02d54451f7ac8))
* **core:** return correct total count of groups ([e2837a5](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/e2837a511652e4fca4c6b134e1584f124327d936))
* **core:** sorting by organization and email for paginated group members ([37fecb8](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/37fecb837dcea622aef673ed43d888f882e5d9ce))
* **core:** update roles policies to match previous admin method privileges ([1f15398](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/1f15398411df1dc6b4b5079345c932cc1cff5e6a))
* **core:** use correct attribute in m365LicenseGroup check ([25d3b37](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/25d3b37c827ff97c5f8e1ceaa05f99b7bcf33828))
* **deps:** update dependency org.xhtmlrenderer:flying-saucer-pdf to v9.4.1 ([171aa38](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/171aa387c5b019114a228970713c4b0d314436d3))
* **deps:** updated flying-saucer-pdf to v9.4.0 ([fe7437f](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/fe7437f4a1ac460c07ed3d836760b337d0623832))
* **deps:** updated spring and other dependencies ([a107b72](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/a107b722279caa94d7bf2ebd3b9e9cacf69bd3b4))
* hande null correctly in enabledO365MailForward ([fa27a22](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/fa27a2268097d3c6c82ec24b1d655421f03af848))
* **registrar:** correctly replace {appDetailUrl} ([fd3870b](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/fd3870b6024c80f61569367c889ccc7d937b936e))
* **registrar:** correctly replace {appGuiUrl} ([91c4af2](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/91c4af2d09aba6825b4f6ab22a4b1263db90858c))

## [33.0.2](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v33.0.1...v33.0.2) (2024-01-10)


### Bug Fixes

* **core:** update roles policies to match previous admin method privileges ([c374e3c](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/c374e3c04115689173dee65411c4f2b4bddcf840))
* **registrar:** correctly replace {appDetailUrl} ([9d6ed79](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/9d6ed79a8404b794f7d28c0e3877eab2d338c72d))
* **registrar:** correctly replace {appGuiUrl} ([8ab97ba](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/8ab97ba6a36dd405c9730e2a6d1705df7e5d8417))

## [33.0.1](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v33.0.0...v33.0.1) (2024-01-02)


### Bug Fixes

* 🐛 URL building in Registrar lib ([8617623](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/8617623211dd2cdbb3ea070c76726aac87e782d3))

## [33.0.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v32.0.4...v33.0.0) (2023-12-04)


### ⚠ BREAKING CHANGES

* **core:** added uuid column to facilities and vo tables
* **core:** new config property externalProgramsDependencies
with value a list of program names required by perun

### Features

* **core:** added uuids to VO and Facility ([023e592](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/023e59288d0cbaa51a10e11acca5750b5710500a))
* **core:** generic JSON service fixes ([ba1d93e](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/ba1d93e8ccb1c46e3c789c22b90563e0d8cb28b2))
* **core:** required external dependencies check when starting perun ([4fdb698](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/4fdb69877f9ab3b3f02b8e207748fa66117b6a2c))


### Bug Fixes

* 🐛 Fix possible NullPointerException when replacing in tpls ([056d8c7](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/056d8c70d1350285b743bc6c8a5a319fed508e98))

## [32.0.4](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v32.0.3...v32.0.4) (2023-11-24)


### Bug Fixes

* remove filepath from .releaserc.json ([413c569](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/413c569ecc12c63748ae000c76124b0b23f9184e))

## [32.0.3](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v32.0.2...v32.0.3) (2023-11-24)


### Bug Fixes

* label for release assets ([d80bacf](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/d80bacf95da75c769aa88c9245a16cdc0bff9d93))

## [32.0.2](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v32.0.1...v32.0.2) (2023-11-24)


### Bug Fixes

* upload assets as generic package instead ([1462574](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/14625749263e7174fe7012f0162d828286ba8989))

## [32.0.1](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v32.0.0...v32.0.1) (2023-11-24)


### Bug Fixes

* empty commit to trigger release ([87ff041](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/87ff041e10d183fbda23cbeed493e361918a5596))

## [32.0.0](https://gitlab.ics.muni.cz/perun/perun-idm/perun/compare/v31.0.0...v32.0.0) (2023-11-23)


### ⚠ BREAKING CHANGES

* **core:** Methods for obtaining admins return only users with valid
status in the authorized group and the corresponding VO.
* **core:** possibility to synchronize group status.
Expects "status" column in SQLExtSource with values "EXPIRED" or "VALID".
Candidate object extended with expectedSyncGroupStatus property.

### Features

* **core:** add attribute modules for m365 ([a88c5da](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/a88c5dabf437ed9c986a36dae56195bfa51a6ad0))
* **core:** optimize getAllSponsoredMembersWithSponsors method ([e1f1064](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/e1f10645a52b1fbd0c21435cde9fc082c0ba3279))
* **core:** synchronize group membership status ([e64478a](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/e64478a76c232290bcf8e104ba17ee880295b58d))


### Bug Fixes

* change references from GitHub to GitLab ([9cd1816](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/9cd181692134d80dd7fda6b7c6e1629327106e23))
* **core:** admins-related methods ([d364007](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/d3640079c40d072ed9a578dcfb849ca354bc07f3))
* empty commit to trigger first GitLab release ([fbcfc98](https://gitlab.ics.muni.cz/perun/perun-idm/perun/commit/fbcfc98fb99021036a926316d6b75ac06c2ed456))

## [31.0.0](https://github.com/CESNET/perun/compare/v30.1.1...v31.0.0) (2023-11-07)


### ⚠ BREAKING CHANGES

* **core:** the `ssh-keygen` tool has to be available on instance machines
* **core:** edit new config property perun.mailchange.replyTo and existing perun.mailchange.backupFrom to customize the respective fields of sent emails (from core API).
 `replyTo` (and `replyToName`) can be defined in `perun-registrar-lib.properties` to achieve the same for registrar
* **core:** Method blockServicesOnDestinations does not throw
ServiceAlreadyBannedException anymore.

### Features

* **core:** allow customization of replyTo field of emails ([1f20e82](https://github.com/CESNET/perun/commit/1f20e82eed7f7a537422ca812866065e09a441b2))
* **core:** bulk-up public ssh key validation ([64aaa86](https://github.com/CESNET/perun/commit/64aaa86972902405a73f771bdd13dd93d7e9723d))
* **core:** filter by role ([84e0ccd](https://github.com/CESNET/perun/commit/84e0ccd9e7a2587ef12daa6c89dc3d00aa398782))
* **engine:** pass service name to send/gen script when using generic scripts ([7c74749](https://github.com/CESNET/perun/commit/7c747498b042b938dadb8b58d3ee411faad99879))


### Bug Fixes

* 🐛 Fix BBMRIResources registration module possible NullPExc ([bfb3e6a](https://github.com/CESNET/perun/commit/bfb3e6ac49bb2a2f5b990dc04da11cd19934ce58))
* 🐛 Use getAllSubgGroups in BBMRIResources reg.module ([3fdcffc](https://github.com/CESNET/perun/commit/3fdcffcc86f2343ba14f7634bd803dc3a9dde320))
* **core:** group admin/membership manager should not have rights for verifying users' mail address ([53dbe02](https://github.com/CESNET/perun/commit/53dbe026b106e0b6dca95b88bba739449e1b4b2f))
* **core:** ignore already blocked destination ([ad1774d](https://github.com/CESNET/perun/commit/ad1774d04b990249ee6c4438fa0bfa56b863bbac))
* correct attribute references in enabledO365MailForward ([c52f15c](https://github.com/CESNET/perun/commit/c52f15c5f5cb02064dd90cd2e349dbb7a4d7ea6c)), closes [ST-1168](https://perunaai.atlassian.net/browse/ST-1168)
* **deps:** update dependency com.google.apis:google-api-services-admin-directory to directory_v1-rev20231005-2.0.0 ([bb691b0](https://github.com/CESNET/perun/commit/bb691b02e4273c9b1ab1777bc4c1d89ea01e5876))
* **deps:** update dependency commons-cli:commons-cli to v1.6.0 ([11c038b](https://github.com/CESNET/perun/commit/11c038be066786ff57ce5148a1cba3b578d4b487))
* **deps:** update dependency io.swagger:swagger-annotations to v1.6.12 ([605aa63](https://github.com/CESNET/perun/commit/605aa6334ca9fb21ac09ded84ee0419815411bf8))
* **deps:** update dependency org.json:json to v20231013 [security] ([c207e8b](https://github.com/CESNET/perun/commit/c207e8b61a25e269d18439671d0841a3e3a9dffe))
* **deps:** update dependency org.springframework.boot:spring-boot-starter-parent to v2.7.17 ([6d6b4e5](https://github.com/CESNET/perun/commit/6d6b4e5214b5da74c66fe8567a76d04cd9412d9c))
* **gui:** typo in candidate title after param ([ae6f8a7](https://github.com/CESNET/perun/commit/ae6f8a7172bb90c08fe3f0110a667782e96221d7))
* **registrar:** log error on submitted embedded aplications ([6587daf](https://github.com/CESNET/perun/commit/6587dafbb4e5b57b5d480b25a83c337eda42695b))
* **registrar:** pass registrar session when submitting embedded applications ([ce6bb52](https://github.com/CESNET/perun/commit/ce6bb52dc1a202fcc57705fce4cc5436920d7dd9))
* **registrar:** transaction for approving multiple applications ([369fcd3](https://github.com/CESNET/perun/commit/369fcd33db8f1b8f24af1f00668edc3477861847))

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
