package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.VosManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;

public class urn_perun_member_attribute_def_def_memberOrganizationsHistoryTest {
  private urn_perun_member_attribute_def_def_memberOrganizationsHistory classInstance;
  private Attribute attributeToCheck;
  private PerunSessionImpl session;
  private Member member;
  private Vo vo;

  private ArrayList<String> memberOrganizationsHistory;

  @Before
  public void setUp() throws Exception {
    classInstance = new urn_perun_member_attribute_def_def_memberOrganizationsHistory();
    attributeToCheck = new Attribute(classInstance.getAttributeDefinition());

    session = mock(PerunSessionImpl.class);
    member = mock(Member.class);
    vo = new Vo(0, "Test VO", "test");
    memberOrganizationsHistory = new ArrayList<>() {{
      add(vo.getShortName());
    }};

    PerunBl perunBl = mock(PerunBl.class);
    when(session.getPerunBl()).thenReturn(perunBl);
    VosManagerBl vosManagerBl = mock(VosManagerBl.class);
    when(perunBl.getVosManagerBl()).thenReturn(vosManagerBl);
  }

  @Test
  public void testSemanticsWithCorrectValue() throws Exception {
    System.out.println("testSemanticsWithCorrectValue()");
    attributeToCheck.setValue(memberOrganizationsHistory);
    when(session.getPerunBl().getVosManagerBl().getVoByShortName(session, vo.getShortName())).thenReturn(vo);
    classInstance.checkAttributeSemantics(session, member, attributeToCheck);
  }

  @Test(expected = WrongReferenceAttributeValueException.class)
  public void testSemanticsWithNullValue() throws Exception {
    System.out.println("testSemanticsWithNullValue()");
    attributeToCheck.setValue(null);
    classInstance.checkAttributeSemantics(session, member, attributeToCheck);
  }
}
