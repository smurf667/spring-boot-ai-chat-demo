package de.engehausen.aidemo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class AiDto {

	public static class ChatRequest {
		private String model;
		private List<ChatMessage> messages;
		private boolean stream = false;

		public ChatRequest() {
			// default constructor for JSON deserialization
		}

		public ChatRequest(final String model, final List<ChatMessage> messages) {
			this.model = model;
			this.messages = messages;
		}

		public String getModel() {
			return model;
		}

		public List<ChatMessage> getMessages() {
			return messages;
		}

		public boolean isStream() {
			return stream;
		}
	}

	public static class ChatMessage {
		private String role;
		private String content;

		public ChatMessage() {
			// default constructor for JSON deserialization
		}

		public ChatMessage(final String role, final String content) {
			this.role = role;
			this.content = content;
		}

		public String getRole() {
			return role;
		}

		public String getContent() {
			return content;
		}
	}

	public static class ChatResponse {
		private ChatMessage message;
		private boolean done;

		public ChatResponse() {
			// default constructor for JSON deserialization
		}

		public ChatMessage getMessage() {
			return message;
		}

		public void setMessage(final ChatMessage message) {
			this.message = message;
		}

		@JsonProperty("done")
		public boolean isDone() {
			return done;
		}

		@JsonProperty("done")
		public void setDone(final boolean done) {
			this.done = done;
		}
	}
}