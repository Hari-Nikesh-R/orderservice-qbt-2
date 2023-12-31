package com.example.controller;

import com.example.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

@RestControllerAdvice
public class ExceptionController {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<Object> handleMethodArguments(MethodArgumentNotValidException exception) {
        AtomicReference<String> errorMessage = new AtomicReference<>(exception.getMessage());
        exception.getFieldErrors().forEach((fieldError -> {
            errorMessage.set(fieldError.getDefaultMessage());
        }));
        return new BaseResponse<>(null, HttpStatus.BAD_REQUEST.value(), errorMessage.get(), false);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) throws IOException {
        return new BaseResponse<>(null, HttpStatus.BAD_REQUEST.value(), "Invalid request", false);
    }
}
