# Java-Console-Chat-Application

A simple Java-based client-server chat system enabling real-time messaging between two users.

## Features

**Server:**
- Listens on port 12345
- Manages up to 2 simultaneous connections
- Routes messages between clients
- Shows connection status and messages in console

**Client:**
- Connects to server at localhost
- Simple console interface
- Separate threads for sending/receiving
- `/exit` command to disconnect

## How to Run

1. Start the server:
   ```
   java ChatServer
   ```

2. Run clients in separate terminals:
   ```
   java ChatClient
   ```

## Usage
- Type messages and press Enter to send
- Type `/exit` to quit
- Server shows all activity and connections

## Requirements
- Java 8 or higher
- Basic network connectivity
