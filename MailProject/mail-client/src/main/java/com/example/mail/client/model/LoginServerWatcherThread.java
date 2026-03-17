package com.example.mail.client.model;

import com.example.mail.client.controller.LoginController;
import javafx.application.Platform;

public class LoginServerWatcherThread extends Thread {

    private final ClientConnection connection;
    private final LoginController controller;
    private volatile boolean running = true;

    public LoginServerWatcherThread(ClientConnection connection, LoginController controller) {
        this.connection = connection;
        this.controller = controller;
        setDaemon(true);
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(2000);

                boolean online = connection.checkServerAvailable();

                Platform.runLater(() -> controller.updateServerStatus(online));

            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    public void shutdown() {
        running = false;
        interrupt();
    }
}
