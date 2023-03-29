package ua.dmjdev.TelegramGPTBot.GPT;

import lombok.Data;

@Data
public class Message {
    private Role role;
    private String content;

    @Override
    public String toString() {
        return role + " : " + content;
    }
}
