package com.telekom.azureaihackathon.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    @Value("${azure.url}")
    private String azureUrl;

    @Value("${azure.openApiKey}")
    private String azureOpenApiKey;

    @Value("${proxy.host}")
    private String proxyHost;

    @Value("${proxy.port}")
    private int proxyPort;

    @Value("${proxy.enabled}")
    private boolean enableProxy;

    @Value("${jira.userName}")
    private String jiraUserName;
    @Value("${jira.password}")
    private String jiraPassword;
    @Value("${jira.baseUrl}")
    private String jiraBaseUrl;


    @Bean(name = "gptWebClient")
    public WebClient internalWebClient() {
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)).build();

        return WebClient.builder()
                .baseUrl(azureUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("api-key", azureOpenApiKey)
                .clientConnector(getReactorClientHttpConnector())
                .exchangeStrategies(exchangeStrategies)
                .build();
    }

    private ClientHttpConnector getReactorClientHttpConnector() {
        HttpClient httpClient = HttpClient.create()
                .tcpConfiguration(client ->
                        client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                                        .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)))
                                .proxy(proxy -> proxy.type(ProxyProvider.Proxy.HTTP)
                                        .host(proxyHost)
                                        .port(proxyPort)));

        HttpClient httpClientNoProxy = HttpClient.create();
        return enableProxy ? new ReactorClientHttpConnector(httpClient) : new ReactorClientHttpConnector(httpClientNoProxy);
    }

    @Bean(name = "jiraWebClient")
    public WebClient JiraTicketService() {
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)).build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(jiraUserName, jiraPassword);
        return WebClient.builder()
                .baseUrl(jiraBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, headers.getFirst(HttpHeaders.AUTHORIZATION))
                .clientConnector(getReactorClientHttpConnector())
                .exchangeStrategies(exchangeStrategies)
                .build();
    }

}
