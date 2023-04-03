package ua.dmjdev.TelegramGPTBot.GPT;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private Role role;
    private String content;

//    public int getTokensCount() {
//        return 0;
//    }
}
