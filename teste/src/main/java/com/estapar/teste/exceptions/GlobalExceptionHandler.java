package com.estapar.teste.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
		log.error("Argumento inválido: {}", ex.getMessage());
		return build(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
		log.error("Body inválido: {}", ex.getMessage());
		return build(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido ou ausente.");
	}

	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException ex) {
		log.error("Recurso não encontrado: {}", ex.getMessage());
		return build(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
		log.error("Conflito de estado: {}", ex.getMessage());
		return build(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(UnprocessableEntityException.class)
	public ResponseEntity<ErrorResponse> handleUnprocessable(UnprocessableEntityException ex) {
		log.error("Entidade não processável: {}", ex.getMessage());
		return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(f -> f.getField() + ": " + f.getDefaultMessage()).findFirst().orElse("Dados inválidos.");
		log.error("Erro de validação: {}", message);
		return build(HttpStatus.UNPROCESSABLE_ENTITY, message);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
		log.error("Erro inesperado: {}", ex.getMessage());
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor.");
	}

	private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message));
	}
}