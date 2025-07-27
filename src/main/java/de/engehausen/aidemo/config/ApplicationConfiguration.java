package de.engehausen.aidemo.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

@Configuration
public class ApplicationConfiguration {

	@Bean
	public RestClient restClient() {
		return RestClient.builder().build();
	}

	@Bean
	public ChatModel chatLanguageModel(@Value("${spring.ai.openai.base-url}") final String baseUrl,
			@Value("${spring.ai.openai.api-key}") final String apiKey, @Value("${app.ai.provider.model}") final String modelName) {
		return OpenAiChatModel
				.builder()
				.baseUrl(baseUrl)
				.apiKey(apiKey)
				.modelName(modelName)
				.timeout(Duration.ofSeconds(60)).build();
	}
}