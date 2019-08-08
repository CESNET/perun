package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeValueException;
import cz.metacentrum.perun.core.api.exceptions.WrongReferenceAttributeValueException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests
 *
 * @author Jiri Harazim <harazim@mail.muni.cz>
 */

public class urn_perun_resource_attribute_def_def_defaultShellTest {

	private static urn_perun_resource_attribute_def_def_defaultShell defShellAttr;
	private static PerunSessionImpl ps;

	@Before
	public  void setUp() {
		defShellAttr = new urn_perun_resource_attribute_def_def_defaultShell();

		//mockujeme PerunSession ps
		ps = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS); //RETURNS_DEEP_STUBS = budeme mockovat nekolik vnorenych volani metod

	}

	@SuppressWarnings("serial")
	@Test
	public void fillAttribute() throws Exception {
		System.out.println("fillAttribute()");

		//tento objekt ocekavame, ze se nam vrati po zavolani fillAttribute()
		final Attribute attrToReturn = new Attribute();
		final String shellName = "mujShell";
		attrToReturn.setValue(new ArrayList<String>(){{add(shellName);}});

		//a tady si nastavime pozadovane chovani
		when(ps.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(Resource.class), anyString())).thenReturn(attrToReturn);

		// ideal scenario
		final Attribute result= defShellAttr.fillAttribute(ps, new Resource(), new AttributeDefinition());

		assertEquals("fillAttribute spatne vyplnil value",shellName, result.getValue());
	}


	@Test
	public void fillAttributeWithEmptyValue() throws Exception {
		System.out.println("fillAttributeWithEmptyValue()");

		final Attribute attrToReturn = new Attribute();
		when(ps.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(Resource.class), anyString())).thenReturn(attrToReturn);


		final Attribute attrResult = defShellAttr.fillAttribute(ps, new Resource(), new Attribute());

		assertNull("Atribut.getValue() ma byt null", attrResult.getValue());
	}

	@Test
	public void fillAttributeWhichNotExists() throws Exception {
		System.out.println("fillAttributeWhichNotExists()");

		//testujeme scenar, kdy budeme hledat neexistujici atribut a proto ocekavame vyjimku AttrNotExists..
		when(ps.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(Resource.class), anyString())).thenThrow(new AttributeNotExistsException("neexistuje"));

		try {
			defShellAttr.fillAttribute(ps, new Resource(), new AttributeDefinition());
			fail();
		} catch (InternalErrorException ex) {
			assertTrue("Mela byt vyhozena vyjimka AttributeNotExistsException", (ex.getCause() instanceof AttributeNotExistsException));
		}
	}



	@SuppressWarnings("serial")
	@Test
	public void checkAttributeSemantics() throws Exception {
		System.out.println("checkAttributeSemantics()");

		final Attribute attrToReturn = new Attribute();
		final String shellName = "mujShell";
		attrToReturn.setValue(new ArrayList<String>(){{add(shellName);}});


		// chceme najit atribut attrToReturn
		when(ps.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(Resource.class), anyString())).thenReturn(attrToReturn);

		// parametr pro hledani
		final Attribute attribute = new Attribute();
		attribute.setValue(shellName);

		//ideal scenario without exceptions..
		defShellAttr.checkAttributeSemantics(ps, new Resource(), attribute);

	}

	@Test(expected=WrongAttributeValueException.class)
	public void checkAttributeWithoutValue() throws Exception {
		System.out.println("checkAttributeWithoutValue()");
		defShellAttr.checkAttributeSemantics(ps, new Resource(), new Attribute());
	}

	@Test
	public void checkAttributeSemanticsWhichNotExists() throws Exception {
		System.out.println("checkAttributeSemanticsWhichNotExists()");

		// hledame neexistujici atribut, proto ocekavame vyjimku
		when(ps.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(Resource.class), anyString())).thenThrow(new AttributeNotExistsException("neexistuje"));

		final Attribute attribute = new Attribute();
		attribute.setValue("mujShell");

		try {
			defShellAttr.checkAttributeSemantics(ps, new Resource(), attribute);
			fail();
		} catch (InternalErrorException ex) {
			assertTrue("Mela byt vyhozena vyjimka AttributeNotExistsException", (ex.getCause() instanceof AttributeNotExistsException));
		}
	}

	@Test(expected=WrongReferenceAttributeValueException.class)
	public void checkAttributeSemanticsWithWrongReference() throws Exception {
		System.out.println("checkAttributeSemanticsWithWrongReference()");

		final Attribute toReturn = new Attribute();
		when(ps.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSession.class), any(Resource.class), anyString())).thenReturn(toReturn);

		final Attribute attribute = new Attribute();
		attribute.setValue("mujShell");

		defShellAttr.checkAttributeSemantics(ps, new Resource(), attribute);
	}


}
