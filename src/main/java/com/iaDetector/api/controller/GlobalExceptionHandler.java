package com.iaDetector.api.controller;

import com.iaDetector.api.dto.ErrorDTO;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.concurrent.TimeoutException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDTO> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ErrorDTO("BAD_REQUEST", ex.getMessage(), null));
    }

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ErrorDTO> handleTimeout(TimeoutException ex) {
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(new ErrorDTO("TIMEOUT", ex.getMessage(), null));
    }

    @ExceptionHandler({MaxUploadSizeExceededException.class, FileSizeLimitExceededException.class})
    public ResponseEntity<ErrorDTO> handleMaxSize(Exception ex) {
        String msg = "Uploaded file exceeds maximum permitted size";
        if (ex.getMessage() != null) {
            msg = ex.getMessage();
        }
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(new ErrorDTO("PAYLOAD_TOO_LARGE", msg, null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDTO> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new ErrorDTO("ERROR", ex.getMessage(), null));
    }
}
