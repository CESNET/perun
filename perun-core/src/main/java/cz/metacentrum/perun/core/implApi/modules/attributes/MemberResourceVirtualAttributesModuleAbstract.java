package cz.metacentrum.perun.core.implApi.modules.attributes;

import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for Resource Member Virtual Attributes modules.
 * Implements methods for modules to perform default function.
 * In the function that the method in the module does nothing, it is not necessary to implement it, simply extend this abstract class.
 *
 * @author Michal Stava <stavamichal@gmail.com>
 */
public abstract class MemberResourceVirtualAttributesModuleAbstract extends MemberResourceAttributesModuleAbstract
    implements MemberResourceVirtualAttributesModuleImplApi {


  public Attribute getAttributeValue(PerunSessionImpl perunSession, Member member, Resource resource,
                                     AttributeDefinition attribute) {
    return new Attribute(attribute);
  }

  public boolean setAttributeValue(PerunSessionImpl perunSession, Member member, Resource resource,
                                   Attribute attribute) {
    return false;
  }

  public boolean removeAttributeValue(PerunSessionImpl perunSession, Member member, Resource resource,
                                      AttributeDefinition attribute) {
    return false;
  }

  @Override
  public List<AuditEvent> resolveVirtualAttributeValueChange(PerunSessionImpl perunSession, AuditEvent message)
      throws AttributeNotExistsException, WrongAttributeAssignmentException {
    return new ArrayList<>();
  }

  @Override
  public List<String> getStrongDependencies() {
    List<String> dependecies = new ArrayList<>();
    return dependecies;
  }
}
