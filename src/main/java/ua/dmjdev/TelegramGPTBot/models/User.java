package ua.dmjdev.TelegramGPTBot.models;

import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
public class User {
    private long id;
    private String name;

}
