package ua.dmjdev.TelegramGPTBot.GPT;

import lombok.Data;

@Data
public class OpenAIResponse {
    private Usage usage;
    private Message message;
    @Data
    public static class Usage {
        private int promptTokens;
        private int completionTokens;
        private int totalTokens;
    }
}
