package com.example.mail.server.util;

import java.io.*;

public class PersistenceUtil {
    private PersistenceUtil() {}

    public static void saveObjectToFile(Object obj, File file) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(obj);
        }
    }

    public static Object loadObjectFromFile(File file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return in.readObject();
        }
    }
}
