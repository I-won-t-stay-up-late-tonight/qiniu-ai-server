package com.qiniuai.chat.audiochat.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.TimeUnit;

/**
 * Apache HttpClient 5.x 连接池配置，管理与第三方API的连接
 */
@Configuration
public class HttpClientConfig {

    // 连接池配置参数
    private static final int MAX_TOTAL_CONNECTIONS = 30;
    private static final int MAX_CONNECTIONS_PER_ROUTE = 10;
    private static final int CONNECTION_TIMEOUT = 5000; // 5秒
    private static final int SOCKET_TIMEOUT = 15000; // 15秒
    private static final long IDLE_TIMEOUT_SECONDS = 30; // 30秒
    private static final long CLEANUP_INTERVAL_SECONDS = 10; // 10秒

    /**
     * 连接池管理器，负责管理HTTP连接
     */
    @Bean(destroyMethod = "close")
    public PoolingHttpClientConnectionManager connectionManager() {
        // 配置Socket参数
        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(Timeout.ofMilliseconds(SOCKET_TIMEOUT))
                .setTcpNoDelay(true)
                .setSoKeepAlive(true)
                .build();

        // 配置连接参数
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(CONNECTION_TIMEOUT))
                .build();

        // 初始化连接池管理器
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
        connectionManager.setDefaultMaxPerRoute(MAX_CONNECTIONS_PER_ROUTE);
        connectionManager.setDefaultSocketConfig(socketConfig);
        connectionManager.setDefaultConnectionConfig(connectionConfig);

        // 设置连接最大存活时间
        connectionManager.setValidateAfterInactivity(TimeValue.of(5, TimeUnit.MINUTES));
        // 清理空闲和过期连接
        TimeValue idleTime = TimeValue.of(IDLE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        connectionManager.closeIdle(idleTime);
        connectionManager.closeExpired();

        // 启动定时清理任务
        startPeriodicCleanupTask(connectionManager, idleTime);

        return connectionManager;
    }

    /**
     * 启动定时任务，定期清理空闲和过期连接
     */
    private void startPeriodicCleanupTask(PoolingHttpClientConnectionManager connectionManager, TimeValue idleTime) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("http-connection-cleaner-");
        scheduler.initialize();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                connectionManager.closeIdle(idleTime);
                connectionManager.closeExpired();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, CLEANUP_INTERVAL_SECONDS * 1000);
    }

    /**
     * 构建HttpClient实例
     */
    @Bean
    public HttpClient httpClient(PoolingHttpClientConnectionManager connectionManager) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(CONNECTION_TIMEOUT))
                .setConnectionKeepAlive(TimeValue.of(5, TimeUnit.MINUTES))
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(3000))
                .setRedirectsEnabled(false)
                .build();

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .evictIdleConnections(TimeValue.of(IDLE_TIMEOUT_SECONDS, TimeUnit.SECONDS))
                .evictExpiredConnections()
                .build();
    }
}
    