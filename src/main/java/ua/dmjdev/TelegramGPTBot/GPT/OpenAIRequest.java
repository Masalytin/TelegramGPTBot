package ua.dmjdev.TelegramGPTBot.GPT;

import lombok.Data;

import java.util.List;

@Data
public class OpenAIRequest {
    private String model;
    private int maxTokens;
    private double temperature;
    private List<Message> messages;
}
