package io.cornerstone.core.hibernate.id;

import javax.annotation.PostConstruct;

import io.cornerstone.core.Application;
import io.cornerstone.core.util.NumberUtils;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(SnowflakeProperties.PREFIX)
@Data
@Slf4j
public class SnowflakeProperties {

	public static final String PREFIX = "snowflake";

	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	@Autowired
	private Environment env;

	private int workerId;

	private int workerIdBits = 8;

	private int sequenceBits = 10;

	@PostConstruct
	private void init() throws Exception {
		if (!this.env.containsProperty(PREFIX + ".worker-id")) {
			Application.current().ifPresentOrElse(app -> {
				String ip = app.getHostAddress();
				int index = ip.lastIndexOf('.');
				String id;
				if (index > 0) {
					id = ip.substring(index + 1);
					this.workerId = Integer.parseInt(id);
				}
				else {
					// IPv6
					index = ip.lastIndexOf(':');
					id = ip.substring(index + 1);
					id = String.valueOf(NumberUtils.xToDecimal(16, id.toUpperCase()));
					this.workerId = Integer.parseInt(id);
					this.workerIdBits = 16;
					if (this.workerId < 0) {
						this.workerId += 2 << this.workerIdBits;
					}
				}
				log.info(
						"Extract snowflake workerId {} from host address {}, please configure {}.worker-id if multiple instances running in the same host",
						this.workerId, app.getHostAddress(), PREFIX);
			}, () -> log.warn("Please configure {}.worker-id if multiple instances running", PREFIX));
		}
	}

	public Snowflake build() {
		return new Snowflake(this.workerId, this.workerIdBits, this.sequenceBits);
	}

}
