package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.BeansUtils;
import cz.metacentrum.perun.core.api.PerunBean;

import java.util.*;

public class AuthzRoles extends HashMap<String, Map<String, Set<Integer>>> {
	private static final long serialVersionUID = 1L;

	public AuthzRoles() {
		super();
	}

	public AuthzRoles(String role) {
		super();
		this.put(role, null);
	}

	public AuthzRoles(String role, PerunBean perunBean) {
		super();
		Map<String, Set<Integer>> perunBeans = new HashMap<String, Set<Integer>>();
		Set<Integer> ids = new HashSet<>();
		ids.add(perunBean.getId());
		perunBeans.put(perunBean.getBeanName(), ids);
		this.put(role, perunBeans);
	}

	public AuthzRoles(String role, String beanName, int id) {
		super();
		Map<String, Set<Integer>> perunBeans = new HashMap<String, Set<Integer>>();
		perunBeans.put(beanName, new HashSet<Integer>(id));
		this.put(role, perunBeans);
	}

	public AuthzRoles(String role, HashMap<String, Set<Integer>> perunBeans) {
		super();
		this.put(role, perunBeans);
	}

	public AuthzRoles(String role, List<? extends PerunBean> perunBeans) {
		super();
		Map<String, Set<Integer>> complementaryObjects = new HashMap<String, Set<Integer>>();
		if (perunBeans != null) {
			for (PerunBean perunBean: perunBeans) {
				if (complementaryObjects.get(perunBean.getBeanName()) == null) {
					complementaryObjects.put(perunBean.getBeanName(), new HashSet<Integer>());
				}
				complementaryObjects.get(perunBean.getBeanName()).add(perunBean.getId());
			}
		}
		this.put(role, complementaryObjects);
	}

	public void putAuthzRoles(String role, Map<String, Set<Integer>> perunBeans) {
		this.putComplementaryObjects(role, perunBeans);
	}

	public void putAuthzRole(String role) {
		this.putComplementaryObject(role, null);
	}

	public void putAuthzRole(String role, PerunBean perunBean) {
		Map<String, Set<Integer>> complementaryObjects = new HashMap<String, Set<Integer>>();
		complementaryObjects.put(perunBean.getBeanName(), new HashSet<Integer>());
		complementaryObjects.get(perunBean.getBeanName()).add(perunBean.getId());

		this.putComplementaryObjects(role, complementaryObjects);
	}

	public void putAuthzRole(String role, Class perunBeanClass, int perunBeanId) {
		Map<String, Set<Integer>> complementaryObjects = new HashMap<String, Set<Integer>>();
		complementaryObjects.put(perunBeanClass.getSimpleName(), new HashSet<Integer>());
		complementaryObjects.get(perunBeanClass.getSimpleName()).add(perunBeanId);

		this.putComplementaryObjects(role, complementaryObjects);
	}

	public boolean hasRole(String role, PerunBean perunBean) {
		//Use converted beanName instead of classic bean name, because for ex.: RichGroup is the same like Group for this purpose
		String convertedBeanName = BeansUtils.convertRichBeanNameToBeanName(perunBean.getBeanName());
		return this.containsKey(role) && this.get(role).containsKey(convertedBeanName)
			&& this.get(role).get(convertedBeanName).contains(perunBean.getId());
	}

	public boolean hasRole(String role, String perunBeanName, int id) {
		//Use converted beanName instead of classic bean name, because for ex.: RichGroup is the same like Group for this purpose
		String convertedBeanName = BeansUtils.convertRichBeanNameToBeanName(perunBeanName);
		return this.containsKey(role) && this.get(role).containsKey(convertedBeanName)
			&& this.get(role).get(convertedBeanName).contains(id);
	}

	public boolean hasRole(String role) {
		return this.containsKey(role);
	}

	public List<String> getRolesNames() {
		try {
			return new ArrayList<>(this.keySet());
		} catch (ConcurrentModificationException e) {
			// concurrency problem try again
			return new ArrayList<>(this.keySet());
		}
	}

	protected void putComplementaryObject(String role, PerunBean perunBean) {
		if (this.containsKey(role)) {
			if (perunBean != null) {
				if (this.get(role).get(perunBean.getBeanName()) == null) {
					this.get(role).put(perunBean.getBeanName(), new HashSet<Integer>());
				}
				this.get(role).get(perunBean.getBeanName()).add(perunBean.getId());
			}
		} else  {
			this.put(role, new HashMap<String, Set<Integer>>());
			if (perunBean != null) {
				if (!this.containsKey(perunBean.getBeanName()) || this.get(perunBean.getBeanName()) == null) {
					this.get(role).put(perunBean.getBeanName(), new HashSet<Integer>());
				}
				if (perunBean != null) {
					this.get(role).get(perunBean.getBeanName()).add(perunBean.getId());
				}
			}
		}
	}

	protected void putComplementaryObjects(String role, Map<String, Set<Integer>> perunBeans) {
		if (this.containsKey(role) && (this.get(role) != null)) {
			if (perunBeans != null) {
				for (String perunBeanName: perunBeans.keySet()) {
					if (this.get(role).get(perunBeanName) != null) {
						this.get(role).get(perunBeanName).addAll(perunBeans.get(perunBeanName));
					} else {
						this.get(role).putAll(perunBeans);
					}
				}
			}
		} else {
			this.put(role, new HashMap<String, Set<Integer>>());
			if (perunBeans != null) {
				this.get(role).putAll(perunBeans);
			}
		}
	}

	public String toString() {
		String roles = "";
		if (!this.isEmpty()) {
			for (String role: this.keySet()) {
				roles += role + this.get(role) + ",";
			}
		}
		return getClass().getSimpleName() + "[" + roles + "]";
	}
}
