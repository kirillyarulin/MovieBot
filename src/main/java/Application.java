import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;

import org.telegram.telegrambots.exceptions.TelegramApiException;


public class Application {

    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi botapi = new TelegramBotsApi();

        try {
            botapi.registerBot(new MovieBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

}
