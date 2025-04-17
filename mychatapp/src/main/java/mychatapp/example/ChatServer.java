//THIS IS THE CODE FOR THE MAIN CLASS 'ChatServer'
package mychatapp.example;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static final int MAX_CLIENTS = 2;
    private static final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();  //Thread-safe set
    private static final ExecutorService executor = Executors.newFixedThreadPool(MAX_CLIENTS);  //Thread pool

    public static void main(String[] args) {
        System.out.println("Chat Server started on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            //Main server loop
            while (true) {
                try {
                    //Accept incoming client connection
                    Socket clientSocket = serverSocket.accept();

                    //Check if server is full
                    if (clients.size() >= MAX_CLIENTS) {
                        rejectConnection(clientSocket);  //Reject if max clients reached
                        continue;
                    }

                    //Create new handler for client and add to set
                    ClientHandler handler = new ClientHandler(clientSocket, clients);
                    clients.add(handler);
                    executor.execute(handler);

                    System.out.println("New client connected. Total clients: " + clients.size());
                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            executor.shutdown();  //Clean up thread pool when server shuts down
        }
    }

    //Reject connection when server is full
    private static void rejectConnection(Socket socket) throws IOException {
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println("SERVER_MSG: Chat room is full. Maximum " + MAX_CLIENTS + " clients allowed.");
        socket.close();  // Close the rejected connection
        System.out.println("Rejected connection - maximum clients reached");
    }
}

class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Set<ClientHandler> clients;
    private BufferedReader in;
    private PrintWriter out;
    private String clientName;

    //Constructor
    public ClientHandler(Socket socket, Set<ClientHandler> clients) {
        this.clientSocket = socket;
        this.clients = clients;
    }

    @Override
    public void run() {
        try {
            setupStreams();     //Initialize I/O streams
            getClientName();    //Get client's name
            notifyClientJoined();  //Notify others of new client

            String message;
            //Main message processing loop
            while ((message = in.readLine()) != null) {
                if ("/exit".equalsIgnoreCase(message)) {
                    break;  //Exit loop if client sends exit command
                }
                broadcastMessage(message);  //Send message to other clients
            }
        } catch (IOException e) {
            System.err.println("Error with client " + clientName + ": " + e.getMessage());
        } finally {
            cleanup();  //Ensure proper cleanup
        }
    }

    //Initialize I/O streams
    private void setupStreams() throws IOException {
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    //Get and set client's name
    private void getClientName() throws IOException {
        out.println("SERVER_MSG: Please enter your name:");
        clientName = in.readLine();
        out.println("SERVER_MSG: Welcome, " + clientName + "! Type /exit to quit.");
        System.out.println(clientName + " connected from " + clientSocket.getRemoteSocketAddress());
    }

    //Notify all clients when new client joins
    private void notifyClientJoined() {
        broadcastServerMessage(clientName + " has joined the chat");
    }

    //Broadcast regular chat message to all other clients
    private void broadcastMessage(String message) {
        System.out.println(clientName + ": " + message);  //Log to server console
        clients.forEach(client -> {
            if (client != this) {
                client.out.println(clientName + ": " + message);
            }
        });
    }

    //Broadcast server notification message to all clients
    private void broadcastServerMessage(String message) {
        System.out.println("SERVER: " + message);
        clients.forEach(client -> client.out.println("SERVER_MSG: " + message));
    }

    //Clean up resources when client disconnects
    private void cleanup() {
        try {
            if (clientName != null) {
                broadcastServerMessage(clientName + " has left the chat");
                System.out.println(clientName + " disconnected");
            }
            clients.remove(this);  //Remove from active clients
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error cleaning up client " + clientName + ": " + e.getMessage());
        }
    }
}
//END OF CODE