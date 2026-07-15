# AnubisGPT

AnubisGPT is a Java Swing desktop chat application that sends prompts to OpenRouter and can optionally read responses aloud with ElevenLabs voice output.

## Features

- Simple desktop chat UI built with Swing
- Sends user prompts to the OpenRouter chat completions API
- Optional text-to-speech playback through ElevenLabs
- Conversation history shown inside the app window
- Custom logo and bundled audio/font support libraries

## Requirements

- Java 8 or newer
- Internet access for OpenRouter and ElevenLabs requests
- Valid API keys for:
  - OpenRouter
  - ElevenLabs, if voice is enabled
- A JLayer-compatible JAR on the classpath for `javazoom.jl.player.Player`

## Project Layout

- `src/Anubius.java` - main application source
- `logo.png` - app logo shown in the UI
- `gson-2.13.0.jar` - JSON parsing dependency
- `lib/` - bundled FreeTTS audio support libraries

## Setup

1. Clone or open the repository.
2. Replace the placeholder API values in `src/Anubius.java`:
   - `OPENROUTER_API_KEY`
   - `ELEVENLABS_API_KEY`
3. Make sure the required JAR files are available on the compile and run classpath.

## Run

### Windows PowerShell

```powershell
javac -cp ".;gson-2.13.0.jar;lib/*;path\to\jlayer.jar" src\Anubius.java
java -cp ".;src;gson-2.13.0.jar;lib/*;path\to\jlayer.jar" Anubius
```

### macOS / Linux

```bash
javac -cp ".:gson-2.13.0.jar:lib/*:/path/to/jlayer.jar" src/Anubius.java
java -cp ".:src:gson-2.13.0.jar:lib/*:/path/to/jlayer.jar" Anubius
```

## Notes

- The app currently uses hardcoded API key placeholders in source code.
- If the voice feature is not needed, you can leave it disabled in the UI.
- If compilation fails, the missing dependency is usually the JLayer JAR used by the mp3 player.
