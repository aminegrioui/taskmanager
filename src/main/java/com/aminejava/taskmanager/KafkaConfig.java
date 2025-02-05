package com.aminejava.taskmanager;

import com.aminejava.taskmanager.dto.user.UserRegisterDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Log
@RequiredArgsConstructor
public class KafkaConfig {


    private final KafkaTemplate<String, UserRegisterDto> kafkaTemplate;

    @Bean
    public NewTopic vorgangTopic() {
        return TopicBuilder.name("task-manager").build();
    }

    public void sendEvent(UserRegisterDto event) {
        log.info("Message sent: Dto : " + event);
        kafkaTemplate.send("task-manager", event);
    }


    @KafkaListener(topics = "task-manager", groupId = "task-manager-group")
    public void consume(UserRegisterDto EmailDto) {
        log.info("Message received: Dto : " + EmailDto);
    }
}
