package io.cornerstone.core.logging;

import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.junit.jupiter.EnabledIf;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
public class LoggingTests {

	@Test
	@EnabledIf("#{T(java.lang.Thread).currentThread().getName()=='main'}")
	void testConsolePatternLayout(CapturedOutput output) {
		Logger logger = LoggerFactory.getLogger(getClass());
		String message = "this is a test for lookup ${sys:user.home}";
		logger.error(message);
		String log = output.toString();
		// remove ANSI Control chars
		log = log.replaceAll("\u001B\\[[\\d;]*[^\\d;]", "").trim();
		String suffix = Thread.currentThread().getName()
				+ "                                         i.c.c.l.LoggingTests ERROR " + message;
		assertThat(log).endsWith(suffix);
		String datetimePattern = "yyyy-MM-dd HH:mm:ss,SSS";
		String prefix = log.substring(0, datetimePattern.length());
		assertThat(DateTimeFormatter.ofPattern(datetimePattern).parse(prefix)).isNotNull();
	}

}
