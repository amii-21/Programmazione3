package com.example.mail.client.model;

import com.example.mail.client.controller.MailboxController;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ClientFetcherThread extends Thread {

    private final ClientConnection connection;
    private final Mailbox mailbox;
    private final ClientState state;
    private final MailboxController controller;

    private volatile boolean running = true;
    private boolean initialLoadDone = false;

    public ClientFetcherThread(ClientConnection connection,
                               Mailbox mailbox,
                               ClientState state,
                               MailboxController controller) {
        this.connection = connection;
        this.mailbox = mailbox;
        this.state = state;
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

                if (!online) continue;

                String request =
                        "FETCH_NEW:" + mailbox.getUserEmail() + ":" + state.getLastEmailId();

                String response = connection.sendRequest(request);

                if (response == null || response.isBlank() || response.equals("EMPTY")) {
                    continue;
                }

                List<Email> fetched = parseEmails(response);
                if (fetched.isEmpty()) continue;

                if (!initialLoadDone) {
                    Platform.runLater(() -> mailbox.addEmails(fetched));

                    state.updateLastReceived(fetched);

                    initialLoadDone = true;
                    continue;
                }

                List<Email> trulyNew = new ArrayList<>();

                for (Email e : fetched) {
                    if (!mailbox.containsEmail(e.getId()) &&
                            !state.isIgnored(e.getId())) {
                        trulyNew.add(e);
                    }
                }

                if (!trulyNew.isEmpty()) {
                    Platform.runLater(() -> {
                        mailbox.addEmails(trulyNew);
                        controller.notifyNewEmails(trulyNew); // ORA sì
                    });
                    state.updateLastReceived(trulyNew);
                }

            } catch (Exception e) {
                Platform.runLater(() -> controller.updateServerStatus(false));
            }
        }
    }

    public void shutdown() {
        running = false;
        interrupt();
    }

    private List<Email> parseEmails(String raw) throws IOException {
        List<Email> list = new ArrayList<>();

        if (!raw.contains("EMAILS")) return list;

        BufferedReader reader = new BufferedReader(new StringReader(raw));
        String line;

        String id = null, from = null, subject = null, body = "";
        List<String> recipients = new ArrayList<>();
        LocalDateTime date = null;

        while ((line = reader.readLine()) != null) {

            if (line.startsWith("ID:")) id = line.substring(3);
            else if (line.startsWith("FROM:")) from = line.substring(5);
            else if (line.startsWith("TO:")) recipients = List.of(line.substring(3).split(","));
            else if (line.startsWith("SUBJECT:")) subject = line.substring(8);
            else if (line.startsWith("BODY:")) body = line.substring(5).replace("\\n", "\n");
            else if (line.startsWith("DATE:")) date = LocalDateTime.parse(line.substring(5));
            else if (line.equals("END_EMAIL")) {
                list.add(new Email(id, from, recipients, subject, body, date));

                id = from = subject = body = null;
                recipients = new ArrayList<>();
            }
        }

        return list;
    }
}
