/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.metacentrum.perun.voot;

import com.sun.security.auth.UserPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import cz.metacentrum.perun.voot.VOOT;
import org.springframework.mock.web.MockServletContext;

/**
 * JavaDoc - TODO
 * 
 * @author Martin Malik <374128@mail.muni.cz>
 */
public class VOOTJUnitTest {
    
    private MockHttpSession session;
    private MockHttpServletRequest request;
    //private MockRequestContext context;
 
    public VOOTJUnitTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        this.session = new MockHttpSession();
        this.request.setUserPrincipal(new UserPrincipal("VOOT"));
        MockServletContext servletContext = new MockServletContext(); 
        //this.request.setSession(session);
        
    }
    
    @After
    public void tearDown() {
    }
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    
    /*
    @Test
    public void getPerson(){
    }
      */             
}
