package ua.dmjdev.TelegramGPTBot.config;

import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ua.dmjdev.TelegramGPTBot.service.TelegramBot;

@Component
@PropertySource("application.properties")
public class BotInitializer {
    private final BotConfig config;
    private final TelegramBot bot;

    public BotInitializer(BotConfig config, TelegramBot bot) {
        this.config = config;
        this.bot = bot;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        TelegramBotsApi telegramBotsApi = null;
        try {
            telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(bot);
            for (long id : config.getADMINS()) {
                bot.sendMessage(id, "Bot started");
            }
            bot.addLog("Bot started");
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

}
