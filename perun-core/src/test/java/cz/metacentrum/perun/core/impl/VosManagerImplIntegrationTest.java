package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.BanOnVo;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.BanNotExistsException;
import cz.metacentrum.perun.core.implApi.MembersManagerImplApi;
import cz.metacentrum.perun.core.implApi.VosManagerImplApi;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class VosManagerImplIntegrationTest extends AbstractPerunIntegrationTest {

	private final static String CLASS_NAME = "VosManagerImpl.";

	private User user;
	private Member member;
	private Member otherMember;
	private Vo vo;
	private Vo otherVo;

	private VosManagerImplApi vosManagerImpl;

	@Before
	public void setUp() throws Exception {
		MembersManagerImplApi membersManagerImplApi = (MembersManagerImplApi) ReflectionTestUtils.getField(
				perun.getMembersManagerBl(), "membersManagerImpl");
		if (membersManagerImplApi == null) {
			throw new RuntimeException("Failed to get membersManagerImpl");
		}

		vosManagerImpl = (VosManagerImplApi) ReflectionTestUtils.getField(perun.getVosManagerBl(), "vosManagerImpl");
		if (vosManagerImpl == null) {
			throw new RuntimeException("Failed to get vosManagerImpl");
		}

		user = new User(-1, "John", "Doe", "", "", "");
		user = perun.getUsersManagerBl().createUser(sess, user);

		vo = new Vo(-1, "Vo", "vo");
		vo = perun.getVosManagerBl().createVo(sess, vo);

		member = membersManagerImplApi.createMember(sess, vo, user);

		otherVo = new Vo(-1, "Other vo", "othervo");
		otherVo = perun.getVosManagerBl().createVo(sess, otherVo);

		otherMember = membersManagerImplApi.createMember(sess, otherVo, user);
	}

	@Test
	public void setBan() throws Exception {
		System.out.println(CLASS_NAME + "setBan");

		BanOnVo originBan = new BanOnVo(-1, member.getId(), vo.getId(), new Date(), "noob");
		originBan = vosManagerImpl.setBan(sess, originBan);

		BanOnVo actualBan = vosManagerImpl.getBanForMember(sess, originBan.getMemberId());

		assertThat(originBan).isEqualTo(actualBan);
	}

	@Test
	public void getBanById() throws Exception {
		System.out.println(CLASS_NAME + "getBanById");

		BanOnVo originBan = new BanOnVo(-1, member.getId(), vo.getId(), new Date(), "noob");
		originBan = vosManagerImpl.setBan(sess, originBan);

		BanOnVo actualBan = vosManagerImpl.getBanById(sess, originBan.getId());

		isValidBan(actualBan, originBan.getId(), originBan.getMemberId(), originBan.getVoId(),
				originBan.getValidityTo(), originBan.getDescription());
	}

	@Test
	public void getBanForMember() throws Exception {
		System.out.println(CLASS_NAME + "getBanForMember");

		BanOnVo originBan = new BanOnVo(-1, member.getId(), vo.getId(), new Date(), "noob");
		originBan = vosManagerImpl.setBan(sess, originBan);

		BanOnVo actualBan = vosManagerImpl.getBanById(sess, originBan.getId());

		isValidBan(actualBan, originBan.getId(), originBan.getMemberId(), originBan.getVoId(),
				originBan.getValidityTo(), originBan.getDescription());
	}

	@Test
	public void getBansForVo() {
		System.out.println(CLASS_NAME + "getBansForVo");

		BanOnVo originBan = new BanOnVo(-1, member.getId(), vo.getId(), new Date(), "noob");
		originBan = vosManagerImpl.setBan(sess, originBan);

		BanOnVo otherBan = new BanOnVo(-1, otherMember.getId(), otherVo.getId(), new Date(), "noob");
		vosManagerImpl.setBan(sess, otherBan);

		List<BanOnVo> voBans = vosManagerImpl.getBansForVo(sess, vo.getId());

		assertThat(voBans).containsOnly(originBan);
	}

	@Test
	public void updateBanDescription() throws Exception {
		System.out.println(CLASS_NAME + "updateBanDescription");

		testUpdateBan(ban -> ban.setDescription("Updated Description"));
	}

	@Test
	public void updateBanValidity() throws Exception {
		System.out.println(CLASS_NAME + "updateBanValidity");

		testUpdateBan(ban -> ban.setValidityTo(new Date(1434343L)));
	}

	@Test
	public void removeBan() throws Exception {
		System.out.println(CLASS_NAME + "removeBan");

		BanOnVo originBan = new BanOnVo(-1, member.getId(), vo.getId(), new Date(), "noob");
		vosManagerImpl.setBan(sess, originBan);

		vosManagerImpl.removeBan(sess, originBan.getId());

		assertThatExceptionOfType(BanNotExistsException.class)
				.isThrownBy(() -> vosManagerImpl.getBanById(sess, originBan.getId()));
	}

	private void testUpdateBan(Consumer<BanOnVo> banChange) throws Exception {
		BanOnVo originBan = new BanOnVo(-1, member.getId(), vo.getId(), new Date(), "noob");
		originBan = vosManagerImpl.setBan(sess, originBan);
		originBan = vosManagerImpl.getBanById(sess, originBan.getId());

		banChange.accept(originBan);

		vosManagerImpl.updateBan(sess, originBan);

		BanOnVo updatedBan = vosManagerImpl.getBanById(sess, originBan.getId());

		assertThat(updatedBan).isEqualByComparingTo(originBan);
	}

	private void isValidBan(BanOnVo ban, int banId, int memberId, int voId, Date validity, String description) {
		assertThat(ban.getId()).isEqualTo(banId);
		assertThat(ban.getMemberId()).isEqualTo(memberId);
		assertThat(ban.getVoId()).isEqualTo(voId);
		assertThat(ban.getValidityTo()).isEqualTo(validity);
		assertThat(ban.getDescription()).isEqualTo(description);

		assertThat(ban.getCreatedAt()).isNotNull();
		assertThat(ban.getCreatedBy()).isNotNull();
		assertThat(ban.getCreatedByUid()).isNotNull();
		assertThat(ban.getModifiedAt()).isNotNull();
		assertThat(ban.getModifiedBy()).isNotNull();
		assertThat(ban.getModifiedByUid()).isNotNull();
	}
}
