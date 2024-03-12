package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for Resource Virtual Attributes modules. Implements methods for modules to perform default function.
 * In the function that the method in the module does nothing, it is not necessary to implement it, simply extend this
 * abstract class.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public abstract class ResourceVirtualAttributesModuleAbstract extends ResourceAttributesModuleAbstract
    implements ResourceVirtualAttributesModuleImplApi {


  public Attribute getAttributeValue(PerunSessionImpl perunSession, Resource resource, AttributeDefinition attribute) {
    return new Attribute(attribute);
  }

  @Override
  public List<String> getStrongDependencies() {
    List<String> dependecies = new ArrayList<>();
    return dependecies;
  }

  public boolean removeAttributeValue(PerunSessionImpl perunSession, Resource resource, AttributeDefinition attribute)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException {
    return false;
  }

  @Override
  public List<AuditEvent> resolveVirtualAttributeValueChange(PerunSessionImpl perunSession, AuditEvent message) {
    return new ArrayList<>();
  }

  public boolean setAttributeValue(PerunSessionImpl perunSession, Resource resource, Attribute attribute)
      throws WrongReferenceAttributeValueException {
    return false;
  }
}
