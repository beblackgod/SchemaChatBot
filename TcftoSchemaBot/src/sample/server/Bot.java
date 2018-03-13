package sample.server;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Created by pc on 07.06.2017.
 */
public class Bot extends TelegramLongPollingBot {
    private SQLliteDateBase dateBase;

    @Override
    public void onUpdateReceived(Update update) {

        String msg = update.getMessage().getText();
        SendMessage sendMessage = new SendMessage().setChatId(update.getMessage().getChatId());

        String botAnswer = "-";
        String url = "";
        addQueries(update);

        if (msg.startsWith("?")) {
            botAnswer = help();
        } else if (msg.startsWith("/start") || msg.startsWith("/Start") || msg.startsWith("start")) {
            addContact(update);
            botAnswer = help();
        } else if (msg.startsWith("связь") || msg.startsWith("Связь") || msg.startsWith("\uD83D\uDC4D")) {
            botAnswer = getFeedback(update);
        } else if (msg.startsWith("спасибо") || msg.startsWith("Спасибо") || msg.startsWith("ок") || msg.startsWith("Ок")) {
            botAnswer = "Был рад Вам помочь!";
        } else {
            botAnswer = getStatus(msg);
        }
        sendMessage.setText(botAnswer);
        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return null;
    }

    @Override
    public String getBotToken() {

        return "****************************************************"; //secret TOKEN

    }

    //рассылка конкретному пользователю
    public void sendMsg(String text, String id) {
        SendMessage sendMessage = new SendMessage().setChatId(id);
        sendMessage.setText(text);
        try {
            sendMessage(sendMessage);
        } catch (TelegramApiException e) {
            showAlert("Не удалось отправить текст,либо текст не был прикреплен.");
        }
    }

    //показать информацию в всплывающем окне
    public void showAlert(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Информационное сообщение");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    //статус схемы
    public String getStatus(String number) {
        dateBase = new SQLliteDateBase();
        String s = "-";
        try {
            dateBase.connect();
            s = dateBase.getStatus(number);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            dateBase.disconnect();
        }
        return s;
    }

    //команда ?
    public String help() {
        String s = "Здравствуйте!\nЯ бот-помощник Северного ТЦФТО по услуге разработка и согласование схем погрузки.\n\nНаберите регистрационный номер, который сообщил Вам сотрудник при регистрации заявки и я направлю Вам статус исполнения Вашей заявки в режиме онлайн.\n\n"+"Также Вы можете оставить обратную связь о данной услуге и о работе чат-бота. Для этого введите команду:\n"+"связь [ваш текст] "+"\n\n" + "\"?\" - отправьте для получения повторной справки\n\n" + "Я доступен с 08:00 до 17:00 по московскому времени. Буду рад Вам помочь!";
        return s;
    }

    //команда start
    public void addContact(Update update) {
        dateBase = new SQLliteDateBase();
        try {
            dateBase.connect();
            Long id = update.getMessage().getChatId();
            String firstName = update.getMessage().getFrom().getFirstName();
            String lastName = update.getMessage().getFrom().getLastName();
            String userName = update.getMessage().getFrom().getUserName();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String date = LocalDate.now().format(formatter);
            dateBase.setAddContact(id, firstName, lastName, userName, date);
        } catch (SQLException e) {
            //e.printStackTrace();
        } catch (ClassNotFoundException e) {
           // e.printStackTrace();
        }finally {
            dateBase.disconnect();
        }
    }


    //команда связь
    public String getFeedback(Update update) {
        dateBase = new SQLliteDateBase();
        try {
            dateBase.connect();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String date = LocalDate.now().format(formatter);
            String text = update.getMessage().getText();
            Long id = update.getMessage().getChatId();
            String userName = update.getMessage().getFrom().getUserName();
            String firstName = update.getMessage().getFrom().getFirstName();
            String lastName = update.getMessage().getFrom().getLastName();
            dateBase.addFeedback(date, text, id, userName, firstName, lastName);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            dateBase.disconnect();
        }
        String s = "Вы оставили обратную связь. Благодаря Вашему участию мы будем совершенствовать услугу по разработке и согласованию схем погрузки и самого чат-бота!\uD83D\uDC4C\nНа всякий случай, убедитесь, что правильно использовали команду. Сообщение должно начинаться со слово связь, далее в нем же текст.";
        return s;
    }

    //команда справка
    public String getInfo() {
        String s = "Сейчас мы занимаемся заполнением данного раздела. Здесь будет самая полезная информация. Может быть у Вас есть идеи, что бы Вы хотели здесь видеть? Воспользуйтесь командой \"связь\".\uD83D\uDCD4";
        return s;
    }

    //запись запроса для дальнейшего анализа
    public void addQueries(Update update) {
        dateBase = new SQLliteDateBase();
        try {
            dateBase.connect();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String date = LocalDate.now().format(formatter);
            String text = update.getMessage().getText();
            Long id = update.getMessage().getChatId();
            String firstName = update.getMessage().getFrom().getFirstName();
            dateBase.addQueries(date, text,id,firstName);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            dateBase.disconnect();
        }
    }
}
