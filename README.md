# Notification Service

A Spring Boot based notification service that stores notifications in PostgreSQL and uses Apache Kafka for asynchronous notification processing.

The service supports multiple notification channels such as **EMAIL, SMS, and PUSH**, Kafka-based asynchronous processing, retry handling, and a **Dead Letter Topic (DLT)** for messages that continue to fail after retries.

---

## Features

* Create and retrieve notifications using REST APIs
* Store notifications in PostgreSQL
* Kafka producer for asynchronous notification events
* Kafka consumer for processing notification events
* Multiple notification channels:

    * EMAIL
    * SMS
    * PUSH
* Notification categories
* Retry mechanism using Spring Kafka
* Configurable retry backoff
* Dead Letter Topic (DLT)
* DLT consumer for failed notifications
* Notification expiry / TTL
* Scheduled deletion of expired notifications
* Docker Compose setup for PostgreSQL and Kafka
* Environment variables for database credentials

---

## Tech Stack

* Java 24
* Spring Boot
* Spring Data JPA
* Spring Kafka
* Apache Kafka
* PostgreSQL
* Docker
* Docker Compose
* Maven

---

# Architecture

```text
                    REST Client
                       │
                       ▼
              ┌─────────────────┐
              │  Spring Boot    │
              │ Notification API│
              └────────┬────────┘
                       │
             ┌─────────┴─────────┐
             │                   │
             ▼                   ▼
        PostgreSQL          Kafka Producer
                                  │
                                  ▼
                         notification-events
                                  │
                                  ▼
                         Kafka Consumer
                                  │
                                  ▼
                     NotificationDispatcher
                           /    |    \
                          /     |     \
                       EMAIL   SMS    PUSH

                                  │
                              Failure
                                  │
                                  ▼
                         DefaultErrorHandler
                                  │
                         Retry × 3 attempts
                                  │
                                  ▼
                   DeadLetterPublishingRecoverer
                                  │
                                  ▼
                    notification-events-dlt
                                  │
                                  ▼
                       DLT Consumer
```

---

# Prerequisites

Install the following before running the project:

* Java 24
* Maven
* Docker Desktop
* Git
* IntelliJ IDEA / STS / VS Code
* Postman (recommended for API testing)

Verify Java:

```bash
java -version
```

Verify Maven:

```bash
mvn -version
```

Verify Docker:

```bash
docker --version
```

Verify Docker Compose:

```bash
docker compose version
```

---

# Project Setup

Clone the repository:

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
```

Move into the project:

```bash
cd notification-service
```

---

# Database Configuration

The application uses PostgreSQL.

PostgreSQL is started using Docker Compose.

The database configuration is:

```text
Database: notification_db
Port: 5432
Username: DB_USERNAME
Password: DB_PASSWORD
```

The username and password should not be committed to GitHub.

---

# Environment Variables

The Docker Compose file expects:

```text
DB_USERNAME
DB_PASSWORD
```

Set these environment variables before starting Docker Compose.

## Windows

For the current terminal session:

```powershell
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_password"
```

Or configure them in your IntelliJ/STS Run Configuration.

For example:

```text
DB_USERNAME=postgres
DB_PASSWORD=your_password
```

Do not put the actual password in `application.properties` or commit it to GitHub.

---

# Docker Compose

The project uses Docker Compose to run PostgreSQL and Kafka.

Example `docker-compose.yml`:

```yaml
services:

  postgres:
    image: postgres:latest
    container_name: notification-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: notification_db
      POSTGRES_USER: ${DB_USERNAME}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql

  kafka:
    image: apache/kafka:latest
    container_name: notification-kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://:9092,CONTROLLER://:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@localhost:9093
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1

volumes:
  postgres_data:
