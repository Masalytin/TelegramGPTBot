package ua.dmjdev.TelegramGPTBot.GPT;

import lombok.Data;

import java.util.List;

@Data
public class OpenAIResponse {
    private List<Message> choices;
}
