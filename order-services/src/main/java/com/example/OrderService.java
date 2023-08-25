package com.example;

import com.example.request.OrderRequest;
import com.example.response.BaseResponse;
import com.example.response.OrderResponse;
import com.example.response.ProductResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface OrderService {
    ResponseEntity<BaseResponse<OrderResponse>> getOrder(String param);
    ResponseEntity<BaseResponse<List<OrderResponse>>> getAllOrder();
 }
