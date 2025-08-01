package com.ikeda.authuser.consumer;

import com.ikeda.authuser.dtos.PaymentEventRecordDto;
import com.ikeda.authuser.enums.PaymentControl;
import com.ikeda.authuser.enums.RoleType;
import com.ikeda.authuser.enums.UserType;
import com.ikeda.authuser.models.RoleModel;
import com.ikeda.authuser.models.UserModel;
import com.ikeda.authuser.services.RoleService;
import com.ikeda.authuser.services.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class PaymentConsumer {

    Logger logger = LogManager.getLogger(PaymentConsumer.class);

    final UserService userService;
    final RoleService roleService;

    public PaymentConsumer(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "${ikeda.broker.queue.paymentEventQueue.name}", durable = "true"),
            exchange = @Exchange(value = "${ikeda.broker.exchange.paymentEvent}", type = ExchangeTypes.FANOUT, ignoreDeclarationExceptions = "true")
            )
    )
    public void listenPaymentEvent(@Payload PaymentEventRecordDto paymentEventRecordDto){
        var userModel = userService.findById(paymentEventRecordDto.userId()).get();
        var roleModel = roleService.findByRoleName(RoleType.ROLE_OPERATIONAL);

        switch (PaymentControl.valueOf(paymentEventRecordDto.paymentControl())){
            case EFFECTED -> {
                if (userModel.getUserType().equals(UserType.USER)){
                    userModel.setUserType(UserType.OPERATIONAL);
                    userModel.getRoles().add(roleModel);
                    userService.updateUserByPaymentEvents(userModel);
                }
            }
            case REFUSED -> {
                if (userModel.getUserType().equals(UserType.OPERATIONAL)){
                    userModel.setUserType(UserType.USER);
                    userModel.getRoles().add(roleModel);
                    userService.updateUserByPaymentEvents(userModel);
                }
            }
            case ERROR -> {
                logger.error("Payment with ERROR userId: {} ", paymentEventRecordDto.userId());
            }
        }
    }
}
