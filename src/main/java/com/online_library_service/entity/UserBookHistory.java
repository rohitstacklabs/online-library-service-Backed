
package com.online_library_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "user_book_history")
@Data
public class UserBookHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull(message = "User is required")
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@NotNull(message = "Book is required")
	@ManyToOne
	@JoinColumn(name = "book_id")
	private Book book;

	@NotNull(message = "Taken date is required")
	@Column(name = "taken_date", nullable = false)
	private LocalDate takenDate;

	@Column(name = "due_date")
	private LocalDate dueDate;

	@Column(name = "returned_date")
	private LocalDate returnedDate;

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