package com.online_library_service.controller;

import com.online_library_service.Service.BorrowService;
import com.online_library_service.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    @PostMapping("/borrow/{bookId}")
    public ResponseEntity<ApiResponse<Void>> borrowBook(@PathVariable Long userId,
                                                        @PathVariable Long bookId) {
        borrowService.borrowBook(userId, bookId);
        return ResponseEntity.ok(ApiResponse.ok("Book borrowed successfully", null));
    }

    @PostMapping("/return/{borrowId}")
    public ResponseEntity<ApiResponse<Void>> returnBook(@PathVariable Long userId,
                                                        @PathVariable Long borrowId) {
        borrowService.returnBook(userId, borrowId);
        return ResponseEntity.ok(ApiResponse.ok("Book returned successfully", null));
    }
}
