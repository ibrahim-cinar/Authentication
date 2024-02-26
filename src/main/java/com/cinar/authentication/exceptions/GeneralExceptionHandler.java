package com.cinar.authentication.exceptions;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GeneralExceptionHandler extends ResponseEntityExceptionHandler {

    @NotNull
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  @NotNull HttpHeaders headers,
                                                                  @NotNull HttpStatus status,
                                                                  @NotNull WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> ExceptionHandler(Exception exception)  {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> UserNotFoundExceptionHandler(UsernameNotFoundException UserNotFoundException)  {
        return new ResponseEntity<>(UserNotFoundException.getMessage(), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<?> EmailNotFoundExceptionHandler(EmailNotFoundException EmailNotFoundException)  {
        return new ResponseEntity<>(EmailNotFoundException.getMessage(), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(EmailAlreadyExistException.class)
    public ResponseEntity<?> EmailAlreadyExistExceptionHandler(EmailAlreadyExistException EmailAlreadyExistException)  {
        return new ResponseEntity<>(EmailAlreadyExistException.getMessage(), HttpStatus.CONFLICT);
    }
    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<?> InvalidInputExceptionHandler(InvalidInputException InvalidInputException)  {
        return new ResponseEntity<>(InvalidInputException.getMessage(), HttpStatus.BAD_REQUEST);
    }
   /* @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> EntityNotFoundExceptionHandler(EntityNotFoundException EntityNotFoundException)  {
        return new ResponseEntity<>(EntityNotFoundException.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UsernameAlreadyExistException.class)
    public ResponseEntity<?> UsernameAlreadyExistExceptionHandler(UsernameAlreadyExistException UsernameAlreadyExistException)  {
        return new ResponseEntity<>(UsernameAlreadyExistException.getMessage(), HttpStatus.CONFLICT);
    }
*/



}