package com.msa4meerkatgramv2auth.global.kafka;

import com.msa4meerkatgramv2auth.domain.auth.event.AuthWithdrawEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AuthEventProducer {
    private final KafkaTemplate<String, AuthWithdrawEvent> kafkaTemplateWithdraw;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AuthWithdrawEvent authWithdrawEvent) {
        kafkaTemplateWithdraw.send(
            "msa4-meerkat.auth.withdraw",
            "auth-withdraw-" + authWithdrawEvent.userId(),
            authWithdrawEvent
        );
    }
}
