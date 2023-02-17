package io.cornerstone.core.servlet;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import io.cornerstone.core.json.JsonSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.FastByteArrayOutputStream;

public class LoggingClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

	private final Logger logger = LoggerFactory.getLogger("rest");

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		MediaType contentType = request.getHeaders().getContentType();
		if ((body.length > 0) && (contentType != null) && supports(contentType)) {
			String str = new String(body, StandardCharsets.UTF_8);
			str = JsonSanitizer.DEFAULT_INSTANCE.sanitize(str);
			this.logger.info("{} {} \n{}", request.getMethod(), request.getURI(), str);
		}
		else {
			this.logger.info("{} {}", request.getMethod(), request.getURI());
		}
		ClientHttpResponse response = execution.execute(request, body);
		contentType = response.getHeaders().getContentType();
		if ((response.getHeaders().getContentLength() != 0) && supports(contentType)) {
			return new ClientHttpResponse() {

				InputStream is;

				@Override
				public InputStream getBody() throws IOException {
					if (this.is == null) {
						this.is = new ContentCachingInputStream(response,
								LoggingClientHttpRequestInterceptor.this.logger);
					}
					return this.is;
				}

				@Override
				public HttpHeaders getHeaders() {
					return response.getHeaders();
				}

				@Override
				public HttpStatusCode getStatusCode() throws IOException {
					return HttpStatusCode.valueOf(getRawStatusCode());
				}

				@SuppressWarnings("deprecation")
				@Override
				public int getRawStatusCode() throws IOException {
					return response.getRawStatusCode();
				}

				@Override
				public String getStatusText() throws IOException {
					return response.getStatusText();
				}

				@Override
				public void close() {
					if (this.is != null) {
						try {
							this.is.close();
							this.is = null;
						}
						catch (IOException ex) {
							LoggingClientHttpRequestInterceptor.this.logger.error(ex.getMessage(), ex);
						}
					}
					response.close();
				}

			};
		}
		else {
			this.logger.info("Received status {} and content type \"{}\" with length {}",
					response.getStatusCode().value(), contentType, response.getHeaders().getContentLength());
		}
		return response;
	}

	protected boolean supports(MediaType contentType) {
		if (contentType == null) {
			return false;
		}
		return contentType.isCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED)
				|| contentType.isCompatibleWith(MediaType.APPLICATION_JSON)
				|| contentType.isCompatibleWith(MediaType.APPLICATION_XML) || contentType.getType().equals("text");
	}

	private static class ContentCachingInputStream extends FilterInputStream {

		private final MediaType contentType;

		private final Logger logger;

		private FastByteArrayOutputStream cachedContent;

		ContentCachingInputStream(ClientHttpResponse response, Logger logger) throws IOException {
			super(response.getBody());
			this.contentType = response.getHeaders().getContentType();
			int contentLength = (int) response.getHeaders().getContentLength();
			this.cachedContent = new FastByteArrayOutputStream(contentLength >= 0 ? contentLength : 1024);
			this.logger = logger;
		}

		@Override
		public int read() throws IOException {
			int ch = super.read();
			if (ch != -1) {
				this.cachedContent.write(ch);
			}
			return ch;
		}

		@Override
		public int read(byte[] b, final int off, final int len) throws IOException {
			int count = super.read(b, off, len);
			if (count != -1) {
				this.cachedContent.write(b, off, count);
			}
			return count;
		}

		@Override
		public void reset() throws IOException {
			super.reset();
			this.cachedContent.reset();
		}

		@Override
		public void close() throws IOException {
			try {
				super.close();
			}
			finally {
				if (this.cachedContent != null) {
					byte[] bytes = this.cachedContent.toByteArray();
					this.cachedContent = null;
					String str = new String(bytes, StandardCharsets.UTF_8);
					if (this.contentType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
						str = JsonSanitizer.DEFAULT_INSTANCE.sanitize(str);
					}
					this.logger.info("Received:\n{}", str);
				}
			}
		}

	}

}
