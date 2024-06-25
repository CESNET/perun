package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleAbstract;
import cz.metacentrum.perun.core.implApi.modules.attributes.ResourceAttributesModuleImplApi;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Contains map of scratch dirs with their Unix permissions for the `fs_scratchdirs` service.
 *
 * @author David Flor
 */
public class urn_perun_resource_attribute_def_def_fsScratchDirs extends ResourceAttributesModuleAbstract implements
    ResourceAttributesModuleImplApi {
  private static final Pattern mountPointPattern = Pattern.compile("^/[-a-zA-Z.0-9_/]*$");
  private static final Pattern permissionsPattern = Pattern.compile("^[01234567]?[01234567]{3}$");

  @Override
  public void checkAttributeSyntax(PerunSessionImpl perunSession, Resource resource, Attribute attribute)
      throws WrongAttributeValueException {
    //Null is ok, it means use default permissions in script (probably 0700)
    if (attribute.getValue() == null) {
      return;
    }

    Map<String, String> scratchDirs = attribute.valueAsMap();

    for (Map.Entry<String, String> entry : scratchDirs.entrySet()) {
      Matcher mountPointMatch = mountPointPattern.matcher(entry.getKey());
      if (!mountPointMatch.matches()) {
        throw new WrongAttributeValueException(attribute, resource,
            "Bad format of home point '" + entry.getKey() + "'");
      }
      Matcher permissionsMatch = permissionsPattern.matcher(entry.getValue());
      if (!permissionsMatch.matches()) {
        throw new WrongAttributeValueException(attribute, resource,
            "Bad format of permissions '" + entry.getValue() + "' for home point '" + entry.getKey() + "'");
      }
    }
    Set<String> scratchDirsKeys = scratchDirs.keySet();
    // get normalized paths and put into set to remove semantic duplicates
    Set<String> scratchDirsNormalized = scratchDirsKeys.stream().map(this::normalizePath).collect(Collectors.toSet());
    if (scratchDirsNormalized.size() != scratchDirsKeys.size()) {
      // some paths were remove => duplicates detected
      throw new WrongAttributeValueException(attribute, resource, "Scratch directory duplicated detected on this " +
                                                                      "resource.");
    }
  }

  @Override
  public void checkAttributeSemantics(PerunSessionImpl perunSession, Resource resource, Attribute attribute)
      throws WrongReferenceAttributeValueException, WrongAttributeAssignmentException {
    if (attribute.getValue() == null) {
      // value can be null - means that only scratches from the facility attribute get applied
      return;
    }
    Facility facility = perunSession.getPerunBl().getResourcesManagerBl().getFacility(perunSession, resource);
    List<Resource> facilityResources =
        perunSession.getPerunBl().getFacilitiesManagerBl().getAssignedResources(perunSession, facility);
    // get normalized paths and put into set to remove semantic duplicates
    Set<String> scratchDirsNormalized = attribute.valueAsMap().keySet().stream().map(this::normalizePath)
                                        .collect(Collectors.toSet());
    facilityResources.remove(resource);
    for (Resource res : facilityResources) {
      try {
        Attribute resFsScratchDirs = perunSession.getPerunBl().getAttributesManagerBl()
                                         .getAttribute(perunSession, res, attribute.getName());
        if (resFsScratchDirs.getValue() != null) {
          Map<String, String> resScratchDirs = resFsScratchDirs.valueAsMap();
          for (String key : resScratchDirs.keySet()) {
            if (scratchDirsNormalized.contains(normalizePath(key))) {
              throw new WrongReferenceAttributeValueException(attribute, resFsScratchDirs, resource, null, res, null,
                  "Scratch directory '" + key + "' is already defined in resource " + res.getName());
            }
          }
        }
      } catch (AttributeNotExistsException e) {
        throw new ConsistencyErrorException(e);
      }
    }
  }

  @Override
  public AttributeDefinition getAttributeDefinition() {
    AttributeDefinition attr = new AttributeDefinition();
    attr.setNamespace(AttributesManager.NS_RESOURCE_ATTR_DEF);
    attr.setFriendlyName("fsScratchDirs");
    attr.setDisplayName("Mount points and permissions for fs_scratch");
    attr.setType(LinkedHashMap.class.getName());
    attr.setDescription("Mount points and respective Unix permissions, which will be applied when new scratch folder " +
                            "is created. Overrides facility scratches. Mount points have to be unique within assigned" +
                            " resources of the facility");
    return attr;
  }

  /**
   * Normalizes file path from string. This helps us detect semantic duplicates (e.g. /dir1/dir2 and /dir1/dir2/)
   *
   * @param pathString path to normalize
   * @return normalized path string
   */
  private String normalizePath(String pathString) {
    try {
      Path path = Paths.get(pathString).normalize();
      return path.toString();
    } catch (InvalidPathException e) {
      throw new InternalErrorException(e);
    }
  }
}
