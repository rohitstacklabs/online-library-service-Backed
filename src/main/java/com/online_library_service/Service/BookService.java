package com.online_library_service.Service;

import com.online_library_service.dto.BookDto;
import com.online_library_service.entity.Book;
import com.online_library_service.enums.BookStatus;
import com.online_library_service.kafka.BookEventProducer;
import com.online_library_service.repository.BookRepository;
import com.online_library_service.repository.UserRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

	private final BookRepository bookRepository;
	private final ModelMapper modelMapper;
	private final BookEventProducer bookEventProducer;

	@CachePut(value = "books", key = "#result.id")
	public BookDto addBook(BookDto bookDto) {
		log.debug("Adding new book: {}", bookDto.getTitle());
		Book book = modelMapper.map(bookDto, Book.class);
		book.setStatus(BookStatus.AVAILABLE);
		book.setActive(true);

		Book savedBook = bookRepository.save(book);

		bookEventProducer.publishBookAddedEvent(savedBook);

		return modelMapper.map(savedBook, BookDto.class);
	}

	@Cacheable(value = "books", key = "#category + #author + #name + #status")
	public List<BookDto> getAllBooks(String category, String author, String name, BookStatus status) {
		log.debug("Fetching books with filters");

		Specification<Book> spec = (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.isTrue(root.get("active")));

			if (category != null && !category.isEmpty()) {
				predicates.add(cb.equal(root.get("category"), category));
			}
			if (author != null && !author.isEmpty()) {
				predicates.add(cb.equal(root.get("author"), author));
			}
			if (name != null && !name.isEmpty()) {
				predicates.add(cb.like(cb.lower(root.get("title")), "%" + name.toLowerCase() + "%"));
			}
			if (status != null) {
				predicates.add(cb.equal(root.get("status"), status));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};

		List<Book> books = bookRepository.findAll(spec);
		return books.stream().map(b -> modelMapper.map(b, BookDto.class)).collect(Collectors.toList());
	}

	@Cacheable(value = "books", key = "#id")
	public BookDto getBookById(Long id) {
		log.debug("Fetching book by id: {}", id);
		Book book = bookRepository.findByIdAndActiveTrue(id)
				.orElseThrow(() -> new RuntimeException("Book not found or inactive"));
		return modelMapper.map(book, BookDto.class);
	}

	@CachePut(value = "books", key = "#id")
	public BookDto updateBook(Long id, BookDto bookDto) {
		log.debug("Updating book id: {}", id);
		Book book = bookRepository.findByIdAndActiveTrue(id)
				.orElseThrow(() -> new RuntimeException("Book not found or inactive"));

		if (bookDto.getTitle() != null)
			book.setTitle(bookDto.getTitle());
		if (bookDto.getAuthor() != null)
			book.setAuthor(bookDto.getAuthor());
		if (bookDto.getCategory() != null)
			book.setCategory(bookDto.getCategory());
		if (bookDto.getStatus() != null)
			book.setStatus(bookDto.getStatus());
		if (bookDto.getImageUrl() != null)
			book.setImageUrl(bookDto.getImageUrl());

		Book updatedBook = bookRepository.save(book);

		bookEventProducer.publishBookUpdatedEvent(updatedBook);

		return modelMapper.map(updatedBook, BookDto.class);
	}

	@CachePut(value = "books", key = "#id")
	public void updateBookStatus(Long id, BookStatus status) {
		log.debug("Updating book status id: {}", id);
		Book book = bookRepository.findByIdAndActiveTrue(id)
				.orElseThrow(() -> new RuntimeException("Book not found or inactive"));
		book.setStatus(status);
		bookRepository.save(book);

		bookEventProducer.publishBookStatusChangedEvent(book, status);
	}

	@CacheEvict(value = "books", key = "#id")
	public void deleteBook(Long id) {
		log.debug("Soft deleting book id: {}", id);
		Book book = bookRepository.findByIdAndActiveTrue(id)
				.orElseThrow(() -> new RuntimeException("Book not found or already inactive"));
		book.setActive(false);
		bookRepository.save(book);

		bookEventProducer.publishBookDeletedEvent(book);
	}
}
