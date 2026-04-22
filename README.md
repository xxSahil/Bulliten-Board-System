# Distributed Bulletin Board System

A multithreaded client-server bulletin board system built in Java using TCP sockets. The project supports concurrent clients, a custom application-layer protocol, synchronized shared-state operations, and a Swing-based GUI client.

## Features

- Multithreaded TCP server with thread-per-client handling
- Custom text-based application-layer protocol
- Structured OK/ERROR server responses
- Thread-safe shared board state using synchronized methods
- Atomic operations for POST, SHAKE, and CLEAR
- GUI client built with Java Swing
- Support for posting, querying, pinning, unpinning, clearing, and shaking notes

## Tech Stack

- Java
- TCP Sockets
- Java Threads
- Swing

## Project Structure

```text
server/
  BBoard.java
  Board.java
  ClientHandler.java
  Note.java

client/
  ClientGUI.java
