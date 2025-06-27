package com.ikeda.authuser.publishers;

import com.ikeda.authuser.dtos.UserEventDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserEventPublisher {

    final RabbitTemplate rabbitTemplate;

    public UserEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Value(value = "ikeda.userevent")
    private String exchangeUserEvent;

    public void publishUserEvent(UserEventDto userEventDto){
        rabbitTemplate.convertAndSend(exchangeUserEvent, "", userEventDto);
    }
}
