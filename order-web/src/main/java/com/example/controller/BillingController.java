package com.example.controller;


import com.example.BillService;
import com.example.response.BaseResponse;
import com.example.response.BillResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.example.helper.Constants.*;
import static com.example.helper.Urls.BILL;
import static com.example.helper.Urls.ORDER_ID_PARAM;

@RestController
@RequestMapping(value = BILL)
public class BillingController {

    @Autowired
    private BillService billService;

    @GetMapping(value = ORDER_ID_PARAM)
    public BaseResponse<BillResponse> getBill(@PathVariable(EMAIL) String email, HttpServletResponse response) {
        return billService.generateBill(email, response);
    }

    @GetMapping
    public ResponseEntity<?> getDailyPurchaseBillPdf(@RequestParam(REQUESTED_DATE) String requestDate, HttpServletResponse response) {
        return billService.generateDailyPurchaseBillPdf(requestDate, response);
    }

}
