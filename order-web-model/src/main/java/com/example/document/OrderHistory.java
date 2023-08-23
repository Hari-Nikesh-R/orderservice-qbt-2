package com.example.document;

import com.example.request.CustomerDetailRequest;
import com.example.response.OrderStatus;
import com.example.response.ProductResponse;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@Document(collection = "order_history")
public class OrderHistory {
    @Id
    private String orderId;
    private List<ProductResponse> availableProduct;
    private OrderStatus orderStatus;
    private double totalOrder;
    private Date createdDate;
    private String email;
    @CreatedBy
    private CustomerDetailRequest orderedCustomerDetail;
}
