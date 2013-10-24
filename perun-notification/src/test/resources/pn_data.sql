CREATE TABLE pn_audit_message (
    message character varying(4000),
    id bigint NOT NULL
);


CREATE TABLE pn_object (
    id bigint NOT NULL,
    name character varying(256),
    properties character varying(4000),
    class_name character varying(256)
);


CREATE TABLE pn_pool_message (
    id bigint NOT NULL,
    regex_id bigint NOT NULL,
    template_id bigint NOT NULL,
    key_attributes character varying(4000) NOT NULL,
    created timestamp NOT NULL,
    notif_message character varying(1000) NOT NULL,
    locale character varying(5)
);


CREATE TABLE pn_receiver (
    id bigint NOT NULL,
    target character varying(1000) NOT NULL,
    type_of_receiver character varying(256) NOT NULL,
    template_id bigint NOT NULL
);


CREATE TABLE pn_regex (
    id bigint NOT NULL,
    note character varying(256),
    regex character varying(4000) NOT NULL
);


CREATE TABLE pn_regex_object (
    object_id bigint NOT NULL,
    regex_id bigint NOT NULL,
    id bigint NOT NULL
);


CREATE TABLE PN_TEMPLATE (
    id bigint NOT NULL,
    primary_properties character varying(4000) NOT NULL,
    notify_trigger character varying(100),
    youngest_message_time bigint,
    oldest_message_time bigint,
    locale character varying(512),
    sender character varying(4000),
    name character varying(256)
);


CREATE TABLE pn_template_message (
    id bigint NOT NULL,
    template_id bigint NOT NULL,
    locale character varying(5) NOT NULL,
    message character varying(4000),
    subject character varying(512)
);


CREATE TABLE pn_template_regex (
    regex_id bigint NOT NULL,
    template_id bigint NOT NULL,
    id bigint NOT NULL
);


ALTER TABLE pn_audit_message
    ADD CONSTRAINT perun_notif_message_id PRIMARY KEY (id);

ALTER TABLE pn_object
    ADD CONSTRAINT perun_notif_object_id PRIMARY KEY (id);

ALTER TABLE pn_pool_message
    ADD CONSTRAINT perun_notif_pool_message_id PRIMARY KEY (id);

ALTER TABLE pn_regex
    ADD CONSTRAINT perun_notif_regex_id PRIMARY KEY (id);

ALTER TABLE pn_template
    ADD CONSTRAINT perun_notif_template_id PRIMARY KEY (id);

ALTER TABLE pn_template_message
    ADD CONSTRAINT perun_notif_template_message_primary_key PRIMARY KEY (id);

ALTER TABLE pn_template_regex
    ADD CONSTRAINT perun_notif_template_regex_id PRIMARY KEY (id);

ALTER TABLE pn_receiver
    ADD CONSTRAINT pn_receiver_id PRIMARY KEY (id);

ALTER TABLE pn_regex_object
    ADD CONSTRAINT pn_regex_object_id PRIMARY KEY (id);

ALTER TABLE pn_pool_message
    ADD CONSTRAINT perun_notif_pool_message_template_id FOREIGN KEY (template_id) REFERENCES pn_template(id);

ALTER TABLE pn_template_message
    ADD CONSTRAINT perun_notif_template_message_template_id FOREIGN KEY (template_id) REFERENCES pn_template(id) DEFERRABLE;

ALTER TABLE pn_template_regex
    ADD CONSTRAINT perun_notif_template_regex_regex_id_fkey FOREIGN KEY (regex_id) REFERENCES pn_regex(id);

ALTER TABLE pn_template_regex
    ADD CONSTRAINT perun_notif_template_regex_template_id_fkey FOREIGN KEY (template_id) REFERENCES pn_template(id);

ALTER TABLE pn_receiver
    ADD CONSTRAINT pn_receiver_template_id FOREIGN KEY (template_id) REFERENCES pn_template(id);

ALTER TABLE pn_regex_object
    ADD CONSTRAINT pn_regex_object_object_id FOREIGN KEY (object_id) REFERENCES pn_object(id);

ALTER TABLE pn_regex_object
    ADD CONSTRAINT pn_regex_object_regex FOREIGN KEY (regex_id) REFERENCES pn_regex(id);

CREATE SEQUENCE pn_audit_message_id_seq;
CREATE SEQUENCE pn_object_id_seq;
CREATE SEQUENCE pn_pool_message_id_seq;
CREATE SEQUENCE pn_receiver_id_seq;
CREATE SEQUENCE pn_regex_id_seq;
CREATE SEQUENCE pn_regex_object_seq;
CREATE SEQUENCE pn_template_id_seq;
CREATE SEQUENCE pn_template_message_id_seq;
CREATE SEQUENCE pn_template_regex_seq;
