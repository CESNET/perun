package cz.metacentrum.perun.core.bl;

import cz.metacentrum.perun.core.blImpl.AttributesManagerBlImpl;
import cz.metacentrum.perun.core.impl.AttributesManagerImpl;
import org.junit.Before;

import static org.mockito.Mockito.mock;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class AttributesManagerBlImplUnitTests {

	private AttributesManagerBl attrManagerBl;
	private AttributesManagerImpl attrManagerImplMock = mock(AttributesManagerImpl.class);

	@Before
	public void setUp() {
		attrManagerBl = new AttributesManagerBlImpl(attrManagerImplMock);
	}
}
