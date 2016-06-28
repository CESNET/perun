package cz.metacentrum.perun.registrar.impl;

import java.io.*;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.*;
import cz.metacentrum.perun.core.impl.Compatibility;
import cz.metacentrum.perun.registrar.exceptions.RegistrarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.transaction.annotation.Transactional;

import cz.metacentrum.perun.core.bl.PerunBl;
import org.springframework.jdbc.core.JdbcPerunTemplate;
import cz.metacentrum.perun.core.impl.Utils;
import cz.metacentrum.perun.registrar.model.Application;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.model.ApplicationFormItem;
import cz.metacentrum.perun.registrar.model.ApplicationFormItemData;
import cz.metacentrum.perun.registrar.model.ApplicationMail;
import cz.metacentrum.perun.registrar.model.Application.AppType;
import cz.metacentrum.perun.registrar.model.ApplicationMail.MailText;
import cz.metacentrum.perun.registrar.model.ApplicationMail.MailType;
import cz.metacentrum.perun.registrar.MailManager;
import cz.metacentrum.perun.registrar.RegistrarManager;

public class MailManagerImpl implements MailManager {

	final static Logger log = LoggerFactory.getLogger(MailManagerImpl.class);

	private static final String MAILS_SELECT_BY_FORM_ID = "select id,app_type,form_id,mail_type,send from application_mails where form_id=?";
	private static final String MAILS_SELECT_BY_PARAMS = "select id,app_type,form_id,mail_type,send from application_mails where form_id=? and app_type=? and mail_type=?";
	private static final String MAIL_TEXTS_SELECT_BY_MAIL_ID= "select locale,subject,text from application_mail_texts where mail_id=?";

	private static final String URN_VO_FROM_EMAIL = "urn:perun:vo:attribute-def:def:fromEmail";
	private static final String URN_VO_TO_EMAIL = "urn:perun:vo:attribute-def:def:toEmail";
	private static final String URN_VO_MAIL_FOOTER = "urn:perun:vo:attribute-def:def:mailFooter";
	protected static final String URN_USER_PREFERRED_MAIL = "urn:perun:user:attribute-def:def:preferredMail";
	private static final String URN_USER_PREFERRED_LANGUAGE = "urn:perun:user:attribute-def:def:preferredLanguage";
	private static final String URN_MEMBER_MAIL = "urn:perun:member:attribute-def:def:mail";
	private static final String URN_MEMBER_EXPIRATION = "urn:perun:member:attribute-def:def:membershipExpiration";
	private static final String URN_GROUP_TO_EMAIL = "urn:perun:group:attribute-def:def:toEmail";
	private static final String URN_GROUP_FROM_EMAIL = "urn:perun:group:attribute-def:def:fromEmail";
	private static final String URN_VO_LANGUAGE_EMAIL = "urn:perun:vo:attribute-def:def:notificationsDefLang";
	private static final String URN_GROUP_LANGUAGE_EMAIL = "urn:perun:group:attribute-def:def:notificationsDefLang";
	private static final String URN_VO_REGISTRATION_URL = "urn:perun:vo:attribute-def:def:registrarURL";
	private static final String URN_GROUP_REGISTRATION_URL = "urn:perun:group:attribute-def:def:registrarURL";

	@Autowired PerunBl perun;
	@Autowired RegistrarManager registrarManager;
	@Autowired private Properties registrarProperties;
	private PerunSession registrarSession;
	private JdbcPerunTemplate jdbc;
	private MailSender mailSender;
	private AttributesManager attrManager;
	private MembersManager membersManager;
	private UsersManager usersManager;
	private GroupsManager groupsManager;

	// Spring setters

	public void setDataSource(DataSource dataSource) {
		this.jdbc =  new JdbcPerunTemplate(dataSource);
	}

	public void setMailSender(MailSender mailSender) {
		this.mailSender = mailSender;
	}

