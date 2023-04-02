package ua.dmjdev.TelegramGPTBot.GPT;

import lombok.Data;

import java.util.List;

@Data
public class OpenAIResponse {
    private Usage usage;
    private List<Message> choices;
    @Data
    public class Usage {
        private int promptTokens;
        private int completionTokens;
        private int totalTokens;
    }
}
