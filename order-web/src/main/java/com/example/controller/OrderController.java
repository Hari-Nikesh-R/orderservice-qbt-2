package com.example.controller;


import com.example.OrderService;
import com.example.response.BaseResponse;
import com.example.response.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.example.helper.Urls.*;


@RestController
@RequestMapping(value = ORDER)
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<BaseResponse<OrderResponse>> getProductByEmail(@RequestParam(PARAM) String param) {
        return orderService.getOrder(param);
    }

    @GetMapping(value = ALL)
    public ResponseEntity<BaseResponse<List<OrderResponse>>> getAllOrder() {
        return orderService.getAllOrder();
    }

}
