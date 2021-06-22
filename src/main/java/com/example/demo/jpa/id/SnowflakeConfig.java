package com.example.demo.jpa.id;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Configuration
@ConfigurationProperties("snowflake")
@Data
@Slf4j
public class SnowflakeConfig {

	@Autowired
	private Environment env;

	private int workerId;

	private int workerIdBits = 8;

	private int sequenceBits = 10;

	@PostConstruct
	private void init() throws Exception {
		if (!env.containsProperty("snowflake.worker-id")) {
			Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
			while (e.hasMoreElements()) {
				NetworkInterface n = e.nextElement();
				Enumeration<InetAddress> ee = n.getInetAddresses();
				while (ee.hasMoreElements()) {
					InetAddress addr = ee.nextElement();
					if (addr instanceof Inet4Address) {
						byte[] address = addr.getAddress();
						if (address[0] == 127 || address[0] == 169 && address[1] == 254)
							continue;
						workerId = address[3];
						log.info(
								"Extract snowflake workerId {} from host address {}, please configure snowflake.worker-id if multiple instances running in the same host",
								workerId, addr.getHostAddress());
						return;
					}
				}
			}
			log.warn("Please configure snowflake.worker-id if multiple instances running");
		}
	}

	public Snowflake build() {
		return new Snowflake(workerId, workerIdBits, sequenceBits);
	}

}
