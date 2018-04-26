package backend;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        //websocket stomp 接入点 前端对接： var socket = new SockJS(sockJSUrl); stompClient = Stomp.over(socket);
        //打成war时，前端对接似乎需要完整接入点url
        registry.addEndpoint("/biye-websocket")
                .setAllowedOrigins("*") //开放跨域（TODO 这并不安全）
                .withSockJS();
    }

}