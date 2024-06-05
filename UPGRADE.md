Upgrade notes

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
