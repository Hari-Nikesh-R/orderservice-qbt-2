package com.example;


import com.example.document.OrderHistory;
import com.example.helper.PdfUtils;
import com.example.request.EmailRequest;
import com.example.request.OrderRequest;
import com.example.request.PurchaseRequest;
import com.example.response.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Override
    public OrderResponse createOrder(OrderRequest orderRequest) {
        try {
            if (Arrays.stream(productQuantityCheck(orderRequest.getAvailableProduct())).anyMatch(ProductQuantityCheckResponse::isAvailable)) {
                reduceStockFromInventory(orderRequest);
                OrderHistory orderHistory = new OrderHistory();
                BeanUtils.copyProperties(orderRequest, orderHistory);
                orderHistory.setCreatedDate(formatDate(new Date()));
                OrderHistory order = orderHistoryRepository.save(orderHistory);
                List<BillResponse> billResponseList = new ArrayList<>();
                billResponseList.add(mapper.convertValue(orderHistory, BillResponse.class));
                EmailRequest emailRequest = constructEmailRequest(PdfUtils.generatePurchaseHistoryPDF(billResponseList, false), orderHistory.getOrderedCustomerDetail().getEmail());
                confirmOrder(orderHistory.getEmail());
                mailService.sendEmail(emailRequest);
                OrderResponse orderResponse = new OrderResponse();
                BeanUtils.copyProperties(order, orderResponse);
                return orderResponse;
            }
            else {
                log.warn("Product was out of stock");
                throw new OutOfQuantityException();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            log.error(exception.fillInStackTrace().getLocalizedMessage());
            return null;
        }
    }

    @Override
    public ResponseEntity<BaseResponse<List<ProductResponse>>> getOrder(String param) {
        try {
            Optional<OrderHistory> optionalOrderHistory = getOrderHistory(param);
            return optionalOrderHistory.map(orderHistory -> ResponseEntity.ok(new BaseResponse<>(orderHistory.getAvailableProduct(), HttpStatus.OK.value(), null, true))).orElseGet(() -> ResponseEntity.ok(new BaseResponse<>(null, HttpStatus.NO_CONTENT.value(), "NO ORDER FOUND", false)));
        }
        catch (Exception exception) {
            log.error(exception.fillInStackTrace().getLocalizedMessage());
            return ResponseEntity.ok(new BaseResponse<>(null, HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.getMessage(), false));
        }
    }


    private synchronized <T> ProductQuantityCheckResponse[] productQuantityCheck(List<T> productRequest) {
        return restTemplate.postForEntity("http://localhost:8089/product/quantity", productRequest, ProductQuantityCheckResponse[].class).getBody();
    }
    private synchronized Optional<OrderHistory> getOrderHistory(String param){
        if (param.matches("[a-z0-9]+@[a-z]+\\.[a-z]{2,3}")) {
            return orderHistoryRepository.findByEmail(param);
        }
        else {
            return orderHistoryRepository.findByOrderId(param);
        }
    }


    private Date formatDate(Date date) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String strDate = formatter.format(date);
        return new SimpleDateFormat("dd-MM-yyyy").parse(strDate);
    }

    private void reduceStockFromInventory(OrderRequest orderRequest) {
        List<PurchaseRequest> purchaseRequests = new ArrayList<>();
        orderRequest.getAvailableProduct().forEach((product) -> {
            PurchaseRequest purchaseRequest = new PurchaseRequest();
            BeanUtils.copyProperties(product, purchaseRequest);
            purchaseRequests.add(purchaseRequest);
        });
        restTemplate.put("http://localhost:8089/purchase/deductStock", purchaseRequests);
    }

    private void confirmOrder(String email) {
        Optional<OrderHistory> orderHistory = getOrderHistory(email);
        if (orderHistory.isPresent()) {
            OrderHistory order = orderHistory.get();
            order.setOrderStatus(OrderStatus.COMPLETED);
            orderHistoryRepository.save(order);
        }
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
