package cz.metacentrum.perun.ldapc.processor.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import cz.metacentrum.perun.core.api.PerunBean;
import cz.metacentrum.perun.ldapc.model.PerunVirtualAttribute;
import cz.metacentrum.perun.ldapc.processor.VirtualAttributeManager;

@Component
public class AbstractVirtualAttributeManager<T extends PerunBean> implements VirtualAttributeManager<T> {

	private final static Logger log = LoggerFactory.getLogger(AbstractVirtualAttributeManager.class);

	/* maps perun group attribute names to ldap attribute descriptions */
	protected Map<String, List<PerunVirtualAttribute<T>>> attributeDependencies;
	
	public AbstractVirtualAttributeManager() {
		attributeDependencies = new HashMap<String, List<PerunVirtualAttribute<T>>>();
	}

	@Override
	public void registerAttributeDependency(String parentName, PerunVirtualAttribute<T> dependentAttr) {
		List<PerunVirtualAttribute<T>> attrDefs;
		if(attributeDependencies.containsKey(parentName)) {
			attrDefs = attributeDependencies.get(parentName);
		} else {
			attrDefs = new ArrayList<PerunVirtualAttribute<T>>();
			attributeDependencies.put(parentName, attrDefs);
		}
		List<String> presentAttrs = attrDefs.stream()
				.map(attr -> attr.getName())
				.distinct()
				.collect(Collectors.toList()); 
		if(!presentAttrs.contains(dependentAttr.getName())) {
			attrDefs.add(dependentAttr);
			log.debug("Added attribute {} dependency on {}", dependentAttr.getName(), parentName);
		}
	}

	@Override 
	public List<String> getRegisteredAttributes() {
		return new ArrayList<String>(attributeDependencies.keySet());
	}
	
	
	@Override
	public Collection<PerunVirtualAttribute<T>> getAttributeDependants(String name) {
		return attributeDependencies.get(name);
	}
	
	@Override
	public Collection<PerunVirtualAttribute<T>> getAllAttributeDependants() {
		List<PerunVirtualAttribute<T>> dependants = new ArrayList<>();
		attributeDependencies.values().forEach(attrDefs -> dependants.addAll(attrDefs));
		return dependants;
	}

	
}
