# Redis

<img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/java/java-original.svg" height="32" width="32" alt="Java logo" /> <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/mongodb/mongodb-original.svg" height="32" width="32" alt="MongoDB logo" /> <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/redis/redis-original.svg" height="32" width="32" alt="Redis logo" /> <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/junit/junit-original.svg" height="32" width="32" alt="JUnit logo" /> <img src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/docker/docker-original.svg" height="32" width="32" alt="Docker logo" />

Aplikacja stworzona na zaliczenie zadania z nierelacyjnych baz danych.

### Stack

- Java 21
- MongoDB 8.2.1
- Redis 8.2.3
- JUnit 5, Mockito
- JMH (Java Microbenchmark Harness)
- Docker

### Uruchamianie aplikacji

1. Uruchomienie dockera:

    ```shell
    docker compose up
    ```
2. Uruchomienie aplikacji:

   Aby uruchomić aplikację, należy wykonać `mvn install`, a następnie wykonać plik `target/redis[...].jar`. Wtedy
   uruchomią się benchmarki.

Aplikacja pominie testy wymagające baz danych, gdy nie ma uruchomionego Dockera z MongoDB i Redisem.
