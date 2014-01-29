package cz.metacentrum.perun.core.entry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import cz.metacentrum.perun.core.AbstractPerunIntegrationTest;
import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributeDefinition;
import cz.metacentrum.perun.core.api.Candidate;
import cz.metacentrum.perun.core.api.ExtSource;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.UserExtSource;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.bl.SearcherBl;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Michal Šťava <stavamichal@gmail.com>
 */

public class SearcherEntryIntegrationTest extends AbstractPerunIntegrationTest {

	// these are in DB only when needed and must be setUp"type"() in right order before use !!
	private User user1;                             // our User
        private User user2;
        private Candidate candidate1;
        private Candidate candidate2;
        private Vo vo;
	String extLogin = "aaa";              // his login in external source
	String extLogin2 = "bbb"; 
	String extSourceName = "LDAPMETA";        // real ext source with his login
	final ExtSource extSource = new ExtSource(0, "testExtSource", "cz.metacentrum.perun.core.impl.ExtSourceInternal");
	private SearcherBl searcherBl;
        private Attribute integerAttr;
        private Attribute stringAttr;
        private Attribute listAttr;
        private Attribute mapAttr;

	// setUp methods moved to every test method to save testing time !!

	@Before
	public void setUp() throws Exception {
		searcherBl = perun.getSearcherBl();
                vo = setUpVo();
                candidate1 = setUpCandidate1();
                candidate2 = setUpCandidate2();
                setUpUser1();
                setUpUser2();
                integerAttr = setUpUserAttributeWithIntegerValue();
                stringAttr = setUpUserAttributeWithStringValue();
                listAttr = setUpUserAttributeWithListValue();
                mapAttr = setUpUserLargeAttributeWithMapValue();
                perun.getAttributesManagerBl().setAttribute(sess, user1, integerAttr);
                perun.getAttributesManagerBl().setAttribute(sess, user1, mapAttr);
                perun.getAttributesManagerBl().setAttribute(sess, user1, listAttr);
                perun.getAttributesManagerBl().setAttribute(sess, user2, stringAttr);
                perun.getAttributesManagerBl().setAttribute(sess, user2, listAttr);
	}

       @Test
	public void getUsersForIntegerValue() throws Exception {
		System.out.println("Searcher.getUsersForIntegerValue");
                Map<String, String> attributesWithSearchingValues = new HashMap<String, String>();
                attributesWithSearchingValues.put(integerAttr.getName(), "100");
                AttributeDefinition attrDef = sess.getPerun().getAttributesManager().getAttributeDefinition(sess, integerAttr.getName());
                Attribute attr = new Attribute(attrDef);
                List<User> users = new ArrayList<User>();
                users = searcherBl.getUsers(sess, attributesWithSearchingValues);
                assertTrue("user1 have to be found", users.contains(user1));
                assertTrue("user2 have not to be found", !users.contains(user2));
	}
       
       @Test
	public void getUsersForStringValue() throws Exception {
		System.out.println("Searcher.getUsersForStringValue");
                Map<String, String> attributesWithSearchingValues = new HashMap<String, String>();
                attributesWithSearchingValues.put(stringAttr.getName(), "UserStringAttribute test value");
                List<User> users = new ArrayList<User>();
                users = searcherBl.getUsers(sess, attributesWithSearchingValues);
                assertTrue("user1 have not to be found", !users.contains(user1));
                assertTrue("user2 have to be found", users.contains(user2));        
	}  
       
        @Test
	public void getUsersForListValue() throws Exception {
		System.out.println("Searcher.getUsersForListValue");
                Map<String, String> attributesWithSearchingValues = new HashMap<String, String>();
                attributesWithSearchingValues.put(listAttr.getName(), "ttribute2");
                List<User> users = new ArrayList<User>();
                users = searcherBl.getUsers(sess, attributesWithSearchingValues);
                assertTrue("user2 have to be found", users.contains(user2));
                assertTrue("user1 have to be found", users.contains(user1));
	}
        
        @Test
        public void getUsersForCoreAttribute() throws Exception {
                System.out.println("Searcher.getUsersForCoreAttribute");
                Attribute attr = perun.getAttributesManagerBl().getAttribute(sess, user1, "urn:perun:user:attribute-def:core:id");
                Map<String, String> attributesWithSearchingValues = new HashMap<String, String>();
                attributesWithSearchingValues.put(attr.getName(), attr.getValue().toString());
                List<User> users = new ArrayList<User>();
                users = searcherBl.getUsers(sess, attributesWithSearchingValues);
                System.out.println(attr.getValue().toString());
                System.out.println(attr.getType().toString());
                System.out.println(users.toString());
        }
                
