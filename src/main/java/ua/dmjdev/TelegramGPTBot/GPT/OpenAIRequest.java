package ua.dmjdev.TelegramGPTBot.GPT;

import lombok.Data;

import java.util.List;

@Data
public class OpenAIRequest {
    private String model;
    private int max_tokens;
    private double temperature;
    private List<Message> messages;
}
