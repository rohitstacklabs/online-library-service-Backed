package com.online_library_service.Service;

import com.online_library_service.entity.Book;
import com.online_library_service.entity.User;
import com.online_library_service.entity.UserBookHistory;
import com.online_library_service.enums.BookStatus;
import com.online_library_service.exception.BadRequestException;
import com.online_library_service.exception.NotFoundException;
import com.online_library_service.repository.BookRepository;
import com.online_library_service.repository.UserBookHistoryRepository;
import com.online_library_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class BorrowService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final UserBookHistoryRepository historyRepository;

    @CacheEvict(value = "reports", key = "'topCategories'")
    public void borrowBook(Long userId, Long bookId) {
        log.debug("User {} borrowing book {}", userId, bookId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        if (!user.isActive()) {
            throw new BadRequestException("User account is deactivated");
        }

        if (LocalDate.now().isAfter(user.getMembershipEndDate())) {
            throw new BadRequestException("Membership expired, please renew");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Book not found with ID: " + bookId));

        if (!book.isActive()) {
            throw new BadRequestException("Book is deactivated");
        }

        if (book.getStatus() != BookStatus.AVAILABLE) {
            throw new BadRequestException("Book is already taken");
        }

        book.setStatus(BookStatus.TAKEN);
        bookRepository.save(book);

        UserBookHistory history = new UserBookHistory();
        history.setUser(user);
        history.setBook(book);
        history.setTakenDate(LocalDate.now());
        historyRepository.save(history);

        log.info("Book {} borrowed successfully by User {}", bookId, userId);
    }

    @CacheEvict(value = "reports", key = "'topCategories'")
    public void returnBook(Long userId, Long borrowId) {
        log.debug("User {} returning borrow record {}", userId, borrowId);

        UserBookHistory history = historyRepository.findById(borrowId)
                .orElseThrow(() -> new NotFoundException("Borrow record not found with ID: " + borrowId));

        if (!history.getUser().getId().equals(userId)) {
            throw new BadRequestException("This borrow record does not belong to the user");
        }

        Book book = history.getBook();
        if (!book.isActive()) {
            throw new BadRequestException("Book is deactivated");
        }

        book.setStatus(BookStatus.AVAILABLE);
        bookRepository.save(book);

        history.setReturnedDate(LocalDate.now());
        historyRepository.save(history);

        log.info("Book {} returned successfully by User {}", book.getId(), userId);
    }
}
