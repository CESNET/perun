package cz.metacentrum.perun.ldapc.processor.impl;

import cz.metacentrum.perun.ldapc.processor.AttributeProcessor;
import cz.metacentrum.perun.ldapc.processor.EventDispatcher.MessageBeans;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractAttributeProcessor extends AbstractEventProcessor implements AttributeProcessor {

	private int beanFlag;
	private Pattern setPattern, removePattern, allAttrsRemovedPattern, virtualAttrChangePattern;


	public AbstractAttributeProcessor(int beanFlag, Pattern setPattern, Pattern removePattern, Pattern allAttrsRemovedPattern,
	                                  Pattern virtualAttrChangePattern) {
		super();
		this.beanFlag = beanFlag;
		this.setPattern = setPattern;
		this.removePattern = removePattern;
		this.allAttrsRemovedPattern = allAttrsRemovedPattern;
		this.virtualAttrChangePattern = virtualAttrChangePattern;
	}

	@Override
	public void processEvent(String msg, MessageBeans beans) {
		Matcher matcher = setPattern.matcher(msg);
		int mask = MessageBeans.ATTRIBUTE_F | beanFlag;
		if (matcher.find() && (beans.getPresentBeansMask() & mask) == mask) {
			processAttributeSet(msg, beans);
			return;
		}
		matcher = removePattern.matcher(msg);
		mask = MessageBeans.ATTRIBUTEDEF_F | beanFlag;
		if (matcher.find() && (beans.getPresentBeansMask() & mask) == mask) {
			processAttributeRemoved(msg, beans);
			return;
		}
		matcher = virtualAttrChangePattern.matcher(msg);
		mask = MessageBeans.ATTRIBUTE_F | beanFlag;
		if (matcher.find() && (beans.getPresentBeansMask() & mask) == mask) {
			processVirtualAttributeChanged(msg, beans);
		}
		mask = beanFlag;
		matcher = allAttrsRemovedPattern.matcher(msg);
		if (matcher.find() && (beans.getPresentBeansMask() & mask) == mask) {
			processAllAttributesRemoved(msg, beans);
			return;
		}
		// OK - we do not know how to handle this one
	}

	public abstract void processAttributeSet(String msg, MessageBeans beans);

	public abstract void processAttributeRemoved(String msg, MessageBeans beans);

	public abstract void processAllAttributesRemoved(String msg, MessageBeans beans);

	public abstract void processVirtualAttributeChanged(String msg, MessageBeans beans);

}
