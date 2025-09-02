package com.online_library_service.repository;

import com.online_library_service.entity.UserBookHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface UserBookHistoryRepository extends JpaRepository<UserBookHistory, Long> {

    List<UserBookHistory> findByUserId(Long userId);

    @Query("SELECT h.book.category, COUNT(h) " +
           "FROM UserBookHistory h GROUP BY h.book.category ORDER BY COUNT(h) DESC")
    List<Object[]> findCategoryCounts();

    @Query("SELECT COUNT(h) FROM UserBookHistory h")
    long findTotalBorrows();

    @Query("SELECT h.takenDate, COUNT(h) " +
           "FROM UserBookHistory h GROUP BY h.takenDate ORDER BY h.takenDate DESC")
    List<Object[]> findDailyBorrows();

    @Query("SELECT FUNCTION('YEAR', h.takenDate), FUNCTION('MONTH', h.takenDate), COUNT(h) " +
           "FROM UserBookHistory h " +
           "GROUP BY FUNCTION('YEAR', h.takenDate), FUNCTION('MONTH', h.takenDate) " +
           "ORDER BY FUNCTION('YEAR', h.takenDate) DESC, FUNCTION('MONTH', h.takenDate) DESC")
    List<Object[]> findMonthlyBorrows();

    @Query("SELECT FUNCTION('YEAR', h.takenDate), COUNT(h) " +
           "FROM UserBookHistory h " +
           "GROUP BY FUNCTION('YEAR', h.takenDate) " +
           "ORDER BY FUNCTION('YEAR', h.takenDate) DESC")
    List<Object[]> findYearlyBorrows();

    @Query("SELECT h.book.title, COUNT(h) " +
           "FROM UserBookHistory h GROUP BY h.book.title ORDER BY COUNT(h) DESC")
    List<Object[]> findTopBorrowedBooks();

    @Query("SELECT h.user.name, COUNT(h) " +
           "FROM UserBookHistory h GROUP BY h.user.name ORDER BY COUNT(h) DESC")
    List<Object[]> findTopActiveUsers();

    @Query("SELECT h FROM UserBookHistory h WHERE h.returnedDate IS NULL")
    List<UserBookHistory> findCurrentlyBorrowed();

    @Query("SELECT h.book.category, COUNT(h) " +
           "FROM UserBookHistory h WHERE h.takenDate BETWEEN :start AND :end " +
           "GROUP BY h.book.category ORDER BY COUNT(h) DESC")
    List<Object[]> findCategoryCountsInRange(LocalDate start, LocalDate end);

    List<UserBookHistory> findByDueDateBeforeAndReturnedDateIsNull(LocalDate date);

    @Query("SELECT COUNT(h) FROM UserBookHistory h WHERE h.user.id = :userId")
    long findUserTotalBorrows(Long userId);

    @Query("SELECT h.book.title, h.takenDate, COUNT(h) " +
           "FROM UserBookHistory h GROUP BY h.book.title, h.takenDate ORDER BY h.takenDate DESC")
    List<Object[]> findBookBorrowTrends();
}
