package com.example;

import com.example.response.BaseResponse;
import com.example.response.BillResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface BillService {
    BaseResponse<BillResponse> generateBill(String email, HttpServletResponse response);
    ResponseEntity<?> generateDailyPurchaseBillPdf(String requestDate, HttpServletResponse response);
}
