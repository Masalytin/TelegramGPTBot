package ua.dmjdev.TelegramGPTBot.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Getter
@PropertySource("application.properties")
public class ChatGPTConfig {
    @Value("${openai.url}")
    private String url;
    @Value("${openai.token}")
    private String token;
}
