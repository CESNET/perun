begin;

CREATE TABLE pn_template
(
  id bigint NOT NULL,
  message text,
  primary_properties character varying(4000) NOT NULL,
  secondary_properties character varying(4000),
  oldest_message_time bigint,
  youngest_message_time bigint,
  subject character varying(256),
  CONSTRAINT pn_template_id PRIMARY KEY (id )
);

CREATE SEQUENCE pn_template_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE TABLE pn_message
(
  message character varying(4000),
  id bigint NOT NULL,
  CONSTRAINT pn_message_id PRIMARY KEY (id )
);

CREATE SEQUENCE pn_message_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE TABLE pn_pool_message
(
  id bigint NOT NULL,
  regex_id bigint NOT NULL,
  template_id bigint NOT NULL,
  key_attributes character varying(4000) NOT NULL,
  other_attributes character varying(4000),
  created timestamp without time zone NOT NULL,
  CONSTRAINT pn_pool_message_id PRIMARY KEY (id ),
  CONSTRAINT pn_pool_message_template_id FOREIGN KEY (template_id)
      REFERENCES pn_template (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE SEQUENCE pn_pool_message_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE TABLE pn_regex
(
  id bigint NOT NULL,
  note character varying(256),
  regex character varying(4000) NOT NULL,
  CONSTRAINT pn_regex_id PRIMARY KEY (id )
);

CREATE SEQUENCE pn_regex_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE TABLE pn_object
(
  id bigint NOT NULL,
  name character varying(256),
  properties character varying(4000),
  CONSTRAINT pn_object_id PRIMARY KEY (id )
);
  
CREATE SEQUENCE pn_object_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
  
CREATE TABLE pn_reciever
(
  id bigint NOT NULL,
  target character varying(256) NOT NULL,
  type_of_reciever character varying(256) NOT NULL,
  template_id bigint NOT NULL,
  CONSTRAINT pn_reciever_id PRIMARY KEY (id ),
  CONSTRAINT pn_reciever_template_id FOREIGN KEY (template_id)
      REFERENCES pn_template (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE SEQUENCE pn_reciever_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
  
CREATE TABLE pn_template_regex
(
  regex_id bigint NOT NULL,
  template_id bigint NOT NULL,
  id bigint NOT NULL,
  CONSTRAINT pn_template_regex_id PRIMARY KEY (id ),
  CONSTRAINT pn_template_regex_regex_id_fkey FOREIGN KEY (regex_id)
      REFERENCES pn_regex (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT pn_template_regex_template_id_fkey FOREIGN KEY (template_id)
      REFERENCES pn_template (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE pn_template_object
(
  template_id bigint NOT NULL,
  object_id bigint NOT NULL,
  id bigint NOT NULL,
  CONSTRAINT pn_template_object_id PRIMARY KEY (id ),
  CONSTRAINT pn_template_object_object FOREIGN KEY (object_id)
      REFERENCES pn_object (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT pn_template_object_template FOREIGN KEY (template_id)
      REFERENCES pn_template (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

end;