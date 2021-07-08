package com.example.demo.core.hibernate.id;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@ConfigurationProperties(SnowflakeProperties.PREFIX)
@Data
@Slf4j
public class SnowflakeProperties {

	public static final String PREFIX = "hibernate-buddy.snowflake";

	@Autowired
	private Environment env;

	private int workerId;

	private int workerIdBits = 8;

	private int sequenceBits = 10;

	@PostConstruct
	private void init() throws Exception {
		if (!env.containsProperty(PREFIX + ".worker-id")) {
			Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
			while (e.hasMoreElements()) {
				NetworkInterface n = e.nextElement();
				Enumeration<InetAddress> ee = n.getInetAddresses();
				while (ee.hasMoreElements()) {
					InetAddress addr = ee.nextElement();
					if (addr.isLoopbackAddress())
						continue;
					if (addr.isSiteLocalAddress() && addr instanceof Inet4Address) {
						workerId = addr.getAddress()[3];
						if (workerId < 0)
							workerId += 256;
						log.info(
								"Extract snowflake workerId {} from host address {}, please configure {}.worker-id if multiple instances running in the same host",
								workerId, addr.getHostAddress(), PREFIX);
						return;
					}
				}
			}
			log.warn("Please configure {}.worker-id if multiple instances running", PREFIX);
		}
	}

	public Snowflake build() {
		return new Snowflake(workerId, workerIdBits, sequenceBits);
	}

}
