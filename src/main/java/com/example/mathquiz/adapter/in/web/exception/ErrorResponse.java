package com.example.mathquiz.adapter.in.web.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ErrorResponse {
    private final long timestamp = System.currentTimeMillis();
    private final int status;
    private final String error;
    private final String message;
}
