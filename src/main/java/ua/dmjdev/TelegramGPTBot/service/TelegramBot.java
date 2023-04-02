package ua.dmjdev.TelegramGPTBot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.dmjdev.TelegramGPTBot.GPT.Role;
import ua.dmjdev.TelegramGPTBot.config.BotConfig;
import ua.dmjdev.TelegramGPTBot.models.User;
import ua.dmjdev.TelegramGPTBot.repo.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@PropertySource("application.properties")
public class TelegramBot extends TelegramLongPollingBot {
    private final UserRepository userRepository;
    private final ChatGPTService gptService;
    private final BotConfig config;

    public TelegramBot(@Value("${bot.token}") String token, ChatGPTService gptService, BotConfig config, UserRepository userRepository) {
        super(token);
        this.gptService = gptService;
        this.config = config;
        this.userRepository = userRepository;
    }

    private final ReplyKeyboardMarkup mainMenu = ReplyKeyboardMarkup.builder()
            .keyboard(new ArrayList<>() {{
                add(new KeyboardRow() {{
                    add("Тестовые");
                    add("кнопки");
                    add("пока");
                }});
                add(new KeyboardRow() {{
                    add("не");
                    add("работают");
                }});
            }}).build();

    @Override
    public String getBotUsername() {
        return config.getUsername();
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
            User chatUser = userRepository.findUserById(uid);
            if (chatUser == null) {
                User user = new User(uid);
                user.getMessages().add(new ua.dmjdev.TelegramGPTBot.GPT.Message(Role.system,
                        "I'm telegram which use ChatGPT Api and send your queries to ChatGPT and give you answer"));
                userRepository.save(user);
                if (text.startsWith("/start")) {
                    sendMessage(uid, "\uD83E\uDD16");
                    sendMessage(uid, "Welcome", mainMenu);
                    return;
                }
            }
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
                chatUser.getMessages().add(new ua.dmjdev.TelegramGPTBot.GPT.Message(Role.user, text));
                try {
                    ua.dmjdev.TelegramGPTBot.GPT.Message response = gptService.getResponse(chatUser.getMessages());
                    chatUser.getMessages().add(response);
                    userRepository.save(chatUser);
                    editMessageText(responseMessage, response.getContent());
                } catch (Exception e) {
                    e.printStackTrace();
                    editMessageText(responseMessage, "❌");
                }
            }
        }
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
