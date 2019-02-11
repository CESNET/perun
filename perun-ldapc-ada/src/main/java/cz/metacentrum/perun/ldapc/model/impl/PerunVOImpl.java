package cz.metacentrum.perun.ldapc.model.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.naming.Name;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.support.LdapNameBuilder;

import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.ldapc.model.PerunAttribute;
import cz.metacentrum.perun.ldapc.model.PerunUser;
import cz.metacentrum.perun.ldapc.model.PerunVO;

public class PerunVOImpl extends AbstractPerunEntry<Vo> implements PerunVO {

	private final static Logger log = LoggerFactory.getLogger(PerunVOImpl.class);

	@Autowired
	private PerunUser user;
	
	@Override
	protected List<String> getDefaultUpdatableAttributes() {
		return Arrays.asList(PerunAttribute.PerunAttributeNames.ldapAttrDescription);
	}

	@Override
	protected List<PerunAttribute<Vo>> getDefaultAttributeDescriptions() {
		return Arrays.asList(
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrOrganization, 
						PerunAttribute.REQUIRED, 
						(PerunAttribute.SingleValueExtractor<Vo>)(vo, attrs) -> vo.getShortName()
						),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrDescription, 
						PerunAttribute.REQUIRED, 
						(PerunAttribute.SingleValueExtractor<Vo>)(vo, attrs) -> vo.getName()
						),
				new PerunAttributeDesc<>(
						PerunAttribute.PerunAttributeNames.ldapAttrPerunVoId, 
						PerunAttribute.REQUIRED, 
						(PerunAttribute.SingleValueExtractor<Vo>)(vo, attrs) -> String.valueOf(vo.getId())
						)
				);
	}

	public void addVo(Vo vo) throws InternalErrorException {
		addEntry(vo);
	}

	public void deleteVo(Vo vo) throws InternalErrorException {
		deleteEntry(vo);
	}

	@Override
	public void updateVo(Vo vo) throws InternalErrorException {
		modifyEntry(vo);
	}

	public String getVoShortName(int voId) throws InternalErrorException {
		DirContextOperations voEntry = findById(String.valueOf(voId));
		String[] voShortNameInformation = voEntry.getStringAttributes(PerunAttribute.PerunAttributeNames.ldapAttrOrganization);
		String voShortName = null;
		if(voShortNameInformation == null || voShortNameInformation[0] == null) 
			throw new InternalErrorException("There is no shortName in ldap for vo with id=" + voId);
		if(voShortNameInformation.length != 1) 
			throw new InternalErrorException("There is not exactly one short name of vo with id=" +  voId + " in ldap. Count of shortnames is " + voShortNameInformation.length);
		voShortName = voShortNameInformation[0];
		return voShortName;
	}

	@Override
	public void addMemberToVO(int voId, Member member) {
		DirContextOperations voEntry = findById(String.valueOf(voId));
		Name memberDN = user.getEntryDN(String.valueOf(member.getUserId()));
		voEntry.addAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrUniqueMember, addBaseDN(memberDN).toString());
		ldapTemplate.modifyAttributes(voEntry);
		DirContextOperations userEntry = findByDN(memberDN);
		userEntry.addAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrMemberOfPerunVo, String.valueOf(voId));
		ldapTemplate.modifyAttributes(userEntry);
	}

	@Override
	public void removeMemberFromVO(int voId, Member member) {
		DirContextOperations voEntry = findById(String.valueOf(voId));
		Name memberDN = user.getEntryDN(String.valueOf(member.getUserId()));
		voEntry.removeAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrUniqueMember, addBaseDN(memberDN).toString());
		ldapTemplate.modifyAttributes(voEntry);
		DirContextOperations userEntry = findByDN(memberDN);
		userEntry.removeAttributeValue(PerunAttribute.PerunAttributeNames.ldapAttrMemberOfPerunVo, String.valueOf(voId));
		ldapTemplate.modifyAttributes(userEntry);
	}

	@Override
	public void synchronizeMembers(Vo vo, List<Member> members) {
		DirContextOperations voEntry = findByDN(buildDN(vo));
		List<Name> memberList = new ArrayList<Name>(members.size());
		for (Member member: members) {
			memberList.add(addBaseDN(user.getEntryDN(String.valueOf(member.getUserId()))));
		}
		voEntry.setAttributeValues(PerunAttribute.PerunAttributeNames.ldapAttrUniqueMember, memberList.stream().map( name -> name.toString()).toArray(String[]::new));
		ldapTemplate.modifyAttributes(voEntry);
		// user attributes are set when synchronizing users
	}

	@Override
	protected Name buildDN(Vo bean) {
		return getEntryDN(String.valueOf(bean.getId()));
	}

	@Override
	protected void mapToContext(Vo bean, DirContextOperations context) throws InternalErrorException {
		context.setAttributeValues("objectclass", Arrays.asList(
				PerunAttribute.PerunAttributeNames.objectClassPerunVO,
				PerunAttribute.PerunAttributeNames.objectClassOrganization).toArray());
		mapToContext(bean, context, getAttributeDescriptions());
	}

	@Override
	public Name getEntryDN(String ...voId) {
		return LdapNameBuilder.newInstance()
				.add(PerunAttribute.PerunAttributeNames.ldapAttrPerunVoId, voId[0])
				.build();
	}

}
