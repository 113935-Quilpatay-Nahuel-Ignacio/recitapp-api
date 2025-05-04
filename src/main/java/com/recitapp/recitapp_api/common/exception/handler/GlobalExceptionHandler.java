package com.recitapp.recitapp_api.common.exception.handler;

import com.recitapp.recitapp_api.common.exception.RecitappException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecitappException.class)
    public ResponseEntity<Map<String, String>> handleRecitappException(RecitappException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", ex.getMessage());
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String message = ex.getMessage();

        if (message.contains("foreign key constraint fails")) {
            if (message.contains("venue_sections")) {
                message = "No se puede eliminar el recinto porque tiene secciones asociadas";
            } else {
                message = "No se puede eliminar debido a restricciones de integridad de datos";
            }
        }

        Map<String, String> errors = new HashMap<>();
        errors.put("error", message);
        return new ResponseEntity<>(errors, HttpStatus.CONFLICT);
    }
}