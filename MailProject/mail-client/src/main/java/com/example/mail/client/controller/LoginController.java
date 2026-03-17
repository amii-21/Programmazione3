package com.example.mail.client.controller;

import com.example.mail.client.model.ClientConnection;
import com.example.mail.client.model.LoginServerWatcherThread;
import com.example.mail.client.model.Mailbox;
import com.example.mail.client.util.EmailValidator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private Label errorLabel;
    @FXML private Label serverStatus;
    @FXML private LoginServerWatcherThread watcher;


    private final ClientConnection connection = new ClientConnection("localhost", 9999);
/*
    @FXML
    private void initialize() {
        serverStatus.setText("Checking server...");

        new Thread(() -> {
            boolean online = connection.checkServerAvailable();
            Platform.runLater(() -> {
                if (online) {
                    serverStatus.setText("Server online");
                    serverStatus.setStyle("-fx-text-fill: green;");
                } else {
                    serverStatus.setText("Server offline");
                    serverStatus.setStyle("-fx-text-fill: red;");
                }
            });
        }).start();
    }*/
@FXML
private void initialize() {
    watcher = new LoginServerWatcherThread(connection, this);
    watcher.start();
}
    public void updateServerStatus(boolean online) {
        if (online) {
            serverStatus.setText("Server: ONLINE");
            serverStatus.setStyle("-fx-text-fill: green;");
        } else {
            serverStatus.setText("Server: OFFLINE");
            serverStatus.setStyle("-fx-text-fill: red;");
        }
    }


    @FXML
    private void handleLogin() {

        String email = emailField.getText().trim();

        if (!EmailValidator.isValid(email)) {
            errorLabel.setText("Invalid email format.");
            return;
        }

        new Thread(() -> {
            String response;

            try {
                response = connection.sendRequest("CHECK_USER:" + email);
            } catch (Exception e) {
                Platform.runLater(() -> {
                    errorLabel.setText("Server unreachable.");
                    serverStatus.setText("Server offline");
                    serverStatus.setStyle("-fx-text-fill: red;");
                });
                return;
            }

            if (response.equals("OK")) {
                Platform.runLater(() -> openMailbox(email));
            } else {
                Platform.runLater(() -> errorLabel.setText("User does not exist."));
            }

        }).start();
    }

    private void openMailbox(String email) {
        if (watcher != null) watcher.shutdown();

        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/MailboxView.fxml"));

            Scene scene = new Scene(loader.load());

            MailboxController controller = loader.getController();
            controller.setMailbox(new Mailbox(email));

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Mailbox - " + email);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
