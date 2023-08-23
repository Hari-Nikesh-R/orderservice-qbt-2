package com.example.controller;


import com.example.OrderService;
import com.example.request.OrderRequest;
import com.example.response.BaseResponse;
import com.example.response.OrderResponse;
import com.example.response.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.helper.Urls.ORDER;
import static com.example.helper.Urls.PARAM;


@RestController
@RequestMapping(value = ORDER)
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping
    public OrderResponse createOrder(@RequestBody @Valid OrderRequest orderRequest) {
        return orderService.createOrder(orderRequest);
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<ProductResponse>>> getProductByEmail(@RequestParam(PARAM) String param) {
        return orderService.getOrder(param);
    }
}
