package cz.metacentrum.perun.core.impl;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.core.api.BeansUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class AttributesManagerImplIntegrationTest {

	private final Map<Attribute, String> attrMap = new HashMap<>();

	@Before
	public void setUp() {
		attrMap.put(getArrayAttribute("a", "b"), "a,b,");
		attrMap.put(getArrayAttribute("a,b", "c"), "a\\,b,c,");
		attrMap.put(getArrayAttribute("a,,b", "c"), "a\\,\\,b,c,");
		attrMap.put(getArrayAttribute("a\\b"), "a\\\\b,");

		attrMap.put(getHashAttribute("a", "b", "c", "d"), "a:b,c:d,");
		attrMap.put(getHashAttribute("a,x", "b,,", ",,c", ",,d,,"), "a\\,x:b\\,\\,,\\,\\,c:\\,\\,d\\,\\,,");
		attrMap.put(getHashAttribute("a:x", "b"), "a\\:x:b,");
		attrMap.put(getHashAttribute("a:x", "b", ":", "::"), "a\\:x:b,\\::\\:\\:,");
	}


	@Test
	public void stringToAttributeValue() throws Exception {
		System.out.println("AttributesManagerImpl.stringToAttributeValue");
		for(Map.Entry<Attribute, String> entry : attrMap.entrySet()) {
			Object value = BeansUtils.stringToAttributeValue(entry.getValue(), entry.getKey().getType());
			assertEquals("Input: " + entry.getValue(), entry.getKey().getValue(), value);
		}
	}

	@Test
	public void attributeValueToString() throws Exception {
		System.out.println("AttributesManagerImpl.attributeValueToString");
		for(Map.Entry<Attribute, String> entry : attrMap.entrySet()) {
			String DBValue = BeansUtils.attributeValueToString(entry.getKey());
			assertEquals(entry.getValue(), DBValue);
		}
	}

	public Attribute getArrayAttribute(String ... value) {
		Attribute attribute = new Attribute();
		attribute.setFriendlyName("test");
		attribute.setNamespace("test");
		attribute.setType(ArrayList.class.getName());
		attribute.setValue(new ArrayList<>(Arrays.asList(value)));
		return attribute;
	}

	public Attribute getHashAttribute(String ... value) {
		if(value.length % 2 == 1) throw new IllegalArgumentException("value");
		Attribute attribute = new Attribute();
		attribute.setFriendlyName("test");
		attribute.setNamespace("test");
		attribute.setType(LinkedHashMap.class.getName());

		Map<String, String> attrValue = new LinkedHashMap<>();
		for(int i = 0; i < value.length; i += 2) {
			attrValue.put(value[i], value[i+1]);
		}
		attribute.setValue(attrValue);
		return attribute;
	}
}
