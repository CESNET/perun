package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Status;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class with main method to get some special action or write version.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 * @version $Id$
 */
public class Main {

    private PerunBl perun;
    private AbstractApplicationContext springCtx;
    private PerunSession perunSession;
    private final static Logger log = LoggerFactory.getLogger(Main.class);
    private final PerunPrincipal pp = new PerunPrincipal("main", ExtSourcesManager.EXTSOURCE_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
    
    private static BufferedWriter writer;
    
    public Main(String fileName) throws Exception{
        try {
            this.springCtx = new ClassPathXmlApplicationContext("perun-beans.xml", "perun-datasources.xml", "perun-transaction-manager.xml");
            this.perun = springCtx.getBean("perun", PerunBl.class);
            this.perunSession = perun.getPerunSession(pp);
        } catch (Exception e) {
            log.error("Application context loading error.", e);
            throw e;
        }
        this.writer = new BufferedWriter(openForFile(fileName));
        
        int LastMessageBeforeInitializingData = perun.getAuditer().getLastMessageId();
        System.err.println("Last message id before starting initializing: " + LastMessageBeforeInitializingData + '\n');
        vosLdifToWriter();
        groupsLdifToWriter();
        usersLdifToWriter();
        int LastMessageAfterInitializingData = perun.getAuditer().getLastMessageId();
        System.err.println("Last message id after initializing: " + LastMessageAfterInitializingData + '\n');
        perun.getAuditer().setLastProcessedId("ldapcConsumer", LastMessageAfterInitializingData);
    }

    public static void main(String[] args) throws Exception {
        if(args.length == 0 || args.length > 2) {
            System.out.println(badUsage(null));
            System.out.println(help());
        } else if(args[0].equals("-h") || args[0].equals("--help")) {
            System.out.println(help());
        } else if(args[0].equals("-v")) {
            System.out.println(version());
        } else if(args[0].equals("-g")) {
            Main main;
            if(args[1] != null && args[1].length() != 0) {
                main = new Main(args[1]);
            } else {
                main = new Main(null);
            }
            writer.close();
        } else {      
            System.out.println(badUsage(args[0]));
            System.out.println(help());
        }
    }
       
    private static String badUsage(String badArgument) {      
        StringBuilder sb = new StringBuilder();
        sb.append("Bad usage. Wrong argument or less than 1 or more than 2 arguments (not supported).");
        sb.append('\n');
        if(badArgument != null) {
            sb.append("Bad argument is '" + badArgument + "'.");
            sb.append('n');
        }
        return sb.toString();
    }
    
    private static String help() {
        StringBuilder sb = new StringBuilder();
        sb.append("--------------HELP-------------");
        sb.append('\n');
        sb.append("-h | --help =>  help");
        sb.append('\n');
        sb.append("-g          =>  generate ldifs (second argument can be path to the file for generating ldif)");
        sb.append('\n');
        sb.append("-v          =>  version");
        sb.append('\n');
        return sb.toString();
    }
    
    private static String version() throws IOException {
        Package p = Main.class.getPackage();
        StringBuilder sb = new StringBuilder();
        sb.append("Title: "+ p.getImplementationTitle());
        sb.append('\n');
        sb.append("Version: "+ p.getImplementationVersion());
        sb.append('\n');
        sb.append("Vendor: "+ p.getImplementationVendor());
        sb.append('\n');

        Properties pom = new Properties();
        pom.load(Main.class.getResourceAsStream("/META-INF/maven/cz.metacentrum.perun/perun-core/pom.properties"));
        
        sb.append("pom = " + pom);
        sb.append('\n');
        return sb.toString();
    }
    
    //Prepare for using file insted of stdout
    private Writer openForFile(String fileName) {
        try {
            if (fileName != null) return new PrintWriter(fileName);
            else return new OutputStreamWriter(System.out);
        } catch (FileNotFoundException ex) {
            System.out.println("# File you specify not exist and can't be created! Script will be canceled now!");
            throw new RuntimeException("File with ldif can't be created.", ex);
        }
    }

    private void vosLdifToWriter() throws Exception {
        List<Vo> vos = perun.getVosManagerBl().getVos(perunSession);

        for(Vo v: vos) {
            String dn = "dn: ";
            String desc = "description: ";
            String oc1 = "objectclass: top";
            String oc2 = "objectclass: organization";
            String oc3 = "objectclass: perunVO";
            String o = "o: ";
            String perunVoId = "perunVoId: ";
            perunVoId+= String.valueOf(v.getId());
            o+= v.getShortName();
            desc+= v.getName();
            dn+= "perunVoId=" + v.getId() + ",dc=perun,dc=cesnet,dc=cz";
            writer.write(dn + '\n');
            writer.write(oc1 + '\n');
            writer.write(oc2 + '\n');
            writer.write(oc3 + '\n');
            writer.write(o + '\n');
            writer.write(perunVoId + '\n');
            writer.write(desc + '\n');
            //Generate all members in member groups of this vo and add them here (only members with status Valid)
            List<Member> validMembers = perun.getMembersManagerBl().getMembers(perunSession, v, Status.VALID);
            for(Member m: validMembers) {
                writer.write("uniqueMember: perunUserId=" + m.getUserId() + ",ou=People,dc=perun,dc=cesnet,dc=cz" + '\n');
            }
            writer.write('\n');
        }
    }   

    private void groupsLdifToWriter() throws Exception {
        List<Vo> vos = perun.getVosManagerBl().getVos(perunSession);

        for(Vo v: vos) {
            List<Group> groups = new ArrayList<Group>();
            groups = perun.getGroupsManagerBl().getGroups(perunSession, v);
            for(Group g: groups) {
                String dn = "dn: ";
                String oc1 = "objectclass: top";
                String oc2 = "objectclass: groupOfUniqueNames";
                String oc3 = "objectclass: perunGroup";
                String cn = "cn: ";
                String perunVoId = "perunVoId: ";
                String parentGroup = "perunParentGroup: ";
                String parentGroupId = "perunParentGroupId: ";
                String perunGroupId = "perunGroupId: ";
                String owner = "owner: ";
                String description = "description: ";
                String perunUniqueGroupName = "perunUniqueGroupName: ";
                List<Member> members = new ArrayList<Member>();
                members = perun.getGroupsManagerBl().getGroupMembers(perunSession, g, Status.VALID);
                perunGroupId+= String.valueOf(g.getId());
                perunVoId+=String.valueOf(g.getVoId());
                dn+= "perunGroupId=" + g.getId() + ",perunVoId=" + g.getVoId() + ",dc=perun,dc=cesnet,dc=cz";
                cn+= g.getName();
                perunUniqueGroupName+= v.getShortName() + ":" + g.getName();
                if(g.getDescription() != null) description+= g.getDescription();
                if(g.getParentGroupId() != null) {
                    parentGroupId+= g.getParentGroupId();
                    parentGroup+= "perunGroupId=" + g.getParentGroupId()+ ",perunVoId=" + g.getVoId() + ",dc=perun,dc=cesnet,dc=cz";
                }
                List<Member> admins = new ArrayList<Member>();
                writer.write(dn + '\n');
                writer.write(oc1 + '\n');
                writer.write(oc2 + '\n');
                writer.write(oc3 + '\n');
                writer.write(cn + '\n');
                writer.write(perunUniqueGroupName + '\n');
                writer.write(perunGroupId + '\n');
                writer.write(perunVoId + '\n');
                if(g.getDescription() != null) writer.write(description + '\n');
                if(g.getParentGroupId() != null) {
                    writer.write(parentGroupId + '\n');
                    writer.write(parentGroup + '\n');
                }
                //ADD Group Members
                for(Member m: members) {
                    writer.write("uniqueMember: " + "perunUserId=" + m.getUserId() + ",ou=People,dc=perun,dc=cesnet,dc=cz");
                    writer.write('\n');
                }
                //ADD resources which group is assigned to
                List<Resource> associatedResources = perun.getResourcesManagerBl().getAssignedResources(perunSession, g);
                for(Resource r: associatedResources) {
                    writer.write("assignedToResourceId: " + r.getId());
                    writer.write('\n');
                }
                //FOR NOW No groups has owner  
                writer.write(owner + '\n');
                writer.write('\n');
            }               
        }  
    } 

    private void usersLdifToWriter() throws Exception {
        List<User> users = perun.getUsersManagerBl().getUsers(perunSession);

        for(User u: users) {
            String dn = "dn: ";
            String entryStatus = "entryStatus: active";
            String oc1 = "objectclass: top";
            String oc2 = "objectclass: person";
            String oc3 = "objectclass: organizationalPerson";
            String oc4 = "objectclass: inetOrgPerson";
            String oc5 = "objectclass: perunUser";
            String oc6 = "objectclass: tenOperEntry";
            String oc7 = "objectclass: inetUser";
            String sn = "sn: ";
            String cn = "cn: ";
            String givenName = "givenName: ";
            String perunUserId = "perunUserId: ";
            String mail = "mail: ";
            String preferredMail = "preferredMail: ";
            String o = "o: ";
            String isServiceUser = "isServiceUser: ";
            String userPassword = "userPassword: ";
            List<String> membersOf = new ArrayList<String>();
            List<Member> members = new ArrayList<Member>();
            members = perun.getMembersManagerBl().getMembersByUser(perunSession, u);
            for(Member m: members) {
                if(m.getStatus().equals(Status.VALID)) {
                    List<Group> groups = new ArrayList<Group>();
                    groups = perun.getGroupsManagerBl().getAllMemberGroups(perunSession, m);
                    for(Group g: groups) {
                        membersOf.add("memberOf: " + "perunGroupId=" + g.getId() + ",perunVoId=" + g.getVoId() + ",dc=perun,dc=cesnet,dc=cz");
                    }
                }
            }
            //Attribute attrMail = perun.getAttributesManagerBl().getAttribute(perunSession, u, AttributesManager.NS_USER_ATTR_DEF + ":mail");
            Attribute attrPreferredMail = perun.getAttributesManagerBl().getAttribute(perunSession, u, AttributesManager.NS_USER_ATTR_DEF + ":preferredMail");
            Attribute attrOrganization = perun.getAttributesManagerBl().getAttribute(perunSession, u, AttributesManager.NS_USER_ATTR_DEF + ":organization");
            Attribute attrVirtCertDNs = perun.getAttributesManagerBl().getAttribute(perunSession, u, AttributesManager.NS_USER_ATTR_VIRT + ":userCertDNs");
            perunUserId+= String.valueOf(u.getId());
            dn+= "perunUserId=" + u.getId() + ",ou=People,dc=perun,dc=cesnet,dc=cz";
            sn+= u.getLastName();
            cn+= u.getFirstName() + " " + u.getLastName();
            if(u.isServiceUser()) isServiceUser+= "1";
            else isServiceUser+= "0";
            givenName+= u.getFirstName();
            if(attrPreferredMail == null || attrPreferredMail.getValue() == null) mail = null;
            else mail+= (String) attrPreferredMail.getValue();
            if(attrPreferredMail == null || attrPreferredMail.getValue() == null) preferredMail =null;
            else preferredMail+= (String) attrPreferredMail.getValue();
            if(attrOrganization == null || attrOrganization.getValue() == null) o= null;
            else o+= (String) attrOrganization.getValue();
            Map<String, String> certDNs = null;
            Set<String> certSubjects = null;
            if(attrVirtCertDNs != null && attrVirtCertDNs.getValue() != null) {
                certDNs = (Map) attrVirtCertDNs.getValue();
                certSubjects = certDNs.keySet();
            }
            writer.write(dn + '\n');
            writer.write(oc1 + '\n');
            writer.write(oc2 + '\n');
            writer.write(oc3 + '\n');
            writer.write(oc4 + '\n');
            writer.write(oc5 + '\n');
            writer.write(oc6 + '\n');
            writer.write(oc7 + '\n');
            writer.write(entryStatus + '\n');
            writer.write(sn + '\n');
            writer.write(cn + '\n');
            writer.write(givenName + '\n');
            writer.write(perunUserId + '\n');
            writer.write(isServiceUser + '\n');
            if(mail != null) writer.write(mail + '\n');
            if(preferredMail != null) writer.write(preferredMail + '\n');
            if(o != null) writer.write(o + '\n');
            if(certSubjects != null && !certSubjects.isEmpty()) {
                for(String s: certSubjects) {
                    writer.write("userCertificateSubject: " + s + '\n');
                }
            }
            //GET ALL USERS UIDs
            List<String> similarUids = perun.getAttributesManagerBl().getAllSimilarAttributeNames(perunSession, AttributesManager.NS_USER_ATTR_DEF + ":uid-namespace:");
            if(similarUids != null && !similarUids.isEmpty()) {
                for(String s: similarUids) {
                    Attribute uidNamespace = perun.getAttributesManagerBl().getAttribute(perunSession, u, s);
                    if(uidNamespace != null && uidNamespace.getValue() != null) {
                        writer.write("uidNumber;x-ns-" + uidNamespace.getFriendlyNameParameter() + ": " + uidNamespace.getValue().toString() + '\n');
                    }
                }
            }
            //GET ALL USERS LOGINs
            List<String> similarLogins = perun.getAttributesManagerBl().getAllSimilarAttributeNames(perunSession, AttributesManager.NS_USER_ATTR_DEF + ":login-namespace:");
            if(similarLogins != null && !similarLogins.isEmpty()) {
                for(String s: similarLogins) {
                    Attribute loginNamespace = perun.getAttributesManagerBl().getAttribute(perunSession, u, s);
                    if(loginNamespace != null && loginNamespace.getValue() != null) {
                        writer.write("login;x-ns-" + loginNamespace.getFriendlyNameParameter() + ": " + loginNamespace.getValue().toString() + '\n');
                        if(loginNamespace.getFriendlyNameParameter().equals("einfra")) {
                            writer.write(userPassword + "{SASL}" + loginNamespace.getValue().toString()  + '@' + loginNamespace.getFriendlyNameParameter().toUpperCase() + '\n');
                        }
                    }
                }
            }
            //GET ALL USERS EXTlogins FOR EVERY EXTSOURCE WITH TYPE EQUALS IDP
            List<UserExtSource> userExtSources = perun.getUsersManagerBl().getUserExtSources(perunSession, u);
            List<String> extLogins = new ArrayList<String>();
            for(UserExtSource ues: userExtSources) {                  
                if(ues != null && ues.getExtSource() != null) {
                    String type = ues.getExtSource().getType();
                    if(type != null) {
                        if(type.equals(ExtSourcesManager.EXTSOURCE_IDP)) {
                            String extLogin;
                            extLogin = ues.getLogin();
                            if(extLogin == null) extLogin = "";
                            writer.write("eduPersonPrincipalNames: " + extLogin + '\n');
                        }
                    }
                }
            }
            //ADD MEMBEROF ATTRIBUTE TO WRITER
            for(String s: membersOf) {
                writer.write(s + '\n');
            }
            writer.write('\n');
        }
        
    } 
}
