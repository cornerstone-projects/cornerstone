package io.cornerstone.core.tracing;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("management.otlp.tracing")
public class OtlpProperties {

	/**
	 * URL to the OTel collector's HTTP API.
	 */
	private String endpoint = "http://localhost:4318/v1/traces";

	/**
	 * Call timeout for the OTel Collector to process an exported batch of data. This
	 * timeout spans the entire call: resolving DNS, connecting, writing the request body,
	 * server processing, and reading the response body. If the call requires redirects or
	 * retries all must complete within one timeout period.
	 */
	private Duration timeout = Duration.ofSeconds(10);

	/**
	 * The method used to compress the payload.
	 */
	private Compression compression = Compression.NONE;

	/**
	 * Custom HTTP headers you want to pass to the collector, for example auth headers.
	 */
	private Map<String, String> headers = new HashMap<>();

	public String getEndpoint() {
		return this.endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public Duration getTimeout() {
		return this.timeout;
	}

	public void setTimeout(Duration timeout) {
		this.timeout = timeout;
	}

	public Compression getCompression() {
		return this.compression;
	}

	public void setCompression(Compression compression) {
		this.compression = compression;
	}

	public Map<String, String> getHeaders() {
		return this.headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	enum Compression {

		/**
		 * Gzip compression.
		 */
		GZIP,

		/**
		 * No compression.
		 */
		NONE

	}

}
