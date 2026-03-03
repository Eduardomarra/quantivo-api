package com.example.quantivo.config;

import java.util.List;

import io.swagger.v3.oas.models.*;
//import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
//import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Quantivo API")
						.description("API do sistema Quantivo para gerenciamento de [descreva o propósito]")
						.version("1.0.0")
						.contact(new Contact()
								.name("Seu Nome")
								.email("seu.email@email.com")
								.url("https://seusite.com"))
						.license(new License()
								.name("Apache 2.0")
								.url("https://www.apache.org/licenses/LICENSE-2.0")))
				.tags(List.of(
						new Tag().name("Autenticação").description("Endpoints de autenticação"),
						new Tag().name("Usuários").description("Gerenciamento de usuários")
						// Adicione mais tags conforme seus controllers
				))
				.components(new Components()
						.addSecuritySchemes("bearerAuth", new SecurityScheme()
								.name("bearerAuth")
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.description("Insira o token JWT no formato: Bearer {token}")))
				.addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
				.components(new Components()
						.addSecuritySchemes("BearerAuth",
								new SecurityScheme()
										.name("Authorization")
										.type(SecurityScheme.Type.HTTP)
										.scheme("bearer")
										.bearerFormat("JWT")
						)
				);
	}
}
