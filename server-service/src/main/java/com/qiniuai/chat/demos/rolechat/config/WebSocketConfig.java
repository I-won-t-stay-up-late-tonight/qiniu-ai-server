package com.qiniuai.chat.demos.rolechat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/**
 * WebSocket配置，支持STOMP协议，实现实时语音数据传输
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.websocket.endpoint}")
    private String websocketEndpoint;
    
    @Value("${app.websocket.broker.prefix}")
    private String brokerPrefix;
    
    @Value("${app.audio.chunk-size}")
    private int audioChunkSize;

    /**
     * 注册WebSocket端点
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. 注册端点：/ws（客户端连接地址是 ws://localhost:8080/ws）
        // 2. setAllowedOrigins("*")：允许跨域（开发环境，生产环境需指定具体域名）
        // 3. withSockJS()：支持SockJS降级（兼容不支持WebSocket的浏览器）
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")
                .withSockJS();
    }

    /**
     * 配置消息代理
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单 broker，用于广播和点对点消息
        config.enableSimpleBroker(brokerPrefix, "/queue");
        // 配置应用前缀，客户端发送消息到服务器的路径需要以此为前缀
        config.setApplicationDestinationPrefixes("/app");
        // 配置用户目的地前缀，用于点对点消息
        config.setUserDestinationPrefix("/user");
    }

    /**
     * 配置WebSocket传输选项
     */
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // 设置消息大小限制，适应音频传输
        registration.setMessageSizeLimit(audioChunkSize * 100); // 100个分片大小
        registration.setSendBufferSizeLimit(audioChunkSize * 200);
        registration.setSendTimeLimit(10000);
    }

    /**
     * 配置客户端入站通道
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 可以添加拦截器，用于认证和授权
    }

    /**
     * 配置客户端出站通道
     */
    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        // 可以配置线程池
        registration.taskExecutor().corePoolSize(4).maxPoolSize(10);
    }
}
    