	/**
	 * Init method, instantiate PerunSession
	 *
	 * @throws PerunException
	 */
	protected void initialize() throws PerunException {

		// gets session for a system principal "perunRegistrar"
		final PerunPrincipal pp = new PerunPrincipal("perunRegistrar",
				ExtSourcesManager.EXTSOURCE_NAME_INTERNAL,
				ExtSourcesManager.EXTSOURCE_INTERNAL);
		registrarSession = perun.getPerunSession(pp, new PerunClient());

		this.attrManager = perun.getAttributesManager();
		this.membersManager = perun.getMembersManager();
		this.usersManager = perun.getUsersManager();
		this.groupsManager = perun.getGroupsManager();

	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer addMail(PerunSession sess, ApplicationForm form, ApplicationMail mail) throws PerunException, DuplicateKeyException {

		if (form.getGroup() != null) {
			if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, form.getVo()) && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, form.getGroup())) {
				throw new PrivilegeException(sess, "addMail");
			}
		} else {
			if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, form.getVo())) {
				throw new PrivilegeException(sess, "addMail");
			}
		}

		int id = Utils.getNewId(jdbc, "APPLICATION_MAILS_ID_SEQ");
		mail.setId(id);

		jdbc.update("insert into application_mails(id, form_id, app_type, mail_type, send) values (?,?,?,?,?)",
				mail.getId(), form.getId(), mail.getAppType().toString(), mail.getMailType().toString(), mail.getSend() ? "1" : "0");

		for (Locale loc : mail.getMessage().keySet()) {
			jdbc.update("insert into application_mail_texts(mail_id,locale,subject,text) values (?,?,?,?)",
					mail.getId(), loc.toString(), mail.getMessage(loc).getSubject(), mail.getMessage(loc).getText());
		}

		log.info("[MAIL MANAGER] Mail notification definition created: {}", mail);
		if (form.getGroup() != null) {
			perun.getAuditer().log(sess, "Mail ID: {} of Type: {} added for Group ID: {}.", mail.getId(), mail.getMailType()+"/"+mail.getAppType(), form.getGroup().getId());
		} else {
			perun.getAuditer().log(sess, "Mail ID: {} of Type: {} added for VO ID: {}.", mail.getId(), mail.getMailType()+"/"+mail.getAppType(), form.getVo().getId());
		}


		return id;

	}

	@Override
	public void deleteMailById(PerunSession sess, ApplicationForm form, Integer id) throws PerunException {

		if (form.getGroup() != null) {
			if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, form.getVo()) && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, form.getGroup())) {
				throw new PrivilegeException(sess, "deleteMail");
			}
		} else {
			if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, form.getVo())) {
				throw new PrivilegeException(sess, "deleteMail");
			}
		}

		ApplicationMail mail = getMailById(sess, id);

		int result = jdbc.update("delete from application_mails where id=?", id);
		if (result == 0) throw new InternalErrorException("Mail notification with id="+id+" doesn't exists!");
		if (result == 1) log.info("[MAIL MANAGER] Mail notification with id={} deleted", id);
		if (result > 1) throw new ConsistencyErrorException("There is more than one mail notification with id="+id);

		if (form.getGroup() != null) {
			perun.getAuditer().log(sess, "Mail ID: {} of Type: {} removed for Group ID: {}.", id, mail.getMailType()+"/"+mail.getAppType(), form.getGroup().getId());
		} else {
			perun.getAuditer().log(sess, "Mail ID: {} of Type: {} removed for VO ID: {}.", id, mail.getMailType()+"/"+mail.getAppType(), form.getVo().getId());
		}

	}

	@Override
	public ApplicationMail getMailById(PerunSession sess, Integer id) throws InternalErrorException, PrivilegeException {

		// TODO authz
		ApplicationMail mail = null;

		// get mail def
		try {
			List<ApplicationMail> mails = jdbc.query("select id,app_type,form_id,mail_type,send from application_mails where id=?", new RowMapper<ApplicationMail>(){
				@Override
				public ApplicationMail mapRow(ResultSet rs, int arg1) throws SQLException {
					return new ApplicationMail(rs.getInt("id"),
							AppType.valueOf(rs.getString("app_type")),
							rs.getInt("form_id"), MailType.valueOf(rs.getString("mail_type")),
							rs.getBoolean("send"));
				}
			}, id);
			// set
			if (mails.size() != 1) {
				log.error("[MAIL MANAGER] Wrong number of mail definitions returned by unique params, expected 1 but was: "+mails.size());
				throw new InternalErrorException("Wrong number of mail definitions returned by unique params, expected 1 but was: "+mails.size());
			}
			mail = mails.get(0);
		} catch (EmptyResultDataAccessException ex) {
			throw new InternalErrorException("Mail definition with ID="+id+" doesn't exists.");
		}

		List<MailText> texts = new ArrayList<MailText>();
		try {
			texts = jdbc.query(MAIL_TEXTS_SELECT_BY_MAIL_ID, new RowMapper<MailText>(){
				@Override
				public MailText mapRow(ResultSet rs, int arg1) throws SQLException {
					return new MailText(new Locale(rs.getString("locale")), rs.getString("subject"), rs.getString("text"));
				}
			}, mail.getId());
		} catch (EmptyResultDataAccessException ex) {
			// if no texts it's error
			log.error("[MAIL MANAGER] Mail do not contains any text message: {}", ex);
			return mail;
		}
		for (MailText text : texts) {
			// fill localized messages
			mail.getMessage().put(text.getLocale(), text);
		}
		return mail;

	}

	@Override
	@Transactional(rollbackFor=Exception.class)
	public void updateMailById(PerunSession sess, ApplicationMail mail) throws PerunException {

		ApplicationForm form = registrarManager.getFormById(sess, mail.getFormId());

		// update sending (enabled / disabled)
		jdbc.update("update application_mails set send=? where id=?", mail.getSend() ? "1" : "0", mail.getId());

		// update texts (easy way = delete and new insert)
		jdbc.update("delete from application_mail_texts where mail_id=?", mail.getId());

		for (Locale loc : mail.getMessage().keySet()) {
			MailText text = mail.getMessage(loc);
			jdbc.update("insert into application_mail_texts(mail_id,locale,subject,text) values (?,?,?,?)",
					mail.getId(), loc.toString(), text.getSubject(), text.getText());
		}

		if (form.getGroup() != null) {
			perun.getAuditer().log(sess, "Mail ID: {} of Type: {} updated for Group ID: {}.", mail.getId(), mail.getMailType()+"/"+mail.getAppType(), form.getGroup().getId());
		} else {
			perun.getAuditer().log(sess, "Mail ID: {} of Type: {} updated for VO ID: {}.", mail.getId(), mail.getMailType()+"/"+mail.getAppType(), form.getVo().getId());
		}

	}

	@Override
	public void setSendingEnabled(PerunSession sess, List<ApplicationMail> mails, boolean enabled) throws PerunException {

		// TODO authz
		if (mails == null) { throw new InternalErrorException("Mails definitions to update can't be null"); }

		for (ApplicationMail mail : mails) {
			// update sending (enabled / disabled)
			if (Compatibility.isPostgreSql()) {
				jdbc.update("update application_mails set send=? where id=?", (enabled) ? '1' : '0', mail.getId());
			} else {
				jdbc.update("update application_mails set send=? where id=?", enabled, mail.getId());
			}

			perun.getAuditer().log(sess, "Sending of Mail ID: {} " + ((enabled) ? " enabled." : " disabled."));

		}

	}

	@Override
	public List<ApplicationMail> getApplicationMails(PerunSession sess, ApplicationForm form) throws PerunException {

		List<ApplicationMail> mails = new ArrayList<ApplicationMail>();
		mails = jdbc.query(MAILS_SELECT_BY_FORM_ID, new RowMapper<ApplicationMail>() {
			@Override
			public ApplicationMail mapRow(ResultSet rs, int arg1) throws SQLException {
				return new ApplicationMail(rs.getInt("id"),
						AppType.valueOf(rs.getString("app_type")),
						rs.getInt("form_id"), MailType.valueOf(rs.getString("mail_type")),
						rs.getBoolean("send"));
			}
		}, form.getId());
		for (ApplicationMail mail : mails) {
			List<MailText> texts = new ArrayList<MailText>();
			texts = jdbc.query(MAIL_TEXTS_SELECT_BY_MAIL_ID, new RowMapper<MailText>(){
				@Override
				public MailText mapRow(ResultSet rs, int arg1) throws SQLException {
					return new MailText(new Locale(rs.getString("locale")), rs.getString("subject"), rs.getString("text"));
				}
			}, mail.getId());
			for (MailText text : texts) {
				// fil localized messages
				mail.getMessage().put(text.getLocale(), text);
			}
		}
		return mails;

	}

	@Override
	public void copyMailsFromVoToVo(PerunSession sess, Vo fromVo, Vo toVo) throws PerunException {

		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, fromVo) ||
				!AuthzResolver.isAuthorized(sess, Role.VOADMIN, toVo)) {
			throw new PrivilegeException(sess, "copyMailsFromVoToVo");
		}

		ApplicationForm formFrom = registrarManager.getFormForVo(fromVo);
		ApplicationForm formTo = registrarManager.getFormForVo(toVo);
		List<ApplicationMail> mails = getApplicationMails(sess, formFrom);
		for (ApplicationMail mail : mails) {
			// to start transaction
			try {
				registrarManager.getMailManager().addMail(sess, formTo, mail);
			} catch (DuplicateKeyException ex) {
				log.info("[MAIL MANAGER] Mail notification of type {} skipped while copying (was already present).", mail.getMailType()+"/"+mail.getAppType());
			}
		}

	}

	@Override
	public void copyMailsFromVoToGroup(PerunSession sess, Vo fromVo, Group toGroup, boolean reverse) throws PerunException {

		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, fromVo)) {
			throw new PrivilegeException(sess, "copyMailsFromVoToVo");
		}
		if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, toGroup) && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, toGroup)) {
			throw new PrivilegeException(sess, "copyMailsFromVoToVo");
		}

		if (reverse) {

			// copy notifications from Group to VO
			ApplicationForm voForm = registrarManager.getFormForVo(fromVo);
			ApplicationForm groupForm = registrarManager.getFormForGroup(toGroup);

			List<ApplicationMail> mails = getApplicationMails(sess, groupForm);
			for (ApplicationMail mail : mails) {
				// to start transaction
				try {
					registrarManager.getMailManager().addMail(sess, voForm, mail);
				} catch (DuplicateKeyException ex) {
					log.info("[MAIL MANAGER] Mail notification of type {} skipped while copying (was already present).", mail.getMailType()+"/"+mail.getAppType());
				}
			}

		} else {

			// copy notifications from VO to Group
			ApplicationForm voForm = registrarManager.getFormForVo(fromVo);
			ApplicationForm groupForm = registrarManager.getFormForGroup(toGroup);

			List<ApplicationMail> mails = getApplicationMails(sess, voForm);
			for (ApplicationMail mail : mails) {
				// to start transaction
				try {
					registrarManager.getMailManager().addMail(sess, groupForm, mail);
				} catch (DuplicateKeyException ex) {
					log.info("[MAIL MANAGER] Mail notification of type {} skipped while copying (was already present).", mail.getMailType()+"/"+mail.getAppType());
				}
			}

		}

	}

	@Override
	public void copyMailsFromGroupToGroup(PerunSession sess, Group fromGroup, Group toGroup) throws PerunException {

		Vo fromVO = perun.getVosManagerBl().getVoById(registrarSession, fromGroup.getVoId());

		if (!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, fromGroup) &&
				!AuthzResolver.isAuthorized(sess, Role.VOADMIN, fromGroup) &&
				!AuthzResolver.isAuthorized(sess, Role.TOPGROUPCREATOR, fromVO)) {
			throw new PrivilegeException(sess, "copyMailsFromGroupToGroup");
		}
		if (!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, toGroup) &&
				!AuthzResolver.isAuthorized(sess, Role.VOADMIN, toGroup)) {
			throw new PrivilegeException(sess, "copyMailsFromGroupToGroup");
		}

		ApplicationForm formFrom = registrarManager.getFormForGroup(fromGroup);
		ApplicationForm formTo = registrarManager.getFormForGroup(toGroup);
		List<ApplicationMail> mails = getApplicationMails(sess, formFrom);
		for (ApplicationMail mail : mails) {
			// to start transaction
			try {
				registrarManager.getMailManager().addMail(sess, formTo, mail);
			} catch (DuplicateKeyException ex) {
				log.info("[MAIL MANAGER] Mail notification of type {} skipped while copying (was already present).", mail.getMailType()+"/"+mail.getAppType());
			}
		}

	}

	@Override
	public void sendMessage(Application app, MailType mailType, String reason, List<Exception> exceptions) {

		try {

			// get form
			ApplicationForm form;
			if (app.getGroup() != null) {
				form = registrarManager.getFormForGroup(app.getGroup());
			} else {
				form = registrarManager.getFormForVo(app.getVo());
			}

			// get mail definition
			ApplicationMail mail = getMailByParams(form.getId(), app.getType(), mailType);
			if (mail == null) {
				log.error("[MAIL MANAGER] Mail not sent. Definition (or mail text) for: {} do not exists for VO: "+app.getVo()+" and Group: "+app.getGroup(), mailType.toString());
				return; // mail not found
			} else if (mail.getSend() == false) {
				log.info("[MAIL MANAGER] Mail not sent. Disabled by VO admin for: " + mail.getMailType() + " / appID: " + app.getId() + " / " + app.getVo() + " / " + app.getGroup());
				return; // sending this mail is disabled by VO admin
			}
			// get app data
			List<ApplicationFormItemData> data = registrarManager.getApplicationDataById(registrarSession, app.getId());
			// get language
			Locale lang = new Locale(getLanguageFromAppData(app, data));
			// get localized subject and text
			MailText mt = mail.getMessage(lang);
			String mailText = "";
			String mailSubject = "";
			if (mt.getText() != null && !mt.getText().isEmpty()) {
				mailText = mt.getText();
			}
			if (mt.getSubject() != null && !mt.getSubject().isEmpty()) {
				mailSubject = mt.getSubject();
			}

			// different behavior based on mail type
			MailType type = mail.getMailType();

			if (MailType.APP_CREATED_USER.equals(type)) {

				SimpleMailMessage message = new SimpleMailMessage();
				// set FROM
				setFromMailAddress(message, app);
				// set TO
				setUsersMailAsTo(message, app, data);

				// substitute common strings
				mailText = substituteCommonStrings(app, data, mailText, reason, exceptions);
				mailSubject = substituteCommonStrings(app, data, mailSubject, reason, exceptions);

				// set subject and text
				message.setSubject(mailSubject);
				message.setText(mailText);

				try {
					// send mail
					mailSender.send(message);
					log.info("[MAIL MANAGER] Sending mail: APP_CREATED_USER to: {} / appID: " + app.getId() + " / " + app.getVo() + " / " + app.getGroup(), message.getTo());
				} catch (MailException ex) {
					log.error("[MAIL MANAGER] Sending mail: APP_CREATED_USER failed because of exception: {}", ex);
				}

			} else if (MailType.APP_CREATED_VO_ADMIN.equals(type)) {

				SimpleMailMessage message = new SimpleMailMessage();

				// set FROM
				setFromMailAddress(message, app);

				// set language independent on user's preferred language.
				lang = new Locale("en");
				try {
					if (app.getGroup() == null) {
						// VO
						Attribute a = attrManager.getAttribute(registrarSession, app.getVo(), URN_VO_LANGUAGE_EMAIL);
						if (a != null && a.getValue() != null) {
							lang = new Locale(BeansUtils.attributeValueToString(a));
						}
					} else {
						Attribute a = attrManager.getAttribute(registrarSession, app.getGroup(), URN_GROUP_LANGUAGE_EMAIL);
						if (a != null && a.getValue() != null) {
							lang = new Locale(BeansUtils.attributeValueToString(a));
						}
					}
				} catch (Exception ex) {
					log.error("Error when resolving notification default language: {}", ex);
				}

				MailText mt2 = mail.getMessage(lang);
				String mailText2 = "";
				String mailSubject2 = "";
				if (mt2.getText() != null && !mt2.getText().isEmpty()) {
					mailText2 = mt2.getText();
				}
				if (mt2.getSubject() != null && !mt2.getSubject().isEmpty()) {
					mailSubject2 = mt2.getSubject();
				}

				// substitute common strings
				mailText2 = substituteCommonStrings(app, data, mailText2, reason, exceptions);
				mailSubject2 = substituteCommonStrings(app, data, mailSubject2, reason, exceptions);

				// set subject and text
				message.setSubject(mailSubject2);
				message.setText(mailText2);

				// send message to all VO or Group admins
				List<String> toEmail = getToMailAddresses(app);

				for (String email : toEmail) {
					message.setTo(email);
					try {
						mailSender.send(message);
						log.info("[MAIL MANAGER] Sending mail: APP_CREATED_VO_ADMIN to: {} / appID: " + app.getId() + " / " + app.getVo() + " / " + app.getGroup(), message.getTo());
					} catch (MailException ex) {
						log.error("[MAIL MANAGER] Sending mail: APP_CREATED_VO_ADMIN failed because of exception: {}", ex);
					}
				}

			} else if (MailType.MAIL_VALIDATION.equals(type)) {

				SimpleMailMessage message = new SimpleMailMessage();
				// set FROM
				setFromMailAddress(message, app);

				// set TO
				message.setTo(""); // empty = not sent

				// substitute common strings
				mailText = substituteCommonStrings(app, data, mailText, reason, exceptions);
				mailSubject = substituteCommonStrings(app, data, mailSubject, reason, exceptions);

				// set subject and text
				message.setSubject(mailSubject);

				// send to all emails, which needs to be validated
				for (ApplicationFormItemData d : data) {
					ApplicationFormItem item = d.getFormItem();
					String value = d.getValue();
					// if mail field and not validated
					if (ApplicationFormItem.Type.VALIDATED_EMAIL.equals(item.getType()) && !"1".equals(d.getAssuranceLevel())) {
						if (value != null && !value.isEmpty()) {

							// set TO
							message.setTo(value);

							// get validation link params
							String i = Integer.toString(d.getId(), Character.MAX_RADIX);
							String m = getMessageAuthenticationCode(i);

							// replace new validation link
							if (mailText.contains("{validationLink-")) {

								Pattern pattern = Pattern.compile("\\{validationLink-[^\\}]+\\}");
								Matcher matcher = pattern.matcher(mailText);
								while (matcher.find()) {

									// whole "{validationLink-something}"
									String toSubstitute = matcher.group(0);

									// new login value to replace in text
									String newValue = "";

									Pattern namespacePattern = Pattern.compile("\\-(.*?)\\}");
									Matcher m2 = namespacePattern.matcher(toSubstitute);
									while (m2.find()) {
										// only namespace "fed", "cert",...
										String namespace = m2.group(1);

										newValue = getPerunUrl(app.getVo(), app.getGroup());

										if (newValue != null && !newValue.isEmpty()) {
											if (!newValue.endsWith("/")) newValue += "/";
											newValue += namespace + "/registrar/";
											newValue += "?vo="+getEncodedString(app.getVo().getShortName());
											newValue += ((app.getGroup() != null) ? "&group="+getEncodedString(app.getGroup().getName()) : "");
											try {
												newValue += "&i=" + URLEncoder.encode(i, "UTF-8") + "&m=" + URLEncoder.encode(m, "UTF-8");
											} catch (UnsupportedEncodingException ex) {
												newValue += "&i=" + i + "&m=" + m;
											}
										}
									}
									// substitute {validationLink-authz} with actual value or empty string
									mailText = mailText.replace(toSubstitute, newValue);

								}

							}

							if (mailText.contains("{validationLink}")) {

								// new backup if validation URL is missing
								String url = getPerunUrl(app.getVo(), app.getGroup());
								if (url != null && !url.isEmpty()) {
									if (!url.endsWith("/")) url += "/";
									url += "registrar/";
								}

								if (url != null && !url.isEmpty()) url = url + "?vo=" + getEncodedString(app.getVo().getShortName());


								if (app.getGroup() != null) {
									// append group name for
									if (url != null && !url.isEmpty()) url += "&group=" + getEncodedString(app.getGroup().getName());
								}

								// construct whole url
								StringBuilder url2 = new StringBuilder(url);

								if (url.contains("?")) {
									if (!url.endsWith("?")) {
										url2.append("&");
									}
								} else {
									if (!url2.toString().isEmpty()) url2.append("?");
								}

								try {
									if (!url2.toString().isEmpty())
										url2.append("i=").append(URLEncoder.encode(i, "UTF-8")).append("&m=").append(URLEncoder.encode(m, "UTF-8"));
								} catch (UnsupportedEncodingException ex) {
									if (!url2.toString().isEmpty()) url2.append("i=").append(i).append("&m=").append(m);
								}

								// replace validation link
								mailText = mailText.replace("{validationLink}", url2.toString());

							}

							// set replaced text
							message.setText(mailText);

							try {
								mailSender.send(message);
								log.info("[MAIL MANAGER] Sending mail: MAIL_VALIDATION to: {} / appID: " + app.getId() + " / " + app.getVo() + " / " + app.getGroup(), message.getTo());
							} catch (MailException ex) {
								log.error("[MAIL MANAGER] Sending mail: MAIL_VALIDATION failed because of exception: {}", ex);
							}

						} else {
							log.error("[MAIL MANAGER] Sending mail: MAIL_VALIDATION failed. Not valid value of VALIDATED_MAIL field: {}", value);
						}
					}
				}

			} else if (type.equals(MailType.APP_APPROVED_USER)) {

				SimpleMailMessage message = new SimpleMailMessage();
				// set FROM
				setFromMailAddress(message, app);

				// set TO
				setUsersMailAsTo(message, app, data);

				// substitute common strings
				mailText = substituteCommonStrings(app, data, mailText, reason, exceptions);
				mailSubject = substituteCommonStrings(app, data, mailSubject, reason, exceptions);

				// set subject and text
				message.setSubject(mailSubject);
				message.setText(mailText);

				try {
					// send mail
					mailSender.send(message);
					log.info("[MAIL MANAGER] Sending mail: APP_APPROVED_USER to: {} / appID: " + app.getId() + " / " + app.getVo() + " / " + app.getGroup(), message.getTo());
				} catch (MailException ex) {
					log.error("[MAIL MANAGER] Sending mail: APP_APPROVED_USER failed because of exception: {}", ex);
				}

			} else if (type.equals(MailType.APP_REJECTED_USER)) {

				SimpleMailMessage message = new SimpleMailMessage();
				// set FROM
				setFromMailAddress(message, app);

				// set TO
				setUsersMailAsTo(message, app, data);

				// substitute common strings
				mailText = substituteCommonStrings(app, data, mailText, reason, exceptions);
				mailSubject = substituteCommonStrings(app, data, mailSubject, reason, exceptions);

				// set subject and text
				message.setSubject(mailSubject);
				message.setText(mailText);

				try {
					// send mail
					mailSender.send(message);
					log.info("[MAIL MANAGER] Sending mail: APP_REJECTED_USER to: {} / appID: " + app.getId() + " / " + app.getVo() + " / " + app.getGroup(), message.getTo());
				} catch (MailException ex) {
					log.error("[MAIL MANAGER] Sending mail: APP_REJECTED_USER failed because of exception: {}", ex);
				}

			} else if (MailType.APP_ERROR_VO_ADMIN.equals(type)) {

				SimpleMailMessage message = new SimpleMailMessage();

				// set FROM
				setFromMailAddress(message, app);

				// set language independent on user's preferred language.
				lang = new Locale("en");
				try {
					if (app.getGroup() == null) {
						// VO
						Attribute a = attrManager.getAttribute(registrarSession, app.getVo(), URN_VO_LANGUAGE_EMAIL);
						if (a != null && a.getValue() != null) {
							lang = new Locale(BeansUtils.attributeValueToString(a));
						}
					} else {
						Attribute a = attrManager.getAttribute(registrarSession, app.getGroup(), URN_GROUP_LANGUAGE_EMAIL);
						if (a != null && a.getValue() != null) {
							lang = new Locale(BeansUtils.attributeValueToString(a));
						}
					}
				} catch (Exception ex) {
					log.error("Error when resolving notification default language: {}", ex);
				}

				MailText mt2 = mail.getMessage(lang);
				String mailText2 = "";
				String mailSubject2 = "";
				if (mt2.getText() != null && !mt2.getText().isEmpty()) {
					mailText2 = mt2.getText();
				}
				if (mt2.getSubject() != null && !mt2.getSubject().isEmpty()) {
					mailSubject2 = mt2.getSubject();
				}

				// substitute common strings
				mailText2 = substituteCommonStrings(app, data, mailText2, reason, exceptions);
				mailSubject2 = substituteCommonStrings(app, data, mailSubject2, reason, exceptions);

				// set subject and text
				message.setSubject(mailSubject2);
				message.setText(mailText2);

				// send message to all VO or Group admins
				List<String> toEmail = getToMailAddresses(app);

				for (String email : toEmail) {
					message.setTo(email);
					try {
						mailSender.send(message);
						log.info("[MAIL MANAGER] Sending mail: APP_ERROR_VO_ADMIN to: {} / appID: " + app.getId() + " / " + app.getVo() + " / " + app.getGroup(), message.getTo());
					} catch (MailException ex) {
						log.error("[MAIL MANAGER] Sending mail: APP_ERROR_VO_ADMIN failed because of exception: {}", ex);
					}
				}

			} else {
				log.error("[MAIL MANAGER] Sending mail type: {} is not supported.", type);
			}

		} catch (Exception ex) {
			// all exceptions are catched and logged to: perun-registrar.log
			log.error("[MAIL MANAGER] Exception thrown when sending email: {}", ex);
		}

	}

	@Override
	public void sendMessage(PerunSession sess, Application app, MailType mailType, String reason) throws PerunException {

		// authz
		if (app.getGroup() == null) {
			if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, app.getVo())) {
				throw new PrivilegeException(sess, "sendMessage");
			}
		} else {
			if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, app.getVo()) && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, app.getGroup())) {
				throw new PrivilegeException(sess, "sendMessage");
			}
		}

		if (MailType.USER_INVITE.equals(mailType)) {
			throw new RegistrarException("USER_INVITE notification can't be sent this way. Use sendInvitation() instead.");
		}

		ApplicationForm form;
		if (app.getGroup() == null) {
			form = registrarManager.getFormForVo(app.getVo());
		} else {
			form = registrarManager.getFormForGroup(app.getGroup());
		}

		ApplicationMail mail = getMailByParams(form.getId(), app.getType(), mailType);
		if (mail == null) throw new RegistrarException("Notification template for "+mailType+" is not defined.");
		if (!mail.getSend()) throw new RegistrarException("Sending of notification "+mailType+" is disabled.");

		if (!AuthzResolver.hasRole(sess.getPerunPrincipal(), Role.PERUNADMIN)) {

			if (MailType.APP_ERROR_VO_ADMIN.equals(mailType)) {
				throw new RegistrarException("APP_ERROR_VO_ADMIN notification can't be sent this way, since it's bound to each approval process. Try to approve application once again to receive this message.");
			}

			if (MailType.APP_CREATED_USER.equals(mailType) || MailType.APP_CREATED_VO_ADMIN.equals(mailType)) {

				if (app.getState().equals(Application.AppState.NEW) || app.getState().equals(Application.AppState.VERIFIED)) {
					sendMessage(app, mailType, null, null);
				} else {
					throw new RegistrarException("Application must be in state NEW or VERIFIED to allow sending of "+mailType+" notification.");
				}

			} else if (MailType.MAIL_VALIDATION.equals(mailType)) {

				if (app.getState().equals(Application.AppState.NEW)) {
					sendMessage(app, mailType, null, null);
				} else {
					throw new RegistrarException("Application must be in state NEW to allow sending of "+mailType+" notification.");
				}

			} else if (MailType.APP_APPROVED_USER.equals(mailType)) {

				if (Application.AppState.APPROVED.equals(app.getState())) {
					sendMessage(app, mailType, null, null);
				} else {
					throw new RegistrarException("Application must be in state APPROVED to allow sending of "+mailType+" notification.");
				}

			} else if (MailType.APP_REJECTED_USER.equals(mailType)) {

				if (Application.AppState.REJECTED.equals(app.getState())) {
					sendMessage(app, mailType, reason, null);
				} else {
					throw new RegistrarException("Application must be in state REJECTED to allow sending of "+mailType+" notification.");
				}

			}

		} else {

			// perun admin can always sent any message with exception of USER_INVITE
			sendMessage(app, mailType, reason, null);

		}
		perun.getAuditer().log(sess, "Mail of Type: {} sent for Application: {}", mailType, app.getId());

	}

	@Override
	public void sendInvitation(PerunSession sess, Vo vo, Group group, String name, String email, String language) throws PerunException {

		if (group == null) {
			if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) &&
					!AuthzResolver.isAuthorized(sess, Role.TOPGROUPCREATOR, vo)) {
				throw new PrivilegeException(sess, "sendInvitation");
			}
		} else {
			if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo) && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
				throw new PrivilegeException(sess, "sendInvitation");
			}
		}

		if (email == null || email.isEmpty()) throw new RegistrarException("You must provide non-empty email of person you are inviting.");

		// get form
		ApplicationForm form;
		if (group != null) {
			form = registrarManager.getFormForGroup(group);
		} else {
			form = registrarManager.getFormForVo(vo);
		}

		// get mail definition
		ApplicationMail mail = getMailByParams(form.getId(), AppType.INITIAL, MailType.USER_INVITE);
		if (mail == null) {
			throw new RegistrarException("You don't have invitation e-mail template defined.");
		} else if (mail.getSend() == false) {
			throw new RegistrarException("Sending of invitations is disabled.");
		}

		if (language == null) {

			language = "en";
			if (group == null) {

				try {
					Attribute a = attrManager.getAttribute(registrarSession, vo, URN_VO_LANGUAGE_EMAIL);
					if (a != null && a.getValue() != null) {
						language = BeansUtils.attributeValueToString(a);
					}
				} catch (Exception ex) {
					log.error("[MAIL MANAGER] Exception thrown when getting preferred language of notification for VO={}: {}", vo, ex);
				}

			} else {

				try {
					Attribute a = attrManager.getAttribute(registrarSession, group, URN_GROUP_LANGUAGE_EMAIL);
					if (a != null && a.getValue() != null) {
						language = BeansUtils.attributeValueToString(a);
					}
				} catch (Exception ex) {
					log.error("[MAIL MANAGER] Exception thrown when getting preferred language of notification for Group={}: {}", group, ex);
				}

			}

		}

		// get language
		Locale lang = new Locale(language);
		// get localized subject and text
		MailText mt = mail.getMessage(lang);
		String mailText = "";
		String mailSubject = "";
		if (mt.getText() != null && !mt.getText().isEmpty()) {
			mailText = mt.getText();
		}
		if (mt.getSubject() != null && !mt.getSubject().isEmpty()) {
			mailSubject = mt.getSubject();
		}

		SimpleMailMessage message = new SimpleMailMessage();

		// fake app to get "from" address
		Application app = new Application();
		app.setVo(vo);
		app.setGroup(group);
		// get from
		setFromMailAddress(message, app);

		message.setTo(email);

		mailText = substituteCommonStringsForInvite(vo, group, null, name, mailText);
		mailSubject = substituteCommonStringsForInvite(vo, group, null, name, mailSubject);

		message.setSubject(mailSubject);
		message.setText(mailText);

		try {
			mailSender.send(message);
			log.info("[MAIL MANAGER] Sending mail: USER_INVITE to: {} / " + app.getVo() + " / " + app.getGroup(), message.getTo());
		} catch (MailException ex) {
			log.error("[MAIL MANAGER] Sending mail: USER_INVITE failed because of exception: {}", ex);
			throw new RegistrarException("Unable to send e-mail.", ex);
		}

	}

	@Override
	public void sendInvitation(PerunSession sess, Vo vo, Group group, User user) throws PerunException {

		if (group == null) {
			if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, vo)) {
				throw new PrivilegeException(sess, "sendInvitation");
			}
		} else {
			if (!AuthzResolver.isAuthorized(sess, Role.VOADMIN, group) && !AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, group)) {
				throw new PrivilegeException(sess, "sendInvitation");
			}
		}

		if (user == null) throw new RegistrarException("Missing user to send notification to.");

		try {

			Member m = membersManager.getMemberByUser(registrarSession, vo, user);
			// is member, is invite to group ?
			if (group != null) {
				List<Group> g = groupsManager.getMemberGroups(registrarSession, m);
				if (g.contains(group)) {
					// user is member of group - can't invite him
					throw new RegistrarException("User to invite is already member of your group: "+group.getShortName());
				}
			} else {
				throw new RegistrarException("User to invite is already member of your VO:"+vo.getShortName());
			}

		} catch (Exception ex) {
			log.error("[MAIL MANAGER] Exception {} when getting member by {} from "+vo.toString(), ex, user);
		}

		// get form
		ApplicationForm form;
		if (group != null) {
			form = registrarManager.getFormForGroup(group);
		} else {
			form = registrarManager.getFormForVo(vo);
		}

		// get mail definition
		ApplicationMail mail = getMailByParams(form.getId(), AppType.INITIAL, MailType.USER_INVITE);
		if (mail == null) {
			throw new RegistrarException("You don't have invitation e-mail template defined.");
		} else if (mail.getSend() == false) {
			throw new RegistrarException("Sending of invitations is disabled.");
		}

		String language = "en";

		try {
			Attribute a = attrManager.getAttribute(registrarSession, user, URN_USER_PREFERRED_LANGUAGE);
			if (a != null && a.getValue() != null) {
				language = BeansUtils.attributeValueToString(a);
			}
		} catch (Exception ex) {
			log.error("[MAIL MANAGER] Exception thrown when getting preferred language for USER={}: {}", user, ex);
		}

		if (group == null) {

			try {
				Attribute a = attrManager.getAttribute(registrarSession, vo, URN_VO_LANGUAGE_EMAIL);
				if (a != null && a.getValue() != null) {
					language = BeansUtils.attributeValueToString(a);
				}
			} catch (Exception ex) {
				log.error("[MAIL MANAGER] Exception thrown when getting preferred language of notification for VO={}: {}", vo, ex);
			}

		} else {

			try {
				Attribute a = attrManager.getAttribute(registrarSession, group, URN_GROUP_LANGUAGE_EMAIL);
				if (a != null && a.getValue() != null) {
					language = BeansUtils.attributeValueToString(a);
				}
			} catch (Exception ex) {
				log.error("[MAIL MANAGER] Exception thrown when getting preferred language of notification for Group={}: {}", group, ex);
			}

		}

		// get language
		Locale lang = new Locale(language);
		// get localized subject and text
		MailText mt = mail.getMessage(lang);
		String mailText = "";
		String mailSubject = "";
		if (mt.getText() != null && !mt.getText().isEmpty()) {
			mailText = mt.getText();
		}
		if (mt.getSubject() != null && !mt.getSubject().isEmpty()) {
			mailSubject = mt.getSubject();
		}

		SimpleMailMessage message = new SimpleMailMessage();

		// fake app to get "from" address
		Application app = new Application();
		app.setVo(vo);
		app.setGroup(group);
		// get from
		setFromMailAddress(message, app);

		String email = "";
		try {
			Attribute a = attrManager.getAttribute(registrarSession, user, URN_USER_PREFERRED_MAIL);
			if (a != null && a.getValue() != null) {
				email = BeansUtils.attributeValueToString(a);
			}
		} catch (Exception ex) {
			log.error("[MAIL MANAGER] Exception thrown when getting preferred language of notification for Group={}: {}", group, ex);
		}

		message.setTo(email);

		mailText = substituteCommonStringsForInvite(vo, group, user, null, mailText);
		mailSubject = substituteCommonStringsForInvite(vo, group, user, null, mailSubject);

		message.setSubject(mailSubject);
		message.setText(mailText);

		try {
			mailSender.send(message);
			log.info("[MAIL MANAGER] Sending mail: USER_INVITE to: {} / " + app.getVo() + " / " + app.getGroup(), message.getTo());
		} catch (MailException ex) {
			log.error("[MAIL MANAGER] Sending mail: USER_INVITE failed because of exception: {}", ex);
			throw new RegistrarException("Unable to send e-mail.", ex);
		}

	}

	@Override
	public String getMessageAuthenticationCode(String input) {
		if (input == null)
			throw new NullPointerException("input must not be null");
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(getPropertyFromConfiguration("secretKey").getBytes("UTF-8"),"HmacSHA256"));
			byte[] macbytes = mac.doFinal(input.getBytes("UTF-8"));
			return new BigInteger(macbytes).toString(Character.MAX_RADIX);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets particular property from registrar.properties file.
	 *
	 * @param propertyName name of the property
	 * @return value of the property
	 */
	@Override
	public String getPropertyFromConfiguration(String propertyName) {

		if (propertyName == null) {
			return "";
		}

		try {

			String result = registrarProperties.getProperty(propertyName);
			if (result == null) {
				return "";
			} else {
				return result;
			}

		} catch (Exception e) {
			log.error("[MAIL MANAGER] Exception when searching through perun-registrar-lib.properties file", e);
		}

		return "";

	}

	/**
	 * Retrieve mail definition from db by params.
	 * Mail contains all texts.
	 * If mail not exists, or no texts exists null is returned.
	 *
	 * @param formId relation to VO form
	 * @param appType application type
	 * @param mailType mail type
	 * @return mail if definition exists or null
	 */
	private ApplicationMail getMailByParams(Integer formId, AppType appType, MailType mailType) {

		ApplicationMail mail = null;

		// get mail def
		try {
			List<ApplicationMail> mails = jdbc.query(MAILS_SELECT_BY_PARAMS, new RowMapper<ApplicationMail>(){
				@Override
				public ApplicationMail mapRow(ResultSet rs, int arg1) throws SQLException {
					return new ApplicationMail(rs.getInt("id"),
							AppType.valueOf(rs.getString("app_type")),
							rs.getInt("form_id"), MailType.valueOf(rs.getString("mail_type")),
							rs.getBoolean("send"));
				}
			}, formId, appType.toString(), mailType.toString());
			// set
			if (mails.size() != 1) {
				log.error("[MAIL MANAGER] Wrong number of mail definitions returned by unique params, expected 1 but was: "+mails.size());
				return mail;
			}
			mail = mails.get(0);
		} catch (EmptyResultDataAccessException ex) {
			return mail;
		}

		List<MailText> texts = new ArrayList<MailText>();
		try {
			texts = jdbc.query(MAIL_TEXTS_SELECT_BY_MAIL_ID, new RowMapper<MailText>(){
				@Override
				public MailText mapRow(ResultSet rs, int arg1) throws SQLException {
					return new MailText(new Locale(rs.getString("locale")), rs.getString("subject"), rs.getString("text"));
				}
			}, mail.getId());
		} catch (EmptyResultDataAccessException ex) {
			// if no texts it's error
			log.error("[MAIL MANAGER] Mail do not contains any text message: {}", ex);
			return null;
		}
		for (MailText text : texts) {
			// fill localized messages
			mail.getMessage().put(text.getLocale(), text);
		}
		return mail;

	}

	/**
	 * Return preferred Locale from application
	 * (return EN if not found)
	 *
	 * @param data
	 * @return
	 */
	private String getLanguageFromAppData(Application app, List<ApplicationFormItemData> data) {

		String language = "en";

		// if user present - get preferred language
		if (app.getUser() != null) {
			try {
				User u = usersManager.getUserById(registrarSession, app.getUser().getId());
				Attribute a = attrManager.getAttribute(registrarSession, u, URN_USER_PREFERRED_LANGUAGE);
				if (a != null && a.getValue() != null) {
					language = BeansUtils.attributeValueToString(a);
				}
			} catch (Exception ex) {
				log.error("[MAIL MANAGER] Exception thrown when getting preferred language for {}: {}", app.getUser(), ex);
			}
		}

		// if preferred language specified on application - rewrite
		for (ApplicationFormItemData item : data) {
			if (item.getFormItem() != null) {
				if (item.getFormItem().getPerunDestinationAttribute() != null &&
						item.getFormItem().getPerunDestinationAttribute().equals(URN_USER_PREFERRED_LANGUAGE)) {
					if (item.getValue() == null || item.getValue().isEmpty()) {
						return language; // return default
					} else {
						return item.getValue(); // or return value
					}
				}
			}
		}
		// return default
		return language;

	}

	/**
	 * Set users mail as TO param for mail message.
	 *
	 * Default value is empty (mail won't be sent).
	 *
	 * Mail is taken from first founded form item of type VALIDATED_MAIL.
	 * If none found and user exists, it's taken from
	 * user's attribute: preferredMail
	 *
	 * @param message message to set TO param
	 * @param app application
	 * @param data application data
	 */
	private void setUsersMailAsTo(SimpleMailMessage message, Application app, List<ApplicationFormItemData> data) {

		message.setTo("");

		try {

			// get TO param from VALIDATED_EMAIL form items (it's best fit)
			for (ApplicationFormItemData d : data) {
				ApplicationFormItem item = d.getFormItem();
				String value = d.getValue();
				if (ApplicationFormItem.Type.VALIDATED_EMAIL.equals(item.getType())) {
					if (value != null && !value.isEmpty()) {
						message.setTo(d.getValue());
						return;// use first mail address
					}
				}
			}
			// get TO param from other form items related to "user - preferredMail"
			for (ApplicationFormItemData d : data) {
				ApplicationFormItem item = d.getFormItem();
				String value = d.getValue();
				if (item.getPerunDestinationAttribute() != null && !item.getPerunDestinationAttribute().isEmpty()) {
					if (item.getPerunDestinationAttribute().equalsIgnoreCase(URN_USER_PREFERRED_MAIL)) {
						if (value != null && !value.isEmpty()) {
							message.setTo(d.getValue());
							return;// use first mail address
						}
					}
				}
			}
			// get TO param from other form items related to "member - mail"
			for (ApplicationFormItemData d : data) {
				ApplicationFormItem item = d.getFormItem();
				String value = d.getValue();
				if (item.getPerunDestinationAttribute() != null && !item.getPerunDestinationAttribute().isEmpty()) {
					if (item.getPerunDestinationAttribute().equalsIgnoreCase(URN_MEMBER_MAIL)) {
						if (value != null && !value.isEmpty()) {
							message.setTo(d.getValue());
							return;// use first mail address
						}
					}
				}
			}
			// get TO param from user if not present on application form
			if (app.getUser() != null) {
				User u = usersManager.getUserById(registrarSession, app.getUser().getId());
				Attribute a = attrManager.getAttribute(registrarSession, u, URN_USER_PREFERRED_MAIL);
				if (a != null && a.getValue() != null) {
					message.setTo(BeansUtils.attributeValueToString(a));
				}
			}

		} catch (Exception ex) {
			// we don't care about exceptions - we have backup address (empty = mail not sent)
			log.error("[MAIL MANAGER] Exception thrown when getting users mail address for application: {}", app);
		}

	}


	/**
	 * Sets proper value "FROM" to mail message based on VO or GROUP attribute "fromEmail"
	 *
	 * If group attribute not set and is group application, get vo attribute as backup.
	 * If any attribute not set, BACKUP_FROM address is used.
	 *
	 * @param message message to set param FROM
	 * @param app application to decide if it's VO or Group application
	 */
	private void setFromMailAddress(SimpleMailMessage message, Application app){

		// set backup
		message.setFrom(getPropertyFromConfiguration("backupFrom"));

		// get proper value from attribute
		try {
			Attribute attrSenderEmail;

			if (app.getGroup() == null) {
				attrSenderEmail = attrManager.getAttribute(registrarSession, app.getVo(), URN_VO_FROM_EMAIL);
			} else {
				attrSenderEmail = attrManager.getAttribute(registrarSession, app.getGroup(), URN_GROUP_FROM_EMAIL);
				// use VO as backup
				if (attrSenderEmail == null || attrSenderEmail.getValue() == null) {
					attrSenderEmail = attrManager.getAttribute(registrarSession, app.getVo(), URN_VO_FROM_EMAIL);
				}
			}

			String senderEmail = "";
			if (attrSenderEmail != null && attrSenderEmail.getValue() != null) {
				senderEmail = BeansUtils.attributeValueToString(attrSenderEmail);
				message.setFrom(senderEmail);
			}
		} catch (Exception ex) {
			// we dont care about exceptions here - we have backup TO/FROM address
			if (app.getGroup() == null) {
				log.error("[MAIL MANAGER] Exception thrown when getting FROM email from attribute "+URN_VO_FROM_EMAIL+". Ex: {}", ex);
			} else {
				log.error("[MAIL MANAGER] Exception thrown when getting FROM email from attribute "+URN_GROUP_FROM_EMAIL+". Ex: {}", ex);
			}
		}
	}

	/**
	 * Get proper values "TO" for mail message based on VO or GROUP attribute "toEmail"
	 *
	 * If group attribute not set and is group application, get vo attribute as backup.
	 * If any attribute not set, BACKUP_FROM address is used.
	 *
	 * @param app application to decide if it's VO or Group application
	 *
	 * @return list of mail addresses to send mail to
	 */
	private List<String> getToMailAddresses(Application app){

		List<String> result = new ArrayList<String>();

		// get proper value from attribute
		try {
			Attribute attrToEmail;
			if (app.getGroup() == null) {
				attrToEmail = attrManager.getAttribute(registrarSession, app.getVo(), URN_VO_TO_EMAIL);
			} else {
				attrToEmail = attrManager.getAttribute(registrarSession, app.getGroup(), URN_GROUP_TO_EMAIL);
				// use VO as backup
				if (attrToEmail == null || attrToEmail.getValue() == null) {
					attrToEmail = attrManager.getAttribute(registrarSession, app.getVo(), URN_VO_TO_EMAIL);
				}
			}
			if (attrToEmail != null && attrToEmail.getValue() != null) {
				ArrayList<String> value = ((ArrayList<String>)attrToEmail.getValue());
				for (String adr : value) {
					if (adr != null && !adr.isEmpty()) {
						result.add(adr);
					}
				}
			}
		} catch (Exception ex) {
			// we dont care about exceptions here - we have backup TO/FROM address
			if (app.getGroup() == null) {
				log.error("[MAIL MANAGER] Exception thrown when getting TO email from attribute "+URN_VO_TO_EMAIL+". Ex: {}", ex);
			} else {
				log.error("[MAIL MANAGER] Exception thrown when getting TO email from attribute "+URN_GROUP_TO_EMAIL+". Ex: {}", ex);
			}
			// set backup
			result.clear();
			result.add(getPropertyFromConfiguration("backupTo"));
		}

		return result;

	}

	/**
	 * Substitute common strings in mail text for INVITATIONS based on passed params
	 *
	 * Substituted strings are:
	 *
	 * {voName} - full vo name
	 * {groupName} - group short name
	 * {displayName} - users display name returned from federation
	 *
	 * {invitationLink-[authz]} - link to VO's/group's application form
	 * {appGuiUrl-[authz]} - url to application GUI for user to see applications state
	 * {perunGuiUrl-[authz]} - url to perun GUI (user detail)
	 *
	 * {mailFooter} - common VO's footer
	 *
	 * @param vo vo this template belong to
	 * @param group group this template belong to
	 * @param user User to get name from, if null, param 'name' is used instead.
	 * @param name Optional name of user (for anonymous, used when user==null).
	 * @param mailText Original mail text template
	 */
	private String substituteCommonStringsForInvite(Vo vo, Group group, User user, String name, String mailText) {

		// replace voName
		if (mailText.contains("{voName}")) {
			mailText = mailText.replace("{voName}", vo.getName());
		}

		// replace groupName
		if (mailText.contains("{groupName}")) {
			if (group != null) {
				mailText = mailText.replace("{groupName}", group.getShortName());
			} else {
				mailText = mailText.replace("{groupName}", "");
			}
		}

		// replace name of user
		if (mailText.contains("{displayName}")) {
			if (user != null) {
				mailText = mailText.replace("{displayName}", user.getDisplayName());
			} else if (name != null && !name.isEmpty()) {
				mailText = mailText.replace("{displayName}", name);
			} else {
				mailText = mailText.replace("{displayName}", "");
			}
		}

		// replace invitation link
		if (mailText.contains("{invitationLink}")) {
			String url = getPerunUrl(vo, group);
			if (!url.endsWith("/")) url += "/";
			url += "registrar/";
			mailText = mailText.replace("{invitationLink}", buildInviteURL(vo, group, url));
		}

		// replace invitation link
		if (mailText.contains("{invitationLink-")) {

			Pattern pattern = Pattern.compile("\\{invitationLink-[^\\}]+\\}");
			Matcher m = pattern.matcher(mailText);
			while (m.find()) {

				// whole "{invitationLink-something}"
				String toSubstitute = m.group(0);

				// new login value to replace in text
				String newValue = "";

				Pattern namespacePattern = Pattern.compile("\\-(.*?)\\}");
				Matcher m2 = namespacePattern.matcher(toSubstitute);
				while (m2.find()) {

					// only namespace "fed", "cert",...
					String namespace = m2.group(1);
					String url = getPerunUrl(vo, group);

					if (url != null && !url.isEmpty()) {
						if (!url.endsWith("/")) url += "/";
						url += namespace + "/registrar/";
						newValue = buildInviteURL(vo, group, url);
					}

				}

				// substitute {invitationLink-authz} with actual value or empty string
				mailText = mailText.replace(toSubstitute, newValue);

			}

		}

		mailText = replacePerunGuiUrl(mailText, vo, group);
		mailText = replaceAppGuiUrl(mailText, vo, group);

		// mail footer
		if (mailText.contains("{mailFooter}")) {
			String footer = "";
			// get proper value from attribute
			try {
				Attribute attrFooter = attrManager.getAttribute(registrarSession, vo, URN_VO_MAIL_FOOTER);
				if (attrFooter != null && attrFooter.getValue() != null) {
					footer = BeansUtils.attributeValueToString(attrFooter);
				}
			} catch (Exception ex) {
				// we dont care about exceptions here
				log.error("[MAIL MANAGER] Exception thrown when getting VO's footer for email from attribute.", ex);
			}
			// replace by footer or empty
			mailText = mailText.replace("{mailFooter}", footer);
		}

		return mailText;

	}

	/**
	 * Replace link for mail invitation not concerning authz type
	 *
	 * @param vo vo to get invite link for
	 * @param group group if is for group application
	 * @param text base of URL for invitation
	 * @return full URL to application form
	 */
	private String buildInviteURL(Vo vo, Group group, String text) {

		if (text == null || text.isEmpty()) return "";

		text += "?vo=" + getEncodedString(vo.getShortName());

		if (group != null) {
			// application for group too
			text += "&group="+getEncodedString(group.getName());
		}

		return text;

	}

	/**
	 * Substitute common strings in mail text by data provided by
	 * application, application data and perun itself.
	 *
	 * Substituted strings are:
	 *
	 * {voName} - full vo name
	 * {groupName} - group short name
	 * {displayName} - user's display name returned from federation if present on form
	 * {firstName} - first name of user if present on form as separate form item
	 * {lastName} - last name of user if present on form as separate form item
	 * {appId} - application id
	 * {actor} - login in external system used when submitting application
	 * {extSource} - external system used for authentication when submitting application
	 * {appGuiUrl} - url to application GUI for user to see applications state
	 *
	 * {appGuiUrl-[authz]} - url to application GUI for user to see applications state
	 * {perunGuiUrl-[authz]} - url to perun GUI (user detail)
	 * {appDetailUrl-[authz]} - link for VO admin to approve / reject application
	 *
	 * {logins} - list of all logins from application
	 * {membershipExpiration} - membership expiration date
	 * {mail} - user preferred mail submitted on application or stored in a system
	 *
	 * {customMessage} - message passed by admin to mail (e.g. reason of application reject)
	 * {errors} - include errors which ocured when processing registrar actions
	 * (e.g. login reservation errors passed to mail for VO admin)
	 *
	 * (if possible links are for: Kerberos, Federation and Certificate authz)
	 *
	 * @param app Application to substitute strings for (get VO etc.)
	 * @param data ApplicationData needed for sustitution (displayName etc.)
	 * @param mailText String to substitute parts of
	 * @param reason Custom message passed by vo admin
	 * @param exceptions list of exceptions thrown when processing registrar actions
	 * @return modified text
	 */
	private String substituteCommonStrings(Application app, List<ApplicationFormItemData> data, String mailText, String reason, List<Exception> exceptions) {

		// replace app ID
		if (mailText.contains("{appId}")) {
			mailText = mailText.replace("{appId}", app.getId()+"");
		}

		// replace actor (app created by)
		if (mailText.contains("{actor}")) {
			mailText = mailText.replace("{actor}", app.getCreatedBy()+"");
		}

		// replace ext source (app created by)
		if (mailText.contains("{extSource}")) {
			mailText = mailText.replace("{extSource}", app.getExtSourceName()+"");
		}

		// replace voName
		if (mailText.contains("{voName}")) {
			mailText = mailText.replace("{voName}", app.getVo().getName());
		}

		// replace groupName
		if (mailText.contains("{groupName}")) {
			if (app.getGroup() != null) {
				mailText = mailText.replace("{groupName}", app.getGroup().getShortName());
			} else {
				mailText = mailText.replace("{groupName}", "");
			}
		}

		// replace customMessage (reason)
		if (mailText.contains("{customMessage}")) {
			if (reason != null && !reason.isEmpty()) {
				mailText = mailText.replace("{customMessage}", reason);
			} else {
				mailText = mailText.replace("{customMessage}", "");
			}
		}

		// replace displayName
		if (mailText.contains("{displayName}")) {
			String nameText = ""; // backup
			for (ApplicationFormItemData d : data) {
				// core attribute
				if ("urn:perun:user:attribute-def:core:displayName".equals(d.getFormItem().getPerunDestinationAttribute())) {
					if (d.getValue() != null && !d.getValue().isEmpty()) {
						nameText = d.getValue();
						break;
					}
				}
				// federation attribute
				if ("cn".equals(d.getFormItem().getFederationAttribute()) || "displayName".equals(d.getFormItem().getFederationAttribute())) {
					if (d.getValue() != null && !d.getValue().isEmpty()) {
						nameText = d.getValue();
						break;
					}
				}
			}

			if (nameText.isEmpty()) {
				User user = null;
				if (app.getUser() != null) {
					user = app.getUser();
				} else {
					try {
						user = usersManager.getUserByExtSourceNameAndExtLogin(registrarSession, app.getExtSourceName(), app.getCreatedBy());
					} catch (Exception ex) {
						// user not found is ok
					}
				}
				if (user != null) nameText = user.getDisplayName();
			}

			mailText = mailText.replace("{displayName}", nameText);
		}

		// replace firstName
		if (mailText.contains("{firstName}")) {
			String nameText = ""; // backup
			for (ApplicationFormItemData d : data) {
				if ("urn:perun:user:attribute-def:core:firstName".equals(d.getFormItem().getPerunDestinationAttribute())) {
					if (d.getValue() != null && !d.getValue().isEmpty()) {
						nameText = d.getValue();
						break;
					}
				}
			}

			if (nameText.isEmpty()) {
				User user = null;
				if (app.getUser() != null) {
					user = app.getUser();
				} else {
					try {
						user = usersManager.getUserByExtSourceNameAndExtLogin(registrarSession, app.getExtSourceName(), app.getCreatedBy());
					} catch (Exception ex) {
						// user not found is ok
					}
				}
				if (user != null) nameText = user.getFirstName();
			}

			mailText = mailText.replace("{firstName}", nameText);
		}

		// replace lastName
		if (mailText.contains("{lastName}")) {
			String nameText = ""; // backup
			for (ApplicationFormItemData d : data) {
				if ("urn:perun:user:attribute-def:core:lastName".equals(d.getFormItem().getPerunDestinationAttribute())) {
					if (d.getValue() != null && !d.getValue().isEmpty()) {
						nameText = d.getValue();
						break;
					}
				}
			}

			if (nameText.isEmpty()) {
				User user = null;
				if (app.getUser() != null) {
					user = app.getUser();
				} else {
					try {
						user = usersManager.getUserByExtSourceNameAndExtLogin(registrarSession, app.getExtSourceName(), app.getCreatedBy());
					} catch (Exception ex) {
						// user not found is ok
					}
				}
				if (user != null) nameText = user.getLastName();
			}

			mailText = mailText.replace("{lastName}", nameText);

		}

		// replace exceptions
		if (mailText.contains("{errors}")) {
			String errorText = "";
			if (exceptions != null && !exceptions.isEmpty()) {
				for (Exception ex : exceptions) {
					errorText = errorText.concat("\n\n"+ex.toString());
				}
			}
			mailText = mailText.replace("{errors}", errorText);
		}

		// replace logins
		if (mailText.contains("{login-")) {

			Pattern pattern = Pattern.compile("\\{login-[^\\}]+\\}");
			Matcher m = pattern.matcher(mailText);
			while (m.find()) {

				// whole "{login-something}"
				String toSubstitute = m.group(0);

				// new login value to replace in text
				String newValue = "";

				Pattern namespacePattern = Pattern.compile("\\-(.*?)\\}");
				Matcher m2 = namespacePattern.matcher(toSubstitute);
				while (m2.find()) {
					// only namespace "meta", "egi-ui",...
					String namespace = m2.group(1);

					// if user not known -> search through form items to get login
					for (ApplicationFormItemData d : data) {
						ApplicationFormItem item = d.getFormItem();
						if (item != null) {
							if (ApplicationFormItem.Type.USERNAME.equals(item.getType())) {
								// if username match namespace
								if (item.getPerunDestinationAttribute().contains("login-namespace:"+namespace)) {
									if (d.getValue() != null && !d.getValue().isEmpty()) {
										// save not null or empty value and break cycle
										newValue = d.getValue();
										break;
									}
								}
							}
						}
					}

					// if user exists, try to get login from attribute instead of application
					// since we do no allow to overwrite login by application
					try {
						if (app.getUser() != null) {
							List<Attribute> logins = attrManager.getLogins(registrarSession, app.getUser());
							for (Attribute a : logins) {
								// replace only correct namespace
								if (a.getFriendlyNameParameter().equalsIgnoreCase(namespace)) {
									if (a.getValue() != null) {
										newValue = BeansUtils.attributeValueToString(a);
										break;
									}
								}
							}
						}
					} catch (Exception ex) {
						log.error("[MAIL MANAGER] Error thrown when replacing login in namespace \""+namespace+"\" for mail. {}", ex);
					}

				}

				// substitute {login-namespace} with actual value or empty string
				mailText = mailText.replace(toSubstitute, newValue);

			}

		}

		mailText = replaceAppDetailUrl(mailText, app.getId(), app.getVo(), app.getGroup());
		mailText = replaceAppGuiUrl(mailText, app.getVo(), app.getGroup());
		mailText = replacePerunGuiUrl(mailText, app.getVo(), app.getGroup());

		// membership expiration
		if (mailText.contains("{membershipExpiration}")) {
			String expiration = "";
			if (app.getUser() != null) {
				try {
					User u = usersManager.getUserById(registrarSession, app.getUser().getId());
					Member m = membersManager.getMemberByUser(registrarSession, app.getVo(), u);
					Attribute a = attrManager.getAttribute(registrarSession, m, URN_MEMBER_EXPIRATION);
					if (a != null && a.getValue() != null) {
						// attribute value is string
						expiration = ((String)a.getValue());
					}
				} catch (Exception ex) {
					log.error("[MAIL MANAGER] Error thrown when getting membership expiration param for mail. {}", ex);
				}
			}
			// replace by date or empty
			mailText = mailText.replace("{membershipExpiration}", expiration);
		}

		// user mail
		if (mailText.contains("{mail}")) {
			String mail = "";
			if (app.getUser() != null) {
				try {
					User u = usersManager.getUserById(registrarSession, app.getUser().getId());
					Attribute a = attrManager.getAttribute(registrarSession, u, URN_USER_PREFERRED_MAIL);
					if (a != null && a.getValue() != null) {
						// attribute value is string
						mail = ((String)a.getValue());
					}
				} catch (Exception ex) {
					log.error("[MAIL MANAGER] Error thrown when getting preferred mail param for mail. {}", ex);
				}
			} else {

				for (ApplicationFormItemData d : data) {
					if ("urn:perun:member:attribute-def:def:mail".equals(d.getFormItem().getPerunDestinationAttribute())) {
						if (d.getValue() != null && !d.getValue().isEmpty()) {
							mail = d.getValue();
							break;
						}
					}
				}

				for (ApplicationFormItemData d : data) {
					if ("urn:perun:user:attribute-def:def:preferredMail".equals(d.getFormItem().getPerunDestinationAttribute())) {
						if (d.getValue() != null && !d.getValue().isEmpty()) {
							mail = d.getValue();
							break;
						}
					}
				}


			}

			// replace by mail or empty
			mailText = mailText.replace("{mail}", mail);
		}

		// mail footer
		if (mailText.contains("{mailFooter}")) {
			String footer = "";
			// get proper value from attribute
			try {
				Attribute attrFooter = attrManager.getAttribute(registrarSession, app.getVo(), URN_VO_MAIL_FOOTER);
				if (attrFooter != null && attrFooter.getValue() != null) {
					footer = BeansUtils.attributeValueToString(attrFooter);
				}
			} catch (Exception ex) {
				// we dont care about exceptions here
				log.error("[MAIL MANAGER] Exception thrown when getting VO's footer for email from attribute.", ex);
			}
			// replace by footer or empty
			mailText = mailText.replace("{mailFooter}", footer);
		}

		return mailText;
	}

	/**
	 * Return base URL of Perun instance taken from VO/Group attribute. If not set,
	 * value of config property "perunUrl" is used. If can't determine, then empty
	 * string is returned.
	 *
	 * e.g. https://perun.cesnet.cz
	 *
	 * @param vo vo to get link for
	 * @param group to get link for
	 * @return Base url or empty string.
	 */
	private String getPerunUrl(Vo vo, Group group) {

		String result = getPropertyFromConfiguration("perunUrl");
		try {

			if (group != null) {

				Attribute a = attrManager.getAttribute(registrarSession, group, URN_GROUP_REGISTRATION_URL);
				if (a != null && a.getValue() != null && !((String)a.getValue()).isEmpty()) {

					result = (String)a.getValue();

				} else {
					// take it from the VO if not on group settings
					Attribute a2 = attrManager.getAttribute(registrarSession, vo, URN_VO_REGISTRATION_URL);
					if (a2 != null && a2.getValue() != null && !((String)a2.getValue()).isEmpty()) {
						result = (String)a2.getValue();
					}
				}

			} else {

				// take it from the VO
				Attribute a2 = attrManager.getAttribute(registrarSession, vo, URN_VO_REGISTRATION_URL);
				if (a2 != null && a2.getValue() != null && !((String)a2.getValue()).isEmpty()) {
					result = (String)a2.getValue();
				}

			}

		} catch (Exception ex) {

			if (group != null) {
				log.error("[MAIL MANAGER] Exception when getting perun instance link for {} : {}", group, ex);
			} else {
				log.error("[MAIL MANAGER] Exception when getting perun instance link for {} : {}", vo, ex);
			}

		} finally {

			return result;

		}

	}

	private String replacePerunGuiUrl(String mailText, Vo vo, Group group) {

		// replace perun GUI links
		if (mailText.contains("{perunGuiUrl}")) {
			String text = getPerunUrl(vo, group);
			if (text != null && !text.isEmpty()) {
				if (!text.endsWith("/")) text += "/";
				text += "gui/";
			}
			mailText = mailText.replace("{perunGuiUrl}", text);
		}

		// replace perun GUI app link
		if (mailText.contains("{perunGuiUrl-")) {

			Pattern pattern = Pattern.compile("\\{perunGuiUrl-[^\\}]+\\}");
			Matcher m = pattern.matcher(mailText);
			while (m.find()) {

				// whole "{perunGuiUrl-something}"
				String toSubstitute = m.group(0);

				// new login value to replace in text
				String newValue = "";

				Pattern namespacePattern = Pattern.compile("\\-(.*?)\\}");
				Matcher m2 = namespacePattern.matcher(toSubstitute);
				while (m2.find()) {

					// only namespace "fed", "cert",...
					String namespace = m2.group(1);

					newValue = getPerunUrl(vo, group);
					if (newValue != null && !newValue.isEmpty()) {
						if (!newValue.endsWith("/")) newValue += "/";
						newValue += namespace + "/gui/";
					}

				}

				// substitute {appGuiUrl-authz} with actual value or empty string
				mailText = mailText.replace(toSubstitute, newValue);

			}

		}

		return mailText;

	}

	private String replaceAppGuiUrl(String mailText, Vo vo, Group group) {

		// replace perun application GUI link with list of applications
		if (mailText.contains("{appGuiUrl}")) {
			// new backup
			String text = getPerunUrl(vo, group);
			if (text != null && !text.isEmpty()) {
				if (!text.endsWith("/")) text += "/";
				text += "registrar/";
				text += "?vo=" + getEncodedString(vo.getShortName()) + "&page=apps";
			}
			if (group != null) {
				text += "&group="+getEncodedString(group.getName());
			}
			mailText = mailText.replace("{appGuiUrl}", text);
		}


		// replace registrar GUI link
		if (mailText.contains("{appGuiUrl-")) {

			Pattern pattern = Pattern.compile("\\{appGuiUrl-[^\\}]+\\}");
			Matcher m = pattern.matcher(mailText);
			while (m.find()) {

				// whole "{appGuiUrl-something}"
				String toSubstitute = m.group(0);

				// new login value to replace in text
				String newValue = "";

				Pattern namespacePattern = Pattern.compile("\\-(.*?)\\}");
				Matcher m2 = namespacePattern.matcher(toSubstitute);
				while (m2.find()) {
					// only namespace "fed", "cert",...
					String namespace = m2.group(1);

					newValue = getPerunUrl(vo, group);

					if (newValue != null && !newValue.isEmpty()) {
						if (!newValue.endsWith("/")) newValue += "/";
						newValue += namespace + "/registrar/";
						newValue += "?vo="+getEncodedString(vo.getShortName());
						newValue += ((group != null) ? "&group="+getEncodedString(group.getName()) : "");
						newValue += "&page=apps";
					}

				}

				// substitute {appGuiUrl-authz} with actual value or empty string
				mailText = mailText.replace(toSubstitute, newValue);

			}

		}

		return mailText;

	}

	private String replaceAppDetailUrl(String mailText, int appId, Vo vo, Group group) {

		// replace appDetail for VO admins
		if (mailText.contains("{appDetailUrl}")) {
			String text = getPerunUrl(vo, group);
			if (text != null && !text.isEmpty()) {
				if (!text.endsWith("/")) text += "/";
				text += "gui/";
				String separator = "#";
				for (String s : getFedAuthz()) {
					if (text.endsWith(s+"/gui/")) {
						separator = "?";
						break;
					}
				}
				text += separator + "vo/appdetail?id="+appId;
			}
			mailText = mailText.replace("{appDetailUrl}", text);
		}

		// replace perun app link
		if (mailText.contains("{appDetailUrl-")) {

			Pattern pattern = Pattern.compile("\\{appDetailUrl-[^\\}]+\\}");
			Matcher m = pattern.matcher(mailText);
			while (m.find()) {

				// whole "{appDetailUrl-something}"
				String toSubstitute = m.group(0);

				// new login value to replace in text
				String newValue = "";

				Pattern namespacePattern = Pattern.compile("\\-(.*?)\\}");
				Matcher m2 = namespacePattern.matcher(toSubstitute);
				while (m2.find()) {

					// only namespace "fed", "cert",...
					String namespace = m2.group(1);

					newValue = getPerunUrl(vo, group);
					if (newValue != null && !newValue.isEmpty()) {
						if (!newValue.endsWith("/")) newValue += "/";
						newValue += namespace + "/gui/";
						newValue += getFedAuthz().contains(namespace) ? "?vo/appdetail?id="+appId : "#vo/appdetail?id="+appId;
					}

				}

				// substitute {appDetailUrl-authz} with actual value or empty string
				mailText = mailText.replace(toSubstitute, newValue);

			}

		}

		return mailText;

	}

	/**
	 * Get parts of URL, which are considered federative and must have "?" in URLs and not "#".
	 *
	 * @return Array of federative URLs
	 */
	private ArrayList<String> getFedAuthz() {

		ArrayList<String> fedAuthz = new ArrayList<String>();
		fedAuthz.add("fed");
		String fedString = getPropertyFromConfiguration("fedAuthz");
		if (fedString != null && !fedString.isEmpty()) {
			String[] array = fedString.split(",");
			for (String s : array) {
				fedAuthz.add(s.trim());
			}
		}
		return fedAuthz;
	}

	/**
	 * Return URL encoded String in utf-8. If not possible, return original string.
	 *
	 * @param s String to encode
	 * @return URL Encoded string
	 */
	private static String getEncodedString(String s) {

		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			return s;
		}

	}

}