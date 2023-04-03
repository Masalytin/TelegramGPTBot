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
import ua.dmjdev.TelegramGPTBot.GPT.OpenAIResponse;
import ua.dmjdev.TelegramGPTBot.config.ChatGPTConfig;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ChatGPTService {

    final ChatGPTConfig gptConfig;

    public ChatGPTService(ChatGPTConfig gptConfig) {
        this.gptConfig = gptConfig;
    }

    public OpenAIResponse getResponse(List<Message> messages) throws IOException {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS).build();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new ParameterNamesModule());
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OpenAIRequest openAIRequest = new OpenAIRequest();
        openAIRequest.setModel("gpt-3.5-turbo");
        openAIRequest.setMaxTokens(150);
        openAIRequest.setTemperature(1);
        openAIRequest.setMessages(messages);
        String requestBody = objectMapper.writeValueAsString(openAIRequest);
        Request request = new Request.Builder().url(gptConfig.getUrl()).addHeader("Content-Type", "application/json").addHeader("Authorization", "Bearer " + gptConfig.getToken()).post(RequestBody.create(MediaType.parse("application/json"), requestBody)).build();
        Response response = httpClient.newCall(request).execute();
        String responseBody = response.body().string();
        ObjectNode jsonResponse = (ObjectNode) objectMapper.readTree(responseBody);
        OpenAIResponse openAIResponse = new OpenAIResponse();
        openAIResponse.setMessage(objectMapper.readValue(jsonResponse.get("choices").get(0).get("message").toString()
                , Message.class));
        openAIResponse.setUsage(objectMapper.readValue(jsonResponse.get("usage").toString()
                , OpenAIResponse.Usage.class));
        return openAIResponse;
    }
}
