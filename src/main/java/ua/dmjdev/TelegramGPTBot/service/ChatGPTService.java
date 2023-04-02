package ua.dmjdev.TelegramGPTBot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.dmjdev.TelegramGPTBot.GPT.Message;
import ua.dmjdev.TelegramGPTBot.GPT.OpenAIRequest;
import ua.dmjdev.TelegramGPTBot.GPT.Role;
import ua.dmjdev.TelegramGPTBot.config.ChatGPTConfig;

import java.io.IOException;
import java.util.List;

@Service
public class ChatGPTService {

    @Autowired
    ChatGPTConfig gptConfig;

    public Message getResponse(List<Message> messages) throws IOException {
        OkHttpClient httpClient = new OkHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        OpenAIRequest openAIRequest = new OpenAIRequest();
        openAIRequest.setModel("gpt-3.5-turbo");
        openAIRequest.setMax_tokens(150);
        openAIRequest.setTemperature(1);
        openAIRequest.setMessages(messages);
        String requestBody = objectMapper.writeValueAsString(openAIRequest);
        Request request = new Request.Builder()
                .url(gptConfig.getUrl())
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + gptConfig.getToken())
                .post(RequestBody.create(MediaType.parse("application/json"), requestBody))
                .build();
        Response response = httpClient.newCall(request).execute();
        String responseBody = response.body().string();

        ObjectNode jsonResponse = (ObjectNode) objectMapper.readTree(responseBody);
        System.out.println(jsonResponse.get("choices").get(0));
        return objectMapper.readValue(jsonResponse.get("choices").get(0).get("message").toString(), Message.class);
    }
}
