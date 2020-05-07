package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AllAttributesRemovedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeRemovedForUser;
import cz.metacentrum.perun.audit.events.AttributesManagerEvents.AttributeSetForUser;
import cz.metacentrum.perun.audit.events.AuditEvent;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.bl.AttributesManagerBl;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.core.bl.UsersManagerBl;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Michal Stava stavamichal@gmail.com
 */
public class urn_perun_user_attribute_def_virt_tcsMails_muTest {

	private final static Logger log = LoggerFactory.getLogger(urn_perun_user_attribute_def_virt_tcsMails_muTest.class);

	private final urn_perun_user_attribute_def_virt_tcsMails_mu classInstance = new urn_perun_user_attribute_def_virt_tcsMails_mu();
	private final AttributeDefinition tcsMailsAttrDef = classInstance.getAttributeDefinition();

	private final User user = new User(10, "Joe", "Doe", "W.", "", "");

	private final String email1 = "Zemail1@mail.cz";
	private final String email2 = "email2@mail.cz";
	private final String email3 = "email3@mail.cz";
	private final String email4 = "email4@mail.cz";
	private final String email5 = "email5@mail.cz";

	Attribute preferredMailAttr = setUpUserAttribute(1, "preferredMail", String.class.getName(), email1);
	Attribute isMailAttr = setUpUserAttribute(2, "ISMail", String.class.getName(), email2);
	Attribute publicMailsAttr = setUpUserAttribute(3, "o365EmailAddresses:mu", ArrayList.class.getName(), new ArrayList(Arrays.asList(email3, email4)));
	Attribute privateMailsAttr = setUpUserAttribute(4, "publicAliasMails", ArrayList.class.getName(), new ArrayList(Arrays.asList(email4, email5)));
	Attribute o365MailsAttr = setUpUserAttribute(5, "privateAliasMails", ArrayList.class.getName(), new ArrayList(Arrays.asList(email1, email3, email5)));

	private final String expectedTestOfMessage = "friendlyName=<tcsMails:mu>";

	private PerunSessionImpl sess;

