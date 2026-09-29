# Kafka 3-Broker Cluster Setup with Docker

A step-by-step guide to running a 3-node KRaft Kafka cluster in Docker, working with topics from the command line, and connecting from a Java project.

## Table of Contents

- [Kafka 3-Broker Cluster Setup with Docker](#kafka-3-broker-cluster-setup-with-docker)
  - [Table of Contents](#table-of-contents)
  - [1. Create the Docker Network](#1-create-the-docker-network)
  - [2. Create the Brokers](#2-create-the-brokers)
    - [Broker 1](#broker-1)
    - [Broker 2](#broker-2)
    - [Broker 3](#broker-3)
    - [Broker summary](#broker-summary)
  - [3. Verify the Containers](#3-verify-the-containers)
  - [4. Topic Management](#4-topic-management)
    - [Create a Topic](#create-a-topic)
    - [Describe a Topic](#describe-a-topic)
    - [List Topics](#list-topics)
    - [Delete a Topic](#delete-a-topic)
  - [5. Console Producer and Consumer](#5-console-producer-and-consumer)
    - [Console Producer](#console-producer)
    - [Console Consumer](#console-consumer)
  - [6. Java Project](#6-java-project)
    - [6.1 Clone below git repo within Linux VM:](#61-clone-below-git-repo-within-linux-vm)
    - [6.2 Once cloned go within the java project as follows:](#62-once-cloned-go-within-the-java-project-as-follows)
    - [6.3 Install Maven if not already installed and verify](#63-install-maven-if-not-already-installed-and-verify)
    - [6.4 Open another shell and run the consumer if not running already and keep this shell running:](#64-open-another-shell-and-run-the-consumer-if-not-running-already-and-keep-this-shell-running)
    - [6.5 Compile and execute java class:](#65-compile-and-execute-java-class)
  - [7. Optional: Passwordless Access from Windows to a Linux VM](#7-optional-passwordless-access-from-windows-to-a-linux-vm)
    - [Step 1: Generate an SSH key pair](#step-1-generate-an-ssh-key-pair)
    - [Step 2: Understand the two files](#step-2-understand-the-two-files)
    - [Step 3: Copy the public key to Linux](#step-3-copy-the-public-key-to-linux)
    - [Step 4: Test passwordless SSH](#step-4-test-passwordless-ssh)
    - [Step 5: Run Kafka commands remotely](#step-5-run-kafka-commands-remotely)

---

## 1. Create the Docker Network

```bash
docker network create kafka-net
```

---

## 2. Create the Brokers

> [!IMPORTANT]
> Replace `<LINUX_VM_IP>` with your Linux VM's IP address in every broker command below.

### Broker 1

```bash
docker run -d \
  --name kafka-1 \
  --hostname kafka-1 \
  --network kafka-net \
  -p 29092:9092 \
  -e KAFKA_NODE_ID=1 \
  -e KAFKA_PROCESS_ROLES=broker,controller \
  -e KAFKA_LISTENERS='PLAINTEXT://:19092,CONTROLLER://:9093,PLAINTEXT_HOST://:9092' \
  -e KAFKA_ADVERTISED_LISTENERS='PLAINTEXT://kafka-1:19092,PLAINTEXT_HOST://<LINUX_VM_IP>:29092' \
  -e KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT \
  -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP='CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT' \
  -e KAFKA_CONTROLLER_QUORUM_VOTERS='1@kafka-1:9093,2@kafka-2:9093,3@kafka-3:9093' \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=3 \
  -e KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=3 \
  -e KAFKA_TRANSACTION_STATE_LOG_MIN_ISR=2 \
  -e CLUSTER_ID=4L6g3nShT-eMCtK--X86sw \
  apache/kafka:latest
```

### Broker 2

```bash
docker run -d \
  --name kafka-2 \
  --hostname kafka-2 \
  --network kafka-net \
  -p 39092:9092 \
  -e KAFKA_NODE_ID=2 \
  -e KAFKA_PROCESS_ROLES=broker,controller \
  -e KAFKA_LISTENERS='PLAINTEXT://:19092,CONTROLLER://:9093,PLAINTEXT_HOST://:9092' \
  -e KAFKA_ADVERTISED_LISTENERS='PLAINTEXT://kafka-2:19092,PLAINTEXT_HOST://<LINUX_VM_IP>:39092' \
  -e KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT \
  -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP='CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT' \
  -e KAFKA_CONTROLLER_QUORUM_VOTERS='1@kafka-1:9093,2@kafka-2:9093,3@kafka-3:9093' \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=3 \
  -e KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=3 \
  -e KAFKA_TRANSACTION_STATE_LOG_MIN_ISR=2 \
  -e CLUSTER_ID=4L6g3nShT-eMCtK--X86sw \
  apache/kafka:latest
```

### Broker 3

```bash
docker run -d \
  --name kafka-3 \
  --hostname kafka-3 \
  --network kafka-net \
  -p 49092:9092 \
  -e KAFKA_NODE_ID=3 \
  -e KAFKA_PROCESS_ROLES=broker,controller \
  -e KAFKA_LISTENERS='PLAINTEXT://:19092,CONTROLLER://:9093,PLAINTEXT_HOST://:9092' \
  -e KAFKA_ADVERTISED_LISTENERS='PLAINTEXT://kafka-3:19092,PLAINTEXT_HOST://<LINUX_VM_IP>:49092' \
  -e KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT \
  -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP='CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT' \
  -e KAFKA_CONTROLLER_QUORUM_VOTERS='1@kafka-1:9093,2@kafka-2:9093,3@kafka-3:9093' \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=3 \
  -e KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=3 \
  -e KAFKA_TRANSACTION_STATE_LOG_MIN_ISR=2 \
  -e CLUSTER_ID=4L6g3nShT-eMCtK--X86sw \
  apache/kafka:latest
```

### Broker summary

| Broker | Container | Node ID | Host port | Internal listener | Advertised external listener |
|--------|-----------|---------|-----------|-------------------|------------------------------|
| 1 | `kafka-1` | 1 | `29092` | `kafka-1:19092` | `<LINUX_VM_IP>:29092` |
| 2 | `kafka-2` | 2 | `39092` | `kafka-2:19092` | `<LINUX_VM_IP>:39092` |
| 3 | `kafka-3` | 3 | `49092` | `kafka-3:19092` | `<LINUX_VM_IP>:49092` |

---

## 3. Verify the Containers

```bash
docker ps
```

You should see `kafka-1`, `kafka-2` and `kafka-3` running.

---

## 4. Topic Management

### Create a Topic

```bash
docker exec kafka-1 /opt/kafka/bin/kafka-topics.sh \
  --create \
  --topic dummy \
  --bootstrap-server kafka-1:19092,kafka-2:19092,kafka-3:19092 \
  --partitions 5 \
  --replication-factor 3
```

### Describe a Topic

```bash
docker exec kafka-1 /opt/kafka/bin/kafka-topics.sh \
  --describe \
  --topic dummy \
  --bootstrap-server kafka-1:19092
```

### List Topics

```bash
docker exec kafka-1 /opt/kafka/bin/kafka-topics.sh \
  --list \
  --bootstrap-server kafka-1:19092
```

### Delete a Topic

```bash
docker exec kafka-1 /opt/kafka/bin/kafka-topics.sh \
  --delete \
  --topic dummy \
  --bootstrap-server kafka-1:19092
```

---

## 5. Console Producer and Consumer

### Console Producer

> [!TIP]
> Add `-i` so stdin is attached. Without it you can't type messages.

```bash
docker exec -i kafka-1 /opt/kafka/bin/kafka-console-producer.sh \
  --topic dummy \
  --bootstrap-server kafka-1:19092
```

### Console Consumer

```bash
docker exec kafka-1 /opt/kafka/bin/kafka-console-consumer.sh \
  --topic dummy \
  --from-beginning \
  --bootstrap-server kafka-1:19092
```

---

## 6. Java Project

### 6.1 Clone below git repo within Linux VM:
```
git clone  https://github.com/shalini06mittal/Sep2026_Labs.git
```

### 6.2 Once cloned go within the java project as follows:
```
cd Sep2026_Labs/Sprint7_Kafka/FirstKafkaDemo/

```
### 6.3 Install Maven if not already installed and verify
```
sudo dnf install maven -y
mvn --version
```

### 6.4 Open another shell and run the consumer if not running already and keep this shell running:
```
docker exec kafka-1 /opt/kafka/bin/kafka-console-consumer.sh \
  --topic dummy \
  --from-beginning \
  --bootstrap-server kafka-1:19092
```

### 6.5 Compile and execute java class:
```
mvn compile
mvn -q exec:java "-Dexec.mainClass=com.demo.AppProducer"
```

---

## 7. Optional: Passwordless Access from Windows to a Linux VM

Follow these steps only if you need to access Docker running inside the Linux VM from a Windows VM without entering the Linux password each time.

### Step 1: Generate an SSH key pair

Run this in Windows PowerShell:

```powershell
ssh-keygen
```

Press Enter through the prompts to accept the default location:

```text
C:\Users\<WindowsUser>\.ssh\id_ed25519
C:\Users\<WindowsUser>\.ssh\id_ed25519.pub
```

### Step 2: Understand the two files

| File | Type | Notes |
|------|------|-------|
| `id_ed25519` | Private key | Keep this secret |
| `id_ed25519.pub` | Public key | Safe to copy to other machines |

### Step 3: Copy the public key to Linux

Replace `<linux-username>@<linux-vm-ip>` with your Linux username and IP address.

```powershell
type $env:USERPROFILE\.ssh\id_ed25519.pub | ssh <linux-username>@<linux-vm-ip> "mkdir -p ~/.ssh && cat >> ~/.ssh/authorized_keys"
```

Enter your Linux password once when prompted.

### Step 4: Test passwordless SSH

From Windows PowerShell:

```powershell
ssh <linux-username>@<linux-vm-ip>
```

If it connects without asking for a password, you're set. You can now access the Linux VM using your key.

### Step 5: Run Kafka commands remotely

Once the brokers are created, run any Kafka command through SSH. Replace `<linux-username>@<linux-vm-ip>` as before. You can also save commands in a `.bat` file and run them as a Windows batch script.

```powershell
ssh <linux-username>@<linux-vm-ip> "docker exec kafka /opt/kafka/bin/kafka-topics.sh --create --topic trade-events --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1"
```
