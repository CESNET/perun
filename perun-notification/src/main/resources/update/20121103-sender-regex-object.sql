begin;

drop table pn_template_object;

CREATE TABLE pn_template_message
(
  id bigint NOT NULL,
  template_id bigint NOT NULL,
  locale character varying(5) NOT NULL,
  message character varying(4000),
  CONSTRAINT perun_notif_template_message_primary_key PRIMARY KEY (id),
  CONSTRAINT perun_notif_template_message_template_id FOREIGN KEY (template_id)
      REFERENCES pn_template (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION DEFERRABLE INITIALLY IMMEDIATE
)

drop sequence pn_template_object_seq;

alter table pn_template add column sender character varying (4000);

alter table pn_object add column class_name character varying (256);

CREATE SEQUENCE pn_regex_object_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

end;