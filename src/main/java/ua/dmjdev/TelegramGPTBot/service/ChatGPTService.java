package ua.dmjdev.TelegramGPTBot.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import okhttp3.*;
import org.springframework.stereotype.Service;
import ua.dmjdev.TelegramGPTBot.GPT.Message;
import ua.dmjdev.TelegramGPTBot.GPT.OpenAIRequest;
import ua.dmjdev.TelegramGPTBot.config.ChatGPTConfig;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ChatGPTService {

    final ChatGPTConfig gptConfig;
    final OkHttpClient httpClient;
    final ObjectMapper objectMapper;

    {
        httpClient = new OkHttpClient.Builder()
                .readTimeout(35, TimeUnit.SECONDS).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new ParameterNamesModule());
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public ChatGPTService(ChatGPTConfig gptConfig) {
        this.gptConfig = gptConfig;
    }

    public Message getResponse(List<Message> messages) throws IOException {
        OpenAIRequest openAIRequest = new OpenAIRequest();
        openAIRequest.setModel("gpt-3.5-turbo");
        openAIRequest.setMaxTokens(300);
        openAIRequest.setTemperature(1);
        openAIRequest.setMessages(messages);
        String requestBody = objectMapper.writeValueAsString(openAIRequest);
        Request request = new Request.Builder().url(gptConfig.getUrl()).addHeader("Content-Type", "application/json").addHeader("Authorization", "Bearer " + gptConfig.getToken()).post(RequestBody.create(MediaType.parse("application/json"), requestBody)).build();
        Response response = httpClient.newCall(request).execute();
        String responseBody = response.body().string();
        ObjectNode jsonResponse = (ObjectNode) objectMapper.readTree(responseBody);
        System.out.println(jsonResponse);
        return objectMapper.readValue(jsonResponse.get("choices").get(0).get("message").toString()
                , Message.class);
    }
}
