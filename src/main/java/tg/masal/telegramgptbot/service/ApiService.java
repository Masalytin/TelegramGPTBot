package tg.masal.telegramgptbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import lombok.Getter;

@Service
@Getter
@PropertySource("application.properties")
public class ApiService {
	final String apiKey;
	final String apiUrl;

	public ApiService(@Value("${chatgpt.apiKey}") String apiKey, @Value("${chatgpt.apiUrl}") String apiUrl) {
		super();
		this.apiKey = apiKey;
		this.apiUrl = apiUrl;
	}
	
}
