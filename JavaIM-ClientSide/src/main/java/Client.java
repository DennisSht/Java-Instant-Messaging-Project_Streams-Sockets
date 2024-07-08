import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Properties;
import java.awt.*;
import javax.swing.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.Serdes.StringSerde;

public class Client extends JFrame {

    private JTextField userText; // Area where message is sent before I send it.
    private JTextArea chatWindow;  // Conversation itself (both mine and others).
    private ObjectOutputStream output; // Output stream.
    private ObjectInputStream input; // Input stream.
    private String message = ""; // Client message.
    private String serverIP; // Server IP address.
    private int portNumber; // Port Number.
    private Socket connection; // Socket connection.

    String bootstrapServers = "127.0.0.1:9092";
    String groupId = "my-fourth-application";
    String topic = "demo_java";

    KafkaConsumer<String, String> consumer;
    KafkaProducer<String, String> producer;


    /**
     * Constructor method.
     *
     * @param host Server IP address.
     * @param port Port Number.
     */
    public Client(String host, String port) {

        // Setup the Client side of the chat server.
        setTitle("Instant Messenger Project - Client side");
        this.serverIP = host;
        this.portNumber = Integer.parseInt(port);
        userText = new JTextField();
        userText.setEditable(false);
        userText.addActionListener(
                event -> {
                    // Send the data from the client text area.
                    sendMessage(event.getActionCommand());
                    userText.setText("");
                }
        );

        // Create chat-window where all the messages get displayed.
        add(userText, BorderLayout.NORTH);
        chatWindow = new JTextArea();
        add(new JScrollPane(chatWindow), BorderLayout.CENTER);
        setSize(325, 150);
        setVisible(true);

        configKafka();
    }

    private void configKafka() {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty("key.serializer", StringSerializer.class.getName());
        properties.setProperty("value.serializer", StringSerializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        System.out.println("Try to connect kafka server");

        consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Arrays.asList(topic));

        producer = new KafkaProducer<>(properties);
        
        System.out.println(" ... server connected");
    }

    public void startRunning() {
        String message = "";
        try {
            while (!message.equals("SERVER - END")) {
                ConsumerRecords<String, String> records = consumer.poll(java.time.Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("Key: " + record.key() + ", Value: " + record.value());
                    System.out.println("Partition: " + record.partition() + ", Offset: " + record.offset());
                    message = record.value();
                    showMessage("\n" + message);                    
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeStream();
        }
    }

    /**
     * Connect to the server (main public).
     */
    /*
    public void startRunning() {
        try {
            connectToServer();  // Connect to one specific server.
            setupStreams();
            whileChatting();
        } catch (EOFException eofException) {
            showMessage("\n Client terminated connection");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            closeStream();
        }
    }
        */

    /**
     * Socket connection to server.
     *
     * @throws IOException If the unable to set up the connection.
     */
    private void connectToServer() throws IOException {
        showMessage("Attempting connection... \n");
        connection = new Socket(InetAddress.getByName(serverIP), portNumber);
        showMessage("Connected to: " + connection.getInetAddress().getHostName());
    }

    /**
     * Get stream to send and receive data.
     *
     * @throws IOException If the unable to create the I/O streams.
     */
    private void setupStreams() throws IOException {
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        input = new ObjectInputStream(connection.getInputStream());
        showMessage("\n Streams are now setup. \n");
    }

    /**
     * During the chat conversation.
     *
     * @throws IOException If the unable to parse data.
     */
    private void whileChatting() throws IOException {
        ableToType(true); // Allow client (user) to type into the text box (public display).
        do {
            try {
                // Whatever user sends through the stream, append as message.
                message = (String) input.readObject();
                showMessage("\n" + message);
            } catch (ClassNotFoundException classNotFoundException) {
                showMessage("\n Cannot parse data that the user sent.");
            }
            // Have a conversation until user ends the chat by typing 'END'.
        } while (!message.equals("SERVER - END"));
    }

    /**
     * Close streams and sockets after done chatting.
     */
    private void closeStream() {
        showMessage("\n Closing connections... \n");
        ableToType(false); // Prevent the user from entering data.
        try {
            output.close(); // Closes the output path to the server.
            input.close(); // Closes the input path to the client, from the server.
            connection.close(); //Closes the connection between the server and the client.
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void sendMessage(String message) {
        producer.send(new ProducerRecord<>(topic, message));
        showMessage("\nCLIENT - " + message);
    }

    /**
     * Send a message to the server.
     *
     * @param message Message to be sent to the server (assumes socket connection is established).
     */
    /*
    private void sendMessage(String message) {
        try {
            output.writeObject("CLIENT - " + message);
            output.flush();
            showMessage("\nCLIENT - " + message);
        } catch (IOException ioException) {
            chatWindow.append("\n Error.  Unable to send message.  Please retry message.");
        }
    }
    */

    /**
     * Changes / updates chat window.
     *
     * @param text Text that is appended to the chat window.
     */
    private void showMessage(final String text) {
        SwingUtilities.invokeLater(
                () -> chatWindow.append(text)
        );
    }

    /**
     * Let the user enter data into their text box.
     *
     * @param typeFlag Boolean flag that either prevents (or allows) the user to enter data.
     */
    private void ableToType(final boolean typeFlag) {
        SwingUtilities.invokeLater(
                () -> userText.setEditable(typeFlag)
        );
    }
}
