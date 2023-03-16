package tg.masal.telegramgptbot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import tg.masal.telegramgptbot.service.TelegramBot;

@Component
public class BotInitializer {
	
	@Autowired
	TelegramBot bot;
	
	@EventListener(ContextRefreshedEvent.class)
	public void init() {
		try {
			TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
			api.registerBot(bot);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
}
