package ua.dmjdev.TelegramGPTBot.GPT;

public enum Role {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant");
    private String  name;

    Role(String name) {
        this.name = name;
    }
}
