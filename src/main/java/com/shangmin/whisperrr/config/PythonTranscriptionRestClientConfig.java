package com.shangmin.whisperrr.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Pooled Apache HttpClient 5 wired into a dedicated {@link RestClient} for the Python transcription
 * upstream.
 */
@Configuration
public class PythonTranscriptionRestClientConfig {

  private static final Logger logger =
      LoggerFactory.getLogger(PythonTranscriptionRestClientConfig.class);

  @Bean(name = "pythonTranscriptionClientHttpRequestFactory")
  ClientHttpRequestFactory pythonTranscriptionClientHttpRequestFactory(
      @Value("${whisperrr.service.connect-timeout:30000}") int connectTimeout,
      @Value("${whisperrr.service.read-timeout:60000}") int readTimeout) {

    RequestConfig requestConfig =
        RequestConfig.custom()
            .setConnectTimeout(Timeout.ofMilliseconds(connectTimeout))
            .setResponseTimeout(Timeout.ofMilliseconds(readTimeout))
            .build();

    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setMaxTotal(20);
    connectionManager.setDefaultMaxPerRoute(10);

    CloseableHttpClient httpClient =
        HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .setKeepAliveStrategy((response, context) -> TimeValue.ofSeconds(60))
            .evictIdleConnections(TimeValue.ofSeconds(30))
            .build();

    logger.info(
        "Python transcription HTTP pool (max={}, per-route={})",
        connectionManager.getMaxTotal(),
        connectionManager.getDefaultMaxPerRoute());

    return new HttpComponentsClientHttpRequestFactory(httpClient);
  }

  @Bean(name = "pythonTranscriptionRestClient")
  RestClient pythonTranscriptionRestClient(
      @Qualifier("pythonTranscriptionClientHttpRequestFactory")
          ClientHttpRequestFactory pythonTranscriptionClientHttpRequestFactory,
      @Value("${whisperrr.service.url}") String pythonServiceUrl) {
    return RestClient.builder()
        .requestFactory(pythonTranscriptionClientHttpRequestFactory)
        .baseUrl(pythonServiceUrl)
        .build();
  }
}
