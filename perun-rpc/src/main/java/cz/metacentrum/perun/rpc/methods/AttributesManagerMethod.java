package cz.metacentrum.perun.rpc.methods;

import java.util.ArrayList;
import java.util.List;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.rpc.RpcException;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

public enum AttributesManagerMethod implements ManagerMethod {

	/*#
	 * Returns User-Facility attributes. Returns only non-empty attributes.

	 * @param facility int Facility ID
	 * @param user int User ID
	 * @return List<Attribute> Attributes
	 */
	/*#
	 * Returns Facility attributes. Returns only non-empty attributes.
	 *
	 * @param facility int Facility ID
	 * @return List<Attribute> Attributes
	 */
	/*#
	 * Returns all VO attributes. Returns only non-empty attributes.
	 *
	 * @param vo int VO ID
	 * @return List<Attribute> Attributes
	 */
	/*#
	 * Returns chosen VO attributes. Returns only non-empty attributes.
	 *
	 * @param vo int VO ID
	 * @param attrNames List<String> Attribute names
	 * @return List<Attribute> Attributes
	 */
	/*#
	 * Returns Member-Resource attributes. Returns only non-empty attributes.
	 *
	 * @param member int Member ID
	 * @param resource int Resource ID
	 * @return List<Attribute> Attributes
	 */
	/*#
	 * Returns Group-Resource attributes. Returns only non-empty attributes.
	 *
	 * @param group int Group ID
	 * @param resource int Resource ID
	 * @return List<Attribute> Attributes
	 */
	/*#
	 * Returns all Group-Resource attributes. Returns only non-empty attributes. Returns also group attributes if workWithGroupAttributes == 1.
	 *
	 * @param group int Group ID
	 * @param resource int Resource ID
	 * @param workWithGroupAttributes int Work with group attributes integer (1 = true, 0 = false). 0 is default value.
	 * @return List<Attribute> Attributes
	 */
	/*#
	 * Returns Resource attributes. Returns only non-empty attributes.
	 *
	 * @param resource int Resource ID
	 * @return List<Attribute> Attributes
	 */
	/*#
	 * Returns all Member attributes. Returns only non-empty attributes.
	 *
	 * @param member int Member ID
	 * @return List<Attribute> Attributes
	 */
	/*#
	 * Returns all Member attributes. Returns only non-empty attributes. Returns also user attributes if workWithUserAttributes == 1.
	 *
	 * @param member int Member ID
	 * @param workWithUserAttributes int  Work with user attributes integer (1 = true, 0 = false). 0 is default value.
	 * @return List<Attribute> Attributes
	 */
	/*#
	 * Returns chosen Member attributes. Returns only non-empty attributes.
	 *
	 * @param member int Member ID
	 * @param attrNames[] List<String> Attribute names
	 * @return List<Attribute> Attributes
	 */
	/*#
	 * Returns all User attributes. Returns only non-empty attributes.
	 *
	 * @param user int User ID
	 * @return List<Attribute> Attributes
	 */
	/*#
	 * Returns chosen User attributes. Returns only non-empty attributes.
	 *
	 * @param user int User ID
	 * @param attrNames[] List<String> Attribute names
	 * @return List<Attribute> Attributes
	 */
	/*#
	 * Returns Group attributes. Returns only non-empty attributes.
	 *
	 * @param group int Group ID
	 * @return List<Attribute> Attributes
	 */
	/*#
	 * Returns Host attributes. Returns only non-empty attributes.
	 *
	 * @param host int Host ID
	 * @return List<Attribute> Attributes
	 */
	getAttributes {

		@Override
		public List<Attribute> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("facility")) {
				if(parms.contains("user")) {
					return ac.getAttributesManager().getAttributes(ac.getSession(),
							ac.getFacilityById(parms.readInt("facility")),
							ac.getUserById(parms.readInt("user")));
				} else {
					return ac.getAttributesManager().getAttributes(ac.getSession(),
							ac.getFacilityById(parms.readInt("facility")));
				}
			} else if (parms.contains("vo")) {
				if (parms.contains("attrNames[]")) {
					return ac.getAttributesManager().getAttributes(ac.getSession(),
							ac.getVoById(parms.readInt("vo")),
							parms.readList("attrNames", String.class));
				} else {
					return ac.getAttributesManager().getAttributes(ac.getSession(),
							ac.getVoById(parms.readInt("vo")));
				}
			} else if (parms.contains("resource")) {
				if (parms.contains("member")) {
					return ac.getAttributesManager().getAttributes(ac.getSession(),
							ac.getResourceById(parms.readInt("resource")),
							ac.getMemberById(parms.readInt("member")));
				}  else if (parms.contains("group")) {
					if (parms.contains("workWithGroupAttributes")) {
						return ac.getAttributesManager().getAttributes(ac.getSession(),
								ac.getResourceById(parms.readInt("resource")),
								ac.getGroupById(parms.readInt("group")),
								parms.readInt("workWithGroupAttributes") == 1);
					} else {
						return ac.getAttributesManager().getAttributes(ac.getSession(),
								ac.getResourceById(parms.readInt("resource")),
								ac.getGroupById(parms.readInt("group")));
					}
				} else {
					return ac.getAttributesManager().getAttributes(ac.getSession(),
							ac.getResourceById(parms.readInt("resource")));
				}
			} else if (parms.contains("member")) {
				if (parms.contains("workWithUserAttributes")){
					if (parms.contains("attrNames[]")) {
						return ac.getAttributesManager().getAttributes(ac.getSession(),
								ac.getMemberById(parms.readInt("member")),
								parms.readList("attrNames", String.class),
								parms.readInt("workWithUserAttributes") == 1);
					} else {
						return ac.getAttributesManager().getAttributes(ac.getSession(),
								ac.getMemberById(parms.readInt("member")), parms.readInt("workWithUserAttributes") == 1);
					}
				} else if (parms.contains("attrNames[]")) {
					return ac.getAttributesManager().getAttributes(ac.getSession(),
							ac.getMemberById(parms.readInt("member")),
							parms.readList("attrNames", String.class));
				} else {
					return ac.getAttributesManager().getAttributes(ac.getSession(),
							ac.getMemberById(parms.readInt("member")));
				}
			} else if (parms.contains("user")) {
				if (parms.contains("attrNames[]")) {
					return ac.getAttributesManager().getAttributes(ac.getSession(),
							ac.getUserById(parms.readInt("user")),
							parms.readList("attrNames", String.class));
				} else {
					return ac.getAttributesManager().getAttributes(ac.getSession(),
							ac.getUserById(parms.readInt("user")));
				}
			} else if (parms.contains("group")) {
				if (parms.contains("attrNames[]")) {
					return ac.getAttributesManager().getAttributes(ac.getSession(),
							ac.getGroupById(parms.readInt("group")),
							parms.readList("attrNames", String.class));
				} else {
					return ac.getAttributesManager().getAttributes(ac.getSession(),
						ac.getGroupById(parms.readInt("group")));
				}
			} else if (parms.contains("host")) {
				return ac.getAttributesManager().getAttributes(ac.getSession(),
						ac.getHostById(parms.readInt("host")));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "facility, vo, resource, member, user, host or group");
			}
		}
	},
	/*#
	 * Returns all entityless attributes with attrName.
	 *
	 * @param attrName String Attribute name
	 * @return List<Attribute> Attributes
	 */
	getEntitylessAttributes {
		@Override
		public List<Attribute> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getAttributesManager().getEntitylessAttributes(ac.getSession(), parms.readString("attrName"));
		}
	},

	/*#
	 * Sets the attributes.
	 *
	 * @param facility int Facility ID
	 * @param user int User ID
	 * @param member int Member ID
	 * @param resource int Resource ID
	 * @param attributes List<Attribute> List of attributes
	 */
	/*#
	 * Sets the attributes.
	 *
	 * @param facility int Facility ID
	 * @param user int User ID
	 * @param attributes List<Attribute> List of attributes
	 */
	/*#
	 * Sets the attributes.
	 *
	 * @param facility int Facility ID
	 * @param attributes List<Attribute> List of attributes
	 */
	/*#
	 * Sets the attributes.
	 *
	 * @param vo int VO ID
	 * @param attributes List<Attribute> List of attributes
	 */
	/*#
	 * Sets the attributes.
	 *
	 * @param member int Member ID
	 * @param resource int Resource ID
	 * @param attributes List<Attribute> List of attributes
	 */
	/*#
	 * Sets the attributes.
	 *
	 * @param member int Member ID
	 * @param resource int Resource ID
	 * @param workWithUserAttributes int Work with user attributes integer (1 = true, 0 = false). 0 is default value.
	 * @param attributes List<Attribute> List of attributes
	 */
	/*#
	 * Sets the attributes.
	 *
	 * @param group int Group ID
	 * @param resource int Resource ID
	 * @param attributes List<Attribute> List of attributes
	 */
	/*#
	 * Sets the attributes.
	 *
	 * @param group int Group ID
	 * @param resource int Resource ID
	 * @param workWithGroupAttributes int Work with group attributes integer (1 = true, 0 = false). 0 is default value.
	 * @param attributes List<Attribute> List of attributes
	 */
	/*#
	 * Sets the attributes.
	 *
	 * @param resource int Resource ID
	 * @param attributes List<Attribute> List of attributes
	 */
	/*#
	 * Sets the attributes.
	 *
	 * @param member int Member ID
	 * @param workWithUserAttributes int Work with user attributes integer (1 = true, 0 = false). 0 is default value.
	 * @param attributes List<Attribute> List of attributes
	 */
	/*#
	 * Sets the attributes.
	 *
	 * @param member int Member ID
	 * @param attributes List<Attribute> List of attributes
	 */
	/*#
	 * Sets the attributes.
	 *
	 * @param user int User ID
	 * @param attributes List<Attribute> List of attributes
	 */

	/*#
	 * Sets the attributes.
	 *
	 * @param group int Group ID
	 * @param attributes List<Attribute> List of attributes
	 */
	/*#
	 * Sets the attributes.
	 *
	 * @param host int Host ID
	 * @param attributes List<Attribute> List of attributes
	 */
	setAttributes {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("facility")) {
				if(parms.contains("user")) {
					if (parms.contains("member") && parms.contains("resource")) {
						ac.getAttributesManager().setAttributes(ac.getSession(),
								ac.getFacilityById(parms.readInt("facility")),
								ac.getResourceById(parms.readInt("resource")),
								ac.getUserById(parms.readInt("user")),
								ac.getMemberById(parms.readInt("member")),
								parms.readList("attributes", Attribute.class));
					} else {
						ac.getAttributesManager().setAttributes(ac.getSession(),
								ac.getFacilityById(parms.readInt("facility")),
								ac.getUserById(parms.readInt("user")),
								parms.readList("attributes", Attribute.class));
					}
				} else {
					ac.getAttributesManager().setAttributes(ac.getSession(),
							ac.getFacilityById(parms.readInt("facility")),
							parms.readList("attributes", Attribute.class));
				}
			} else if (parms.contains("vo")) {
				ac.getAttributesManager().setAttributes(ac.getSession(),
						ac.getVoById(parms.readInt("vo")),
						parms.readList("attributes", Attribute.class));
			} else if (parms.contains("resource")) {
				if (parms.contains("member")) {
					if (parms.contains("workWithUserAttributes")) {
						ac.getAttributesManager().setAttributes(ac.getSession(),
								ac.getResourceById(parms.readInt("resource")),
								ac.getMemberById(parms.readInt("member")),
								parms.readList("attributes", Attribute.class),
								parms.readInt("workWithUserAttributes") == 1);
					} else {
						ac.getAttributesManager().setAttributes(ac.getSession(),
								ac.getResourceById(parms.readInt("resource")),
								ac.getMemberById(parms.readInt("member")),
								parms.readList("attributes", Attribute.class));
					}
				} else if (parms.contains("group")) {
					if (parms.contains("workWithGroupAttributes")) {
						ac.getAttributesManager().setAttributes(ac.getSession(),
								ac.getResourceById(parms.readInt("resource")),
								ac.getGroupById(parms.readInt("group")),
								parms.readList("attributes", Attribute.class),
								parms.readInt("workWithGroupAttributes") == 1);
					} else {
						ac.getAttributesManager().setAttributes(ac.getSession(),
								ac.getResourceById(parms.readInt("resource")),
								ac.getGroupById(parms.readInt("group")),
								parms.readList("attributes", Attribute.class));
					}
				} else {
					ac.getAttributesManager().setAttributes(ac.getSession(),
							ac.getResourceById(parms.readInt("resource")),
							parms.readList("attributes", Attribute.class));
				}
			} else if (parms.contains("member")) {
				if(parms.contains("workWithUserAttributes")){
					if(parms.readInt("workWithUserAttributes")!=1){
						ac.getAttributesManager().setAttributes(ac.getSession(),
								ac.getMemberById(parms.readInt("member")),
								parms.readList("attributes", Attribute.class),
								false);
					}else{
						ac.getAttributesManager().setAttributes(ac.getSession(),
								ac.getMemberById(parms.readInt("member")),
								parms.readList("attributes", Attribute.class),
								true);
					}
				}else{
					ac.getAttributesManager().setAttributes(ac.getSession(),
							ac.getMemberById(parms.readInt("member")),
							parms.readList("attributes", Attribute.class));
				}
			} else if (parms.contains("user")) {
				ac.getAttributesManager().setAttributes(ac.getSession(),
						ac.getUserById(parms.readInt("user")),
						parms.readList("attributes", Attribute.class));
			} else if (parms.contains("group")) {
				ac.getAttributesManager().setAttributes(ac.getSession(),
						ac.getGroupById(parms.readInt("group")),
						parms.readList("attributes", Attribute.class));
			} else if (parms.contains("host")) {
				ac.getAttributesManager().setAttributes(ac.getSession(),
						ac.getHostById(parms.readInt("host")),
						parms.readList("attributes", Attribute.class));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "facility, vo, resource, member, user, host or group");
			}

			return null;
		}
	},

	/*#
	 * Returns an Attribute by its ID. Returns only non-empty attributes.
	 *
	 * @param facility int Facility ID
	 * @param user int User ID
	 * @param attributeId int Attribute ID
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its ID. Returns only non-empty attributes.
	 *
	 * @param facility int Facility ID
	 * @param attributeId int Attribute ID
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its ID. Returns only non-empty attributes.
	 *
	 * @param vo int VO ID
	 * @param attributeId int Attribute ID
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its ID. Returns only non-empty attributes.
	 *
	 * @param member int Member ID
	 * @param resource int Resource ID
	 * @param attributeId int Attribute ID
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its ID. Returns only non-empty attributes.
	 *
	 * @param group int Group ID
	 * @param resource int Resource ID
	 * @param attributeId int Attribute ID
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its ID. Returns only non-empty attributes.
	 *
	 * @param resource int Resource ID
	 * @param attributeId int Attribute ID
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its ID. Returns only non-empty attributes.
	 *
	 * @param member int Member ID
	 * @param attributeId int Attribute ID
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its ID. Returns only non-empty attributes.
	 *
	 * @param user int User ID
	 * @param attributeId int Attribute ID
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its ID. Returns only non-empty attributes.
	 *
	 * @param host int Host ID
	 * @param attributeId int Attribute ID
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its name. Returns only non-empty attributes.
	 *
	 * @param facility int Facility ID
	 * @param user int User ID
	 * @param attributeName String Attribute name
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its name. Returns only non-empty attributes.
	 *
	 * @param facility int Facility ID
	 * @param attributeName String Attribute name
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its name. Returns only non-empty attributes.
	 *
	 * @param vo int VO ID
	 * @param attributeName String Attribute name
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its name. Returns only non-empty attributes.
	 *
	 * @param member int Member ID
	 * @param resource int Resource ID
	 * @param attributeName String Attribute name
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its name. Returns only non-empty attributes.
	 *
	 * @param group int Group ID
	 * @param resource int Resource ID
	 * @param attributeName String Attribute name
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its name. Returns only non-empty attributes.
	 *
	 * @param resource int Resource ID
	 * @param attributeName String Attribute name
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its name. Returns only non-empty attributes.
	 *
	 * @param member int Member ID
	 * @param attributeName String Attribute name
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its name. Returns only non-empty attributes.
	 *
	 * @param user int User ID
	 * @param attributeName String Attribute name
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its name. Returns only non-empty attributes.
	 *
	 * @param host int Host ID
	 * @param attributeName String Attribute name
	 * @return Attribute Found Attribute
	 */
	getAttribute {

		@Override
		public Attribute call(ApiCaller ac, Deserializer parms) throws PerunException {
			if(parms.contains("attributeId")) {
				if (parms.contains("facility")) {
					if (parms.contains("user")) {
						return ac.getAttributesManager().getAttributeById(ac.getSession(),
								ac.getFacilityById(parms.readInt("facility")),
								ac.getUserById(parms.readInt("user")),
								parms.readInt("attributeId"));
					} else {
						return ac.getAttributesManager().getAttributeById(ac.getSession(),
								ac.getFacilityById(parms.readInt("facility")),
								parms.readInt("attributeId"));
					}
				} else if (parms.contains("vo")) {
					return ac.getAttributesManager().getAttributeById(ac.getSession(),
							ac.getVoById(parms.readInt("vo")),
							parms.readInt("attributeId"));
				} else if (parms.contains("resource")) {
					if (parms.contains("member")) {
						return ac.getAttributesManager().getAttributeById(ac.getSession(),
								ac.getResourceById(parms.readInt("resource")),
								ac.getMemberById(parms.readInt("member")),
								parms.readInt("attributeId"));
					}else if(parms.contains("group")) {
						return ac.getAttributesManager().getAttributeById(ac.getSession(),
								ac.getResourceById(parms.readInt("resource")),
								ac.getGroupById(parms.readInt("group")),
								parms.readInt("attributeId"));
					} else {
						return ac.getAttributesManager().getAttributeById(ac.getSession(),
								ac.getResourceById(parms.readInt("resource")),
								parms.readInt("attributeId"));
					}
				} else if (parms.contains("member")) {
					return ac.getAttributesManager().getAttributeById(ac.getSession(),
							ac.getMemberById(parms.readInt("member")),
							parms.readInt("attributeId"));
				} else if (parms.contains("user")) {
					return ac.getAttributesManager().getAttributeById(ac.getSession(),
							ac.getUserById(parms.readInt("user")),
							parms.readInt("attributeId"));
					/*  Not implemented yet
							} else if (parms.contains("group")) {
							return ac.getAttributesManager().getAttributeById(ac.getSession(),
							ac.getGroupById(parms.readInt("group")),
							parms.readInt("attributeId"));
							*/
				} else if (parms.contains("host")) {
					return ac.getAttributesManager().getAttributeById(ac.getSession(),
							ac.getHostById(parms.readInt("host")),
							parms.readInt("attributeId"));
					/*  Not implemented yet
							} else if (parms.contains("key")) {
							return ac.getAttributesManager().getAttributeById(ac.getSession(),
							parms.readString("key"),
							parms.readInt("attributeId"));
							*/
				} else {
					throw new RpcException(RpcException.Type.MISSING_VALUE, "facility, vo, resource, member, user, host, key or group");
				}
			} else {
				if (parms.contains("facility")) {
					if (parms.contains("user")) {
						return ac.getAttributesManager().getAttribute(ac.getSession(),
								ac.getFacilityById(parms.readInt("facility")),
								ac.getUserById(parms.readInt("user")),
								parms.readString("attributeName"));
					} else {
						return ac.getAttributesManager().getAttribute(ac.getSession(),
								ac.getFacilityById(parms.readInt("facility")),
								parms.readString("attributeName"));
					}
				} else if (parms.contains("vo")) {
					return ac.getAttributesManager().getAttribute(ac.getSession(),
							ac.getVoById(parms.readInt("vo")),
							parms.readString("attributeName"));
				} else if (parms.contains("resource")) {
					if (parms.contains("member")) {
						return ac.getAttributesManager().getAttribute(ac.getSession(),
								ac.getResourceById(parms.readInt("resource")),
								ac.getMemberById(parms.readInt("member")),
								parms.readString("attributeName"));
					}else if(parms.contains("group")) {
						return ac.getAttributesManager().getAttribute(ac.getSession(),
								ac.getResourceById(parms.readInt("resource")),
								ac.getGroupById(parms.readInt("group")),
								parms.readString("attributeName"));
					} else {
						return ac.getAttributesManager().getAttribute(ac.getSession(),
								ac.getResourceById(parms.readInt("resource")),
								parms.readString("attributeName"));
					}
				} else if (parms.contains("member")) {
					return ac.getAttributesManager().getAttribute(ac.getSession(),
							ac.getMemberById(parms.readInt("member")),
							parms.readString("attributeName"));
				} else if (parms.contains("user")) {
					return ac.getAttributesManager().getAttribute(ac.getSession(),
							ac.getUserById(parms.readInt("user")),
							parms.readString("attributeName"));
				} else if (parms.contains("group")) {
					return ac.getAttributesManager().getAttribute(ac.getSession(),
							ac.getGroupById(parms.readInt("group")),
							parms.readString("attributeName"));
				} else if (parms.contains("host")) {
					return ac.getAttributesManager().getAttribute(ac.getSession(),
							ac.getHostById(parms.readInt("host")),
							parms.readString("attributeName"));
				} else if (parms.contains("key")) {
					return ac.getAttributesManager().getAttribute(ac.getSession(),
							parms.readString("key"),
							parms.readString("attributeName"));
				} else {
					throw new RpcException(RpcException.Type.MISSING_VALUE, "facility, vo, resource, member, user, host, key or group");
				}
			}
		}
	},

	/*#
	 * Returns AttributeDefinition.
	 *
	 * @param attributeName String Attribute name
	 * @return AttributeDefinition Definition of an Attribute
	 */
	getAttributeDefinition {

		@Override
		public AttributeDefinition call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getAttributesManager().getAttributeDefinition(ac.getSession(),
					parms.readString("attributeName"));
		}
	},
	/*#
	 * Returns all AttributeDefinitions.
	 *
	 * @return List<AttributeDefinition> Definitions of Attributes
	 */
	getAttributesDefinition {
		@Override
		public List<AttributeDefinition> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getAttributesManager().getAttributesDefinition(ac.getSession());
		}
	},

	/*#
	 * Returns AttributeDefinition.
	 *
	 * @param id int Attribute ID
	 * @return AttributeDefinition Definition of an Attribute
	 */
	getAttributeDefinitionById {

		@Override
		public AttributeDefinition call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getAttributeDefinitionById(parms.readInt("id"));
		}
	},

	/*#
	 * Returns all AttributeDefinitions in a namespace.
	 *
	 * @param namespace String Namespace
	 * @return List<AttributeDefinition> Definitions of Attributes in a namespace
	 */
	getAttributesDefinitionByNamespace {
		@Override
		public List<AttributeDefinition> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getAttributesManager().getAttributesDefinitionByNamespace(ac.getSession(),
					parms.readString("namespace"));
		}
	},

	/*#
	 * Returns all AttributeDefinitions for every entity and possible combination of entities with rights.
	 * Only attribute definition of attributes user can read (or write) you will get.
	 *
	 * Combination of entities is based on provided parameters, which are optional
	 * (at least one must be present).
	 *
	 * @param member int ID of Member
	 * @param user int ID of User
	 * @param vo int ID of Virtual organization
	 * @param group int ID of Group
	 * @param resource int ID of Resource
	 * @param facility int ID of Facility
	 * @param host int ID of Host
	 *
	 * @return List<AttributeDefinition> Definitions of Attributes for entities
	 */
	getAttributesDefinitionWithRights {
		@Override
		public List<AttributeDefinition> call(ApiCaller ac, Deserializer parms) throws PerunException {
			User user = null;
			Member member = null;
			Vo vo = null;
			Group group = null;
			Resource resource = null;
			Facility facility = null;
			Host host = null;
			//Not supported entityless attirbutes now
			//String entityless = null;
			List<PerunBean> entities = new ArrayList<PerunBean>();

			//If member exists in query
			if(parms.contains("member")) {
				member = ac.getMemberById(parms.readInt("member"));
				entities.add(member);
			}
			//If user exists in query
			if(parms.contains("user")) {
				user = ac.getUserById(parms.readInt("user"));
				entities.add(user);
			}
			//If vo exists in query
			if(parms.contains("vo")) {
				vo = ac.getVoById(parms.readInt("vo"));
				entities.add(vo);
			}
			//If group exists in query
			if(parms.contains("group")) {
				group = ac.getGroupById(parms.readInt("group"));
				entities.add(group);
			}
			//If resource exists in query
			if(parms.contains("resource")) {
				resource = ac.getResourceById(parms.readInt("resource"));
				entities.add(resource);
			}
			//If facility exists in query
			if(parms.contains("facility")) {
				facility = ac.getFacilityById(parms.readInt("facility"));
				entities.add(facility);
			}
			//If host exists in query
			if(parms.contains("host")) {
				host = ac.getHostById(parms.readInt("host"));
				entities.add(host);
			}
			//If entityless exists in query
			/*if(parms.contains("entityless")) {

				}*/

			List<AttributeDefinition> attributesDefinition = ac.getAttributesManager().getAttributesDefinitionWithRights(ac.getSession(), entities);

			return attributesDefinition;
		}
	},


	/*#
	 * Returns an Attribute by its ID.
	 *
	 * @param facility int Facility ID
	 * @param id int Attribute ID
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its ID.
	 *
	 * @param vo int VO ID
	 * @param id int Attribute ID
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its ID.
	 *
	 * @param member int Member ID
	 * @param resource int Resource ID
	 * @param id int Attribute ID
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its ID.
	 *
	 * @param resource int Resource ID
	 * @param id int Attribute ID
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its ID.
	 *
	 * @param host int Host ID
	 * @param id int Attribute ID
	 * @return Attribute Found Attribute
	 */
	getAttributeById {

		@Override
		public Attribute call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("facility")) {
				return ac.getAttributeById(
						ac.getFacilityById(parms.readInt("facility")),
						parms.readInt("id"));
			} else if (parms.contains("vo")) {
				return ac.getAttributeById(
						ac.getVoById(parms.readInt("vo")),
						parms.readInt("id"));
			} else if (parms.contains("resource")) {
				if (parms.contains("member")) {
					return ac.getAttributeById(
							ac.getResourceById(parms.readInt("resource")),
							ac.getMemberById(parms.readInt("member")),
							parms.readInt("id"));
				} else {
					return ac.getAttributeById(
							ac.getResourceById(parms.readInt("resource")),
							parms.readInt("id"));
				}
			} else if (parms.contains("host")) {
				return ac.getAttributeById(
						ac.getHostById(parms.readInt("host")),
						parms.readInt("id"));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "facility, vo, host or resource");
			}
		}
	},

	/*#
	 * Sets an Attribute.
	 *
	 * @param facility int Facility ID
	 * @param user int User ID
	 * @param attribute Attribute JSON object
	 */
	/*#
	 * Sets an Attribute.
	 *
	 * @param facility int Facility ID
	 * @param attribute Attribute JSON object
	 */
	/*#
	 * Sets an Attribute.
	 *
	 * @param vo int VO ID
	 * @param attribute Attribute JSON object
	 */
	/*#
	 * Sets an Attribute.
	 *
	 * @param member int Member ID
	 * @param resource int Resource ID
	 * @param attribute Attribute JSON object
	 */
	/*#
	 * Sets an Attribute.
	 *
	 * @param group int Group ID
	 * @param resource int Resource ID
	 * @param attribute Attribute JSON object
	 */
	/*#
	 * Sets an Attribute.
	 *
	 * @param resource int Resource ID
	 * @param attribute Attribute JSON object
	 */
	/*#
	 * Sets an Attribute.
	 *
	 * @param member int Member ID
	 * @param attribute Attribute JSON object
	 */
	/*#
	 * Sets an Attribute.
	 *
	 * @param user int User ID
	 * @param attribute Attribute JSON object
	 */
	/*#
	 * Sets an Attribute.
	 *
	 * @param group int Group ID
	 * @param attribute Attribute JSON object
	 *
	 */
	/*#
	 * Sets an Attribute.
	 *
	 * @param host int Host ID
	 * @param attribute Attribute JSON object
	 */
	setAttribute {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("facility")) {
				if(parms.contains("user")) {
					ac.getAttributesManager().setAttribute(ac.getSession(),
							ac.getFacilityById(parms.readInt("facility")),
							ac.getUserById(parms.readInt("user")),
							parms.read("attribute", Attribute.class));
				} else {
					ac.getAttributesManager().setAttribute(ac.getSession(),
							ac.getFacilityById(parms.readInt("facility")),
							parms.read("attribute", Attribute.class));
				}
			} else if (parms.contains("vo")) {
				ac.getAttributesManager().setAttribute(ac.getSession(),
						ac.getVoById(parms.readInt("vo")),
						parms.read("attribute", Attribute.class));
			} else if (parms.contains("resource")) {
				if (parms.contains("member")) {
					ac.getAttributesManager().setAttribute(ac.getSession(),
							ac.getResourceById(parms.readInt("resource")),
							ac.getMemberById(parms.readInt("member")),
							parms.read("attribute", Attribute.class));
				} else if(parms.contains("group")) {
					ac.getAttributesManager().setAttribute(ac.getSession(),
							ac.getResourceById(parms.readInt("resource")),
							ac.getGroupById(parms.readInt("group")),
							parms.read("attribute", Attribute.class));
				} else {
					ac.getAttributesManager().setAttribute(ac.getSession(),
							ac.getResourceById(parms.readInt("resource")),
							parms.read("attribute", Attribute.class));
				}
			} else if (parms.contains("member")) {
				ac.getAttributesManager().setAttribute(ac.getSession(),
						ac.getMemberById(parms.readInt("member")),
						parms.read("attribute", Attribute.class));
			} else if (parms.contains("user")) {
				ac.getAttributesManager().setAttribute(ac.getSession(),
						ac.getUserById(parms.readInt("user")),
						parms.read("attribute", Attribute.class));
			} else if (parms.contains("group")) {
				ac.getAttributesManager().setAttribute(ac.getSession(),
						ac.getGroupById(parms.readInt("group")),
						parms.read("attribute", Attribute.class));
			} else if (parms.contains("host")) {
				ac.getAttributesManager().setAttribute(ac.getSession(),
						ac.getHostById(parms.readInt("host")),
						parms.read("attribute", Attribute.class));
			} else if (parms.contains("key")) {
				ac.getAttributesManager().setAttribute(ac.getSession(),
						parms.readString("key"),
						parms.read("attribute", Attribute.class));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "facility, vo, resource, user, member, host, key or group");
			}

			return null;
		}
	},

	/*#
	 * Creates AttributeDefinition
	 *
	 * @param attribute AttributeDefinition object
	 * @return AttributeDefinition Created AttributeDefinition
	 */
	createAttribute {

		@Override
		public AttributeDefinition call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			AttributeDefinition attribute = parms.read("attribute", AttributeDefinition.class);

			return ac.getAttributesManager().createAttribute(ac.getSession(),attribute);

		}
	},

	/*#
	 * Deletes attribute definition from Perun.
	 *
	 * Deletion fails if any entity in Perun has
	 * any value for this attribute set.
	 *
	 * @param attribute int AttributeDefinition ID
	 */
	deleteAttribute {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getAttributesManager().deleteAttribute(ac.getSession(),
					ac.getAttributeDefinitionById(parms.readInt("attribute")));
			return null;
		}
	},

	/*#
	 * Returns required attributes.
	 *
	 * @param member int Member ID
	 * @param service int Service ID
	 * @param resource int Resource ID
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param member int Member ID
	 * @param service int Service ID
	 * @param resource int Resource ID
	 * @param workWithUserAttributes int Work with user attributes integer (1 = true, 0 = false). 0 is default value.
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param group int Group ID
	 * @param service int Service ID
	 * @param resource int Resource ID
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param service int Service ID
	 * @param resource int Resource ID
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param facility int Facility ID
	 * @param service int Service ID
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param facility int Facility ID
	 * @param services List<int> list of Service IDs
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param host int Host ID
	 * @param service int Service ID
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param member int Member ID
	 * @param resource int Resource ID
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param member int Member ID
	 * @param resource int Resource ID
	 * @param workWithUserAttributes int Work with user attributes integer (1 = true, 0 = false). 0 is default value.
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param resource int Resource ID
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param facility int Facility ID
	 * @param user int User ID
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param facility int Facility ID
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param member int Member ID
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param member int Member ID
	 * @param workWithUserAttributes int Work with user attributes integer (1 = true, 0 = false). 0 is default value.
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param user int User ID
	 * @return List<Attribute> Required Attributes
	 */
	getRequiredAttributes {

		@Override
		public List<Attribute> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("service")) {
				if (parms.contains("resource")) {
					if (parms.contains("member")) {
						if (parms.contains("workWithUserAttributes")) {
							return ac.getAttributesManager().getRequiredAttributes(ac.getSession(),
									ac.getServiceById(parms.readInt("service")),
									ac.getResourceById(parms.readInt("resource")),
									ac.getMemberById(parms.readInt("member")),
									parms.readInt("workWithUserAttributes") == 1);
						} else {
							return ac.getAttributesManager().getRequiredAttributes(ac.getSession(),
									ac.getServiceById(parms.readInt("service")),
									ac.getResourceById(parms.readInt("resource")),
									ac.getMemberById(parms.readInt("member")));
						}
					} else if (parms.contains("group")) {
						return ac.getAttributesManager().getRequiredAttributes(ac.getSession(),
								ac.getServiceById(parms.readInt("service")),
								ac.getResourceById(parms.readInt("resource")),
								ac.getGroupById(parms.readInt("group")));
					} else {
						return ac.getAttributesManager().getRequiredAttributes(ac.getSession(),
								ac.getServiceById(parms.readInt("service")),
								ac.getResourceById(parms.readInt("resource")));
					}
				} else if (parms.contains("facility")) {
					return ac.getAttributesManager().getRequiredAttributes(ac.getSession(),
							ac.getServiceById(parms.readInt("service")),
							ac.getFacilityById(parms.readInt("facility")));
				} else if (parms.contains("host")) {
					return ac.getAttributesManager().getRequiredAttributes(ac.getSession(),
							ac.getServiceById(parms.readInt("service")),
							ac.getHostById(parms.readInt("host")));
				} else if (parms.contains("vo")) {
					return ac.getAttributesManager().getRequiredAttributes(ac.getSession(),
							ac.getServiceById(parms.readInt("service")),
							ac.getVoById(parms.readInt("vo")));
				} else {
					throw new RpcException(RpcException.Type.MISSING_VALUE, "host, resource or facility");
				}
			} else if (parms.contains("services[]")) {
				// get list of services
				List<Service> services = new ArrayList<Service>();
				List<Integer> servIds = parms.readList("services", Integer.class);
				for (Integer id : servIds) {
					Service s = ac.getServiceById(id);
					if (!services.contains(s)) {
						services.add(s);
					}
				}
				if (parms.contains("facility")) {
					return ac.getAttributesManager().getRequiredAttributes(ac.getSession(), services,
							ac.getFacilityById(parms.readInt("facility")));
				} else if (parms.contains("resource")) {
					return ac.getAttributesManager().getRequiredAttributes(ac.getSession(), services,
							ac.getResourceById(parms.readInt("resource")));
				} else {
					throw new RpcException(RpcException.Type.MISSING_VALUE, "facility or resource");
				}
			} else if (parms.contains("resource")) {
				if (parms.contains("member")) {
					if (parms.contains("workWithUserAttributes")) {
						return ac.getAttributesManager().getRequiredAttributes(ac.getSession(),
								ac.getResourceById(parms.readInt("resource")),
								ac.getMemberById(parms.readInt("member")), parms.readInt("workWithUserAttributes") == 1);
					} else {
						return ac.getAttributesManager().getRequiredAttributes(ac.getSession(),
								ac.getResourceById(parms.readInt("resource")),
								ac.getMemberById(parms.readInt("member")));
					}
				} else {
					return ac.getAttributesManager().getRequiredAttributes(ac.getSession(),
							ac.getResourceById(parms.readInt("resource")));
				}
			} else if (parms.contains("facility")) {
				if (parms.contains("user")) {
					return ac.getAttributesManager().getRequiredAttributes(ac.getSession(),
							ac.getFacilityById(parms.readInt("facility")),
							ac.getUserById(parms.readInt("user")));
				} else {
					return ac.getAttributesManager().getRequiredAttributes(ac.getSession(),
							ac.getFacilityById(parms.readInt("facility")));
				}
			} else if (parms.contains("member")) {
				if (parms.contains("workWithUserAttributes")){
					return ac.getAttributesManager().getRequiredAttributes(ac.getSession(),
							ac.getMemberById(parms.readInt("member")), parms.readInt("workWithUserAttributes") == 1);
				} else {
					return ac.getAttributesManager().getRequiredAttributes(ac.getSession(),
							ac.getMemberById(parms.readInt("member")), false);
				}
			} else if (parms.contains("user")) {
				return ac.getAttributesManager().getRequiredAttributes(ac.getSession(),
						ac.getUserById(parms.readInt("user")));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "service, resource, facility, member or user");
			}
		}
	},

	/*#
	 * Returns required attributes definition for a Service.
	 *
	 * @param service int Service ID
	 * @return List<AttributeDefinition> Attributes definitions
	 */
	getRequiredAttributesDefinition {

		@Override
		public List<AttributeDefinition> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getAttributesManager().getRequiredAttributesDefinition(ac.getSession(),
					ac.getServiceById(parms.readInt("service")));
		}
	},

	/*#
	 * Gets member-resource attributes and also user, user-facility and member attributes, if workWithUserAttributes == 1.
	 * It returns attributes required by all services assigned to specified resource. Both empty and non-empty attributes are returned.
	 *
	 * @param resourceToGetServicesFrom int Resource to get services from ID
	 * @param resource int Resource ID
	 * @param member int Member ID
	 * @param workWithUserAttributes int Work with user attributes integer (1 = true, 0 = false). 0 is default value.
	 * @return List<Attribute> Member-resource attributes (if workWithUserAttributes == 1 also user, user-facility and member attributes)
	 */
	/*#
	 * Gets member-resource attributes.
	 * It returns attributes required by all services assigned to specified resource. Both empty and non-empty attributes are returned.
	 *
	 * @param resourceToGetServicesFrom int Resource to get services from ID
	 * @param resource int Resource ID
	 * @param member int Member ID
	 * @return List<Attribute> Member-resource attributes
	 */
	/*#
	 * Gets member attributes.
	 * It returns attributes required by all services assigned to specified resource. Both empty and non-empty attributes are returned.
	 *
	 * @param member int Member ID
	 * @param resourceToGetServicesFrom int Resource to get services from ID
	 * @return List<Attribute> Member attributes
	 */
	/*#
	 * Gets user-facility attributes.
	 * It returns attributes required by all services assigned to specified resource. Both empty and non-empty attributes are returned.
	 *
	 * @param resourceToGetServicesFrom int Resource to get services from ID
	 * @param facility int Facility ID
	 * @param user int User ID
	 * @return List<Attribute> User-facility attributes
	 */
	/*#
	 * Gets user attributes.
	 * It returns attributes required by all services assigned to specified resource. Both empty and non-empty attributes are returned.
	 *
	 * @param user int User ID
	 * @param resourceToGetServicesFrom int Resource to get services from ID
	 * @return List<Attribute> User's attributes
	 */
	/*#
	 * Gets group-resource and also group attributes, if workWithGroupAttributes == 1.
	 * It returns attributes required by all services assigned to specified resource. Both empty and non-empty attributes are returned.
	 *
	 * @param resourceToGetServicesFrom int Resource to get services from ID
	 * @param group int Group ID
	 * @param resource int Resource ID
	 * @param workWithGroupAttributes int Work with group attributes integer (1 = true, 0 = false). 0 is default value.
	 * @return List<Attribute> Group-resource and (if workWithGroupAttributes == 1) group required attributes
	 */
	/*#
	 * Gets group-resource attributes.
	 * It returns attributes required by all services assigned to specified resource. Both empty and non-empty attributes are returned.
	 *
	 * @param resourceToGetServicesFrom int Resource to get services from ID
	 * @param resource int Resource ID
	 * @param group int Group ID
	 * @return List<Attribute> Group-resource attributes
	 */
	/*#
	 * Gets group attributes.
	 * It returns attributes required by all services assigned to specified resource. Both empty and non-empty attributes are returned.
	 *
	 * @param resourceToGetServicesFrom int Resource to get services from ID
	 * @param group int Group ID
	 * @return List<Attribute> Group's attributes
	 */
	getResourceRequiredAttributes {

		@Override
		public List<Attribute> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("resourceToGetServicesFrom")) {
				if (parms.contains("member")) {
					if (parms.contains("resource")) {
						if (parms.contains("workWithUserAttributes")){
							return	ac.getAttributesManager().getResourceRequiredAttributes(ac.getSession(),
									ac.getResourceById(parms.readInt("resourceToGetServicesFrom")),
									ac.getResourceById(parms.readInt("resource")),
									ac.getMemberById(parms.readInt("member")), parms.readInt("workWithUserAttributes") == 1);
						} else {
							return ac.getAttributesManager().getResourceRequiredAttributes(ac.getSession(),
									ac.getResourceById(parms.readInt("resourceToGetServicesFrom")),
									ac.getResourceById(parms.readInt("resource")),
									ac.getMemberById(parms.readInt("member")));
						}
					} else {
						return ac.getAttributesManager().getResourceRequiredAttributes(ac.getSession(),
								ac.getResourceById(parms.readInt("resourceToGetServicesFrom")),
								ac.getMemberById(parms.readInt("member")));
					}
				} else if (parms.contains("user")) {
					if (parms.contains("facility")) {
						return ac.getAttributesManager().getResourceRequiredAttributes(ac.getSession(),
								ac.getResourceById(parms.readInt("resourceToGetServicesFrom")),
								ac.getFacilityById(parms.readInt("facility")),
								ac.getUserById(parms.readInt("user")));
					} else {
						return ac.getAttributesManager().getResourceRequiredAttributes(ac.getSession(),
								ac.getResourceById(parms.readInt("resourceToGetServicesFrom")),
								ac.getUserById(parms.readInt("user")));
					}
				} else if (parms.contains("group")) {
					if (parms.contains("resource")) {
						if (parms.contains("workWithGroupAttributes")) {
							return ac.getAttributesManager().getResourceRequiredAttributes(ac.getSession(),
									ac.getResourceById(parms.readInt("resourceToGetServicesFrom")),
									ac.getResourceById(parms.readInt("resource")),
									ac.getGroupById(parms.readInt("group")),
									parms.readInt("workWithGroupAttributes") == 1);
						} else {
							return ac.getAttributesManager().getResourceRequiredAttributes(ac.getSession(),
									ac.getResourceById(parms.readInt("resourceToGetServicesFrom")),
									ac.getResourceById(parms.readInt("resource")),
									ac.getGroupById(parms.readInt("group")));
						}
					} else {
						return ac.getAttributesManager().getResourceRequiredAttributes(ac.getSession(),
								ac.getResourceById(parms.readInt("resourceToGetServicesFrom")),
								ac.getGroupById(parms.readInt("group")));
					}
				} else {
					throw new RpcException(RpcException.Type.MISSING_VALUE, "member or user");
				}
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "resourceToGetServicesFrom");
			}
		}
	},

	/*#
	 * Tries to fill group attribute.
	 *
	 * @param group int Group ID
	 * @param attribute int Attribute ID
	 * @return Attribute attribute which may have filled value
	 */
	/*#
	 * Tries to fill host attribute.
	 *
	 * @param host int Host ID
	 * @param attribute int Attribute ID
	 * @return Attribute attribute which MAY have filled value
	 */
	/*#
	 * Tries to fill group-resource attribute.
	 *
	 * @param resource int Resource ID
	 * @param group int Group ID
	 * @param attribute int Attribute ID
	 * @return Attribute attribute which MAY have filled value
	 */
	/*#
	 * Tries to fill member-resource attribute.
	 *
	 * @param resource int Resource ID
	 * @param member int Member ID
	 * @param attribute int Attribute ID
	 * @return Attribute attribute which MAY have filled value
	 */
	/*#
	 * Tries to fill resource attribute.
	 *
	 * @param resource int Resource ID
	 * @param attribute int Attribute ID
	 * @return Attribute attribute which MAY have filled value
	 */
	/*#
	 * Tries to fill user-facility attribute.
	 *
	 * @param user int User ID
	 * @param facility int Facility ID
	 * @param attribute int Attribute ID
	 * @return Attribute attribute which MAY have filled value
	 */
	/*#
	 * Tries to fill user attribute.
	 *
	 * @param user int User ID
	 * @param attribute int Attribute ID
	 * @return Attribute attribute which MAY have filled value
	 */
	/*#
	 * Tries to fill member attribute.
	 *
	 * @param member int Member ID
	 * @param attribute int Attribute ID
	 * @return Attribute attribute which MAY have filled value
	 */
	fillAttribute {

		@Override
		public Attribute call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("group")) {
				Group group = ac.getGroupById(parms.readInt("group"));
				return ac.getAttributesManager().fillAttribute(ac.getSession(),
						group,
						ac.getAttributeById(group, parms.readInt("attribute")));
			} else if (parms.contains("host")) {
				Host host = ac.getHostById(parms.readInt("host"));
				return ac.getAttributesManager().fillAttribute(ac.getSession(),
						host,
						ac.getAttributeById(host, parms.readInt("attribute")));
			} else if (parms.contains("resource")) {
				Resource resource = ac.getResourceById(parms.readInt("resource"));
				if (parms.contains("group")) {
					Group group = ac.getGroupById(parms.readInt("group"));
					return ac.getAttributesManager().fillAttribute(ac.getSession(),
							resource,
							group,
							ac.getAttributeById(resource, group, parms.readInt("attribute")));
				} else if (parms.contains("member")) {
					Member member = ac.getMemberById(parms.readInt("member"));
					return ac.getAttributesManager().fillAttribute(ac.getSession(),
							resource,
							member,
							ac.getAttributeById(resource, member, parms.readInt("attribute")));
				} else {
					return ac.getAttributesManager().fillAttribute(ac.getSession(),
							resource,
							ac.getAttributeById(resource, parms.readInt("attribute")));
				}
			} else if (parms.contains("user")) {
				User user = ac.getUserById(parms.readInt("user"));
				if (parms.contains("facility")) {
					Facility facility = ac.getFacilityById(parms.readInt("facility"));
					return ac.getAttributesManager().fillAttribute(ac.getSession(),
							facility,
							user,
							ac.getAttributeById(facility, user, parms.readInt("attribute")));
				} else {
					return ac.getAttributesManager().fillAttribute(ac.getSession(),
							user,
							ac.getAttributeById(user, parms.readInt("attribute")));
				}
			} else if (parms.contains("member")) {
				Member member = ac.getMemberById(parms.readInt("member"));
				return ac.getAttributesManager().fillAttribute(ac.getSession(),
						member,
						ac.getAttributeById(member, parms.readInt("attribute")));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "group, host, resource, user, member");
			}
		}
	},

	/*#
	 * Tries to fill host attributes.
	 *
	 * @param host int Host ID
	 * @param attributes List<Attribute> List of attributes
	 * @return List<Attribute> attributes which MAY have filled value
	 */
	/*#
	 * Tries to fill group-resource attributes.
	 *
	 * @param resource int Resource ID
	 * @param group int Group ID
	 * @param attributes List<Attribute> List of attributes
	 * @return List<Attribute> attributes which MAY have filled value
	 */
	/*#
	 * Tries to fill user, member, member-resource and user-facility attributes.
	 *
	 * @param facility int Facility ID
	 * @param resource int Resource ID
	 * @param user int User ID
	 * @param member int Member ID
	 * @param attributes List<Attribute> List of attributes
	 * @return List<Attribute> attributes which MAY have filled value
	 */
	/*#
	 * Tries to fill member-resource attributes and also user and user-facility attributes, if workWithUserAttributes == 1.
	 *
	 * @param resource int Resource ID
	 * @param member int Member ID
	 * @param attributes List<Attribute> List of attributes
	 * @param workWithUserAttributes int Work with user attributes integer (1 = true, 0 = false). 0 is default value.
	 * @return List<Attribute> attributes which MAY have filled value
	 */
	/*#
	 * Tries to fill member-resource attributes.
	 *
	 * @param resource int Resource ID
	 * @param member int Member ID
	 * @param attributes List<Attribute> List of attributes
	 * @return List<Attribute> attributes which MAY have filled value
	 */
	/*#
	 * Tries to fill resource attributes.
	 *
	 * @param resource int Resource ID
	 * @param attributes List<Attribute> List of attributes
	 * @return List<Attribute> attributes which MAY have filled value
	 */
	/*#
	 * Tries to fill group attributes.
	 *
	 * @param group int Group ID
	 * @param attributes List<Attribute> List of attributes
	 * @return List<Attribute> attributes which MAY have filled value
	 */
	/*#
	 * Tries to fill user-facility attributes.
	 *
	 * @param facility int Facility ID
	 * @param user int User ID
	 * @param attributes List<Attribute> List of attributes
	 * @return List<Attribute> attributes which MAY have filled value
	 */
	/*#
	 * Tries to fill user attributes.
	 *
	 * @param user int User ID
	 * @param attributes List<Attribute> List of attributes
	 * @return List<Attribute> attributes which MAY have filled value
	 */
	/*#
	 * Tries to fill member attributes.
	 *
	 * @param member int Member ID
	 * @param attributes List<Attribute> List of attributes
	 * @return List<Attribute> attributes which MAY have filled value
	 */
	fillAttributes {

		@Override
		public List<Attribute> call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			List<Attribute> attributes = new ArrayList<Attribute>();
			if(parms.contains("attributes")) {
				attributes = parms.readList("attributes", Attribute.class);
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "attributes");
			}


			if (parms.contains("host")) {
				Host host = ac.getHostById(parms.readInt("host"));
				return ac.getAttributesManager().fillAttributes(ac.getSession(),
						host,
						attributes);
			} else if (parms.contains("resource")) {
				Resource resource = ac.getResourceById(parms.readInt("resource"));
				if (parms.contains("group")) {
					Group group = ac.getGroupById(parms.readInt("group"));
					return ac.getAttributesManager().fillAttributes(ac.getSession(),
							resource,
							group,
							attributes);
				} else if (parms.contains("user")) {
					User user = ac.getUserById(parms.readInt("user"));
					if (parms.contains("facility") && parms.contains("member")) {
						Facility facility = ac.getFacilityById(parms.readInt("facility"));
						Member member = ac.getMemberById(parms.readInt("member"));
						return ac.getAttributesManager().fillAttributes(ac.getSession(),
								facility,
								resource,
								user,
								member,
								attributes);
					} else {
						throw new RpcException(RpcException.Type.MISSING_VALUE, "facility, member");
					}
				} else if (parms.contains("member")) {
					Member member = ac.getMemberById(parms.readInt("member"));
					if (parms.contains("workWithUserAttributes")) {
						if(parms.readInt("workWithUserAttributes") != 1) {
							return ac.getAttributesManager().fillAttributes(ac.getSession(),
									resource,
									member,
									attributes,
									false);
						} else {
							return ac.getAttributesManager().fillAttributes(ac.getSession(),
									resource,
									member,
									attributes,
									true);
						}
					} else {
						return ac.getAttributesManager().fillAttributes(ac.getSession(),
								resource,
								member,
								attributes);
					}
				} else {
					return ac.getAttributesManager().fillAttributes(ac.getSession(),
							resource,
							attributes);
				}
			} else if (parms.contains("group")) {
				Group group = ac.getGroupById(parms.readInt("group"));
				return ac.getAttributesManager().fillAttributes(ac.getSession(),
						group,
						attributes);
			} else if (parms.contains("user")) {
				User user = ac.getUserById(parms.readInt("user"));
				if (parms.contains("facility")) {
					Facility facility = ac.getFacilityById(parms.readInt("facility"));
					return ac.getAttributesManager().fillAttributes(ac.getSession(),
							facility,
							user,
							attributes);
				} else {
					return ac.getAttributesManager().fillAttributes(ac.getSession(),
							user,
							attributes);
				}
			} else if (parms.contains("member")) {
				Member member = ac.getMemberById(parms.readInt("member"));
				return ac.getAttributesManager().fillAttributes(ac.getSession(),
						member,
						attributes);
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "group, host, resoure, user, member");
			}
		}
	},

	/*#
	 * Checks if this facility attribute is valid.
	 *
	 * @param facility int Facility ID
	 * @param attribute int Attribute ID
	 */
	/*#
	 * Checks if this vo attribute is valid.
	 *
	 * @param vo int Vo ID
	 * @param attribute int Attribute ID
	 */
	/*#
	 * Checks if this member-resource attribute is valid.
	 *
	 * @param member int Member ID
	 * @param resource int Resource ID
	 * @param attribute int Attribute ID
	 */
	/*#
	 * Checks if this resource attribute is valid.
	 *
	 * @param resource int Resource ID
	 * @param attribute int Attribute ID
	 */
	/*#
	 * Checks if this host attribute is valid.
	 *
	 * @param host int Host ID
	 * @param attribute int Attribute ID
	 */
	/*#
	 * Checks if this user attribute is valid.
	 *
	 * @param user int User ID
	 * @param attribute int Attribute ID
	 */
	checkAttributeValue {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("facility")) {
				ac.getAttributesManager().checkAttributeValue(ac.getSession(),
						ac.getFacilityById(parms.readInt("facility")),
						parms.read("attribute", Attribute.class));
			} else if (parms.contains("vo")) {
				ac.getAttributesManager().checkAttributeValue(ac.getSession(),
						ac.getVoById(parms.readInt("vo")),
						parms.read("attribute", Attribute.class));
			} else if (parms.contains("resource")) {
				if (parms.contains("member")) {
					ac.getAttributesManager().checkAttributeValue(ac.getSession(),
							ac.getResourceById(parms.readInt("resource")),
							ac.getMemberById(parms.readInt("member")),
							parms.read("attribute", Attribute.class));
				} else {
					ac.getAttributesManager().checkAttributeValue(ac.getSession(),
							ac.getResourceById(parms.readInt("resource")),
							parms.read("attribute", Attribute.class));
				}
			} else if (parms.contains("user")) {
				ac.getAttributesManager().checkAttributeValue(ac.getSession(),
						ac.getUserById(parms.readInt("user")),
						parms.read("attribute", Attribute.class));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "resource, vo, facility, member or user");
			}

			return null;
		}
	},
	/*#
	 * Checks if these facility attributes are valid.
	 *
	 * @param facility int Facility ID
	 * @param attributes List<Attribute> Attributes List
	 */
	/*#
	 * Checks if these vo attributes are valid.
	 *
	 * @param vo int Vo ID
	 * @param attributes List<Attribute> Attributes List
	 */
	/*#
	 * Checks if these member-resource attributes are valid.
	 *
	 * @param member int Member ID
	 * @param resource int Resource ID
	 * @param attributes List<Attribute> Attributes List
	 */
	/*#
	 * Checks if these resource attributes are valid.
	 *
	 * @param resource int Resource ID
	 * @param attributes List<Attribute> Attributes List
	 */
	/*#
	 * Checks if these host attributes are valid.
	 *
	 * @param host int Host ID
	 * @param attributes List<Attribute> Attributes List
	 */
	/*#
	 * Checks if these user attributes are valid.
	 *
	 * @param user int User ID
	 * @param attributes List<Attribute> Attributes List
	 */
	checkAttributesValue {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("facility")) {
				ac.getAttributesManager().checkAttributesValue(ac.getSession(),
						ac.getFacilityById(parms.readInt("facility")),
						parms.readList("attributes", Attribute.class));
				return null;
			} else if (parms.contains("vo")) {
				ac.getAttributesManager().checkAttributesValue(ac.getSession(),
						ac.getVoById(parms.readInt("vo")),
						parms.readList("attributes", Attribute.class));
				return null;
			} else if (parms.contains("resource")) {
				if (parms.contains("member")) {
					ac.getAttributesManager().checkAttributesValue(ac.getSession(),
							ac.getResourceById(parms.readInt("resource")),
							ac.getMemberById(parms.readInt("member")),
							parms.readList("attributes", Attribute.class));
					return null;
				} else {
					ac.getAttributesManager().checkAttributesValue(ac.getSession(),
							ac.getResourceById(parms.readInt("resource")),
							parms.readList("attributes", Attribute.class));
					return null;
				}
			} else if (parms.contains("host")) {
				ac.getAttributesManager().checkAttributesValue(ac.getSession(),
						ac.getHostById(parms.readInt("host")),
						parms.readList("attributes", Attribute.class));
				return null;
			} else if (parms.contains("user")) {
				ac.getAttributesManager().checkAttributesValue(ac.getSession(),
						ac.getUserById(parms.readInt("user")),
						parms.readList("attributes", Attribute.class));
				return null;
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "resource, vo, host or facility");
			}
		}
	},

	/*#
	 * Remove attributes of namespace:
	 *
	 * user, user-facility, member, member-resource
	 *
	 * @param facility int Facility ID
	 * @param user int User ID
	 * @param member int Member ID
	 * @param resource int Resource ID
	 * @param attributes List<Integer> List of attributes IDs to remove
	 */
	/*#
	 * Remove attributes of namespace:
	 *
	 * user-facility
	 *
	 * @param facility int Facility ID
	 * @param user int User ID
	 * @param attributes List<Integer> List of attributes IDs to remove
	 */
	/*#
	 * Remove attributes of namespace:
	 *
	 * facility
	 *
	 * @param facility int Facility ID
	 * @param attributes List<Integer> List of attributes IDs to remove
	 */
	/*#
	 * Remove attributes of namespace:
	 *
	 * vo
	 *
	 * @param vo int VO ID
	 * @param attributes List<Integer> List of attributes IDs to remove
	 */
	/*#
	 * Remove attributes of namespace:
	 *
	 * resource
	 *
	 * @param resource int Resource ID
	 * @param attributes List<Integer> List of attributes IDs to remove
	 */
	/*#
	 * Remove attributes of namespace:
	 *
	 * group-resource, group (optional)
	 *
	 * @param resource int Resource ID
	 * @param group int Group ID
	 * @param workWithGroupAttributes int Work with group attributes integer (1 = true, 0 = false). 0 is default value.
	 * @param attributes List<Integer> List of attributes IDs to remove
	 */
	/*#
	 * Remove attributes of namespace:
	 *
	 * group-resource
	 *
	 * @param resource int Resource ID
	 * @param group int Group ID
	 * @param attributes List<Integer> List of attributes IDs to remove
	 */
	/*#
	 * Remove attributes of namespace:
	 *
	 * member-resource
	 *
	 * @param resource int Resource ID
	 * @param member int Member ID
	 * @param attributes List<Integer> List of attributes IDs to remove
	 */
	/*#
	 * Remove attributes of namespace:
	 *
	 * member, user (optional)
	 *
	 * @param member int Member ID
	 * @param workWithUserAttributes int Set to 1 if you want to remove also user attributes. 0 is default value.
	 * @param attributes List<Integer> List of attributes IDs to remove
	 */
	/*#
	 * Remove attributes of namespace:
	 *
	 * member
	 *
	 * @param member int Member ID
	 * @param attributes List<Integer> List of attributes IDs to remove
	 */
	/*#
	 * Remove attributes of namespace:
	 *
	 * group
	 *
	 * @param group int Group ID
	 * @param attributes List<Integer> List of attributes IDs to remove
	 */
	/*#
	 * Remove attributes of namespace:
	 *
	 * host
	 *
	 * @param host int Host ID
	 * @param attributes List<Integer> List of attributes IDs to remove
	 */
	/*#
	 * Remove attributes of namespace:
	 *
	 * user
	 *
	 * @param user int User ID
	 * @param attributes List<Integer> List of attributes IDs to remove
	 */
	removeAttributes {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			int[] ids = parms.readArrayOfInts("attributes");
			List<AttributeDefinition> attributes = new ArrayList<AttributeDefinition>(ids.length);
			for(int i : ids) {
				attributes.add(ac.getAttributeDefinitionById(i));
			}

			if (parms.contains("facility")){
				if(parms.contains("resource") && parms.contains("user") && parms.contains("member")) {

					Facility facility = ac.getFacilityById(parms.readInt("facility"));
					Member member = ac.getMemberById(parms.readInt("member"));
					User user = ac.getUserById(parms.readInt("user"));
					Resource resource = ac.getResourceById(parms.readInt("resource"));
					ac.getAttributesManager().removeAttributes(ac.getSession(),facility, resource, user, member, attributes);

				} else if (parms.contains("user")) {
					Facility facility = ac.getFacilityById(parms.readInt("facility"));
					User user = ac.getUserById(parms.readInt("user"));
					ac.getAttributesManager().removeAttributes(ac.getSession(), facility, user, attributes);
				} else {
					Facility facility = ac.getFacilityById(parms.readInt("facility"));
					ac.getAttributesManager().removeAttributes(ac.getSession(), facility, attributes);
				}

			} else if (parms.contains("vo")) {
				Vo vo = ac.getVoById(parms.readInt("vo"));
				ac.getAttributesManager().removeAttributes(ac.getSession(), vo, attributes);

			} else if (parms.contains("resource")) {
				Resource resource = ac.getResourceById(parms.readInt("resource"));
				if (parms.contains("member")) {
					Member member = ac.getMemberById(parms.readInt("member"));
					ac.getAttributesManager().removeAttributes(ac.getSession(), resource, member, attributes);
				} else if (parms.contains("group")) {
					Group group = ac.getGroupById(parms.readInt("group"));
					if (parms.contains("workWithGroupAttributes")) {
						ac.getAttributesManager().removeAttributes(ac.getSession(), resource, group, attributes, parms.readInt("workWithGroupAttributes") == 1 );
					} else {
						ac.getAttributesManager().removeAttributes(ac.getSession(), resource, group, attributes);
					}
				} else {
					ac.getAttributesManager().removeAttributes(ac.getSession(), resource, attributes);
				}

			} else if (parms.contains("group")) {
				Group group = ac.getGroupById(parms.readInt("group"));
				ac.getAttributesManager().removeAttributes(ac.getSession(), group, attributes);

			} else if (parms.contains("host")) {
				Host host = ac.getHostById(parms.readInt("host"));
				ac.getAttributesManager().removeAttributes(ac.getSession(), host, attributes);

			} else if (parms.contains("member")) {
				if (parms.contains("workWithUserAttributes")) {
					Member member = ac.getMemberById(parms.readInt("member"));
					if(parms.readInt("workWithUserAttributes") != 1) {
						ac.getAttributesManager().removeAttributes(ac.getSession(), member, false, attributes);
					} else {
						ac.getAttributesManager().removeAttributes(ac.getSession(), member, true, attributes);
					}

				} else {
					Member member = ac.getMemberById(parms.readInt("member"));
					ac.getAttributesManager().removeAttributes(ac.getSession(), member, attributes);
				}
			} else if (parms.contains("user")) {
				User user = ac.getUserById(parms.readInt("user"));
				ac.getAttributesManager().removeAttributes(ac.getSession(), user, attributes);

			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "facility, vo, group, host, resource, member or user");
			}

			return null;
		}
	},

	/*#
	 * Remove attribute of namespace:
	 *
	 * user-facility
	 *
	 * @param facility int Facility ID
	 * @param user int User ID
	 * @param attribute int ID of attribute to remove
	 */
	/*#
	 * Remove attribute of namespace:
	 *
	 * facility
	 *
	 * @param facility int Facility ID
	 * @param attribute int ID of attribute to remove
	 */
	/*#
	 * Remove attribute of namespace:
	 *
	 * vo
	 *
	 * @param vo int VO ID
	 * @param attribute int ID of attribute to remove
	 */
	/*#
	 * Remove attribute of namespace:
	 *
	 * resource
	 *
	 * @param resource int Resource ID
	 * @param attribute int ID of attribute to remove
	 */
	/*#
	 * Remove attribute of namespace:
	 *
	 * group-resource
	 *
	 * @param resource int Resource ID
	 * @param group int Group ID
	 * @param attribute int ID of attribute to remove
	 */
	/*#
	 * Remove attribute of namespace:
	 *
	 * member-resource
	 *
	 * @param resource int Resource ID
	 * @param member int Member ID
	 * @param attribute int ID of attribute to remove
	 */
	/*#
	 * Remove attribute of namespace:
	 *
	 * member
	 *
	 * @param member int Member ID
	 * @param attribute int ID of attribute to remove
	 */
	/*#
	 * Remove attribute of namespace:
	 *
	 * group
	 *
	 * @param group int Group ID
	 * @param attribute int ID of attribute to remove
	 */
	/*#
	 * Remove attribute of namespace:
	 *
	 * host
	 *
	 * @param host int Host ID
	 * @param attribute int ID of attribute to remove
	 */
	/*#
	 * Remove attribute of namespace:
	 *
	 * user
	 *
	 * @param user int User ID
	 * @param attribute int ID of attribute to remove
	 */
	removeAttribute {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("facility")) {
				Facility facility = ac.getFacilityById(parms.readInt("facility"));

				if (parms.contains("user")) {
					ac.getAttributesManager().removeAttribute(ac.getSession(),
							facility, ac.getUserById(parms.readInt("user")),
							ac.getAttributeDefinitionById(parms.readInt("attribute")));
				} else {
					ac.getAttributesManager().removeAttribute(ac.getSession(),
							facility, ac.getAttributeDefinitionById(parms.readInt("attribute")));
				}
				return null;
			} else if (parms.contains("vo")) {
				Vo vo = ac.getVoById(parms.readInt("vo"));

				ac.getAttributesManager().removeAttribute(ac.getSession(),
						vo, ac.getAttributeDefinitionById(parms.readInt("attribute")));
				return null;
			} else if (parms.contains("resource")) {
				if (parms.contains("member")) {
					Resource resource = ac.getResourceById(parms.readInt("resource"));

					ac.getAttributesManager().removeAttribute(ac.getSession(),
							resource,
							ac.getMemberById(parms.readInt("member")),
							ac.getAttributeDefinitionById(parms.readInt("attribute")));
					return null;
				} else if(parms.contains("group")) {
					ac.getAttributesManager().removeAttribute(ac.getSession(),
							ac.getResourceById(parms.readInt("resource")),
							ac.getGroupById(parms.readInt("group")),
							ac.getAttributeDefinitionById(parms.readInt("attribute")));
					return null;
				} else {
					Resource resource = ac.getResourceById(parms.readInt("resource"));

					ac.getAttributesManager().removeAttribute(ac.getSession(),
							resource,
							ac.getAttributeDefinitionById(parms.readInt("attribute")));
					return null;
				}
			} else if (parms.contains("member")) {
				ac.getAttributesManager().removeAttribute(ac.getSession(),
						ac.getMemberById(parms.readInt("member")),
						ac.getAttributeDefinitionById(parms.readInt("attribute")));
				return null;
			} else if (parms.contains("user")) {
				ac.getAttributesManager().removeAttribute(ac.getSession(),
						ac.getUserById(parms.readInt("user")),
						ac.getAttributeDefinitionById(parms.readInt("attribute")));
				return null;
			} else if (parms.contains("group")) {
				ac.getAttributesManager().removeAttribute(ac.getSession(),
						ac.getGroupById(parms.readInt("group")),
						ac.getAttributeDefinitionById(parms.readInt("attribute")));
				return null;
			} else if (parms.contains("host")) {
				ac.getAttributesManager().removeAttribute(ac.getSession(),
						ac.getHostById(parms.readInt("host")),
						ac.getAttributeDefinitionById(parms.readInt("attribute")));
				return null;
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "facility, vo, group, resource, member, host or user");
			}
		}
	},

	/*#
	 * Unset all attributes for the user on the facility.
	 *
	 * @param facility int Facility ID
	 * @param user int User ID
	 */
	/*#
	 * Unset all attributes for the facility and also user-facility attributes if workWithUserAttributes == 1.
	 *
	 * @param facility int Facility ID
	 * @param workWithUserAttributes int Remove also user facility attributes integer (1 = true, 0 = false). 0 is default value.
	 */
	/*#
	 * Unset all attributes for the facility.
	 *
	 * @param facility int Facility ID
	 */
	/*#
	 * Unset all attributes for the vo.
	 *
	 * @param vo int Vo ID
	 */
	/*#
	 * Unset all attributes for the member on the resource.
	 *
	 * @param member int Member ID
	 * @param resource int Resource ID
	 */
	/*#
	 * Unset all group-resource attributes and also group attributes if WorkWithGroupAttributes == 1.
	 *
	 * @param group int Group ID
	 * @param resource int Resource ID
	 * @param workWithGroupAttributes int Work with group attributes integer (1 = true, 0 = false). 0 is default value.
	 */
	/*#
	 * Unset all group-resource attributes.
	 *
	 * @param group int Group ID
	 * @param resource int Resource ID
	 */
	/*#
	 * Unset all resource attributes.
	 *
	 * @param resource int Resource ID
	 */
	/*#
	 * Unset all member attributes.
	 *
	 * @param member int Member ID
	 */
	/*#
	 * Unset all user attributes.
	 *
	 * @param user int User ID
	 */
	/*#
	 * Unset all group attributes.
	 *
	 * @param group int Group ID
	 */
	/*#
	 * Unset all host attributes.
	 *
	 * @param host int Host ID
	 */
	removeAllAttributes {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("facility")) {
				Facility facility = ac.getFacilityById(parms.readInt("facility"));
				if (parms.contains("user")) {
					ac.getAttributesManager().removeAllAttributes(ac.getSession(),
							facility, ac.getUserById(parms.readInt("user")));
				} else if (parms.contains("removeAlsoUserFacilityAttributes")) {
					ac.getAttributesManager().removeAllAttributes(ac.getSession(),
							facility, parms.readInt("workWithUserAttributes") == 1);
				} else {
					ac.getAttributesManager().removeAllAttributes(ac.getSession(),
							facility);
				}
				return null;
			} else if (parms.contains("vo")) {
				Vo vo = ac.getVoById(parms.readInt("vo"));

				ac.getAttributesManager().removeAllAttributes(ac.getSession(),
						vo);
				return null;
			} else if (parms.contains("resource")) {
				if (parms.contains("member")) {
					ac.getAttributesManager().removeAllAttributes(ac.getSession(),
							ac.getResourceById(parms.readInt("resource")),
							ac.getMemberById(parms.readInt("member")));
					return null;
				}else if(parms.contains("group")) {
					if (parms.contains("workWithGroupAttributes")) {
						ac.getAttributesManager().removeAllAttributes(ac.getSession(),
								ac.getResourceById(parms.readInt("resource")),
								ac.getGroupById(parms.readInt("group")),
								parms.readInt("workWithGroupAttributes") == 1);
					} else {
						ac.getAttributesManager().removeAllAttributes(ac.getSession(),
								ac.getResourceById(parms.readInt("resource")),
								ac.getGroupById(parms.readInt("group")));
					}
					return null;
				} else {
					ac.getAttributesManager().removeAllAttributes(ac.getSession(),
							ac.getResourceById(parms.readInt("resource")));
					return null;
				}
			} else if (parms.contains("member")) {
				ac.getAttributesManager().removeAllAttributes(ac.getSession(),
						ac.getMemberById(parms.readInt("member")));
				return null;
			} else if (parms.contains("user")) {
				ac.getAttributesManager().removeAllAttributes(ac.getSession(),
						ac.getUserById(parms.readInt("user")));
				return null;
			} else if (parms.contains("group")) {
				ac.getAttributesManager().removeAllAttributes(ac.getSession(),
						ac.getGroupById(parms.readInt("group")));
				return null;
			} else if (parms.contains("host")) {
				ac.getAttributesManager().removeAllAttributes(ac.getSession(),
						ac.getHostById(parms.readInt("host")));
				return null;
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "facility, resource, vo, group, member, host or user");
			}
		}
	},

	/*#
	 * Get all users logins as attributes
	 * (all attributes with URN starting with:
	 * "urn:perun:user:attribute-def:def:login-namespace:").
	 *
	 * @param user int User ID
	 * @return List<Attribute> list of users logins as attributes
	 */
	getLogins {
		@Override
		public List<Attribute> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getAttributesManager().getLogins(ac.getSession(), ac.getUserById(parms.readInt("user")));
		}
	},

	/*#
	 * Updates attribute definition in Perun based on provided AttributeDefinition object.
	 *
	 * Update is done on attribute definition selected by its ID.
	 *
	 * @param attributeDefinition AttributeDefinition object with updated properties to store in DB
	 * @return AttributeDefinition updated attribute definition
	 */
	updateAttributeDefinition {

		@Override
		public AttributeDefinition call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getAttributesManager().updateAttributeDefinition(ac.getSession(), parms.read("attributeDefinition", AttributeDefinition.class));
		}
	},

	/*#
	 * Takes all member-related attributes (member, user, member-resource, user-facility) and tries to fill them and set them.
	 *
	 * @param member int Member ID
	 */
	doTheMagic {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.getAttributesManager().doTheMagic(ac.getSession(), ac.getMemberById(parms.readInt("member")));
			return null;
		}
	},

	/*#
	 * Gets attribute rights.
	 *
	 * @param attributeId int Attribute ID
	 * @return List<AttributeRights> all rights of the attribute
	 */
	getAttributeRights {
		@Override
		public List<AttributeRights> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getAttributesManager().getAttributeRights(ac.getSession(), parms.readInt("attributeId"));
		}
	},

	/*#
	 * Sets all attribute rights in the list given as a parameter.
	 *
	 * @param rights List<AttributeRights> list of attribute rights
	 */
	setAttributeRights {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getAttributesManager().setAttributeRights(ac.getSession(), parms.readList("rights", AttributeRights.class));
			return null;
		}
	};

}
