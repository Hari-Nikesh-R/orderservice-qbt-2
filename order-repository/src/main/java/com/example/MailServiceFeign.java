package com.example;

import com.example.request.EmailRequest;
import com.example.response.BaseResponse;
import feign.Headers;
import feign.RequestLine;
import org.springframework.web.bind.annotation.RequestBody;

public interface MailServiceFeign {
    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @RequestLine("POST /mail/send")
    BaseResponse<Object> sendMail(@RequestBody EmailRequest emailRequest);
}
