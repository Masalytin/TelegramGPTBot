package tg.masal.telegramgptbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;

@Getter
@Configuration
@PropertySource("application.properties")
public class BotConfig {
	@Value("${bot.name}")
	private String name;
	@Value("${bot.token}")
	private String token;
	
	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}
	
}
