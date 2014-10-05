package cz.metacentrum.perun.dispatcher;


import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.controller.service.GeneralServiceManager;
import cz.metacentrum.perun.core.api.ExtSourcesManager;
import cz.metacentrum.perun.core.api.Facility;
import cz.metacentrum.perun.core.api.Group;
import cz.metacentrum.perun.core.api.Member;
import cz.metacentrum.perun.core.api.Owner;
import cz.metacentrum.perun.core.api.Perun;
import cz.metacentrum.perun.core.api.PerunPrincipal;
import cz.metacentrum.perun.core.api.PerunSession;
import cz.metacentrum.perun.core.api.Resource;
import cz.metacentrum.perun.core.api.Service;
import cz.metacentrum.perun.core.api.User;
import cz.metacentrum.perun.core.api.Vo;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.blImpl.PerunBlImpl;
import cz.metacentrum.perun.taskslib.model.ExecService;

public class TestDataSourcePopulator {
	@Autowired
	private Perun perun;
	private PerunSession testSession;
    @Autowired
    private GeneralServiceManager generalServiceManager;
	
	// the test database is populated with these data (configured as beans) 
	@Autowired 
	private Group group1;
	@Autowired
	private Vo vo1;
	@Autowired
	private User user1;
	@Autowired
	private Facility facility1;
	@Autowired
	private Resource resource1;
	@Autowired
	private Service service1;
	@Autowired
	private Owner owner1;
	private Member member1;
	@Autowired
	private ExecService execservice1;
	@Autowired
	private ExecService execservice2;
	
	
	public TestDataSourcePopulator() {
	}
	
	/**
	 * Populate database with enough data to run simple test. Basic data objects are created by Spring 
	 * as beans and then inserted into DB using the Perun core API.
	 * 
	 * @throws InternalErrorException
	 */
	public final void initDb() throws InternalErrorException {
		PerunPrincipal pp = new PerunPrincipal("perunTests", ExtSourcesManager.EXTSOURCE_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL);
		PerunBlImpl perunBl = (PerunBlImpl)perun;
		testSession = perun.getPerunSession(pp);
		try {
			// create VO for tests
			vo1 = perun.getVosManager().createVo(testSession, vo1);
			// create some group in there
			group1 = perun.getGroupsManager().createGroup(testSession, vo1, group1);
			// create user in the VO
			// skip the xEntry (authorization check), 
			// could skip the xBl a go directly to xImpl to avoid writing audit log
			user1 = perunBl.getUsersManagerBl().createUser(testSession, user1);
			// make the user the member of the group
			member1 = perun.getMembersManager().createMember(testSession, vo1, user1);
			member1.setStatus("VALID");
			perun.getGroupsManager().addMember(testSession, group1, member1);
			// now create some facility
			facility1 = perun.getFacilitiesManager().createFacility(testSession, facility1);
			// create a resource
			resource1 = perun.getResourcesManager().createResource(testSession, resource1, vo1, facility1);
			// assign the group to this resource
			perun.getResourcesManager().assignGroupToResource(testSession, group1, resource1);
			// create owner
			perun.getOwnersManager().createOwner(testSession, owner1);
			// create service
			service1 = perun.getServicesManager().createService(testSession, service1, owner1);
			// assign service to the resource
			perun.getResourcesManager().assignService(testSession, resource1, service1);
			// create execService
			int id = generalServiceManager.insertExecService(testSession, execservice1, owner1);
			// stash back the created id (this should be really done somewhere else)
			execservice1.setId(id);
			// create execService
			id = generalServiceManager.insertExecService(testSession, execservice2, owner1);
			// stash back the created id (this should be really done somewhere else)
			execservice2.setId(id);
		} catch (Exception e) {
			throw new InternalErrorException("error populating database", e);
		}
	}

	public Perun getPerun() {
		return perun;
	}

	public void setPerun(Perun perun) {
		this.perun = perun;
	}

	public PerunSession getTestSession() {
		return testSession;
	}

	public void setTestSession(PerunSession testSession) {
		this.testSession = testSession;
	}

	public GeneralServiceManager getGeneralServiceManager() {
		return generalServiceManager;
	}

	public void setGeneralServiceManager(GeneralServiceManager generalServiceManager) {
		this.generalServiceManager = generalServiceManager;
	}

	public Group getGroup1() {
		return group1;
	}

	public void setGroup1(Group group1) {
		this.group1 = group1;
	}

	public Vo getVo1() {
		return vo1;
	}

	public void setVo1(Vo vo1) {
		this.vo1 = vo1;
	}

	public User getUser1() {
		return user1;
	}

	public void setUser1(User user1) {
		this.user1 = user1;
	}

	public Facility getFacility1() {
		return facility1;
	}

	public void setFacility1(Facility facility1) {
		this.facility1 = facility1;
	}

	public Resource getResource1() {
		return resource1;
	}

	public void setResource1(Resource resource1) {
		this.resource1 = resource1;
	}

	public Service getService1() {
		return service1;
	}

	public void setService1(Service service1) {
		this.service1 = service1;
	}

	public Owner getOwner1() {
		return owner1;
	}

	public void setOwner1(Owner owner1) {
		this.owner1 = owner1;
	}

	public Member getMember1() {
		return member1;
	}

	public void setMember1(Member member1) {
		this.member1 = member1;
	}

	public ExecService getExecService1() {
		return execservice1;
	}

	public void setExecService1(ExecService execService1) {
		this.execservice1 = execService1;
	}

}
