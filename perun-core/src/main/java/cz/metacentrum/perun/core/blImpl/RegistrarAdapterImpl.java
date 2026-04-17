package cz.metacentrum.perun.core.blImpl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.InvalidLoginException;
import cz.metacentrum.perun.core.api.exceptions.PasswordDeletionFailedException;
import cz.metacentrum.perun.core.api.exceptions.PasswordOperationTimeoutException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.RegistrarAdapter;
import cz.metacentrum.perun.registrar.model.ApplicationForm;
import cz.metacentrum.perun.registrar.openapi.IdmMessagesApi;
import cz.metacentrum.perun.registrar.openapi.invoker.ApiClient;
import cz.metacentrum.perun.registrar.openapi.model.ConsolidateUserDTO;
import cz.metacentrum.perun.registrar.openapi.model.IdmMemberDTO;
import cz.metacentrum.perun.registrar.openapi.model.IdmObject;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistrarAdapterImpl implements RegistrarAdapter {

  private static final String USE_NEW_ATTR_GROUP = AttributesManager.NS_GROUP_ATTR_DEF + ":useNewRegistration";
  private static final String USE_NEW_ATTR_VO = AttributesManager.NS_VO_ATTR_DEF + ":useNewRegistration";
  private static final Logger LOG = LoggerFactory.getLogger(RegistrarAdapterImpl.class);

  private PerunBl perunBl;
  private IdmMessagesApi registrarApi;
  private ApiClient apiClient;


  public void init() {
    String apiUrl = BeansUtils.getCoreConfig().getRegistrarApiUrl();
    if (apiUrl == null || apiUrl.isEmpty()) {
      LOG.debug("Registrar API URL not configured, skipping RegistrarAdapter initialization");
      return;
    }
    this.apiClient = new ApiClient();
    this.apiClient.setBasePath(apiUrl);
    this.apiClient.addDefaultHeader("X-API-Key", BeansUtils.getCoreConfig().getRegistrarApiSecret());
    registrarApi = new IdmMessagesApi(this.apiClient);
  }

  public void setPerunBl(PerunBl perunBl) {
    this.perunBl = perunBl;
  }


  @Override
  public void onDeleteUser(PerunSession sess, User user) {
    if (registrarApi != null) {
      registrarApi.onDeleteIdmUser(String.valueOf(user.getId()))
          .doOnError((err) -> LOG.error("Failed to notify Registrar on user delete", err))
          .subscribe();
    }
    // delete all users applications and submitted data, this is needed only when 'anonymizeInstead'
    // because applications are deleted on cascade when user's row is deleted in DB
    perunBl.getUsersManagerBl().deleteUsersApplications(sess, user);
  }

  @Override
  public void onDeleteMember(PerunSession sess, Member member) {
    // do the new registrar logic first, since the old removes more broadly (e.g. all reserved
    // logins for user object of the member;
    if (registrarApi != null) {
      IdmObject idmObject = new IdmObject();
      idmObject.setIdmObjectType(IdmObject.IdmObjectTypeEnum.VO);
      idmObject.setObjectId(String.valueOf(member.getVoId()));
      IdmMemberDTO idmMemberDTO = new IdmMemberDTO();
      idmMemberDTO.setIdmObject(idmObject);
      idmMemberDTO.setUserId(String.valueOf(member.getUserId()));
      registrarApi.onDeleteIdmMember(idmMemberDTO)
          .doOnError((err) -> LOG.error("Failed to notify Registrar on member delete", err))
          .subscribe();
    }

    perunBl.getMembersManagerBl().rejectAllMemberOpenApplications(sess, member);
  }

  @Override
  public void onRemoveMemberFromGroup(PerunSession sess, Group group, Member member) {

  }

  @Override
  public void onDeleteGroup(PerunSession sess, Group group) {
    if (registrarApi != null) {
      IdmObject idmObject = new IdmObject();
      idmObject.setIdmObjectType(IdmObject.IdmObjectTypeEnum.GROUP);
      idmObject.setObjectId(String.valueOf(group.getId()));
      registrarApi.onDeleteIdmObject(idmObject)
          .doOnError((err) -> LOG.error("Failed to notify Registrar on group delete", err))
          .subscribe();
    }
    // todo determine the process of switching between registrar (e.g. don't allow if apps exist, keep old apps, etc.)
    //  and then we can decide whether to perform both operations or decide based on `useNew` attr
    // delete all Groups reserved logins from KDC and DB
    List<Integer> list = perunBl.getGroupsManagerBl().getGroupApplicationIds(sess, group);
    for (Integer appId : list) {
      // for each application
      try {
        perunBl.getUsersManagerBl().deleteReservedLoginsOnlyByGivenApp(sess, appId);
      } catch (InvalidLoginException e) {
        throw new InternalErrorException(
            "We are deleting reserved login from group applications, but its syntax is not allowed by namespace " +
            "configuration.", e);
      } catch (PasswordOperationTimeoutException ex) {
        throw new InternalErrorException("Failed to delete reserved login " + ex.getLogin() + " from KDC.", ex);
      } catch (PasswordDeletionFailedException ex) {
        throw new InternalErrorException("Failed to delete reserved login " + ex.getLogin() + " from KDC.", ex);
      }
    }
  }

  @Override
  public void onDeleteVo(PerunSession sess, Vo vo)
      throws PasswordOperationTimeoutException, InvalidLoginException, PasswordDeletionFailedException {
    if (registrarApi != null) {
      IdmObject idmObject = new IdmObject();
      idmObject.setIdmObjectType(IdmObject.IdmObjectTypeEnum.VO);
      idmObject.setObjectId(String.valueOf(vo.getId()));
      registrarApi.onDeleteIdmObject(idmObject)
          .doOnError((err) -> LOG.error("Failed to notify Registrar on VO delete", err))
          .subscribe();
    }

    // delete all VO reserved logins from KDC and DB
    List<Integer> list = perunBl.getVosManagerBl().getVoApplicationIds(sess, vo);
    for (Integer appId : list) {
      perunBl.getUsersManagerBl().deleteReservedLoginsOnlyByGivenApp(sess, appId);
    }
  }

  @Override
  public void onDeleteAttributeDefinition(PerunSession sess, AttributeDefinition attributeDefinition)
      throws RelationExistsException {
    if (registrarApi != null) {
      List<UUID> formIds = registrarApi.checkIdmAttributeUsed(attributeDefinition.getFriendlyName())
              .collectList().block();
      if (formIds != null && !formIds.isEmpty()) {
        throw new RelationExistsException("Attribute " + attributeDefinition.getFriendlyName() + " is used in open " +
                                              "applications in the following forms (New Registrar): " + formIds);
      }
    }
    //Check relation to any application form as a source or destination attribute
    List<ApplicationForm> applicationForms = perunBl.getAttributesManagerBl().getAppFormsWhereAttributeRelated(sess,
        attributeDefinition);
    if (!applicationForms.isEmpty()) {
      throw new RelationExistsException("Attribute definition with id: " + attributeDefinition.getId() +
                                            " has a relation (as a source or destination attribute)" +
                                            " to these application form items: " +
                                            applicationForms.stream().map(appForm -> {
                                              String message =
                                                  "Application form items: " +
                                                      perunBl.getAttributesManagerBl()
                                                          .getAppFormItemsForAppFormAndAttribute(sess, appForm.getId(),
                                                          attributeDefinition);
                                              message += appForm.getGroup() == null ?
                                                             " in the application form in VO with id: " +
                                                                 appForm.getVo().getId() :
                                                             " in the application form in Group with id: " +
                                                                 appForm.getGroup().getId() +
                                                                 " (under Vo with id: " + appForm.getVo().getId() + ")";
                                              return message;
                                            })
                                                .toList());
    }
  }

  @Override
  public void onUserIdentityJoined(PerunSession sess, UserExtSource userExtSource) {
    if (!userExtSource.getExtSource().getType().equals(ExtSourcesManager.EXTSOURCE_IDP)) {
      return;
    }

    if (registrarApi == null) {
      return;
    }

    ConsolidateUserDTO consolidateUserDTO = new ConsolidateUserDTO();
    consolidateUserDTO.setIdentityIdentifier(userExtSource.getLogin());
    consolidateUserDTO.setIdentityIssuer(userExtSource.getExtSource().getName());
    consolidateUserDTO.setUserId(String.valueOf(userExtSource.getUserId()));

    registrarApi.userConsolidated(consolidateUserDTO)
          .doOnError((err) -> LOG.error("Failed to notify Registrar on user identity consolidation", err))
        .subscribe();
  }

  @Override
  public String getInviteUrlForVo(Vo vo) {
    if (registrarApi == null) {
      return "";
    }
    return registrarApi.inviteUrl("VO", String.valueOf(vo.getId()), null)
               .block();
  }

  @Override
  public String getInviteUrlForGroup(Group group) {
    if (registrarApi == null) {
      return "";
    }
    return registrarApi.inviteUrl("GROUP", String.valueOf(group.getId()), null)
               .block();
  }

  private boolean doesGroupUseNewRegistration(PerunSession sess, Group group) {
    Attribute attr;
    try {
      attr = perunBl.getAttributesManagerBl().getAttribute(sess, group, USE_NEW_ATTR_GROUP);
    } catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
      LOG.debug("Error thrown while checking registrar usage for group {}, continuing with old registrar",
          group, e);
      return false;
    }

    if (attr == null || attr.getValue() == null) {
      return false;
    }
    return attr.valueAsBoolean();
  }

  private boolean doesVoUseNewRegistration(PerunSession sess, Vo vo) {
    Attribute attr;
    try {
      attr = perunBl.getAttributesManagerBl().getAttribute(sess, vo, USE_NEW_ATTR_VO);
    } catch (WrongAttributeAssignmentException | AttributeNotExistsException e) {
      LOG.debug("Error thrown while checking registrar usage for VO {}, continuing with old registrar",
          vo, e);
      return false;
    }

    if (attr == null || attr.getValue() == null) {
      return false;
    }
    return attr.valueAsBoolean();

  }
}
