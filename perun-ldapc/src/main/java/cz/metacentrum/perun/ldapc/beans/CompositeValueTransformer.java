package cz.metacentrum.perun.ldapc.beans;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cz.metacentrum.perun.core.api.Attribute;
import cz.metacentrum.perun.ldapc.model.AttributeValueTransformer;


public class CompositeValueTransformer extends ValueTransformerBase implements AttributeValueTransformer {

	protected List<AttributeValueTransformer> transformerList;
	
	@Override
	public String getValue(String value, Attribute attr) {
		String result = value;
		for (AttributeValueTransformer transformer : transformerList) {
			result = transformer.getValue(result, attr);
		}
		return result;
	}

	@Override
	public String getValue(Collection<String> value, Attribute attr) {
		Collection<String> intermediate = value;
		String result = null;
		for(AttributeValueTransformer transformer : transformerList) {
			if(result == null) {
				if(transformer.isReduce()) {
					result = transformer.getValue(intermediate, attr);
				} else {
					if(transformer.isMassTransformationPreferred()) {
						intermediate = Arrays.asList(transformer.getAllValues(intermediate, attr));
					} else {
						intermediate = intermediate.stream()
								.map(one -> transformer.getValue(one, attr))
								.collect(Collectors.toList());
								
					}	
				}
			} else {
				result = transformer.getValue(result, attr);
			}
		}
		return result;
	}

	@Override
	public String getValue(Map<String, String> value, Attribute attr) {
		Collection<String> intermediate = null;
		String result = null;
		for(AttributeValueTransformer transformer : transformerList) {
			if(intermediate == null) {
				if(transformer.isReduce()) {
					// first transformer has to reduce map to array
					result = transformer.getValue(value, attr);
				} else {
					// first transformer has to reduce map to array
					intermediate = Arrays.asList(transformer.getAllValues(value, attr));
				}
			} else {

				if(result == null) {

					if(transformer.isReduce()) {
						result = transformer.getValue(intermediate, attr);
					} else {
						if(transformer.isMassTransformationPreferred()) {
							intermediate = Arrays.asList(transformer.getAllValues(intermediate, attr));
						} else {
							intermediate = intermediate.stream()
									.map(one -> transformer.getValue(one, attr))
									.collect(Collectors.toList());
								
						}	
					}
				} else {
					result = transformer.getValue(result, attr);
				}
			}
		}
		return result;
	}

	@Override
	public String[] getAllValues(Collection<String> value, Attribute attr) {
		String[] result = null;
		Collection<String> intermediate = value; 
		for (AttributeValueTransformer transformer : transformerList) {
			if(intermediate == null) {
				intermediate = Arrays.asList(result);
			}
			if(transformer.isMassTransformationPreferred()) {
				result = transformer.getAllValues(intermediate, attr);
			} else {
				result = intermediate.stream()
						.map(one -> transformer.getValue(one, attr))
						.toArray(String[]::new);
			}
			intermediate = null;
		}
		return result;
	}

	@Override
	public String[] getAllValues(Map<String, String> value, Attribute attr) {
		String[] result = null;
		Collection<String> intermediate = null; 
		for (AttributeValueTransformer transformer : transformerList) {
			if(result == null) {
				// first transformer has to reduce map to array
				result = transformer.getAllValues(value, attr);
			} else {
				if(intermediate == null) {
					intermediate = Arrays.asList(result);
				}
				if(transformer.isMassTransformationPreferred()) {
					result = transformer.getAllValues(intermediate, attr);
				} else {
					result = intermediate.stream()
							.map(one -> transformer.getValue(one, attr))
							.toArray(String[]::new);
				}
				intermediate = null;
			}
		}
		return result;
	}

	public List<AttributeValueTransformer> getTransformerList() {
		return transformerList;
	}

	public void setTransformerList(List<AttributeValueTransformer> transformerList) {
		this.transformerList = transformerList;
	}

}
