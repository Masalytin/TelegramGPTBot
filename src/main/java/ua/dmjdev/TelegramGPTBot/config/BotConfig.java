package ua.dmjdev.TelegramGPTBot.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Arrays;

@Configuration
@Getter
@PropertySource("application.properties")
public class BotConfig {
    @Value("${bot.username}")
    private String username;
    @Value("${bot.token}")
    private String token;
    @Value("${bot.admins}")
    private String admins;

    public long[] getADMINS() {
        return Arrays.stream(admins.split(";"))
                .mapToLong(Long::parseLong)
                .toArray();
    }
}
