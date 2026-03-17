package com.example.mail.server.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Email implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String sender;
    private final List<String> recipients;
    private final String subject;
    private final String body;
    private final LocalDateTime sentDate;

    public Email(String sender,
                 List<String> recipients,
                 String subject,
                 String body,
                 LocalDateTime sentDate) {

        this.id = UUID.randomUUID().toString();
        this.sender = sender;
        this.recipients = recipients;
        this.subject = subject;
        this.body = body;
        this.sentDate = sentDate;
    }

    public String getId() { return id; }
    public String getSender() { return sender; }
    public List<String> getRecipients() { return recipients; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public LocalDateTime getSentDate() { return sentDate; }
}
