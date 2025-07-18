package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeAction;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributePolicyCollection;
import cz.metacentrum.perun.core.api.AttributeRules;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Host;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Pair;
import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.RichAttribute;
import cz.metacentrum.perun.core.api.RichMember;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AnonymizationNotSupportedException;
import cz.metacentrum.perun.core.api.exceptions.AttributeAlreadyMarkedUniqueException;
import cz.metacentrum.perun.core.api.exceptions.AttributeDefinitionExistsException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotMarkedUniqueException;
import cz.metacentrum.perun.core.api.exceptions.GroupResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberGroupMismatchException;
import cz.metacentrum.perun.core.api.exceptions.MemberResourceMismatchException;
import cz.metacentrum.perun.core.api.exceptions.ModuleNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.RelationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongModuleTypeException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.implApi.modules.attributes.UserVirtualAttributesModuleImplApi;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.utils.graphs.Graph;
import cz.metacentrum.perun.utils.graphs.GraphTextFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Michal Prochazka <michalp@ics.muni.cz>
 * @author Slavek Licehammer <glory@ics.muni.cz>
 */
public interface AttributesManagerBl {

  /**
   * Blocks the attribute value for future use (optionally in the attribute namespace)
   *
   * @param session
   * @param attribute
   * @throws AttributeNotExistsException when the attribute does not exist
   */
  void blockAttributeValue(PerunSession session, Attribute attribute) throws AttributeNotExistsException;

  /**
   * Check if attribute is from the same namespace as it's handler
   *
   * @param sess
   * @param attributeDefinition
   * @param handler             attribute's handler
   * @throws WrongAttributeAssignmentException
   * @throws InternalErrorException
   */
  void checkAttributeAssignment(PerunSession sess, AttributeDefinition attributeDefinition, PerunBean handler)
      throws WrongAttributeAssignmentException;

  /**
   * Check if attribute is from the same namespace as combination of perunBean handlers. Order of handlers does not
   * matter.
   *
   * @param sess                perun session
   * @param attributeDefinition the attribute definition
   * @param handler1            one of attribute's handlers
   * @param handler2            one of attribute's handlers
   * @throws WrongAttributeAssignmentException if assignment of attribute is not correct
   * @throws InternalErrorException            if both handlers are empty or namespace for handlers can't be found
   */
  void checkAttributeAssignment(PerunSession sess, AttributeDefinition attributeDefinition, PerunBean handler1,
                                PerunBean handler2) throws WrongAttributeAssignmentException;

  /**
   * This method check validity of value on all attributes which depends on the attributes in richAttr object.
   * <p>
   * There are two types of dependency "normal" and "strong" and every non-virtual attribute can have some "normal"
   * dependencies and every virtual attribute can have some "normal" and some "strong" dependencies.
   * <p>
   * Normal dependency means that if we have attributes A and B and attribute A is dependent on attribute B, then if
   * value of attribute B has been changed, we need to check that value of attribute A is still valid. In other words:
   * If '->' means depends on, then in case that A -> B, if value of B has been changed, we need to check that value of
   * A is still valid.
   * <p>
   * Strong dependency means that if we have attribute A and B and attribute A is strongly dependent on attribute B,
   * then if value of attribute B has been changed, it can affect value of attribute A too therefore we need to check
   * value of attribute A and also check all attributes which depend on attribute A. In other words: If '=>' means
   * strongly depends on and '->' means depends on, then in case that A => B and C -> A, if value of B has been changed,
   * we need to check not only A, but also C, because validity of attribute C could been affected but change of
   * attribute A.
   * <p>
   * RichAttribute is needed because it contains useful objects in holders.
   *
   * @param sess
   * @param richAttr RichAttribute with attribute an its' holders
   * @throws InternalErrorException
   * @throws WrongAttributeValueException
   * @throws WrongAttributeAssignmentException
   * @throws WrongReferenceAttributeValueException
   */
  void checkAttributeDependencies(PerunSession sess, RichAttribute richAttr)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  void checkAttributeExists(PerunSession sess, AttributeDefinition attribute) throws AttributeNotExistsException;

