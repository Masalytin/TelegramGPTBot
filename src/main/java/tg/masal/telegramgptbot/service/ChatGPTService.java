package tg.masal.telegramgptbot.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ChatGPTService {

    private final String OPENAI_API_URL = "https://api.openai.com/v3/engines/davinci-codex/completions";

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Autowired
    private ApiKeyService apiKeyService;

    public ChatGPTService() {
        httpClient = new OkHttpClient();
        objectMapper = new ObjectMapper();
    }

    public String getResponse(String prompt) throws IOException {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("prompt", prompt);
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0.7);

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), requestBody.toString());

        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + apiKeyService.getApiKey())
                .post(body)
                .build();

        Response response = httpClient.newCall(request).execute();
        String responseBody = response.body().string();

        ObjectNode jsonResponse = (ObjectNode) objectMapper.readTree(responseBody);
        System.out.println(jsonResponse);
        String output = jsonResponse.get("choices").get(0).get("text").asText();

        return output;
    }
}
