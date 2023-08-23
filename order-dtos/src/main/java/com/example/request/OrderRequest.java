package com.example.request;

import com.example.response.OrderStatus;
import com.example.response.ProductResponse;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private @Valid CustomerDetailRequest orderedCustomerDetail;
    private @Valid List<ProductResponse> availableProduct;
    private OrderStatus orderStatus;
    private double totalOrder;
}
