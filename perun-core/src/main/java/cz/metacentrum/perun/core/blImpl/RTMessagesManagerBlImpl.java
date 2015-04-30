package cz.metacentrum.perun.core.blImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.RTMessage;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.RTMessagesManagerBl;
import cz.metacentrum.perun.core.impl.Utils;

/**
 * RTMessage manager can create a new message and send it to RT like predefined service user.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class RTMessagesManagerBlImpl implements RTMessagesManagerBl{
	private String rtURL;
	private PerunBl perunBl;
	private final static org.slf4j.Logger log = LoggerFactory.getLogger(RTMessagesManagerBlImpl.class);
	private final String defaultQueue = "perunv3";

	private Pattern ticketNumberPattern = Pattern.compile("^# Ticket ([0-9]+) created.");

	public RTMessagesManagerBlImpl(PerunBl perunBl) throws InternalErrorException {
		this();
		this.perunBl = perunBl;
		rtURL = BeansUtils.getPropertyFromConfiguration("perun.rt.url");
	}

	public RTMessagesManagerBlImpl() throws InternalErrorException {
		rtURL = BeansUtils.getPropertyFromConfiguration("perun.rt.url");
	}

	public RTMessage sendMessageToRT(PerunSession sess, int voId, String subject, String text) throws InternalErrorException {
		return sendMessageToRT(sess, voId, null, subject, text);
	}

	@Deprecated
	public RTMessage sendMessageToRT(PerunSession sess, Member meber, String queue, String subject, String text) throws InternalErrorException {
		throw new InternalErrorException("This method is not supported now!");
	}

	public RTMessage sendMessageToRT(PerunSession sess, String queue, String subject, String text) throws InternalErrorException {
		return sendMessageToRT(sess, 0, queue, subject, text);
	}

	public RTMessage sendMessageToRT(PerunSession sess, int voId, String queue, String subject, String text) throws InternalErrorException {
		log.debug("Parameters of rtMessage are queue='" + queue +"', subject='{}' and text='{}'", subject, text);

		//Get Email from User who get from session
		String email = null;
		User user = sess.getPerunPrincipal().getUser();
		
		//try to get user/member email from user in session
		if(user != null) email = findUserPreferredEmail(sess, user);
		else {
			email = null;
			log.error("Can't get user from session.");
		}

		//try to get email from additionalInformations in session (attribute mail)
		if(email == null) {
			Matcher emailMatcher;
			Map<String,String> additionalInfo = sess.getPerunPrincipal().getAdditionalInformations();
			//If there are some data in additionalInfo
			if(additionalInfo != null) {
				String mailInfo = additionalInfo.get("mail");
				//If there is notnull attribute "mail" in map
				if(mailInfo != null) {
					//If attribute mail has separator ',' or ';'
					if(mailInfo.contains(";")) {
						String[] mailsFromInfo = mailInfo.split(";");
						for(String mail: mailsFromInfo) {
							emailMatcher = Utils.emailPattern.matcher(mail);
							if(emailMatcher.matches()) {
								email = mail;
								break;
							}
						}
					} else if(mailInfo.contains(",")) {
						String[] mailsFromInfo = mailInfo.split(",");
						for(String mail: mailsFromInfo) {
							emailMatcher = Utils.emailPattern.matcher(mail);
							if(emailMatcher.matches()) {
								email = mail;
								break;
							}
						}
					} else {
						//If there is no separator, test if this has format of email, if yes, save it to email
						emailMatcher = Utils.emailPattern.matcher(mailInfo);
						if(emailMatcher.matches()) {
							email = mailInfo;
						}
					}
				}
			}
		}

		//Prepare sending message
		HttpResponse response;
		HttpClient httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY, org.apache.http.client.params.CookiePolicy.IGNORE_COOKIES);

		StringBuilder responseMessage = new StringBuilder();
		String ticketNumber = "0";
		try {
			response = httpClient.execute(this.prepareDataAndGetHttpRequest(sess, voId, queue, email, subject, text));
			BufferedReader bw = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			//Reading response from RT
			String line;
			while((line = bw.readLine()) != null) {
				responseMessage.append(line);
				responseMessage.append('\n');
				//Matcher for ticketNumber
				Matcher ticketNumberMatcher = this.ticketNumberPattern.matcher(line);
				if(ticketNumberMatcher.find()) {
					ticketNumber = ticketNumberMatcher.group(1);
				}
			}
		} catch (IOException ex) {
			throw new InternalErrorException("IOException has been throw while executing http request.", ex);
		}

		//Return message if response is ok, or throw exception with bad response
		int ticketNum = Integer.valueOf(ticketNumber);
		if(ticketNum != 0) {
			RTMessage rtmessage = new RTMessage(email, ticketNum);
			log.debug("RT message was send successfully and the ticket has number: " + ticketNum);
			return rtmessage;
		} else {
			throw new InternalErrorException("RT message was not send due to error with RT returned this message: " + responseMessage.toString());
		}
	}

	private String findUserPreferredEmail(PerunSession sess, User user) throws InternalErrorException {
		String email = null;
		Attribute userPreferredMail = null;
		try {
			userPreferredMail = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, "urn:perun:user:attribute-def:def:preferredMail");
		} catch (WrongAttributeAssignmentException ex) {
			throw new InternalErrorException(ex);
		} catch (AttributeNotExistsException ex) {
			throw new ConsistencyErrorException(ex);
		}

		if(userPreferredMail == null || userPreferredMail.getValue() == null) {
			try {
				userPreferredMail = getPerunBl().getAttributesManagerBl().getAttribute(sess, user, "urn:perun:user:attribute-def:def:mail");
			} catch (WrongAttributeAssignmentException ex) {
				throw new InternalErrorException(ex);
			} catch (AttributeNotExistsException ex) {
				throw new ConsistencyErrorException(ex);
			}

		}

		if(userPreferredMail != null && userPreferredMail.getValue() != null) {
			email = (String) userPreferredMail.getValue();
		}
		return email;
	}

	private HttpUriRequest prepareDataAndGetHttpRequest(PerunSession sess, int voId, String queue, String requestor, String subject, String text) throws InternalErrorException {
		//Ticket from this part is already evidet like 'new'
		String id = "ticket/new";
		//If there is no requestor, it is uknown requestor
		if(requestor == null || requestor.isEmpty()) {
			requestor = "unknown";
		}
		//If queue is null, try to check if exist value in attribute rtVoQueue, if not, use default
		if(queue == null || queue.isEmpty()) {
			Vo vo = null;
			if(voId != 0) {
				try {
					vo = perunBl.getVosManagerBl().getVoById(sess, voId);
				} catch (VoNotExistsException ex) {
					throw new InternalErrorException("VoId with Id=" + voId + " not exists.", ex);
				}
				Attribute voQueue = null;
				try {
					voQueue = perunBl.getAttributesManagerBl().getAttribute(sess, vo, AttributesManager.NS_VO_ATTR_DEF + ":RTVoQueue");
				} catch (AttributeNotExistsException ex) {
					throw new InternalErrorException("Attribute RTVoQueue not exists.", ex);
				} catch (WrongAttributeAssignmentException ex) {
					throw new InternalErrorException(ex);
				}
				if(voQueue.getValue() != null) {
					queue = (String) voQueue.getValue();
				} else queue = defaultQueue;
			} else queue = defaultQueue;
		}
		//If subject is null or empty, use Unspecified instead
		if(subject == null || subject.isEmpty()) subject = "(No subject)";
		//Text can be null so if it is, put empty string
		if(text == null) text = "";

		//Prepare credentials
		String username = BeansUtils.getPropertyFromConfiguration("perun.rt.serviceuser.username");
		String password = BeansUtils.getPropertyFromConfiguration("perun.rt.serviceuser.password");

		//Prepare content of message
		MultipartEntity entity = new MultipartEntity();
		try {
			entity.addPart("Content-Typ", new StringBody("application/x-www-form-urlencoded"));
			entity.addPart("charset", new StringBody("utf-8"));
			entity.addPart("Connection", new StringBody("Close"));
			StringBody content = new StringBody("id: " + id + '\n' +
					"Queue: " + queue + '\n' +
					"Requestor: " + requestor + '\n' +
					"Subject: " + subject + '\n' +
					"Text: " + text,
					Charset.forName("utf-8"));
			entity.addPart("content", content);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		//Test rtURL for null
		if(rtURL == null || rtURL.length() == 0) throw new InternalErrorException("rtURL is not prepared and is null in the moment of posting.");

		// prepare post request
		HttpPost post = new HttpPost(rtURL);
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);

		post.addHeader(BasicScheme.authenticate(credentials, "utf-8", false));
		post.setEntity(entity);

		return post;
	}

	public PerunBl getPerunBl() {
		return this.perunBl;
	}

	public void setPerunBl(PerunBl perunBl) {
		this.perunBl = perunBl;
	}
}