```

---

# Start Docker Services

After setting the environment variables:

```bash
docker compose up -d
```

Check running containers:

```bash
docker ps
```

You should see:

```text
notification-postgres
notification-kafka
```

---

# Stop Docker Services

To stop the containers:

```bash
docker compose down
```

This removes the containers but keeps the PostgreSQL named volume.

### Important

Do not use:

```bash
docker compose down -v
```

unless you intentionally want to delete the PostgreSQL Docker volume and its database data.

---

# Check PostgreSQL

Check the PostgreSQL container:

```bash
docker ps
```

You can connect to PostgreSQL using:

```text
localhost:5432
```

Database:

```text
notification_db
```

---

# Kafka Configuration

Kafka is running inside the Docker container:

```text
Container: notification-kafka
Kafka port: 9092
```

The Spring Boot application connects to:

```text
localhost:9092
```

The main Kafka topic is:

```text
notification-events
```

The consumer group is:

```text
notification-group
```

The Dead Letter Topic is:

```text
notification-events-dlt
```

---

# Create the Kafka Topic

The official Apache Kafka Docker image contains Kafka commands under:

```text
/opt/kafka/bin/
```

Create the main topic:

```bash
docker exec -it notification-kafka \
/opt/kafka/bin/kafka-topics.sh \
--create \
--topic notification-events \
--bootstrap-server localhost:9092 \
--partitions 3 \
--replication-factor 1
```

If the topic already exists, Kafka will report that it already exists.

---

# Create the Dead Letter Topic

The DLT must exist before the application tries to publish failed messages to it.

Create it using:

```bash
docker exec -it notification-kafka \
/opt/kafka/bin/kafka-topics.sh \
--create \
--topic notification-events-dlt \
--bootstrap-server localhost:9092 \
--partitions 3 \
--replication-factor 1
```

---

# List Kafka Topics

To see all Kafka topics:

```bash
docker exec -it notification-kafka \
/opt/kafka/bin/kafka-topics.sh \
--list \
--bootstrap-server localhost:9092
```

You should see:

```text
notification-events
notification-events-dlt
```

---

# Describe a Kafka Topic

Main topic:

```bash
docker exec -it notification-kafka \
/opt/kafka/bin/kafka-topics.sh \
--describe \
--topic notification-events \
--bootstrap-server localhost:9092
```

DLT:

```bash
docker exec -it notification-kafka \
/opt/kafka/bin/kafka-topics.sh \
--describe \
--topic notification-events-dlt \
--bootstrap-server localhost:9092
```

This shows information such as:

* Number of partitions
* Replication factor
* Partition leaders
* Replicas

---

# Spring Boot Kafka Configuration

The application uses Spring Kafka to consume messages from:

```text
notification-events
```

The consumer uses:

```text
notification-group
```

The basic listener looks like:

```java
@KafkaListener(
        topics = "notification-events",
        groupId = "notification-group"
)
public void consume(NotificationEvent notificationEvent) {

    System.out.println(
            "Received notification from Kafka for user: "
                    + notificationEvent.getUserId()
    );

    notificationDispatcher.dispatch(notificationEvent);
}
```

---

# Notification Dispatcher

The `NotificationDispatcher` determines which channel should be used.

```text
channel
   │
   ├── EMAIL
   ├── SMS
   └── PUSH
```

For example:

```java
switch (event.getChannel()) {

    case "EMAIL":
        sendEmail(event);
        break;

    case "SMS":
        sendSms(event);
        break;

    case "PUSH":
        sendPush(event);
        break;

    default:
        throw new IllegalArgumentException(
                "Unsupported notification Channel: "
                        + event.getChannel()
        );
}
```

An unsupported channel causes an exception.

For example:

```json
{
  "channel": "WHATSAPP"
}
```

will fail because `WHATSAPP` is not currently supported.

---

# Kafka Retry Configuration

The project uses Spring Kafka's `DefaultErrorHandler`.

Example:

```java
@Bean
public DefaultErrorHandler errorHandler(
        KafkaTemplate<Object, Object> kafkaTemplate) {

    DeadLetterPublishingRecoverer recoverer =
            new DeadLetterPublishingRecoverer(kafkaTemplate);

    FixedBackOff backOff = new FixedBackOff(
            2000L,
            3
    );

    return new DefaultErrorHandler(
            recoverer,
            backOff
    );
}
```

## How this works

The configuration:

```java
new FixedBackOff(2000L, 3)
```

means:

```text
Initial attempt
      ↓
   FAILURE
      ↓
Wait 2 seconds
      ↓
Retry
      ↓
   FAILURE
      ↓
Wait 2 seconds
      ↓
Retry
      ↓
   FAILURE
      ↓
Wait 2 seconds
      ↓
Retry
      ↓
   FAILURE
      ↓
DLT
```

The `DefaultErrorHandler` receives the exception thrown by the Kafka listener.

The application does not need to manually call the retry mechanism.

---

# Dead Letter Topic

The `DeadLetterPublishingRecoverer` is responsible for publishing a message that has exhausted its retries to the DLT.

```text
notification-events
        │
        ▼
     Consumer
        │
        ▼
     Failure
        │
        ▼
     Retry
      × 3
        │
        ▼
DeadLetterPublishingRecoverer
        │
        ▼
