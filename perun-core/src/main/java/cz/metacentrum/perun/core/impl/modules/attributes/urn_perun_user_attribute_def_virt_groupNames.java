package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains all group names of the user
 *
 * @author Ondrej Velisek <ondrejvelisek@gmail.com>
 */
public class urn_perun_user_attribute_def_virt_groupNames extends UserVirtualAttributesModuleAbstract implements UserVirtualAttributesModuleImplApi {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_virt_groupNames.class);
	private static final String FRIENDLY_NAME = "groupNames";

	private Pattern memberAddedToPattern = Pattern.compile("Member:\\[(.|\\s)*\\] added to Group:\\[(.|\\s)*\\]", Pattern.MULTILINE);
	private Pattern memberTotallyRemovedFromPattern = Pattern.compile("Member:\\[(.|\\s)*\\] was removed from Group:\\[(.|\\s)*\\] totally", Pattern.MULTILINE);


	@Override
	public Attribute getAttributeValue(PerunSessionImpl sess, User user, AttributeDefinition attributeDefinition) throws InternalErrorException {
		Attribute attribute = new Attribute(attributeDefinition);
		List<String> groupNames = new ArrayList<>();

		List<Member> members = sess.getPerunBl().getMembersManagerBl().getMembersByUser(sess, user);
		Set<String> voNames = new HashSet<>();
		for (Member member : members) {
			Vo vo = sess.getPerunBl().getMembersManagerBl().getMemberVo(sess, member);
			voNames.add(vo.getShortName());

			List<Group> groups = sess.getPerunBl().getGroupsManagerBl().getMemberGroups(sess, member);
			for (Group group : groups) {
				groupNames.add(vo.getShortName() +":"+ group.getName());
			}
		}
		groupNames.addAll(voNames);

		attribute.setValue(groupNames);
		return attribute;
	}

	@Override
	public List<String> resolveVirtualAttributeValueChange(PerunSessionImpl sess, String message) throws InternalErrorException, AttributeNotExistsException, WrongAttributeAssignmentException {
		List<String> resolvingMessages = new ArrayList<>();
		if (message == null) return resolvingMessages;

		Matcher memberAddedToMatcher = memberAddedToPattern.matcher(message);
		Matcher memberTotallyRemovedFromMatcher = memberTotallyRemovedFromPattern.matcher(message);

		User user;
		Attribute attribute;

		if (memberAddedToMatcher.find() || memberTotallyRemovedFromMatcher.find()) {

			user = sess.getPerunBl().getModulesUtilsBl().getUserFromMessage(sess, message);
			if (user != null) {
				String messageAttributeSet;

				attribute = sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, AttributesManager.NS_USER_ATTR_VIRT + ":" + FRIENDLY_NAME);
				List<String> value = attribute.valueAsList();

				if (value == null || value.isEmpty()) {
					AttributeDefinition attributeDefinition = new AttributeDefinition(attribute);
					messageAttributeSet = attributeDefinition.serializeToString() + " removed for " + user.serializeToString() + ".";
				} else {
					messageAttributeSet = attribute.serializeToString() + " set for " + user.serializeToString() + ".";
				}
				resolvingMessages.add(messageAttributeSet);
			} else {
				log.error("Failed to get user from message: {}", message);
			}
		}
		return resolvingMessages;
	}

	@Override
	public AttributeDefinition getAttributeDefinition() {
		AttributeDefinition attr = new AttributeDefinition();
		attr.setNamespace(AttributesManager.NS_USER_ATTR_VIRT);
		attr.setFriendlyName("groupNames");
		attr.setDisplayName("Group names");
		attr.setType(ArrayList.class.getName());
		attr.setDescription("Full names of groups which the user is a member.");
		return attr;
	}

}
