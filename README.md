# Pixit

PiXiT is an online game inspired by a board game called Dixit. In PiXiT instead of using painted cards with dream-like
imagery, users play with artistic photographs (kindly provided by Unsplash). It is not affiliated
with the authors of the original Dixit

## Live

You can play the game at [pixit.fun](https://pixit.fun/)!

## Building

Dependencies:

- Redis
- Unsplash API

Copy `application.properties` into `application-dev.properties` and `application-prod.properties`, then fill the missing
values for the local/dev and prod environments.

Use `gradle build` to build, `gradle jar` (or `./gradlew bootJar`) to produce a JAR file.

Use `java -jar build/libs/pixit-0.0.2-SNAPSHOT.jar --spring.profiles.active=dev` to run locally in dev mode. Access
at http://localhost:8080/.

Use `java -jar build/libs/pixit-0.0.2-SNAPSHOT.jar --spring.profiles.active=prod` to run in production mode.