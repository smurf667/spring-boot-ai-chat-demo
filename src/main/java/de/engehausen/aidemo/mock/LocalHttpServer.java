package de.engehausen.aidemo.mock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

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

	private final ObjectMapper objectMapper = new ObjectMapper();
	private HttpServer server;

	@PostConstruct
	public void startServer() throws IOException {
		server = HttpServer.create(new InetSocketAddress("localhost", 11434), 0);
		server.createContext("/api/chat", new PostHandler());
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

			try (final InputStream is = exchange.getRequestBody()) {
				final ChatRequest request = objectMapper.readValue(is, ChatRequest.class);
				final Optional<ChatMessage> gardenQuestion = request
					.getMessages()
					.stream()
					.filter(message -> "user".equals(message.getRole()) && message.getContent().toLowerCase(Locale.ENGLISH).contains("garden"))
					.findAny();
				final ChatResponse response = new ChatResponse();
				response.setMessage(new ChatMessage("assistant", gardenQuestion.isPresent() ? "Gardens are fun, don't you think?" : "I am sorry, I cannot do that, Dave."));
				response.setDone(true);
				final byte[] responseBytes = objectMapper.writeValueAsBytes(response);
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
