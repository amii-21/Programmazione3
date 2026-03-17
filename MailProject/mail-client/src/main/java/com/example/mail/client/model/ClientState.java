package com.example.mail.client.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClientState {

    private final Set<String> ignoredIds = new HashSet<>();
    private String lastEmailId = "";

    public boolean isIgnored(String id) {
        return ignoredIds.contains(id);
    }

    public void ignoreEmail(String id) {
        ignoredIds.add(id);
    }

    public void updateLastReceived(List<Email> emails) {
        if (!emails.isEmpty()) {
            lastEmailId = emails.get(emails.size() - 1).getId();
        }
    }

    public String getLastEmailId() {
        return lastEmailId;
    }
}
