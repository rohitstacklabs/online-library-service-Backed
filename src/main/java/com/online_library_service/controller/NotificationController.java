package com.online_library_service.controller;

import com.online_library_service.Service.NotificationService;
import com.online_library_service.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> sendManualNotification(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String body) {
        notificationService.sendEmail(to, subject, body);
        return ResponseEntity.ok(ApiResponse.ok("Notification sent manually", null));
    }
}
