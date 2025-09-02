package com.online_library_service.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;
    private final int maxRetries = 3;
    private final long retryDelay = 2000L;
    private final int batchSize = 100; 

    
    @Async("taskExecutor")
    public void sendEmail(String to, String subject, String text) {
        log.debug("Preparing to send email to {}", to);

        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(to);
                message.setSubject(subject);
                message.setText(text);

                mailSender.send(message);
                log.info("✅ Email sent to {} successfully on attempt {}", to, attempt + 1);
                break;
            } catch (MailException ex) {
                attempt++;
                log.warn("⚠️ Failed to send email to {} on attempt {}: {}", to, attempt, ex.getMessage());
                if (attempt >= maxRetries) {
                    log.error("❌ Could not send email to {} after {} attempts", to, maxRetries, ex);
                    break;
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(retryDelay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Email retry interrupted for {}", to, e);
                    break;
                }
            }
        }
    }


    @Async("taskExecutor")
    public void sendBulkEmail(String[] recipients, String subject, String text) {
        if (recipients == null || recipients.length == 0) {
            log.warn("No recipients provided for bulk email");
            return;
        }

        for (int i = 0; i < recipients.length; i += batchSize) {
            String[] batch = Arrays.copyOfRange(recipients, i, Math.min(i + batchSize, recipients.length));
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(batch);
                message.setSubject(subject);
                message.setText(text);

                mailSender.send(message);
                log.info("✅ Bulk email sent to {} users in batch {}/{}", 
                        batch.length, 
                        (i / batchSize) + 1, 
                        (int) Math.ceil((double) recipients.length / batchSize));
            } catch (MailException ex) {
                log.error("❌ Failed to send bulk email to batch {}/{} (size: {}): {}", 
                        (i / batchSize) + 1, 
                        (int) Math.ceil((double) recipients.length / batchSize), 
                        batch.length, 
                        ex.getMessage());
            }
        }
    }
}