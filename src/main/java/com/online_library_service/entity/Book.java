package com.online_library_service.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.online_library_service.enums.BookStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "books")
@Data
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    private BookStatus status;

    private boolean active = true;

    private String imageUrl; 
    
    @CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;
	
	private LocalDateTime updatedAt;

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = null; 
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
