# Mongoloid Khanate Discord Bot

![Rating](https://img.shields.io/badge/Rating-1%2F5-Red)
![Sauce](https://img.shields.io/badge/100%25-Spaghetti%20Code-orange)
![Build Status](https://img.shields.io/badge/Build-Passing-green)



License
----
![AUR license](https://img.shields.io/badge/License-MIT-blue)



## AI voice chat

Build and run with **Java 17** and FFmpeg. The project now uses JDA 6.7.0
with the libdave-jvm JNI provider for Discord voice encryption. The packaged
natives support Windows x64, Linux x64 (glibc 2.28+), and Linux ARM64 (glibc 2.35+). Other architectures
need the matching libdave native artifact in `build.gradle`.

Set these values as environment variables or in the active settings file
(`Options/dev.appsettings.json` in development, `Options/appsettings.json`
in production). Environment variables override file settings:

| Variable | Value |
| --- | --- |
| `OPENAI_API_KEY` | OpenAI API key with transcription and Responses access |
| `OPENAI_CHAT_MODEL` | Responses-compatible conversational model available to your account |
| `ELEVENLABS_API_KEY` | ElevenLabs API key with text-to-speech access |
| `ELEVENLABS_VOICE_ID` | Authorized synthetic voice ID |
| `ELEVENLABS_MODEL_ID` | Text-to-speech model available to your account |
| `FFMPEG_PATH` | Optional executable path; defaults to `ffmpeg` on PATH |

Use an authorized synthetic voice; do not use an unauthorized clone of a real
person. The character is a fictional comedic AI portrayal of Max Knight.

Enable **Server Members Intent** and **Message Content Intent** in the Discord
Developer Portal. Grant the bot View Channel, Connect, Speak, and Send Messages
in the voice channel's text chat; grant Change Nickname for its own nickname
and retain permissions needed by existing features.

Administrators issue these commands in `#mongoloid-bot`:

- `~Max join`: connect to your regular voice channel, with listening disabled.
- `~Max start`: validate settings and FFmpeg, post the external-service disclosure,
  and enable the conversation loop.
- `~Max stop`: cancel processing/playback and disable listening; stay connected.
- `~Max leave`: stop, disconnect, and discard session history.

The bot sets its server nickname to `replacement_tonight_` when joining voice,
including automatic joins. A nickname permission failure is logged and does
not stop voice. The nickname stays in place after leaving.

Only one AI session can exist across the bot. Audio is combined across the
channel, with no speaker identification. Capture ends after about one second
of silence or 15 seconds. Incoming audio is ignored during processing/playback.
The session remembers ten completed conversation pairs in memory. Errors are
logged and listening resumes without a scripted spoken fallback.

Recordings and generated audio remain in memory except for short-lived FFmpeg
input/output files, deleted after conversion. Audio goes to OpenAI; generated
response text goes to ElevenLabs. Disclosure is posted before listening and
again when participants enter. Upstream service retention policies apply.

Build and test with `./gradlew test shadowJar` (`gradlew.bat` on Windows).
Run the `*-all.jar` from `build/libs` using the existing startup arguments:
`java -jar "build/libs/Mongoloid Bot-<version>-all.jar" <discord-token> noArduino`.
Do not commit credentials. `OPENAI_CHAT_MODEL` overrides the existing text summarizer's `OPENAI_MODEL`
setting; when absent, voice uses `OPENAI_MODEL` as a compatibility fallback.
Restart MongoBot after editing settings because the settings loader caches them.

Automated tests cover audio framing/WAV/VAD, native DAVE loading, and local HTTP
service contracts. Live Discord connections and paid API requests require your
credentials and must be tested separately. To smoke-test live: join, start,
speak from two accounts, confirm transcription/response/TTS/playback log events,
confirm an audible reply and a second turn, then stop/leave during a request.

Discord voice also needs outbound TCP to the host **and port** supplied by the
voice gateway (observed port 2053), plus bidirectional voice UDP through the
network firewall/NAT. Allowing only outbound HTTPS on port 443 is insufficient.
`~Max start` requires the audio connection status to be `CONNECTED`.
