package com.example.mail.server.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Mailbox implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String account;
    private final List<Email> messages;

    public Mailbox(String account) {
        this.account = account;
        this.messages = new ArrayList<>();
    }

    public String getAccount() { return account; }

    public synchronized List<Email> getMessages() {
        return Collections.unmodifiableList(new ArrayList<>(messages));
    }

    public synchronized void addEmail(Email email) {
        messages.add(email);
    }

    public synchronized void removeEmailById(String id) {
        messages.removeIf(e -> e.getId().equals(id));
    }

    public synchronized List<Email> getMessagesAfter(String lastKnownId) {

        // se non c’è lastKnownId → ritorno tutto
        if (lastKnownId == null || lastKnownId.isEmpty()) {
            return new ArrayList<>(messages);
        }

        List<Email> result = new ArrayList<>();
        boolean found = false;

        for (Email e : messages) {

            // quando trovo id, segno che da ora in poi raccoglierò
            if (e.getId().equals(lastKnownId)) {
                found = true;
                continue; // non includo l'email del lastKnownId
            }

            if (found) {
                result.add(e);
            }
        }

        // se ID non è stato trovato affatto → ritorno tutto
        if (!found) {
            return new ArrayList<>(messages);
        }

        // se trovato ma non ci sono email nuove → ritorno lista vuota
        return result;
    }

}
