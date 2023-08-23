package com.example;

import com.example.document.OrderHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderHistoryRepository extends MongoRepository<OrderHistory, String> {
    Optional<OrderHistory> findByEmail(String email);
    Optional<OrderHistory> findByOrderId(String orderId);

    List<OrderHistory> findAllByCreatedDate(Date requestDate);
}
