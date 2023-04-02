package ua.dmjdev.TelegramGPTBot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.dmjdev.TelegramGPTBot.GPT.OpenAIResponse;
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
                    add("Profile");
                    add("кнопки");
                    add("пока");
                }});
                add(new KeyboardRow() {{
                    add("не");
                    add("работают");
                }});
            }})
            .resizeKeyboard(true).build();

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
        if (update.hasCallbackQuery()) {
            try {
                onCallbackQueryHandler(update.getCallbackQuery());
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void onCallbackQueryHandler(CallbackQuery callbackQuery) throws TelegramApiException {
        String qid = callbackQuery.getId();
        User queryUser = userRepository.findUserById(callbackQuery.getFrom().getId());
        if (queryUser == null) {
            execute(AnswerCallbackQuery.builder()
                    .callbackQueryId(qid)
                    .text("You must send to bot \"/start\" ")
                    .showAlert(true).build());
            return;
        }
        String data = callbackQuery.getData();
        switch (data) {
            case "remove-context":
                queryUser.setMessages(List.of(new ua.dmjdev.TelegramGPTBot.GPT.Message(Role.SYSTEM,
                        "I am a telegram bot that uses the ChatGPT API and sends your requests to ChatGPT and gives you answers to your questions")));
                userRepository.save(queryUser);
                execute(AnswerCallbackQuery.builder()
                        .callbackQueryId(qid)
                        .text("Context deleted")
                        .showAlert(true).build()
                );
                editMessageTextWithKeyboard(callbackQuery.getMessage(),
                        "Profile\n" +
                                "ID: " + queryUser.getId() + "\n" +
                                "Count of messages: " + queryUser.getCountOfMessages() + "\n" +
                                "Count of messages in context: " + queryUser.getMessages().stream()
                                .filter(m -> m.getRole().equals(Role.USER)).count(),
                        callbackQuery.getMessage().getReplyMarkup());
                break;
        }
    }

    public void onMessageHandler(Message message) throws TelegramApiException {
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
            List<ua.dmjdev.TelegramGPTBot.GPT.Message> chatUserMessages = chatUser.getMessages();
            if (chatUser == null) {
                User user = new User(uid);
                user.getMessages().add(new ua.dmjdev.TelegramGPTBot.GPT.Message(Role.SYSTEM,
                        "I am a telegram bot that uses the ChatGPT API and sends your requests to ChatGPT and gives you answers to your questions"));
                userRepository.save(user);
                sendMessage(uid, "\uD83E\uDD16");
                sendMessage(uid, "Welcome", mainMenu);
                return;
            }
            if (text.startsWith("/start")) {
                sendMessage(uid, "\uD83E\uDD16");
                sendMessage(uid, "Hello", mainMenu);
            } else {
                switch (text) {
                    case "Profile":
                        sendMessage(uid, "Profile\n" +
                                        "ID: " + chatUser.getId() + "\n" +
                                        "Count of messages: " + chatUser.getCountOfMessages() + "\n" +
                                        "Count of messages in context: " + chatUserMessages.stream()
                                        .filter(m -> m.getRole().equals(Role.USER)).count(),
                                InlineKeyboardMarkup.builder()
                                        .keyboard(new ArrayList<>() {{
                                            add(new ArrayList<>() {{
                                                add(InlineKeyboardButton.builder()
                                                        .text("Delete context")
                                                        .callbackData("remove-context").build()
                                                );
                                            }});
                                        }}).build()
                        );
                        return;
                }
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
                    return;
                }
                Message botMessage = sendMessage(uid, "\uD83D\uDCE4");
                chatUserMessages.add(new ua.dmjdev.TelegramGPTBot.GPT.Message(Role.USER, text));
                try {
                    OpenAIResponse response = gptService.getResponse(chatUserMessages);
                    ua.dmjdev.TelegramGPTBot.GPT.Message responseMessage = response.getChoices().get(0);
                    chatUserMessages.add(responseMessage);
//                    if (response.getUsage().getTotalTokens() > 1800) {
//                        for (int i = 0; i < 150; i++) {
//                            i -= chatUserMessages.get(1).getTokensCount();
//                            chatUserMessages.remove(1);
//                        }
//                    }
                    chatUser.incCountOfMessages();
                    userRepository.save(chatUser);
                    editMessageText(botMessage, responseMessage.getContent());
                } catch (Exception e) {
                    e.printStackTrace();
                    editMessageText(botMessage, "❌");
                }
            }
        }
    }

    public Message sendMessage(long uid, String text) throws TelegramApiException {
        return execute(SendMessage.builder()
                .chatId(uid)
                .text(text).build());
    }

    public Message sendMessage(long uid, String text, ReplyKeyboard keyboard) throws TelegramApiException {
        return execute(SendMessage.builder()
                .chatId(uid)
                .text(text)
                .replyMarkup(keyboard).build());
    }

    public void editMessageText(Message msg, String newText) throws TelegramApiException {
        execute(EditMessageText.builder()
                .chatId(msg.getChatId())
                .messageId(msg.getMessageId())
                .text(newText).build());
    }

    public void editMessageTextWithKeyboard(Message msg, String newText, InlineKeyboardMarkup keyboard) throws TelegramApiException {
        execute(EditMessageText.builder()
                .chatId(msg.getChatId())
                .messageId(msg.getMessageId())
                .text(newText).build());
        execute(EditMessageReplyMarkup.builder()
                .chatId(msg.getChatId())
                .messageId(msg.getMessageId())
                .replyMarkup(keyboard).build()
        );
    }
}
