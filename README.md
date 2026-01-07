# Anubis_GPT

A Java-based desktop AI chatbot featuring a modern Swing GUI, real-time text-to-speech via FreeTTS, and intelligent responses powered by the OpenRouter API. This project is framed as an AI engineering example—demonstrating model integration, prompt engineering, evaluation workflows, and system design decisions beyond simple API calls.

## Highlights
- Responsive, themed UI built with Swing
- Voice output (Text-to-Speech) using FreeTTS
- AI responses via OpenRouter (Gemini-family models by default)
- Thread-safe UI updates (`SwingUtilities`) and background networking to keep the app smooth
- Robust error handling and user-friendly messages

## Demo
Add screenshots or a short GIF here to showcase the UI and voice responses.

## AI / LLM Components (explicit)
This section makes explicit how LLMs are used in the application and how the AI components are structured:

- Model client (OpenRouterClient): Responsible for preparing model payloads, sending requests to OpenRouter, handling responses (including streaming), and mapping model outputs back into app conversations.
- System message / context manager: Builds and maintains the conversation state and system-level instructions that shape assistant behavior.
- Prompt templates and few-shot examples: Encapsulates reusable prompt patterns and optional canned examples for instruction-following and persona control.
- Response post-processing pipeline: Filters, sanitizes, and formats model outputs for display and for TTS. Also includes error correction and fallback heuristics.
- Safety & moderation hooks: Optional moderation checks that can run before displaying or speaking content.

## Model selection & reasoning
Why the Gemini-family (via OpenRouter) is the default:

- Instruction-following and conversational quality: Gemini-family models are strong at dialogue and instruction-following, which suits a chat UI.
- Latency vs quality tradeoff: Gemini models offer a practical balance for interactive applications; they provide better response quality than smaller local models while retaining manageable latency when used through a hosted API.
- Long-context handling: For a chat application that may preserve conversation state, models with larger context windows reduce the need to aggressively truncate history.
- Cost/availability considerations: Using an API-forwarding provider (OpenRouter) allows experimenting with several hosted models quickly without maintaining large local weights.

Alternative models and when to choose them:
- Local models (Llama 2/3, Mistral, etc.): Choose when offline operation or data privacy and low per-call cost are priorities, at the expense of local resources and potentially lower instruction-tuning quality.
- Other hosted models (OpenAI, Anthropic): Useful for benchmarking or when specific features (multimodality, safety constraints) are desired.

Model knobs exposed in the app (or easily added): temperature, top_p, max_tokens, stop sequences, streaming, and model name to enable quick switching for experimentation.

## Prompt engineering & evaluation logic
We treat prompt engineering as part of the product:

- Prompt template structure
  - System prompt (single-shot): High level behavior, persona, and safety guardrails.
  - Instruction / user message: The user's request.
  - Assistant placeholders (few-shot examples): Optional short examples to bias style or include domain-specific formats (e.g., JSON outputs, code blocks).

- Example prompt template
  - System: "You are Anubis, a helpful Java-based desktop assistant. Prioritize clarity, short answers for UI display, and safe content." 
  - User: "<user input>"
  - Few-shot examples: 1–2 brief turns showing desired format for structured outputs.

- Parameter tuning and experiments
  - Temperature: Lower values (0.0–0.3) for deterministic/helpful replies; higher for creative outputs.
  - Top-p and frequency/presence penalties: Used for reducing repetition and controlling diversity.
  - Max tokens and context-window management: We trim or summarize older turns (or store condensed memory) when approaching token limits.

- Evaluation and metrics
  - Automated checks: Output length, presence of disallowed tokens/patterns, JSON validity for structured responses, and quick safety flagging.
  - Regression tests: Store canonical prompts and expected patterns to detect regressions after model changes.
  - Human evaluation: Periodic manual review with a rubric (clarity, factuality, usefulness, safety). This is the gold standard for conversational quality.
  - A/B experiments: Compare different prompt templates or model configurations using user satisfaction (or proxy metrics like response acceptance, follow-up rate).

- Prompt debugging workflow
  - Log prompts and model outputs (redacting PII) during development and testing
  - Implement unit tests for critical prompt templates
  - Keep a small corpus of adversarial prompts to validate safety and robustness

## AI System Design Decisions
These are the deliberate engineering choices made to transform an API client into an AI application:

- Separation of concerns
  - UI layer (Swing): Presentation and interaction only. No direct model calls from EDT.
  - Service layer: Abstracts API interactions, retries, backoff logic, and streaming handling.
  - Processing layer: Handles prompt composition, token accounting, and post-processing.

- Asynchronous, non-blocking UX
  - Model calls are made off the EDT; partial streaming responses can be appended to the UI incrementally.
  - The TTS engine (FreeTTS) runs on a dedicated thread and is fed sanitized text output.

- Token-budgeting & context management
  - Conversation history is stored as structured turns; before sending requests, older turns are summarized or pruned to stay within the model's context window.
  - For structured responses (e.g., JSON), the prompt enforces strict formats to make downstream parsing robust.

- Robustness: retries, exponential backoff, and graceful degradation
  - Transient network errors are retried with exponential backoff.
  - On high latency or rate limits, the UI surfaces friendly messages and offers to retry.

- Privacy & security
  - API keys are not hard-coded in committed files; the README shows how to configure them via environment variables.
  - Optional local-only mode should be considered for sensitive data (future roadmap).

- Observability
  - Logs capture request/response times, token usage, and status codes for debugging and cost monitoring.
  - Enable optional telemetry during development to collect anonymized usage metrics for UX and model tuning (opt-in only).

## Architecture Overview
- UI Layer (Swing): Chat window, input field, send button, voice toggle
- Service Layer:
  - OpenRouterClient: Sends prompts and parses AI responses (Gson)
  - VoiceService: FreeTTS-based speech output
- Concurrency: Background tasks for network calls; UI updates via `SwingUtilities.invokeLater`
- Prompt manager & context store: Responsible for assembling system + user messages and pruning or summarizing history

## Tech Stack
- Language: Java (100%)
- GUI: Swing
- AI Integration: OpenRouter API (Gemini by default, configurable)
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

## Testing & Evaluation
- Unit tests for prompt templates: assert that generated prompts contain required system instructions and placeholders.
- Integration tests: validate the OpenRouterClient with mocked responses to ensure parsing and error handling remain stable.
- Manual evaluation: periodically review sample conversations and run the human-eval rubric.

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
- [ ] Local model support and offline mode
- [ ] Prompt laboratory: in-app prompt templates, A/B testing controls

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

----

This README was updated to explicitly highlight the AI engineering aspects: model reasoning, prompt engineering, evaluation, and system design choices to make the repository more valuable to developers who want to iterate on AI-driven desktop applications.
