package com.online_library_service.repository;

import com.online_library_service.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Page<User> findByActiveTrue(Pageable pageable);

    List<User> findByActiveTrue();

    Optional<User> findByEmailAndActiveTrue(String email);
}