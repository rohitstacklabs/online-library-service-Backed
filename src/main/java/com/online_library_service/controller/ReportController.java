package com.online_library_service.controller;

import com.online_library_service.Service.ReportService;
import com.online_library_service.entity.UserBookHistory;
import com.online_library_service.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/top-category")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getTopCategories() {
        return ResponseEntity.ok(ApiResponse.ok("Top categories fetched successfully", reportService.getTopCategories()));
    }

    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDailyBorrows() {
        return ResponseEntity.ok(ApiResponse.ok("Daily borrows fetched successfully", reportService.getDailyBorrows()));
    }

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMonthlyBorrows() {
        return ResponseEntity.ok(ApiResponse.ok("Monthly borrows fetched successfully", reportService.getMonthlyBorrows()));
    }

    @GetMapping("/yearly")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getYearlyBorrows() {
        return ResponseEntity.ok(ApiResponse.ok("Yearly borrows fetched successfully", reportService.getYearlyBorrows()));
    }

    @GetMapping("/top-books")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTopBorrowedBooks() {
        return ResponseEntity.ok(ApiResponse.ok("Top borrowed books fetched successfully", reportService.getTopBorrowedBooks()));
    }

    @GetMapping("/top-users")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getTopActiveUsers() {
        return ResponseEntity.ok(ApiResponse.ok("Top active users fetched successfully", reportService.getTopActiveUsers()));
    }

    @GetMapping("/currently-borrowed")
    public ResponseEntity<ApiResponse<List<UserBookHistory>>> getCurrentlyBorrowed() {
        return ResponseEntity.ok(ApiResponse.ok("Currently borrowed books fetched successfully", reportService.getCurrentlyBorrowed()));
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<UserBookHistory>>> getOverdueBooks() {
        return ResponseEntity.ok(ApiResponse.ok("Overdue books fetched successfully", reportService.getOverdueBooks()));
    }

    @GetMapping("/user/{userId}/total-borrows")
    public ResponseEntity<ApiResponse<Long>> getUserTotalBorrows(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok("Total borrows for user fetched successfully", reportService.getUserTotalBorrows(userId)));
    }

    @GetMapping("/book-trends")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getBookBorrowTrends() {
        return ResponseEntity.ok(ApiResponse.ok("Book borrow trends fetched successfully", reportService.getBookBorrowTrends()));
    }

    @GetMapping("/category-range")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCategoryCountsInRange(@RequestParam LocalDate start,
                                                                                          @RequestParam LocalDate end) {
        return ResponseEntity.ok(ApiResponse.ok("Category counts in range fetched successfully", reportService.getCategoryCountsInRange(start, end)));
    }
}
