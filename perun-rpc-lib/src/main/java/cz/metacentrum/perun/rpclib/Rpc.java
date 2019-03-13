package cz.metacentrum.perun.rpclib;

import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.core.api.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.metacentrum.perun.core.api.exceptions.AttributeDefinitionExistsException;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ConsistencyErrorException;
import cz.metacentrum.perun.core.api.exceptions.DestinationAlreadyAssignedException;
import cz.metacentrum.perun.core.api.exceptions.DestinationNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityExistsException;
import cz.metacentrum.perun.core.api.exceptions.FacilityNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.GroupNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.HostNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.MemberNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.OwnerNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.core.api.exceptions.PrivilegeException;
import cz.metacentrum.perun.core.api.exceptions.RelationExistsException;
import cz.metacentrum.perun.core.api.exceptions.ResourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceExistsException;
import cz.metacentrum.perun.core.api.exceptions.ServiceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserExtSourceNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.UserNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.VoNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.rpclib.api.Deserializer;
import cz.metacentrum.perun.rpclib.api.RpcCaller;

public class Rpc {
	// VosManager
	public static class VosManager {
		public static Vo getVoById(RpcCaller rpcCaller, int id) throws VoNotExistsException, InternalErrorException, PrivilegeException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("id", new Integer(id));

			try {
				return rpcCaller.call("vosManager", "getVoById", params).read(Vo.class);
			} catch (VoNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static Vo getVoByShortName(RpcCaller rpcCaller, String shortName) throws VoNotExistsException, InternalErrorException, PrivilegeException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("shortName", shortName);

			try {
				return rpcCaller.call("vosManager", "getVoByShortName", params).read(Vo.class);
			} catch (VoNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static List<Vo> getVos(RpcCaller rpcCaller) throws InternalErrorException {
			try {
				return rpcCaller.call("vosManager", "getVos").readList(Vo.class);
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}
	}

	// AuditMessagesManager
	public static class AuditMessagesManager {
		public static List<PerunBean> parseLog(RpcCaller rpcCaller, String log) throws InternalErrorException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("log", log);

			try {
				return rpcCaller.call("auditMessagesManager", "parseLog", params).readList(PerunBean.class);
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static List<AuditMessage> pollConsumerMessagesForParser(RpcCaller rpcCaller, String consumerName) throws InternalErrorException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("consumerName", consumerName);

			try {
				Deserializer deserializer = rpcCaller.call("auditMessagesManager", "pollConsumerMessagesForParser", params);
				// FIXME - this is check to prevent NullPointerException caused by communication with RPC.
				if (deserializer == null) throw new RpcException(RpcException.Type.UNCATCHED_EXCEPTION, "Unable to create deserializer.");
				return deserializer.readList(AuditMessage.class);
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static void setLastProcessedId(RpcCaller rpcCaller, String consumerName, int lastProcessedId) throws InternalErrorException, PrivilegeException {
			Map<String, Object> params = new HashMap<>();
			params.put("consumerName", consumerName);
			params.put("lastProcessedId", new Integer(lastProcessedId));

			try {
				rpcCaller.call("auditMessagesManager", "setLastProcessedId", params);
			} catch (PrivilegeException | InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}
	}

	// FacilitiesManager
	public static class FacilitiesManager {
		public static Facility getFacilityById(RpcCaller rpcCaller, int id) throws InternalErrorException, PrivilegeException, FacilityNotExistsException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("id", new Integer(id));

			try {
				return rpcCaller.call("facilitiesManager", "getFacilityById", params).read(Facility.class);
			} catch (InternalErrorException e) {
				throw e;
			} catch (FacilityNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static Facility createFacility(RpcCaller rpcCaller, Facility facility) throws InternalErrorException, PrivilegeException, FacilityExistsException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("facility", facility);

			try {
				return rpcCaller.call("facilitiesManager", "createFacility", params).read(Facility.class);
			} catch (FacilityExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static void deleteFacility(RpcCaller rpcCaller, Facility facility) throws InternalErrorException, RelationExistsException, FacilityNotExistsException, PrivilegeException {
			Map<String, Object> params = new HashMap<String,Object>();
			params.put("facility", facility.getId());

			try {
				rpcCaller.call("facilitiesManager", "deleteFacility", params);
			} catch (PrivilegeException e) {
				throw e;
			} catch (FacilityNotExistsException e) {
				throw e;
			} catch (RelationExistsException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static List<Resource> getAssignedResources(RpcCaller rpcCaller, Facility facility) throws InternalErrorException, FacilityNotExistsException, PrivilegeException {
			Map<String, Object> params = new HashMap<String,Object>();
			params.put("facility", facility.getId());

			try {
				return rpcCaller.call("facilitiesManager", "getAssignedResources", params).readList(Resource.class);
			} catch (PrivilegeException e) {
				throw e;
			} catch (FacilityNotExistsException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static Host getHostById(RpcCaller rpcCaller, int id) throws InternalErrorException, PrivilegeException, HostNotExistsException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("id", id);

			try {
				return rpcCaller.call("facilitiesManager", "getHostById", params).read(Host.class);
			} catch (HostNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static Facility getFacilityForHost(RpcCaller rpcCaller, Host host) throws InternalErrorException, PrivilegeException, HostNotExistsException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("host", host.getId());

			try {
				return rpcCaller.call("facilitiesManager", "getFacilityForHost", params).read(Facility.class);
			} catch (HostNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}
	}

	// GroupsManager
	public static class GroupsManager {
		public static Group getGroupById(RpcCaller rpcCaller, int id) throws GroupNotExistsException, InternalErrorException, PrivilegeException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("id", new Integer(id));

			try {
				return rpcCaller.call("groupsManager", "getGroupById", params).read(Group.class);
			} catch (GroupNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static List<Group> getMemberGroups(RpcCaller rpcCaller, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("member", member.getId());

			try {
				return rpcCaller.call("groupsManager", "getMemberGroups", params).readList(Group.class);
			} catch (MemberNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static List<Group> getAllMemberGroups(RpcCaller rpcCaller, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("member", member.getId());

			try {
				return rpcCaller.call("groupsManager", "getAllMemberGroups", params).readList(Group.class);
			} catch (MemberNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static List<Group> getAllGroupsWhereMemberIsActive(RpcCaller rpcCaller, Member member) throws InternalErrorException, PrivilegeException, MemberNotExistsException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("member", member.getId());

			try {
				return rpcCaller.call("groupsManager", "getAllGroupsWhereMemberIsActive", params).readList(Group.class);
			} catch (MemberNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}
	}

	// ResourcesManager
	public static class ResourcesManager {
		public static Resource getResourceById(RpcCaller rpcCaller, int id) throws InternalErrorException, PrivilegeException, ResourceNotExistsException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("id", new Integer(id));

			try {
				return rpcCaller.call("resourcesManager", "getResourceById", params).read(Resource.class);
			} catch (ResourceNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static List<Resource> getAssignedResources(RpcCaller rpcCaller, Group group) throws InternalErrorException, GroupNotExistsException, PrivilegeException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("group", group.getId());

			try {
				return rpcCaller.call("resourcesManager", "getAssignedResources", params).readList(Resource.class);
			} catch (GroupNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static Facility getFacility(RpcCaller rpcCaller, Resource resource) throws InternalErrorException, ResourceNotExistsException, PrivilegeException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("resource", resource.getId());

			try {
				return rpcCaller.call("resourcesManager", "getFacility", params).read(Facility.class);
			} catch (ResourceNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static List<Service> getAssignedServices(RpcCaller rpcCaller, Resource resource) throws InternalErrorException, ResourceNotExistsException, PrivilegeException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("resource", resource.getId());

			try {
				return rpcCaller.call("resourcesManager", "getAssignedServices", params).readList(Service.class);
			} catch (ResourceNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static List<Resource> getAllowedResources(RpcCaller rpcCaller, Member member) throws InternalErrorException, MemberNotExistsException, PrivilegeException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("member", member.getId());

			try {
				return rpcCaller.call("resourcesManager", "getAllowedResources", params).readList(Resource.class);
			} catch (MemberNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}
	}

	// ServicesManager
	public static class ServicesManager {
		public static Destination addDestination(RpcCaller rpcCaller, Service service, Facility facility, Destination destination) throws PrivilegeException, InternalErrorException,
					 ServiceNotExistsException, FacilityNotExistsException, DestinationAlreadyAssignedException {

						 Map<String, Object> params = new HashMap<String, Object>();
						 params.put("service", service.getId());
						 params.put("facility", facility.getId());
						 params.put("destination", destination.getDestination());
						 params.put("type", destination.getType());

						 try {
							 return rpcCaller.call("servicesManager", "addDestination", params).read(Destination.class);
						 } catch (DestinationAlreadyAssignedException e) {
							 throw e;
						 } catch (FacilityNotExistsException e) {
							 throw e;
						 } catch (ServiceNotExistsException e) {
							 throw e;
						 } catch (PrivilegeException e) {
							 throw e;
						 } catch (InternalErrorException e) {
							 throw e;
						 } catch (PerunException e) {
							 throw new ConsistencyErrorException(e);
						 }
		}

		public static Service createService(RpcCaller rpcCaller, Service service) throws InternalErrorException, PrivilegeException, OwnerNotExistsException, ServiceExistsException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("service", service);

			try {
				return rpcCaller.call("servicesManager", "createService", params).read(Service.class);
			} catch (ServiceExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static void deleteService(RpcCaller rpcCaller, Service service) throws InternalErrorException, ServiceNotExistsException, PrivilegeException, RelationExistsException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("service", service.getId());

			try {
				rpcCaller.call("servicesManager", "deleteService", params);
			} catch (RelationExistsException e) {
				throw e;
			} catch (ServiceNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static List<Destination> getDestinations(RpcCaller rpcCaller, Service service, Facility facility) throws PrivilegeException, InternalErrorException, ServiceNotExistsException,
					 FacilityNotExistsException {

						 Map<String, Object> params = new HashMap<String, Object>();
						 params.put("service", service.getId());
						 params.put("facility", facility.getId());

						 try {
							 return rpcCaller.call("servicesManager", "getDestinations", params).readList(Destination.class);
						 } catch (FacilityNotExistsException e) {
							 throw e;
						 } catch (ServiceNotExistsException e) {
							 throw e;
						 } catch (PrivilegeException e) {
							 throw e;
						 } catch (InternalErrorException e) {
							 throw e;
						 } catch (PerunException e) {
							 throw new ConsistencyErrorException(e);
						 }
		}

		public static Service getServiceById(RpcCaller rpcCaller, int id) throws InternalErrorException, PrivilegeException, ServiceNotExistsException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("id", new Integer(id));

			try {
				return rpcCaller.call("servicesManager", "getServiceById", params).read(Service.class);
			} catch (ServiceNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static List<Service> getServices(RpcCaller rpcCaller) throws InternalErrorException, PrivilegeException {
			try {
				return rpcCaller.call("servicesManager", "getServices").readList(Service.class);
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static void removeAllDestinations(RpcCaller rpcCaller, Service service, Facility facility) throws PrivilegeException, InternalErrorException, ServiceNotExistsException,
					 FacilityNotExistsException {

						 Map<String, Object> params = new HashMap<String, Object>();
						 params.put("service", service.getId());
						 params.put("facility", facility.getId());

						 try {
							 rpcCaller.call("servicesManager", "removeAllDestinations", params);
						 } catch (FacilityNotExistsException e) {
							 throw e;
						 } catch (ServiceNotExistsException e) {
							 throw e;
						 } catch (PrivilegeException e) {
							 throw e;
						 } catch (InternalErrorException e) {
							 throw e;
						 } catch (PerunException e) {
							 throw new ConsistencyErrorException(e);
						 }
		}

		public static void updateService(RpcCaller rpcCaller, Service service) throws InternalErrorException, ServiceNotExistsException, PrivilegeException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("service", service);

			try {
				rpcCaller.call("servicesManager", "updateService", params);
			} catch (ServiceNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static int getDestinationIdByName(RpcCaller rpcCaller, String name) throws InternalErrorException, DestinationNotExistsException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("name", name);

			try {
				return rpcCaller.call("servicesManager", "getDestinationIdByName", params).readInt();
			} catch (DestinationNotExistsException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static List<Service> getAssignedServices(RpcCaller rpcCaller, Facility facility) throws InternalErrorException, FacilityNotExistsException, PrivilegeException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("facility", facility.getId());

			try {
				return rpcCaller.call("servicesManager", "getAssignedServices", params).readList(Service.class);
			} catch (PrivilegeException e) {
				throw e;
			} catch (FacilityNotExistsException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}
	}

	// AttributesManager
	public static class AttributesManager {
		public static AttributeDefinition getAttributeDefinitionById(RpcCaller rpcCaller, int id) throws PrivilegeException, InternalErrorException, AttributeNotExistsException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("id", new Integer(id));

			try {
				return rpcCaller.call("attributesManager", "getAttributeDefinitionById", params).read(AttributeDefinition.class);
			} catch (AttributeNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static List<AttributeDefinition> getAttributesDefinitionByNamespace(RpcCaller rpcCaller, String namespace) throws PrivilegeException, InternalErrorException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("namespace", namespace);

			try {
				return rpcCaller.call("attributesManager", "getAttributesDefinitionByNamespace", params).readList(AttributeDefinition.class);
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static AttributeDefinition getAttributeDefinition(RpcCaller rpcCaller, String attributeName) throws PrivilegeException, InternalErrorException, AttributeNotExistsException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("attributeName", attributeName);

			try {
				return rpcCaller.call("attributesManager", "getAttributeDefinition", params).read(AttributeDefinition.class);
			} catch (AttributeNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static AttributeDefinition createAttribute(RpcCaller rpcCaller, AttributeDefinition attributeDefinition) throws PrivilegeException, InternalErrorException, AttributeDefinitionExistsException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("attribute", attributeDefinition);

			try {
				return rpcCaller.call("attributesManager", "createAttribute", params).read(AttributeDefinition.class);
			} catch (AttributeDefinitionExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static Attribute getAttribute(RpcCaller rpcCaller, Facility facility, String attributeName) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, FacilityNotExistsException, WrongAttributeAssignmentException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("facility", facility.getId());
			params.put("attributeName", attributeName);

			try {
				return rpcCaller.call("attributesManager", "getAttribute", params).read(Attribute.class);
			} catch (AttributeNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (FacilityNotExistsException e) {
				throw e;
			} catch (WrongAttributeAssignmentException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static Attribute getAttribute(RpcCaller rpcCaller, Vo vo, String attributeName) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, VoNotExistsException, WrongAttributeAssignmentException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("vo", vo.getId());
			params.put("attributeName", attributeName);

			try {
				return rpcCaller.call("attributesManager", "getAttribute", params).read(Attribute.class);
			} catch (AttributeNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (VoNotExistsException e) {
				throw e;
			} catch (WrongAttributeAssignmentException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static Attribute getAttribute(RpcCaller rpcCaller, User user, String attributeName) throws PrivilegeException, InternalErrorException, AttributeNotExistsException, UserNotExistsException, WrongAttributeAssignmentException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("user", user.getId());
			params.put("attributeName", attributeName);

			try {
				return rpcCaller.call("attributesManager", "getAttribute", params).read(Attribute.class);
			} catch (AttributeNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (UserNotExistsException e) {
				throw e;
			} catch (WrongAttributeAssignmentException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static List<Attribute> getAttributes(RpcCaller rpcCaller, Member member) throws PrivilegeException, InternalErrorException, MemberNotExistsException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("member", member.getId());

			try {
				return rpcCaller.call("attributesManager", "getAttributes", params).readList(Attribute.class);
			} catch (MemberNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static List<Attribute> getLogins(RpcCaller rpcCaller, User user)
			throws PrivilegeException, InternalErrorException, UserNotExistsException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("user", user.getId());

			try {
				return rpcCaller.call("attributesManager", "getLogins", params).readList(Attribute.class);
			} catch (UserNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static List<AttributeDefinition> getRequiredAttributesDefinition(RpcCaller rpcCaller, Service service)
			throws InternalErrorException, PrivilegeException, ServiceNotExistsException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("service", service.getId());

			try {
				return rpcCaller.call("attributesManager", "getRequiredAttributesDefinition", params).readList(AttributeDefinition.class);
			} catch (ServiceNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static void setAttribute(RpcCaller rpcCaller, Member member, Attribute attribute)
			throws PrivilegeException, InternalErrorException, MemberNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("member", member.getId());
			params.put("attribute", attribute);

			try {
				rpcCaller.call("attributesManager", "setAttribute", params);
			} catch (WrongReferenceAttributeValueException e) {
				throw e;
			} catch (WrongAttributeAssignmentException e) {
				throw e;
			} catch (WrongAttributeValueException e) {
				throw e;
			} catch (AttributeNotExistsException e) {
				throw e;
			} catch (MemberNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static void setAttribute(RpcCaller rpcCaller, User user, Attribute attribute)
			throws PrivilegeException, InternalErrorException, UserNotExistsException, AttributeNotExistsException, WrongAttributeValueException, WrongAttributeAssignmentException, WrongReferenceAttributeValueException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("user", user.getId());
			params.put("attribute", attribute);

			try {
				rpcCaller.call("attributesManager", "setAttribute", params);
			} catch (WrongReferenceAttributeValueException e) {
				throw e;
			} catch (WrongAttributeAssignmentException e) {
				throw e;
			} catch (WrongAttributeValueException e) {
				throw e;
			} catch (AttributeNotExistsException e) {
				throw e;
			} catch (UserNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}
	}

	// MembersManager
	public static class MembersManager {
		public static Member getMemberById(RpcCaller rpcCaller, int id) throws PrivilegeException, InternalErrorException, MemberNotExistsException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("id", new Integer(id));

			try {
				return rpcCaller.call("membersManager", "getMemberById", params).read(Member.class);
			} catch (MemberNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static Member getMemberByUser(RpcCaller rpcCaller, Vo vo, User user) throws PrivilegeException, InternalErrorException, MemberNotExistsException, UserNotExistsException, VoNotExistsException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("vo", vo.getId());
			params.put("user", user.getId());

			try {
				return rpcCaller.call("membersManager", "getMemberByUser", params).read(Member.class);
			} catch (VoNotExistsException e) {
				throw e;
			} catch (UserNotExistsException e) {
				throw e;
			} catch (MemberNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static RichMember getRichMemberWithAttributes(RpcCaller rpcCaller, Member member) throws PrivilegeException, InternalErrorException, MemberNotExistsException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("id", member.getId());

			try {
				return rpcCaller.call("membersManager", "getRichMemberWithAttributes", params).read(RichMember.class);
			} catch (MemberNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static List<Member> findMembersByName(RpcCaller rpcCaller, String searchString) throws InternalErrorException, PrivilegeException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("searchString", searchString);

			try {
				return rpcCaller.call("membersManager", "findMembersByName", params).readList(Member.class);
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}
	}

	// OwnersManager
	public static class OwnersManager {
		public static Owner getOwnerById(RpcCaller rpcCaller, int id) throws OwnerNotExistsException, InternalErrorException, PrivilegeException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("id", id);

			try {
				return rpcCaller.call("ownersManager", "getOwnerById", params).read(Owner.class);
			} catch (OwnerNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static Owner createOwner(RpcCaller rpcCaller, Owner owner) throws PrivilegeException, InternalErrorException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("owner", owner);

			try {
				return rpcCaller.call("ownersManager", "createOwner", params).read(Owner.class);
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static List<Owner> getOwners(RpcCaller rpcCaller) throws PrivilegeException, InternalErrorException {
			try {
				return rpcCaller.call("ownersManager", "getOwners").readList(Owner.class);
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static void deleteOwner(RpcCaller rpcCaller, Owner owner) throws OwnerNotExistsException, InternalErrorException, PrivilegeException, RelationExistsException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("owner", owner.getId());

			try {
				rpcCaller.call("ownersManager", "deleteOwner");
			} catch (PrivilegeException e) {
				throw e;
			} catch (OwnerNotExistsException e) {
				throw e;
			} catch (RelationExistsException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}
	}

	// UsersManager
	public static class UsersManager {
		public static List<User> getUsers(RpcCaller rpcCaller) throws InternalErrorException, PrivilegeException {
			try {
				return rpcCaller.call("usersManager", "getUsers").readList(User.class);
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static User getUserByMember(RpcCaller rpcCaller, Member member) throws InternalErrorException, MemberNotExistsException, PrivilegeException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("member", member.getId());

			try {
				return rpcCaller.call("usersManager", "getUserByMember", params).read(User.class);
			} catch (MemberNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static User getUserById(RpcCaller rpcCaller, int id) throws InternalErrorException, UserNotExistsException, PrivilegeException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("id", new Integer(id));

			try {
				return rpcCaller.call("usersManager", "getUserById", params).read(User.class);
			} catch (UserNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static List<User> findUsersByName(RpcCaller rpcCaller, String titleBefore, String firstName, String middleName, String lastName, String titleAfter)
			throws InternalErrorException, UserNotExistsException,
											PrivilegeException {
							 Map<String, Object> params = new HashMap<String, Object>();
							 params.put("titleBefore", titleBefore);
							 params.put("firstName", firstName);
							 params.put("middleName", middleName);
							 params.put("lastName", lastName);
							 params.put("titleAfter", titleAfter);

							 try {
								 return rpcCaller.call("usersManager", "findUsersByName", params).readList(User.class);
							 } catch (UserNotExistsException e) {
								 throw e;
							 } catch (PrivilegeException e) {
								 throw e;
							 } catch (InternalErrorException e) {
								 throw e;
							 } catch (PerunException e) {
								 throw new ConsistencyErrorException(e);
							 }
		}

		public static User getUserByUserExtSource(RpcCaller rpcCaller, UserExtSource userExtSource)
			throws InternalErrorException, UserNotExistsException, UserExtSourceNotExistsException, PrivilegeException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("userExtSource", userExtSource);

			try {
				return rpcCaller.call("usersManager", "getUserByUserExtSource", params).read(User.class);
			} catch (UserNotExistsException e) {
				throw e;
			} catch (UserExtSourceNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static UserExtSource getUserExtSourceByExtLogin(RpcCaller rpcCaller, ExtSource source, String extLogin) throws InternalErrorException,
					 PrivilegeException, ExtSourceNotExistsException, UserExtSourceNotExistsException {
						 Map<String, Object> params = new HashMap<String, Object>();
						 params.put("extSource", source);
						 params.put("extSourceLogin", extLogin);

						 try {
							 return rpcCaller.call("usersManager", "getUserExtSourceByExtLogin", params).read(UserExtSource.class);
						 } catch (ExtSourceNotExistsException e) {
							 throw e;
						 } catch (UserExtSourceNotExistsException e) {
							 throw e;
						 } catch (PrivilegeException e) {
							 throw e;
						 } catch (InternalErrorException e) {
							 throw e;
						 } catch (PerunException e) {
							 throw new ConsistencyErrorException(e);
						 }
		}

		public static User getUserByExtSourceNameAndExtLogin(RpcCaller rpcCaller, String extSourceName, String extLogin)
			throws ExtSourceNotExistsException, UserExtSourceNotExistsException, UserNotExistsException, InternalErrorException, PrivilegeException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("extSourceName", extSourceName);
			params.put("extLogin", extLogin);

			try {
				return rpcCaller.call("usersManager", "getUserByExtSourceNameAndExtLogin", params).read(User.class);
			} catch (ExtSourceNotExistsException e) {
				throw e;
			} catch (UserExtSourceNotExistsException e) {
				throw e;
			} catch (UserNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static User updateUser(RpcCaller rpcCaller, User user)
			throws InternalErrorException, UserNotExistsException, PrivilegeException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("user", user);

			try {
				return rpcCaller.call("usersManager", "updateUser", params).read(User.class);
			} catch (UserNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}

		public static List<Resource> getAllowedResources(RpcCaller rpcCaller, User user) throws InternalErrorException, UserNotExistsException, PrivilegeException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("user", user.getId());

			try {
				return rpcCaller.call("usersManager", "getAllowedResources", params).readList(Resource.class);
			} catch (UserNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}
	}

	// ExtSourcesManager
	public static class ExtSourcesManager {
		public static ExtSource getExtSourceByName(RpcCaller rpcCaller, String name)
			throws InternalErrorException, ExtSourceNotExistsException, PrivilegeException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("name", name);

			try {
				return rpcCaller.call("extSourcesManager", "getExtSourceByName", params).read(ExtSource.class);
			} catch (ExtSourceNotExistsException e) {
				throw e;
			} catch (PrivilegeException e) {
				throw e;
			} catch (InternalErrorException e) {
				throw e;
			} catch (PerunException e) {
				throw new ConsistencyErrorException(e);
			}
		}
	}

	// GeneralServiceManager
	public static class GeneralServiceManager {

		public static void blockServiceOnFacility(RpcCaller rpcCaller, Service service, Facility facility) throws InternalErrorException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("service", service.getId());
			params.put("facility", facility.getId());

			try {
				rpcCaller.call("generalServiceManager", "blockServiceOnFacility", params);
			} catch(InternalErrorException  e) {    throw e;
			} catch(PerunException e) {    throw new ConsistencyErrorException(e);
			}
		}

		public static void blockServiceOnDestination(RpcCaller rpcCaller, Service service, int destinationId) throws InternalErrorException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("service", service.getId());
			params.put("destination", destinationId);

			try {
				rpcCaller.call("generalServiceManager", "blockServiceOnDestination", params);
			} catch(InternalErrorException  e) {    throw e;
			} catch(PerunException e) {    throw new ConsistencyErrorException(e);
			}
		}

		public static void freeAllDenialsOnFacility(RpcCaller rpcCaller, Facility facility) throws InternalErrorException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("facility", facility.getId());

			try {
				rpcCaller.call("generalServiceManager", "unblockAllServicesOnFacility", params);
			} catch(PerunException e) {    throw new ConsistencyErrorException(e);
			}
		}

		public static void freeAllDenialsOnDestination(RpcCaller rpcCaller, int destinationId) throws InternalErrorException {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("destination", destinationId);

			try {
				rpcCaller.call("generalServiceManager", "unblockAllServicesOnDestination", params);
			} catch(PerunException e) {    throw new ConsistencyErrorException(e);
			}
		}

	}
}
