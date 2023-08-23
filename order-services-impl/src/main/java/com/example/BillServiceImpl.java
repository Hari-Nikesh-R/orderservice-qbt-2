package com.example;

import com.example.document.OrderHistory;
import com.example.helper.PdfUtils;
import com.example.response.BaseResponse;
import com.example.response.BillResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class BillServiceImpl implements BillService {

    private static final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));
    @Autowired
    private OrderHistoryRepository orderHistoryRepository;
    @Autowired
    private ObjectMapper mapper;

    @Override
    public BaseResponse<BillResponse> generateBill(String email, HttpServletResponse response) {
        try {
            Optional<OrderHistory> optionalOrderHistory = getOrderHistoryByEmail(email);
            return optionalOrderHistory.map(orderHistory -> {
                String fileName = calendar.get(Calendar.DATE) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.YEAR);
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=" + fileName + "-bill.pdf");
                List<BillResponse> billResponses = new ArrayList<>();
                billResponses.add(mapper.convertValue(orderHistory, BillResponse.class));
                return new BaseResponse<>(mapper.convertValue(optionalOrderHistory.get(), BillResponse.class), HttpStatus.OK.value(), null, PdfUtils.generatePdfAndSave(billResponses, fileName));
            }).orElseGet(() -> new BaseResponse<>(null, HttpStatus.NO_CONTENT.value(), "No Order found", false));
        } catch (Exception exception) {
            return new BaseResponse<>(null, HttpStatus.OK.value(), exception.getMessage(), false);
        }
    }

    @Override
    public ResponseEntity<?> generateDailyPurchaseBillPdf(String requestDate, HttpServletResponse response) {
        try {
            List<OrderHistory> orderHistories = orderHistoryRepository.findAllByCreatedDate(parseStringToDate(requestDate));
            if (orderHistories.isEmpty()) {
                return ResponseEntity.ok(new BaseResponse<>(new ArrayList<>(), HttpStatus.NO_CONTENT.value(), "List is empty", false));
            } else {
                String fileName = calendar.get(Calendar.DATE) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.YEAR);
                List<BillResponse> billResponses = mapper.convertValue(orderHistories, new TypeReference<>() {
                });
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=" + fileName + "-bill.pdf");
               // boolean generated = PdfUtils.generatePdfAndSave(billResponses, fileName, requestDate);
//                todo:  for dynamically receive and download in front end as ByteResourceArray
                byte[] generated = PdfUtils.generatePurchaseHistoryPDF(billResponses, true);
                if (generated.length == 0) {
                    return ResponseEntity.ok(new BaseResponse<>(null, HttpStatus.OK.value(), "Pdf not generated", false));
                }
                else {
                    ByteArrayResource resource = new ByteArrayResource(generated);
                   return ResponseEntity.ok()
                            .contentLength(generated.length)
                            .contentType(MediaType.APPLICATION_PDF)
                            .body(resource);
                }
//                return ResponseEntity.ok(new BaseResponse<>("Generated", HttpStatus.OK.value(), null, generated));
            }
        } catch (Exception exception) {
            return ResponseEntity.ok(new BaseResponse<>(null, HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getMessage(), false));
        }
    }
    private synchronized Optional<OrderHistory> getOrderHistoryByEmail(String email){
        return orderHistoryRepository.findByEmail(email);
    }
    private Date parseStringToDate(String requestDate) throws ParseException {
        return new SimpleDateFormat("dd-MM-yyyy").parse(requestDate);
    }
}
