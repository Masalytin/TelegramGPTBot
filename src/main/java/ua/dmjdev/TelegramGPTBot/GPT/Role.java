package ua.dmjdev.TelegramGPTBot.GPT;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "lowercase")
public enum Role {
    SYSTEM,
    USER,
    ASSISTANT;

    @JsonValue
    public String toLowerCase() {
        return name().toLowerCase();
    }
}
