package cz.metacentrum.perun.core.impl.modules.attributes;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.AttributesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.impl.PerunSessionImpl;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Papperwing
 */
public class urn_perun_user_facility_attribute_def_virt_defaultUnixGIDTest {
    
    public urn_perun_user_facility_attribute_def_virt_defaultUnixGIDTest() {
    }
    
    private static urn_perun_user_facility_attribute_def_virt_defaultUnixGID classInstance;
    private static PerunSessionImpl session;
    private static Attribute preffered;
    private static Attribute defDefGID;
    private static Attribute facilityGID;
    private static List<Integer> resourceGID;
    private static List<Integer> listOfGIDs;
    private static User user;
    private static Facility facility;
    private static Resource resource;
    private static Resource resource1;
    private static List<Resource> resourceList;
    private static String namespace;

    
    @Before
    public void setUp() {
        classInstance = new urn_perun_user_facility_attribute_def_virt_defaultUnixGID();
        session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
        preffered = new Attribute();
        defDefGID = new Attribute();
        facilityGID = new Attribute();
        resourceGID = new ArrayList();
        listOfGIDs = new ArrayList<Integer>();
        for(int i=0;i<5;i++) listOfGIDs.add(i);
        preffered.setValue(new ArrayList(listOfGIDs));
        listOfGIDs.clear();
        listOfGIDs.add(2);
        facilityGID.setValue(listOfGIDs);
        resourceGID.addAll(listOfGIDs);
        namespace = "Test";
        
        
        user = new User();
        facility = new Facility();
        resource = new Resource();
        resource.setName("myResource");
        resource.setDescription("desc");

        resource1 = new Resource();
        resource1.setId(1);
        resource1.setName("myResource");
        resource1.setDescription("desc");
        
        resourceList = new ArrayList<Resource>();
        resourceList.add(resource1);
    }
    
    @Test
    public void getAttributeValueTest() throws Exception{
        System.out.println("urn_perun_user_facility_attribute_def_virt_shell.GetAttributeValue()");
        
        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), any(User.class), eq(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":defaultUnixGID"))).thenReturn(defDefGID);
        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), eq(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace"))).thenReturn(facilityGID);
        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(User.class), eq(AttributesManager.NS_USER_ATTR_DEF + ":preferredUnixGIDs"))).thenReturn(preffered);
        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), eq(AttributesManager.NS_FACILITY_ATTR_DEF + ":unixGID-namespace")).getValue()).thenReturn(namespace);
        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Resource.class), eq(AttributesManager.NS_RESOURCE_ATTR_DEF + ":unixGID-namespace:" + namespace)).getValue()).thenReturn(resourceGID);
        when(session.getPerunBl().getUsersManagerBl().getAllowedResources(any(PerunSessionImpl.class), any(Facility.class), any(User.class))).thenReturn(resourceList);
        
        Attribute testAttr = classInstance.getAttributeValue(session, facility, user, session.getPerunBl().getAttributesManagerBl().getAttributeDefinition(session, AttributesManager.NS_USER_FACILITY_ATTR_VIRT + "shell"));
        assertEquals(2, (String)testAttr.getValue());
        
    }
}