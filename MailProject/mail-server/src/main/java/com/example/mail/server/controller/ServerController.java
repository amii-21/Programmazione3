package com.example.mail.server.controller;

import com.example.mail.server.model.MailServerModel;
import com.example.mail.server.net.MailServer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.File;

public class ServerController {

    @FXML private Label portLabel;
    @FXML private TextArea logArea;
    @FXML private ListView<String> accountsList;
    @FXML private Button startButton;
    @FXML private Button stopButton;

    private MailServerModel model;
    private MailServer server;
    private final int fixedPort = 9999;
    private final File persistenceFile = new File("mailboxes.dat");

    public void initialize() {
        model = new MailServerModel(persistenceFile);
        portLabel.setText(String.valueOf(fixedPort));
        updateButtons(false);
        refreshAccounts();
        appendLog("Mail server model initialized.");
    }

    @FXML
    private void startServer() {
        if(server == null) {
            server = new MailServer(fixedPort, model, this::appendLog);
        }
        server.start();
        appendLog("Starting server on port " + fixedPort + "...");
        updateButtons(true);
    }

    @FXML
    private void stopServer() {
        if (server != null) {
            server.stop();
            appendLog("Server stop requested.");
        }
        updateButtons(false);
    }

    private void refreshAccounts() {
        Platform.runLater(() -> {
            accountsList.getItems().setAll(model.listAccounts());
        });
    }

    private void updateButtons(boolean serverRunning) {
        startButton.setDisable(serverRunning);
        stopButton.setDisable(!serverRunning);
    }

    private void appendLog(String msg) {
        Platform.runLater(() -> {
            logArea.appendText(msg + "\n");
            refreshAccounts();
        });
    }
}
