package com.online_library_service.Service;

import com.online_library_service.entity.UserBookHistory;
import com.online_library_service.exception.NoDataFoundException;
import com.online_library_service.repository.UserBookHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

	private final UserBookHistoryRepository historyRepository;

	@Cacheable(value = "reports", key = "'topCategories'")
	public Map<String, Double> getTopCategories() {
		log.info("Fetching report: Top Categories");
		List<Object[]> rows = historyRepository.findCategoryCounts();
		long total = historyRepository.findTotalBorrows();

		if (total == 0 || rows.isEmpty()) {
			log.warn("No borrow history found for categories");
			throw new NoDataFoundException("No borrow history found for categories.");
		}

		Map<String, Double> percentages = new LinkedHashMap<>();
		for (Object[] r : rows) {
			String category = (String) r[0];
			long count = ((Number) r[1]).longValue();
			double pct = (count * 100.0) / total;
			percentages.put(category, pct);
		}
		return percentages;
	}

	@Cacheable(value = "reports", key = "'dailyBorrows'")
	public List<Map<String, Object>> getDailyBorrows() {
		log.info("Fetching report: Daily Borrows");
		List<Object[]> rows = historyRepository.findDailyBorrows();
		if (rows.isEmpty()) {
			log.warn("No daily borrow data available");
			throw new NoDataFoundException("No daily borrow data available.");
		}

		List<Map<String, Object>> out = new ArrayList<>();
		for (Object[] r : rows) {
			Map<String, Object> m = new HashMap<>();
			m.put("date", r[0]);
			m.put("count", r[1]);
			out.add(m);
		}
		return out;
	}

	@Cacheable(value = "reports", key = "'monthlyBorrows'")
	public List<Map<String, Object>> getMonthlyBorrows() {
		log.info("Fetching report: Monthly Borrows");
		List<Object[]> rows = historyRepository.findMonthlyBorrows();
		if (rows.isEmpty()) {
			log.warn("No monthly borrow data available");
			throw new NoDataFoundException("No monthly borrow data available.");
		}

		List<Map<String, Object>> out = new ArrayList<>();
		for (Object[] r : rows) {
			Map<String, Object> m = new HashMap<>();
			m.put("year", r[0]);
			m.put("month", r[1]);
			m.put("count", r[2]);
			out.add(m);
		}
		return out;
	}

	@Cacheable(value = "reports", key = "'yearlyBorrows'")
	public List<Map<String, Object>> getYearlyBorrows() {
		log.info("Fetching report: Yearly Borrows");
		List<Object[]> rows = historyRepository.findYearlyBorrows();
		if (rows.isEmpty()) {
			log.warn("No yearly borrow data available");
			throw new NoDataFoundException("No yearly borrow data available.");
		}

		List<Map<String, Object>> out = new ArrayList<>();
		for (Object[] r : rows) {
			Map<String, Object> m = new HashMap<>();
			m.put("year", r[0]);
			m.put("count", r[1]);
			out.add(m);
		}
		return out;
	}

	@Cacheable(value = "reports", key = "'topBooks'")
	public List<Map<String, Object>> getTopBorrowedBooks() {
		log.info("Fetching report: Top Borrowed Books");
		List<Object[]> rows = historyRepository.findTopBorrowedBooks();
		if (rows.isEmpty()) {
			log.warn("No top borrowed books data available");
			throw new NoDataFoundException("No top borrowed books data available.");
		}

		List<Map<String, Object>> out = new ArrayList<>();
		for (Object[] r : rows) {
			Map<String, Object> m = new HashMap<>();
			m.put("title", r[0]);
			m.put("count", r[1]);
			out.add(m);
		}
		return out;
	}

	@Cacheable(value = "reports", key = "'topUsers'")
	public List<Map<String, Object>> getTopActiveUsers() {
		log.info("Fetching report: Top Active Users");
		List<Object[]> rows = historyRepository.findTopActiveUsers();
		if (rows.isEmpty()) {
			log.warn("No top active users data available");
			throw new NoDataFoundException("No top active users data available.");
		}

		List<Map<String, Object>> out = new ArrayList<>();
		for (Object[] r : rows) {
			Map<String, Object> m = new HashMap<>();
			m.put("user", r[0]);
			m.put("count", r[1]);
			out.add(m);
		}
		return out;
	}

	public List<UserBookHistory> getCurrentlyBorrowed() {
		log.info("Fetching report: Currently Borrowed Books");
		List<UserBookHistory> list = historyRepository.findCurrentlyBorrowed();
		if (list.isEmpty()) {
			log.warn("No currently borrowed books found");
			throw new NoDataFoundException("No currently borrowed books found.");
		}
		return list;
	}

	public List<Map<String, Object>> getCategoryCountsInRange(LocalDate start, LocalDate end) {
		log.info("Fetching report: Category Counts in range {} - {}", start, end);

		if (start == null || end == null || start.isAfter(end)) {
			log.error("Invalid date range provided: start={} end={}", start, end);
			throw new IllegalArgumentException("Invalid date range provided.");
		}

		List<Object[]> rows = historyRepository.findCategoryCountsInRange(start, end);
		if (rows.isEmpty()) {
			log.warn("No category data available between {} and {}", start, end);
			throw new NoDataFoundException("No category data available in the given range.");
		}

		List<Map<String, Object>> out = new ArrayList<>();
		for (Object[] r : rows) {
			Map<String, Object> m = new HashMap<>();
			m.put("category", r[0]);
			m.put("count", r[1]);
			out.add(m);
		}
		return out;
	}

	public List<UserBookHistory> getOverdueBooks() {
		log.info("Fetching report: Overdue Books");
		List<UserBookHistory> list = historyRepository.findByDueDateBeforeAndReturnedDateIsNull(LocalDate.now());
		if (list.isEmpty()) {
			log.warn("No overdue books found");
			throw new NoDataFoundException("No overdue books found.");
		}
		return list;
	}

	public long getUserTotalBorrows(Long userId) {
		log.info("Fetching report: Total Borrows for user {}", userId);
		long count = historyRepository.findUserTotalBorrows(userId);
		if (count == 0) {
			log.warn("No borrows found for user {}", userId);
			throw new NoDataFoundException("No borrows found for this user.");
		}
		return count;
	}

	public List<Map<String, Object>> getBookBorrowTrends() {
		log.info("Fetching report: Book Borrow Trends");
		List<Object[]> rows = historyRepository.findBookBorrowTrends();
		if (rows.isEmpty()) {
			log.warn("No book borrow trends available");
			throw new NoDataFoundException("No book borrow trends available.");
		}

		List<Map<String, Object>> out = new ArrayList<>();
		for (Object[] r : rows) {
			Map<String, Object> m = new HashMap<>();
			m.put("book", r[0]);
			m.put("date", r[1]);
			m.put("count", r[2]);
			out.add(m);
		}
		return out;
	}
}
