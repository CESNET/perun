package cz.metacentrum.perun.core.blImpl;

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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

/**
 * RTMessage manager can create a new message and send it to RT like predefined service user.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public class RTMessagesManagerBlImpl implements RTMessagesManagerBl {

  private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RTMessagesManagerBlImpl.class);

  private final PerunBl perunBl;
  private final String rtURL;
  private final String rtDefaultQueue;
  private final MailSender mailSender = BeansUtils.getDefaultMailSender();

  private final Pattern ticketNumberPattern = Pattern.compile("^# Ticket ([0-9]+) created.");

  public RTMessagesManagerBlImpl(PerunBl perunBl, String rtURL, String rtDefaultQueue) {
    this.perunBl = perunBl;
    this.rtURL = rtURL;
    this.rtDefaultQueue = rtDefaultQueue;
  }

  private String findUserPreferredEmail(PerunSession sess, User user) {
    String email = null;
    Attribute userPreferredMail;
    try {
      userPreferredMail = getPerunBl().getAttributesManagerBl()
          .getAttribute(sess, user, "urn:perun:user:attribute-def:def:preferredMail");
    } catch (WrongAttributeAssignmentException ex) {
      throw new InternalErrorException(ex);
    } catch (AttributeNotExistsException ex) {
      throw new ConsistencyErrorException(ex);
    }

    if (userPreferredMail == null || userPreferredMail.getValue() == null) {
      try {
        userPreferredMail =
            getPerunBl().getAttributesManagerBl().getAttribute(sess, user, "urn:perun:user:attribute-def:def:mail");
      } catch (WrongAttributeAssignmentException ex) {
        throw new InternalErrorException(ex);
      } catch (AttributeNotExistsException ex) {
        throw new ConsistencyErrorException(ex);
      }

    }

    if (userPreferredMail != null && userPreferredMail.getValue() != null) {
      email = (String) userPreferredMail.getValue();
    }
    return email;
  }

  public PerunBl getPerunBl() {
    return this.perunBl;
  }

  private HttpUriRequest prepareDataAndGetHttpRequest(PerunSession sess, int voId, String queue, String requestor,
                                                      String subject, String text) {
    //Ticket from this part is already evidet like 'new'
    String id = "ticket/new";
    //If there is no requestor, it is uknown requestor
    if (requestor == null || requestor.isEmpty()) {
      requestor = "unknown";
    }
    //If queue is null, try to check if exist value in attribute rtVoQueue, if not, use default
    if (queue == null || queue.isEmpty()) {
      Vo vo;
      if (voId != 0) {
        try {
          vo = perunBl.getVosManagerBl().getVoById(sess, voId);
        } catch (VoNotExistsException ex) {
          throw new InternalErrorException("VoId with Id=" + voId + " not exists.", ex);
        }
        Attribute voQueue;
        try {
          voQueue =
              perunBl.getAttributesManagerBl().getAttribute(sess, vo, AttributesManager.NS_VO_ATTR_DEF + ":RTVoQueue");
        } catch (AttributeNotExistsException ex) {
          throw new InternalErrorException("Attribute RTVoQueue not exists.", ex);
        } catch (WrongAttributeAssignmentException ex) {
          throw new InternalErrorException(ex);
        }
        if (voQueue.getValue() != null) {
          queue = (String) voQueue.getValue();
        } else {
          queue = rtDefaultQueue;
        }
      } else {
        queue = rtDefaultQueue;
      }
    }
    //If subject is null or empty, use Unspecified instead
    if (subject == null || subject.isEmpty()) {
      subject = "(No subject)";
    }
    //Text can be null so if it is, put empty string
    if (text == null) {
      text = "";
    }

    //Prepare credentials
    String username = BeansUtils.getCoreConfig().getRtServiceuserUsername();
    String password = BeansUtils.getCoreConfig().getRtServiceuserPassword();

    //Prepare content of message
    MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
    try {
      entityBuilder.addPart("Content-Type",
          new StringBody("application/x-www-form-urlencoded", ContentType.create("text/plain", Consts.UTF_8)));
      entityBuilder.addPart("charset",
          new StringBody(StandardCharsets.UTF_8.toString(), ContentType.create("text/plain", Consts.UTF_8)));
      entityBuilder.addPart("Connection", new StringBody("Close", ContentType.create("text/plain", Consts.UTF_8)));
      StringBody content = new StringBody(
          "id: " + id + '\n' + "Queue: " + queue + '\n' + "Requestor: " + requestor + '\n' + "Subject: " + subject +
          '\n' + "Text: " + text, ContentType.create("text/plain", Consts.UTF_8));
      entityBuilder.addPart("content", content);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    //Test rtURL for null
    if (rtURL == null || rtURL.length() == 0) {
      throw new InternalErrorException("rtURL is not prepared and is null in the moment of posting.");
    }

    // prepare post request
    HttpPost post = new HttpPost(rtURL);
    UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);

    post.addHeader(BasicScheme.authenticate(credentials, "utf-8", false));
    post.setEntity(entityBuilder.build());

    return post;
  }

  @Override
  @Deprecated
  public RTMessage sendMessageToRT(PerunSession sess, Member meber, String queue, String subject, String text) {
    throw new InternalErrorException("This method is not supported now!");
  }

  @Override
  public RTMessage sendMessageToRT(PerunSession sess, String queue, String subject, String text) {
    return sendMessageToRT(sess, 0, queue, subject, text);
  }

  @Override
  public RTMessage sendMessageToRT(PerunSession sess, int voId, String subject, String text) {
    return sendMessageToRT(sess, voId, null, subject, text);
  }

  @Override
  public RTMessage sendMessageToRT(PerunSession sess, int voId, String queue, String subject, String text) {
    LOG.debug("Parameters of rtMessage are queue='" + queue + "', subject='{}' and text='{}'", subject, text);

    //Get Email from User who get from session
    String email;
    User user = sess.getPerunPrincipal().getUser();

    //try to get user/member email from user in session
    if (user != null) {
      email = findUserPreferredEmail(sess, user);
    } else {
      email = null;
      LOG.info("Can't get user from session.");
    }

    //try to get email from additionalInformations in session (attribute mail)
    if (email == null) {
      Matcher emailMatcher;
      Map<String, String> additionalInfo = sess.getPerunPrincipal().getAdditionalInformations();
      //If there are some data in additionalInfo
      if (additionalInfo != null) {
        String mailInfo = additionalInfo.get("mail");
        //If there is notnull attribute "mail" in map
        if (mailInfo != null) {
          //If attribute mail has separator ',' or ';'
          if (mailInfo.contains(";")) {
            String[] mailsFromInfo = mailInfo.split(";");
            for (String mail : mailsFromInfo) {
              emailMatcher = Utils.EMAIL_PATTERN.matcher(mail);
              if (emailMatcher.matches()) {
                email = mail;
                break;
              }
            }
          } else if (mailInfo.contains(",")) {
            String[] mailsFromInfo = mailInfo.split(",");
            for (String mail : mailsFromInfo) {
              emailMatcher = Utils.EMAIL_PATTERN.matcher(mail);
              if (emailMatcher.matches()) {
                email = mail;
                break;
              }
            }
          } else {
            //If there is no separator, test if this has format of email, if yes, save it to email
            emailMatcher = Utils.EMAIL_PATTERN.matcher(mailInfo);
            if (emailMatcher.matches()) {
              email = mailInfo;
            }
          }
        }
      }
    }

    if (StringUtils.isNotBlank(BeansUtils.getCoreConfig().getRtSendToMail())) {

      // redirect all RT messages to mail address
      SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
      simpleMailMessage.setSubject("[" + queue + "] " + subject);

      if (email != null) {
        simpleMailMessage.setText("Requestor: " + email + "\n\n" + text);
        simpleMailMessage.setReplyTo(email);
      } else {
        simpleMailMessage.setText("Requestor: UNKNOWN\n\n" + text);
      }
      simpleMailMessage.setFrom(BeansUtils.getCoreConfig().getMailchangeBackupFrom());
      simpleMailMessage.setTo(BeansUtils.getCoreConfig().getRtSendToMail());

      try {
        LOG.trace("Message to be sent: {}", simpleMailMessage);
        mailSender.send(simpleMailMessage);
      } catch (MailException ex) {
        LOG.error("RT message was not send to email address, due to an error.", ex);
        throw new InternalErrorException(
            "RT message was not send to email address, due to an error: " + ex.getMessage());
      }

      return new RTMessage(email, 0);

    } else {

      //Prepare sending message
      HttpResponse response;
      HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
      // just like cookie-policy: ignore cookies
      httpClientBuilder.disableCookieManagement();
      HttpClient httpClient = httpClientBuilder.build();

      StringBuilder responseMessage = new StringBuilder();
      String ticketNumber = "0";
      try {
        response = httpClient.execute(this.prepareDataAndGetHttpRequest(sess, voId, queue, email, subject, text));
        BufferedReader bw = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        //Reading response from RT
        String line;
        while ((line = bw.readLine()) != null) {
          responseMessage.append(line);
          responseMessage.append('\n');
          //Matcher for ticketNumber
          Matcher ticketNumberMatcher = this.ticketNumberPattern.matcher(line);
          if (ticketNumberMatcher.find()) {
            ticketNumber = ticketNumberMatcher.group(1);
          }
        }
      } catch (IOException ex) {
        throw new InternalErrorException("IOException has been throw while executing http request.", ex);
      }

      //Return message if response is ok, or throw exception with bad response
      int ticketNum = Integer.parseInt(ticketNumber);
      if (ticketNum != 0) {
        RTMessage rtmessage = new RTMessage(email, ticketNum);
        LOG.debug("RT message was send successfully and the ticket has number: " + ticketNum);
        return rtmessage;
      } else {
        throw new InternalErrorException(
            "RT message was not send due to error with RT returned this message: " + responseMessage.toString());
      }

    }

  }


}
