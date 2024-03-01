package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.ArrayList;
import java.util.List;


/**
 * Abstract class for Group Virtual Attributes modules. Implements methods for modules to perform default function. In
 * the function that the method in the module does nothing, it is not necessary to implement it, simply extend this
 * abstract class.
 *
 * @author Pavel Vyskocil <vyskocilpavel@muni.com>
 */
public abstract class GroupVirtualAttributesModuleAbstract extends GroupAttributesModuleAbstract
    implements GroupVirtualAttributesModuleImplApi {

  public Attribute getAttributeValue(PerunSessionImpl perunSession, Group group, AttributeDefinition attribute) {
    return new Attribute(attribute);
  }

  @Override
  public List<String> getStrongDependencies() {
    List<String> dependecies = new ArrayList<>();
    return dependecies;
  }

  public void removeAttributeValue(PerunSessionImpl perunSession, Group group, AttributeDefinition attribute) {

  }

  @Override
  public List<AuditEvent> resolveVirtualAttributeValueChange(PerunSessionImpl perunSession, AuditEvent message) {
    return new ArrayList<>();
  }

  public boolean setAttributeValue(PerunSessionImpl perunSession, Group group, Attribute attribute) {
    return false;
  }
}
