package com.example;


import com.example.document.OrderHistory;
import com.example.helper.PdfUtils;
import com.example.request.EmailRequest;
import com.example.request.OrderRequest;
import com.example.response.BaseResponse;
import com.example.response.BillResponse;
import com.example.response.OrderResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private static final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"));
    @Autowired
    private OrderHistoryRepository orderHistoryRepository;

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private MailService mailService;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MailServiceFeign mailServiceFeign;

    @KafkaListener(topics = "mytopic", groupId = "mygroup")
    public void createOrder(String orderRequest) {
        try {
            OrderHistory orderHistory = new OrderHistory();
            OrderRequest orderReq = mapper.readValue(orderRequest, OrderRequest.class);
            BeanUtils.copyProperties(orderReq, orderHistory);
            orderHistory.setCreatedDate(formatDate(new Date()));
            orderHistoryRepository.save(orderHistory);
            List<BillResponse> billResponseList = new ArrayList<>();
            billResponseList.add(mapper.convertValue(orderHistory, BillResponse.class));
            EmailRequest emailRequest = constructEmailRequest(PdfUtils.generatePurchaseHistoryPDF(billResponseList, false), orderHistory.getEmail());
            mailService.sendEmail(emailRequest);
        } catch (Exception exception) {
            exception.printStackTrace();
            log.error(exception.fillInStackTrace().getLocalizedMessage());
        }
    }

    @Override
    public ResponseEntity<BaseResponse<OrderResponse>> getOrder(String param) {
        try {
            Optional<OrderHistory> optionalOrderHistory = getOrderHistory(param);
            return optionalOrderHistory.map(orderHistory -> {
                        OrderResponse orderResponse = new OrderResponse();
                        BeanUtils.copyProperties(orderHistory, orderResponse);
                        return ResponseEntity.ok(new BaseResponse<>(orderResponse, HttpStatus.OK.value(), null, true));
                    })
                    .orElseGet(() -> ResponseEntity.ok(new BaseResponse<>(null, HttpStatus.NO_CONTENT.value(), "NO ORDER FOUND", false)));
        } catch (Exception exception) {
            log.error(exception.fillInStackTrace().getLocalizedMessage());
            return ResponseEntity.ok(new BaseResponse<>(null, HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getMessage(), false));
        }
    }

    @Override
    public ResponseEntity<BaseResponse<List<OrderResponse>>> getAllOrder() {
        try {
            return ResponseEntity.ok(new BaseResponse<>(mapper.convertValue(orderHistoryRepository.findAll(), new TypeReference<>() {
            }), HttpStatus.OK.value(), null, true));
        } catch (Exception exception) {
            return ResponseEntity.ok(new BaseResponse<>(null, HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getMessage(), false));
        }
    }

    private synchronized Optional<OrderHistory> getOrderHistory(String param) {
        if (param.matches("[a-z0-9]+@[a-z]+\\.[a-z]{2,3}")) {
            return orderHistoryRepository.findByEmail(param);
        } else {
            return orderHistoryRepository.findByOrderId(param);
        }
    }


    private Date formatDate(Date date) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String strDate = formatter.format(date);
        return new SimpleDateFormat("dd-MM-yyyy").parse(strDate);
    }

    private String currentDateAsString() {
        return calendar.get(Calendar.DATE) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.YEAR);
    }

    private EmailRequest constructEmailRequest(byte[] byteArrayResource, String email) {
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setRecipient(email);
        emailRequest.setSubject("Invoice for Purchase");
        emailRequest.setPdfData(byteArrayResource);
        return emailRequest;
    }
}
