package tg.masal.telegramgptbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

@Service
@PropertySource("application.properties")
public class ApiKeyService {
	final String apiKey;

	public ApiKeyService(@Value("${chatgpt.apiKey}") String apiKey) {
		super();
		this.apiKey = apiKey;
	}

	public String getApiKey() {
		return apiKey;
	}
}