notification-events-dlt
```

The DLT allows failed messages to be retained instead of simply losing them.

---

# DLT Consumer

The application contains a separate consumer for:

```text
notification-events-dlt
```

Example:

```java
@KafkaListener(
        topics = "notification-events-dlt",
        groupId = "notification-dlt-group"
)
public void consume(NotificationEvent event) {

    System.out.println(
            "========== DLT MESSAGE =========="
    );

    System.out.println(
            "Notification ID: "
                    + event.getNotificationId()
    );

    System.out.println(
            "User ID: "
                    + event.getUserId()
    );

    System.out.println(
            "Message: "
                    + event.getMessage()
    );

    System.out.println(
            "Channel: "
                    + event.getChannel()
    );

    System.out.println(
            "Category: "
                    + event.getCategory()
    );

    System.out.println(
            "================================="
    );
}
```

The DLT consumer can later be extended to:

* Store failed notifications
* Alert an administrator
* Send monitoring alerts
* Reprocess failed notifications
* Record failure reasons

---

# How to Test Kafka + Retry + DLT

This is the easiest way to verify that the entire Kafka failure flow is working.

## Step 1: Start Docker

```bash
docker compose up -d
```

Check:

```bash
docker ps
```

Make sure:

```text
notification-postgres
notification-kafka
```

are running.

---

## Step 2: Make Sure the DLT Exists

Run:

```bash
docker exec -it notification-kafka \
/opt/kafka/bin/kafka-topics.sh \
--list \
--bootstrap-server localhost:9092
```

You should see:

```text
notification-events
notification-events-dlt
```

---

## Step 3: Start Spring Boot

Run the Spring Boot application from IntelliJ, STS, or Maven.

The application should connect to:

```text
PostgreSQL → localhost:5432
Kafka → localhost:9092
```

---

## Step 4: Send a Valid Notification

Example:

```json
{
  "userId": 1001,
  "message": "Email notification test",
  "channel": "EMAIL",
  "category": "PAYMENT",
  "read": false
}
```

The expected flow is:

```text
POST API
   ↓
PostgreSQL
   ↓
Kafka
   ↓
Consumer
   ↓
Dispatcher
   ↓
EMAIL sent successfully
```

Expected log:

```text
Notification sent to Kafka for user: 1001

Received notification from Kafka for user: 1001

EMAIL sent to user 1001: Email notification test
```

---

# Test Retry and DLT

To intentionally generate an error, send an unsupported channel.

For example:

```json
{
  "userId": 1001,
  "message": "Kafka DLT test",
  "channel": "WHATSAPP",
  "category": "PAYMENT",
  "read": false
}
```

Since `WHATSAPP` is not supported, the dispatcher throws:

```text
IllegalArgumentException
```

You should see something similar to:

```text
Received notification from Kafka for user: 1001
```

followed by:

```text
Record in retry and not yet recovered
```

The message will be retried approximately every 2 seconds.

After the configured retries are exhausted, the message is sent to:

```text
notification-events-dlt
```

---

# Verify the DLT Using Kafka Console Consumer

You can manually inspect the DLT.

Run:

```bash
docker exec -it notification-kafka \
/opt/kafka/bin/kafka-console-consumer.sh \
--bootstrap-server localhost:9092 \
--topic notification-events-dlt \
--from-beginning
```

You should see the failed notification.

Example:

```json
{
  "notificationId": 211,
  "userId": 1001,
  "message": "Kafka DLT test",
  "channel": "WHATSAPP",
  "category": "PAYMENT"
}
```

Press:

```text
Ctrl + C
```

to stop the console consumer.

---

# Verify the DLT Through the Spring Boot Application

If the DLT consumer is running, you should also see:

```text
========== DLT MESSAGE ==========
Notification ID: 211
User ID: 1001
Message: Kafka DLT test
Channel: WHATSAPP
Category: PAYMENT
=================================
```

This confirms that:

1. The Kafka producer sent the event.
2. The main Kafka consumer received it.
3. The dispatcher failed.
4. Spring Kafka detected the exception.
5. The message was retried.
6. Retries were exhausted.
7. `DeadLetterPublishingRecoverer` published the event to the DLT.
8. The DLT consumer received the failed event.

---

# Complete Failure Flow

```text
                  notification-events
                          │
                          ▼
                  Kafka Consumer
                          │
                          ▼
              NotificationDispatcher
                          │
                          ▼
                       FAILURE
                          │
                          ▼
                 DefaultErrorHandler
                          │
              ┌───────────┴───────────┐
              │                       │
          Retry #1                 Retry #2
              │                       │
              └───────────┬───────────┘
                          │
                       Retry #3
                          │
                       FAILURE
                          │
                          ▼
            DeadLetterPublishingRecoverer
                          │
                          ▼
               notification-events-dlt
                          │
                          ▼
               KafkaNotificationDltConsumer
