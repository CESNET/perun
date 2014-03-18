CREATE SEQUENCE pn_object_id_seq maxvalue 1.0000E+28;
CREATE SEQUENCE pn_pool_message_id_seq maxvalue 1.0000E+28;
CREATE SEQUENCE pn_reciever_id_seq maxvalue 1.0000E+28;
CREATE SEQUENCE pn_regex_id_seq maxvalue 1.0000E+28;
CREATE SEQUENCE pn_template_id_seq maxvalue 1.0000E+28;
CREATE SEQUENCE pn_audit_message_id_seq maxvalue 1.0000E+28;
CREATE SEQUENCE pn_template_regex_seq maxvalue 1.0000E+28;
CREATE SEQUENCE pn_template_message_id_seq maxvalue 1.0000E+28;
CREATE SEQUENCE pn_regex_object_seq maxvalue 1.0000E+28;



ALTER TABLE  pn_audit_message
    ADD CONSTRAINT pn_message_id PRIMARY KEY (id);


--
-- Name: pn_object_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE  pn_object
    ADD CONSTRAINT pn_object_id PRIMARY KEY (id);


--
-- Name: pn_pool_message_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE  pn_pool_message
    ADD CONSTRAINT pn_pool_message_id PRIMARY KEY (id);


--
-- Name: pn_reciever_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE  pn_reciever
    ADD CONSTRAINT pn_reciever_id PRIMARY KEY (id);


--
-- Name: pn_regex_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE  pn_regex
    ADD CONSTRAINT pn_regex_id PRIMARY KEY (id);


--
-- Name: pn_tmpl_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE  pn_template
    ADD CONSTRAINT pn_tmpl_id PRIMARY KEY (id);


--
-- Name: pn_tmpl_message_primary_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE pn_template_message
    ADD CONSTRAINT pn_tmpl_message_primary_key PRIMARY KEY (id);


--
-- Name: pn_tmpl_regex_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE pn_template_regex
    ADD CONSTRAINT pn_tmpl_regex_id PRIMARY KEY (id);

--
-- Name: pn_regex_object_pk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE pn_regex_object
		ADD CONSTRAINT pn_regex_object_pk PRIMARY KEY (id);


--
-- Name: pn_pool_message_tmpl_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE  pn_pool_message
    ADD CONSTRAINT pn_pool_message_tmpl_id FOREIGN KEY (template_id) REFERENCES pn_template(id);


--
-- Name: pn_reciever_tmpl_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE  pn_reciever
    ADD CONSTRAINT pn_reciever_tmpl_id FOREIGN KEY (template_id) REFERENCES pn_template(id);


--
-- Name: pn_tmpl_msg_tmpl_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE pn_template_message
    ADD CONSTRAINT pn_tmpl_message_tmpl_id FOREIGN KEY (template_id) REFERENCES pn_template(id) DEFERRABLE;


--
-- Name: pn_tmpl_regex_regex_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE pn_template_regex
    ADD CONSTRAINT pn_tmpl_rgx_rgx_id_fkey FOREIGN KEY (regex_id) REFERENCES pn_regex(id);


--
-- Name: pn_tmpl_regex_tmpl_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE pn_template_regex
    ADD CONSTRAINT pn_rgx_tmpl_id_fkey FOREIGN KEY (template_id) REFERENCES pn_template(id);

--
-- Name: pn_rgx_obj_rgx_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE pn_regex_object
		ADD CONSTRAINT pn_rgx_obj_rgx_id_fkey FOREIGN KEY (regex_id) REFERENCES pn_regex(id);

--
-- Name: pn_rgx_obj_obj_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE pn_regex_object
		ADD CONSTRAINT pn_rgx_obj_obj_id_fkey FOREIGN KEY (object_id) REFERENCES pn_object(id);


--
-- PostgreSQL database dump complete
--


--
-- Name: pn_audit_message; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE pn_audit_message (
    message varchar2(4000),
    id integer NOT NULL
);


--
-- Name: pn_object; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE pn_object (
    id integer NOT NULL,
    name varchar2(256),
    properties varchar2(4000),
		class_name varchar2(512)
);


--
-- Name: pn_pool_message; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE pn_pool_message (
    id integer NOT NULL,
    regex_id integer NOT NULL,
    template_id integer NOT NULL,
    key_attributes varchar2(4000) NOT NULL,
    created date default sysdate NOT NULL,
    notif_message varchar2(1000) NOT NULL,
    locale varchar2(5)
);


--
-- Name: pn_reciever; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE pn_reciever (
    id integer NOT NULL,
    target varchar2(256) NOT NULL,
    type_of_reciever varchar2(256) NOT NULL,
    template_id integer NOT NULL
);


--
-- Name: pn_regex; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE pn_regex (
    id integer NOT NULL,
    note varchar2(256),
    regex varchar2(4000) NOT NULL
);


--
-- Name: pn_template; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE pn_template (
    id integer NOT NULL,
    primary_properties varchar2(4000) NOT NULL,
    notify_trigger varchar2(100),
    youngest_message_time integer,
    oldest_message_time integer,
    subject varchar2(512),
		sender varchar2(4000),
    locale varchar2(512)
);


--
-- Name: pn_template_message; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE pn_template_message (
    id integer NOT NULL,
    template_id integer NOT NULL,
    locale varchar2(5) NOT NULL,
    message varchar2(4000)
);


--
-- Name: pn_template_regex; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE pn_template_regex (
    regex_id integer NOT NULL,
    template_id integer NOT NULL,
    id integer NOT NULL
);

--
-- Name: pn_regex_object; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE pn_regex_object (
		id integer NOT NULL,
		regex_id integer NOT NULL,
		object_id integer NOT NULL
);