	@Before
	public void setUp() throws Exception {
		tcsMailsAttrDef.setId(100);
		//prepare mocks
		sess = mock(PerunSessionImpl.class);
		PerunBl perunBl = mock(PerunBl.class);
		AttributesManagerBl am = mock(AttributesManagerBl.class);
		UsersManagerBl um = mock(UsersManagerBl.class);
		when(sess.getPerunBl()).thenReturn(perunBl);
		when(perunBl.getAttributesManagerBl()).thenReturn(am);
		when(perunBl.getUsersManagerBl()).thenReturn(um);
		when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, preferredMailAttr.getName())).thenReturn(preferredMailAttr);
		when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, isMailAttr.getName())).thenReturn(isMailAttr);
		when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, publicMailsAttr.getName())).thenReturn(publicMailsAttr);
		when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, privateMailsAttr.getName())).thenReturn(privateMailsAttr);
		when(sess.getPerunBl().getAttributesManagerBl().getAttribute(sess, user, o365MailsAttr.getName())).thenReturn(o365MailsAttr);
		when(sess.getPerunBl().getAttributesManagerBl().getAttributeDefinition(sess, tcsMailsAttrDef.getName())).thenReturn(tcsMailsAttrDef);
	}

	private Attribute setUpUserAttribute(int id, String friendlyName, String type, Object value) {
		AttributeDefinition attrDef = new AttributeDefinition();
		attrDef.setId(id);
		attrDef.setFriendlyName(friendlyName);
		attrDef.setNamespace(AttributesManager.NS_USER_ATTR_DEF);
		attrDef.setType(type);
		Attribute attr = new Attribute(attrDef);
		attr.setValue(value);
		return attr;
	}

	@Test
	public void getAttributeValue() {
		@SuppressWarnings("unchecked") ArrayList<String> attributeValue = classInstance.getAttributeValue(sess, user, tcsMailsAttrDef).valueAsList();
		assertThat(attributeValue, is(notNullValue()));
		//we want to be sure, that preferredEmail is first (defined by sorting in module)
		assertTrue(attributeValue.get(0).equals(email1));
		assertTrue(attributeValue.get(1).equals(email2));
		assertTrue(attributeValue.get(2).equals(email3));
		assertTrue(attributeValue.get(3).equals(email4));
		assertTrue(attributeValue.get(4).equals(email5));
		assertThat(attributeValue.size(), is(5));
	}

	@Test
	public void resolveVirtualAttributeValueChangeSet1() throws Exception {
		AuditEvent userSet = new AttributeSetForUser(preferredMailAttr, user);
		List<AuditEvent> msgs = classInstance.resolveVirtualAttributeValueChange(sess, userSet);
		assertTrue("audit should contain change of tcsMails",msgs.get(0).getMessage().contains(expectedTestOfMessage));
	}

	@Test
	public void resolveVirtualAttributeValueChangeSet2() throws Exception {
		AuditEvent userSet = new AttributeSetForUser(isMailAttr, user);
		List<AuditEvent> msgs = classInstance.resolveVirtualAttributeValueChange(sess, userSet);
		assertTrue("audit should contain change of tcsMails",msgs.get(0).getMessage().contains(expectedTestOfMessage));
	}

	@Test
	public void resolveVirtualAttributeValueChangeSet3() throws Exception {
		AuditEvent userSet = new AttributeSetForUser(publicMailsAttr, user);
		List<AuditEvent> msgs = classInstance.resolveVirtualAttributeValueChange(sess, userSet);
		assertTrue("audit should contain change of tcsMails",msgs.get(0).getMessage().contains(expectedTestOfMessage));
	}

	@Test
	public void resolveVirtualAttributeValueChangeSet4() throws Exception {
		AuditEvent userSet = new AttributeSetForUser(privateMailsAttr, user);
		List<AuditEvent> msgs = classInstance.resolveVirtualAttributeValueChange(sess, userSet);
		assertTrue("audit should contain change of tcsMails",msgs.get(0).getMessage().contains(expectedTestOfMessage));
	}

	@Test
	public void resolveVirtualAttributeValueChangeSet5() throws Exception {
		AuditEvent userSet = new AttributeSetForUser(o365MailsAttr, user);
		List<AuditEvent> msgs = classInstance.resolveVirtualAttributeValueChange(sess, userSet);
		assertTrue("audit should contain change of tcsMails",msgs.get(0).getMessage().contains(expectedTestOfMessage));
	}

	@Test
	public void resolveVirtualAttributeValueChangeRemoved1() throws Exception {
		AuditEvent userRem = new AttributeRemovedForUser(preferredMailAttr, user);
		List<AuditEvent> msgs = classInstance.resolveVirtualAttributeValueChange(sess, userRem);
		assertTrue("audit should contain change of tcsMails",msgs.get(0).getMessage().contains(expectedTestOfMessage));
	}

	@Test
	public void resolveVirtualAttributeValueChangeRemoved2() throws Exception {
		AuditEvent userRem = new AttributeRemovedForUser(isMailAttr, user);
		List<AuditEvent> msgs = classInstance.resolveVirtualAttributeValueChange(sess, userRem);
		assertTrue("audit should contain change of tcsMails",msgs.get(0).getMessage().contains(expectedTestOfMessage));
	}

	@Test
	public void resolveVirtualAttributeValueChangeRemoved3() throws Exception {
		AuditEvent userRem = new AttributeRemovedForUser(publicMailsAttr, user);
		List<AuditEvent> msgs = classInstance.resolveVirtualAttributeValueChange(sess, userRem);
		assertTrue("audit should contain change of tcsMails",msgs.get(0).getMessage().contains(expectedTestOfMessage));
	}

	@Test
	public void resolveVirtualAttributeValueChangeRemoved4() throws Exception {
		AuditEvent userRem = new AttributeRemovedForUser(privateMailsAttr, user);
		List<AuditEvent> msgs = classInstance.resolveVirtualAttributeValueChange(sess, userRem);
		assertTrue("audit should contain change of tcsMails",msgs.get(0).getMessage().contains(expectedTestOfMessage));
	}

	@Test
	public void resolveVirtualAttributeValueChangeRemoved5() throws Exception {
		AuditEvent userRem = new AttributeRemovedForUser(o365MailsAttr, user);
		List<AuditEvent> msgs = classInstance.resolveVirtualAttributeValueChange(sess, userRem);
		assertTrue("audit should contain change of tcsMails",msgs.get(0).getMessage().contains(expectedTestOfMessage));
	}

	@Test
	public void resolveVirtualAttributeValueChangeRemovedAll() throws Exception {
		AuditEvent allRemForUser = new AllAttributesRemovedForUser(user);
		List<AuditEvent> msgs = classInstance.resolveVirtualAttributeValueChange(sess, allRemForUser);
		assertTrue("audit should contain change of tcsMails",msgs.get(0).getMessage().contains(expectedTestOfMessage));
	}
}
