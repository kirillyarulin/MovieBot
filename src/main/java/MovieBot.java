import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class MovieBot extends TelegramLongPollingBot {
    Properties properties = new Properties();
    Map<Long,ResponseMessage> mapResponseMessages;

    public MovieBot() {
        try {
            properties.load(new FileInputStream("src/main/resources/application.properties"));
            mapResponseMessages = new HashMap<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        String textRequest = message.getText();
        ResponseMessage responseMessage;
        long chartId = message.getChatId();

        if (textRequest != null) {
            System.out.println(textRequest);
            textRequest = textRequest.toLowerCase();
            if (mapResponseMessages.containsKey(chartId)) {
                responseMessage = mapResponseMessages.get(chartId);
            } else {
                responseMessage = new ResponseMessage(properties.getProperty("tmdb.api.key"));
                if (!textRequest.equals("/start")) {
                    textRequest = "/start";
                }
            }


            if (textRequest.equals("/genres")) {
                Set<String> genres = responseMessage.getAvailableGenres();
                StringBuilder stringBuilder = new StringBuilder();
                for (String x : genres) {
                    stringBuilder.append(x).append("\n");
                }
                sendMessage(chartId, stringBuilder.toString());
                return;
            }


            if (textRequest.equals("/start")) {
                mapResponseMessages.put(chartId, new ResponseMessage(properties.getProperty("tmdb.api.key")));
                sendMessage(chartId, "Привет. Этот бот поможет тебе выбрать интересный фильм для просмотра.\n" +
                        "Для начала введи временной промежуток (два года через пробел), в котором ты хочешь выбрать фильм");
            } else {
                switch (responseMessage.getState()) {
                    case EMPTY:
                        try {
                            String[] years = textRequest.split("( +)|( *- *)( *, *)");
                            responseMessage.setRangeOfYears(Integer.parseInt(years[0]),Integer.parseInt(years[1]));
                            sendMessage(chartId, "Ну а теперь введи жанр который бы ты хотел посмотреть (также можешь ввести несколько жанров через запятую)\nЧтобы посмотреть список доступных жанров напиши /genres");
                        } catch (RuntimeException e) {
                            sendMessage(chartId, "Я тебя не понимаю. Введи промежуток лет, в котором ты хочешь выбрать фильм (введи два года через пробел)");
                        }
                        break;
                    case WITH_RANGE_OF_YEARS:
                        try {
                            responseMessage.addGenres(textRequest);
                            sendRandomMovie(chartId,responseMessage);
                        } catch (RuntimeException e) {
                            sendMessage(chartId, "Что то я не припоминаю такой жанр. Введи другой");
                        }
                        break;
                    case READY:
                        if (textRequest.equals("/next") || textRequest.equals("еще") || textRequest.equals("ещё")) {
                            sendRandomMovie(chartId,responseMessage);
                        } else if (textRequest.matches("^ *(([1-9])|(\\d\\.\\d*)) *$")) {
                            responseMessage.setVoteAverage(Float.parseFloat(textRequest));
                            sendRandomMovie(chartId,responseMessage);
                        } else if (textRequest.matches("^ *\\d{1,4} +\\d{1,4} *$")) {
                            try {
                                String[] years = textRequest.split(" +");
                                responseMessage.setRangeOfYears(Integer.parseInt(years[0]),Integer.parseInt(years[1]));
                                sendRandomMovie(chartId,responseMessage);
                            } catch (RuntimeException e) {
                                sendMessage(chartId, "Я тебя не совсем понимаю, ты ввел не корректные даты. Попробуй еще раз");
                                sendInfo(chartId);
                            }
                        } else if (textRequest.matches("^ *([А-Яа-я]+),?( *, *[А-Яа-я]+ *)* *$")) {
                            try {
                                responseMessage.addGenres(textRequest);
                                sendRandomMovie(chartId,responseMessage);
                            } catch (RuntimeException e) {
                                sendMessage(chartId, "Я тебя не совсем понимаю, ты ввел не корректные жанры. Попробуй еще раз");
                                sendInfo(chartId);
                            }
                        }
                        else {
                            sendMessage(chartId, "Я тебя не совсем понимаю, попробуй еще раз");
                            sendInfo(chartId);
                        }


                }
            }


        }
    }

    private void sendRandomMovie(long chartId, ResponseMessage responseMessage) {
        try {
            Movie movie = responseMessage.getRandomMovie();
            sendImage(chartId, movie.getPosterPath(), movie.getTitle() + "\nГод: " + movie.getYear() + "\nОценка: " + movie.getVoteAverage());
            if (movie.getOverview() != null && movie.getOverview().length() > 0) {
                sendMessage(chartId, "Описание: " + movie.getOverview());
            }
        } catch (Exception e) {
            sendMessage(chartId,"Хмм, фильмов с такими параметрами очень мало. Можешь попробовать еще а лучше измени какие-нибудь фильтры.");
        }
        sendInfo(chartId);
    }

    private void sendInfo(long chartId) {
        sendMessage(chartId, "Чтобы получить другой случайный фильм напиши /next или \"ещё\".\n" +
                "Чтобы задать минимальную оценку фильма напиши число от 0 до 10.\n" +
                "Чтобы поменять жанры перечисли новые жанры\n" +
                "Чтобы поменять промежуток времени напиши два года через пробел\n");
    }

    public String getBotUsername() {
        return properties.getProperty("botUserName");
    }

    public String getBotToken() {
        return properties.getProperty("accessToken");
    }

    public void sendMessage(Long chartId,String text) {
        try {
            execute(new SendMessage(chartId,text));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendImage(Long chartId, String url) {
        try {
            sendPhoto(new SendPhoto().setChatId(chartId).setPhoto(url));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendImage(Long chartId, String url, String caption) {
        try {
            sendPhoto(new SendPhoto().setChatId(chartId).setPhoto(url).setCaption(caption));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


}
