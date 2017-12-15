package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Jakub Peschel <410368@mail.muni.cz>
 */
public class urn_perun_user_facility_attribute_def_def_basicDefaultGIDTest {

		private static urn_perun_user_facility_attribute_def_def_basicDefaultGID classInstance;
		private static PerunSessionImpl session;
		private static AttributeDefinition attrDef;
		private static Facility facility;
		private static User user;
		private static Attribute basic;
		private static Attribute namespaceAttribute;
		private static List<Resource> allowedResources;
		private static List<Resource> allowedResourcesWithSameGid;
		private static Resource resource1;
		private static Resource resource2;
		private static Resource resource3;

		public urn_perun_user_facility_attribute_def_def_basicDefaultGIDTest() {
		}

		@Before
		public void setUp() throws Exception{
				classInstance = new urn_perun_user_facility_attribute_def_def_basicDefaultGID();
				session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
				attrDef = session.getPerunBl().getAttributesManagerBl().getAttributeDefinition(session, AttributesManager.NS_USER_FACILITY_ATTR_DEF + "basicDefaultGID");
				user = new User();
				facility = new Facility(1, "testFa");
				basic = new Attribute();
				namespaceAttribute = new Attribute();
				namespaceAttribute.setValue("test");
				allowedResources = new ArrayList<>();
				allowedResourcesWithSameGid = new ArrayList<>();
				resource1 = new Resource(1, "test1", "desc", 1);
				resource2 = new Resource(2, "test2", "desc", 1);
				resource3 = new Resource(3, "test3", "desc", 1);


		}

		@Test ( expected = WrongAttributeValueException.class)
		public void checkValueAttributeIsNotSetTest() throws Exception{
				System.out.println("urn_perun_user_facility_attribute_def_def_basicDefaultGID.checkValueAttributeIsNotSetTest()");
				//setup
				allowedResources.clear();
				allowedResourcesWithSameGid.clear();
				allowedResources.add(resource1);
				//mock
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), eq(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace"))).thenReturn(namespaceAttribute);
				when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSessionImpl.class), any(Facility.class), any(User.class))).thenReturn(allowedResources);
				when(session.getPerunBl().getResourcesManagerBl().getResourcesByAttribute(any(PerunSessionImpl.class), any(Attribute.class))).thenReturn(allowedResourcesWithSameGid);

				classInstance.checkAttributeValue(session, facility, user, basic);
		}

		@Test
		public void checkValueAttributeIsSetTest() throws Exception{
				System.out.println("urn_perun_user_facility_attribute_def_def_basicDefaultGID.checkValueAttributeIsSetTest()");
				//setup
				basic.setValue(1);
				allowedResources.clear();
				allowedResourcesWithSameGid.clear();
				allowedResources.add(resource1);
				allowedResources.add(resource2);
				allowedResourcesWithSameGid.add(resource2);
				allowedResourcesWithSameGid.add(resource3);
				//mock
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), eq(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace"))).thenReturn(namespaceAttribute);
				when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSessionImpl.class), any(Facility.class), any(User.class))).thenReturn(allowedResources);
				when(session.getPerunBl().getResourcesManagerBl().getResourcesByAttribute(any(PerunSessionImpl.class), any(Attribute.class))).thenReturn(allowedResourcesWithSameGid);

				classInstance.checkAttributeValue(session, facility, user, basic);
		}

		@Test(expected = WrongAttributeValueException.class)
		public void checkValueAttributeIsSetWithoutAllowedResourcesTest() throws Exception{
				System.out.println("urn_perun_user_facility_attribute_def_def_basicDefaultGID.checkValueAttributeIsSetWithoutAllowedResourcesTest()");
				//setup
				basic.setValue(1);
				allowedResources.clear();
				allowedResourcesWithSameGid.clear();
				allowedResourcesWithSameGid.add(resource2);
				allowedResourcesWithSameGid.add(resource3);
				//mock
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), eq(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace"))).thenReturn(namespaceAttribute);
				when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSessionImpl.class), any(Facility.class), any(User.class))).thenReturn(allowedResources);
				when(session.getPerunBl().getResourcesManagerBl().getResourcesByAttribute(any(PerunSessionImpl.class), any(Attribute.class))).thenReturn(allowedResourcesWithSameGid);

				classInstance.checkAttributeValue(session, facility, user, basic);
		}

		@Test(expected = WrongAttributeValueException.class)
		public void checkValueAttributeIsSetWithBadValueTest() throws Exception{
				System.out.println("urn_perun_user_facility_attribute_def_def_basicDefaultGID.checkValueAttributeIsSetWithBadValueTest()");
				//setup
				basic.setValue(1);
				allowedResources.clear();
				allowedResourcesWithSameGid.clear();
				allowedResources.add(resource1);
				allowedResources.add(resource2);
				//mock
				when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), eq(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace"))).thenReturn(namespaceAttribute);
				when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSessionImpl.class), any(Facility.class), any(User.class))).thenReturn(allowedResources);
				when(session.getPerunBl().getResourcesManagerBl().getResourcesByAttribute(any(PerunSessionImpl.class), any(Attribute.class))).thenReturn(allowedResourcesWithSameGid);

				classInstance.checkAttributeValue(session, facility, user, basic);
		}

}
