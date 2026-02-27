package com.example.quantivo.exception;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@Value("${spring.profiles.active:}")
	private String[] activeProfiles;

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(
			ResourceNotFoundException ex,
			HttpServletRequest request
	) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.NOT_FOUND.value(),
				"NOT_FOUND",
				ex.getMessage(),
				request.getRequestURI()
		);

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusiness(
			BusinessException ex,
			HttpServletRequest request
	) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				"BUSINESS_ERROR",
				ex.getMessage(),
				request.getRequestURI()
		);

		return ResponseEntity.badRequest().body(error);
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleInvalidCredentials(
			InvalidCredentialsException ex,
			HttpServletRequest request
	) {
		ErrorResponse error = new ErrorResponse(
				HttpStatus.UNAUTHORIZED.value(),
				"INVALID_CREDENTIALS",
				ex.getMessage(),
				request.getRequestURI()
		);

		return ResponseEntity
				.status(HttpStatus.UNAUTHORIZED)
				.body(error);
	}


	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneric(
			Exception ex,
			HttpServletRequest request
	) {
		log.error("Erro inesperado: {} - URI: {}", ex.getMessage(), request.getRequestURI(), ex);

		String message = "Ocorreu um erro inesperado. Tente novamente mais tarde.";

		// Verifica se está em desenvolvimento
		if (activeProfiles != null &&
				Arrays.asList(activeProfiles).contains("desenvolvimento")) {
			message = ex.getMessage();
		}

		ErrorResponse error = new ErrorResponse(
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"INTERNAL_ERROR",
				message,
				request.getRequestURI()
		);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(
			MethodArgumentNotValidException ex,
			HttpServletRequest request
	) {
		List<String> errors = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.toList());

		String message = "Erro de validação: " + String.join(", ", errors);
		log.warn("Erro de validação: {} - URI: {}", errors, request.getRequestURI());

		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				"VALIDATION_ERROR",
				message,
				request.getRequestURI()
		);
		return ResponseEntity.badRequest().body(error);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
			HttpMessageNotReadableException ex,
			HttpServletRequest request
	) {
		log.error("Erro de formato JSON: {} - URI: {}", ex.getMessage(), request.getRequestURI());

		ErrorResponse error = new ErrorResponse(
				HttpStatus.BAD_REQUEST.value(),
				"INVALID_JSON",
				"Formato de JSON inválido. Verifique a sintaxe da requisição.",
				request.getRequestURI()
		);
		return ResponseEntity.badRequest().body(error);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDenied(
			AccessDeniedException ex,
			HttpServletRequest request
	) {
		log.warn("Acesso negado para URI: {}", request.getRequestURI());

		ErrorResponse error = new ErrorResponse(
				HttpStatus.FORBIDDEN.value(),
				"ACCESS_DENIED",
				"Você não tem permissão para acessar este recurso",
				request.getRequestURI()
		);
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
	}
}
