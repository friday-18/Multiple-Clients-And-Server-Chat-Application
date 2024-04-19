import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientChatGUI {
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton exitButton; // Added exit button
    private PrintWriter writer;
    private String username; // Added to store the username
    private Socket socket;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new ClientChatGUI();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public ClientChatGUI() throws IOException {
        frame = new JFrame("Chat Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        chatArea = new JTextArea();
        chatArea.setEditable(false);

        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        messageField = new JTextField();
        messageField.addActionListener(e -> {
            sendMessage(messageField.getText());
            messageField.setText("");
        });

        exitButton = new JButton("Exit");
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exitChat();
            }
        });

        frame.setLayout(new BorderLayout());
        frame.add(chatScrollPane, BorderLayout.CENTER);
        frame.add(messageField, BorderLayout.SOUTH);
        frame.add(exitButton, BorderLayout.EAST); // Add exit button to the EAST

        username = JOptionPane.showInputDialog(frame, "Enter your username:");
        socket = new Socket("localhost", 5000);
        writer = new PrintWriter(socket.getOutputStream(), true);

        writer.println(username);

        // Set the frame title to include the username
        frame.setTitle("Chat Application - " + username);

        new Thread(() -> readMessages(socket)).start();

        // Set the frame visible after initializing the socket and starting the thread
        frame.setVisible(true);
    }

    private void sendMessage(String message) {
        writer.println(message);
        // Display the sent message in the chat area with a different color
        appendToChatArea("You: " + message, Color.BLUE);
    }

    private void readMessages(Socket socket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String message;
            while ((message = reader.readLine()) != null) {
                if (message.equals("USER_EXIT")) {
                    // Notify the user has disconnected
                    appendToChatArea("User has disconnected: " + username, Color.RED);
                } else {
                    // Display normal messages
                    appendToChatArea(message, Color.BLACK);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Handle the case when the server closes the connection
            // You may want to add appropriate handling here, such as closing the socket or displaying a message to the user.
        }
    }

    private void appendToChatArea(String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            chatArea.setForeground(color);
            chatArea.append(message + "\n");
        });
    }

    private void exitChat() {
        try {
            // Notify the server about the exit
            writer.println("USER_LEFT!!");
            // Close the socket and dispose of the frame
            socket.close();
            frame.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
