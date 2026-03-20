package com.example.quantivo.to;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
		description = "Resposta contendo token JWT",
		example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3O..."
)
public record LoginResponse(String token, UsuarioTO usuario) {
}
