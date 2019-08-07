package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

/**
 * @author Vladimir Mecko vladimir.mecko@gmail.com
 */
public class urn_perun_resource_attribute_def_def_apacheAuthzFileTest {
	private static urn_perun_resource_attribute_def_def_apacheAuthzFile authzFileAttr;
	private static PerunSessionImpl ps;

	@Before
	public void setUp() {
		authzFileAttr = new urn_perun_resource_attribute_def_def_apacheAuthzFile();
		ps = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void checkAttributeWithoutValue() throws Exception {
		System.out.println("checkAttributeWithoutValue()");
		authzFileAttr.checkAttributeSemantics(ps, new Resource(), new Attribute());
	}

	@Test(expected = WrongAttributeValueException.class)
	public void checkAttributeWithWrongSlashInFilePath() throws Exception {
		System.out.println("checkAttributeWithWrongValue()");
		final Attribute attr = new Attribute();
		attr.setValue("bad/file/path");
		authzFileAttr.checkAttributeSemantics(ps, new Resource(), attr);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void checkAttributeWithNoSlashInFilePath() throws Exception {
		System.out.println("checkAttributeWithNoSlashInFilePath()");
		final Attribute attr = new Attribute();
		attr.setValue("pathToFile");
		authzFileAttr.checkAttributeSemantics(ps, new Resource(), attr);
	}

	@Test(expected = WrongAttributeValueException.class)
	public void checkAttributeEndingWithSlash() throws Exception {
		System.out.println("checkAttributeEndingWithSlash()");
		final Attribute attr = new Attribute();
		attr.setValue("/ending/with/slash/");
		authzFileAttr.checkAttributeSemantics(ps, new Resource(), attr);
	}
}
