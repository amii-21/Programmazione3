package com.example.mail.client.model;

import java.io.*;
import java.net.Socket;

public class ClientConnection {

    private final String host;
    private final int port;

    public ClientConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean checkServerAvailable() {
        try (Socket ignored = new Socket(host, port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public String sendRequest(String request) throws IOException {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()))) {

            out.println(request);

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line).append("\n");
            }
            return response.toString().trim();
        }
    }
}
