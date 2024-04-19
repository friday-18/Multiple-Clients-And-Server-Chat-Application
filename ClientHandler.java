import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    private static List<ClientHandler> clients = new ArrayList<>();

    private Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.writer = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String username = reader.readLine();
            System.out.println(username + " joined the chat.");

            String clientMessage;
            while ((clientMessage = reader.readLine()) != null) {
                System.out.println(username + ": " + clientMessage);

                // Broadcast the message to all clients
                ChatServer.broadcastMessage(username + ": " + clientMessage, this);
            }

            // Handle client disconnection
            System.out.println(username + " left the chat.");
            clients.remove(this);
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        writer.println(message);
    }
}
