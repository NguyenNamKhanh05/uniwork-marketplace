package com.uniwork.backend.hello;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HelloMessageRepository extends JpaRepository<HelloMessage, Long> {

    List<HelloMessage> findAllByOrderByCreatedAtDesc();
}
