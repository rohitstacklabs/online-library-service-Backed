package com.online_library_service.kafka;

import com.online_library_service.Service.NotificationService;
import com.online_library_service.dto.UserExpiryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MembershipExpiryConsumer {

	private final NotificationService notificationService;

	@KafkaListener(topics = "membership.expired", groupId = "membership-notification-group", concurrency = "3")
	public void handleExpiryEvent(UserExpiryEvent event) {
		log.info("Received expiry event for userId={} email={}", event.getUserId(), event.getEmail());
		try {
			sendExpiryEmailAsync(event);
		} catch (Exception ex) {
			log.error("Failed to process expiry event for userId={} email={}", event.getUserId(), event.getEmail(), ex);
		}
	}

	@Async
	public void sendExpiryEmailAsync(UserExpiryEvent event) {
		String subject = "Your Library Membership has expired";
		String body = String.format(
				"Hi %s,\n\nYour library membership expired on %s. Please renew to continue borrowing books.\n\nRegards,\nLibrary Team",
				event.getName(), event.getMembershipEndDate());

		notificationService.sendEmail(event.getEmail(), subject, body);
		log.info("Notification sent for userId={}", event.getUserId());
	}
}
