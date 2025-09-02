package com.online_library_service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.online_library_service.entity.Book;
import com.online_library_service.enums.BookStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookEventProducer {

    @Qualifier("stringKafkaTemplate")
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.book-events}")
    private String topic;

    private final int maxRetries = 3;
    private final long retryDelay = 1000L;

    public void publishBookAddedEvent(Book book) {
        publishBookEvent("BOOK_ADDED", book, null, 0);
    }

    public void publishBookUpdatedEvent(Book book) {
        publishBookEvent("BOOK_UPDATED", book, null, 0);
    }

    public void publishBookStatusChangedEvent(Book book, BookStatus newStatus) {
        publishBookEvent("BOOK_STATUS_CHANGED", book, newStatus, 0);
    }

    public void publishBookDeletedEvent(Book book) {
        publishBookEvent("BOOK_DELETED", book, null, 0);
    }

    private void publishBookEvent(String eventType, Book book, BookStatus newStatus, int attempt) {
        if (attempt >= maxRetries) {
            log.error("❌ Failed to publish {} event for book {} after {} attempts", eventType, book.getId(), maxRetries);
            return;
        }

        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", eventType);
            event.put("bookId", book.getId());
            event.put("title", book.getTitle());
            event.put("author", book.getAuthor());
            event.put("category", book.getCategory());
            event.put("status", newStatus != null ? newStatus.name() : book.getStatus().name());

            String payload = objectMapper.writeValueAsString(event);
            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(topic, book.getId().toString(), payload);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("✅ Published {} event for book {}: {}", eventType, book.getId(), payload);
                } else {
                    log.warn("⚠️ Failed to publish {} event for book {} on attempt {}: {}",
                            eventType, book.getId(), attempt + 1, ex.getMessage());
                    try {
                        TimeUnit.MILLISECONDS.sleep(retryDelay);
                        publishBookEvent(eventType, book, newStatus, attempt + 1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("Retry interrupted for {} event, book {}", eventType, book.getId(), e);
                    }
                }
            });
        } catch (JsonProcessingException e) {
            log.error("❌ Failed to serialize {} event for book {}", eventType, book.getId(), e);
        }
    }
}
