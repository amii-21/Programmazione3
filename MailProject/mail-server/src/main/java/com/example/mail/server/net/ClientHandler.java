package com.example.mail.server.net;

import com.example.mail.server.model.Email;
import com.example.mail.server.model.MailServerModel;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final MailServerModel model;
    private final Consumer<String> logger;

    public ClientHandler(Socket socket, MailServerModel model, Consumer<String> logger) {
        this.socket = socket;
        this.model = model;
        this.logger = logger;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String line = in.readLine();
            if (line == null) return;

            if (line.startsWith("CHECK_USER:")) {
                handleCheckUser(line, out);
            }
            else if (line.equals("SEND")) {
                handleSend(in, out);
            }
            else if (line.startsWith("FETCH_NEW:")) {
                handleFetchNew(line, out);
            }
            else {
                out.println("ERROR: unknown command");
            }

        } catch (Exception e) {
            logger.accept("Client error: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignore) {}
        }
    }

    private void handleCheckUser(String line, PrintWriter out) {
        String user = line.substring("CHECK_USER:".length()).trim();

        if (model.userExists(user)) {
            out.println("OK");
            logger.accept("[CHECK_USER] OK → " + user);
        } else {
            out.println("ERROR: user not found");
            logger.accept("[CHECK_USER] FAIL → " + user);
        }
    }

    private void handleSend(BufferedReader in, PrintWriter out) throws IOException {

        String from = null;
        String to = null;
        String subject = "";
        StringBuilder body = new StringBuilder();

        String line;
        while ((line = in.readLine()) != null && !line.equals("END")) {

            if (line.startsWith("FROM:")) {
                from = line.substring(5).trim();
            }
            else if (line.startsWith("TO:")) {
                to = line.substring(3).trim();
            }
            else if (line.startsWith("SUBJECT:")) {
                subject = line.substring(8).trim();
            }
            else if (line.startsWith("BODY:")) {
                body.append(line.substring(5)).append("\n");
            }
            else {
                body.append(line).append("\n");
            }
        }

        if (from == null || to == null) {
            out.println("ERROR: missing data");
            logger.accept("[SEND] ERROR – dati mancanti");
            return;
        }

        List<String> recipients = Arrays.stream(to.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        Email mail = new Email(
                from,
                recipients,
                subject,
                body.toString().trim(),
                LocalDateTime.now()
        );

        List<String> missing = model.deliverEmail(mail);

        if (missing.isEmpty()) {
            out.println("OK");
            logger.accept("[SEND] Delivered – from " + from + " to " + to);
        } else {
            out.println("ERROR: missing recipients: " + String.join(",", missing));
            logger.accept("[SEND] FAILED – Missing recipients " + missing);
        }
    }

    private void handleFetchNew(String line, PrintWriter out) {

        String[] parts = line.split(":");
        if (parts.length < 2) {
            out.println("ERROR: malformed command");
            return;
        }

        String user = parts[1].trim();
        String lastId = (parts.length >= 3) ? parts[2].trim() : "";

        if (!model.userExists(user)) {
            out.println("ERROR: user not found");
            logger.accept("[FETCH_NEW] FAIL – " + user);
            return;
        }

        List<Email> newEmails = model.fetchNew(user, lastId);

        if (newEmails.isEmpty()) {
            out.println("EMPTY");
            return;
        }

        out.println("EMAILS");

        for (Email e : newEmails) {
            out.println("ID:" + e.getId());
            out.println("FROM:" + e.getSender());
            out.println("TO:" + String.join(",", e.getRecipients()));
            out.println("SUBJECT:" + e.getSubject());
            out.println("BODY:" + e.getBody().replace("\n", "\\n"));
            out.println("DATE:" + e.getSentDate());
            out.println("END_EMAIL");
        }

        out.println("END");

        logger.accept("[FETCH_NEW] Delivered " + newEmails.size() + " emails to " + user);
    }
}
