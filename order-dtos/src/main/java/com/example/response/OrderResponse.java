package com.example.response;

import com.example.request.CustomerDetailRequest;
import lombok.Data;

import java.util.List;

@Data
public class OrderResponse {
    private double totalOrder;
    private CustomerDetailRequest orderedCustomerDetail;
    private OrderStatus orderStatus;
    private String orderId;
    private List<ProductResponse> availableProduct;
}
