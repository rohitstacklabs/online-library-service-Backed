package com.online_library_service.scheduler;

import com.online_library_service.entity.User;
import com.online_library_service.kafka.MembershipEventProducer;
import com.online_library_service.repository.UserRepository;
import com.online_library_service.dto.UserExpiryEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MembershipScheduler {

    private final UserRepository userRepository;
    private final MembershipEventProducer eventProducer;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void checkExpiredMemberships() {
        log.info("Running membership expiration check...");
        LocalDate today = LocalDate.now();

        List<User> activeUsers = userRepository.findByActiveTrue();
        int expiredCount = 0;

        for (User user : activeUsers) {
            if (user.getMembershipEndDate() != null && user.getMembershipEndDate().isBefore(today)) {
                user.setActive(false);
                expiredCount++;

                userRepository.save(user);

                UserExpiryEvent event = new UserExpiryEvent(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getMembershipEndDate(),
                    "MEMBERSHIP_EXPIRED"
                );
                eventProducer.publishExpiryEvent(event);
            }
        }

        log.info("MembershipScheduler finished. Deactivated {} users.", expiredCount);
    }
}
