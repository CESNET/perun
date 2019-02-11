package cz.metacentrum.perun.ldapc.model;

import java.util.List;

import javax.naming.Name;
import javax.naming.directory.ModificationItem;

import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;

public interface PerunVO extends PerunEntry<Vo> {

	/**
	 * Create vo in LDAP.
	 *
	 * @param vo the vo
	 * @throws InternalErrorException if NameNotFoundException is thrown
	 */
	public void addVo(Vo vo) throws InternalErrorException;

	/**
	 * Delete existing vo in LDAP.
	 *
	 * @param vo the vo
	 * @throws InternalErrorException if NameNotFoundException is thrown
	 */
	public void deleteVo(Vo vo) throws InternalErrorException;

	public void updateVo(Vo vo) throws InternalErrorException; 
	
	/**
	 * Find Vo in LDAP and return shortName of this Vo.
	 *
	 * @param voId vo id
	 *
	 * @return shortName of vo with vo id
	 * @throws InternalErrorException if shortName has not right format (null, not exists, 0 length, more than 1 shortName exist)
	 */
	public String getVoShortName(int voId) throws InternalErrorException;


	public void addMemberToVO(int voId, Member member);

	public void removeMemberFromVO(int voId, Member member);

	public void synchronizeMembers(Vo vo, List<Member> members);

}
