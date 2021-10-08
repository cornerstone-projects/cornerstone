package io.cornerstone.core.sequence.simple;

import io.cornerstone.core.sequence.SimpleSequence;
import io.cornerstone.core.util.NumberUtils;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

public abstract class AbstractSimpleSequence implements SimpleSequence, InitializingBean, BeanNameAware {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	@Setter
	private String sequenceName;

	private String beanName;

	@Getter
	@Setter
	private int paddingLength = 4;

	public String getSequenceName() {
		return StringUtils.hasLength(this.sequenceName) ? this.sequenceName : this.beanName;
	}

	@Override
	public String nextStringValue() {
		return NumberUtils.format(nextIntValue(), this.paddingLength);
	}

	@Override
	public void setBeanName(String beanName) {
		if (StringUtils.hasLength(beanName)) {
			if (beanName.endsWith("SimpleSequence")) {
				beanName = beanName.substring(0, beanName.length() - "SimpleSequence".length());
			}
			else if (beanName.endsWith("Sequence")) {
				beanName = beanName.substring(0, beanName.length() - "Sequence".length());
			}
			else if (beanName.endsWith("Seq")) {
				beanName = beanName.substring(0, beanName.length() - "Seq".length());
			}
			this.beanName = beanName;
		}
	}

}
