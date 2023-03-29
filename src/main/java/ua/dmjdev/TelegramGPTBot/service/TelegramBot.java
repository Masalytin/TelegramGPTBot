package ua.dmjdev.TelegramGPTBot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;

@Component
@PropertySource("application.properties")
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private ChatGPTService gptService;

    private final ReplyKeyboardMarkup mainMenu = ReplyKeyboardMarkup.builder()
            .keyboard(new ArrayList<>() {{
                add(new KeyboardRow() {{
                    add("");
                    add("");
                    add("");
                }});
                add(new KeyboardRow() {{
                    add("");
                    add("");
                }});
            }}).build();

    public TelegramBot(DefaultBotOptions options, @Value("${bot.token}") String botToken) {
        super(options, botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            try {
                onMessageHandler(update.getMessage());
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void onMessageHandler(Message message) throws TelegramApiException {
        if (message.hasText()) {
            if (message.getChat().isGroupChat()) {
                sendMessage(message.getChatId(), "I don't work in group chats", InlineKeyboardMarkup.builder()
                        .keyboardRow(new ArrayList<>() {{
                            add(InlineKeyboardButton.builder()
                                    .text("G")
                                    .url("gg.com").build()
                            );
                        }}).build());
                return;
            }
            String text = message.getText();
            long uid = message.getFrom().getId();
            if (text.startsWith("/start")) {
                sendMessage(uid, "\uD83E\uDD16");
                sendMessage(uid, "Hello", mainMenu);
            } else {
                if (execute(GetChatMember.builder()
                        .chatId("-1001875522662")
                        .userId(uid).build()).getStatus().equals("left")) {
                    sendMessage(uid, "You need to join our Channel for use this bot", InlineKeyboardMarkup.builder()
                            .keyboardRow(new ArrayList<>() {{
                                add(InlineKeyboardButton.builder()
                                        .text("Join to channel")
                                        .url("https://t.me/+-CsMVIyMQGFjZDM6").build()
                                );
                            }}).build());
                }
                Message responseMessage = sendMessage(uid, "\uD83D\uDCE4");
                editMessageText(responseMessage, gptService.getResponse(text));
                editMessageText(responseMessage, "❌");
            }
        }
    }

    @Override
    @Value("${bot.name}")
    public String getBotUsername() {
        return "";
    }

    private Message sendMessage(long uid, String text) throws TelegramApiException {
        return execute(SendMessage.builder()
                .chatId(uid)
                .text(text).build());
    }

    private Message sendMessage(long uid, String text, ReplyKeyboard keyboard) throws TelegramApiException {
        return execute(SendMessage.builder()
                .chatId(uid)
                .text(text)
                .replyMarkup(keyboard).build());
    }

    private void editMessageText(Message msg, String newText) throws TelegramApiException {
        execute(EditMessageText.builder()
                .chatId(msg.getChatId())
                .messageId(msg.getMessageId())
                .text(newText).build());
    }
}
