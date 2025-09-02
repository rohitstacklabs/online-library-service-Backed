package com.online_library_service.repository;

import com.online_library_service.entity.Book;
import com.online_library_service.enums.BookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {


    List<Book> findByActiveTrue();

    List<Book> findByCategoryAndActiveTrue(String category);

    List<Book> findByAuthorAndActiveTrue(String author);

    List<Book> findByTitleContainingAndActiveTrue(String title);

    List<Book> findByStatusAndActiveTrue(BookStatus status);

    Optional<Book> findByIdAndActiveTrue(Long id);
}