  /**
   * Check if value of this facility attribute has valid semantics.
   *
   * @param sess      perun session
   * @param facility  facility for which you want to check validity of attribute
   * @param attribute attribute to check
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException     if attribute isn't facility attribute
   * @throws WrongReferenceAttributeValueException if the attribute value has wrong/illegal semantics
   */
  void checkAttributeSemantics(PerunSession sess, Facility facility, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Check if value of this vo attribute has valid semantics.
   *
   * @param sess      perun session
   * @param vo        vo for which you want to check validity of attribute
   * @param attribute attribute to check
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException     if attribute isn't vo attribute
   * @throws WrongReferenceAttributeValueException if the attribute value has wrong/illegal semantics
   */
  void checkAttributeSemantics(PerunSession sess, Vo vo, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Check if value of this group attribute has valid semantics.
   *
   * @param sess      perun session
   * @param group     group for which you want to check validity of attribute
   * @param attribute attribute to check
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException     if attribute isn't group attribute
   * @throws WrongReferenceAttributeValueException if the attribute value has wrong/illegal semantics
   */
  void checkAttributeSemantics(PerunSession sess, Group group, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Check if value of this resource attribute has valid semantics.
   *
   * @param sess      perun session
   * @param resource  resource for which you want to check validity of attribute
   * @param attribute attribute to check
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException     if attribute isn't resource attribute
   * @throws WrongReferenceAttributeValueException if the attribute value has wrong/illegal semantics
   */
  void checkAttributeSemantics(PerunSession sess, Resource resource, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Check if value of this member-resource attribute has valid semantics.
   *
   * @param sess      perun session
   * @param member    member for which (and for specified resource) you want to check validity of attribute
   * @param resource  resource for which (and for specified member) you want to check validity of attribute
   * @param attribute attribute to check
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException     if attribute isn't member-resource attribute
   * @throws WrongReferenceAttributeValueException if the attribute value has wrong/illegal semantics
   * @throws MemberResourceMismatchException
   */
  void checkAttributeSemantics(PerunSession sess, Member member, Resource resource, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException, MemberResourceMismatchException;

  /**
   * Check if value of this member-group attribute has valid semantics.
   *
   * @param sess      perun session
   * @param group     group for which (and for specified member) you want to check validity of attribute
   * @param member    member for which (and for specified group) you want to check validity of attribute
   * @param attribute attribute to check
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException if attribute isn't member-resource attribute
   */
  void checkAttributeSemantics(PerunSession sess, Member member, Group group, Attribute attribute)
      throws WrongAttributeAssignmentException, MemberGroupMismatchException, WrongReferenceAttributeValueException;

  /**
   * Check if value of this member attribute has valid semantics.
   *
   * @param sess      perun session
   * @param member    member for which (and for specified resource) you want to check validity of attribute
   * @param attribute attribute to check
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException     if attribute isn't member-resource attribute
   * @throws WrongReferenceAttributeValueException if the attribute value has wrong/illegal semantics
   */
  void checkAttributeSemantics(PerunSession sess, Member member, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Check if value of this user-facility attribute has valid semantics.
   *
   * @param sess      perun session
   * @param facility  facility for which (and for specified user) you want to check validity of attribute
   * @param user      user for which (and for specified facility) you want to check validity of attribute
   * @param attribute attribute to check
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException     if attribute isn't user-facility attribute
   * @throws WrongReferenceAttributeValueException if the attribute value has wrong/illegal semantics
   */
  void checkAttributeSemantics(PerunSession sess, Facility facility, User user, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Check if value of this group-resource attribute has valid semantics
   *
   * @param sess     perun session
   * @param resource resource for which (and for specified group) you want to check validity of attribute
   * @param group    group for which (and for specified resource) you want to check validity of attribute
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException     if attribute isn't group-resource attribute
   * @throws WrongReferenceAttributeValueException if the attribute value has wrong/illegal semantics
   * @throws GroupResourceMismatchException        if group and resource are not in the same VO
   */

  void checkAttributeSemantics(PerunSession sess, Resource resource, Group group, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException, GroupResourceMismatchException;

  /**
   * Check if value of this user attribute has valid semantics.
   *
   * @param sess      perun session
   * @param user      user for which (and for specified facility) you want to check validity of attribute
   * @param attribute attribute to check
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException     if attribute isn't user-facility attribute
   * @throws WrongReferenceAttributeValueException if the attribute value has wrong/illegal semantics
   */
  void checkAttributeSemantics(PerunSession sess, User user, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Check if the value of this host attribute has valid semantics
   *
   * @param sess      perun session
   * @param host      host which attribute validity is checked
   * @param attribute attribute to check
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException if the attribute isn't host attribute
   */
  void checkAttributeSemantics(PerunSession sess, Host host, Attribute attribute)
      throws WrongAttributeAssignmentException;

  /**
   * Check if the value of this entityless attribute has valid semantics
   *
   * @param sess      perun session
   * @param key       check the attribute for this key
   * @param attribute attribute to check
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongReferenceAttributeValueException if the attribute value has wrong/illegal semantics
   * @throws WrongAttributeAssignmentException     if the attribute isn't entityless attribute
   */
  void checkAttributeSemantics(PerunSession sess, String key, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Check if value of this user ext source attribute has valid semantics.
   *
   * @param sess      perun session
   * @param ues       user external source for which you want to check validity of attribute
   * @param attribute attribute to check
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException if attribute isn't user external source attribute
   */
  void checkAttributeSemantics(PerunSession sess, UserExtSource ues, Attribute attribute)
      throws WrongAttributeAssignmentException;

  /**
   * Batch version of checkAttributeSemantics
   *
   * @param sess      perun session
   * @param facility  facility for which you want to check validity of attribute
   * @param attribute attribute to check
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException if attribute isn't facility attribute
   * @throws WrongAttributeValueException      if the attribute value has wrong/illegal syntax
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSemantics(PerunSession, UserExtSource,
   * Attribute) / void checkAttributesSemantics(PerunSession sess, UserExtSource ues, List<Attribute> attributes) throws
   * WrongAttributeAssignmentException;
   * <p>
   * /** Check if value of this facility attribute has valid syntax.
   */
  void checkAttributeSyntax(PerunSession sess, Facility facility, Attribute attribute)
      throws WrongAttributeValueException, WrongAttributeAssignmentException;

  /**
   * Check if value of this vo attribute has valid syntax.
   *
   * @param sess      perun session
   * @param vo        vo for which you want to check validity of attribute
   * @param attribute attribute to check
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException if attribute isn't vo attribute
   * @throws WrongAttributeValueException      if the attribute value has wrong/illegal syntax
   */
  void checkAttributeSyntax(PerunSession sess, Vo vo, Attribute attribute)
      throws WrongAttributeValueException, WrongAttributeAssignmentException;

  /**
   * Check if value of this group attribute has valid syntax.
   *
   * @param sess      perun session
   * @param group     group for which you want to check validity of attribute
   * @param attribute attribute to check
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException if attribute isn't group attribute
   * @throws WrongAttributeValueException      if the attribute value has wrong/illegal syntax
   */
  void checkAttributeSyntax(PerunSession sess, Group group, Attribute attribute)
      throws WrongAttributeValueException, WrongAttributeAssignmentException;

  /**
   * Check if value of this resource attribute has valid syntax.
   *
   * @param sess      perun session
   * @param resource  resource for which you want to check validity of attribute
   * @param attribute attribute to check
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeValueException      if the attribute value has wrong/illegal syntax
   * @throws WrongAttributeAssignmentException if attribute isn't resource attribute
   */
  void checkAttributeSyntax(PerunSession sess, Resource resource, Attribute attribute)
      throws WrongAttributeValueException, WrongAttributeAssignmentException;

  /**
   * Check if value of this member-resource attribute has valid syntax.
   *
   * @param sess      perun session
   * @param member    member for which (and for specified resource) you want to check validity of attribute
   * @param resource  resource for which (and for specified member) you want to check validity of attribute
   * @param attribute attribute to check
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeValueException      if the attribute value has wrong/illegal syntax
   * @throws WrongAttributeAssignmentException if attribute isn't member-resource attribute
   * @throws MemberResourceMismatchException   if member and resource are not in the same VO
   */
  void checkAttributeSyntax(PerunSession sess, Member member, Resource resource, Attribute attribute)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, MemberResourceMismatchException;

  /**
   * Check if value of this member-group attribute has valid syntax.
   *
   * @param sess      perun session
   * @param group     group for which (and for specified member) you want to check validity of attribute
   * @param member    member for which (and for specified group) you want to check validity of attribute
   * @param attribute attribute to check
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeValueException      if the attribute value has wrong/illegal syntax
   * @throws WrongAttributeAssignmentException if attribute isn't member-group attribute
   */
  void checkAttributeSyntax(PerunSession sess, Member member, Group group, Attribute attribute)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, MemberGroupMismatchException;

  /**
   * Check if value of this member attribute has valid syntax.
   *
   * @param sess      perun session
   * @param member    member for which (and for specified resource) you want to check validity of attribute
   * @param attribute attribute to check
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeValueException      if the attribute value has wrong/illegal syntax
   * @throws WrongAttributeAssignmentException if attribute isn't member attribute
   */
  void checkAttributeSyntax(PerunSession sess, Member member, Attribute attribute)
      throws WrongAttributeValueException, WrongAttributeAssignmentException;

  /**
   * Check if value of this user-facility attribute has valid syntax.
   *
   * @param sess      perun session
   * @param facility  facility for which (and for specified user) you want to check validity of attribute
   * @param user      user for which (and for specified facility) you want to check validity of attribute
   * @param attribute attribute to check
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeValueException      if the attribute value has wrong/illegal syntax
   * @throws WrongAttributeAssignmentException if attribute isn't user-facility attribute
   */
  void checkAttributeSyntax(PerunSession sess, Facility facility, User user, Attribute attribute)
      throws WrongAttributeValueException, WrongAttributeAssignmentException;

  /**
   * Check if value of this group-resource attribute has valid syntax
   *
   * @param sess     perun session
   * @param resource resource for which (and for specified group) you want to check validity of attribute
   * @param group    group for which (and for specified resource) you want to check validity of attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException if attribute isn't group-resource attribute
   * @throws WrongAttributeValueException      if the attribute value has wrong/illegal syntax
   * @throws GroupResourceMismatchException    if group and resource are not in the same VO
   */
  void checkAttributeSyntax(PerunSession sess, Resource resource, Group group, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, GroupResourceMismatchException;

  /**
   * Check if value of this user attribute has valid syntax.
   *
   * @param sess      perun session
   * @param user      user for which (and for specified facility) you want to check validity of attribute
   * @param attribute attribute to check
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeValueException      if the attribute value has wrong/illegal syntax
   * @throws WrongAttributeAssignmentException if attribute isn't user attribute
   */
  void checkAttributeSyntax(PerunSession sess, User user, Attribute attribute)
      throws WrongAttributeValueException, WrongAttributeAssignmentException;

  /**
   * Check if the value of this host attribute has valid syntax.
   *
   * @param sess      perun session
   * @param host      host which attribute validity is checked
   * @param attribute attribute to check
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeValueException      if the attribute value has wrong/illegal syntax
   * @throws WrongAttributeAssignmentException if attribute isn't host attribute
   */
  void checkAttributeSyntax(PerunSession sess, Host host, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException;

  /**
   * Check if the value of this entityless attribute has valid syntax
   *
   * @param sess      perun session
   * @param key       check the attribute for this key
   * @param attribute attribute to check
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeValueException      if the attribute value has wrong/illegal syntax
   * @throws WrongAttributeAssignmentException if the attribute isn't entityless attribute
   */
  void checkAttributeSyntax(PerunSession sess, String key, Attribute attribute)
      throws WrongAttributeValueException, WrongAttributeAssignmentException;

  /**
   * Check if value of this user external source attribute has valid syntax.
   *
   * @param sess      perun session
   * @param ues       user external source for which you want to check validity of attribute
   * @param attribute attribute to check
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeValueException      if the attribute value has wrong/illegal syntax
   * @throws WrongAttributeAssignmentException if attribute isn't user external source attribute
   */
  void checkAttributeSyntax(PerunSession sess, UserExtSource ues, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException;

  void checkAttributesExists(PerunSession sess, List<? extends AttributeDefinition> attributes)
      throws AttributeNotExistsException;

  /**
   * Batch version of checkAttributeSemantics
   *
   * @throws WrongReferenceAttributeValueException if the attribute value has wrong/illegal semantics
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSemantics(PerunSession, Facility, Attribute)
   */
  void checkAttributesSemantics(PerunSession sess, Facility facility, List<Attribute> attributes)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Batch version of checkAttributeSemantics
   *
   * @throws WrongReferenceAttributeValueException if the attribute value has wrong/illegal semantics
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSemantics(PerunSession, Vo, Attribute)
   */
  void checkAttributesSemantics(PerunSession sess, Vo vo, List<Attribute> attributes)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Batch version of checkAttributeSemantics
   *
   * @throws WrongReferenceAttributeValueException if the attribute value has wrong/illegal semantics
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSemantics(PerunSession, Group, Attribute)
   */
  void checkAttributesSemantics(PerunSession sess, Group group, List<Attribute> attributes)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Batch version of checkAttributeSemantics
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSemantics(PerunSession, UserExtSource,
   * Attribute)
   */
  void checkAttributesSemantics(PerunSession sess, UserExtSource ues, List<Attribute> attributes)
      throws WrongAttributeAssignmentException;


  /**
   * Batch version of checkAttributeSemantics
   *
   * @throws WrongReferenceAttributeValueException if the attribute value has wrong/illegal semantics
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSemantics(PerunSession, Resource, Attribute)
   */
  void checkAttributesSemantics(PerunSession sess, Resource resource, List<Attribute> attributes)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Batch version of checkAttributeSemantics
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSemantics(PerunSession, Member, Resource,
   * Attribute)
   */
  void checkAttributesSemantics(PerunSession sess, Member mber, Resource resource, List<Attribute> attributes)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException, MemberResourceMismatchException;

  /**
   * Batch version of checkAttributeSemantics
   *
   * @param workWithUserAttributes method can process also user and user-facility attributes (user is automatically get
   *                               from member a facility is get from resource)
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSemantics(PerunSession, Member, Resource,
   * Attribute)
   */
  void checkAttributesSemantics(PerunSession sess, Member member, Resource resource, List<Attribute> attributes,
                                boolean workWithUserAttributes)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException, MemberResourceMismatchException;

  /**
   * Batch version of checkAttributeSemantics
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSemantics(PerunSession, Member, Group,
   * Attribute)
   */
  void checkAttributesSemantics(PerunSession sess, Member member, Group group, List<Attribute> attributes)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException, MemberGroupMismatchException;

  /**
   * Batch version of checkAttributeSemantics
   *
   * @param workWithUserAttributes method can process also user and member attributes (user is automatically get from
   *                               member) !!WARNING THIS IS VERY TIME-CONSUMING METHOD. DON'T USE IT IN BATCH!!
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSemantics(PerunSession, Member, Group,
   * Attribute)
   */
  void checkAttributesSemantics(PerunSession sess, Member member, Group group, List<Attribute> attributes,
                                boolean workWithUserAttributes)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException, MemberGroupMismatchException;

  /**
   * Check if value of attributes has valid semantics. Attributes can be from namespace: member, user, member-resource
   * and user-facility.
   *
   * @param sess       perun session
   * @param facility   facility for which you want to check validity of attribute
   * @param resource   resource for which you want to check validity of attribute
   * @param user       user for which you want to check validity of attribute
   * @param member     member for which you want to check validity of attribute
   * @param attributes list of attributes to check
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException     if attribute does not belong to appropriate entity
   * @throws WrongReferenceAttributeValueException if the attribute value has wrong/illegal semantics
   * @throws MemberResourceMismatchException       if member and resource are not in the same VO
   */
  void checkAttributesSemantics(PerunSession sess, Facility facility, Resource resource, User user, Member member,
                                List<Attribute> attributes)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException, MemberResourceMismatchException;

  /**
   * Check if value of attributes has valid semantics. Attributes can be from namespace: member, user, member-group,
   * member-resource and user-facility.
   *
   * @param sess       perun session
   * @param facility   facility for which you want to check validity of attribute
   * @param resource   resource for which you want to check validity of attribute
   * @param group      group for which you want to check validity of attribute
   * @param user       user for which you want to check validity of attribute
   * @param member     member for which you want to check validity of attribute
   * @param attributes list of attributes to check
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException     if attribute does not belong to appropriate entity
   * @throws WrongReferenceAttributeValueException if the attribute value has wrong/illegal semantics
   * @throws GroupResourceMismatchException        if group and resource are not in the same VO
   * @throws MemberResourceMismatchException       if member and resource are not in the same VO
   */
  void checkAttributesSemantics(PerunSession sess, Facility facility, Resource resource, Group group, User user,
                                Member member, List<Attribute> attributes)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException, GroupResourceMismatchException,
                 MemberResourceMismatchException, MemberGroupMismatchException;

  /**
   * Batch version of checkAttributeSemantics
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSemantics(PerunSession, Member, Resource,
   * Attribute)
   */
  void checkAttributesSemantics(PerunSession sess, Member member, List<Attribute> attributes)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Batch version of checkAttributeSemantics
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSemantics(PerunSession, Facility, User,
   * Attribute)
   */
  void checkAttributesSemantics(PerunSession sess, Facility facility, User user, List<Attribute> attributes)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * batch version of checkAttributeSemantics
   * <p>
   * ee
   * cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSemantics(cz.metacentrum.perun.core.api.PerunSession,
   * cz.metacentrum.perun.core.api.Resource, cz.metacentrum.perun.core.api.Group,
   * cz.metacentrum.perun.core.api.Attribute)
   */
  void checkAttributesSemantics(PerunSession sess, Resource resource, Group group, List<Attribute> attribute)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException, GroupResourceMismatchException;

  /**
   * batch version of checkAttributeSemantics with workWithGroupAttributes parameter If workWithGroupAttributes is true,
   * checks whether attribute is group-resource or group attribute.
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSemantics(
   *cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.Resource,
   * cz.metacentrum.perun.core.api.Group, cz.metacentrum.perun.core.api.Attribute)
   */
  void checkAttributesSemantics(PerunSession sess, Resource resource, Group group, List<Attribute> attribute,
                                boolean workWithGroupAttribute)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException, GroupResourceMismatchException;

  /**
   * Batch version of checkAttributeSemantics
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSemantics(PerunSession, User, Attribute)
   */
  void checkAttributesSemantics(PerunSession sess, User user, List<Attribute> attributes)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Batch version of checkAttributeSemantics
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSemantics(PerunSession, Host, Attribute)
   */
  void checkAttributesSemantics(PerunSession sess, Host host, List<Attribute> attributes)
      throws WrongAttributeAssignmentException;

  /**
   * Batch version of checkAttributeSyntax
   *
   * @throws WrongAttributeValueException if any of attributes values has wrong/illegal syntax
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSyntax(PerunSession, Facility, Attribute)
   */
  void checkAttributesSyntax(PerunSession sess, Facility facility, List<Attribute> attributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException;

  /**
   * Batch version of checkAttributeSyntax
   *
   * @throws WrongAttributeValueException if any of attributes values has wrong/illegal syntax
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSyntax(PerunSession, Vo, Attribute)
   */
  void checkAttributesSyntax(PerunSession sess, Vo vo, List<Attribute> attributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException;

  /**
   * Batch version of checkAttributeSyntax
   *
   * @throws WrongAttributeValueException if any of attributes values has wrong/illegal syntax
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSyntax(PerunSession, Group, Attribute)
   */
  void checkAttributesSyntax(PerunSession sess, Group group, List<Attribute> attributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException;

  /**
   * Batch version of checkAttributeSyntax
   *
   * @throws WrongAttributeValueException if any of attributes values has wrong/illegal syntax
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSyntax(PerunSession, Resource, Attribute)
   */
  void checkAttributesSyntax(PerunSession sess, Resource resource, List<Attribute> attributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException;

  /**
   * Batch version of checkAttributeSyntax
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSyntax(PerunSession, Member, Resource,
   * Attribute)
   */
  void checkAttributesSyntax(PerunSession sess, Member member, Resource resource, List<Attribute> attributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, MemberResourceMismatchException;

  /**
   * Batch version of checkAttributeSyntax
   *
   * @param workWithUserAttributes method can process also user and user-facility attributes (user is automatically get
   *                               from member a facility is get from resource)
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSyntax(PerunSession, Member, Resource,
   * Attribute) / void checkAttributesSyntax(PerunSession sess, Member member, Resource resource, List<Attribute>
   * attributes) throws WrongAttributeValueException, WrongAttributeAssignmentException,
   * MemberResourceMismatchException;
   * <p>
   * /** Batch version of checkAttributeSyntax
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSyntax(PerunSession, Member, Resource,
   * Attribute)
   */
  void checkAttributesSyntax(PerunSession sess, Member member, Resource resource, List<Attribute> attributes,
                             boolean workWithUserAttributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, MemberResourceMismatchException;

  /**
   * Batch version of checkAttributeSyntax
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSyntax(PerunSession, Member, Group, Attribute)
   */
  void checkAttributesSyntax(PerunSession sess, Member member, Group group, List<Attribute> attributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, MemberGroupMismatchException;

  /**
   * Batch version of checkAttributeSyntax
   *
   * @param workWithUserAttributes method can process also user and member attributes (user is automatically get from
   *                               member) !!WARNING THIS IS VERY TIME-CONSUMING METHOD. DON'T USE IT IN BATCH!!
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSyntax(PerunSession, Member, Group, Attribute)
   */
  void checkAttributesSyntax(PerunSession sess, Member member, Group group, List<Attribute> attributes,
                             boolean workWithUserAttributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, MemberGroupMismatchException;

  /**
   * Check if value of attributes has valid syntax. Attributes can be from namespace: member, user, member-resource and
   * user-facility.
   *
   * @param sess       perun session
   * @param facility   facility for which you want to check validity of attribute
   * @param resource   resource for which you want to check validity of attribute
   * @param user       user for which you want to check validity of attribute
   * @param member     member for which you want to check validity of attribute
   * @param attributes list of attributes to check
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeValueException      if the attribute value has wrong/illegal syntax
   * @throws WrongAttributeAssignmentException if attribute does not belong to appropriate entity
   * @throws MemberResourceMismatchException   if member and resource are not in the same VO
   */
  void checkAttributesSyntax(PerunSession sess, Facility facility, Resource resource, User user, Member member,
                             List<Attribute> attributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, MemberResourceMismatchException;

  /**
   * Check if value of attributes has valid syntax. Attributes can be from namespace: member, user, member-group,
   * member-resource and user-facility.
   *
   * @param sess       perun session
   * @param facility   facility for which you want to check validity of attribute
   * @param resource   resource for which you want to check validity of attribute
   * @param user       user for which you want to check validity of attribute
   * @param member     member for which you want to check validity of attribute
   * @param attributes list of attributes to check
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeValueException      if the attribute value has wrong/illegal syntax
   * @throws WrongAttributeAssignmentException if attribute does not belong to appropriate entity
   * @throws GroupResourceMismatchException    if group and resource are not in the same VO
   * @throws MemberResourceMismatchException   if member and resource are not in the same VO
   */
  void checkAttributesSyntax(PerunSession sess, Facility facility, Resource resource, Group group, User user,
                             Member member, List<Attribute> attributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, GroupResourceMismatchException,
                 MemberResourceMismatchException, MemberGroupMismatchException;

  /**
   * Batch version of checkAttributeSyntax
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSyntax(PerunSession, Member, Resource,
   * Attribute)
   */
  void checkAttributesSyntax(PerunSession sess, Member member, List<Attribute> attributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException;

  /**
   * Batch version of checkAttributeSyntax
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSyntax(PerunSession, Facility, User, Attribute)
   */
  void checkAttributesSyntax(PerunSession sess, Facility facility, User user, List<Attribute> attributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException;

  /**
   * batch version of checkAttributeSyntax
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSyntax(
   *cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.Resource,
   * cz.metacentrum.perun.core.api.Group, cz.metacentrum.perun.core.api.Attribute)
   */
  void checkAttributesSyntax(PerunSession sess, Resource resource, Group group, List<Attribute> attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, GroupResourceMismatchException;

  /**
   * batch version of checkAttributeSyntax with workWithGroupAttributes parameter. If workWithGroupAttributes is true,
   * checks whether attribute is group-resource or group attribute.
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSyntax(
   *cz.metacentrum.perun.core.api.PerunSession, cz.metacentrum.perun.core.api.Resource,
   * cz.metacentrum.perun.core.api.Group, cz.metacentrum.perun.core.api.Attribute)
   */
  void checkAttributesSyntax(PerunSession sess, Resource resource, Group group, List<Attribute> attribute,
                             boolean workWithGroupAttribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, GroupResourceMismatchException;

  /**
   * Batch version of checkAttributeSyntax
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSyntax(PerunSession, User, Attribute)
   */
  void checkAttributesSyntax(PerunSession sess, User user, List<Attribute> attributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException;

  /**
   * Batch version of checkAttributeSyntax
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSyntax(PerunSession, Host, Attribute)
   */
  void checkAttributesSyntax(PerunSession sess, Host host, List<Attribute> attributes)
      throws WrongAttributeAssignmentException, WrongAttributeValueException;

  /**
   * Batch version of checkAttributeSyntax
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#checkAttributeSyntax(PerunSession, UserExtSource, Attribute)
   */
  void checkAttributesSyntax(PerunSession sess, UserExtSource ues, List<Attribute> attributes)
      throws WrongAttributeAssignmentException, WrongAttributeValueException;

  /**
   * Check if group is assigned on resource. If not, throw GroupResourceMismatch Exception
   *
   * @param sess
   * @param group
   * @param resource
   * @throws InternalErrorException
   * @throws GroupResourceMismatchException
   */
  void checkGroupIsFromTheSameVoLikeResource(PerunSession sess, Group group, Resource resource)
      throws GroupResourceMismatchException;

  /**
   * Determine if attribute is from specified namespace.
   *
   * @param sess
   * @param attribute
   * @param namespace
   * @throws WrongAttributeAssignmentException if the attribute isn't from specified namespace
   */
  void checkNamespace(PerunSession sess, AttributeDefinition attribute, String namespace)
      throws WrongAttributeAssignmentException;

  /**
   * Determine if attributes are from specified namespace.
   *
   * @param sess
   * @param attributes
   * @param namespace
   * @throws WrongAttributeAssignmentException if any of the attribute isn't from specified namespace
   */
  void checkNamespace(PerunSession sess, List<? extends AttributeDefinition> attributes, String namespace)
      throws WrongAttributeAssignmentException;

  /**
   * Converts attribute to nonunique.
   * <p>
   * Unmarks unique flag from attribute definition, and deletes all values from a special table with unique constraint
   * that ensures that all values remain unique.
   *
   * @param session perun session
   * @param attrId  attribute id
   * @throws AttributeNotExistsException       when the attribute definition for attrId doesn't exist
   * @throws AttributeNotMarkedUniqueException when the attribute definition is not unique
   */
  int convertAttributeToNonunique(PerunSession session, int attrId)
      throws AttributeNotExistsException, AttributeNotMarkedUniqueException;

  /**
   * Converts attribute to unique.
   * <p>
   * Marks the attribute definition as unique, and copies all values to a special table with unique constraint that
   * ensures that all values remain unique. Values of type ArrayList and LinkedHashMap are splitted into multiple
   * entries, thus each of the entries must be unique. For LinkedHashMap, the unique entries are strings in the form of
   * "key=value", thus it is possible to have same values for different keys.
   * <p>
   * Entityless attributes cannot be converted to unique.
   *
   * @param session perun session
   * @param attrId  attribute id
   */
  void convertAttributeToUnique(PerunSession session, int attrId)
      throws AttributeNotExistsException, AttributeAlreadyMarkedUniqueException;

  /**
   * Creates an attribute, the attribute is stored into the appropriate DB table according to the namespace Also tries
   * to initialize its attribute module and register the module for listening Auditer messages.
   *
   * @param sess                perun session
   * @param attributeDefinition attribute to create
   * @return attribute with set id
   * @throws AttributeDefinitionExistsException if attribute already exists
   * @throws InternalErrorException             if an exception raise in concrete implementation, the exception is
   *                                            wrapped in InternalErrorException
   */
  AttributeDefinition createAttribute(PerunSession sess, AttributeDefinition attributeDefinition)
      throws AttributeDefinitionExistsException;

  /**
   * Appends each member with the member attributes (only def and opt) named in the memberAttrNames list if the value of
   * the attribute for the given member is found in the database.
   *
   * @param sess            perun session
   * @param members         to decorate with the given member attributes
   * @param memberAttrNames list of the member attribute names
   * @return the original list of RichMembers with added member attributes
   * @throws InternalErrorException if RuntimeErrorException has been thrown
   */
  List<RichMember> decorateMembersWithDefOptMemberAttributes(PerunSession sess, List<RichMember> members,
                                                             List<String> memberAttrNames);

  /**
   * Appends each member with the user attributes (only def and opt) named in the userAttrNames list if the value of the
   * attribute for the given member(user) is found in the database.
   *
   * @param sess          perun session
   * @param members       to decorate with the given user attributes
   * @param userAttrNames list of the user attribute names
   * @return the original list of RichMembers with added user attributes
   * @throws InternalErrorException if RuntimeErrorException has been thrown
   */
  List<RichMember> decorateMembersWithDefOptUserAttributes(PerunSession sess, List<RichMember> members,
                                                           List<String> userAttrNames);

  /**
   * Deletes the attribute.
   *
   * @param sess
   * @param attributeDefinition
   * @throws InternalErrorException  if an exception raise in concrete implementation, the exception is wrapped in
   *                                 InternalErrorException
   * @throws RelationExistsException if attribute definition has any relation to some application form item or to some
   *                                 service as a required attribute
   */
  void deleteAttribute(PerunSession sess, AttributeDefinition attributeDefinition) throws RelationExistsException;

  /**
   * Deletes the attribute.
   *
   * @param sess
   * @param attributeDefinition attribute to delete
   * @param force               delete also all existing relation. If this parameter is true the RelationExistsException
   *                            is never thrown.
   * @throws InternalErrorException
   */
  void deleteAttribute(PerunSession sess, AttributeDefinition attributeDefinition, boolean force);

  /**
   * Executes extra logic for attribute module connected to a user entity being deleted
   *
   * @param sess
   * @param user
   * @param attribute
   * @throws WrongAttributeAssignmentException if the attribute isn't user attribute
   */
  void deletedEntityHook(PerunSession sess, User user, Attribute attribute) throws WrongAttributeAssignmentException;

  /**
   * Same as doTheMagic(sess, member, false);
   */
  void doTheMagic(PerunSession sess, Member member)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * This function takes all member-related attributes (member, user, member-resource, user-facility) and tries to fill
   * them and set them. If trueMagic is set, this method can remove invalid attribute value (value which didn't pass
   * checkAttributeSemantics test) and try to fill and set another. In this case, WrongReferenceAttributeValueException,
   * WrongAttributeValueException are thrown if same attribute can't be set corraclty.
   *
   * @param sess
   * @param member
   * @param trueMagic
   * @throws InternalErrorException
   * @throws WrongAttributeValueException
   * @throws WrongReferenceAttributeValueException
   */
  void doTheMagic(PerunSession sess, Member member, boolean trueMagic)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Escapes QUERY PARAMETER VALUES in URL value. Does not modify domain or parameters names. e.g. 'domain/?vo=vo name'
   * => 'domain/?vo=vo+name'
   *
   * @param value url
   * @return url with escaped special characters in query parameter's values
   */
  String escapeQueryParameters(String value);

  /**
   * This method try to fill a value of the resource attribute. Value may be copied from some facility attribute.
   *
   * @param sess      perun session
   * @param resource  resource, attribute of which you want to fill
   * @param attribute attribute to fill. If attributes already have set value, this value won't be overwritten. This
   *                  means the attribute value must be empty otherwise this method won't fill it.
   * @return attribute which MAY have filled value
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  Attribute fillAttribute(PerunSession sess, Resource resource, Attribute attribute)
      throws WrongAttributeAssignmentException;

  /**
   * This method try to fill value of the member-resource attribute. This value is automatically generated, but not all
   * attributes can be filled this way.
   *
   * @param sess      perun session
   * @param member    attribute of this member (and resource) and you want to fill
   * @param resource  attribute of this resource (and member) and you want to fill
   * @param attribute attribute to fill. If attributes already have set value, this value won't be overwritten. This
   *                  means the attribute value must be empty otherwise this method won't fill it.
   * @return attribute which MAY have filled value
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws MemberResourceMismatchException
   */
  Attribute fillAttribute(PerunSession sess, Member member, Resource resource, Attribute attribute)
      throws WrongAttributeAssignmentException, MemberResourceMismatchException;

  /**
   * This method tries to fill value of the member-group attribute. This value is automatically generated, but not all
   * attributes can be filled this way.
   *
   * @param sess      perun session
   * @param member    attribute of this member (and group) you want to fill
   * @param group     attribute of this group (and member) you want to fill
   * @param attribute attribute to fill. If attributes already have set value, this value won't be overwritten. This
   *                  means the attribute value must be empty otherwise this method won't fill it.
   * @return attribute which MAY have filled value
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  Attribute fillAttribute(PerunSession sess, Member member, Group group, Attribute attribute)
      throws WrongAttributeAssignmentException, MemberGroupMismatchException;

  /**
   * This method try to fill value of the member attribute. This value is automatically generated, but not all
   * attributes can be filled this way.
   *
   * @param sess      perun session
   * @param member    attribute of this member (and resource) and you want to fill
   * @param attribute attribute to fill. If attributes already have set value, this value won't be overwritten. This
   *                  means the attribute value must be empty otherwise this method won't fill it.
   * @return attribute which MAY have filled value
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  Attribute fillAttribute(PerunSession sess, Member member, Attribute attribute);

  /**
   * This method try to fill value of the user-facility attribute. This value is automatically generated, but not all
   * attributes can be filled this way.
   *
   * @param sess      perun session
   * @param facility  attribute of this facility (and user) and you want to fill
   * @param user      attribute of this user (and facility) and you want to fill
   * @param attribute attribute to fill. If attributes already have set value, this value won't be overwritten. This
   *                  means the attribute value must be empty otherwise this method won't fill it.
   * @return attribute which MAY have filled value
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  Attribute fillAttribute(PerunSession sess, Facility facility, User user, Attribute attribute)
      throws WrongAttributeAssignmentException;

  /**
   * This method try to fill value of the user attribute. This value is automatically generated, but not all attributes
   * can be filled this way.
   *
   * @param sess      perun session
   * @param user      attribute of this user (and facility) and you want to fill
   * @param attribute attribute to fill. If attributes already have set value, this value won't be overwritten. This
   *                  means the attribute value must be empty otherwise this method won't fill it.
   * @return attribute which MAY have filled value
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  Attribute fillAttribute(PerunSession sess, User user, Attribute attribute) throws WrongAttributeAssignmentException;

  /**
   * This method tries to fill value of the host attribute. This value is automatically generated, but not all
   * attributes can be filled this way
   *
   * @param sess
   * @param host
   * @param attribute attribute to fill. If attributes already have set value, this value won't be overwritten. This
   *                  means the attribute value must be empty otherwise this method won't fill it.
   * @return attribute which may have filled value
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  Attribute fillAttribute(PerunSession sess, Host host, Attribute attribute) throws WrongAttributeAssignmentException;

  Attribute fillAttribute(PerunSession sess, Group group, Attribute attribute) throws WrongAttributeAssignmentException;

  /**
   * This method tries to fill value of group-resource attribute. This value is automatically generated, but not all
   * attributes can be filled this way
   *
   * @param sess
   * @param resource
   * @param group
   * @param attribute
   * @return attribute which may have filled value
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws GroupResourceMismatchException
   */
  Attribute fillAttribute(PerunSession sess, Resource resource, Group group, Attribute attribute)
      throws WrongAttributeAssignmentException, GroupResourceMismatchException;

  /**
   * This method tries to fill value of the user external source attribute. This value is automatically generated, but
   * not all attributes can be filled this way
   *
   * @param sess
   * @param ues
   * @param attribute attribute to fill. If attributes already have set value, this value won't be overwritten. This
   *                  means the attribute value must be empty otherwise this method won't fill it.
   * @return attribute which may have filled value
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  Attribute fillAttribute(PerunSession sess, UserExtSource ues, Attribute attribute)
      throws WrongAttributeAssignmentException;

  /**
   * Batch version of fillAttribute. This method skips all attributes with not-null value.
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#fillAttribute(PerunSession, Resource, Attribute)
   */
  List<Attribute> fillAttributes(PerunSession sess, Resource resource, List<Attribute> attributes)
      throws WrongAttributeAssignmentException;

  /**
   * Batch version of fillAttribute. This method skips all attributes with not-null value.
   *
   * @throws WrongAttributeValueException if any of attributes values is wrong/illegal
   * @see cz.metacentrum.perun.core.api.AttributesManager#fillAttribute(PerunSession, Member, Resource, Attribute)
   */
  List<Attribute> fillAttributes(PerunSession sess, Member member, Resource resource, List<Attribute> attributes)
      throws WrongAttributeAssignmentException, MemberResourceMismatchException;

  /**
   * @param workWithUserAttributes method can process also user and user-facility attributes (user is automatically get
   *                               from member a facility is get from resource) !!WARNING THIS IS VERY TIME-CONSUMING
   *                               METHOD. DON'T USE IT IN BATCH!!
   */
  List<Attribute> fillAttributes(PerunSession sess, Member member, Resource resource, List<Attribute> attributes,
                                 boolean workWithUserAttributes)
      throws WrongAttributeAssignmentException, MemberResourceMismatchException;

  /**
   * Batch version of fillAttribute. This method skips all attributes with not-null value.
   *
   * @throws WrongAttributeAssignmentException if any of attributes values is wrong/illegal
   * @see cz.metacentrum.perun.core.api.AttributesManager#fillAttribute(PerunSession, Member, Group, Attribute)
   */
  List<Attribute> fillAttributes(PerunSession sess, Member member, Group group, List<Attribute> attributes)
      throws WrongAttributeAssignmentException, MemberGroupMismatchException;

  /**
   * @param workWithUserAttributes method can process also user and memebr attributes (user is automatically get from
   *                               member) !!WARNING THIS IS VERY TIME-CONSUMING METHOD. DON'T USE IT IN BATCH!!
   */
  List<Attribute> fillAttributes(PerunSession sess, Member member, Group group, List<Attribute> attributes,
                                 boolean workWithUserAttributes)
      throws WrongAttributeAssignmentException, MemberGroupMismatchException;

  /**
   * This method try to fill value of the user, member, member-resource and user-facility attributes. This value is
   * automatically generated, but not all attributes can be filled this way. This method skips all attributes with
   * not-null value.
   *
   * @param sess
   * @param facility
   * @param resource
   * @param user
   * @param member
   * @param attributes
   * @return
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws MemberResourceMismatchException
   */
  List<Attribute> fillAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member,
                                 List<Attribute> attributes)
      throws WrongAttributeAssignmentException, MemberResourceMismatchException;

  /**
   * This method try to fill value of the user, member, member-resource and user-facility attributes. This value is
   * automatically generated, but not all attributes can be filled this way. This method skips all attributes with
   * not-null value.
   * <p>
   * if returnOnlyAttributesWithChangedValue is true - return only attributes which changed value by filling new one If
   * false, has the same functionality like fillAttributes(PerunSession sess, Facility facility, Resource resource, User
   * user, Member member, List<Attribute> attributes)
   *
   * @param sess
   * @param facility
   * @param resource
   * @param user
   * @param member
   * @param attributes
   * @param returnOnlyAttributesWithChangedValue
   * @return
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws MemberResourceMismatchException
   */
  List<Attribute> fillAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member,
                                 List<Attribute> attributes, boolean returnOnlyAttributesWithChangedValue)
      throws WrongAttributeAssignmentException, MemberResourceMismatchException;

  /**
   * Batch version of fillAttribute. This method skips all attributes with not-null value.
   *
   * @throws WrongAttributeValueException if any of attributes values is wrong/illegal
   * @see cz.metacentrum.perun.core.api.AttributesManager#fillAttribute(PerunSession, Member, Resource, Attribute)
   */
  List<Attribute> fillAttributes(PerunSession sess, Member member, List<Attribute> attributes);

  /**
   * Batch version of fillAttribute. This method skips all attributes with not-null value.
   *
   * @throws WrongAttributeValueException if any of attributes values is wrong/illegal
   * @see cz.metacentrum.perun.core.api.AttributesManager#fillAttribute(PerunSession, Facility, User, Attribute)
   */
  List<Attribute> fillAttributes(PerunSession sess, Facility facility, User user, List<Attribute> attributes)
      throws WrongAttributeAssignmentException;

  /**
   * Batch version of fillAttribute. This method skips all attributes with not-null value.
   *
   * @throws WrongAttributeValueException if any of attributes values is wrong/illegal
   * @see cz.metacentrum.perun.core.api.AttributesManager#fillAttribute(PerunSession, User, Attribute)
   */
  List<Attribute> fillAttributes(PerunSession sess, User user, List<Attribute> attributes)
      throws WrongAttributeAssignmentException;

  List<Attribute> fillAttributes(PerunSession sess, Group group, List<Attribute> groupReqAttributes)
      throws WrongAttributeAssignmentException;

  /**
   * Batch version of fillAttribute. This method skips all attributes with not-null value.
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#fillAttribute(PerunSession, Host, Attribute)
   */
  List<Attribute> fillAttributes(PerunSession sess, Host host, List<Attribute> attributes)
      throws WrongAttributeAssignmentException;

  /**
   * Batch version of fillAttribute. This method skips all attributes with not-null value.
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#fillAttribute(cz.metacentrum.perun.core.api.PerunSession,
   * cz.metacentrum.perun.core.api.Resource, cz.metacentrum.perun.core.api.Group,
   * cz.metacentrum.perun.core.api.Attribute)
   */
  List<Attribute> fillAttributes(PerunSession sess, Resource resource, Group group, List<Attribute> attribute)
      throws WrongAttributeAssignmentException, GroupResourceMismatchException;

  List<Attribute> fillAttributes(PerunSession sess, Resource resource, Group group, List<Attribute> attribute,
                                 boolean workWithGroupAttributes)
      throws WrongAttributeAssignmentException, GroupResourceMismatchException;

  /**
   * Batch version of fillAttribute. This method skips all attributes with not-null value.
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#fillAttribute(PerunSession, UserExtSource, Attribute)
   */
  List<Attribute> fillAttributes(PerunSession sess, UserExtSource ues, List<Attribute> attributes)
      throws WrongAttributeAssignmentException;

  /**
   * Check if value of this group attribute has valid semantics no matter if attribute is required or not.
   *
   * @param sess      perun session
   * @param group     group for which you want to check validity of attribute
   * @param attribute attribute to check
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException     if attribute isn't group attribute
   * @throws WrongReferenceAttributeValueException if the attribute value has wrong/illegal semantics
   */
  void forceCheckAttributeSemantics(PerunSession sess, Group group, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Check if value of this resource attribute has valid semantics no matter if attribute is required or not.
   *
   * @param sess      perun session
   * @param resource  resource for which you want to check validity of attribute
   * @param attribute attribute to check
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException     if attribute isn't resource attribute
   * @throws WrongReferenceAttributeValueException if the attribute value has wrong/illegal semantics
   */
  void forceCheckAttributeSemantics(PerunSession sess, Resource resource, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Check if value of attributes has valid semantics. Attributes can be from namespace: member, user, member-resource
   * and user-facility. This method does not validate, if all of these attributes are truly required! This methods calls
   * semantics checks on ALL of the given attributes.
   *
   * @param sess       perun session
   * @param facility   facility for which you want to check validity of attribute
   * @param resource   resource for which you want to check validity of attribute
   * @param user       user for which you want to check validity of attribute
   * @param member     member for which you want to check validity of attribute
   * @param attributes list of attributes to check
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException     if attribute does not belong to appropriate entity
   * @throws WrongReferenceAttributeValueException if the attribute value has wrong/illegal semantics
   * @throws MemberResourceMismatchException       if member and resource are not in the same VO
   */
  void forceCheckAttributesSemantics(PerunSession sess, Facility facility, Resource resource, User user, Member member,
                                     List<Attribute> attributes)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException, MemberResourceMismatchException;

  /**
   * Get all <b>non-empty</b> attributes associated with the group starts with name startPartOfName. Get only nonvirtual
   * attributes with notNull Value.
   *
   * @param sess            perun session
   * @param group           to get the attributes from
   * @param startPartOfName attribute name start with this part
   * @return list of attributes which name start with startPartOfName
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getAllAttributesStartWithNameWithoutNullValue(PerunSession sess, Group group, String startPartOfName);

  /**
   * Get all <b>non-empty</b> attributes associated with the resource starts with name startPartOfName. Get only
   * nonvirtual attributes with notNull value.
   *
   * @param sess            perun session
   * @param resource        to get the attributes from
   * @param startPartOfName attribute name start with this part
   * @return list of attributes which name start with startPartOfName
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getAllAttributesStartWithNameWithoutNullValue(PerunSession sess, Resource resource,
                                                                String startPartOfName);

  /**
   * @return get map of all Dependencies
   */
  Map<AttributeDefinition, Set<AttributeDefinition>> getAllDependencies();

  /**
   * Returns list of all possible namespaces.
   *
   * @param sess session
   * @return list of namespaces
   */
  List<String> getAllNamespaces(PerunSession sess);

  /**
   * This method get all similar attr_names which start with partOfAttributeName
   *
   * @param sess
   * @param startingPartOfAttributeName is something like: urn:perun:user_facility:attribute-def:def:login-namespace:
   * @return list of similar attribute names like: urn:perun:user_facility:attribute-def:def:login-namespace:cesnet etc.
   * @throws InternalErrorException
   * @throws AttributeNotExistsException
   */
  List<String> getAllSimilarAttributeNames(PerunSession sess, String startingPartOfAttributeName);

  /**
   * Get all values for specified attribute. Attribute can't be core, core-managed or virt.
   *
   * @param sess
   * @param attributeDefinition
   * @return
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException if attribute is core, core-managed or virt
   */
  List<Object> getAllValues(PerunSession sess, AttributeDefinition attributeDefinition)
      throws WrongAttributeAssignmentException;

  /**
   * Gets anonymized value of the attribute.
   *
   * @param sess      perun session
   * @param user      user for which you want the anonymized attribute value
   * @param attribute attribute to get anonymized value from
   * @return attribute with anonymized value
   * @throws InternalErrorException             if an exception raise in concrete implementation, the exception is
   *                                            wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException  if attribute isn't user attribute
   * @throws AnonymizationNotSupportedException if the module doesn't exist or it doesn't implement this method
   */
  Attribute getAnonymizedValue(PerunSession sess, User user, Attribute attribute)
      throws WrongAttributeAssignmentException, AnonymizationNotSupportedException;

  /**
   * Returns list of app form items' shortnames for which the given attribute is a source or a destination attribute in
   * the given application form
   *
   * @param sess      session
   * @param appFormId id of application form
   * @param attr      attribute definition
   * @return list of shortnames
   */
  List<String> getAppFormItemsForAppFormAndAttribute(PerunSession sess, int appFormId, AttributeDefinition attr);

  /**
   * Returns all application forms where the given attribute definition is a source or a destination attribute for any
   * application from item
   *
   * @param sess session
   * @param attr attribute definition
   * @return list of application forms where the given attribute definition has relation to any application form item
   */
  List<ApplicationForm> getAppFormsWhereAttributeRelated(PerunSession sess, AttributeDefinition attr);

  /**
   * Get particular attribute for the facility.
   *
   * @param sess
   * @param facility      to get attribute from
   * @param attributeName attribute name defined in the particular manager
   * @return attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  Attribute getAttribute(PerunSession sess, Facility facility, String attributeName)
      throws AttributeNotExistsException, WrongAttributeAssignmentException;

  /**
   * Get particular attribute for the vo.
   *
   * @param sess
   * @param vo            to get attribute from
   * @param attributeName attribute name defined in the particular manager
   * @return attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws AttributeNotExistsException
   * @throws WrongAttributeAssignmentException
   */
  Attribute getAttribute(PerunSession sess, Vo vo, String attributeName)
      throws AttributeNotExistsException, WrongAttributeAssignmentException;

  /**
   * Get particular attribute for the group.
   *
   * @param sess
   * @param group         group get attribute from
   * @param attributeName attribute name defined in the particular manager
   * @return attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws AttributeNotExistsException
   * @throws WrongAttributeAssignmentException
   */
  Attribute getAttribute(PerunSession sess, Group group, String attributeName)
      throws WrongAttributeAssignmentException, AttributeNotExistsException;

  /**
   * Get particular attribute for the resource.
   *
   * @param sess
   * @param resource      to get attribute from
   * @param attributeName attribute name defined in the particular manager
   * @return attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  Attribute getAttribute(PerunSession sess, Resource resource, String attributeName)
      throws WrongAttributeAssignmentException, AttributeNotExistsException;

  /**
   * Get particular attribute for the member on this resource.
   *
   * @param sess
   * @param member        to get attribute from
   * @param resource      to get attribute from
   * @param attributeName attribute name defined in the particular manager
   * @return attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws MemberResourceMismatchException
   */
  Attribute getAttribute(PerunSession sess, Member member, Resource resource, String attributeName)
      throws MemberResourceMismatchException, WrongAttributeAssignmentException, AttributeNotExistsException;

  /**
   * Get particular attribute for the member in this group.
   *
   * @param sess
   * @param member        to get attribute from
   * @param group         to get attribute from
   * @param attributeName attribute name defined in the particular manager
   * @return attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws AttributeNotExistsException       if the attribute doesn't exists in the underlying data source
   */
  Attribute getAttribute(PerunSession sess, Member member, Group group, String attributeName)
      throws AttributeNotExistsException, WrongAttributeAssignmentException, MemberGroupMismatchException;

  /**
   * Get particular attribute for the member.
   *
   * @param sess
   * @param member        to get attribute from
   * @param attributeName attribute name defined in the particular manager
   * @return attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  Attribute getAttribute(PerunSession sess, Member member, String attributeName)
      throws WrongAttributeAssignmentException, AttributeNotExistsException;

  /**
   * Get particular attribute for the user on this facility.
   *
   * @param sess
   * @param facility      to get attribute from
   * @param user          to get attribute from
   * @param attributeName attribute name defined in the particular manager
   * @return attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  Attribute getAttribute(PerunSession sess, Facility facility, User user, String attributeName)
      throws WrongAttributeAssignmentException, AttributeNotExistsException;

  /**
   * Get particular attribute for the user.
   *
   * @param sess
   * @param user          to get attribute from
   * @param attributeName attribute name defined in the particular manager
   * @return attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  Attribute getAttribute(PerunSession sess, User user, String attributeName)
      throws WrongAttributeAssignmentException, AttributeNotExistsException;

  /**
   * Get particular attribute for the host
   *
   * @param sess
   * @param host          host to get attribute from
   * @param attributeName attribute name
   * @return attribute
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws AttributeNotExistsException
   */
  Attribute getAttribute(PerunSession sess, Host host, String attributeName)
      throws WrongAttributeAssignmentException, AttributeNotExistsException;

  /**
   * Get particular group attribute on the resource
   *
   * @param sess
   * @param resource
   * @param group
   * @param attributeName
   * @return attribute
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws AttributeNotExistsException
   * @throws GroupResourceMismatchException
   */
  Attribute getAttribute(PerunSession sess, Resource resource, Group group, String attributeName)
      throws WrongAttributeAssignmentException, AttributeNotExistsException, GroupResourceMismatchException;

  /**
   * Get particular entityless attribute
   *
   * @param sess          perun session
   * @param key           key to get attribute for
   * @param attributeName
   * @return attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws AttributeNotExistsException       if the attribute doesn't exists in the underlaying data source
   * @throws WrongAttributeAssignmentException if attribute isn't entityless attribute
   */
  Attribute getAttribute(PerunSession sess, String key, String attributeName)
      throws AttributeNotExistsException, WrongAttributeAssignmentException;

  /**
   * Get particular attribute for the User External Source.
   *
   * @param sess
   * @param ues           to get attribute from
   * @param attributeName attribute name defined in the particular manager
   * @return attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException if atribute prefix does not match entity.
   */
  Attribute getAttribute(PerunSession sess, UserExtSource ues, String attributeName)
      throws WrongAttributeAssignmentException, AttributeNotExistsException;

  /**
   * Get particular attribute for the facility.
   *
   * @param sess
   * @param facility to get attribute from
   * @param id       attribute id
   * @return attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  Attribute getAttributeById(PerunSession sess, Facility facility, int id)
      throws WrongAttributeAssignmentException, AttributeNotExistsException;

  /**
   * Get particular attribute for the vo.
   *
   * @param sess
   * @param vo   to get attribute from
   * @param id   attribute id
   * @return attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  Attribute getAttributeById(PerunSession sess, Vo vo, int id)
      throws WrongAttributeAssignmentException, AttributeNotExistsException;

  /**
   * Get particular attribute for the resource.
   *
   * @param sess
   * @param resource to get attribute from
   * @param id       attribute id
   * @return attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  Attribute getAttributeById(PerunSession sess, Resource resource, int id)
      throws WrongAttributeAssignmentException, AttributeNotExistsException;

  /**
   * Get particular attribute for the member on this resource.
   *
   * @param sess
   * @param member   to get attribute from
   * @param resource to get attribute from
   * @param id       attribute id
   * @return attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws MemberResourceMismatchException
   */
  Attribute getAttributeById(PerunSession sess, Member member, Resource resource, int id)
      throws WrongAttributeAssignmentException, AttributeNotExistsException, MemberResourceMismatchException;

  /**
   * Get particular attribute for the member in this group. Also it can return only member or only user attribute if
   * attr definition is not from NS_MEMBER_GROUP_ATTR but from NS_MEMBER_ATTR or NS_GROUP_ATTR
   *
   * @param sess   perun session
   * @param member to get attribute from
   * @param group  to get attribute from
   * @param id     attribute id
   * @return memberGroup, member OR user attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws AttributeNotExistsException       if the attribute doesn't exists in the underlying data source
   */
  Attribute getAttributeById(PerunSession sess, Member member, Group group, int id)
      throws AttributeNotExistsException, WrongAttributeAssignmentException, MemberGroupMismatchException;

  /**
   * Get particular attribute for the member.
   *
   * @param sess
   * @param member to get attribute from
   * @param id     attribute id
   * @return attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  Attribute getAttributeById(PerunSession sess, Member member, int id)
      throws WrongAttributeAssignmentException, AttributeNotExistsException;

  /**
   * Get particular attribute for the user on this facility.
   *
   * @param sess
   * @param facility to get attribute from
   * @param user     to get attribute from
   * @param id       attribute id
   * @return attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  Attribute getAttributeById(PerunSession sess, Facility facility, User user, int id)
      throws WrongAttributeAssignmentException, AttributeNotExistsException;

  /**
   * Get particular attribute for the user.
   *
   * @param sess
   * @param user to get attribute from
   * @param id   attribute id
   * @return attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  Attribute getAttributeById(PerunSession sess, User user, int id)
      throws WrongAttributeAssignmentException, AttributeNotExistsException;

  /**
   * Get particular attribute for the host
   *
   * @param sess perun session
   * @param host to get attribute from
   * @param id   id of attribute
   * @return attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException if attribute is not host attribute
   * @throws AttributeNotExistsException       if attribute doesn't exist
   */
  Attribute getAttributeById(PerunSession sess, Host host, int id)
      throws WrongAttributeAssignmentException, AttributeNotExistsException;

  /**
   * Get particular group-resource attribute
   *
   * @param sess
   * @param resource
   * @param group
   * @param id
   * @return
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws AttributeNotExistsException
   * @throws GroupResourceMismatchException
   */
  Attribute getAttributeById(PerunSession sess, Resource resource, Group group, int id)
      throws WrongAttributeAssignmentException, AttributeNotExistsException, GroupResourceMismatchException;

  /**
   * Get particular group attribute
   *
   * @param sess
   * @param group
   * @param id
   * @return
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws AttributeNotExistsException
   */
  Attribute getAttributeById(PerunSession sess, Group group, int id)
      throws WrongAttributeAssignmentException, AttributeNotExistsException;

  /**
   * Get particular attribute for the user external source
   *
   * @param sess perun session
   * @param ues  to get attribute from
   * @param id   id of attribute
   * @return attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException if attribute is not user external source attribute
   * @throws AttributeNotExistsException       if attribute doesn't exist
   */
  Attribute getAttributeById(PerunSession sess, UserExtSource ues, int id)
      throws WrongAttributeAssignmentException, AttributeNotExistsException;

  /**
   * Get attribute definition (attribute without defined value).
   *
   * @param attributeName attribute name defined in the particular manager
   * @return attribute
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  AttributeDefinition getAttributeDefinition(PerunSession sess, String attributeName)
      throws AttributeNotExistsException;

  /**
   * Get attribute definition (attribute without defined value).
   *
   * @param id attribute id
   * @return attribute
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  AttributeDefinition getAttributeDefinitionById(PerunSession sess, int id) throws AttributeNotExistsException;

  /**
   * Get particular attribute for the user for update - locking the attribute for user.
   *
   * @param sess
   * @param user          to get attribute from
   * @param attributeName attribute name defined in the particular manager
   * @return attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  Attribute getAttributeForUpdate(PerunSession sess, User user, String attributeName)
      throws WrongAttributeAssignmentException, AttributeNotExistsException;

  /**
   * Generates graph describing dependencies of attribute modules.
   *
   * @param session session
   * @return graph of dependencies
   */
  Graph getAttributeModulesDependenciesGraph(PerunSession session);

  /**
   * Generates graph describing attribute modules dependencies. Text output format can be specified by
   * {@link GraphTextFormat} format.
   *
   * @param session session
   * @param format  text output format
   * @return body of text file containing description of modules dependencies.
   */
  String getAttributeModulesDependenciesGraphAsString(PerunSession session, GraphTextFormat format);

  /**
   * Generates graph describing dependencies for given AttributeDefinition. Text output format can be specified by
   * {@link GraphTextFormat} format.
   *
   * @param session             session
   * @param format              text output format
   * @param attributeDefinition attribute definition which dependencies will be used
   * @return body of text file containing description of modules dependencies.
   */
  String getAttributeModulesDependenciesGraphAsString(PerunSession session, GraphTextFormat format,
                                                      AttributeDefinition attributeDefinition);

  /**
   * Gets attribute policy collections for an attribute definition with given id.
   *
   * @param sess        perun session
   * @param attributeId id of the attribute definition
   * @return all policy collections of the attribute definition
   * @throws InternalErrorException
   */
  List<AttributePolicyCollection> getAttributePolicyCollections(PerunSession sess, int attributeId);

  /**
   * Gets attribute rules containing policy collections and critical actions for an attribute definition with given id
   *
   * @param sess        perun session
   * @param attributeId id of the attribute definition
   * @return attribute rules of the attribute definition
   */
  AttributeRules getAttributeRules(PerunSession sess, int attributeId);

  /**
   * Get all <b>non-empty</b> attributes associated with the facility.
   *
   * @param sess     perun session
   * @param facility facility to get the attributes from
   * @return list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getAttributes(PerunSession sess, Facility facility);

  /**
   * Get all attributes associated with the facility which have name in list attrNames (empty too). Virtual attribute
   * too.
   *
   * @param sess      perun session
   * @param facility  to get the attributes from
   * @param attrNames list of attributes' names
   * @return list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getAttributes(PerunSession sess, Facility facility, List<String> attrNames);

  /**
   * Get all <b>non-empty</b> attributes associated with the vo.
   *
   * @param sess perun session
   * @param vo   vo to get the attributes from
   * @return list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getAttributes(PerunSession sess, Vo vo);

  /**
   * Get all <b>non-empty</b> attributes associated with the group.
   *
   * @param sess  perun session
   * @param group group to get the attributes from
   * @return list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getAttributes(PerunSession sess, Group group);

  /**
   * Get all <b>non-empty</b> attributes associated with the resource.
   *
   * @param sess     perun session
   * @param resource resource to get the attributes from
   * @return list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getAttributes(PerunSession sess, Resource resource);

  /**
   * Get all <b>non-empty</b> attributes associated with the member on the resource.
   *
   * @param sess     perun session
   * @param member   to get the attributes from
   * @param resource to get the attributes from
   * @return list of attributes
   * @throws InternalErrorException          if an exception raise in concrete implementation, the exception is wrapped
   *                                         in InternalErrorException
   * @throws MemberResourceMismatchException
   */
  List<Attribute> getAttributes(PerunSession sess, Member member, Resource resource)
      throws MemberResourceMismatchException;

  /**
   * Gets all <b>non-empty</b> attributes associated with the member on the resource and if workWithUserAttributes is
   * true, gets also all <b>non-empty</b> user, user-facility and member attributes.
   *
   * @param sess                   perun session
   * @param member                 to get the attributes from
   * @param resource               to get the attributes from
   * @param workWithUserAttributes if true returns also user-facility, user and member attributes (user is automatically
   *                               get from member a facility is get from resource)
   * @return list of attributes
   * @throws InternalErrorException          if an exception raise in concrete implementation, the exception is wrapped
   *                                         in InternalErrorException
   * @throws MemberResourceMismatchException !!WARNING THIS IS VERY TIME-CONSUMING METHOD. DON'T USE IT IN BATCH!!
   */
  List<Attribute> getAttributes(PerunSession sess, Member member, Resource resource, boolean workWithUserAttributes)
      throws MemberResourceMismatchException;

  /**
   * Gets selected <b>non-empty</b> attributes associated with the member and the resource. It returns member and
   * member-resource attributes and also user and user-facility attributes if workWithUserAttributes is true. Attributes
   * are selected by list of attr_names. Empty list means all attributes.
   *
   * @param sess                   perun session
   * @param member                 to get the attributes from
   * @param resource               to get the attributes from
   * @param workWithUserAttributes if true returns also user and user-facility attributes (user is automatically get
   *                               from member and facility is get from resource)
   * @return list of selected attributes
   * @throws InternalErrorException          if an exception raise in concrete implementation, the exception is wrapped
   *                                         in InternalErrorException
   * @throws MemberResourceMismatchException !!WARNING THIS IS VERY TIME-CONSUMING METHOD. DON'T USE IT IN BATCH!!
   */
  List<Attribute> getAttributes(PerunSession sess, Member member, Resource resource, List<String> attrNames,
                                boolean workWithUserAttributes) throws MemberResourceMismatchException;

  /**
   * Gets selected attributes associated with the member, group and the resource. It returns member, member-resource and
   * member-group attributes and also user and user-facility attributes if workWithUserAttributes is true. Attributes
   * are selected by list of attr_names. Empty list means all <b>non-empty</b> attributes.
   *
   * @param sess                   perun session
   * @param group                  group to get the attributes from
   * @param member                 to get the attributes from
   * @param resource               to get the attributes from
   * @param workWithUserAttributes if true returns also user and user-facility attributes (user is automatically get
   *                               from member and facility is get from resource)
   * @return list of selected attributes
   * @throws InternalErrorException          if an exception raise in concrete implementation, the exception is wrapped
   *                                         in InternalErrorException
   * @throws MemberResourceMismatchException
   * @throws GroupResourceMismatchException  !!WARNING THIS IS VERY TIME-CONSUMING METHOD. DON'T USE IT IN BATCH!!
   */
  List<Attribute> getAttributes(PerunSession sess, Group group, Member member, Resource resource,
                                List<String> attrNames, boolean workWithUserAttributes)
      throws MemberResourceMismatchException, GroupResourceMismatchException, MemberGroupMismatchException;

  /**
   * Get all <b>non-empty</b> attributes associated with the member in the group.
   *
   * @param sess   perun session
   * @param member to get the attributes from
   * @param group  group to get the attributes from
   * @return list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getAttributes(PerunSession sess, Member member, Group group) throws MemberGroupMismatchException;

  /**
   * Get all attributes (empty and virtual too) associated with the member in the group which have name in list
   * attrNames.
   *
   * @param sess      perun session
   * @param member    to get the attributes from
   * @param group     group to get the attributes from
   * @param attrNames list of attributes' names
   * @return list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getAttributes(PerunSession sess, Member member, Group group, List<String> attrNames)
      throws MemberGroupMismatchException;

  /**
   * Get all attributes associated with the member in the group and if workWithUserAttributes is true, gets also all
   * <b>non-empty</b> user and member attributes.
   *
   * @param sess                   perun session
   * @param member                 to get the attributes from
   * @param group                  group to get the attributes from
   * @param workWithUserAttributes if true returns also user and member attributes (user is automatically get from
   *                               member)
   * @return list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getAttributes(PerunSession sess, Member member, Group group, boolean workWithUserAttributes)
      throws MemberGroupMismatchException;

  /**
   * Get all attributes associated with the member in the group which have name in list attrNames (empty too). If
   * workWithUserAttribute is true, return also all user attributes in list of attrNames (with virtual attributes too).
   *
   * @param sess                   perun session
   * @param member                 to get the attributes from
   * @param group                  group to get the attributes from
   * @param attrNames              list of attributes' names
   * @param workWithUserAttributes if true returns also user and member attributes (user is automatically get from
   *                               member)
   * @return list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getAttributes(PerunSession sess, Member member, Group group, List<String> attrNames,
                                boolean workWithUserAttributes) throws MemberGroupMismatchException;

  /**
   * Get all <b>non-empty</b> attributes associated with the member and if workWithUserAttributes is true, get all
   * <b>non-empty</b> attributes associated with user, who is this member.
   *
   * @param sess   perun session
   * @param member to get the attributes from
   * @return list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getAttributes(PerunSession sess, Member member, boolean workWithUserAttributes);

  /**
   * Get all <b>non-empty</b> attributes associated with the member.
   *
   * @param sess   perun session
   * @param member to get the attributes from
   * @return list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getAttributes(PerunSession sess, Member member);

  /**
   * Get all attributes by the list of attrNames if they are in one of these namespaces: - member - group - member-group
   * - resource - member-resource - group-resource - user (get from member object) - facility (get from resource object)
   * - user-facility
   * <p>
   * Return all attributes even if they are <b>empty</b> or <b>virtual</b>.
   * <p>
   * PRIVILEGE: Get only those attributes the principal has access to.
   *
   * @param sess      perun session
   * @param group
   * @param resource
   * @param attrNames list of attributes' names
   * @return list of attributes
   * @throws InternalErrorException          if an exception raise in concrete implementation, the exception is wrapped
   *                                         in InternalErrorException
   * @throws GroupResourceMismatchException  if resource and group are not from the same vo
   * @throws MemberResourceMismatchException if member and resource are not from the same vo
   * @parem group
   * @parem member
   */
  List<Attribute> getAttributes(PerunSession sess, Resource resource, Group group, Member member,
                                List<String> attrNames)
      throws GroupResourceMismatchException, MemberResourceMismatchException, MemberGroupMismatchException;

  /**
   * Get all attributes associated with the member which have name in list attrNames (empty too) Virtual attributes
   * too.
   * <p>
   * If workWithUserAttribute is true, return also all user attributes in list of attrNames (with virtual attributes
   * too).
   *
   * @param sess                   perun session
   * @param member                 to get the attributes from
   * @param attrNames              list of attributes' names
   * @param workWithUserAttributes if user attributes need to be return too
   * @return list of member (and also if needed user) attributes
   * @throws InternalErrorException
   */
  List<Attribute> getAttributes(PerunSession sess, Member member, List<String> attrNames,
                                boolean workWithUserAttributes);

  /**
   * Get all attributes associated with the member which have name in list attrNames (empty and virtual too).
   *
   * @param sess      perun session
   * @param member    to get the attributes from
   * @param attrNames list of attributes' names
   * @return list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getAttributes(PerunSession sess, Member member, List<String> attrNames);

  /**
   * Get all attributes associated with the group which have name in list attrNames (empty too). Virtual attribute too.
   *
   * @param sess      perun session
   * @param group     to get the attributes from
   * @param attrNames list of attributes' names
   * @return list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getAttributes(PerunSession sess, Group group, List<String> attrNames);

  /**
   * Get all attributes associated with the resource which have name in list attrNames (empty too). Virtual attribute
   * too.
   *
   * @param sess      perun session
   * @param resource  to get the attributes from
   * @param attrNames list of attributes' names
   * @return list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getAttributes(PerunSession sess, Resource resource, List<String> attrNames);

  /**
   * Get all attributes associated with the vo which have name in list attrNames (empty and virtual too).
   *
   * @param sess      perun session
   * @param vo        to get the attributes from
   * @param attrNames list of attributes' names
   * @return list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wraped in
   *                                InternalErrorException
   */
  List<Attribute> getAttributes(PerunSession sess, Vo vo, List<String> attrNames);

  /**
   * Get all attributes associated with the user which have name in list attrNames (empty and virtual too).
   *
   * @param sess      perun session
   * @param user      to get the attributes from
   * @param attrNames list of attributes' names
   * @return list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getAttributes(PerunSession sess, User user, List<String> attrNames);

  /**
   * Get all attributes associated with the UserExtSource which have name in list attrNames (empty and virtual too).
   *
   * @param sess      perun session
   * @param ues       to get the attributes from
   * @param attrNames list of attributes' names
   * @return list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wraped in
   *                                InternalErrorException
   */
  List<Attribute> getAttributes(PerunSession sess, UserExtSource ues, List<String> attrNames);

  /**
   * Get all <b>non-empty</b> attributes associated with the user on the facility.
   *
   * @param sess     perun session
   * @param facility to get the attributes from
   * @param user     to get the attributes from
   * @return list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getAttributes(PerunSession sess, Facility facility, User user);

  /**
   * Get all <b>non-empty</b> attributes associated with the user.
   *
   * @param sess perun session
   * @param user to get the attributes from
   * @return list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getAttributes(PerunSession sess, User user);

  /**
   * Get all <b>non-empty</b> attributes associated with the host
   *
   * @param sess perun session
   * @param host host to get attributes from
   * @return list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getAttributes(PerunSession sess, Host host);

  /**
   * Get all attributes associated with the host which have name in list attrNames (empty and virtual too). Empty list
   * attrNames will return no attributes.
   *
   * @param sess      perun session
   * @param host      host to get attributes for
   * @param attrNames list of attributes' names
   * @return list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getAttributes(PerunSession sess, Host host, List<String> attrNames) throws InternalErrorException;

  /**
   * Get all <b>non-empty</b> attributes associated with the group on resource.
   *
   * @param sess
   * @param resource
   * @param group
   * @return list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getAttributes(PerunSession sess, Resource resource, Group group);

  List<Attribute> getAttributes(PerunSession sess, Resource resource, Group group, boolean workWithGroupAttributes)
      throws GroupResourceMismatchException;

  /**
   * Get selected attributes associated with the group on resource. Get also empty and virtual attributes if they are
   * selected in the list. If list is empty, return all possible <b>non-empty</b> attributes.
   *
   * @param sess
   * @param resource                the resource
   * @param group                   the group
   * @param workWithGroupAttributes if true, get also group attributes
   * @param attrNames               list of selected attribtues
   * @return list of selected attributes associated with the group on resource
   * @throws InternalErrorException
   * @throws GroupResourceMismatchException
   */
  List<Attribute> getAttributes(PerunSession sess, Resource resource, Group group, List<String> attrNames,
                                boolean workWithGroupAttributes) throws GroupResourceMismatchException;

  /**
   * Get all <b>non-empty</b> member, user, member-resource and user-facility attributes.
   *
   * @param sess
   * @param facility
   * @param resource
   * @param user
   * @param member
   * @return
   * @throws InternalErrorException
   * @throws MemberResourceMismatchException
   */
  List<Attribute> getAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member)
      throws MemberResourceMismatchException;

  /**
   * Get all entityless attributes with subject equaled String key
   *
   * @param sess
   * @param key
   * @return
   * @throws InternalErrorException
   */
  List<Attribute> getAttributes(PerunSession sess, String key);

  /**
   * Get all <b>non-empty</b> attributes associated with the UserExtSource.
   *
   * @param sess perun session
   * @param ues  to get the attributes from
   * @return list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getAttributes(PerunSession sess, UserExtSource ues);

  /**
   * Returns all attributes with not-null value which fits the attributeDefinition. Can't process core or virtual
   * attributes.
   *
   * @param sess
   * @param attributeDefinition can't be core or virtual attribute
   * @return list of attributes
   * @throws InternalErrorException
   */
  List<Attribute> getAttributesByAttributeDefinition(PerunSession sess, AttributeDefinition attributeDefinition)
      throws WrongAttributeAssignmentException;

  /**
   * Get all attributes definition (attribute without defined value).
   *
   * @return List od attributes definitions
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<AttributeDefinition> getAttributesDefinition(PerunSession sess);

  /**
   * From listOfAttributesNames get list of attributeDefinitions
   *
   * @param sess
   * @param listOfAttributesNames
   * @return list of AttributeDefinitions
   * @throws InternalErrorException
   * @throws AttributeNotExistsException
   */
  List<AttributeDefinition> getAttributesDefinition(PerunSession sess, List<String> listOfAttributesNames)
      throws AttributeNotExistsException;

  /**
   * Get attributes definition (attribute without defined value) with specified namespace.
   *
   * @param namespace get only attributes with this namespace
   * @return List of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<AttributeDefinition> getAttributesDefinitionByNamespace(PerunSession sess, String namespace);

  /**
   * Get all (for entities) attributeDefinitions which user has right to READ them and fill attribute writable (if user
   * has also right to WRITE them). For entities means that return only those attributeDefinition which are in namespace
   * of entities or possible combination of entities. For Example: If enityties are "member, user, resource" then return
   * only AD in namespaces "member, user, resource and resource-member"
   *
   * @param sess
   * @param entities list of perunBeans (member, user...)
   * @return list of AttributeDefinitions with rights (writable will be filled correctly by user in session)
   * @throws InternalErrorException
   */
  List<AttributeDefinition> getAttributesDefinitionWithRights(PerunSession sess, List<PerunBean> entities);

  /**
   * Returns critical actions on given attribute.
   *
   * @param sess   session
   * @param attrId attribute definition id
   * @return list of critical actions
   */
  List<AttributeAction> getCriticalAttributeActions(PerunSession sess, int attrId);

  /**
   * Returns entityless attribute by attr_id and key (subject) for update!
   * <p>
   * For update - means lock row with attr_values in DB (entityless_attr_values with specific subject and attr_id)
   * <p>
   * Not lock row in attr_names!
   * <p>
   * IMPORTANT: This method use "select for update" and locks row for transaction. Use clever.
   * <p>
   * If attribute with subject=key not exists, create new one with null value and return it.
   *
   * @param sess
   * @param attrName
   * @param key
   * @return attr_value in string
   * @throws InternalErrorException      if runtime error exception has been thrown
   * @throws AttributeNotExistsException throw exception if attribute with value not exists in DB
   */
  Attribute getEntitylessAttributeForUpdate(PerunSession sess, String key, String attrName)
      throws AttributeNotExistsException;

  /**
   * Get all entityless attributes with attributeName
   *
   * @param sess     perun session
   * @param attrName
   * @return attribute
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getEntitylessAttributes(PerunSession sess, String attrName);

  /**
   * Get entityless attributes mapped by their keys.
   *
   * @param sess     session
   * @param attrName attribute name
   * @return Map of entityless attributes mapped by their keys
   * @throws AttributeNotExistsException       when the attribute definition for attrName doesn't exist
   * @throws WrongAttributeAssignmentException when passed non-entityless attribute
   */
  Map<String, Attribute> getEntitylessAttributesWithKeys(PerunSession sess, String attrName)
      throws AttributeNotExistsException, WrongAttributeAssignmentException;

  /**
   * Get entityless attributes mapped by their keys. Returns only attributes for specified keys.
   *
   * @param sess     session
   * @param attrName attribute name
   * @return Map of entityless attributes mapped by their keys
   * @throws AttributeNotExistsException       when the attribute definition for attrName doesn't exist, or when there
   *                                           is no such attribute for one of the specified keys
   * @throws WrongAttributeAssignmentException when passed non-entityless attribute
   */
  Map<String, Attribute> getEntitylessAttributesWithKeys(PerunSession sess, String attrName, List<String> keys)
      throws AttributeNotExistsException, WrongAttributeAssignmentException;

  /**
   * Returns list of Keys which fits the attributeDefinition.
   *
   * @param sess
   * @param attributeDefinition
   * @return
   * @throws InternalErrorException
   */
  List<String> getEntitylessKeys(PerunSession sess, AttributeDefinition attributeDefinition);

  /**
   * Gets map from keys to string values for an entityless attribute.
   *
   * @param sess          session
   * @param attributeName full attribute name
   * @return unordered hashmap
   */
  Map<String, String> getEntitylessStringAttributeMapping(PerunSession sess, String attributeName)
      throws WrongAttributeAssignmentException, AttributeNotExistsException;

  /**
   * Gets the friendly name from the attribute name.
   *
   * @param attributeName
   * @return the friendly name from the attribute name
   */
  String getFriendlyNameFromAttributeName(String attributeName);

  /**
   * Returns list of definitions of IdP attributes that are filled to fedInfo
   *
   * @param sess session
   * @return list of attribute definitions
   */
  List<AttributeDefinition> getIdpAttributeDefinitions(PerunSession sess);

  /**
   * Get all <b>non-empty</b> attributes with user's logins.
   *
   * @param sess
   * @param user
   * @return list of attributes with login
   * @throws InternalErrorException
   */
  List<Attribute> getLogins(PerunSession sess, User user);

  /**
   * Gets the namespace from the attribute name.
   *
   * @param attributeName
   * @return the namespace from the attribute name
   */
  String getNamespaceFromAttributeName(String attributeName);

  /**
   * Finds ids of PerunBeans that have the attribute's value for the attribute. The attribute must be marked as unique.
   * <p>
   * This method is intended for finding whether a unique value is already assigned, and if yes, then whether it is the
   * same object which is being updated or some other object. This is typically needed in checkAttributeSemantics()
   * method of attribute modules.
   * <p>
   * The return type is a set of pairs of ids. It is a set because for collection types (ArrayList and LinkedHashMap)
   * each of the entries in the collection may be assigned to a different object. The set contains pairs, because some
   * attribute types are attached to a pair of PerunBeans (e.g. group_resource) and some are attached to single beans
   * (e.g. group).
   * <p>
   * If the attribute is attached to a pair of beans, the returned Pairs contain ids of the objects in the order of
   * beans as listed in the attribute namespace, e.g. for group_resource attribute the left part of the pair contains
   * group id, and the right part contains resource id. If the attribute is attached to a single bean, the left part of
   * the pair contains the id, and the right part contains zero.
   * <p>
   * For simple value types (String, Integer, Boolean), this methods returns either an empty set (if the simple value is
   * not assigned yet), or a set containing a single Pair. For collection values types (ArrayList, LinkedHashMap), this
   * method returns an empty set, or a set with one or more pairs.
   *
   * @param sess      session
   * @param attribute attribute with a filled value that will be checked for uniqueness
   * @return a Set of Pairs with ids of PerunBeans that have the attribute value
   */
  Set<Pair<Integer, Integer>> getPerunBeanIdsForUniqueAttributeValue(PerunSession sess, Attribute attribute);

  /**
   * Get facility attributes which are required by all services which are connected to this facility.
   *
   * @param sess     perun session
   * @param facility you get attributes for this facility
   * @return list of facility attributes which are required by all services which are connected to this facility.
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getRequiredAttributes(PerunSession sess, Facility facility);

  /**
   * Get resource attributes which are required by selected services.
   *
   * @param sess     perun session
   * @param resource you get attributes for this resource
   * @param services
   * @return list of resource attributes which are required by selected services.
   * @throws InternalErrorException
   */
  List<Attribute> getRequiredAttributes(PerunSession sess, List<Service> services, Resource resource);

  /**
   * Get resource attributes which are required by services which is related to this resource.
   *
   * @param sess     perun session
   * @param resource resource for which you want to get the attributes
   * @return list of resource attributes which are required by services which are assigned to resource.
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getRequiredAttributes(PerunSession sess, Resource resource);

  /**
   * Get member-resource attributes which are required by services which are relater to this member-resource.
   *
   * @param sess     perun session
   * @param member   you get attributes for this member and the resource
   * @param resource you get attributes for this resource and the member
   * @return list of facility attributes which are required by services which are assigned to resource.
   * @throws InternalErrorException          if an exception raise in concrete implementation, the exception is wrapped
   *                                         in InternalErrorException
   * @throws MemberResourceMismatchException
   */
  List<Attribute> getRequiredAttributes(PerunSession sess, Member member, Resource resource)
      throws MemberResourceMismatchException;

  /**
   * If workWithUserAttribute is false => Get member-resource attributes which are required by services which are
   * relater to this member-resource. If workWithUserAttributes is true => Get member-resource, user-facility, user and
   * member attributes. (user is get from member and facility from resource)
   *
   * @param sess                   perun session
   * @param member                 you get attributes for this member and the resource
   * @param resource               you get attributes for this resource and the member
   * @param workWithUserAttributes method can process also user, user-facility and member attributes (user is
   *                               automatically get from member a facility is get from resource)
   * @return list of member-resource attributes or if workWithUserAttributes is true return list of member-resource,
   * user, member and user-facility attributes
   * @throws InternalErrorException          if an exception raise in concrete implementation, the exception is wrapped
   *                                         in InternalErrorException
   * @throws MemberResourceMismatchException !!WARNING THIS IS VERY TIME-CONSUMING METHOD. DON'T USE IT IN BATCH!!
   */
  List<Attribute> getRequiredAttributes(PerunSession sess, Member member, Resource resource,
                                        boolean workWithUserAttributes) throws MemberResourceMismatchException;

  /**
   * Get user-facility attributes which are required by services which are related to this user-facility.
   *
   * @param sess     perun session
   * @param facility facility from which the services are taken
   * @param user     you get attributes for this user
   * @return list of facility attributes which are required by services which are assigned to facility.
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getRequiredAttributes(PerunSession sess, Facility facility, User user);

  /**
   * Get user attributes which are required by services which are relater to this user.
   *
   * @param sess perun session
   * @param user
   * @return list of user's attributes which are required by services which are related to this user
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getRequiredAttributes(PerunSession sess, User user);

  /**
   * Get member attributes which are required by services which are relater to this member and if is
   * workWithUserAttributes = true, then also user required attributes
   *
   * @param sess                   perun session
   * @param member                 you get attributes for this member
   * @param workWithUserAttributes method can process also user and user-facility attributes (user is automatically get
   *                               from member a facility is get from resource)
   * @return list of member, user attributes which are required by services which are related to this member
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getRequiredAttributes(PerunSession sess, Member member, boolean workWithUserAttributes);

  /**
   * Get member, member-group attributes which are required by services which are related to this member and group. If
   * workWithUserAttributes = TRUE, then also user attributes are returned.
   *
   * @param sess                   perun session
   * @param member                 you get attributes for this member
   * @param group                  you get attribute for this group
   * @param workWithUserAttributes method can process also user if this is TRUE
   * @return list of attributes which are required by services which are related to this member and group
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getRequiredAttributes(PerunSession sess, Member member, Group group, boolean workWithUserAttributes)
      throws MemberGroupMismatchException;

  /**
   * Get facility attributes which are required by the service.
   *
   * @param sess     perun session
   * @param facility you get attributes for this facility
   * @param service  attribute required by this service you'll get
   * @return list of facility attributes which are required by the service
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Facility facility);

  /**
   * Get vo attributes which are required by the service.
   *
   * @param sess    perun session
   * @param vo      you get attributes for this vo
   * @param service attribute required by this service you'll get
   * @return list of vo attributes which are required by the service
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Vo vo);

  /**
   * Get resource attributes which are required by the service.
   *
   * @param sess     perun session
   * @param resource resource for which you want to get the attributes
   * @param service  attribute required by this service you'll get
   * @return list of resource attributes which are required by the service
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource);

  /**
   * Get member-resource attributes which are required by the service.
   *
   * @param sess     perun session
   * @param service  attribute required by this service you'll get
   * @param member   you get attributes for this member and the resource
   * @param resource you get attributes for this resource and the member
   * @return list of attributes which are required by the service.
   * @throws InternalErrorException          if an exception raise in concrete implementation, the exception is wrapped
   *                                         in InternalErrorException
   * @throws MemberResourceMismatchException
   */
  List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member, Resource resource)
      throws MemberResourceMismatchException;

  List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member, Resource resource,
                                        boolean workWithUserAttributes) throws MemberResourceMismatchException;

  /**
   * Get member, member-resource and member-group attributes which are required by the service. if
   * workWithUserAttributes == TRUE return also user and user-facility attributes
   *
   * @param sess                   perun session
   * @param resource               you get attributes for this resource and the member and group
   * @param group                  you get attributes for this group and resource and member
   * @param member                 you get attributes for this member and the resource and group
   * @param service                attribute required by this service you'll get
   * @param workWithUserAttributes if TRUE also user and user-facility attributes
   * @return list of attributes which are required by the service.
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Group group,
                                        Member member, boolean workWithUserAttributes)
      throws MemberResourceMismatchException, GroupResourceMismatchException;

  /**
   * Get member-resource, member, user-facility and user attributes which are required by service for each member in
   * list of members. If workWithUserAttributes is false return only member-resource attributes. !!! Method checks if
   * members list is not empty (returns empty HashMap)!!!
   *
   * @param sess                   perun session
   * @param service                attribute required by this service
   * @param resource               you get attributes for this resource
   * @param members                you get attributes for this list of members
   * @param workWithUserAttributes if true method can process also user, user-facility and member attributes
   * @return map of member objects and his list of attributes
   * @throws InternalErrorException          if an exception raise in concrete implementation, the exception is wrapped
   *                                         in InternalErrorException
   * @throws MemberResourceMismatchException if methods checkMemberIsFromTheSameVoLikeResource finds that user is not
   *                                         from same vo like resource
   */
  HashMap<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, Facility facility,
                                                         Resource resource, List<Member> members,
                                                         boolean workWithUserAttributes)
      throws MemberResourceMismatchException;

  /**
   * Get member-resource attributes which are required by service for each member in list of members. !!! Method checks
   * if members list is not empty (returns empty HashMap)!!!
   *
   * @param sess     perun session
   * @param service  attribute required by this service
   * @param resource you get attributes for this resource and the members
   * @param members  you get attributes for this list of members and the resource
   * @return map of member objects and his list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  HashMap<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, Resource resource,
                                                         List<Member> members);

  /**
   * Get member attributes which are required by service for each member in list of members. !!! Method checks if
   * members list is not empty (returns empty HashMap)!!!
   *
   * @param sess     perun session
   * @param service  attribute required by this service
   * @param resource resource only to get allowed members
   * @param members  you get attributes for this list of members
   * @return map of member objects and his list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  HashMap<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Resource resource, Service service,
                                                         List<Member> members);

  /**
   * Get user-facility attributes which are required by the service for each user in list of users. !!! Method checks if
   * members list is not empty (returns empty HashMap)!!!
   *
   * @param sess     perun session
   * @param service  attribute required by this service
   * @param facility you get attributes for this facility and user
   * @param users    you get attributes for this user and facility
   * @return map of user and his list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  HashMap<User, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, Facility facility,
                                                       List<User> users);

  /**
   * Get user attributes which are required by the service for each user in list of users. !!! Method checks if members
   * list is not empty (returns empty HashMap)!!!
   *
   * @param sess    perun session
   * @param service attribute required by this service
   * @param users   you get attributes for this user and facility
   * @return map of user and his list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  HashMap<User, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, List<User> users);

  /**
   * Get member-group attributes which are required by the service.
   *
   * @param sess    perun session
   * @param member  you get attributes for this member and the group
   * @param group   you get attributes for this group in which member is associated
   * @param service attribute required by this service you'll get
   * @return list of attributes which are required by the service.
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member, Group group)
      throws MemberGroupMismatchException;

  /**
   * Get member-group attributes which are required by the service, for the given members and the given group.
   *
   * @param sess    session
   * @param service service
   * @param members members
   * @param group   group
   * @return Member-Group attributes grouped by members
   * @throws MemberGroupMismatchException if some of the given members is not from the same vo as the given group
   */
  Map<Member, List<Attribute>> getRequiredAttributes(PerunSession sess, Service service, List<Member> members,
                                                     Group group) throws MemberGroupMismatchException;

  List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member, Group group,
                                        boolean workWithUserAttributes) throws MemberGroupMismatchException;

  /**
   * Get memner, user, member-resource, user-facility attributes which are required by the service.
   *
   * @param sess
   * @param service
   * @param facility
   * @param resource
   * @param user
   * @param member
   * @return
   * @throws InternalErrorException
   * @throws MemberResourceMismatchException
   */
  List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Facility facility, Resource resource,
                                        User user, Member member) throws MemberResourceMismatchException;

  /**
   * Get member, user, member-resource, user-facility attributes which are required by the services. The same attributes
   * are returned only once.
   *
   * @param sess     session
   * @param services services
   * @param facility facility
   * @param resource resource
   * @param user     user
   * @param member   member
   * @return List of member, user, member-resource and user-facility attributes required by given services
   * @throws MemberResourceMismatchException member resource mismatch exception
   */
  List<Attribute> getRequiredAttributes(PerunSession sess, List<Service> services, Facility facility, Resource resource,
                                        User user, Member member) throws MemberResourceMismatchException;

  /**
   * Get group-resource attributes which are required by the service.
   *
   * @param sess                perun session
   * @param resource
   * @param group
   * @param service             attribute required by this service you'll get
   * @param withGroupAttributes get also group attributes (which is required by the service) for this group
   * @return list of attributes which are required by the service.
   * @throws InternalErrorException         if an exception raise in concrete implementation, the exception is wrapped
   *                                        in InternalErrorException
   * @throws GroupResourceMismatchException
   */
  List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Group group,
                                        boolean withGroupAttributes) throws GroupResourceMismatchException;

  /**
   * Get group-resource attributes which are required by the services.
   *
   * @param sess                session
   * @param services            services
   * @param resource            resource
   * @param group               group
   * @param withGroupAttributes get also group attributes (which is required by the service) for this group
   * @return list of attributes which are required by the service.
   * @throws GroupResourceMismatchException group resource mismatch exception
   */
  List<Attribute> getRequiredAttributes(PerunSession sess, List<Service> services, Resource resource, Group group,
                                        boolean withGroupAttributes) throws GroupResourceMismatchException;

  List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Resource resource, Group group)
      throws GroupResourceMismatchException;

  List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Group group);

  /**
   * Get host attributes which are required by service
   *
   * @param sess
   * @param service
   * @param host
   * @return
   * @throws InternalErrorException
   */
  List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Host host);

  /**
   * Get member attributes which are required by the service.
   *
   * @param sess    perun session
   * @param member  you get attributes for this member
   * @param service attribute required by this service you'll get
   * @return list of attributes which are required by the service.
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Member member);

  /**
   * Get user attributes which are required by the service.
   *
   * @param sess    perun session
   * @param user    you get attributes for this user
   * @param service attribute required by this service you'll get
   * @return list of attributes which are required by the service.
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getRequiredAttributes(PerunSession sess, Service service, User user);

  /**
   * Get user-facility attributes which are required by the service.
   *
   * @param sess     perun session
   * @param user     you get attributes for this user and the facility
   * @param facility you get attributes for this facility and the user
   * @param service  attribute required by this service you'll get
   * @return list of attributes which are required by the service.
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getRequiredAttributes(PerunSession sess, Service service, Facility facility, User user);

  /**
   * Get all attributes which are required by service. Required attributes are requisite for Service to run.
   *
   * @param sess    sess
   * @param service service from which the attributes will be listed
   * @return All attributes which are required by service.
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<AttributeDefinition> getRequiredAttributesDefinition(PerunSession sess, Service service);

  /**
   * Get group attributes which are required by the given service for given groups.
   *
   * @param sess    session
   * @param service service for which are taken the required attributes
   * @param groups  groups
   * @return attributes mapped by their groups
   */
  Map<Group, List<Attribute>> getRequiredAttributesForGroups(PerunSession sess, Service service, List<Group> groups);

  /**
   * Get facility attributes which are required by services. Services are known from the resource.
   *
   * @param sess                      perun session
   * @param resourceToGetServicesFrom resource from which the services are taken
   * @param facility                  you get attributes for this facility
   * @return list of facility attributes which are required by services which are assigned to resource
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom,
                                                Facility facility);

  /**
   * Get resource attributes which are required by services. Services are known from the resourceToGetServicesFrom.
   *
   * @param sess                      perun session
   * @param resourceToGetServicesFrom resource from which the services are taken
   * @param resource                  resource from which the services are taken and for which you want to get the
   *                                  attributes
   * @return list of resource attributes which are required by services which are assigned to resource.
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom,
                                                Resource resource);

  /**
   * Get member-resource attributes which are required by services. Services are known from the
   * resourceToGetServicesFrom.
   *
   * @param sess                      perun session
   * @param resourceToGetServicesFrom resource from which the services are taken
   * @param member                    you get attributes for this member
   * @param resource                  you get attributes for this resource and the member
   * @return list of facility attributes which are required by services which are assigned to resource.
   * @throws InternalErrorException          if an exception raise in concrete implementation, the exception is wrapped
   *                                         in InternalErrorException
   * @throws MemberResourceMismatchException
   */
  List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member,
                                                Resource resource) throws MemberResourceMismatchException;

  /**
   * Get member-resource attributes which are required by services and if workWithUserAttributes is true also user,
   * user-facility and member attributes. Services are known from the resourceToGetServicesFrom.
   *
   * @param sess                      perun session
   * @param resourceToGetServicesFrom getRequired attributes from services which are assigned on this resource
   * @param member                    you get attributes for this member and the resource
   * @param resource                  you get attributes for this resource and the member
   * @param workWithUserAttributes    method can process also user, user-facility and member attributes (user is
   *                                  automatically get from member a facility is get from resource)
   * @return list of member-resource attributes (if workWithUserAttributes is true also user, user-facility and member
   * attributes) which are required by services which are assigned to another resource.
   * @throws InternalErrorException          if an exception raise in concrete implementation, the exception is wrapped
   *                                         in InternalErrorException
   * @throws MemberResourceMismatchException !!WARNING THIS IS VERY TIME-CONSUMING METHOD. DON'T USE IT IN BATCH!!
   */
  List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member,
                                                Resource resource, boolean workWithUserAttributes)
      throws MemberResourceMismatchException;

  /**
   * Get member-group attributes which are required by services. Services are known from the resourceToGetServicesFrom.
   *
   * @param sess                      perun session
   * @param resourceToGetServicesFrom resource from which the services are taken
   * @param member                    you get attributes for this member
   * @param group                     you get attributes for this group and the member
   * @return list of member-group's attributes which are required by services defined on specified resource
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member,
                                                Group group) throws MemberGroupMismatchException;

  /**
   * Get member-group attributes which are required by services if workWithUserAttributes is true also user and member
   * attributes. Services are known from the resourceToGetServicesFrom.
   *
   * @param sess                      perun session
   * @param resourceToGetServicesFrom resource from which the services are taken
   * @param member                    you get attributes for this member
   * @param group                     you get attributes for this group and the member
   * @param workWithUserAttributes    method can process also user and member attributes (user is automatically get from
   *                                  member)
   * @return list of member-group's attributes which are required by services defined on specified resource
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member,
                                                Group group, boolean workWithUserAttributes)
      throws MemberGroupMismatchException;

  /**
   * Get member, user, member-resource and user-facility attributes which are required by services which are defined on
   * "resourceToGetServicesFrom" resource.
   *
   * @param sess                      perun session
   * @param resourceToGetServicesFrom getRequired attributes from services which are assigned on this resource
   * @param facility
   * @param resource                  you get attributes for this resource and the member
   * @param user
   * @param member                    you get attributes for this member and the resource
   * @return list of member-resource attributes which are required by services which are assigned to specified resource
   * @throws InternalErrorException          if an exception raise in concrete implementation, the exception is wrapped
   *                                         in InternalErrorException
   * @throws MemberResourceMismatchException
   */
  List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom,
                                                Facility facility, Resource resource, User user, Member member)
      throws MemberResourceMismatchException;

  /**
   * Get member attributes which are required by services defined on specified resource
   *
   * @param sess                      perun session
   * @param member                    you get attributes for this member
   * @param resourceToGetServicesFrom getRequired attributes from services which are assigned on this resource
   * @return list of member attributes which are required by services defined on specified resource
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Member member);

  /**
   * Get user-facility attributes which are required by services. Services are known from the resource.
   *
   * @param sess                      perun session
   * @param resourceToGetServicesFrom resource from which the services are taken
   * @param facility                  facility from which the services are taken
   * @param user                      you get attributes for this user
   * @return list of user-facility attributes which are required by service
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom,
                                                Facility facility, User user);

  /**
   * Get user attributes which are required by services. Services are known from the resource.
   *
   * @param sess                      perun session
   * @param resourceToGetServicesFrom resource from which the services are taken
   * @param user                      you get attributes for this user
   * @return list of users attributes which are required by service
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, User user);

  /**
   * Get the group-resource attributes which are required by services. Services are known from the resource.
   *
   * @param sess
   * @param resourceToGetServicesFrom resource from which the services are taken
   * @param resource
   * @param group
   * @return
   * @throws InternalErrorException
   * @throws GroupResourceMismatchException
   */
  List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom,
                                                Resource resource, Group group);

  List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom,
                                                Resource resource, Group group, boolean workWithGroupAttributes)
      throws GroupResourceMismatchException;

  /**
   * Get the host attributes which are required by services. Services are known from the resource.
   *
   * @param sess
   * @param resourceToGetServicesFrom
   * @param host
   * @return
   * @throws InternalErrorException
   */
  List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Host host);

  List<Attribute> getResourceRequiredAttributes(PerunSession sess, Resource resourceToGetServicesFrom, Group group);

  /**
   * Get attributes definions required by all services assigned on the resource.
   *
   * @param sess     perun session
   * @param resource
   * @return attributes definions required by all services assigned on the resource.
   * @throws InternalErrorException
   */
  List<AttributeDefinition> getResourceRequiredAttributesDefinition(PerunSession sess, Resource resource);

  /**
   * Method get attribute Definition attrDef and aidingAttr which only holds one or two useful objects in holders.
   * Thanks useful objects, method find all possibly richAttributes which can be get on specific attrDef with all
   * existing combinations of needed objects.
   * <p>
   * Example: I am looking for Member attrDef and I have primaryHolder: User. So i will find all members of this user
   * and return all richAttributes of combination attribute + specific member in primaryHolder.
   *
   * @param sess
   * @param attrDef
   * @param aidingAttr
   * @return
   * @throws InternalErrorException
   * @throws AttributeNotExistsException
   * @throws VoNotExistsException
   * @throws UserNotExistsException
   * @throws WrongAttributeAssignmentException
   * @throws MemberResourceMismatchException
   * @throws GroupResourceMismatchException
   */
  List<RichAttribute> getRichAttributesWithHoldersForAttributeDefinition(PerunSession sess, AttributeDefinition attrDef,
                                                                         RichAttribute aidingAttr)
      throws AttributeNotExistsException, VoNotExistsException, UserNotExistsException,
                 WrongAttributeAssignmentException, GroupResourceMismatchException, MemberResourceMismatchException,
                 MemberGroupMismatchException;

  /**
   * Get All user_facility attributes for any existing user
   *
   * @param sess
   * @param facility
   * @return list of user facility attributes
   * @throws InternalErrorException
   */
  List<Attribute> getUserFacilityAttributesForAnyUser(PerunSession sess, Facility facility);

  /**
   * Gets IDs of users who use the given login in any namespace.
   *
   * @param sess  perun session
   * @param login login
   * @return list of user IDs
   */
  List<Integer> getUserIdsByLogin(PerunSession sess, String login);

  /**
   * Get user virtual attribute module by the attribute.
   *
   * @param sess
   * @param attribute attribute for which you get the module
   * @return instance of user attribute module
   * @throws InternalErrorException
   * @throws WrongModuleTypeException
   * @throws ModuleNotExistsException
   */
  UserVirtualAttributesModuleImplApi getUserVirtualAttributeModule(PerunSession sess, AttributeDefinition attribute);

  /**
   * Get all virtual attributes associated with the member-resource attributes.
   *
   * @param sess     perun session
   * @param member   to get the attributes from
   * @param resource to get the attributes from
   * @return list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getVirtualAttributes(PerunSession sess, Member member, Resource resource);

  /**
   * Get all virtual attributes associated with the user on the facility.
   *
   * @param sess     perun session
   * @param facility to get the attributes from
   * @param user     to get the attributes from
   * @return list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getVirtualAttributes(PerunSession sess, Facility facility, User user);

  /**
   * Get all virtual attributes associated with the user.
   *
   * @param sess perun session
   * @param user to get the attributes from
   * @return list of attributes
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  List<Attribute> getVirtualAttributes(PerunSession sess, User user);

  /**
   * Checks if the action is critical on given attribute.
   *
   * @param sess   session
   * @param attr   attribute definition
   * @param action critical action
   * @return true if action is critical, false otherwise
   */
  boolean isAttributeActionCritical(PerunSession sess, AttributeDefinition attr, AttributeAction action);

  /**
   * Checks whether the value of the passed attribute (checks separate key-value pairs and list items as well) is
   * blocked (in namespace if the attribute is namespace based). Throws exception with details if blocked
   *
   * @param session
   * @param attribute
   * @return pair, the left being a boolean - true if value blocked, right being a string providing further information,
   * mainly for the complex attribute types (e.g. which key-value pair is blocked, etc.)
   */
  Pair<Boolean, String> isAttributeValueBlocked(PerunSession session, Attribute attribute);

  /**
   * Checks if the action is critical on given attribute for all objects.
   *
   * @param sess   session
   * @param attr   attribute definition
   * @param action critical action
   * @return true if action is globally critical, false otherwise
   */
  boolean isAttributeActionGloballyCritical(PerunSession sess, AttributeDefinition attr, AttributeAction action);

  /**
   * Determine if attribute is core attribute.
   *
   * @param sess
   * @param attribute
   * @return true if attribute is core attribute
   */
  boolean isCoreAttribute(PerunSession sess, AttributeDefinition attribute);

  /**
   * Determine if attribute is core-managed attribute.
   *
   * @param sess
   * @param attribute
   * @return true if attribute is core-managed
   */
  boolean isCoreManagedAttribute(PerunSession sess, AttributeDefinition attribute);

  /**
   * Determine if attribute is defined (def) attribute.
   *
   * @param sess
   * @param attribute
   * @return true if attribute is defined attribute false otherwise
   */
  boolean isDefAttribute(PerunSession sess, AttributeDefinition attribute);

  /**
   * Determine if attribute is from specified namespace.
   *
   * @param sess
   * @param attribute
   * @param namespace
   * @return true if the attribute is from specified namespace false otherwise
   */
  boolean isFromNamespace(PerunSession sess, AttributeDefinition attribute, String namespace);

  /**
   * Check if the login is already in use for some namespace or globally
   *
   * @param sess      perun session
   * @param login     login to be checked
   * @param namespace login can be checked for given namespace or globally, if namespace is null
   * @return true if login is already in use
   */
  boolean isLoginAlreadyUsed(PerunSession sess, String login, String namespace);

  /**
   * Determine if attribute is optional (opt) attribute.
   *
   * @param sess
   * @param attribute
   * @return true if attribute is optional attribute false otherwise
   */
  boolean isOptAttribute(PerunSession sess, AttributeDefinition attribute);

  /**
   * Check if this the attribute is truly required for the facility right now. Truly means that the nothing (member,
   * resource...) is invalid.
   *
   * @param sess
   * @param facility
   * @param attributeDefinition
   * @return
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  boolean isTrulyRequiredAttribute(PerunSession sess, Facility facility, AttributeDefinition attributeDefinition)
      throws WrongAttributeAssignmentException;

  /**
   * Check if this the attribute is truly required for the resource right now. Truly means that the nothing (member,
   * resource...) is invalid.
   *
   * @param sess
   * @param resource
   * @param attributeDefinition
   * @return
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  boolean isTrulyRequiredAttribute(PerunSession sess, Resource resource, AttributeDefinition attributeDefinition)
      throws WrongAttributeAssignmentException;

  /**
   * Check if this the attribute is truly required for the member right now. Truly means that the nothing (member,
   * resource...) is invalid.
   *
   * @param sess
   * @param member
   * @param attributeDefinition
   * @return
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  boolean isTrulyRequiredAttribute(PerunSession sess, Member member, AttributeDefinition attributeDefinition)
      throws WrongAttributeAssignmentException;

  /**
   * Check if this the attribute is truly required for the user right now. Truly means that the nothing (member,
   * resource...) is invalid.
   *
   * @param sess
   * @param user
   * @param attributeDefinition
   * @return
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  boolean isTrulyRequiredAttribute(PerunSession sess, User user, AttributeDefinition attributeDefinition)
      throws WrongAttributeAssignmentException;

  /**
   * Check if this the attribute is truly required for the user and the facility right now. Truly means that the nothing
   * (member, resource...) is invalid.
   *
   * @param sess
   * @param facility
   * @param user
   * @param attributeDefinition
   * @return
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  boolean isTrulyRequiredAttribute(PerunSession sess, Facility facility, User user,
                                   AttributeDefinition attributeDefinition) throws WrongAttributeAssignmentException;

  /**
   * Check if this the attribute is truly required for the member and the resource right now. Truly means that the
   * nothing (member, resource...) is invalid.
   *
   * @param sess
   * @param member
   * @param resource
   * @param attributeDefinition
   * @return
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws MemberResourceMismatchException
   */
  boolean isTrulyRequiredAttribute(PerunSession sess, Member member, Resource resource,
                                   AttributeDefinition attributeDefinition)
      throws WrongAttributeAssignmentException, MemberResourceMismatchException;

  /**
   * Check if this the attribute is truly required for the member and the group right now. Truly means that the nothing
   * (member, group...) is invalid.
   *
   * @param sess
   * @param member
   * @param group
   * @param attributeDefinition
   * @return
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  boolean isTrulyRequiredAttribute(PerunSession sess, Member member, Group group,
                                   AttributeDefinition attributeDefinition)
      throws WrongAttributeAssignmentException, MemberGroupMismatchException;

  /**
   * Determine if attribute is virtual (virt) attribute.
   *
   * @param sess
   * @param attribute
   * @return true if attribute is virtual attribute false otherwise
   */
  boolean isVirtAttribute(PerunSession sess, AttributeDefinition attribute);

  /**
   * Merges attribute value if the attribute type is list or map. In other cases it only stores new value. If the type
   * is list, new values are added to the current stored list. It the type is map, new values are added and existing are
   * overwritten with new values, but only if there is some change.
   *
   * @param sess
   * @param user
   * @param attribute
   * @return attribute with updated value
   * @throws InternalErrorException
   * @throws WrongAttributeValueException
   * @throws WrongReferenceAttributeValueException
   * @throws WrongAttributeAssignmentException
   */
  Attribute mergeAttributeValue(PerunSession sess, User user, Attribute attribute)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException;

  /**
   * Merges attribute value if the attribute type is list or map. In other cases it only stores new value. If the type
   * is list, new values are added to the current stored list. It the type is map, new values are added and existing are
   * overwritten with new values, but only if there is some change.
   *
   * @param sess
   * @param member
   * @param attribute
   * @return attribute with updated value
   * @throws InternalErrorException
   * @throws WrongAttributeValueException
   * @throws WrongReferenceAttributeValueException
   * @throws WrongAttributeAssignmentException
   */
  Attribute mergeAttributeValue(PerunSession sess, Member member, Attribute attribute)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException;

  /**
   * Merges attribute value if the attribute type is list or map. In other cases it only stores new value. If the type
   * is list, new values are added to the current stored list. It the type is map, new values are added and existing are
   * overwritten with new values, but only if there is some change.
   *
   * @param sess
   * @param group
   * @param attribute
   * @return attribute with updated value
   * @throws InternalErrorException
   * @throws WrongAttributeValueException
   * @throws WrongReferenceAttributeValueException
   * @throws WrongAttributeAssignmentException
   */
  Attribute mergeAttributeValue(PerunSession sess, Group group, Attribute attribute)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException;

  /**
   * Merges attribute value if the attribute type is list or map. In other cases it only stores new value. If the type
   * is list, new values are added to the current stored list. It the type is map, new values are added and existing are
   * overwritten with new values, but only if there is some change.
   * <p>
   * This method creates nested transaction to prevent storing value to DB if it throws any exception.
   *
   * @param sess
   * @param user
   * @param attribute
   * @return attribute with updated value
   * @throws InternalErrorException
   * @throws WrongAttributeValueException
   * @throws WrongReferenceAttributeValueException
   * @throws WrongAttributeAssignmentException
   */
  Attribute mergeAttributeValueInNestedTransaction(PerunSession sess, User user, Attribute attribute)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException;

  /**
   * Merges attribute value if the attribute type is list or map. In other cases it only stores new value. If the type
   * is list, new values are added to the current stored list. It the type is map, new values are added and existing are
   * overwritten with new values, but only if there is some change.
   * <p>
   * This method creates nested transaction to prevent storing value to DB if it throws any exception.
   *
   * @param sess
   * @param member
   * @param attribute
   * @return attribute with updated value
   * @throws InternalErrorException
   * @throws WrongAttributeValueException
   * @throws WrongReferenceAttributeValueException
   * @throws WrongAttributeAssignmentException
   */
  Attribute mergeAttributeValueInNestedTransaction(PerunSession sess, Member member, Attribute attribute)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException;

  /**
   * Merges attributes values if the attribute type is list or map. In other cases it only stores new value. If the type
   * is list, new values are added to the current stored list. It the type is map, new values are added and existing are
   * overwritten with new values, but only if there is some change.
   *
   * @param sess
   * @param user
   * @param attributes
   * @throws InternalErrorException
   * @throws WrongAttributeValueException
   * @throws WrongReferenceAttributeValueException
   * @throws WrongAttributeAssignmentException
   */
  void mergeAttributesValues(PerunSession sess, User user, List<Attribute> attributes)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException;

  /**
   * Merges attributes values if the attribute type is list or map. In other cases it only stores new value. If the type
   * is list, new values are added to the current stored list. It the type is map, new values are added and existing are
   * overwritten with new values, but only if there is some change.
   *
   * @param sess
   * @param member
   * @param attributes
   * @throws InternalErrorException
   * @throws WrongAttributeValueException
   * @throws WrongReferenceAttributeValueException
   * @throws WrongAttributeAssignmentException
   */
  void mergeAttributesValues(PerunSession sess, Member member, List<Attribute> attributes)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException;

  /**
   * Unset all attributes for the group and resource. If workWithGroupAttributes is true, remove also all group
   * attributes.
   * <p>
   * PRIVILEGE: Remove attributes only when principal has access to write on them.
   *
   * @param sess                    perun session
   * @param group                   group to set on
   * @param resource                resource to set on
   * @param workWithGroupAttributes if true, remove also group attributes, if false, remove only group_resource
   *                                attributes
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongReferenceAttributeValueException if some reference attribute has illegal value
   * @throws GroupResourceMismatchException
   */
  void removeAllAttributes(PerunSession sess, Resource resource, Group group, boolean workWithGroupAttributes)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException, GroupResourceMismatchException;

  /**
   * Unset all attributes for the facility.
   *
   * @param sess     perun session
   * @param facility remove attributes from this facility
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  void removeAllAttributes(PerunSession sess, Facility facility)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Unset all attributes for the facility. If removeAlsoUserFacilityAttributes is true, remove all user-facility
   * attributes of this facility and any user allowed in this facility.
   *
   * @param sess                             perun session
   * @param facility                         remove attributes from this facility
   * @param removeAlsoUserFacilityAttributes if true, remove all user-facility attributes for any user in this facility
   *                                         too
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongReferenceAttributeValueException
   * @throws WrongAttributeValueException
   */
  void removeAllAttributes(PerunSession sess, Facility facility, boolean removeAlsoUserFacilityAttributes)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Unset all attributes for the vo.
   *
   * @param sess perun session
   * @param vo   remove attributes from this vo
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  void removeAllAttributes(PerunSession sess, Vo vo)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Unset all attributes for the group.
   *
   * @param sess  perun session
   * @param group remove attributes from this group
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  void removeAllAttributes(PerunSession sess, Group group)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Unset all attributes for the resource.
   *
   * @param sess     perun session
   * @param resource remove attributes from this resource
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  void removeAllAttributes(PerunSession sess, Resource resource)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Unset all attributes for the member on the resource.
   *
   * @param sess     perun session
   * @param member   remove attributes from this member
   * @param resource remove attributes from this resources
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  void removeAllAttributes(PerunSession sess, Member member, Resource resource)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException, MemberResourceMismatchException;

  /**
   * Unset all attributes for the member in the group.
   *
   * @param sess   perun session
   * @param group  remove attributes for this group
   * @param member remove attributes from this member
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongReferenceAttributeValueException
   * @throws WrongAttributeValueException
   */
  void removeAllAttributes(PerunSession sess, Member member, Group group)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException, MemberGroupMismatchException;

  /**
   * Unset all attributes for the member.
   *
   * @param sess   perun session
   * @param member remove attributes from this member
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  void removeAllAttributes(PerunSession sess, Member member)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Unset all attributes (user-facility) for the user on the facility.
   *
   * @param sess     perun session
   * @param facility
   * @param user
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  void removeAllAttributes(PerunSession sess, Facility facility, User user)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Unset all attributes for the user.
   *
   * @param sess perun session
   * @param user remove attributes from this user
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  void removeAllAttributes(PerunSession sess, User user)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException, WrongAttributeAssignmentException;

  /**
   * Unset all attributes for the host.
   *
   * @param sess perun session
   * @param host remove attributes from this host
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  void removeAllAttributes(PerunSession sess, Host host) throws WrongAttributeValueException;

  /**
   * Unset all group-resource attributes
   *
   * @param sess
   * @param resource
   * @param group
   * @throws InternalErrorException         if an exception raise in concrete implementation, the exception is wrapped
   *                                        in InternalErrorException
   * @throws GroupResourceMismatchException
   */
  void removeAllAttributes(PerunSession sess, Resource resource, Group group)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException, GroupResourceMismatchException;

  /**
   * Unset all attributes for the user external source.
   *
   * @param sess perun session
   * @param ues  remove attributes from this user external source
   * @throws InternalErrorException if an exception raise in concrete implementation, the exception is wrapped in
   *                                InternalErrorException
   */
  void removeAllAttributes(PerunSession sess, UserExtSource ues)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Remove all non-virtual group-resource attributes assigned to resource
   *
   * @param sess
   * @param resource
   * @throws InternalErrorException
   * @throws WrongAttributeValueException
   * @throws WrongReferenceAttributeValueException
   * @throws GroupResourceMismatchException
   */
  void removeAllGroupResourceAttributes(PerunSession sess, Resource resource)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException, GroupResourceMismatchException;

  /**
   * Remove all non-virtual member-resource attributes assigned to resource
   *
   * @param sess
   * @param resource
   * @throws InternalErrorException
   */
  void removeAllMemberResourceAttributes(PerunSession sess, Resource resource);

  /**
   * Unset all <b>non-virtual</b> user-facility attributes for the user and <b>all facilities</b>
   *
   * @param sess perun session
   * @param user
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongReferenceAttributeValueException
   * @throws WrongAttributeValueException
   */
  void removeAllUserFacilityAttributes(PerunSession sess, User user)
      throws WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Unset particular attribute for the facility. Core attributes can't be removed this way.
   *
   * @param sess      perun session
   * @param facility  remove attribute from this facility
   * @param attribute attribute to remove
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException if attribute isn't facility attribute or if it is core attribute
   */
  void removeAttribute(PerunSession sess, Facility facility, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Unset particular entityless attribute with subject equals key.
   *
   * @param sess      perun session
   * @param key       subject of entityless attribute
   * @param attribute attribute to remove
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is wrong/illegal
   * @throws WrongReferenceAttributeValueException if the attribute isn't entityless attribute
   * @throws WrongAttributeAssignmentException
   */
  void removeAttribute(PerunSession sess, String key, AttributeDefinition attribute)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Unset particular attribute for the vo. Core attributes can't be removed this way.
   *
   * @param sess      perun session
   * @param vo        remove attribute from this vo
   * @param attribute attribute to remove
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException if attribute isn't vo attribute or if it is core attribute
   */
  void removeAttribute(PerunSession sess, Vo vo, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Unset particular attribute for the group. Core attributes can't be removed this way.
   *
   * @param sess      perun session
   * @param group     remove attribute from this group
   * @param attribute attribute to remove
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException if attribute isn't group attribute or if it is core attribute
   */
  void removeAttribute(PerunSession sess, Group group, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Unset particular attribute for the resource. Core attributes can't be removed this way.
   *
   * @param sess      perun session
   * @param resource  remove attribute from this resource
   * @param attribute attribute to remove
   * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first
   * place
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException if attribute isn't resource attribute or if it is core attribute
   */
  boolean removeAttribute(PerunSession sess, Resource resource, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Unset particular attribute for the member on the resource. Core attributes can't be removed this way.
   *
   * @param sess      perun session
   * @param member    remove attribute from this member
   * @param resource  remove attributes for this resource
   * @param attribute attribute to remove
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException if attribute isn't member-resource attribute or if it is core attribute
   * @throws MemberResourceMismatchException
   */
  void removeAttribute(PerunSession sess, Member member, Resource resource, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException,
                 MemberResourceMismatchException;

  /**
   * Unset particular attribute for the member in the group. Core attributes can't be removed this way.
   *
   * @param sess      perun session
   * @param group     remove attributes for this group
   * @param member    remove attribute from this member
   * @param attribute attribute to remove
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongReferenceAttributeValueException
   * @throws WrongAttributeValueException
   * @throws WrongAttributeAssignmentException     if attribute isn't member-resource attribute or if it is core
   *                                               attribute
   */
  void removeAttribute(PerunSession sess, Member member, Group group, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException,
                 MemberGroupMismatchException;

  /**
   * Unset particular attribute for the member. Core attributes can't be removed this way.
   *
   * @param sess      perun session
   * @param member    remove attribute from this member
   * @param attribute attribute to remove
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException if attribute isn't member-resource attribute or if it is core attribute
   */
  void removeAttribute(PerunSession sess, Member member, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Unset particular attribute for the user on the facility. Core attributes can't be removed this way.
   *
   * @param sess      perun session
   * @param user      remove attribute from this user
   * @param facility  remove attributes for this facility
   * @param attribute attribute to remove
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException if attribute isn't user-facility attribute or if it is core attribute
   */
  void removeAttribute(PerunSession sess, Facility facility, User user, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Unset particular attribute for the user. Core attributes can't be removed this way.
   *
   * @param sess      perun session
   * @param user      remove attribute from this user
   * @param attribute attribute to remove
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException if attribute isn't user-facility attribute or if it is core attribute
   */
  void removeAttribute(PerunSession sess, User user, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Unset particular attribute for the host. Core attributes can't be removed this way.
   *
   * @param sess
   * @param host
   * @param attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException if attribute is not host attribute
   */
  void removeAttribute(PerunSession sess, Host host, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException;

  /**
   * Unset particular group-resource attribute
   *
   * @param sess
   * @param resource
   * @param group
   * @param attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws GroupResourceMismatchException
   */
  void removeAttribute(PerunSession sess, Resource resource, Group group, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException,
                 GroupResourceMismatchException;

  /**
   * Unset particular attribute for the user external source.
   *
   * @param sess      perun session
   * @param ues       remove attribute from this user external source
   * @param attribute attribute to remove
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException     if attribute isn't user external source attribute
   * @throws WrongAttributeValueException
   * @throws WrongReferenceAttributeValueException
   */
  void removeAttribute(PerunSession sess, UserExtSource ues, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Unset all attributes for the key (entityless) without check of value.
   *
   * @param sess
   * @param key
   * @param attribute
   * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first
   * place
   * @throws WrongAttributeAssignmentException if attribute isn't entityless attribute or if it is core attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   */
  boolean removeAttributeWithoutCheck(PerunSession sess, String key, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException;

  /**
   * Unset all attributes for the facility without check of value.
   *
   * @param sess
   * @param facility
   * @param attribute
   * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first
   * place
   * @throws WrongAttributeAssignmentException if attribute isn't facility attribute or if it is core attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   */
  boolean removeAttributeWithoutCheck(PerunSession sess, Facility facility, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException;

  /**
   * Unset all attributes for the host without check of value.
   *
   * @param sess
   * @param host
   * @param attribute
   * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first
   * place
   * @throws WrongAttributeAssignmentException if attribute isn't host attribute or if it is core attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   */
  boolean removeAttributeWithoutCheck(PerunSession sess, Host host, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException;

  /**
   * Unset all attributes for the vo without check of value.
   *
   * @param sess
   * @param vo
   * @param attribute
   * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first
   * place
   * @throws WrongAttributeAssignmentException if attribute isn't vo attribute or if it is core attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   */
  boolean removeAttributeWithoutCheck(PerunSession sess, Vo vo, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException;

  /**
   * Unset all attributes for the group without check of value.
   *
   * @param sess
   * @param group
   * @param attribute
   * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first
   * place
   * @throws WrongAttributeAssignmentException if attribute isn't group attribute or if it is core attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   */
  boolean removeAttributeWithoutCheck(PerunSession sess, Group group, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException;

  /**
   * Unset all attributes for the resource without check of value.
   *
   * @param sess
   * @param resource
   * @param attribute
   * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first
   * place
   * @throws WrongAttributeAssignmentException if attribute isn't resource attribute or if it is core attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   */
  boolean removeAttributeWithoutCheck(PerunSession sess, Resource resource, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException;

  /**
   * Unset all attributes for the member-resource without check of value.
   *
   * @param sess
   * @param member
   * @param resource
   * @param attribute
   * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first
   * place
   * @throws WrongAttributeAssignmentException     if attribute isn't member-resource attribute or if it is core
   *                                               attribute
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongReferenceAttributeValueException if there is problem with removing value because of actual value of
   *                                               referenced attribute
   * @throws MemberResourceMismatchException
   */
  boolean removeAttributeWithoutCheck(PerunSession sess, Member member, Resource resource,
                                      AttributeDefinition attribute)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException, MemberResourceMismatchException;

  /**
   * Unset all attributes for the member-group without check of value.
   *
   * @param sess
   * @param member
   * @param group
   * @param attribute
   * @return
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   */
  boolean removeAttributeWithoutCheck(PerunSession sess, Member member, Group group, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException, MemberGroupMismatchException;

  /**
   * Unset all attributes for the member without check of value.
   *
   * @param sess
   * @param member
   * @param attribute
   * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first
   * place
   * @throws WrongAttributeAssignmentException if attribute isn't member attribute or if it is core attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   */
  boolean removeAttributeWithoutCheck(PerunSession sess, Member member, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException;

  /**
   * Unset all attributes for the user-facility without check of value.
   *
   * @param sess
   * @param facility
   * @param user
   * @param attribute
   * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first
   * place
   * @throws WrongAttributeAssignmentException if attribute isn't user-facility attribute or if it is core attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   */
  boolean removeAttributeWithoutCheck(PerunSession sess, Facility facility, User user, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException;

  /**
   * Unset all attributes for the user without check of value.
   *
   * @param sess
   * @param user
   * @param attribute
   * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first
   * place
   * @throws WrongAttributeAssignmentException if attribute isn't user attribute or if it is core attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   */
  boolean removeAttributeWithoutCheck(PerunSession sess, User user, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException;

  /**
   * Unset all attributes for the group-resource without check of value.
   *
   * @param sess
   * @param resource
   * @param group
   * @param attribute
   * @return {@code true} if attribute was changed (deleted) or {@code false} if attribute was not present in a first
   * place
   * @throws WrongAttributeAssignmentException if attribute isn't group-resource attribute or if it is core attribute
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws GroupResourceMismatchException
   */
  boolean removeAttributeWithoutCheck(PerunSession sess, Resource resource, Group group, AttributeDefinition attribute)
      throws WrongAttributeAssignmentException, GroupResourceMismatchException;

  /**
   * Unset the group_resource attributes. If an attribute is core attribute, then the attribute isn't unseted (it's
   * skipped without notification). If workWithGroupAttributes is true, unset also group attributes.
   * <p>
   * Remove only attributes which are in list of attributes.
   * <p>
   * PRIVILEGE: Remove attributes only when principal has access to write on them.
   *
   * @param sess                    perun session
   * @param group                   group to set on
   * @param resource                resource to set on
   * @param attributes              attributes which will be used to removing
   * @param workWithGroupAttributes if true, remove also group attributes, if false, remove only group_resource
   *                                attributes
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException     if attribute is not group-resource or group attribute
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongReferenceAttributeValueException if some reference attribute has illegal value
   * @throws GroupResourceMismatchException
   */
  void removeAttributes(PerunSession sess, Resource resource, Group group,
                        List<? extends AttributeDefinition> attributes, boolean workWithGroupAttributes)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException,
                 GroupResourceMismatchException;

  /**
   * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this
   * way.
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession, Facility, AttributeDefinition)
   */
  void removeAttributes(PerunSession sess, Facility facility, List<? extends AttributeDefinition> attributes)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Unset all <b>non-empty</b> attributes associated with the member and if workWithUserAttributes is true, unset all
   * <b>non-empty</b> attributes associated with user, who is this member.
   *
   * @param sess                   perun session
   * @param member                 remove attribute from this member
   * @param workWithUserAttributes true if I want to unset all attributes associated with user, who is the member too
   * @param attributes             attribute to remove
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws WrongAttributeValueException
   * @throws WrongReferenceAttributeValueException if attribute isn't member attribute or if it is core attribute
   */
  void removeAttributes(PerunSession sess, Member member, boolean workWithUserAttributes,
                        List<? extends AttributeDefinition> attributes)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Unset the member, user, member-resource and user-facility attributes. If an attribute is core attribute then the
   * attribute isn't unseted (It's skipped without any notification).
   *
   * @param sess       perun session
   * @param facility
   * @param resource   resource to set on
   * @param user
   * @param member     member to set on
   * @param attributes
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not member-resource, user, member or user-facility
   *                                               attribute
   * @throws WrongReferenceAttributeValueException
   * @throws MemberResourceMismatchException
   */
  void removeAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member,
                        List<? extends AttributeDefinition> attributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException,
                 MemberResourceMismatchException;

  /**
   * Unset the member, user, member-group, member-resource and user-facility attributes. If an attribute is core
   * attribute then the attribute isn't unseted (It's skipped without any notification).
   *
   * @param sess       perun session
   * @param facility
   * @param resource   resource to set on
   * @param group      group to set on
   * @param user
   * @param member     member to set on
   * @param attributes
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not member-resource, user, member or user-facility
   *                                               attribute
   * @throws WrongReferenceAttributeValueException
   */
  void removeAttributes(PerunSession sess, Facility facility, Resource resource, Group group, User user, Member member,
                        List<? extends AttributeDefinition> attributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException,
                 GroupResourceMismatchException, MemberResourceMismatchException, MemberGroupMismatchException;

  /**
   * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this
   * way.
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession sess, Vo vo, AttributeDefinition
   * attribute)
   */
  void removeAttributes(PerunSession sess, Vo vo, List<? extends AttributeDefinition> attributes)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this
   * way.
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession sess, Group group,
   * AttributeDefinition attribute)
   */
  void removeAttributes(PerunSession sess, Group group, List<? extends AttributeDefinition> attributes)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this
   * way.
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession sess, Resource resource,
   * AttributeDefinition attribute)
   */
  void removeAttributes(PerunSession sess, Resource resource, List<? extends AttributeDefinition> attributes)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this
   * way.
   */
  void removeAttributes(PerunSession sess, Member member, Resource resource,
                        List<? extends AttributeDefinition> attributes)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException,
                 MemberResourceMismatchException;

  /**
   * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this
   * way.
   *
   * @throws AttributeNotExistsException if the any of attributes doesn't exists in underlying data source
   * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession, Member, Group,
   * AttributeDefinition)
   */
  void removeAttributes(PerunSession sess, Member member, Group group, List<? extends AttributeDefinition> attributes)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException,
                 MemberGroupMismatchException;

  void removeAttributes(PerunSession sess, Member member, Group group, List<? extends AttributeDefinition> attributes,
                        boolean workWithUserAttributes)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException,
                 MemberGroupMismatchException;

  /**
   * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this
   * way.
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession, Member, Resource,
   * AttributeDefinition)
   */
  void removeAttributes(PerunSession sess, Member member, List<? extends AttributeDefinition> attributes)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this
   * way.
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession sess, Facility facility, User
   * user, AttributeDefinition attribute)
   */
  void removeAttributes(PerunSession sess, Facility facility, User user, List<? extends AttributeDefinition> attributes)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this
   * way.
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession sess, Facility facility, User
   * user, AttributeDefinition attribute)
   */
  void removeAttributes(PerunSession sess, User user, List<? extends AttributeDefinition> attributes)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Batch version of removeAttribute. This method automatically skip all core attributes which can't be removed this
   * way.
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession sess, Host host,
   * AttributeDefinition attribute)
   */
  void removeAttributes(PerunSession sess, Host host, List<? extends AttributeDefinition> attributes)
      throws WrongAttributeAssignmentException, WrongAttributeValueException;

  /**
   * Batch version of removeAttribute.
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession sess, Resource resource, Group
   * group, AttributeDefinition attribute)
   */
  void removeAttributes(PerunSession sess, Resource resource, Group group,
                        List<? extends AttributeDefinition> attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException,
                 GroupResourceMismatchException;

  /**
   * Batch version of removeAttribute.
   *
   * @see cz.metacentrum.perun.core.api.AttributesManager#removeAttribute(PerunSession sess, UserExtSource ues,
   * AttributeDefinition attribute)
   */
  void removeAttributes(PerunSession sess, UserExtSource ues, List<? extends AttributeDefinition> attributes)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Store the particular attribute associated with the facility. Core attributes can't be set this way.
   *
   * @param sess      perun session
   * @param facility  facility to set on
   * @param attribute attribute to set
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not facility attribute or if it is core attribute
   * @throws WrongReferenceAttributeValueException
   */
  void setAttribute(PerunSession sess, Facility facility, Attribute attribute)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Store the particular attribute associated with the vo. Core attributes can't be set this way.
   *
   * @param sess      perun session
   * @param vo        vo to set on
   * @param attribute attribute to set
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not vo attribute or if it is core attribute
   * @throws WrongReferenceAttributeValueException
   */
  void setAttribute(PerunSession sess, Vo vo, Attribute attribute)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Store the particular attribute associated with the group. Core attributes can't be set this way.
   *
   * @param sess      perun session
   * @param group     group to set on
   * @param attribute attribute to set
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not group attribute or if it is core attribute
   * @throws WrongReferenceAttributeValueException
   */
  void setAttribute(PerunSession sess, Group group, Attribute attribute)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Store the particular attribute associated with the resource. Core attributes can't be set this way.
   *
   * @param sess      perun session
   * @param resource  resource to set on
   * @param attribute attribute to set
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not resource attribute or if it is core attribute
   * @throws WrongReferenceAttributeValueException
   */
  void setAttribute(PerunSession sess, Resource resource, Attribute attribute)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Store the particular attribute associated with the resource and member combination.  Core attributes can't be set
   * this way.
   *
   * @param sess      perun session
   * @param member    member to set on
   * @param resource  resource to set on
   * @param attribute attribute to set
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not member-resource attribute or if it is core
   *                                               attribute
   * @throws WrongReferenceAttributeValueException
   * @throws MemberResourceMismatchException
   */
  void setAttribute(PerunSession sess, Member member, Resource resource, Attribute attribute)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException,
                 MemberResourceMismatchException;

  /**
   * Store the particular attribute associated with the group and member combination. Core attributes can't be set this
   * way.
   *
   * @param sess      perun session
   * @param member    member to set on
   * @param group     group to set on
   * @param attribute attribute to set
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not member-resource attribute or if it is core
   *                                               attribute
   * @throws WrongReferenceAttributeValueException
   */
  void setAttribute(PerunSession sess, Member member, Group group, Attribute attribute)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException,
                 MemberGroupMismatchException;

  /**
   * Store the particular attribute associated with the member.  Core attributes can't be set this way.
   *
   * @param sess      perun session
   * @param member    member to set on
   * @param attribute attribute to set
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not member-resource attribute or if it is core
   *                                               attribute
   * @throws WrongReferenceAttributeValueException
   */
  void setAttribute(PerunSession sess, Member member, Attribute attribute)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Store the attribute associated with the facility and user combination.  Core attributes can't be set this way.
   *
   * @param sess      perun session
   * @param facility  facility to set on
   * @param user      user to set on
   * @param attribute attribute to set
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not user-facility attribute
   * @throws WrongReferenceAttributeValueException
   */
  void setAttribute(PerunSession sess, Facility facility, User user, Attribute attribute)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Store the attribute associated with the user. Core attributes can't be set this way.
   *
   * @param sess      perun session
   * @param user      user to set on
   * @param attribute attribute to set
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not user-facility attribute
   * @throws WrongReferenceAttributeValueException
   */
  void setAttribute(PerunSession sess, User user, Attribute attribute)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Store the attribute associated with the host. Core attributes can't be set this way.
   *
   * @param sess      perun session
   * @param host      host to set attributes for
   * @param attribute attribute to be set
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException if attribute is not host attribute
   * @throws WrongAttributeValueException      if the attribute value is illegal
   */
  void setAttribute(PerunSession sess, Host host, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Store the group-resource attribute
   *
   * @param sess
   * @param resource
   * @param group
   * @param attribute
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws WrongAttributeValueException
   * @throws WrongReferenceAttributeValueException
   * @throws GroupResourceMismatchException
   */
  void setAttribute(PerunSession sess, Resource resource, Group group, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException,
                 GroupResourceMismatchException;

  /**
   * Stores entityless attribute (associated with string key).
   *
   * @param sess      perun session
   * @param key       store the attribute for this key
   * @param attribute attribute to set
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not entityless attribute
   * @throws WrongReferenceAttributeValueException
   */
  void setAttribute(PerunSession sess, String key, Attribute attribute)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Store the attribute associated with the user external source.
   *
   * @param sess      perun session
   * @param ues       user external source to set on
   * @param attribute attribute to set
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not user external source attribute
   * @throws WrongReferenceAttributeValueException
   */
  void setAttribute(PerunSession sess, UserExtSource ues, Attribute attribute)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Marks the action on attribute as critical, which may require additional authentication of user performing that
   * action on attribute.
   *
   * @param sess     session
   * @param attr     attribute definition
   * @param action   critical action
   * @param critical true if action should be set critical, false to non-critical
   * @param global   true if action should be globally critical, false if action should be critical only for critical
   *                 objects
   * @throws RelationExistsException    if trying to mark already critical action
   * @throws RelationNotExistsException if trying to unmark not critical action
   */
  void setAttributeActionCriticality(PerunSession sess, AttributeDefinition attr, AttributeAction action,
                                     boolean critical, boolean global)
      throws RelationExistsException, RelationNotExistsException;

  /**
   * Store the particular attribute associated with the group. Core attributes can't be set this way.
   * <p>
   * This method creates nested transaction to prevent storing value to DB if it throws any exception.
   *
   * @param sess      perun session
   * @param group     group to set on
   * @param attribute attribute to set
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not group attribute or if it is core attribute
   * @throws WrongReferenceAttributeValueException
   */
  void setAttributeInNestedTransaction(PerunSession sess, Group group, Attribute attribute)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Store the particular attribute associated with the member-group relationship. Core attributes can't be set this
   * way.
   * <p>
   * This method creates nested transaction to prevent storing value to DB if it throws any exception.
   *
   * @param sess      perun session
   * @param member    member to set on
   * @param group     group to set on
   * @param attribute attribute to set
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not member-group attribute or if it is core
   *                                               attribute
   * @throws WrongReferenceAttributeValueException
   */
  void setAttributeInNestedTransaction(PerunSession sess, Member member, Group group, Attribute attribute)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException,
                 AttributeNotExistsException, MemberGroupMismatchException;

  /**
   * Store the particular attribute associated with the member. Core attributes can't be set this way.
   * <p>
   * This method creates nested transaction to prevent storing value to DB if it throws any exception.
   *
   * @param sess      perun session
   * @param member    member to set on
   * @param attribute attribute to set
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not member-resource attribute or if it is core
   *                                               attribute
   * @throws WrongReferenceAttributeValueException
   */
  void setAttributeInNestedTransaction(PerunSession sess, Member member, Attribute attribute)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Store the attribute associated with the user. Core attributes can't be set this way.
   * <p>
   * This method creates nested transaction to prevent storing value to DB if it throws any exception.
   *
   * @param sess      perun session
   * @param user      user to set on
   * @param attribute attribute to set
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not user-facility attribute
   * @throws WrongReferenceAttributeValueException
   */
  void setAttributeInNestedTransaction(PerunSession sess, User user, Attribute attribute)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Store the particular attribute associated with the user external source. Core attributes can't be set this way.
   * <p>
   * This method creates nested transaction to prevent storing value to DB if it throws any exception.
   *
   * @param sess      perun session
   * @param ues       user external source to set on
   * @param attribute attribute to set
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not user external source attribute
   * @throws WrongReferenceAttributeValueException
   */
  void setAttributeInNestedTransaction(PerunSession sess, UserExtSource ues, Attribute attribute)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Deletes old attribute policy collections and sets all new attribute policy collections.
   *
   * @param sess              perun session
   * @param policyCollections list of policy collections to set
   * @throws InternalErrorException
   */
  void setAttributePolicyCollections(PerunSession sess, List<AttributePolicyCollection> policyCollections);

  /**
   * Just store the particular attribute associated with the facility, doesn't preform any value check. Core attributes
   * can't be set this way.
   *
   * @param sess      perun session
   * @param facility  facility to set on
   * @param attribute attribute to set
   * @return true, if attribute was set in DB
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException if attribute is not resource attribute or if it is core attribute
   */
  boolean setAttributeWithoutCheck(PerunSession sess, Facility facility, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException;

  /**
   * Just store the particular attribute associated with the vo, doesn't preform any value check. Core attributes can't
   * be set this way.
   *
   * @param sess
   * @param vo
   * @param attribute
   * @return
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws WrongAttributeValueException
   */
  boolean setAttributeWithoutCheck(PerunSession sess, Vo vo, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException;

  /**
   * Just store the particular attribute associated with the user-facility, doesn't preform any value check. Core
   * attributes can't be set this way.
   *
   * @param sess
   * @param facility
   * @param user
   * @param attribute
   * @return
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws WrongAttributeValueException
   */
  boolean setAttributeWithoutCheck(PerunSession sess, Facility facility, User user, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException;

  /**
   * Just store the particular attribute associated with the member-resource, doesn't preform any value check. Core
   * attributes can't be set this way.
   *
   * @param sess
   * @param member
   * @param resource
   * @param attribute
   * @param workWithUserAttributes
   * @return
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws WrongAttributeValueException
   * @throws WrongReferenceAttributeValueException
   * @throws MemberResourceMismatchException
   */
  boolean setAttributeWithoutCheck(PerunSession sess, Member member, Resource resource, Attribute attribute,
                                   boolean workWithUserAttributes)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException,
                 MemberResourceMismatchException;

  /**
   * Just store the particular attribute associated with the member-group, doesn't preform any value check. Core
   * attributes can't be set this way.
   *
   * @param sess
   * @param member
   * @param group
   * @param attribute
   * @param workWithUserAttributes
   * @return
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws WrongAttributeValueException
   * @throws WrongReferenceAttributeValueException
   */
  boolean setAttributeWithoutCheck(PerunSession sess, Member member, Group group, Attribute attribute,
                                   boolean workWithUserAttributes)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException,
                 MemberGroupMismatchException;

  /**
   * Just store the particular attribute associated with the member, doesn't preform any value check. Core attributes
   * can't be set this way.
   *
   * @param sess
   * @param member
   * @param attribute
   * @return
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws WrongAttributeValueException
   * @throws WrongReferenceAttributeValueException
   */
  boolean setAttributeWithoutCheck(PerunSession sess, Member member, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Just store the particular attribute associated with the host, doesn't preform any value check. Core attributes
   * can't be set this way.
   *
   * @param sess
   * @param host
   * @param attribute
   * @return
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws WrongAttributeValueException
   */
  boolean setAttributeWithoutCheck(PerunSession sess, Host host, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException;

  /**
   * Just store the particular attribute associated with the entityless, doesn't preform any value check. Core
   * attributes can't be set this way.
   *
   * @param sess
   * @param key
   * @param attribute
   * @return
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws WrongAttributeValueException
   */
  boolean setAttributeWithoutCheck(PerunSession sess, String key, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException;

  /**
   * Just store the particular attribute associated with the resource, doesn't preform any value check. Core attributes
   * can't be set this way.
   *
   * @param sess      perun session
   * @param resource  resource to set on
   * @param attribute attribute to set
   * @return true, if attribute was set in DB
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException     if attribute is not resource attribute or if it is core attribute
   * @throws WrongAttributeValueException
   * @throws WrongReferenceAttributeValueException
   */
  boolean setAttributeWithoutCheck(PerunSession sess, Resource resource, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Just store the particular attribute associated with the group, doesn't preform any value check. Core attributes
   * can't be set this way.
   *
   * @param sess      perun session
   * @param group
   * @param attribute
   * @return true, if attribute was set in DB
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException     if attribute is not group attribute or if it is core attribute
   * @throws WrongAttributeValueException
   * @throws WrongReferenceAttributeValueException
   */
  boolean setAttributeWithoutCheck(PerunSession sess, Group group, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Just store the attribute associated with the user, doesn't preform any value check.  Core attributes can't be set
   * this way.
   *
   * @param sess      perun session
   * @param user      user to set on
   * @param attribute attribute to set
   * @return true, if attribute was set in DB
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeAssignmentException     if attribute is not user-facility attribute
   * @throws WrongAttributeValueException
   * @throws WrongReferenceAttributeValueException
   */
  boolean setAttributeWithoutCheck(PerunSession sess, User user, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException;

  /**
   * Just store the group-resource attribute, do not preform any value check.
   *
   * @param sess
   * @param resource
   * @param group
   * @param attribute
   * @return true, if attribute was set in DB
   * @throws InternalErrorException
   * @throws WrongReferenceAttributeValueException Can be raised while storing virtual attribute if another attribute
   *                                               which is required for set virtual attribute have wrong value
   * @throws WrongAttributeAssignmentException
   * @throws WrongAttributeValueException
   * @throws GroupResourceMismatchException
   */
  boolean setAttributeWithoutCheck(PerunSession sess, Resource resource, Group group, Attribute attribute)
      throws WrongAttributeAssignmentException, WrongAttributeValueException, WrongReferenceAttributeValueException,
                 GroupResourceMismatchException;

  /**
   * Store the attributes associated with the facility. If an attribute is core attribute then the attribute isn't
   * stored (It's skipped without any notification).
   *
   * @param sess       perun session
   * @param facility   facility to set on
   * @param attributes attribute to set
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not facility attribute
   * @throws WrongReferenceAttributeValueException
   */
  void setAttributes(PerunSession sess, Facility facility, List<Attribute> attributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Store the attributes associated with the vo. If an attribute is core attribute then the attribute isn't stored
   * (It's skipped without any notification).
   *
   * @param sess       perun session
   * @param vo         vo to set on
   * @param attributes attribute to set
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not vo attribute
   * @throws WrongReferenceAttributeValueException
   */
  void setAttributes(PerunSession sess, Vo vo, List<Attribute> attributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Store the attributes associated with the group. If an attribute is core attribute then the attribute isn't stored
   * (It's skipped without any notification).
   *
   * @param sess       perun session
   * @param group      group to set on
   * @param attributes attribute to set
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not group attribute
   * @throws WrongReferenceAttributeValueException
   */
  void setAttributes(PerunSession sess, Group group, List<Attribute> attributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Store the attributes associated with the resource. If an attribute is core attribute then the attribute isn't
   * stored (It's skipped without any notification).
   *
   * @param sess       perun session
   * @param resource   resource to set on
   * @param attributes attribute to set
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not resource attribute
   * @throws WrongReferenceAttributeValueException
   * @throws WrongAttributeValueException
   */
  void setAttributes(PerunSession sess, Resource resource, List<Attribute> attributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Store the attributes associated with the resource and member combination. If an attribute is core attribute then
   * the attribute isn't stored (It's skipped without any notification).
   *
   * @param sess       perun session
   * @param member     member to set on
   * @param resource   resource to set on
   * @param attributes attribute to set
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not member-resource attribute
   * @throws WrongReferenceAttributeValueException
   * @throws MemberResourceMismatchException
   */
  void setAttributes(PerunSession sess, Member member, Resource resource, List<Attribute> attributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException,
                 MemberResourceMismatchException;

  /**
   * Store the attributes associated with the resource and member combination. If an attribute is core attribute then
   * the attribute isn't stored (It's skipped without any notification). If workWithUserAttributes is true, the method
   * stores also the attributes associated with user, user-facility and member.
   *
   * @param sess                   perun session
   * @param member                 member to set on
   * @param resource               resource to set on
   * @param attributes             attribute to set
   * @param workWithUserAttributes method can process also user, user-facility and member attributes (user is
   *                               automatically get from member a facility is get from resource)
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not member-resource attribute
   * @throws WrongReferenceAttributeValueException
   * @throws MemberResourceMismatchException       !!WARNING THIS IS VERY TIME-CONSUMING METHOD. DON'T USE IT IN
   *                                               BATCH!!
   */
  void setAttributes(PerunSession sess, Member member, Resource resource, List<Attribute> attributes,
                     boolean workWithUserAttributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException,
                 MemberResourceMismatchException;

  /**
   * Store the attributes associated with the group and member combination. If an attribute is core attribute then the
   * attribute isn't stored (It's skipped without any notification).
   *
   * @param sess       perun session
   * @param member     member to set on
   * @param group      group to set on
   * @param attributes attribute to set
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not member-group attribute
   * @throws WrongReferenceAttributeValueException
   */
  void setAttributes(PerunSession sess, Member member, Group group, List<Attribute> attributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException,
                 MemberGroupMismatchException;

  /**
   * Store the attributes associated with the resource and member combination. If an attribute is core attribute then
   * the attribute isn't stored (It's skipped without any notification). If workWithUserAttributes is true, the method
   * stores also the attributes associated with user and member.
   *
   * @param sess                   perun session
   * @param member                 member to set on
   * @param group                  group to set on
   * @param attributes             attribute to set
   * @param workWithUserAttributes method can process also user and member attributes (user is automatically get from
   *                               member)
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not member-resource attribute
   * @throws WrongReferenceAttributeValueException
   */
  void setAttributes(PerunSession sess, Member member, Group group, List<Attribute> attributes,
                     boolean workWithUserAttributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException,
                 MemberGroupMismatchException;

  /**
   * Store the member, user, member-resource and user-facility attributes. If an attribute is core attribute then the
   * attribute isn't stored (It's skipped without any notification).
   *
   * @param sess     perun session
   * @param facility
   * @param resource
   * @param user
   * @param member
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not member-resource attribute
   * @throws WrongReferenceAttributeValueException
   * @throws MemberResourceMismatchException
   */
  void setAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member,
                     List<Attribute> attributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException,
                 MemberResourceMismatchException;

  /**
   * Store the member, user, member-group, member-resource and user-facility attributes. If an attribute is core
   * attribute then the attribute isn't stored (It's skipped without any notification).
   *
   * @param sess       perun session
   * @param facility
   * @param resource
   * @param group
   * @param user
   * @param member
   * @param attributes
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not member-resource attribute
   * @throws WrongReferenceAttributeValueException
   */
  void setAttributes(PerunSession sess, Facility facility, Resource resource, Group group, User user, Member member,
                     List<Attribute> attributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException,
                 GroupResourceMismatchException, MemberResourceMismatchException, MemberGroupMismatchException;

  /**
   * Store the attributes associated with the resource. If an attribute is core attribute then the attribute isn't
   * stored (It's skipped without any notification).
   *
   * @param sess       perun session
   * @param member     member to set on
   * @param attributes attribute to set
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not member-resource attribute
   * @throws WrongReferenceAttributeValueException
   */
  void setAttributes(PerunSession sess, Member member, List<Attribute> attributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Store the attributes associated with the facility and user combination. If an attribute is core attribute then the
   * attribute isn't stored (It's skipped without any notification).
   *
   * @param sess       perun session
   * @param facility   facility to set on
   * @param user       user to set on
   * @param attributes attribute to set
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not user-facility attribute
   * @throws WrongReferenceAttributeValueException
   */
  void setAttributes(PerunSession sess, Facility facility, User user, List<Attribute> attributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Store the attributes associated with the user. If an attribute is core attribute then the attribute isn't stored
   * (It's skipped without any notification).
   *
   * @param sess       perun session
   * @param user       user to set on
   * @param attributes attribute to set
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not user-facility attribute
   * @throws WrongReferenceAttributeValueException
   */
  void setAttributes(PerunSession sess, User user, List<Attribute> attributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Store the attributes associated with the host. If an attribute is core attribute then the attribute isn't stored
   * (It's skipped without any notification).
   *
   * @param sess       perun session
   * @param host       host to set attributes for
   * @param attributes attributes to be set
   * @throws InternalErrorException            if an exception raise in concrete implementation, the exception is
   *                                           wrapped in InternalErrorException
   * @throws WrongAttributeValueException      if the attribute value is illegal
   * @throws WrongAttributeAssignmentException if attribute is not host attribute
   */
  void setAttributes(PerunSession sess, Host host, List<Attribute> attributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Stores the group-resource attributes.
   *
   * @param sess
   * @param resource
   * @param group
   * @param attributes
   * @throws InternalErrorException
   * @throws WrongAttributeValueException
   * @throws WrongAttributeAssignmentException
   * @throws GroupResourceMismatchException
   */
  void setAttributes(PerunSession sess, Resource resource, Group group, List<Attribute> attributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException,
                 GroupResourceMismatchException;

  void setAttributes(PerunSession sess, Resource resource, Group group, List<Attribute> attributes,
                     boolean workWithGroupAttributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException,
                 GroupResourceMismatchException;

  /**
   * Store the attributes associated with the user external source. If an attribute is core attribute then the attribute
   * isn't stored (It's skipped without any notification).
   *
   * @param sess       perun session
   * @param ues        user external source to set on
   * @param attributes attribute to set
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not user external source attribute
   * @throws WrongReferenceAttributeValueException
   */
  void setAttributes(PerunSession sess, UserExtSource ues, List<Attribute> attributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Store the attributes associated with member and user (which we get from this member) if workWithUserAttributes is
   * true. If an attribute is core attribute then the attribute isn't stored (It's skipped without any notification).
   *
   * @param sess                   perun session
   * @param member                 member to set on
   * @param attributes             attribute to set
   * @param workWithUserAttributes true/false If true, we can use user attributes (get from this member) too
   * @throws InternalErrorException                if an exception raise in concrete implementation, the exception is
   *                                               wrapped in InternalErrorException
   * @throws WrongAttributeValueException          if the attribute value is illegal
   * @throws WrongAttributeAssignmentException     if attribute is not member attribute or with
   *                                               workWithUserAttributes=true, if its not member or user attribute.
   * @throws WrongReferenceAttributeValueException
   */
  void setAttributes(PerunSession sess, Member member, List<Attribute> attributes, boolean workWithUserAttributes)
      throws WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException;

  /**
   * Get and set required attribute for member, resource, user and facility.
   * <p>
   * Procedure: 1] Get all member, member-resource, user, user-facility required attributes for member and resource. 2]
   * Fill attributes and store those which were really filled. (value changed) 3] Set filled attributes. 4] Refresh
   * value in all virtual attributes. 5] Check all attributes and their dependencies.
   *
   * @param sess
   * @param member
   * @param resource
   * @param user
   * @param facility
   * @param forceAttributesChecks if true, all required attributes for given resource and user will be semantically
   *                              checked, no matter if the user has truly access to the given resource
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws WrongReferenceAttributeValueException
   * @throws AttributeNotExistsException
   * @throws WrongAttributeValueException
   * @throws MemberResourceMismatchException
   */
  void setRequiredAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member,
                             boolean forceAttributesChecks)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException,
                 WrongAttributeValueException, MemberResourceMismatchException;

  /**
   * Get and set required attribute for member, resource, user and facility.
   * <p>
   * Procedure: 1] Get all member, member-resource, user, user-facility required attributes for member and resource. 2]
   * Fill attributes and store those which were really filled. (value changed) 3] Set filled attributes. 4] Refresh
   * value in all virtual attributes. 5] Check all attributes and their dependencies.
   *
   * @param sess
   * @param member
   * @param resource
   * @param user
   * @param facility
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws WrongReferenceAttributeValueException
   * @throws AttributeNotExistsException
   * @throws WrongAttributeValueException
   * @throws MemberResourceMismatchException
   */
  void setRequiredAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException,
                 WrongAttributeValueException, MemberResourceMismatchException;

  /**
   * Get and set required attribute for member, resource, user, facility and specific service.
   * <p>
   * Procedure: 1] Get all member, member-resource, user, user-facility required attributes for member, resource and
   * specific service. 2] Fill attributes and store those which were really filled. (value changed) 3] Set filled
   * attributes. 4] Refresh value in all virtual attributes. 5] Check all attributes and their dependencies.
   *
   * @param sess
   * @param service
   * @param facility
   * @param resource
   * @param user
   * @param member
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws WrongReferenceAttributeValueException
   * @throws AttributeNotExistsException
   * @throws WrongAttributeValueException
   * @throws MemberResourceMismatchException
   */
  void setRequiredAttributes(PerunSession sess, Service service, Facility facility, Resource resource, User user,
                             Member member)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException,
                 WrongAttributeValueException, MemberResourceMismatchException;

  /**
   * Get and set required attribute for member, resource, user, facility and specific service.
   * <p>
   * Procedure: 1] Get all member, member-resource, user, user-facility required attributes for member, resource and
   * specific service. 2] Fill attributes and store those which were really filled. (value changed) 3] Set filled
   * attributes. 4] Refresh value in all virtual attributes. 5] Check all attributes and their dependencies.
   *
   * @param sess
   * @param service
   * @param facility
   * @param resource
   * @param user
   * @param member
   * @param forceAttributesChecks if true, all required attributes for given resource and user will be semantically
   *                              checked, no matter if the user has truly access to the given resource
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws WrongReferenceAttributeValueException
   * @throws AttributeNotExistsException
   * @throws WrongAttributeValueException
   * @throws MemberResourceMismatchException
   */
  void setRequiredAttributes(PerunSession sess, Service service, Facility facility, Resource resource, User user,
                             Member member, boolean forceAttributesChecks)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException,
                 WrongAttributeValueException, MemberResourceMismatchException;

  /**
   * Get all required attributes for user, user-facility, member and member-resource attributes and set them.
   *
   * @param sess                  session
   * @param services              services
   * @param facility              facility
   * @param resource              resource
   * @param user                  user
   * @param member                member
   * @param forceAttributesChecks if true, all required attributes for given resource and user will be semantically
   *                              checked, no matter if the user has truly access to the given resource
   * @throws WrongAttributeAssignmentException
   * @throws WrongReferenceAttributeValueException
   * @throws AttributeNotExistsException
   * @throws WrongAttributeValueException
   * @throws MemberResourceMismatchException
   */
  void setRequiredAttributes(PerunSession sess, List<Service> services, Facility facility, Resource resource, User user,
                             Member member, boolean forceAttributesChecks)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException,
                 WrongAttributeValueException, MemberResourceMismatchException;

  /**
   * Take list of required attributes and set those which are empty and can be filled, then check them all.
   * <p>
   * Important: this method DO NOT set non-empty attributes in list, just refresh their values and check them
   * <p>
   * Procedure: 1] Get all attrs from arrayList (they should be required attributes) 2] Fill empty attributes and store
   * those which were really filled. (value changed) 3] Set filled attributes. 4] Refresh value in all attributes (not
   * only in virtual ones - because of possible change by changeAttributeHook in other filledAttributes) 5] Check all
   * attributes and their dependencies.
   *
   * @param sess
   * @param facility
   * @param resource
   * @param user
   * @param member
   * @param attributes
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws WrongReferenceAttributeValueException
   * @throws AttributeNotExistsException
   * @throws WrongAttributeValueException
   * @throws MemberResourceMismatchException
   */
  void setRequiredAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member,
                             List<Attribute> attributes)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException,
                 WrongAttributeValueException, MemberResourceMismatchException;

  /**
   * Take list of required attributes and set those which are empty and can be filled, then check them all.
   * <p>
   * Important: this method DO NOT set non-empty attributes in list, just refresh their values and check them
   * <p>
   * Procedure: 1] Get all attrs from arrayList (they should be required attributes) 2] Fill empty attributes and store
   * those which were really filled. (value changed) 3] Set filled attributes. 4] Refresh value in all attributes (not
   * only in virtual ones - because of possible change by changeAttributeHook in other filledAttributes) 5] Check all
   * attributes and their dependencies.
   *
   * @param sess
   * @param facility
   * @param resource
   * @param user
   * @param member
   * @param attributes
   * @param forceAttributesChecks if true, all required attributes for given resource and user will be semantically
   *                              checked, no matter if the user has truly access to the given resource
   * @throws InternalErrorException
   * @throws WrongAttributeAssignmentException
   * @throws WrongReferenceAttributeValueException
   * @throws AttributeNotExistsException
   * @throws WrongAttributeValueException
   * @throws MemberResourceMismatchException
   */
  void setRequiredAttributes(PerunSession sess, Facility facility, Resource resource, User user, Member member,
                             List<Attribute> attributes, boolean forceAttributesChecks)
      throws WrongAttributeAssignmentException, WrongReferenceAttributeValueException, AttributeNotExistsException,
                 WrongAttributeValueException, MemberResourceMismatchException;

  /**
   * Set all Attributes in list to "writable = true".
   *
   * @param sess
   * @param attributes
   * @return list of attributes
   */
  List<Attribute> setWritableTrue(PerunSession sess, List<Attribute> attributes);

  /**
   * Converts string into the Object defined by type.
   *
   * @param value
   * @param type
   * @return
   * @throws InternalErrorException
   */
  Object stringToAttributeValue(String value, String type);

  /**
   * Updates AttributeDefinition.
   *
   * @param perunSession
   * @param attributeDefinition
   * @return returns updated attributeDefinition
   * @throws InternalErrorException
   */
  AttributeDefinition updateAttributeDefinition(PerunSession perunSession, AttributeDefinition attributeDefinition);
}

