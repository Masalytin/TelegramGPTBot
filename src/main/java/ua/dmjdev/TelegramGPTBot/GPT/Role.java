package ua.dmjdev.TelegramGPTBot.GPT;

public enum Role {
    system("system"),
    user("user"),
    assistant("assistant");
    private String  name;

    Role(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
