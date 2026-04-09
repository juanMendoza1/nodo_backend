package com.nodo.inv.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.orm.ObjectOptimisticLockingFailureException; 
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Credenciales incorrectas");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, String>> handleDisabled(DisabledException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "El usuario está desactivado");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
    
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Conflicto de sincronización: El registro fue modificado por otro usuario u otra tablet al mismo tiempo.");
        error.put("code", "SYNC_CONFLICT");
        error.put("action", "RETRY");
        
        // Retornamos un 409 Conflict
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }
}