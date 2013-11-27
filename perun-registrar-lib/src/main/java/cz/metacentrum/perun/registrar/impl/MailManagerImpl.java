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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.transaction.annotation.Transactional;

import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.bl.PerunBl;
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
	private static final String URN_USER_PREFERRED_MAIL = "urn:perun:user:attribute-def:def:preferredMail";
    private static final String URN_USER_PREFERRED_LANGUAGE = "urn:perun:user:attribute-def:def:preferredLanguage";
    private static final String URN_MEMBER_MAIL = "urn:perun:member:attribute-def:def:mail";
    private static final String URN_MEMBER_EXPIRATION = "urn:perun:member:attribute-def:def:membershipExpiration";
    private static final String URN_GROUP_TO_EMAIL = "urn:perun:group:attribute-def:def:toEmail";
    private static final String URN_GROUP_FROM_EMAIL = "urn:perun:group:attribute-def:def:fromEmail";
    private static final String URN_VO_LANGUAGE_EMAIL = "urn:perun:vo:attribute-def:def:notificationsDefLang";
    private static final String URN_GROUP_LANGUAGE_EMAIL = "urn:perun:group:attribute-def:def:notificationsDefLang";

	@Autowired PerunBl perun;
	@Autowired RegistrarManager registrarManager;
    @Autowired private Properties registrarProperties;
	private PerunSession registrarSession;
	private SimpleJdbcTemplate jdbc;
	private MailSender mailSender;
	private AttributesManager attrManager;
	private MembersManager membersManager;
	private UsersManager usersManager;
	
	// Spring setters
	
	public void setDataSource(DataSource dataSource) {
		this.jdbc =  new SimpleJdbcTemplate(dataSource);
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
	        ExtSourcesManager.EXTSOURCE_INTERNAL,
	        ExtSourcesManager.EXTSOURCE_INTERNAL);
	    registrarSession = perun.getPerunSession(pp);
	    
	    this.attrManager = perun.getAttributesManager();
	    this.membersManager = perun.getMembersManager();
	    this.usersManager = perun.getUsersManager();

	}

	@Override
	public Integer addMail(PerunSession sess, ApplicationForm form, ApplicationMail mail) throws PerunException {
		
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
		
		int result = jdbc.update("delete from application_mails where id=?", id);
		if (result == 0) throw new InternalErrorException("Mail notification with id="+id+" doesn't exists!");
		if (result == 1) log.info("[MAIL MANAGER] Mail notification with id={} deleted", id); 
		if (result > 1) throw new ConsistencyErrorException("There is more than one mail notification with id="+id);
		
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
		
		// update sending (enabled / disabled)
		jdbc.update("update application_mails set send=? where id=?", mail.getSend(), mail.getId());
		
		// update texts (easy way = delete and new insert)
		jdbc.update("delete from application_mail_texts where mail_id=?", mail.getId());
		
		for (Locale loc : mail.getMessage().keySet()) {
			MailText text = mail.getMessage(loc);
			jdbc.update("insert into application_mail_texts(mail_id,locale,subject,text) values (?,?,?,?)",
					mail.getId(), loc.toString(), text.getSubject(), text.getText());			
		}

	}
	
	@Override
	public void setSendingEnabled(PerunSession sess, List<ApplicationMail> mails, boolean enabled) throws PerunException {
		
		// TODO authz
		if (mails == null) { throw new InternalErrorException("Mails definitions to update can't be null"); }
		
		for (ApplicationMail mail : mails) {
			// update sending (enabled / disabled)
			jdbc.update("update application_mails set send=? where id=?", enabled, mail.getId());
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
            addMail(sess, formTo, mail);
        }

    }

    @Override
    public void copyMailsFromGroupToGroup(PerunSession sess, Group fromGroup, Group toGroup) throws PerunException {

        if (!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, fromGroup) ||
                !AuthzResolver.isAuthorized(sess, Role.VOADMIN, perun.getVosManager().getVoById(sess, fromGroup.getVoId()))) {
            throw new PrivilegeException(sess, "copyMailsFromGroupToGroup");
        }
        if (!AuthzResolver.isAuthorized(sess, Role.GROUPADMIN, toGroup) ||
                !AuthzResolver.isAuthorized(sess, Role.VOADMIN, perun.getVosManager().getVoById(sess, toGroup.getVoId()))) {
            throw new PrivilegeException(sess, "copyMailsFromGroupToGroup");
        }

        ApplicationForm formFrom = registrarManager.getFormForGroup(fromGroup);
        ApplicationForm formTo = registrarManager.getFormForGroup(toGroup);
        List<ApplicationMail> mails = getApplicationMails(sess, formFrom);
        for (ApplicationMail mail : mails) {
            addMail(sess, formTo, mail);
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
				log.error("[MAIL MANAGER] Mail not send. Definition (or mail text) for: {} do not exists for VO: "+app.getVo()+" and Group: "+app.getGroup(), mailType.toString());
				return; // mail not found
			} else if (mail.getSend() == false) {
				log.info("[MAIL MANAGER] Mail not send. Disabled by VO admin.");
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
					log.info("[MAIL MANAGER] Sending mail: APP_CREATED_USER to: {}", message.getTo());
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
						log.info("[MAIL MANAGER] Sending mail: APP_CREATED_VO_ADMIN to: {}", message.getTo());
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
				message.setText(mailText);
				
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
						    
						    // get base url for validation
						    String url = getPropertyFromConfiguration("registrarGuiFed");
                            String urlNon = getPropertyFromConfiguration("registrarGuiNon");
                            String urlCert = getPropertyFromConfiguration("registrarGuiCert");
                            String urlKrb = getPropertyFromConfiguration("registrarGuiKrb");

                            if (url != null && !url.isEmpty()) url = url + "?vo=" + app.getVo().getShortName();
                            if (urlNon != null && !urlNon.isEmpty()) urlNon = urlNon + "?vo=" + app.getVo().getShortName();
                            if (urlCert != null && !urlCert.isEmpty()) urlCert = urlCert + "?vo=" + app.getVo().getShortName();
                            if (urlKrb != null && !urlKrb.isEmpty()) urlKrb = urlKrb + "?vo=" + app.getVo().getShortName();

                            if (app.getGroup() != null) {
								// append group name for 
								url += "&group="+app.getGroup().getName();
                                urlNon += "&group="+app.getGroup().getName();
                                urlKrb += "&group="+app.getGroup().getName();
                                urlCert += "&group="+app.getGroup().getName();
							}
							
						    // construct whole url
						    StringBuilder url2 = new StringBuilder(url);
                            StringBuilder urlNon2 = new StringBuilder(urlNon);
                            StringBuilder urlCert2 = new StringBuilder(urlCert);
                            StringBuilder urlKrb2 = new StringBuilder(urlKrb);

                            if (url.contains("?")) {
						    	if (!url.endsWith("?")) {
						    		url2.append("&");
						    	}
						    } else {
						    	url2.append("?");
						    }
                            if (urlNon.contains("?")) {
                                if (!urlNon.endsWith("?")) {
                                    urlNon2.append("&");
                                }
                            } else {
                                urlNon2.append("&");
                            }
                            if (urlKrb.contains("?")) {
                                if (!urlKrb.endsWith("?")) {
                                    urlKrb2.append("&");
                                }
                            } else {
                                urlKrb2.append("&");
                            }
                            if (urlCert.contains("?")) {
                                if (!urlCert.endsWith("?")) {
                                    urlCert2.append("&");
                                }
                            } else {
                                urlCert2.append("&");
                            }

						    try {
						    	url2.append("i=").append(URLEncoder.encode(i, "UTF-8")).append("&m=").append(URLEncoder.encode(m, "UTF-8"));
                                urlNon2.append("i=").append(URLEncoder.encode(i, "UTF-8")).append("&m=").append(URLEncoder.encode(m, "UTF-8"));
                                urlKrb2.append("i=").append(URLEncoder.encode(i, "UTF-8")).append("&m=").append(URLEncoder.encode(m, "UTF-8"));
                                urlCert2.append("i=").append(URLEncoder.encode(i, "UTF-8")).append("&m=").append(URLEncoder.encode(m, "UTF-8"));
                                log.info("[MAIL MANAGER] Whole encoded url: {}", url2.toString());
						    } catch (UnsupportedEncodingException ex) {
						    	url2.append("i=").append(i).append("&m=").append(m);
                                urlNon2.append("i=").append(i).append("&m=").append(m);
                                urlCert2.append("i=").append(i).append("&m=").append(m);
                                urlKrb2.append("i=").append(i).append("&m=").append(m);
                                log.info("[MAIL MANAGER] Unable to encode as UTF-8, send unencoded: {}",url2.toString());
						    }
						    
						    // replace validation link
						    message.setText(message.getText().replace("{validationLink}", url2.toString()));
                            message.setText(message.getText().replace("{validationLinkNon}", urlNon2.toString()));
                            message.setText(message.getText().replace("{validationLinkCert}", urlCert2.toString()));
                            message.setText(message.getText().replace("{validationLinkKrb}", urlKrb2.toString()));

                            try {
								mailSender.send(message);
								log.info("[MAIL MANAGER] Sending mail: MAIL_VALIDATION to: {}", message.getTo());
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
					log.info("[MAIL MANAGER] Sending mail: APP_APPROVED_USER to: {}", message.getTo());
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
					log.info("[MAIL MANAGER] Sending mail: APP_REJECTED_USER to: {}", message.getTo());
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
                        log.info("[MAIL MANAGER] Sending mail: APP_ERROR_VO_ADMIN to: {}", message.getTo());
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
				log.error("[MAIL MANAGER] Exception thrown when getting preferred language for User={}: {}", app.getUser(), ex);
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
     * If attribute not set, BACKUP_FROM address is used
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
     * If attribute not set, BACKUP_TO address is used
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
	 * Substitute common strings in mail text by data provided by
	 * application, application data and perun itself.
	 * 
	 * Substituted strings are:
	 * {voName} - full vo name
	 * {displayName} - users display name returned from federation
	 * {firstName}
	 * {lastName}
	 * {appId} - application id
	 * {appGuiUrl} - url to application GUI for user to see applications state
	 * {perunGuiUrlFed} - url to perun GUI (user detail)
	 * {perunGuiUrlKerb} - url to perun GUI (user detail)
	 * {perunGuiUrlCert} - url to perun GUI (user detail)
	 * {appDetailUrlFed} - link for VO admin to approve / reject application
	 * {appDetailUrlKerb} - link for VO admin to approve / reject application
	 * {appDetailUrlCert} - link for VO admin to approve / reject application
	 * {logins} - list of all logins from application
	 * {membershipExpiration} - membership expiration date
	 * 
	 * {customMessage} - message passed by admin to mail (e.g. reason of application reject)
	 * {errors} - include errors which occured when processing registrar actions 
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
				mailText = mailText.replace("{groupName}", app.getGroup().getName());
			} else {
				mailText = mailText.replace("{groupName}", "");
			}
		}
		
		// replace perun application GUI link with list of applications
		if (mailText.contains("{appGuiUrl}")) {
            String text = getPropertyFromConfiguration("registrarGuiFed");
			if (text != null && !text.isEmpty()) text = text + "?vo=" + app.getVo().getShortName() + "&page=apps";
			if (app.getGroup() != null) {
				text = text + "&group="+app.getGroup().getId();
			}
			mailText = mailText.replace("{appGuiUrl}", text);
		}

        if (mailText.contains("{appGuiUrlKrb}")) {
            String text = getPropertyFromConfiguration("registrarGuiKrb");
            if (text != null && !text.isEmpty()) text = text + "?vo=" + app.getVo().getShortName() + "&page=apps";
            if (app.getGroup() != null) {
                text = text + "&group="+app.getGroup().getId();
            }
            mailText = mailText.replace("{appGuiUrlKrb}", text);
        }

        if (mailText.contains("{appGuiUrlCert}")) {
            String text = getPropertyFromConfiguration("registrarGuiCert");
            if (text != null && !text.isEmpty()) text = text + "?vo=" + app.getVo().getShortName() + "&page=apps";
            if (app.getGroup() != null) {
                text = text + "&group="+app.getGroup().getId();
            }
            mailText = mailText.replace("{appGuiUrlCert}", text);
        }

        if (mailText.contains("{appGuiUrlNon}")) {
            String text = getPropertyFromConfiguration("registrarGuiNon");
            if (text != null && !text.isEmpty()) text = text + "?vo=" + app.getVo().getShortName() + "&page=apps";
            if (app.getGroup() != null) {
                text = text + "&group="+app.getGroup().getId();
            }
            mailText = mailText.replace("{appGuiUrlNon}", text);
        }

		// replace appDetail for vo admin
		if (mailText.contains("{appDetailUrlFed}")) {
			String text = getPropertyFromConfiguration("perunGuiFederation");
            // MUST USE "?" malformed URL while redirecting - GUI now can handle this
			if (text!=null && !text.isEmpty()) text = text+"?vo/appdetail?id="+app.getId();
			mailText = mailText.replace("{appDetailUrlFed}", text);
		}
		// replace appDetail for vo admin
		if (mailText.contains("{appDetailUrlKerb}")) {
			String text = getPropertyFromConfiguration("perunGuiKerberos");
			if (text!=null && !text.isEmpty()) text = text+"#vo/appdetail?id="+app.getId();			
			mailText = mailText.replace("{appDetailUrlKerb}", text);
		}
		// replace appDetail for vo admin
		if (mailText.contains("{appDetailUrlCert}")) {
			String text = getPropertyFromConfiguration("perunGuiCert");
			if (text!=null && !text.isEmpty()) text = text+"#vo/appdetail?id="+app.getId();			
			mailText = mailText.replace("{appDetailUrlCert}", text);
		}
		
		// replace perun gui link
		if (mailText.contains("{perunGuiUrlFed}")) {
			String text = getPropertyFromConfiguration("perunGuiFederation");
			mailText = mailText.replace("{perunGuiUrlFed}", text);
		}
		// replace perun gui link
		if (mailText.contains("{perunGuiUrlKerb}")) {
			String text = getPropertyFromConfiguration("perunGuiKerberos");
			mailText = mailText.replace("{perunGuiUrlKerb}", text);
		}
		// replace perun gui link
		if (mailText.contains("{perunGuiUrlCert}")) {
			String text = getPropertyFromConfiguration("perunGuiCert");
			mailText = mailText.replace("{perunGuiUrlCert}", text);
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

            Pattern LoginPattern = Pattern.compile("\\{login-[^\\}]+\\}");
            Matcher m = LoginPattern.matcher(mailText);
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
	
}