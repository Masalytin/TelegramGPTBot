package tg.masal.telegramgptbot.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ChatGPTService {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Autowired
    private ApiService apiKeyService;

    public ChatGPTService() {
        httpClient = new OkHttpClient();
        objectMapper = new ObjectMapper();
    }

    public String getResponse(String prompt) throws IOException {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("prompt", prompt);
        requestBody.put("max_tokens", 250);
        requestBody.put("temperature", 0.7);

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), requestBody.toString());

        Request request = new Request.Builder()
                .url(apiKeyService.apiUrl)
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
