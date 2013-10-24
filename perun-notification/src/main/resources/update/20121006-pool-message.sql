begin;

alter table pn_pool_message drop column other_attributes;

alter table pn_pool_message add column notif_message character varying(1000) NOT NULL;

alter table pn_template drop column secondary_properties;

end;