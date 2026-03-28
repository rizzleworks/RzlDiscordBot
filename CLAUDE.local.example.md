# Local Development

The local Paper server is at `../rizzleworks-server` — start with `./start.sh`.

Deploy the plugin locally:
```bash
./gradlew shadowJar && cp build/libs/discord-bot-1.0.0.jar <path-to-paper-server>/plugins/
```