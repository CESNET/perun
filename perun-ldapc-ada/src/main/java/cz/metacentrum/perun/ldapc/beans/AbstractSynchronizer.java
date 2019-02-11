package cz.metacentrum.perun.ldapc.beans;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import cz.metacentrum.perun.core.api.exceptions.InternalErrorException;
import cz.metacentrum.perun.core.bl.PerunBl;
import cz.metacentrum.perun.ldapc.service.LdapcManager;

public abstract class AbstractSynchronizer {

	@Autowired
	protected LdapcManager ldapcManager;

	protected List<String> fillPerunAttributeNames(List<String> attrNames) throws InternalErrorException  {
		PerunBl perun = (PerunBl)ldapcManager.getPerunBl();
		List<String> result = new ArrayList<String>();
		for(String name : attrNames) {
			if(name.endsWith(":")) {
				result.addAll(perun.getAttributesManagerBl().getAllSimilarAttributeNames(ldapcManager.getPerunSession(), name));
			} else {
				result.add(name);
			}
		}
		return result;
	}
}
