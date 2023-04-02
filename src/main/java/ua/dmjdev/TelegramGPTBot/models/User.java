package ua.dmjdev.TelegramGPTBot.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import ua.dmjdev.TelegramGPTBot.GPT.Message;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
@Document
public class User {
    @Id
    private long id;
    private List<Message> messages = new ArrayList();
    private int countOfMessages;

    public User(long id) {
        this.id = id;
    }

    public void incCountOfMessages() {
        countOfMessages++;
    }
}
