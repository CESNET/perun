/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
public class urn_perun_user_facility_attribute_def_virt_shellTest {
    
    public urn_perun_user_facility_attribute_def_virt_shellTest() {
    }
    
    private static urn_perun_user_facility_attribute_def_virt_shell classInstance;
    private static PerunSessionImpl session;
    private static Attribute preffered;
    private static Attribute def_def_shell;
    private static Attribute facilityShell;
    private static Attribute resourceShell;
    private static List<String> listOfMntPts;
    private static User user;
    private static Facility facility;
    private static Resource resource;
    private static Resource resource1;

    
    @Before
    public void setUp() {
        classInstance = new urn_perun_user_facility_attribute_def_virt_shell();
        session = mock(PerunSessionImpl.class, RETURNS_DEEP_STUBS);
        preffered = new Attribute();
        def_def_shell = new Attribute();
        facilityShell = new Attribute();
        resourceShell = new Attribute();
        listOfMntPts = new ArrayList<>();
        for(int i=0;i<5;i++)
        listOfMntPts.add("/mnt/bash" + i);
        preffered.setValue(listOfMntPts);
        listOfMntPts.clear();
        listOfMntPts.add("/mnt/bash2");
        facilityShell.setValue(listOfMntPts);
        resourceShell.setValue(listOfMntPts);
        
        
        user = new User();
        facility = new Facility();
        resource = new Resource();
        resource.setName("myResource");
        resource.setDescription("desc");

        resource1 = new Resource();
        resource1.setId(1);
        resource1.setName("myResource");
        resource1.setDescription("desc");
    }
    
    @Test
    public void getAttributeValueTest() throws Exception{
        System.out.println("urn_perun_user_facility_attribute_def_virt_shell.GetAttributeValue()");
        
        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), any(User.class), eq(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":shell"))).thenReturn(def_def_shell);
        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), eq(AttributesManager.NS_USER_FACILITY_ATTR_DEF + ":prefferedShells"))).thenReturn(preffered);
        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), any(User.class), eq(AttributesManager.NS_RESOURCE_ATTR_DEF + ":shells"))).thenReturn(resourceShell);
        when(session.getPerunBl().getAttributesManagerBl().getAttribute(any(PerunSessionImpl.class), any(Facility.class), any(User.class), eq(AttributesManager.NS_FACILITY_ATTR_DEF + ":shells"))).thenReturn(facilityShell);
        
        Attribute testAttr = classInstance.getAttributeValue(session, facility, user, session.getPerunBl().getAttributesManagerBl().getAttributeDefinition(session, AttributesManager.NS_USER_FACILITY_ATTR_VIRT + "shell"));
        assertEquals("/mnt/bash2", (String)testAttr.getValue());
        
    }
}