        @Test
	public void getUsersForMapValue() throws Exception {
		System.out.println("Searcher.getUsersForMapValue");
                Map<String, String> attributesWithSearchingValues = new HashMap<String, String>();
                attributesWithSearchingValues.put(mapAttr.getName(), "UserLargeAttribute=test value");
                List<User> users = new ArrayList<User>();
                users = searcherBl.getUsers(sess, attributesWithSearchingValues);
                assertTrue("user2 have not to be found", !users.contains(user2));
                assertTrue("user1 have to be found", users.contains(user1));
	}

	// PRIVATE METHODS -----------------------------------------------------------

	private void setUpUser1() throws Exception {
            Member member = perun.getMembersManagerBl().createMember(sess, vo, candidate1);
            user1 = perun.getUsersManagerBl().getUserByMember(sess, member);
	}
        
        private void setUpUser2() throws Exception {
            Member member = perun.getMembersManagerBl().createMember(sess, vo, candidate2);
            user2 = perun.getUsersManagerBl().getUserByMember(sess, member);
	}

	private Vo setUpVo() throws Exception {

		Vo newVo = new Vo(0, "UserManagerTestVo", "UMTestVo");
		Vo returnedVo = perun.getVosManager().createVo(sess, newVo);
		// create test VO in database
		assertNotNull("unable to create testing Vo",returnedVo);
		assertEquals("both VOs should be the same",newVo,returnedVo);

		ExtSource es = perun.getExtSourcesManager().getExtSourceByName(sess, extSourceName);
		// get real external source from DB
		perun.getExtSourcesManager().addExtSource(sess, returnedVo, es);
		// add real ext source to our VO
		return returnedVo;
	}

	private Candidate setUpCandidate1(){

		Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName("aaa1");
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName("bbb1");
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		final UserExtSource userExtSource = new UserExtSource(extSource, extLogin);
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<String,String>());
		return candidate;

	}
        
	private Candidate setUpCandidate2(){

		Candidate candidate = new Candidate();  //Mockito.mock(Candidate.class);
		candidate.setFirstName("aaa2");
		candidate.setId(0);
		candidate.setMiddleName("");
		candidate.setLastName("bbb2");
		candidate.setTitleBefore("");
		candidate.setTitleAfter("");
		final UserExtSource userExtSource = new UserExtSource(extSource, extLogin2);
		candidate.setUserExtSource(userExtSource);
		candidate.setAttributes(new HashMap<String,String>());
		return candidate;

        }
        
       private Attribute setUpUserAttributeWithIntegerValue() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:user:attribute-def:opt");
		attr.setFriendlyName("user_integer_test_attribute");
		attr.setType(Integer.class.getName());
		attr.setValue(100);
		assertNotNull("unable to create user attribute",perun.getAttributesManagerBl().createAttribute(sess, attr));
		// create new resource member attribute
                return attr;

	}        
        
       private Attribute setUpUserAttributeWithStringValue() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:user:attribute-def:opt");
		attr.setFriendlyName("user_string_test_attribute");
		attr.setType(String.class.getName());
		attr.setValue("UserStringAttribute test value");
		assertNotNull("unable to create user attribute",perun.getAttributesManagerBl().createAttribute(sess, attr));
		// create new resource member attribute
                return attr;
	}

       	private Attribute setUpUserAttributeWithListValue() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:user:attribute-def:opt");
		attr.setFriendlyName("user_list_test_attribute");
		attr.setType(ArrayList.class.getName());
                List<String> value = new ArrayList<String>();
                value.add("UserStringAttribute test value");
                value.add("UserStringAttribute2 test2 value2");
                attr.setValue(value);
                assertNotNull("unable to create user attribute",perun.getAttributesManagerBl().createAttribute(sess, attr));
                // create new resource member attribute
                return attr;

	}
       
	private Attribute setUpUserLargeAttributeWithMapValue() throws Exception {

		Attribute attr = new Attribute();
		attr.setNamespace("urn:perun:user:attribute-def:opt");
		attr.setFriendlyName("user_map_test_large_attribute");
		attr.setType(LinkedHashMap.class.getName());
		Map<String, String> value = new LinkedHashMap<String, String>();
		value.put("UserLargeAttribute", "test value");
		attr.setValue(value);
		assertNotNull("unable to create user attribute",perun.getAttributesManagerBl().createAttribute(sess, attr));
		return attr;

	}

}
