package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Testing class for login-namespace elixir-persistent attribute
 *
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class urn_perun_user_attribute_def_virt_groupNamesTest {

    private static urn_perun_user_attribute_def_virt_groupNames classInstance;
    private static PerunSessionImpl session;
    private static User user;
    private static Vo vo1;
    private static Vo vo2;
    private static Member member1;
    private static Member member2;
    private static Group group1;
    private static Group group2;

    @Before
    public void SetUp() throws AttributeNotExistsException, InternalErrorException, WrongAttributeAssignmentException {
        classInstance = new urn_perun_user_attribute_def_virt_groupNames();
        session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
        user = new User();
        user.setId(1);
        member1 = new Member();
        member1.setId(1);
        member2 = new Member();
        member2.setId(2);
        vo1 = new Vo();
        vo1.setId(1);
        vo1.setShortName("testVo1");
        vo2 = new Vo();
        vo2.setId(2);
        vo2.setShortName("testVo2");
        group1 = new Group();
        group1.setId(1);
        group1.setName("testGroup1");
        group2 = new Group();
        group2.setId(2);
        group2.setName("testGroup2");
    }

    @Test
    public void getUserGroupNamesAttributeValue() throws Exception {
        System.out.println("getUserGroupNamesAttributeValue()");

        when(session.getPerunBl().getMembersManagerBl().getMembersByUser(session, user)).thenReturn(
                Arrays.asList(member1, member2)
        );

        when(session.getPerunBl().getGroupsManagerBl().getMemberGroups(session, member1)).thenReturn(
                Arrays.asList(group1)
        );
        when(session.getPerunBl().getGroupsManagerBl().getMemberGroups(session, member2)).thenReturn(
                Arrays.asList(group2)
        );

        when(session.getPerunBl().getMembersManagerBl().getMemberVo(session, member1)).thenReturn(
                vo1
        );
        when(session.getPerunBl().getMembersManagerBl().getMemberVo(session, member2)).thenReturn(
                vo2
        );

        Attribute receivedAttr = classInstance.getAttributeValue(session, user, classInstance.getAttributeDefinition());
        assertTrue(receivedAttr.getValue() instanceof List);
        List<String> receivedValue = (List<String>) receivedAttr.getValue();

        List<String> expected = Arrays.asList(vo1.getShortName()+":"+group1.getName(), vo2.getShortName()+":"+group2.getName());
        assertEquals(new HashSet<>(expected), new HashSet<>(receivedValue));
    }

}
