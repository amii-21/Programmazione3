package com.example.mail.server.model;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import com.example.mail.server.util.PersistenceUtil;

public class MailServerModel {

    private final ConcurrentHashMap<String, Mailbox> mailboxes = new ConcurrentHashMap<>();
    private final File persistenceFile;

    public MailServerModel(File persistenceFile) {
        this.persistenceFile = persistenceFile;
        load();
    }

    //lettura del file
    @SuppressWarnings("unchecked")
    private void load() {
        if (persistenceFile.exists()) { //se il file esiste
            try {
                Object obj = PersistenceUtil.loadObjectFromFile(persistenceFile);
                if (obj instanceof Map) {
                    mailboxes.putAll((Map<String, Mailbox>) obj); //viene caricato in memoria
                }
            } catch (Exception e) {
                System.err.println("Load error: " + e.getMessage());
                createDefaultAccounts();
                persist();
            }
        } else { //se il file non esiste, vengono create le caselle di posta di default
            createDefaultAccounts();
            persist();
        }
    }

    private void createDefaultAccounts() {
        addMailbox(new Mailbox("giada@mail.local"));
        addMailbox(new Mailbox("bob@mail.local"));
        addMailbox(new Mailbox("carlo@mail.local"));

//        for (String userw : mailboxes.keySet()) {
//            Email welcome = new Email(
//                    "system@mail.local",
//                    List.of(userw),
//                    "Welcome",
//                    "Benvenuto nel mail server.",
//                    LocalDateTime.now()
//            );
//
//            mailboxes.get(userw).addEmail(welcome);
//        }
    }

    //scrittura del file 
    public synchronized void persist() {
        try {
            PersistenceUtil.saveObjectToFile(new HashMap<>(mailboxes), persistenceFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized boolean userExists(String user) {
        return mailboxes.containsKey(user);
    }

    public synchronized void addMailbox(Mailbox mailbox) {
        mailboxes.put(mailbox.getAccount(), mailbox);
    }

    public synchronized Set<String> listAccounts() {
        return new TreeSet<>(mailboxes.keySet());
    }

    public synchronized Mailbox getMailbox(String user) {
        return mailboxes.get(user);
    }

    public synchronized List<String> deliverEmail(Email email) {
        List<String> missing = new ArrayList<>();

        for (String r : email.getRecipients()) {
            Mailbox mb = mailboxes.get(r);
            if (mb == null) {
                missing.add(r);
            } else {
                mb.addEmail(email);
            }
        }

        persist();
        return missing;
    }

    public synchronized List<Email> fetchNew(String user, String lastKnownId) {
        Mailbox m = mailboxes.get(user);
        if (m == null) return List.of();
        return m.getMessagesAfter(lastKnownId);
    }
}
