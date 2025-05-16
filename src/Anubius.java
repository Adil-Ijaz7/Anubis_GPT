import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import com.google.gson.*;
import com.sun.speech.freetts.*;

public class Anubius extends JFrame {
    private final JTextArea chatArea;
    private final JTextField inputField;
    private final JButton sendButton, voiceToggleButton;
    private final ArrayList<String> messages = new ArrayList<>();
    private final String OPENROUTER_API_KEY = "YOUR-API-KEY";
    private boolean voiceEnabled = false;
    private final Voice voice;

    public Anubius() {
        setTitle("AnubisGPT");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Initialize FreeTTS
        System.setProperty("freetts.voices",
                "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        VoiceManager vm = VoiceManager.getInstance();
        voice = vm.getVoice("kevin16");
        if (voice != null) voice.allocate();

        // --- TOP BAR ---
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        topBar.setBackground(new Color(255, 255, 255));
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

        // --- CHAT PANEL ---
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
        chatScroll.setBackground(Color.WHITE);

        // --- INPUT PANEL ---
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

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);


        // Voice toggle
        voiceToggleButton = new JButton("Enable Voice");
        voiceToggleButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        voiceToggleButton.setBackground(new Color(0, 128, 128));
        voiceToggleButton.setForeground(Color.WHITE);
        voiceToggleButton.setFocusPainted(false);
        voiceToggleButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        voiceToggleButton.addActionListener(e -> toggleVoice());

        // Send button
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

        // Event listeners
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

    private void speak(String text) {
        if (voiceEnabled && voice != null) {
            new Thread(() -> voice.speak(text)).start();
        }
    }

    private void showGreetingMessage() {
        String greeting = "AnubisGPT: Hello! I'm your AnubisGPT. How can I help you today?\n\n";
        chatArea.setText(greeting);
        messages.add("AI: Hello! I'm your AI assistant.");
        speak("Hello! I am your AnubisGPT. How can I help you today?");
    }

    private void sendMessage() {
        String userMessage = inputField.getText().trim();
        if (userMessage.isEmpty()) return;

        // Add user message (right-aligned)
        chatArea.append(String.format("%" + (chatArea.getColumns() - 5) + "s\n", "You: " + userMessage));
        messages.add("You: " + userMessage);
        inputField.setText("");

        new Thread(() -> {
            try {
                String aiResponse = getAIResponse(userMessage);
                SwingUtilities.invokeLater(() -> {
                    // Add AI response (left-aligned)
                    chatArea.append("AnubisGPT: " + aiResponse + "\n\n");
                    messages.add("AI: " + aiResponse);
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
                    speak(aiResponse);
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
        body.addProperty("model", "google/gemini-2.0-flash-001");//models can be switched from here
        //openai/gpt-4o-mini
        //google/gemini-2.0-flash-001 (273B)
        //qwen/qwen3-0.6b-04-28:free(2025 model)
        //meta-llama/llama-3-8b-instruct
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
