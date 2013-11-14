package cz.metacentrum.perun.core.blImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.LoggerFactory;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
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
import cz.metacentrum.perun.core.impl.PerunAuthenticatorImpl;
import cz.metacentrum.perun.core.impl.Utils;

/**
 * RTMessage manager can create a new message and send it to RT like predefined service user.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 * @version $Id$
 */
public class RTMessagesManagerBlImpl implements RTMessagesManagerBl{  
    private String rtURL;   
    private PerunBl perunBl;
    private final static org.slf4j.Logger log = LoggerFactory.getLogger(RTMessagesManagerBlImpl.class); 
    private final String defaultQueue = "perunv3";
    
    public RTMessagesManagerBlImpl(PerunBl perunBl) throws InternalErrorException {
        this();
        this.perunBl = perunBl;
    }
    
    public RTMessagesManagerBlImpl() throws InternalErrorException {
        rtURL = Utils.getPropertyFromConfiguration("perun.rt.url");
        try {
            PerunAuthenticatorImpl.getPerunAuthenticator().registerAuthenticationForURL(new URL(rtURL), new RTAuthenticator().getPasswordAuthentication());
        } catch(MalformedURLException ex) {
            log.error("Wrong RT URL in configuration file: {}. {}", rtURL, ex);
            log.error("Create authenticator for RT system failed. Perun won't be able to authenticate itself againt RT"); 
        }
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
        boolean sendSuccesfully = false;
        HttpURLConnection conn = null;
        try {
            //Get Email from User who get from session
            String email = null;
            User user = sess.getPerunPrincipal().getUser(); 
            if(user != null) email = findUserPreferredEmail(sess, user);
            
            //Enable cookies
            CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

            //Create new RT URL
            URL url;
            try {
                url = new URL(rtURL);
            } catch (MalformedURLException ex) {
                throw new InternalErrorException("Cannot create URL of RT.", ex);
            }

            //Create URL connection to RT
            try {
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset='utf-8'");
                // Try Keep-Alive
                conn.setRequestProperty("Connection", "Close");
                // We will send output
                conn.setDoOutput(true);
                // Using POST
                conn.setRequestMethod("POST");
                log.debug("Connection opened");
            } catch (IOException e) {
                throw new InternalErrorException("Cannot open connection to URL of RT."); 
            } 

            //Create all required and norequred parameters
            //TODO maybe other behaviour for nonget queue or subject
            Map<String, String> params = new HashMap<String, String>();
            if (email != null) params.put("Requestor", email);
            else params.put("Requestor", "unknown");
            if (text != null) {
                //At REST parser, there is every new line with space count like next line for previous key
                text = text.replace("\n", "\n ");
                params.put("Text", text);
            }
            if (subject != null) params.put("Subject", subject);
            else params.put("Subject", "Request without specific subject"); 
            
            //If queue is null, try to check if exist value in attribute rtVoQueue, if not, use default
            if(queue != null) params.put("Queue", queue);
            else {
                Vo vo = null;
                if(voId != 0) {  
                    try {
                        vo = perunBl.getVosManagerBl().getVoById(sess, voId);
                    } catch (VoNotExistsException ex) {
                        throw new InternalErrorException(ex);
                    }
                    Attribute voQueue = null;
                    try {
                        voQueue = perunBl.getAttributesManagerBl().getAttribute(sess, vo, AttributesManager.NS_VO_ATTR_DEF + ":RTVoQueue");
                    } catch (AttributeNotExistsException ex) {
                        throw new InternalErrorException(ex);
                    } catch (WrongAttributeAssignmentException ex) {
                        throw new InternalErrorException(ex);
                    }
                    if(voQueue.getValue() != null) {
                        params.put("Queue", (String) voQueue.getValue());
                    } else params.put("Queue", defaultQueue);
                } else params.put("Queue", defaultQueue);
            }
            
            params.put("id", "ticket/new");
            
            log.debug("Content of params in map for Queue is '{}'", params.get("Queue"));
            
            //Send parameters to RT server
            this.sendParametersToRTServer(conn, params);

            //Get answer from server
            InputStream rpcServerAnswer = null;
            try {
                rpcServerAnswer = conn.getInputStream();
            } catch (IOException ex) {
                try {
                    PerunAuthenticatorImpl.getPerunAuthenticator().unRegisterAuthenticationForURL(new URL(rtURL));
                    PerunAuthenticatorImpl.getPerunAuthenticator().registerAuthenticationForURL(new URL(rtURL), new RTAuthenticator().getPasswordAuthentication());
                } catch(MalformedURLException e) {
                    log.error("Malformed URL when unregistering and registering Authenticator for url {}", rtURL, e); 
                }
                throw new InternalErrorException("Answer from RT server failed.", ex);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(rpcServerAnswer));       
            String line;
            String sendingInformation = "";
            String answerLine = null;
            try {
                while((line = reader.readLine()) != null) {
                    if(!line.isEmpty()) {
                        sendingInformation+= line + '\n';
                        if(line.charAt(0)=='#') answerLine=line;
                    }
                }
            } catch (IOException ex) {
                throw new InternalErrorException("Reading from answer of RT server failed.", ex);
            }
            conn.disconnect();

            //Code for parsing answer to get information about send, or not send and some reason   
            //TODO maybe needs to repair
            if(!(sendingInformation.contains("# Ticket") && sendingInformation.contains("created."))) {
                throw new InternalErrorException(sendingInformation);                  
            }
            Integer ticketNumber = 0;
            for(String str : answerLine.split(" ")) {
                if(str.matches("^[0-9]+$")) {
                    ticketNumber=Integer.valueOf(str);
                    break;
                }
            }
            if(ticketNumber == 0) {
                throw new InternalErrorException("Ticket was created but number was not parsed correctly.");
            }
            RTMessage rtmessage = new RTMessage(email, ticketNumber);
            sendSuccesfully = true;
            return rtmessage;
        } finally {
            if(conn != null) conn.disconnect();
            if(!sendSuccesfully) log.error("Failed to send message to RT. Queue=" + queue + ", Text={}, User={}", text, sess.getPerunPrincipal().getUser());
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
    
    protected void sendParametersToRTServer(HttpURLConnection conn, Map<String, String> params) throws InternalErrorException {
      StringBuilder contentAll = new StringBuilder();
      StringBuilder content = new StringBuilder();
      //Create POST variable content and give String content to it
      contentAll.append("content=");
  
      for(Entry<String, String> entry : params.entrySet()) {
          content.append(entry.getKey());
          content.append(':');
          content.append(' ');
          content.append(entry.getValue());
          content.append('\n');
      }
      try {
        contentAll.append(URLEncoder.encode(content.toString(), "UTF-8"));
      }catch (UnsupportedEncodingException ex) {
          throw new InternalError("Error at endocing content to UTF-8: " + ex);
      }
      
      //Get OutputStream
      OutputStream out = null;
      try {
          out = conn.getOutputStream();
      } catch (IOException ex) {
          throw new InternalErrorException("Output data streming failed.", ex);
      }
      
      // Logging the sent parameters
      log.debug("Sending message to RT: \n {}",contentAll.toString());
      
      //Write data through DataOutputStream using OutputStream and flush them like content to RT
      OutputStreamWriter writer = new OutputStreamWriter(out);  
      try {
          writer.write(contentAll.toString());
          writer.flush();
      } catch (IOException ex) {
            throw new InternalErrorException("Data streaming failed.", ex);
      } finally {
            try {
                writer.close();
            } catch (IOException ex) {
                throw new InternalErrorException("Data streaming close failed.", ex);
            }
        }  
    }
    
    static class RTAuthenticator extends Authenticator {
      
        public PasswordAuthentication getPasswordAuthentication() {
          
            String username = null;
            String password = null;
            
            try {
                username = Utils.getPropertyFromConfiguration("perun.rt.serviceuser.username");
                password = Utils.getPropertyFromConfiguration("perun.rt.serviceuser.password");
            } catch (InternalErrorException ex) {
                log.error("Cannot load credentials of service user for the RT from the configuration file");
            }
            return new PasswordAuthentication(username, password.toCharArray());
        }
    }
    
    public PerunBl getPerunBl() {
        return this.perunBl;
    }
    
    public void setPerunBl(PerunBl perunBl) {
        this.perunBl = perunBl;
    }
}
