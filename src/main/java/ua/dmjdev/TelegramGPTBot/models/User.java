package ua.dmjdev.TelegramGPTBot.models;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.dmjdev.TelegramGPTBot.GPT.Message;

import java.util.List;

@Entity
@NoArgsConstructor
@Data
public class User {
    private long id;
    private String name;
    private List<Message> messages;
}
