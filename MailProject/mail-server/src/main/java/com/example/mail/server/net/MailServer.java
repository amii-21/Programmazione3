package com.example.mail.server.net;

import com.example.mail.server.model.MailServerModel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class MailServer {

    private ServerSocket serverSocket;
    private final int port;
    private volatile boolean running;

    private ExecutorService pool = Executors.newCachedThreadPool();
    private final MailServerModel model;
    private final Consumer<String> logger;

    public MailServer(int port, MailServerModel model, Consumer<String> logger) {
        this.port = port;
        this.model = model;
        this.logger = logger;
    }

    public void start() {
        running = true;

        if (pool == null || pool.isShutdown() || pool.isTerminated()) {
            pool = Executors.newCachedThreadPool();
        }

        pool.submit(() -> {
            try (ServerSocket ss = new ServerSocket(port)) {
                serverSocket = ss;
                log("Server listening on port " + port);

                while (running) {
                    Socket client = ss.accept();
                    log("Connection from " + client.getRemoteSocketAddress());
                    pool.submit(new ClientHandler(client, model, logger));
                }
            } catch (IOException e) {
                if (running) log("Server error: " + e.getMessage());
            } finally {
                log("Server stopped.");
            }
        });
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed())
                serverSocket.close();
        } catch (IOException ignored) {}
        if(pool != null && !pool.isShutdown()){
            pool.shutdownNow();
        }
        log("Shutdown requested.");
    }

    private void log(String msg) { logger.accept(msg); }
}
