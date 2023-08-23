package com.example;

import com.example.request.OrderRequest;
import com.example.response.BaseResponse;
import com.example.response.OrderResponse;
import com.example.response.ProductResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(OrderRequest orderRequest);
    ResponseEntity<BaseResponse<List<ProductResponse>>> getOrder(String param);
 }
