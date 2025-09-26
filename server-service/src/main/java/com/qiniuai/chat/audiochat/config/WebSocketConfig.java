package com.qiniuai.chat.audiochat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.util.List;

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
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                ;
    }

    // 关键：必须配置STOMP消息代理，否则无法处理STOMP帧
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 1. 启用内存代理，支持广播（/topic）和点对点（/queue）消息
        config.enableSimpleBroker(brokerPrefix, "/queue");
        // 2. 配置应用前缀：客户端发送消息需加 /app 前缀（如 /app/voice-call）
        config.setApplicationDestinationPrefixes("/app");
        // 3. 配置用户前缀：点对点消息需加 /user 前缀（如 /user/123/queue/msg）
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> converters) {
        converters.add(new MappingJackson2MessageConverter());
        return true;
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
        // 线程池
        registration.taskExecutor().corePoolSize(4).maxPoolSize(10);
    }
}
