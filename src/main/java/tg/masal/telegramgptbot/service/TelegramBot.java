package tg.masal.telegramgptbot.service;

import java.awt.Button;
import java.util.ArrayList;

import javax.swing.ButtonGroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import tg.masal.telegramgptbot.config.BotConfig;

@Service
public class TelegramBot extends TelegramLongPollingBot {
	
	final BotConfig config;
	
	public TelegramBot(BotConfig config) {
		this.config = config;
	}

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
			ReplyKeyboardMarkup mainMenu = new ReplyKeyboardMarkup() {{
				setKeyboard(new ArrayList<>() {{
					add(new KeyboardRow() {{
						add("Test");
						add("button2");
					}});
				}});
				setResizeKeyboard(true);
			}};
			if (text.equals("/start")) {
				sendMessage(chatId, "Hello");
			} else {
				sendMessage(chatId, "Test", mainMenu);
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
	
	private void sendMessage(long chatId, String text, ReplyKeyboard keyboard) {
		try {
			execute(SendMessage.builder().chatId(chatId).text(text).replyMarkup(keyboard).build());
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
}
