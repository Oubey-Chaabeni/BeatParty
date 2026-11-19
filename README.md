# Beat Party

**Drop in as a guest. Join a music club. Chat, suggest tracks, and vote on what plays next—live.**

Beat Party is a collaborative, real-time listening room application built with a **distributed Java backend** and a **React web interface**. There are no accounts or passwords: you pick a guest handle, jump into a live music club, and listen in sync with everyone else in the room.

---

## What Can You Do in Beat Party?

- **Zero-Friction Guest Entry:** Pick a random DJ handle and color ring—no sign-up required.
- **Browse or Launch Music Clubs:** Join themed rooms (Synthwave, Lo-Fi, Hip-Hop, House) or create your own club in seconds.
- **Live Voting Queue:** Search real songs (via the iTunes Search API), add them to the room's queue, and upvote or downvote tracks. The highest-voted song automatically moves to the top and plays next.
- **Synchronized Playback:** Everyone in the same club hears the exact same second of the song at the same time.
- **Live Club Chat & Slash Commands:** Chat in real time or type commands like `/suggest`, `/skip`, `/vibe`, and `/queue` directly in the chat box.

---

## How It Works Under the Hood (Plain-English Architecture)

Instead of putting everything into one giant program, Beat Party splits its workload across specialized backend services that talk to each other over the network. Here is what each piece does, assuming you have never used these technologies before:

```text
  [ React Browsers ]
          |
          |  HTTP & WebSockets (browser-friendly)
          v
  [ Web Gateway (Javalin) ]  <--- Translator between browsers and Java services
     |        ^         ^
     |        |         |
    RMI     JMS/JNDI   TCP Socket
     |        |         |
     v        |         v
[ RMI Server ]+   [ TCP Socket Server ]
(Clubs & Queue)   (Playback Timer & Chat)
     |
     +---> publishes events ---> [ ActiveMQ Broker ]
```

### 1. The Frontend (`frontend/` — React + Tailwind + iTunes API)
What you see and click in the browser. Because web browsers only speak web protocols (HTTP and WebSockets), React talks only to our **Web Gateway**. It also queries Apple's public iTunes Search API to fetch real album artwork and 30-second audio previews.

### 2. The Web Gateway (`web-gateway/` — Javalin)
Think of this as the **bilingual receptionist** at the front door. Browsers cannot speak directly to Java's internal networking tools (like RMI or JMS), so the Web Gateway receives button clicks from React, forwards them to the right Java backend service, and pushes live updates back out to every open browser tab via WebSockets.

### 3. The Brain: RMI Server (`rmi-server/` — Java RMI)
**What is Java RMI (Remote Method Invocation)?**
Normally, a Java program can only call functions inside itself. RMI lets one Java program call a function running on a *completely different server* over the network as if it were local.

**What it does in Beat Party:**
The RMI Server is the **Club & Queue Manager**. It holds the live list of clubs, stores each club's music queue in thread-safe memory, accepts track suggestions, recalculates track rankings when guests upvote/downvote, and tracks skip votes. Whenever the Web Gateway needs to read or change a club's state, it calls a remote method on the RMI Server.

### 4. The Megaphone: ActiveMQ + JMS + JNDI (`activemq` Docker)
**What are JMS and ActiveMQ?**
Imagine the RMI Server updates a club's queue when someone upvotes a song. How do all the connected browsers find out immediately without constantly asking *"Did anything change yet?"* every half-second?
- **JMS (Java Message Service)** is Java's standard way of sending asynchronous notifications between programs (Publish/Subscribe).
- **ActiveMQ** is the actual message-broker server (the post office) running in Docker that delivers those notifications.
When a vote happens, the RMI Server drops a quick event into a **JMS Topic** (like a radio channel). The Web Gateway subscribes to that channel and instantly forwards the update to React.

**What is JNDI (Java Naming and Directory Interface)?**
JNDI is a **phonebook** (`jndi.properties`). Instead of hardcoding ActiveMQ's exact server addresses and vendor classes inside our Java code, our code simply asks JNDI: *"Look up `ConnectionFactory` and `queueEvents` for me."* That keeps our Java code clean and decoupled from the broker.

### 5. The Heartbeat & Chat Line: TCP Socket Server (`socket-server/`)
**What is a TCP Socket?**
A socket is a direct, raw, two-way network tunnel between two computers—like keeping a phone call open instead of sending letters.

**What it does in Beat Party:**
Our multi-threaded TCP Socket Server handles the two things that need continuous, fast streaming:
1. **Playback Sync Clock:** Every second, it ticks the current timestamp for each club (`01:24 / 03:45`) so everyone stays synchronized. When a track finishes, it notifies the RMI Server to pop the top-voted song next.
2. **Live Chat & Slash Commands:** It receives chat messages, parses commands like `/skip` or `/vibe`, and broadcasts them to connected club listeners.

---

## Project Structure

```text
beat-party/
├── common/             # Shared Java data objects (DTOs) & RMI interface
├── rmi-server/         # Club & priority voting queue server + JMS publisher
├── socket-server/      # Multi-threaded TCP server for chat & time-sync
├── web-gateway/        # REST + WebSocket bridge for React + JMS subscriber
├── frontend/           # React + Vite + Tailwind UI
├── docker-compose.yml  # Starts the ActiveMQ JMS broker
└── pom.xml             # Root Maven multi-module configuration
```

---

## Prerequisites

- **Java JDK 17+**
- **Maven 3.8+**
- **Node.js 18+ & npm**
- **Docker & Docker Compose** (for ActiveMQ)

---

## How to Run Locally

### 1. Start the JMS Broker (ActiveMQ)
From the project root:
```bash
docker compose up -d
```
*(Optional: You can inspect live JMS topics in the ActiveMQ console at `http://localhost:8161` using `admin` / `admin`.)*

### 2. Build the Java Modules
```bash
mvn clean compile
```

### 3. Start the 3 Java Backend Services (in 3 separate terminals)
Start them in this order so the dependencies are ready:

**Terminal 1 — RMI Server (Port 1099):**
```bash
mvn -pl rmi-server exec:java
```

**Terminal 2 — TCP Socket Server (Port 9090):**
```bash
mvn -pl socket-server exec:java
```

**Terminal 3 — Web Gateway (Port 8080):**
```bash
mvn -pl web-gateway exec:java
```

### 4. Start the React Frontend (Port 5173)
**Terminal 4:**
```bash
cd frontend
npm install
npm run dev
```

Open **`http://localhost:5173`** in two browser windows side-by-side to test live multi-user sync, voting, and chat.

---

## Port Reference

| Service | Technology | Port |
| :--- | :--- | :--- |
| ActiveMQ Broker | JMS / OpenWire | `61616` |
| ActiveMQ Web Console | HTTP | `8161` |
| RMI Server | Java RMI Registry | `1099` |
| Socket Server | Raw TCP Sockets | `9090` |
| Web Gateway | Javalin (HTTP + WS) | `8080` |
| Frontend | React (Vite) | `5173` |

---

## Team & Responsibilities

- **Partner A (`@github-handle-1`):** Shared `common/` contracts, `rmi-server/` (clubs & priority voting queue), JNDI + ActiveMQ JMS event publishing, Docker setup.
- **Partner B (`@github-handle-2`):** Multi-threaded `socket-server/` (playback sync & slash-command chat), `web-gateway/` (Javalin REST/WebSockets + JMS subscriber), React UI & iTunes API integration.