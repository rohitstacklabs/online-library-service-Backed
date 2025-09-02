package com.online_library_service.kafka;

import com.online_library_service.dto.UserExpiryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MembershipEventProducer {

    @Qualifier("userExpiryKafkaTemplate")
    private final KafkaTemplate<String, UserExpiryEvent> kafkaTemplate;

    private static final String TOPIC = "membership.expired";

    public void publishExpiryEvent(UserExpiryEvent event) {
        log.info("Publishing expiry event for userId={} email={}", event.getUserId(), event.getEmail());

        kafkaTemplate.send(TOPIC, event.getUserId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null && result != null) {
                        RecordMetadata metadata = result.getRecordMetadata();
                        log.debug("✅ Published event for userId {} to partition {} offset {}",
                                event.getUserId(), metadata.partition(), metadata.offset());
                    } else if (ex != null) {
                        log.error("❌ Failed to publish event for userId {}", event.getUserId(), ex);
                    }
                });
    }
}
