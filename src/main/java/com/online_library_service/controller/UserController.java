package com.online_library_service.controller;

import com.online_library_service.Service.UserService;
import com.online_library_service.dto.MembershipDto;
import com.online_library_service.dto.UserDto;
import com.online_library_service.entity.UserBookHistory;
import com.online_library_service.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.ok("Fetched all users", userService.getAllUsers()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("User fetched successfully", userService.getUserById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {
        return ResponseEntity.ok(ApiResponse.ok("User updated successfully", userService.updateUser(id, userDto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.ok("User deactivated successfully", null));
    }

    @GetMapping("/{id}/membership")
    public ResponseEntity<ApiResponse<String>> checkMembership(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Membership status fetched", userService.checkMembership(id)));
    }

    @PutMapping("/{id}/membership")
    public ResponseEntity<ApiResponse<Void>> extendMembership(@PathVariable Long id, @Valid @RequestBody MembershipDto dto) {
        userService.extendMembership(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Membership extended successfully", null));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<UserBookHistory>>> getUserHistory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("User history fetched", userService.getUserHistory(id)));
    }
}
