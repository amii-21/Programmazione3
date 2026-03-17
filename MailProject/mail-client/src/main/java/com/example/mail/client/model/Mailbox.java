package com.example.mail.client.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashSet;
import java.util.Set;

public class Mailbox {

    private final String userEmail;

    private final ObservableList<Email> inbox = FXCollections.observableArrayList();

    // email eliminate → non vanno più mostrate
    private final Set<String> ignored = new HashSet<>();

    public Mailbox(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public ObservableList<Email> getInbox() {
        return inbox;
    }

    public synchronized void addEmail(Email e) {
        if (!ignored.contains(e.getId())) {
            inbox.add(e);
        }
    }

    public synchronized void addEmails(Iterable<Email> emails) {
        for (Email e : emails) {
            addEmail(e);
        }
    }

    public synchronized void removeEmail(Email e) {
        inbox.remove(e);
        ignored.add(e.getId());
    }

    public synchronized boolean containsEmail(String id) {
        return inbox.stream().anyMatch(e -> e.getId().equals(id));
    }

    public synchronized boolean isIgnored(String id) {
        return ignored.contains(id);
    }

    public synchronized void ignoreEmail(String id) {
        ignored.add(id);
    }

    public synchronized String getLastEmailID() {
        if (inbox.isEmpty()) return "";
        return inbox.get(inbox.size() - 1).getId();
    }
}
