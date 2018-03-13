package sample.server;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by pc on 11.06.2017.
 */
public class SQLliteDateBase {
    private Connection connection;
    private PreparedStatement addContact;
    private PreparedStatement getContactList;
    private PreparedStatement addFeedback;
    private PreparedStatement addQueries;
    private PreparedStatement showSchemaStatus;
    private PreparedStatement addSchema;
    private PreparedStatement changeSchema;
    private Statement stm;

    //получение данных для таблиц
    public String getSelectAllTable(String table) {
        String s = "";
        if (table.equals("schemas")) s = "SELECT number,schema,cargo,status,date from schemas";
        if (table.equals("feedback")) s = "SELECT id, date,textFeedback,userId,username,firstName,lastName from feedback";
        if (table.equals("contacts")) s = "SELECT id,firstName,lastName,userName,date from contacts";
        if (table.equals("queries")) s = "SELECT id,date,text,userID,firstName from queries";
        return s;
    }

    //подключиться к базе данных
    public void connect() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:schemas.db");
        addContact = connection.prepareStatement("INSERT INTO contacts (id,firstName,lastName,userName,date) VALUES (?,?,?,?,?)");
        getContactList = connection.prepareStatement("SELECT id from contacts");
        addFeedback = connection.prepareStatement("INSERT INTO feedback (date,textFeedback,userId,username,firstName,lastName) VALUES (?,?,?,?,?,?)");
        addQueries = connection.prepareStatement("INSERT INTO queries (date,text,userID,firstName) VALUES (?,?,?,?)");
        showSchemaStatus =connection.prepareStatement("SELECT number, schema, cargo, status, date FROM schemas WHERE number = ?;");
        addSchema = connection.prepareStatement("INSERT INTO schemas (number,schema,cargo,status,date) VALUES (?,?,?,?,?)");
      changeSchema = connection.prepareStatement("UPDATE schemas SET number = ? , schema = ? , cargo = ? , status = ?, date = ? WHERE number = ?");
    }

    //сохранить схему
    public void addSchema(String number,String schema, String cargo, String status, String date){
        try {
            String array[] ={number,schema,cargo,status,date};
            for(int i=0;i<array.length;i++){
                if(array[i]== null||array[i].equals("")){
                    array[i] = "-";
                }
            }
            addSchema.setString(1,array[0]);
            addSchema.setString(2,array[1]);
            addSchema.setString(3,array[2]);
            addSchema.setString(4,array[3]);
            addSchema.setString(5,array[4]);
            addSchema.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //изменить схему
    public void changeSchema(String number,String schema, String cargo, String status, String date){
        try {
            changeSchema.setString(1,number);
            changeSchema.setString(2,schema);
            changeSchema.setString(3,cargo);
            changeSchema.setString(4,status);
            changeSchema.setString(5,date);
            changeSchema.setString(6,number);
            changeSchema.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //проверка наличия схемы с данным номером
    public String doesItExist(String number){
        try {
            showSchemaStatus.setString(1,number);
            ResultSet rs = showSchemaStatus.executeQuery();
            if(rs.next()){//если выцепили номер схемы
                return "yes";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "no";
    }
    //получить статус схемы
    public String getStatus(String number){
        try {
            showSchemaStatus.setString(1,number);
            ResultSet rs = showSchemaStatus.executeQuery();
            if(rs.next()){//если выцепили номер схемы
                String n = rs.getString("number");
                String schema = rs.getString("schema");
                String cargo = rs.getString("cargo");
                String status = rs.getString("status");
                String date = rs.getString("date");
                return "Регистрационный номер заявки: "+n +"\n" + "Номер схемы: " + schema +"\n"+"Груз: "+ cargo  +"\n"+"Статус схемы: " + status +"\n" +"Срок предоставления по договору: " + date;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Заказ с данным номером отсутствует в базе данных.Проверьте правильность ввода данных. Для справки наберите \"?\"";
    }
    //закрыть подключение
    public void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //добавить запрос
    public void addQueries(String date,String text,Long userId,String firstName){
        try {
            String array[] ={date,text,firstName};
            for(int i=0;i<array.length;i++){
                if(array[i]== null||array[i].equals("")){
                    array[i] = "-";
                }
            }
            addQueries.setString(1, array[0]);
            addQueries.setString(2, array[1]);
            addQueries.setLong(3, userId);
            addQueries.setString(4, firstName);
            addQueries.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    //поместить контакт в БД
    public void setAddContact(Long id, String firstName, String lastName, String userName, String date) {
        try {
            String array[] ={firstName,lastName,userName,date};
            for(int i=0;i<array.length;i++){
                if(array[i]== null||array[i].equals("")){
                    array[i] = "-";
                }
            }
            addContact.setLong(1, id);
            addContact.setString(2, array[0]);
            addContact.setString(3, array[1]);
            addContact.setString(4, array[2]);
            addContact.setString(5, array[3]);
            addContact.executeUpdate();
        } catch (SQLException e) {
            //e.printStackTrace();
        }
    }

    //выбрать все контакты из БД
    public ArrayList<Long> getContactList() {
        ArrayList<Long> contactlist = new ArrayList<>();
        try {
            ResultSet rs = getContactList.executeQuery();
            //System.out.println(rs);
            while (rs.next()) {
                Long id = rs.getLong(1);
                contactlist.add(id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //System.out.println(contactlist);
        return contactlist;
    }

    //добавить обратную связь пользователя в БД
    public void addFeedback(String date, String text, Long id, String userName, String firstName, String lastName) {
        try {
            String array[] ={date,text,userName,firstName,lastName};
            for(int i=0;i<5;i++){
                if(array[i]== null||array[i].equals("")){
                    array[i] = "-";
                }
            }
            addFeedback.setString(1, array[0]);
            addFeedback.setString(2, array[1]);
            addFeedback.setLong(3, id);
            addFeedback.setString(4, array[2]);
            addFeedback.setString(5, array[3]);
            addFeedback.setString(6, array[4]);
            addFeedback.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ

    //получить id последней записи
    public int getLastId() {
        int id = 0;
        try {
            stm = connection.createStatement();
            ResultSet rs = stm.executeQuery("SELECT LAST_INSERT_ROWID();");
            if (rs.next()) {
                id = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    //для таблицы
    public ObservableList<ObservableList> getAllInfo(ObservableList<ObservableList> data, TableView tableView, String array[], String sql) {
        try {
            //connection = DriverManager.getConnection("jdbc:sqlite:C:/Users/pc/Desktop/profsouz.db");
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:schemas.db");
            stm = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            tableView.getColumns().clear();
            ResultSet rs = stm.executeQuery(sql);
            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                //We are using non property style for making dynamic table
                final int j = i;
                TableColumn<ObservableList<String>, String> col = new TableColumn<>(rs.getMetaData().getColumnName(i + 1));
                col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(j).toString()));

               col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(j).toString()));
                if(array[i].equals(null) || array[i].equals("")){
                    array[i]="-";
                }
                col.setText(array[i]);
                col.setMinWidth(140);
                tableView.getColumns().addAll(col);
            }
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                //row.removeAll();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    //Iterate Column
                    row.add(rs.getString(i));
                }
                data.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                //if (rs!=null) rs.close();
                if (stm != null) stm.close();
                if (connection != null) connection.close();
            } catch (Exception e) {
            }
        }
        return data;

    }

}
