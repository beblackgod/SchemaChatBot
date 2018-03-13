package sample.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import sample.server.Bot;
import sample.server.SQLliteDateBase;
import sample.server.Server;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    TextField tfUrl;
    @FXML
    Button btnStartServer;
    @FXML
    Button btnSchemas;

    @FXML
    Button btnFeedback;
    @FXML
    Button btnContacts;
    @FXML
    TableView tvTable;
    @FXML
    TextArea taText;
    @FXML
    HBox authPanel;
    @FXML
    HBox mainPanel;
    @FXML
    Button btnAuth;
    @FXML
    TextField tfLogin;
    @FXML
    PasswordField pfPass;
    @FXML
    HBox broadcastPanel;
    @FXML
    HBox sendMsgPanel;
    @FXML
    VBox schemaPanel;
    @FXML
    TextField tfRegNumber;
    @FXML
    TextField tfSchemaNumber;
    @FXML
    TextField tfCargo;
    @FXML
    TextField tfStatus;
    @FXML
    TextField tfDate;
    @FXML
    Button btnChange;
    @FXML
    Button btnSave;
    @FXML
    Button btnClear;

    @FXML
    TextField tfMsg;
    @FXML
    TextField tfID;
    @FXML
    ComboBox cmbStatus;

    private Bot bot;
    private boolean isServerStarted = false;
    private boolean isMainPage;
    private boolean authorized;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthorized(false);
    }

    //КНОПКИ
    //запустить сервер, который вызывает бота
    public void startServer() {
        if (!isServerStarted) {
            try {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Server s = new Server();
                        bot = s.getBot();
                    }
                });
                t.setDaemon(true);
                t.start();
                isServerStarted = true;
                showSchemasTable();
                showAlert("Сервер запущен. Чат-бот доступен в сети Интернет.");
            } catch (Exception e) {
                showAlert("Не удалось подключиться к серверу.");
            }
        } else {
            showAlert("Сервер уже запущен.");
        }
    }

    //отправить сообщение конкретному пользователю
    public void sendMessage() {
        if (isServerStarted) {
            try {
                String id = tfID.getText();
                String text = tfMsg.getText();
                bot.sendMsg(text, id);
                tfMsg.clear();
                tfID.clear();
            } catch (Exception e) {
                showAlert("Не удалось совершить отправку, проверьте соединение с сервером.");
                e.printStackTrace();
            }
        } else {
            showAlert("Не удалось совершить отправку, сервер не запущен. Чат бот не в сети.");
        }
    }

    //показать таблицу schemas
    public void showSchemasTable() {
        clear();
        setMainPage(true);
        loadTable("schemas", new String[]{"Номер", "Номер схемы", "Груз", "Статус", "Даты"});
    }

    //показать таблицу feedback
    public void showFeedbackTable() {
        setMainPage(false);
        loadTable("feedback", new String[]{"Номер", "Дата", "Текст сообщения", "id пользователя", "username", "Имя", "Фамилия"});
    }

    //показать таблицу contacts
    public void showContactsTable() {
        setMainPage(false);
        loadTable("contacts", new String[]{"id", "Имя", "Фамилия", "username", "Дата добавления"});
    }
    //показать таблицу queries
    public void showQueriesTable() {
        setMainPage(false);
        loadTable("queries", new String[]{"Номер", "Дата", "Текст","id","Имя"});
    }

    //очистить поля с инф-цией по схеме
    public void clear() {
        tfRegNumber.clear();
        tfSchemaNumber.clear();
        tfCargo.clear();
        tfStatus.clear();
        tfDate.clear();
    }

    //сохранить новую заявку либо изменения в существующей
    public void save() {
        if (tfRegNumber.getText().equals("")) {
            showAlert("Проверьте правильность ввода данных.");
        } else {
            String number = tfRegNumber.getText();
            String schema = tfSchemaNumber.getText();
            String cargo = tfCargo.getText();
            String status = tfStatus.getText();
            String date = tfDate.getText();
            SQLliteDateBase dateBase = new SQLliteDateBase();
            try {
                dateBase.connect();
                String exist = dateBase.doesItExist(number);
                System.out.println("exist: " + exist);
                //если новая
                if(exist.equals("no")){
                dateBase.addSchema(number, schema, cargo, status, date);
                dateBase.disconnect();
                }
                //если вносим изменения в существующую
                else{
                    dateBase.changeSchema(number, schema, cargo, status, date);
                    dateBase.disconnect();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        showSchemasTable();
    }

    //ФУНКЦИИ
    //загрузка таблицы из базы данных
    public void loadTable(String table, String array[]) {
        taText.clear();
        Platform.runLater(() -> {
            SQLliteDateBase dateBase = new SQLliteDateBase();
            try {
                ObservableList<ObservableList> tableList;
                dateBase.connect();
                tableList = FXCollections.observableArrayList();//инициализировали пустую коллекцию
                tableList = dateBase.getAllInfo(tableList, tvTable, array, dateBase.getSelectAllTable(table));
                tvTable.getItems().clear();
                tvTable.setItems(tableList);//привязали элемент управления к коллекции

            } catch (Exception e) {
                showAlert("Не удалось отобразить таблицу.");
                e.printStackTrace();
            } finally {
                dateBase.disconnect();
            }

        });
    }

    //отображение из таблицы в textarea и в поля
    public void selectRowinTable() {
        taText.clear();
        try {
            String s = (tvTable.getSelectionModel().getSelectedItem()).toString();
            String array[] = s.split(",");
            taText.appendText(array[1] + "\n");
            for (int i = 2; i < array.length; i++) {
                taText.appendText(array[i]);
            }
        } catch (Exception e) {
        }

        try {
            String s = (tvTable.getSelectionModel().getSelectedItem()).toString();
            String array[] = s.split(",");
            String sFirst = "";
            for (int i = 1; i < 8; i++) {
                sFirst += array[0].charAt(i);
            }
            String sSecond = "";
            for (int i = 1; i < array[1].length(); i++) {
                sSecond += array[1].charAt(i);
            }
            String sThird = "";
            for (int i = 1; i < array[2].length(); i++) {
                sThird += array[2].charAt(i);
            }
            String sForth = "";
            for (int i = 1; i < array[3].length(); i++) {
                sForth += array[3].charAt(i);
            }
            String sFifth = "";
            for (int i = 1; i < 11; i++) {
                sFifth += array[4].charAt(i);
            }
            tfRegNumber.setText(sFirst);
            tfSchemaNumber.setText(sSecond);
            tfCargo.setText(sThird);
            tfStatus.setText(sForth);
            tfDate.setText(sFifth);
        } catch (Exception e) {
        }


    }

    //выпадающий список
    public void showStatus() {
        String array[] = {"Схема находится в разработке.", "Схема на рассмотрении у грузоотправителя.", "Схема находится на проверке.", "Схема находится на подписании у грузоотправителя.", "Схема находится на согласовании со стороны ОАО \"РЖД.\"", "Согласованная схема предоставлена на станцию и грузоотправителю.", "Схема ожидает оплаты."};
        cmbStatus.getItems().clear();
        ObservableList<String> obslist = FXCollections.observableArrayList(array);
        cmbStatus.setItems(obslist);
    }

    //выбрать статус из выпадающего списка
    public void changeStatus(){
        try {
            tfStatus.setText((cmbStatus.getValue().toString()));
        } catch (Exception e) {
        }
    }

    //метод, позволяющий показывать всплывающие окна
    public void showAlert(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Информационное сообщение");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    //управление панелью
    public void setMainPage(boolean isMainPage) {
        this.isMainPage = isMainPage;
        if (isMainPage) {
            broadcastPanel.setVisible(true);
            broadcastPanel.setManaged(true);
            sendMsgPanel.setVisible(false);
            sendMsgPanel.setManaged(false);
            taText.setVisible(false);
            taText.setManaged(false);
            schemaPanel.setVisible(true);
            schemaPanel.setManaged(true);

        } else {
            broadcastPanel.setVisible(false);
            broadcastPanel.setManaged(false);
            sendMsgPanel.setVisible(true);
            sendMsgPanel.setManaged(true);
            taText.setVisible(true);
            taText.setManaged(true);
            schemaPanel.setVisible(false);
            schemaPanel.setManaged(false);
        }
    }

    //управление панелью 2
    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
        if (authorized) {
            authPanel.setVisible(false);
            authPanel.setManaged(false);
            mainPanel.setVisible(true);
            mainPanel.setManaged(true);
        } else {
            authPanel.setVisible(true);
            authPanel.setManaged(true);
            mainPanel.setVisible(false);
            mainPanel.setManaged(false);
        }

    }

    //авторизация
    public void authorize() {
         if (tfLogin.getText().equals("") & pfPass.getText().equals("")) {
        setAuthorized(true);
        setMainPage(true);
        } else {
            showAlert("Не верные логин, пароль. Проверьте правильность ввода данных.");
        }
    }
}
