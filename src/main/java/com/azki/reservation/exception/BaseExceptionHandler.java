package com.azki.reservation.exception;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class BaseExceptionHandler {

    @ExceptionHandler({DataIntegrityViolationException.class})
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        String errorMessage = ex.getMessage();
        HttpStatus status = HttpStatus.CONFLICT;
        String path = request.getRequestURI();
        // Check if the error message contains information about the unique constraint
        if (errorMessage.contains("uc_tbl_user_email")) {
            return new ResponseEntity<>(new ErrorResponse(status, path, "Email is already in use in User."), status);
        } else if (errorMessage.contains("uc_tbl_user_username")) {
            return new ResponseEntity<>(new ErrorResponse(status, path, "Username is already in use in User."), status);
        }

        else if (errorMessage.contains("FK_TBL_AVAILABLE_SLOTS_ON_USER")) {
            return new ResponseEntity<>(new ErrorResponse(status, path, "AvailableSlots.reservedBy.id is already in use in another relations, first remove the relations then try again."), status);
        }

        // If the specific constraint is not identified, return a generic error message
        return new ResponseEntity<>(new ErrorResponse(status,path,"constraint violation occurred." + ex.getMessage()), status);
    }

    @ExceptionHandler({javax.validation.ConstraintViolationException.class, ConstraintViolationException.class})
    public ResponseEntity<?> handleConstraintViolationException(javax.validation.ConstraintViolationException exception, HttpServletRequest request) {
        String details = exception.getConstraintViolations().stream()
                .map(m -> String.format("{%s %s %s}", m.getRootBeanClass().getName(), m.getPropertyPath(), m.getMessage()))
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST, request.getRequestURI(), details));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST, request.getRequestURI(), errors));
    }
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, HttpServletRequest request) {
        return new ResponseEntity<>(ex.toErrorResponse(request.getRequestURI()), ex.getHttpStatus());
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedForRest(WebRequest request, AccessDeniedException ex) {
        String path = request.getDescription(false).substring(4); // Extract the request path
        return new ResponseEntity<>(new ErrorResponse(HttpStatus.FORBIDDEN,path,ex.getMessage()),HttpStatus.FORBIDDEN);
    }
}
