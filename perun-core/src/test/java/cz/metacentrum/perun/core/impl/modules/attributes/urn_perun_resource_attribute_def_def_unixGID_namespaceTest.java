package cz.metacentrum.perun.core.impl.modules.attributes;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;

import org.junit.Ignore;
/**
 *
 * @author Michal Stava <stavamichal@gmail.com>
 * @date 22.04.2013
 */
public class urn_perun_resource_attribute_def_def_unixGID_namespaceTest {
	private static urn_perun_resource_attribute_def_def_unixGID_namespace classInstance;
	private static PerunSessionImpl session;
	private static Attribute attribute;
	private static final String A_F_unixGroup_namespace = AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGroup-namespace";
	private static final String A_E_namespace_minGID = AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-minGID";
	private static final String A_E_namespace_maxGID = AttributesManager.NS_ENTITYLESS_ATTR_DEF + ":namespace-maxGID";
	private static final String A_G_unixGID_namespace = AttributesManager.NS_GROUP_ATTR_DEF + ":unixGID-namespace";
	private static final String A_R_unixGroupName_namespace = AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGroupName-namespace";
	private static final String A_G_unixGroupName_namespace = AttributesManager.NS_GROUP_ATTR_DEF + ":unixGroupName-namespace";

	@Before
	public void setUp() {
		classInstance = new urn_perun_resource_attribute_def_def_unixGID_namespace();
		session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
		attribute = new Attribute();
	}

	@Ignore
	@Test
	public void testFillAttributeValue() throws Exception {
		System.out.println("testFillAttributeValue()");


	}
}
