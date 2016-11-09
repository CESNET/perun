package cz.metacentrum.perun.rpc.methods;

import java.util.ArrayList;
import java.util.List;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.PerunException;
import cz.metacentrum.perun.rpc.ApiCaller;
import cz.metacentrum.perun.rpc.ManagerMethod;
import cz.metacentrum.perun.core.api.exceptions.RpcException;
import cz.metacentrum.perun.rpc.deserializer.Deserializer;

public enum AttributesManagerMethod implements ManagerMethod {

	/*#
	 * Returns all non-empty User-Facility attributes for selected User and Facility.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param user int User <code>id</code>
	 * @return List<Attribute> All non-empty User-Facility attributes
	 * @throw UserNotExistsException When User with <code>id</code> doesn't exist.
	 * @throw FacilityNotExistsException When Facility with <code>id</code> doesn't exist.
	 */
	/*#
	 * Returns all non-empty Facility attributes for selected Facility.
	 *
	 * @param facility int Facility <code>id</code>
	 * @return List<Attribute> All non-empty Facility attributes
	 * @throw FacilityNotExistsException When Facility with <code>id</code> doesn't exist.
	 */
	/*#
	 * Returns all non-empty Vo attributes for selected Vo.
	 *
	 * @param vo int Vo <code>id</code>
	 * @return List<Attribute> All non-empty Vo attributes
	 * @throw VoNotExistsException When Vo with <code>id</code> doesn't exist.
	 */
	/*#
	 * Returns all specified Vo attributes for selected Vo.
	 *
	 * @param vo int VO <code>id</code>
	 * @param attrNames List<String> Attribute names
	 * @return List<Attribute> Specified Vo attributes
	 * @throw VoNotExistsException When Vo with <code>id</code> doesn't exist.
	 * @exampleParam attrNames [ "urn:perun:vo:attribute-def:def:contactEmail" , "urn:perun:vo:attribute-def:core:shortName" ]
	 */
	/*#
	 * Returns all non-empty UserExtSource attributes for selected UserExtSource.
	 *
	 * @param userExtSource int UserExtSource <code>id</code>
	 * @return List<Attribute> All non-empty UserExtSource attributes
	 * @throw UserExtSourceNotExistsException When Ues with <code>id</code> doesn't exist.
	 */
	/*#
	 * Returns all specified UserExtSource attributes for selected UserExtSource.
	 *
	 * @param userExtSource int UserExtSource <code>id</code>
	 * @param attrNames List<String> Attribute names
	 * @return List<Attribute> Specified UserExtSource attributes
	 * @throw UserExtSourceNotExistsException When userExtSource with <code>id</code> doesn't exist.
	 * @exampleParam attrNames [ "urn:perun:ues:attribute-def:opt:optionalAttribute" ]
	 */
	/*#
	 * Returns all non-empty Member-Resource attributes for selected Member and Resource.
	 *
	 * @param member int Member <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @param workWithUserAttributes boolean If <code>true</code>, return also User and Member attributes. <code>False</code> is default.
	 * @return List<Attribute> All non-empty Member-Resource attributes
	 * @throw MemberNotExistsException When Member with <code>id</code> doesn't exist.
	 * @throw ResourceNotExistsException When Resource with <code>id</code> doesn't exist.
	 */
	/*#
	 * Returns all non-empty Member-Resource attributes for selected Member and Resource.
	 *
	 * @param member int Member <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @return List<Attribute> All non-empty Member-Resource attributes
	 * @throw MemberNotExistsException When Member with <code>id</code> doesn't exist.
	 * @throw ResourceNotExistsException When Resource with <code>id</code> doesn't exist.
	 */
	/*#
	 * Returns all non-empty Group-Resource attributes for selected Group and Resource.
	 *
	 * @param group int Group <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @return List<Attribute> All non-empty Group-Resource attributes
	 * @throw GroupNotExistsException When Group with <code>id</code> doesn't exist.
	 * @throw ResourceNotExistsException When Resource with <code>id</code> doesn't exist.
	 */
	/*#
	 * Returns all non-empty Group-Resource attributes for selected Group and Resource.
	 * If <code>workWithGroupAttributes == true</code> then also all non-empty Group attributes are returned.
	 *
	 * @param group int Group <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @param workWithGroupAttributes boolean If <code>true</code>, return also Group attributes. <code>False</code> is default.
	 * @return List<Attribute> All non-empty Group-Resource attributes
	 * @throw GroupNotExistsException When Group with <code>id</code> doesn't exist.
	 * @throw ResourceNotExistsException When Resource with <code>id</code> doesn't exist.
	 */
	/*#
	 * Returns all non-empty Resource attributes for selected Resource.
	 *
	 * @param resource int Resource <code>id</code>
	 * @return List<Attribute> All non-empty Resource attributes
	 * @throw ResourceNotExistsException When Resource with <code>id</code> doesn't exist.
	 */
	/*#
	 * Returns all specified Member-Group attributes for selected Member and Group.
	 * If <code>workWithUserAttribute == true</code> then also all non-empty User attributes are returned.
	 *
	 * @param member int Member <code>id</code>
	 * @param group int Group <code>id</code>
	 * @param attrNames List<String> Attribute names
	 * @param workWithUserAttributes boolean If <code>true</code>, return also User and Member attributes. <code>False</code> is default.
	 * @return List<Attribute> Specified Member-Group attributes
	 * @throw GroupNotExistsException When Group with <code>id</code> doesn't exist.
	 * @throw MemberNotExistsException When Member with <code>id</code> doesn't exist.
	 */
	/*#
	 * Returns all non-empty Member attributes for selected Member.
	 *
	 * @param member int Member <code>id</code>
	 * @return List<Attribute> All non-empty Member attributes
	 * @throw MemberNotExistsException When Member with <code>id</code> doesn't exist.
	 */
	/*#
	 * Returns all non-empty Member attributes for selected Member.
	 * If <code>workWithUserAttributes == true</code> then also all non-empty User attributes are returned.
	 *
	 * @param member int Member <code>id</code>
	 * @param workWithUserAttributes boolean If <code>true</code>, return also User attributes. <code>False</code> is default.
	 * @return List<Attribute> All non-empty Member attributes
	 * @throw MemberNotExistsException When Member with <code>id</code> doesn't exist.
	 */
	/*#
	 * Returns all specified Member-Group attributes for selected Member and Group
	 *
	 * @param member int Member <code>id</code>
	 * @param group int Group <code>id</code>
	 * @param attrNames List<String> Attribute names
	 * @return List<Attribute> Specified Member-Group attributes
	 * @throw GroupNotExistsException When Group with <code>id</code> doesn't exist.
	 * @throw MemberNotExistsException When Member with <code>id</code> doesn't exist.
	 */
	/*#
	 * Returns all specified Member attributes for selected Member.
	 *
	 * @param member int Member <code>id</code>
	 * @param attrNames List<String> Attribute names
	 * @return List<Attribute> Specified Member attributes
	 * @throw MemberNotExistsException When Member with <code>id</code> doesn't exist.
	 * @exampleParam attrNames [ "urn:perun:member:attribute-def:def:mail" , "urn:perun:member:attribute-def:def:membershipExpiration" ]
	 */
	/*#
	 * Returns all non-empty Member-Group attributes for selected Member and Group.
	 *
	 * @param member int Member <code>id</code>
	 * @param group int Group <code>id</code>
	 * @return List<Attribute> All Member-Group attributes
	 * @throw GroupNotExistsException When Group with <code>id</code> doesn't exist.
	 * @throw MemberNotExistsException When Member with <code>id</code> doesn't exist.
	 */
	/*#
	 * Returns all non-empty User attributes for selected User.
	 *
	 * @param user int User <code>id</code>
	 * @return List<Attribute> All non-empty User attributes
	 * @throw UserNotExistsException When User with <code>id</code> doesn't exist.
	 */
	/*#
	 * Returns all specified User attributes for selected User.
	 *
	 * @param user int User <code>id</code>
	 * @param attrNames List<String> Attribute names
	 * @return List<Attribute> Specified User attributes
	 * @throw UserNotExistsException When User with <code>id</code> doesn't exist.
	 * @exampleParam attrNames [ "urn:perun:user:attribute-def:def:phone" , "urn:perun:user:attribute-def:def:preferredMail" ]
	 */
	/*#
	 * Returns all non-empty Group attributes for selected Group.
	 *
	 * @param group int Group <code>id</code>
	 * @return List<Attribute> All non-empty Group attributes
	 * @throw GroupNotExistsException When Group with <code>id</code> doesn't exist.
	 */
	/*#
	 * Returns all specified Group attributes for selected Group.
	 *
	 * @param group int Group <code>id</code>
	 * @param attrNames List<String> Attribute names
	 * @return List<Attribute> Specified Group attributes
	 * @throw GroupNotExistsException When Group with <code>id</code> doesn't exist.
	 * @exampleParam attrNames [ "urn:perun:user:attribute-def:core:description" , "urn:perun:user:attribute-def:def:synchronizationEnabled" ]
	 */
	/*#
	 * Returns all non-empty Host attributes for selected Host.
	 *
	 * @param host int Host <code>id</code>
	 * @return List<Attribute> All non-empty Host attributes
	 * @throw HostNotExistsException When Group with <code>id</code> doesn't exist.
	 * @exampleParam attrNames [ "urn:perun:host:attribute-def:core:hostname" , "urn:perun:host:attribute-def:def:frontend" ]
	 */
	/*#
	 * Returns all non-empty Entityless attributes with subject equaled <code>key</code>.
	 *
	 * @param key String String <code>key</code>
	 * @return List<Attribute> All non-empty Entityless attributes
	 */
	getAttributes {

		@Override
		public List<Attribute> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("facility")) {
				if (parms.contains("user")) {
					if (parms.contains("member") && parms.contains("resource")) {
						return ac.getAttributesManager().getAttributes(ac.getSession(),
								ac.getFacilityById(parms.readInt("facility")),
								ac.getResourceById(parms.readInt("resource")),
								ac.getUserById(parms.readInt("user")),
								ac.getMemberById(parms.readInt("member")));
					} else {
						return ac.getAttributesManager().getAttributes(ac.getSession(),
								ac.getFacilityById(parms.readInt("facility")),
								ac.getUserById(parms.readInt("user")));
					}
				} else {
					return ac.getAttributesManager().getAttributes(ac.getSession(),
							ac.getFacilityById(parms.readInt("facility")));
				}
			} else if (parms.contains("vo")) {
				if (parms.contains("attrNames")) {
					return ac.getAttributesManager().getAttributes(ac.getSession(),
							ac.getVoById(parms.readInt("vo")),
							parms.readList("attrNames", String.class));
				} else {
					return ac.getAttributesManager().getAttributes(ac.getSession(),
							ac.getVoById(parms.readInt("vo")));
				}
			} else if (parms.contains("resource")) {
				if (parms.contains("member")) {
					if (parms.contains("workWithUserAttributes")) {
						return ac.getAttributesManager().getAttributes(ac.getSession(),
								ac.getResourceById(parms.readInt("resource")),
								ac.getMemberById(parms.readInt("member")),
								parms.readBoolean("workWithUserAttributes"));
					} else {
						return ac.getAttributesManager().getAttributes(ac.getSession(),
								ac.getResourceById(parms.readInt("resource")),
								ac.getMemberById(parms.readInt("member")));
					}
				}  else if (parms.contains("group")) {
					if (parms.contains("workWithGroupAttributes")) {
						return ac.getAttributesManager().getAttributes(ac.getSession(),
								ac.getResourceById(parms.readInt("resource")),
								ac.getGroupById(parms.readInt("group")),
								parms.readBoolean("workWithGroupAttributes"));
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
				if (parms.contains("workWithUserAttributes")) {
					if (parms.contains("attrNames")) {
						if (parms.contains("group")) {
							return ac.getAttributesManager().getAttributes(ac.getSession(),
									ac.getMemberById(parms.readInt("member")),
									ac.getGroupById(parms.readInt("group")),
									parms.readList("attrNames", String.class),
									parms.readBoolean("workWithUserAttributes"));
						} else {
							return ac.getAttributesManager().getAttributes(ac.getSession(),
									ac.getMemberById(parms.readInt("member")),
									parms.readList("attrNames", String.class),
									parms.readBoolean("workWithUserAttributes"));
						}
					} else {
						return ac.getAttributesManager().getAttributes(ac.getSession(),
								ac.getMemberById(parms.readInt("member")),
								parms.readBoolean("workWithUserAttributes"));
					}
				} else if (parms.contains("attrNames")) {
					if (parms.contains("group")) {
						return ac.getAttributesManager().getAttributes(ac.getSession(),
								ac.getMemberById(parms.readInt("member")),
								ac.getGroupById(parms.readInt("group")),
								parms.readList("attrNames", String.class));
					} else {
						return ac.getAttributesManager().getAttributes(ac.getSession(),
								ac.getMemberById(parms.readInt("member")),
								parms.readList("attrNames", String.class));
					}
				} else if (parms.contains("group")) {
					return ac.getAttributesManager().getAttributes(ac.getSession(),
							ac.getMemberById(parms.readInt("member")),
							ac.getGroupById(parms.readInt("group")));
				} else {
					return ac.getAttributesManager().getAttributes(ac.getSession(),
							ac.getMemberById(parms.readInt("member")));
				}
			} else if (parms.contains("user")) {
				if (parms.contains("attrNames")) {
					return ac.getAttributesManager().getAttributes(ac.getSession(),
							ac.getUserById(parms.readInt("user")),
							parms.readList("attrNames", String.class));
				} else {
					return ac.getAttributesManager().getAttributes(ac.getSession(),
							ac.getUserById(parms.readInt("user")));
				}
			} else if (parms.contains("group")) {
				if (parms.contains("attrNames")) {
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
			} else if (parms.contains("key")) {
				return ac.getAttributesManager().getAttributes(ac.getSession(),
						parms.readString("key"));
			} else if (parms.contains("userExtSource")) {
				if (parms.contains("attrNames")) {
					return ac.getAttributesManager().getAttributes(ac.getSession(),
							ac.getUserExtSourceById(parms.readInt("userExtSource")),
							parms.readList("attrNames", String.class));
				} else {
					return ac.getAttributesManager().getAttributes(ac.getSession(),
							ac.getUserExtSourceById(parms.readInt("userExtSource")));
				}
			}
			else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "facility, vo, resource, member, user, host, group, userExtSource or key");
			}
		}
	},

	/*#
	 * Returns all entityless attributes with <code>attrName</code> (for all namespaces of same attribute).
	 *
	 * @param attrName String Attribute name
	 * @return List<Attribute> All entityless attributes with same name
	 * @exampleParam attrName "urn:perun:entityless:attribute-def:def:namespace-minUID"
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
	 * @param facility int Facility <code>id</code>
	 * @param user int User <code>id</code>
	 * @param member int Member <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @param attributes List<Attribute> List of attributes
	 */
	/*#
	 * Sets the attributes.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param user int User <code>id</code>
	 * @param attributes List<Attribute> List of attributes
	 */
	/*#
	 * Sets the attributes.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param attributes List<Attribute> List of attributes
	 */
	/*#
	 * Sets the attributes.
	 *
	 * @param vo int VO <code>id</code>
	 * @param attributes List<Attribute> List of attributes
	 */
	/* Sets the attributes.
	 *
	 * @param userExtSource int UserExtSource <code>id</code>
	 * @param attributes List<Attribute> List of attributes
	 */
	/*#
	 * Sets the attributes.
	 *
	 * @param member int Member <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @param attributes List<Attribute> List of attributes
	 */
	/*#
	 * Sets the attributes.
	 *
	 * @param member int Member <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @param workWithUserAttributes boolean Work with user attributes. False is default value.
	 * @param attributes List<Attribute> List of attributes
	 */
	/*#
	 * Sets the attributes.
	 *
	 * @param group int Group <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @param attributes List<Attribute> List of attributes
	 */
	/*#
	 * Sets the attributes.
	 *
	 * @param group int Group <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @param workWithGroupAttributes boolean Work with group attributes. False is default value.
	 * @param attributes List<Attribute> List of attributes
	 */
	/*#
	 * Sets the attributes.
	 *
	 * @param resource int Resource <code>id</code>
	 * @param attributes List<Attribute> List of attributes
	 */
	/*#
	 * Sets the attributes.
	 *
	 * @param member int Member <code>id</code>
	 * @param group int Group <code>id</code>
	 * @param attributes List<Attribute> List of attributes
	 * @param workWithUserAttributes boolean If <code>true</code>, store also User and Member attributes. <code>False</code> is default.
	 */
	/*#
	 * Sets the attributes.
	 *
	 * @param member int Member <code>id</code>
	 * @param workWithUserAttributes boolean Work with user attributes. False is default value.
	 * @param attributes List<Attribute> List of attributes
	 */
	/*#
	 * Sets the attributes.
	 * 
	 * @param member int Member <code>id</code>
	 * @param group int Group <code>id</code>
	 * @param attributes List<Attribute> List of attributes
	 */
	/*#
	 * Sets the attributes.
	 *
	 * @param member int Member <code>id</code>
	 * @param attributes List<Attribute> List of attributes
	 */
	/*#
	 * Sets the attributes.
	 *
	 * @param user int User <code>id</code>
	 * @param attributes List<Attribute> List of attributes
	 */

	/*#
	 * Sets the attributes.
	 *
	 * @param group int Group <code>id</code>
	 * @param attributes List<Attribute> List of attributes
	 */
	/*#
	 * Sets the attributes.
	 *
	 * @param host int Host <code>id</code>
	 * @param attributes List<Attribute> List of attributes
	 */
	setAttributes {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("facility")) {
				if (parms.contains("user")) {
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
								parms.readBoolean("workWithUserAttributes"));
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
								parms.readBoolean("workWithGroupAttributes"));
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
				if (parms.contains("workWithUserAttributes")) {
					if (parms.contains("group")) {
						ac.getAttributesManager().setAttributes(ac.getSession(),
								ac.getMemberById(parms.readInt("member")),
								ac.getGroupById(parms.readInt("group")),
								parms.readList("attributes", Attribute.class),
								parms.readBoolean("workWithUserAttributes"));
					} else {
						ac.getAttributesManager().setAttributes(ac.getSession(),
								ac.getMemberById(parms.readInt("member")),
								parms.readList("attributes", Attribute.class),
								parms.readBoolean("workWithUserAttributes"));
					}
				} else if (parms.contains("group")) {
					ac.getAttributesManager().setAttributes(ac.getSession(),
							ac.getMemberById(parms.readInt("member")),
							ac.getGroupById(parms.readInt("group")),
							parms.readList("attributes", Attribute.class));
				} else {
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
			} else if (parms.contains("userExtSource")) {
				ac.getAttributesManager().setAttributes(ac.getSession(),
						ac.getUserExtSourceById(parms.readInt("userExtSource")),
						parms.readList("attributes", Attribute.class));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "facility, vo, resource, member, user, host, group or userExtSource");
			}

			return null;
		}
	},

	/*#
	 * Returns an Attribute by its <code>id</code>. Returns only non-empty attributes.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param user int User <code>id</code>
	 * @param attributeId int Attribute <code>id</code>
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its <code>id</code>. Returns only non-empty attributes.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param attributeId int Attribute <code>id</code>
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its <code>id</code>. Returns only non-empty attributes.
	 *
	 * @param vo int VO <code>id</code>
	 * @param attributeId int Attribute <code>id</code>
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its <code>id</code>. Returns only non-empty attributes.
	 *
	 * @param member int Member <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @param attributeId int Attribute <code>id</code>
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its <code>id</code>. Returns only non-empty attributes.
	 *
	 * @param group int Group <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @param attributeId int Attribute <code>id</code>
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its <code>id</code>. Returns only non-empty attributes.
	 *
	 * @param resource int Resource <code>id</code>
	 * @param attributeId int Attribute <code>id</code>
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its <code>id</code>. Returns only non-empty attributes.
	 *
	 * @param member int Member <code>id</code>
	 * @param group int Group <code>id</code>
	 * @param attributeId int Attribute <code>id</code>
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its <code>id</code>. Returns only non-empty attributes.
	 *
	 * @param member int Member <code>id</code>
	 * @param attributeId int Attribute <code>id</code>
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its <code>id</code>. Returns only non-empty attributes.
	 *
	 * @param user int User <code>id</code>
	 * @param attributeId int Attribute <code>id</code>
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its <code>id</code>. Returns only non-empty attributes.
	 *
	 * @param host int Host <code>id</code>
	 * @param attributeId int Attribute <code>id</code>
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its <code>id</code>. Returns only non-empty attributes.
	 *
	 * @param userExtSource int UserExtSource <code>id</code>
	 * @param attributeId int Attribute <code>id</code>
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its name. Returns only non-empty attributes.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param user int User <code>id</code>
	 * @param attributeName String Attribute name
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its name. Returns only non-empty attributes.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param attributeName String Attribute name
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its name. Returns only non-empty attributes.
	 *
	 * @param vo int VO <code>id</code>
	 * @param attributeName String Attribute name
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its name. Returns only non-empty attributes.
	 *
	 * @param member int Member <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @param attributeName String Attribute name
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its name. Returns only non-empty attributes.
	 *
	 * @param group int Group <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @param attributeName String Attribute name
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its name. Returns only non-empty attributes.
	 *
	 * @param group int Group <code>id</code>
	 * @param attributeName String Attribute name
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its name. Returns only non-empty attributes.
	 *
	 * @param resource int Resource <code>id</code>
	 * @param attributeName String Attribute name
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its name. Returns only non-empty attributes.
	 *
	 * @param member int Member <code>id</code>
	 * @param group int Group <code>id</code>
	 * @param attributeName String Attribute name   
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its name. Returns only non-empty attributes.
	 *
	 * @param member int Member <code>id</code>
	 * @param attributeName String Attribute name
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its name. Returns only non-empty attributes.
	 *
	 * @param user int User <code>id</code>
	 * @param attributeName String Attribute name
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its name. Returns only non-empty attributes.
	 *
	 * @param host int Host <code>id</code>
	 * @param attributeName String Attribute name
	 * @return Attribute Found Attribute
	 */
	/*#
	 * Returns an Attribute by its name. Returns only non-empty attributes.
	 *
	 * @param userExtSource int UserExtSource <code>id</code>
	 * @param attributeName String Attribute name
	 * @return Attribute Found Attribute
	 */
	getAttribute {

		@Override
		public Attribute call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("attributeId")) {
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
					} else if (parms.contains("group")) {
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
					if (parms.contains("group")) {
						return ac.getAttributesManager().getAttributeById(ac.getSession(),
								ac.getMemberById(parms.readInt("member")),
								ac.getGroupById(parms.readInt("group")),
								parms.readInt("attributeId"));	
					} else {
						return ac.getAttributesManager().getAttributeById(ac.getSession(),
								ac.getMemberById(parms.readInt("member")),
								parms.readInt("attributeId"));
					}
				} else if (parms.contains("user")) {
					return ac.getAttributesManager().getAttributeById(ac.getSession(),
							ac.getUserById(parms.readInt("user")),
							parms.readInt("attributeId"));
				} else if (parms.contains("group")) {
					return ac.getAttributesManager().getAttributeById(ac.getSession(),
							ac.getGroupById(parms.readInt("group")),
							parms.readInt("attributeId"));
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
				} else if (parms.contains("userExtSource")) {
					return ac.getAttributesManager().getAttributeById(ac.getSession(),
							ac.getUserExtSourceById(parms.readInt("userExtSource")),
							parms.readInt("attributeId"));
				} else {
					throw new RpcException(RpcException.Type.MISSING_VALUE, "facility, vo, resource, member, user, host, key, group or userExtSource");
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
					} else if (parms.contains("group")) {
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
					if (parms.contains("group")) {
						return ac.getAttributesManager().getAttribute(ac.getSession(),
								ac.getMemberById(parms.readInt("member")),
								ac.getGroupById(parms.readInt("group")),
								parms.readString("attributeName"));
					} else {
						return ac.getAttributesManager().getAttribute(ac.getSession(),
								ac.getMemberById(parms.readInt("member")),
								parms.readString("attributeName"));
					}
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
				} else if (parms.contains("userExtSource")) {
					return ac.getAttributesManager().getAttribute(ac.getSession(),
							ac.getUserExtSourceById(parms.readInt("userExtSource")),
							parms.readString("attributeName"));
				} else if (parms.contains("key")) {
					return ac.getAttributesManager().getAttribute(ac.getSession(),
							parms.readString("key"),
							parms.readString("attributeName"));
				} else {
					throw new RpcException(RpcException.Type.MISSING_VALUE, "facility, vo, resource, member, user, host, key, group or userExtSource");
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
	 * @param id int Attribute <code>id</code>
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
	 * @param member int <code>id</code> of Member
	 * @param user int <code>id</code> of User
	 * @param vo int <code>id</code> of Virtual organization
	 * @param group int <code>id</code> of Group
	 * @param resource int <code>id</code> of Resource
	 * @param facility int <code>id</code> of Facility
	 * @param host int <code>id</code> of Host
	 * @param userExtSource int <code>id</code> of UserExtSource
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
			UserExtSource ues = null;
			//Not supported entityless attirbutes now
			//String entityless = null;
			List<PerunBean> entities = new ArrayList<PerunBean>();

			//If member exists in query
			if (parms.contains("member")) {
				member = ac.getMemberById(parms.readInt("member"));
				entities.add(member);
			}
			//If user exists in query
			if (parms.contains("user")) {
				user = ac.getUserById(parms.readInt("user"));
				entities.add(user);
			}
			//If vo exists in query
			if (parms.contains("vo")) {
				vo = ac.getVoById(parms.readInt("vo"));
				entities.add(vo);
			}
			//If group exists in query
			if (parms.contains("group")) {
				group = ac.getGroupById(parms.readInt("group"));
				entities.add(group);
			}
			//If resource exists in query
			if (parms.contains("resource")) {
				resource = ac.getResourceById(parms.readInt("resource"));
				entities.add(resource);
			}
			//If facility exists in query
			if (parms.contains("facility")) {
				facility = ac.getFacilityById(parms.readInt("facility"));
				entities.add(facility);
			}
			//If host exists in query
			if (parms.contains("host")) {
				host = ac.getHostById(parms.readInt("host"));
				entities.add(host);
			}
			//If userExtSource exists in query
			if (parms.contains("userExtSource")) {
				ues = ac.getUserExtSourceById(parms.readInt("userExtSource"));
				entities.add(ues);
			}
			//If entityless exists in query
			/*if(parms.contains("entityless")) {

				}*/

			List<AttributeDefinition> attributesDefinition = ac.getAttributesManager().getAttributesDefinitionWithRights(ac.getSession(), entities);

			return attributesDefinition;
		}
	},

	/*#
	 * Sets an Attribute.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param user int User <code>id</code>
	 * @param attribute Attribute JSON object
	 */
	/*#
	 * Sets an Attribute.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param attribute Attribute JSON object
	 */
	/*#
	 * Sets an Attribute.
	 *
	 * @param vo int VO <code>id</code>
	 * @param attribute Attribute JSON object
	 */
	/*#
	 * Sets an Attribute.
	 *
	 * @param member int Member <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @param attribute Attribute JSON object
	 */
	/*#
	 * Sets an Attribute.
	 *
	 * @param group int Group <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @param attribute Attribute JSON object
	 */
	/*#
	 * Sets an Attribute.
	 *
	 * @param resource int Resource <code>id</code>
	 * @param attribute Attribute JSON object
	 */
	/*#
	 * Sets an Attribute.
	 *
	 * @param member int Member <code>id</code>
	 * @param group int Group <code>id</code>
	 * @param attribute Attribute JSON object
	 */
	/*#
	 * Sets an Attribute.
	 *
	 * @param member int Member <code>id</code>
	 * @param attribute Attribute JSON object
	 */
	/*#
	 * Sets an Attribute.
	 *
	 * @param user int User <code>id</code>
	 * @param attribute Attribute JSON object
	 */
	/*#
	 * Sets an Attribute.
	 *
	 * @param group int Group <code>id</code>
	 * @param attribute Attribute JSON object
	 *
	 */
	/*#
	 * Sets an Attribute.
	 *
	 * @param host int Host <code>id</code>
	 * @param attribute Attribute JSON object
	 */
	/*#
	 * Sets an Attribute.
	 *
	 * @param userExtSource int UserExtSource <code>id</code>
	 * @param attribute Attribute JSON object
	 */
	setAttribute {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("facility")) {
				if (parms.contains("user")) {
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
				} else if (parms.contains("group")) {
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
				if (parms.contains("group")) {
					ac.getAttributesManager().setAttribute(ac.getSession(),
							ac.getMemberById(parms.readInt("member")),
							ac.getGroupById(parms.readInt("group")),
							parms.read("attribute", Attribute.class));
				} else {
					ac.getAttributesManager().setAttribute(ac.getSession(),
							ac.getMemberById(parms.readInt("member")),
							parms.read("attribute", Attribute.class));
				}
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
			} else if (parms.contains("userExtSource")) {
				ac.getAttributesManager().setAttribute(ac.getSession(),
						ac.getUserExtSourceById(parms.readInt("userExtSource")),
						parms.read("attribute", Attribute.class));
			} else if (parms.contains("key")) {
				ac.getAttributesManager().setAttribute(ac.getSession(),
						parms.readString("key"),
						parms.read("attribute", Attribute.class));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "facility, vo, resource, user, member, host, key, group or userExtSource");
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

			return ac.getAttributesManager().createAttribute(ac.getSession(), attribute);

		}
	},

	/*#
	 * Deletes attribute definition from Perun.
	 *
	 * Deletion fails if any entity in Perun has
	 * any value for this attribute set.
	 *
	 * @param attribute int AttributeDefinition <code>id</code>
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
	 * @param member int Member <code>id</code>
	 * @param service int Service <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param member int Member <code>id</code>
	 * @param service int Service <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @param workWithUserAttributes boolean Work with user attributes. False is default value.
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param group int Group <code>id</code>
	 * @param service int Service <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param service int Service <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 * 
	 * @param service int Service <code>id</code>
	 * @param member int Member <code>id</code>   
	 * @param group int Group <code>id</code>
	 * @param workWithUserAttributes boolean If <code>true</code>, return also User and Member attributes. <code>False</code> is default.
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param service int Service <code>id</code>
	 * @param member int Member <code>id</code>   
	 * @param group int Group <code>id</code>
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param service int Service <code>id</code>
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param services List<int> list of Service IDs
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param host int Host <code>id</code>
	 * @param service int Service <code>id</code>
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param member int Member <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param member int Member <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @param workWithUserAttributes boolean Work with user attributes. False is default value.
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param resource int Resource <code>id</code>
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param user int User <code>id</code>
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param facility int Facility <code>id</code>
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param member int Member <code>id</code>
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param member int Member <code>id</code>
	 * @param workWithUserAttributes boolean Work with user attributes. False is default value.
	 * @return List<Attribute> Required Attributes
	 */
	/*#
	 * Returns required attributes.
	 *
	 * @param user int User <code>id</code>
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
									parms.readBoolean("workWithUserAttributes"));
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
				} else if (parms.contains("member")) {
					if (parms.contains("group")) {
						if (parms.contains("workWithUserAttributes")) {
							return ac.getAttributesManager().getRequiredAttributes(ac.getSession(),
									ac.getServiceById(parms.readInt("service")),
									ac.getMemberById(parms.readInt("member")),
									ac.getGroupById(parms.readInt("group")),
									parms.readBoolean("workWithUserAttributes"));
						} else {
							return ac.getAttributesManager().getRequiredAttributes(ac.getSession(),
									ac.getServiceById(parms.readInt("service")),
									ac.getMemberById(parms.readInt("member")),
									ac.getGroupById(parms.readInt("group")));
						}
					} else {
						throw new RpcException(RpcException.Type.MISSING_VALUE, "group");
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
			} else if (parms.contains("services")) {
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
								ac.getMemberById(parms.readInt("member")), parms.readBoolean("workWithUserAttributes"));
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
				if (parms.contains("workWithUserAttributes")) {
					return ac.getAttributesManager().getRequiredAttributes(ac.getSession(),
							ac.getMemberById(parms.readInt("member")), parms.readBoolean("workWithUserAttributes"));
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
	 * @param service int Service <code>id</code>
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
	 * Gets member, user, member-resource and user-facility attributes.
	 * It returns attributes required by all services assigned to specified resource. Both empty and non-empty attributes are returned.
	 *
	 * @param resourceToGetServicesFrom int Resource to get services from <code>id</code>
	 * @param facility int Facility <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @param user int User <code>id</code>
	 * @param member int Member <code>id</code>
	 * @return List<Attribute> Member, user, member-resource and user-facility attributes
	 */
	/*#
	 * Gets member-resource attributes and also user, user-facility and member attributes, if workWithUserAttributes == true.
	 * It returns attributes required by all services assigned to specified resource. Both empty and non-empty attributes are returned.
	 *
	 * @param resourceToGetServicesFrom int Resource to get services from <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @param member int Member <code>id</code>
	 * @param workWithUserAttributes boolean Work with user attributes. False is default value.
	 * @return List<Attribute> Member-resource attributes (if workWithUserAttributes == true also user, user-facility and member attributes)
	 */
	/*#
	 * Gets member-resource attributes.
	 * It returns attributes required by all services assigned to specified resource. Both empty and non-empty attributes are returned.
	 *
	 * @param resourceToGetServicesFrom int Resource to get services from <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @param member int Member <code>id</code>
	 * @return List<Attribute> Member-resource attributes
	 */
	/*#
	 * Gets member-group attributes and also user and member attributes, if workWithUserAttributes == true.
	 * It returns attributes required by all services assigned to specified resource. Both empty and non-empty attributes are returned.
	 * 
	 * @param resourceToGetServicesFrom int Resource to get services from <code>id</code>
	 * @param member int Member <code>id</code>
	 * @param group int Group <code>id</code>
	 * @param workWithUserAttributes boolean If <code>true</code>, return also User and Member attributes. <code>False</code> is default.
	 * @return List<Attribute> Member-group attributes
	 */
	/*#
	 * Gets member-group attributes.
	 * It returns attributes required by all services assigned to specified resource. Both empty and non-empty attributes are returned.
	 *
	 * @param resourceToGetServicesFrom int Resource to get services from <code>id</code>
	 * @param member int Member <code>id</code>
	 * @param group int Group <code>id</code>
	 * @return List<Attribute> Member-group attributes
	 */
	/*#
	 * Gets member attributes.
	 * It returns attributes required by all services assigned to specified resource. Both empty and non-empty attributes are returned.
	 *
	 * @param member int Member <code>id</code>
	 * @param resourceToGetServicesFrom int Resource to get services from <code>id</code>
	 * @return List<Attribute> Member attributes
	 */
	/*#
	 * Gets user-facility attributes.
	 * It returns attributes required by all services assigned to specified resource. Both empty and non-empty attributes are returned.
	 *
	 * @param resourceToGetServicesFrom int Resource to get services from <code>id</code>
	 * @param facility int Facility <code>id</code>
	 * @param user int User <code>id</code>
	 * @return List<Attribute> User-facility attributes
	 */
	/*#
	 * Gets user attributes.
	 * It returns attributes required by all services assigned to specified resource. Both empty and non-empty attributes are returned.
	 *
	 * @param user int User <code>id</code>
	 * @param resourceToGetServicesFrom int Resource to get services from <code>id</code>
	 * @return List<Attribute> User's attributes
	 */
	/*#
	 * Gets group-resource and also group attributes, if workWithGroupAttributes == true.
	 * It returns attributes required by all services assigned to specified resource. Both empty and non-empty attributes are returned.
	 *
	 * @param resourceToGetServicesFrom int Resource to get services from <code>id</code>
	 * @param group int Group <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @param workWithGroupAttributes boolean Work with group attributes. False is default value.
	 * @return List<Attribute> Group-resource and (if workWithGroupAttributes == true) group required attributes
	 */
	/*#
	 * Gets group-resource attributes.
	 * It returns attributes required by all services assigned to specified resource. Both empty and non-empty attributes are returned.
	 *
	 * @param resourceToGetServicesFrom int Resource to get services from <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @param group int Group <code>id</code>
	 * @return List<Attribute> Group-resource attributes
	 */
	/*#
	 * Gets group attributes.
	 * It returns attributes required by all services assigned to specified resource. Both empty and non-empty attributes are returned.
	 *
	 * @param resourceToGetServicesFrom int Resource to get services from <code>id</code>
	 * @param group int Group <code>id</code>
	 * @return List<Attribute> Group's attributes
	 */
	/*#
	 * Gets host attributes.
	 * It returns attributes required by all services assigned to specified host. Both empty and non-empty attributes are returned.
	 *
	 * @param resourceToGetServicesFrom int Resource to get services from <code>id</code>
	 * @param host int Host <code>id</code>
	 * @return List<Attribute> Group's attributes
	 */
	getResourceRequiredAttributes {

		@Override
		public List<Attribute> call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("resourceToGetServicesFrom")) {
				if (parms.contains("member")) {
					if (parms.contains("resource")) {
						if (parms.contains("facility") && parms.contains("user")) {
							return ac.getAttributesManager().getResourceRequiredAttributes(ac.getSession(),
									ac.getResourceById(parms.readInt("resourceToGetServicesFrom")),
									ac.getFacilityById(parms.readInt("facility")),
									ac.getResourceById(parms.readInt("resource")),
									ac.getUserById(parms.readInt("user")),
									ac.getMemberById(parms.readInt("member")));
						} else if (parms.contains("workWithUserAttributes")) {
							return ac.getAttributesManager().getResourceRequiredAttributes(ac.getSession(),
									ac.getResourceById(parms.readInt("resourceToGetServicesFrom")),
									ac.getResourceById(parms.readInt("resource")),
									ac.getMemberById(parms.readInt("member")),
									parms.readBoolean("workWithUserAttributes"));
						} else {
							return ac.getAttributesManager().getResourceRequiredAttributes(ac.getSession(),
									ac.getResourceById(parms.readInt("resourceToGetServicesFrom")),
									ac.getResourceById(parms.readInt("resource")),
									ac.getMemberById(parms.readInt("member")));
						}
					} else if (parms.contains("group")) {
						if (parms.contains("workWithUserAttributes")) {
							return ac.getAttributesManager().getResourceRequiredAttributes(ac.getSession(),
									ac.getResourceById(parms.readInt("resourceToGetServicesFrom")),
									ac.getMemberById(parms.readInt("member")),
									ac.getGroupById(parms.readInt("group")),
									parms.readBoolean("workWithUserAttributes"));
						} else {
							return ac.getAttributesManager().getResourceRequiredAttributes(ac.getSession(),
									ac.getResourceById(parms.readInt("resourceToGetServicesFrom")),
									ac.getMemberById(parms.readInt("member")),
									ac.getGroupById(parms.readInt("group")));
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
									parms.readBoolean("workWithGroupAttributes"));
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
				} else if (parms.contains("host")) {
					return ac.getAttributesManager().getResourceRequiredAttributes(ac.getSession(),
							ac.getResourceById(parms.readInt("resourceToGetServicesFrom")),
							ac.getHostById(parms.readInt("host")));
				} else {
					throw new RpcException(RpcException.Type.MISSING_VALUE, "member, group, host or user");
				}
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "resourceToGetServicesFrom");
			}
		}
	},

	/*#
	 * Tries to fill host attribute.
	 *
	 * @param host int Host <code>id</code>
	 * @param attribute int Attribute <code>id</code>
	 * @return Attribute attribute which MAY have filled value
	 */
	/*#
	 * Tries to fill group-resource attribute.
	 *
	 * @param resource int Resource <code>id</code>
	 * @param group int Group <code>id</code>
	 * @param attribute int Attribute <code>id</code>
	 * @return Attribute attribute which MAY have filled value
	 */
	/*#
	 * Tries to fill member-resource attribute.
	 *
	 * @param resource int Resource <code>id</code>
	 * @param member int Member <code>id</code>
	 * @param attribute int Attribute <code>id</code>
	 * @return Attribute attribute which MAY have filled value
	 */
	/*#
	 * Tries to fill resource attribute.
	 *
	 * @param resource int Resource <code>id</code>
	 * @param attribute int Attribute <code>id</code>
	 * @return Attribute attribute which MAY have filled value
	 */
	/*#
	 * Tries to fill user-facility attribute.
	 *
	 * @param user int User <code>id</code>
	 * @param facility int Facility <code>id</code>
	 * @param attribute int Attribute <code>id</code>
	 * @return Attribute attribute which MAY have filled value
	 */
	/*#
	 * Tries to fill user attribute.
	 *
	 * @param user int User <code>id</code>
	 * @param attribute int Attribute <code>id</code>
	 * @return Attribute attribute which MAY have filled value
	 */
	/*#
	 * Tries to fill member-group attribute.
	 *
	 * @param member int Member <code>id</code>
	 * @param group int Group <code>id</code>
	 * @param attribute int Attribute <code>id</code>
	 * @return Attribute attribute which MAY have filled value
	 */
	/*#
	 * Tries to fill member attribute.
	 *
	 * @param member int Member <code>id</code>
	 * @param attribute int Attribute <code>id</code>
	 * @return Attribute attribute which MAY have filled value
	 */
	/*#
	 * Tries to fill group attribute.
	 *
	 * @param group int Group <code>id</code>
	 * @param attribute int Attribute <code>id</code>
	 * @return Attribute attribute which may have filled value
	 */
	fillAttribute {

		@Override
		public Attribute call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("host")) {
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
				if (parms.contains("group")) {
					Group group = ac.getGroupById(parms.readInt("group"));
					return ac.getAttributesManager().fillAttribute(ac.getSession(),
							member,
							group,
							ac.getAttributeById(member, group, parms.readInt("attribute")));	
				} else {
					return ac.getAttributesManager().fillAttribute(ac.getSession(),
							member,
							ac.getAttributeById(member, parms.readInt("attribute")));
				}
			} else if (parms.contains("group")) {
					Group group = ac.getGroupById(parms.readInt("group"));
					return ac.getAttributesManager().fillAttribute(ac.getSession(),
							group,
							ac.getAttributeById(group, parms.readInt("attribute")));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "host, resource, user, member or group");
			}
		}
	},

	/*#
	 * Tries to fill host attributes.
	 *
	 * @param host int Host <code>id</code>
	 * @param attributes List<Attribute> List of attributes
	 * @return List<Attribute> attributes which MAY have filled value
	 */
	/*#
	 * Tries to fill group-resource attributes.
	 *
	 * @param resource int Resource <code>id</code>
	 * @param group int Group <code>id</code>
	 * @param attributes List<Attribute> List of attributes
	 * @return List<Attribute> attributes which MAY have filled value
	 */
	/*#
	 * Tries to fill user, member, member-resource and user-facility attributes.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @param user int User <code>id</code>
	 * @param member int Member <code>id</code>
	 * @param attributes List<Attribute> List of attributes
	 * @return List<Attribute> attributes which MAY have filled value
	 */
	/*#
	 * Tries to fill member-resource attributes and also user and user-facility attributes, if workWithUserAttributes == true.
	 *
	 * @param resource int Resource <code>id</code>
	 * @param member int Member <code>id</code>
	 * @param attributes List<Attribute> List of attributes
	 * @param workWithUserAttributes boolean Work with user attributes. False is default value.
	 * @return List<Attribute> attributes which MAY have filled value
	 */
	/*#
	 * Tries to fill member-resource attributes.
	 *
	 * @param resource int Resource <code>id</code>
	 * @param member int Member <code>id</code>
	 * @param attributes List<Attribute> List of attributes
	 * @return List<Attribute> attributes which MAY have filled value
	 */
	/*#
	 * Tries to fill resource attributes.
	 *
	 * @param resource int Resource <code>id</code>
	 * @param attributes List<Attribute> List of attributes
	 * @return List<Attribute> attributes which MAY have filled value
	 */
	/*#
	 * Tries to fill member-group attributes and also member and user attributes, if workWithUserAttributes == true.
	 * 
	 * @param member int Member <code>id</code>
	 * @param group int Group <code>id</code>
	 * @param attributes List<Attribute> List of attributes   
	 * @param workWithUserAttributes boolean If <code>true</code>, process also User and Member attributes. <code>False</code> is default.
	 * @return List<Attribute> attributes which MAY have filled value
	 */
	/*#
	 * Tries to fill member-group attributes.
	 * 
	 * @param member int Member <code>id</code>
	 * @param group int Group <code>id</code>
	 * @param attributes List<Attribute> List of attributes   
	 * @return List<Attribute> attributes which MAY have filled value
	 */
	/*#
	 * Tries to fill member attributes.
	 *
	 * @param member int Member <code>id</code>
	 * @param attributes List<Attribute> List of attributes
	 * @return List<Attribute> attributes which MAY have filled value
	 */
	/*#
	 * Tries to fill user-facility attributes.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param user int User <code>id</code>
	 * @param attributes List<Attribute> List of attributes
	 * @return List<Attribute> attributes which MAY have filled value
	 */
	/*#
	 * Tries to fill user attributes.
	 *
	 * @param user int User <code>id</code>
	 * @param attributes List<Attribute> List of attributes
	 * @return List<Attribute> attributes which MAY have filled value
	 */
	/*#
	 * Tries to fill group attributes.
	 *
	 * @param group int Group <code>id</code>
	 * @param attributes List<Attribute> List of attributes
	 * @return List<Attribute> attributes which MAY have filled value
	 */
	fillAttributes {

		@Override
		public List<Attribute> call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			List<Attribute> attributes = new ArrayList<Attribute>();
			if (parms.contains("attributes")) {
				attributes = parms.readList("attributes", Attribute.class);
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "attributes");
			}
			
			if (parms.contains("host")) {
				return ac.getAttributesManager().fillAttributes(ac.getSession(),
						ac.getHostById(parms.readInt("host")),
						attributes);
			} else if (parms.contains("resource")) {
				if (parms.contains("group")) {
					return ac.getAttributesManager().fillAttributes(ac.getSession(),
							ac.getResourceById(parms.readInt("resource")),
							ac.getGroupById(parms.readInt("group")),
							attributes);
				} else if (parms.contains("user")) {
					if (parms.contains("facility") && parms.contains("member")) {
						return ac.getAttributesManager().fillAttributes(ac.getSession(),
								ac.getFacilityById(parms.readInt("facility")),
								ac.getResourceById(parms.readInt("resource")),
								ac.getUserById(parms.readInt("user")),
								ac.getMemberById(parms.readInt("member")),
								attributes);
					} else {
						throw new RpcException(RpcException.Type.MISSING_VALUE, "facility, member");
					}
				} else if (parms.contains("member")) {
					if (parms.contains("workWithUserAttributes")) {
						return ac.getAttributesManager().fillAttributes(ac.getSession(),
								ac.getResourceById(parms.readInt("resource")),
								ac.getMemberById(parms.readInt("member")),
								attributes,
								parms.readBoolean("workWithUserAttributes"));
					} else {
						return ac.getAttributesManager().fillAttributes(ac.getSession(),
								ac.getResourceById(parms.readInt("resource")),
								ac.getMemberById(parms.readInt("member")),
								attributes);
					}
				} else {
					return ac.getAttributesManager().fillAttributes(ac.getSession(),
							ac.getResourceById(parms.readInt("resource")),
							attributes);
				}
			} else if (parms.contains("member")) {
				if (parms.contains("group")) {
					if (parms.contains("workWithUserAttributes")) {
						return ac.getAttributesManager().fillAttributes(ac.getSession(),
								ac.getMemberById(parms.readInt("member")),
								ac.getGroupById(parms.readInt("group")),
								attributes,
								parms.readBoolean("workWithUserAttributes"));
					} else {
						return ac.getAttributesManager().fillAttributes(ac.getSession(),
								ac.getMemberById(parms.readInt("member")),
								ac.getGroupById(parms.readInt("group")),
								attributes);
					}
				} else {
					return ac.getAttributesManager().fillAttributes(ac.getSession(),
							ac.getMemberById(parms.readInt("member")),
							attributes);
				}
			} else if (parms.contains("user")) {
				if (parms.contains("facility")) {
					return ac.getAttributesManager().fillAttributes(ac.getSession(),
							ac.getFacilityById(parms.readInt("facility")),
							ac.getUserById(parms.readInt("user")),
							attributes);
				} else {
					return ac.getAttributesManager().fillAttributes(ac.getSession(),
							ac.getUserById(parms.readInt("user")),
							attributes);
				}
			} else if (parms.contains("group")) {
				return ac.getAttributesManager().fillAttributes(ac.getSession(),
						ac.getGroupById(parms.readInt("group")),
						attributes);
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "host, resource, member, user or group");
			}
		}
	},

	/*#
	 * Checks if this user-facility attribute is valid.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param user int User <code>id</code>
	 * @param attribute int Attribute <code>id</code>
	 */
	/*#
	 * Checks if this facility attribute is valid.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param attribute int Attribute <code>id</code>
	 */
	/*#
	 * Checks if this vo attribute is valid.
	 *
	 * @param vo int Vo <code>id</code>
	 * @param attribute int Attribute <code>id</code>
	 */
	/*#
	 * Checks if this member-resource attribute is valid.
	 *
	 * @param resource int Resource <code>id</code>
	 * @param member int Member <code>id</code>
	 * @param attribute int Attribute <code>id</code>
	 */
	/*#
	 * Checks if this group-resource attribute is valid.
	 *
	 * @param resource int Resource <code>id</code>
	 * @param group int Group <code>id</code>
	 * @param attribute int Attribute <code>id</code>
	 */
	/*#
	 * Checks if this resource attribute is valid.
	 *
	 * @param resource int Resource <code>id</code>
	 * @param attribute int Attribute <code>id</code>
	 */
	/*#
	 * Checks if this member-group attribute is valid.
	 *
	 * @param member int Member <code>id</code>
	 * @param group int Group <code>id</code>
	 * @param attribute int Attribute <code>id</code>   
	 */
	/*#
	 * Checks if this member attribute is valid.
	 *
	 * @param member int Member <code>id</code>
	 * @param attribute int Attribute <code>id</code>   
	 */
	/*#
	 * Checks if this group attribute is valid.
	 *
	 * @param group int Group <code>id</code>
	 * @param attribute int Attribute <code>id</code>   
	 */
	/*#
	 * Checks if this host attribute is valid.
	 *
	 * @param host int Host <code>id</code>
	 * @param attribute int Attribute <code>id</code>
	 */
	/*#
	 * Checks if this user attribute is valid.
	 *
	 * @param user int User <code>id</code>
	 * @param attribute int Attribute <code>id</code>
	 */
	/*#
	 * Checks if this userExtSource attribute is valid.
	 *
	 * @param userExtSource int UserExtSource <code>id</code>
	 * @param attribute int Attribute <code>id</code>
	 */
	checkAttributeValue {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("facility")) {
				if (parms.contains("user")) {
					ac.getAttributesManager().checkAttributeValue(ac.getSession(),
							ac.getFacilityById(parms.readInt("facility")),
							ac.getUserById(parms.readInt("user")),
							parms.read("attribute", Attribute.class));
				} else {
					ac.getAttributesManager().checkAttributeValue(ac.getSession(),
							ac.getFacilityById(parms.readInt("facility")),
							parms.read("attribute", Attribute.class));
				}
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
				} else if (parms.contains("group")) {
					ac.getAttributesManager().checkAttributeValue(ac.getSession(),
							ac.getResourceById(parms.readInt("resource")),
							ac.getGroupById(parms.readInt("group")),
							parms.read("attribute", Attribute.class));
				} else {
					ac.getAttributesManager().checkAttributeValue(ac.getSession(),
							ac.getResourceById(parms.readInt("resource")),
							parms.read("attribute", Attribute.class));
				}
			} else if (parms.contains("member")) {
				if (parms.contains("group")) {
					ac.getAttributesManager().checkAttributeValue(ac.getSession(),
							ac.getMemberById(parms.readInt("member")),
							ac.getGroupById(parms.readInt("group")),
							parms.read("attribute", Attribute.class));
				} else {
					ac.getAttributesManager().checkAttributeValue(ac.getSession(),
							ac.getMemberById(parms.readInt("member")),
							parms.read("attribute", Attribute.class));
				}
			} else if (parms.contains("group")) {
				ac.getAttributesManager().checkAttributeValue(ac.getSession(),
						ac.getGroupById(parms.readInt("group")),
						parms.read("attribute", Attribute.class));
			} else if (parms.contains("host")) {
				ac.getAttributesManager().checkAttributeValue(ac.getSession(),
						ac.getHostById(parms.readInt("host")),
						parms.read("attribute", Attribute.class));
			} else if (parms.contains("user")) {
				ac.getAttributesManager().checkAttributeValue(ac.getSession(),
						ac.getUserById(parms.readInt("user")),
						parms.read("attribute", Attribute.class));
			} else if (parms.contains("userExtSource")) {
				ac.getAttributesManager().checkAttributeValue(ac.getSession(),
						ac.getUserExtSourceById(parms.readInt("userExtSource")),
						parms.read("attribute", Attribute.class));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "facility, vo, resource, member, group, host, user or userExtSource");
			}

			return null;
		}
	},
	
	/*#
	 * Checks if these facility, resource, user and member attributes are valid.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @param user int User <code>id</code>
	 * @param member int Member <code>id</code>
	 * @param attributes List<Attribute> Attributes List
	 */
	/*#
	 * Checks if these user-facility attributes are valid.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param user int User <code>id</code>
	 * @param attributes List<Attribute> Attributes List
	 */
	/*#
	 * Checks if these facility attributes are valid.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param attributes List<Attribute> Attributes List
	 */
	/*#
	 * Checks if these vo attributes are valid.
	 *
	 * @param vo int Vo <code>id</code>
	 * @param attributes List<Attribute> Attributes List
	 */
	/*#
	 * Checks if these member-resource attributes are valid.
	 *
	 * @param resource int Resource <code>id</code>
	 * @param member int Member <code>id</code>
	 * @param attributes List<Attribute> Attributes List
	 * @param workWithUserAttributes boolean If <code>true</code>, process also User and Member attributes. <code>False</code> is default.
	 */
	/*#
	 * Checks if these member-resource attributes are valid.
	 *
	 * @param resource int Resource <code>id</code>
	 * @param member int Member <code>id</code>
	 * @param attributes List<Attribute> Attributes List
	 */
	/*#
	 * Checks if these group-resource attributes are valid.
	 *
	 * @param resource int Resource <code>id</code>
	 * @param group int Group <code>id</code>
	 * @param attributes List<Attribute> Attributes List
	 */
	/*#
	 * Checks if these resource attributes are valid.
	 *
	 * @param resource int Resource <code>id</code>
	 * @param attributes List<Attribute> Attributes List
	 */
	/*#
	 * Checks if these member-group attributes are valid.
	 * 
	 * @param member int Member <code>id</code>
	 * @param group int Group <code>id</code>
	 * @param attributes List<Attribute> Attributes List
	 * @param workWithUserAttributes boolean If <code>true</code>, process also User and Member attributes. <code>False</code> is default.   
	 */
	/*#
	 * Checks if these member-group attributes are valid.
	 * 
	 * @param member int Member <code>id</code>
	 * @param group int Group <code>id</code>
	 * @param attributes List<Attribute> Attributes List
	 */
	/*#
	 * Checks if these member attributes are valid.
	 * 
	 * @param member int Member <code>id</code>
	 * @param attributes List<Attribute> Attributes List 
	 */
	/*#
	 * Checks if these host attributes are valid.
	 *
	 * @param host int Host <code>id</code>
	 * @param attributes List<Attribute> Attributes List
	 */
	/*#
	 * Checks if these user attributes are valid.
	 *
	 * @param user int User <code>id</code>
	 * @param attributes List<Attribute> Attributes List
	 */
	/*#
	 * Checks if these userExtSource attributes are valid.
	 *
	 * @param userExtSource int UserExtSource <code>id</code>
	 * @param attributes List<Attribute> Attributes List
	 */
	checkAttributesValue {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			if (parms.contains("facility")) {
				if (parms.contains("user")) {
					if (parms.contains("resource") && parms.contains("member")) {
						ac.getAttributesManager().checkAttributesValue(ac.getSession(),
								ac.getFacilityById(parms.readInt("facility")),
								ac.getResourceById(parms.readInt("resource")),
								ac.getUserById(parms.readInt("user")),
								ac.getMemberById(parms.readInt("member")),
								parms.readList("attributes", Attribute.class));
					} else {
						ac.getAttributesManager().checkAttributesValue(ac.getSession(),
								ac.getFacilityById(parms.readInt("facility")),
								ac.getUserById(parms.readInt("user")),
								parms.readList("attributes", Attribute.class));
					}
				} else {
					ac.getAttributesManager().checkAttributesValue(ac.getSession(),
							ac.getFacilityById(parms.readInt("facility")),
							parms.readList("attributes", Attribute.class));
				}
			} else if (parms.contains("vo")) {
				ac.getAttributesManager().checkAttributesValue(ac.getSession(),
						ac.getVoById(parms.readInt("vo")),
						parms.readList("attributes", Attribute.class));
			} else if (parms.contains("resource")) {
				if (parms.contains("member")) {
					if (parms.contains("workWithUserAttributes")) {
						ac.getAttributesManager().checkAttributesValue(ac.getSession(),
								ac.getResourceById(parms.readInt("resource")),
								ac.getMemberById(parms.readInt("member")),
								parms.readList("attributes", Attribute.class),
								parms.readBoolean("workWithUserAttributes"));
					} else {
						ac.getAttributesManager().checkAttributesValue(ac.getSession(),
								ac.getResourceById(parms.readInt("resource")),
								ac.getMemberById(parms.readInt("member")),
								parms.readList("attributes", Attribute.class));
					}
				} else if (parms.contains("group")) {
					ac.getAttributesManager().checkAttributesValue(ac.getSession(),
							ac.getResourceById(parms.readInt("resource")),
							ac.getGroupById(parms.readInt("group")),
							parms.readList("attributes", Attribute.class));
				} else {
					ac.getAttributesManager().checkAttributesValue(ac.getSession(),
							ac.getResourceById(parms.readInt("resource")),
							parms.readList("attributes", Attribute.class));
				}
			} else if (parms.contains("member")) {
				if (parms.contains("group")) {
					if (parms.contains("workWithUserAttributes")) {
						ac.getAttributesManager().checkAttributesValue(ac.getSession(),
								ac.getMemberById(parms.readInt("member")),
								ac.getGroupById(parms.readInt("group")),
								parms.readList("attributes", Attribute.class),
								parms.readBoolean("workWithUserAttributes"));
					} else {
						ac.getAttributesManager().checkAttributesValue(ac.getSession(),
								ac.getMemberById(parms.readInt("member")),
								ac.getGroupById(parms.readInt("group")),
								parms.readList("attributes", Attribute.class));
					}
				} else {
					ac.getAttributesManager().checkAttributesValue(ac.getSession(),
							ac.getMemberById(parms.readInt("member")),
							parms.readList("attributes", Attribute.class));
				}
			} else if (parms.contains("host")) {
				ac.getAttributesManager().checkAttributesValue(ac.getSession(),
						ac.getHostById(parms.readInt("host")),
						parms.readList("attributes", Attribute.class));
			} else if (parms.contains("user")) {
				ac.getAttributesManager().checkAttributesValue(ac.getSession(),
						ac.getUserById(parms.readInt("user")),
						parms.readList("attributes", Attribute.class));
			} else if (parms.contains("userExtSource")) {
				ac.getAttributesManager().checkAttributesValue(ac.getSession(),
						ac.getUserExtSourceById(parms.readInt("userExtSource")),
						parms.readList("attributes", Attribute.class));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "facility, vo, resource, member, host, user or userExtSource");
			}
		return null;
		}
	},

	/*#
	 * Remove attributes of namespace:
	 *
	 * user, user-facility, member, member-resource
	 *
	 * @param facility int Facility <code>id</code>
	 * @param user int User <code>id</code>
	 * @param member int Member <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @param attributes List<Integer> List of attributes IDs to remove
	 */
	/*#
	 * Remove attributes of namespace:
	 *
	 * user-facility
	 *
	 * @param facility int Facility <code>id</code>
	 * @param user int User <code>id</code>
	 * @param attributes List<Integer> List of attributes IDs to remove
	 */
	/*#
	 * Remove attributes of namespace:
	 *
	 * facility
	 *
	 * @param facility int Facility <code>id</code>
	 * @param attributes List<Integer> List of attributes IDs to remove
	 */
	/*#
	 * Remove attributes of namespace:
	 *
	 * vo
	 *
	 * @param vo int VO <code>id</code>
	 * @param attributes List<Integer> List of attributes IDs to remove
	 */
	/*#
	 * Remove attributes of namespace:
	 *
	 * resource
	 *
	 * @param resource int Resource <code>id</code>
	 * @param attributes List<Integer> List of attributes IDs to remove
	 */
	/*#
	 * Remove attributes of namespace:
	 *
	 * group-resource, group (optional)
	 *
	 * @param resource int Resource <code>id</code>
	 * @param group int Group <code>id</code>
	 * @param workWithGroupAttributes boolean Work with group attributes. False is default value.
	 * @param attributes List<Integer> List of attributes IDs to remove
	 */
	/*#
	 * Remove attributes of namespace:
	 *
	 * group-resource
	 *
	 * @param resource int Resource <code>id</code>
	 * @param group int Group <code>id</code>
	 * @param attributes List<Integer> List of attributes IDs to remove
	 */
	/*#
	 * Remove attributes of namespace:
	 *
	 * member-resource
	 *
	 * @param resource int Resource <code>id</code>
	 * @param member int Member <code>id</code>
	 * @param attributes List<Integer> List of attributes IDs to remove
	 */
	/*#
	 * Remove attributes of namespace:
	 *
	 * member, user (optional)
	 *
	 * @param member int Member <code>id</code>
	 * @param workWithUserAttributes boolean Set to true if you want to remove also user attributes. False is default value.
	 * @param attributes List<Integer> List of attributes IDs to remove
	 */
	/*#
	 * Remove attributes of namespace:
	 * 
	 * member-group
	 *
	 * @param member int Member <code>id</code>
	 * @param group int Group <code>id</code>
	 * @param attributes List<Integer> List of attributes IDs to remove
	 */
	/*#
	 * Remove attributes of namespace:
	 *
	 * member
	 *
	 * @param member int Member <code>id</code>
	 * @param attributes List<Integer> List of attributes IDs to remove
	 */
	/*#
	 * Remove attributes of namespace:
	 *
	 * group
	 *
	 * @param group int Group <code>id</code>
	 * @param attributes List<Integer> List of attributes IDs to remove
	 */
	/*#
	 * Remove attributes of namespace:
	 *
	 * host
	 *
	 * @param host int Host <code>id</code>
	 * @param attributes List<Integer> List of attributes IDs to remove
	 */
	/*#
	 * Remove attributes of namespace:
	 *
	 * user
	 *
	 * @param user int User <code>id</code>
	 * @param attributes List<Integer> List of attributes IDs to remove
	 */
	/*#
	 * Remove attributes of namespace:
	 *
	 * userExtSource
	 *
	 * @param userExtSource int UserExtSource <code>id</code>
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

			if (parms.contains("facility")) {
				if (parms.contains("resource") && parms.contains("user") && parms.contains("member")) {
					ac.getAttributesManager().removeAttributes(ac.getSession(),
							ac.getFacilityById(parms.readInt("facility")),
							ac.getResourceById(parms.readInt("resource")),
							ac.getUserById(parms.readInt("user")),
							ac.getMemberById(parms.readInt("member")),
							attributes);
				} else if (parms.contains("user")) {
					ac.getAttributesManager().removeAttributes(ac.getSession(),
							ac.getFacilityById(parms.readInt("facility")),
							ac.getUserById(parms.readInt("user")),
							attributes);
				} else {
					ac.getAttributesManager().removeAttributes(ac.getSession(),
							ac.getFacilityById(parms.readInt("facility")),
							attributes);
				}
			} else if (parms.contains("vo")) {
				ac.getAttributesManager().removeAttributes(ac.getSession(),
						ac.getVoById(parms.readInt("vo")),
						attributes);
			} else if (parms.contains("resource")) {
				if (parms.contains("member")) {
					ac.getAttributesManager().removeAttributes(ac.getSession(),
							ac.getResourceById(parms.readInt("resource")),
							ac.getMemberById(parms.readInt("member")),
							attributes);
				} else if (parms.contains("group")) {
					if (parms.contains("workWithGroupAttributes")) {
						ac.getAttributesManager().removeAttributes(ac.getSession(),
								ac.getResourceById(parms.readInt("resource")),
								ac.getGroupById(parms.readInt("group")),
								attributes, 
								parms.readBoolean("workWithGroupAttributes"));
					} else {
						ac.getAttributesManager().removeAttributes(ac.getSession(),
								ac.getResourceById(parms.readInt("resource")),
								ac.getGroupById(parms.readInt("group")),
								attributes);
					}
				} else {
					ac.getAttributesManager().removeAttributes(ac.getSession(),
							ac.getResourceById(parms.readInt("resource")),
							attributes);
				}
			} else if (parms.contains("member")) {
				if (parms.contains("workWithUserAttributes")) {
					ac.getAttributesManager().removeAttributes(ac.getSession(),
							ac.getMemberById(parms.readInt("member")),
							parms.readBoolean("workWithUserAttributes"), attributes);
				} else if (parms.contains("group")) {
					ac.getAttributesManager().removeAttributes(ac.getSession(),
							ac.getMemberById(parms.readInt("member")),
							ac.getGroupById(parms.readInt("group")),
							attributes);
				} else {
					ac.getAttributesManager().removeAttributes(ac.getSession(),
							ac.getMemberById(parms.readInt("member")),
							attributes);
				}
			} else if (parms.contains("host")) {
				ac.getAttributesManager().removeAttributes(ac.getSession(),
						ac.getHostById(parms.readInt("host")),
						attributes);
			} else if (parms.contains("user")) {
				ac.getAttributesManager().removeAttributes(ac.getSession(),
						ac.getUserById(parms.readInt("user")),
						attributes);

			} else if (parms.contains("group")) {
				ac.getAttributesManager().removeAttributes(ac.getSession(),
						ac.getGroupById(parms.readInt("group")),
						attributes);
			} else if (parms.contains("userExtSource")) {
				ac.getAttributesManager().removeAttributes(ac.getSession(),
						ac.getUserExtSourceById(parms.readInt("userExtSource")),
						attributes);
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "facility, vo, group, host, resource, member, user or userExtSource");
			}

			return null;
		}
	},

	/*#
	 * Remove attribute of namespace:
	 *
	 * user-facility
	 *
	 * @param facility int Facility <code>id</code>
	 * @param user int User <code>id</code>
	 * @param attribute int <code>id</code> of attribute to remove
	 */
	/*#
	 * Remove attribute of namespace:
	 *
	 * facility
	 *
	 * @param facility int Facility <code>id</code>
	 * @param attribute int <code>id</code> of attribute to remove
	 */
	/*#
	 * Remove attribute of namespace:
	 *
	 * vo
	 *
	 * @param vo int VO <code>id</code>
	 * @param attribute int <code>id</code> of attribute to remove
	 */
	/*#
	 * Remove attribute of namespace:
	 *
	 * resource
	 *
	 * @param resource int Resource <code>id</code>
	 * @param attribute int <code>id</code> of attribute to remove
	 */
	/*#
	 * Remove attribute of namespace:
	 *
	 * group-resource
	 *
	 * @param resource int Resource <code>id</code>
	 * @param group int Group <code>id</code>
	 * @param attribute int <code>id</code> of attribute to remove
	 */
	/*#
	 * Remove attribute of namespace:
	 *
	 * member-resource
	 *
	 * @param resource int Resource <code>id</code>
	 * @param member int Member <code>id</code>
	 * @param attribute int <code>id</code> of attribute to remove
	 */
	/*#
	 * Remove attribute of namespace:
	 * 
	 * member-group
	 *
	 * @param member int Member <code>id</code>
	 * @param group int Group <code>id</code>
	 * @param attribute int <code>id</code> of attribute to remove
	 */
	/*#
	 * Remove attribute of namespace:
	 *
	 * member
	 *
	 * @param member int Member <code>id</code>
	 * @param attribute int <code>id</code> of attribute to remove
	 */
	/*#
	 * Remove attribute of namespace:
	 *
	 * group
	 *
	 * @param group int Group <code>id</code>
	 * @param attribute int <code>id</code> of attribute to remove
	 */
	/*#
	 * Remove attribute of namespace:
	 *
	 * host
	 *
	 * @param host int Host <code>id</code>
	 * @param attribute int <code>id</code> of attribute to remove
	 */
	/*#
	 * Remove attribute of namespace:
	 *
	 * user
	 *
	 * @param user int User <code>id</code>
	 * @param attribute int <code>id</code> of attribute to remove
	 */
	/*#
	 * Remove attribute of namespace:
	 *
	 * userExtSource
	 *
	 * @param userExtSource int UserExtSource <code>id</code>
	 * @param attribute int <code>id</code> of attribute to remove
	 */
	removeAttribute {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("facility")) {
				if (parms.contains("user")) {
					ac.getAttributesManager().removeAttribute(ac.getSession(),
							ac.getFacilityById(parms.readInt("facility")),
							ac.getUserById(parms.readInt("user")),
							ac.getAttributeDefinitionById(parms.readInt("attribute")));
				} else {
					ac.getAttributesManager().removeAttribute(ac.getSession(),
							ac.getFacilityById(parms.readInt("facility")),
							ac.getAttributeDefinitionById(parms.readInt("attribute")));
				}
			} else if (parms.contains("vo")) {
				ac.getAttributesManager().removeAttribute(ac.getSession(),
						ac.getVoById(parms.readInt("vo")),
						ac.getAttributeDefinitionById(parms.readInt("attribute")));
			} else if (parms.contains("resource")) {
				if (parms.contains("member")) {
					ac.getAttributesManager().removeAttribute(ac.getSession(),
							ac.getResourceById(parms.readInt("resource")),
							ac.getMemberById(parms.readInt("member")),
							ac.getAttributeDefinitionById(parms.readInt("attribute")));
				} else if (parms.contains("group")) {
					ac.getAttributesManager().removeAttribute(ac.getSession(),
							ac.getResourceById(parms.readInt("resource")),
							ac.getGroupById(parms.readInt("group")),
							ac.getAttributeDefinitionById(parms.readInt("attribute")));
				} else {
					ac.getAttributesManager().removeAttribute(ac.getSession(),
							ac.getResourceById(parms.readInt("resource")),
							ac.getAttributeDefinitionById(parms.readInt("attribute")));
				}
			} else if (parms.contains("member")) {
				if (parms.contains("group")) {
					ac.getAttributesManager().removeAttribute(ac.getSession(),
							ac.getMemberById(parms.readInt("member")),
							ac.getGroupById(parms.readInt("group")),
							ac.getAttributeDefinitionById(parms.readInt("attribute")));
				} else {
					ac.getAttributesManager().removeAttribute(ac.getSession(),
							ac.getMemberById(parms.readInt("member")),
							ac.getAttributeDefinitionById(parms.readInt("attribute")));
				}
			} else if (parms.contains("user")) {
				ac.getAttributesManager().removeAttribute(ac.getSession(),
						ac.getUserById(parms.readInt("user")),
						ac.getAttributeDefinitionById(parms.readInt("attribute")));
			} else if (parms.contains("group")) {
				ac.getAttributesManager().removeAttribute(ac.getSession(),
						ac.getGroupById(parms.readInt("group")),
						ac.getAttributeDefinitionById(parms.readInt("attribute")));
			} else if (parms.contains("host")) {
				ac.getAttributesManager().removeAttribute(ac.getSession(),
						ac.getHostById(parms.readInt("host")),
						ac.getAttributeDefinitionById(parms.readInt("attribute")));
			} else if (parms.contains("userExtSource")) {
				ac.getAttributesManager().removeAttribute(ac.getSession(),
						ac.getUserExtSourceById(parms.readInt("userExtSource")),
						ac.getAttributeDefinitionById(parms.readInt("attribute")));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "facility, vo, group, resource, member, host, user or userExtSource");
			}
		return null;
		}
	},

	/*#
	 * Unset all attributes for the user on the facility.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param user int User <code>id</code>
	 */
	/*#
	 * Unset all attributes for the facility and also user-facility attributes if workWithUserAttributes == true.
	 *
	 * @param facility int Facility <code>id</code>
	 * @param workWithUserAttributes boolean Remove also user facility attributes. False is default value.
	 */
	/*#
	 * Unset all attributes for the facility.
	 *
	 * @param facility int Facility <code>id</code>
	 */
	/*#
	 * Unset all attributes for the vo.
	 *
	 * @param vo int Vo <code>id</code>
	 */
	/*#
	 * Unset all attributes for the member on the resource.
	 *
	 * @param member int Member <code>id</code>
	 * @param resource int Resource <code>id</code>
	 */
	/*#
	 * Unset all group-resource attributes and also group attributes if WorkWithGroupAttributes == true.
	 *
	 * @param group int Group <code>id</code>
	 * @param resource int Resource <code>id</code>
	 * @param workWithGroupAttributes boolean Work with group attributes. False is default value.
	 */
	/*#
	 * Unset all group-resource attributes.
	 *
	 * @param group int Group <code>id</code>
	 * @param resource int Resource <code>id</code>
	 */
	/*#
	 * Unset all resource attributes.
	 *
	 * @param resource int Resource <code>id</code>
	 */
	/*#
	 * Unset all member-group attributes.
	 *
	 * @param member int Member <code>id</code>
	 * @param group int Group <code>id</code>
	 */
	/*#
	 * Unset all member attributes.
	 *
	 * @param member int Member <code>id</code>
	 */
	/*#
	 * Unset all user attributes.
	 *
	 * @param user int User <code>id</code>
	 */
	/*#
	 * Unset all group attributes.
	 *
	 * @param group int Group <code>id</code>
	 */
	/*#
	 * Unset all host attributes.
	 *
	 * @param host int Host <code>id</code>
	 */
	/*#
	 * Unset all attributes for the userExtSource.
	 *
	 * @param userExtSource int UserExtSource <code>id</code>
	 */
	removeAllAttributes {

		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			if (parms.contains("facility")) {
				if (parms.contains("user")) {
					ac.getAttributesManager().removeAllAttributes(ac.getSession(),
							ac.getFacilityById(parms.readInt("facility")),
							ac.getUserById(parms.readInt("user")));
				} else if (parms.contains("workWithUserAttributes")) {
					ac.getAttributesManager().removeAllAttributes(ac.getSession(),
							ac.getFacilityById(parms.readInt("facility")),
							parms.readBoolean("workWithUserAttributes"));
				} else {
					ac.getAttributesManager().removeAllAttributes(ac.getSession(),
							ac.getFacilityById(parms.readInt("facility")));
				}
			} else if (parms.contains("vo")) {
				ac.getAttributesManager().removeAllAttributes(ac.getSession(),
						ac.getVoById(parms.readInt("vo")));
			} else if (parms.contains("resource")) {
				if (parms.contains("member")) {
					ac.getAttributesManager().removeAllAttributes(ac.getSession(),
							ac.getResourceById(parms.readInt("resource")),
							ac.getMemberById(parms.readInt("member")));
				} else if (parms.contains("group")) {
					if (parms.contains("workWithGroupAttributes")) {
						ac.getAttributesManager().removeAllAttributes(ac.getSession(),
								ac.getResourceById(parms.readInt("resource")),
								ac.getGroupById(parms.readInt("group")),
								parms.readBoolean("workWithGroupAttributes"));
					} else {
						ac.getAttributesManager().removeAllAttributes(ac.getSession(),
								ac.getResourceById(parms.readInt("resource")),
								ac.getGroupById(parms.readInt("group")));
					}
				} else {
					ac.getAttributesManager().removeAllAttributes(ac.getSession(),
							ac.getResourceById(parms.readInt("resource")));
				}
			} else if (parms.contains("member")) {
				if (parms.contains("group")) {
					ac.getAttributesManager().removeAllAttributes(ac.getSession(),
							ac.getMemberById(parms.readInt("member")),
							ac.getGroupById(parms.readInt("group")));
				} else {
					ac.getAttributesManager().removeAllAttributes(ac.getSession(),
							ac.getMemberById(parms.readInt("member")));
				}
			} else if (parms.contains("user")) {
				ac.getAttributesManager().removeAllAttributes(ac.getSession(),
						ac.getUserById(parms.readInt("user")));
			} else if (parms.contains("group")) {
				ac.getAttributesManager().removeAllAttributes(ac.getSession(),
						ac.getGroupById(parms.readInt("group")));
			} else if (parms.contains("host")) {
				ac.getAttributesManager().removeAllAttributes(ac.getSession(),
						ac.getHostById(parms.readInt("host")));
			} else if (parms.contains("userExtSource")) {
				ac.getAttributesManager().removeAllAttributes(ac.getSession(),
						ac.getUserExtSourceById(parms.readInt("userExtSource")));
			} else {
				throw new RpcException(RpcException.Type.MISSING_VALUE, "facility, resource, vo, group, member, host, user or userExtSource");
			}

			return null;
		}
	},

	/*#
	 * Get all users logins as Attributes. Meaning it returns all non-empty User attributes with
	 * URN starting with: "urn:perun:user:attribute-def:def:login-namespace:".
	 *
	 * @param user int User <code>id</code>
	 * @return List<Attribute> List of users logins as Attributes
	 * @throw UserNotExistsException When User with <code>id</code> doesn't exist.
	 */
	getLogins {
		@Override
		public List<Attribute> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getAttributesManager().getLogins(ac.getSession(), 
					ac.getUserById(parms.readInt("user")));
		}
	},

	/*#
	 * Updates AttributeDefinition in Perun based on provided object.
	 * Update is done on AttributeDefinition selected by its <code>id</code>.
	 *
	 * @param attributeDefinition AttributeDefinition AttributeDefinition with updated properties to store in DB
	 * @return AttributeDefinition updated AttributeDefinition
	 * @throw AttributeNotExistsException When AttributeDefinition with <code>id</code> in object doesn't exist.
	 */
	updateAttributeDefinition {

		@Override
		public AttributeDefinition call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			return ac.getAttributesManager().updateAttributeDefinition(ac.getSession(), 
					parms.read("attributeDefinition", AttributeDefinition.class));
		}
	},

	/*#
	 * Takes all Member related attributes (Member, User, Member-Resource, User-Facility) and tries to fill them and set them.
	 *
	 * @param member int Member <code>id</code>
	 * @throw WrongAttributeAssignmentException When we try to fill/set Attribute from unrelated namespace.
	 * @throw WrongAttributeValueException When value of some Attribute is not correct.
	 * @throw WrongReferenceAttributeValueException When value of some Attribute is not correct regarding to other Attribute value.
	 * @throw MemberNotExistsException When Member with <code>id</code> doesn't exist.
	 */
	doTheMagic {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.getAttributesManager().doTheMagic(ac.getSession(), 
					ac.getMemberById(parms.readInt("member")));
			return null;
		}
	},

	/*#
	 * Gets AttributeRights for specified Attribute. Rights specify which Role can do particular actions
	 * (read / write) with Attribute. Method always return rights for following roles:
	 * voadmin, groupadmin, facilityadmin, self.
	 *
	 * @param attributeId int Attribute <code>id</code>
	 * @return List<AttributeRights> all rights of the attribute
	 * @throw AttributeNotExistsException When Attribute with <code>id</code> doesn't exist.
	 */
	getAttributeRights {
		@Override
		public List<AttributeRights> call(ApiCaller ac, Deserializer parms) throws PerunException {
			return ac.getAttributesManager().getAttributeRights(ac.getSession(), 
					parms.readInt("attributeId"));
		}
	},

	/*#
	 * Sets all AttributeRights in the list given as a parameter. Allowed Roles to set
	 * rights for are: voadmin, groupadmin, facilityadmin, self.
	 *
	 * @param rights List<AttributeRights> List of AttributeRights to set.
	 * @throw AttributeNotExistsException When Attribute with <code>id</code> doesn't exist.
	 */
	setAttributeRights {
		@Override
		public Void call(ApiCaller ac, Deserializer parms) throws PerunException {
			ac.stateChangingCheck();

			ac.getAttributesManager().setAttributeRights(ac.getSession(), 
					parms.readList("rights", AttributeRights.class));
			return null;
		}
	};

}
