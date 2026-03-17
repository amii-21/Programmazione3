package com.example.mail.client.controller;

import com.example.mail.client.model.*;
import com.example.mail.client.util.EmailValidator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MailboxController {

    @FXML private ListView<Email> emailListView;

    @FXML private TextField subjectField;
    @FXML private TextArea bodyArea;

    @FXML private TextField composeRecipients;
    @FXML private TextField composeSubject;
    @FXML private TextArea composeBody;

    @FXML private Label connectionStatus;

    private Mailbox mailbox;
    private ClientConnection connection;
    private ClientFetcherThread fetcher;
    private ClientState state = new ClientState();


    public void initialize() {
        connection = new ClientConnection("localhost", 9999);
    }


    public void setMailbox(Mailbox mailbox) {
        this.mailbox = mailbox;

        emailListView.setItems(mailbox.getInbox());

        emailListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> showEmailDetails()
        );

        fetcher = new ClientFetcherThread(connection, mailbox, state,this);
        fetcher.start();
    }

    @FXML
    private void showEmailDetails() {
            //System.out.println("SHOW DETAILS CALLED");
        Email selected = emailListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            subjectField.clear();
            bodyArea.clear();
            return;
        }

        subjectField.setText(selected.getSubject() != null ? selected.getSubject() : "");
        bodyArea.setText(selected.getBody() != null ? selected.getBody() : "");
    }


    @FXML
    private void deleteEmail() {
        Email selected = emailListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            state.ignoreEmail(selected.getId());
            mailbox.removeEmail(selected);
        }
    }

    @FXML
    private void sendEmail() {

        String recipientsText = composeRecipients.getText();
        String subject = composeSubject.getText();
        String body = composeBody.getText();

        if (recipientsText.isEmpty() || subject.isEmpty() || body.isEmpty()) {
            showAlert("Missing fields", "Fill all fields!");
            return;
        }

        List<String> recipients = Arrays.stream(recipientsText.split(","))
                .map(String::trim).toList();

        for (String r : recipients) {
            if (!EmailValidator.isValid(r)) {
                showAlert("Invalid email", "Recipient: " + r);
                return;
            }
        }

        String request = "SEND\n" +
                "FROM:" + mailbox.getUserEmail() + "\n" +
                "TO:" + recipientsText + "\n" +
                "SUBJECT:" + subject + "\n" +
                "BODY:" + body + "\nEND";

        try {
            String response = connection.sendRequest(request);
            if (response.equalsIgnoreCase("OK")) {

                showAlert("Success", "Email sent!");
                composeRecipients.clear();
                composeSubject.clear();
                composeBody.clear();

            } else if (response.startsWith("ERROR: missing recipients")) {
                String missing = response.replace("ERROR: missing recipients:", "").trim();
                showAlert("Invalid recipient", "These addresses do not exist:\n" + missing);

            } else {
                // qualunque altro tipo di errore
                showAlert("Server error", response);
            }

        } catch (IOException e) {
            updateServerStatus(false);
        }
    }

    @FXML
    private void replyToEmail() {
        Email selected = emailListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        resetComposeFields();
        composeRecipients.setText(selected.getSender());
        composeSubject.setText("RE: " + selected.getSubject());

        composeBody.setText(
                "\n\n--- Previous message ---\n" +
                        "From: " + selected.getSender() + "\n" +
                        "Date: " + selected.getDate() + "\n" +
                        selected.getBody()
        );
    }

    @FXML
    private void replyAll() {
        Email selected = emailListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        resetComposeFields();

        // Recupera TUTTI i destinatari dell'email originale
        List<String> recipients = new ArrayList<>(selected.getRecipients());

        // Aggiungi anche il mittente
        recipients.add(selected.getSender());

        // Rimuovi il tuo indirizzo
        recipients.remove(mailbox.getUserEmail());

        // Rimuovi duplicati
        recipients = recipients.stream().distinct().toList();

        // Concatena in stringa unica
        String finalRecipients = String.join(",", recipients);

        composeRecipients.setText(finalRecipients);
        composeSubject.setText("RE: " + selected.getSubject());
        composeBody.setText(
                "\n\n--- Previous message ---\n" +
                        "From: " + selected.getSender() + "\n" +
                        "Date: " + selected.getDate() + "\n" +
                        selected.getBody()
        );

    }



    @FXML
    private void forwardEmail() {
        Email selected = emailListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        resetComposeFields();

        composeSubject.setText("FWD: " + selected.getSubject());
        composeBody.setText(
                "\n\n--- Forwarded message ---\n" +
                        "From: " + selected.getSender() + "\n" +
                        "To: " + String.join(", ", selected.getRecipients()) + "\n" +
                        "Date: " + selected.getDate() + "\n\n" +
                        selected.getBody()
        );

    }

    @FXML
    private void clearCompose() {
        resetComposeFields();
    }


    public void updateServerStatus(boolean online) {
        if (online) {
            connectionStatus.setText("Server: ONLINE");
            connectionStatus.setStyle("-fx-text-fill: green;");
        } else {
            connectionStatus.setText("Server: OFFLINE");
            connectionStatus.setStyle("-fx-text-fill: red;");
        }
    }

    public void notifyNewEmails(List<Email> emails) {
        if (emails == null || emails.isEmpty()) return;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("You received " + emails.size() + " new email(s)");
        alert.setContentText("From: " + emails.get(0).getSender());
        alert.show();
    }


    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(title);
        a.setContentText(msg);
        a.show();
    }

    private void resetComposeFields() {
        composeRecipients.clear();
        composeSubject.clear();
        composeBody.clear();
    }



}
