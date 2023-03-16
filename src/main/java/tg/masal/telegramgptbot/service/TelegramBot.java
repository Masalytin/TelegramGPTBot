package tg.masal.telegramgptbot.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import tg.masal.telegramgptbot.config.BotConfig;

@Service
public class TelegramBot extends TelegramLongPollingBot {
	@Autowired
	BotConfig config;

	@Override
	public String getBotUsername() {
		return config.getName();
	}

	@Override
	public String getBotToken() {
		return config.getToken();
	}

	@Override
	public void onUpdateReceived(Update update) {
		if (update.hasMessage()) {
			messageHander(update.getMessage());
		}
	}

	private void messageHander(Message message) {
		
		if (message.hasText()) {
			String text = message.getText();
			long chatId = message.getChatId();
			if (text.equals("/start ")) {
				sendMessage(chatId, "Hello");
			} else {
				sendMessage(chatId, "", new ReplyKeyboardMarkup(new ArrayList<>()));
			}
		}
	}

	private void sendMessage(long chatId, String text) {
		try {
			execute(SendMessage.builder().chatId(chatId).text(text).build());
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
	
	private void sendMessage(long chatId, String message, ReplyKeyboard keyboard) {
		
	}
}
