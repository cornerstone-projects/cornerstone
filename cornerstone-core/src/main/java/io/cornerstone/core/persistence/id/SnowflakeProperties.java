package io.cornerstone.core.persistence.id;

import java.util.Locale;

import io.cornerstone.core.Application;
import io.cornerstone.core.util.NumberUtils;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import static org.springframework.boot.cloud.CloudPlatform.KUBERNETES;

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

	private int workerId = -1;

	private int workerIdBits = 8;

	private int sequenceBits = 10;

	@PostConstruct
	private void init() {
		if (this.workerId >= 0) {
			// workerId configured
			return;
		}
		Application.current().ifPresentOrElse(app -> {
			if (CloudPlatform.getActive(this.env) == KUBERNETES) {
				String hostName = app.getHostName();
				if (hostName.matches(".+-\\d+$")) {
					String ordinal = hostName.substring(hostName.lastIndexOf('-') + 1);
					this.workerId = Integer.parseInt(ordinal);
					log.info(
							"Autoconfigure snowflake workerId {} from host name {}, please configure {}.worker-id if it's not desired.",
							this.workerId, hostName, PREFIX);
					return;
				}
			}
			String ip = app.getHostAddress();
			int index = ip.lastIndexOf('.');
			String id;
			if (index > 0) {
				id = ip.substring(index + 1);
				this.workerId = Integer.parseInt(id);
				if (this.workerIdBits < 8) {
					if (this.env.containsProperty(PREFIX + ".worker-id-bits")) {
						log.warn("Increase snowflake workerIdBits from {} to 8", this.workerIdBits);
					}
					this.workerIdBits = 8;
				}
			}
			else {
				// IPv6
				index = ip.lastIndexOf(':');
				id = ip.substring(index + 1);
				id = String.valueOf(NumberUtils.xToDecimal(16, id.toUpperCase(Locale.ROOT)));
				this.workerId = Integer.parseInt(id);
				if (this.workerId < 0) {
					this.workerId += 2 << 16;
				}
				if (this.workerIdBits < 16) {
					if (this.env.containsProperty(PREFIX + ".worker-id-bits")) {
						log.warn("Increase snowflake workerIdBits from {} to 16", this.workerIdBits);
					}
					this.workerIdBits = 16;
				}
			}
			log.info(
					"Autoconfigure snowflake workerId {} from host address {}, please configure {}.worker-id if multiple instances running in the same host.",
					this.workerId, app.getHostAddress(), PREFIX);
		}, () -> log.warn("Please configure {}.worker-id if multiple instances running", PREFIX));
	}

	public Snowflake build() {
		return new Snowflake(this.workerId, this.workerIdBits, this.sequenceBits);
	}

}
