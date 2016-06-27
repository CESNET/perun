package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Kristyna Kysela
 */

public class urn_perun_member_group_attribute_def_virt_isGroupAdminTest  extends AbstractPerunIntegrationTest {


    private static urn_perun_member_group_attribute_def_virt_isGroupAdmin classInstance;
    private PerunSessionImpl session;
    private User user;
    private Member member;
    private Group group;
    private Attribute attribute;
    private AttributeDefinition attrDef;
    private Vo vo;
    private UsersManager usersManager;
    private VosManager vosManager;
    private MembersManager membersManager;
    private GroupsManager groupsManager;

    @Before
    public void setUp() throws Exception {
        classInstance = new urn_perun_member_group_attribute_def_virt_isGroupAdmin();
        usersManager = perun.getUsersManager();
        vosManager = perun.getVosManager();
        membersManager = perun.getMembersManager();
        groupsManager = perun.getGroupsManager();
        final PerunPrincipal pp = new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
        this.session = new PerunSessionImpl(perun, pp, new PerunClient());
        this.attrDef = classInstance.getAttributeDefinition();
        this.attribute = new Attribute(attrDef);
        this.user = setUpUser();
        this.vo = setUpVo();
        this.member = setUpMember(vo);
        this.group = setUpGroup(vo, member);

    }

    private User setUpUser() throws InternalErrorException, PrivilegeException {
        User newUser = new User();
        newUser.setFirstName("adam");
        newUser.setMiddleName("");
        newUser.setLastName("novak");
        newUser.setTitleBefore("");
        newUser.setTitleAfter("");
        newUser = usersManager.createUser(sess, newUser);
        assertNotNull(newUser);
        return newUser;
    }


    private Vo setUpVo() throws Exception {
        Vo newVo = new Vo(0, "TestVo", "TestVo");
        Vo returnedVo = vosManager.createVo(sess, newVo);
        assertNotNull(returnedVo);
        assertEquals(newVo,returnedVo);
        return returnedVo;

    }

    private Member setUpMember(Vo vo) throws Exception {

        Member newMember = membersManager.createMember(sess, vo, user);
        assertNotNull(newMember);
        return newMember;

    }

    private Group setUpGroup(Vo vo, Member member) throws Exception {

        Group newGroup = new Group("TestGroup","");
        newGroup = groupsManager.createGroup(sess, vo, newGroup);
        User user = usersManager.getUserByMember(sess, member);
        groupsManager.addMember(sess, newGroup, member);
        groupsManager.addAdmin(sess, newGroup, user);
        return newGroup;

    }

    @Test
    public void testGetAttributeValueGroupAdmin() throws Exception {
        System.out.println("testGetAttributeValue() - isGroupAdmin");
        attribute.setValue(true);
        Attribute testAttr = classInstance.getAttributeValue(session, member, group, attrDef);
        assertEquals(testAttr, attribute);
    }

    @Test
    public void testGetAttributeValueNotGroupAdmin() throws Exception{
        System.out.println("testGetAttributeValue() - isNotGroupAdmin");
        attribute.setValue(false);
        groupsManager.removeAdmin(sess, group, user);
        Attribute testAttr = classInstance.getAttributeValue(session, member, group, attrDef);
        assertEquals(testAttr, attribute);
    }



}
