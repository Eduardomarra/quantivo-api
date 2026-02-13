package com.example.quantivo.exception;

public class InvalidCredentialsException extends RuntimeException{

	public InvalidCredentialsException() {
		super("Usuário ou senha inválidos");
	}
}
