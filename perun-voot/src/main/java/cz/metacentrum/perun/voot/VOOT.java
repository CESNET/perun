/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.metacentrum.perun.voot;

import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UsersManager;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JavaDoc - TODO 
 * 
 * @author Martin Malik <374128@mail.muni.cz>
 */
public class VOOT {
    
    private Matcher matcher;
    private Pattern pattern;
    
    private Perun perun;
    private PerunSession session;
    private PerunPrincipal perunPrincipal;
    private User user;
    
    private int count=0;
    
    private static final String GET_PERSON_PATTERN = "(?i)people/@me(|/)"; 
    private static final String GET_GROUP_MEMBERS_PATTERN = "(?i)people/@me/[A-Za-z0-9_-]+(|/)";  
    private static final String ALL_GROUPS_PATTERN = "(?i)groups(|/)";
    private static final String IS_MEMBER_OF_PATTERN = "(?i)groups/@me(|/)";
    
    public void process(PerunSession session, String path, String[] parameters) throws InternalErrorException, MemberNotExistsException, PrivilegeException, GroupNotExistsException, VoNotExistsException{

        Perun perun = session.getPerun();
        PerunPrincipal perunPrincipal = session.getPerunPrincipal();
        User user = session.getPerunPrincipal().getUser();
         
        if(parsePath(path, GET_PERSON_PATTERN)){
       
            VootPerson vootPerson = new VootPerson(user);
            GetPersonResponse response = new GetPersonResponse();
            response.setEntry(vootPerson);
            response.setPage();
            return;
        }
        
        if(parsePath(path, GET_GROUP_MEMBERS_PATTERN)){
            
            String[] pathArray = path.split("(?i)people/@me/");
            String groupId = pathArray[1];      
            groupId.replace('/',' ');
            
            List<Member> members = getGroupMembers(groupId);
            
            GetMembersResponse response = new GetMembersResponse();
            VootPerson[] vootPersons = new VootPerson[members.size()];
            
            Iterator it=members.iterator();
            
            UsersManager usersManager = perun.getUsersManager();
            
            int i=0;
            while(it.hasNext()){
                vootPersons[i] = new VootPerson(usersManager.getUserByMember(session,(Member) it.next()));
                i++;
            }
            
            response.setEntry(vootPersons);
            
            if (count<1) count=vootPersons.length;
            response.setPage(count); 
            
            return;
        }
        
        if(parsePath(path, ALL_GROUPS_PATTERN)){
            return;
        }
        
        if(parsePath(path, IS_MEMBER_OF_PATTERN)){
            
            List<Group> groups = isMemberOf(user.getId());
            Iterator it = groups.iterator();
            
            VootGroup[] vootGroups = new VootGroup[groups.size()];
            
            int i=0;
            while(it.hasNext()){
                vootGroups[i] = new VootGroup ((Group)it.next());
                i++;
            }
            
            IsMemberOfResponse response = new IsMemberOfResponse();
            response.setEntry(vootGroups);
            response.setPage(vootGroups.length);
                    
            return;
        }
            
    }
    
    private boolean parsePath(String path, String patternString){
        matcher = null;
        pattern = null;
        pattern.compile(patternString);
        matcher = pattern.matcher(path);
        return matcher.matches();
    }
    
    private List<Member> getGroupMembers(String groupId) throws InternalErrorException, PrivilegeException, GroupNotExistsException, VoNotExistsException{
        Group group = null;
        group = perun.getGroupsManager().getGroupById(session, Integer.valueOf(groupId));
        return perun.getGroupsManager().getGroupMembers(session, group);
    }
    
    private List<Group> isMemberOf(int perunId) throws InternalErrorException, InternalErrorException, PrivilegeException, MemberNotExistsException{
        Member member =null;
        member = perun.getMembersManager().getMemberById(session, perunId);
        return perun.getGroupsManager().getAllMemberGroups(session, member);
    }
}