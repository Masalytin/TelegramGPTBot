package ua.dmjdev.TelegramGPTBot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.dmjdev.TelegramGPTBot.GPT.OpenAIRequest;
import ua.dmjdev.TelegramGPTBot.config.ChatGPTConfig;

import java.util.List;

@Service
public class ChatGPTService {

    @Autowired
    ChatGPTConfig gptConfig;
    public String getResponse(String  promt) {
        OkHttpClient httpClient = new OkHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        OpenAIRequest request = new OpenAIRequest();
        request.setModel("gpt-3.5-turbo");
        request.setMaxTokens(50);
        request.setTemperature(1);
        request.setPrompt(promt);


        return null;
    }
}
