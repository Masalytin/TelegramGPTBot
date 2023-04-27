package ua.dmjdev.TelegramGPTBot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.LeaveChat;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
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

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
                }});
                add(new KeyboardRow() {{
                    add("Help");
                    add("Stats");
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
            if (!update.getMessage().isUserMessage())
                return;
            new Thread(() -> {
                MethodThread thread = new MethodThread(update.getMessage().getFrom().getId(), () -> {
                    try {
                        onMessageHandler(update.getMessage());
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                });
                while (Thread.getAllStackTraces().keySet().stream()
                        .filter(t -> t instanceof MethodThread && ((MethodThread) t).userId == thread.userId)
                        .limit(1).toList().size() != 0) {
                    try {
                        Thread.sleep(new Random().nextInt(30, 400));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                thread.start();
            }).start();
        }
        if (update.hasCallbackQuery()) {
            new Thread(() -> {
                MethodThread thread = new MethodThread(update.getCallbackQuery().getFrom().getId(), () -> {
                    try {
                        onCallbackQueryHandler(update.getCallbackQuery());
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                });
                while (Thread.getAllStackTraces().keySet().stream()
                        .filter(t -> t instanceof MethodThread && ((MethodThread) t).userId == thread.userId)
                        .limit(1).toList().size() != 0) {
                    try {
                        Thread.sleep(new Random().nextInt(30, 400));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                thread.start();
            }).start();
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
                        .showAlert(true).build());
                editMessageTextWithKeyboard(callbackQuery.getMessage(),
                        "Profile\n" +
                                "ID: " + queryUser.getId() + "\n" +
                                "Count of messages: " + queryUser.getCountOfMessages(),
                        callbackQuery.getMessage().getReplyMarkup());
                break;
        }
    }

    public void onMessageHandler(Message message) throws TelegramApiException {
        if (message.hasText()) {
            if (message.getChat().isGroupChat()) {
                sendMessage(message.getChatId(), "I don't working in group chats. Bye");
                execute(LeaveChat.builder()
                        .chatId(message.getChatId())
                        .build());
                return;
            }
            String text = message.getText();
            long userID = message.getFrom().getId();
            User chatUser = userRepository.findUserById(userID);
            if (chatUser == null) {
                User user = new User(userID);
                user.getMessages().add(new ua.dmjdev.TelegramGPTBot.GPT.Message(Role.SYSTEM,
                        "I am a telegram bot that uses the ChatGPT API and sends your requests to ChatGPT and gives you answers to your questions"));
                userRepository.save(user);
                sendMessage(userID, "\uD83E\uDD16");
                sendMessage(userID, "Welcome", mainMenu);
                return;
            }
            List<ua.dmjdev.TelegramGPTBot.GPT.Message> chatUserMessages = chatUser.getMessages();
            if (text.startsWith("/start")) {
                sendMessage(userID, "\uD83E\uDD16");
                sendMessage(userID, "Hello", mainMenu);
            } else {
                if (execute(GetChatMember.builder()
                        .chatId("-1001875522662")
                        .userId(userID).build()).getStatus().equals("left")) {
                    sendMessage(userID, "You need to join our Channel for use this bot", InlineKeyboardMarkup.builder()
                            .keyboardRow(new ArrayList<>() {{
                                add(InlineKeyboardButton.builder()
                                        .text("Join to channel")
                                        .url("https://t.me/+-CsMVIyMQGFjZDM6").build()
                                );
                            }}).build());
                    return;
                }
                switch (text) {
                    case "Profile":
                    case "/profile":
                        sendProfile(chatUser);
                        return;
                    case "Help":
                    case "/help":
                        sendInfoMessage(userID);
                        return;
                    case "Stats":
                    case "/stats":
                        sendMessage(userID, "Count of users: " + userRepository.count() + "\n" +
                                "Count of messages: " + userRepository.findAll().stream()
                                .map(u -> u.getCountOfMessages())
                                .reduce(0, (sum, c) -> sum += c));
                        return;
                    case "/reset":
                        clearMessages(chatUser);
                        return;
                }
                if (text.length() > 350) {
                    sendMessage(userID, "Max text size 350 symbols");
                    return;
                }
                Message botMessage = sendMessage(userID, "\uD83D\uDCE4");
                chatUserMessages.add(new ua.dmjdev.TelegramGPTBot.GPT.Message(Role.USER, text));
                try {
                    ua.dmjdev.TelegramGPTBot.GPT.Message responseMessage = gptService.getResponse(chatUserMessages);
                    editMessageText(botMessage, responseMessage.getContent());
                    chatUserMessages.add(responseMessage);
                    chatUser.incCountOfMessages();
                    while (chatUserMessages.size() >= 5) {
                        chatUserMessages.remove(1);
                    }
                    userRepository.save(chatUser);
                } catch (Exception e) {
                    e.printStackTrace();
                    editMessageText(botMessage, "❌");
                }
            }
        }
    }

    private void sendInfoMessage(long userID) throws TelegramApiException {
        sendMessage(userID, """
                INFO
                                
                 I am a Telegram chatbot that uses ChatGPT, one of the latest in Artificial Intelligence language models, to answer any questions you may have. I am designed to provide helpful and informative replies to your requests. Simply start a conversation with me and type out your question, and I will respond as accurately and quickly as possible.
                 
                 You can ask me anything you want, and I will do my best to provide a suitable response. If I am unable to understand your request or provide an appropriate answer, I will let you know. Feel free to engage with me, share your thoughts, or ask me for recommendations.\s
                 
                 I'm continuously learning and updating my knowledge, so I get better with time. I'm here to assist you in any way I can.ae
                                
                Commands:
                - /start: This command reloads the bot
                                
                - /profile: Use this command to view your current profile
                                
                - /reset: This command resets the context and any earlier interactions you might have had with the bot. It can be useful when you're trying to begin a new conversation or start over with a new topic.
                                
                - /help: This command will provide you with all of the instructions and information for the bot, including its purpose, features, and how to engage with it - this is what you're reading right now!
                """);
    }

    private void clearMessages(User chatUser) throws TelegramApiException {
        chatUser.setMessages(List.of(new ua.dmjdev.TelegramGPTBot.GPT.Message(Role.SYSTEM,
                "I am a telegram bot that uses the ChatGPT API and sends your requests to ChatGPT and gives you answers to your questions")));
        userRepository.save(chatUser);
        sendMessage(chatUser.getId(), "Context cleared");
    }

    public Message sendMessage(long userID, String text) throws TelegramApiException {
        return execute(SendMessage.builder()
                .chatId(userID)
                .text(text)
                .parseMode("HTML").build());
    }

    public Message sendMessage(long userID, String text, ReplyKeyboard keyboard) throws TelegramApiException {
        return execute(SendMessage.builder()
                .chatId(userID)
                .text(text)
                .replyMarkup(keyboard)
                .parseMode("HTML").build());
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

    private void sendProfile(User user) throws TelegramApiException {
        execute(SendPhoto.builder()
                .chatId(user.getId())
                .photo(new InputFile(Paths.get("src/main/resources/images/gpt-logo.png").toFile()))
                .caption("Profile\n" +
                        "ID: " + user.getId() + "\n" +
                        "Count of messages: " + user.getCountOfMessages())
                .replyMarkup(InlineKeyboardMarkup.builder()
                        .keyboard(new ArrayList<>() {{
                            add(new ArrayList<>() {{
                                add(InlineKeyboardButton.builder()
                                        .text("Delete context")
                                        .callbackData("remove-context").build()
                                );
                            }});
                        }}).build())
                .build());
    }

    private static class MethodThread extends Thread {
        private long userId;

        public MethodThread(long userId, Runnable runnable) {
            super(runnable);
            this.userId = userId;
        }
    }
}