```

---

# PostgreSQL Notification Expiry / TTL

Notifications also have an expiry time.

The application periodically checks for expired notifications using a scheduled job.

The database query looks for notifications where:

```text
expires_at < current time
```

Expired notifications are then deleted.

Example log:

```text
Deleted 5 expired notifications
```

If no notification has expired yet:

```text
Deleted 0 expired notifications
```

This is independent of Kafka.

Kafka handles asynchronous notification processing, while PostgreSQL handles notification persistence and expiry.

---

# Common Docker/Kafka Commands

## Check containers

```bash
docker ps
```

## Check Kafka logs

```bash
docker logs notification-kafka
```

## Check PostgreSQL logs

```bash
docker logs notification-postgres
```

## Follow Kafka logs

```bash
docker logs -f notification-kafka
```

## Stop containers

```bash
docker compose down
```

## Start containers again

```bash
docker compose up -d
```

---

# Important Docker Note

Do not run:

```bash
docker compose down -v
```

unless you intentionally want to remove the PostgreSQL volume.

Using:

```bash
docker compose down
```

keeps the PostgreSQL data stored in:

```text
postgres_data
```

---

# Kafka Topic Commands

Because the official Apache Kafka image stores Kafka CLI tools under `/opt/kafka/bin`, use:

```bash
/opt/kafka/bin/kafka-topics.sh
```

instead of:

```bash
kafka-topics.sh
```

For example:

```bash
docker exec -it notification-kafka \
/opt/kafka/bin/kafka-topics.sh \
--list \
--bootstrap-server localhost:9092
```

---

# Troubleshooting

## Kafka container is not running

Check:

```bash
docker ps
```

If it is not running:

```bash
docker compose up -d
```

Then check:

```bash
docker logs notification-kafka
```

---

## Kafka topic does not exist

List topics:

```bash
docker exec -it notification-kafka \
/opt/kafka/bin/kafka-topics.sh \
--list \
--bootstrap-server localhost:9092
```

Create the topic if necessary.

---

## DLT topic does not exist

Create:

```bash
docker exec -it notification-kafka \
/opt/kafka/bin/kafka-topics.sh \
--create \
--topic notification-events-dlt \
--bootstrap-server localhost:9092 \
--partitions 3 \
--replication-factor 1
```

---

## Application cannot connect to Kafka

Check that Kafka is running:

```bash
docker ps
```

The Spring Boot application should use:

```text
localhost:9092
```

Make sure port `9092` is exposed by Docker.

---

## Application cannot connect to PostgreSQL

Check:

```bash
docker ps
```

PostgreSQL should be running on:

```text
localhost:5432
```

Also verify:

```text
DB_USERNAME
DB_PASSWORD
```

---

# Project Structure

A simplified project structure:

```text
src/
└── main/
    └── java/
        └── com/example/
            ├── Controller/
            │
            ├── Entity/
            │
            ├── Repository/
            │
            ├── Service/
            │   └── NotificationDispatcher.java
            │
            ├── Kafka/
            │   ├── KafkaConsumerConfig.java
            │   ├── KafkaNotificationConsumer.java
            │   └── KafkaNotificationDltConsumer.java
            │
            └── Event/
                └── NotificationEvent.java
```

---

# Kafka Concepts Used

### Producer

Publishes notification events to Kafka.

```text
Spring Boot → Kafka
```

### Consumer

Reads notification events from Kafka.

```text
Kafka → Spring Boot
```

### Consumer Group

The main consumer uses:

```text
notification-group
```

The DLT consumer uses:

```text
notification-dlt-group
```

Consumer groups allow Kafka consumers to coordinate processing of partitions.

### Retry

Failed messages are retried using:

```text
DefaultErrorHandler
```

with:

```text
2 second delay
3 retries
```

### DLT

A Dead Letter Topic stores messages that could not be successfully processed after retries.

```text
notification-events-dlt
```

### DeadLetterPublishingRecoverer

Publishes failed records to the DLT after retry attempts are exhausted.

---

# Future Improvements

Possible future enhancements include:

* Idempotent Kafka consumers
* Better exception classification
* Immediate DLT for non-retryable exceptions
* DLT message reprocessing
* Real email integration
* Real SMS integration
* Push notification integration
* Authentication and authorization
* API documentation using Swagger/OpenAPI
* Metrics and monitoring
* Structured logging
* Integration tests using Testcontainers
* Kafka producer/consumer monitoring
* Pagination and filtering for notification APIs

---

# Summary

This project demonstrates an event-driven notification system using Spring Boot, PostgreSQL, Kafka, Docker, retry handling, and Dead Letter Topics.

The main notification flow is:

```text
REST API
   ↓
PostgreSQL
   ↓
Kafka Producer
   ↓
notification-events
   ↓
Kafka Consumer
   ↓
Notification Dispatcher
   ↓
EMAIL / SMS / PUSH
```

The failure flow is:

```text
Consumer Failure
      ↓
Retry
      ↓
Retry
      ↓
Retry
      ↓
Dead Letter Publishing Recoverer
      ↓
notification-events-dlt
      ↓
DLT Consumer
```

This provides asynchronous processing and failure handling while keeping failed notifications available for inspection and future recovery.
