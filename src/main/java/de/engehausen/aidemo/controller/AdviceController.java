package de.engehausen.aidemo.controller;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import de.engehausen.aidemo.dto.AiDto;

@RestController
@RequestMapping("/api")
public class AdviceController {

	@Value("${app.system.prompt}")
	private String systemPrompt;
	@Value("${app.ai.provider.model}")
	private String modelName;
	@Value("${app.ai.provider.chat-url}")
	private String aiProviderChatUrl;

	private final RestClient restClient;
	private final ChatClient.Builder chatClientBuilder;

	public AdviceController(final RestClient restClient, final ChatClient.Builder chatClientBuilder) {
		this.restClient = restClient;
		this.chatClientBuilder = chatClientBuilder;
	}

	@PostMapping(value = "/advice-restclient", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
	public String getAdviceWithRestClient(@RequestBody final String userPrompt) {
		final List<AiDto.ChatMessage> messages = List.of(new AiDto.ChatMessage("system", systemPrompt),
				new AiDto.ChatMessage("user", userPrompt));
		final AiDto.ChatRequest request = new AiDto.ChatRequest(modelName, messages);
		final AiDto.ChatResponse response = restClient
				.post()
				.uri(aiProviderChatUrl)
				.contentType(MediaType.APPLICATION_JSON)
				.body(request)
				.retrieve()
				.body(AiDto.ChatResponse.class);
		return response != null ? response.getMessage().getContent() : "Error: No response from model.";
	}

	@PostMapping(value = "/advice-springai", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
	public String getAdviceWithSpringAi(@RequestBody final String userPrompt) {
		final ChatClient chatClient = chatClientBuilder.build();
		final ChatResponse response = chatClient
				.prompt()
				.system(systemPrompt)
				.user(userPrompt)
				.call()
				.chatResponse();
		return response.getResult().getOutput().getText();
	}
}