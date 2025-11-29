# Anubis_GPT

A Java-based desktop AI chatbot featuring a modern Swing GUI, real-time text-to-speech via FreeTTS, and intelligent responses powered by the OpenRouter API.

## Highlights
- Responsive, themed UI built with Swing
- Voice output (Text-to-Speech) using FreeTTS
- AI responses via OpenRouter (Gemini models)
- Thread-safe UI updates (`SwingUtilities`) and background networking to keep the app smooth
- Robust error handling and user-friendly messages

## Demo
Add screenshots or a short GIF here to showcase the UI and voice responses.

## Architecture Overview
- UI Layer (Swing): Chat window, input field, send button, voice toggle
- Service Layer: 
  - OpenRouterClient: Sends prompts and parses AI responses (Gson)
  - VoiceService: FreeTTS-based speech output
- Concurrency: Background tasks for network calls; UI updates via `SwingUtilities.invokeLater`

## Tech Stack
- Language: Java (100%)
- GUI: Swing
- AI Integration: OpenRouter API (Gemini)
- Voice: FreeTTS
- JSON: Google Gson

## Prerequisites
- Java 17+ (recommended)
- An OpenRouter API key: https://openrouter.ai

## Installation
```bash
# Clone the repository
git clone https://github.com/Adil-Ijaz7/Anubis_GPT.git
cd Anubis_GPT
```

Open the project in your preferred Java IDE (IntelliJ IDEA, Eclipse, NetBeans).

## Configuration
Set your OpenRouter API key in the code (or externalize via environment variable if you prefer):
```java
private static final String OPENROUTER_API_KEY = "your_api_key_here";
```
Optionally, externalize configuration:
```bash
# Unix/macOS
export OPENROUTER_API_KEY="your_api_key_here"

# Windows (PowerShell)
$Env:OPENROUTER_API_KEY="your_api_key_here"
```
Then read it in code via `System.getenv("OPENROUTER_API_KEY")` with a fallback.

## Running
- Run the `main()` method of the application entry point.
- Type your prompt and press Enter or click Send.
- Toggle voice to enable/disable TTS.

## Usage Tips
- Keep prompts concise for faster responses.
- If the UI becomes unresponsive, ensure network calls are off the EDT (Event Dispatch Thread).
- Check the console/logs for detailed error messages.

## Error Handling
- Network failures: Gracefully reported to the user
- JSON parsing errors: Logged with helpful context (via Gson)
- TTS failures: Voice service errors surfaced without crashing the UI

## Project Structure (example)
```
src/
├─ Anubius.java
```

## Roadmap
- [ ] Chat history persistence
- [ ] Model selection UI
- [ ] Microphone input and speech-to-text
- [ ] Theming/dark mode toggle
- [ ] Packaging as a standalone executable (e.g., jpackage)

## Contributing
Pull requests are welcome! Please:
- Open an issue to discuss major changes
- Keep code style consistent
- Include tests where applicable

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Acknowledgments
- [OpenRouter](https://openrouter.ai) for API access to cutting-edge models
- [FreeTTS](http://freetts.sourceforge.net/docs/index.php) for Java TTS
- [Gson](https://github.com/google/gson) for JSON parsing
####end](https://github.com/Adil-Ijaz7/Anubis_GPT)
