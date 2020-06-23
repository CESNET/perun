package cz.metacentrum.perun.notif.utils;

import cz.metacentrum.perun.core.api.*;
import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.notif.dao.jdbc.PerunNotifTemplateDaoImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

public class NotifUtils {

	public static PerunSession session = null;

	public static Map<String, String> parseMap(String row) {

		if (row == null) {
			return null;
		}

		Map<String, String> result = new HashMap<String, String>();

		String[] splittedValue = row.split(PerunNotifTemplateDaoImpl.DELIMITER);
		for (String entry : splittedValue) {
			String key = entry.substring(0, entry.indexOf("="));
			String value = entry.substring(entry.indexOf(entry.indexOf("=")));

			result.put(key, value);
		}

		return result;
	}

	public static List<PerunBean> parseMessage(String message) {

		List<PerunBean> result = new ArrayList<PerunBean>();

		cz.metacentrum.perun.core.api.User user = new cz.metacentrum.perun.core.api.User();
		user.setId(92979);
		user.setTitleBefore("Bc.");
		user.setFirstName("Tom");
		user.setLastName("Tunkl");
		user.setTitleAfter("CSc.");
		result.add(user);

		Group group = new Group();
		group.setId(0);
		group.setName("members");
		group.setDescription("Group test");
		result.add(group);

		Vo vo = new Vo();
		vo.setId(165684);
		vo.setName("AttributesManagerTestVo");
		vo.setShortName("AMTVO");
		result.add(vo);

		Facility facility = new Facility();
		facility.setId(123736);
		facility.setName("AttrTestFacility");
		result.add(facility);

		Resource resource = new Resource();
		resource.setId(0);
		resource.setName("ServicesManagerTestResource");
		resource.setDescription("testovaci");
		result.add(resource);

		Destination destination = new Destination();
		destination.setId(0);
		destination.setType("CLUSTER");
		result.add(destination);

		Member member = new Member();
		member.setId(6235);
		member.setVoId(181);
		member.setUserId(5032);
		result.add(member);

		return result;
	}

	public static PerunSession getPerunSession(PerunBl perun) {
		if (session == null) {
			if (perun != null) {
				session = perun.getPerunSession(
						new PerunPrincipal("perunNotifications", ExtSourcesManager.EXTSOURCE_NAME_INTERNAL, ExtSourcesManager.EXTSOURCE_INTERNAL),
						new PerunClient());
			} else {
				throw new InternalErrorException("PerunBl is null");
			}
		}
		return session;
	}
}
