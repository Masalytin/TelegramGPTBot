package ua.dmjdev.TelegramGPTBot.GPT;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Message {
    private Role role;
    private String content;

    @Override
    public String toString() {
        return role + " : " + content;
    }
}
