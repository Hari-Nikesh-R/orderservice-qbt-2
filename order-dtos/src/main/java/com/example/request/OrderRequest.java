package com.example.request;

import com.example.response.OrderStatus;
import com.example.response.ProductResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private @Valid CustomerDetailRequest orderedCustomerDetail;
    private @Valid List<ProductResponse> availableProduct;
    private OrderStatus orderStatus;
    private String orderId;
    private String email;
    private double totalOrder;
}
