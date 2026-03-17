package com.example.mail.client.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Email {

    private final String id;
    private final String sender;
    private final List<String> recipients;
    private final String subject;
    private final String body;
    private final LocalDateTime date;

    public Email(String id,
                 String sender,
                 List<String> recipients,
                 String subject,
                 String body,
                 LocalDateTime date) {

        this.id = id;
        this.sender = sender;
        this.recipients = recipients;
        this.subject = subject;
        this.body = body;
        this.date = date;
    }

    public String getId() { return id; }

    public String getSender() { return sender; }

    public List<String> getRecipients() { return recipients; }

    public String getSubject() { return subject; }

    public String getBody() { return body; }

    public LocalDateTime getDate() { return date; }

    @Override
    public String toString() {
        return subject + "   (" + sender + ")";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Email)) return false;
        Email email = (Email) o;
        return id.equals(email.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
