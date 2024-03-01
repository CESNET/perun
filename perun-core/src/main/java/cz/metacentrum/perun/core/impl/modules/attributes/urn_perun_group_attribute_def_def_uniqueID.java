package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.GroupAttributesModuleImplApi;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class for checking and filling unique IDs for groups in the Perun.
 * This unique ID is unpredictable and is usable for identifying of groups in other systems.
 *
 * @author Michal Stava stavamichal@gmail.com
 */
public class urn_perun_group_attribute_def_def_uniqueID extends GroupAttributesModuleAbstract
    implements GroupAttributesModuleImplApi {

  @Override
  public Attribute fillAttribute(PerunSessionImpl session, Group group, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException {
    Attribute filledAttribute = new Attribute(attribute);
    filledAttribute.setValue(sha1HashCount(group).toString());
    return filledAttribute;
  }

  /**
   * Generate unique ID as hexadecimal string representation of SHA1 digest from group ID.
   * Input is salted per Perun instance. Effective resulting string consist of [0-9a-f] characters.
   * <p>
   * It is used to generate unique IDs for Perun groups.
   *
   * @param group Group to generate ID for
   * @return Builder to get string ID
   * @throws InternalErrorException When generation fails
   */
  protected StringBuilder sha1HashCount(Group group) {
    try {
      String salt = BeansUtils.getCoreConfig().getInstanceId();
      MessageDigest mDigest = MessageDigest.getInstance("SHA1");
      // counts sha1hash and converts output to hex
      int length = 4 + salt.getBytes(StandardCharsets.UTF_8).length;
      byte[] result = mDigest.digest(ByteBuffer
          .allocate(length)
          .putInt(group.getId())
          .put(salt.getBytes(StandardCharsets.UTF_8))
          .array());
      StringBuilder sb = new StringBuilder();
      for (byte b : result) {
        sb.append(Integer
            .toString((b & 0xff) + 0x100, 16)
            .substring(1));
      }

      return sb;
    } catch (NoSuchAlgorithmException ex) {
      throw new InternalErrorException("Algorithm for sha1hash was not found.", ex);
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_GROUP_ATTR_DEF);
    attr.setFriendlyName("uniqueID");
    attr.setDisplayName("Unique ID");
    attr.setType(String.class.getName());
    attr.setDescription("Generated, unpredictable and unique ID of Group in Perun.");
    attr.setUnique(true);
    return attr;
  }
}
