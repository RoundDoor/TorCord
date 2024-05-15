package dev.rounddoor;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;


public class Main {

    // OkHttpClient instance
    private static final OkHttpClient client = new OkHttpClient();

    // User ID
    private static String USER_ID;

    // GUI components
    private static JTextArea textArea;

    /**
     * This is the main method that starts the chat application.
     *
     * @param args The command line arguments.
     */
    public static void main(String[] args) throws SocketException {

        generateUserID();
        // Generate the text area for the GUI so that the websocket can update it while the rest of the GUI is being built
        textArea = new JTextArea(20, 40);
        // Start the WebSocket connection
        System.out.println(System.nanoTime()/1000000);
        startWebSocket();
        // Start the GUI
        System.out.println(System.nanoTime()/1000000);
        guiStart();


    }


    /**
     * This method is used to start the GUI for the chat application.
     */
    public static void guiStart() {
        JFrame frame = new JFrame("TorCord");
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        JPanel panel = new JPanel();
        frame.add(panel);

        textArea.setEditable(false);
        panel.add(textArea);

        JScrollPane scrollPane = new JScrollPane(textArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scrollPane);

        JTextField textField = new JTextField(40);
        panel.add(textField);

        JButton send = new JButton("Send");
        panel.add(send);

        frame.setVisible(true);


        // Send button action listener
        send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = textField.getText();
                textField.setText("");

                // Build the request with the specified URL
                String url = "http://127.0.0.1:8000/msg";
                String json = "{\"userID\":\"" + USER_ID + "\",\"content\":\"" + message + "\"}";

                MediaType mediaType = MediaType.get("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(json, mediaType);

                Request request = new Request.Builder().url(url).post(body).build();

                try {
                    Response response = client.newCall(request).execute();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });


    }

    /**
     * This method is used to start the WebSocket connection to the server. It listens for new messages and updates the GUI accordingly.
     * The WebSocket URL should be replaced with the appropriate URL for your server.
     */
    public static void startWebSocket() {
        new Thread(() -> {
            Request request = new Request.Builder().url("http://127.0.0.1:8000/msgStream")  // Replace with your WebSocket URL
                    .build();

            WebSocket webSocket = client.newWebSocket(request, new WebSocketListener() {
                @Override
                public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                    System.out.println("WebSocket opened");
                }

                @Override
                public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                    System.out.println("Received: " + text);
                    JSONObject msg = new JSONObject(text);
                    textArea.append(formatMessage(msg));
                    textArea.setCaretPosition(textArea.getDocument().getLength());
                    // Here you can update your GUI to display the new message
                }

                @Override
                public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, Response response) {
                    System.out.println("WebSocket error: " + t.getMessage());
                    t.printStackTrace();
                }
            });

            client.dispatcher().executorService().shutdown();
        }).start();
    }

    /**
     * This method is used to format the message received from the server.
     *
     * @param msg The message received from the server.
     * @return The formatted message.
     */
    public static String formatMessage(JSONObject msg) {
        String userID = msg.get("userID").toString();
        String content = msg.get("content").toString();
        String msgString = userID + ": " + content + "\n";
        int userIDLength = userID.length();
        int contentLength = content.length();
        // Break up msg into lines of 70
        // If the message is less than 70 characters
        if (msgString.length() <= 70) {
            return msgString;
        }
        // If the message is greater than 70 characters
        else {
            for (int i = 0; i < msgString.length(); i += 70) {
                if (i + 70 < msgString.length()) {
                    String line = msgString.substring(i, i + 70);
                    String leftover = "";
                    // If the line ends in the middle of a word
                    if (line.charAt(69) != ' ') {
                        int j = 69;
                        while (line.charAt(j) != ' ') {
                            j--;
                        }
                        line = line.substring(0, j);  // get the line up to the last word
                        leftover = msgString.substring(i + j, i + 70); // get the leftover characters
                        leftover = leftover.replace(" ", ""); // remove white space
                    }
                    msgString = msgString.substring(0, i) + line + "\n" + leftover + msgString.substring(i + 70);
                }
            }
        }
        return msgString;
    }


// TODO: Implement the generateUserID method to parse the MAC address of the user's network interface into a unique user ID.

    /**
     * This method is used to generate a unique user ID based on the user's MAC address.
     *
     * @return The generated user ID.
     */
    public static String generateUserID() throws SocketException {
        USER_ID = getHWID();
        return getHWID();
    }


    /**
     * This method is used to get the MAC address of the user's network interface.
     *
     * @return The MAC address of the user's network interface.
     */
    public static String getHWID() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface network = networkInterfaces.nextElement();
            byte[] mac = network.getHardwareAddress();

            if (mac != null) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < mac.length; i++) {
                    sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                }
                return sb.toString();
            }
        }
        return null;
    }

    /**
     * This method is used to test the API. It sends a GET request to the API and prints the response.
     *
     * @param msgID This is the message ID that will be appended to the API URL.
     */
    public static void apiTest(int msgID) {
        // Build the request with the specified URL
        Request request = new Request.Builder().url("http://127.0.0.1:8000/msg/" + msgID).build();

        try {
            // Execute the request and get the response
            Response response = client.newCall(request).execute();

            // Parse the response body to a JSONObject
            assert response.body() != null;
            JSONObject jsonObject = new JSONObject(response.body().string());

            // Print the JSONObject
            System.out.println(jsonObject.toString());
        } catch (Exception e) {
            // Print the stack trace for any exceptions
            e.printStackTrace();
        }
    }

}