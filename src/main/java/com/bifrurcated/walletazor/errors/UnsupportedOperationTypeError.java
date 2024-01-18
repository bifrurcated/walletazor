package com.bifrurcated.walletazor.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UnsupportedOperationTypeError extends ResponseStatusException {
    public UnsupportedOperationTypeError(String operationType) {
        super(HttpStatus.BAD_REQUEST, "operation type is not supported: " + operationType);
    }
}
