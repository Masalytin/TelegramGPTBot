package ua.dmjdev.TelegramGPTBot.GPT;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Role {
    @JsonProperty("system")
    SYSTEM,
    @JsonProperty("user")
    USER,
    @JsonProperty("assistant")
    ASSISTANT;
}
