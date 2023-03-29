package ua.dmjdev.TelegramGPTBot;

import org.springframework.beans.factory.annotation.Autowired;
import ua.dmjdev.TelegramGPTBot.service.TelegramBot;

public class MessageHandler implements Runnable {
    @Autowired
    TelegramBot bot;

    @Override
    public void run() {

    }

    public void sendMessage() {
        run();
    }
}
