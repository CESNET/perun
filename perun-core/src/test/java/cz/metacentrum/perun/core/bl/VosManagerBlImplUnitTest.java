package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.MemberCandidate;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.RichUser;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.blImpl.VosManagerBlImpl;
import cz.metacentrum.perun.core.implApi.VosManagerImplApi;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class VosManagerBlImplUnitTest {
  private VosManagerBlImpl vosManagerBl;
  private VosManagerImplApi vosManagerImpl = mock(VosManagerImplApi.class);
  private PerunSession sess = mock(PerunSession.class);
  private Candidate candidate1 = mock(Candidate.class);
  private Candidate candidate2 = mock(Candidate.class);
  private Vo vo = mock(Vo.class);
  private Group group = mock(Group.class);
  private VosManagerBlImpl vosManagerBlSpy;

  @Before
  public void setUp() {
    vosManagerBl = new VosManagerBlImpl(vosManagerImpl);
    vosManagerBl.setPerunBl(mock(PerunBl.class, RETURNS_DEEP_STUBS));
    vosManagerBlSpy = spy(vosManagerBl);
  }

  @Test
  public void createMemberCandidatesReturnsOnlyOneMemberCandidateForDuplicateUsers() throws Exception {
    User user = new User();
    RichUser richUser = new RichUser();

    when(vosManagerBlSpy.getPerunBl().getUsersManagerBl().getUserByUserExtSources(any(), any()))
        .thenReturn(user);
    when(candidate1.getUserExtSources()).
        thenReturn(Collections.emptyList());
    when(candidate2.getUserExtSources()).
        thenReturn(Collections.emptyList());
    when(vosManagerBlSpy.getPerunBl().getUsersManagerBl()
        .convertUserToRichUserWithAttributesByNames(any(), any(), any()))
        .thenReturn(richUser);

    List<MemberCandidate> memberCandidates = vosManagerBlSpy.createMemberCandidates(sess, Collections.emptyList(),
        vo, group, Arrays.asList(candidate1, candidate2), Collections.emptyList());
    assertThat(memberCandidates).hasSize(1);
  }

  @Test
  public void createMemberCandidatesReturnsCandidate() throws Exception {
    when(vosManagerBlSpy.getPerunBl().getUsersManagerBl().getUserByUserExtSources(any(), any()))
        .thenThrow(UserNotExistsException.class);
    when(candidate1.getUserExtSources())
        .thenReturn(Collections.emptyList());

    List<MemberCandidate> memberCandidates = vosManagerBlSpy.createMemberCandidates(sess, Collections.emptyList(),
        vo, group, Arrays.asList(candidate1), Collections.emptyList());
    assertThat(memberCandidates).hasSize(1);
    assertThat(memberCandidates.get(0).getCandidate()).isEqualTo(candidate1);
    assertThat(memberCandidates.get(0).getRichUser()).isNull();
  }

  @Test
  public void createMemberCandidatesReturnsUser() throws Exception {
    User user = new User();
    RichUser richUser = new RichUser();

    when(vosManagerBlSpy.getPerunBl().getUsersManagerBl().getUserByUserExtSources(any(), any()))
        .thenReturn(user);
    when(candidate1.getUserExtSources()).
        thenReturn(Collections.emptyList());
    when(vosManagerBlSpy.getPerunBl().getUsersManagerBl()
        .convertUserToRichUserWithAttributesByNames(any(), any(), any()))
        .thenReturn(richUser);

    List<MemberCandidate> memberCandidates = vosManagerBlSpy.createMemberCandidates(sess, Collections.emptyList(),
        vo, group, Arrays.asList(candidate1), Collections.emptyList());
    assertThat(memberCandidates).hasSize(1);
    assertThat(memberCandidates.get(0).getRichUser()).isEqualTo(candidate1);
    assertThat(memberCandidates.get(0).getCandidate()).isNull();
  }
}
