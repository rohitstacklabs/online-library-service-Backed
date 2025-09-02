package com.online_library_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.online_library_service.Service.BookService;
import com.online_library_service.dto.BookDto;
import com.online_library_service.enums.BookStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Slf4j
public class BookController {

	private final BookService bookService;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<BookDto> addBookWithImage(@RequestParam("title") String title,
			@RequestParam("author") String author, @RequestParam("category") String category,
			@RequestParam("status") BookStatus status, @RequestParam("image") MultipartFile image) {
		try {
			String imageUrl = saveImage(image);

			BookDto bookDto = new BookDto();
			bookDto.setTitle(title);
			bookDto.setAuthor(author);
			bookDto.setCategory(category);
			bookDto.setStatus(status);
			bookDto.setImageUrl(imageUrl);

			BookDto saved = bookService.addBook(bookDto);
			return ResponseEntity.status(HttpStatus.CREATED).body(saved);
		} catch (IOException e) {
			log.error("Failed to save image", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	private String saveImage(MultipartFile image) throws IOException {
		if (image == null || image.isEmpty())
			return null;
		String original = image.getOriginalFilename() != null ? image.getOriginalFilename() : "file";
		String fileName = UUID.randomUUID().toString() + "_" + original;
		Path filePath = Paths.get("uploads/" + fileName);
		Files.createDirectories(filePath.getParent());
		Files.copy(image.getInputStream(), filePath);
		return "/uploads/" + fileName;
	}

	@GetMapping
	public ResponseEntity<?> getAllBooks(@RequestParam(required = false) String category,
			@RequestParam(required = false) String author, @RequestParam(required = false) String name,
			@RequestParam(required = false) BookStatus status) {
		return ResponseEntity.ok(bookService.getAllBooks(category, author, name, status));
	}

	@GetMapping("/{id}")
	public ResponseEntity<BookDto> getBook(@PathVariable Long id) {
		return ResponseEntity.ok(bookService.getBookById(id));
	}

	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<BookDto> updateBook(@PathVariable Long id,
			@RequestParam(value = "title", required = false) String title,
			@RequestParam(value = "author", required = false) String author,
			@RequestParam(value = "category", required = false) String category,
			@RequestParam(value = "status", required = false) BookStatus status,
			@RequestParam(value = "image", required = false) MultipartFile image) {

		try {
			BookDto existing = bookService.getBookById(id);

			BookDto bookDto = new BookDto();
			bookDto.setTitle(title != null ? title : existing.getTitle());
			bookDto.setAuthor(author != null ? author : existing.getAuthor());
			bookDto.setCategory(category != null ? category : existing.getCategory());
			bookDto.setStatus(status != null ? status : existing.getStatus());

			if (image != null && !image.isEmpty()) {
				String imageUrl = saveImage(image);
				bookDto.setImageUrl(imageUrl);
			} else {
				bookDto.setImageUrl(existing.getImageUrl());
			}

			BookDto updated = bookService.updateBook(id, bookDto);
			return ResponseEntity.ok(updated);
		} catch (IOException e) {
			log.error("Error saving image while updating book id {}", id, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (RuntimeException re) {
			log.error("Error updating book id {}", id, re);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		} catch (Exception ex) {
			log.error("Unexpected error updating book id {}", id, ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PutMapping("/{id}/status")
	public ResponseEntity<Void> updateStatus(@PathVariable Long id, @RequestParam BookStatus status) {
		bookService.updateBookStatus(id, status);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
		bookService.deleteBook(id);
		return ResponseEntity.noContent().build();
	}
}
