package tg.masal.telegramgptbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("application.properties")
public class BotConfig {
	@Value("${bot.name}")
	private String name;
	@Value("${bot.token}")
	private String token;
	
	public String getName() {
		return name;
	}
	
	public String getToken() {
		return token;
	}
	
}
