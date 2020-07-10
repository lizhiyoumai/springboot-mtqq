package com.example.mtqqdemo.controller;

import com.example.mtqqdemo.sender.MqttMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * message test
 */
@RestController
@RequestMapping("/mqtt")
@Slf4j
public class MessageApiController {

    @Resource
    private MqttMessageSender messageSender;

    @PostMapping("publish")
    public void sendMessage(String topic, String message) {
        log.info("\n----------------------------START---------------------------\n" +
                "接收到发布请求:\ntopic:" + topic + "\nmessage:" + message +
                "\n-----------------------------END----------------------------");
        messageSender.send(topic, message);
    }

}
