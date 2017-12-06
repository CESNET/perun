package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.exceptions.AttributeNotExistsException;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.api.exceptions.WrongAttributeAssignmentException;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class urn_perun_user_attribute_def_virt_eduPersonScopedAffiliationsTest {

	@Test
	public void getAttributeValue() throws Exception {
		urn_perun_user_attribute_def_virt_eduPersonScopedAffiliations classInstance = new urn_perun_user_attribute_def_virt_eduPersonScopedAffiliations();
		PerunSessionImpl session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
		User user = new User();
		user.setId(1);
		UserExtSource ues1 = new UserExtSource(10, new ExtSource(100, "name1", "type1"), "login1");
		UserExtSource ues2 = new UserExtSource(20, new ExtSource(200, "name2", "type2"), "login2");
		Attribute att1 = new Attribute();
		String VALUE1 = "member@somewhere.edu";
		att1.setValue(VALUE1);
		Attribute att2 = new Attribute();
		String VALUE2 = "affiliate@company.com";
		String VALUE3 = "library-walk-in@company.com";
		att2.setValue(VALUE2+";"+VALUE3);

		when(session.getPerunBl().getUsersManagerBl().getUserExtSources(session, user)).thenReturn(
				Arrays.asList(ues1, ues2)
		);

		String attributeName = classInstance.getSourceAttributeName();
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues1, attributeName)).thenReturn(
				att1
		);
		when(session.getPerunBl().getAttributesManagerBl().getAttribute(session, ues2, attributeName)).thenReturn(
				att2
		);

		Attribute receivedAttr = classInstance.getAttributeValue(session, user, classInstance.getAttributeDefinition());
		assertTrue(receivedAttr.getValue() instanceof List);
		assertEquals("destination attribute name wrong",classInstance.getDestinationAttributeFriendlyName(),receivedAttr.getFriendlyName());

		@SuppressWarnings("unchecked")
		List<String> actual = (List<String>) receivedAttr.getValue();
		Collections.sort(actual);
		List<String> expected = Arrays.asList(VALUE1,VALUE2,VALUE3);
		Collections.sort(expected);
		assertEquals("collected values are incorrect",expected,actual);
	}

}
