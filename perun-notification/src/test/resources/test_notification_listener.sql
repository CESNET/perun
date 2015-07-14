INSERT INTO pn_template VALUES (-4, 'cz.metacentrum.perun.core.api.Member=getId();METHOD=getAttributesManagerBl().getAttribute(cz.metacentrum.perun.core.api.PerunSession, getUsersManagerBl().getUserByMember(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.Member), "urn:perun:user:attribute-def:def:preferredLanguage").getValue().equals("en")/getUsersManagerBl().getUserByMember(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.Member).getId()/getAttributesManagerBl().getAttribute(cz.metacentrum.perun.core.api.PerunSession, getUsersManagerBl().getUserByMember(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.Member), "urn:perun:user:attribute-def:def:preferredLanguage").getValue().equals("cs")',
'all_regex_ids', 10, 20, 'getAttributesManagerBl().getAttribute(cz.metacentrum.perun.core.api.PerunSession, getUsersManagerBl().getUserByMember(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.Member), "urn:perun:user:attribute-def:def:preferredLanguage").getValue()'
, 'noreply@meta.cz', NULL);

INSERT INTO pn_template_message VALUES (-1, -4, 'en', 'Good day,
  thank you for Your registration to virtual organization MetaCentrum VO,
activity MetaCentrum association CESNET, which focuses on sophisticated computation.

  Name: ${perun.getUsersManagerBl().getUserByMember(perunSession, retrievedObjects[-52"]["Member"]).getFirstName()}<br/>
  Surname: ${perun.getUsersManagerBl().getUserByMember(perunSession, retrievedObjects["-52"]["Member"]).getLastName()}<br/>

  Accounts are valid on machines till $membershipExpiration', 'Subject');

INSERT INTO pn_template_message VALUES (-2, -4, 'cs', 'Dobrý den,
  děkujeme za Vaši registraci do virtualni organizace MetaCentrum VO,
aktivity MetaCentrum sdružení CESNET, zaměřené na náročné výpočty.

  Váš účet je nyní propagován na všechny servery, plně funkční bude
během hodiny.

  Jméno: ${perun.getUsersManagerBl().getUserByMember(perunSession, retrievedObjects["-52"]["Member"]).getFirstName()}<br/>
  Přijmení: ${perun.getUsersManagerBl().getUserByMember(perunSession, retrievedObjects["-52"]["Member"]).getLastName()}<br/>

Jazyk: ${perun.getAttributesManagerBl().getAttribute(perunSession, perun.getUsersManagerBl().getUserByMember(perunSession, retrievedObjects["-52"]["Member"]), "urn:perun:user:attribute-def:def:preferredLanguage").getValue()}', 'Předmět');


INSERT INTO pn_regex VALUES (-62, 'Member validated', 'Member:\[.*\]* validated.');
INSERT INTO pn_regex VALUES (-52, 'Member created', 'Member:\[.*\]* created.');

INSERT INTO pn_template_regex VALUES (-52, -4, -4);
INSERT INTO pn_template_regex VALUES (-62, -4, -5);

INSERT INTO pn_receiver VALUES (-4, 'getUsersManagerBl().getUserByMember(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.Member).getId()', 'EMAIL_USER', -4, 'cs');

INSERT INTO pn_object VALUES (-33, 'METHOD', 'getAttributesManagerBl().getAttribute(cz.metacentrum.perun.core.api.PerunSession, getUsersManagerBl().getUserByMember(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.Member), "urn:perun:user:attribute-def:def:preferredLanguage").getValue().equals("cs");getAttributesManagerBl().getAttribute(cz.metacentrum.perun.core.api.PerunSession, getUsersManagerBl().getUserByMember(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.Member), "urn:perun:user:attribute-def:def:preferredLanguage").getValue().equals("en")', NULL);
INSERT INTO pn_object VALUES (-19, 'METHOD', 'getUsersManagerBl().getUserByMember(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.Member).getLastName()', NULL);
INSERT INTO pn_object VALUES (-21, 'METHOD', 'getUsersManagerBl().getUserByMember(cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.Member).getFirstName()', NULL);
INSERT INTO pn_object VALUES (-32, 'cz.metacentrum.perun.core.api.Group', 'getUserId;getName();getId;getDescription', 'cz.metacentrum.perun.core.api.Group');
INSERT INTO pn_object VALUES (-31, 'cz.metacentrum.perun.core.api.Member', 'getUserId', 'cz.metacentrum.perun.core.api.Member');
INSERT INTO pn_object VALUES (-2, 'cz.metacentrum.perun.core.api.Group', 'getName;getDescription;getId', 'cz.metacentrum.perun.core.api.Group');
INSERT INTO pn_object VALUES (-1, 'cz.metacentrum.perun.core.api.Member', 'getUserId', 'cz.metacentrum.perun.core.api.Member');

INSERT INTO pn_regex_object VALUES (-1, -52, -31);
INSERT INTO pn_regex_object VALUES (-2, -62, -31);
