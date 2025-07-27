# AI chat integration in a Spring Boot application

This Spring Boot application demonstrates integration with OpenAPI-style chat endpoints using two different approaches:

- Vanilla `RestClient`
  - `POST http://localhost:8080/api/advice-restclient`
- Spring AI Integration
  - `POST http://localhost:8080/api/advice-springai`

The endpoints take plain text, which is used as input to the AI chat, which is configured to be an expert on gardening topics.

By default, the application assumes you're running a `tinyllama` model locally via Ollama, exposed on port `11434`.

If you're unable to connect to an actual endpoint, you can use a mocked backend instead:
- Enable mock mode by setting the environment variable `MOCK=true`
  - Windows: `set MOCK=true`
  - Linux/macOS: `export MOCK=true`
- In mock mode, responses are limited:
  - If the prompt contains `"garden"`, a related response is returned
  - Otherwise, a generic answer is provided

Start the application with:

```bash
mvn spring-boot:run
```

## Example calls

```bash
curl -X POST http://localhost:8080/api/advice-springai \
  -H "Content-Type: text/plain" \
  -d "How do I eat bibimbap?"

curl -X POST http://localhost:8080/api/advice-restclient \
  -H "Content-Type: text/plain" \
  -d "It's spring, what food should I plant in my garden?"
```

## Logging and diagnostics

- The Apache Http Client is used for communication
- HTTP logging is enabled:
  - Inspect full request/response exchanges, including headers and JSON payloads
  - Useful for debugging and understanding the integration flow
