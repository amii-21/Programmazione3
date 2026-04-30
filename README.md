# Mail Client/Server Application

A desktop email application with a separated client/server architecture, built in Java with a JavaFX GUI.

## Architecture

The project is split into two independent processes:

- **Server** — handles authentication, receives and routes emails between accounts, persists mailbox state
- **Client** — JavaFX GUI with real-time notifications from the server

## Features

- User login and session management
- Real-time inbox updates (server push via Socket)
- Full email operations: compose, reply, reply-all, forward, delete
- Multi-account support on the server side

## Stack

- Java
- JavaFX (UI)
- Java Sockets (client/server communication)
- OOP design patterns (MVC, Observer)

## How to run

```bash
# Prerequisites: Java 17+ with JavaFX SDK

# 1. Clone the repo
git clone https://github.com/amii-21/Programmazione3.git

# 2. Start the server
cd mail-server
mvn clean javafx:run

# 3. Start one or more clients (separate terminal)
cd mail-client
mvn clean javafx:run
```

---
