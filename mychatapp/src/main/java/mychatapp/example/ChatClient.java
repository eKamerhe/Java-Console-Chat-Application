//THIS IS THE CODE FOR THE CLIENT CLASS 'ChatClient'
package mychatapp.example;

import java.io.*;
import java.net.*;

public class ChatClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;
    private static final String EXIT_COMMAND = "/exit";

    public static void main(String[] args) {
        System.out.println("Attempting to connect to chat server...");

        //Auto-closing socket and streams
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
             BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Connected to chat server. Type " + EXIT_COMMAND + " to quit.");

            //Receiving messages from server
            Thread messageReceiver = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = serverIn.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server");
                }
            });
            messageReceiver.start();

            //Sending messages
            String userInputLine;
            //Read user input from console
            while ((userInputLine = userInput.readLine()) != null) {
                if (EXIT_COMMAND.equalsIgnoreCase(userInputLine)) {
                    serverOut.println(EXIT_COMMAND);
                    break;
                }
                serverOut.println(userInputLine);
            }

            System.out.println("Disconnecting from server...");
            messageReceiver.interrupt();

        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + SERVER_HOST);
        } catch (ConnectException e) {
            System.err.println("Server unavailable. Please check if server is running.");
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }
}
//END OF CODE