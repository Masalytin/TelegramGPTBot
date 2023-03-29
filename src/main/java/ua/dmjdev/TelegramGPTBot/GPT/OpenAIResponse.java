package ua.dmjdev.TelegramGPTBot.GPT;

import lombok.Data;

import java.util.List;

@Data
public class OpenAIResponse {
    @Data
    public class Choice {
        private String text;
        private double score;
    }

    private List<Choice> choices;
}
