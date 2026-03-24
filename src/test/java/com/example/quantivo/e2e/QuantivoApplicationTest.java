package com.example.quantivo.e2e;

import com.example.quantivo.QuantivoApplication;
import com.example.quantivo.entity.Usuario;
import com.example.quantivo.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

@SpringBootTest(
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = QuantivoApplication.class
)
@ActiveProfiles("test")
@DisplayName("Testes End-to-End da API Quantivo")
class QuantivoE2ETest {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private String baseUrl;
	private String authToken;
	private UUID usuarioId;
	private String usuarioEmail;
	private String usuarioSenha;

	@BeforeEach
	void setUp() {
		baseUrl = "http://localhost:" + port;
		usuarioEmail = "e2e@teste.com";
		usuarioSenha = "senha123";

		// Limpa o banco antes de cada teste
		usuarioRepository.deleteAll();

		// Cria usuário de teste
		Usuario usuario = new Usuario();
		usuario.setEmail(usuarioEmail);
		usuario.setSenha(passwordEncoder.encode(usuarioSenha));
		usuario.setAtivo(true);

		Usuario saved = usuarioRepository.save(usuario);
		usuarioId = saved.getId();
	}

	@Test
	@DisplayName("E2E - Fluxo completo: Login → Criar Lista → Adicionar Item → Buscar Itens → Deletar")
	void e2e_fluxoCompleto() {
		// 1. Login
		realizarLogin();
		assertThat(authToken).isNotNull();

		// 2. Criar lista mensal
		UUID listaId = criarListaMensal();
		assertThat(listaId).isNotNull();

		// 3. Adicionar item
		UUID itemId = adicionarItem(listaId);
		assertThat(itemId).isNotNull();

		// 4. Buscar itens
		buscarItens(listaId);

		// 5. Buscar resumo
		buscarResumo(listaId);

		// 6. Alterar item
		alterarItem(itemId);

		// 7. Deletar item
		deletarItem(itemId);

		// 8. Deletar lista
		deletarLista(listaId);
	}

	private void realizarLogin() {
		String loginJson = """
            {
                "email": "%s",
                "senha": "%s"
            }
            """.formatted(usuarioEmail, usuarioSenha);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>(loginJson, headers);

		ResponseEntity<String> response = restTemplate.postForEntity(
				baseUrl + "/auth/login",
				request,
				String.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		authToken = extractToken(response.getBody());
	}

	private UUID criarListaMensal() {
		String criarListaJson = """
            {
                "usuarioId": "%s"
            }
            """.formatted(usuarioId);

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(authToken);
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>(criarListaJson, headers);

		ResponseEntity<String> response = restTemplate.postForEntity(
				baseUrl + "/lista-mensal/criar",
				request,
				String.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		// Extrai o ID da lista do JSON
		String body = response.getBody();
		int idStart = body.indexOf("\"idLista\":\"") + 11;
		int idEnd = body.indexOf("\"", idStart);
		return UUID.fromString(body.substring(idStart, idEnd));
	}

	private UUID adicionarItem(UUID listaId) {
		String adicionarItemJson = """
            {
                "nomeProduto": "Produto E2E",
                "quantidade": 5,
                "valorUnitario": 25.50
            }
            """;

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(authToken);
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>(adicionarItemJson, headers);

		ResponseEntity<String> response = restTemplate.postForEntity(
				baseUrl + "/lista-mensal/" + listaId + "/itens",
				request,
				String.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		// Extrai o ID do item do JSON
		String body = response.getBody();
		int idStart = body.indexOf("\"id\":\"") + 6;
		int idEnd = body.indexOf("\"", idStart);
		return UUID.fromString(body.substring(idStart, idEnd));
	}

	private void buscarItens(UUID listaId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(authToken);
		HttpEntity<String> request = new HttpEntity<>(headers);

		ResponseEntity<String> response = restTemplate.exchange(
				baseUrl + "/item-lista/itens/" + listaId,
				HttpMethod.GET,
				request,
				String.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("Produto E2E");
	}

	private void buscarResumo(UUID listaId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(authToken);
		HttpEntity<String> request = new HttpEntity<>(headers);

		ResponseEntity<String> response = restTemplate.exchange(
				baseUrl + "/lista-mensal/resumo/" + listaId,
				HttpMethod.GET,
				request,
				String.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("totalItens");
	}

	private void alterarItem(UUID itemId) {
		String alterarItemJson = """
            {
                "nomeProduto": "Produto Alterado",
                "quantidade": 10,
                "valorUnitario": 30.00
            }
            """;

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(authToken);
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> request = new HttpEntity<>(alterarItemJson, headers);

		ResponseEntity<String> response = restTemplate.exchange(
				baseUrl + "/lista-mensal/itens/" + itemId,
				HttpMethod.PUT,
				request,
				String.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("Produto Alterado");
	}

	private void deletarItem(UUID itemId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(authToken);
		HttpEntity<String> request = new HttpEntity<>(headers);

		ResponseEntity<Void> response = restTemplate.exchange(
				baseUrl + "/lista-mensal/deletar-item/" + itemId,
				HttpMethod.DELETE,
				request,
				Void.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}

	private void deletarLista(UUID listaId) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(authToken);
		HttpEntity<String> request = new HttpEntity<>(headers);

		ResponseEntity<Void> response = restTemplate.exchange(
				baseUrl + "/lista-mensal/deletar/" + listaId,
				HttpMethod.DELETE,
				request,
				Void.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}

	private String extractToken(String responseBody) {
		int tokenStart = responseBody.indexOf("\"token\":\"") + 9;
		int tokenEnd = responseBody.indexOf("\"", tokenStart);
		return responseBody.substring(tokenStart, tokenEnd);
	}
}
