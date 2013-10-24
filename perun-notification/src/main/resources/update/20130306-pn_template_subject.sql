alter table pn_template_message add column subject character varying (512);

alter table pn_template drop column subject;