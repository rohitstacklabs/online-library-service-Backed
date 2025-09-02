package com.online_library_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.online_library_service.Service.NotificationService;
import com.online_library_service.Service.NotificationWebSocketHandler;
import com.online_library_service.entity.User;
import com.online_library_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookEventConsumer {

	private final NotificationService notificationService;
	private final NotificationWebSocketHandler notificationWebSocketHandler;
	private final UserRepository userRepository;
	private final ObjectMapper objectMapper;
	private final int pageSize = 1000;

	@KafkaListener(topics = "book.events", groupId = "book-notification-group", concurrency = "3")
	public void handleBookEvent(String payload) {
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> event = objectMapper.readValue(payload, HashMap.class);

			String eventType = (String) event.get("eventType");
			String title = (String) event.get("title");
			String author = (String) event.get("author");
			String category = (String) event.get("category");
			String status = (String) event.get("status");

			String subject;
			String body;
			String wsMessage;

			switch (eventType) {
			case "BOOK_ADDED":
				subject = "New Book Added: " + title;
				body = String.format(
						"A new book has been added to the library!\n\nTitle: %s\nAuthor: %s\nCategory: %s\n\nCheck it out in your library account.",
						title, author, category);
				wsMessage = "New book added: " + title;
				break;

			case "BOOK_UPDATED":
				subject = "Book Updated: " + title;
				body = String.format(
						"The book details have been updated.\n\nTitle: %s\nAuthor: %s\nCategory: %s\nStatus: %s", title,
						author, category, status);
				wsMessage = "Book updated: " + title;
				break;

			case "BOOK_STATUS_CHANGED":
				subject = "Book Status Changed: " + title;
				body = String.format("The status of book '%s' has been changed to: %s", title, status);
				wsMessage = "Book status changed: " + title;
				break;

			case "BOOK_DELETED":
				subject = "Book Deleted: " + title;
				body = String.format("The book '%s' has been removed from the library collection.", title);
				wsMessage = "Book deleted: " + title;
				break;

			default:
				log.debug("Skipping unknown event type: {}", eventType);
				return;
			}

			int page = 0;
			List<User> users;
			Pageable pageable;

			do {
				pageable = PageRequest.of(page, pageSize);
				users = userRepository.findByActiveTrue(pageable).getContent();

				if (!users.isEmpty()) {
					String[] emails = users.stream().map(User::getEmail).toArray(String[]::new);
					notificationService.sendBulkEmail(emails, subject, body);

					for (User user : users) {
						try {
							Map<String, String> wsPayload = new HashMap<>();
							wsPayload.put("message", wsMessage);

							String wsJson = objectMapper.writeValueAsString(wsPayload);
							notificationWebSocketHandler.sendNotificationToUser(user.getId(), wsJson);
						} catch (Exception e) {
							log.error("Failed to send WS notification to user {}", user.getId(), e);
						}
					}

					log.info("✅ Sent notifications for {} event: '{}' to {} users in page {}", eventType, title,
							users.size(), page + 1);
				}

				page++;
			} while (!users.isEmpty());

		} catch (Exception ex) {
			log.error("❌ Failed to process book event: {}", payload, ex);
		}
	}
}
