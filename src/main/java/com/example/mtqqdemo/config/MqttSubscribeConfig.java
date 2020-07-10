package com.example.mtqqdemo.config;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * mqtt subscribe config
 */
@Slf4j
@Configuration
@IntegrationComponentScan
@ConditionalOnProperty(value = "spring.mqtt.subscribe.enabled", havingValue = "true")
public class MqttSubscribeConfig {

    @Value("${spring.mqtt.subscribe.username}")
    private String username;

    @Value("${spring.mqtt.subscribe.password}")
    private String password;

    @Value("${spring.mqtt.subscribe.url}")
    private String hostUrl;

//    @Value("${spring.mqtt.subscribe.client.id}")
//    private String clientId;

    @Value("${spring.mqtt.subscribe.default.topic}")
    private String defaultTopic;

    @Value("${spring.mqtt.subscribe.completionTimeout}")
    private int completionTimeout;

    @Bean
    public MqttConnectOptions getReceiverMqttConnectOptions(){
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());
        List<String> hostList = Arrays.asList(hostUrl.trim().split(","));
        String[] serverURIs = new String[hostList.size()];
        hostList.toArray(serverURIs);
        mqttConnectOptions.setServerURIs(serverURIs);
        mqttConnectOptions.setKeepAliveInterval(2000);
        return mqttConnectOptions;
    }

    @Bean
    public MqttPahoClientFactory receiverMqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(getReceiverMqttConnectOptions());
        return factory;
    }

    /**
     * 接收通道
     * @return MessageChannel
     */
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    /**
     * 配置client,监听的topic
     * @return MessageProducer
     */
    @Bean
    public MessageProducer inbound() {
        List<String> topicList = Arrays.asList(defaultTopic.trim().split(","));
        String[] topics = new String[topicList.size()];
        topicList.toArray(topics);

        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(UUID.randomUUID().toString(),
                        receiverMqttClientFactory(),
                        topics);
        adapter.setCompletionTimeout(completionTimeout);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(0);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    /**
     * 通过通道获取数据
     * @return MessageHandler
     */
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                String topic = message.getHeaders().get("mqtt_receivedTopic").toString();
                String msg = message.getPayload().toString();
                log.info("\n----------------------------START---------------------------\n" +
                        "接收到订阅消息:\ntopic:" + topic + "\nmessage:" + msg +
                        "\n-----------------------------END----------------------------");
            }
        };
    }
}
