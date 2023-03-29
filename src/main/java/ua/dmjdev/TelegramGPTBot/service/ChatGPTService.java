package ua.dmjdev.TelegramGPTBot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.dmjdev.TelegramGPTBot.GPT.Message;
import ua.dmjdev.TelegramGPTBot.config.ChatGPTConfig;

import java.io.IOException;
import java.util.List;

@Service
public class ChatGPTService {

    @Autowired
    ChatGPTConfig gptConfig;

    public String getResponse(String prompt) throws IOException {
        OkHttpClient httpClient = new OkHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("prompt", prompt);
        requestBody.put("max_tokens", 250);
        requestBody.put("temperature", 0.7);

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), requestBody.toString());

        Request request = new Request.Builder()
                .url(gptConfig.getUrl())
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + gptConfig.getToken())
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
