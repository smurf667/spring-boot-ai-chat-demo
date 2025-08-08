package de.engehausen.aidemo.mock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import de.engehausen.aidemo.dto.AiDto.ChatMessage;
import de.engehausen.aidemo.dto.AiDto.ChatRequest;
import de.engehausen.aidemo.dto.AiDto.ChatResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
@ConditionalOnBooleanProperty(name = "MOCK")
public class LocalHttpServer {

	private static final Logger LOGGER = LoggerFactory.getLogger(LocalHttpServer.class);
	private static final String COMPLETION_RESPONSE_TEMPLATE = "{ \"id\": \"chatcmpl-415\", \"object\": \"chat.completion\", \"created\": 1754661821, \"model\": \"mock-o-rama\", \"system_fingerprint\": \"mock\", \"choices\": [ { \"index\": 0, \"message\": { \"role\": \"assistant\", \"content\": \"@CONTENT@\" }, \"finish_reason\": \"stop\" } ], \"usage\": { \"prompt_tokens\": 1, \"completion_tokens\": 1, \"total_tokens\": 2 } }";

	private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	private HttpServer server;

	@PostConstruct
	public void startServer() throws IOException {
		server = HttpServer.create(new InetSocketAddress("localhost", 11434), 0);
		server.createContext("/", new PostHandler());
		server.setExecutor(null);
		server.start();
		LOGGER.debug("Mock server started.");
	}

	@PreDestroy
	public void stopServer() {
		if (server != null) {
			server.stop(0);
			LOGGER.debug("Mock server stopped.");
		}
	}

	class PostHandler implements HttpHandler {
		@Override
		public void handle(final HttpExchange exchange) throws IOException {
			if (!HttpMethod.POST.name().equalsIgnoreCase(exchange.getRequestMethod())) {
				exchange.sendResponseHeaders(HttpStatus.METHOD_NOT_ALLOWED.value(), -1);
				return;
			}
			LOGGER.debug("received {} {} (headers: {})", exchange.getRequestMethod(), exchange.getRequestURI().toASCIIString(), exchange.getRequestHeaders());
			try (final InputStream is = exchange.getRequestBody()) {
				final ChatRequest request = objectMapper.readValue(is, ChatRequest.class);
				final Optional<ChatMessage> gardenQuestion = request
					.getMessages()
					.stream()
					.filter(message -> "user".equals(message.getRole()) && message.getContent().toLowerCase(Locale.ENGLISH).contains("garden"))
					.findAny();
				final byte[] responseBytes;
				if (exchange.getRequestURI().getPath().startsWith("/api/chat")) {
					final ChatResponse response = new ChatResponse();
					response.setMessage(new ChatMessage("assistant", gardenQuestion.isPresent() ? "Gardens are fun, don't you think?" : "I am sorry, I cannot do that, Dave."));
					response.setDone(true);
					responseBytes = objectMapper.writeValueAsBytes(response);
				} else {
					responseBytes = COMPLETION_RESPONSE_TEMPLATE.replace("@CONTENT@", gardenQuestion.isPresent() ? "How does your garden grow?" : "It can only be attributed to... human error!").getBytes(StandardCharsets.UTF_8);
				}
				exchange.getResponseHeaders().add("Content-Type", "application/json");
				exchange.sendResponseHeaders(HttpStatus.OK.value(), responseBytes.length);
				try (final OutputStream os = exchange.getResponseBody()) {
					os.write(responseBytes);
				}
			} catch (Exception e) {
				LOGGER.error("Handling error", e);
				exchange.sendResponseHeaders(HttpStatus.INTERNAL_SERVER_ERROR.value(), -1);
			}
		}
	}
}
