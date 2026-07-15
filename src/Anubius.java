import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import com.google.gson.*;
import javazoom.jl.player.Player;

public class Anubius extends JFrame {
    private final JTextArea chatArea;
    private final JTextField inputField;
    private final JButton sendButton, voiceToggleButton;
    private final java.util.List<String> messages = new ArrayList<>();
    private final String OPENROUTER_API_KEY = "YOUR_API"; // Replace with your key
    private final String ELEVENLABS_API_KEY = "YOUR_API";      // Replace with your key
    private final String VOICE_ID = "EXAVITQu4vr4xnSDxMaL"; // Default ElevenLabs voice
    private boolean voiceEnabled = false;

    public Anubius() {
        setTitle("AnubisGPT");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- TOP BAR ---
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel logoLabel = new JLabel(new ImageIcon(new ImageIcon("logo.png")
                .getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH)));
        JLabel appName = new JLabel("AnubisGPT");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 24));
        appName.setForeground(new Color(0, 128, 128));

        topBar.add(logoLabel);
        topBar.add(Box.createHorizontalStrut(10));
        topBar.add(appName);

        // --- MAIN CONTENT ---
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        mainPanel.setBackground(Color.WHITE);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(Color.WHITE);
        chatArea.setForeground(Color.BLACK);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        chatArea.setMargin(new Insets(10, 20, 10, 20));

        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        inputPanel.setBackground(Color.WHITE);

        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBackground(new Color(240, 240, 240));
        inputField.setForeground(Color.BLACK);
        inputField.setCaretColor(new Color(0, 128, 128));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        voiceToggleButton = new JButton("Enable Voice");
        voiceToggleButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        voiceToggleButton.setBackground(new Color(0, 128, 128));
        voiceToggleButton.setForeground(Color.WHITE);
        voiceToggleButton.setFocusPainted(false);
        voiceToggleButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        voiceToggleButton.addActionListener(e -> toggleVoice());

        sendButton = new JButton("Send");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setBackground(new Color(0, 128, 128));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        buttonPanel.add(voiceToggleButton);
        buttonPanel.add(sendButton);

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        mainPanel.add(chatScroll, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        add(topBar, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        showGreetingMessage();
        setVisible(true);
    }

    private void toggleVoice() {
        voiceEnabled = !voiceEnabled;
        voiceToggleButton.setText(voiceEnabled ? "Voice Enabled" : "Voice Disabled");
        voiceToggleButton.setBackground(voiceEnabled ?
                new Color(0, 100, 100) : new Color(0, 128, 128));
    }

    private void speakWithElevenLabs(String text) {
        if (!voiceEnabled || text == null || text.isEmpty()) return;

        new Thread(() -> {
            try {
                URL url = new URL("https://api.elevenlabs.io/v1/text-to-speech/" + VOICE_ID);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "audio/mpeg");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("xi-api-key", ELEVENLABS_API_KEY);
                conn.setDoOutput(true);

                JsonObject body = new JsonObject();
                body.addProperty("text", text);
                body.addProperty("model_id", "eleven_monolingual_v1");
                JsonObject voiceSettings = new JsonObject();
                voiceSettings.addProperty("stability", 0.5);
                voiceSettings.addProperty("similarity_boost", 0.75);
                body.add("voice_settings", voiceSettings);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes());
                os.flush();
                os.close();

                InputStream audioStream = conn.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(audioStream);
                Player player = new Player(bis);
                player.play(); // Play the mp3
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void showGreetingMessage() {
        String greeting = "AnubisGPT: Hello! I'm your AnubisGPT. How can I help you today?\n\n";
        chatArea.setText(greeting);
        messages.add("AI: Hello! I'm your AI assistant.");
        speakWithElevenLabs("Hello! I am your AnubisGPT. How can I help you today?");
    }

    private void sendMessage() {
        String userMessage = inputField.getText().trim();
        if (userMessage.isEmpty()) return;

        chatArea.append(String.format("%" + (chatArea.getColumns() - 5) + "s\n", "You: " + userMessage));
        messages.add("You: " + userMessage);
        inputField.setText("");

        new Thread(() -> {
            try {
                String aiResponse = getAIResponse(userMessage);
                SwingUtilities.invokeLater(() -> {
                    chatArea.append("AnubisGPT: " + aiResponse + "\n\n");
                    messages.add("AI: " + aiResponse);
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
                    speakWithElevenLabs(aiResponse);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    chatArea.append("Error: " + e.getMessage() + "\n");
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
                });
            }
        }).start();
    }

    private String getAIResponse(String prompt) throws IOException {
        URL url = new URL("https://openrouter.ai/api/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + OPENROUTER_API_KEY);
        conn.setDoOutput(true);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", prompt);

        JsonArray messages = new JsonArray();
        messages.add(userMsg);

        JsonObject body = new JsonObject();
        body.addProperty("model", "google/gemini-pro");
        body.add("messages", messages);

        OutputStream os = conn.getOutputStream();
        os.write(body.toString().getBytes());
        os.flush();
        os.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();

        JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
        JsonArray choices = json.getAsJsonArray("choices");

        if (choices.size() > 0) {
            String aiResponse = choices.get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
            return aiResponse.replaceAll("[*#_']", "");
        }
        return "No response available.";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Anubius app = new Anubius();
            app.setVisible(true);
        });
    }
}
